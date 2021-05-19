/*-
 * Copyright © 2021 Diamond Light Source Ltd.
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

package uk.ac.gda.client.livecontrol;

import java.io.IOException;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Subclass of {@link CommandControl} to run the specified command in a Bash shell
 */
public class BashCommandControl extends CommandControl {
	private static final Logger logger = LoggerFactory.getLogger(BashCommandControl.class);

	@Override
	protected void runCommand(String command) {
		logger.debug("Running Bash command: {}", command);
		try {
			Runtime.getRuntime().exec(command);
		} catch (IOException e) {
			final String message = String.format("Error running Bash command: '%s'", command);
			logger.error(message, e);
			MessageDialog.openError(Display.getDefault().getActiveShell(), "Error", message);
		}
	}
}
