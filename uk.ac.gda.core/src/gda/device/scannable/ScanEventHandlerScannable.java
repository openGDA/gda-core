/*-
 * Copyright © 2012 Diamond Light Source Ltd.
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;

/**
 * Class to be derived from to allow quick creation of so called zero input zero extranames scannable
 * Useful when you want to respond to a scan event
 */
public abstract class ScanEventHandlerScannable extends ScannableBase {

	protected static final Logger logger = LoggerFactory.getLogger(ScanEventHandlerScannable.class);

	protected void log(String msg){
		logger.info(msg);
	}

	@Override
	public boolean isBusy() throws DeviceException {
		return false;
	}
	
	public ScanEventHandlerScannable(){
		setInputNames(new String[0]);
		setExtraNames(new String[0]);
		setOutputFormat(new String[0]);
	}

	@Override
	public void rawAsynchronousMoveTo(Object position) throws DeviceException {
		//do nothing
	}

	@Override
	public Object rawGetPosition() throws DeviceException {
		return null;
	}

	@Override
	public String toFormattedString() {
		return "ScanEventHandler:"+getName();
	}

	@Override
	public void waitWhileBusy() throws DeviceException, InterruptedException {
		//do nothing as never busy
	}

}
