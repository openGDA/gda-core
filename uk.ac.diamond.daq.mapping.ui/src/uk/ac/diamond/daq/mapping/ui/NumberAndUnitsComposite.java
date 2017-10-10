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

package uk.ac.diamond.daq.mapping.ui;

import org.eclipse.core.databinding.conversion.NumberToStringConverter;
import org.eclipse.core.databinding.conversion.StringToNumberConverter;
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

	private static final String DEFAULT_UNITS_PROPERTY = "uk.ac.diamond.daq.mapping.ui.deaultUnits";

	/** The text field showing the numeric value */
	private final Text text;
	/** The combo box to select the units */
	private final ComboViewer unitsCombo;

	/** Use core data-binding's converters for String <-> Number */
	private final StringToNumberConverter stringToNumberConverter = StringToNumberConverter.toDouble(false);
	private final NumberToStringConverter numberToStringConverter = NumberToStringConverter.fromDouble(false);

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
		Number number = ((Number) stringToNumberConverter.convert(text.getText()));
		if (number == null) {
			return null;
		}
		double value = number.doubleValue();
		// Always have mm in the model so convert to mm and save current mm value
		currentValueMm = getUnits().toMm(value);
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
		double number = Double.parseDouble(numberToStringConverter.convert(value).toString());
		// Update the current Value
		currentValueMm = number;
		// We are always given mm so convert to the units for display
		text.setText(Double.toString(getUnits().fromMm(number)));
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
