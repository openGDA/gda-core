/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.mapping.ui.experiment;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.State;
import org.eclipse.ui.handlers.RegistryToggleState;

public class PlottingUtils {

	private PlottingUtils() {
		// prevent instantiation
	}

	/**
	 * Set the toggle state of a command to the given value
	 * <p>
	 * Similar to <code>HandlerUtil.toggleCommandState()</code> but sets a defined state rather than toggling the
	 * existing state.
	 *
	 * @param command
	 *            the command to change
	 * @param newState
	 *            the boolean state to set
	 * @throws ExecutionException
	 *             if the command does not have a toggle state, or it is not boolean
	 */
	static void setCommandState(Command command, boolean newState) throws ExecutionException {
		final State state = command.getState(RegistryToggleState.STATE_ID);
		if (state == null) {
			throw new ExecutionException("The command does not have a toggle state");
		}
		if (!(state.getValue() instanceof Boolean)) {
			throw new ExecutionException("The command's toggle state doesn't contain a boolean value");
		}
		state.setValue(Boolean.valueOf(newState));
	}
}
