/*-
 * Copyright © 2017 Diamond Light Source Ltd.
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

package gda.data.fileregistrar;

import org.eclipse.scanning.api.device.IRunnableDeviceService;

/**
 * Holds the IRunnableDeviceService which the file registrar uses
 * to register itself with.
 */
public class FileRegistrarServiceHolder {


	// This is provided by OSGi. Making static usually gives the best
	// opportunity that one of the load cycles will have set the service.
	private static IRunnableDeviceService runnableDeviceService;

	public static IRunnableDeviceService getRunnableDeviceService() {
		return runnableDeviceService;
	}

	public static void setRunnableDeviceService(IRunnableDeviceService rs) {
		runnableDeviceService = rs;
	}


}
