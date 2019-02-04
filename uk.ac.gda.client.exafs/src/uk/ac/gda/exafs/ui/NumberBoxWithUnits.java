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

package uk.ac.gda.exafs.ui;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.regex.Pattern;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NumberBoxWithUnits extends Composite {
	private static final Logger logger = LoggerFactory.getLogger(NumberBoxWithUnits.class);

	private Text textBox; // underlying SWT text widget
	private NumberFormat formatter; // formatter used to convert numerical value into a string
	private String units = ""; // optional units appended to number shown in textbox
	private Double numberValue; // numerical value displayed in textbox

	private boolean textBoxHasFocus;
	private boolean modified;
	private Double maximumValue;
	private Double minimumValue;

	// Pattern used to verify user input to Textbox is valid number
	private Pattern validNumberPattern = Pattern.compile(scientificNumberRegex);

	// Regex strings used for number validation :

	private static final String exponentRegex = "[eE]-?\\d*"; // 'e' or 'E', followed by optional -, followed by 0 or more digits

	// optional -, followed by 1 or more digits and optional (dot followed by 0 or more digits) followed by optional exponent
	private static final String scientificNumberRegex = "-?\\d+(\\.\\d*)?("+exponentRegex+")?";

	private static final String integerRegex = "-?\\d*";

	private boolean displayInteger = false;

	public NumberBoxWithUnits(Composite parent, int style) {
		super(parent, style);
		setLayout(new GridLayout());

		textBox = new Text(this, SWT.BORDER);
		textBox.setLayoutData(new GridData(SWT.LEFT, SWT.BEGINNING, false, false));

		textBox.addListener(SWT.FocusIn, event -> listenerForFocus(true));
		textBox.addListener(SWT.FocusOut, event -> listenerForFocus(false));
		textBox.addVerifyListener(doubleVerifier);

		setDisplayIntegers(false);

		numberValue = 0.0;
		textBox.setText(getTextBoxString(numberValue, true));
	}

	/**
	 * Switch between showing number with/without units when textbox loses/gains focus
	 * The numberValue is updated from textbox when textBox loses focus (i.e. user edit finishes)
	 * @param isFocused
	 */
	private void listenerForFocus(boolean isFocused) {
		boolean focusChange = this.textBoxHasFocus != isFocused;
		this.textBoxHasFocus = isFocused;
		logger.debug("Textbox focused ? {} Focus change {}. Current number value = {}", isFocused, focusChange, numberValue);
		if (isFocused) {
			// update the textbox to show the number, without units
			textBox.setText(getTextBoxString(numberValue, false));
		}else{
			// update textbox to show the formatted number with units
			updateTextbox();
		}
	}

	/**
	 * Update textbox with currently stored numerical value.
	 * The text colour will be set to red if {@link #isValueInRange()} returns false.
	 */
	public void updateTextbox() {
		setTextBoxColorFromLimits();
		textBox.setText(getTextBoxString(numberValue, true));
	}

	/**
	 * Set foreground color of textbox to red if value is outside of limits.
	 */
	private void setTextBoxColorFromLimits() {
		// Set foreground color to red if number is out of range
		int swtColor = isValueInRange() ? SWT.COLOR_BLACK : SWT.COLOR_RED;
		Color color = this.getParent().getDisplay().getSystemColor(swtColor);
		textBox.setForeground(color);
	}

	@Override
	public void update() {
		super.update();
		updateTextbox();
	}

	/**
	 * Return formatted number string with units optionally appended to it.
	 * @param newValue numerical value to be formatted
	 * @param appendUnits whether to append unit string
	 * @return String with formatted number with unit string appended to it
	 */
	private String getTextBoxString(Double newValue, boolean appendUnits) {
		if (appendUnits) {
			return formatter.format(newValue)+" "+units;
		} else {
			return formatter.format(newValue);
		}
	}

	/**
	 * @return true if value has been modified by user editing
	 */
	public boolean modified() {
		return modified;
	}

	private VerifyListener doubleVerifier = verifyEvent -> {
		modified = false;
		// Only do number verification if textbox currently has focus for user input
		if (!textBoxHasFocus) {
			return;
		}
		String currentText = ((Text) verifyEvent.widget).getText();
		String newText = currentText.substring(0, verifyEvent.start) + verifyEvent.text	+ currentText.substring(verifyEvent.end);

		logger.debug("Verify text : current string = {}, new string = {}, textbox = {}", currentText, newText, textBox.getText());
		verifyEvent.doit = verifyNumber(newText);

		if(verifyEvent.doit) {
			// Set modified flag if numerical value has changed as result of editing
			try {
				// Empty string is allowed (common 'intermediate' string when entering a number).
				// Set to zero since this causes number parsing to fail...
				if (newText.isEmpty() || newText.equals("-")) {
					newText = "0";
				}
				Double newValue = NumberFormat.getInstance().parse(newText.toUpperCase()).doubleValue();
				logger.debug("New value : {}", newValue);
				if ( newValue.equals(numberValue) ) {
					logger.debug("Verify text : Numbers are the same");
					modified = false;
				} else {
					numberValue = newValue;
					modified = true;
				}

				// Update the text color (black for within limit, red if outside)
				setTextBoxColorFromLimits();

			} catch (ParseException e) {
				logger.error("Problem parsing number from textbox", e);
			}
		}
	};

	private boolean verifyNumber(String numberString) {
		// Empty strings are allowed
		if (numberString.isEmpty() ) {
			logger.debug("Regex : empty string");
			return true;
		}

		boolean regexMatch = validNumberPattern.matcher(numberString).matches();
		logger.debug("Regex matches {} : {}", numberString, regexMatch);
		return regexMatch;
	}

	/** Test if currently value stored is within acceptable limits i.e. [minimumValue ... maximumValue].
	 * If mininimumValue and/or maximumValue limit has not been set then that part or range is open ended
	 * (i.e. number is always valid for it).
	 * @return true/false
	 */
	public boolean isValueInRange() {
		boolean minValueOk = minimumValue == null ? true : numberValue.doubleValue()>minimumValue.doubleValue();
		boolean maxValueOk = maximumValue == null ? true : numberValue.doubleValue()<maximumValue.doubleValue();
		return minValueOk && maxValueOk;
	}

	/**
	 * Set format - how the numerical value is converted to string for display in textbox
	 *
	 * @param format
	 */
	public void setFormat(NumberFormat format) {
		this.formatter = format;
	}

	public void setFormat(String format) {
		this.formatter = new DecimalFormat(format);
	}

	/**
	 * Set string to be appended to the formatted number in the textbox (string showing number 'units')
	 *
	 * @param units
	 */
	public void setUnits(String units) {
		this.units = units;
	}

	public String getUnits() {
		return units;
	}

	/**
	 * Maximum 'allowed' value. Values above this will set the text colour to red
	 * @param maximum
	 */
	public void setMaximum(Double maximum) {
		this.maximumValue = maximum;
	}

	public Number getMaximumValue() {
		return maximumValue;
	}

	/**
	 * Minimum 'allowed' value. Values below this will set the text colour to red
	 * @param minimum
	 */
	public void setMinimum(Double minimum) {
		this.minimumValue = minimum;
	}

	public Number getMinimumvValue() {
		return minimumValue;
	}

	/**
	 *
	 * @return Numerical value currently displayed by the widget
	 */
	public Double getValue() {
		return numberValue;
	}

	/**
	 * Set the numerical value to be displayed by the widget.
	 * The number shown in widget is displayed according to the decimal/floating point format
	 * with any units appended to it.
	 * @param numberValue
	 */
	public void setValue(Double numberValue) {
		this.numberValue = numberValue;
		updateTextbox();
	}

	public Control getWidget() {
		return textBox;
	}

	/**
	 * Set up display format to use for numbers and pattern to use for number validation.
	 * depending on on value of 'displayInteger' :
	 * <li> true - setup for integers
	 * <li> false - setup for floating point numbers, scientific notation with optional exponent.
	 * @param displayInteger -
	 */
	public void setDisplayIntegers(boolean displayInteger) {
		this.displayInteger = displayInteger;
		if (displayInteger) {
			formatter = new DecimalFormat("0");
			validNumberPattern = Pattern.compile(integerRegex);
		} else {
			formatter = new DecimalFormat("0.#####E0");
			validNumberPattern = Pattern.compile(scientificNumberRegex);
		}
	}

	/**
	 * Useful for testing
	 * @param args
	 */
	public static void main(String[] args) {
		Display display = new Display();
		final Shell shell = new Shell(display);
		shell.setLayout(new GridLayout(2, true));
		shell.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true, 1, 1));

		Label label = new Label(shell, SWT.NONE);
		label.setText("NumberBoxWithUnits : ");
		label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true, 1, 1));

		NumberBoxWithUnits numberBox = new NumberBoxWithUnits(shell, SWT.NONE);
		numberBox.setDisplayIntegers(false);
		numberBox.setUnits("eV");
		numberBox.setMinimum(2050.0);
		numberBox.setMaximum(26000.0);
		numberBox.setValue(100.0);
		numberBox.getWidget().setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true, 1, 1));
		numberBox.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true, 1, 1));

		shell.open();

		// Set up the event loop.
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				// If no more entries in event queue
				display.sleep();
			}
		}
		display.dispose();
	}
}
