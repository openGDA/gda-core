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
import org.eclipse.dawnsci.nexus.device.INexusDeviceService;
import org.eclipse.dawnsci.nexus.scan.NexusScanFileService;
import org.eclipse.scanning.api.device.IDeviceWatchdogService;
import org.eclipse.scanning.api.device.IRunnableDeviceService;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.points.IPointGeneratorService;
import org.eclipse.scanning.api.scan.IFilePathService;
import org.eclipse.scanning.api.scan.IParserService;

import uk.ac.diamond.daq.api.messaging.MessagingService;

public class ServiceHolder {

	// OSGi stuff
	private static NexusScanFileService nexusScanFileService;

	public static NexusScanFileService getNexusScanFileService() {
		return nexusScanFileService;
	}

	public void setNexusScanFileService(NexusScanFileService nexusScanFileService) {
		ServiceHolder.nexusScanFileService = nexusScanFileService;
	}

	private static INexusDeviceService nexusDeviceService;

	public static INexusDeviceService getNexusDeviceService() {
		return nexusDeviceService;
	}

	public void setNexusDeviceService(INexusDeviceService nexusDeviceService) {
		ServiceHolder.nexusDeviceService = nexusDeviceService;
	}

    private static IOperationService operationService;

	public static IOperationService getOperationService() {
		return operationService;
	}

	public void setOperationService(IOperationService operationService) {
		ServiceHolder.operationService = operationService;
	}

    private static IDeviceWatchdogService watchdogService;

	public static IDeviceWatchdogService getWatchdogService() {
		return watchdogService;
	}

	public void setWatchdogService(IDeviceWatchdogService watchdogService) {
		ServiceHolder.watchdogService = watchdogService;
	}


	private static IPersistenceService persistenceService;

	public static IPersistenceService getPersistenceService() {
		return persistenceService;
	}

	public void setPersistenceService(IPersistenceService persistenceService) {
		ServiceHolder.persistenceService = persistenceService;
	}

	private static ILoaderService loaderService;

	public static ILoaderService getLoaderService() {
		return loaderService;
	}

	public void setLoaderService(ILoaderService loaderService) {
		ServiceHolder.loaderService = loaderService;
	}

	private static IEventService eventService;

	public static IEventService getEventService() {
		return eventService;
	}

	public void setEventService(IEventService eventService) {
		ServiceHolder.eventService = eventService;
	}

	private static IFilePathService filePathService;

	public static IFilePathService getFilePathService() {
		return filePathService;
	}

	public void setFilePathService(IFilePathService filePathService) {
		ServiceHolder.filePathService = filePathService;
	}

	private static IRunnableDeviceService runnableDeviceService;

	public static IRunnableDeviceService getRunnableDeviceService() {
		return runnableDeviceService;
	}

	public void setRunnableDeviceService(IRunnableDeviceService runnableDeviceService) {
		ServiceHolder.runnableDeviceService = runnableDeviceService;
	}

	private static IPointGeneratorService generatorService;


	public static IPointGeneratorService getGeneratorService() {
		return generatorService;
	}

	public void setGeneratorService(IPointGeneratorService generatorService) {
		ServiceHolder.generatorService = generatorService;
	}

	private static IParserService parserService;

	public static IParserService getParserService() {
		return parserService;
	}

	public void setParserService(IParserService parserService) {
		ServiceHolder.parserService = parserService;
	}

	private static IMarshallerService marshallerService;


	public static IMarshallerService getMarshallerService() {
		return marshallerService;
	}

	public void setMarshallerService(IMarshallerService marshallerService) {
		ServiceHolder.marshallerService = marshallerService;
	}

	private static MessagingService messagingService;

	public static MessagingService getMessagingService() {
		return messagingService;
	}

	public void setMessagingService(MessagingService messagingService) {
		ServiceHolder.messagingService = messagingService;
	}

	/**
	 * Used to provide services when tests running in non-OSGi mode.
	 */
	public void setTestServices(ILoaderService ls,
			IOperationService oservice) {
		loaderService = ls;
		operationService = oservice;
	}

	/**
	 * Used to provide services when tests running in non-OSGi mode.
	 */
	public void setTestServices(ILoaderService ls,
			IOperationService oservice, IFilePathService fpservice,
			IPointGeneratorService gService) {
		loaderService = ls;
		operationService = oservice;
		filePathService = fpservice;
		generatorService = gService;
	}

}
