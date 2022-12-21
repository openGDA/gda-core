/*-
 * Copyright © 2014 Diamond Light Source Ltd.
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

package gda.jython.scriptcontroller.logging;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.LinkedHashMap;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.commandqueue.JythonScriptProgressProvider;
import gda.configuration.properties.LocalProperties;
import gda.factory.FactoryException;
import gda.jython.InterfaceProvider;
import gda.jython.scriptcontroller.ScriptControllerBase;
import uk.ac.gda.api.remoting.ServiceInterface;

/**
 * A script controller which logs in a Derby database all the ScriptControllerLoggingMessage objects which are passed
 * through it.
 * <p>
 * It provides the tools to enable other classes to extract information about the contents of the database.
 */
@ServiceInterface(ILoggingScriptController.class)
public class LoggingScriptController extends ScriptControllerBase implements ILoggingScriptController {

	private static final Logger logger = LoggerFactory.getLogger(LoggingScriptController.class);
	private static final SecureRandom random = new SecureRandom();
	private static final String DRIVER = "org.apache.derby.jdbc.EmbeddedDriver";
	private static final String DATE_ADDED_COL_NAME = "DATE_ADDED";
	private static final String DATE_UPDATED_COL_NAME = "DATE_UPDATED";
	private static final String PK_COLUMNNAME = "UNIQUE_ID";
	private static final String SK_COLUMNNAME = "SCRIPT_NAME";

	public static String createUniqueID(String key) {
		long n = random.nextLong();
		if (n == Long.MIN_VALUE) {
			n = 0; // corner case
		} else {
			n = Math.abs(n);
		}

		return key + "_" + Long.toString(n);
	}

	private Class<? extends ScriptControllerLoggingMessage> messageClassToLog;
	private String directory;
	protected String url;
	private Connection conn;
	private HashMap<Method, String> columnGetters;
	private HashMap<Method, String> refreshColumnGetters;
	private String tableName;
	private PreparedStatement psInsert;
	private PreparedStatement psFetchStartTime;
	private PreparedStatement psSimpleListAll;
	private PreparedStatement psFetchEntry;
	private PreparedStatement psRefresh;
	private PreparedStatement psLatestRunID;
	private String dbName;

	public LoggingScriptController() {
		super();

		try {
			Class.forName(DRIVER);
		} catch (ClassNotFoundException e) {
			logger.error("Derby driver class not found. LoggingScriptController objects cannot be used until class "
					+ DRIVER + " is in the path.", e);
		}

	}

	@Override
	public void configure() throws FactoryException {
		if (isConfigured()) {
			return;
		}
		super.configure();
		try {
			createDatabase();
		} catch (Exception e) {
			throw new FactoryException("Exception while configuring script controller" + getName(), e);
		}
		setConfigured(true);
	}

	@Override
	public void reconfigure() throws FactoryException {
		super.reconfigure();
		try {
			closeDatabase();
			createDatabase();
		} catch (Exception e) {
			throw new FactoryException("Exception while reconfiguring script controller" + getName(), e);
		}
	}

	/**
	 * Expects arg is of the class messageClassToLog.
	 */
	@Override
	public void update(Object o, Object arg) {
		if (arg.getClass().equals(messageClassToLog)) {
			ScriptControllerLogResults result = logMessage((ScriptControllerLoggingMessage) arg);
			if (result != null) {
				notifyIObservers(this, result);
			}

			float percent = ((ScriptControllerLoggingMessage) arg).getPercentDone();
			String msg = ((ScriptControllerLoggingMessage) arg).getMsg();
			try {
				JythonScriptProgressProvider.sendProgress(percent, msg, false);
			} catch (InterruptedException e) {
				// ignore as false flag used
			}
		}
		super.update(o, arg);
	}

	@Override
	public ScriptControllerLogResults[] getTable() {
		ResultSet rs = null;
		try {
			ScriptControllerLogResults[] results = new ScriptControllerLogResults[] {};
			String myVisit = InterfaceProvider.getBatonStateProvider().getBatonHolder().getVisitID();

			psSimpleListAll.setString(1, myVisit);
			rs = psSimpleListAll.executeQuery();
			Boolean atFirst = rs.next();
			while (atFirst) {
				ScriptControllerLogResults result = new ScriptControllerLogResults(rs.getString(1), rs.getString(2),
						rs.getTimestamp(3), rs.getTimestamp(4));
				results = (ScriptControllerLogResults[]) ArrayUtils.add(results, result);
				atFirst = rs.next();
			}
			return results;
		} catch (SQLException e) {
			logger.error("Exception fetching stored log messages in " + getName(), e);
			return new ScriptControllerLogResults[] {};
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
				}
			}
		}
	}

	@Override
	public synchronized ScriptControllerLogResultDetails getDetails(String uniqueID) {
		ResultSet rs = null;
		try {
			// psFetchEntry
			psFetchEntry.setString(1, uniqueID);
			rs = psFetchEntry.executeQuery();
			Boolean atFirst = rs.next();
			if (!atFirst) {
				return new ScriptControllerLogResultDetails(uniqueID, new HashMap<String, String>());
			}
			HashMap<String, String> details = new LinkedHashMap<String, String>();
			for (String columnName : columnGetters.values()) {
				String nameInTable = columnName.replace(" ", "_");
				String value = rs.getString(nameInTable);
				details.put(columnName, value);
			}
			return new ScriptControllerLogResultDetails(uniqueID, details);
		} catch (SQLException e) {
			//This exception occurs per scan point and causes the jython console, logging and plot to gring to a halt
			//Removed until we can understand why
			logger.error("Exception adding to log by " + getName(), e);
			return new ScriptControllerLogResultDetails(uniqueID, new HashMap<String, String>());
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
				}
			}
		}
	}

	@Override
	public ScriptControllerLogResultDetails getMostRecentRun() {
		ResultSet rs = null;
		String id = "";
		try {
			rs = psLatestRunID.executeQuery();
			rs.next();
			id = rs.getString(1);
			return getDetails(id);
		} catch (SQLException e) {
			logger.error("Exception fetching details of most recent run in " + getName(), e);
			return new ScriptControllerLogResultDetails(id, new HashMap<String, String>());
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
				}
			}
		}

	}

	public Class<? extends ScriptControllerLoggingMessage> getMessageClassToLog() {
		return messageClassToLog;
	}

	public void setMessageClassToLog(Class<? extends ScriptControllerLoggingMessage> messageClassToLog) {
		this.messageClassToLog = messageClassToLog;
	}

	public String getDirectory() {
		return directory;
	}

	public void setDirectory(String directory) {
		this.directory = directory;
	}

	private void createDatabase() throws Exception {

		if (getMessageClassToLog() == null) {
			logger.warn("Message class type not set, cannot create Script Controller log database " + getName());
		}

		tableName = messageClassToLog.getSimpleName();
		String varDir = getDirectory();
		if (varDir == null) {
			LocalProperties.getVarDir();
		}
		try {
			makeDB(varDir);
		} catch (SQLException e) {
			// assume a problem with either directory not available or the schema has changed
			if ((new File(dbName)).exists()){
				FileUtils.deleteDirectory(new File(dbName));
				makeDB(varDir);
			} else {
				if (new File(varDir).exists()) {
					makeDB(varDir);
				} else {
					throw new Exception(varDir + " could not be created");
				}
			}
		}
	}

	private void makeDB(String varDir) throws SQLException {
		connectToDB(varDir);//createConnection();
		determineColumns();
		createTable();
		createPreparedStatements();
	}

	private void connectToDB(String varDir) throws SQLException {
		dbName = varDir + tableName;
		url = "jdbc:derby:" + dbName + ";create=true";
		conn = DriverManager.getConnection(url);
	}

	private void createTable() throws SQLException {
		if (tableExists()) {
			return;
		}

		// make SQL to create table
		String createString = "CREATE TABLE " + tableName + " ( " + PK_COLUMNNAME + " VARCHAR(50) PRIMARY KEY,";
		createString += SK_COLUMNNAME + " VARCHAR(80),";

		for (String columnName : columnGetters.values()) {
			columnName = columnName.replace(" ", "_");
			createString += columnName + " VARCHAR(150),";
		}
		createString += DATE_ADDED_COL_NAME + " TIMESTAMP," + DATE_UPDATED_COL_NAME + " TIMESTAMP)";

		// run the SQL
		Statement s = conn.createStatement();
		s.execute(createString);
		s.close();

	}

	private boolean tableExists() {
		ResultSet rs = null;
		try {
			DatabaseMetaData dbmd = conn.getMetaData();

			rs = dbmd.getTables(null, null, tableName.toUpperCase(), new String[] { "TABLE" });
			int rowcount = 0;
			while (rs.next())
				rowcount++;
			rs.close();
			return rowcount != 0;
		} catch (SQLException e) {
			logger.debug("Exception counting number of rows in table by LogginScriptController " + getName(), e);
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
				}
			}
		}
		return false;
	}

	private void determineColumns() {
		Method[] gettersWeWant = new Method[0];
		columnGetters = new LinkedHashMap<Method, String>();
		refreshColumnGetters = new HashMap<Method, String>();

		// loop over all methods to find ones with the correct annotation
		Method[] methods = messageClassToLog.getDeclaredMethods();
		for (Method method : methods) {
			Annotation[] annotations = method.getDeclaredAnnotations();
			for (Annotation annotation : annotations) {
				if (annotation instanceof ScriptControllerLogColumn) {
					gettersWeWant = (Method[]) ArrayUtils.add(gettersWeWant, method);
					continue;
				}
			}
		}

		// order the methods bsed on the annotation's column index
		Method[] gettersWeWant_ordered = new Method[gettersWeWant.length];
		for (Method method : gettersWeWant) {
			Annotation[] annotations = method.getDeclaredAnnotations();
			for (Annotation annotation : annotations) {
				if (annotation instanceof ScriptControllerLogColumn column) {
					gettersWeWant_ordered[column.columnIndex()] = method;
				}
			}
		}

		// add the method references and their column labels to the hashmaps
		for (Method method : gettersWeWant_ordered) {
			Annotation[] annotations = method.getDeclaredAnnotations();
			for (Annotation annotation : annotations) {
				if (annotation instanceof ScriptControllerLogColumn column) {
					columnGetters.put(method, column.columnName());
					if (((ScriptControllerLogColumn) annotation).refresh()) {
						refreshColumnGetters.put(method, ((ScriptControllerLogColumn) annotation).columnName());
					}
				}
			}
		}

	}

	private void createPreparedStatements() throws SQLException {

		String listOfColumns = PK_COLUMNNAME + "," + SK_COLUMNNAME + ",";
		String listOfQs = "?,?,";
		for (String columnName : columnGetters.values()) {
			columnName = columnName.replace(" ", "_");
			listOfColumns += columnName + ",";
			listOfQs += "?,";
		}
		listOfColumns = listOfColumns.substring(0, listOfColumns.length() - 1);
		listOfQs += "?,?";

		String insertStatment = "INSERT INTO " + tableName + " (" + listOfColumns + "," + DATE_ADDED_COL_NAME + ","
				+ DATE_UPDATED_COL_NAME + ") VALUES (" + listOfQs + ")";
		psInsert = conn.prepareStatement(insertStatment);

		String listStatement = "SELECT " + PK_COLUMNNAME + "," + SK_COLUMNNAME + "," + DATE_ADDED_COL_NAME + ","
				+ DATE_UPDATED_COL_NAME + " FROM " + tableName + " WHERE visit_id = (?) ORDER BY " + DATE_UPDATED_COL_NAME + " DESC";
		psSimpleListAll = conn.prepareStatement(listStatement);

		String fetchStatement = "SELECT * FROM " + tableName + " WHERE " + PK_COLUMNNAME + "= (?)";
		psFetchEntry = conn.prepareStatement(fetchStatement);

		String fetchStartTimeStatement = "SELECT " + DATE_ADDED_COL_NAME + " FROM " + tableName + " WHERE " + PK_COLUMNNAME + "= (?)";
		psFetchStartTime = conn.prepareStatement(fetchStartTimeStatement);

		String sqlColumns = "";
		for (String columnName : refreshColumnGetters.values()) {
			columnName = columnName.replace(" ", "_");
			sqlColumns += columnName + "=(?),";
		}
		sqlColumns += DATE_UPDATED_COL_NAME + "=(?)";
		psRefresh = conn.prepareStatement("UPDATE " + tableName + " SET " + sqlColumns + " WHERE " + PK_COLUMNNAME
				+ "= (?)");

		psLatestRunID = conn.prepareStatement("SELECT " + PK_COLUMNNAME + " FROM " + tableName + " ORDER BY "
				+ DATE_UPDATED_COL_NAME + " DESC FETCH FIRST ROW ONLY");
	}

	private synchronized ScriptControllerLogResults logMessage(ScriptControllerLoggingMessage arg) {
		ResultSet rs = null;
		try {
			// get its primary key value
			psFetchEntry.setString(1, arg.getUniqueID());
			rs = psFetchEntry.executeQuery();
			int rowcount = 0;
			while (rs.next())
				rowcount++;

			if (rowcount > 0) {
				return refreshExistingEntry(arg);
			}
			return insertNewEntry(arg);

		} catch (SQLException e) {
			// If this exception occurs per scan point then the jython console, logging and plot grind to a halt
			logger.error("Exception adding to log by " + getName(), e);
			return null;
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
				}
			}
		}
	}

	private ScriptControllerLogResults insertNewEntry(ScriptControllerLoggingMessage arg) throws SQLException {
		String[] valuesToAdd = getValues(arg);
		psInsert.setString(1, arg.getUniqueID());
		psInsert.setString(2, arg.getName());
		int i = 3;
		for (; i <= valuesToAdd.length + 2; i++) {
			psInsert.setString(i, valuesToAdd[i - 3]);
		}
		Timestamp now = new Timestamp(System.currentTimeMillis());
		psInsert.setTimestamp(i++, now);
		psInsert.setTimestamp(i++, now);
		psInsert.executeUpdate();
		return new ScriptControllerLogResults(arg.getUniqueID(), arg.getName(), now, now);
	}

	private ScriptControllerLogResults refreshExistingEntry(ScriptControllerLoggingMessage arg) throws SQLException {
		String[] valuesToAdd = getUpdateValues(arg);
		int i = 1;
		for (; i <= valuesToAdd.length; i++) {
			psRefresh.setString(i, valuesToAdd[i - 1]);
		}
		Timestamp now = new Timestamp(System.currentTimeMillis());
		psRefresh.setTimestamp(i++, now);
		psRefresh.setString(i++, arg.getUniqueID());
		int numLinesUpdated = psRefresh.executeUpdate();
		if (numLinesUpdated != 1) {
			System.out.println("something's wrong");
			return null;
		}

		psFetchStartTime.setString(1, arg.getUniqueID());
		ResultSet rsStartTime = psFetchStartTime.executeQuery();
		rsStartTime.next();
		Timestamp startTime = rsStartTime.getTimestamp(1);

		return new ScriptControllerLogResults(arg.getUniqueID(), arg.getName(), startTime, now);
	}

	private String[] getUpdateValues(ScriptControllerLoggingMessage arg) {
		String[] values = new String[0];
		for (Method getter : refreshColumnGetters.keySet()) {
			try {
				String value = getter.invoke(arg, new Object[] {}).toString();
				values = (String[]) ArrayUtils.add(values, value);
			} catch (Exception e) {
				logger.debug("Exception invoking method " + getter.getName(), e);
			}
		}
		return values;
	}

	private String[] getValues(ScriptControllerLoggingMessage arg) {
		String[] values = new String[0];
		for (Method getter : columnGetters.keySet()) {
			try {
				String value = getter.invoke(arg, new Object[] {}).toString();
				values = (String[]) ArrayUtils.add(values, value);
			} catch (Exception e) {
				logger.debug("Exception invoking method " + getter.getName(), e);
			}
		}
		return values;
	}

	private void closeDatabase() throws SQLException {

		if (conn != null) {
			conn.close();
			boolean gotSQLExc = false;
			try {
				DriverManager.getConnection("jdbc:derby:" + dbName + ";shutdown=true");
			} catch (SQLException se) {
				if (se.getSQLState().equals("XJ015")) {
					gotSQLExc = true;
				}
			}
			if (!gotSQLExc) {
				logger.info("LoggingScriptController database for " + messageClassToLog
						+ " objects did not shut down normally");
			} else {
				logger.info("LoggingScriptController database for " + messageClassToLog + " objects shut down normally");
			}
		}
	}

}
