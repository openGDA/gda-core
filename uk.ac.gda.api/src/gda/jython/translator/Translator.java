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

import gda.jython.AliasedCommandProvider;

/**
 * The interface that supports GDA extended jython syntax.  It allows dynamically adding special keyword into GDA at
 * runtime and translate these keywords related syntax to corresponding Python syntax (for example functions/methods)
 * before execution.
 *
 * <p>
 * All classes should return the original string in the event of an error during the translation, so that the Jython may
 * create a syntax error message.
 */
public interface Translator extends AliasedCommandProvider {
	/**
	 * Translate a command potentially containing GDA extended syntax into the
	 * Jython equivalent. Commands should be passed as one string rather than a
	 * line at a time as multiline constructs may need to be handled differently.
	 * @param originalCommand The full command as entered, possibly using extended syntax
	 * @return Pure Jython equivalent of originalCommand. If original command contains
	 *     other syntax errors, a best effort is made to preserve the errors after translation
	 *     rather than fixing them.
	 */
	public String translate(String originalCommand);

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

	/**
	 * Remove the given command from the held lists of aliases.
	 * This will remove the command from both vararg and non-vararg lists
	 * @param command to remove
	 */
	public void removeAlias(String command);
}
