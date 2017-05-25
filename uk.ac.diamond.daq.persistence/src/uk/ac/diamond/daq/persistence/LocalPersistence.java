/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.persistence;

import gda.configuration.properties.LocalProperties;

import java.io.File;
import java.util.Properties;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import oracle.toplink.essentials.config.TopLinkProperties;
import uk.ac.diamond.daq.persistence.LocalDatabase.LocalDatabaseException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides a static method to access the gda's local database using the Java Persistence API (aka Glassfish).
 *
 * @see uk.ac.diamond.daq.persistence.LocalDatabase
 */
public class LocalPersistence {
	private static final Logger logger = LoggerFactory.getLogger(LocalPersistence.class);

	/**
	 * Creates a Java Persistence API (aka Glassfish) EntityManagerFactory associated with the GDA's local database.
	 *
	 * @param persistenceUnitName
	 *            The name of the persistent unit matching the xml entry in /srv/META-INF/persistence.xml
	 * @return An EntityManagerFactory
	 * @throws LocalDatabaseException
	 */
	public static EntityManagerFactory createPersistenceEntityManagerFactory(String persistenceUnitName)
	throws LocalDatabaseException {
		Properties properties = new Properties();

		File toplinkApplicationLocation = determineTopLinkApplicationLocation();
		if (!toplinkApplicationLocation.exists()) {
			toplinkApplicationLocation.mkdirs();
		}

		logger.info("Loading persistence entity " + persistenceUnitName + " using toplink.jdbc.url="
				+ LocalDatabase.getJdbcUrl() + " and user=" + LocalDatabase.getJdbcUsername());

		properties.put(TopLinkProperties.JDBC_DRIVER, LocalDatabase.getJdbcDriver());
		properties.put(TopLinkProperties.JDBC_URL, LocalDatabase.getJdbcUrl());
		properties.put(TopLinkProperties.JDBC_USER, LocalDatabase.getJdbcUsername());
		properties.put(TopLinkProperties.JDBC_PASSWORD, LocalDatabase.getJdbcPassword());
		properties.put(TOPLINK_APPLICATION_LOCATION, toplinkApplicationLocation.getAbsolutePath());

		return Persistence.createEntityManagerFactory(persistenceUnitName, properties);
	}

	private static final String TOPLINK_APPLICATION_LOCATION = "toplink.application-location";

	/**
	 * Returns the TopLink application location.
	 */
	private static File determineTopLinkApplicationLocation() {

		// Use the value of the TopLink property, if it's been set
		final String applicationLocation = LocalProperties.get(TOPLINK_APPLICATION_LOCATION);
		if (applicationLocation != null) {
			return new File(applicationLocation);
		}

		// Otherwise, fall back to ${gda.var}/toplink
		File toplinkApplicationLocation = new File(new File(LocalProperties.getVarDir()), "toplink");
		return toplinkApplicationLocation;
	}
}
