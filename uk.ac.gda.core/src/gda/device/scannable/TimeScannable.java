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

import java.text.SimpleDateFormat;
import java.util.Date;

import gda.device.DeviceException;

/**
 * Returns the current date/time
 *
 * @see java.text.SimpleDateFormat
 */
public class TimeScannable extends ScannableBase {

	private String dateformat = "EEE, d MMM yyyy HH:mm:ss z";
	private boolean useUtcMillis = false;

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
		if (useUtcMillis) {
			return System.currentTimeMillis();
		} else {
			Date now = new Date();
			SimpleDateFormat formatter = new SimpleDateFormat(dateformat);
			return formatter.format(now);
		}
	}

	@Override
	public boolean isBusy() throws DeviceException {
		return false;
	}

	public boolean isUseUtcMillis() {
		return useUtcMillis;
	}

	public void setUseUtcMillis(boolean useUtcMillis) {
		if (useUtcMillis) {
			outputFormat = new String[] {"%d"};
		} else {
			outputFormat = new String[] {"%s"};
		}
		this.useUtcMillis = useUtcMillis;
	}
}
