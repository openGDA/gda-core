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

import uk.ac.diamond.daq.util.logging.deprecation.DeprecationLogger;

/**
 * Input Class - Deprecated. Use InputCommands instead. But used in Scripts so must remain until all scripts are changed.
 */
public class Input {
	private static final DeprecationLogger logger = DeprecationLogger.getLogger(Input.class);
	/**
	 * For use within scripts to request input from the Jython terminal
	 *
	 * @param promptString *
	 * @return Object
	 * @throws InterruptedException
	 * @Deprecated Use {@link InputCommands#requestInput(String)} instead
	 */
	@Deprecated(since = "9.29", forRemoval = true)
	public static Object requestInput(String promptString) throws InterruptedException {
		logger.deprecatedMethod("requestInput(String)", "9.31", "InputCommands#requestInput(String) (or raw_input(str) if running from Jython)");
		return InputCommands.requestInput(promptString);
	}
}
