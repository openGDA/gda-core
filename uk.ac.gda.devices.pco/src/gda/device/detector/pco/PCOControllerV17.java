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

package gda.device.detector.pco;

import gda.configuration.epics.ConfigurationNotFoundException;
import gda.configuration.epics.Configurator;
import gda.device.DeviceException;
import gda.device.detector.IPCOControllerV17;
import gda.device.detector.areadetector.IPVProvider;
import gda.device.detector.areadetector.v17.ADBase;
import gda.device.detector.areadetector.v17.FfmpegStream;
import gda.device.detector.areadetector.v17.ImageMode;
import gda.device.detector.areadetector.v17.NDArray;
import gda.device.detector.areadetector.v17.NDFile;
import gda.device.detector.areadetector.v17.NDFileHDF5;
import gda.device.detector.areadetector.v17.NDFileNexus;
import gda.device.detector.areadetector.v17.NDOverlay;
import gda.device.detector.areadetector.v17.NDProcess;
import gda.device.detector.areadetector.v17.NDROI;
import gda.device.detector.areadetector.v17.NDStats;
import gda.epics.connection.EpicsController;
import gda.epics.interfaces.PcocamType;
import gda.factory.FactoryException;
import gda.jython.InterfaceProvider;
import gov.aps.jca.CAException;
import gov.aps.jca.Channel;
import gov.aps.jca.TimeoutException;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import uk.ac.gda.devices.pco.LiveModeUtil;

/**
 * Separating out the detector from the controller - Part of GDA-4231 area detector stuff to get all detectors aligned
 * to EPICS V1.7
 * 
 * @author rsr31645
 */
public class PCOControllerV17 implements IPCOControllerV17, InitializingBean {
	private static final Logger logger = LoggerFactory.getLogger(PCOControllerV17.class);
	// PCO specific EPICS interface element and PV fields
	private static final String PIX_RATE = "PIX_RATE";
	private static final String PIX_RATE_RBV = "PIX_RATE_RBV";
	private static final String ADC_MODE = "ADC_MODE";
	private static final String ADC_MODE_RBV = "ADC_MODE_RBV";
	private static final String CAM_RAM_USE_RBV = "CAM_RAM_USE_RBV";
	private static final String ELEC_TEMP_RBV = "ELEC_TEMP_RBV";
	private static final String POWER_TEMP_RBV = "POWER_TEMP_RBV";
	private static final String STORAGE_MODE = "STORAGE_MODE";
	private static final String STORAGE_MODE_RBV = "STORAGE_MODE_RBV";
	private static final String RECORDER_MODE = "RECORDER_MODE";
	private static final String RECORDER_MODE_RBV = "RECORDER_MODE_RBV";
	private static final String TIMESTAMP_MODE = "TIMESTAMP_MODE";
	private static final String TIMESTAMP_MODE_RBV = "TIMESTAMP_MODE_RBV";
	private static final String ACQUIRE_MODE = "ACQUIRE_MODE";
	private static final String ACQUIRE_MODE_RBV = "ACQUIRE_MODE_RBV";
	private static final String ARM_MODE = "ARM_MODE";
	private static final String ARM_MODE_RBV = "ARM_MODE_RBV";
	private static final String DELAY_TIME = "DELAY_TIME";
	private static final String DELAY_TIME_RBV = "DELAY_TIME_RBV";

	private int initialTimestampMode = 1;

	/**
	 * Mark Basham's calibrated readout time for each ADC mode. These are set in Spring configuration.
	 */
	private int readout1ADC8Mhz;
	private int readout1ADC32Mhz;
	private int readout2ADC32Mhz;
	private int readout2ADC8Mhz;
	/**
	 * specify the software simulated trigger PV used for Trigger mode acquisition. The default is a digital IO channel
	 */
	private String triggerPV = "BL12I-EA-DIO-01:OUT:00";
	/**
	 * EPICS Utility
	 */
	private final EpicsController EPICS_CONTROLLER = EpicsController.getInstance();

	/**
	 * Map that stores the channel against the PV name
	 */
	private Map<String, Channel> channelMap = new HashMap<String, Channel>();

	private FfmpegStream mjpeg1;
	private FfmpegStream mjpeg2;
	/**
	 * area detcetor and its plugin components
	 */
	private ADBase areaDetector;
	private NDFile tiff;
	private NDROI roi1;
	private NDROI roi2;
	private NDProcess proc1;
	private NDProcess proc2;
	private NDStats stat;
	private NDOverlay draw;
	private NDArray array;
	private NDFileNexus nxs;
	private NDFileHDF5 hdf;

	private String name;
	private String deviceName;
	private String basePVName;
	private IPVProvider pvProvider;
	private PcocamType config;

	/**
	 * detector's trigger mode - auto, soft, Ext + Soft, Ext Pulse. used to switch detcetor operation mode.
	 */
	private TriggerMode triggermode = TriggerMode.SOFT;
	private final boolean isLive;

	public enum TriggerMode {
		AUTO, SOFT, EXTSOFT, EXTPULSE
	}

	public PCOControllerV17() {
		super();
		isLive = LiveModeUtil.isLiveMode();
	}

	@Override
	public void setTriggerMode(TriggerMode value) throws Exception {
		this.triggermode = value;
		switch (value) {
		case AUTO:
			areaDetector.setTriggerMode((short) TriggerMode.AUTO.ordinal());
			break;
		case SOFT:
			areaDetector.setTriggerMode((short) TriggerMode.SOFT.ordinal());
			break;
		case EXTSOFT:
			areaDetector.setTriggerMode((short) TriggerMode.EXTSOFT.ordinal());
			break;
		case EXTPULSE:
			areaDetector.setTriggerMode((short) TriggerMode.EXTPULSE.ordinal());
			break;
		default:
			listTriggerModes();
			throw new IllegalArgumentException();
		}
	}

	@Override
	public TriggerMode getTriggerMode() {
		return this.triggermode;
	}

	@Override
	public void listTriggerModes() {
		print("Available trigger modes:");
		for (TriggerMode each : TriggerMode.values()) {
			print(each.name());
		}
	}

	@Override
	public void setTriggerMode(int value) throws Exception {
		areaDetector.setTriggerMode((short) value);
		switch (value) {
		case 0:
			this.triggermode = TriggerMode.AUTO;
			break;
		case 1:
			this.triggermode = TriggerMode.SOFT;
			break;
		case 2:
			this.triggermode = TriggerMode.EXTSOFT;
			break;
		case 3:
			this.triggermode = TriggerMode.EXTPULSE;
			break;
		default:
			listTriggerModes();
			throw new IllegalArgumentException();
		}
	}

	/**
	 * method to print message to the Jython Terminal console.
	 * 
	 * @param msg
	 */
	private void print(String msg) {
		if (InterfaceProvider.getTerminalPrinter() != null) {
			InterfaceProvider.getTerminalPrinter().print(msg);
		}
	}

	@Override
	public void trigger() throws Exception {
		EPICS_CONTROLLER.caputWait(createChannel(triggerPV), 1);
		// try {
		// Thread.sleep(10);
		// } catch (InterruptedException e) {
		// logger.info("{} : trigger wait interrupt.", getName());
		// throw e;
		// }
		EPICS_CONTROLLER.caput(createChannel(triggerPV), 0);
	}

	@Override
	public void stop() throws Exception {
		areaDetector.stopAcquiring();
		hdf.stopCapture();
		tiff.stopCapture();
	}

	/**
	 * calibrated camera readout time for current ADC mode and Pixel Rate.
	 * 
	 * @return readout time
	 * @throws Exception
	 * @throws InterruptedException
	 * @throws CAException
	 * @throws TimeoutException
	 */
	@Override
	public int getReadoutTime() throws Exception {
		int readouttime = 200;
		if (isLive) {
			if (getADCMode() < 1) {
				if (getPixRate() < 16000000) {
					readouttime = getReadout1ADC8Mhz();
				} else {
					readouttime = getReadout1ADC32Mhz();
				}
			} else {
				if (getPixRate() < 16000000) {
					readouttime = getReadout2ADC8Mhz();
				} else {
					readouttime = getReadout2ADC32Mhz();
				}
			}
			print(String.format("PauseTime is %d ADCmode is %d and Pixrate is %d", readouttime, getADCMode(),
					getPixRate()));
		}
		else {
			LoggerFactory.getLogger("PCOControllerV17:"+this.getName()).info("getReadoutTime: Not live!");
		}
		return readouttime;

	}

	@Override
	public int getADCMode() throws TimeoutException, CAException, InterruptedException, Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getADC_MODE_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel("ADC_MODE_RBV", ADC_MODE_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getADCMode", ex);
			throw ex;
		}
	}

	@Override
	public void setADCMode(int value) throws Exception {
		if (!isLive) {
			LoggerFactory.getLogger("PCOControllerV17:"+this.getName()).info("setADCMode: Not live!");
			return;
		}

		try {
			if (config != null) {
				EPICS_CONTROLLER.caputWait(createChannel(config.getADC_MODE().getPv()), value);
			} else {
				EPICS_CONTROLLER.caputWait(getChannel("ADC_MODE", ADC_MODE), value);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setADCMode", ex);
			logger.error("{} : {}", getName(), areaDetector.getStatusMessage_RBV());
			throw ex;
		}
	}

	@Override
	public int getPixRate() throws TimeoutException, CAException, InterruptedException, Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getPIX_RATE_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel("PIX_RATE_RBV", PIX_RATE_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getPixRate", ex);
			throw ex;
		}
	}

	@Override
	public void setPixRate(int value) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getPIX_RATE().getPv()), value);
			} else {
				EPICS_CONTROLLER.caput(getChannel("PIX_RATE", PIX_RATE), value);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setPixRate", ex);
			logger.error("{} : {}", getName(), areaDetector.getStatusMessage_RBV());
			throw ex;
		}
	}

	@Override
	public double getCamRamUsage() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetDouble(createChannel(config.getCAM_RAM_USE_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetDouble(getChannel("CAM_RAM_USE_RBV", CAM_RAM_USE_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getCamRamUsage", ex);
			throw ex;
		}
	}

	@Override
	public double getElectronicTemperature() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetDouble(createChannel(config.getELEC_TEMP_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetDouble(getChannel("ELEC_TEMP_RBV", ELEC_TEMP_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getElectronicTemperature", ex);
			throw ex;
		}
	}

	@Override
	public double getPowerSupplyTemperature() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetDouble(createChannel(config.getPOWER_TEMP_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetDouble(getChannel("POWER_TEMP_RBV", POWER_TEMP_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getPowerSupplyTemperature", ex);
			throw ex;
		}
	}

	@Override
	public int getStorageMode() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getSTORAGE_MODE_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel("STORAGE_MODE_RBV", STORAGE_MODE_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getStorageMode", ex);
			throw ex;
		}
	}

	@Override
	public void setStorageMode(int value) throws Exception {
		if (value != 0 && value != 1) {
			throw new IllegalArgumentException(getName()
					+ ": Input must be 0 for 'Recorder' mode or 1 for 'FIFO buffer' mode");
		}
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getSTORAGE_MODE().getPv()), value);
			} else {
				EPICS_CONTROLLER.caput(getChannel("STORAGE_MODE", STORAGE_MODE), value);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setStorageMode", ex);
			logger.error("{} : {}", getName(), areaDetector.getStatusMessage_RBV());
			throw ex;
		}
	}

	@Override
	public int getRecorderMode() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getRECORDER_MODE_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel("RECORDER_MODE_RBV", RECORDER_MODE_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getRecorderMode", ex);
			throw ex;
		}
	}

	@Override
	public void setRecorderMode(int value) throws Exception {
		if (value != 0 && value != 1) {
			throw new IllegalArgumentException(getName()
					+ ": Input must be 0 for 'Sequence' mode or 1 for 'Ring buffer' mode");
		}
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getRECORDER_MODE().getPv()), value);
			} else {
				EPICS_CONTROLLER.caput(getChannel("RECORDER_MODE", RECORDER_MODE), value);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setRecorderMode", ex);
			logger.error("{} : {}", getName(), areaDetector.getStatusMessage_RBV());
			throw ex;
		}
	}

	@Override
	public int getTimestampMode() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getTIMESTAMP_MODE_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel("TIMESTAMP_MODE_RBV", TIMESTAMP_MODE_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getTimestampMode", ex);
			throw ex;
		}
	}

	@Override
	public void setTimestampMode(int value) throws Exception {
		if (value < 0 && value > 3) {
			throw new IllegalArgumentException(getName()
					+ ": Input must be 0 - None, 1 - BCD, 2 - BCD+ASCII, and 3 - ASCII");
		}
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getTIMESTAMP_MODE().getPv()), value);
			} else {
				EPICS_CONTROLLER.caput(getChannel("TIMESTAMP_MODE", TIMESTAMP_MODE), value);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setTimestampMode", ex);
			logger.error("{} : {}", getName(), areaDetector.getStatusMessage_RBV());
			throw ex;
		}
	}

	@Override
	public int getAcquireMode() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getACQUIRE_MODE_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel("ACQUIRE_MODE_RBV", ACQUIRE_MODE_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getAcquireMode", ex);
			throw ex;
		}
	}

	@Override
	public void setAcquireMode(int value) throws Exception {
		if (!isLive) {
			LoggerFactory.getLogger("PCOControllerV17:"+this.getName()).info("setAcquireMode: Not live!");
			return;
		}

		if (value < 0 && value > 2) {
			throw new IllegalArgumentException(getName()
					+ ": Input must be 0 - Auto, 1 - Ext. enable, and 2 - Ext. trigger");
		}
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getACQUIRE_MODE().getPv()), value);
			} else {
				EPICS_CONTROLLER.caput(getChannel(ACQUIRE_MODE, ACQUIRE_MODE), value);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setAcquireMode", ex);
			logger.error("{} : {}", getName(), areaDetector.getStatusMessage_RBV());
			throw ex;
		}
	}

	@Override
	public int getArmMode() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getARM_MODE_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(ARM_MODE_RBV, ARM_MODE_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getArmMode", ex);
			throw ex;
		}
	}

	@Override
	public void armCamera() throws Exception {
		setArmMode(1);
	}

	@Override
	public void disarmCamera() throws Exception {
		setArmMode(0);
	}

	@Override
	public void setArmMode(int value) throws Exception {
		if (!isLive) {
			LoggerFactory.getLogger("PCOControllerV17:"+this.getName()).info("setArmMode: Not live!");
			return;
		}
		if (value != 0 && value != 1) {
			throw new IllegalArgumentException(getName() + ": Input must be 0 - Disarmed, 1 - Armed");
		}
		try {
			if (config != null) {
				EPICS_CONTROLLER.caputWait(createChannel(config.getARM_MODE().getPv()), value);
			} else {
				EPICS_CONTROLLER.caputWait(getChannel(ARM_MODE, ARM_MODE), value);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setArmMode", ex);
			logger.error("{} : {}", getName(), areaDetector.getStatusMessage_RBV());
			throw ex;
		}
	}

	@Override
	public double getDelayTime() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetDouble(createChannel(config.getDELAY_TIME_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetDouble(getChannel(DELAY_TIME_RBV, DELAY_TIME_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getDelayTime", ex);
			throw ex;
		}
	}

	@Override
	public void setDelayTime(double value) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getDELAY_TIME().getPv()), value);
			} else {
				EPICS_CONTROLLER.caput(getChannel(DELAY_TIME, DELAY_TIME), value);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setDelayTime", ex);
			logger.error("{} : {}", getName(), areaDetector.getStatusMessage_RBV());
			throw ex;
		}
	}

	@Override
	public void setImageMode(int imageMode) throws Exception {
		areaDetector.setImageMode(imageMode);
	}

	@Override
	public void setNumImages(int numimages) throws Exception {
		areaDetector.setNumImages(numimages);
	}

	@Override
	public int getNumImages() throws Exception {
		return areaDetector.getNumImages();
	}

	/**
	 * asynchronous start of camera exposure
	 */
	@Override
	public void acquire() throws Exception {
		areaDetector.startAcquiring();
	}

	@Override
	public void resetAll() throws Exception {
		// The camera needs to be disarmed before the resetAll is called.
		if (isLive) {
			if (isArmed()) {
				disarmCamera();
			}
			// Set the timestamp mode to the initial time stamp mode
			setTimestampMode(initialTimestampMode);
		}
		else {
			LoggerFactory.getLogger("PCOControllerV17:"+this.getName()).info("resetAll: Not live!");
		}
		if (areaDetector != null)
			areaDetector.reset();
		if (draw != null)
			draw.reset();
		if (array != null)
			array.reset();
		if (mjpeg1 != null)
			mjpeg1.reset();
		if (mjpeg2 != null)
			mjpeg2.reset();
		if (nxs != null)
			nxs.reset();
		if (hdf != null)
			hdf.reset();
		if (roi1 != null)
			roi1.reset();
		if (roi2 != null)
			roi2.reset();
		if (stat != null)
			stat.reset();
		if (proc1 != null)
			proc1.reset();
		if (proc2 != null)
			proc2.reset();
		if (tiff != null)
			tiff.reset();
		// initialise area detector and all its plugins

		hdf.setNumRowChunks(areaDetector.getArraySizeY_RBV());
		if (areaDetector.getAcquireState() == 1) {
			areaDetector.stopAcquiring(); // force stop any active camera acquisition on reset, this will disarm camera
			Thread.sleep(3000);
		}
		// makeDetectorReadyForCollection(); // initialise area detector ready for acquisition
		initialisePluginsArrayDimensions();
	}

	public void initialisePluginsArrayDimensions() throws Exception {
		if ((tiff.getPluginBase().isCallbackEnabled() && tiff.getPluginBase().getArraySize0_RBV() == 0)
				|| (hdf.getFile().getPluginBase().isCallbackEnabled() && 
					hdf.getFile().getPluginBase().getArraySize0_RBV() == 0)) {
			if (this.getAreaDetector().getArraySizeX_RBV() == 0
					|| (         tiff.getPluginBase().getArraySize0_RBV() != this.getAreaDetector().getArraySizeX_RBV() || 
						hdf.getFile().getPluginBase().getArraySize0_RBV() !=      getAreaDetector().getArraySizeX_RBV())) {
				// dummy acquisition to ensure all plugin array dimensions are initialised,
				// these must be called at least once after IOC restarts.
				int cachedImgMode = areaDetector.getImageMode();
				areaDetector.setImageMode(ImageMode.SINGLE.ordinal());
				areaDetector.setAcquireTime(0.01);

				areaDetector.startAcquiringSynchronously();
				areaDetector.setImageMode(cachedImgMode);
			}
		}
	}

	@Override
	public void makeDetectorReadyForCollection() throws Exception {
		if (isLive) {
			if (getArmMode() == 1) {
				setArmMode(0); // disarm camera before change parameters
			}
		}
		else {
			LoggerFactory.getLogger("PCOControllerV17:"+this.getName()).info("makeDetectorReadyForCollection: Not live!");
		}
		areaDetector.setArrayCounter(0);
		tiff.stopCapture();
		tiff.getPluginBase().setDroppedArrays(0);
		tiff.getPluginBase().setArrayCounter(0);
		hdf.stopCapture();
		hdf.getFile().getPluginBase().setDroppedArrays(0);
		hdf.getFile().getPluginBase().setArrayCounter(0);
		// set the image mode to Multiple
		areaDetector.setImageMode((short) 0);
		areaDetector.setTriggerMode((short) 2); // EXT + SOFT
		this.setADCMode(1); // two ADC
		this.setAcquireMode(0); // AUTO
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
		// hdf.setFilePath(PathConstructor.createFromDefaultProperty());
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

	@Override
	public int getExposures() throws Exception {
		return areaDetector.getNumExposures();
	}

	@Override
	public NDFileNexus getNxs() {
		return nxs;
	}

	public void setNxs(NDFileNexus nxs) {

		this.nxs = nxs;
	}

	@Override
	public NDOverlay getDraw() {
		return draw;
	}

	public void setDraw(NDOverlay draw) {
		this.draw = draw;
	}

	@Override
	public NDStats getStat() {
		return stat;
	}

	public void setStat(NDStats stat) {
		this.stat = stat;
	}

	@Override
	public NDProcess getProc1() {
		return proc1;
	}

	public void setProc1(NDProcess proc) {
		this.proc1 = proc;
	}

	@Override
	public NDProcess getProc2() {
		return proc2;
	}

	public void setProc2(NDProcess proc) {
		this.proc2 = proc;
	}

	@Override
	public NDROI getRoi1() {
		return roi1;
	}

	public void setRoi1(NDROI roi) {
		this.roi1 = roi;
	}

	@Override
	public NDROI getRoi2() {
		return roi2;
	}

	public void setRoi2(NDROI roi) {
		this.roi2 = roi;
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

			String fullPvName;
			if (pvProvider != null) {
				fullPvName = pvProvider.getPV(pvElementName);
			} else {
				fullPvName = basePVName + pvPostFix;
			}
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
		if (getDeviceName() == null && basePVName == null && pvProvider == null) {
			throw new IllegalArgumentException("''deviceName','basePVName' or pvProvider needs to be declared");
		}
		if (areaDetector == null) {
			throw new IllegalArgumentException("'areaDetector' needs to be declared");
		}
		if (tiff == null) {
			throw new IllegalArgumentException("'tiff' needs to be declared");
		}
		if (roi1 == null) {
			throw new IllegalArgumentException("'roi1' needs to be declared");
		}
		if (roi2 == null) {
			throw new IllegalArgumentException("'roi2' needs to be declared");
		}
		if (proc1 == null) {
			throw new IllegalArgumentException("'proc1' needs to be declared");
		}
		if (proc2 == null) {
			throw new IllegalArgumentException("'proc2' needs to be declared");
		}
		if (stat == null) {
			throw new IllegalArgumentException("'stat' needs to be declared");
		}
		if (draw == null) {
			throw new IllegalArgumentException("'draw' needs to be declared");
		}
		if (array == null) {
			throw new IllegalArgumentException("'array' needs to be declared");
		}
		if (hdf == null) {
			throw new IllegalArgumentException("'hdf' needs to be declared");
		}
		if (nxs == null) {
			logger.warn("'nxs' plugin is not available");
		}
		if (mjpeg1 == null) {
			throw new IllegalArgumentException("'mjpeg1' needs to be declared");
		}
		if (mjpeg2 == null) {
			throw new IllegalArgumentException("'mjpeg2' needs to be declared");
		}
	}

	@Override
	public NDFile getTiff() {
		return tiff;
	}

	public void setTiff(NDFile tiff) {
		this.tiff = tiff;
	}

	@Override
	public ADBase getAreaDetector() {
		return areaDetector;
	}

	public void setAreaDetector(ADBase areaDetector) {
		this.areaDetector = areaDetector;
	}

	public void setHdf(NDFileHDF5 hdf) {
		this.hdf = hdf;
	}

	@Override
	public NDFileHDF5 getHdf() {
		return hdf;
	}

	public void setArray(NDArray array) {
		this.array = array;
	}

	@Override
	public NDArray getArray() {
		return array;
	}

	@Override
	public FfmpegStream getMJpeg1() {
		return mjpeg1;
	}

	public void setMjpeg1(FfmpegStream mjpeg) {
		this.mjpeg1 = mjpeg;
	}

	@Override
	public FfmpegStream getMJpeg2() {
		return mjpeg2;
	}

	public void setMjpeg2(FfmpegStream mjpeg) {
		this.mjpeg2 = mjpeg;
	}

	@Override
	public void setTriggerPV(String triggerPV) {
		this.triggerPV = triggerPV;
	}

	@Override
	public String getTriggerPV() {
		return triggerPV;
	}

	public String getDeviceName() {
		return deviceName;
	}

	public void setDeviceName(String deviceName) throws FactoryException {
		this.deviceName = deviceName;
		initializeConfig();
	}

	private void initializeConfig() throws FactoryException {
		if (deviceName != null) {
			try {
				config = Configurator.getConfiguration(deviceName, PcocamType.class);
			} catch (ConfigurationNotFoundException e) {
				logger.error("EPICS configuration for device {} not found", getDeviceName());
				throw new FactoryException("EPICS configuration for device " + getDeviceName() + " not found.", e);
			} catch (Exception ex) {
				logger.error("EPICS configuration for device {} not found", getDeviceName());
				throw new FactoryException("EPICS configuration for device " + getDeviceName() + " not found.", ex);
			}
		}
	}

	@Override
	public String getBasePVName() {
		return basePVName;
	}

	public void setBasePVName(String basePVName) {
		this.basePVName = basePVName;
	}

	public void setPvProvider(IPVProvider pvProvider) {
		this.pvProvider = pvProvider;
	}

	public IPVProvider getPvProvider() {
		return pvProvider;
	}

	@Override
	public int getReadout1ADC8Mhz() {
		return readout1ADC8Mhz;
	}

	public void setReadout1ADC8Mhz(int readout1adc8Mhz) {
		readout1ADC8Mhz = readout1adc8Mhz;
	}

	@Override
	public int getReadout1ADC32Mhz() {
		return readout1ADC32Mhz;
	}

	public void setReadout1ADC32Mhz(int readout1adc32Mhz) {
		readout1ADC32Mhz = readout1adc32Mhz;
	}

	@Override
	public int getReadout2ADC32Mhz() {
		return readout2ADC32Mhz;
	}

	public void setReadout2ADC32Mhz(int readout2adc32Mhz) {
		readout2ADC32Mhz = readout2adc32Mhz;
	}

	@Override
	public int getReadout2ADC8Mhz() {
		return readout2ADC8Mhz;
	}

	public void setReadout2ADC8Mhz(int readout2adc8Mhz) {
		readout2ADC8Mhz = readout2adc8Mhz;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
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

	@Override
	public int getNumCaptured() throws Exception {
		return hdf.getFile().getNumCaptured_RBV();
	}

	@Override
	public String getFullFileName() throws Exception {
		return hdf.getFile().getFullFileName_RBV();
	}

	@Override
	public String getTiffFullFileName() throws Exception {
		return getTiff().getFullFileName_RBV();
	}

	@Override
	public int getNextFileNumber() throws Exception {
		return tiff.getFileNumber_RBV();
	}

	@Override
	public boolean isArmed() throws Exception {
		return getArmMode() == 1;
	}

	public void setInitialTimestampMode(int initialTimestampMode) {
		this.initialTimestampMode = initialTimestampMode;
	}

}
