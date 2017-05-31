/*-
 * Copyright © 2017 Diamond Light Source Ltd.
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

package uk.ac.gda.client.closeactions.contactinfo;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

public class ISPyBVisits {

	private final JdbcTemplate template;

	public ISPyBVisits(JdbcTemplate template) {
		this.template= template;
	}

	private static final RowMapper<String> NEXT_LC_MAPPER = new RowMapper<String>() {
		@Override
		public String mapRow(ResultSet rs, int rowNum) throws SQLException {
			String[] session = rs.getString(1).split("-");
			String visit = session[0];
			return visit;
		}
	};

	public List<String> followingVisits(String beamline) throws Exception {

		final String sql = "CALL ispyb.retrieve_current_sessions(? , ?);";
		final int twentyFourHourTolerance = 60 * 24;

		final Object[] parameters = new Object[] { beamline, twentyFourHourTolerance };

		final List<String> visits = template.query(sql, parameters, NEXT_LC_MAPPER);

		return visits;
	}
}