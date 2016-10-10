/*-
 * Copyright © 2016 Diamond Light Source Ltd.
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

package gda.data.metadata.icat;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;

import gda.configuration.properties.LocalProperties;
import gda.jython.authoriser.AuthoriserProvider;

public abstract class DlsIcatBase extends IcatBase {

	private static final Logger logger = LoggerFactory.getLogger(DlsIcatBase.class);

	/**
	 * Name of the java property which defines the Icat database username
	 */
	public static final String USER_PROP = "gda.data.metadata.dlsicat.user";

	/**
	 * Name of the java property which defines the Icat database password
	 */
	public static final String PASSWORD_PROP = "gda.data.metadata.dlsicat.password";

	/**
	 * The access string to retrieve the visit ID from the database.
	 */
	private static final String VISIT_QUERY = "VISIT";

	/**
	 * The access string to retrieve the experiment ID from the database.
	 */
	private static final String TITLE_QUERY = "TITLE";

	@Override
	protected String getVisitIDAccessName() {
		return VISIT_QUERY;
	}

	@Override
	protected String getExperimentTitleAccessName() {
		return TITLE_QUERY;
	}

	@Override
	protected String getValue(String visitIDFilter, String userNameFilter, String accessName) throws Exception {
		if (VISIT_QUERY.equals(accessName)) {
			final List<String> visits = getUsefulVisits(userNameFilter);
			return Joiner.on(", ").join(visits);
		}
		if (TITLE_QUERY.equals(accessName))
			return getTitleForVisit(visitIDFilter);
		throw new IllegalArgumentException(String.format(
			"unknown query request: visitIDFilter=%s, userNameFilter=%s, accessName=%s",
			StringUtils.quote(visitIDFilter), StringUtils.quote(userNameFilter), StringUtils.quote(accessName)));
	}

	protected List<String> getUsefulVisits(String user) throws Exception {
		Connection connection = null;
		try {
			connection = connectToDatabase();

			final List<String> visits = getVisitsForUser(connection, user);

			// append to the list extra options if local staff
			if (AuthoriserProvider.getAuthoriser().isLocalStaff(user)) {
				// allow beamline staff to use the current visit ID
				for (String visitPrefix : new String[] {"CM", "NR"}) {
					final Optional<String> extraVisit = getLatestVisitWithPrefix(connection, visitPrefix);
					visits.addAll(extraVisit.asSet());
				}
			}

			return visits;

		} catch (Exception e) {
			throw new Exception("Processing or reading data from dicat database", e);
		} finally {
			closeConnection(connection);
		}
	}

	protected String getTitleForVisit(String visitID) throws Exception {
		Connection connection = null;
		try {
			connection = connectToDatabase();
			final String title = getTitleForVisitUsingConnection(visitID, connection);
			return title;
		} catch (Exception e) {
			throw new Exception("Processing or reading data from dicat database", e);
		} finally {
			closeConnection(connection);
		}
	}

	protected void closeConnection(Connection connection) {
		if (connection != null) {
			try {
				connection.close();
			} catch (SQLException e) {
				logger.error("error closing database connection", e);
			}
		}
	}

	protected static String getMandatoryProperty(String propertyName, String name) {
		final String value = LocalProperties.get(propertyName);
		if (value == null) {
			throw new RuntimeException(String.format("ICAT %s not set. Have you set the %s property?", name, propertyName));
		}
		return value;
	}

	protected abstract Connection connectToDatabase() throws Exception;

	protected abstract List<String> getVisitsForUser(Connection connection, String user) throws Exception;

	protected abstract Optional<String> getLatestVisitWithPrefix(Connection connection, String visitPrefix) throws Exception;

	protected abstract String getTitleForVisitUsingConnection(String visitID, Connection connection) throws Exception;

}
