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

import gda.device.DeviceException;
import gda.device.Scannable;
import gda.jython.JythonServerFacade;
import gda.observable.IObserver;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class which provides a GUI composite to allow easy control of a scannable.
 * <p>
 * It provides the current position which can be edited and moved and buttons to allow incremental moves. It also provides a stop button to abort moves. Tapping
 * the up and down arrows while in the position box will nudge the position by the increment.
 * <p>
 * The format of the displayed number will be specified by the scannable output format.
 */
public class NudgePositionerComposite extends Composite {

	private static final Logger logger = LoggerFactory.getLogger(NudgePositionerComposite.class);
	public static final double DEFAULT_INCREMENT = 1.0;

	// GUI Elements
	private Label displayNameLabel;
	private Text positionText;
	private Text incrementText;
	private Button stopButton;
	private Button decrementButton;
	private Button incrementButton;

	// Update job
	private Job updateReadbackJob;

	private Scannable scannable;
	private Double lowerLimit; // Use Double to allow null for if no limits are set
	private Double upperLimit;
	private String scannableName;
	private String userUnits;
	private String displayName; // Allow a different prettier name be used if required
	private Double incrementValue = DEFAULT_INCREMENT;
	private Double currentPosition;

	/**
	 * Constructor for a NudgePositionerComposite only requires the specification of minimal parameters.
	 *
	 * @param parent
	 *            The parent composite
	 * @param style
	 *            SWT style parameter (Typically SWT.NONE)
	 */
	public NudgePositionerComposite(Composite parent, int style) {
		super(parent, style);

		// Setup layout
		GridLayout gridLayout = new GridLayout(3, false);
		gridLayout.horizontalSpacing = 0;
		gridLayout.verticalSpacing = 0;
		this.setLayout(gridLayout);

		// Name label
		displayNameLabel = new Label(this, SWT.NONE);
		displayNameLabel.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 3, 1));

		// Position text box
		positionText = new Text(this, SWT.BORDER);
		positionText.setTextLimit(10);
		positionText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 3, 1));
		positionText.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent key) {
				// If enter was pressed move to new position
				if (key.character == SWT.CR) { // enter or numpad enter pressed
					// Get the new position from the text box
					double newPosition = Double.valueOf(positionText.getText().split(" ")[0]);
					move(newPosition);
				}
				// If up was pressed increment position and move
				if (key.keyCode == SWT.ARROW_UP) { // up arrow pressed
					move(currentPosition + incrementValue);
				}
				// If down was pressed decrement position and move
				if (key.keyCode == SWT.ARROW_DOWN) { // down arrow pressed
					move(currentPosition - incrementValue);
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

		// Decrement button
		decrementButton = new Button(this, SWT.NONE);
		decrementButton.setText("-");
		decrementButton.setLayoutData(GridDataFactory.fillDefaults().hint(28, SWT.DEFAULT).grab(true, false).create());
		decrementButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				move(currentPosition - incrementValue);
			}
		});

		// Increment text box
		incrementText = new Text(this, SWT.BORDER);
		GridData incrementGridData = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		incrementGridData.widthHint = 30; // Make the increment box a little wider
		incrementText.setLayoutData(GridDataFactory.fillDefaults().hint(30, SWT.DEFAULT).grab(true, false).create());
		incrementText.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent key) {
				// If enter was pressed update increment and switch focus to position box to allow up down tapping.
				if (key.character == SWT.CR) { // enter or numpad enter pressed
					setIncrement(Double.valueOf(incrementText.getText()));
					positionText.setFocus();
				}
			}
		});

		// Increment button
		incrementButton = new Button(this, SWT.NONE);
		incrementButton.setText("+");
		incrementButton.setLayoutData(GridDataFactory.fillDefaults().hint(28, SWT.DEFAULT).grab(true, false).create());
		incrementButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				move(currentPosition + incrementValue);
			}
		});

		// Stop button
		stopButton = new Button(this, SWT.NONE);
		stopButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					scannable.stop();
				} catch (DeviceException e1) {
					logger.error("Error while stopping " + scannableName, e);
				}
			}
		});
		stopButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 3, 1));
		stopButton.setText("Stop");

		// At this time the control is built but no scannable is set so disable it.
		disable();
	}

	/**
	 * Moves the scannable to a new position by calling {@link Scannable} asynchronousMoveTo(position). Checks if the position is within limits and if the
	 * scannable is busy before moving
	 *
	 * @param position
	 *            The demanded position
	 */
	private void move(double position) {
		boolean batonHeld = JythonServerFacade.getInstance().isBatonHeld();
		if (!batonHeld) {
			MessageDialog dialog = new MessageDialog(Display.getDefault().getActiveShell(), "Baton not held", null,
					"You do not hold the baton, please take the baton using the baton manager.", MessageDialog.ERROR, new String[] { "Ok" }, 0);
			dialog.open();
		} else if (checkLimits(position)) {
			try {
				if (!scannable.isBusy()) {
					scannable.asynchronousMoveTo(position);
				}
			} catch (DeviceException e) {
				logger.error("Error while trying to move {}", scannableName, e);
			}
		}
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
			if (newPosition >= lowerLimit && newPosition <= upperLimit) {
				return true;
			} else {
				return false;
			}
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
		final String currentPositionString = String.format(scannable.getOutputFormat()[0], currentPosition).trim();
		// Update the GUI in the UI thread
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
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
				positionText.setEditable(!moving);
				if (stopButton != null) {
						stopButton.setEnabled(moving);
				}
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
		Double currentPosition = null;
		try {
			Object getPosition = scannable.getPosition();

			if (getPosition.getClass().isArray())
				// The scannable returns an array assume the relevant value is the first and its a double
				currentPosition = (Double) ((Object[]) getPosition)[0];
			else if (getPosition instanceof Double) {
				currentPosition = (Double) getPosition;
			} else {
				logger.error("Error while parsing currrent position of {}", scannableName);
			}
		} catch (DeviceException e) {
			logger.error("Error while getting currrent position of {}", scannableName, e);
		}

		return currentPosition;
	}

	// TODO This method needs rewriting to remove the use of jython e.g the commented out example but working
	// Might need to try casting to something with the get limits methods. The problem is the remoting gives an
	// adaptor class which doesn't have the get limits methods.
	private void determineUserUnits() {
		JythonServerFacade jythonServer = JythonServerFacade.getInstance();
		String command = "\'" + scannableName + "\' in globals()";
		String evaluateCommand = jythonServer.evaluateCommand(command);
		if (evaluateCommand.equals("True")) {
			command = "\'getUserUnits\' in dir(" + scannableName + ")";
			evaluateCommand = jythonServer.evaluateCommand(command);
			if (evaluateCommand.equals("True")) {
				command = scannableName + ".getUserUnits()";
				setUserUnits(jythonServer.evaluateCommand(command));
			}
		}
		// This was a example using reflection which should work if the remoting provided the required methods
		// Class<? extends Scannable> scannableClass = scannable.getClass();
		// try {
		// Method getUnitsMethod = scannableClass.getMethod("getUserUnits");
		// setUserUnits((String) getUnitsMethod.invoke(scannable));
		// } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
		// logger.info("Getting user units for scannable {} failed. No getUserUnits() method accessible", scannable.getName());
		// }

	}

	public void setUserUnits(String userUnits) {
		this.userUnits = userUnits;
	}

	private void determineScannableLimits() {
		JythonServerFacade jythonServer = JythonServerFacade.getInstance();
		String command = "\'" + scannableName + "\' in globals()";
		String evaluateCommand = jythonServer.evaluateCommand(command);
		if (evaluateCommand.equals("True")) {
			command = "\'getLowerInnerLimit\' in dir(" + scannableName + ")";
			evaluateCommand = jythonServer.evaluateCommand(command);
			if (evaluateCommand.equals("True")) {
				command = scannableName + ".getLowerInnerLimit()";
				evaluateCommand = jythonServer.evaluateCommand(command);
				lowerLimit = Double.parseDouble(evaluateCommand);
				command = scannableName + ".getUpperInnerLimit()";
				evaluateCommand = jythonServer.evaluateCommand(command);
				upperLimit = Double.parseDouble(evaluateCommand);
				positionText.setToolTipText(lowerLimit + " : " + upperLimit);
			} else {
				command = "\'getLowerGdaLimits\' in dir(" + scannableName + ")";
				evaluateCommand = jythonServer.evaluateCommand(command);
				if (evaluateCommand.equals("True")) {
					command = scannableName + ".getLowerGdaLimits()";
					evaluateCommand = jythonServer.evaluateCommand(command);
					if (!evaluateCommand.equals("None")) {
						command = scannableName + ".getLowerGdaLimits()[0]";
						evaluateCommand = jythonServer.evaluateCommand(command);
						lowerLimit = Double.parseDouble(evaluateCommand);
						command = scannableName + ".getUpperGdaLimits()[0]";
						evaluateCommand = jythonServer.evaluateCommand(command);
						upperLimit = Double.parseDouble(evaluateCommand);
						positionText.setToolTipText(lowerLimit + " : " + upperLimit);
					}
				}
			}
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
		this.redraw();
	}

	/**
	 * Shows the stop button.
	 *
	 * @see #hideStopButton()
	 */
	public void showStopButton() {
		stopButton.setVisible(true);
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

		// Add an observer to the scannable when an event occurs such as starting to move
		// start the updateReadbackJob. If the job is already running a maximum of one extra will
		// be scheduled.
		final IObserver iObserver = new IObserver() {
			@Override
			public void update(final Object source, Object arg) {
				// Start the updateReadbackJob
				updateReadbackJob.schedule();
			}
		};
		scannable.addIObserver(iObserver);

		this.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				scannable.deleteIObserver(iObserver);
				updateReadbackJob.cancel();
			}
		});

		determineScannableLimits();
		determineUserUnits();
		updateReadbackJob.schedule(); // Get initial values
	}

}