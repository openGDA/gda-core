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
		Display.getDefault().asyncExec(() -> {
			for (Button watchdogCheckbox : watchdogCheckBoxes) {
				final String watchdogName = (String) watchdogCheckbox.getData();
				watchdogCheckbox.setSelection(isWatchdogEnabled(watchdogName));
			}
		});
	}

	/**
	 * When a checkbox is checked/unchecked, enable/disable the corresponding watchdog on the server
	 *
	 * @param e
	 *            the system event associated with (un)checking the checkbox
	 */
	private void handleSelectionEvent(SelectionEvent e) {
		final Button button = (Button) e.getSource();
		final String watchdogName = (String) button.getData();
		final String selected = button.getSelection() ? "True" : "False";
		final String command = String.format("set_watchdog_enabled(\"%s\", %s)", watchdogName, selected);
		InterfaceProvider.getCommandRunner().runCommand(command);
	}

	/**
	 * Get the state of a watchdog from the server
	 *
	 * @param watchdogName
	 *            name of the watchdog, as defined on the server
	 * @return {@code true} if the watchdog is enabled, {@code false} if it is disabled
	 */
	private boolean isWatchdogEnabled(String watchdogName) {
		final String command = String.format("is_watchdog_enabled(\"%s\")", watchdogName);
		final String result = InterfaceProvider.getCommandRunner().evaluateCommand(command);
		return result.equals("True");
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
