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

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.slf4j.Logger;

import com.google.common.collect.ImmutableMap;

public class ConfigureExitSlitsUtils {
	private static final String PLUGIN_ID = "uk.ac.diamond.daq.client.gui.exitslit.configuration";

	public static final String MOTOR_ERROR = "Motor error";

	// Minimum update frequency of scannables (in milliseconds)
	public static final int SCANNABLE_UPDATE_PERIOD = 200;

	public static final Color COLOUR_WHITE = Display.getCurrent().getSystemColor(SWT.COLOR_WHITE);
	public static final Color COLOUR_RED = Display.getCurrent().getSystemColor(SWT.COLOR_RED);

	public static final ImmutableMap<String, Integer> SHUTTER_COLOUR_MAP = ImmutableMap.of(
		"Open", SWT.COLOR_DARK_GREEN,
		"Opening", SWT.COLOR_DARK_YELLOW,
		"Closed", SWT.COLOR_RED,
		"Closing", SWT.COLOR_DARK_YELLOW,
		"Reset", SWT.COLOR_DARK_YELLOW);

	public static final ImmutableMap<String, Integer> DIAGNOSTIC_COLOUR_MAP = ImmutableMap.of(
		"Screen", SWT.COLOR_DARK_GREEN,
		"Out", SWT.COLOR_RED,
		"Photodiode", SWT.COLOR_RED);

	private ConfigureExitSlitsUtils() {
		// Prevent instantiation
	}

	public static Composite createComposite(Composite parent, int numColumns) {
		final Composite composite = new Composite(parent, SWT.NONE);
		GridDataFactory.swtDefaults().applyTo(composite);
		GridLayoutFactory.swtDefaults().numColumns(numColumns).applyTo(composite);
		composite.setBackground(COLOUR_WHITE);
		return composite;
	}

	public static Button createButton(Composite parent, String text, String toolTipText) {
		final Button button = new Button(parent, SWT.PUSH);
		GridDataFactory.swtDefaults().applyTo(button);
		button.setText(text);
		button.setToolTipText(toolTipText);
		return button;
	}

	public static Button createStopButton(Composite parent) {
		final Button btnStop = new Button(parent, SWT.PUSH);
		btnStop.setToolTipText("Stop motors");
		btnStop.setImage(AbstractUIPlugin.imageDescriptorFromPlugin(getPluginId(), "icons/stop.png").createImage());
		btnStop.setEnabled(false);
		return btnStop;
	}

	public static Label createLabel(Composite parent, String text) {
		final Label label = new Label(parent, SWT.NONE);
		GridDataFactory.swtDefaults().applyTo(label);
		label.setText(text);
		label.setBackground(COLOUR_WHITE);
		return label;
	}

	public static Button createCheckBox(Composite parent, String text) {
		final Button checkBox = new Button(parent, SWT.CHECK);
		checkBox.setText(text);
		checkBox.setBackground(COLOUR_WHITE);
		return checkBox;
	}

	public static void createSeparator(Composite parent) {
		final Label separator = new Label(parent, SWT.HORIZONTAL | SWT.SEPARATOR);
		GridDataFactory.fillDefaults().applyTo(separator);
	}

	public static void displayError(String text, String message, Exception ex, Logger logger) {
		logger.error(message, ex);
		final MessageBox messageBox = new MessageBox(Display.getCurrent().getActiveShell(), SWT.ICON_ERROR | SWT.OK);
		messageBox.setText(text);
		messageBox.setMessage(String.format("%s: %s", message, ex.getMessage()));
		messageBox.open();
	}

	public static String getPluginId() {
		return PLUGIN_ID;
	}
}
