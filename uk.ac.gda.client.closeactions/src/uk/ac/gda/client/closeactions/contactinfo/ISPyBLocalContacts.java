package uk.ac.gda.client.closeactions.contactinfo;

import java.util.regex.Pattern;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.regex.Matcher;

import org.mariadb.jdbc.MariaDbDataSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import gda.configuration.properties.LocalProperties;

public class ISPyBLocalContacts {

	private static String ispybUrl = "gda.px.contactinfo.ispyb.url";
	private static  String ispybUser = "gda.px.contactinfo.ispyb.user";
	private static String ispybPass = "gda.px.contactinfo.ispyb.password";

	private static final RowMapper<String> LC_MAPPER = new RowMapper<String>() {
		@Override
		public String mapRow(ResultSet rs, int rowNum) throws SQLException {
			final String visit = rs.getString(4);
			return visit;
		}
	};

	private static final RowMapper<String> NEXT_LC_MAPPER = new RowMapper<String>() {
		@Override
		public String mapRow(ResultSet rs, int rowNum) throws SQLException {
			String session = rs.getString(1) + ',' + rs.getString(2)+ ',' + rs.getString(3);
			return session;
		}
	};

	public static JdbcTemplate connectToDatabase() throws Exception {

		final String url = LocalProperties.get(ispybUrl);
		final String username = LocalProperties.get(ispybUser);
		final String password = LocalProperties.get(ispybPass);

		MariaDbDataSource ds = new MariaDbDataSource();
		ds.setUrl(url);
		ds.setUserName(username);
		ds.setPassword(password);

		final JdbcTemplate template = new JdbcTemplate(ds);
		return template;
	}

	private final JdbcTemplate template;

	public ISPyBLocalContacts(JdbcTemplate template) {
		this.template= template;
	}
	
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

	public List<String> forFollowingVisit(String beamline) throws Exception {

		final String sql = "CALL ispyb.retrieve_current_sessions(? , ?);";
		final int twentyFourHourTolerance = 60 * 24;

		final Object[] parameters = new Object[] { beamline, twentyFourHourTolerance };

		final List<String> fedID = template.query(sql, parameters, NEXT_LC_MAPPER);

		return fedID;
	}
}