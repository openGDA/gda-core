/*-
 * Copyright © 2011-2013 Diamond Light Source Ltd.
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

package uk.ac.gda.server.ncd.subdetector;

import java.util.List;

import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import gda.data.NumTracker;
import gda.data.fileregistrar.FileRegistrarHelper;
import gda.data.metadata.GDAMetadataProvider;
import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.Timer;
import gda.device.detector.NXDetectorData;
import gda.device.detector.nxdata.NXDetectorDataAppender;
import gda.device.detector.nxdetector.NXPlugin;
import gda.device.timer.FrameSet;
import gda.factory.FactoryException;
import gda.jython.InterfaceProvider;
import gda.observable.IObserver;
import gda.scan.ScanInformation;
import uk.ac.gda.api.remoting.ServiceInterface;
import uk.ac.gda.server.ncd.pilatus.PilatusADController;

/**
 * the update method has some device specific code to flag errors when triggering (gating) is attempted
 * outside the detector's capabilities
 */
@ServiceInterface(INcdSubDetector.class)
public class NcdPilatusAD extends NcdSubDetector implements InitializingBean, IObserver, LastImageProvider {

	// Setup the logging facilities
	private static final Logger logger = LoggerFactory.getLogger(NcdPilatusAD.class);
	protected PilatusADController controller;
	private Timer timer;
	protected DeviceException tfgMisconfigurationException = new DeviceException("Triggering not set up");
	private String filename = "/dev/null";
	private List<NXPlugin> nxplugins;

	@Override
	public void clear() throws DeviceException {
		// we are clear
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (controller == null) {
			throw new IllegalArgumentException(getName() + " - controller needs to be set");
		}
	}

	@Override
	public void configure() throws FactoryException {
		if (isConfigured()) {
			return;
		}
		try {
			controller.configure();
			controller.setImageMode(1); // multiple image
			controller.setTriggerMode(1); // ext enable
			controller.setDelay(0);
		} catch (Exception e) {
			throw new FactoryException(getName() + " - error setting up area detector", e);
		}
		setConfigured(true);
	}

	/**
	 * @return Returns the controller.
	 */
	public PilatusADController getController() {
		return controller;
	}

	@Override
	public int[] getDataDimensions() throws DeviceException {
		try {
			return new int[] { controller.getAreaDetector().getArraySizeY_RBV(),
					controller.getAreaDetector().getArraySizeX_RBV() };
		} catch (Exception e) {
			throw new DeviceException(getName() + " - failed to get image dimension", e);
		}
	}

	public int getStatus() throws DeviceException {
		int state = -1;
		try {
			// check detector acquire control
			state = controller.getAcquireState();
			if (state == 0) {
				state = Detector.IDLE;
			} else if (state == 1) {
				state = Detector.BUSY;
			} else if (state == 6) {
				state = Detector.FAULT;
			}
			return state;

		} catch (Exception e) {
			throw new DeviceException(getName() + " - Failure to read status", e);
		}
	}

	public void resetAll() throws Exception {
		controller.resetAll();
	}

	/**
	 * @param controller
	 *            The controller to set.
	 */
	public void setController(PilatusADController controller) {
		this.controller = controller;
	}

	@Override
	public void start() throws DeviceException {
		if (tfgMisconfigurationException != null)
			throw tfgMisconfigurationException;

		try {
			int state = getStatus();
			if (state != Detector.IDLE) {
				throw new DeviceException(String.format("%s - detector must be IDLE, but current state is %d", getName(), state));
			}
			controller.acquire();

			int totalmillis = 4000;
			int grain = 25;
			for (int i = 0; i < totalmillis / grain; i++) {
				if (controller.isArmed()) {
					logger.debug("{} - Was armed after {} ms.", getName(), i * grain);
					return;
				}
				Thread.sleep(grain);
			}

			throw new DeviceException(getName() + " - Timeout waiting for triggers to be armed.");
		} catch (DeviceException de) {
			// if proper type just rethrow
			throw de;
		} catch (Exception e) {
			throw new DeviceException(getName() + " - error starting acquire", e);
		}
	}

	@Override
	public void stop() throws DeviceException {
		try {
			controller.stop();
		} catch (Exception e) {
			throw new DeviceException(getName() + " - Cannot stop detector", e);
		}
	}

	@Override
	public void setTimer(Timer timer) {
		if (timer == this.timer) {
			return;
		}
		if (this.timer != null) {
			this.timer.deleteIObserver(this);
		}
		this.timer = timer;
		if (this.timer != null) {
			this.timer.addIObserver(this);
		}
	}

	@Override
	public void update(Object source, Object arg) throws RuntimeException {
		if (!(arg instanceof List<?>)) {
			return;
		}

		double shortestFrame = Double.MAX_VALUE;
		double shortestWait = Double.MAX_VALUE;
		double longestFrame = 0;
		double longestWait = 0;
		int framecount = 0;

		@SuppressWarnings("unchecked")
		List<FrameSet> l = (List<FrameSet>) arg;
		for (FrameSet f : l) {
			framecount += f.frameCount;
			if (shortestFrame > f.requestedLiveTime) {
				shortestFrame = f.requestedLiveTime;
			}
			if (shortestWait > f.requestedDeadTime) {
				shortestWait = f.requestedDeadTime;
			}
			if (longestFrame < f.requestedLiveTime) {
				longestFrame = f.requestedLiveTime;
			}
			if (longestWait < f.requestedDeadTime) {
				longestWait = f.requestedDeadTime;
			}
		}

		// convert to seconds
		shortestFrame = shortestFrame / 1000;
		shortestWait = shortestWait / 1000;
		longestFrame = longestFrame / 1000;
		longestWait = longestWait / 1000;

		tfgMisconfigurationException = checkTiming(shortestFrame, longestFrame, shortestWait, longestWait);
		if (tfgMisconfigurationException != null)
			return;

		try {
			controller.setExposures(1);
			controller.setNumImages(framecount);
			controller.setAcquirePeriod(shortestFrame + shortestWait);
			controller.setAcquireTime(shortestFrame);
			controller.setReadTimeout(longestFrame + longestWait + 2000.0); // Wuge and Dora trigger on water bath reaching temperature
		} catch (Exception e) {
			logger.error("error setting up acqusition on area detector for " + getName(), e);
		}
	}

	@SuppressWarnings("unused")
	protected DeviceException checkTiming(double shortestFrame, double longestFrame, double shortestWait, double longestWait) {

		if (shortestWait < 0.00095) {
			return new DeviceException("Readout time too short for " + getName()
					+ ". Need more than 0.95ms.");
		}
		if ((shortestWait + shortestFrame) < 0.004) {
			return new DeviceException("Cycle time too short for " + getName()
					+ ". Need more than 4ms live and dead time combined.");
		}

		return null;
	}

	public void setThresholdkeV(double d) throws Exception {
		controller.setThresholdkeV(d);
	}

	public double getThresholdkeV() throws Exception {
		return controller.getThresholdkeV();
	}

	@Override
	public Dataset readLastImage() throws DeviceException {
		try {
			float[] data;
			data = controller.getCurrentArray();
			int[] dims = getDataDimensions();
			Dataset ds = DatasetFactory.createFromObject(data, dims);
			return ds;
		} catch (Exception e) {
			throw new DeviceException(getName() + " - error reading last array", e);
		}
	}

	private void setupFilename() throws Exception {
		String beamline = null;
		try {
			beamline = GDAMetadataProvider.getInstance().getMetadataValue("instrument", "gda.instrument", null);
		} catch (DeviceException e1) {
		}

		// If the beamline name isn't set then default to 'base'.
		if (beamline == null) {
			beamline = "base";
		}

		// Check to see if the data directory has been defined.
		String dataDir = InterfaceProvider.getPathConstructor().createFromDefaultProperty();
		// Now lets try and setup the NumTracker...
		NumTracker runNumber = new NumTracker(beamline);
		// Get the current number
		Number scanNumber = runNumber.getCurrentFileNumber();

		filename = String.format("%s/%s-%d-%s.h5", dataDir, beamline, scanNumber, getName());
		controller.setAbsoluteFilename(filename);
	}

	/**
	 * we need to initiate the file saving in here in order for that to be in line with the scan
	 */
	@Override
	public void atScanStart() throws DeviceException {
		logger.debug("{} - scan start", getName());
		if (tfgMisconfigurationException != null)
			throw tfgMisconfigurationException;

		ScanInformation scanInformation = InterfaceProvider.getCurrentScanInformationHolder()
				.getCurrentScanInformation();
		try {
			if (nxplugins != null) {
				for (NXPlugin nxpi: nxplugins) {
					nxpi.stop();
					nxpi.prepareForCollection(controller.getNumImages(), scanInformation);
					nxpi.prepareForLine();
				}
			}
		} catch (Exception e) {
			throw new DeviceException(getName() + " - error setting up nxplugins", e);
		}
		try {
			controller.setScanDimensions(scanInformation.getDimensions());
			setupFilename();
			controller.resetCounters();
			controller.startRecording();
		} catch (Exception e) {
			throw new DeviceException(getName() + " - error setting up data recording to HDF5", e);
		}
	}

	@Override
	public void atScanEnd() throws DeviceException {
		logger.debug("{} - scan end", getName());
		try {
			controller.stopAcquiringWithTimeout();
			controller.endRecording();
			if (nxplugins != null) {
				for (NXPlugin nxpi: nxplugins) {
					nxpi.stop();
					nxpi.completeCollection();
				}
			}
		} catch (Exception e) {
			throw new DeviceException(getName() + " - error finalising data acquitision/writing", e);
		} finally {
			try {
				FileRegistrarHelper.registerFile(filename);
			} catch (Exception e) {
				logger.warn("error getting file name for archiving from {}", getName(), e);
			}
		}
	}

	@Override
	public void writeout(int frames, NXDetectorData dataTree) throws DeviceException {
		dataTree.addScanFileLink(getTreeName(), "nxfile://" + filename + "#entry/instrument/detector/data");

		addMetadata(dataTree);

		if (nxplugins != null) {
			for (NXPlugin nxpi: nxplugins) {
				try {
					List<NXDetectorDataAppender> appenderlist = nxpi.read(1);
					for(NXDetectorDataAppender appender : appenderlist)
						appender.appendTo(dataTree, getTreeName());
				} catch (Exception e) {
					throw new DeviceException(getName() + " - error reading nxplugin " + nxpi.getName(), e);
				}
			}
		}
		// delay returning from that method until area detector had a chance to read in all files
		// not pretty, but best solution I can come up with now.
		// there should be some getstatus or are you ready call
		controller.waitForReady();

		if (getStatus() == Detector.FAULT)
			throw new DeviceException(getName() + " - Detector in fault while reading");

		logger.info("{} - we think we are ready for the next acquisition now.", getName());
	}

	public List<NXPlugin> getNxplugins() {
		return nxplugins;
	}

	public void setNxplugins(List<NXPlugin> nxplugins) {
		this.nxplugins = nxplugins;
	}
}