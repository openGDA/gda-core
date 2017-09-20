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

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.python.core.PyException;
import org.python.core.PyObject;

import gda.device.DeviceException;
import gda.factory.Findable;
import gda.jython.batoncontrol.ClientDetails;
import gda.jython.commandinfo.ICommandThreadInfo;
import gda.jython.completion.AutoCompletion;
import gda.observable.IObserver;

/**
 * The distributed interface for the JythonServer object. This interface should not be operated directly, but via the
 * InterfaceProvider.
 */
public interface Jython extends Findable {

	/**
	 * Name of this object. This should agree with the string used in Castor.
	 */
	public static final String SERVER_NAME = "command_server";

	/**
	 * Script, scan or queue not in use
	 */
	public static int IDLE = 0;

	/**
	 * Script, scan or queue in progress but paused
	 */
	public static int PAUSED = 1;

	/**
	 * Script, scan or queue in progress and not paused
	 */
	public static int RUNNING = 2;

	/**
	 * String passed to IObservers of the CommandServer that it is waiting for input via the setRawInput method
	 */
	public static final String RAW_INPUT_REQUESTED = "raw input requested";

	/**
	 * String passed to IObservers of the CommandServer that it has received input via the setRawInput method
	 */
	public static final String RAW_INPUT_RECEIVED = "raw input received";

	/**
	 * Runs a single line Jython command through the interpreter and returns the result in the form of a string. Note:
	 * this method waits until the command has finished so it can return the result. If the command takes a long time it
	 * will hang the thread which calls this method. So this method must be called in a separate thread from the main
	 * GUI thread, else the GUI will seize up until the command given to this method has returned. For an example of
	 * the, see the gda.jython.JythonTerminal class.
	 *
	 * @param command
	 *            String
	 * @param JSFIdentifier
	 *            - the unique ID of the JythonServerFacade calling this method.
	 * @return String
	 */
	public String evaluateCommand(String command, String JSFIdentifier);

	/**
	 * Executes the Jython command in a new thread (unless the command starts
	 * with the text 'print').
	 *
	 * @param command
	 *            String
	 * @param JSFIdentifier
	 *            - the unique ID of the JythonServerFacade calling this method.
	 */
	public void runCommand(String command, String JSFIdentifier);

	/**
	 * Runs the Jython command, and changes the ScriptStatus as is goes.
	 *
	 * @param command
	 *            String
	 * @param JSFIdentifier
	 *            - the unique ID of the JythonServerFacade calling this method.
	 */
	public void runScript(String command, String JSFIdentifier);

	/**
	 * Similar to {@link #runCommand}, except that a boolean is returned if the command was complete or if additional lines of a
	 * multi-line command are required. Used only by the JythonTerminal to determine which prompt to display. Note: this
	 * method waits until the command has finished so it can return the result. If the command takes a long time it will
	 * hang the thread which calls this method. So this method must be called in a separate thread from the main GUI
	 * thread, else the GUI will seize up until the command given to this method has returned.
	 *
	 * @param command
	 *            String
	 * @param JSFIdentifier
	 *            - the unique ID of the JythonServerFacade calling this method.
	 * @return boolean
	 */
	public boolean runsource(String command, String JSFIdentifier);

	/**
	 * Similar to {@link #runCommand}, except that a boolean is returned if the command was complete or if additional lines of a
	 * multi-line command are required. Used only by the JythonTerminal to determine which prompt to display. Note: this
	 * method waits until the command has finished so it can return the result. If the command takes a long time it will
	 * hang the thread which calls this method. So this method must be called in a separate thread from the main GUI
	 * thread, else the GUI will seize up until the command given to this method has returned.
	 *
	 * @param command
	 *            the command to run
	 * @param JSFIdentifier
	 *            the unique ID of the JythonServerFacade calling this method.
	 * @param stdin the InputStream to use as stdin for this command
	 * @return true if command was incomplete and more is required (eg "if True:"), false otherwise (including on error)
	 */
	public boolean runsource(String command, String JSFIdentifier, InputStream stdin);

	/**
	 * Foe use by the JythonServerFacade class. Allows an instance of this class operating in a different process to
	 * remotely register itself with a JythonServer.
	 *
	 * @param anIObserver
	 * @param JSFIdentifier
	 * @param hostName
	 * @param username
	 * @param fullName
	 * @param visitID
	 * @return the index number of the facade (i.e. its public key)
	 * @throws DeviceException
	 *             - thrown if an error during authentication/authorisation
	 */
	public int addFacade(IObserver anIObserver, String JSFIdentifier, String hostName, String username, String fullName, String visitID)
			throws DeviceException;


	/**
	 * Removes the registration of the facade with this server and returns the baton if applicable.
	 *
	 * @param uniqueFacadeName
	 */
	public void removeFacade(String uniqueFacadeName);

	/**
	 * For an already registered JythonServerFacade, changes information for that facade. This can change the permission
	 * level of this facade (and so this client) or the visit which data is savaed as when this client has the baton.
	 *
	 * @param uniqueFacadeName
	 * @param username
	 * @param visitID
	 * @throws DeviceException
	 */
	public void switchUser(String uniqueFacadeName, String username, String visitID) throws DeviceException;


	/**
	 * Cleanly stops the current scan.
	 *
	 * @param JSFIdentifier
	 *            - the unique ID of the JythonServerFacade calling this method.
	 */
	public void requestFinishEarly(String JSFIdentifier);


	/**
	 * @return true if the current scan has had requestFinishEarly called on it.
	 */
	public boolean isFinishEarlyRequested();

	/**
	 * Stops all scripts, scans, and commands immediately. Also calls the stop method on all motors.
	 *
	 * @param JSFIdentifier
	 *            - the unique ID of the JythonServerFacade calling this method.
	 */
	public void beamlineHalt(String JSFIdentifier);


	/**
	 * Stops all scripts, scans, and commands running from the Jython Server immediately.
	 *
	 * @param JSFIdentifier
	 *            - the unique ID of the JythonServerFacade calling this method.
	 */
	public void abortCommands(String JSFIdentifier);

	/**
	 * Pauses the current scan
	 *
	 * @param JSFIdentifier
	 *            - the unique ID of the JythonServerFacade calling this method.
	 */
	public void pauseCurrentScan(String JSFIdentifier);

	/**
	 * Pauses the current script the next time it calls the ScriptBase.checkForPuases method until resumeCurrentScript
	 * is called.
	 *
	 * @param JSFIdentifier
	 *            - the unique ID of the JythonServerFacade calling this method.
	 */
	public void pauseCurrentScript(String JSFIdentifier);

	/**
	 * Resumes the currently paused scan.
	 *
	 * @param JSFIdentifier
	 *            - the unique ID of the JythonServerFacade calling this method.
	 */
	public void resumeCurrentScan(String JSFIdentifier);

	/**
	 * Restarts the last scan which was run, if it ended due to an error or it was interrupted. If the last scan
	 * completed successfully, then this will do nothing.
	 *
	 * @param JSFIdentifier
	 *            - the unique ID of the JythonServerFacade calling this method.
	 */
	public void restartCurrentScan(String JSFIdentifier);

	/**
	 * Resumes the currently paused script
	 *
	 * @param JSFIdentifier
	 *            - the unique ID of the JythonServerFacade calling this method.
	 */
	public void resumeCurrentScript(String JSFIdentifier);

	/**
	 * Returns the scan of the queue (running, idle, paused).
	 *
	 * @param JSFIdentifier
	 *            - the unique ID of the JythonServerFacade calling this method.
	 * @return int
	 */
	public int getScanStatus(String JSFIdentifier);

	/**
	 * Returns the status of the script (running, idle, paused).
	 *
	 * @param JSFIdentifier
	 *            - the unique ID of the JythonServerFacade calling this method.
	 * @return int
	 */
	public int getScriptStatus(String JSFIdentifier);

	/**
	 * Returns information about each active command thread
	 *
	 * @return Array of command thread information
	 */
	public ICommandThreadInfo[] getCommandThreadInfo();

	/**
	 * Sets the script status.
	 *
	 * @param status
	 *            int
	 * @param JSFIdentifier
	 *            - the unique ID of the JythonServerFacade calling this method.
	 */
	public void setScriptStatus(int status, String JSFIdentifier);

	/**
	 * Pass a copy of an object to the Jython interpreter. This object must be relatively simple otherwise it will not
	 * be passed over CORBA successfully. So the object must be a native type (or only contain native types) and must
	 * not have any object references inside.
	 *
	 * @param objectName
	 * @param obj
	 * @param JSFIdentifier
	 *            - the unique ID of the JythonServerFacade calling this method.
	 */
	public void placeInJythonNamespace(String objectName, Object obj, String JSFIdentifier);

	/**
	 * Get a copy of an object from the Jython interpreter. Note that the retreived object will have to be cast by the
	 * local code. As the object will be passed over CORBA, this method should only be used on native types or classes
	 * containing only native types.
	 *
	 * @param objectName
	 * @param JSFIdentifier
	 *            - the unique ID of the JythonServerFacade calling this method.
	 * @return Object
	 */
	public Object getFromJythonNamespace(String objectName, String JSFIdentifier);

	/**
	 * Returns the name of the GDA release being used by the CommandServer. This is the String returned by the
	 * JythonServer's local copy of gda.util.Version. This enables Client and Server processes to check if they are of
	 * the same GDA version.
	 *
	 * @param JSFIdentifier
	 *            - the unique ID of the JythonServerFacade calling this method.
	 * @return the current GDA release used by the CommandServer.
	 */
	public String getRelease(String JSFIdentifier);

	/**
	 * This is used by clients when they start to immediately give any output messages from the startup script.
	 *
	 * @param JSFIdentifier
	 *            - the unique ID of the JythonServerFacade calling this method.
	 * @return the output generated when the CommandServer ran its startup file as a string.
	 */
	public String getStartupOutput(String JSFIdentifier);

	/**
	 * After a requestRawInput has been called, the response should be returned via this method. The string is then the
	 * returned string to script which called requestRawInput. This method is distributed because the response could
	 * come from any object. The requestRawInput should only come from within a script, so that method is not
	 * distributed.
	 *
	 * @param theInput
	 * @param JSFIdentifier
	 *            - the unique ID of the JythonServerFacade calling this method.
	 */
	public void setRawInput(String theInput, String JSFIdentifier);

	// methods associated with control
	/**
	 * Ask for the baton for control of the beamline devices.
	 *
	 * @param uniqueIdentifier
	 * @return boolean
	 */
	public boolean requestBaton(String uniqueIdentifier);

	/**
	 * Inform the JythonServer that the baton is no longer required by this connection.
	 *
	 * @param uniqueIdentifier
	 *            String
	 */
	public void returnBaton(String uniqueIdentifier);

	/**
	 * Inform the JythonServer which Client should now have the baton. This method will only be run for current holders
	 * of the baton.
	 *
	 * @param myJSFIdentifier
	 * @param indexOfReciever
	 */
	public void assignBaton(String myJSFIdentifier, int indexOfReciever);


	/**
	 * Returns details of this client
	 *
	 * @param name
	 * @return ClientDetails
	 */
	public ClientDetails getClientInformation(String name);

	/**
	 * Returns an array of objects describing the other clients on this beamline.
	 * <p>
	 * This array may be slightly out of date if the client was not closed down properly, but after a few minutes the
	 * client should be automatically removed from the list.
	 *
	 * @param myJSFIdentifier
	 * @return ClientDetails[]
	 */
	public ClientDetails[] getOtherClientInformation(String myJSFIdentifier);

	/**
	 * @param myJSFIdentifier
	 * @return boolean if the given client holds the beamline baton
	 */
	public boolean amIBatonHolder(String myJSFIdentifier);

	/**
	 * Returns true if any client holds the baton. If no client holds the baton then all clients may operate hardware
	 * subject to their authorisation level.
	 *
	 * @return true if any client holds the baton.
	 */
	public boolean isBatonHeld();


	/**
	 * @param indexOfClient
	 * @return the authorisation level of the given client
	 */
	public int getAuthorisationLevel(int indexOfClient);

	/**
	 * Broadcast a message to other users on this beamline. Such messages will be displayed in a special viewer.
	 *
	 * @param myJSFIdentifier
	 * @param message
	 */
	public void sendMessage(String myJSFIdentifier, String message);

	/**
	 * Returns previous messages sent during this visit.
	 */
	public List<UserMessage> getMessageHistory(String myJSFIdentifier);

	// commands for aliasing methods in the Jython environment
	/**
	 * Add a new aliased command.
	 *
	 * @param commandName
	 * @param JSFIdentifier
	 *            - the unique ID of the JythonServerFacade calling this method.
	 */
	public void addAliasedCommand(String commandName, String JSFIdentifier);

	/**
	 * Add a new vararg aliased command
	 *
	 * @param commandName
	 * @param JSFIdentifier
	 *            - the unique ID of the JythonServerFacade calling this method.
	 */
	public void addAliasedVarargCommand(String commandName, String JSFIdentifier);

	/**
	 * Get the list of aliased commands - GDA syntax extension in Jython environment
	 * @param JSFIdentifier
	 * @return the aliased commands
	 */
	public Vector<String> getAliasedCommands(String JSFIdentifier);
	/**
	 * Get the list of vararg aliased commands - GDA syntax extension in Jython environment
	 * @param JSFIdentifier
	 * @return the aliased vararg commands
	 */
	public Vector<String> getAliasedVarargCommands(String JSFIdentifier);

	/**
	 * Returns the contents of the top-level Jython namespace.
	 *
	 * @return Map
	 * @throws DeviceException
	 */
	public Map<String, Object> getAllFromJythonNamespace() throws DeviceException;

	/**
	 * Finds a script in the server's script folders.
	 * @param scriptToRun The name of the script.
	 * @return The path to the script, or null if it can't be found.
	 */
	public String locateScript(String scriptToRun);

	/**
	 * The default location for Jython scripts in the server.
	 * @return A path to a folder where the server will search for Jython scripts.
	 */
	public String getDefaultScriptProjectFolder();

	public List<String> getAllScriptProjectFolders();

	public String getProjectNameForPath(String path);

	public boolean projectIsUserType(String path);

	public boolean projectIsConfigType(String path);

	public boolean projectIsCoreType(String path);

	/**
	 * Evaluates a string as a Python expression and returns the result. Bypasses translator, batton control, and is not
	 * available across corba.
	 * <p>
	 * This is of particular utility compared to other offerings as calls are synchronous, throw exceptions and can
	 * return an actual object.
	 *
	 * @param s
	 *            The pure Jython string command to eval.
	 * @return PyObject The result of the eval
	 * @throws PyException If eval resulted in exception.
	 */
	public PyObject eval(String s) throws PyException;

	/**
	 * Executes a string of Python source in the local namespace. Bypasses translator, batton control, and is not
	 * available across corba.
	 * <p>
	 * This is of particular utility compared to other offerings as calls are synchronous and throw exceptions.
	 *
	 * @param s The pure Jython string command to exec.
	 * @throws PyException If exec resulted in exception.
	 */
	public void exec(String s) throws PyException;

	public AutoCompletion getCompletionsFor(String line, int posn);

	public void print(String text);
}
