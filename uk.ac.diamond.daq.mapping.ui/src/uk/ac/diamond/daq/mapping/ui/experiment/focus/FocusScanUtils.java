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

import static gda.configuration.properties.LocalProperties.GDA_INITIAL_LENGTH_UNITS;
import static org.jscience.physics.units.SI.METER;
import static org.jscience.physics.units.SI.MICRO;
import static org.jscience.physics.units.SI.MILLI;
import static org.jscience.physics.units.SI.NANO;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.jscience.physics.quantities.Length;
import org.jscience.physics.units.Unit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.common.collect.ImmutableList;

import gda.configuration.properties.LocalProperties;
import gda.function.ILinearFunction;
import gda.function.LinearFunctionSerializer;
import uk.ac.gda.client.NumberAndUnitsComposite;

/**
 * Utility functions for {@link uk.ac.diamond.daq.mapping.ui.experiment.focus.FocusScanWizard}
 */
public class FocusScanUtils {
	private static final Logger logger = LoggerFactory.getLogger(FocusScanUtils.class);

	private static final Unit<Length> MODEL_LENGTH_UNIT = MILLI(METER);
	private static final List<Unit<Length>> LENGTH_UNITS = ImmutableList.of(MILLI(METER), MICRO(METER), NANO(METER));
	private static final Unit<Length> INITIAL_LENGTH_UNIT = getInitialLengthUnit();

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
		final Path filePath = Paths.get(energyFocusConfigPath).normalize();
		logger.debug("Saving energy focus configuration to {}", filePath);
		logger.debug(energyFocusFunction.getAsString());
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

	/**
	 * Get the initial units (i.e. the units shown in the combo box when it is first displayed)
	 * <p>
	 * This defaults to millimetres but can be set in a property
	 *
	 * @return the initial units
	 */
	@SuppressWarnings("unchecked")
	public static Unit<Length> getInitialLengthUnit() {
		final String unitString = LocalProperties.get(GDA_INITIAL_LENGTH_UNITS, "mm").toLowerCase();
		try {
			final Unit<?> unit = Unit.valueOf(unitString);
			if (unit.isCompatible(MODEL_LENGTH_UNIT)) {
				return (Unit<Length>) unit;
			}
			logger.warn("Value '{}' of property '{}' is not a valid length unit: assuming millimetres", unitString, GDA_INITIAL_LENGTH_UNITS);
			return MODEL_LENGTH_UNIT;
		} catch (Exception e) {
			logger.warn("Cannot parse value '{}' of property '{}': assuming millimetres", unitString, GDA_INITIAL_LENGTH_UNITS);
			return MODEL_LENGTH_UNIT;
		}
	}

	/**
	 * Create a {@link NumberAndUnitsComposite} for length units, assuming model units are mm
	 *
	 * @param parent
	 *            composite
	 * @return a {@link NumberAndUnitsComposite} initialised for length
	 */
	protected static NumberAndUnitsComposite<Length> createNumberAndUnitsLengthComposite(Composite parent) {
		return new NumberAndUnitsComposite<>(parent, SWT.NONE, MODEL_LENGTH_UNIT, LENGTH_UNITS, INITIAL_LENGTH_UNIT);
	}
}
