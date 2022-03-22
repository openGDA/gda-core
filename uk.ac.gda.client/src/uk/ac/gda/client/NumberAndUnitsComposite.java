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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.measure.Quantity;
import javax.measure.Unit;
import javax.measure.quantity.Energy;
import javax.measure.quantity.Length;
import javax.measure.quantity.Time;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import gda.configuration.properties.LocalProperties;
import gda.jscience.physics.units.NonSIext;
import tec.units.indriya.quantity.Quantities;

/**
 * A Composite for displaying a number with a unit.<br>
 * The selection of units supported is configured in the constructor.<br>
 * It supports JFace databinding via {@link NumberUnitsWidgetProperty} *
 * <p>
 * Generic parameter Q is the type of quantity this composite can be used to edit e.g. {@link Energy}, {@link Length},
 * {@link Time}
 * <p>
 * If the style READ_ONLY is not specified, the composite contains a text box with the numeric value
 * (displayed in scientific format if appropriate) and a drop-down box to choose units:<br>
 * <img src="NumberAndUnitsComposite-microns.png"><br>
 * It automatically converts the numeric value when the units are changed:<br>
 * <img src="NumberAndUnitsComposite-nanometres.png"><br>
 * If the style READ_ONLY is specified, then a read-only label will be created to display the units instead of a drop-down box.
 *
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>READ_ONLY, NO_BACKGROUND, NO_FOCUS, NO_MERGE_PAINTS, NO_REDRAW_RESIZE, NO_RADIO_GROUP, EMBEDDED, DOUBLE_BUFFERED</dd>
 * <dt><b>Events:</b></dt>
 * <dd>(none)</dd>
 * </dl>
 *
 *
 * @author James Mudd
 * @author Anthony Hull
 */
public class NumberAndUnitsComposite<Q extends Quantity<Q>> extends Composite {

	public interface UnitSelectionChangedListener<Q extends Quantity<Q>> {
		public void unitSelectionChanged(Unit<Q> newUnit);
	}

	/** Display 3 decimal places by default */
	private static final String DEFAULT_DECIMAL_FORMAT = "0.###";
	/** Used to format when the absolute number in the current units is 1e-3< number <1e3 */
	private static final String PROPERTY_DECIMAL_FORMAT = "gda.client.decimalFormat";

	/** The text field showing the numeric value */
	private final Text numberText;
	/** The combo box to select the units */
	private ComboViewer unitsComboViewer;

	/** Units of the corresponding model value */
	private final Unit<Q> modelUnit;
	/** Cache the current units to allow the text to be updated when the units are changed */
	private volatile Unit<Q> currentUnit;

	private NumberFormat decimalFormat;
	private NumberFormat scientificFormat;

	private double valueInCurrentUnits;
	private boolean displayingFormattedData;

	private final List<UnitSelectionChangedListener<Q>> unitSelectionChangeListeners = new ArrayList<>(1);

	/**
	 * Constructor for the case where only one unit is permitted
	 * <p>
	 * For parameters, see (@link
	 * {@link NumberAndUnitsComposite#NumberAndUnitsComposite(Composite, int, Unit, Set, Unit)}
	 */
	public NumberAndUnitsComposite(Composite parent, int style, Unit<Q> modelUnit) {
		this(parent, style, modelUnit, Collections.singleton(modelUnit), modelUnit);
	}

	/**
	 * Constructor for the case where the initially-displayed unit is the same as the model unit
	 * <p>
	 * For parameters, see (@link
	 * {@link NumberAndUnitsComposite#NumberAndUnitsComposite(Composite, int, Unit, Set, Unit)}
	 */
	public NumberAndUnitsComposite(Composite parent, int style, Unit<Q> modelUnit, Set<Unit<Q>> validUnits) {
		this(parent, style, modelUnit, validUnits, modelUnit);
	}

	/**
	 * General constructor to create a new {@link NumberAndUnitsComposite}.
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
	public NumberAndUnitsComposite(Composite parent, int style, Unit<Q> modelUnit, Set<Unit<Q>> validUnits, Unit<Q> initialUnit) {
		super(parent, style);
		this.modelUnit = modelUnit;
		this.currentUnit = initialUnit;

		// Check that default units is one of the valid units
		if (!validUnits.contains(initialUnit)) {
			throw new IllegalArgumentException(String.format("Default unit '%s' is not one of the valid values", initialUnit));
		}

		GridDataFactory.fillDefaults().applyTo(this);
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(this);

		numberText = createNumberControl();

		// TODO Might want to add validation to stop people typing letters in but need to be very careful WRT data binding.
		createUnitsControl(validUnits, initialUnit);

		String format = getDecimalFormat();
		decimalFormat = new DecimalFormat(format);
		scientificFormat = new DecimalFormat(format + "E0");
	}

	private Text createNumberControl() {
		final Text text = new Text(this, SWT.BORDER);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(text);
		// When the text is changed fire the event for the data binding.
		text.addModifyListener(e -> {
			cacheValue();
			notifyListeners(SWT.Modify, null);
		});

		return text;
	}

	private void createUnitsControl(Set<Unit<Q>> validUnits, Unit<Q> initialUnit) {
		if ((getStyle() & SWT.READ_ONLY) != 0) {
			final Label label = new Label(this, SWT.NONE);
			label.setText(getLabel(initialUnit));
		} else {
			unitsComboViewer = new ComboViewer(this, SWT.NONE | SWT.READ_ONLY);
			unitsComboViewer.setContentProvider(ArrayContentProvider.getInstance());
			unitsComboViewer.setInput(validUnits);
			unitsComboViewer.setSelection(new StructuredSelection(initialUnit));
			unitsComboViewer.addSelectionChangedListener(this::handleUnitChange);
			unitsComboViewer.setLabelProvider(LabelProvider.createTextProvider(this::getLabel));
			unitsComboViewer.getCombo().addListener(SWT.MouseWheel, evt -> evt.doit = false);
		}
	}

	private String getLabel(Object element) {
		if (element instanceof Unit<?>) {
			return NonSIext.getUnitString((Unit<?>) element);
		}
		throw new IllegalArgumentException("Unexpected element type: " + element.getClass());
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void handleUnitChange(SelectionChangedEvent event) {
		final Quantity<? extends Quantity<?>> currentValue = getValueAsQuantity(); // in "old" units
		currentUnit = (Unit<Q>) ((StructuredSelection) event.getSelection()).getFirstElement();
		setValue(((Quantity) currentValue).to(modelUnit).getValue().doubleValue());

		unitSelectionChangeListeners.forEach(listener -> listener.unitSelectionChanged(currentUnit));
	}

	/**
	 * Cache the not formatted currently-displayed value
	 */
	private void cacheValue() {
		if (!displayingFormattedData) {
			valueInCurrentUnits = Double.parseDouble(numberText.getText());
		}
	}

	/**
	 * Get the currently-displayed value in the form of a Quantity object
	 *
	 * @return Current value as a quantity
	 */
	public Quantity<Q> getValueAsQuantity() {
		return Quantities.getQuantity(valueInCurrentUnits, currentUnit);
	}

	/**
	 * Gets the value set in the UI in model units. Called by the data binding to update the model with changes by the
	 * user in the UI
	 *
	 * @return the value in model units
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public double getValue() {
		return ((Quantity) getValueAsQuantity()).to(modelUnit).getValue().doubleValue();
	}

	/**
	 * Sets the value shown by this UI element. This is called by the data binding to update the UI with changes in the
	 * model. This should always be called in model units.
	 *
	 * @param value
	 *            The value to set in model units
	 */
	public void setValue(double value) {
		valueInCurrentUnits = Quantities.getQuantity(value, modelUnit).to(currentUnit).getValue().doubleValue();
		final double absValueInCurrentUnits = Math.abs(valueInCurrentUnits);

		displayingFormattedData = true;

		// Check if the absolute the value is not exactly zero and larger than 1000 or smaller than 0.001
		// Check for != 0.0 so 0 is displayed as 0 not 0E0
		if (absValueInCurrentUnits != 0.0 && (absValueInCurrentUnits <= 1e-3 || absValueInCurrentUnits >= 1e3)) {
			numberText.setText(scientificFormat.format(valueInCurrentUnits));
		} else {
			numberText.setText(decimalFormat.format(valueInCurrentUnits));
		}

		displayingFormattedData = false;
	}

	public void setUnit(Unit<Q> unit) {
		if (unitsComboViewer != null) {
			unitsComboViewer.setSelection(new StructuredSelection(unit));
		}
	}

	public Unit<Q> getUnit() {
		return currentUnit;
	}

	public void addUnitSelectionChangedListener(UnitSelectionChangedListener<Q> listener) {
		unitSelectionChangeListeners.add(listener);
	}

	/**
	 * Gets the value set in local properties in decimal format
	 * If it is not set, the default decimal format is used instead
	 * @return the number of decimal places to display in the mapping GUI
	 */
	public String getDecimalFormat() {
		return LocalProperties.get(PROPERTY_DECIMAL_FORMAT, DEFAULT_DECIMAL_FORMAT);
	}

	@Override
	public String toString() {
		return "NumberAndUnitsComposite [text=" + numberText + ", unitsComboViewer=" + unitsComboViewer + ", modelUnit=" + modelUnit
				+ ", currentUnit=" + currentUnit + ", decimalFormat=" + decimalFormat + ", scientificFormat="
				+ scientificFormat + ", valueInCurrentUnits=" + valueInCurrentUnits + ", displayingFormattedData="
				+ displayingFormattedData + "]";
	}
}
