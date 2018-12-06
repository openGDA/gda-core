/*-
 * Copyright © 2014 Diamond Light Source Ltd.
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

package gda.rcp.views;

import static org.eclipse.swt.events.SelectionListener.widgetSelectedAdapter;

import java.text.DecimalFormat;
import java.util.Objects;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.swtdesigner.SWTResourceManager;

import gda.device.DeviceException;
import gda.device.IScannableMotor;
import gda.device.Scannable;
import gda.device.ScannableMotion;
import gda.device.ScannableMotionUnits;
import gda.jython.JythonServerFacade;
import gda.observable.IObserver;
import gda.rcp.GDAClientActivator;
import uk.ac.diamond.daq.concurrent.Async;
import uk.ac.gda.client.UIHelper;

/**
 * A class which provides a GUI composite to allow easy control of a scannable.
 * <p>
 * It provides the current position which can be edited and moved (unless READ_ONLY is specified) and buttons to allow
 * incremental moves. It also provides a stop button to abort moves. Tapping the up and down arrows while in the
 * position box will nudge the position by the increment.
 * <p>
 * The format of the displayed number will be specified by the scannable output format.
 * <p>
 * Example of vertical & horizontal format:<br>
 * <img src="nudgepositionercomposite.png" />
 */
public class NudgePositionerComposite extends Composite {

	private static final Logger logger = LoggerFactory.getLogger(NudgePositionerComposite.class);
	private static final double DEFAULT_INCREMENT = 1.0;
	private static final int NUDGE_BUTTON_WIDTH = 28;
	private static final int DEFAULT_INCREMENT_TEXT_WIDTH = 30;

	// GUI Elements
	private final CLabel displayNameLabel;
	private final Text positionText;
	private final Text incrementText;
	private final Button stopButton;
	private final Button decrementButton;
	private final Button incrementButton;
	private final Composite nudgeAmountComposite;

	// Update job
	private Job updateReadbackJob;

	private Scannable scannable;
	private Double lowerLimit; // Use Double to allow null for if no limits are set
	private Double upperLimit;
	private String scannableName;
	private String scannableOutputFormat;
	private String userUnits;
	private String displayName; // Allow a different prettier name be used if required
	private Double incrementValue = DEFAULT_INCREMENT;
	private Double currentPosition;
	private RowData stopButtonRowData;
	private int incrementTextWidth = DEFAULT_INCREMENT_TEXT_WIDTH;
	private final boolean readOnlyPosition;

	/**
	 * Constructor for a NudgePositionerComposite only requires the specification of minimal parameters.
	 *
	 * @param parent
	 *            The parent composite
	 * @param style
	 *            SWT style parameter. This is typically SWT.NONE, but you can specify:
	 *            <ul>
	 *            <li>SWT.VERTICAL or SWT.HORIZONTAL to lay the controls out vertically or horizontally respectively
	 *            (default is vertical)</li>
	 *            <li>SWT.READ_ONLY to make the position text box read-only. Note that the increment/decrement text box
	 *            will always be writable</li>
	 *            </ul>
	 */
	public NudgePositionerComposite(Composite parent, int style) {
		// Mask out style attributes that are intended for specific controls
		super(parent, style & ~SWT.HORIZONTAL & ~SWT.VERTICAL & ~SWT.READ_ONLY);

		parent.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));
		parent.setBackgroundMode(SWT.INHERIT_FORCE);

		// Setup layout
		final int rowLayoutType = ((style & SWT.HORIZONTAL) != 0) ? SWT.HORIZONTAL : SWT.VERTICAL;
		final RowLayout rowLayout = new RowLayout(rowLayoutType);
		rowLayout.fill = true;
		rowLayout.center = true;
		rowLayout.marginTop = 1;
		rowLayout.marginBottom = 1;
		rowLayout.spacing = 1;
		this.setLayout(rowLayout);

		// Name label
		displayNameLabel = new CLabel(this, SWT.CENTER);

		// Position text box
		positionText = new Text(this, SWT.BORDER);
		readOnlyPosition = (style & SWT.READ_ONLY) != 0;
		if (readOnlyPosition) {
			positionText.setEditable(false);
		} else {
			positionText.addKeyListener(new KeyAdapter() {
				@Override
				public void keyPressed(KeyEvent key) {
					// If enter was pressed move to new position
					if (key.character == SWT.CR) { // enter or numpad enter pressed
						// Get the new position from the text box
						double newPosition = Double.parseDouble(positionText.getText().split(" ")[0]);
						move(newPosition);
					}
					// If up was pressed increment position and move
					if (key.keyCode == SWT.ARROW_UP) { // up arrow pressed
						moveBy(incrementValue);
					}
					// If down was pressed decrement position and move
					if (key.keyCode == SWT.ARROW_DOWN) { // down arrow pressed
						moveBy(-incrementValue);
					}
				}
			});
			positionText.addFocusListener(new FocusAdapter() {
				@Override
				public void focusLost(FocusEvent e) {
					// Update to ensure current position is shown when focus is lost
					updateReadbackJob.schedule();
				}
			});
		}

		// Increment/decrement value
		nudgeAmountComposite = new Composite(this, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(3).spacing(1, 1).applyTo(nudgeAmountComposite);
		final GridDataFactory buttonDataFactory = GridDataFactory.fillDefaults().hint(NUDGE_BUTTON_WIDTH, SWT.DEFAULT).grab(true, false);

		// Decrement button
		decrementButton = new Button(nudgeAmountComposite, SWT.NONE);
		decrementButton.setText("-");
		buttonDataFactory.applyTo(decrementButton);
		decrementButton.addSelectionListener(widgetSelectedAdapter(e -> moveBy(-incrementValue)));

		// Increment text box
		incrementText = new Text(nudgeAmountComposite, SWT.BORDER);
		GridDataFactory.fillDefaults().hint(incrementTextWidth, SWT.DEFAULT).grab(true, false).applyTo(incrementText);
		incrementText.addListener(SWT.Modify, event -> setIncrement(incrementText.getText()));

		// Increment button
		incrementButton = new Button(nudgeAmountComposite, SWT.NONE);
		incrementButton.setText("+");
		buttonDataFactory.applyTo(incrementButton);
		incrementButton.addSelectionListener(widgetSelectedAdapter(e-> moveBy(incrementValue)));

		// Stop button
		stopButton = new Button(this, SWT.NONE);
		stopButton.addSelectionListener(widgetSelectedAdapter(e -> {
			try {
				scannable.stop();
			} catch (DeviceException ex) {
				logger.error("Error while stopping " + scannableName, ex);
			}
		}));
		stopButtonRowData = new RowData();
		stopButton.setLayoutData(stopButtonRowData);
		stopButton.setText("Stop");
		final ImageDescriptor stopImage = GDAClientActivator.getImageDescriptor("icons/stop.png");
		Objects.requireNonNull(stopImage, "Missing image for stop button");
		stopButton.setImage(stopImage.createImage());

		// At this time the control is built but no scannable is set so disable it.
		disable();
	}

	private void moveBy(double amountToMove) {
		if (currentPosition == null) {
			final String message = String.format("Cannot move %s", scannableName);
			final String reason = "Position is unknown";
			logger.error("{} : {}", message, reason);
			UIHelper.showError(message, reason);
		} else {
			move(currentPosition + amountToMove);
		}
	}

	/**
	 * Moves the scannable to a new position.<br>
	 * Checks if the position is within limits and if the scannable is busy before moving
	 *
	 * @param position
	 *            The demanded position
	 */
	private void move(double position) {
		final boolean batonHeld = JythonServerFacade.getInstance().amIBatonHolder();
		if (!batonHeld) {
			final MessageDialog dialog = new MessageDialog(Display.getDefault().getActiveShell(), "Baton not held", null,
					"You do not hold the baton, please take the baton using the baton manager.", MessageDialog.ERROR, new String[] { "Ok" }, 0);
			dialog.open();
		} else if (checkLimits(position)) {
			try {
				if (!scannable.isBusy()) {
					runMoveInThread(position);
				}
			} catch (DeviceException e) {
				logger.error("Error while trying to move {}", scannableName, e);
			}
		} else {
			// Log positions to full accuracy: round to max. 4 decimal places to display to user
			logger.error("Cannot move {} to {}: position is outside the allowed limits [{} : {}]", scannableName, position, lowerLimit, upperLimit);
			final DecimalFormat df = new DecimalFormat("0.####");
			final String message = String.format("Cannot move %s to %s%nPosition is outside the allowed limits [%s : %s]",
					scannableName, df.format(position), df.format(lowerLimit), df.format(upperLimit));
			MessageDialog.openError(Display.getDefault().getActiveShell(), "Error moving device", message);
		}
	}

	/**
	 * Move the scannable, ensuring that the GUI is not blocked during the move.
	 * <p>
	 * In theory, we could call {@link gda.device.scannable.ScannableMotionBase#asynchronousMoveTo(Object)}, but
	 * creating our own thread here ensures that the GUI is not blocked if asynchronousmoveTo() is written in such a way
	 * that it can block.
	 *
	 * @param position
	 *            The demanded position
	 */
	private void runMoveInThread(double position) {
		Async.execute(() -> {
			try {
				scannable.moveTo(position);
			} catch (DeviceException e) {
				logger.error("Error while trying to move {}", scannableName, e);
			}
		});
	}

	/**
	 * Checks if the new position is within the limits
	 *
	 * @param newPosition
	 * @return true if newPosition is within limits or if no limits are set
	 */
	private boolean checkLimits(double newPosition) {
		if (upperLimit == null || lowerLimit == null) {
			return true; // Limits are not set
		} else {
			return (newPosition >= lowerLimit && newPosition <= upperLimit);
		}
	}

	/**
	 * This is used to update the GUI. Only this method should be used to update the GUI to ensure display is consistent. The position is updated and the
	 * controls enabled/disabled as appropriate.
	 *
	 * @param currentPositionString
	 *            The newest position to display typically from {@link #getCurrentPosition()}
	 * @param moving
	 *            Flag showing if the scannable is moving
	 */
	private void updateGui(final Double currentPosition, final boolean moving) {
		// Save the new position
		this.currentPosition = currentPosition;
		// Format current position using output format
		final String currentPositionString = String.format(scannableOutputFormat, currentPosition).trim();
		// Update the GUI in the UI thread
		Display.getDefault().asyncExec(() -> {
			if (positionText.isDisposed()) {
				logger.warn("Attempting to update positionText when it has been disposed");
				return;
			}
			// Update the position
			if (currentPositionString == null) {
				positionText.setText("null");
			} else if (userUnits == null || userUnits.equals("")) {
				positionText.setText(currentPositionString);
			} else {
				positionText.setText(currentPositionString + " " + userUnits);
			}
			// Update the controls enabled/disabled
			decrementButton.setEnabled(!moving);
			incrementButton.setEnabled(!moving);
			positionText.setEditable(!moving && !readOnlyPosition);
			if (stopButton != null) {
				stopButton.setEnabled(moving);
			}
		});
	}

	/**
	 * Calls {@link Scannable} getPosition() method and parses it into a String using getOutputFormat() If the scannable returns an array the first element is
	 * used.
	 *
	 * @return The current position of the scannable
	 */
	private Double getCurrentPosition() {
		Double currentPos = null;
		try {
			final Object getPosition = scannable.getPosition();

			if (getPosition.getClass().isArray()) {
				// The scannable returns an array assume the relevant value is the first and its a double
				currentPos = (Double) ((Object[]) getPosition)[0];
			} else if (getPosition instanceof Double) {
				currentPos = (Double) getPosition;
			} else {
				logger.error("Error while parsing currrent position of {}", scannableName);
			}
		} catch (DeviceException e) {
			logger.error("Error while getting currrent position of {}", scannableName, e);
		}

		return currentPos;
	}

	private void determineUserUnits() {
		try {
			if (scannable instanceof ScannableMotionUnits) {
				setUserUnits(((ScannableMotionUnits) scannable).getUserUnits());
			} else {
				logger.debug("No user units available for {}", scannableName);
			}
		} catch (Exception e) {
			logger.error("Error getting user limits for {}", scannableName, e);
		}
	}

	public void setUserUnits(String userUnits) {
		this.userUnits = userUnits;
	}

	private void determineScannableLimits() {
		try {
			if (scannable instanceof IScannableMotor) {
				final IScannableMotor scannableMotor = (IScannableMotor) scannable;
				lowerLimit = scannableMotor.getLowerInnerLimit();
				upperLimit = scannableMotor.getUpperInnerLimit();
			} else if (scannable instanceof ScannableMotion) {
				final ScannableMotion scannableMotion = (ScannableMotion) scannable;
				final Double[] lowerLimits = scannableMotion.getLowerGdaLimits();
				if (lowerLimits != null && lowerLimits.length > 0) {
					lowerLimit = scannableMotion.getLowerGdaLimits()[0];
				}
				final Double[] upperLimits = scannableMotion.getLowerGdaLimits();
				if (upperLimits != null && upperLimits.length > 0) {
					upperLimit = scannableMotion.getUpperGdaLimits()[0];
				}
			}
			if (lowerLimit == null && upperLimit == null) {
				logger.debug("No scannable units available for {}", scannableName);
			} else {
				positionText.setToolTipText(lowerLimit + " : " + upperLimit);
			}
		} catch (Exception e) {
			logger.error("Error getting scannable limits for {}", scannableName, e);
		}
	}

	/**
	 * Change the increment programmatically from a view
	 *
	 * @param increment
	 *            The new increment value
	 */
	public void setIncrement(double increment) {
		this.incrementValue = increment;
		this.incrementText.setText(String.valueOf(increment));
	}

	private void setIncrement(String incrementText) {
		if (incrementText.isEmpty()) {
			return;
		}
		incrementValue = Double.parseDouble(incrementText);
	}

	public double getIncrement() {
		return this.incrementValue;
	}

	/**
	 * Sets the limits which will be used to check if a new position is valid.
	 *
	 * @param lowerLimit
	 *            the desired lower limit
	 * @param upperLimit
	 *            the desired upper limit
	 * @throws IllegalArgumentException
	 *             if the lowerLimit > upperLimit
	 */
	public void setLimits(double lowerLimit, double upperLimit) {
		if (lowerLimit > upperLimit) {
			throw new IllegalArgumentException("The lower limit must be lower than the upper limit");
		}
		this.lowerLimit = lowerLimit;
		this.upperLimit = upperLimit;
	}

	public Double getLowerLimit() {
		return lowerLimit;
	}

	public Double getUpperLimit() {
		return upperLimit;
	}

	/**
	 * Clears the limits set on the NudgePositionerComposite does not affect the underlying scannable limits.
	 */
	public void removeLimits() {
		lowerLimit = null;
		upperLimit = null;
	}

	public String getDisplayName() {
		return displayName;
	}

	/**
	 * Sets a different name to be used in the GUI instead of the default which is the scannable name
	 * <p>
	 * After calling this method the control will be automatically redrawn.
	 *
	 * @param displayName
	 *            The name to be used in the GUI
	 */
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
		displayNameLabel.setText(displayName);
		this.redraw();
	}

	/**
	 * Hides the stop button.
	 * <p>
	 * This is useful for scannables which can't be stopped (move instantly) i.e. setting a voltage.
	 *
	 * @see #showStopButton()
	 */
	public void hideStopButton() {
		stopButton.setVisible(false);
		// Exclude the stop button so the layout will compress when it's hidden
		stopButtonRowData.exclude = true;
		this.redraw();
	}

	/**
	 * Shows the stop button.
	 *
	 * @see #hideStopButton()
	 */
	public void showStopButton() {
		stopButton.setVisible(true);
		// Don't exclude the stop button so the layout will expand when it's shown
		stopButtonRowData.exclude = false;
		this.redraw();
	}

	/**
	 * Check if the stop button is visible
	 *
	 * @return true if stop button is visible
	 * @see #showStopButton()
	 * @see #hideStopButton()
	 */
	public boolean isStopButtonVisible() {
		return stopButton.isVisible();
	}

	@Override
	public void setEnabled(boolean enabled) {
		if (enabled) {
			// When the control is enabled reconfigure to ensure values are current
			configure();
		} else { // disabled
			// Clear the position and increments
			positionText.setText("");
			incrementText.setText("");
		}
		// Disable the controls
		displayNameLabel.setEnabled(enabled);
		positionText.setEnabled(enabled);
		incrementText.setEnabled(enabled);
		stopButton.setEnabled(enabled);
		decrementButton.setEnabled(enabled);
		incrementButton.setEnabled(enabled);

		this.redraw();
	}

	/**
	 * Sets the control enabled
	 * <p>
	 * Equivalent to setEnabled(true)
	 *
	 * @see #setEnabled(boolean)
	 */
	public void enable() {
		setEnabled(true);
	}

	/**
	 * Sets the control disabled (grayed)
	 * <p>
	 * Equivalent to setEnabled(false)
	 *
	 * @see #setEnabled(boolean)
	 */
	public void disable() {
		setEnabled(false);
	}

	public Scannable getScannable() {
		return scannable;
	}

	/**
	 * This sets the scannable which will be controlled and will automatically configure the control.
	 * <p>
	 * This can also be called to change the scannable controlled at any time which will reconfigure the control.
	 *
	 * @param scannable
	 *            The scannable to control
	 */
	public void setScannable(Scannable scannable) {
		if (scannable == null) {
			throw new IllegalArgumentException("Scannable cannot be set null");
		}

		this.scannable = scannable;
		this.scannableName = scannable.getName();
		this.scannableOutputFormat = scannable.getOutputFormat()[0];

		// If no display name is set when the scannable is set, set it to the scannable name
		if (displayName == null) {
			setDisplayName(scannableName);
		}
		enable();
	}

	private void configure() {
		if (scannable == null) {
			throw new IllegalStateException("Scannable is not set");
		}

		// Set the increment text
		incrementText.setText(incrementValue.toString());

		// TODO This should setup the control after the scannable is set
		// This is the job which handles updating of the composite. It need to be scheduled when a move is
		// started after which it will continue to run until the move finishes.
		updateReadbackJob = new Job("Update " + scannableName + " nudge positioner readback value") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				boolean moving = true;
				while (moving) { // Loop which runs while scannable is moving
					try {
						moving = scannable.isBusy();
					} catch (DeviceException e) {
						logger.error("Error while determining whether {} is busy", scannableName, e);
						return Status.CANCEL_STATUS;
					}

					// Check if the user has cancelled the job
					if (monitor.isCanceled()) {
						return Status.CANCEL_STATUS;
					}

					try {
						Thread.sleep(100); // Pause to stop loop running to fast. ~ 10 Hz
					} catch (InterruptedException e) {
						logger.error("Thread interrupted during update job for {}", scannableName, e);
						return Status.CANCEL_STATUS; // Thread interrupted so cancel update job
					}

					// Update the GUI
					updateGui(getCurrentPosition(), moving);
				}
				return Status.OK_STATUS;
			}
		};

		// Add an observer to the scannable to start the updateReadbackJob when an event occurs such as starting to move.
		// If the job is already running a maximum of one extra will be scheduled.
		final IObserver iObserver = (source, arg) -> updateReadbackJob.schedule();
		scannable.addIObserver(iObserver);

		this.addDisposeListener(e -> {
			scannable.deleteIObserver(iObserver);
			updateReadbackJob.cancel();
		});

		determineScannableLimits();
		determineUserUnits();
		updateReadbackJob.schedule(); // Get initial values
	}

	public int getIncrementTextWidth() {
		return incrementTextWidth;
	}

	public void setIncrementTextWidth(int incrementTextWidth) {
		this.incrementTextWidth = incrementTextWidth;
		((GridData) incrementText.getLayoutData()).widthHint = incrementTextWidth;
	}
}