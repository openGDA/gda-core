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

package gda.jython.commands;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.scannable.ScannableUtils;
import gda.device.scannable.scannablegroup.IScannableGroup;
import gda.device.scannable.scannablegroup.ScannableGroup;
import gda.factory.FactoryException;
import gda.factory.Findable;
import gda.factory.Finder;
import gda.jython.InterfaceProvider;
import gda.jython.JythonServer;
import gda.jython.JythonServerFacade;
import gda.jython.ScriptBase;
import gda.jython.scriptcontroller.Scriptcontroller;

/**
 * Holder for a series of static methods to operate Scannable objects
 */
public final class GeneralCommands {

	private static final Logger logger = LoggerFactory.getLogger(GeneralCommands.class);

	private GeneralCommands() {
		// Prevent instances
	}

	/**
	 * Print the default help message
	 */
	public static void gdahelp() {
		final List<String> helpString = new ArrayList<>();

		helpString.add("Available console commands in addition to Jython syntax:");
		helpString.add("* help                            -prints this message");
		helpString.add("* pos <scannable>                 -gets the current position of the object");
		helpString.add("* pos <scannable> new_position    -moves the object to the new position");
		helpString.add("* inc <scannable> increment       -incremental move by the given amount");
		helpString.add("* upos <scannable> new_position   -pos command without print out during the move");
		helpString.add("* uinc <scannable> increment      -inc command without print out during the move");
		helpString.add("* ls                              -list all the interfaces of existing objects");
		helpString.add("* ls interfaceName                -lists all the objects of the given type (interface)");
		helpString.add("* ls_names                        -lists the names of all findables");
		helpString.add("* list_defaults                   -lists all objects which would be used in a scan by default");
		helpString.add("* add_default <scannable>         -add an object to the defaults list");
		helpString.add("* remove_default <scannable>      -remove an object from the defaults list");
		helpString.add("* pause                           -during a script, checks to see if the pause/resume button has been pressed");
		helpString.add("* watch <scannable>               -adds the scannable to the watch sub-panel in the terminal panel.");
		helpString.add("* level <scannable> [value]       -if value is declared then sets move order (level) of the scannable, else returns the current level.");
		helpString.add("* alias functionName              -where functionName is a function in the global namespace.  This dynamically adds a function to the extended syntax.");
		helpString.add("* run \"scriptName\"                -runs the named script.");
		helpString.add("* record [on|off]                 -starts/stops recording all terminal output to a file placed in the scripts directory");
		helpString.add("* scan <scannable> start stop step [pseudoDevice2] [start] [[stop] step]        -automated movement of a group of pseudoDevices in concurrent steps, with data collected after each step");
		helpString.add("* testscan <scannable> start stop step [pseudoDevice2] [start] [[stop] step]    -as scan, except does not move anything and justs validates the parameters.");
		helpString.add("* cscan <scannable> [centroid] width step [pseudoDevice2] [centroid] [width]    -as scan, except uses different inputs");
		helpString.add("* pscan <scannable> start step no_points [pseudoDevice2] start [step]           -as scan, except uses different inputs");
		helpString.add("* gscan <scannable> start stop step [pseudoDevice2] [start] [stop] [step]       -grid scan in which each dimension is moved separately.  Data is collected after each step.");
		helpString.add("* timescan detector numberOfPoints pauseTime collectTime                        -Time scan in which the positions of a list of pseudoDevices are collected at regular intervals. ");
		helpString.add("* tscan numberOfPoints pauseTime [collectTime] scannable...                     -Time scan in which the positions of a list of pseudoDevices are collected at regular intervals. ");
		helpString.add("* cvscan [motor1] [start] [stop] time [motor2 start stop] [detectors]           -constant velocity scan (XPS motor only).");
		helpString.add("* robotscan sample startNumber stopNumber [step] [translator start stop step] cv-motor startAngle [stopAngle] totalTime -cvscan multiple sample at multiple sample position (Motoman & XPS only). ");

		// Print the help
		final String output = String.join("\n", helpString);
		InterfaceProvider.getTerminalPrinter().print(output);
	}

	/**
	 * List all the types of objects (interfaces) in use on this beamline
	 */
	public static void ls() {
		ls("");
	}

	/**
	 * Lists all the available types of objects or objects of a given type
	 *
	 * @param interfaceName
	 */
	public static void ls(String interfaceName) {

		if (interfaceName == null || interfaceName.compareTo("") == 0 || interfaceName.compareTo("all") == 0) {
			List<String> availableInterfaces = Finder.getInstance().listAllInterfaces();

			availableInterfaces.remove("Findable");
			availableInterfaces.remove("Hashtable");
			availableInterfaces.remove("Map");
			availableInterfaces.remove("Cloneable");
			availableInterfaces.remove("PollerListener");
			availableInterfaces.remove("Findable");
			availableInterfaces.remove("MonitorListener");
			availableInterfaces.remove("PutListener");
			availableInterfaces.remove("GetListener");
			availableInterfaces.remove("ConnectionListener");
			availableInterfaces.remove("Serializable");
			availableInterfaces.remove("Jython");
			availableInterfaces.remove("Configurable");
			availableInterfaces.remove("Localizable");
			availableInterfaces.remove("JythonServer");
			availableInterfaces.remove("Runnable");
			availableInterfaces.remove("IObservable");
			availableInterfaces.remove("Motor");
			availableInterfaces.remove("IObserver");
			availableInterfaces.remove("Scriptcontroller");
			availableInterfaces.remove("Device");
			availableInterfaces.remove("Closeable");
			availableInterfaces.remove("Flushable");
			availableInterfaces.remove("RobotListener");
			availableInterfaces.remove("Observer");
			availableInterfaces.remove("IIsBeingObserved");
			availableInterfaces.remove("Metadata");
			availableInterfaces.remove("IPlotManager");
			availableInterfaces.remove("PlotServerDevice");
			availableInterfaces.remove("IFileRegistrar");
			availableInterfaces.remove("IDataWriterExtender");
			availableInterfaces.remove("IJythonServerNotifer");
			availableInterfaces.remove("AlarmListener");
			availableInterfaces.remove("IDefaultScannableProvider");
			availableInterfaces.remove("ICurrentScanHolder");
			availableInterfaces.remove("DataWriterFactory");
			availableInterfaces.remove("INeXusInfoWriteable");

			String output = String.join("\n", availableInterfaces);

			// print out to the Jython interpreter directly (so output is formatted correctly)
			try {
				InterfaceProvider.getTerminalPrinter().print(output);
			} catch (Exception e) {
				// do nothing
			}
		}
	}

	/**
	 * List all the instances of a particular type (interface) of object
	 *
	 * @param theInterface
	 * @throws DeviceException
	 */
	public static void ls(Class<Findable> theInterface) throws DeviceException {
		Map<String, Object> map = InterfaceProvider.getJythonNamespace().getAllFromJythonNamespace();

		String output = "\n";
		for (String objName : map.keySet()) {
			Object obj = map.get(objName);
			if (theInterface.isInstance(obj)) {
				if (obj instanceof IScannableGroup) {
					output += ScannableUtils.prettyPrintScannableGroup((ScannableGroup) obj) + "\n";
				} else if (obj instanceof Scannable) {
					output += ((Scannable) obj).toFormattedString() + "\n";
				} else {
					output += obj.toString() + "\n";
				}
			}
		}
		InterfaceProvider.getTerminalPrinter().print(output);
	}

	/**
	 * List the names of all Scannables whose name does not startwith __
	 * @throws DeviceException
	 */
	public static void ls_names() throws DeviceException {
		ls_names(Scannable.class);
	}
	/**
	 * List all the instances of a particular type (interface) of object which are also Findable
	 *
	 * @param theInterface
	 * @throws DeviceException
	 */
	public static void ls_names(Class<? extends Object> theInterface) throws DeviceException {
		Map<String, Object> map = InterfaceProvider.getJythonNamespace().getAllFromJythonNamespace();

		String output = "\n";
		for (String objName : map.keySet()) {
			Object obj = map.get(objName);
			if ( theInterface.isInstance(obj) &&  (obj instanceof Findable )) {
				String name = ((Findable)obj).getName();
				if( ! name.startsWith("__")){
					output += name + "\n";
				}
			}
		}
		InterfaceProvider.getTerminalPrinter().print(output);
	}

	/**
	 * To allow the list function to be used all the way to the bottom of the object tree.
	 *
	 * @param theScannable
	 */
	public static void ls(Scannable theScannable) {
		InterfaceProvider.getTerminalPrinter().print(theScannable.getName());
	}

	/**
	 * Pauses while the flag in ScriptBase has been set to pause any scripts. This will return when the flag has been
	 * unset.
	 * <p>
	 * If the interrupt flag has been set then an exception will be thrown. This will still be thrown if this method is
	 * in the middle of waiting to resume.
	 *
	 * @throws Exception
	 */
	public static void pause() throws Exception {
		ScriptBase.checkForPauses();
	}

	/**
	 * Runs a script of the given name. It is assumed that the script is at the top-level of the user script directory
	 * or the beamline script directory.
	 * <p>
	 * This method is for typing convenience. Most scripts should really be imported and their method called directly.
	 *
	 * @param scriptName
	 * @throws Exception
	 */
	public static void run(String scriptName) throws Exception {
		// NOTE: ideally this method would try the entire python sys.path, but this would
		//       require making a breaking change and possibly be overkill!
		JythonServer server = Finder.getInstance().find(JythonServer.SERVERNAME);

		// allow full paths to be given to this method
		String path = scriptName;
		if (!scriptName.startsWith(File.separator)) { // if path starts with a backslash assume a full path has been given
			path = server.getJythonScriptPaths().pathToScript(scriptName);
			if (path == null) {
				throw new FileNotFoundException("Could not run " + scriptName + " script. File not found in "
						+ server.getJythonScriptPaths().description() + ".");
			}
		}

		// Run the file
		logger.info("<<< Running {} ({})", scriptName, path);
		server.runCommandSynchronously(path);
		logger.info("Completed running {} >>>", scriptName);

	}

	/**
	 * Restarts the Jython server without a need for a GDA restart. The Jython namespace is completely reset and
	 * localStation re-run. However, connections to hardware are not re-established i.e. Object Servers are not
	 * reconfigured.
	 */
	public static void reset_namespace() {
		logger.info("Resetting Jython namespace");
		((JythonServer) Finder.getInstance().find(JythonServer.SERVERNAME)).restart();
		reconfigureScriptControllers();
	}

	private static void reconfigureScriptControllers() {
		Map<String, Scriptcontroller> scriptControllers = Finder.getInstance().getFindablesOfType(Scriptcontroller.class);
		for (Scriptcontroller sc : scriptControllers.values()) {
			logger.debug("Reconfiguring '{}' script controller", sc.getName());
			try {
				sc.reconfigure();
			} catch (FactoryException ex) {
				logger.error("Could not reconfigure '{}' script controller", ex);
			}
		}
	}

	/**
	 * Add a new aliased command.
	 *
	 * @param commandName
	 */
	public static void alias(String commandName) {
		gda.jython.JythonServerFacade.getInstance().addAliasedCommand(commandName);
	}

	/**
	 * Add a new vararg aliased command
	 *
	 * @param commandName
	 * @deprecated Use {@link #vararg_alias(String)} instead
	 */
	@Deprecated
	public static void vararg_regex(String commandName) {
		logger.warn("'vararg_regex' is deprecated and will be removed, replace with 'vararg_alias'");
		JythonServerFacade.getInstance().addAliasedVarargCommand(commandName);
	}

	/**
	 * Add a new vararg aliased command
	 *
	 * @param commandName
	 */
	public static void vararg_alias(String commandName) {
		JythonServerFacade.getInstance().addAliasedVarargCommand(commandName);
	}

	/**
	 * runs system command which input as string
	 *
	 * @param command
	 */
	public static void cmd(String command) {
		logger.info("About to execute '{}' in a new system process", command);
		String s = null;

		try {

			Process p = Runtime.getRuntime().exec(command);

			BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));

			BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));

			// read the output from the command
			JythonServerFacade.getInstance().print("Here is the standard output of the command:\n");
			while ((s = stdInput.readLine()) != null) {
				JythonServerFacade.getInstance().print(s);
			}

			JythonServerFacade.getInstance().print("\n");

			// read any errors from the attempted command
			JythonServerFacade.getInstance().print("Here is the standard error of the command (if any):\n");
			while ((s = stdError.readLine()) != null) {
				JythonServerFacade.getInstance().print(s);
			}
		} catch (IOException e) {
			JythonServerFacade.getInstance().print(e.getMessage() + " or command not found.");
		}
	}
}