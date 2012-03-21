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

package gda.device.detector.countertimer;

import gda.device.DeviceException;

import java.util.Arrays;

import org.apache.commons.lang.ArrayUtils;

/**
 * Version of TFGXspress2 which returns the raw numbers from the TFG memory.
 * <p>
 * This should not be used when using regions of interest mode in the xspress.
 */
public class TfgXspress2Raw extends TfgXspress2 {

	@Override
	public String[] getOutputFormat() {
		String[] formats = new String[getExtraNames().length];
		Arrays.fill(formats, super.getOutputFormat()[0]);
		return formats;
	}
	
	/**
	 * element0all, element0reset, element0counts, element0time etc...
	 */
	@Override
	public String[] getExtraNames() {
		String[] channelNames = this.extraNames;
		channelNames = (String[]) ArrayUtils.removeElement(channelNames, channelNames[channelNames.length - 1]);
		String[] myChannelNames = new String[channelNames.length * 4];
		for (int i = 0; i <  channelNames.length;i++){
			myChannelNames[i*4] = (channelNames[i] + "all");
			myChannelNames[i*4+1] = (channelNames[i] + "reset");
			myChannelNames[i*4+2] = (channelNames[i] + "counts");
			myChannelNames[i*4+3] = (channelNames[i] + "time");
		}
		return myChannelNames;
	}
    
	@Override
	public Object readout() throws DeviceException {
		return xspress.getRawScalerData();
	}
	
	/**
	 * Override ScannableBase to work within scans
	 */
	@Override
	public String[] getInputNames() {
		if (slave) {
			return new String[] {};
		}
		return new String[] { "time" };
	}
}
