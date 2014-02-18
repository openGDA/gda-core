/*-
 * Copyright © 2009 Diamond Light Source Ltd.
 *
 * This file is part of GDA.
 *
 * GDA is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License version 3 as published by the Free
 * Software Foundation.
 *
 * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along
 * with GDA. If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.gda.server.ncd.detectorsystem;

import gda.configuration.properties.LocalProperties;
import gda.data.nexus.tree.NexusTreeProvider;
import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.Timer;
import gda.device.detector.DetectorBase;
import gda.device.detector.NXDetectorData;
import gda.device.scannable.PositionCallableProvider;
import gda.factory.FactoryException;
import gda.observable.IObserver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.server.ncd.beans.CalibLabel;
import uk.ac.gda.server.ncd.beans.CalibrationLabels;
import uk.ac.gda.server.ncd.subdetector.IHaveExtraNames;
import uk.ac.gda.server.ncd.subdetector.INcdSubDetector;

/**
 * Detector system of non crystalline diffraction to allow scans to take time series at each point.
 */
public class NcdDetectorSystem extends DetectorBase implements NcdDetector, PositionCallableProvider<NexusTreeProvider> {

	private static final Logger logger = LoggerFactory.getLogger(NcdDetectorSystem.class);

	public static final String SAXS_DETECTOR = "SAXS";
	public static final String WAXS_DETECTOR = "WAXS";
	public static final String CALIBRATION_DETECTOR = "CALIB";
	public static final String FLUORESCENCE_DETECTOR = "FLUO";
	public static final String OTHER_DETECTOR = "OTHER";
	public static final String TIMES_DETECTOR = "TIMES";
	public static final String SYSTEM_DETECTOR = "SYS";
	public static final String REDUCTION_DETECTOR = "REDUCTION";

	/**
	 * array of valid/known detector types
	 */
	public static final String[] detectorTypes = new String[] { SAXS_DETECTOR, WAXS_DETECTOR, CALIBRATION_DETECTOR,
			FLUORESCENCE_DETECTOR, OTHER_DETECTOR, TIMES_DETECTOR, SYSTEM_DETECTOR, REDUCTION_DETECTOR };
	public static final String[] physicalDetectors = new String[] { SAXS_DETECTOR, WAXS_DETECTOR, CALIBRATION_DETECTOR,
			FLUORESCENCE_DETECTOR, OTHER_DETECTOR, TIMES_DETECTOR };
	/**
	 * Map of names to configured ncd sub detectors
	 */
	protected Collection<INcdSubDetector> subDetectors = new ArrayList<INcdSubDetector>();

	private Timer timer;

	private INcdSubDetector calibDetector = null;
	private String calLabelFileName = LocalProperties.getVarDir() + "caliblabels.xml";
	private CalibrationLabels calibLabels = null;

	@Override
	public void configure() throws FactoryException {
		for (INcdSubDetector det : subDetectors) {
			try {
				if (det != null && det.getDetectorType().equals(CALIBRATION_DETECTOR)) {
					calibDetector = det;
					loadCalibrationLabels();
					configCalibLabels();
				}
			} catch (DeviceException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void collectData() throws DeviceException {
		clear();
		start();
	}

	@Override
	public int getStatus() throws DeviceException {
		if (timer.getStatus() == Timer.IDLE)
			return Detector.IDLE;
		return Detector.BUSY;
	}

	@Override
	public Callable<NexusTreeProvider> getPositionCallable() throws DeviceException {
		final int frames = getNumberOfFrames();
		final NXDetectorData detectorData = readoutPhysicalDetectors(frames);
		Callable<NexusTreeProvider> callable = new Callable<NexusTreeProvider>() {
			@Override
			public NexusTreeProvider call() throws Exception {
				postProcess(frames, detectorData);
				return detectorData;
			}
		};
		return callable;
	}

	@Override
	public synchronized NexusTreeProvider readout() throws DeviceException {
		Callable<NexusTreeProvider> positionCallable = getPositionCallable();

		try {
			NexusTreeProvider treeProvider = positionCallable.call();
			return treeProvider;
		} catch (DeviceException e) {
			throw e;
		} catch (Exception e) {
			throw new DeviceException("something wrong in the callable", e);
		}
	}

	private void postProcess(int frames, NXDetectorData nxdata) throws DeviceException {
		for (INcdSubDetector det : subDetectors) {
			if ("REDUCTION".equalsIgnoreCase(det.getDetectorType())) {
				det.writeout(frames, nxdata);
			}
		}
	}

	private synchronized NXDetectorData readoutPhysicalDetectors(int frames) throws DeviceException {
		NXDetectorData nxdata = new NXDetectorData(this);
		if (frames == 0) {
			throw new DeviceException("trying to read out 0 frames");
		}
		
		nxdata.setPlottableValue(getName(), (double) frames);

		logger.debug("starting to read physical detectors");
		for (String detectorType : physicalDetectors) {
			for (INcdSubDetector det : subDetectors) {
				if (detectorType.equalsIgnoreCase(det.getDetectorType())) {
					det.writeout(frames, nxdata);
				}
			}
		}
		logger.debug("done reading physical detectors");
		return nxdata;
	}

	@Override
	public boolean createsOwnFiles() throws DeviceException {
		return false;
	}

	@Override
	public synchronized void clear() throws DeviceException {
		logger.debug("being cleared");
		for (INcdSubDetector det : subDetectors) {
			// This is a kludge for detectors which require partial clearing due to excessive
			// clear times.
			det.setAttribute("TotalFrames", getNumberOfFrames());
			det.clear();
		}
	}

	@Override
	public void start() throws DeviceException {
		for (INcdSubDetector det : subDetectors) {
			if (det != null) {
				det.start();
			}
		}
		timer.start();
	}

	@Override
	public void stop() throws DeviceException {
		timer.stop();
		for (INcdSubDetector det : subDetectors) {
			if (det != null) {
				det.stop();
			}
		}
	}

	/**
	 * @return number of collected frames
	 * @throws DeviceException
	 */
	@Override
	public int getNumberOfFrames() throws DeviceException {
		return (Integer) timer.getAttribute("TotalFrames");
	}

	@Override
	public String getTfgName() throws DeviceException {
		return timer.getName();
	}

	@Override
	public void close() throws DeviceException {
		for (INcdSubDetector det : subDetectors) {
			if (det != null) {
				det.close();
			}
		}
	}

	@Override
	public void addIObserver(IObserver observer) {
		timer.addIObserver(observer);
		// TODO this used to have bad effects, need to check again
		super.addIObserver(observer);
	}

	@Override
	public void deleteIObserver(IObserver observer) {
		timer.deleteIObserver(observer);
		super.deleteIObserver(observer);
	}

	@Override
	public String getDescription() throws DeviceException {
		return "NCD Detector System for GDA NeXus writing";
	}

	@Override
	public String getDetectorID() throws DeviceException {
		return "unknown";
	}

	@Override
	public String getDetectorType() throws DeviceException {
		return "SYS";
	}

	@Override
	public double getCollectionTime() {
		try {
			if (timer != null) {
				return ((Number) timer.getAttribute("TotalExptTime")).doubleValue() / 1000;
			}
		} catch (DeviceException de) {
			logger.error("talking to tfg", de);
		}
		return 0;
	}

	@Override
	public void setCollectionTime(double collectionTime) {
		logger.info("reconfiguring Tfg, setting collection time to :" + collectionTime + " seconds.");
		try {
			if (timer != null) {
				timer.clearFrameSets();
				timer.addFrameSet(1, 1, collectionTime * 1000);
				timer.loadFrameSets();
			}
		} catch (DeviceException de) {
			logger.error("talking to tfg", de);
		}
	}

	@Override
	public void addDetector(INcdSubDetector det) throws DeviceException {
		if (det != null) {
			logger.debug("adding " + det.getName());
			det.setTimer(timer);
			subDetectors.add(det);
		}
	}

	@Override
	public void removeDetector(INcdSubDetector det) {
		logger.debug("removing " + det.getName());
		try {
			det.setTimer(null);
		} catch (DeviceException e) {
			// ignore
		}
		subDetectors.remove(det);
	}

	public Timer getTimer() {
		return timer;
	}

	public void setTimer(Timer timer) {
		this.timer = timer;
		for (INcdSubDetector det : subDetectors) {
			try {
				det.setTimer(timer);
			} catch (DeviceException e) {
				logger.error("could not set timer on detector " + det.getName(), e);
			}
		}
	}

	@Override
	public String[] getExtraNames() {
		ArrayList<String> labels = new ArrayList<String>();
		labels.add(getName());
		for (INcdSubDetector  det : subDetectors) {
				if (det instanceof IHaveExtraNames) {
					labels.addAll(Arrays.asList(((IHaveExtraNames) det).getExtraNames()));
				}
		}
		return labels.toArray(new String[labels.size()]);
	}

	@Override
	public String[] getInputNames() {
		return new String[] {};
	}

	public Collection<INcdSubDetector> getDetectors() {
		return subDetectors;
	}

	public void setDetectors(Collection<INcdSubDetector> dets) {
		subDetectors = dets;
		setTimer(timer);
	}

	@Override
	public void atScanStart() throws DeviceException {
		for (INcdSubDetector det : subDetectors) {
			det.atScanStart();
		}
	}

	@Override
	public void atScanEnd() throws DeviceException {
		for (INcdSubDetector det : subDetectors) {
			det.atScanEnd();
		}
	}

	@Override
	public void atCommandFailure() throws DeviceException {
		atScanEnd();
	}
	
	@Override
	public Object getAttribute(String attributeName) throws DeviceException {
		if ("CalibrationLabels".equalsIgnoreCase(attributeName))
			return calibLabels;
		return super.getAttribute(attributeName);
	}

	@Override
	public void setAttribute(String attributeName, Object value) throws DeviceException {
		if ("CalibrationLabels".equalsIgnoreCase(attributeName)) {
			calibLabels = (CalibrationLabels) value;
			configCalibLabels();
			saveCalibrationLabels();
			this.notifyIObservers(this, calibLabels);
		}
		super.setAttribute(attributeName, value);
	}

	private void loadCalibrationLabels() {
		try {
			calibLabels = CalibrationLabels.createFromXML(calLabelFileName);
			logger.info("Loaded calibration label parameter file {}", calLabelFileName);
		} catch (Exception e1) {
			logger.info("could not load calibration label file {} {} -- creating new one", calLabelFileName, e1);
			calibLabels = new CalibrationLabels();

			int channels = 9;
			try {
				int[] dim = calibDetector.getDataDimensions();
				channels = dim[0] * dim[1];
			} catch (DeviceException e) {

			}
			for (int i = 0; i < channels; i++) {
				calibLabels.addCalibLabel(new CalibLabel(String.format("%d", i), ""));
			}
			saveCalibrationLabels();

		}
		calibLabels.getCalibrationLabels().set(0, new CalibLabel("0", "Timer (10 ns)"));
	}

	private void saveCalibrationLabels() {
		try {
			CalibrationLabels.writeToXML(calibLabels, calLabelFileName);
			calibLabels.getCalibrationLabels().set(0, new CalibLabel("0", "Timer (10 ns)"));
		} catch (Exception e1) {
			logger.warn("Error saving parameter file {}", calLabelFileName);
		}
	}

	public CalibrationLabels getCalibrationLabels() {
		return calibLabels;
	}

	public void configCalibLabels() {
		try {
			if (calibDetector != null) {
				String description = "";
				for (CalibLabel label : calibLabels.getCalibrationLabels()) {
					description += label.getSource();
					description += "\r\n";
				}
				calibDetector.setAttribute("description", description);
			}
		} catch (DeviceException e1) {
			logger.error("could not set ExtraHeaders: ", e1.getMessage());
		}
	}
}