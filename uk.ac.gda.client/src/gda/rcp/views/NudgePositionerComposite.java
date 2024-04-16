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

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.IScannableMotor;
import gda.device.ITweakableScannableMotor;
import gda.device.MotorException;
import gda.device.Scannable;
import gda.device.ScannableMotion;
import gda.device.ScannableMotionUnits;
import uk.ac.gda.client.UIHelper;

/**
 * A class which provides a GUI composite to allow easy control of a scannable
 * <p>
 * It provides the current position which can be edited and moved (unless READ_ONLY is specified) and buttons to allow
 * incremental moves.<br>
 * A stop button to abort moves is provided by the base class {@link AbstractPositionerComposite}.<br>
 * Tapping the up and down arrows while in the position box will nudge the position by the increment.
 * <p>
 * The format of the displayed number will be specified by the scannable output format.
 * <p>
 * Example of vertical & horizontal format:<br>
 * <img src="nudgepositionercomposite.png" />
 */
public class NudgePositionerComposite extends AbstractPositionerComposite {

	private static final Logger logger = LoggerFactory.getLogger(NudgePositionerComposite.class);
	private static final double DEFAULT_INCREMENT = 1.0;
	private static final int NUDGE_BUTTON_WIDTH = 28;
	private static final int DEFAULT_INCREMENT_TEXT_WIDTH = 30;

	// GUI Elements
	private Text positionText;
	private Text incrementText;

	private Button decrementButton;
	private Button incrementButton;

	private Double lowerLimit; // Use Double to allow null for if no limits are set
	private Double upperLimit;

	private final DecimalFormat fourDecimalPlaces = new DecimalFormat("0.####");

	private String userUnits = "";
	private Double incrementValue = DEFAULT_INCREMENT;
	private Double currentPosition;

	private int incrementTextWidth = DEFAULT_INCREMENT_TEXT_WIDTH;
	private boolean readOnlyPosition;
	private CLabel displayUnitLabel;

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
		super(parent, style & ~SWT.READ_ONLY);
		readOnlyPosition = (style & SWT.READ_ONLY) != 0;
	}
	public NudgePositionerComposite(Composite parent, int style, boolean unitDisplayOutsideTextBox) {
		// Mask out style attributes that are intended for specific controls
		super(parent, style & ~SWT.READ_ONLY, unitDisplayOutsideTextBox);
		readOnlyPosition = (style & SWT.READ_ONLY) != 0;
	}

	@Override
	protected void createPositionerControl() {
		// Position text box
		positionText = new Text(this, SWT.BORDER);
		if (readOnlyPosition) {
			positionText.setEditable(false);
		} else {
			positionText.addKeyListener(new KeyAdapter() {
				@Override
				public void keyPressed(KeyEvent key) {
					// If enter was pressed move to new position
					if (key.character == SWT.CR) { // enter or numpad enter pressed
						// Get the new position from the text box
						double newPosition;
						if (unitDisplayOutsideTextBox) {
							newPosition = Double.parseDouble(positionText.getText());
						} else {
							newPosition = Double.parseDouble(positionText.getText().split(" ")[0]);
						}
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
					scheduleUpdateReadbackJob();
				}
			});
		}

		if (unitDisplayOutsideTextBox) {
			// Unit label
			displayUnitLabel = new CLabel(this, SWT.CENTER);
			displayUnitLabel.setLayoutData(new RowData());
		}

		Composite nudgeAmountComposite = new Composite(this, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(3).spacing(1, 1).applyTo(nudgeAmountComposite);
		final GridDataFactory buttonDataFactory = GridDataFactory.fillDefaults().hint(NUDGE_BUTTON_WIDTH, SWT.DEFAULT).grab(true, false);

		// Decrement button
		decrementButton = new Button(nudgeAmountComposite, SWT.NONE);
		decrementButton.setText("-");
		buttonDataFactory.applyTo(decrementButton);
		decrementButton.addSelectionListener(widgetSelectedAdapter(e -> moveBy(-incrementValue)));
		decrementButton.addSelectionListener(widgetSelectedAdapter(e -> scheduleUpdateReadbackJob()));

		// Increment text box
		incrementText = new Text(nudgeAmountComposite, SWT.BORDER);
		GridDataFactory.fillDefaults().hint(incrementTextWidth, SWT.DEFAULT).grab(true, false).applyTo(incrementText);
		incrementText.addListener(SWT.Modify, event -> setIncrement(incrementText.getText()));

		// Increment button
		incrementButton = new Button(nudgeAmountComposite, SWT.NONE);
		incrementButton.setText("+");
		buttonDataFactory.applyTo(incrementButton);
		incrementButton.addSelectionListener(widgetSelectedAdapter(e-> moveBy(incrementValue)));
		incrementButton.addSelectionListener(widgetSelectedAdapter(e-> scheduleUpdateReadbackJob()));
	}

	/**
	 * Sets a different name to be used in the GUI instead of the default which is the scannable name
	 * <p>
	 * After calling this method the control will be automatically redrawn.
	 *
	 * @param userUnits
	 *            The name to be used in the GUI
	 */
	public void setDisplayUnit(String userUnits) {
		Objects.requireNonNull(userUnits);
		this.userUnits = userUnits;
		displayUnitLabel.setText(userUnits);
		if (userUnits.isEmpty()) {
			((RowData)displayUnitLabel.getLayoutData()).exclude=true;
		}
		this.redraw();
	}

	private void moveBy(double amountToMove) {
		if (currentPosition == null) {
			final String message = String.format("Cannot move %s", getScannable().getName());
			final String reason = "Position is unknown";
			logger.error("{} : {}", message, reason);
			UIHelper.showError(message, reason);
		} else {
			if (getScannable() instanceof ITweakableScannableMotor) {
				tweak(amountToMove);
			} else {
				move(currentPosition + amountToMove);
			}
		}
	}

	private void tweak(double amountToMove) {
		if (!checkBatonHeld()) return;
		if (moveAllowed(currentPosition + amountToMove)) {
			if (amountToMove > 0) {
				try {
					((ITweakableScannableMotor) getScannable()).forward();
				} catch (MotorException e) {
					logger.error("Error while trying tweak forward", e);
				}
			} else if (amountToMove < 0) {
				try {
					((ITweakableScannableMotor) getScannable()).reverse();
				} catch (MotorException e) {
					logger.error("Error while trying tweak reverse", e);
				}
			}
		} else {
			logger.error("Cannot tweak {} by {} as the requested position is outside limits.", getScannable().getName(), amountToMove);

			String message = "Move disallowed";
			if (!reasonForDisallowingMove.isEmpty()) {
				message += ":\n" + reasonForDisallowingMove;
			}
			MessageDialog.openError(Display.getDefault().getActiveShell(), "Error moving device", message);
			setReasonForDisallowingMove("");
		}
	}

	@Override
	public void setScannable(Scannable scannable) {
		Objects.requireNonNull(scannable);
		if (scannable instanceof ITweakableScannableMotor) {
			if (incrementValue != DEFAULT_INCREMENT) {
				// live control bean configures the increment, set it to motor
				((ITweakableScannableMotor) scannable).setIncrement(incrementValue);
			} else {
				// use motor's tweak step in control system.
				double increment = ((ITweakableScannableMotor) scannable).getIncrement();
				setIncrement(increment);
			}
		}
		super.setScannable(scannable);
	}

	@Override
	protected boolean moveAllowed(Object newPosition) {
		double position = convertPosition(newPosition);
		final boolean allowed = checkLimits(position);
		if (!allowed) {
			setReasonForDisallowingMove(String.format("Position %s is outside the allowed limits [%s : %s]",
					fourDecimalPlaces.format(position), fourDecimalPlaces.format(lowerLimit), fourDecimalPlaces.format(upperLimit)));
		}
		return allowed;
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
	 * @param currentPosition
	 *            The newest position to display
	 * @param moving
	 *            Flag showing if the scannable is moving
	 */
	@Override
	protected void updatePositionerControl(final Object currentPosition, final boolean moving) {

		// Save the new position
		this.currentPosition = convertPosition(currentPosition);
		// Format current position using output format
		final String currentPositionString = String.format(getScannableOutputFormat(), this.currentPosition).trim();
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
				if (unitDisplayOutsideTextBox) {
					positionText.setText(currentPositionString);
				} else {
					positionText.setText(currentPositionString + " " + userUnits);
				}
			}
			// Update the controls enabled/disabled
			if (getScannable() instanceof ITweakableScannableMotor) {
				decrementButton.setEnabled(true);
				incrementButton.setEnabled(true);
			} else {
				decrementButton.setEnabled(!moving);
				incrementButton.setEnabled(!moving);
			}
			positionText.setEditable(!moving && !readOnlyPosition);

			//Update layout to ensure text boxes fit any new text
			this.requestLayout();
		});
	}

	/**
	 * Change the increment programmatically or from Spring bean definition
	 *
	 * @param increment
	 *            The new increment value
	 */
	public void setIncrement(double increment) {
		this.incrementValue = increment;
		this.incrementText.setText(String.valueOf(increment));
		if (getScannable() instanceof ITweakableScannableMotor) {
			// set increment value to the underlying motor
			((ITweakableScannableMotor)getScannable()).setIncrement(increment);
		}
	}

	/**
	 * Change the increment from a view's text box
	 *
	 * @param incrementText
	 *            The new increment value
	 */
	protected void setIncrement(String incrementText) {
		if (incrementText.isEmpty()) {
			return;
		}
		incrementValue = Double.parseDouble(incrementText);
		if (getScannable() instanceof ITweakableScannableMotor) {
			// set increment value to the underlying motor
			((ITweakableScannableMotor)getScannable()).setIncrement(incrementValue);
		}
	}

	public void setFixedIncrementInput() {
		incrementText.setEnabled(false);
		incrementText.setEditable(false);
	}

	private void determineUserUnits() {
		try {
			if (getScannable() instanceof ScannableMotionUnits) {
				setUserUnits(((ScannableMotionUnits) getScannable()).getUserUnits());
			} else {
				logger.debug("No user units available for {}", getScannable().getName());
			}
		} catch (Exception e) {
			logger.error("Error getting user limits for {}", getScannable().getName(), e);
		}
	}

	public void setUserUnits(String userUnits) {
		this.userUnits = userUnits;
	}

	/**
	 * Sets the description label for the composite to a bold font
	 */
	public void setLabelToBold() {
		FontDescriptor boldDescriptor = FontDescriptor.createFrom(displayNameLabel.getFont()).setStyle(SWT.BOLD);
		Font boldFont = boldDescriptor.createFont(displayNameLabel.getDisplay());
		displayNameLabel.setFont(boldFont);
		displayNameLabel.addDisposeListener(e -> displayNameLabel.getFont().dispose());
	}

	private void determineScannableLimits() {
		try {
			if (getScannable() instanceof IScannableMotor) {
				final IScannableMotor scannableMotor = (IScannableMotor) getScannable();
				lowerLimit = scannableMotor.getLowerInnerLimit();
				upperLimit = scannableMotor.getUpperInnerLimit();
			} else if (getScannable() instanceof ScannableMotion) {
				final ScannableMotion scannableMotion = (ScannableMotion) getScannable();
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
				logger.debug("No scannable units available for {}", getScannable().getName());
			} else {
				positionText.setToolTipText(lowerLimit + " : " + upperLimit);
			}
		} catch (Exception e) {
			logger.error("Error getting scannable limits for {}", getScannable().getName(), e);
		}
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

	private Double convertPosition(final Object currentPosition) {
		if (currentPosition.getClass().isArray()) {
			// The scannable returns an array assume the relevant value is the first and its a double
			return (Double) ((Object[]) currentPosition)[0];
		} else if (currentPosition instanceof Double) {
			return (Double) currentPosition;
		} else if (currentPosition instanceof Integer) {
			return Double.valueOf((Integer) currentPosition);
		} else {
			logger.error("Error while parsing current position of {}", getScannable().getName());
			return null;
		}
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		if (enabled) {
			// Set the increment text
			incrementText.setText(incrementValue.toString());

			determineScannableLimits();
			if (userUnits.isBlank()) {
				determineUserUnits();
			}
			if (unitDisplayOutsideTextBox) {
				setDisplayUnit(userUnits);
			}
		} else {
			// Clear the position and increments
			positionText.setText("");
			incrementText.setText("");
		}
		// Disable the controls
		positionText.setEnabled(enabled);
		incrementText.setEnabled(enabled);
		decrementButton.setEnabled(enabled);
		incrementButton.setEnabled(enabled);

		this.redraw();
	}

	public void setIncrementTextWidth(int incrementTextWidth) {
		this.incrementTextWidth = incrementTextWidth;
		((GridData) incrementText.getLayoutData()).widthHint = incrementTextWidth;
	}
	/**
	 * hides the increment control composite
	 * <p>
	 * This is useful if users don't use increment control and
	 * want more screen estate to display other live control elements
	 *
	 * @see #showIncrementControl()
	 */
	public void hideIncrementControl() {
		decrementButton.setVisible(false);
		((GridData)decrementButton.getLayoutData()).exclude=true;
		incrementButton.setVisible(false);
		((GridData)incrementButton.getLayoutData()).exclude=true;
		incrementText.setVisible(false);
		((GridData)incrementText.getLayoutData()).exclude=true;
		this.redraw();
	}

	/**
	 * shows the increment control composite
	 *
	 * @see #hideIncrementControl()
	 */
	public void showIncrementControl() {
		decrementButton.setVisible(true);
		((GridData)decrementButton.getLayoutData()).exclude=false;
		incrementButton.setVisible(true);
		((GridData)incrementButton.getLayoutData()).exclude=false;
		incrementText.setVisible(true);
		((GridData)incrementText.getLayoutData()).exclude=false;
		this.redraw();
	}
	/**
	 * check if the Increment Control composite is visible
	 * @return true if the increment control composite is visible
	 * @see #hideIncrementControl()
	 * @see #showIncrementControl()
	 */
	public boolean isIncrementControlVisible() {
		return decrementButton.isVisible();
	}

	protected Text getPositionText() {
		return positionText;
	}

	protected void setPositionText(Text positionText) {
		this.positionText = positionText;
	}

	protected Text getIncrementText() {
		return incrementText;
	}

	protected void setIncrementText(Text incrementText) {
		this.incrementText = incrementText;
	}

	protected Button getDecrementButton() {
		return decrementButton;
	}

	protected void setDecrementButton(Button decrementButton) {
		this.decrementButton = decrementButton;
	}

	protected Button getIncrementButton() {
		return incrementButton;
	}

	protected void setIncrementButton(Button incrementButton) {
		this.incrementButton = incrementButton;
	}

	protected boolean isReadOnlyPosition() {
		return readOnlyPosition;
	}

	protected void doIncrementValue() {
		moveBy(incrementValue);
	}

	protected void doDecrementValue() {
		moveBy(-incrementValue);
	}
}