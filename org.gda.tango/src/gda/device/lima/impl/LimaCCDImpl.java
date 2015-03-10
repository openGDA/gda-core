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

package gda.device.lima.impl;

import fr.esrf.Tango.DevError;
import fr.esrf.Tango.DevFailed;
import fr.esrf.Tango.ErrSeverity;
import fr.esrf.TangoApi.DeviceAttribute;
import fr.esrf.TangoApi.DeviceData;
import gda.device.DeviceException;
import gda.device.base.impl.BaseImpl;
import gda.device.lima.LimaBin;
import gda.device.lima.LimaCCD;
import gda.device.lima.LimaFlip;
import gda.device.lima.LimaROIInt;
import gda.device.lima.LimaSavingHeaderDelimiter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

public class LimaCCDImpl extends BaseImpl implements LimaCCD, InitializingBean {
	
	private static final Logger logger = LoggerFactory.getLogger(LimaCCDImpl.class);

	static final String ACQ_STATUS_VAL_FAULT = "Fault";
	static final String ACQ_STATUS_VAL_RUNNING = "Running";
	static final String ACQ_STATUS_VAL_READY = "Ready";
	 static final String ATTRIBUTE_SAVING_NEXT_NUMBER = "saving_next_number";
	 static final String ATTRIBUTE_WRITE_STATISTICS = "write_statistics";
	 static final String ATTRIBUTE_READY_FOR_NEXT_ACQ = "ready_for_next_acq";
	 static final String ATTRIBUTE_READY_FOR_NEXT_IMAGE = "ready_for_next_image";
	 static final String ATTRIBUTE_ACQ_NB_FRAMES = "acq_nb_frames";
	 static final String ATTRIBUTE_LIMA_TYPE = "lima_type";
	 static final String ATTRIBUTE_CAMERA_TYPE = "camera_type";
	 static final String ATTRIBUTE_CAMERA_MODEL = "camera_model";
	 static final String ATTRIBUTE_ACQ_STATUS = "acq_status";
	 static final String ATTRIBUTE_DEBUG_MODULES = "debug_modules";
	 static final String ATTRIBUTE_DEBUG_TYPES = "debug_types";
	 static final String ATTRIBUTE_LAST_IMAGE_SAVED = "last_image_saved";
	 static final String ATTRIBUTE_LAST_IMAGE_READY = "last_image_ready";
	 static final String ATTRIBUTE_SAVING_HEADER_DELIMITER = "saving_header_delimiter";
	 static final String ATTRIBUTE_SAVING_COMMON_HEADER = "saving_common_header";
	 static final String ATTRIBUTE_SAVING_FRAME_PER_FILE = "saving_frame_per_file";
	 static final String ATTRIBUTE_SHUTTER_MODE = "shutter_mode";
	 static final String ATTRIBUTE_IMAGE_FLIP = "image_flip";
	 static final String ATTRIBUTE_IMAGE_BIN = "image_bin";
	 static final String ATTRIBUTE_IMAGE_ROI = "image_roi";
	 static final String ATTRIBUTE_IMAGE_HEIGHT = "image_height";
	 static final String ATTRIBUTE_IMAGE_WIDTH = "image_width";
	 static final String ATTRIBUTE_IMAGE_TYPE = "image_type";
	 static final String ATTRIBUTE_ACC_LIVE_TIME = "acc_live_time";
	 static final String ATTRIBUTE_ACC_DEAD_TIME = "acc_dead_time";
	 static final String ATTRIBUTE_ACC_TIME_MODE = "acc_time_mode";
	 static final String ATTRIBUTE_ACC_MAX_EXPO_TIME = "acc_max_expotime";
	 static final String ATTRIBUTE_ACC_NB_FRAMES = "acc_nb_frames";
	 static final String ATTRIBUTE_ACC_EXPO_TIME = "acc_expotime";
	 static final String ATTRIBUTE_ACQ_EXPO_TIME = "acq_expo_time";
	 static final String ATTRIBUTE_LATENCY_TIME = "latency_time";
	 static final String ATTRIBUTE_ACQ_MODE = "acq_mode";
	 static final String ATTRIBUTE_ACQ_TRIGGER_MODE = "acq_trigger_mode";
	 static final String ATTRIBUTE_SAVING_OVERWRITE_POLICY = "saving_overwrite_policy";
	 static final String ATTRIBUTE_SAVING_FORMAT = "saving_format";
	 static final String ATTRIBUTE_SAVING_DIRECTORY = "saving_directory";
	 static final String ATTRIBUTE_SAVING_PREFIX = "saving_prefix";
	 static final String ATTRIBUTE_SAVING_SUFFIX = "saving_suffix";
	 static final String ATTRIBUTE_SAVING_MODE = "saving_mode";
	 static final String ATTRIBUTE_SHUTTER_OPEN_TIME = "shutter_open_time";

	 static final String COMMAND_SET_IMAGE_HEADER = "setImageHeader";
	 static final String COMMAND_RESET = "reset";
	 static final String COMMAND_OPEN_SHUTTER_MANUAL = "openShutterManual";
	 static final String COMMAND_CLOSE_SHUTTER_MANUAL = "closeShutterManual";
	 static final String COMMAND_GET_IMAGE = "getImage";

	 static final String COMMAND_PREPARE_ACQ = "prepareAcq";
	static final String COMMAND_START_ACQ = "startAcq";
	 static final String COMMAND_STOP_ACQ = "stopAcq";

	 static final String SAVING_OVERWRITE_POLICY_OVERWRITE = "OVERWRITE";
	 static final String SAVING_OVERWRITE_POLICY_APPEND = "APPEND";
	 static final String SAVING_OVERWRITE_POLICY_ABORT = "ABORT";
	 static final String SAVING_FORMAT_RAW = "RAW";
	 static final String SAVING_FORMAT_EDF = "EDF";
	 static final String SAVING_FORMAT_CBF = "CBF";
	 static final String SAVING_MODE_MANUAL = "MANUAL";
	 static final String SAVING_MODE_AUTO_HEADER = "AUTO_HEADER";
	 static final String SAVING_MODE_AUTO_FRAME = "AUTO_FRAME";
	 static final String SHUTTER_MODE_MANUAL = "MANUAL";
	 static final String SHUTTER_MODE_AUTO_SEQUENCE = "AUTO_SEQUENCE";
	 static final String SHUTTER_MODE_AUTO_FRAME = "AUTO_FRAME";
	 static final String IMAGE_TYPE_BPP8 = "Bpp8";
	 static final String IMAGE_TYPE_BPP8S = "Bpp8S";
	 static final String IMAGE_TYPE_BPP10 = "Bpp10";
	 static final String IMAGE_TYPE_BPP10S = "Bpp10S";
	 static final String IMAGE_TYPE_BPP12 = "Bpp12";
	 static final String IMAGE_TYPE_BPP12S = "Bpp12S";
	 static final String IMAGE_TYPE_BPP14 = "Bpp14";
	 static final String IMAGE_TYPE_BPP14S = "Bpp14S";
	 static final String IMAGE_TYPE_BPP16 = "Bpp16";
	 static final String IMAGE_TYPE_BPP16S = "Bpp16S";
	 static final String IMAGE_TYPE_BPP32 = "Bpp32";
	 static final String IMAGE_TYPE_BPP32S = "Bpp32S";
	 static final String ACC_TIME_MODE_REAL = "REAL";
	 static final String ACC_TIME_MODE_LIVE = "LIVE";
	 static final String ACQ_TRIGGER_MODE_INTERNAL_TRIGGER_MULTI = "INTERNAL_TRIGGER_MULTI";
	 static final String ACQ_TRIGGER_MODE_INTERNAL_TRIGGER = "INTERNAL_TRIGGER";
	 static final String ACQ_TRIGGER_MODE_EXTERNAL_TRIGGER_MULTI = "EXTERNAL_TRIGGER_MULTI";
	 static final String ACQ_TRIGGER_MODE_EXTERNAL_TRIGGER = "EXTERNAL_TRIGGER";
	 static final String ACQ_TRIGGER_MODE_EXTERNAL_START_STOP = "EXTERNAL_START_STOP";
	 static final String ACQ_TRIGGER_MODE_EXTERNAL_GATE = "EXTERNAL_GATE";
	 static final String ACQMODE_ACCUMULATION = "ACCUMULATION";
	 static final String ACQMODE_CONCATENATION = "CONCATENATION";
	 static final String ACQMODE_SINGLE = "SINGLE";

	@Override
	public String getLimaType() throws DevFailed {
		return getTangoDeviceProxy().getAttributeAsString(ATTRIBUTE_LIMA_TYPE);
	}

	@Override
	public String getCameraType() throws DevFailed {
		return getTangoDeviceProxy().getAttributeAsString(ATTRIBUTE_CAMERA_TYPE);
	}

	@Override
	public String getCameraModel() throws DevFailed {
		return getTangoDeviceProxy().getAttributeAsString(ATTRIBUTE_CAMERA_MODEL);
	}

	@Override
	public AcqStatus getAcqStatus() throws DevFailed {
		String val = getTangoDeviceProxy().getAttributeAsString(ATTRIBUTE_ACQ_STATUS);
		if (val.equals(ACQ_STATUS_VAL_READY))
			return AcqStatus.READY;
		if (val.equals(ACQ_STATUS_VAL_RUNNING))
			return AcqStatus.RUNNING;
		if (val.equals(ACQ_STATUS_VAL_FAULT))
			return AcqStatus.FAULT;
		throw new DevFailed(new DevError[] { new DevError(getTangoDeviceProxy().toString()
				+ " : acq_status returned unknown value :" + val, ErrSeverity.ERR, "", "") });
	}

	@Override
	public AcqMode getAcqMode() throws DevFailed {
		String val = getTangoDeviceProxy().getAttributeAsString(ATTRIBUTE_ACQ_MODE);
		if (val.equals(ACQMODE_SINGLE))
			return AcqMode.SINGLE;
		if (val.equals(ACQMODE_CONCATENATION))
			return AcqMode.CONCATENATION;
		if (val.equals(ACQMODE_ACCUMULATION))
			return AcqMode.ACCUMULATION;
		throw new DevFailed(new DevError[] { new DevError(getTangoDeviceProxy().toString()
				+ " : acq_mode returned unknown value :" + val, ErrSeverity.ERR, "", "") });
	}

	@Override
	public void setAcqMode(AcqMode mode) throws DevFailed {
		String val = "";
		switch (mode) {
		case SINGLE:
			val = ACQMODE_SINGLE;
			break;
		case ACCUMULATION:
			val = ACQMODE_ACCUMULATION;
			break;
		case CONCATENATION:
			val = ACQMODE_CONCATENATION;
			break;
		}
		getTangoDeviceProxy().setAttribute(ATTRIBUTE_ACQ_MODE, val);
	}

	@Override
	public int getAcqNbFrames() throws DevFailed {
		return getTangoDeviceProxy().getAttributeAsInt(ATTRIBUTE_ACQ_NB_FRAMES);
	}

	@Override
	public void setAcqNbFrames(Integer acqNbFrames) throws DevFailed {
		getTangoDeviceProxy().setAttribute(ATTRIBUTE_ACQ_NB_FRAMES, acqNbFrames);
	}

	@Override
	public AcqTriggerMode getAcqTriggerMode() throws DevFailed {
		String val = getTangoDeviceProxy().getAttributeAsString(ATTRIBUTE_ACQ_TRIGGER_MODE);
		if (val.equals(ACQ_TRIGGER_MODE_EXTERNAL_GATE))
			return AcqTriggerMode.EXTERNAL_GATE;
		if (val.equals(ACQ_TRIGGER_MODE_EXTERNAL_START_STOP))
			return AcqTriggerMode.EXTERNAL_START_STOP;
		if (val.equals(ACQ_TRIGGER_MODE_EXTERNAL_TRIGGER))
			return AcqTriggerMode.EXTERNAL_TRIGGER;
		if (val.equals(ACQ_TRIGGER_MODE_EXTERNAL_TRIGGER_MULTI))
			return AcqTriggerMode.EXTERNAL_TRIGGER_MULTI;
		if (val.equals(ACQ_TRIGGER_MODE_INTERNAL_TRIGGER))
			return AcqTriggerMode.INTERNAL_TRIGGER;
		if (val.equals(ACQ_TRIGGER_MODE_INTERNAL_TRIGGER_MULTI))
			return AcqTriggerMode.INTERNAL_TRIGGER_MULTI;
		throw new DevFailed(new DevError[] { new DevError(getTangoDeviceProxy().toString()
				+ " : acq_trigger_mode returned unknown value :" + val, ErrSeverity.ERR, "", "") });
	}

	@Override
	public void setAcqTriggerMode(AcqTriggerMode mode) throws DevFailed {
		String val = "";
		switch (mode) {
		case EXTERNAL_GATE:
			val = ACQ_TRIGGER_MODE_EXTERNAL_GATE;
			break;
		case EXTERNAL_START_STOP:
			val = ACQ_TRIGGER_MODE_EXTERNAL_START_STOP;
			break;
		case EXTERNAL_TRIGGER:
			val = ACQ_TRIGGER_MODE_EXTERNAL_TRIGGER;
			break;
		case EXTERNAL_TRIGGER_MULTI:
			val = ACQ_TRIGGER_MODE_EXTERNAL_TRIGGER_MULTI;
			break;
		case INTERNAL_TRIGGER:
			val = ACQ_TRIGGER_MODE_INTERNAL_TRIGGER;
			break;
		case INTERNAL_TRIGGER_MULTI:
			val = ACQ_TRIGGER_MODE_INTERNAL_TRIGGER_MULTI;
			break;
		}
		getTangoDeviceProxy().setAttribute(ATTRIBUTE_ACQ_TRIGGER_MODE, val);
	}

	@Override
	public double getLatencyTime() throws DevFailed {
		return getTangoDeviceProxy().getAttributeAsDouble(ATTRIBUTE_LATENCY_TIME);
	}

	@Override
	public void setLatencyTime(double latencyTime) throws DevFailed {
		getTangoDeviceProxy().setAttribute(ATTRIBUTE_LATENCY_TIME, latencyTime);
	}

	@Override
	public double getAcqExpoTime() throws DevFailed {
		return getTangoDeviceProxy().getAttributeAsDouble(ATTRIBUTE_ACQ_EXPO_TIME);
	}

	@Override
	public void setAcqExpoTime(double acqExpoTime) throws DevFailed {
		getTangoDeviceProxy().setAttribute(ATTRIBUTE_ACQ_EXPO_TIME, acqExpoTime);
	}

	@Override
	public double getAccExpoTime() throws DevFailed {
		return getTangoDeviceProxy().getAttributeAsDouble(ATTRIBUTE_ACC_EXPO_TIME);
	}

	@Override
	public int getAccNbFrames() throws DevFailed {
		return getTangoDeviceProxy().getAttributeAsInt(ATTRIBUTE_ACC_NB_FRAMES);
	}

	@Override
	public double getAccMaxExpoTime() throws DevFailed {
		return getTangoDeviceProxy().getAttributeAsDouble(ATTRIBUTE_ACC_MAX_EXPO_TIME);
	}

	@Override
	public void setAccMaxExpoTime(double accMaxExpoTime) throws DevFailed {
		getTangoDeviceProxy().setAttribute(ATTRIBUTE_ACC_MAX_EXPO_TIME, accMaxExpoTime);
	}

	@Override
	public AccTimeMode getAccTimeMode() throws DevFailed {
		String val = getTangoDeviceProxy().getAttributeAsString(ATTRIBUTE_ACC_TIME_MODE);
		if (val.equals(ACC_TIME_MODE_LIVE))
			return AccTimeMode.LIVE;
		if (val.equals(ACC_TIME_MODE_REAL))
			return AccTimeMode.REAL;
		throw new DevFailed(new DevError[] { new DevError(getTangoDeviceProxy().toString()
				+ " : acc_time_mode returned unknown value :" + val, ErrSeverity.ERR, "", "") });

	}

	@Override
	public void setAccTimeMode(AccTimeMode accTimeMode) throws DevFailed {
		String val = "";
		switch (accTimeMode) {
		case LIVE:
			val = ACC_TIME_MODE_LIVE;
			break;
		case REAL:
			val = ACC_TIME_MODE_REAL;
			break;
		}
		getTangoDeviceProxy().setAttribute(ATTRIBUTE_ACC_TIME_MODE, val);
	}

	@Override
	public double getAccDeadTime() throws DevFailed {
		return getTangoDeviceProxy().getAttributeAsDouble(ATTRIBUTE_ACC_DEAD_TIME);
	}

	@Override
	public double getAccLiveTime() throws DevFailed {
		return getTangoDeviceProxy().getAttributeAsDouble(ATTRIBUTE_ACC_LIVE_TIME);
	}

	@Override
	public ImageType getImageType() throws DevFailed {
		String val = getTangoDeviceProxy().getAttributeAsString(ATTRIBUTE_IMAGE_TYPE);
		if (val.equals(IMAGE_TYPE_BPP8))
			return ImageType.BPP8;
		if (val.equals(IMAGE_TYPE_BPP8S))
			return ImageType.BPP8S;
		if (val.equals(IMAGE_TYPE_BPP10))
			return ImageType.BPP10;
		if (val.equals(IMAGE_TYPE_BPP10S))
			return ImageType.BPP10S;
		if (val.equals(IMAGE_TYPE_BPP12))
			return ImageType.BPP12;
		if (val.equals(IMAGE_TYPE_BPP12S))
			return ImageType.BPP12S;
		if (val.equals(IMAGE_TYPE_BPP14))
			return ImageType.BPP14;
		if (val.equals(IMAGE_TYPE_BPP14S))
			return ImageType.BPP14S;
		if (val.equals(IMAGE_TYPE_BPP16))
			return ImageType.BPP16;
		if (val.equals(IMAGE_TYPE_BPP16S))
			return ImageType.BPP16S;
		if (val.equals(IMAGE_TYPE_BPP32))
			return ImageType.BPP32;
		if (val.equals(IMAGE_TYPE_BPP32S))
			return ImageType.BPP32S;
		throw new DevFailed(new DevError[] { new DevError(getTangoDeviceProxy().toString()
				+ " : image_type returned unknown value :" + val, ErrSeverity.ERR, "", "") });
	}

	@Override
	public long getImageWidth() throws DevFailed {
		return getTangoDeviceProxy().getAttributeAsLong(ATTRIBUTE_IMAGE_WIDTH);
	}

	@Override
	public long getImageHeight() throws DevFailed {
		return getTangoDeviceProxy().getAttributeAsLong(ATTRIBUTE_IMAGE_HEIGHT);
	}

	@Override
	public LimaROIInt getImageROIInt() throws DevFailed {
		int[] val = getTangoDeviceProxy().getAttributeAsIntArray(ATTRIBUTE_IMAGE_ROI);
		if (val.length < 5) // 4 plus null at the end
		{
			throw new DevFailed(new DevError[] { new DevError(getTangoDeviceProxy().toString() + " : "
					+ ATTRIBUTE_IMAGE_ROI + " returned length < 5", ErrSeverity.ERR, "", "") });
		}
		LimaROIIntImpl limaROI = new LimaROIIntImpl();
		limaROI.setBeginX(val[0]);
		limaROI.setEndX(val[1]);
		limaROI.setBeginY(val[2]);
		limaROI.setEndY(val[3]);
		return limaROI;
	}

	@Override
	public void setImageROIInt(LimaROIInt limaROI) throws DevFailed {
		int[] val = new int[] { limaROI.getBeginX(), limaROI.getEndX(), limaROI.getBeginY(), limaROI.getEndY() };
		getTangoDeviceProxy().setAttribute(ATTRIBUTE_IMAGE_ROI, val, 4, 1);
	}

	@Override
	public LimaBin getImageBin() throws DevFailed {
		long[] val = getTangoDeviceProxy().getAttributeAsULongArray(ATTRIBUTE_IMAGE_BIN);
		if (val.length < 3) // 2 plus null at the end
		{
			throw new DevFailed(new DevError[] { new DevError(getTangoDeviceProxy().toString() + " : "
					+ ATTRIBUTE_IMAGE_BIN + " returned length < 3", ErrSeverity.ERR, "", "") });
		}

		LimaBinImpl limaBin = new LimaBinImpl();
		limaBin.setBinX(val[0]);
		limaBin.setBinY(val[1]);
		return limaBin;
	}

	@Override
	public void setImageBin(LimaBin limaBin) throws DevFailed {
		long[] val = new long[] { limaBin.getBinX(), limaBin.getBinY() };
		getTangoDeviceProxy().setAttribute(ATTRIBUTE_IMAGE_BIN, val, 2, 1);
	}

	@Override
	public void setImageBin(long xBinValue, long yBinValue) throws DeviceException {
		DeviceAttribute deviceAttribute = new DeviceAttribute(ATTRIBUTE_IMAGE_BIN, 2, 1);
		deviceAttribute.insert_ul(new long[]{xBinValue, yBinValue});
		try {
			getTangoDeviceProxy().write_attribute(deviceAttribute);
		} catch (DevFailed e) {
			logger.error("Unable to set image bin value to detector", e);
			throw new DeviceException("Unable to set image bin value to detector", e);
		}
	}

	@Override
	public LimaFlip getImageFlip() throws DevFailed {
		boolean[] val = getTangoDeviceProxy().getAttributeAsBooleanArray(ATTRIBUTE_IMAGE_FLIP);
		if (val.length < 3) // 2 plus true at the end
		{
			throw new DevFailed(new DevError[] { new DevError(getTangoDeviceProxy().toString() + " : "
					+ ATTRIBUTE_IMAGE_FLIP + " returned length < 3", ErrSeverity.ERR, "", "") });
		}
		LimaFlipImpl limaFlipImpl = new LimaFlipImpl();
		limaFlipImpl.setFlipX(val[0]);
		limaFlipImpl.setFlipY(val[1]);
		return limaFlipImpl;
	}

	@Override
	public void setImageFlip(LimaFlip limaFlip) throws DevFailed {
		boolean[] val = new boolean[] { limaFlip.getFlipX(), limaFlip.getFlipY() };
		getTangoDeviceProxy().setAttribute(ATTRIBUTE_IMAGE_FLIP, val, 2, 1);
	}

	@Override
	public ShutterMode getShutterMode() throws DevFailed {
		String val = getTangoDeviceProxy().getAttributeAsString(ATTRIBUTE_SHUTTER_MODE);
		if (val.equals(SHUTTER_MODE_AUTO_FRAME))
			return ShutterMode.AUTO_FRAME;
		if (val.equals(SHUTTER_MODE_AUTO_SEQUENCE))
			return ShutterMode.AUTO_SEQUENCE;
		if (val.equals(SHUTTER_MODE_MANUAL))
			return ShutterMode.MANUAL;
		throw new DevFailed(new DevError[] { new DevError(getTangoDeviceProxy().toString()
				+ " : shutter_mode returned unknown value :" + val, ErrSeverity.ERR, "", "") });
	}

	@Override
	public void setShutterMode(ShutterMode shutterMode) throws DevFailed {
		String val = "";
		switch (shutterMode) {
		case AUTO_FRAME:
			val = SHUTTER_MODE_AUTO_FRAME;
			break;
		case AUTO_SEQUENCE:
			val = SHUTTER_MODE_AUTO_SEQUENCE;
			break;
		case MANUAL:
			val = SHUTTER_MODE_MANUAL;
			break;
		}
		getTangoDeviceProxy().setAttribute(ATTRIBUTE_SHUTTER_MODE, val);
	}

	@Override
	public double getShutterOpenTime() throws DevFailed {
		return getTangoDeviceProxy().getAttributeAsDouble(ATTRIBUTE_SHUTTER_OPEN_TIME);
	}

	@Override
	public void setShutterOpenTime(double shutterOpenTime) throws DevFailed {
		getTangoDeviceProxy().setAttribute(ATTRIBUTE_SHUTTER_OPEN_TIME, shutterOpenTime);
	}

	@Override
	public SavingMode getSavingMode() throws DevFailed {
		String val = getTangoDeviceProxy().getAttributeAsString(ATTRIBUTE_SAVING_MODE);
		if (val.equals(SAVING_MODE_AUTO_FRAME))
			return SavingMode.AUTO_FRAME;
		if (val.equals(SAVING_MODE_AUTO_HEADER))
			return SavingMode.AUTO_HEADER;
		if (val.equals(SAVING_MODE_MANUAL))
			return SavingMode.MANUAL;
		throw new DevFailed(new DevError[] { new DevError(getTangoDeviceProxy().toString()
				+ " : saving_mode returned unknown value :" + val, ErrSeverity.ERR, "", "") });
	}

	@Override
	public void setSavingMode(SavingMode savingMode) throws DevFailed {
		String val = "";
		switch (savingMode) {
		case AUTO_FRAME:
			val = SAVING_MODE_AUTO_FRAME;
			break;
		case AUTO_HEADER:
			val = SAVING_MODE_AUTO_HEADER;
			break;
		case MANUAL:
			val = SAVING_MODE_MANUAL;
			break;
		}
		getTangoDeviceProxy().setAttribute(ATTRIBUTE_SAVING_MODE, val);
	}

	@Override
	public String getSavingDirectory() throws DevFailed {
		return getTangoDeviceProxy().getAttributeAsString(ATTRIBUTE_SAVING_DIRECTORY);
	}

	@Override
	public void setSavingDirectory(String savingDirectory) throws DevFailed {
		getTangoDeviceProxy().setAttribute(ATTRIBUTE_SAVING_DIRECTORY, savingDirectory);
	}

	@Override
	public String getSavingPrefix() throws DevFailed {
		return getTangoDeviceProxy().getAttributeAsString(ATTRIBUTE_SAVING_PREFIX);
	}

	@Override
	public void setSavingPrefix(String savingPrefix) throws DevFailed {
		getTangoDeviceProxy().setAttribute(ATTRIBUTE_SAVING_PREFIX, savingPrefix);
	}

	@Override
	public String getSavingSuffix() throws DevFailed {
		return getTangoDeviceProxy().getAttributeAsString(ATTRIBUTE_SAVING_SUFFIX);
	}

	@Override
	public void setSavingSuffix(String savingSuffix) throws DevFailed {
		getTangoDeviceProxy().setAttribute(ATTRIBUTE_SAVING_SUFFIX, savingSuffix);
	}

	@Override
	public int getSavingNextNumber() throws DevFailed {
		return getTangoDeviceProxy().getAttributeAsInt(ATTRIBUTE_SAVING_NEXT_NUMBER);
	}

	@Override
	public void setSavingNextNumber(int savingNextNumber) throws DevFailed {
		getTangoDeviceProxy().setAttribute(ATTRIBUTE_SAVING_NEXT_NUMBER, savingNextNumber);
	}

	@Override
	public SavingFormat getSavingFormat() throws DevFailed {
		String val = getTangoDeviceProxy().getAttributeAsString(ATTRIBUTE_SAVING_FORMAT);
		if (val.equals(SAVING_FORMAT_CBF))
			return SavingFormat.CBF;
		if (val.equals(SAVING_FORMAT_EDF))
			return SavingFormat.EDF;
		if (val.equals(SAVING_FORMAT_RAW))
			return SavingFormat.RAW;
		throw new DevFailed(new DevError[] { new DevError(getTangoDeviceProxy().toString()
				+ " : saving_format returned unknown value :" + val, ErrSeverity.ERR, "", "") });

	}

	@Override
	public void setSavingFormat(SavingFormat savingFormat) throws DevFailed {
		String val = "";
		switch (savingFormat) {
		case CBF:
			val = SAVING_FORMAT_CBF;
			break;
		case EDF:
			val = SAVING_FORMAT_EDF;
			break;
		case RAW:
			val = SAVING_FORMAT_RAW;
			break;
		}
		getTangoDeviceProxy().setAttribute(ATTRIBUTE_SAVING_FORMAT, val);
	}

	@Override
	public SavingOverwritePolicy getSavingOverwritePolicy() throws DevFailed {
		String val = getTangoDeviceProxy().getAttributeAsString(ATTRIBUTE_SAVING_OVERWRITE_POLICY);
		if (val.equals(SAVING_OVERWRITE_POLICY_ABORT))
			return SavingOverwritePolicy.ABORT;
		if (val.equals(SAVING_OVERWRITE_POLICY_APPEND))
			return SavingOverwritePolicy.APPEND;
		if (val.equals(SAVING_OVERWRITE_POLICY_OVERWRITE))
			return SavingOverwritePolicy.OVERWRITE;
		throw new DevFailed(new DevError[] { new DevError(getTangoDeviceProxy().toString()
				+ " : saving_overwrite_policy returned unknown value :" + val, ErrSeverity.ERR, "", "") });
	}

	@Override
	public void setSavingOverwritePolicy(SavingOverwritePolicy savingOverwritePolicy) throws DevFailed {
		String val = "";
		switch (savingOverwritePolicy) {
		case ABORT:
			val = SAVING_OVERWRITE_POLICY_ABORT;
			break;
		case APPEND:
			val = SAVING_OVERWRITE_POLICY_APPEND;
			break;
		case OVERWRITE:
			val = SAVING_OVERWRITE_POLICY_OVERWRITE;
			break;
		}
		getTangoDeviceProxy().setAttribute(ATTRIBUTE_SAVING_OVERWRITE_POLICY, val);
	}

	@Override
	public int getSavingFramePerFile() throws DevFailed {
		return getTangoDeviceProxy().getAttributeAsInt(ATTRIBUTE_SAVING_FRAME_PER_FILE);
	}

	@Override
	public void setSavingFramePerFile(int savingFramePerFile) throws DevFailed {
		getTangoDeviceProxy().setAttribute(ATTRIBUTE_SAVING_FRAME_PER_FILE, savingFramePerFile);
	}

	@Override
	public String[] getSavingCommonHeader() throws DevFailed {
		return getTangoDeviceProxy().getAttributeAsStringArray(ATTRIBUTE_SAVING_COMMON_HEADER);
	}

	@Override
	public void setSavingCommonHeader(String[] savingCommonHeader) throws DevFailed {
		getTangoDeviceProxy().setAttribute(ATTRIBUTE_SAVING_COMMON_HEADER, savingCommonHeader,
				savingCommonHeader.length, 1);
	}

	@Override
	public LimaSavingHeaderDelimiter getLimaSavingHeaderDelimiter() throws DevFailed {
		String[] attributeAsStringArray = getTangoDeviceProxy().getAttributeAsStringArray(
				ATTRIBUTE_SAVING_HEADER_DELIMITER);

		if (attributeAsStringArray.length != 3) {
			throw new DevFailed(new DevError[] { new DevError(getTangoDeviceProxy().toString() + " : "
					+ ATTRIBUTE_SAVING_HEADER_DELIMITER + " returned length != 3", ErrSeverity.ERR, "", "") });
		}

		LimaSavingHeaderDelimiterImpl limaSavingHeaderDelimiterImpl = new LimaSavingHeaderDelimiterImpl();
		limaSavingHeaderDelimiterImpl.setKeyHeaderDelimiter(attributeAsStringArray[0]);
		limaSavingHeaderDelimiterImpl.setEntryHeaderDelimiter(attributeAsStringArray[1]);
		limaSavingHeaderDelimiterImpl.setImageNumberHeaderDelimiter(attributeAsStringArray[1]);
		return limaSavingHeaderDelimiterImpl;
	}

	@Override
	public void setLimaSavingHeaderDelimiter(LimaSavingHeaderDelimiter limaSavingHeaderDelimiter) throws DevFailed {
		String[] val = new String[] { limaSavingHeaderDelimiter.getKeyHeaderDelimiter(),
				limaSavingHeaderDelimiter.getEntryHeaderDelimiter(),
				limaSavingHeaderDelimiter.getImageNumberHeaderDelimiter() };
		getTangoDeviceProxy().setAttribute(ATTRIBUTE_SAVING_HEADER_DELIMITER, val, 3, 1);
	}

	@Override
	public int getLastImageReady() throws DevFailed {
		return getTangoDeviceProxy().getAttributeAsInt(ATTRIBUTE_LAST_IMAGE_READY);
	}

	@Override
	public int getLastImageSaved() throws DevFailed {
		return getTangoDeviceProxy().getAttributeAsInt(ATTRIBUTE_LAST_IMAGE_SAVED);
	}

	@Override
	public boolean getReadyForNextImage() throws DevFailed {
		return getTangoDeviceProxy().getAttributeAsBoolean(ATTRIBUTE_READY_FOR_NEXT_IMAGE);
	}

	@Override
	public boolean getReadyForNextAcq() throws DevFailed {
		return getTangoDeviceProxy().getAttributeAsBoolean(ATTRIBUTE_READY_FOR_NEXT_ACQ);
	}

	@Override
	public double[] getWriteStatistics() throws DevFailed {
		return getTangoDeviceProxy().getAttributeAsDoubleArray(ATTRIBUTE_WRITE_STATISTICS);
	}

	@Override
	public String[] getDebugModules() throws DevFailed {
		return getTangoDeviceProxy().getAttributeAsStringArray(ATTRIBUTE_DEBUG_MODULES);
	}

	@Override
	public void setDebugModules(String[] debugModules) throws DevFailed {
		getTangoDeviceProxy().setAttribute(ATTRIBUTE_DEBUG_MODULES, debugModules, debugModules.length, 1);
	}

	@Override
	public String[] getDebugTypes() throws DevFailed {
		return getTangoDeviceProxy().getAttributeAsStringArray(ATTRIBUTE_DEBUG_TYPES);
	}

	@Override
	public void setDebugTypes(String[] debugTypes) throws DevFailed {
		getTangoDeviceProxy().setAttribute(ATTRIBUTE_DEBUG_TYPES, debugTypes, debugTypes.length, 1);
	}

	private void sendSimpleCommand(String command) throws DevFailed {
		getTangoDeviceProxy().sendSimpleCommand(command);
	}

	@Override
	public void prepareAcq() throws DevFailed {
		sendSimpleCommand(COMMAND_PREPARE_ACQ);
	}

	@Override
	public void startAcq() throws DevFailed {
		sendSimpleCommand(COMMAND_START_ACQ);
	}

	@Override
	public void stopAcq() throws DevFailed {
		sendSimpleCommand(COMMAND_STOP_ACQ);
	}

	@Override
	public void setImageHeader(String[] headers) throws DevFailed {
		DeviceData argin = new DeviceData();
		argin.insert(headers);
		getTangoDeviceProxy().command_inout(COMMAND_SET_IMAGE_HEADER, argin);
	}

	@Override
	public byte[] getImage(int imageNumber) throws DevFailed {
		DeviceData argin = new DeviceData();
		argin.insert(imageNumber);
		DeviceData argout = getTangoDeviceProxy().command_inout(COMMAND_GET_IMAGE, argin);
		return argout.extractByteArray();
		/*
		 * int width = 516; int height = 516; int n = 0; short[] shortData = new short[width * height * 1]; for (int j =
		 * 0; j < byteData.length; j += 2, n++) { shortData[n] = (short) (((byteData[j + 1] & 0xff) << 8) | (byteData[j]
		 * & 0xff)); } return new ShortDataset(shortData, width, height);
		 */}

	@Override
	public void closeShutterManual() throws DevFailed {
		sendSimpleCommand(COMMAND_CLOSE_SHUTTER_MANUAL);
	}

	@Override
	public void closeOpenManual() throws DevFailed {
		sendSimpleCommand(COMMAND_OPEN_SHUTTER_MANUAL);
	}

	@Override
	public void reset() throws DevFailed {
		sendSimpleCommand(COMMAND_RESET);
	}
}
