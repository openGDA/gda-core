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

import org.eclipse.core.runtime.Platform;
import org.eclipse.dawnsci.nexus.appender.INexusFileAppenderService;
import org.eclipse.dawnsci.nexus.device.INexusDeviceService;
import org.eclipse.dawnsci.nexus.scan.NexusScanFileService;
import org.eclipse.dawnsci.nexus.template.NexusTemplateService;
import org.eclipse.scanning.api.device.IRunnableDeviceService;
import org.eclipse.scanning.api.device.IScannableDeviceService;
import org.eclipse.scanning.api.scan.IFilePathService;
import org.eclipse.scanning.device.CommonBeamlineDevicesConfiguration;

import gda.data.scan.datawriter.NexusDataWriterConfiguration;
import uk.ac.gda.common.activemq.ISessionService;
import uk.ac.gda.core.GDACoreActivator;

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

	public void setRunnableDeviceService(IRunnableDeviceService runnableDeviceService) {
		ServiceHolder.runnableDeviceService = runnableDeviceService;
	}

	private static IScannableDeviceService scannableDeviceService;

	public static IScannableDeviceService getScannableDeviceService() {
		return scannableDeviceService;
	}

	public void setScannableDeviceService(IScannableDeviceService scannableDeviceService) {
		ServiceHolder.scannableDeviceService = scannableDeviceService;
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

	private static INexusDeviceService nexusDeviceService;

	public static INexusDeviceService getNexusDeviceService() {
		return nexusDeviceService;
	}

	public void setNexusDeviceService(INexusDeviceService nexusDeviceService) {
		ServiceHolder.nexusDeviceService = nexusDeviceService;
	}

	private static NexusScanFileService nexusScanFileService;

	public static NexusScanFileService getNexusScanFileService() {
		return nexusScanFileService;
	}

	public void setNexusScanFileService(NexusScanFileService nexusScanFileService) {
		ServiceHolder.nexusScanFileService = nexusScanFileService;
	}

	private static INexusFileAppenderService nexusFileAppenderService;

	public static INexusFileAppenderService getNexusFileAppenderService() {
		return nexusFileAppenderService;
	}

	public void setNexusFileAppenderService(INexusFileAppenderService nexusFileAppenderService) {
		ServiceHolder.nexusFileAppenderService = nexusFileAppenderService;
	}

	private static volatile NexusDataWriterConfiguration nexusDataWriterConfiguration;

	public void setNexusWriterConfiguration(NexusDataWriterConfiguration nexusDataWriterConfiguration) {
		// Note: this method is not typically called by OSGi as this bean is declared in spring which is loaded after OSGi wiring
		// it should be set by unit tests that require it
		ServiceHolder.nexusDataWriterConfiguration = nexusDataWriterConfiguration;
	}

	public static NexusDataWriterConfiguration getNexusDataWriterConfiguration() {
		if (nexusDataWriterConfiguration == null) {
			synchronized (ServiceHolder.class) { // safe double-checked locking idiom
				if (nexusDataWriterConfiguration == null) {
					nexusDataWriterConfiguration = GDACoreActivator.getService(NexusDataWriterConfiguration.class)
							.orElseGet(NexusDataWriterConfiguration::new); // create an empty bean if not defined in spring
				}
			}
		}

		return nexusDataWriterConfiguration;
	}

	private static volatile CommonBeamlineDevicesConfiguration commonBeamlineDevicesConfiguration;

	public void setCommonBeamlineDevicesConfiguration(CommonBeamlineDevicesConfiguration commonBeamlineDevicesConfiguration) {
		// note that this method is not typically set by OSGi as this bean is declared in spring which is loaded after OSGi wiring
		// it should be set by unit tests that require it
		ServiceHolder.commonBeamlineDevicesConfiguration = commonBeamlineDevicesConfiguration;
	}

	public static CommonBeamlineDevicesConfiguration getCommonBeamlineDevicesConfiguration() {
		if (commonBeamlineDevicesConfiguration == null) {
			synchronized (ServiceHolder.class) { // safe double-checked locking idiom
				if (commonBeamlineDevicesConfiguration == null && Platform.isRunning()) {
					commonBeamlineDevicesConfiguration = GDACoreActivator.getService(CommonBeamlineDevicesConfiguration.class).orElse(null);
					// note: this bean is optional, so this field will be null if the bean is not defined in spring
				}
			}
		}

		return commonBeamlineDevicesConfiguration;
	}

	private static ISessionService sessionService;

	public static ISessionService getSessionService() {
		return sessionService;
	}

	public void setSessionService(ISessionService sessionService) {
		ServiceHolder.sessionService = sessionService;
	}

}
