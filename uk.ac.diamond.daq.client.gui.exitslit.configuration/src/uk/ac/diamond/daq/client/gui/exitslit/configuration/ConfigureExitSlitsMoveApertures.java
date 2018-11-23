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

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.eclipse.swt.events.SelectionListener.widgetSelectedAdapter;
import static uk.ac.diamond.daq.client.gui.exitslit.configuration.ConfigureExitSlitsUtils.COLOUR_WHITE;
import static uk.ac.diamond.daq.client.gui.exitslit.configuration.ConfigureExitSlitsUtils.DIAGNOSTIC_COLOUR_MAP;
import static uk.ac.diamond.daq.client.gui.exitslit.configuration.ConfigureExitSlitsUtils.MOTOR_ERROR;
import static uk.ac.diamond.daq.client.gui.exitslit.configuration.ConfigureExitSlitsUtils.SCANNABLE_UPDATE_PERIOD;
import static uk.ac.diamond.daq.client.gui.exitslit.configuration.ConfigureExitSlitsUtils.createButton;
import static uk.ac.diamond.daq.client.gui.exitslit.configuration.ConfigureExitSlitsUtils.createStopButton;
import static uk.ac.diamond.daq.client.gui.exitslit.configuration.ConfigureExitSlitsUtils.displayError;

import java.text.DecimalFormat;
import java.util.concurrent.ScheduledFuture;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ProgressBar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.IScannableMotor;
import uk.ac.diamond.daq.concurrent.Async;
import uk.ac.gda.dls.client.views.ReadonlyScannableComposite;

/**
 * Controls to move the exit slit apertures in or out
 * <p>
 * This composite is used for two different steps in the configuration process, where the aim is to move the apertures
 * in and out respectively. Its behaviour is controlled by the constructor parameter "moveIn"
 */
public class ConfigureExitSlitsMoveApertures extends ConfigureExitSlitsComposite {
	private static final Logger logger = LoggerFactory.getLogger(ConfigureExitSlitsMoveApertures.class);

	/**
	 * Amount in mm by which the actual position of the apertures can differ from the desired position but still be
	 * treated as "in position"
	 */
	private static final double POSITION_TOLERANCE = 0.001;

	/**
	 * <code>true</code> if the user should move the apertures in, and hence to make the "Next" button active when they are in<br>
	 * <code>false</code> if the user should move the apertures out
	 */
	private final boolean moveIn;

	private final ConfigureExitSlitsParameters params;

	private final Button btnMoveIn;
	private final Button btnMoveOut;
	private final Button btnStop;

	private final ProgressBar progressBar;
	private ScheduledFuture<?> progressBarUpdater;

	// Start point and size of the move - used to update the progress bar
	private double moveStart;
	private double moveSize;

	private final IScannableMotor apertureYMotor;

	private static final int COLUMNS = 5;

	private final DecimalFormat floatFormat = new DecimalFormat("#.#####");

	public ConfigureExitSlitsMoveApertures(Composite parent, int style, String title, String description, ConfigureExitSlitsParameters params, boolean moveIn) {
		super(parent, style, title, description);
		this.params = params;
		this.moveIn = moveIn;
		apertureYMotor = params.getApertureArrayYMotor();

		GridLayoutFactory.swtDefaults().numColumns(COLUMNS).applyTo(this);
		setBackground(COLOUR_WHITE);

		final ReadonlyScannableComposite aperturePosition = new ReadonlyScannableComposite(this, SWT.NONE, apertureYMotor, "Aperture array motor", "", 2, true);
		GridDataFactory.swtDefaults().applyTo(aperturePosition);
		aperturePosition.setBackground(COLOUR_WHITE);
		aperturePosition.setMinPeriodMS(SCANNABLE_UPDATE_PERIOD);
		aperturePosition.setColourMap(DIAGNOSTIC_COLOUR_MAP);

		final double inPos = params.getApertureArrayInPosition();
		btnMoveIn = createButton(this, "Move in", tooltipMessage("in", inPos));
		btnMoveIn.addSelectionListener(widgetSelectedAdapter(e -> moveApertures(inPos)));

		final double outPos = params.getApertureArrayOutPosition();
		btnMoveOut = createButton(this, "Move out", tooltipMessage("out", outPos));
		btnMoveOut.addSelectionListener(widgetSelectedAdapter(e -> moveApertures(outPos)));

		btnStop = createStopButton(this);
		btnStop.addSelectionListener(widgetSelectedAdapter(e -> stopMotor()));

		progressBar = new ProgressBar(this, SWT.SMOOTH);
		progressBar.setMinimum(0);
		progressBar.setMaximum(100);
		progressBar.setSelection(0);

		updateButtons();
	}

	private String tooltipMessage(String direction, double position) {
		return String.format("Move apertures %s (position %s)", direction, floatFormat.format(position));
	}

	private boolean apertureInPosition() {
		return moveIn ? isApertureArrayIn() : isApertureArrayOut();
	}

	@Override
	protected void onUpdate(Object source, Object arg) {
		try {
			// Motor no longer moving, so cancel the updating task and do one final update
			if (!apertureYMotor.isBusy()) {
				if (progressBarUpdater != null) {
					progressBarUpdater.cancel(true);
				}
				updateProgressBar();
			}
		} catch (DeviceException e) {
			logger.error("Error updating progress bar", e);
		}
		super.onUpdate(source, arg);
	}

	@Override
	protected void updateButtons() {
		if (btnMoveIn != null && btnMoveOut != null) {
			try {
				btnMoveIn.setEnabled(!apertureYMotor.isBusy() && !isApertureArrayIn());
				btnMoveOut.setEnabled(!apertureYMotor.isBusy() && !isApertureArrayOut());
				btnStop.setEnabled(apertureYMotor.isBusy());
			} catch (DeviceException e) {
				logger.error("Error accessing aperture motor {}", apertureYMotor.getName(), e);
			}
		}
	}

	private boolean isApertureArrayIn() {
		try {
			return Math.abs((double) apertureYMotor.getPosition() - params.getApertureArrayInPosition()) < POSITION_TOLERANCE;
		} catch (DeviceException e) {
			logger.error("Error getting position of aperture {}", apertureYMotor.getName(), e);
			return false;
		}
	}

	private boolean isApertureArrayOut() {
		try {
			return Math.abs((double) apertureYMotor.getPosition() - params.getApertureArrayOutPosition()) < POSITION_TOLERANCE;
		} catch (DeviceException e) {
			logger.error("Error getting position of aperture {}", apertureYMotor.getName(), e);
			return false;
		}
	}

	private void moveApertures(double position) {
		try {
			// Set up progress bar
			moveStart = (double) apertureYMotor.getPosition();
			moveSize = Math.abs(moveStart - position);
			progressBar.setSelection(0);

			// Start progress bar updater
			progressBarUpdater = Async.scheduleAtFixedRate(this::updateProgressBar, 0, 500, MILLISECONDS);

			// Start move
			apertureYMotor.asynchronousMoveTo(position);
		} catch (DeviceException e) {
			final String message = String.format("Error moving aperture motor %s", apertureYMotor.getName());
			displayError(MOTOR_ERROR, message, e, logger);
		}
	}

	private void stopMotor() {
		try {
			apertureYMotor.stop();
		} catch (DeviceException e) {
			displayError(MOTOR_ERROR, "Error stopping aperture motor", e, logger);
		}
	}

	private void updateProgressBar() {
		try {
			final double currentPosition = (double) apertureYMotor.getPosition();
			final int percentDone = (int) Math.round((Math.abs(currentPosition - moveStart) * 100.0) / moveSize);
			Display.getDefault().asyncExec(() -> progressBar.setSelection(percentDone));
		} catch (Exception e) {
			logger.error("Failed to update progress bar", e);
		}
	}

	@Override
	public boolean canGoToNextPage() {
		return !isBusy() && apertureInPosition();
	}

	@Override
	public boolean canGoToPreviousPage() {
		return !isBusy();
	}

	private boolean isBusy() {
		try {
			return apertureYMotor.isBusy();
		} catch (DeviceException e) {
			logger.error("Error getting busy state of aperture motor {}", apertureYMotor.getName(), e);
			return true;
		}
	}

	@Override
	public void setVisible(boolean visible) {
		if (visible) {
			apertureYMotor.addIObserver(updateObserver);
		} else {
			apertureYMotor.deleteIObserver(updateObserver);
		}
		super.setVisible(visible);
	}

	@Override
	public void dispose() {
		if (progressBarUpdater != null) {
			progressBarUpdater.cancel(true);
		}
		apertureYMotor.deleteIObserver(updateObserver);
		super.dispose();
	}
}
