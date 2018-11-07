/*-
 * Copyright © 2017 Diamond Light Source Ltd.
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

package uk.ac.gda.client;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import gda.configuration.properties.LocalProperties;

/**
 * A Composite for displaying a number with a unit. Currently this only supports mm, µm and nm but could be generalised.
 * It supports JFace databinding via {@link NumberUnitsWidgetProperty}
 *
 * @author James Mudd
 */
public class NumberAndUnitsComposite extends Composite {

	private static final String DEFAULT_UNITS_PROPERTY = "uk.ac.diamond.daq.mapping.ui.defaultUnits";

	/** The text field showing the numeric value */
	private final Text text;
	/** The combo box to select the units */
	private final ComboViewer unitsCombo;

	/** Used to format when the absolute number in the current units is <=1e-3 or >=1e3 */
	private final NumberFormat scientificFormat = new DecimalFormat("0.#####E0");
	/** Used to format when the absolute number in the current units is 1e-3< number <1e3 */
	private final NumberFormat decimalFormat = new DecimalFormat("0.#####");

	/** Cache the current value to allow the text to be updated when the units are changed */
	private volatile double currentValueMm;

	public NumberAndUnitsComposite(Composite parent, int style) {
		super(parent, style);

		GridDataFactory.fillDefaults().applyTo(this);
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(this);

		text = new Text(this, SWT.BORDER);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(text);
		// When the text is changed fire the event for the data binding.
		text.addModifyListener(e -> notifyListeners(SWT.Modify, null));
		// TODO Might want to add validation to stop people typing letters in but need to be very careful WRT data
		// binding.
		unitsCombo = new ComboViewer(this, SWT.NONE | SWT.READ_ONLY);
		unitsCombo.add(Units.values());
		final String defaultUnits = LocalProperties.get(DEFAULT_UNITS_PROPERTY, "mm").toUpperCase();
		unitsCombo.setSelection(new StructuredSelection(Units.valueOf(defaultUnits)));
		unitsCombo.addSelectionChangedListener(event -> setValue(currentValueMm));
	}

	/**
	 * Gets the value set in the UI in mm. Called by the data binding to update the model with changes by the user in
	 * the UI
	 *
	 * @return the value in mm
	 */
	public Object getValue() {
		try {
			final double value = Double.parseDouble(text.getText());

			// Always have mm in the model so convert to mm and save current mm value
			currentValueMm = getUnits().toMm(value);
		} catch (NumberFormatException e) {
			// Expected when building the GUI
		}
		return currentValueMm;
	}

	/**
	 * Sets the value shown by this UI element. This is called my the data binding to update the UI with changes in the
	 * model. This should always be called in mm.
	 *
	 * @param value
	 *            The value to set in mm
	 */
	public void setValue(Object value) {
		// This should only ever be called with a double so cast it
		double number = (double) value;
		// Update the current Value
		currentValueMm = number;

		// Value in current units
		final double valueInCurrentUnits = getUnits().fromMm(number);
		final double absValueInCurrentUnits = Math.abs(valueInCurrentUnits);

		// Check if the absolute the value is not exactly zero and larger than 1000 or smaller than 0.001
		// Check for != 0.0 so 0 is displayed as 0 not 0E0
		if (absValueInCurrentUnits != 0.0 && (absValueInCurrentUnits <= 1e-3 || absValueInCurrentUnits >= 1e3)) {
			text.setText(scientificFormat.format(valueInCurrentUnits));
		} else {
			text.setText(decimalFormat.format(valueInCurrentUnits));
		}
	}

	/**
	 * Gets the units currently selected in the UI
	 *
	 * @return The currently selected Units
	 */
	private Units getUnits() {
		return (Units) unitsCombo.getStructuredSelection().getFirstElement();
	}

	/**
	 * Enum holding units and there conversion to and from mm
	 */
	private enum Units {
		MM("mm", 1.0), UM("µm", 1e3), NM("nm", 1e6);

		private final String displayName;
		private final double conversionFactor;

		Units(String displayName, double conversionFactor) {
			this.displayName = displayName;
			this.conversionFactor = conversionFactor;
		}

		public double toMm(double value) {
			return value / conversionFactor;
		}

		public double fromMm(double value) {
			return value * conversionFactor;
		}

		@Override
		public String toString() {
			return displayName;
		}
	}

}
