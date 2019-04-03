/*-
 * Copyright © 2018 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.client.gui.exitslit.configuration;

import static uk.ac.diamond.daq.client.gui.exitslit.configuration.ConfigureExitSlitsUtils.displayError;

import java.util.Objects;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.factory.Finder;

/**
 * Handler to display a dialogue guiding the user through exit slit configuration
 */
public class ConfigureExitSlitsHandler extends AbstractHandler {
	private static final Logger logger = LoggerFactory.getLogger(ConfigureExitSlitsHandler.class);

	/**
	 * Name of bean containing parameters to pass to the dialog
	 */
	private static final String EXIT_SLIT_CONFIG_PARAMS = "exit_slit_config_params";

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			final Shell activeShell = Display.getCurrent().getActiveShell();
			final ConfigureExitSlitsParameters params = Finder.getInstance().find(EXIT_SLIT_CONFIG_PARAMS);
			Objects.requireNonNull(params, "No parameter bean '" + EXIT_SLIT_CONFIG_PARAMS + "' found");

			final ConfigureExitSlitsDialog dialog = new ConfigureExitSlitsDialog(activeShell, params);
			dialog.create();
			dialog.open();
		} catch (Exception e) {
			displayError("Error configuring exit slits", "Cannot open exit slit configuration dialog", e, logger);
		}
		return null;
	}
}
