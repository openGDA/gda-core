/*-
 * Copyright © 2020 Diamond Light Source Ltd.
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.swtdesigner.SWTResourceManager;

import gda.jython.InterfaceProvider;
import uk.ac.diamond.daq.concurrent.Async;

/**
 * View to show and change the enabled state of a set of watchdogs and to enable/disable each one.
 * <p>
 * The view polls the state of the corresponding watchdogs, so will update the GUI if changes are made on the command
 * line.
 * </p>
 */
public class WatchdogControl extends LiveControlBase {
	private static final Logger logger = LoggerFactory.getLogger(WatchdogControl.class);
	private static final int NUM_COLUMNS = 2;
	private static boolean watchdogServiceImportNeeded = true;

	private static final String ENABLED_COMMAND = "watchdogService.getWatchdog(\"%s\").isEnabled()";

	/**
	 * Command to check if the watchdog is enabled, currently in a scan and currently pausing that scan,
	 */
	private static final String PAUSING_COMMAND = "watchdogService.getWatchdog(\"%s\").isPausing()";

	/**
	 * Frequency (in seconds) of polling watchdog states
	 */
	private int pollingFrequency = 2;

	/**
	 * Names of the watchdogs to be shown in this view
	 */
	private List<String> watchdogNames = Collections.emptyList();

	/**
	 * Check boxes to show the state of each watchdog
	 */
	private List<Button> watchdogCheckBoxes;

	/**
	 * Future to control the polling of watchdog states, specifically to allow the polling to be cancelled.
	 */
	private ScheduledFuture<?> pollingFuture;

	@Override
	public void createControl(Composite composite) {
		final Composite mainComposite = new Composite(composite, SWT.NONE);
		mainComposite.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));
		mainComposite.setBackgroundMode(SWT.INHERIT_FORCE);
		GridLayoutFactory.fillDefaults().numColumns(NUM_COLUMNS).applyTo(mainComposite);

		watchdogCheckBoxes = new ArrayList<>(watchdogNames.size());
		for (String watchdogName : watchdogNames) {
			try {
				final Button watchdogCheckbox = new Button(mainComposite, SWT.CHECK);
				GridDataFactory.swtDefaults().applyTo(watchdogCheckbox);
				watchdogCheckbox.setText(watchdogName);
				watchdogCheckbox.setData(watchdogName);
				watchdogCheckbox.addSelectionListener(SelectionListener.widgetSelectedAdapter(this::handleSelectionEvent));
				watchdogCheckBoxes.add(watchdogCheckbox);
			} catch(Exception e) {
				logger.warn("Error creating control for watchdog {}", watchdogName, e);
			}
		}

		pollingFuture = Async.scheduleWithFixedDelay(this::updateSelectionStates, 0, pollingFrequency, TimeUnit.SECONDS);
	}

	private void updateSelectionStates() {
		Display.getDefault().asyncExec(() -> watchdogCheckBoxes.stream()
				.filter(w -> !w.isDisposed())
				.forEach(this::updateState));
	}

	private void updateState(Button watchdogCheckbox) {
		final String watchdogName = watchdogCheckbox.getText();
		final String isEnabledResult = evaluateWatchdogCommand(watchdogName, ENABLED_COMMAND);
		final String isPausingResult = evaluateWatchdogCommand(watchdogName, PAUSING_COMMAND);
		if (isEnabledResult != null && isPausingResult != null) {
			final boolean isEnabled = isEnabledResult.equals("True");
			final boolean isPausing = isPausingResult.equals("True");
			watchdogCheckbox.setSelection(isEnabled);
			String tooltipText = watchdogName + " is ";
			tooltipText += isEnabled ? "Enabled": "Disabled";
			tooltipText += isPausing ? " and pausing the scan" : "";
			watchdogCheckbox.setToolTipText(tooltipText);
		}
	}

	private void ensureWatchdogServiceImported() {
		if (watchdogServiceImportNeeded) {
			InterfaceProvider.getCommandRunner().runCommand("from gdascripts.watchdogs.watchdogs import watchdogService");
			watchdogServiceImportNeeded = false;
		}
	}

	/**
	 * When a checkbox is checked/unchecked, enable/disable the corresponding watchdog on the server
	 *
	 * @param e
	 *            the system event associated with (un)checking the checkbox
	 */
	private void handleSelectionEvent(SelectionEvent e) {
		ensureWatchdogServiceImported();
		final Button button = (Button) e.getSource();
		final String watchdogName = (String) button.getData();
		final String selected = button.getSelection() ? "True" : "False";
		final String command = String.format("watchdogService.getWatchdog(\"%s\").setEnabled(%s)", watchdogName, selected);
		InterfaceProvider.getCommandRunner().runCommand(command);
	}

	/**
	 * Get the state of a watchdog from the server
	 *
	 * @param watchdogName
	 *            name of the watchdog, as defined on the server
	 * @param command to check a watchdog status
	 *
	 * @return command result
	 */
	private String evaluateWatchdogCommand(String watchdogName, String command) {
		ensureWatchdogServiceImported();
		return InterfaceProvider.getCommandRunner().evaluateCommand(String.format(command, watchdogName));
	}

	@Override
	public void dispose() {
		final boolean result = pollingFuture.cancel(true);
		logger.debug("Watchdog polling cancelled: result {}", result);
	}

	public void setWatchdogNames(List<String> watchdogNames) {
		this.watchdogNames = watchdogNames;
	}

	public void setPollingFrequency(int pollingFrequency) {
		this.pollingFrequency = pollingFrequency;
	}
}
