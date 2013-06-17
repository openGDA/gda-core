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

package uk.ac.gda.server.ncd.pilatus;

import gda.data.PathConstructor;
import gda.device.DeviceException;
import gda.device.detector.areadetector.AreaDetectorROI;
import gda.device.detector.areadetector.IPVProvider;
import gda.device.detector.areadetector.v17.ADBase;
import gda.device.detector.areadetector.v17.FfmpegStream;
import gda.device.detector.areadetector.v17.NDArray;
import gda.device.detector.areadetector.v17.NDOverlay;
import gda.device.detector.areadetector.v17.NDProcess;
import gda.device.detector.areadetector.v17.NDROI;
import gda.device.detector.areadetector.v17.NDStats;
import gda.epics.connection.EpicsController;
import gda.factory.FactoryException;
import gov.aps.jca.Channel;
import gov.aps.jca.TimeoutException;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import uk.ac.gda.server.ncd.epics.NDFileHDF5Impl;

public class PilatusADController implements InitializingBean {

	static final Logger logger = LoggerFactory.getLogger(PilatusADController.class);

	// Values internal to the object for Channel Access
	private final EpicsController EPICS_CONTROLLER = EpicsController.getInstance();

	private static final String ACQUISITION_MODE_RBV = "AcquisitionMode_RBV";

	/**
	 * Map that stores the channel against the PV name
	 */
	private Map<String, Channel> channelMap = new HashMap<String, Channel>();
	
	// Variables to hold the spring settings
	private ADBase areaDetector;
	private String basePVName = null;
	private IPVProvider pvProvider;
	private NDROI roi;
	private NDProcess proc;
	private NDStats stats;
	private NDArray array;
	private NDOverlay draw;
	private NDFileHDF5Impl hdf5;
	private FfmpegStream mjpeg;

	public void configure() throws FactoryException {
		try {
			hdf5.setAutoSave((short) 0);
			hdf5.setAutoIncrement((short) 0);
			hdf5.setFileNumber((short) 0);
			hdf5.setFileName("unused");
			hdf5.setFilePath("unused");
			hdf5.getPluginBase().enableCallbacks();
			array.getPluginBase().enableCallbacks();
		} catch (Exception e) {
			throw new FactoryException("error configuring relevant area detector plugins", e);
		}
	}
	
	public ADBase getAreaDetector() {
		return areaDetector;
	}

	public void setAreaDetector(ADBase areaDetector) {
		this.areaDetector = areaDetector;
	}

	public NDROI getRoi() {
		return roi;
	}

	public void setRoi(NDROI roi) {
		this.roi = roi;
	}
	
	public NDProcess getProc() {
		return proc;
	}

	public void setProc(NDProcess proc) {
		this.proc = proc;
	}
	
	public NDStats getStats() {
		return stats;
	}

	public void setStats(NDStats stat) {
		this.stats = stat;
	}
	
	public NDOverlay getDraw() {
		return draw;
	}

	public void setDraw(NDOverlay draw) {
		this.draw = draw;
	}
	
	public NDFileHDF5Impl getHDF5() {
		return hdf5;
	}

	public void setHDF5(NDFileHDF5Impl hdf5) {
		this.hdf5 = hdf5;
	}
	
	public FfmpegStream getMjpeg() {
		return mjpeg;
	}

	public void setMjpeg(FfmpegStream mjpeg) {
		this.mjpeg = mjpeg;
	}

	public String getBasePVName() {
		return basePVName;
	}

	public void setBasePVName(String basePVName) {
		this.basePVName = basePVName;
	}

	public void resetAll() throws Exception {
		areaDetector.reset();
		stats.reset();
		array.reset();
		hdf5.reset();
	}

	public int getTriggerMode() throws Exception {
		return areaDetector.getTriggerMode();
	}
	
	public void setTriggerMode(int mode) throws Exception {
		areaDetector.setTriggerMode((short) mode);
	}

	public void setExposures(int numberOfExposures) throws Exception {
		areaDetector.setNumExposures(numberOfExposures);
	}

	public int getExposures() throws Exception {
		return areaDetector.getNumExposures();
	}

	public void setNumImages(int numberOfExposures) throws Exception {
		areaDetector.setNumImages(numberOfExposures);
		hdf5.setNumExtraDims(1);
		hdf5.setExtraDimSizeN(numberOfExposures);
	}

	public int getNumImages() throws Exception {
		return areaDetector.getNumImages();
	}
	
	public void setAcquireTime(double time) throws Exception {
		areaDetector.setAcquireTime(time);
	}
	
	public double getAcquireTime() throws Exception {
		return areaDetector.getAcquireTime();
	}

	public String getAquisitionMode() throws Exception {
		return EPICS_CONTROLLER.cagetString(getChannel(ACQUISITION_MODE_RBV));
	}

	private Channel getChannel(String pvPostFix) throws Exception {
		String fullPvName;
		if (pvProvider != null) {
			fullPvName = pvProvider.getPV(pvPostFix);
		} else {
			fullPvName = basePVName + pvPostFix;
		}
		Channel channel = channelMap.get(fullPvName);
		if (channel == null) {
			channel = EPICS_CONTROLLER.createChannel(fullPvName);
			channelMap.put(fullPvName, channel);
		}
		return channel;
	}

	public IPVProvider getPvProvider() {
		return pvProvider;
	}

	public void setPvProvider(IPVProvider pvProvider) {
		this.pvProvider = pvProvider;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (basePVName == null && pvProvider == null) {
			throw new IllegalArgumentException("'basePVName' or pvProvider needs to be declared");
		}

		if (areaDetector == null) {
			throw new IllegalArgumentException("'areaDetector' needs to be declared");
		}
		
		if (hdf5 == null) {
			throw new IllegalArgumentException("'hdf5' needs to be declared");
		}
		
		if (array == null) {
			throw new IllegalArgumentException("'array' needs to be declared");
		}

		if (stats == null) {
			throw new IllegalArgumentException("'stats' needs to be declared");
		}
	}

	public void acquire() throws Exception {
		areaDetector.startAcquiring();
	}

	public short getDetectorState() throws Exception {
		return areaDetector.getDetectorState_RBV();
	}

	public int getAcquireState() throws Exception {
		return areaDetector.getAcquireState();
	}

	public int getArrayCounter() throws Exception {
		return areaDetector.getArrayCounter_RBV();
	}

	public void setAcquirePeriod(double acquirePeriod) throws Exception {
		areaDetector.setAcquirePeriod(acquirePeriod);
	}

	public void stopAcquiringWithTimeout() throws Exception {
		int totalmillis = 10 * 1000;
		int grain = 25;
		for (int i = 0; i < totalmillis/grain; i++) {
			if (areaDetector.getAcquireState() == 0) 
				return;
			Thread.sleep(grain);
		}
		stopAcquiring();
	}
	
	public void stopAcquiring() throws Exception {
		try {
			areaDetector.stopAcquiring();
			return;
		} catch (Exception e) {
			// ignore once;
		}
		areaDetector.stopAcquiring();
	}

	public void setImageMode(int imageMode) throws Exception {
		areaDetector.setImageMode((short) imageMode);
	}

	public AreaDetectorROI getAreaDetectorROI() throws Exception {
		return areaDetector.getAreaDetectorROI();
	}

	public int getImageMode() throws Exception {
		return areaDetector.getImageMode_RBV();
	}

	public int getCaptureState() throws Exception {
		return areaDetector.getAcquireState();
	}

	public float[] getCurrentArray() throws Exception {
		return array.getFloatArrayData();
	}
	
	// PILATUS STUFF
	// TODO this needs to be in it's own plugin class, I guess
	
	final String PILATUS_DELAY_TIME = "DelayTime";
	final String PILATUS_DELAY_TIME_RBV = "DelayTime_RBV";
	final String PILATUS_READ_TIMEOUT = "ImageFileTmot";

	public void setReadTimeout(double d) throws Exception {
		EPICS_CONTROLLER.caput(getChannel(PILATUS_READ_TIMEOUT), d);
	}
	
	public double getReadTimeout() throws Exception {
		return EPICS_CONTROLLER.cagetDouble(getChannel(PILATUS_READ_TIMEOUT));
	}
	
	public void setDelay(double d) throws Exception {
		EPICS_CONTROLLER.caput(getChannel(PILATUS_DELAY_TIME), d);
	}
	
	public double getDelay() throws Exception {
		return EPICS_CONTROLLER.cagetDouble(getChannel(PILATUS_DELAY_TIME_RBV));
	}
	
	final String PILATUS_THRESHOLD_KEV = "ThresholdEnergy";
	
	public void setThresholdkeV(double d) throws Exception {
		EPICS_CONTROLLER.caput(getChannel(PILATUS_THRESHOLD_KEV), d);
	}
	
	public double getThresholdkeV() throws Exception {
		return EPICS_CONTROLLER.cagetDouble(getChannel(PILATUS_THRESHOLD_KEV));
	}
	
	final String PILATUS_ARMED = "Armed";
	
	public boolean isArmed() throws Exception {
		return EPICS_CONTROLLER.cagetEnum(getChannel(PILATUS_ARMED)) == 1;
	}

	public void setArray(NDArray array) {
		this.array = array;
	}

	public NDArray getArray() {
		return array;
	}

	/**
	 * hdf5 plugin only allows up to two added dimensions
	 * 
	 * @param dimensions
	 * @throws Exception
	 */
	public void setScanDimensions(int[] dimensions) throws Exception {
		hdf5.setNumExtraDims(dimensions.length > 2 ? 2 : dimensions.length);
		hdf5.setExtraDimSizeN(areaDetector.getNumImages());
		if (dimensions.length > 1) {
			int totalother = 1;
			for (int i = 1; i < dimensions.length; i++) {
				totalother *= dimensions[i];
			}
			hdf5.setExtraDimSizeY(dimensions[0]);
			hdf5.setExtraDimSizeX(totalother);
		} else {
			hdf5.setExtraDimSizeX(dimensions[0]);
		}
	}

	public void startRecording() throws Exception {
		if (hdf5.getCapture() == 1) 
				throw new DeviceException("detector found already saving data when it should not be");
		
		hdf5.setFilePath(PathConstructor.createFromDefaultProperty());
		hdf5.startCapture();
		int totalmillis = 60 * 1000;
		int grain = 25;
		for (int i = 0; i < totalmillis/grain; i++) {
			if (hdf5.getCapture() == 1) return;
			Thread.sleep(grain);
		}
		throw new TimeoutException("Timeout waiting for hdf file creation.");
	}

	public void endRecording() throws Exception {
		
		// when we are called here there no more frames will be collected, so we just need to make sure
		// all the ones in the system are written out
		
		// which the current settings writing a 2M frame on i22 takes ~50ms first time around and the same amount on closing

		throwIfWriteError();
		
		int totalFramesCollected = 1;
		int totalmillis = 30 * 1000;
		
		int grain = 80;
		for (int i = 0; i < totalmillis/grain; i++) {
			totalFramesCollected = areaDetector.getArrayCounter_RBV();
			totalmillis = 30 * 1000 + totalFramesCollected * 100;

			
			if (hdf5.getFile().getCapture_RBV() == 0) return;
			
			if (hdf5.getPluginBase().getArrayCounter_RBV() == totalFramesCollected) {
				hdf5.stopCapture();
			}
		
			if (hdf5.getQueueUse() > 1) {
				// reset wait time while we still churn through frames, keep waiting loop for closing only
				i = 0;
			}
				
			
			if (hdf5.getPluginBase().getDroppedArrays_RBV() > 0)
				throw new DeviceException("dropped frames in the hdf5 recording");
			
			throwIfWriteError();
				
			Thread.sleep(grain);
		}
		
		hdf5.stopCapture();
		logger.warn("Waited very long for hdf writing to finish, still not done. Hope all will be ok in the end.");
		throwIfWriteError();
	}
	
	private void throwIfWriteError() throws Exception {
		if (hdf5.getWriteStatus() != 0) {
			String message = "error in EPICS hdf5 file writer";
			try {
				message = message + " " + hdf5.getWriteMessage();
			} catch (Exception e) { }
			throw new DeviceException(message);
		}
	}
	
	public void stop() throws Exception {
		stopAcquiring();
		hdf5.stopCapture();
	}

	public String getAbsoulteFileNameRBV() throws Exception {
		throwIfWriteError();
		return hdf5.getFullFileName_RBV();
	}

	public void setAbsoluteFilename(String name) throws Exception {
		hdf5.setFileTemplate(name);
	}
	
	public void resetCounters() throws Exception {
		areaDetector.setArrayCounter(0);
		array.getPluginBase().setArrayCounter(0);
		array.getPluginBase().setDroppedArrays(0);
		hdf5.getPluginBase().setArrayCounter(0);
		hdf5.getPluginBase().setDroppedArrays(0);
	}

	public void waitForReady() throws DeviceException {

		try {
			int totalmillis = 150 * areaDetector.getNumImages_RBV()+5000;
			int grain = 25;
			for (int i = 0; i < totalmillis/grain; i++) {
				if (getAcquireState() == 0)
					return;
				Thread.sleep(grain);
			}
			logger.error("took too long to read in the frames, expect this scan to fall over any second.");
		} catch (Exception e) {
			throw new DeviceException("interrupted waiting for frames to be read in");
		}
	}
}