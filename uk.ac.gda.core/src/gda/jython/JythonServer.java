/*-
 * Copyright © 2014 Diamond Light Source Ltd., Science and Technology
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

import static java.text.MessageFormat.format;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import org.apache.sshd.server.PasswordAuthenticator;
import org.python.core.Py;
import org.python.core.PyList;
import org.python.core.PyObject;
import org.python.core.PyStringMap;
import org.python.util.InteractiveConsole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import gda.commandqueue.IFindableQueueProcessor;
import gda.configuration.properties.LocalProperties;
import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.Motor;
import gda.device.Scannable;
import gda.device.Stoppable;
import gda.factory.Configurable;
import gda.factory.FactoryException;
import gda.factory.Findable;
import gda.factory.Finder;
import gda.factory.Localizable;
import gda.jython.authoriser.Authoriser;
import gda.jython.authoriser.AuthoriserProvider;
import gda.jython.batoncontrol.BatonManager;
import gda.jython.batoncontrol.ClientDetails;
import gda.jython.commandinfo.CommandThreadEvent;
import gda.jython.commandinfo.CommandThreadEventType;
import gda.jython.commandinfo.CommandThreadInfo;
import gda.jython.commandinfo.CommandThreadType;
import gda.jython.commandinfo.ICommandThreadInfo;
import gda.jython.commands.InputCommands;
import gda.jython.completion.AutoCompletion;
import gda.jython.completion.TextCompleter;
import gda.jython.completion.impl.JythonCompleter;
import gda.jython.corba.impl.JythonImpl;
import gda.jython.socket.SocketServer;
import gda.jython.socket.SocketServer.ServerType;
import gda.jython.translator.Translator;
import gda.messages.InMemoryMessageHandler;
import gda.messages.MessageHandler;
import gda.observable.IObservable;
import gda.observable.IObserver;
import gda.observable.ObservableComponent;
import gda.scan.Scan;
import gda.scan.Scan.ScanStatus;
import gda.scan.ScanDataPoint;
import gda.scan.ScanInformation;
import gda.scan.ScanInterruptedException;
import gda.util.exceptionUtils;

/**
 * This controls the information given to the Jython engine (GDAJythonInterpreter). This implements the Jython
 * interface, so is distributed, but classes outside this package should not communicate with this class directly.
 * Instead they should talk to the Jython Server (Command Server) via the JythonServerFacade class. The exception to
 * this rule is scan objects, which require information via methods which are not distributed and are shared via the
 * ICurrentScanHolder, IJythonServerNotifer, and IDefaultScannableProvider interfaces.
 */
public class JythonServer implements Jython, LocalJython, Configurable, Localizable, Serializable,
		ICurrentScanInformationHolder, IJythonServerNotifer, IDefaultScannableProvider, IObservable, ITerminalInputProvider,
		TextCompleter {

	private static Logger logger = LoggerFactory.getLogger(JythonServer.class);

	/**
	 * Name of this object. This should agree with the string used in Castor.
	 */
	public static final String SERVERNAME = "command_server";
	public static final String NULL = String.valueOf((char)0);
	private boolean atStartup = true;

	// the Jython interpreter
	private GDAJythonInterpreter interp = null;

	// References to the last object which created a scan (and so where scan
	// data points should be sent to)
	String scanObserverName = "";

	// There may be to facades - localFacade distributes output within the
	// server, remoteFacade distributes it to CORBA - see setFacade().
	IObserver localFacade = null;

	IObserver remoteFacade = null;

	// tcp/ip socket for communication
	SocketServer socket = null;

	// to ensure configuration only performed once
	boolean configured = false;

	// whether interpreter initialization has completed
	boolean initialized = false;

	// store any output from during setup to be displayed by terminals
	boolean runningLocalStation = false;

	String bufferedLocalStationOutput = "";

	// part of the Localizable interface
	boolean isLocal = false;

	// the current script being run
	volatile int currentScriptPosition = 0;

	// status of the current script
	volatile int scriptStatus = Jython.IDLE;

	// status of the current scan
	volatile int lastScanStatus = Jython.IDLE;

	// the current scan object
	volatile Scan currentScan = null;

	// threads to run interaction with the GDAJythonInterpreter object
	Vector<Thread> runsourceThreads = new Vector<Thread>();

	Vector<Thread> runCommandThreads = new Vector<Thread>();

	Vector<Thread> evalThreads = new Vector<Thread>();

	Vector<Scannable> defaultScannables = new Vector<Scannable>();

	// volatile parameters when dealing with user input from scripts
	volatile boolean expectingInputForRawInput = false;

	volatile String theRawInput;

	BatonManager batonManager = new BatonManager();

	private ScriptPaths jythonScriptPaths;

	private File gdaVarDirectory;

	private File cacheDirectory;

	private ServerType remoteServerType = ServerType.TELNET;

	private PasswordAuthenticator authenticator;

	private int remotePort = -1;

	private String gdaStationScript;

	private TextCompleter jythonCompleter;

	// configure whether #panicStop() tries to stop all Scannables found in the Jython namespace
	private boolean stopJythonScannablesOnStopAll = true;

	Vector<Terminal> myTerminals = new Vector<Terminal>();

	ObservableComponent jythonServerStatusObservers = new ObservableComponent();


	/**
	 * Provide access to the Jython interpreter, so we can test whether it's configured correctly.
	 *
	 * @return This object's Jython interpreter instance.
	 */
	GDAJythonInterpreter getJythonInterpreter() {
		return interp;
	}

	/**
	 * @return full path of the station startupscript
	 */
	public String getGdaStationScript() {
		return gdaStationScript;
	}

	/**
	 * Constructor.
	 */
	public JythonServer() {
	}

	/**
	 * Get the jython script folder object.
	 *
	 * @return This object's jython script path finder.
	 */
	public ScriptPaths getJythonScriptPaths() {
		return jythonScriptPaths;
	}

	/**
	 * Set this object's jython script folder list.
	 *
	 * @param jythonScriptPaths
	 *            A jython script finder instance.
	 */
	public void setJythonScriptPaths(ScriptPaths jythonScriptPaths) {
		this.jythonScriptPaths = jythonScriptPaths;
	}

	/**
	 * Sets the 'var' directory used by this Jython server.
	 *
	 * @param gdaVarDirectory
	 *            the 'var' directory
	 */
	public void setGdaVarDirectory(File gdaVarDirectory) {
		this.gdaVarDirectory = gdaVarDirectory;
	}

	/**
	 * Sets the cache directory used by this Jython server.
	 *
	 * @param cacheDirectory
	 *            the cache directory
	 */
	public void setCacheDirectory(File cacheDirectory) {
		this.cacheDirectory = cacheDirectory;
	}

	/**
	 * Sets the station startup script used by this Jython server.
	 *
	 * @param gdaStationScript
	 *            the station script
	 */
	public void setGdaStationScript(String gdaStationScript) {
		this.gdaStationScript = gdaStationScript;
	}

	/**
	 * Removes the Pseudo device from ths list of defaults
	 *
	 * @param scannable
	 */
	public void removeDefault(Scannable scannable) {
		if (defaultScannables.contains(scannable)) {
			defaultScannables.remove(scannable);
		}
	}

	/**
	 * Sets the remote server type.
	 *
	 * @param remoteServerType
	 *            the remote server type
	 */
	public void setRemoteServerType(SocketServer.ServerType remoteServerType) {
		this.remoteServerType = remoteServerType;
	}

	/**
	 * Sets the {@link PasswordAuthenticator} to use to authenticate users connecting via SSH to the Jython server. Not
	 * needed if a Telnet server is enabled.
	 */
	public void setAuthenticator(PasswordAuthenticator authenticator) {
		this.authenticator = authenticator;
	}

	/**
	 * Sets the port number that will accept connections to this server.
	 *
	 * @param remotePort
	 *            the port number
	 */
	public void setRemotePort(int remotePort) {
		this.remotePort = remotePort;
	}

	/**
	 * Add a Pseudo Device to the list of defaults
	 *
	 * @param scannable
	 */
	public void addDefault(Scannable scannable) {
		if (!defaultScannables.contains(scannable)) {
			defaultScannables.add(scannable);
		}
	}

	/**
	 * Returns a vector of all the names of the default scannables for display purposes.
	 *
	 * @return Vector<String>
	 */
	public Vector<String> getDefaultScannableNames() {
		Vector<Scannable> objs = getDefaultScannables();

		Vector<String> names = new Vector<String>();

		for (Scannable obj : objs) {
			names.add(obj.getName());
		}
		return names;
	}

	// to fulfil the Findable interface

	@Override
	public String getName() {
		return SERVERNAME;
	}

	@Override
	public void setName(String name) {
		// do nothing: this name needs to be fixed for all processes.
	}

	// to fulfil the localizable interface

	@Override
	public void setLocal(boolean local) {
		isLocal = local;
	}

	@Override
	public boolean isLocal() {
		return isLocal;
	}

	private MessageHandler messageHandler;

	public void setMessageHandler(MessageHandler messageHandler) {
		this.messageHandler = messageHandler;
	}

	// to fulfil the Configurable interface

	@Override
	public void configure() throws FactoryException {
		if (!configured) {
			// force garbage collect (useful if doing a restart)
			System.gc();

			if (messageHandler == null) {
				final InMemoryMessageHandler messageHandler = new InMemoryMessageHandler();
				messageHandler.setMaxMessagesPerVisit(10);
				setMessageHandler(messageHandler);
			}

			// reset the defaultScannables array
			defaultScannables = new Vector<Scannable>();

			try {
				// create the objects references in the interpreter namespace
				interp = createJythonInterpreter();
				interp.configure();
			} catch (Exception e) {
				throw new FactoryException(e.getMessage(), e);
			}

			interp.placeInJythonNamespace("command_server", this);
			runningLocalStation = true;
			try {
				interp.initialise(this);
				initialized = true;
			} catch (Exception e) {
				throw new FactoryException(e.getMessage(), e);
			} finally {
				runningLocalStation = false;
			}

			// open a socket for communication, if a port has been defined - not during reset_namespace
			int port = determineRemotePortNumber();
			if (port != -1 && atStartup) {
				socket = new SocketServer();
				socket.setServerType(remoteServerType);
				socket.setAuthenticator(authenticator);
				socket.setPort(port);
				new Thread(socket, "Jython SocketServer port " + port).start();
				atStartup = false;
			}

			jythonCompleter = new JythonCompleter(this);

			configured = true;
		}
	}

	private GDAJythonInterpreter createJythonInterpreter() {
		GDAJythonInterpreter interpreter = new GDAJythonInterpreter();
		interpreter.setJythonScriptPaths(jythonScriptPaths);
		interpreter.setGdaVarDirectory(gdaVarDirectory);
		interpreter.setCacheDirectory(cacheDirectory);
		return interpreter;
	}

	private int determineRemotePortNumber() {
		// Use remotePort property if it has been set
		if (remotePort != -1) {
			return remotePort;
		}

		// Use property if it has been set
		String port = LocalProperties.get("gda.jython.socket");
		if (port != null) {
			return Integer.parseInt(port);
		}

		// No port number set
		return -1;
	}

	/**
	 * Runs a command in the same thread and only returns when the command completed. This method is not distributed and
	 * is only for use by the "run" command which runs scripts from within other scripts or from the GDA terminal. It
	 * assumes that the contents of this file have not been translated yet.
	 *
	 * @param scriptFullPath
	 * @throws Exception
	 */
	public void runCommandSynchronously(String scriptFullPath) throws Exception {
		// this is a copy of the contents of the RunCommandRunner run method.
		// This method is not called directly as run() in the Runnable interface
		// does not throw any exceptions.

		// make a note of current script status - only clear flags at the end if this method call was the one which
		// changed the script state

		if (Thread.interrupted()) {
			throw new InterruptedException();
		}

		boolean wasAlreadyRunning = (getScriptStatus(null) == Jython.RUNNING);
		try {
			setScriptStatus(Jython.RUNNING, null);
			this.interp.exec(JythonServerFacade.slurp(new File(scriptFullPath)));
		} finally {
			if (!wasAlreadyRunning) {
				setScriptStatus(Jython.IDLE, null);
			}
		}
	}

	@Override
	public void runCommand(String command, String JSFIdentifier) {

		// check to see if this is a print command, if it is then it is not good
		// to create a separate thread for it, but just to print it straight to
		// the screen

		// If this becomes a big issue, a new thread should be created to run
		// all the print statements, but sequentially
		checkStateForRunCommand();

		if (command.startsWith("print")) {
			// do this immediately
			this.interp.exec(command);

		} else {

			// See bug #335 for why this must repeat most of the code of the
			// runCommand(String, String) method.
			try {
				int authorisationLevel = this.batonManager.getAuthorisationLevelOf(JSFIdentifier);
				RunCommandRunner runner = new RunCommandRunner(this, command, authorisationLevel);
				runCommandThreads.add(runner);
				// start the thread and return immediately.
				runner.start();
				clearThreads();
				notifyRefreshCommandThreads();
			} catch (Exception ex) {
				logger.info("Command Terminated." + ex.getMessage(), ex);
			}
		}
	}

	private boolean isConfigured() {
		return configured;
	}

	public boolean isInitialized() {
		return initialized;
	}

	void checkStateForRunCommand() throws IllegalStateException {
		// only allow if configured or initialised or runningLocalStation
		if (!(isConfigured() || isInitialized() || runningLocalStation)) {
			throw new IllegalStateException("JythonServer is not configured yet.");
		}
	}

	@Override
	public void runCommand(String command, String scanObserver, String JSFIdentifier) {
		try {
			checkStateForRunCommand();
			setScanObserver(scanObserver);
			int authorisationLevel = this.batonManager.getAuthorisationLevelOf(JSFIdentifier);
			RunCommandRunner runner = new RunCommandRunner(this, command, authorisationLevel);
			// Thread newthread = uk.ac.gda.util.ThreadManager.getThread(runner);
			runCommandThreads.add(runner);
			// start the thread and return immediately.
			runner.start();
			clearThreads();
			notifyRefreshCommandThreads();
		} catch (Exception ex) {
			logger.info("Command Terminated." + ex.getMessage(), ex);
		}
	}

	@Override
	public void runScript(String command, String JSFIdentifier) {
		// See bug #335 for why this must repeat most of the code of the
		// runCommand(String, String) method.
		try {
			int authorisationLevel = this.batonManager.getAuthorisationLevelOf(JSFIdentifier);
			RunScriptRunner runner = new RunScriptRunner(this, command, authorisationLevel);
			runCommandThreads.add(runner);
			// start the thread and return immediately.
			runner.start();
			clearThreads();
			notifyRefreshCommandThreads();
		} catch (Exception ex) {
			logger.info("Command Terminated." + ex.getMessage(), ex);
		}
	}

	@Override
	public void runScript(String command, String scanObserver, String JSFIdentifier) {
		try {
			int authorisationLevel = this.batonManager.getAuthorisationLevelOf(JSFIdentifier);
			setScanObserver(scanObserver);
			RunScriptRunner runner = new RunScriptRunner(this, command, authorisationLevel);
			runner.setName(nameThread(command));
			runCommandThreads.add(runner);
			// start the thread and return immediately.
			runner.start();
			clearThreads();
			notifyRefreshCommandThreads();
		} catch (Exception ex) {
			logger.error("Command Terminated." + ex.getMessage(), ex);
		}
	}

	@Override
	public String evaluateCommand(String command, String JSFIdentifier) {
		try {
			int authorisationLevel = this.batonManager.getAuthorisationLevelOf(JSFIdentifier);
			EvaluateRunner runner = new EvaluateRunner(interp, command, authorisationLevel);
			runner.setName(nameThread(command));
			runner.start();
			runner.join();
			return runner.result;
		} catch (Exception ex) {
			exceptionUtils.logException(logger, "evaluateCommand failed for " + command, ex);
		}
		return "";
	}

	private String nameThread(final String command) {
		String cmd = command;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		if (cmd.length() > 100) {
			cmd = cmd.substring(0, 100) + " ...";

		}
		String s = sdf.format(new Date()) + " : " + cmd.replace("\n", ";");
		return s;
	}

	@Override
	public boolean runsource(String command, String source, String JSFIdentifier) {
		return runsource(command, source, JSFIdentifier, null);
	}

	@Override
	public boolean runsource(String command, String source, String JSFIdentifier, InputStream stdin) {
		try {
			setScanObserver(source);
			int authorisationLevel = this.batonManager.getAuthorisationLevelOf(JSFIdentifier);
			echoInputToServerSideTerminalObservers(">>> " + command);
			RunSourceRunner runner = new RunSourceRunner(interp, command, authorisationLevel, stdin);
			runner.setName(nameThread(command));
			runsourceThreads.add(runner);
			runner.start();
			CommandThreadInfo info = notifyStartCommandThread(CommandThreadType.SOURCE,runner);
			runner.join();
			this.notifyTerminateCommandThread(info);
			return runner.result;
		} catch (Exception ex) {
			logger.info("Command terminated." + ex.getMessage(), ex);
		}
		return false;
	}

	private void echoInputToServerSideTerminalObservers(String s) {
		byte[] utf8Bytes;
		try {
			utf8Bytes = s.getBytes("UTF8");
			for (Terminal terminal : myTerminals) {
				terminal.write(utf8Bytes);
			}
		} catch (UnsupportedEncodingException e) {
			logger.error("Problem extracting UTF8 byte array from command: " + s, e);
		}
	}

	@Override
	public void setRawInput(String theInput, String JSFIdentifier) {
		if (expectingInputForRawInput) {
			// tell all terminals that an input has been received
			updateIObservers(RAW_INPUT_RECEIVED);
			theRawInput = theInput;
			expectingInputForRawInput = false;
			return;
		}
	}

	/**
	 * when users ask for command_line input during a script, this gives the Jython the response
	 *
	 * @return - the output from the user
	 * @throws InterruptedException
	 */
	public String requestRawInput() throws InterruptedException {
		expectingInputForRawInput = true;

		// tell all terminals that they should alter their input prompt
		updateIObservers(RAW_INPUT_REQUESTED);

		// call raw_input which waits for an input from the user
		while (expectingInputForRawInput) {
			Thread.sleep(100);
		}
		if (NULL.equals(theRawInput)) {
			throw Py.EOFError("EOF when reading a line");
		}
		return theRawInput;
	}

	@Override
	public synchronized void requestFinishEarly(String JSFIdentifier) {
		if (currentScan != null)
			currentScan.requestFinishEarly();
	}

	@Override
	public void pauseCurrentScan(String JSFIdentifier) {
		if (currentScan != null)
			currentScan.pause();
	}

	@Override
	public void resumeCurrentScan(String JSFIdentifier) {
		if (currentScan != null)
			currentScan.resume();
	}

	@Override
	public void restartCurrentScan(String JSFIdentifier) {
		// if the last scan has finished and was aborted for some reason, then re-run it
		if (currentScan != null) {
			ScanStatus status = currentScan.getStatus();
			if (status == ScanStatus.COMPLETED_AFTER_FAILURE || status == ScanStatus.COMPLETED_AFTER_STOP) {
				this.placeInJythonNamespace("the_restarted_scan", this.currentScan, JSFIdentifier);
				runCommand("the_restarted_scan.runScan()", JSFIdentifier);
			}
		}
	}

	@Override
	public void beamlineHalt(String JSFIdentifier) {
		abortCommands(true);
	}

	@Override
	public void abortCommands(String JSFIdentifier) {
		abortCommands(false);
	}

	private void abortCommands(final boolean andCallStopAll) {
		if (LocalProperties.check("gda.jython.hardware.stop.immediately", false)) {
			for (Scannable scannable : currentScan.getScannables()) {
				try {
					if (scannable.isBusy()) {
						scannable.stop();
					}
				} catch (DeviceException e) {
					logger.error("Failed to stop {}", scannable.getName());
					logger.error("Failed on calling scannable stop", e);
				}
			}
			for (Detector detector : currentScan.getDetectors()) {
				try {
					if (detector.getStatus()==Detector.BUSY) {
						detector.stop();
					}
				} catch (DeviceException e) {
					logger.error("Failed to stop {}", detector.getName());
					logger.error("Failed on calling detector stop", e);
				}
			}
		}
		uk.ac.gda.util.ThreadManager.getThread(new Runnable() {
			@Override
			public void run() {
				try {
					// first stop any command queue that might be running
					List<IFindableQueueProcessor> commandQueue = Finder.getInstance().listFindablesOfType(
							IFindableQueueProcessor.class);
					// unlikely to ever have more than one processor, but have this loop just in case
					for (IFindableQueueProcessor queue : commandQueue) {
						try {
							queue.stop(-1);
						} catch (Exception e) {
							// log and continue with the aborting process
							logger.error("Exception while stopping queue after abort called", e);
						}
					}

					interruptThreads();
					interp.getInterp().interrupt(Py.getThreadState());
				} finally {
					if (andCallStopAll)
						stopAll();
				}

				scriptStatus = Jython.IDLE;
				updateStatus();
				updateIObservers(new PanicStopEvent());
			}
		}).start();
	}

	@Override
	public void pauseCurrentScript(String JSFIdentifier) {
		ScriptBase.setPaused(true);
	}

	@Override
	public void resumeCurrentScript(String JSFIdentifier) {
		ScriptBase.setPaused(false);
	}

	private final Writer terminalWriter = new Writer() {

		@Override
		public void write(char[] cbuf, int off, int len) throws IOException {

			final String str = new String(cbuf, off, len);
			final TerminalOutput output = new TerminalOutput(str);

			updateIObservers(output);

			if (runningLocalStation) {
				bufferedLocalStationOutput += str;
			}
		}

		@Override
		public void flush() throws IOException {
			// do nothing
		}

		@Override
		public void close() throws IOException {
			// do nothing
		}
	};

	public Writer getTerminalWriter() {
		return terminalWriter;
	}

	@Override
	public int addFacade(IObserver anIObserver, String uniqueFacadeName, String hostName, String username, String fullName,
			String visitID) throws DeviceException {

		try {
			// JythonServer is allowed to have two facades. The remoteFacade is responsible for distributing stuff out
			// to CORBA and must be a JythonImpl. The localFacade will normally be an instance of JythonServerFacade
			// within the same ObjectServer.
			if (remoteFacade == null && anIObserver instanceof JythonImpl) {
				remoteFacade = anIObserver;
			} else if (localFacade == null) {
				localFacade = anIObserver;
			}

			// get the authoriser defined by java property
			Authoriser authoriser = AuthoriserProvider.getAuthoriser();

			// identify the authorisation level and record the new facade in the list of facade names
			int indexNumber = this.batonManager.getNewFacadeIndex();

			// if no username supplied, then its an object server
			if (username.compareTo("") == 0) {
				ClientDetails info = new ClientDetails(indexNumber, "", "", hostName, Integer.MAX_VALUE, false, visitID);
				this.batonManager.addFacade(uniqueFacadeName, info);
			} else {
				// add the facade and associated roles to the list of registered facades
				int accessLevel = authoriser.getAuthorisationLevel(username);
				ClientDetails info = new ClientDetails(indexNumber, username, fullName, hostName, accessLevel, false, visitID);
				logger.info("User " + username + " logged into GDA with authorisation level " + accessLevel);
				this.batonManager.addFacade(uniqueFacadeName, info);
			}
			return indexNumber;
		} catch (ClassNotFoundException e) {
			throw new DeviceException(e.getMessage());
		}
	}

	@Override
	public void switchUser(String uniqueFacadeName, String username, String visitID) throws DeviceException {
		try {
			Authoriser authoriser = AuthoriserProvider.getAuthoriser();
			int accessLevel = authoriser.getAuthorisationLevel(username);
			this.batonManager.switchUser(uniqueFacadeName, username, accessLevel, visitID);
		} catch (ClassNotFoundException e) {
			throw new DeviceException(e.getMessage());
		}
	}

	@Override
	public void removeFacade(String uniqueFacadeName) {
		this.batonManager.removeFacade(uniqueFacadeName);
	}

	@Override
	public void notifyServer(Object scan, Object data) {
		if (data instanceof ScanDataPoint) {
			((ScanDataPoint) data).setCreatorPanelName(scanObserverName);
		} else if (scan == currentScan && data instanceof ScanStatus){
			updateScanStatus(((ScanStatus)data).asJython());
		}

		updateIObservers(data);
	}

	/**
	 * Allows a restart of the command server while keeping the same object reference to this object.
	 */
	public void restart() {
		this.configured = false;
		interruptThreads();
		try {
			configure();
		} catch (FactoryException e) {
			logger.error(
					"Error while restarting the Jython interpreter. Fix the problem and then restart GDA immediately. Error message was: "
							+ e.getMessage(), e);
		}
	}

	@Override
	public int getScanStatus(String JSFIdentifier) {
		if (currentScan == null){
			return Jython.IDLE;
		}
		return currentScan.getStatus().asJython();
	}

	@Override
	public int getScriptStatus(String JSFIdentifier) {
		return scriptStatus;
	}

	@Override
	public void setScriptStatus(int newStatus, String JSFIdentifier) {
		// check if the status value is recognised and has changed
		if ((newStatus == Jython.IDLE || newStatus == Jython.RUNNING || newStatus == Jython.PAUSED)
				&& scriptStatus != newStatus) {
			scriptStatus = newStatus;
			updateStatus();
		}
	}

	@Override
	public void placeInJythonNamespace(String objectName, Object obj, String JSFIdentifier) {
		this.interp.placeInJythonNamespace(objectName, obj);
	}

	@Override
	public Object getFromJythonNamespace(String objectName, String JSFIdentifier) {
		return interp.getFromJythonNamespace(objectName);
	}

	/**
	 * Returns the contents of the top-level namespace.
	 * <p>
	 * This returns object references so cannot be distributed.
	 *
	 * @return PyObject
	 */
	@Override
	public Map<String, Object> getAllFromJythonNamespace() throws DeviceException {

		PyStringMap locals = (PyStringMap) this.interp.getAllFromJythonNamepsace();

		PyList dict = locals.keys();
		dict.sort();

		LinkedHashMap<String, Object> output = new LinkedHashMap<String, Object>();

		for (int i = 0; i < dict.__len__(); i++) {
			PyObject key = dict.__getitem__(i);
			output.put(key.asString(), locals.get(key).__tojava__(Object.class));
		}

		return output;
	}

	/**
	 * Changes dynamically the syntax translator to use.
	 *
	 * @param newTranslator
	 */
	public void setTranslator(Translator newTranslator) {
		this.interp.setTranslator(newTranslator);
	}

	@Override
	public String getRelease(String JSFIdentifier) {
		return gda.util.Version.getRelease();
	}

	@Override
	public String getStartupOutput(String JSFIdentifier) {
		return this.bufferedLocalStationOutput;
	}

	@Override
	public void setCurrentScan(Scan newScan) {
		this.currentScan = newScan;
	}

	@Override
	public void addAliasedCommand(String commandName, String JSFIdentifier) {
		if (this.batonManager.isJSFRegistered(JSFIdentifier)) {
			GDAJythonInterpreter.getTranslator().addAliasedCommand(commandName);
		}
	}
	//TODO either none or both addAliased{,Vararg}Command methods should require the guard: if (this.batonManager.isJSFRegistered(JSFIdentifier))
	@Override
	public void addAliasedVarargCommand(String commandName, String JSFIdentifier) {
		GDAJythonInterpreter.getTranslator().addAliasedVarargCommand(commandName);
	}

	@Override
	public Vector<Scannable> getDefaultScannables() {
		return defaultScannables;
	}

	@Override
	public boolean amIBatonHolder(String myJSFIdentifier) {
		return this.batonManager.amIBatonHolder(myJSFIdentifier);
	}

	@Override
	public void assignBaton(String myJSFIdentifier, int indexOfReciever) {
		this.batonManager.assignBaton(myJSFIdentifier, indexOfReciever);
	}

	@Override
	public ClientDetails getClientInformation(String myJSFIdentifier) {
		return this.batonManager.getClientInformation(myJSFIdentifier);
	}

	@Override
	public ClientDetails[] getOtherClientInformation(String myJSFIdentifier) {
		return this.batonManager.getOtherClientInformation(myJSFIdentifier);
	}

	@Override
	public boolean isBatonHeld() {
		return this.batonManager.isBatonHeld();
	}

	@Override
	public boolean requestBaton(String uniqueIdentifier) {
		return this.batonManager.requestBaton(uniqueIdentifier);
	}

	@Override
	public void returnBaton(String uniqueIdentifier) {
		this.batonManager.returnBaton(uniqueIdentifier);
	}

	@Override
	public int getAuthorisationLevel(int indexOfClient) {
		return this.batonManager.getAuthorisationLevelOf(indexOfClient);
	}

	@Override
	public void sendMessage(String myJSFIdentifier, String message) {
		ClientDetails details = this.batonManager.getClientInformation(myJSFIdentifier);
		final UserMessage msg = new UserMessage(details.getIndex(), details.getUserID(), message);

		// Save message first...
		saveMessage(details, msg);

		// ...before notifying clients
		updateIObservers(msg);
	}

	private void saveMessage(ClientDetails details, UserMessage message) {
		final String visit = details.getVisitID();
		if (StringUtils.hasText(visit)) {
			messageHandler.saveMessage(visit, message);
		}
	}

	@Override
	public List<UserMessage> getMessageHistory(String myJSFIdentifier) {
		final ClientDetails details = this.batonManager.getClientInformation(myJSFIdentifier);
		final String visit = details.getVisitID();
		if (StringUtils.hasText(visit)) {
			List<UserMessage> messages = messageHandler.getMessageHistory(visit);
			return messages;
		}
		return null;
	}

	private void updateIObservers(Object messageObject) {
		// localFacade will be null during configure phase, and before implFactory made
		if (localFacade != null) {
			localFacade.update(null, messageObject);
		}
		if (remoteFacade != null) {
			remoteFacade.update(null, messageObject);
		}
	}

	private void updateScanStatus(int newStatus) {
		// check if the status value has changed
		if (newStatus != lastScanStatus){
			lastScanStatus = newStatus;
			updateStatus();
		}
	}

	/*
	 * Creates a new JythonServerStatus object with the current state of the Jython system and passes it to all
	 * JythonFacade objects for distribution locally.
	 */
	private void updateStatus() {
		JythonServerStatus newStatus = new JythonServerStatus(scriptStatus, lastScanStatus);
		updateIObservers(newStatus);
		logger.info(newStatus.toString());
		jythonServerStatusObservers.notifyIObservers(null, newStatus);
	}

	/*
	 * If GDA syntax is used by commands passed through this class, any scans created will pass their data back to the
	 * scanObserver object. @param scanObserver IObserver
	 */
	private void setScanObserver(String scanObserver) {
		this.scanObserverName = scanObserver;
	}

	private synchronized void stopAll() {
		logger.info("Stopping Motors/Scannables/Stoppables (likely due to a panic stop button push) ...");
		InterfaceProvider.getTerminalPrinter().print("!!! Stopping motors");

		// As most Scannables will be backed by 'real' Motors it is most important to stop these 'real' motors first
		stopMotorsInFinder();

		if (isStoppingJythonScannablesOnStopAll()) {
			InterfaceProvider.getTerminalPrinter().print("!!! Stopping Jython scannables");
			// Note: Jython won't find interfaces (such as Scannable) on classes of objects that extend PyObject. Hence
			// the two classes below.
			String jythonCommand = "dontuse=None\n" + "for dontuse in globals().values(): \n"
					+ "\tif isinstance(dontuse,(PseudoDevice, ScannableBase)):\n" + "\t\ttry:\n" + "\t\t\tdontuse.stop()\n"
					+ "\t\texcept:\n" + "\t\t\tprint '    problem stopping ' + dontuse.getName()\n" + "\n"
					+ "del dontuse\n" + "\n";
			interp.exec(jythonCommand);
		} else {
			logger.info("Configured *not* to stop Scannables found in Jython namespace.");
		}

		List<Findable> stoppables = Finder.getInstance().listAllObjects(Stoppable.class.getSimpleName());
		if (!stoppables.isEmpty()) {
			InterfaceProvider.getTerminalPrinter().print("!!! Stopping stoppables");
		}

		for (Findable f : stoppables) {
			try {
				Stoppable s = (Stoppable) f;
				s.stop();
			} catch (Exception e) {
				logger.warn("Failed to stop " + StringUtils.quote(f.getName()), e);
			}
		}

		InterfaceProvider.getTerminalPrinter().print("!!! Stop-all complete");
		logger.info("... Stop complete");
	}

	private void stopMotorsInFinder() {

		class StopMotor implements Callable<Void> {
			private final Motor motor;

			public StopMotor(Motor motor) {
				this.motor = motor;
			}

			@Override
			public Void call() throws Exception {
				try {
					motor.stop();
				} catch (Exception e) {
					throw new Exception("!!!Could not stop motor '" + motor.getName() +"':" + e.getClass() + ":" + e.getMessage(), e);
				}
				return null;
			}

		}
		// Don't use an executor or thread pool so that we can name the threads with the motor name
		LinkedList<FutureTask<Void>> futureTasks = new LinkedList<FutureTask<Void>>();

		List<Motor> motors = Finder.getInstance().listFindablesOfType(Motor.class);
		logger.info("Stopping the " + motors.size() + " Motor instances registered in Finder");

		for (Motor motor : motors) {
			futureTasks.add(new FutureTask<Void>(new StopMotor(motor)));
			String threadName = format("{0}-JythonServer.StopMotor({1})", Thread.currentThread().getName(), motor.getName());
			(new Thread(futureTasks.getLast(), threadName)).start();
		}
		while(!futureTasks.isEmpty()) {
			try {
				futureTasks.pop().get();
			} catch (InterruptedException e) {
				// This thread is off running by itself, and its task is possibly *very* important. So...
				logger.info(Thread.currentThread().getName() + " swallowing InterruptedException while stopping all Motors");
			} catch (ExecutionException e) {
				// This thread is off running by itself, and its task is possibly *very* important. So...
				String msg = "Problem stopping a Motor: " + e.getCause().getMessage();
				logger.error(msg, e);
				InterfaceProvider.getTerminalPrinter().print(msg);
			}
		}
	}

	/**
	 * Stop all threads and clear the arrays of threads
	 */
	private synchronized void interruptThreads() {
		// in every thread started by this command server,
		// raise an interrupt
		Thread current = Thread.currentThread();
		for (Thread thread : this.runCommandThreads) {
			if (thread.getState() != Thread.State.TERMINATED && thread != current) {
				thread.interrupt();
			}
		}

		for (Thread thread : this.runsourceThreads) {
			if (thread.getState() != Thread.State.TERMINATED && thread != current) {
				thread.interrupt();
			}
		}

		for (Thread thread : this.evalThreads) {
			if (thread.getState() != Thread.State.TERMINATED && thread != current) {
				thread.interrupt();
			}
		}

		clearThreads();
		notifyRefreshCommandThreads();
	}

	/**
	 * Remove references to terminated threads from the lists of threads.
	 */
	private synchronized void clearThreads() {
		Vector<Thread> removeFromCommandThread = new Vector<Thread>();
		for (Thread thread : this.runCommandThreads) {
			if (thread.getState() == Thread.State.TERMINATED) {
				removeFromCommandThread.add(thread);
			}
		}
		for (Thread thread : removeFromCommandThread) {
			this.runCommandThreads.remove(thread);
		}

		Vector<Thread> removeFromSourceThread = new Vector<Thread>();
		for (Thread thread : this.runsourceThreads) {
			if (thread.getState() == Thread.State.TERMINATED) {
				removeFromSourceThread.add(thread);
			}
		}
		for (Thread thread : removeFromSourceThread) {
			this.runsourceThreads.remove(thread);
		}

		Vector<Thread> removeFromEvalThread = new Vector<Thread>();
		for (Thread thread : this.evalThreads) {
			if (thread.getState() == Thread.State.TERMINATED) {
				removeFromEvalThread.add(thread);
			}
		}
		for (Thread thread : removeFromEvalThread) {
			this.evalThreads.remove(thread);
		}
	}

	/**
	 * Base class for all the types of thread started by the JythonServer to run Jython commands.
	 */
	public static abstract class JythonServerThread extends Thread {
		GDAJythonInterpreter interpreter = null;
		String cmd = "";
		JythonServer server = null;
		/**
		 * The authorisation level of the user whose JythonServerFacade sent this command
		 */
		public int authorisationLevel;

		/**
		 * Access the command responsible for the thread
		 * @return command string
		 */
		public String getCommand() {
			return cmd;
		}

		/**
		 * Override flag
		 */
		public boolean hasBeenAuthorised = false;
	}

	/*
	 * Allows the Jython interpreter evaluate command to be run in a separate thread.
	 */
	private static class EvaluateRunner extends JythonServerThread {
		/**
		 * A string representing the result of the evaluated command.
		 */
		public String result = "";

		/**
		 * Constructor.
		 *
		 * @param interpreter
		 * @param command
		 * @param authorisationLevel
		 */
		public EvaluateRunner(GDAJythonInterpreter interpreter, String command, int authorisationLevel) {
			this.interpreter = interpreter;
			this.cmd = command;
			this.authorisationLevel = authorisationLevel;
		}

		@Override
		public void run() {
			result = interpreter.evaluate(cmd);
		}
	}

	/*
	 * Allows the Jython interpreter runcode command to be run in its own thread.
	 */
	private static class RunCommandRunner extends JythonServerThread {

		/**
		 * Constructor.
		 *
		 * @param server
		 * @param command
		 * @param authorisationLevel
		 */
		public RunCommandRunner(JythonServer server, String command, int authorisationLevel) {
			this.server = server;
			this.interpreter = server.interp;
			this.cmd = command;
			this.authorisationLevel = authorisationLevel;
		}

		@Override
		public void run() {
			try {
				this.interpreter.exec(cmd);
			} catch (Exception e) {
				logger.error(
						"CommandServer: error while running command: '" + cmd + "' encountered an error: "
								+ e.getMessage(), e);
			}
		}
	}

	/*
	 * Allows the Jython interpreter runcode command to be run in its own thread.
	 */
	private static class RunScriptRunner extends JythonServerThread {

		/**
		 * Constructor.
		 *
		 * @param server
		 * @param command
		 * @param authorisationLevel
		 */
		public RunScriptRunner(JythonServer server, String command, int authorisationLevel) {
			this.server = server;
			this.interpreter = server.interp;
			this.cmd = command;
			this.authorisationLevel = authorisationLevel;
		}

		@Override
		public void run() {
			String fileoutName = "";
			try {
				// open up a new file
				fileoutName = LocalProperties.getVarDir() + ".tempScript2";
				File out = new File(fileoutName);
				FileWriter buf = new FileWriter(out);
				// write the command to file
				buf.write(cmd);
				buf.close();
				// then run the file
				this.interpreter.runscript(out);
			} catch (IOException e) {
				logger.error("CommandServer: error while writing temporary script file." + fileoutName + " "
						+ e.getMessage());
				server.setScriptStatus(Jython.IDLE, null);
			} catch (Exception e) {
				if (e.getCause() instanceof ScanInterruptedException) {
					logger.info("CommandServer: " + e.getCause().getMessage());
				} else {
				logger.error(
						"CommandServer: error while running command: '" + cmd + "' encountered an error: "
								+ e.getMessage(), e);
				}
				server.setScriptStatus(Jython.IDLE, null);
			}
		}
	}

	/*
	 * This allows each command sent to the Jython Interpreter in its own thread
	 */
	private static class RunSourceRunner extends JythonServerThread {
		/**
		 * Returns true if the command is a completed Jython command or false if more input is required.
		 */
		public boolean result = false;
		private InputStream stdin;

		/**
		 * Constructor.
		 *
		 * @param interpreter The interpreter used to run the command
		 * @param command The command to run
		 * @param authorisationLevel The authorisation of the user who requested this command be run.
		 *         Prevents moves of devices which protection levels higher than the level given.
		 * @param stdin InputStream to take input from. Can be null - will use {@link InputCommands#requestInput(String)}
		 *         for input (input/raw_input) if so.
		 */
		public RunSourceRunner(GDAJythonInterpreter interpreter, String command, int authorisationLevel, InputStream stdin) {
			this.interpreter = interpreter;
			this.cmd = command;
			this.authorisationLevel = authorisationLevel;
			this.stdin = stdin;
		}

		/**
		 * Run the command
		 *
		 * If stdin was provided (non-null), create new system state with that stdin and use that to run the command,
		 * otherwise run in existing interpreter. The environment (globals()) will be the same for both.
		 */
		@Override
		public void run() {
			if (stdin == null) {
				result = interpreter.runsource(cmd);
			} else {
				try (InteractiveConsole py = interpreter.getChildConsole(stdin)) {
					result = py.runsource(cmd);
				}
			}
		}
	}

	@Override
	public Vector<String> getAliasedCommands(String JSFIdentifier) {
		return GDAJythonInterpreter.getTranslator().getAliasedCommands();
	}

	@Override
	public Vector<String> getAliasedVarargCommands(String JSFIdentifier) {
		return GDAJythonInterpreter.getTranslator().getAliasedVarargCommands();
	}

	@Override
	public String locateScript(String scriptToRun) {
		return jythonScriptPaths.pathToScript(scriptToRun);
	}

	@Override
	public String getDefaultScriptProjectFolder() {
		List<String> paths = jythonScriptPaths.getPaths();
		return (paths.size() > 0) ? paths.get(0) : null;
	}

	@Override
	public List<String> getAllScriptProjectFolders() {
		return jythonScriptPaths.getPaths();
	}

	@Override
	public String getProjectNameForPath(String path) {
		int index = jythonScriptPaths.getPaths().indexOf(path);
		if (index < 0)
			return null;
		return jythonScriptPaths.nameAt(index);
	}

	@Override
	public boolean projectIsUserType(String path) {
		int index = jythonScriptPaths.getPaths().indexOf(path);
		if (index < 0)
			return false;
		return jythonScriptPaths.getProject(index).isUserProject();
	}

	@Override
	public boolean projectIsConfigType(String path) {
		int index = jythonScriptPaths.getPaths().indexOf(path);
		if (index < 0)
			return false;
		return jythonScriptPaths.getProject(index).isConfigProject();
	}

	@Override
	public boolean projectIsCoreType(String path) {
		int index = jythonScriptPaths.getPaths().indexOf(path);
		if (index < 0)
			return false;
		return jythonScriptPaths.getProject(index).isCoreProject();
	}

	public Vector<Thread> getRunsourceThreads() {
		return runsourceThreads;
	}

	public Vector<Thread> getRunCommandThreads() {
		return runCommandThreads;
	}

	public Vector<Thread> getEvalThreads() {
		return evalThreads;
	}

	public InteractiveConsole getInterp() {
		return interp.getInterp();
	}

	private CommandThreadInfo extractCommandThreadInfo(CommandThreadType comtype, JythonServerThread jthread) {
		CommandThreadInfo info = new CommandThreadInfo();
		info.setCommand(jthread.getCommand());
		info.setCommandThreadType(comtype.toString());
		info.setDate(""); //TODO
		info.setId(jthread.getId());
		info.setInterrupted(jthread.isInterrupted());
		info.setName(jthread.getName());
		info.setPriority(jthread.getPriority());
		info.setState(jthread.getState().toString());
		info.setTime(""); //TODO
		return info;
	}

	@SuppressWarnings("unused") // future feature
	private void notifyClearCommandThreads() {
		this.updateIObservers(new CommandThreadEvent(CommandThreadEventType.CLEAR,null));
	}

	private void notifyRefreshCommandThreads() {
		this.updateIObservers(new CommandThreadEvent(CommandThreadEventType.REFRESH,null));
	}

	private CommandThreadInfo notifyStartCommandThread(CommandThreadType comType, JythonServerThread thread) {
		return this.notifyCommandThreadEvent(CommandThreadEventType.START, comType, thread);
	}

	private void notifyTerminateCommandThread(CommandThreadInfo info) {
		this.updateIObservers(new CommandThreadEvent(CommandThreadEventType.TERMINATE,info));
	}

	@SuppressWarnings("unused") // future feature
	private CommandThreadInfo notifyUpdateCommandThread(CommandThreadType comType, JythonServerThread thread) {
		return this.notifyCommandThreadEvent(CommandThreadEventType.UPDATE, comType, thread);
	}

	private CommandThreadInfo notifyCommandThreadEvent(CommandThreadEventType eType, CommandThreadType comType, JythonServerThread thread) {
		CommandThreadInfo info = null==thread ? null : extractCommandThreadInfo(comType, thread);
		this.updateIObservers(new CommandThreadEvent(eType,info));
		return info;
	}

	@Override
	public ICommandThreadInfo[] getCommandThreadInfo() {
		Vector<ICommandThreadInfo> infos = new Vector<ICommandThreadInfo>();
		for (Thread t : runsourceThreads) {
			if (t.isAlive() && t instanceof JythonServerThread) {
				infos.add(extractCommandThreadInfo(CommandThreadType.SOURCE,(JythonServerThread) t));
			}
		}
		for (Thread t : runCommandThreads) {
			if (t.isAlive() && t instanceof JythonServerThread) {
				infos.add(extractCommandThreadInfo(CommandThreadType.COMMAND,(JythonServerThread) t));
			}
		}
		for (Thread t : evalThreads) {
			if (t.isAlive() && t instanceof JythonServerThread) {
				infos.add(extractCommandThreadInfo(CommandThreadType.EVAL,(JythonServerThread) t));
			}
		}
		return infos.toArray(new ICommandThreadInfo[infos.size()]);
	}

	public void setStopJythonScannablesOnStopAll(boolean stopJythonScannablesOnStopAll) {
		this.stopJythonScannablesOnStopAll = stopJythonScannablesOnStopAll;
	}

	public boolean isStoppingJythonScannablesOnStopAll() {
		return stopJythonScannablesOnStopAll;
	}

	@Override
	public ScanInformation getCurrentScanInformation() {
		return currentScan == null ? null : currentScan.getScanInformation();
	}

	@Override
	public boolean isFinishEarlyRequested() {
		//TODO: Potential NPE?
		return currentScan.isFinishEarlyRequested();
	}

	@Override
	public void addIObserver(IObserver anIObserver) {
		// put all Terminals in a separate list as well as they will want extra output
		if (anIObserver instanceof Terminal) {
			if (!myTerminals.contains(anIObserver)) {
				myTerminals.addElement((Terminal) anIObserver);
			}
		}
	}

	@Override
	public void deleteIObserver(IObserver anIObserver) {
		if (anIObserver instanceof Terminal) {
			myTerminals.removeElement(anIObserver);
		}
	}

	@Override
	public void deleteIObservers() {
		myTerminals.removeAllElements();
	}

	public void setDisableBatonControlOverVisitMetadataEntry(boolean disable) {
		batonManager.setDisableControlOverVisitMetadataEntry(disable);
	}

	public boolean isDisableBatonControlOverVisitMetadataEntry() {
		return batonManager.isDisableControlOverVisitMetadataEntry();
	}

	@Override
	public void addInputTerminal(Terminal term) {
		addIObserver(term);
	}

	@Override
	public void deleteInputTerminal(Terminal term) {
		deleteIObserver(term);
	}

	@Override
	public void addJythonServerStatusObserver(IJythonServerStatusObserver anObserver) {
		jythonServerStatusObservers.addIObserver(anObserver);
	}

	@Override
	public void deleteJythonServerStatusObserver(IJythonServerStatusObserver anObserver) {
		jythonServerStatusObservers.deleteIObserver(anObserver);
	}

	@Override
	public JythonServerStatus getJythonServerStatus() {
		return new JythonServerStatus(scriptStatus, getScanStatus(null));
	}

	public void showUsers() {

		final ITerminalPrinter tp = InterfaceProvider.getTerminalPrinter();

		final List<ClientDetails> clients = batonManager.getAllClients();

		tp.print(String.format("%d client%s connected%s",
			clients.size(),
			(clients.size() == 1) ? "" : "s",
			(clients.size() > 0) ? ":" : "."));

		if (!clients.isEmpty()) {

			tp.print("");

			tp.print(String.format("%-6s   %-15s   %-20s   %-10s   %s",
				"Number", "Username", "Hostname", "Visit", "Holds baton?"));

			tp.print("=============================================================================");

			for (ClientDetails c : clients) {
				tp.print(String.format("%-6d   %-15s   %-20s   %-10s   %s",
					c.getIndex(),
					c.getUserID(),
					c.getHostname(),
					c.getVisitID(),
					c.isHasBaton() ? "yes" : ""));
			}
		}
	}

	@Override
	public PyObject eval(String s) {
		return interp.getInterp().eval(s);
	}

	@Override
	public void exec(String s) {
		interp.getInterp().exec(s);
	}

	public final boolean isAtStartup() {
		return atStartup;
	}

	@Override
	public AutoCompletion getCompletionsFor(String line, int posn) {
		return jythonCompleter.getCompletionsFor(line, posn);
	}
}
