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

import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.python.core.Py;
import org.python.core.PyObject;
import org.python.core.PyStringMap;
import org.python.core.PyTuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.scannable.JythonScannableWrapper;
import gda.device.scannable.ScannableUtils;
import gda.device.scannable.scannablegroup.IScannableGroup;
import gda.device.scannable.scannablegroup.ScannableGroup;
import gda.factory.FactoryException;
import gda.factory.Findable;
import gda.factory.Finder;
import gda.jython.GdaJythonBuiltin;
import gda.jython.ITerminalPrinter;
import gda.jython.InterfaceProvider;
import gda.jython.JythonServer;
import gda.jython.JythonServerFacade;
import gda.jython.ScriptBase;
import gda.jython.batoncontrol.ClientDetails;
import gda.jython.scriptcontroller.Scriptcontroller;
import gda.observable.IObservable;
import gda.observable.IObserver;

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
	@GdaJythonBuiltin(docstring="Print the GDA specific help")
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
	@GdaJythonBuiltin(overload="List all the interfaces of all the objects available from the finder.\n"
			+ "(Except a selection of common ones)")
	public static void ls() {
		ls("");
	}

	/**
	 * Lists all the available types of objects or objects of a given type
	 *
	 * @param interfaceName
	 */
	@GdaJythonBuiltin(overload="List available types of object in the namespace.\n"
			+ "Argument must be None, 'all' or empty string.")
	public static void ls(String interfaceName) {

		if (interfaceName == null || interfaceName.compareTo("") == 0 || interfaceName.compareTo("all") == 0) {
			List<String> availableInterfaces = Finder.listAllInterfaces();

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
			availableInterfaces.remove("Metadata");
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
			InterfaceProvider.getTerminalPrinter().print(output);
		}
	}

	/**
	 * List all the instances of a particular type (interface) of object
	 *
	 * @param theInterface
	 */
	@GdaJythonBuiltin(overload="List all the Objects of the given type in the Jython namespace")
	public static <F extends Findable> void ls(Class<F> theInterface) {

		final Map<String, F> objectsOfType = Finder.findSingleton(JythonServer.class).getAllObjectsOfType(theInterface);
		ScannableCommands.removeScannablesInGroups(objectsOfType);

		StringBuilder output = new StringBuilder("\n");
		for (Entry<String, F> entry : objectsOfType.entrySet()) {
			if (entry.getValue() instanceof IScannableGroup) {
				output.append(ScannableUtils.prettyPrintScannableGroup((ScannableGroup) entry.getValue()));
			} else if (entry.getValue() instanceof Scannable) {
				output.append(((Scannable) entry.getValue()).toFormattedString());
			} else {
				output.append(entry.getValue().toString());
			}
			output.append("\n");
		}
		InterfaceProvider.getTerminalPrinter().print(output.toString());
	}

	/**
	 * List the names of all Scannables whose name does not startwith __
	 */
	@GdaJythonBuiltin(overload="List all the scannables in the namespace")
	public static void ls_names() {
		ls_names(Scannable.class);
	}
	/**
	 * List all the instances of a particular type (interface) of object which are also Findable
	 *
	 * @param theInterface
	 */
	@GdaJythonBuiltin(overload="List all the Findables in the namespace that are instances of\n"
			+ "the given interface")
	public static <F extends Findable> void ls_names(Class<F> theInterface) {
		StringBuilder output = new StringBuilder("\n");
		Collection<String> names = Finder.findSingleton(JythonServer.class).getAllObjectsOfType(theInterface).keySet();
		output.append(names.stream().filter(name -> !name.startsWith("__")).collect(Collectors.joining("\n")));
		InterfaceProvider.getTerminalPrinter().print(output.toString());
	}

	/**
	 * To allow the list function to be used all the way to the bottom of the object tree.
	 *
	 * @param theScannable
	 */
	@GdaJythonBuiltin(overload="Print the name of a scannable")
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
	 * @throws InterruptedException
	 */
	@GdaJythonBuiltin(docstring="Check if the script has been paused and wait if it has")
	public static void pause() throws InterruptedException {
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
	@GdaJythonBuiltin(docstring="Run a script from one of the configured script projects.\n"
			+ "Absolute script paths are also accepted.")
	public static void run(String scriptName) throws Exception {
		// NOTE: ideally this method would try the entire python sys.path, but this would
		//       require making a breaking change and possibly be overkill!
		JythonServer server = Finder.findSingleton(JythonServer.class);

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

	@GdaJythonBuiltin(docstring="Add a runnable to be executed before the namespace is reset.\n"
			+ "Can be used for cleaning up resources/deregistering listeners etc.")
	public static void add_reset_hook(Runnable hook) {
		logger.info("Adding reset hook to JythonServer");
		Finder.findSingleton(JythonServer.class).addResetHook(hook);
	}

	/**
	 * Jython utility method for adding objects as observers within a Jython namespace lifetime
	 *
	 * <p>If a Jython object is added as an observer of a Java object on the server, the
	 * reference lives through the interpreter being recreated
	 * (via {@link #reset_namespace}). This often means the observer cannot be removed as
	 * there are no references accessible from the Jython shell and
	 * {@link IObservable#deleteIObservers()} has the risk of removing other observers that
	 * are still required.</p>
	 * <p>This adds the observer and also adds a reset hook so that the observer is removed when
	 * the namespace is reset.</p>
	 * @param observer The Jython object to add as an observer
	 * @param observable The object to observe
	 */
	@GdaJythonBuiltin(docstring="Add an observer to observable.\n"
			+ "Adds a an observer while also registering a reset hook so that"
			+ "the observer is removed when the Jython namespace is reset."
			+ "Prevents Jython observers accumulating after multiple resets.")
	public static void add_jython_observer(IObserver observer, IObservable observable) {
		observable.addIObserver(observer);
		add_reset_hook(() -> observable.deleteIObserver(observer));
	}

	/**
	 * Restarts the Jython server without a need for a GDA restart. The Jython namespace is completely reset and
	 * localStation re-run. However, connections to hardware are not re-established i.e. Object Servers are not
	 * reconfigured.
	 */
	@GdaJythonBuiltin(docstring="Reset the Jython environment")
	public static void reset_namespace() {
		logger.info("Resetting Jython namespace");
		Finder.findSingleton(JythonServer.class).restart();
		reconfigureScriptControllers();
		reconnectJythonScannableWrappers();
	}

	private static void reconnectJythonScannableWrappers() {
		Map<String, JythonScannableWrapper> jythonScannableWrappers = Finder.getFindablesOfType(JythonScannableWrapper.class);
		jythonScannableWrappers.values().stream().forEach(JythonScannableWrapper::connectScannable);
	}

	private static void reconfigureScriptControllers() {
		Map<String, Scriptcontroller> scriptControllers = Finder.getFindablesOfType(Scriptcontroller.class);
		for (Scriptcontroller sc : scriptControllers.values()) {
			logger.debug("Reconfiguring '{}' script controller", sc.getName());
			try {
				sc.reconfigure();
			} catch (FactoryException ex) {
				logger.error("Could not reconfigure '{}' script controller", sc.getName(), ex);
			}
		}
	}

	/**
	 * Add a new aliased command.
	 *
	 * @param commandName
	 */
	@GdaJythonBuiltin(docstring="Add a command as an alias\n"
			+ "This allows it to be called without the parentheses, eg\n"
			+ "    >>> def add(a, b): return a + b\n"
			+ "    ... \n"
			+ "    >>> alias('add')\n"
			+ "    >>> add 1 3\n"
			+ "    4\n"
			+ "    >>>",
			overload="String should be the name of the function to be aliased")
	public static void alias(String commandName) {
		Objects.requireNonNull(commandName, "Aliased command cannot be null");
		JythonServerFacade.getInstance().addAliasedCommand(commandName);
	}

	/**
	 * Add a new aliased command.
	 *
	 * @param callable
	 * @throws DeviceException
	 */
	@GdaJythonBuiltin(overload="PyObject is the callable to be aliased and should be referenced\n"
			+ "by a single name in the namespace.")
	public static void alias(PyObject callable) throws DeviceException {
		alias(aliasName(callable));
	}

	/**
	 * Add a new vararg aliased command
	 *
	 * @param commandName
	 */
	@GdaJythonBuiltin(docstring="Add a command as an alias\n"
			+ "This allows it to be called without the parentheses, eg\n"
			+ "    >>> def add(a): return sum(a)\n"
			+ "    ... \n"
			+ "    >>> vararg_alias('add')\n"
			+ "    >>> add 1 2 3 4 5\n"
			+ "    15\n"
			+ "    >>>"
			+ "\n"
			+ "This version differs from alias in that it is intended for functions that\n"
			+ "accept a single (non-vararg) sequence argument (eg list or array), and is not\n"
			+ "for functions that accept vararg arguments already.",
				overload="String should be the name of the function to be aliased")
	public static void vararg_alias(String commandName) {
		Objects.requireNonNull(commandName, "Aliased command cannot be null");
		JythonServerFacade.getInstance().addAliasedVarargCommand(commandName);
	}

	/**
	 * Add a new vararg aliased command
	 *
	 * @param callable
	 * @throws DeviceException
	 */
	@GdaJythonBuiltin(overload="PyObject is the callable to be aliased and should be referenced\n"
			+ "by a single name in the namespace.")
	public static void vararg_alias(PyObject callable) throws DeviceException {
		vararg_alias(aliasName(callable));
	}

	@GdaJythonBuiltin(docstring="Remove an aliased command. This removes commands aliased using either\n"
			+ "alias or vararg_alias",
			overload="String should be the name of an aliasedfunction.")
	public static void remove_alias(String command) {
		JythonServerFacade.getInstance().removeAliasedCommand(command);
	}

	@GdaJythonBuiltin(overload="PyObject is the aliased callable to be removed and should be referenced\n"
			+ "by a single name in the namespace.")
	public static void remove_alias(PyObject callable) throws DeviceException {
		remove_alias(aliasName(callable));
	}

	/** Get the name that an object should be aliased as */
	@SuppressWarnings("unchecked")
	private static String aliasName(PyObject callable) throws DeviceException {
		if (callable == null || callable == Py.None) {
			throw new IllegalArgumentException("Can't alias None");
		}
		if (!callable.isCallable()) {
			throw new IllegalArgumentException(callable + " can't be aliased");
		}
		// Get all the names in the global namespace that reference this object
		final Set<String> names = JythonServerFacade.getInstance().getAllNamesForObject(callable);
		// add builtin functions
		names.addAll(stream(((Iterable<PyObject>)((PyStringMap)Py.getSystemState()
						.getBuiltins()).iteritems()).spliterator(), false)
				.map(PyTuple.class::cast)
				.filter(t -> t.__getitem__(1) == callable)
				.map(t -> t.__getitem__(0))
				.map(PyObject::toString)
				.filter(name -> !("_".equals(name)))
				.collect(toList()));
		switch (names.size()) {
		case 0:
			throw new IllegalArgumentException(callable + " does not exist in namespace");
		case 1:
			return names.iterator().next();
		default:
			throw new IllegalArgumentException(callable + " is referenced by multiple names ("
					+ String.join(", ",  names)
					+ ") - use the required name to create alias");
		}
	}

	/**
	 * runs system command which input as string
	 *
	 * @param command
	 */
	@GdaJythonBuiltin(docstring="Run a system command")
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

	@GdaJythonBuiltin(docstring="Show details of a user from a given client index")
	public static void whois(int id) {
		ITerminalPrinter printer = InterfaceProvider.getTerminalPrinter();
		if (id == 0) {
			printer.print("Server User");
		} else {
			for (ClientDetails cd : InterfaceProvider.getBatonStateProvider().getOtherClientInformation()) {
				if (cd.getIndex() == id) {
					printer.print(cd.toString());
					return;
				}
			}
			printer.print("User not recognised");
		}
	}
}
