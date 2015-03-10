/*-
 * Copyright © 2013 Diamond Light Source Ltd.
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

package gda.device.monitor;

import gda.device.DeviceException;
import gda.device.Monitor;
import gda.device.scannable.ScannableUtils;
import gda.device.scannable.TangoScannable;

public class TangoMonitor extends TangoScannable implements Monitor{

	double waitForSystemTimeInMillis = 0.0;

	@Override
	public void asynchronousMoveTo(Object position) throws DeviceException {
		//This method is for operating the object and telling it to do some
		//work, but monitors should be passive, so if you are trying to
		// operate it then you are doing something wrong! 

		double waitTime = ScannableUtils.objectToArray(position)[0];
		waitForSystemTimeInMillis = System.currentTimeMillis() + 1000
				* waitTime;
	}

	@Override
	public String getUnit() throws DeviceException {
		return "";
	}

	@Override
	public int getElementCount() throws DeviceException {
		return 1;
	}
}
