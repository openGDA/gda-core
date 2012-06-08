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

package gda.jython.scriptcontroller;

import gda.factory.Findable;
import gda.factory.Finder;
import gda.jython.IJythonNamespace;
import gda.jython.IScanStatusHolder;
import gda.jython.IScriptController;
import gda.jython.InterfaceProvider;
import gda.jython.gui.JythonGuiConstants;
import gda.observable.IObserver;

import java.io.File;
import java.io.Serializable;
import java.util.Map;

/**
 * Class to wrap up commands to control a ScriptController
 * <p>
 * It is recommended that the script sends progress messages back to this object's IObserver in the form of
 * gda.jython.scriptcontroller.event classes.
 */
public final class ScriptExecutor implements IObserver {

	/**
	 * Script name to use when no script should be run.
	 */
	public static final String NO_SCRIPT = "";

	/**
	 * Panel name to use when no panel should be notified.
	 */
	public static final String NO_PANEL = "";

	private ScriptExecutor() {
	}

	private void PrintToTerminal(String string) {
		InterfaceProvider.getTerminalPrinter().print(string);
	}

	/**
	 * Method calls by the script controller. If the changeCode is a String the value is sent to the jythonServerFacade
	 * which will display the string on the terminal
	 * 
	 * @param changeCode
	 *            The object being sent by the caller. We only act on String objects {@inheritDoc}
	 * @see gda.observable.IObserver#update(java.lang.Object, java.lang.Object)
	 */
	@Override
	public void update(Object theObserved, Object changeCode) {
		if (theObserved instanceof Scriptcontroller && changeCode instanceof String) {
			PrintToTerminal((String) changeCode);
		}
	}

	/**
	 * Starts a script
	 * 
	 * @param scriptControllerName
	 *            The name of the ScriptController on the ObjectServer. The ScriptController is used as a mechanism for
	 *            communicating between the running script and any observers of the ScriptController.
	 * @param anIObserver
	 *            The object to be notified by the named ScriptController. This allows the ScriptController to inform
	 *            the object of status. If this is null the function creates a ScriptExecutor and uses that as the
	 *            observer - this causes any Strings sent by the ScriptController to be used in a call to
	 *            jythonServerFacade.print
	 * @param jythonObjects
	 *            A map of objects to be put into the Jython namespace. Such objects can then be accessed by name within
	 *            scripts. Note that such objects must support Serializable however there are further restrictions due
	 *            to the Corba implementation. One such restriction is that the class cannot contain <code>enum</code>.
	 *            To get around such a problem one can defined a method for such a class that creates a conforming
	 *            Serializable object which itself has a method to create the non-conforming object. The Jython
	 *            namespace will contain the conforming object but the script can simply call that object's method to
	 *            create the non-conforming object.
	 * @param scriptToRun
	 *            The path of the script file to be executed. This passed to the function jythonServerFacade.runScript
	 * @param panelToBeNotified
	 *            The name of the panel to be sent ScanDataPoint data generated within the script.
	 * @return Returns either the anIObserver parameter supplied or, if this is null, the ScriptExecutor object created.
	 */
	static public IObserver Start(String scriptControllerName, IObserver anIObserver,
			Map<String, Serializable> jythonObjects, String scriptToRun, String panelToBeNotified) {
		return Start(scriptControllerName, anIObserver, jythonObjects, scriptToRun, panelToBeNotified, false, false);
	}

	/**
	 * @param scriptControllerName
	 * @param anIObserver
	 * @param jythonObjects
	 * @param scriptToRun
	 * @param panelToBeNotified
	 * @param allowMultipleScripts
	 * @param scriptIsCommand
	 * @return IObserver
	 * @throws IllegalArgumentException
	 */
	static public IObserver Start(String scriptControllerName, IObserver anIObserver,
			Map<String, Serializable> jythonObjects, String scriptToRun, String panelToBeNotified,
			boolean allowMultipleScripts, boolean scriptIsCommand) throws IllegalArgumentException {

		final SetupRunResponse setupRunResponse = ScriptExecutor.setupRun(scriptControllerName, anIObserver,
				jythonObjects, allowMultipleScripts);
		if (scriptIsCommand) {
			if (scriptToRun == null)
				throw new IllegalArgumentException("ScriptExecutor. scriptIsCommand but  scriptToRun == null");
			InterfaceProvider.getCommandRunner().runCommand(scriptToRun);
		} else {
			String script = null;
			if (scriptToRun != null && !scriptToRun.equals("")) {
				script = scriptToRun;
			} else {
				script = InterfaceProvider.getCommandRunner().locateScript(
						setupRunResponse.getScriptController().getCommand());
			}

			if (script == null || script.equals("")) {
				throw new IllegalArgumentException("ScriptExecutor: script is null or empty");
			}
			File scriptFile = new File(script);
			if (!scriptFile.canRead()) {
				throw new IllegalArgumentException("ScriptExecutor: unable to read script file " + script);
			}
			InterfaceProvider.getCommandRunner().runScript(scriptFile, panelToBeNotified);
		}
		return setupRunResponse.getAnIObserver();
	}

	/**
	 * Runs the command and blocks until the command is through. Therefore it also removes the IObserver after the
	 * command is run.
	 * 
	 * @param scriptControllerName
	 * @param anIObserver
	 * @param jythonObjects
	 * @param command
	 * @param observerName
	 *            can be null
	 * @return true if command was not complete and more input is required.
	 */
	static public boolean Run(final String scriptControllerName, final IObserver anIObserver,
			final Map<String, Serializable> jythonObjects, final String command, String observerName) {

		if (anIObserver == null)
			throw new NullPointerException("Field anIObserver cannot be null");

		SetupRunResponse setupRunResponse = ScriptExecutor.setupRun(scriptControllerName, anIObserver, jythonObjects,
				false);
		try {
			if (observerName == null)
				observerName = JythonGuiConstants.TERMINALNAME;
			return InterfaceProvider.getCommandRunner().runsource(command, observerName);
		} finally {
			setupRunResponse.getScriptController().deleteIObserver(anIObserver);
		}
	}

	static public void setupNameSpace(final Map<String, Serializable> jythonObjects){
		IJythonNamespace jythonNamespace = InterfaceProvider.getJythonNamespace();
		if (jythonObjects != null) {
			String key = "";
			for (Map.Entry<String, Serializable> pair : jythonObjects.entrySet()) {
				key = pair.getKey();
				if (key != null)
					jythonNamespace.placeInJythonNamespace(key, pair.getValue());
			}
		}
	}
	static private final SetupRunResponse setupRun(final String scriptControllerName, IObserver anIObserver,
			final Map<String, Serializable> jythonObjects, final boolean allowMultipleScripts) {

		if (scriptControllerName == null || scriptControllerName.equals("")) {
			throw new IllegalArgumentException("ScriptExecutor: ScriptControllerName is null or empty");
		}

		IScanStatusHolder scanStatusHolder = InterfaceProvider.getScanStatusHolder();
		IScriptController scriptController2 = InterfaceProvider.getScriptController();
		if (!allowMultipleScripts && (scriptController2.getScriptStatus() == gda.jython.Jython.RUNNING)) {
			throw new RuntimeException("ScriptExecutor:  JythonServerFacade is busy running a script");
		}
		if ((scanStatusHolder.getScanStatus() == gda.jython.Jython.RUNNING)) {
			throw new RuntimeException("ScriptExecutor:  JythonServerFacade is busy running a scan");
		}
		Findable findable = Finder.getInstance().find(scriptControllerName);

		if (findable == null || !(findable instanceof Scriptcontroller)) {
			throw new IllegalArgumentException("ScriptExecutor: " + scriptControllerName
					+ " is not the name of a findable Scriptcontroller ");
		}

		Scriptcontroller scriptController = (Scriptcontroller) findable;

		if (anIObserver == null) {
			anIObserver = new ScriptExecutor();
		}

		scriptController.addIObserver(anIObserver);
		setupNameSpace(jythonObjects);

		return new SetupRunResponse(scriptController, anIObserver);
	}

	/**
	 * Removes an IObserver object from the list of IObservers of a named ScriptController
	 * 
	 * @param scriptControllerName
	 *            The name of the ScriptController on the ObjectServer. The ScriptController is used as a mechanism for
	 *            communicating between the running script and any observers of the ScriptController.
	 * @param anIObserver
	 *            The IObserver to be removed from the list of IObservers of the named ScriptController
	 */
	static public void DeleteIObserver(String scriptControllerName, IObserver anIObserver) {

		if (anIObserver == null)
			return;

		if (scriptControllerName == null || scriptControllerName.equals("")) {
			throw new IllegalArgumentException("ScriptExecutor: ScriptControllerName is null or empty");
		}

		Findable findable = Finder.getInstance().find(scriptControllerName);

		if (findable == null || !(findable instanceof Scriptcontroller)) {
			throw new IllegalArgumentException("ScriptExecutor: " + scriptControllerName
					+ " is not the name of a findable Scriptcontroller ");
		}

		Scriptcontroller scriptController = (Scriptcontroller) findable;

		scriptController.deleteIObserver(anIObserver);

	}

	/**
	 * Halts the current script being run by JythonServerFacade and removes an IObserver object from the list of
	 * IObservers of a named ScriptController
	 * 
	 * @param scriptControllerName
	 *            The name of the ScriptController on the ObjectServer. The ScriptController is used as a mechanism for
	 *            communicating between the running script and any observers of the ScriptController.
	 * @param anIObserver
	 *            If not null, the IObserver to be removed from the list of IObservers of the named ScriptController.
	 */
	static public void Stop(String scriptControllerName, IObserver anIObserver) {

		if (scriptControllerName == null || scriptControllerName.equals("")) {
			throw new IllegalArgumentException("ScriptExecutor: ScriptControllerName is null or empty");
		}

		Findable findable = Finder.getInstance().find(scriptControllerName);

		if (findable == null || !(findable instanceof Scriptcontroller)) {
			throw new IllegalArgumentException("ScriptExecutor: " + scriptControllerName
					+ " is not the name of a findable Scriptcontroller ");
		}

		InterfaceProvider.getScriptController().haltCurrentScript();

		Scriptcontroller scriptController = (Scriptcontroller) findable;

		if (anIObserver != null) {
			scriptController.deleteIObserver(anIObserver);
		}
	}

}

class SetupRunResponse {

	private final Scriptcontroller scriptController;
	private final IObserver anIObserver;

	public SetupRunResponse(Scriptcontroller scriptController, IObserver anIObserver) {
		this.scriptController = scriptController;
		this.anIObserver = anIObserver;
	}

	/**
	 * @return Returns the scriptController.
	 */
	public Scriptcontroller getScriptController() {
		return scriptController;
	}

	/**
	 * @return Returns the anIObserver.
	 */
	public IObserver getAnIObserver() {
		return anIObserver;
	}

}
