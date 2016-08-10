/*-
 * Copyright © 2009-2013 Diamond Light Source Ltd.
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

import gda.configuration.properties.LocalProperties;
import gda.jython.authoriser.AuthoriserProvider;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;

/**
 * The Icat which talks to the Diamond ICAT database.
 *
 * <p>This is an abbreviated schema diagram for the ICAT4 database:
 *
 * <pre>
 * +------------------+        +---------------+        +-------------------+
 * | SHIFT            |        | INVESTIGATION |        | INVESTIGATIONUSER |        +------------------+
 * |------------------|        |---------------|        |-------------------|        | USER_            |
 * | INVESTIGATION_ID |--------| ID            |--------| INVESTIGATION_ID  |        |------------------|
 * | STARTDATE        |*      1| VISIT_ID      |1      *| USER_ID           |--------| ID               |
 * | ENDDATE          |        | INSTRUMENT_ID |        | ...               |*      1| NAME             |
 * | ...              |        | ...           |        +-------------------+        | FULLNAME         |
 * +------------------+        +---------------+                                     | ...              |
 *                                                                                   +------------------+
 * </pre>
 */
public class DLSdicat extends IcatBase {
	private static final Logger logger = LoggerFactory.getLogger(DLSdicat.class);

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

	protected List<String> getUsefulVisits(String user) throws Exception {
		Connection connection = null;
		try {
			connection = connectToDatabase();
			final List<String> visits = getVisitsForUser(connection, user);
			return visits;
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

	protected List<String> getVisitsForUser(Connection connection, String user) throws Exception {
		ResultSet resultSet = null;

		PreparedStatement prepared = null;
		List<String> value;

		try {
			prepared = connection.prepareStatement("select lower(visit_id) from icatdls42.investigation i "+
						"inner join icatdls42.instrument ins on i.instrument_id = ins.id "+
						"inner join icatdls42.shift s on s.investigation_id = i.id "+
						"inner join icatdls42.investigationuser iu on iu.investigation_id = i.id "+
						"inner join icatdls42.user_ u on u.id = iu.user_id "+
						"where u.name = ? and ins.name = ? " +
						"and systimestamp between s.startdate-? and s.enddate+? "+
						"and ( s.\"COMMENT\" <> 'Cancelled'  or s.\"COMMENT\" is NULL ) " +
						"order by s.startdate");

			prepared.setString(1, user);
			prepared.setString(2, getInstrumentName());

			int tolerance = LocalProperties.getAsInt(SHIFT_TOL_PROP,1440);

			prepared.setFloat(3, tolerance/1440);
			prepared.setFloat(4, tolerance/1440);

			resultSet = prepared.executeQuery();

			value = concatenateResultSet(resultSet);

			// append to the list extra options if local staff
			if (AuthoriserProvider.getAuthoriser().isLocalStaff(user)) {
				// allow beamline staff to use the current visit ID
				for (String visitPrefix : new String[] {"CM", "NR"}) {
					final Optional<String> extraVisit = getLatestVisitWithPrefix(connection, visitPrefix);
					value.addAll(extraVisit.asSet());
				}
			}

		} finally {
			if (resultSet != null && !resultSet.isClosed()) {
				try {
					resultSet.close();
				} catch (SQLException e) {
					logger.error("error closing database connection", e);
				}
			}
			if (prepared != null && !prepared.isClosed()) {
				try {
					prepared.close();
				} catch (SQLException e) {
					logger.error("error closing database connection", e);
				}
			}
		}

		return value;
	}

	protected String getTitleForVisit(String visitID) throws Exception {
		ResultSet resultSet = null;
		Connection connection = null;
		PreparedStatement prepared = null;

		try {
			connection = connectToDatabase();
			prepared = connection.prepareStatement("select title from icatdls42.investigation i where lower(i.visit_id) = ? ");
			prepared.setString(1, visitID);
			resultSet = prepared.executeQuery();
			if (resultSet.next())
				return resultSet.getString("title");
			return null;
		} catch (Exception e) {
			throw new Exception("Processing or reading data from dicat database", e);
		} finally {
			if (resultSet != null && !resultSet.isClosed()) {
				try {
					resultSet.close();
				} catch (SQLException e) {
					logger.error("error closing database connection", e);
				}
			}
			if (prepared != null && !prepared.isClosed()) {
				try {
					prepared.close();
				} catch (SQLException e) {
					logger.error("error closing database connection", e);
				}
			}if (connection != null) {
				try {
					connection.close();
				} catch (SQLException e) {
					logger.error("error closing database connection", e);
				}
			}
		}
	}

	@Override
	protected String getValue(String visitIDFilter, String userNameFilter, String accessName) throws Exception {
		if (VISIT_QUERY.equals(accessName)) {
			final List<String> visits = getUsefulVisits(userNameFilter);
			return Joiner.on(", ").join(visits);
		}
		if (TITLE_QUERY.equals(accessName))
			return getTitleForVisit(visitIDFilter);
		throw new IllegalArgumentException("unknown query request");
	}

	private Optional<String> getLatestVisitWithPrefix(Connection connection, String visitPrefix) throws SQLException {

		visitPrefix += "%";

		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			statement = connection.prepareStatement("select v2.visit from "+
					"( select lower(i.visit_id) visit from icatdls42.investigation i "+
					"inner join icatdls42.instrument ins on i.instrument_id = ins.id "+
					"inner join icatdls42.shift s on s.investigation_id = i.id "+
					"where i.visit_id like ? and ins.name = ? and systimestamp > s.startdate "+
					"order by s.startdate DESC ) v2 where rownum <=1");
			statement.setString(1, visitPrefix);
			statement.setString(2, getInstrumentName());
			resultSet = statement.executeQuery();
			if (resultSet.next()) {
				final String visit = resultSet.getString(1);
				if (!visit.isEmpty()) {
					return Optional.of(visit);
				}
			}
			return Optional.absent();
		} catch (Exception e) {
			logger.error("Unable to retrieve visits from database", e);
			return Optional.absent();
		} finally {
			if (resultSet != null && !resultSet.isClosed()) {
				try {
					resultSet.close();
				} catch (SQLException e) {
					logger.error("error closing database connection", e);
				}
			}
			if (statement != null && !statement.isClosed()) {
				try {
					statement.close();
				} catch (SQLException e) {
					logger.error("error closing database connection", e);
				}
			}
		}
	}

	private Connection connectToDatabase() throws Exception {
		Connection connection = null;
		Class.forName("oracle.jdbc.driver.OracleDriver").newInstance();

		java.util.Properties info = new java.util.Properties();

		final String username = LocalProperties.get(USER_PROP);
		if (username == null) {
			throw new RuntimeException("DiCAT username not set. Have you set the " + USER_PROP + " property?");
		}

		final String password = LocalProperties.get(PASSWORD_PROP);
		if (password == null) {
			throw new RuntimeException("DiCAT password not set. Have you set the " + PASSWORD_PROP + " property?");
		}

		info.put ("user", username);
		info.put ("password", password);
		info.put ("oracle.jdbc.timezoneAsRegion", "false");

		connection = DriverManager.getConnection(LocalProperties.get(URL_PROP), info);
		logger.info("Successfully connected to DiCat, using " + LocalProperties.get(URL_PROP));
		return connection;
	}

	private List<String> concatenateResultSet(ResultSet resultSet) throws SQLException {
		List<String> value = new ArrayList<String>();
		while (resultSet.next()) {
			String result = resultSet.getString(1);
			value.add(result);
		}
		return value;
	}
}
