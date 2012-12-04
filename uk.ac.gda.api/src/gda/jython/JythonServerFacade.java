/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council Daresbury Laboratory
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

package gda.jython;

import gda.configuration.properties.LocalProperties;
import gda.device.DeviceException;
import gda.factory.Findable;
import gda.factory.Finder;
import gda.jython.authenticator.Authenticator;
import gda.jython.authenticator.AuthenticatorProvider;
import gda.jython.authenticator.UserAuthentication;
import gda.jython.authoriser.AuthoriserProvider;
import gda.jython.batoncontrol.BatonChanged;
import gda.jython.batoncontrol.BatonLeaseRenewRequest;
import gda.jython.batoncontrol.ClientDetails;
import gda.observable.IObserver;
import gda.observable.ObservableComponent;
import gda.scan.IScanDataPoint;
import gda.scan.Scan;
import gda.util.LibGdaCommon;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Vector;

import org.python.core.PyFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

/**
 * Provides a single point of access for the Jython package for all Java classes. This will work whether the Java is
 * located client-side, server-side, on the same or different ObjectServers.
 * <p>
 * This object holds inside a reference to the CommandServer object which holds inside of it the interpreter.
 * <p>
 * Calls to this class must not occur during instantiation of objects. However, if they use the Configurable interface,
 * then this class can be used during their configure phase.
 * <P>
 * Objects should register themselves as IObservers of this object to receive updates about scans, scripts, the script
 * queue and data from scans.
 * <P>
 * This object does not directly implement the Jython interface. This is deliberate to safely restrict what
 * functionality is given to other classes in the GDA. Only this class should communicate directly to the local
 * JythonServer or JythonAdapter.
 */
public class JythonServerFacade implements IObserver, JSFObserver, IScanStatusHolder, ICommandRunner,
		ICurrentScanController, ITerminalPrinter, IJythonNamespace, IAuthorisationHolder, IScanDataPointProvider,
		IScriptController, IPanicStop, IBatonStateProvider, InitializingBean, AliasedCommandProvider, IJythonContext,
		ITerminalOutputProvider {

	private static final Logger logger = LoggerFactory.getLogger(JythonServerFacade.class);

	private static JythonServerFacade theInstance = null;

	Vector<INamedScanDataPointObserver> namedSDPObservers = new Vector<INamedScanDataPointObserver>();
	
	Vector<IScanDataPointObserver> allSDPObservers = new Vector<IScanDataPointObserver>();

	String name = "";

	boolean redirectNextInputTo_Raw_Input = false;

	Jython commandServer = null;

	Vector<Terminal> myTerminals = new Vector<Terminal>();

	ObservableComponent myIObservers = new ObservableComponent();

	private volatile int originalAuthorisationLevel = 0;

	private volatile int indexNumberInJythonServer = 0;

	private volatile String originalUsername = null;

	private volatile String visitID = "";

	private volatile String alternateUsername = null;

	private volatile boolean runningAsAlternateUser = false;

	private IScanDataPoint lastScanDataPoint;

	/**
	 * Creates a Jython server facade, using the {@link Finder} to obtain the Jython command server.
	 * 
	 * @throws InstantiationException
	 */
	private JythonServerFacade() throws InstantiationException {
		this((Jython) Finder.getInstance().find("command_server"));
	}

	/**
	 * Creates a Jython server facade, using the specified Jython command server.
	 * 
	 * @param commandServer
	 *            the Jython command server
	 * @throws InstantiationException
	 */
	protected JythonServerFacade(Jython commandServer) throws InstantiationException {
		try {
			// because this is a call to the Finder, this facade cannot be called during the instantiation phase. It can
			// be called during the configure phase.
			name = generateRandomName();
			this.commandServer = commandServer;

			InetAddress hostAddress = java.net.InetAddress.getLocalHost();
			String localHost = hostAddress.getHostName();

			// check that we found something
			if (commandServer == null) {
				final String msg = "Can't find a Command Server.";
				logger.error(msg);
				throw new InstantiationException(msg); // Important - do no exit incorrect for rcp gda client
			}

			// check that the client matches the GDA release of the JythonServer.
			String csRelease = commandServer.getRelease(name);
			String myRelease = gda.util.Version.getRelease();
			if (!csRelease.equals(myRelease)) {
				final String msg = "Using a different release of the GDA to the Command Server.";
				logger.error(msg);
				throw new InstantiationException(msg); // Important - do no exit incorrect for rcp gda client
			}

			// register with the Command Server and validate login information supplied by the user
			try {
				originalUsername = UserAuthentication.getUsername();
				indexNumberInJythonServer = commandServer.addFacade(this, name, localHost, UserAuthentication
						.getUsername(), "");
				originalAuthorisationLevel = commandServer.getAuthorisationLevel(indexNumberInJythonServer);
			} catch (DeviceException e) {
				final String msg = "Login failed for user: " + UserAuthentication.getUsername();
				logger.error(msg);
				throw new InstantiationException(msg); // Important - do no exit incorrect for rcp gda client
			}

			// create a list of all GUI panels which support IObserver interface to receive updates
			ArrayList<Findable> panels = Finder.getInstance().listAllLocalObjects("AcquisitionPanel");
			for (Findable panel : panels) {
				if (panel instanceof IObserver) {
					addIObserver((IObserver) panel);
				} 
			}

		} catch (Exception ex) {
			logger.error("CommandServerFacade: error during instantiation: " + ex.getMessage(),ex);
			throw new InstantiationException("CommandServerFacade: error during instantiation: " + ex.getMessage());
		}
	}

	/**
	 * Returns the local singleton instance.
	 * 
	 * @return JythonServerFacade
	 */
	public static synchronized JythonServerFacade getInstance() {

		if (theInstance == null) {
			try {
				theInstance = new JythonServerFacade();
			} catch (InstantiationException ex) {
				logger.error("Error instatiating JythonServerFacade", ex);
			}
		}
		return theInstance;
	}

	/**
	 * Returns the local singleton instance throwing an Exception if one is required rather than logging it directly.
	 * 
	 * @return JythonServerFacade
	 * @throws Exception
	 */
	public static JythonServerFacade getCurrentInstance() throws Exception {

		if (theInstance == null) {
			theInstance = new JythonServerFacade();
		}
		return theInstance;
	}

	/**
	 * Deregisters the local singleton instance from the JythonServer and sets it to null. This should only be used when
	 * shutting down the process.
	 */
	public static void disconnect() {
		if (theInstance != null) {
			theInstance.commandServer.removeFacade(theInstance.name);
			theInstance = null;
		}
	}

	// methods to copy the Jython interface

	/**
	 * Returns the current authorisation level (this value could be dynamic if a baton is in use)
	 * 
	 * @return the authorisationLevel at this moment in time
	 */
	@Override
	public int getAuthorisationLevel() {

		// if an object server then auth level will be Integer.MAX_VALUE
		if (LocalProperties.isBatonManagementEnabled()
				&& originalAuthorisationLevel == Integer.MAX_VALUE) {
			return originalAuthorisationLevel;
		}

		// return 0 if the baton is in use but not held by this client
		if (LocalProperties.isBatonManagementEnabled() && !amIBatonHolder()) {
			return 0;
		}

		// otherwise return the value from the JythonServer when this object first registered
		return originalAuthorisationLevel;
	}

	/**
	 * @return the authorisation level ignoring any current baton status
	 */
	@Override
	public int getAuthorisationLevelAtRegistration() {
		return originalAuthorisationLevel;
	}

	/**
	 * @return the index (public id) of this Client.
	 */
	public int getClientID() {
		return indexNumberInJythonServer;
	}

	/**
	 * @param scriptName
	 *            String
	 * @param sourceName
	 *            String
	 * @see Jython#runCommand(String, String)
	 */
	public void runScript(String scriptName, String sourceName) {
		// open up a new file
		File file = new File(locateScript(scriptName));
		runScript(file, sourceName);
	}

	/**
	 * @param scriptName
	 *            String
	 * @param sourceName
	 *            String
	 * @param scan
	 * @throws Exception 
	 * @see Jython#runCommand(String, String)
	 */
	public void runScript(String scriptName, String sourceName, Scan scan) throws Exception {
		// open up a new file
		String filePath = locateScript(scriptName);
		if(filePath==null)
			throw new Exception("Unable to locate file for script:"+scriptName);
		File file = new File(filePath);
		runScript(file, sourceName, scan);
	}

	/**
	 * @param script
	 *            File
	 * @param sourceName
	 *            String
	 * @see Jython#runCommand(String,String)
	 */
	@Override
	public void runScript(File script, String sourceName) {
		try {
			String commands;
			commands = slurp(script);
			// only run if no other scan is runningcommandserver
			// FIXME this has an obvious race condition, but worse it just ignores the request if busy
			if (commandServer.getScriptStatus(name) == Jython.IDLE) {
				commandServer.runScript(commands, sourceName, name);
			} else {
				logger.error("Unable to run script " + script.getAbsolutePath() + " as server os busy");
			}
		} catch (IOException e) {
			logger.error("Unable to run script " + script.getAbsolutePath(),e);
		}
	}

	/**
	 * @param script
	 *            File
	 * @param sourceName
	 *            String
	 * @param scan
	 * @throws Exception 
	 * @see Jython#runCommand(String,String)
	 */
	public void runScript(File script, String sourceName, Scan scan) throws Exception {
		// slurp!
		String commands = slurp(script);
		// only run if no other scan is running
		// FIXME this has an obvious race condition, but worse it just ignores the request if busy
		if (commandServer.getScriptStatus(name) == Jython.IDLE || scan.isChild()) {
			commandServer.runScript(commands, sourceName, name);
		} else {
			throw new Exception("Unable to run script " + script.getAbsolutePath() + " as server os busy");
		}
	}

	/**
	 * @param script
	 *            {@link InputStream}
	 * @param sourceName
	 *            String
	 * @see Jython#runCommand(String,String)
	 */
	public void runScript(InputStream script, String sourceName) {
		// slurp!
		String commands = slurp(script);
		// only run if no other scan is runningcommandserver
		// FIXME this has an obvious race condition, but worse it just ignores the request if busy
		if (commandServer.getScriptStatus(name) == Jython.IDLE) {
			commandServer.runScript(commands, sourceName, name);
		}
	}

	/**
	 * @param script
	 *            {@link InputStream}
	 * @param sourceName
	 *            String
	 * @param scan
	 * @see Jython#runCommand(String,String)
	 */
	public void runScript(InputStream script, String sourceName, Scan scan) {
		// slurp!
		String commands = slurp(script);
		// only run if no other scan is running
		// FIXME this has an obvious race condition, but worse it just ignores the request if busy
		if (commandServer.getScriptStatus(name) == Jython.IDLE || scan.isChild()) {
			commandServer.runScript(commands, sourceName, name);
		}
	}

	/**
	 * @param command
	 *            String
	 * @see Jython#runCommand(String,String)
	 */
	@Override
	public void runCommand(String command) {
		commandServer.runCommand(command, name);
	}

	/**
	 * @param command
	 *            String
	 * @param scanObserver
	 *            String
	 * @see Jython#runCommand(String, String,String)
	 */
	@Override
	public void runCommand(String command, String scanObserver) {
		commandServer.runCommand(command, scanObserver, name);
	}

	/**
	 * @param command
	 *            String commandserver
	 * @return String
	 * @see Jython#evaluateCommand(String,String)
	 */
	@Override
	public String evaluateCommand(String command) {
		return commandServer.evaluateCommand(command, name);
	}

	/**
	 * @see Jython#haltCurrentScan
	 */
	@Override
	public void haltCurrentScan() {
		commandServer.haltCurrentScan(name);
	}

	/**
	 * @see Jython#pauseCurrentScan
	 */
	@Override
	public void pauseCurrentScan() {
		commandServer.pauseCurrentScan(name);
	}

	/**
	 * @see Jython#resumeCurrentScan
	 */
	@Override
	public void resumeCurrentScan() {
		commandServer.resumeCurrentScan(name);
	}

	/**
	 * @see Jython#restartCurrentScan(String)
	 */
	@Override
	public void restartCurrentScan() {
		commandServer.restartCurrentScan(name);
	}

	/**
	 * @see Jython#panicStop(String)
	 */
	@Override
	public void panicStop() {
		commandServer.panicStop(name);
	}

	/**
	 * @see Jython#haltCurrentScript
	 */
	@Override
	public void haltCurrentScript() {
		commandServer.haltCurrentScript(name);
	}

	/**
	 * @param theObserved
	 *            Object
	 * @param changeCode
	 *            Object
	 */
	private void notifyIObservers(Object theObserved, Object changeCode) {
		myIObservers.notifyIObservers(theObserved, changeCode);
	}

	/**
	 * @see Jython#pauseCurrentScript
	 */
	@Override
	public void pauseCurrentScript() {
		commandServer.pauseCurrentScript(name);
	}

	/**
	 * @see Jython#resumeCurrentScript
	 */
	@Override
	public void resumeCurrentScript() {
		commandServer.resumeCurrentScript(name);
	}

	@Override
	public void update(Object dataSource, Object data) {
		try {
			// byte arrays are only for the terminal
			if (data instanceof byte[]) {
				for (Terminal terminal : myTerminals) {
					terminal.write((byte[]) data);
				}
			}

			// pass data from a scan to all relevant guiPanels
			else if (data instanceof IScanDataPoint) {

				boolean panelUpdated = false;
				IScanDataPoint point = (IScanDataPoint) data;
				lastScanDataPoint = point;
				for (IScanDataPointObserver observer : allSDPObservers) {
					try {
						observer.update(this, point);
						panelUpdated = true;
					} catch (Throwable e) {
						// don't allow an exception to prevent the loop from continuing
						logger.warn("Exception when broadcasting a ScanDataPoint " + e.getMessage(), e);
					}
				}

				// if source of scan command named, then send the SDP to the named panel
				String panelName = point.getCreatorPanelName();
				if (panelName != null) {
					for (INamedScanDataPointObserver observer : namedSDPObservers) {
						String name = observer.getName();
						if (name.contains(panelName)) {
							try {
								if(!allSDPObservers.contains(observer)){
									//not done in loop above - we do not want to update the observer twice
									observer.update(this, data);
								}
								panelUpdated = true;
							} catch (Throwable e) {
								// don't allow an exception to prevent the loop from continuing
								logger.warn("Exception when broadcasting a ScanDataPoint " + e.getMessage(), e);
							}
						}
					}
				}

				// if we can't find the panel, but a panel name was given then print an error message. Unless we are on
				// the same ObjectServer as the JythonServer and so would not expect any GUI panels anyway
				if (panelName != null && !panelUpdated && namedSDPObservers.size() > 0) {
					logger.warn("Could not send ScanDataPoint to the named panel " + panelName);
				}
			}
			else if (data instanceof BatonChanged) {
				// the baton has changed hands or there is a new client for the moment, simply distribute the message object
				amIBatonHolder();
				notifyIObservers(this, data);
			} else if (data instanceof BatonLeaseRenewRequest){
				amIBatonHolder();
				notifyIObservers(this, data);
			} 
			// fan out all other messages
			else {
				notifyIObservers(this, data);
			}
		} catch (Exception ex1) {
			logger.error("exception while updating local observers: " + ex1.getMessage(), ex1);
		}
	}

	/**
	 * @param command
	 *            String
	 * @param source
	 *            String
	 * @return boolean
	 * @see Jython#runsource
	 */
	@Override
	public boolean runsource(String command, String source) {
		return commandServer.runsource(command, source, name);
	}

	/**
	 * @see Jython#getScriptStatus
	 * @return int
	 */
	@Override
	public int getScriptStatus() {
		return commandServer.getScriptStatus(name);
	}

	/**
	 * @see Jython#setRawInput
	 * @param theRawInput
	 */
	public void setRawInput(String theRawInput) {
		commandServer.setRawInput(theRawInput, name);
	}

	/**
	 * @see Jython#getScanStatus
	 * @return int
	 */
	@Override
	public int getScanStatus() {
		return commandServer.getScanStatus(name);
	}

	/**
	 * @see Jython#setScanStatus
	 * @param status
	 *            int
	 */
	@Override
	public void setScanStatus(int status) {
		commandServer.setScanStatus(status, name);
	}

	/**
	 * @see Jython#setScriptStatus
	 * @param status
	 *            int
	 */
	@Override
	public void setScriptStatus(int status) {
		commandServer.setScriptStatus(status, name);
	}

	// to fulfil the IObservable interface
	// Local objects should register themselves as observers of this class
	// so that they may receive status messages about the status of scripting and scanning
	@Override
	public void addIObserver(IObserver anIObserver) {
		myIObservers.addIObserver(anIObserver);

		// put all Terminals in a separate list as well as they will want extra output
		if (anIObserver instanceof Terminal) {
			if (!myTerminals.contains(anIObserver)) {
				myTerminals.addElement((Terminal) anIObserver);
			}
		}

		// objects wishing to see SDPs
		if (anIObserver instanceof INamedScanDataPointObserver) {
			if (!namedSDPObservers.contains(anIObserver)) {
				namedSDPObservers.addElement((INamedScanDataPointObserver) anIObserver);
			}
		} else if (anIObserver instanceof IScanDataPointObserver) {
			if (!allSDPObservers.contains(anIObserver)) {
				allSDPObservers.addElement((IScanDataPointObserver) anIObserver);
			}
		}
	}

	@Override
	public void deleteIObserver(IObserver anIObserver) {
		myIObservers.deleteIObserver(anIObserver);
		
		if (anIObserver instanceof IScanDataPointObserver) {
			allSDPObservers.removeElement(anIObserver);
		} else if (anIObserver instanceof INamedScanDataPointObserver) {
			namedSDPObservers.removeElement(anIObserver);
		}

	}

	@Override
	public void deleteIObservers() {
		myIObservers.deleteIObservers();
		myTerminals.removeAllElements();
		namedSDPObservers.removeAllElements();
		allSDPObservers.removeAllElements();
	}

	/**
	 * @param objectName
	 * @param obj
	 * @see Jython#placeInJythonNamespace(String, Object,String)
	 */
	@Override
	public void placeInJythonNamespace(String objectName, Object obj) {
		commandServer.placeInJythonNamespace(objectName, obj, name);
	}

	/**
	 * @param objectName
	 * @return Object
	 * @see Jython#getFromJythonNamespace(String,String)
	 */
	@Override
	public Object getFromJythonNamespace(String objectName) {
		try {
			// bug in Jython: should not have to catch the error, if an object
			// is not defined then null should be returned
			return commandServer.getFromJythonNamespace(objectName, name);
		} catch (NullPointerException e) {
			return null;
		}
	}

	/**
	 * Makes it easy for strings to be printed on the console and not anywhere else. Also this method fixes awkward
	 * strings which have \n or double- quotes in them which could cause a syntax error at run time.
	 * 
	 * @param text
	 *            the string to be printed
	 */
	@Override
	public void print(String text) {
		if (text == null) {
			return;
		}
		text = text.replaceAll("\n", "\\\\n");
		text = text.replaceAll("\"", "'");
		text = "print\"" + text + "\"";
		commandServer.runCommand(text, name);
	}
	/**
	 * Makes it easy for strings to be printed on the console and not anywhere else. Also this method fixes awkward
	 * strings which have \n or double- quotes in them which could cause a syntax error at run time.
	 * 
	 * @param text
	 *            the string to be printed
	 */
	public void print_(String text) {
		if (text == null) {
			return;
		}
		text = text.replaceAll("\n", "\\\\n");
		text = text.replaceAll("\"", "'");
		text = "print\"" + text + "\"";
		commandServer.runCommand(text, name);
	}

	/**
	 * @see Jython#getStartupOutput(String)
	 * @return string of the message from the last (re)start of the Command Server
	 */
	public String getStartupOutput() {
		return commandServer.getStartupOutput(name);
	}

	/**
	 * Places the contents of a file into a string. Careful what you give this method! Named after the Perl command (if
	 * you're interested).
	 * 
	 * @param pyFile
	 * @return contents of the file as a string
	 */
	public static String slurp(PyFile pyFile) {
		String contents = "";
		try {
			String str;
			while ((str = pyFile.readline().asString()).compareTo("") != 0) {
				contents += str;
			}
			return contents;
		} finally{
			pyFile.close();
		}
	}

	/**
	 * Places the contents of a file into a string. Careful what you give this method! Named after the Perl command (if
	 * you're interested).
	 * 
	 * @param file
	 *            File
	 * @return String
	 * @throws IOException 
	 */
	public static String slurp(File file) throws IOException {
		String contents = "";
		BufferedReader in = new BufferedReader(new FileReader(file));
		try {
			String str;
			while ((str = in.readLine()) != null) {
				contents += str;
				contents += System.getProperty("line.separator");
			}
			return contents;
		}finally{
			in.close();
		}
	}

	/**
	 * Places the contents of a file into a string. Careful what you give this method! Named after the Perl command (if
	 * you're interested).
	 * 
	 * @param file
	 *            File
	 * @return String
	 */
	public static String slurp(InputStream file) {
		String contents = "";
		try {

			BufferedReader in = new BufferedReader(new InputStreamReader(file));
			String str;
			while ((str = in.readLine()) != null) {
				contents += str;
				contents += System.getProperty("line.separator");
			}
			in.close();
		} catch (IOException e) {
			logger.warn("Error while JythonServerFacade reading InputStream " + file.toString());
			return "";
		}
		return contents;
	}

	private String generateRandomName() {
		StringBuffer randomString = new StringBuffer();
		Random random = new Random();

		// Generate a random string
		for (int i = 0; i < 15; i++) {
			// 26 uppercase letters + 9 digits + 26 lowercase letters
			int digit = random.nextInt(51);

			char alphaNum;
			if (digit < 26) {
				alphaNum = (char) (digit + 'A');
			} else if (digit < 35) {
				alphaNum = (char) (digit - 26 + '0');
			} else {
				alphaNum = (char) (digit - 35 + 'a');
			}

			randomString.append(alphaNum);
		}
		return randomString.toString();
	}

	/**
	 * Add a new aliased command.
	 * 
	 * @param commandName
	 */
	public void addAliasedCommand(String commandName) {
		commandServer.addAliasedCommand(commandName, name);
	}

	/**
	 * Add a new vararg aliased command
	 * 
	 * @param commandName
	 */
	public void addAliasedVarargCommand(String commandName) {
		commandServer.addAliasedVarargCommand(commandName, name);
	}

	@Override
	public boolean requestBaton() {
		return commandServer.requestBaton(name);
	}

	@Override
	public void returnBaton() {
		commandServer.returnBaton(name);
	}

	/**
	 * @param index
	 * @see gda.jython.Jython#assignBaton(String,int)
	 */
	@Override
	public void assignBaton(int index) {
		commandServer.assignBaton(name, index);
	}

	/**
	 * @param username
	 * @param password
	 * @return true if switch successful
	 * @see gda.jython.Jython#switchUser(String,String,String)
	 */
	@Override
	public boolean switchUser(String username, String password) {
		try {
			Authenticator authenticator = AuthenticatorProvider.getAuthenticator();
			if (authenticator.isAuthenticated(username, password)) {
				runningAsAlternateUser = true;
				alternateUsername = username;
				commandServer.switchUser(name, username, visitID);
				return true;
			}
		} catch (DeviceException e) {
			logger.error("Exception while trying to switch user: " + e.getMessage());
		} catch (Exception e) {
			logger.error("Exception while trying to authenticate user " + username + ": " + e.getMessage());
		}
		return false;
	}

	/**
	 * Reverts to the original user this client was initially logged in as.
	 * 
	 * @see gda.jython.Jython#switchUser(String,String,String)
	 */
	@Override
	public void revertToOriginalUser() {
		try {
			runningAsAlternateUser = false;
			alternateUsername = null;
			commandServer.switchUser(name, originalUsername, "");
		} catch (DeviceException e) {
			logger.error("Exception while trying to revert to original user: " + e.getMessage());
		}
	}

	/**
	 * Switches the visit this client will collect data as when it holds the baton.
	 * 
	 * @param visitID
	 */
	@Override
	public void changeVisitID(String visitID) {
		try {
			this.visitID = visitID;
			commandServer.switchUser(name, "", visitID);
		} catch (DeviceException e) {
			logger.error("Exception while trying to revert to original user: " + e.getMessage());
		}
	}

	@Override
	public ClientDetails[] getOtherClientInformation() {
		return commandServer.getOtherClientInformation(name);
	}

	@Override
	public ClientDetails getBatonHolder() {

		if (amIBatonHolder()) {
			String localHost = "unknown";
			try {
				InetAddress hostAddress = java.net.InetAddress.getLocalHost();
				localHost = hostAddress.getHostName();
			} catch (UnknownHostException e) {
				logger.error("Problems getting host name", e);
			}
			final String username = UserAuthentication.getUsername();
			final String fullName = LibGdaCommon.getFullNameOfUser(username);
			return new ClientDetails(indexNumberInJythonServer, username, fullName, localHost,
					getAuthorisationLevel(), true, visitID);
		}

		final ClientDetails[] others = getOtherClientInformation();
		ClientDetails batonedUser = null;
		for (int i = 0; i < others.length; i++) {
			if (others[i].isHasBaton()) {
				batonedUser = others[i];
			}
		}
		return batonedUser;
	}

	@Override
	public boolean amIBatonHolder() {
		return commandServer.amIBatonHolder(name);
	}

	@Override
	public boolean isBatonHeld() {
		return commandServer.isBatonHeld();
	}

	/**
	 * @param message
	 * @see gda.jython.Jython#sendMessage(String,String)
	 */
	@Override
	public void sendMessage(String message) {
		commandServer.sendMessage(name, message);
	}
	
	@Override
	public List<UserMessage> getMessageHistory() {
		return commandServer.getMessageHistory(name);
	}

	@Override
	public void addIScanDataPointObserver(IScanDataPointObserver anIObserver) {
		addIObserver(anIObserver);
	}

	@Override
	public void deleteIScanDataPointObserver(IScanDataPointObserver anIObserver) {
		deleteIObserver(anIObserver);
	}

	@Override
	public IScanDataPoint getLastScanDataPoint() {
		return lastScanDataPoint;
	}

	@Override
	public void addBatonChangedObserver(IObserver anObserver) {
		addIObserver(anObserver);
	}

	@Override
	public void deleteBatonChangedObserver(IObserver anObserver) {
		deleteIObserver(anObserver);
	}

	@Override
	public String toString() {
		return "JythonServerFacade(name=" + name + ")";
	}

	@Override
	public synchronized void afterPropertiesSet() throws Exception {
		if (theInstance == null) {
			logger.info("Setting JythonServerFacade singleton to Spring-instantiated instance " + this);
			theInstance = this;
		}
	}

	@Override
	public ClientDetails getMyDetails() {
		int authorisationLevel = this.originalAuthorisationLevel;
		String username = this.originalUsername;
		if (runningAsAlternateUser) {
			username = this.alternateUsername;
			try {
				authorisationLevel = AuthoriserProvider.getAuthoriser().getAuthorisationLevel(alternateUsername);
			} catch (ClassNotFoundException e) {
				authorisationLevel = 0;
			}
		}
		
		final String fullName = LibGdaCommon.getFullNameOfUser(username);
		
		String localHost = "unknown";
		InetAddress hostAddress;
		try {
			hostAddress = java.net.InetAddress.getLocalHost();
			localHost = hostAddress.getHostName();
		} catch (UnknownHostException e) {
			logger.error("Problems while getting host name", e);
		}

		return new ClientDetails(indexNumberInJythonServer, username, fullName, localHost, authorisationLevel, amIBatonHolder(), visitID);
	}
	
	@Override
	public Vector<String> getAliasedCommands(){
		return commandServer.getAliasedCommands(name);
	}
	
	@Override
	public Vector<String> getAliasedVarargCommands(){
		return commandServer.getAliasedVarargCommands(name);
	}

	@Override
	public Map<String, Object> getAllFromJythonNamespace() throws DeviceException {
		return commandServer.getAllFromJythonNamespace();
	}

	@Override
	public String locateScript(String scriptToRun) {
		return commandServer.locateScript(scriptToRun);
	}

	@Override
	public String getDefaultScriptProjectFolder() {
		return commandServer.getDefaultScriptProjectFolder();
	}

	@Override
	public List<String> getAllScriptProjectFolders() {
		return commandServer.getAllScriptProjectFolders();
	}

	@Override
	public String getProjectNameForPath(String path) {
		return commandServer.getProjectNameForPath(path);
	}

	@Override
	public boolean projectIsUserType(String path) {
		return commandServer.projectIsUserType(path);
	}

	@Override
	public boolean projectIsConfigType(String path) {
		return commandServer.projectIsConfigType(path);
	}

	@Override
	public boolean projectIsCoreType(String path) {
		return commandServer.projectIsCoreType(path);
	}

	@Override
	public void addOutputTerminal(Terminal term) {
		addIObserver(term);
	}

	@Override
	public void deleteOutputTerminal(Terminal term) {
		deleteIObserver(term);
	}
	
}