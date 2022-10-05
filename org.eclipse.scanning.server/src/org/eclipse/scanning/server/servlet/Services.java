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
package org.eclipse.scanning.server.servlet;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.dawnsci.nexus.device.INexusDeviceService;
import org.eclipse.scanning.api.IValidatorService;
import org.eclipse.scanning.api.device.IDeviceWatchdogService;
import org.eclipse.scanning.api.device.IRunnableDeviceService;
import org.eclipse.scanning.api.device.IScannableDeviceService;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.IMessagingService;
import org.eclipse.scanning.api.points.IPointGeneratorService;
import org.eclipse.scanning.api.scan.IFilePathService;
import org.eclipse.scanning.api.scan.IScanService;
import org.eclipse.scanning.api.scan.process.IPreprocessor;
import org.eclipse.scanning.api.script.IScriptService;

import uk.ac.diamond.daq.api.messaging.MessagingService;


/**
 * This class holds services for the scanning server servlets. Services should be configured to be optional and dynamic
 * and will then be injected correctly by Equinox DS.
 *
 * @author Matthew Gerring
 * @author Colin Palmer
 *
 */
public class Services {

	private static IEventService           eventService;
	private static IPointGeneratorService  generatorService;
	private static IRunnableDeviceService  runnableDeviceService;
	private static IScanService			   scanService;
	private static IScannableDeviceService connector;
	private static INexusDeviceService     nexusDeviceService;
	private static IFilePathService        filePathService;
	private static IScriptService          scriptService;
	private static IMessagingService       messagingService;
	private static IValidatorService       validatorService;
	private static IDeviceWatchdogService  watchdogService;
	private static MessagingService gdaMessagingService;

	private static final Set<IPreprocessor> preprocessors = new LinkedHashSet<>();

	public static IFilePathService getFilePathService() {
		return filePathService;
	}

	public void setFilePathService(IFilePathService filePathService) {
		Services.filePathService = filePathService;
	}

	public static IEventService getEventService() {
		return eventService;
	}

	public void setEventService(IEventService eventService) {
		Services.eventService = eventService;
	}

	public static IPointGeneratorService getGeneratorService() {
		return generatorService;
	}

	public void setGeneratorService(IPointGeneratorService generatorService) {
		Services.generatorService = generatorService;
	}

	public static IRunnableDeviceService getRunnableDeviceService() {
		return runnableDeviceService;
	}

	public void setRunnableDeviceService(IRunnableDeviceService deviceService) {
		Services.runnableDeviceService = deviceService;
	}

	public static IScanService getScanService() {
		return scanService;
	}

	public void setScanService(IScanService deviceService) {
		Services.scanService = deviceService;
	}

	public static IScannableDeviceService getConnector() {
		return connector;
	}

	public void setConnector(IScannableDeviceService connector) {
		Services.connector = connector;
	}

	public static INexusDeviceService getNexusDeviceService() {
		return nexusDeviceService;
	}

	public void setNexusDeviceService(INexusDeviceService nexusDeviceService) {
		Services.nexusDeviceService = nexusDeviceService;
	}

	public static IScriptService getScriptService() {
		return scriptService;
	}

	public void setScriptService(IScriptService scriptService) {
		Services.scriptService = scriptService;
	}

	public synchronized void addPreprocessor(IPreprocessor preprocessor) {
		preprocessors.add(preprocessor);
	}

	public synchronized void removePreprocessor(IPreprocessor preprocessor) {
		preprocessors.remove(preprocessor);
	}

	public static Collection<IPreprocessor> getPreprocessors() {
		return preprocessors;
	}

	public static IMessagingService getMessagingService() {
		return messagingService;
	}

	public void setMessagingService(IMessagingService messagingService) {
		Services.messagingService = messagingService;
	}

	public static IValidatorService getValidatorService() {
		return validatorService;
	}

	public void setValidatorService(IValidatorService validatorService) {
		Services.validatorService = validatorService;
	}

	public static IDeviceWatchdogService getWatchdogService() {
		return watchdogService;
	}

	public void setWatchdogService(IDeviceWatchdogService watchdogService) {
		Services.watchdogService = watchdogService;
	}

	public static MessagingService getGdaMessagingService() {
		return gdaMessagingService;
	}

	public void setGdaMessagingService(MessagingService gdaMessagingService) {
		Services.gdaMessagingService = gdaMessagingService;
	}
}
