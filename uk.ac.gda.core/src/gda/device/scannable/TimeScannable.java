/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council
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

package gda.device.scannable;

import gda.device.DeviceException;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Returns the current date/time
 * 
 * @see java.text.SimpleDateFormat
 */
public class TimeScannable extends ScannableBase {
	
	String dateformat = "EEE, d MMM yyyy HH:mm:ss z";
	
	/**
	 * 
	 */
	public TimeScannable(){
		this.inputNames = new String[]{};
		this.extraNames = new String[]{"time"};
		this.outputFormat = new String[]{"%s"};
	}
	
	/**
	 * @param name 
	 */
	public TimeScannable(String name){
		this();
		this.setName(name);
	}

	@Override
	public void asynchronousMoveTo(Object position) throws DeviceException {
		// 
	}

	@Override
	public Object getPosition() throws DeviceException {
		SimpleDateFormat formatter = new SimpleDateFormat(dateformat);
		Date now = new Date();
		return formatter.format(now);
	}

	@Override
	public boolean isBusy() throws DeviceException {
		return false;
	}
}
