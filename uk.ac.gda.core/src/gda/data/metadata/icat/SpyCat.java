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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.mariadb.jdbc.MariaDbDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import gda.configuration.properties.LocalProperties;
import gda.data.metadata.VisitData;

/**
 * {@link IcatBase} subclass that retrieves visit information from ISPyB.
 */
public class SpyCat extends DlsIcatBase {

	private static final Logger logger = LoggerFactory.getLogger(SpyCat.class);

	////////////////////////////////////////////////////////////////////////////////////////////////

	@Override
	protected List<String> getVisitsForUser(Connection connection, String username) throws Exception {
		logger.trace("getVisitsForUser({}, {})", connection, username);

		final String sql = "CALL ispyb.retrieve_current_sessions_for_person(?, ?, ?)";

		final int tolerance = LocalProperties.getAsInt(SHIFT_TOL_PROP, 1440);
		final Object[] parameters = new Object[] { getInstrumentName(), username, tolerance };

		final JdbcTemplate template = makeJdbcTemplateFromConnection(connection);

		final List<String> visits = template.query(sql, parameters, VISIT_MAPPER);

		logger.debug("SQL query '{}' with parameters {} returned {}", sql, Arrays.toString(parameters), visits);

		return visits;
	}

	@Override
	protected Optional<String> getLatestVisitWithPrefix(Connection connection, String visitPrefix) {
		logger.trace("getLatestVisitWithPrefix({}, {})", connection, visitPrefix);
		return getVisitWithPrefix(connection, visitPrefix, VISIT_MAPPER).stream().findFirst();
	}

	@Override
	protected List<VisitData> getVisitDataWithPrefix(Connection connection, String visitPrefix) {
		return getVisitWithPrefix(connection, visitPrefix, VISIT_DATA_MAPPER);
	}

	private static final RowMapper<String> VISIT_MAPPER = new RowMapper<>() {
		@Override
		public String mapRow(ResultSet rs, int rowNum) throws SQLException {
			return rs.getString(1);
		}
	};

	private static final RowMapper<VisitData> VISIT_DATA_MAPPER = new RowMapper<>() {
		@Override
		public VisitData mapRow(ResultSet rs, int rowNum) throws SQLException {
			// Ensure dates are standard Java Dates, not SQL Times
			return new VisitData(rs.getString(1), new Date(rs.getTime(2).getTime()), new Date(rs.getTime(3).getTime()));
		}
	};

	private <T> List<T> getVisitWithPrefix(Connection connection, String visitPrefix, RowMapper<T> mapper) {
		logger.trace("getVisitWithPrefix({}, {})", connection, visitPrefix);

		final String sql = "CALL ispyb.retrieve_most_recent_session(?, ?)";

		final Object[] parameters = new Object[] { getInstrumentName(), visitPrefix };

		final JdbcTemplate template = makeJdbcTemplateFromConnection(connection);

		final List<T> visits = template.query(sql, mapper, parameters);

		logger.debug("SQL query '{}' with parameters {} returned {}", sql, parameters, visits);

		return visits;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////

	@Override
	protected String getTitleForVisitUsingConnection(String visit, Connection connection) throws Exception {
		logger.trace("getTitleForVisitUsingConnection({}, {})", visit, connection);

		// Split visit code (e.g. "cm14486-3") into proposal code ("cm") and proposal number (14486)
		final Pattern visitPattern = Pattern.compile("([a-z]+)(\\d+)-(\\d+)");
		final Matcher matcher = visitPattern.matcher(visit);
		if (!matcher.matches()) {
			throw new RuntimeException("Invalid visit: " + visit);
		}
		final String proposalCode = matcher.group(1);
		final int proposalNumber = Integer.parseInt(matcher.group(2));

		final String sql = "SELECT ispyb.retrieve_proposal_title(?, ?)";

		final Object[] parameters = new Object[] { proposalCode, proposalNumber };

		final JdbcTemplate template = makeJdbcTemplateFromConnection(connection);

		final String title = template.queryForObject(sql, parameters, String.class);

		logger.debug("SQL query '{}' with parameters {} returned '{}'", sql, Arrays.toString(parameters), title);

		return title;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////

	@Override
	protected Connection connectToDatabase() throws Exception {

		final String url = getMandatoryProperty(URL_PROP, "URL");
		final String username = getMandatoryProperty(USER_PROP, "username");
		final String password = getMandatoryProperty(PASSWORD_PROP, "password");

		final MariaDbDataSource ds = new MariaDbDataSource();
		ds.setUrl(url);
		ds.setUserName(username);
		ds.setPassword(password);

		return ds.getConnection();
	}

	/*
	 * It would be best to create JdbcTemplate objects directly from a DataSource, but DLSdicat used Connection objects
	 * directly, so this class does too. This method wraps a Connection in a DataSource, so that the DataSource can then
	 * be used to create a JdbcTemplate.
	 */
	private static JdbcTemplate makeJdbcTemplateFromConnection(Connection connection) {
		final SingleConnectionDataSource dataSource = new SingleConnectionDataSource(connection, true);
		return new JdbcTemplate(dataSource);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////

}
