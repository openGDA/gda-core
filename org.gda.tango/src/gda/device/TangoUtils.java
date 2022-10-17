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

package gda.device;

import fr.esrf.Tango.DevError;
import fr.esrf.Tango.DevFailed;

public class TangoUtils {
	static public DeviceException createDeviceExceptionStack(DevFailed failed) {
		DeviceException prev = null;
		for (int i = failed.errors.length - 1; i >= 0; i--) {
			DevError error = failed.errors[i];
			DeviceException devEx = new DeviceException("_DevError [getReason()=" + error.reason + ", getSeverity()="
					+ error.severity.toString() + "," + " getDesc()=" + error.desc + ", getOrigin()=" + error.origin
					+ "]", prev);
			prev = devEx;
		}
		return prev;
	}	
}
