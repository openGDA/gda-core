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

package gda.data.metadata.icat;

import gda.configuration.properties.LocalProperties;
import gda.util.exceptionUtils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.util.StringUtils;

/**
 * The Icat which talks to the Diamond ICAT database.
 * 
 * <p>This is an abbreviated schema diagram for the ICAT database:
 * 
 * <pre>
 * +------------------+        +---------------+        +------------------+
 * | SHIFT            |        | INVESTIGATION |        | INVESTIGATOR     |        +------------------+
 * |------------------|        |---------------|        |------------------|        | FACILITY_USER    |
 * | INVESTIGATION_ID |--------| ID            |--------| INVESTIGATION_ID |        |------------------|
 * | START_DATE       |*      1| VISIT_ID      |1      *| FACILITY_USER_ID |--------| FACILITY_USER_ID |
 * | END_DATE         |        | INSTRUMENT    |        | ...              |*      1| FEDERAL_ID       |
 * | ...              |        | ...           |        +------------------+        | FIRST_NAME       |
 * +------------------+        +---------------+                                    | LAST_NAME        |
 *                                                                                  | ...              |
 *                                                                                  +------------------+
 * </pre>
 */
public class DLSIcat extends IcatBase {
	
	protected static class Shift {
		
		private String investigationId;
		private Date startDate;
		private Date endDate;
		
		public Shift(String investigationId, Date startDate, Date endDate) {
			this.investigationId = investigationId;
			this.startDate = startDate;
			this.endDate = endDate;
		}
		
		public String getInvestigationId() {
			return investigationId;
		}
		
		public Date getStartDate() {
			return startDate;
		}
		
		public Date getEndDate() {
			return endDate;
		}
		
		@Override
		public String toString() {
			return String.format("Shift(investigationId=%s, startDate=%s, endDate=%s)",
				StringUtils.quote(investigationId),
				startDate,
				endDate);
		}
	}
	
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
	private static final String VISIT_QUERY = "lower(visit_id)visit_id:investigation:id";

	/**
	 * The access string to retrieve the experiment ID from the database.
	 */
	private static final String TITLE_QUERY = "TITLE:investigation:id";
	
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
		ResultSet resultSet = null;
		Connection connection = null;
		String value = null;
		Statement statement;

		try {
			connection = connectToDatabase();
			statement = connection.createStatement();
			
			List<Shift> shifts = findAllShiftsForUser(statement, userNameFilter);

			if (shifts.size() > 0) {

				filterShiftsByTimeAndVisit(visitIDFilter, statement, shifts);

				// run query, filtering on anything left, and build a comma separated list of results
				if (shifts.size() > 0) {
					resultSet = statement.executeQuery(createQuery(accessName, shifts));
					value = concatenateResultSet(resultSet);
				}
			}

		} catch (Exception e) {
			throw new Exception("Exception while trying to connect to the Icat database", e);
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException e) {
					exceptionUtils.logException(logger, "getValue.connection.close", e);
				}
			}
		}

		return value;
	}

	private Connection connectToDatabase() throws Exception {
		Connection connection = null;
		Class.forName("oracle.jdbc.driver.OracleDriver").newInstance();
		connection = DriverManager.getConnection(LocalProperties.get(URL_PROP), LocalProperties.get(USER_PROP),
				LocalProperties.get(PASSWORD_PROP));
		return connection;
	}

	/**
	 * Finds all possible shifts for the specified user on the beamline.
	 */
	private List<Shift> findAllShiftsForUser(Statement statement, String userName) throws SQLException {
		ResultSet resultSet = null;
		String sql = "select distinct investigation_id, start_date, end_date from shift "
				+ "where investigation_id in (select investigation_id from investigator "
				+ "where facility_user_id in (select facility_user_id from facility_user where federal_id = ";

		resultSet = statement.executeQuery(sql + "'" + userName + "'))");

		List<Shift> shifts = new ArrayList<Shift>();

		while (resultSet.next()) {
			Shift s = SHIFT_ROW_MAPPER.mapRow(resultSet, 0);
			shifts.add(s);
		}
		
		return shifts;
	}
	
	private static final RowMapper<Shift> SHIFT_ROW_MAPPER = new RowMapper<Shift>() {
		@Override
		public Shift mapRow(ResultSet rs, int rowNum) throws SQLException {
			String investigationId = rs.getString("INVESTIGATION_ID");
			
			String startDateString = rs.getString("START_DATE");
			String endDateString = rs.getString("END_DATE");
			
			SimpleDateFormat format = new SimpleDateFormat(ORACLE_DATE_FORMAT);
			
			Date startDate;
			Date endDate;
			
			try {
				startDate = format.parse(startDateString);
			} catch (ParseException e) {
				throw new RuntimeException(String.format("Unable to parse start date %s", StringUtils.quote(startDateString)));
			}
			
			try {
				endDate = format.parse(endDateString);
			} catch (ParseException e) {
				throw new RuntimeException(String.format("Unable to parse end date %s", StringUtils.quote(endDateString)));
			}
			
			Shift s = new Shift(investigationId, startDate, endDate);
			return s;
		}
	};
	
	/**
	 * Dates are returned from the icat. An example is "2006-12-5.0.0. 0. 0" We believe the last two zeros
	 * in the case are seconds and milli-seconds. Neither of these are relevant for our purposes here, which
	 * are based on coarser time scales. Therefore our date format class instance ignores these.
	 */
	private static final String ORACLE_DATE_FORMAT = "yyyy-M-d.H.m.";
	
	/**
	 * From a set of allocated shifts determine which are current on the instrument in use. Instrument equates to an end
	 * station on the facility. The instrument is assumed to be stored in another metadata item, name instrument. At the
	 * end the ArrayLists ids, shiftStart and shiftEnd will be updated to contain investigations currently scheduled on
	 * this instrument. A tolerance can be applied to the shift periods via the XML element <shiftTolerance>.
	 * 
	 * @param filterByVisitID
	 *            Whether to use the stored visitID to filter the investigation value
	 * @param statement
	 *            Object used to execute an SQL statement via JDBC.
	 * @throws ParseException
	 * @throws SQLException
	 */

	/**
	 * Filters the list of {@link Shift}s, keeping only the shifts that are currently in progress.
	 * 
	 * <p>If {@link IcatBase#setOperatingDate(Date) operatingDate} has been set, only the visits in progress at that
	 * time will be retained.
	 * 
	 * @param visitId an optional visit ID; if specified, only that visit will be kept
	 */
	private void filterShiftsByTimeAndVisit(String visitId, Statement statement, List<Shift> shifts) throws SQLException {
		ResultSet resultSet = null;
		Iterator<Shift> shiftsIterator = shifts.iterator();
		long tolerance = LocalProperties.getAsInt(SHIFT_TOL_PROP,1440);

		while (shiftsIterator.hasNext()) {
			Shift shift = shiftsIterator.next();

			String query = "select id from investigation where" + " instrument='" + getInstrumentName() + "' and id='"
					+ shift.getInvestigationId() + "'";

			// add a filter on visit ID if relevant
			if (visitId != null && !visitId.isEmpty()) {
				query += " AND lower(visit_id)='" + visitId + "'";
			}

			resultSet = statement.executeQuery(query);

			if (resultSet.next()) {

				Date now;
				if (operatingDate != null) {
					now = operatingDate;
				} else {
					now = new Date();
				}

				final long toleranceInMilliseconds = tolerance * 60 * 1000;
				Date adjustedStartDate = new Date(shift.getStartDate().getTime() - toleranceInMilliseconds);
				Date adjustedEndDate   = new Date(shift.getEndDate().getTime()   + toleranceInMilliseconds);
				if (now.before(adjustedStartDate) || now.after(adjustedEndDate)) {
					shiftsIterator.remove();
				}
			} else {
				shiftsIterator.remove();
			}
		}
	}

	/**
	 * Create an SQL query to get the actual item of metadata for a required investigation. This relies upon the
	 * metadata accessName obtained via XML configuration. The query will be of the form
	 * 
	 * <pre>
	 *   select distinct columns from table where name=id1 or name=id2 ....
	 * </pre>
	 * 
	 * Elements of this query will be determined using the accessName, assumed to be of the form columns:table:name.
	 * 
	 * <pre>
	 *   columns: should be a single column name or a comma separated list of
	 *   columns from a database table.
	 *   table: the name of the database table.
	 *   name: the name used to refer to investigation id in the table.
	 * </pre>
	 * 
	 * @return The SQL query.
	 */
	protected static String createQuery(String accessName, List<Shift> shifts) {
		String[] sqlItems = accessName.split(":");
		final String columns = sqlItems[0];
		final String table = sqlItems[1];
		final String name = sqlItems[2];

		String query = "select distinct " + columns + " from " + table + " where ";

		// add condition for each investigation ID
		for (Shift s : shifts) {
			query += name + "='" + s.getInvestigationId() + "' OR ";
		}
		
		// remove trailing " OR "
		query = query.substring(0, query.length() - 4);

		// add a closing ) if the access name included a nested query;
		// it is assumed the access name had the opening (
		if (table.contains("(")) {
			query += ")";
		}

		return query;
	}

	/**
	 * An sql query may result in a table of results. This method concatenates those results into a single string.
	 * Columns in each row are space separated. Each row is comma separated.
	 * 
	 * @param resultSet
	 *            A set of results from a JDBC sql command.
	 * @return The concatented contents of the input result set.
	 * @throws SQLException
	 */
	private String concatenateResultSet(ResultSet resultSet) throws SQLException {
		String value = "";
		ResultSetMetaData rsm = resultSet.getMetaData();
		int columns = rsm.getColumnCount();
		int length;
		String result;

		while (resultSet.next()) {
			for (int i = 1; i <= columns; i++) {
				result = resultSet.getString(i);
				if (result != null) {
					value += result;
					if (i == columns) {
						value += ", ";
					} else {
						value += " ";
					}
				}
			}
		}
		if ((length = value.length()) > 0) {
			value = value.substring(0, length - 2);
		}

		return value;
	}

}
