/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council Daresbury Laboratory
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

package gda.device.amplifier;

import gda.configuration.properties.LocalProperties;
import gda.factory.corba.util.EventService;
import gda.factory.corba.util.NetService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 */
public class TestObjectAssistant {
	private static final Logger logger = LoggerFactory.getLogger(TestObjectAssistant.class);

	String url = LocalProperties.get("gda.objectserver.xml");

	String name = LocalProperties.get("gda.objectserver.name");

	String args[] = { "-d2" };

	boolean createOnStartup = false;

	/**
	 * 
	 */
	public void createLocalObjects() {
		// Create an instance of the local factory with create on
		// startup set to false, forcing all objects to be created,
		// if necessary, by the ImplFactory so their signatures are
		// published on the name service.
		// LocalFactory localFactory = new LocalFactory(false);
		try {
			// localFactory.configure();
		} catch (Exception e) {
			logger.debug(e.getStackTrace().toString());
		}
		// Finder finder = Finder.getInstance();
		// finder.addFactory(localFactory);

	}

	/**
	 * 
	 */
	public void createRemoteObjects() {
		// Create an instance of the local factory with create on
		// startup set to false, forcing all objects to be created,
		// if necessary, by the ImplFactory so their signatures are
		// published on the name service.

		try {
			// LocalFactory localFactory = new LocalFactory(false);
			// localFactory.configure();

			NetService.getInstance();
			EventService.getInstance();

			// Use the adapter factory to locate objects that are required
			// by those implemented on this server, therefore this code
			// should be added to the finder before this servers objects
			// are created by the local factory.

			// AdapterFactory adapterFactory = new
			// AdapterFactory(netService);
			// adapterFactory.configure();

			// Finder finder = Finder.getInstance();
			// finder.addFactory(localFactory);
			// finder.addFactory(adapterFactory);
		} catch (Exception e) {
			logger.debug(e.getStackTrace().toString());
		}
	}
}