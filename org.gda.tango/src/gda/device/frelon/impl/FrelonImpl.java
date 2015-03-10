/*-
 * Copyright © 2014 Diamond Light Source Ltd.
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

package gda.device.frelon.impl;

import fr.esrf.Tango.DevError;
import fr.esrf.Tango.DevFailed;
import fr.esrf.Tango.ErrSeverity;
import gda.device.base.impl.BaseImpl;
import gda.device.frelon.Frelon;

public class FrelonImpl extends BaseImpl implements Frelon {

	private static final String SPEED = "SPEED";
	private static final String PRECISION = "PRECISION";
	private static final String SPB2_CONFIG = "spb2_config";
	private static final String SEQ_STATUS = "seq_status";
	private static final String ROI_BIN_OFFSET = "roi_bin_offset";
	private static final String SLOW = "SLOW";
	private static final String KINETIC = "KINETIC";
	private static final String FAST = "FAST";
	private static final String NONE = "NONE";
	private static final String ROI_MODE = "roi_mode";
	private static final String OFF = "OFF";
	private static final String E2V_CORRECTION = "e2v_correction";
	private static final String ON = "ON";
	private static final String INPUT_CHANNEL = "input_channel";
	private static final String FULL_FRAME = "FULL FRAME";
	private static final String FRAME_TRANSFERT = "FRAME TRANSFERT";//TODO THIS SEEMS INCORRECT
	private static final String ATTRIBUTE_ESPIA_DEV_NB = "espia_dev_nb";
	private static final String ATTRIBUTE_IMAGE_MODE = "image_mode";

	@Override
	public short getESPIABoardNumber() throws DevFailed {
		return getTangoDeviceProxy().getAttributeAsShort(ATTRIBUTE_ESPIA_DEV_NB);
	}

	@Override
	public void setESPIABoardNumber(short eSPIABoardNumber) throws DevFailed {
		getTangoDeviceProxy().setAttribute(ATTRIBUTE_ESPIA_DEV_NB, eSPIABoardNumber);
	}

	@Override
	public ImageMode getImageMode() throws DevFailed {
		String val = getTangoDeviceProxy().getAttributeAsString(ATTRIBUTE_IMAGE_MODE);
		if (val.equals(FRAME_TRANSFERT))
			return ImageMode.FRAME_TRANSFERT;
		if (val.equals(FULL_FRAME))
			return ImageMode.FULL_FRAME;
		throw new DevFailed(new DevError[] { new DevError(getTangoDeviceProxy().toString() + " : "
				+ ATTRIBUTE_IMAGE_MODE + " returned unknown value :" + val, ErrSeverity.ERR, "", "") });
	}

	@Override
	public void setImageMode(ImageMode imageMode) throws DevFailed {
		String val = "";
		switch (imageMode) {
		case FRAME_TRANSFERT:
			val = FRAME_TRANSFERT;
			break;
		case FULL_FRAME:
			val = FULL_FRAME;
			break;
		}
		getTangoDeviceProxy().setAttribute(ATTRIBUTE_IMAGE_MODE, val);

	}

	@Override
	public InputChannels getInputChannels() throws DevFailed {
		String val = getTangoDeviceProxy().getAttributeAsString(INPUT_CHANNEL);
		if (val.equals("1"))
			return InputChannels.I1;
		if (val.equals("2"))
			return InputChannels.I2;
		if (val.equals("3"))
			return InputChannels.I3;
		if (val.equals("4"))
			return InputChannels.I4;
		if (val.equals("1-2"))
			return InputChannels.I1_2;
		if (val.equals("3-4"))
			return InputChannels.I3_4;
		if (val.equals("1-3"))
			return InputChannels.I1_3;
		if (val.equals("1-2-3-4"))
			return InputChannels.I1_2_3_4;
		throw new DevFailed(new DevError[] { new DevError(getTangoDeviceProxy().toString() + " : " + INPUT_CHANNEL
				+ " returned unknown value :" + val, ErrSeverity.ERR, "", "") });
	}

	@Override
	public void setInputChannels(InputChannels inputChannels) throws DevFailed {
		String val = "";
		switch (inputChannels) {
		case I1:
			val = "1";
			break;
		case I2:
			val = "2";
			break;
		case I3:
			val = "3";
			break;
		case I4:
			val = "4";
			break;
		case I1_2:
			val = "1-2";
			break;
		case I3_4:
			val = "3-4";
			break;
		case I1_3:
			val = "1-3";
			break;
		case I2_4:
			val = "2-4";
			break;
		case I1_2_3_4:
			val = "1-2-3-4";
			break;
		}
		getTangoDeviceProxy().setAttribute(INPUT_CHANNEL, val);

	}

	@Override
	public boolean isE2VCorrectionActive() throws DevFailed {
		return getTangoDeviceProxy().getAttributeAsString(E2V_CORRECTION).equals(ON);
	}

	@Override
	public void setE2VCorrectionActive(boolean activate) throws DevFailed {
		getTangoDeviceProxy().setAttribute(E2V_CORRECTION, activate ? ON : OFF);

	}

	@Override
	public ROIMode getROIMode() throws DevFailed {
		String val = getTangoDeviceProxy().getAttributeAsString(ROI_MODE);
		if (val.equals(NONE))
			return ROIMode.NONE;
		if (val.equals(SLOW))
			return ROIMode.SLOW;
		if (val.equals(FAST))
			return ROIMode.FAST;
		if (val.equals(KINETIC))
			return ROIMode.KINETIC;
		throw new DevFailed(new DevError[] { new DevError(getTangoDeviceProxy().toString() + " : " + ROI_MODE
				+ " returned unknown value :" + val, ErrSeverity.ERR, "", "") });
	}

	@Override
	public void setROIMode(ROIMode roiMode) throws DevFailed {
		String val = "";
		switch (roiMode) {
		case NONE:
			val = NONE;
			break;
		case FAST:
			val = FAST;
			break;
		case KINETIC:
			val = KINETIC;
			break;
		case SLOW:
			val = SLOW;
			break;
		}
		getTangoDeviceProxy().setAttribute(ROI_MODE, val);

	}

	@Override
	public long getROIBinOffset() throws DevFailed {
		return getTangoDeviceProxy().getAttributeAsLong(ROI_BIN_OFFSET);
	}

	@Override
	public void setROIBinOffset(long roiBinOffset) throws DevFailed {
		getTangoDeviceProxy().setAttribute(ROI_BIN_OFFSET, roiBinOffset);

	}

	@Override
	public SPB2Config getSPB2Config() throws DevFailed {
		String val = getTangoDeviceProxy().getAttributeAsString(SPB2_CONFIG);
		if (val.equals(PRECISION))
			return SPB2Config.PRECISION;
		if (val.equals(SPEED))
			return SPB2Config.SPEED;
		throw new DevFailed(new DevError[] { new DevError(getTangoDeviceProxy().toString() + " : " + SPB2_CONFIG
				+ " returned unknown value :" + val, ErrSeverity.ERR, "", "") });
	}

	@Override
	public void setSPB2Config(SPB2Config sPB2Config) throws DevFailed {
		String val = "";
		switch (sPB2Config) {
		case PRECISION:
			val = PRECISION;
			break;
		case SPEED:
			val = SPEED;
			break;
		}
		getTangoDeviceProxy().setAttribute(SPB2_CONFIG, val);

	}

	@Override
	public long getSeqStatus() throws DevFailed {
		return getTangoDeviceProxy().getAttributeAsLong(SEQ_STATUS);
	}

}
