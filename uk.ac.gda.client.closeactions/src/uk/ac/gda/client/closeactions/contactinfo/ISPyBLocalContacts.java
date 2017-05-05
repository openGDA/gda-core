package uk.ac.gda.client.closeactions.contactinfo;

import java.util.regex.Pattern;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.regex.Matcher;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

public class ISPyBLocalContacts {

	private final JdbcTemplate template;

	public ISPyBLocalContacts(JdbcTemplate template) {
		this.template = template;
	}

	private static final RowMapper<String> LC_MAPPER = new RowMapper<String>() {
		@Override
		public String mapRow(ResultSet rs, int rowNum) throws SQLException {
			final String visit = rs.getString(4);
			return visit;
		}
	};

	public List<String> forCurrentVisit(String visit) throws Exception {

		// Split visit code (e.g. "cm14486-3") into proposal code ("cm") and proposal number (14486)
		final Pattern visitPattern = Pattern.compile("([a-z]+)(\\d+)-(\\d+)");
		final Matcher matcher = visitPattern.matcher(visit);
		if (!matcher.matches()) {
			throw new RuntimeException("Invalid visit: " + visit);
		}
		final String proposalCode = matcher.group(1);
		final int proposalNumber = Integer.parseInt(matcher.group(2));
		final int endthing = Integer.parseInt(matcher.group(3));

		final String sql = "CALL ispyb.retrieve_lcs_for_session(?, ?, ?)";

		final Object[] parameters = new Object[] { proposalCode, proposalNumber, endthing };

		final List<String> fedID = template.query(sql, parameters, LC_MAPPER);

		return fedID;
	}
}