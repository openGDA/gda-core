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

package uk.ac.diamond.daq.mapping.ui.experiment.focus;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.slf4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import gda.function.ILinearFunction;
import gda.function.LinearFunctionSerializer;

/**
 * Utility functions for {@link uk.ac.diamond.daq.mapping.ui.experiment.focus.FocusScanWizard}
 */
public class FocusScanUtils {

	private static ObjectMapper objectMapper = new ObjectMapper();
	static {
		final SimpleModule module = new SimpleModule();
		module.addSerializer(ILinearFunction.class, new LinearFunctionSerializer());
		objectMapper.registerModule(module);
	}

	private FocusScanUtils() {
		// prevent instantiation
	}

	/**
	 * Save energy/focus configuration parameters to a file
	 *
	 * @param energyFocusFunction
	 *            live function containing parameter values
	 * @param energyFocusConfigPath
	 *            file into which the parameters are to be saved
	 * @param logger
	 *            for logging information or error messages
	 */
	public static void saveConfig(ILinearFunction energyFocusFunction, String energyFocusConfigPath, Logger logger) {
		logger.debug("Saving energy focus configuration to file");
		final Path filePath = Paths.get(energyFocusConfigPath);
		try {
			final String energyFocusConfigJson = objectMapper.writeValueAsString(energyFocusFunction);
			// save to a file in gda_var so it can be picked up by localStation
			filePath.toFile().getParentFile().mkdirs();
			Files.write(filePath, energyFocusConfigJson.getBytes());
		} catch (Exception e) {
			final String message = "Error saving function configuration";
			logger.error(message, e);
			displayError(message, e.getMessage(), logger);
		}
	}

	/**
	 * Log an error and display it to the user in a pop-up message box
	 *
	 * @param text
	 *            message box title
	 * @param message
	 *            message to be logged and displayed in the body of the message box
	 * @param logger
	 *            for logging information or error messages
	 */
	public static void displayError(String text, String message, Logger logger) {
		logger.error(message);
		final MessageBox messageBox = new MessageBox(Display.getCurrent().getActiveShell(), SWT.ICON_ERROR | SWT.OK);
		messageBox.setText(text);
		messageBox.setMessage(message);
		messageBox.open();
	}

	/**
	 * Display a pop-up message box with Yes & No buttons
	 *
	 * @param text
	 *            message box title
	 * @param message
	 *            message to be logged and displayed in the body of the message box
	 * @return <code>true</code> if the user clicks Yes, <code>false</code> if they click No
	 */
	public static boolean displayYesNoMessage(String text, String message) {
		final MessageBox messageBox = new MessageBox(Display.getCurrent().getActiveShell(), SWT.ICON_QUESTION | SWT.YES | SWT.NO);
		messageBox.setText(text);
		messageBox.setMessage(message);
		return messageBox.open() == SWT.YES;
	}
}
