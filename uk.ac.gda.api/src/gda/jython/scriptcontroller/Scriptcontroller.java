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
import gda.factory.Reconfigurable;
import gda.observable.IObservable;

/**
 * A interface for a distributed class which runs a Jython script. This class is distributed and IObservable so that GUI
 * object can receive messages back from the running script to inform the user of progress.
 * <p>
 * To run the script, the GUI should run the command returned by the getCommand method using the JythonServerFacade
 * object. The GUI should be set to IObserve this Scriptcontroller, which will then pass messages from the script to its
 * observers via CORBA messaging.
 * <p>
 * It is recommended that the script sends progress messages back to this object's IObserver in the form of
 * gda.jython.scriptcontroller.event classes.
 * <p>
 * If parameters need to be given to the script, a serializable object should be created of the type the script is
 * expecting and placed into the Jython namespace under the name return by this objects getParametersName method. The
 * object is placed into the Jython namespace using the JythonServerFacade object.
 * <p>
 * The import command is the command to run when this object is configured so that the script that this object controls
 * is loaded into the Jython namespace
 */
public interface Scriptcontroller extends IObservable, Findable, Reconfigurable {
	
	public static final String SCRIPT_RUNNER_START_MARKER = "ScriptRunner started";
	public static final String SCRIPT_RUNNER_RUNNING_MARKER = "ScriptRunner running";
	public static final String SCRIPT_RUNNER_END_MARKER = "ScriptRunner ended";
	public static final String SCRIPT_RUNNER_ERROR_MARKER = "ScriptRunner error";
	
	/**
	 * Returns the command that would be run by the runScript command. This should be a function in a Jython file placed
	 * in one of the directories defined by the command server's script_paths bean.
	 * 
	 * @return a Jython command which starts the script this object represents
	 */
	public String getCommand();

	/**
	 * Sets the command which would be run by the runScript command.
	 * 
	 * @param scriptName
	 */
	public void setCommand(String scriptName);

	/**
	 * Returns the name of the object which is used to pass parameters to the script.
	 * 
	 * @return the name of an object in the Jython namespace
	 */
	public String getParametersName();

	/**
	 * Sets the name of the object in the Jython namepsace which the script and this object uses as a mechanism to pass
	 * parameters to the script.
	 * <p>
	 * This object must be serializable.
	 * 
	 * @param parametersName
	 */
	public void setParametersName(String parametersName);

	/**
	 * Returns the command which should be run to load the script into the Jython namespace.
	 * 
	 * @return the command to load the script
	 */
	public String getImportCommand();

	/**
	 * Sets the import command.
	 * 
	 * @param command
	 */
	public void setImportCommand(String command);
}
