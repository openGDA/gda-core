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

import gda.configuration.properties.LocalProperties;

import java.sql.Connection;
import java.sql.DriverManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides static methods to help access the GDA's local database via JDBC. The following parameters (with likely
 * values) must be specified in the java.properties file:
 * <p>
 * The following java properties (with defaults if not specified in your properties file) are used:
 * <p>
 * gda.util.persistence.database.dbpath (${gda.var}/GdaDatabase)
 * <p>
 * gda.util.persistence.database.username (gda)
 * <p>
 * gda.util.persistence.database.password (gda)
 * <p> # For embedded database
 * <p>
 * gda.util.persistence.database.driver (org.apache.derby.jdbc.EmbeddedDriver)
 * <p>
 * gda.util.persistence.database.protocol (jdbc:derby:)
 * <p> # For External database use:
 * <p>
 * #gda.util.persistence.database.driver = org.apache.derby.jdbc.ClientDriver
 * <p>
 * #gda.util.persistence.database.protocol = jdbc:derby://localhost:1527/

 */
public class LocalDatabase {


	public static final String DB_PATH_PROP = "gda.util.persistence.database.dbpath";
	public static final String DB_USERNAME_PROP = "gda.util.persistence.database.username";
	public static final String DB_PASSWORD_PROP = "gda.util.persistence.database.password";
	public static final String DB_DRIVER_PROP = "gda.util.persistence.database.driver";
	public static final String DB_PROTOCOL_PROP = "gda.util.persistence.database.protocol";

	public static final String DB_PATH_DEFAULT = "${gda.var}/gdaDatabase";
	public static final String DB_USERNAME_DEFAULT = "gda";
	public static final String DB_PASSWORD_DEFAULT = "gda";
	public static final String DB_DRIVER_DEFAULT = "org.apache.derby.jdbc.EmbeddedDriver";
	public static final String DB_PROTOCOL_DEFAULT = "jdbc:derby:";


	private static final Logger logger = LoggerFactory.getLogger(LocalDatabase.class);

	/**
	 * Creates a JDBC connection to the local database
	 *
	 * @return Connection
	 * @throws LocalDatabaseException
	 */
	public Connection createJdbcConnection() throws LocalDatabaseException {
		Connection toReturn;
		try {
			Class.forName(getJdbcDriver()).newInstance();
			toReturn = DriverManager.getConnection(getJdbcUrl(), getJdbcUsername(), getJdbcPassword());
			logger.info("Connected to database: '" + getJdbcUrl() + " as user: " + getJdbcUsername());
		} catch (Exception e) {
			logger.error("Could not connect to database using: '" + getJdbcUrl() + "' as user: " + getJdbcUsername());
			throw new LocalDatabaseException("Could not connect to database using: '" + getJdbcUrl() + "' as user: "
					+ getJdbcUsername(), e);
		}
		return toReturn;
	}

	/**
	 * Gets the local database driver string configured in java.properties using
	 * the property {@code gda.util.persistence.database.driver}.
	 *
	 * @return The local database's driver
	 * @throws LocalDatabaseException
	 * @throws LocalDatabaseException
	 *             if java.properties is not configured properly
	 */
	static String getJdbcDriver() throws LocalDatabaseException {
		String toReturn = LocalProperties.get(DB_DRIVER_PROP,DB_DRIVER_DEFAULT);
		if (toReturn == null)
			throw new LocalDatabaseException("The java.property gda.util.persistence.database.driver is not configured");
		return toReturn;
	}

	/**
	 * Gets the local database protocol string configured in java.properties
	 * using the property {@code gda.util.persistence.database.protocol}.
	 *
	 * @return The local database's protocol
	 * @throws LocalDatabaseException
	 * @throws LocalDatabaseException
	 *             if java.properties is not configured properly
	 */
	static String getJdbcProtocol() throws LocalDatabaseException {
		String toReturn = LocalProperties.get(DB_PROTOCOL_PROP,DB_PROTOCOL_DEFAULT);
		if (toReturn == null)
			throw new LocalDatabaseException(
					"The java.property gda.util.persistence.database.protocol is not configured");
		return toReturn;
	}

	/**
	 * Gets the local database path string configured in java.properties using
	 * the property {@code gda.util.persistence.database.dbpath}.
	 *
	 * @return The local database's path
	 * @throws LocalDatabaseException
	 * @throws LocalDatabaseException
	 *             if java.properties is not configured properly
	 */
	static String getJdbcDBPath() throws LocalDatabaseException {
		String toReturn = LocalProperties.get(DB_PATH_PROP,DB_PATH_DEFAULT);
		if (toReturn == null)
			throw new LocalDatabaseException("The java.property gda.util.persistence.database.dbpath is not configured");
		return toReturn;
	}

	/**
	 * Returns a string describing the JDBC URL for the local database. This
	 * has the form {@code "<protocol><dbpath>;create=true"}.
	 *
	 *
	 * <p>For example:
	 * <p>
	 * <code>jdbc:derby:/dls/i01/software/gda/config/var/gdaDatabase;create=true</code> *
	 * <p>
	 *
	 * @return The local database's JDBC URL
	 * @throws LocalDatabaseException
	 */
	static String getJdbcUrl() throws LocalDatabaseException {
		return getJdbcProtocol() + getJdbcDBPath() + ";create=true";
	}

	/**
	 * Gets the local database username configured in java.properties using
	 * the property {@code gda.util.persistence.database.username}.
	 *
	 * @return The local database's username
	 * @throws LocalDatabaseException
	 */
	static String getJdbcUsername() throws LocalDatabaseException {
		String toReturn = LocalProperties.get(DB_USERNAME_PROP,DB_USERNAME_DEFAULT);
		if (toReturn == null)
			throw new LocalDatabaseException(
					"The java.property gda.util.persistence.database.username is not configured");
		return toReturn;
	}

	/**
	 * Gets the local database password configured in java.properties using the
	 * property {@code gda.util.persistence.database.password}.
	 *
	 * @return The local database's password
	 * @throws LocalDatabaseException
	 * @throws LocalDatabaseException
	 *             if java.properties is not configured properly
	 */
	static String getJdbcPassword() throws LocalDatabaseException {
		String toReturn = LocalProperties.get(DB_PASSWORD_PROP,DB_PASSWORD_DEFAULT);
		if (toReturn == null)
			throw new LocalDatabaseException(
					"The java.property gda.util.persistence.database.password is not configured");
		return toReturn;
	}

	/**
	 * LocalDatabaseException Class
	 */
	public static class LocalDatabaseException extends Exception {
		/**
		 * Constructor
		 */
		public LocalDatabaseException() {
		}

		/**
		 * @param msg
		 */
		public LocalDatabaseException(String msg) {
			super(msg);
		}

		/**
		 * Create a data table exception with another Throwable as the cause.
		 *
		 * @param message
		 *            the message for this Exception
		 * @param cause
		 *            the cause (will become the detail message).
		 */
		public LocalDatabaseException(String message, Throwable cause) {
			super(message, cause);
		}
	}

}
