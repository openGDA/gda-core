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
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.mariadb.jdbc.MariaDbDataSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import com.google.common.base.Optional;

import gda.configuration.properties.LocalProperties;

/**
 * {@link IcatBase} subclass that retrieves visit information from ISPyB.
 */
public class SpyCat extends DlsIcatBase {

	////////////////////////////////////////////////////////////////////////////////////////////////

	@Override
	protected List<String> getVisitsForUser(Connection connection, String username) throws Exception {

		final String sql = "CALL ispyb.retrieve_current_sessions_for_person(?, ?, ?)";

		final int tolerance = LocalProperties.getAsInt(SHIFT_TOL_PROP, 1440);
		final Object[] parameters = new Object[] { getInstrumentName(), username, tolerance };

		final JdbcTemplate template = makeJdbcTemplateFromConnection(connection);

		final List<String> visits = template.query(sql, parameters, VISIT_MAPPER);

		return visits;
	}

	@Override
	protected Optional<String> getLatestVisitWithPrefix(Connection connection, String visitPrefix) {

		final String sql = "CALL ispyb.retrieve_most_recent_session(?, ?)";

		final Object[] parameters = new Object[] { getInstrumentName(), visitPrefix };

		final JdbcTemplate template = makeJdbcTemplateFromConnection(connection);

		final List<String> visits = template.query(sql, VISIT_MAPPER, parameters);

		return (visits.isEmpty()) ? Optional.<String>absent() : Optional.of(visits.get(0));
	}

	private static final RowMapper<String> VISIT_MAPPER = new RowMapper<String>() {
		@Override
		public String mapRow(ResultSet rs, int rowNum) throws SQLException {
			final String visit = rs.getString(1);
			return visit;
		}
	};

	////////////////////////////////////////////////////////////////////////////////////////////////

	@Override
	protected String getTitleForVisitUsingConnection(String visit, Connection connection) throws Exception {

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

		return title;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////

	@Override
	protected Connection connectToDatabase() throws Exception {

		final String url = getMandatoryProperty(URL_PROP, "URL");
		final String username = getMandatoryProperty(USER_PROP, "username");
		final String password = getMandatoryProperty(PASSWORD_PROP, "password");

		MariaDbDataSource ds = new MariaDbDataSource();
		ds.setUrl(url);
		ds.setUserName(username);
		ds.setPassword(password);

		return ds.getConnection();
	}

	/*
	 * It would be best to create JdbcTemplate objects directly from a DataSource, but DLSdicat used Connection
	 * objects directly, so this class does too. This method wraps a Connection in a DataSource, so that the
	 * DataSource can then be used to create a JdbcTemplate.
	 */
	private static JdbcTemplate makeJdbcTemplateFromConnection(Connection connection) {
		final SingleConnectionDataSource dataSource = new SingleConnectionDataSource(connection, true);
		final JdbcTemplate template = new JdbcTemplate(dataSource);
		return template;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////

}
