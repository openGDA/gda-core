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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.nio.file.Files;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Collectors;

import org.python.core.PyException;
import org.python.core.PyObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.StringUtils;

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
import gda.jython.batoncontrol.UnknownClientException;
import gda.jython.commandinfo.CommandThreadEvent;
import gda.jython.commandinfo.CommandThreadEventType;
import gda.jython.commandinfo.ICommandThreadInfo;
import gda.jython.commandinfo.ICommandThreadInfoProvider;
import gda.jython.commandinfo.ICommandThreadObserver;
import gda.jython.completion.AutoCompletion;
import gda.jython.completion.TextCompleter;
import gda.observable.IObserver;
import gda.observable.ObservableComponent;
import gda.scan.IScanDataPoint;
import gda.scan.ScanEvent;
import gda.util.LibGdaCommon;

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
		IScriptController, ICommandAborter, IBatonStateProvider, InitializingBean, AliasedCommandProvider,
		IJythonContext, ITerminalOutputProvider, ICommandThreadInfoProvider, TextCompleter {

	private static final Logger logger = LoggerFactory.getLogger(JythonServerFacade.class);

	private static JythonServerFacade theInstance = null;

	private final Set<IScanDataPointObserver> scanDataPointObservers = new CopyOnWriteArraySet<>();
	private final Set<ICommandThreadObserver> commandThreadObservers = new CopyOnWriteArraySet<>();
	private final Set<Terminal> myTerminals = new CopyOnWriteArraySet<>();

	private final ObservableComponent scanEventObservers = new ObservableComponent();
	private final ObservableComponent myIObservers = new ObservableComponent();

	private String name = "";

	private final Jython commandServer;

	private static JythonServerFacade unattendedDataCollectionClient;

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
		this(Finder.find(Jython.SERVER_NAME));
	}

	/**
	 * Creates a Jython server facade, using the specified Jython command server.
	 *
	 * @param commandServer
	 *            the Jython command server
	 * @throws InstantiationException
	 */
	protected JythonServerFacade(Jython commandServer) throws InstantiationException {
		this(commandServer, getCurrentUsername(), getCurrentFullName());
	}

	/**
	 * Creates a Jython server facade, using the specified Jython command server, username and fullName.
	 *
	 * @param commandServer
	 *            the Jython command server
	 * @param username
	 *            the username of the person using this JSF
	 * @param fullName
	 *            the full name of the person using this JSF
	 * @throws InstantiationException
	 */
	protected JythonServerFacade(Jython commandServer, String username, String fullName) throws InstantiationException {
		try {
			InetAddress hostAddress = java.net.InetAddress.getLocalHost();
			String localHost = hostAddress.getHostName();

			// because this is a call to the Finder, this facade cannot be called during the instantiation phase. It can
			// be called during the configure phase.
			// JSF identifier constructed from host name, to allow user friendly error message when ClientInfo unavailable
			// Username can change during lifetime of facade, but identifier cannot, rather avoid confusion.
			name = String.format("%s-%s", localHost, UUID.randomUUID());
			this.commandServer = commandServer;

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
				// username is an empty string on the GDA server
				if (StringUtils.hasText(originalUsername)) {
					fullName = LibGdaCommon.getFullNameOfUser(originalUsername);
				}
				commandServer.addIObserver(this);
				indexNumberInJythonServer = commandServer.addFacade(name, localHost, username, fullName, "");
				originalAuthorisationLevel = commandServer.getAuthorisationLevel(indexNumberInJythonServer);
			} catch (DeviceException e) {
				final String msg = "Login failed for user: " + UserAuthentication.getUsername();
				logger.error(msg);
				throw new InstantiationException(msg); // Important - do no exit incorrect for rcp gda client
			}

		} catch (Exception ex) {
			logger.error("Error during instantiation", ex);
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
	public static synchronized JythonServerFacade getCurrentInstance() throws Exception {

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
			try {
				theInstance.commandServer.removeFacade(theInstance.name);
			} catch (UnknownClientException e) {
				logger.debug("Client {} was unknown to server at time of shut down", theInstance.name, e);
			}
			theInstance = null;
		}
	}

	/**
	 * @return A JSF designated for unattended data collections
	 * @throws InstantiationException
	 */
	private static JythonServerFacade createUnattendedClientFacade() throws InstantiationException {
		unattendedDataCollectionClient =  new JythonServerFacade(
				Finder.findSingleton(Jython.class),
				"UDC",
				"Unattended Data Collection");
		return unattendedDataCollectionClient;
	}

	/**
	 * @return The JSf singleton for unattended data collections
	 * @throws InstantiationException
	 */
	public static JythonServerFacade getUnattendedClientFacade() throws InstantiationException {

		if (unattendedDataCollectionClient == null) {
			unattendedDataCollectionClient = createUnattendedClientFacade();
		}
		return unattendedDataCollectionClient;
	}

	/**
	 * @return The username associated with the current JSF
	 */
	private static String getCurrentUsername() {
		return UserAuthentication.getUsername();
	}

	/**
	 * @return The full name associated with the current JSF
	 */
	private static String getCurrentFullName() {
		String currentUsername = getCurrentUsername();
		return LibGdaCommon.getFullNameOfUser(currentUsername);
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
		if (LocalProperties.isBatonManagementEnabled() && originalAuthorisationLevel == Integer.MAX_VALUE) {
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

	public CommandThreadEvent runScript(String scriptName) {
		// open up a new file
		File file = new File(locateScript(scriptName));
		return runScript(file);
	}

	@Override
	public CommandThreadEvent runScript(File script) {
		try {
			String commands;
			commands = Files.readAllLines(script.toPath()).stream().collect(Collectors.joining("\n"));
			logger.debug("Running script {}", script.getAbsolutePath());
			String scriptName = script.getName();
			final CommandThreadEvent event = commandServer.runScript(commands, scriptName, name);
			if (event.getEventType() == CommandThreadEventType.BUSY) {
				logger.error("Unable to run script " + script.getAbsolutePath() + " as server is busy");
			}
			return event;
		} catch (IOException e) {
			logger.error("Unable to run script " + script.getAbsolutePath(), e);
			return new CommandThreadEvent(CommandThreadEventType.SUBMIT_ERROR, null);
		}
	}

	@Override
	public void runCommand(String command) {
		commandServer.runCommand(command, name);
	}

	@Override
	public String evaluateCommand(String command) {
		return commandServer.evaluateCommand(command, name);
	}

	@Override
	public void requestFinishEarly() {
		commandServer.requestFinishEarly(name);
	}

	@Override
	public boolean isFinishEarlyRequested() {
		return commandServer.isFinishEarlyRequested();
	}

	@Override
	public void pauseCurrentScan() {
		commandServer.pauseCurrentScan(name);
	}

	@Override
	public void resumeCurrentScan() {
		commandServer.resumeCurrentScan(name);
	}

	@Override
	public void restartCurrentScan() {
		commandServer.restartCurrentScan(name);
	}

	@Override
	public void beamlineHalt() {
		commandServer.beamlineHalt(name);
	}

	@Override
	public void abortCommands() {
		commandServer.abortCommands(name);
	}

	private void notifyIObservers(Object theObserved, Object changeCode) {
		myIObservers.notifyIObservers(theObserved, changeCode);
	}

	@Override
	public void pauseCurrentScript() {
		commandServer.pauseCurrentScript(name);
	}

	@Override
	public void resumeCurrentScript() {
		commandServer.resumeCurrentScript(name);
	}

	@Override
	public void update(Object dataSource, Object data) {
		try {
			if (data instanceof TerminalOutput) {
				final TerminalOutput output = (TerminalOutput) data;
				for (Terminal terminal : myTerminals) {
					try {
						terminal.write(output.getOutput());
					} catch (Exception e) {
						logger.error("Failed to write '{}' to terminal {}", output.getOutput(), terminal, e);
					}
				}
			} else if (data instanceof TerminalInput) {
				TerminalInput ti = (TerminalInput) data;
				try {
					ClientDetails details = getMyDetails();
					if (details.getIndex() != ti.getIndex() && !ti.getInput().isEmpty()) {
						for (Terminal terminal : myTerminals) {
							try {
								terminal.writeInput((TerminalInput) data);
							} catch (Exception e) {
								logger.error("Failed to write '{}' to terminal {}", data, terminal, e);
							}
						}
					}
				} catch (UnknownClientException uce) {
					logger.error("Failed to write '{}' to any terminals of this client, client is unknown to the server", data, uce);
				}
			}

			// pass data from a scan to all relevant guiPanels
			else if (data instanceof IScanDataPoint) {

				IScanDataPoint point = (IScanDataPoint) data;
				lastScanDataPoint = point;
				for (IScanDataPointObserver observer : scanDataPointObservers) {
					try {
						observer.update(this, point);
					} catch (Throwable e) {
						// don't allow an exception to prevent the loop from continuing
						logger.warn("Exception when broadcasting a ScanDataPoint", e);
					}
				}
			} else if (data instanceof BatonChanged || data instanceof BatonLeaseRenewRequest) {
				// the baton has changed hands or there is a new client for the moment, simply distribute the message
				// object
				amIBatonHolder();
				notifyIObservers(this, data);
			} else if (data instanceof CommandThreadEvent) {
				for (ICommandThreadObserver observer: commandThreadObservers) {
					observer.update(this, data);
				}
			} else if (data instanceof ScanEvent) {
				scanEventObservers.notifyIObservers(this, data);
			}
			// fan out all other messages
			else {
				notifyIObservers(this, data);
			}
		} catch (Exception ex1) {
			logger.error("exception while updating local observers", ex1);
		}
	}

	/**
	 * @param command to run
	 * @return true if more is needed, false if not
	 *
	 * @see Jython#runsource(String, String)
	 */
	@Override
	public boolean runsource(String command) {
		return commandServer.runsource(command, name);
	}

	/**
	 * @param command to run
	 * @param stdin input stream to use as stdin for this command
	 * @return true if more is needed, false if not
	 *
	 * @see Jython#runsource(String, String, InputStream)
	 */
	public boolean runsource(String command, InputStream stdin) {
		return commandServer.runsource(command, name, stdin);
	}

	@Override
	public JythonStatus getScriptStatus() {
		return commandServer.getScriptStatus(name);
	}

	public void setRawInput(String theRawInput) {
		commandServer.setRawInput(theRawInput, name);
	}

	@Override
	public JythonStatus getScanStatus() {
		return commandServer.getScanStatus(name);
	}

	public JythonServerStatus getServerStatus() {
		return commandServer.getJythonServerStatus();
	}

	@Override
	public Optional<String> getScriptName() {
		return getServerStatus().getScriptName();
	}

	@Override
	public boolean isScanRunning() {
		return getScanStatus() != JythonStatus.IDLE;
	}

	@Override
	public void setScriptStatus(JythonStatus status) {
		commandServer.setScriptStatus(status, name);
	}

	// to fulfil the IObservable interface
	// Local objects should register themselves as observers of this class
	// so that they may receive status messages about the status of scripting and scanning
	@Override
	public void addIObserver(IObserver anIObserver) {
		myIObservers.addIObserver(anIObserver);

		// objects wishing to see SDPs
		if (anIObserver instanceof IScanDataPointObserver) {
			scanDataPointObservers.add((IScanDataPointObserver) anIObserver);
			logger.debug("Added {} as SDP observer. Now have {} observers", anIObserver, scanDataPointObservers.size());
		}

	}

	@Override
	public void deleteIObserver(IObserver anIObserver) {
		myIObservers.deleteIObserver(anIObserver);

		if (anIObserver instanceof IScanDataPointObserver) {
			scanDataPointObservers.remove(anIObserver);
			logger.debug("Removed {} as SDP observer. Now have {} observers", anIObserver, scanDataPointObservers.size());
		}
	}

	@Override
	public void deleteIObservers() {
		myIObservers.deleteIObservers();
		final int numTerminals = myTerminals.size();
		myTerminals.clear();
		final int numSDPObservers = scanDataPointObservers.size();
		scanDataPointObservers.clear();
		logger.debug("Deleting all IObservers, Removed {} terminals and {} SDP observers",
				numTerminals, numSDPObservers);
	}

	@Override
	public void placeInJythonNamespace(String objectName, Object obj) {
		commandServer.placeInJythonNamespace(objectName, obj, name);
	}

	@Override
	public Object getFromJythonNamespace(String objectName) {
		return commandServer.getFromJythonNamespace(objectName, name);
	}

	/**
	 * Print to Jython console on all clients (and notify anything added via {@link #addOutputTerminal(Terminal)}).
	 * Makes it easy for strings to be printed on the console and not anywhere else.
	 *
	 * @param text the string to be printed
	 */
	@Override
	public void print(String text) {
		if (text == null) {
			return;
		}
		commandServer.print(text + "\n");
	}

	/**
	 * @see Jython#getStartupOutput(String)
	 * @return string of the message from the last (re)start of the Command Server
	 */
	public String getStartupOutput() {
		return commandServer.getStartupOutput(name);
	}

	public void addAliasedCommand(String commandName) {
		try {
			commandServer.addAliasedCommand(commandName, name);
		} catch (UnknownClientException e) {
			logUnknownClient(e);
		}
	}

	public void addAliasedVarargCommand(String commandName) {
		try {
			commandServer.addAliasedVarargCommand(commandName, name);
		} catch (UnknownClientException e) {
			logUnknownClient(e);
		}
	}

	public void removeAliasedCommand(String commandName) {
		try {
			commandServer.removeAlias(commandName, name);
		} catch (UnknownClientException e) {
			logUnknownClient(e);
		}
	}

	@Override
	public boolean requestBaton() {
		try {
			return commandServer.requestBaton(name);
		} catch (UnknownClientException e) {
			logUnknownClient(e);
			return false;
		}
	}

	@Override
	public void returnBaton() {
		try {
			commandServer.returnBaton(name);
		} catch (UnknownClientException e) {
			logUnknownClient(e);
		}
	}

	@Override
	public void assignBaton(int receiverIndex, int currentBatonHolderIndex) {
		try {
			commandServer.assignBaton(name, receiverIndex, currentBatonHolderIndex);
		} catch (UnknownClientException e) {
			logUnknownClient(e);
		}
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
			logger.error("Exception while trying to switch user to {}", username, e);
		} catch (Exception e) {
			logger.error("Exception while trying to authenticate user {}", username, e);
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
			logger.error("Exception while trying to revert to original user", e);
		} catch (UnknownClientException e) {
			logUnknownClient(e);
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
			logger.error("Exception while trying to revert to original user", e);
		} catch (UnknownClientException e) {
			logUnknownClient(e);
		}
	}

	@Override
	public ClientDetails[] getOtherClientInformation() {
		try {
			return commandServer.getOtherClientInformation(name);
		} catch (UnknownClientException e) {
			logUnknownClient(e);
			return new ClientDetails[] {};
		}
	}

	@Override
	public ClientDetails getBatonHolder() {

		if (amIBatonHolder()) {
			return getMyDetails();
		}

		final ClientDetails[] others = getOtherClientInformation();
		ClientDetails batonedUser = null;
		for (int i = 0; i < others.length; i++) {
			if (others[i].hasBaton()) {
				batonedUser = others[i];
			}
		}
		return batonedUser;
	}

	@Override
	public boolean amIBatonHolder() {
		try {
			return commandServer.amIBatonHolder(name);
		} catch (UnknownClientException e) {
			logUnknownClient(e);
			return false;
		}
	}

	@Override
	public boolean isBatonHeld() {
		return commandServer.isBatonHeld();
	}

	@Override
	public void sendMessage(String message) {
		try {
			commandServer.sendMessage(name, message);
		} catch (UnknownClientException e) {
			logUnknownClient(e);
		}
	}

	@Override
	public List<UserMessage> getMessageHistory() {
		try {
			return commandServer.getMessageHistory(name);
		} catch (UnknownClientException e) {
			logUnknownClient(e);
			return List.of();
		}
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
	public void addScanEventObserver(IObserver anObserver) {
		scanEventObservers.addIObserver(anObserver);
	}

	@Override
	public void deleteScanEventObserver(IObserver anObserver) {
		scanEventObservers.deleteIObserver(anObserver);
	}

	@Override
	public void addCommandThreadObserver(ICommandThreadObserver anObserver) {
		commandThreadObservers.add(anObserver);
	}

	@Override
	public void deleteCommandThreadObserver(ICommandThreadObserver anObserver) {
		commandThreadObservers.remove(anObserver);
	}

	@Override
	public ICommandThreadInfo[] getCommandThreadInfo() {
		return commandServer.getCommandThreadInfo();
	}

	@Override
	public ClientDetails getMyDetails() {
		ClientDetails myDetails;
		try {
			myDetails = commandServer.getClientInformation(name);
			if (runningAsAlternateUser) {
				myDetails.setUserID(this.alternateUsername);
				try {
					myDetails.setAuthorisationLevel(
							AuthoriserProvider.getAuthoriser().getAuthorisationLevel(alternateUsername));
				} catch (ClassNotFoundException e) {
					myDetails.setAuthorisationLevel(0);
				}
			}
			return myDetails;
		} catch (UnknownClientException e) {
			logUnknownClient(e);
			return null;
		}
	}

	@Override
	public Collection<String> getAliasedCommands() {
		return commandServer.getAliasedCommands(name);
	}

	@Override
	public Collection<String> getAliasedVarargCommands() {
		return commandServer.getAliasedVarargCommands(name);
	}

	@Override
	public boolean hasAlias(String command) {
		return commandServer.hasAlias(command, name);
	}

	@Override
	public Set<String> getAllNamesForObject(Object obj) throws DeviceException {
		return commandServer.getAllNamesForObject(obj);
	}

	@Override
	public <F extends Findable> Set<String> getAllNamesForType(Class<F> clazz) {
		return commandServer.getAllNamesForType(clazz);
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
	public boolean addOutputTerminal(Terminal term) {
		if (myTerminals.add(term)) {
			logger.debug("Added {} as terminal. Now have {} terminals", term, myTerminals.size());
			return true;
		}
		return false;
	}

	@Override
	public boolean deleteOutputTerminal(Terminal term) {
		if (myTerminals.remove(term)) {
			logger.debug("Removed a terminal, now have {} terminals", myTerminals.size());
			return true;
		}
		return false;
	}

	/**
	 * Evaluates a string as a Python expression and returns the result. Bypasses translator, batton control, and is not
	 * available across corba.
	 * <p>
	 * This is of particular utility compared to other offerings as calls are synchronous, throw exceptions and can
	 * return an actual object.
	 */
	public PyObject eval(String s) throws PyException {
		return commandServer.eval(s);
	}

	/**
	 * Executes a string of Python source in the local namespace. Bypasses translator, batton control, and is not
	 * available across corba.
	 * <p>
	 * This is of particular utility compared to other offerings as calls are synchronous and throw exceptions.
	 */
	public void exec(String s) throws PyException {
		commandServer.exec(s);
	}

	@Override
	public AutoCompletion getCompletionsFor(String line, int posn) {
		return commandServer.getCompletionsFor(line, posn);
	}

	private void logUnknownClient(UnknownClientException e) {
		logger.info("This client {}, logged in as {} is unknown to the server, try restarting it?", this.name, getCurrentFullName());
		logger.debug("Client unknown to server exception:", e);
	}

}
