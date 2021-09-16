/*-
 * Copyright © 2021 Diamond Light Source Ltd.
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;
import gda.factory.ConfigurableBase;
import gda.factory.FactoryException;
import gda.factory.Finder;
import gda.jython.InterfaceProvider;
import uk.ac.diamond.daq.concurrent.Async;

/**
 * Client object for requesting a Data Server port from the server via the RMI exported {@link DataServerClientConfig}
 * It is assumed that the server object is named {@code dataservermanager}
 */
public class DataServerClientConfig extends ConfigurableBase {

	private static final Logger logger = LoggerFactory.getLogger(DataServerClientConfig.class);

	private DataServerSpawner serverInfo;
	private int myClientId;

	@Override
	public void configure() throws FactoryException {
		var manager = Finder.findOptionalOfType("dataservermanager", DataServerSpawner.class);
		manager.ifPresent(this::haveServerInfo);
		setConfigured(true);
	}

	private void haveServerInfo(DataServerSpawner serverInfo) {
		this.serverInfo = serverInfo;
		Async.submit(this::asyncConfigure);
	}


	private void asyncConfigure() {
		myClientId = InterfaceProvider.getBatonStateProvider().getMyDetails().getIndex();
		int port = serverInfo.createDataserver(myClientId);
		setProperties(port);
		Runtime.getRuntime().addShutdownHook(new Thread(this::destroyMyServer));
	}

	private void setProperties(int port) {
		logger.info("Setting DataServer port properties to: {}", port);
		LocalProperties.set("gda.dataserver.port", Integer.toString(port));
		System.setProperty("GDA/gda.dataserver.port", Integer.toString(port));
		System.setProperty("gda.dataserver.port", Integer.toString(port));

	}

	private void destroyMyServer() {
		serverInfo.destroyDataserver(myClientId);
	}

}
