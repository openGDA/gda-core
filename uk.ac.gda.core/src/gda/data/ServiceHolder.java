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

package gda.data;

import org.eclipse.dawnsci.nexus.template.NexusTemplateService;
import org.eclipse.scanning.api.device.IRunnableDeviceService;
import org.eclipse.scanning.api.scan.IFilePathService;

/**
 * Holds the IRunnableDeviceService which the file registrar uses
 * to register itself with.
 */
public class ServiceHolder {


	// This is provided by OSGi. Making static usually gives the best
	// opportunity that one of the load cycles will have set the service.
	private static IRunnableDeviceService runnableDeviceService;

	public static IRunnableDeviceService getRunnableDeviceService() {
		return runnableDeviceService;
	}

	public void setRunnableDeviceService(IRunnableDeviceService rs) {
		runnableDeviceService = rs;
	}

    private static IFilePathService filePathService;

	public static IFilePathService getFilePathService() {
		return filePathService;
	}

	public void setFilePathService(IFilePathService filePathService) {
		ServiceHolder.filePathService = filePathService;
	}

	private static NexusTemplateService nexusTemplateService;

	public static NexusTemplateService getNexusTemplateService() {
		return nexusTemplateService;
	}

	public void setNexusTemplateService(NexusTemplateService nexusTemplateService) {
		ServiceHolder.nexusTemplateService = nexusTemplateService;
	}

}
