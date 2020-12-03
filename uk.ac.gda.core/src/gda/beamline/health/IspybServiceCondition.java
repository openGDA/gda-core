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

package gda.beamline.health;

import java.sql.Connection;
import java.sql.SQLException;

import org.mariadb.jdbc.MariaDbDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;
import gda.factory.FactoryException;

public class IspybServiceCondition extends RateLimitedServerCondition {
	private static final Logger logger = LoggerFactory.getLogger(IspybServiceCondition.class);

	/** Name of the java property which defines the Icat database url (file or database) */
	public static final String URL_PROP = "gda.data.metadata.icat.url";

	/** Name of the java property which defines the Icat database username */
	public static final String USER_PROP = "gda.data.metadata.dlsicat.user";

	/** Name of the java property which defines the Icat database password */
	public static final String PASSWORD_PROP = "gda.data.metadata.dlsicat.password";

	/** URL for database access */
	private String url;

	/** User name for database access */
	private String username;

	/** Password for database access */
	private String password;

	@Override
	public void configure() throws FactoryException {
		super.configure();
		try {
			url = getMandatoryProperty(URL_PROP, "URL");
			username = getMandatoryProperty(USER_PROP, "username");
			password = getMandatoryProperty(PASSWORD_PROP, "password");
		} catch (Exception e) {
			setConfigured(false);
			final String message = "Error configuring " + getName();
			setErrorMessage(message);
			throw new FactoryException(message, e);
		}
		setConfigured(true);
	}

	@Override
	protected boolean isServiceRunning() {
		if (!isConfigured()) {
			return false;
		}

		Connection connection = null;
		try {
			final MariaDbDataSource ds = new MariaDbDataSource();
			ds.setUrl(url);
			ds.setUserName(username);
			ds.setPassword(password);
			connection = ds.getConnection();
		} catch (Exception e) {
			return false;
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException e) {
					logger.error("Error closing database connection to {}", url, e);
				}
			}
		}
		return true;
	}

	private static String getMandatoryProperty(String propertyName, String name) {
		final String value = LocalProperties.get(propertyName);
		if (value == null) {
			throw new IllegalStateException(
					String.format("ICAT %s not set. Have you set the %s property?", name, propertyName));
		}
		return value;
	}
}
