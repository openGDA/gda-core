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

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.swtdesigner.SWTResourceManager;

import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.scannable.ScannablePositionChangeEvent;
import gda.factory.FactoryException;
import gda.jython.JythonServerFacade;

/**
 * A class which provides a GUI composite to allow easy control of a scannable.
 * <p>
 * It provides the current position which can be edited and set to new value.
 * <p>
 * The format of the displayed number will be specified by the scannable output format.
 */
public class InputTextComposite extends Composite {

	private static final Logger logger = LoggerFactory.getLogger(InputTextComposite.class);
	private static final int DEFAULT_TEXT_WIDTH = 30;

	// GUI Elements
	private Label displayNameLabel;
	private Text positionText;

	private Scannable scannable;
	private Double lowerLimit; // Use Double to allow null for if no limits are set
	private Double upperLimit;
	private String scannableName;
	private String userUnit;
	private String displayName; // Allow a different prettier name be used if required
	private int textWidth = DEFAULT_TEXT_WIDTH;
	private Label unitLabel;
	private boolean textInput;
	private boolean readOnly;
	/**
	 * Constructor
	 *
	 * @param parent
	 *            The parent composite
	 * @param style
	 *            SWT style parameter (Typically SWT.NONE)
	 */
	public InputTextComposite(Composite parent, int style) {
		super(parent, style);

		parent.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));
		parent.setBackgroundMode(SWT.INHERIT_FORCE);

		// Setup layout
		GridLayout gridLayout = new GridLayout(3, false);
		gridLayout.horizontalSpacing = 0;
		gridLayout.verticalSpacing = 0;
		gridLayout.marginHeight = 0;
		this.setLayout(gridLayout);

		// Name label
		displayNameLabel = new Label(this, SWT.NONE);
		displayNameLabel.setLayoutData(GridDataFactory.fillDefaults().hint(SWT.DEFAULT, SWT.DEFAULT).align(SWT.CENTER, SWT.CENTER).grab(true, false).create());

		// Position text box
		positionText = new Text(this, SWT.BORDER);
		positionText.setLayoutData(GridDataFactory.fillDefaults().hint(getTextWidth(), SWT.DEFAULT).align(SWT.CENTER, SWT.CENTER).grab(true, false).create());
		positionText.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent key) {
				// If enter was pressed set PV to new value
				if (key.character == SWT.CR) { // enter or numpad enter pressed
					// Get the new value from the text box
					if (!isTextInput()) {
						//input is number
						double newValue = Double.parseDouble(positionText.getText().split(" ")[0]);
						setToNewValue(newValue);
					} else {
						// input is String
						String newValue=positionText.getText();
						setToNewValue(newValue);
					}
				}
			}
		});

		// Name label
		unitLabel = new Label(this, SWT.NONE);
		unitLabel.setLayoutData(GridDataFactory.fillDefaults().hint(SWT.DEFAULT, SWT.DEFAULT).align(SWT.CENTER, SWT.CENTER).grab(true, false).create());

		// At this time the control is built but no scannable is set so disable it.
		disable();
	}

	/**
	 * Set the scannable to a new position by calling {@link Scannable} asynchronousMoveTo(position).
	 * Checks if the position is within limits and if the scannable is busy before moving
	 *
	 * @param position
	 *            The demanded position
	 */
	private void setToNewValue(double position) {
		boolean batonHeld = JythonServerFacade.getInstance().isBatonHeld();
		if (!batonHeld) {
			openMessageBox();
		} else if (checkLimits(position)) {
			try {
				if (!scannable.isBusy()) {
					scannable.asynchronousMoveTo(position);
				}
			} catch (DeviceException e) {
				logger.error("Error while trying to move {}", scannableName, e);
			}
		} else {
			showInputErrorBox();
		}
	}

	private void showInputErrorBox() {
		MessageDialog dialog = new MessageDialog(Display.getDefault().getActiveShell(), "Input Outside Limits", null,
				"Your input value is outside the limits allowed.", MessageDialog.ERROR, new String[] { "Ok" }, 0);
		dialog.open();
	}

	private void openMessageBox() {
		MessageDialog dialog = new MessageDialog(Display.getDefault().getActiveShell(), "Baton not held", null,
				"You do not hold the baton, please take the baton using the baton manager.", MessageDialog.ERROR, new String[] { "Ok" }, 0);
		dialog.open();
	}

	private void setToNewValue(String position) {
		boolean batonHeld = JythonServerFacade.getInstance().isBatonHeld();
		if (!batonHeld) {
			openMessageBox();
		} else {
			try {
				scannable.asynchronousMoveTo(position);
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
		if (upperLimit == null && lowerLimit == null) {
			return true; // Limits are not set
		} else if (upperLimit == null && lowerLimit != null) {
			return newPosition >= lowerLimit;
		} else if (upperLimit != null && lowerLimit == null) {
			return newPosition <= upperLimit;
		} else {
			return (newPosition >= lowerLimit && newPosition <= upperLimit);
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



	@Override
	public void setEnabled(boolean enabled) {
		if (enabled) {
			// When the control is enabled reconfigure to ensure values are current
			configure();
		} else { // disabled
			// Clear the position and increments
			positionText.setText("");
		}
		// Disable the controls
		displayNameLabel.setEnabled(enabled);
		if (isReadOnly()) {
			positionText.setEnabled(false);
		} else {
			positionText.setEnabled(enabled);
		}
		unitLabel.setEnabled(enabled);

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
		scannable.addIObserver(this::updateValue);
		this.addDisposeListener(e -> scannable.deleteIObserver(this::updateValue));
		//The original pulling current position call blocks when GUI is starting - not clear what caused it.
		try {
			scannable.reconfigure();
			updateValue(scannable.getPosition(), null);
		} catch (FactoryException exception) {
			logger.error("reconfigure scannable '{}' failed.", scannable.getName(), exception);
		} catch (DeviceException exception) {
			logger.error("Unable to get initial position of {}", scannable.getName(), exception);
		}
	}

	private void updateValue(Object arg, Object source) {
		Object[] argArray;
		if (arg instanceof ScannablePositionChangeEvent) {
			Display.getDefault().asyncExec(
					() -> positionText.setText(String.format(scannable.getOutputFormat()[0], (Double)((ScannablePositionChangeEvent) arg).newPosition)));
		} else if (arg.getClass().isArray()) {
			// EPICS monitor by default always sending array
			argArray = (Object[]) arg;
			Display.getDefault().asyncExec(() -> {
				// only display the 1st value.
				if (isTextInput()) {
					positionText.setText(argArray[0].toString());
				} else {
					positionText.setText(String.format(scannable.getOutputFormat()[0], argArray[0]));
				}
			});
		} else {
			Display.getDefault().asyncExec(() -> {
				// only display the 1st value.
				if (isTextInput()) {
					positionText.setText(arg.toString());
				} else {
					positionText.setText(String.format(scannable.getOutputFormat()[0], arg));
				}
			});
		}
	}

	public boolean isTextInput() {
		return textInput;
	}

	public void setTextInput(boolean textInput) {
		this.textInput = textInput;
	}

	public String getUserUnit() {
		return userUnit;
	}

	public void setUserUnit(String userUnit) {
		this.userUnit = userUnit;
		unitLabel.setText(userUnit);
		this.redraw();
	}

	public int getTextWidth() {
		return textWidth;
	}

	public void setTextWidth(int textWidth) {
		this.textWidth = textWidth;
		((GridData) positionText.getLayoutData()).widthHint = textWidth;
	}

	public boolean isReadOnly() {
		return readOnly;
	}

	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}

	public void setLowerLimit(Double lowerLimit2) {
		lowerLimit = lowerLimit2;
	}

	public void setUpperLimit(Double upperlimit2) {
		upperLimit = upperlimit2;
	}

	/**
	 * Sets the description label for the composite to a bold font
	 */
	public void setLabelToBold() {
    	FontDescriptor boldDescriptor = FontDescriptor.createFrom(displayNameLabel.getFont()).setStyle(SWT.BOLD);
		Font boldFont = boldDescriptor.createFont(displayNameLabel.getDisplay());
		displayNameLabel.setFont(boldFont);
    }
}