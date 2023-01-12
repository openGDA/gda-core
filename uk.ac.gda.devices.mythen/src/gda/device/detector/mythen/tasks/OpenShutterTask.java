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

package gda.device.detector.mythen.tasks;

import gda.device.DeviceException;
import gda.device.Scannable;

public class OpenShutterTask implements ScanTask {

	private static final String SHUTTER_OPEN_POSITION = "OPEN";
	
	private Scannable shutterScannable;
	
	/**
	 * Sets the {@link Scannable} representing the shutter.
	 * 
	 * @param shutterScannable the shutter scannable
	 */
	public void setShutterScannable(Scannable shutterScannable) {
		this.shutterScannable = shutterScannable;
	}

	@Override
	public void run() throws DeviceException {
		shutterScannable.moveTo(SHUTTER_OPEN_POSITION);
	}

}
