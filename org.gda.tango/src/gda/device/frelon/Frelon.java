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

package gda.device.frelon;

import fr.esrf.Tango.DevFailed;
import gda.device.base.Base;

public interface Frelon extends Base {

	
	short getESPIABoardNumber() throws DevFailed;
	
	enum ImageMode {
		FRAME_TRANSFERT, FULL_FRAME
	}
	
	ImageMode getImageMode() throws DevFailed;
	
	void setImageMode(ImageMode imageMode) throws DevFailed;	
	
	enum InputChannels {
		I1,I2,I3,I4,I1_2,I3_4,I1_3,I2_4,I1_2_3_4
	}
	
	InputChannels getInputChannels() throws DevFailed;
	
	void setInputChannels(InputChannels inputChannels) throws DevFailed;	
	
	boolean isE2VCorrectionActive() throws DevFailed;
	
	void setE2VCorrectionActive(boolean activate) throws DevFailed;

	enum ROIMode {
		NONE, SLOW, FAST, KINETIC
	}
	
	ROIMode getROIMode() throws DevFailed;
	
	void setROIMode(ROIMode roiMode) throws DevFailed;	
	
	int getROIBinOffset() throws DevFailed;
	
//	void setROIBinOffset(long roiBinOffset) throws DevFailed;
	
	enum SPB2Config {
		PRECISION, SPEED
	}
	
	SPB2Config getSPB2Config() throws DevFailed;
	
	void setSPB2Config(SPB2Config sPB2Config) throws DevFailed;	
	
	int getSeqStatus() throws DevFailed;
	/**
	 * send a command through the serial line
	 * @param cmd
	 * @return command's result
	 * @throws DevFailed
	 */
	String[] execSerialCommand(String cmd) throws DevFailed;
	/**
	 * reset the espia link
	 * @throws DevFailed
	 */
	void resetLink() throws DevFailed;

	void setROIBinOffset(int roiBinOffset) throws DevFailed;
}
