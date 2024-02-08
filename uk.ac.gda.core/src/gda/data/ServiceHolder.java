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

import org.eclipse.dawnsci.nexus.appender.INexusFileAppenderService;
import org.eclipse.dawnsci.nexus.device.INexusDeviceService;
import org.eclipse.dawnsci.nexus.scan.NexusScanFileService;
import org.eclipse.dawnsci.nexus.template.NexusTemplateService;
import org.eclipse.scanning.api.device.IScannableDeviceService;
import org.eclipse.scanning.api.scan.IFilePathService;
import org.eclipse.scanning.device.CommonBeamlineDevicesConfiguration;

import gda.data.scan.datawriter.NexusDataWriterConfiguration;
import gda.data.scan.nexus.device.ScannableNexusDeviceConfigurationRegistry;
import uk.ac.diamond.daq.util.logging.deprecation.DeprecationLogger;
import uk.ac.diamond.mq.ISessionService;
import uk.ac.diamond.osgi.services.ServiceProvider;

/**
 * Holds the IRunnableDeviceService which the file registrar uses
 * to register itself with.
 */
@Deprecated(since="9.35", forRemoval=true)
public class ServiceHolder {

	private static final DeprecationLogger logger = DeprecationLogger.getLogger(ServiceHolder.class);

	public static IScannableDeviceService getScannableDeviceService() {
		logger.deprecatedMethod("getScannableDeviceService", "9.35",
				"ServiceProvider.getService(IScannableDeviceService.class");
		return ServiceProvider.getService(IScannableDeviceService.class);
	}

	public void setScannableDeviceService(IScannableDeviceService scannableDeviceService) {
		logger.deprecatedMethod("SetScannableDeviceService()", "9.35", "Nothing, just remove");
	}

	public static ScannableNexusDeviceConfigurationRegistry getScannableNexusDeviceConfigurationRegistry() {
		logger.deprecatedMethod("getScannableDeviceService()", "9.35",
				"ServiceProvider.getService(ScannableNexusDeviceConfigurationRegistry.class");
		return ServiceProvider.getService(ScannableNexusDeviceConfigurationRegistry.class);
	}

	public void setScannableNexusDeviceConfigurationRegistry(ScannableNexusDeviceConfigurationRegistry registry) {
		logger.deprecatedMethod("setScannableNexusDeviceConfiguration()", "9.35", "Nothing, just remove");
	}

    public static IFilePathService getFilePathService() {
		logger.deprecatedMethod("getFilePathService()", "9.35",
				"ServiceProvider.getService(IFilePathService.class)");
		return ServiceProvider.getService(IFilePathService.class);
	}

	public void setFilePathService(IFilePathService filePathService) {
		logger.deprecatedMethod("setFilePathService()", "9.35", "Nothing, just remove");
	}

	public static NexusTemplateService getNexusTemplateService() {
		logger.deprecatedMethod("getNexusTemplateService()", "9.35",
				"ServiceProvider.getService(NexusTemplateService.class)");
		return ServiceProvider.getService(NexusTemplateService.class);
	}

	public void setNexusTemplateService(NexusTemplateService nexusTemplateService) {
		logger.deprecatedMethod("setNexusTemplateService()", "9.35", "Nothing, just remove");
	}

	public static INexusDeviceService getNexusDeviceService() {
		logger.deprecatedMethod("getNexusDeviceService()", "9.35",
				"ServiceProvider.getService(INexusDeviceService.class)");
		return ServiceProvider.getService(INexusDeviceService.class);
	}

	public void setNexusDeviceService(INexusDeviceService nexusDeviceService) {
		logger.deprecatedMethod("setNexusDeviceService()", "9.35", "Nothing, just remove");
	}

	public static NexusScanFileService getNexusScanFileService() {
		logger.deprecatedMethod("getNexusScanFileService()", "9.35",
				"ServiceProvider.getService(NexusScanFileService.class)");
		return ServiceProvider.getService(NexusScanFileService.class);
	}

	public void setNexusScanFileService(NexusScanFileService nexusScanFileService) {
		logger.deprecatedMethod("setNexusScanFileService()", "9.35", "Nothing, just remove");
	}

	public static INexusFileAppenderService getNexusFileAppenderService() {
		logger.deprecatedMethod("getNexusFileAppendService()", "9.35",
				"ServiceProvider.getService(INexusFileAppenderService.class)");
		return ServiceProvider.getService(INexusFileAppenderService.class);
	}

	public void setNexusFileAppenderService(INexusFileAppenderService nexusFileAppenderService) {
		logger.deprecatedMethod("setNexusFileAppenderService()", "9.35", "Nothing, just remove");
	}

	public static NexusDataWriterConfiguration getNexusDataWriterConfiguration() {
		logger.deprecatedMethod("getNexusDataWriterConfiguration()", "9.35",
				"NexusDataWriterConfiguration.getInstance()");
		return NexusDataWriterConfiguration.getInstance();
	}

	public void setNexusWriterConfiguration(NexusDataWriterConfiguration nexusDataWriterConfiguration) {
		logger.deprecatedMethod("setNexusWriterConfiguration()", "9.35", "Nothing, just remove");
	}

	public static CommonBeamlineDevicesConfiguration getCommonBeamlineDevicesConfiguration() {
		logger.deprecatedMethod("getCommonBeamlineDeviceConfiguration()", "9.35",
				"CommonBeamlineDevicesConfiguration.getInstance()");
		return CommonBeamlineDevicesConfiguration.getInstance();
	}

	public void setCommonBeamlineDevicesConfiguration(CommonBeamlineDevicesConfiguration commonBeamlineDevicesConfiguration) {
		logger.deprecatedMethod("setCommonBeamlineDevicesConfiguration()", "9.35", "Nothing, just remove");
	}

	public static ISessionService getSessionService() {
		logger.deprecatedMethod("getSessionService()", "9.35",
				"ServiceProvider.getService(ISessionService.class");
		return ServiceProvider.getService(ISessionService.class);
	}

	public void setSessionService(ISessionService sessionService) {
		logger.deprecatedMethod("setSessionService()", "9.35", "Nothing, just remove");
	}

}
