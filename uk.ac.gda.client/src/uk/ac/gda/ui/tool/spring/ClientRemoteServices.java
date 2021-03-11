/*-
 * Copyright © 2020 Diamond Light Source Ltd.
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

package uk.ac.gda.ui.tool.spring;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.dawnsci.analysis.api.io.IRemoteDatasetService;
import org.eclipse.dawnsci.plotting.api.IPlottingService;
import org.eclipse.scanning.api.device.IRunnableDeviceService;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.bean.IBeanListener;
import org.eclipse.scanning.api.event.core.ISubscriber;
import org.eclipse.scanning.api.scan.IFilePathService;
import org.eclipse.scanning.api.ui.IStageScanConfiguration;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import gda.configuration.properties.LocalProperties;

/**
 * Provides various remote services.
 * This class
 * <ul>
 * <li>
 *  consolidates any client {@code PlatformUI.getWorkbench()} invocation
 * </li>
 * <li>
 *  being annotated as @Service, can be injected in other Spring component using @Autowire
 * </li>
 * <li>
 *  Spring enabled test can mock it in a natural way
 * </li>
 * </ul>
 *
 * @author Maurizio Nagni
 */
@Service
public class ClientRemoteServices {

	private static final Logger logger = LoggerFactory.getLogger(ClientRemoteServices.class);

	private ClientRemoteServices() {}

	/**
	 * A {@code IRunnableDeviceService} instance
	 * @return a proxy to a remote runnableDeviceService instance
	 */
	public IRunnableDeviceService getIRunnableDeviceService() {
		return getRemoteService(IRunnableDeviceService.class);
	}

	public IPlottingService getIPlottingService() {
		return getService(IPlottingService.class);
	}

	public IFilePathService getIFilePathService() {
		return getService(IFilePathService.class);
	}

	public IEventService getIEventService() {
		return getService(IEventService.class);
	}

	/**
	 * Create subscriber to ActiveMQ topic
	 */
	public <T> ISubscriber<IBeanListener<T>> createSubscriber(String topic) throws URISyntaxException {
		return getIEventService().createSubscriber(new URI(LocalProperties.getActiveMQBrokerURI()), topic);
	}

	public IStageScanConfiguration getIStageScanConfiguration() {
		return getService(IStageScanConfiguration.class);
	}

	public IRemoteDatasetService getIRemoteDatasetService() {
		return getService(IRemoteDatasetService.class);
	}

	private <T> T getRemoteService(Class<T> klass) {
		IEventService eventService = getIEventService();
		if (eventService != null) {
			try {
				URI jmsURI = new URI(LocalProperties.getActiveMQBrokerURI());
				return eventService.createRemoteService(jmsURI, klass);
			} catch (EventException | URISyntaxException e) {
				logger.warn("Cannot retrieve remote service {}", klass, e);
			}
		}
		return null;
	}

	private <T> T getService(Class<T> klass) {
		return PlatformUI.getWorkbench().getService(klass);
	}
}
