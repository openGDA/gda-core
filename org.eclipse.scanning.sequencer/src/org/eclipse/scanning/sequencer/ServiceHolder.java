/*-
 *******************************************************************************
 * Copyright (c) 2011, 2016 Diamond Light Source Ltd.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthew Gerring - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.scanning.sequencer;

import org.eclipse.dawnsci.analysis.api.io.ILoaderService;
import org.eclipse.dawnsci.analysis.api.persistence.IMarshallerService;
import org.eclipse.dawnsci.analysis.api.persistence.IPersistenceService;
import org.eclipse.dawnsci.analysis.api.processing.IOperationService;
import org.eclipse.dawnsci.nexus.INexusFileFactory;
import org.eclipse.dawnsci.nexus.device.INexusDeviceService;
import org.eclipse.dawnsci.nexus.scan.NexusScanFileService;
import org.eclipse.scanning.api.device.IDeviceWatchdogService;
import org.eclipse.scanning.api.device.IRunnableDeviceService;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.points.IPointGeneratorService;
import org.eclipse.scanning.api.scan.IFilePathService;
import org.eclipse.scanning.api.scan.IParserService;
import org.eclipse.scanning.device.CommonBeamlineDevicesConfiguration;

import uk.ac.diamond.daq.api.messaging.MessagingService;
import uk.ac.diamond.daq.util.logging.deprecation.DeprecationLogger;
import uk.ac.diamond.osgi.services.ServiceProvider;

@Deprecated(since="9.35", forRemoval=true)
public class ServiceHolder {

	private static final DeprecationLogger logger = DeprecationLogger.getLogger(ServiceHolder.class);

	@Deprecated(since="9.35", forRemoval=true)
	public static NexusScanFileService getNexusScanFileService() {
		logger.deprecatedMethod("getNexusScanFileService()", "9.35",
			  "ServiceProvider.getService(NexusScanFileService.class)");
		return ServiceProvider.getService(NexusScanFileService.class);
	}

	@SuppressWarnings("unused")
	public void setNexusScanFileService(NexusScanFileService nexusScanFileService) {
		logger.deprecatedMethod("setNexusScanFileService()", "9.35", "Nothing, just remove");
	}

	public static INexusDeviceService getNexusDeviceService() {
		logger.deprecatedMethod("getNexusDeviceService()", "9.35",
			  "ServiceProvider.getService(INexusDeviceService.class)");
		return ServiceProvider.getService(INexusDeviceService.class);
	}

	@SuppressWarnings("unused")
	public void setNexusDeviceService(INexusDeviceService nexusDeviceService) {
		logger.deprecatedMethod("setNexusDeviceService()", "9.35", "Nothing, just remove");
	}

	public static INexusFileFactory getNexusFileFactory() {
		logger.deprecatedMethod("getNexusFileFactory()", "9.35",
			  "ServiceProvider.getService(INexusFileFactory.class)");
		return ServiceProvider.getService(INexusFileFactory.class);
	}

	@SuppressWarnings("unused")
	public void setNexusFileFactory(INexusFileFactory nexusFileFactory) {
		logger.deprecatedMethod("setNexusFileFactory()", "9.35", "Nothing, just remove");
	}

	public static IOperationService getOperationService() {
		logger.deprecatedMethod("getOperationService()", "9.35",
			  "ServiceProvider.getService(IOperationService.class)");
		return ServiceProvider.getService(IOperationService.class);
	}

	@SuppressWarnings("unused")
	public void setOperationService(IOperationService operationService) {
		logger.deprecatedMethod("setOperationService()", "9.35", "Nothing, just remove");
	}

	public static IDeviceWatchdogService getWatchdogService() {
		logger.deprecatedMethod("getWatchdogService()", "9.35",
			  "ServiceProvider.getService(IDeviceWatchdogService.class)");
		return ServiceProvider.getService(IDeviceWatchdogService.class);
	}

	@SuppressWarnings("unused")
	public void setWatchdogService(IDeviceWatchdogService watchdogService) {
		logger.deprecatedMethod("setWatchdogService()", "9.35", "Nothing, just remove");
	}

	public static IPersistenceService getPersistenceService() {
		logger.deprecatedMethod("getPersistenceService()", "9.35",
			  "ServiceProvider.getService(IPersistenceService.class)");
		return ServiceProvider.getService(IPersistenceService.class);
	}

	@SuppressWarnings("unused")
	public void setPersistenceService(IPersistenceService persistenceService) {
		logger.deprecatedMethod("setPersistenceService()", "9.35", "Nothing, just remove");
	}

	public static ILoaderService getLoaderService() {
		logger.deprecatedMethod("getLoaderService()", "9.35",
			  "ServiceProvider.getService(ILoaderService.class)");
		return ServiceProvider.getService(ILoaderService.class);
	}

	@SuppressWarnings("unused")
	public void setLoaderService(ILoaderService loaderService) {
		logger.deprecatedMethod("setLoaderService()", "9.35", "Nothing, just remove");
	}

	public static IEventService getEventService() {
		logger.deprecatedMethod("getEventService()", "9.35",
			  "ServiceProvider.getService(IEventService.class)");
		return ServiceProvider.getService(IEventService.class);
	}

	@SuppressWarnings("unused")
	public void setEventService(IEventService eventService) {
		logger.deprecatedMethod("setEventService()", "9.35", "Nothing, just remove");
	}

	public static IFilePathService getFilePathService() {
		logger.deprecatedMethod("getFilePathService()", "9.35",
			  "ServiceProvider.getService(IFilePathService.class");
		return ServiceProvider.getService(IFilePathService.class);
	}

	@SuppressWarnings("unused")
	public void setFilePathService(IFilePathService filePathService) {
		logger.deprecatedMethod("setFilePathService()", "9.35", "Nothing, just remove");
	}

	public static IRunnableDeviceService getRunnableDeviceService() {
		logger.deprecatedMethod("getRunnableDeviceService()", "9.35",
			  "ServiceProvider.getService(IRunnableDeviceService.class)");
		return ServiceProvider.getService(IRunnableDeviceService.class);
	}

	@SuppressWarnings("unused")
	public void setRunnableDeviceService(IRunnableDeviceService runnableDeviceService) {
		logger.deprecatedMethod("setRunnableDeviceService()", "9.35", "Nothing, just remove");
	}

	public static IPointGeneratorService getGeneratorService() {
		logger.deprecatedMethod("getGeneratorService()", "9.35",
			  "ServiceProvider.getService(IPointGeneratorService.class)");
		return ServiceProvider.getService(IPointGeneratorService.class);
	}

	@SuppressWarnings("unused")
	public void setGeneratorService(IPointGeneratorService generatorService) {
		logger.deprecatedMethod("setGeneratorService()", "9.35", "Nothing, just remove");
	}

	public static IParserService getParserService() {
		logger.deprecatedMethod("getParserService()", "9.35",
			  "ServiceProvider.getService(IParserService.class)");
		return ServiceProvider.getService(IParserService.class);
	}

	@SuppressWarnings("unused")
	public void setParserService(IParserService parserService) {
		logger.deprecatedMethod("setParserService()", "9.35", "Nothing, just remove");
	}

	public static IMarshallerService getMarshallerService() {
		logger.deprecatedMethod("getMarshallerService()", "9.35",
			  "ServiceProvider.getService(IMarshallerService.class)");
		return ServiceProvider.getService(IMarshallerService.class);
	}

	@SuppressWarnings("unused")
	public void setMarshallerService(IMarshallerService marshallerService) {
		logger.deprecatedMethod("setMarshallerService()", "9.35", "Nothing, just remove");
	}

	public static MessagingService getMessagingService() {
		logger.deprecatedMethod("getMessagingService()", "9.35",
			  "ServiceProvider.getService(MessagingService.class)");
		return ServiceProvider.getService(MessagingService.class);
	}

	@SuppressWarnings("unused")
	public void setMessagingService(MessagingService messagingService) {
		logger.deprecatedMethod("setMessagingService()", "9.35", "Nothing, just remove");
	}

	@SuppressWarnings("unused")
	public void setCommonBeamlineDevicesConfiguration(CommonBeamlineDevicesConfiguration commonBeamlineDevicesConfiguration) {
		logger.deprecatedMethod("setCommonBeamlineDevicesConfiguration()", "9.35", "Nothing, just remove");
	}

	public static CommonBeamlineDevicesConfiguration getCommonBeamlineDevicesConfiguration() {
		logger.deprecatedMethod("getCommonBeamlineDevicesConfiguration()", "9.35",
			  "ServiceProvider.getService(CommonBeamlineDevicesConfiguration.class)");
		return ServiceProvider.getService(CommonBeamlineDevicesConfiguration.class);
	}

	/**
	 * Used to provide services when tests running in non-OSGi mode.
	 */
	@SuppressWarnings("unused")
	public void setTestServices(ILoaderService ls,
			IOperationService oservice) {
		logger.deprecatedMethod("setTestServices()", "9.35", "Nothing, just remove");
	}

	/**
	 * Used to provide services when tests running in non-OSGi mode.
	 */
	@SuppressWarnings("unused")
	public void setTestServices(ILoaderService ls,
			IOperationService oservice, IFilePathService fpservice,
			IPointGeneratorService gService) {
		logger.deprecatedMethod("setTestServices()", "9.35", "Nothing, just remove");
	}
}
