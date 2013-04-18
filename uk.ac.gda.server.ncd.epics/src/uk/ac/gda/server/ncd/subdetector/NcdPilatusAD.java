/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

import gda.data.NumTracker;
import gda.data.PathConstructor;
import gda.data.fileregistrar.FileRegistrarHelper;
import gda.data.metadata.GDAMetadataProvider;
import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.Timer;
import gda.device.detector.NXDetectorData;
import gda.device.timer.FrameSet;
import gda.factory.FactoryException;
import gda.jython.InterfaceProvider;
import gda.observable.IObserver;
import gda.scan.ScanInformation;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.FloatDataset;
import uk.ac.gda.server.ncd.pilatus.PilatusADController;

/**
 * the update method has some device specific code to flag errors when triggering (gating) is attempted
 * outside the detector's capabilities
 */
public class NcdPilatusAD extends NcdSubDetector implements InitializingBean, IObserver, LastImageProvider {

	// Setup the logging facilities
	private static final Logger logger = LoggerFactory.getLogger(NcdPilatusAD.class);
	protected PilatusADController controller;
	private Timer timer;
	protected DeviceException tfgMisconfigurationException = new DeviceException("Triggering not set up");
	private String predictedFilename = "/dev/null";

	@Override
	public void clear() throws DeviceException {
		// we are clear
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (controller == null) {
			throw new IllegalArgumentException("controller needs to be set");
		}
	}

	@Override
	public void configure() throws FactoryException {
		try {
			controller.configure();
			controller.setImageMode(1); // multiple image
			controller.setTriggerMode(1); // ext enable
			controller.setDelay(0);
		} catch (Exception e) {
			throw new FactoryException("error setting up area detector", e);
		}
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
			throw new DeviceException("failed to get image dimension", e);
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
			throw new DeviceException("Failure to read status", e);
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
				throw new DeviceException(String.format("detector must be IDLE, but current state is %d", state));
			}
			controller.acquire();

			int totalmillis = 4000;
			int grain = 25;
			for (int i = 0; i < totalmillis / grain; i++) {
				if (controller.isArmed()) {
					logger.debug(String.format("Was armed after %d ms.", i * grain));
					return;
				}
				Thread.sleep(grain);
			}

			throw new DeviceException("Timeout waiting for triggers to be armed.");
		} catch (DeviceException de) {
			// if proper type just rethrow
			throw de;
		} catch (Exception e) {
			throw new DeviceException("error starting acquire", e);
		}
	}

	@Override
	public void stop() throws DeviceException {
		try {
			controller.stop();
		} catch (Exception e) {
			throw new DeviceException("Cannot stop detector", e);
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
	public AbstractDataset readLastImage() throws DeviceException {
		try {
			float[] data;
			data = controller.getCurrentArray();
			int[] dims = getDataDimensions();
			AbstractDataset ds = new FloatDataset(data, dims);
			return ds;
		} catch (Exception e) {
			throw new DeviceException("error reading last array", e);
		}
	}

	private String setupFilename() throws Exception {
		String beamline = null;
		try {
			beamline = GDAMetadataProvider.getInstance().getMetadataValue("instrument", "gda.instrument", null);
		} catch (DeviceException e1) {
		}

		// If the beamline name isn't set then default to 'base'.
		if (beamline == null) {
			beamline = "base";
		}

		controller.setFilenamePrefix(beamline);
		controller.setFilenamePostfix(getName());

		// Check to see if the data directory has been defined.
		String dataDir = PathConstructor.createFromDefaultProperty();

		controller.setDirectory(dataDir);

		// Now lets try and setup the NumTracker...
		NumTracker runNumber = new NumTracker(beamline);
		// Get the current number
		Number scanNumber = runNumber.getCurrentFileNumber();

		controller.setFileNumber(scanNumber);
		
		return controller.predictFilename();
	}

	/**
	 * we need to initiate the file saving in here in order for that to be in line with the scan
	 */
	@Override
	public void atScanStart() throws DeviceException {
		logger.debug("scan start");
		if (tfgMisconfigurationException != null)
			throw tfgMisconfigurationException;

		ScanInformation scanInformation = InterfaceProvider.getCurrentScanInformationHolder()
				.getCurrentScanInformation();
		try {
			controller.setScanDimensions(scanInformation.getDimensions());
			predictedFilename = setupFilename();
			controller.resetCounters();
			controller.startRecording();
		} catch (Exception e) {
			throw new DeviceException("error setting up data recording to HDF5", e);
		}
	}

	@Override
	public void atScanEnd() throws DeviceException {
		logger.debug("scan end");
		try {
			controller.stopAcquiringWithTimeout();
			controller.endRecording();
		} catch (Exception e) {
			throw new DeviceException("error finalising data acquitision/writing", e);
		} finally {
			try {
				FileRegistrarHelper.registerFile(controller.getHDFFileName());
			} catch (Exception e) {
				logger.warn("error getting file name for archiving from "+getName(), e);
			}
		}
	}

	@Override
	public void writeout(int frames, NXDetectorData dataTree) throws DeviceException {
		String filename = predictedFilename;
		try {
			filename = controller.getHDFFileName();
		} catch (Exception e) {
			// will happen with LazyOpen
		}
		if (filename != null && !filename.equals(predictedFilename))
			throw new DeviceException("predicted hdf5 filename is wrong, this code can not be relied on.");
		
		dataTree.addScanFileLink(getName(), "nxfile://" + filename + "#entry/instrument/detector/data");

		addMetadata(dataTree);
		
		// delay returning from that method until area detector had a chance to read in all files
		// not pretty, but best solution I can come up with now.
		// there should be some getstatus or are you ready call

		try {
			int totalmillis = 40000;
			int grain = 25;
			for (int i = 0; i < totalmillis / grain; i++) {
				int state = getStatus();
				if (state == Detector.IDLE) {
					return;
				}
				Thread.sleep(grain);
			}
		} catch (InterruptedException e) {
			logger.error("interupted waiting for ioc to read files", e);
		}
	}
}