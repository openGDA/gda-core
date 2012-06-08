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

package gda.jython.translator;

import java.util.Vector;

/**
 * The interface that supports GDA extended jython syntax.  It allows dynamically adding special keyword into GDA at 
 * runtime and translate these keywords related syntax to corresponding Python syntax (for example functions/methods)
 * before execution.
 * 
 * <p>
 * All classes should return the original string in the event of an error during the translation, so that the Jython may
 * create a syntax error message.
 */
public interface Translator {
	/**
	 * The method called by GDAJythonInterpreter to perform the translation of any line of Jython
	 * 
	 * @param original_command
	 *            String
	 * @return String
	 */
	public String translate(String original_command);

	/**
	 * Called from within the translate method. This assumes that the original_command is a single, self contained
	 * jython command - no line breaks or ;'s.
	 * 
	 * @param original_command
	 *            String
	 * @return String
	 */
	public String translateGroup(String original_command);

	/**
	 * Returns a string describing the commands the class responds to.
	 * 
	 * @return String
	 */
	public String getHelpMessage();

	/**
	 * Adds a new aliased command.
	 * 
	 * @param commandName
	 */
	public void addAliasedCommand(String commandName);

	/**
	 * Adds a new variable argument aliased command.
	 * 
	 * @param commandName
	 */
	public void addAliasedVarargCommand(String commandName);
	
	public Vector<String> getAliasedCommands();
	
	public Vector<String> getAliasedVarargCommands();

}
