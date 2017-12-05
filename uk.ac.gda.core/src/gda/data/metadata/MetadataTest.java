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

package gda.data.metadata;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.factory.FactoryException;
import gda.util.ObjectServer;

/**
 * Metadata Test Class.
 */
public class MetadataTest {
	private static final Logger logger = LoggerFactory.getLogger(MetadataTest.class);

	/**
	 * Main test method.
	 *
	 * @param args
	 * @throws FactoryException
	 */
	public static void main(String[] args) throws FactoryException {
		MetadataEntry entry;
		List<IMetadataEntry> entries;

		ObjectServer.createLocalImpl("/home/shk/config/xml/metadata_server.xml");
		Metadata metadata = GDAMetadataProvider.getInstance();


		entry = new StoredMetadataEntry("temperature", "45.4 deg");
		try {
			metadata.addMetadataEntry(entry);
			entries = metadata.getMetadataEntries();
			for (IMetadataEntry e : entries) {
				logger.debug("Entry " + e.getName() + " = " + e.getMetadataValue());
			}

			metadata.setMetadataValue("temperature", "57.2 deg");
			logger.debug("Temperature " + metadata.getMetadataValue("temperature"));
		} catch (DeviceException e1) {
			e1.printStackTrace();
		}

		System.exit(0);
	}
}
