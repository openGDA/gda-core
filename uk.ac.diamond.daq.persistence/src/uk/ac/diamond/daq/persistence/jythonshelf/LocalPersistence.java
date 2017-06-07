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

package uk.ac.diamond.daq.persistence.jythonshelf;

import java.io.File;
import java.util.Properties;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;
import oracle.toplink.essentials.config.TopLinkProperties;
import uk.ac.diamond.daq.persistence.jythonshelf.LocalDatabase.LocalDatabaseException;



/**
 * Provides a static method to access the gda's local database using the Java Persistence API.
 *
 * @see uk.ac.diamond.daq.persistence.jythonshelf.LocalDatabase
 */
public class LocalPersistence {
	private static final Logger logger = LoggerFactory.getLogger(LocalPersistence.class);

	private LocalPersistence(){              // prevent instantiation
	}

	/**
	 * Creates a Java Persistence API EntityManagerFactory associated with the GDA's local database. As the telnet
	 * and RCP clients use different classloaders (because of how their listeners are created) it is necessary to
	 * switch the ThreadContextClassLoader to this bundle's ClassLoader to guarantee that the persistence.xml resource
	 * can be correctly loaded when creating the EntityManager. It is reset again afterwards
	 *
	 * @param persistenceUnitName
	 *            The name of the persistence unit matching the xml entry in /META-INF/persistence.xml
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

		logger.info("Loading persistence entity {} using toplink.jdbc.url{} and user={}",
				persistenceUnitName, LocalDatabase.getJdbcUrl(), LocalDatabase.getJdbcUsername());

		properties.put(TopLinkProperties.JDBC_DRIVER, LocalDatabase.getJdbcDriver());
		properties.put(TopLinkProperties.JDBC_URL, LocalDatabase.getJdbcUrl());
		properties.put(TopLinkProperties.JDBC_USER, LocalDatabase.getJdbcUsername());
		properties.put(TopLinkProperties.JDBC_PASSWORD, LocalDatabase.getJdbcPassword());
		properties.put(TOPLINK_APPLICATION_LOCATION, toplinkApplicationLocation.getAbsolutePath());

		// Cache the thread ClassLoader and then substitute our own to guarantee resolution of the persistence.xml file
		ClassLoader tccl = Thread.currentThread().getContextClassLoader();
		ClassLoader persistenceAware = LocalPersistence.class.getClassLoader();

		try {
			Thread.currentThread().setContextClassLoader(persistenceAware);
			return Persistence.createEntityManagerFactory(persistenceUnitName, properties);
		} finally {
			Thread.currentThread().setContextClassLoader(tccl);
		}
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
		return new File(new File(LocalProperties.getVarDir()), "toplink");
	}
}
