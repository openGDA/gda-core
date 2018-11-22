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
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.jscience.physics.quantities.Quantity;
import org.jscience.physics.units.Unit;

/**
 * A Composite for displaying a number with a unit.<br>
 * The selection of units supported is configured in the constructor.<br>
 * It supports JFace databinding via {@link NumberUnitsWidgetProperty} *
 * <p>
 * Generic parameter Q is the type of quantity this composite can be used to edit e.g.
 * {@link org.jscience.physics.quantities.Energy}, {@link org.jscience.physics.quantities.Length},
 * {@link org.jscience.physics.quantities.Duration}
 *
 * @author James Mudd
 */
public class NumberAndUnitsComposite<Q extends Quantity> extends Composite {
	/** The text field showing the numeric value */
	private final Text text;
	/** The combo box to select the units */
	private final ComboViewer unitsCombo;

	/** Used to format when the absolute number in the current units is <=1e-3 or >=1e3 */
	private final NumberFormat scientificFormat = new DecimalFormat("0.#####E0");
	/** Used to format when the absolute number in the current units is 1e-3< number <1e3 */
	private final NumberFormat decimalFormat = new DecimalFormat("0.#####");

	/** Units of the corresponding model value */
	private final Unit<Q> modelUnit;

	/** Cache the current units to allow the text to be updated when the units are changed */
	private volatile Unit<Q> currentUnit;

	/**
	 * Constructor for the case where only one unit is permitted
	 * <p>
	 * For parameters, see (@link
	 * {@link NumberAndUnitsComposite#NumberAndUnitsComposite(Composite, int, Unit, List, Unit)}
	 */
	public NumberAndUnitsComposite(Composite parent, int style, Unit<Q> modelUnit) {
		this(parent, style, modelUnit, Arrays.asList(modelUnit), modelUnit);
	}

	/**
	 * Constructor for the case where the initially-displayed unit is the same as the model unit
	 * <p>
	 * For parameters, see (@link
	 * {@link NumberAndUnitsComposite#NumberAndUnitsComposite(Composite, int, Unit, List, Unit)}
	 */
	public NumberAndUnitsComposite(Composite parent, int style, Unit<Q> modelUnit, List<Unit<Q>> validUnits) {
		this(parent, style, modelUnit, validUnits, modelUnit);
	}

	/**
	 * Constructor
	 *
	 * @param parent
	 *            parent composite
	 * @param style
	 *            SWT style
	 * @param modelUnit
	 *            Units of the model to which the composite is bound
	 * @param validUnits
	 *            Units that the user can choose for this value
	 * @param initialUnit
	 *            Units to be selected when the combo box is first displayed
	 */
	public NumberAndUnitsComposite(Composite parent, int style, Unit<Q> modelUnit, List<Unit<Q>> validUnits, Unit<Q> initialUnit) {
		super(parent, style);
		this.modelUnit = modelUnit;
		this.currentUnit = initialUnit;

		// Check that default units is one of the valid units
		if (!validUnits.contains(initialUnit)) {
			throw new IllegalArgumentException(String.format("Default unit '%s' is not one of the valid values", initialUnit));
		}

		GridDataFactory.fillDefaults().applyTo(this);
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(this);

		text = new Text(this, SWT.BORDER);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(text);
		// When the text is changed fire the event for the data binding.
		text.addModifyListener(e -> notifyListeners(SWT.Modify, null));
		// TODO Might want to add validation to stop people typing letters in but need to be very careful WRT data
		// binding.
		unitsCombo = new ComboViewer(this, SWT.NONE | SWT.READ_ONLY);
		unitsCombo.add(validUnits.stream().toArray());
		unitsCombo.setSelection(new StructuredSelection(initialUnit));
		unitsCombo.addSelectionChangedListener(this::handleUnitChange);
	}

	@SuppressWarnings("unchecked")
	private void handleUnitChange(SelectionChangedEvent event) {
		final Quantity currentValue = getValueAsQuantity(); // in "old" units
		currentUnit = (Unit<Q>) ((StructuredSelection) event.getSelection()).getFirstElement();
		setValue(currentValue.to(modelUnit).getAmount());
	}

	/**
	 * Get the current value in the text box without doing any units conversion
	 *
	 * @return current value in the text box as a double
	 */
	private double getTextAsDouble() {
		try {
			return Double.parseDouble(text.getText());
		} catch (NumberFormatException e) {
			// Expected when building the GUI
			return 0.0;
		}
	}

	/**
	 * Get the currently-displayed value in the form of a Quantity object
	 *
	 * @return Current value as a quantity
	 */
	public Quantity getValueAsQuantity() {
		return Quantity.valueOf(getTextAsDouble(), currentUnit);
	}

	/**
	 * Gets the value set in the UI in model units. Called by the data binding to update the model with changes by the
	 * user in the UI
	 *
	 * @return the value in model units
	 */
	public double getValue() {
		return getValueAsQuantity().to(modelUnit).getAmount();
	}

	/**
	 * Sets the value shown by this UI element. This is called by the data binding to update the UI with changes in the
	 * model. This should always be called in model units.
	 *
	 * @param value
	 *            The value to set in model units
	 */
	public void setValue(double value) {
		// Value in current units
		final double valueInCurrentUnits = Quantity.valueOf(value, modelUnit).to(currentUnit).getAmount();
		final double absValueInCurrentUnits = Math.abs(valueInCurrentUnits);

		// Check if the absolute the value is not exactly zero and larger than 1000 or smaller than 0.001
		// Check for != 0.0 so 0 is displayed as 0 not 0E0
		if (absValueInCurrentUnits != 0.0 && (absValueInCurrentUnits <= 1e-3 || absValueInCurrentUnits >= 1e3)) {
			text.setText(scientificFormat.format(valueInCurrentUnits));
		} else {
			text.setText(decimalFormat.format(valueInCurrentUnits));
		}
	}
}
