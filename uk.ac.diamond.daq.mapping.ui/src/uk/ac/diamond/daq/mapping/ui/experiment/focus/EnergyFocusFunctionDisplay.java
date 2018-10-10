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

package uk.ac.diamond.daq.mapping.ui.experiment.focus;

import static uk.ac.diamond.daq.mapping.ui.experiment.focus.FocusScanUtils.displayError;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.function.ILinearFunction;

/**
 * Display and allow editing of a linear function representing an energy/focus mapping
 * <p>
 * Controls are written directly into the parent composite, which must have a grid layout with at least 2 columns
 */
public class EnergyFocusFunctionDisplay {
	private static final Logger logger = LoggerFactory.getLogger(EnergyFocusFunctionDisplay.class);

	private final ILinearFunction energyFocusFunction;

	private final ValueAndLengthUnits slopeDividendComposite;
	private final ValueAndLengthUnits interceptionComposite;
	private final ValueAndLabel slopeDivisorComposite;

	public EnergyFocusFunctionDisplay(Composite parent, ILinearFunction energyFocusFunction) {
		this.energyFocusFunction = energyFocusFunction;

		createLabel(parent, "Slope dividend:");
		slopeDividendComposite = new ValueAndLengthUnits(parent, energyFocusFunction.getSlopeDividend());

		createLabel(parent, "Interception:");
		interceptionComposite = new ValueAndLengthUnits(parent, energyFocusFunction.getInterception());

		createLabel(parent, "Slope divisor:");
		slopeDivisorComposite = new ValueAndLabel(parent, energyFocusFunction.getSlopeDivisor());
	}

	/**
	 * Update the GDA function with values from this display
	 */
	public void updateEnergyFocusFunction() {
		try {
			energyFocusFunction.setSlopeDividend(getSlopeDividend());
			energyFocusFunction.setInterception(getInterception());
			energyFocusFunction.setSlopeDivisor(getSlopeDivisor());
		} catch (Exception e) {
			displayError("Error updating energy focus function", e.getMessage(), logger);
		}
	}

	public String getSlopeDividend() {
		return slopeDividendComposite.getValue();
	}

	public String getInterception() {
		return interceptionComposite.getValue();
	}

	public String getSlopeDivisor() {
		return slopeDivisorComposite.getValue();
	}

	/**
	 * Update display from energyFocusFunction
	 */
	public void refresh() {
		slopeDividendComposite.updateValue(energyFocusFunction.getSlopeDividend());
		interceptionComposite.updateValue(energyFocusFunction.getInterception());
		slopeDivisorComposite.updateValue(energyFocusFunction.getSlopeDivisor());
	}

	private static Label createLabel(Composite parent, String text) {
		final Label label = new Label(parent, SWT.NONE);
		GridDataFactory.swtDefaults().applyTo(label);
		label.setText(text);
		return label;
	}

	private static Text createText(Composite parent, String text) {
		final Text textBox = new Text(parent, SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(textBox);
		textBox.setText(text);
		return textBox;
	}

	/**
	 * Composite to show the slope divisor, whose value can be changed, but whose units cannot.<br>
	 */
	private static class ValueAndLabel extends Composite {

		private final Text valueText;
		private final Label unitsLabel;

		public ValueAndLabel(Composite parent, String value) {
			super(parent, SWT.NONE);
			GridDataFactory.fillDefaults().grab(true, false).applyTo(this);
			GridLayoutFactory.fillDefaults().numColumns(2).applyTo(this);

			// The value has come from the linear function, so we can assume it's in a regular format
			final String[] parsedValue = value.split(" ");
			valueText = createText(this, parsedValue[0]);
			unitsLabel = createLabel(this, parsedValue[1]);
		}

		public String getValue() {
			return String.format("%s %s", valueText.getText(), unitsLabel.getText());
		}

		public void updateValue(String value) {
			final String[] parsedValue = value.split(" ");
			valueText.setText(parsedValue[0]);
			unitsLabel.setText(parsedValue[1]);
		}
	}

	/**
	 * Composite to show a length and units, where the user can change the value of both.
	 * <p>
	 * When the units are changed, the text in the text box is also changed, so the value itself stays the same.
	 */
	private static class ValueAndLengthUnits extends Composite {

		private final Text valueText;
		private final ComboViewer unitsCombo;
		private LengthUnits currentUnits;

		public ValueAndLengthUnits(Composite parent, String value) {
			super(parent, SWT.NONE);
			GridDataFactory.fillDefaults().grab(true, false).applyTo(this);
			GridLayoutFactory.fillDefaults().numColumns(2).applyTo(this);

			final String[] parsedValue = value.split(" ");
			valueText = createText(this, parsedValue[0]);

			unitsCombo = new ComboViewer(this, SWT.NONE | SWT.READ_ONLY);
			unitsCombo.add(LengthUnits.values());
			updateUnits(parsedValue[1]);
			unitsCombo.addSelectionChangedListener(this::handleSelectionChanged);
		}

		private void updateUnits(String units) {
			currentUnits = LengthUnits.fromInternalName(units);
			unitsCombo.setSelection(new StructuredSelection(currentUnits), true);
		}

		private void handleSelectionChanged(@SuppressWarnings("unused") SelectionChangedEvent event) {
			// If units have changed, update value accordingly
			final LengthUnits newUnits = (LengthUnits) unitsCombo.getStructuredSelection().getFirstElement();
			if (newUnits != currentUnits) {
				double oldValue;
				try {
					oldValue = Double.parseDouble(valueText.getText());
				} catch (Exception e) {
					displayError("Error changing units", "Current value is not valid: units not changed", logger);
					// Restore old units
					unitsCombo.setSelection(new StructuredSelection(currentUnits), true);
					return;
				}

				final double newValue = newUnits.fromMm(currentUnits.toMm(oldValue));
				valueText.setText(Double.toString(newValue));
				currentUnits = newUnits;
			}
		}

		public void updateValue(String value) {
			final String[] parsedValue = value.split(" ");
			valueText.setText(parsedValue[0]);
			updateUnits(parsedValue[1]);
		}

		public String getValue() {
			final LengthUnits selectedUnits = (LengthUnits) unitsCombo.getStructuredSelection().getFirstElement();
			return String.format("%s %s", valueText.getText(), selectedUnits.getInternalName());
		}

		/**
		 * Enum holding units and their conversion to and from mm
		 * <p>
		 * Each unit has a display value and an internal value, because we want to display microns as "µm", but the
		 * Quantity object stores them as "um".
		 */
		private enum LengthUnits {
			MM("mm", "mm", 1.0), UM("µm", "um", 1e3), NM("nm", "nm", 1e6);

			private final String displayName;
			private final String internalName;
			private final double conversionFactor;

			private static Map<String, LengthUnits> internalNameMap;

			LengthUnits(String displayName, String internalName, double conversionFactor) {
				this.displayName = displayName;
				this.internalName = internalName;
				this.conversionFactor = conversionFactor;
			}

			public double toMm(double value) {
				return value / conversionFactor;
			}

			public double fromMm(double value) {
				return value * conversionFactor;
			}

			private static void initialiseMap() {
				if (internalNameMap == null) {
					internalNameMap = new HashMap<>();
					for (LengthUnits unit : LengthUnits.values()) {
						internalNameMap.put(unit.getInternalName(), unit);
					}
				}
			}

			public static LengthUnits fromInternalName(String displayName) {
				initialiseMap();
				return internalNameMap.get(displayName);
			}

			@Override
			public String toString() {
				return displayName;
			}

			public String getInternalName() {
				return internalName;
			}
		}
	}
}
