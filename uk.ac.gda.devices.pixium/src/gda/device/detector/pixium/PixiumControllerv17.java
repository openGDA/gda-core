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

package gda.device.detector.pixium;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.detector.areadetector.AreaDetectorROI;
import gda.device.detector.areadetector.v17.ADBase;
import gda.device.detector.areadetector.v17.FfmpegStream;
import gda.device.detector.areadetector.v17.NDArray;
import gda.device.detector.areadetector.v17.NDFile;
import gda.device.detector.areadetector.v17.NDFileHDF5;
import gda.device.detector.areadetector.v17.NDFileNexus;
import gda.device.detector.areadetector.v17.NDOverlay;
import gda.device.detector.areadetector.v17.NDProcess;
import gda.device.detector.areadetector.v17.NDROI;
import gda.device.detector.areadetector.v17.NDStats;
import gda.epics.connection.EpicsController;
import gda.epics.interfaces.PixiumType;
import gov.aps.jca.CAException;
import gov.aps.jca.CAStatus;
import gov.aps.jca.Channel;
import gov.aps.jca.TimeoutException;
import gov.aps.jca.event.PutEvent;
import gov.aps.jca.event.PutListener;
import uk.ac.gda.devices.pixium.IPixiumController;

public class PixiumControllerv17 implements IPixiumController, InitializingBean {

	static final Logger logger = LoggerFactory.getLogger(PixiumControllerv17.class);

	private static final String LOGICAL_MODE = "LogicalMode";
	private static final String LOGICAL_MODE_RBV = "LogicalMode_RBV";
	private static final String PU_MODE = "PUMode";
	private static final String PU_MODE_RBV = "PUMode_RBV";
	private static final String NUMBER_OF_OFFSETS = "NumberOfOffsets";
	private static final String NUMBER_OF_OFFSETS_RBV = "NumberOfOffsets_RBV";
	private static final String OFFSET_REFERENCE_NUMBER = "OffsetReferenceNumber";
	private static final String OFFSET_REFERENCE_NUMBER_RBV = "OffsetReferenceNumber_RBV";
	private static final String OFFSET_REFERENCE = "OffsetReference";
	private static final String OFFSET_REFERENCE_RBV = "OffsetReference_RBV";
	private static final String OFFSET_CALIBRATION = "OffsetCalibration";
	private static final String FRAME_RATE = "FrameRate";
	private static final String FRAME_RATE_RBV = "FrameRate_RBV";
	private static final String X_RAY_WINDOW = "XRayWindow";
	private static final String X_RAY_WINDOW_RBV = "XRayWindow_RBV";
	private static final String LOGICAL_MODE_STATUS = "LogicalModeStatus";
	private static final String ACQUISITION_MODE = "AcquisitionMode_RBV";
	private static final String DELTA_FREQ = "DeltaFreq";
	private static final String DELTA_FREQ_RBV = "DeltaFreq_RBV";
	private static final String CONNECTION = "Connection";
	private static final String CONNECTION_RBV = "Connection_RBV";
	private static final String LOGICAL_MODE_CHANGE = "ChangeMode";
	private static final String LOGICAL_MODE_CHANGE_RBV = "ChangeMode_RBV";
	private static final String OFFSET_REFERENCE_APPLY = "DefineOffsetReference";
	private static final String OFFSET_REFERENCE_APPLY_RBV = "DefineOffsetReference_RBV";
	private static final String MODE_CONTROL_DEFINE = "DefineMode";
	private static final String MODE_CONTROL_DEFINE_RBV = "DefineMode_RBV";
	private static final String MODE_CONTROL_DELETE = "DeleteMode";
	private static final String MODE_CONTROL_DELETE_RBV = "DeleteMode_RBV";
	private static final String MODE_CONTROL_LOAD = "LoadMode";
	private static final String MODE_CONTROL_LOAD_RBV = "LoadMode_RBV";
	private static final String MODE_CONTROL_UNLOAD = "UnloadMode";
	private static final String MODE_CONTROL_UNLOAD_RBV = "UnloadMode_RBV";
	private static final String MODE_CONTROL_ACTIVATE = "ActivateMode";
	private static final String MODE_CONTROL_ACTIVATE_RBV = "ActivateMode_RBV";
	private static final String MODE_CONTROL_DEACTIVATE = "DeactivateMode";
	private static final String MODE_CONTROL_DEACTIVATE_RBV = "DeactivateMode_RBV";
	private static final String PIXIUM_ABC_THRESHOLD = "Threshold";
	private static final String PIXIUM_ABC_THRESHOLD_RBV = "Threshold_RBV";
	private static final String PIXIUM_ABC_MIN_VOLTAGE = "ABC_MIN_VOLTAGE";
	private static final String PIXIUM_ABC_MIN_VOLTAGE_RBV = "ABC_MIN_VOLTAGE_RBV";
	private static final String PIXIUM_ABC_MAX_VOLTAGE = "ABC_MAX_VOLTAGE";
	private static final String PIXIUM_ABC_MAX_VOLTAGE_RBV = "ABC_MAX_VOLTAGE_RBV";

	private Vector<String> logicalModeStates = new Vector<String>();

	private Double idlePollTime_ms;
	private Vector<String> logicalModes=new Vector<String>();
	private Vector<String> puModes=new Vector<String>();
	/**
	 * Map that stores the channel against the PV name
	 */
	private Map<String, Channel> channelMap = new HashMap<String, Channel>();
	// Variables to hold the spring settings
	private ADBase areaDetector;
	private NDFile tiff;

	private String basePVName = null;

	private NDROI roi1;

	private NDProcess proc;

	private NDStats stat;

	private NDOverlay draw;

	private NDArray arr;

	private NDFileNexus nxs;

	private FfmpegStream mjpeg;

	private NDFileHDF5 hdf;

	private PixiumType config;

	// Getters and Setters for Spring
	@Override
	public ADBase getAreaDetector() {
		return areaDetector;
	}

	public void setAreaDetector(ADBase areaDetector) {
		this.areaDetector = areaDetector;
	}

	@Override
	public NDFile getTiff() {
		return tiff;
	}

	public void setTiff(NDFile fullFrameSaver) {
		this.tiff = fullFrameSaver;
	}

	@Override
	public NDROI getRoi() {
		return roi1;
	}

	public void setRoi(NDROI roi) {
		this.roi1 = roi;
	}

	@Override
	public NDProcess getProc() {
		return proc;
	}

	public void setProc(NDProcess proc) {
		this.proc = proc;
	}

	@Override
	public NDStats getStat() {
		return stat;
	}

	public void setStat(NDStats stat) {
		this.stat = stat;
	}

	@Override
	public NDOverlay getDraw() {
		return draw;
	}

	public void setDraw(NDOverlay draw) {
		this.draw = draw;
	}

	@Override
	public NDFileNexus getNxs() {
		return nxs;
	}

	public void setHdf(NDFileHDF5 hdf) {
		this.hdf = hdf;
	}

	@Override
	public NDFileHDF5 getHdf() {
		return hdf;
	}

	public void setNxs(NDFileNexus nxs) {
		this.nxs = nxs;
	}

	@Override
	public FfmpegStream getMjpeg() {
		return mjpeg;
	}

	public void setMjpeg(FfmpegStream mjpeg) {
		this.mjpeg = mjpeg;
	}

	@Override
	public String getBasePVName() {
		return basePVName;
	}

	public void setBasePVName(String basePVName) {
		this.basePVName = basePVName;
	}

	// Values internal to the object for Channel Access
	private final EpicsController EPICS_CONTROLLER = EpicsController.getInstance();
	/**
	 * set up capturing ready for detector exposure/acquire
	 */
	private String name;

	private CalibrationPutListener offsetCalCallback = new CalibrationPutListener();
	private volatile int status = Detector.IDLE;

	private ConnectionPutListener connectionCallback = new ConnectionPutListener();

	private ChangeModePutListener changeModeCallback = new ChangeModePutListener();

	private ApplyOffsetReferencePutListener applyOffsetReferenceCallback = new ApplyOffsetReferencePutListener();

	private class CalibrationPutListener implements PutListener {

		@Override
		public void putCompleted(PutEvent event) {
			if (event.getStatus() != CAStatus.NORMAL) {
				logger.error("Put failed. Channel {} : Status {}", ((Channel) event.getSource()).getName(), event
						.getStatus());
				setStatus(Detector.FAULT);
				return;
			}
			logger.info("{} : Offset calibration completed.", getName());
			setStatus(Detector.IDLE);
		}
	}

	private class ConnectionPutListener implements PutListener {

		@Override
		public void putCompleted(PutEvent event) {
			if (event.getStatus() != CAStatus.NORMAL) {
				logger.error("Put failed. Channel {} : Status {}", ((Channel) event.getSource()).getName(), event
						.getStatus());
				return;
			}
			logger.info("{} connected to detector.", getName());
		}
	}
	private class ChangeModePutListener implements PutListener {

		@Override
		public void putCompleted(PutEvent event) {
			if (event.getStatus() != CAStatus.NORMAL) {
				logger.error("Put failed. Channel {} : Status {}", ((Channel) event.getSource()).getName(), event
						.getStatus());
					return;
			}
			logger.info("{} : changing mode completed.", getName());
			setStatus(Detector.IDLE);
		}
	}
	private class ApplyOffsetReferencePutListener implements PutListener {

		@Override
		public void putCompleted(PutEvent event) {
			if (event.getStatus() != CAStatus.NORMAL) {
				logger.error("Put failed. Channel {} : Status {}", ((Channel) event.getSource()).getName(), event
						.getStatus());
					return;
			}
			logger.info("{} : apply offset reference completed.", getName());
			setStatus(Detector.IDLE);
		}
	}

	@Override
	public void resetAndEnableCameraControl() throws Exception {
		// initialise Frame Saver Elements, wait to capture
		tiff.getPluginBase().enableCallbacks();
		tiff.setFileNumber(0);
		tiff.setNumCapture(areaDetector.getNumImages_RBV()); // number per scan data point defined in camera
		tiff.setFileWriteMode((short) 2); //stream
		// ready to capture
		//tiff.startCapture();

		// Make sure the image sub-sample channel is also available
		arr.getPluginBase().enableCallbacks();

		// reset area detector
		areaDetector.stopAcquiring();
		areaDetector.setArrayCounter(0);
		// set the image mode to Multiple
		areaDetector.setImageMode((short) 1);
	}
	/**
	 * set up continuing acquiring ready for file capture
	 */
	@Override
	public void resetAndStartFilesRecording() throws Exception {
		// initialise Frame Saver Elements
		tiff.getPluginBase().enableCallbacks();
		tiff.setFileNumber(0);
		// set 'stream' mode
		tiff.setFileWriteMode((short) 2);
		tiff.stopCapture(); // must stop existing capture if any

		// Make sure the image sub-sample channel is also available
		arr.getPluginBase().enableCallbacks();

		// reset area detector and start continuous acquiring
		areaDetector.stopAcquiring();
		areaDetector.setArrayCounter(0);
		// set the image mode to Continuous
		areaDetector.setImageMode((short) 2); //file capture control only works in continuous acquiring mode
		areaDetector.startAcquiring();
	}

	@Override
	public void resetAll() throws Exception {
		initialise();
		if (areaDetector != null) areaDetector.reset();
		if (roi1 != null) roi1.reset();
		if (proc != null) proc.reset();
		if (stat != null) stat.reset();
		if (draw != null) draw.reset();
		if (arr != null)  arr.reset();
		if (tiff != null) tiff.reset();
		if (hdf != null)  hdf.reset();
		if (nxs != null)  nxs.reset();
		if (mjpeg != null) mjpeg.reset();
		hdf.setNumRowChunks(areaDetector.getArraySizeY_RBV());
		if (areaDetector.getAcquireState() == 1) {
			areaDetector.stopAcquiring(); // force stop any active camera acquisition
		}
		setSensibleDetectorParametersForAcquisition(); // initialise area detector ready for acquisition
		initialisePluginsArrayDimensions();

	}
	public void initialisePluginsArrayDimensions() throws Exception {
		if ((tiff.getPluginBase().isCallbackEnabled() && tiff.getPluginBase().getArraySize0_RBV() == 0)
				|| (hdf.getFile().getPluginBase().isCallbackEnabled() && hdf.getFile().getPluginBase().getArraySize0_RBV() == 0)) {
			// dummy acquisition to ensure all enabled EPICS plugin array dimensions are initialised,
			// these must be called at least once after IOC restarts.
			areaDetector.setImageMode((short)0); //
			areaDetector.startAcquiring();
		}
	}

	private void setSensibleDetectorParametersForAcquisition() throws Exception {
		areaDetector.setArrayCounter(0);
		// set the image mode to Multiple
		areaDetector.setImageMode((short) 0); //Single
		areaDetector.setTriggerMode((short) 0); // Internal
	}

	/**
	 * hdf5 plugin only allows up to two added dimensions
	 *
	 * @param dimensions
	 * @throws Exception
	 */
	@Override
	public void setScanDimensions(int[] dimensions) throws Exception {
		hdf.setNumExtraDims(dimensions.length > 2 ? 2 : dimensions.length);
		hdf.setExtraDimSizeN(areaDetector.getNumImages());
		if (dimensions.length > 1) {
			int totalother = 1;
			for (int i = 1; i < dimensions.length; i++) {
				totalother *= dimensions[i];
			}
			hdf.setExtraDimSizeY(dimensions[0]);
			hdf.setExtraDimSizeX(totalother);
		} else
			hdf.setExtraDimSizeX(dimensions[0]);
	}
	@Override
	public void startRecording() throws Exception {
		if (hdf.getCapture() == 1)
			throw new DeviceException("detector found already saving data when it should not be");
		//hdf.setFilePath(PathConstructor.createFromDefaultProperty());
		hdf.startCapture();
		int totalmillis = 60 * 1000;
		int grain = 25;
		for (int i = 0; i < totalmillis / grain; i++) {
			if (hdf.getCapture() == 1)
				return;
			Thread.sleep(grain);
		}
		throw new TimeoutException("Timeout waiting for hdf file creation.");
	}
	@Override
	public void endRecording() throws Exception {
		// writing the buffers can take a long time
		int totalmillis = 120 * 1000;
		int grain = 25;
		for (int i = 0; i < totalmillis / grain; i++) {
			if (hdf.getFile().getCapture_RBV() == 0)
				return;
			Thread.sleep(grain);
		}
		hdf.stopCapture();
		logger.warn("Waited very long for hdf writing to finish, still not done. Hope all we be ok in the end.");
		if (hdf.getFile().getPluginBase().getDroppedArrays_RBV() > 0)
			throw new DeviceException("sorry, we missed some frames");
	}
	// Pixium specific methods
	@Override
	public int getLogicalMode() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getLOGICAL_MODE_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel("LOGICAL_MODE_RBV", LOGICAL_MODE_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getLogicalMode", ex);
			throw ex;
		}
	}

	@Override
	public void setLogicalMode(int value) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getLOGICAL_MODE().getPv()), value);
			} else {
				EPICS_CONTROLLER.caput(getChannel("LOGICAL_MODE", LOGICAL_MODE), value);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setLogicalMode", ex);
			throw ex;
		}
	}
	private void initialiseLogicalModes() throws Exception{
		this.logicalModes.clear();
		String[] logicalModes = new String[]{};
		try {
			if (config != null) {
				logicalModes =EPICS_CONTROLLER.cagetLabels(createChannel(config.getLOGICAL_MODE().getPv()));
			} else {
				logicalModes =EPICS_CONTROLLER.cagetLabels(getChannel("LOGICAL_MODE", LOGICAL_MODE));
			}
			for (String each : logicalModes) {
				this.logicalModes.add(each);
			}
		} catch (Exception ex) {
			logger.warn("Cannot initialiseLogicalModes", ex);
			throw ex;
		}
	}
	public String getLogicalModeName() throws Exception {
		int val = getLogicalMode();
		return logicalModes.get(val);
	}
	public void setLogicalModeName(String logicalmode) throws Exception {
		int index = -1;
		if (logicalModes.contains(logicalmode)) {
			index = logicalModes.indexOf(logicalmode);
		} else {
			throw new IllegalArgumentException("Logical mode : "+logicalmode+ " is not supported.");
		}
		if (index != -1) {
			setLogicalMode(index);
		}
	}
	@Override
	public int getPUMode() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getPU_MODE_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel("PU_MODE_RBV", PU_MODE_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getPUMode", ex);
			throw ex;
		}
	}

	@Override
	public void setPUMode(int value) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getPU_MODE().getPv()), value);
			} else {
				EPICS_CONTROLLER.caput(getChannel("PU_MODE", PU_MODE), value);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setPUMode", ex);
			throw ex;
		}
	}
	private void initialisePUModes() throws Exception{
		this.puModes.clear();
		String[] puModes = new String[]{};
		try {
			if (config != null) {
				puModes =EPICS_CONTROLLER.cagetLabels(createChannel(config.getPU_MODE().getPv()));
			} else {
				puModes =EPICS_CONTROLLER.cagetLabels(getChannel("PU_MODE", PU_MODE));
			}
			for (String each : puModes) {
				this.puModes.add(each);
			}
		} catch (Exception ex) {
			logger.warn("Cannot initialisePUModes", ex);
			throw ex;
		}
	}
	public String getPUModeName() throws Exception {
		int val = getPUMode();
		return puModes.get(val);
	}
	public void setPUModeName(String pumode) throws Exception {
		int index = -1;
		if (puModes.contains(pumode)) {
			index = puModes.indexOf(pumode);
		} else {
			throw new IllegalArgumentException("PU mode : "+pumode+ " is not supported.");
		}
		if (index != -1) {
			setLogicalMode(index);
		}
	}
	@Override
	public int getNumberOfOffsets() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getNUMBER_OF_OFFSETS_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel("NUMBER_OF_OFFSETS_RBV", NUMBER_OF_OFFSETS_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getNumberOfOffsets", ex);
			throw ex;
		}
	}

	@Override
	public void setNumberOfOffsets(int value) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getNUMBER_OF_OFFSETS().getPv()), value);
			} else {
				EPICS_CONTROLLER.caput(getChannel("NUMBER_OF_OFFSETS", NUMBER_OF_OFFSETS), value);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setNumberOfOffsets", ex);
			logger.error("{} : {}", getName(),areaDetector.getStatusMessage_RBV());
			throw ex;
		}
	}
	@Override
	public int getOffsetReferenceNumber() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getOFFSET_REFERENCE_NUMBER_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel("OFFSET_REFERENCE_NUMBER_RBV", OFFSET_REFERENCE_NUMBER_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getOffsetReferenceNumber", ex);
			throw ex;
		}
	}

	@Override
	public void setOffsetReferenceNumber(int value) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getOFFSET_REFERENCE_NUMBER().getPv()), value);
			} else {
				EPICS_CONTROLLER.caput(getChannel("OFFSET_REFERENCE_NUMBER", OFFSET_REFERENCE_NUMBER), value);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setOffsetReferenceNumber", ex);
			logger.error("{} : {}", getName(),areaDetector.getStatusMessage_RBV());
			throw ex;
		}
	}
	@Override
	public int getOffsetReference() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getOFFSET_REFERENCE_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel("OFFSET_REFERENCE_RBV", OFFSET_REFERENCE_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getOffsetReference", ex);
			throw ex;
		}
	}

	@Override
	public void setOffsetReference(int value) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getOFFSET_REFERENCE().getPv()), value);
			} else {
				EPICS_CONTROLLER.caput(getChannel("OFFSET_REFERENCE", OFFSET_REFERENCE), value);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setOffsetReference", ex);
			logger.error("{} : {}", getName(),areaDetector.getStatusMessage_RBV());
			throw ex;
		}
	}

	@Override
	public void startOffsetCalibration() throws Exception {
		try {
			setStatus(Detector.BUSY);
			if (config != null) {
				EPICS_CONTROLLER.caputWait(createChannel(config.getOFFSET_CALIBRATION().getPv()), 1);
			} else {
				EPICS_CONTROLLER.caputWait(getChannel("OFFSET_CALIBRATION", OFFSET_CALIBRATION), 1);
			}
			setStatus(Detector.IDLE);
			logger.info("starting {} offset calibration processing, please wait until it completes.", getName());
		} catch (Exception ex) {
			setStatus(Detector.IDLE);
			logger.warn("Cannot startOffsetCalibration", ex);
			logger.error("{} : {}", getName(),areaDetector.getStatusMessage_RBV());
			throw ex;
		}
	}
	@Override
	public void startOffsetCalibration(double timeout) throws Exception {
		try {
			setStatus(Detector.BUSY);
			if (config != null) {
				EPICS_CONTROLLER.caputWait(createChannel(config.getOFFSET_CALIBRATION().getPv()), 1, timeout);
			} else {
				EPICS_CONTROLLER.caputWait(getChannel("OFFSET_CALIBRATION", OFFSET_CALIBRATION), 1, timeout);
			}
			setStatus(Detector.IDLE);
			logger.info("starting {} offset calibration processing, please wait until it completes.", getName());
		} catch (Exception ex) {
			setStatus(Detector.IDLE);
			logger.warn("Cannot startOffsetCalibration", ex);
			logger.error("{} : {}", getName(),areaDetector.getStatusMessage_RBV());
			throw ex;
		}
	}
	@Override
	public void abortOffsetCalibration() throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getOFFSET_CALIBRATION().getPv()), 0);
			} else {
				EPICS_CONTROLLER.caput(getChannel("OFFSET_CALIBRATION", OFFSET_CALIBRATION), 0);
			}
			setStatus(Detector.IDLE);
			logger.info("abort {} offset calibration processing", getName());
		} catch (Exception ex) {
			setStatus(Detector.IDLE);
			logger.warn("Cannot abortOffsetCalibration", ex);
			logger.error("{} : {}", getName(),areaDetector.getStatusMessage_RBV());
			throw ex;
		}
	}
	@Override
	public int getFrameRate() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getFRAME_RATE_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel("FRAME_RATE_RBV", FRAME_RATE_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getFrameRate", ex);
			throw ex;
		}
	}

	@Override
	public void setFrameRate(int value) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getFRAME_RATE().getPv()), value);
			} else {
				EPICS_CONTROLLER.caput(getChannel("FRAME_RATE", FRAME_RATE), value);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setFrameRate", ex);
			logger.error("{} : {}", getName(),areaDetector.getStatusMessage_RBV());
			throw ex;
		}
	}
	@Override
	public int getXRayWindow() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getX_RAY_WINDOW_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel("X_RAY_WINDOW_RBV", X_RAY_WINDOW_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getXRayWindow", ex);
			throw ex;
		}
	}

	@Override
	public void setXRayWindow(int value) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getX_RAY_WINDOW().getPv()), value);
			} else {
				EPICS_CONTROLLER.caput(getChannel("X_RAY_WINDOW", X_RAY_WINDOW), value);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setXRayWindow", ex);
			logger.error("{} : {}", getName(),areaDetector.getStatusMessage_RBV());
			throw ex;
		}
	}
	@Override
	public int getLogicalModeStatus() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getLOGICAL_MODE_STATUS().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel("LOGICAL_MODE_STATUS", LOGICAL_MODE_STATUS));
		} catch (Exception ex) {
			logger.warn("Cannot getLogicalModeStatus", ex);
			throw ex;
		}
	}
	private void initialiseLogicalModeStatusList() throws Exception {
		this.logicalModeStates.clear();
		String[] logicalModeStates = new String[]{};
		try {
			if (config != null) {
				logicalModeStates =EPICS_CONTROLLER.cagetLabels(createChannel(config.getLOGICAL_MODE_STATUS().getPv()));
			} else {
				logicalModeStates =EPICS_CONTROLLER.cagetLabels(getChannel("LOGICAL_MODE_STATUS", LOGICAL_MODE_STATUS));
			}
			for (String each : logicalModeStates) {
				this.logicalModeStates.add(each);
			}
		} catch (Exception ex) {
			logger.warn("Cannot initialiseLogicalModeStatusList", ex);
			throw ex;
		}
	}
	public String getLogicalModeStatusName() throws Exception{
		int index = getLogicalModeStatus();
		return logicalModeStates.get(index);
	}
	@Override
	public String getAcquisitionMode() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetString(createChannel(config.getACQUISITION_MODE().getPv()));
			}
			return EPICS_CONTROLLER.cagetString(getChannel("ACQUISITION_MODE", ACQUISITION_MODE));
		} catch (Exception ex) {
			logger.warn("Cannot getLogicalModeStatus", ex);
			throw ex;
		}
	}
	@Override
	public int getDeltaFreq() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getDELTA_FREQ_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel("DELTA_FREQ_RBV", DELTA_FREQ_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getDeltaFreq", ex);
			throw ex;
		}
	}

	@Override
	public void setDeltaFreq(int value) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getDELTA_FREQ().getPv()), value);
			} else {
				EPICS_CONTROLLER.caput(getChannel("DELTA_FREQ", DELTA_FREQ), value);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setDeltaFreq", ex);
			logger.error("{} : {}", getName(),areaDetector.getStatusMessage_RBV());
			throw ex;
		}
	}
	@Override
	public String getConnectionState() throws Exception {
		int val = -1;
		try {
			if (config != null) {
				val = EPICS_CONTROLLER.cagetInt(createChannel(config.getCONNECTION_RBV().getPv()));
			} else {
				val = EPICS_CONTROLLER.cagetInt(getChannel("CONNECTION_RBV", CONNECTION_RBV));
			}
			if (val==0) return "Disconnect";
			return "Connect";
		} catch (Exception ex) {
			logger.warn("Cannot getDeltaFreq", ex);
			throw ex;
		}
	}

	@Override
	public void connect() throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getCONNECTION().getPv()), 1, connectionCallback);
			} else {
				EPICS_CONTROLLER.caput(getChannel("CONNECTION", CONNECTION), 1, connectionCallback);
			}
			logger.info("Try to connect to detector {}, please wait.", getName());
		} catch (Exception ex) {
			logger.warn("Cannot connect to detector", ex);
			logger.error("{} : {}", getName(),areaDetector.getStatusMessage_RBV());
			throw ex;
		}
	}
	@Override
	public void disconnect() throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getCONNECTION().getPv()), 0);
			} else {
				EPICS_CONTROLLER.caput(getChannel("CONNECTION", CONNECTION), 0);
			}
			logger.info("Try to disconnect detector {} plaese wait.", getName());
		} catch (Exception ex) {
			logger.warn("Cannot disconnect detector", ex);
			logger.error("{} : {}", getName(),areaDetector.getStatusMessage_RBV());
			throw ex;
		}
	}
	@Override
	public void changeMode() throws Exception {
		try {
			setStatus(Detector.BUSY);
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getLOGICAL_MODE_CHANGE().getPv()), 1, changeModeCallback);
			} else {
				EPICS_CONTROLLER.caput(getChannel("LOGICAL_MODE_CHANGE", LOGICAL_MODE_CHANGE), 1, changeModeCallback);
			}
			logger.info("mode changing please wait.", getName());
		} catch (Exception ex) {
			setStatus(Detector.IDLE);
			logger.warn("Cannot change mode", ex);
			logger.error("{} : {}", getName(),areaDetector.getStatusMessage_RBV());
			throw ex;
		}
	}
	@Override
	public String getChangeModeState() throws Exception {
		int val = -1;
		try {
			if (config != null) {
				val = EPICS_CONTROLLER.cagetInt(createChannel(config.getLOGICAL_MODE_CHANGE_RBV().getPv()));
			} else {
				val = EPICS_CONTROLLER.cagetInt(getChannel("LOGICAL_MODE_CHANGE_RBV", LOGICAL_MODE_CHANGE_RBV));
			}
			if (val==0) return "Done";
			return "Changing";
		} catch (Exception ex) {
			logger.warn("Cannot getChangeModeState", ex);
			throw ex;
		}
	}
	@Override
	public void applyOffsetReference() throws Exception {
		try {
			setStatus(Detector.BUSY);
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getOFFSET_REFERENCE_APPLY().getPv()), 1, applyOffsetReferenceCallback);
			} else {
				EPICS_CONTROLLER.caput(getChannel("OFFSET_REFERENCE_APPLY", OFFSET_REFERENCE_APPLY), 1, applyOffsetReferenceCallback);
			}
			logger.info("{} : Apply new Offset Reference please wait.", getName());
		} catch (Exception ex) {
			setStatus(Detector.IDLE);
			logger.warn("Cannot apply new offset reference to the mode", ex);
			logger.error("{} : {}", getName(),areaDetector.getStatusMessage_RBV());
			throw ex;
		}
	}
	@Override
	public String getApplyOffsetReferenceState() throws Exception {
		int val = -1;
		try {
			if (config != null) {
				val = EPICS_CONTROLLER.cagetInt(createChannel(config.getOFFSET_REFERENCE_APPLY_RBV().getPv()));
			} else {
				val = EPICS_CONTROLLER.cagetInt(getChannel("OFFSET_REFERENCE_APPLY_RBV", OFFSET_REFERENCE_APPLY_RBV));
			}
			if (val==0) return "Done";
			return "Applying";
		} catch (Exception ex) {
			logger.warn("Cannot getApplyOffsetReferenceState", ex);
			throw ex;
		}
	}
	@Override
	public void defineMode() throws Exception {
		try {
			setStatus(Detector.BUSY);
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getMODE_CONTROL_DEFINE().getPv()), 1, new PutListener() {

					@Override
					public void putCompleted(PutEvent event) {
						if (event.getStatus() != CAStatus.NORMAL) {
							logger.error("Put failed. Channel {} : Status {}", ((Channel) event.getSource()).getName(), event
									.getStatus());
								return;
						}
						logger.info("{} : define new mode completed.", getName());
						setStatus(Detector.IDLE);
					}
				});
			} else {
				EPICS_CONTROLLER.caput(getChannel("MODE_CONTROL_DEFINE", MODE_CONTROL_DEFINE), 1, new PutListener() {

					@Override
					public void putCompleted(PutEvent event) {
						if (event.getStatus() != CAStatus.NORMAL) {
							logger.error("Put failed. Channel {} : Status {}", ((Channel) event.getSource()).getName(), event
									.getStatus());
								return;
						}
						logger.info("{} : define new mode completed.", getName());
						setStatus(Detector.IDLE);
					}
				});
			}
			logger.info("{} : Define new mode please wait.", getName());
		} catch (Exception ex) {
			setStatus(Detector.IDLE);
			logger.warn("Cannot define new mode", ex);
			logger.error("{} : {}", getName(),areaDetector.getStatusMessage_RBV());
			throw ex;
		}
	}
	@Override
	public String getDefineModeState() throws Exception {
		int val = -1;
		try {
			if (config != null) {
				val = EPICS_CONTROLLER.cagetInt(createChannel(config.getMODE_CONTROL_DEFINE_RBV().getPv()));
			} else {
				val = EPICS_CONTROLLER.cagetInt(getChannel("MODE_CONTROL_DEFINE_RBV", MODE_CONTROL_DEFINE_RBV));
			}
			if (val==0) return "Done";
			return "Defining";
		} catch (Exception ex) {
			logger.warn("Cannot getDefineModeState", ex);
			throw ex;
		}
	}
	@Override
	public void deleteMode() throws Exception {
		try {
			setStatus(Detector.BUSY);
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getMODE_CONTROL_DELETE().getPv()), 1, new PutListener() {

					@Override
					public void putCompleted(PutEvent event) {
						if (event.getStatus() != CAStatus.NORMAL) {
							logger.error("Put failed. Channel {} : Status {}", ((Channel) event.getSource()).getName(), event
									.getStatus());
								return;
						}
						logger.info("{} : delete mode completed.", getName());
						setStatus(Detector.IDLE);
					}
				});
			} else {
				EPICS_CONTROLLER.caput(getChannel("MODE_CONTROL_DELETE", MODE_CONTROL_DELETE), 1, new PutListener() {

					@Override
					public void putCompleted(PutEvent event) {
						if (event.getStatus() != CAStatus.NORMAL) {
							logger.error("Put failed. Channel {} : Status {}", ((Channel) event.getSource()).getName(), event
									.getStatus());
								return;
						}
						logger.info("{} : delete mode completed.", getName());
						setStatus(Detector.IDLE);
					}
				});
			}
			logger.info("{} : deleting mode please wait.", getName());
		} catch (Exception ex) {
			setStatus(Detector.IDLE);
			logger.warn("Cannot delete mode", ex);
			logger.error("{} : {}", getName(),areaDetector.getStatusMessage_RBV());
			throw ex;
		}
	}
	@Override
	public String getDeleteModeState() throws Exception {
		int val = -1;
		try {
			if (config != null) {
				val = EPICS_CONTROLLER.cagetInt(createChannel(config.getMODE_CONTROL_DELETE_RBV().getPv()));
			} else {
				val = EPICS_CONTROLLER.cagetInt(getChannel("MODE_CONTROL_DELETE_RBV", MODE_CONTROL_DELETE_RBV));
			}
			if (val==0) return "Done";
			return "Deleting";
		} catch (Exception ex) {
			logger.warn("Cannot getDeleteModeState", ex);
			throw ex;
		}
	}
	@Override
	public void loadMode() throws Exception {
		try {
			setStatus(Detector.BUSY);
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getMODE_CONTROL_LOAD().getPv()), 1, new PutListener() {

					@Override
					public void putCompleted(PutEvent event) {
						if (event.getStatus() != CAStatus.NORMAL) {
							logger.error("Put failed. Channel {} : Status {}", ((Channel) event.getSource()).getName(), event
									.getStatus());
								return;
						}
						logger.info("{} : load mode completed.", getName());
						setStatus(Detector.IDLE);
					}
				});
			} else {
				EPICS_CONTROLLER.caput(getChannel("MODE_CONTROL_LOAD", MODE_CONTROL_LOAD), 1, new PutListener() {

					@Override
					public void putCompleted(PutEvent event) {
						if (event.getStatus() != CAStatus.NORMAL) {
							logger.error("Put failed. Channel {} : Status {}", ((Channel) event.getSource()).getName(), event
									.getStatus());
								return;
						}
						logger.info("{} : load mode completed.", getName());
						setStatus(Detector.IDLE);
					}
				});
			}
			logger.info("{} : loading mode please wait.", getName());
		} catch (Exception ex) {
			setStatus(Detector.IDLE);
			logger.warn("Cannot load mode", ex);
			logger.error("{} : {}", getName(),areaDetector.getStatusMessage_RBV());
			throw ex;
		}
	}
	@Override
	public String getLoadModeState() throws Exception {
		int val = -1;
		try {
			if (config != null) {
				val = EPICS_CONTROLLER.cagetInt(createChannel(config.getMODE_CONTROL_LOAD_RBV().getPv()));
			} else {
				val = EPICS_CONTROLLER.cagetInt(getChannel("MODE_CONTROL_LOAD_RBV", MODE_CONTROL_LOAD_RBV));
			}
			if (val==0) return "Done";
			return "Loading";
		} catch (Exception ex) {
			logger.warn("Cannot getLoadModeState", ex);
			throw ex;
		}
	}
	@Override
	public void unloadMode() throws Exception {
		try {
			setStatus(Detector.BUSY);
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getMODE_CONTROL_UNLOAD().getPv()), 1, new PutListener() {

					@Override
					public void putCompleted(PutEvent event) {
						if (event.getStatus() != CAStatus.NORMAL) {
							logger.error("Put failed. Channel {} : Status {}", ((Channel) event.getSource()).getName(), event
									.getStatus());
								return;
						}
						logger.info("{} : unload mode completed.", getName());
						setStatus(Detector.IDLE);
					}
				});
			} else {
				EPICS_CONTROLLER.caput(getChannel("MODE_CONTROL_UNLOAD", MODE_CONTROL_UNLOAD), 1, new PutListener() {

					@Override
					public void putCompleted(PutEvent event) {
						if (event.getStatus() != CAStatus.NORMAL) {
							logger.error("Put failed. Channel {} : Status {}", ((Channel) event.getSource()).getName(), event
									.getStatus());
								return;
						}
						logger.info("{} : unload mode completed.", getName());
						setStatus(Detector.IDLE);
					}
				});
			}
			logger.info("{} : unloading mode please wait.", getName());
		} catch (Exception ex) {
			setStatus(Detector.IDLE);
			logger.warn("Cannot unload mode", ex);
			logger.error("{} : {}", getName(),areaDetector.getStatusMessage_RBV());
			throw ex;
		}
	}
	@Override
	public String getUnloadModeState() throws Exception {
		int val = -1;
		try {
			if (config != null) {
				val = EPICS_CONTROLLER.cagetInt(createChannel(config.getMODE_CONTROL_UNLOAD_RBV().getPv()));
			} else {
				val = EPICS_CONTROLLER.cagetInt(getChannel("MODE_CONTROL_UNLOAD_RBV", MODE_CONTROL_UNLOAD_RBV));
			}
			if (val==0) return "Done";
			return "Unloading";
		} catch (Exception ex) {
			logger.warn("Cannot getUnloadModeState", ex);
			throw ex;
		}
	}
	@Override
	public void activateMode() throws Exception {
		try {
			setStatus(Detector.BUSY);
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getMODE_CONTROL_ACTIVATE().getPv()), 1, new PutListener() {

					@Override
					public void putCompleted(PutEvent event) {
						if (event.getStatus() != CAStatus.NORMAL) {
							logger.error("Put failed. Channel {} : Status {}", ((Channel) event.getSource()).getName(), event
									.getStatus());
								return;
						}
						logger.info("{} : activate mode completed.", getName());
						setStatus(Detector.IDLE);
					}
				});
			} else {
				EPICS_CONTROLLER.caput(getChannel("MODE_CONTROL_ACTIVATE", MODE_CONTROL_ACTIVATE), 1, new PutListener() {

					@Override
					public void putCompleted(PutEvent event) {
						if (event.getStatus() != CAStatus.NORMAL) {
							logger.error("Put failed. Channel {} : Status {}", ((Channel) event.getSource()).getName(), event
									.getStatus());
								return;
						}
						logger.info("{} : activate mode completed.", getName());
						setStatus(Detector.IDLE);
					}
				});
			}
			logger.info("{} : activating mode please wait.", getName());
		} catch (Exception ex) {
			setStatus(Detector.IDLE);
			logger.warn("Cannot activate mode", ex);
			logger.error("{} : {}", getName(),areaDetector.getStatusMessage_RBV());
			throw ex;
		}
	}
	@Override
	public String getActivateModeState() throws Exception {
		int val = -1;
		try {
			if (config != null) {
				val = EPICS_CONTROLLER.cagetInt(createChannel(config.getMODE_CONTROL_ACTIVATE_RBV().getPv()));
			} else {
				val = EPICS_CONTROLLER.cagetInt(getChannel("MODE_CONTROL_ACTIVATE_RBV", MODE_CONTROL_ACTIVATE_RBV));
			}
			if (val==0) return "Done";
			return "Activating";
		} catch (Exception ex) {
			logger.warn("Cannot getActivateModeState", ex);
			throw ex;
		}
	}
	@Override
	public void deactivateMode() throws Exception {
		try {
			setStatus(Detector.BUSY);
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getMODE_CONTROL_DEACTIVATE().getPv()), 1, new PutListener() {

					@Override
					public void putCompleted(PutEvent event) {
						if (event.getStatus() != CAStatus.NORMAL) {
							logger.error("Put failed. Channel {} : Status {}", ((Channel) event.getSource()).getName(), event
									.getStatus());
								return;
						}
						logger.info("{} : deactivate mode completed.", getName());
						setStatus(Detector.IDLE);
					}
				});
			} else {
				EPICS_CONTROLLER.caput(getChannel("MODE_CONTROL_DEACTIVATE", MODE_CONTROL_DEACTIVATE), 1, new PutListener() {

					@Override
					public void putCompleted(PutEvent event) {
						if (event.getStatus() != CAStatus.NORMAL) {
							logger.error("Put failed. Channel {} : Status {}", ((Channel) event.getSource()).getName(), event
									.getStatus());
								return;
						}
						logger.info("{} : deactivate mode completed.", getName());
						setStatus(Detector.IDLE);
					}
				});
			}
			logger.info("{} : deactivating mode please wait.", getName());
		} catch (Exception ex) {
			setStatus(Detector.IDLE);
			logger.warn("Cannot deactivate mode", ex);
			logger.error("{} : {}", getName(),areaDetector.getStatusMessage_RBV());
			throw ex;
		}
	}
	@Override
	public String getDeactivateModeState() throws Exception {
		int val = -1;
		try {
			if (config != null) {
				val = EPICS_CONTROLLER.cagetInt(createChannel(config.getMODE_CONTROL_DEACTIVATE_RBV().getPv()));
			} else {
				val = EPICS_CONTROLLER.cagetInt(getChannel("MODE_CONTROL_DEACTIVATE_RBV", MODE_CONTROL_DEACTIVATE_RBV));
			}
			if (val==0) return "Done";
			return "Deactivating";
		} catch (Exception ex) {
			logger.warn("Cannot getDeactivateModeState", ex);
			throw ex;
		}
	}
	@Override
	public int getThreshold() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getPIXIUM_ABC_THRESHOLD_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel("PIXIUM_ABC_THRESHOLD_RBV", PIXIUM_ABC_THRESHOLD_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getThreshold", ex);
			throw ex;
		}
	}

	@Override
	public void setThreshold(int value) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getPIXIUM_ABC_THRESHOLD().getPv()), value);
			} else {
				EPICS_CONTROLLER.caput(getChannel("PIXIUM_ABC_THRESHOLD", PIXIUM_ABC_THRESHOLD), value);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setThreshold", ex);
			logger.error("{} : {}", getName(),areaDetector.getStatusMessage_RBV());
			throw ex;
		}
	}
	@Override
	public int getABCMinVoltage() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getPIXIUM_ABC_MIN_VOLTAGE_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel("PIXIUM_ABC_MIN_VOLTAGE_RBV", PIXIUM_ABC_MIN_VOLTAGE_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getABCMinVoltage", ex);
			throw ex;
		}
	}

	@Override
	public void setABCMinVoltage(int value) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getPIXIUM_ABC_MIN_VOLTAGE().getPv()), value);
			} else {
				EPICS_CONTROLLER.caput(getChannel("PIXIUM_ABC_MIN_VOLTAGE", PIXIUM_ABC_MIN_VOLTAGE), value);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setABCMinVoltage", ex);
			logger.error("{} : {}", getName(),areaDetector.getStatusMessage_RBV());
			throw ex;
		}
	}
	@Override
	public int getABCMaxVoltage() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getPIXIUM_ABC_MAX_VOLTAGE_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel("PIXIUM_ABC_MAX_VOLTAGE_RBV", PIXIUM_ABC_MAX_VOLTAGE_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getABCMaxVoltage", ex);
			throw ex;
		}
	}

	@Override
	public void setABCMaxVoltage(int value) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getPIXIUM_ABC_MAX_VOLTAGE().getPv()), value);
			} else {
				EPICS_CONTROLLER.caput(getChannel("PIXIUM_ABC_MAX_VOLTAGE", PIXIUM_ABC_MAX_VOLTAGE), value);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setABCMaxVoltage", ex);
			logger.error("{} : {}", getName(),areaDetector.getStatusMessage_RBV());
			throw ex;
		}
	}
	@Override
	public String setMode(int logicalMode, int offsetNum) throws Exception {

		setLogicalMode(logicalMode);
		Thread.sleep(2000);
		setOffsetReferenceNumber(offsetNum);
		changeMode();
		while (getStatus()==Detector.BUSY) {
			Thread.sleep(100);
		}
		return this.report();
	}

	@Override
	public String report() throws Exception {
		String result = this.getAcquisitionMode();

		result += "\nBinning      = " + areaDetector.getBinning().toString();
		result += "\nROI          = " + areaDetector.getAreaDetectorROI().toString();
		result += "\nX-Ray window = " + this.getXRayWindow() + "ms";
		result += "\nFrequency    = " + this.getFrequency() + "mHz";
		return result;
	}

	@Override
	public void setExposures(int numberOfExposures) throws Exception {
		areaDetector.setNumExposures(numberOfExposures);
	}

	@Override
	public int getExposures() throws Exception {
		return areaDetector.getNumExposures();
	}

	private int getFrequency() throws Exception {
		//TODO complex query of logical mode data
		EPICS_CONTROLLER.caput(getChannel(OFFSET_REFERENCE),
				EPICS_CONTROLLER.cagetInt(getChannel(OFFSET_REFERENCE_NUMBER)));
		return EPICS_CONTROLLER.cagetInt(getChannel(FRAME_RATE_RBV));
	}
	@Override
	public double getAcquireTime() throws Exception {
		return areaDetector.getAcquireTime_RBV();
	}

	@Override
	public double getAcquirePeriod() throws Exception {
		return areaDetector.getAcquirePeriod_RBV();
	}
	/**
	 * This method allows to toggle between the method in which the PV is acquired.
	 *
	 * @param pvElementName
	 * @param args
	 * @return {@link Channel} to talk to the relevant PV.
	 * @throws Exception
	 */
	private Channel getChannel(String pvElementName, String... args) throws Exception {
		try {
			String pvPostFix = null;
			if (args.length > 0) {
				// PV element name is different from the pvPostFix
				pvPostFix = args[0];
			} else {
				pvPostFix = pvElementName;
			}

			String fullPvName = basePVName + pvPostFix;
			return createChannel(fullPvName);
		} catch (Exception exception) {
			logger.warn("Problem getting channel", exception);
			throw exception;
		}
	}

	public Channel createChannel(String fullPvName) throws CAException, TimeoutException {
		Channel channel = channelMap.get(fullPvName);
		if (channel == null) {
			try {
				channel = EPICS_CONTROLLER.createChannel(fullPvName);
			} catch (CAException cae) {
				logger.warn("Problem creating channel", cae);
				throw cae;
			} catch (TimeoutException te) {
				logger.warn("Problem creating channel", te);
				throw te;

			}
			channelMap.put(fullPvName, channel);
		}
		return channel;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (basePVName == null) {
			throw new IllegalArgumentException("'basePVName' needs to be declared");
		}

		if (areaDetector == null) {
			throw new IllegalArgumentException("'areaDetector' needs to be declared");
		}
		if (tiff == null) {
			throw new IllegalArgumentException("'fullFrameSaver' needs to be declared");
		}

		if (roi1 == null) {
			throw new IllegalArgumentException("'roi1' needs to be declared");
		}
		if (proc == null) {
			throw new IllegalArgumentException("'proc' needs to be declared");
		}
		if (stat == null) {
			throw new IllegalArgumentException("'stat' needs to be declared");
		}
		if (draw == null) {
			throw new IllegalArgumentException("'draw' needs to be declared");
		}
		if (arr == null) {
			throw new IllegalArgumentException("'arr' needs to be declared");
		}
		if (hdf == null) {
			throw new IllegalArgumentException("'hdf' needs to be declared");
		}
		if (nxs == null) {
			logger.warn("'nxs' plugin is not available");
		}
		if (mjpeg == null) {
			throw new IllegalArgumentException("'mjpeg' needs to be declared");
		}
	}
	public void initialise() throws Exception {
		initialiseLogicalModes();
		initialisePUModes();
		initialiseLogicalModeStatusList();
	}
	@Override
	public void acquire() throws Exception {
		areaDetector.startAcquiring();

	}

	@Override
	public short getDetectorState() throws Exception {
		return areaDetector.getDetectorState_RBV();
	}

	@Override
	public int getAcquireState() throws Exception {
		return areaDetector.getAcquireState();
	}

	@Override
	public void setNumImages(int numberOfImage) throws Exception {
		areaDetector.setNumImages(numberOfImage);
	}

	@Override
	public int getArrayCounter() throws Exception {
		return areaDetector.getArrayCounter_RBV();
	}

	@Override
	public void setAcquirePeriod(double acquirePeriod) throws Exception {
		areaDetector.setAcquirePeriod(acquirePeriod);
	}

	@Override
	public void stopAcquiring() throws Exception {
		areaDetector.stopAcquiring();
	}

	@Override
	public void setImageMode(int imageMode) throws Exception {
		areaDetector.setImageMode((short) imageMode);
	}

	@Override
	public AreaDetectorROI getAreaDetectorROI() throws Exception {
		return areaDetector.getAreaDetectorROI();
	}

	@Override
	public Double getIdlePollTime_ms() {
		return idlePollTime_ms;
	}

	@Override
	public void setIdlePollTime_ms(Double idlePollTime_ms) {
		this.idlePollTime_ms = idlePollTime_ms;
	}

	@Override
	public void startTiffCapture() throws Exception {
		tiff.startCapture();
	}

	@Override
	public void startHdfCapture() throws Exception {
		hdf.startCapture();
	}
	@Override
	public int getImageMode() throws Exception {
		return areaDetector.getImageMode_RBV();
	}

	@Override
	public void setTiffNumCapture(int numcapture) throws Exception {
		tiff.setNumCapture(numcapture);
	}
	@Override
	public void setHdfNumCapture(int numcapture) throws Exception {
		hdf.setNumCapture(numcapture);
	}

	@Override
	public String getTiffFullFileName() throws Exception {
		return tiff.getFullFileName_RBV();
	}
	@Override
	public String getHdfFullFileName() throws Exception {
		return hdf.getFullFileName_RBV();
	}

	@Override
	public int getTiffCaptureState() throws Exception {
		return tiff.getCapture_RBV();
	}

	@Override
	public int getHdfCaptureState() throws Exception {
		return hdf.getCapture_RBV();
	}
	@Override
	public void stopTiffCapture() throws Exception {
		tiff.stopCapture();
	}
	@Override
	public void stopHdfCapture() throws Exception {
		hdf.stopCapture();
	}

	@Override
	public void setName(String name) {
		this.name=name;
	}

	@Override
	public String getName() {
		return name;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	@Override
	public int getStatus() {
		return status;
	}

	public void setArr(NDArray arr) {
		this.arr = arr;
	}

	public NDArray getArr() {
		return arr;
	}

	@Override
	public void stop() throws Exception {
		areaDetector.stopAcquiring();
		hdf.stopCapture();
		tiff.stopCapture();
	}
	@Override
	public void disableTiffSaver() throws Exception {
		tiff.getPluginBase().disableCallbacks();
	}

	@Override
	public void enableTiffSaver() throws Exception {
		tiff.getPluginBase().enableCallbacks();
	}

	@Override
	public void disableHdfSaver() throws Exception {
		hdf.getFile().getPluginBase().disableCallbacks();
	}

	@Override
	public void enableHdfSaver() throws Exception {
		hdf.getFile().getPluginBase().enableCallbacks();
	}
	@Override
	public String getHDFFileName() throws Exception {
		return hdf.getFullFileName_RBV();
	}

}
