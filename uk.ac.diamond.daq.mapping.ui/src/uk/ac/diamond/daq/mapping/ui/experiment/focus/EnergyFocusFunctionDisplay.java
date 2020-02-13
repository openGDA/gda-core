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

import static si.uom.NonSI.ELECTRON_VOLT;
import static tec.units.indriya.unit.MetricPrefix.KILO;
import static tec.units.indriya.unit.MetricPrefix.MICRO;
import static tec.units.indriya.unit.MetricPrefix.MILLI;
import static tec.units.indriya.unit.MetricPrefix.NANO;
import static tec.units.indriya.unit.Units.METRE;
import static uk.ac.diamond.daq.mapping.ui.experiment.focus.FocusScanUtils.displayError;
import static uk.ac.diamond.daq.mapping.ui.experiment.focus.FocusScanUtils.getInitialLengthUnit;

import java.util.Set;

import javax.measure.Quantity;
import javax.measure.Unit;
import javax.measure.quantity.Energy;
import javax.measure.quantity.Length;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;

import gda.function.ILinearFunction;
import uk.ac.gda.client.NumberAndUnitsComposite;

/**
 * Display and allow editing of a linear function representing an energy/focus mapping
 */
public class EnergyFocusFunctionDisplay {
	private static final Logger logger = LoggerFactory.getLogger(EnergyFocusFunctionDisplay.class);

	private static final int COMPOSITE_WIDTH = 200;

	private static final Unit<Length> MODEL_LENGTH_UNIT = MILLI(METRE);
	private static final Set<Unit<Length>> LENGTH_UNITS = ImmutableSet.of(MILLI(METRE), MICRO(METRE), NANO(METRE));
	private static final Unit<Length> INITIAL_LENGTH_UNIT = getInitialLengthUnit();

	private static final Unit<Energy> MODEL_ENERGY_UNIT = ELECTRON_VOLT;
	private static final Set<Unit<Energy>> ENERGY_UNITS = ImmutableSet.of(KILO(ELECTRON_VOLT), ELECTRON_VOLT);
	private static final Unit<Energy> INITIAL_ENERGY_UNIT = ELECTRON_VOLT;

	private final ILinearFunction<Energy, Length> energyFocusFunction;

	private final NumberAndUnitsComposite<Length> slopeDividendComposite;
	private final NumberAndUnitsComposite<Length> interceptionComposite;
	private final NumberAndUnitsComposite<Energy> slopeDivisorComposite;

	public EnergyFocusFunctionDisplay(Composite parent, ILinearFunction<Energy, Length> energyFocusFunction) {
		final Composite mainComposite = new Composite(parent, SWT.NONE);
		GridLayoutFactory.swtDefaults().numColumns(2).applyTo(mainComposite);
		this.energyFocusFunction = energyFocusFunction;

		slopeDividendComposite = createRow(mainComposite, "Slope dividend:", MODEL_LENGTH_UNIT, LENGTH_UNITS, INITIAL_LENGTH_UNIT);
		interceptionComposite = createRow(mainComposite, "Interception:", MODEL_LENGTH_UNIT, LENGTH_UNITS, INITIAL_LENGTH_UNIT);
		slopeDivisorComposite = createRow(mainComposite, "Slope divisor:", MODEL_ENERGY_UNIT, ENERGY_UNITS, INITIAL_ENERGY_UNIT);

		// Set initial values
		update();
	}

	/**
	 * Update the energy focus function (on the server) with values from this display
	 */
	public void updateEnergyFocusFunction() {
		try {
			energyFocusFunction.setSlopeDividend(slopeDividendComposite.getValueAsQuantity());
			energyFocusFunction.setInterception(interceptionComposite.getValueAsQuantity());
			energyFocusFunction.setSlopeDivisor(slopeDivisorComposite.getValueAsQuantity());
			logger.debug("Updated energy focus function: {}", energyFocusFunction.getAsString());
		} catch (Exception e) {
			displayError("Error updating energy focus function", e.getMessage(), logger);
		}
	}

	/**
	 * Update display from energyFocusFunction
	 */
	public void update() {
		final Quantity<Length> slopeDividend = energyFocusFunction.getSlopeDividend().asType(Length.class);
		slopeDividendComposite.setValue(slopeDividend.to(MODEL_LENGTH_UNIT).getValue().doubleValue());

		final Quantity<Length> interception = energyFocusFunction.getInterception().asType(Length.class);
		interceptionComposite.setValue(interception.to(MODEL_LENGTH_UNIT).getValue().doubleValue());

		final Quantity<Energy> slopeDivisor = energyFocusFunction.getSlopeDivisor().asType(Energy.class);
		slopeDivisorComposite.setValue(slopeDivisor.to(MODEL_ENERGY_UNIT).getValue().doubleValue());
	}

	/**
	 * Create a row consisting of a label and a {@link NumberAndUnitsComposite}
	 *
	 * @param parent
	 *            Composite on which to create the widgets
	 * @param text
	 *            Label text
	 * @param quantity
	 *            The {@link Quantity} that the {@link NumberAndUnitsComposite} should support
	 * @return The {@link NumberAndUnitsComposite} that has been created
	 */
	private static <Q extends Quantity<Q>> NumberAndUnitsComposite<Q> createRow(Composite parent, String text,
			Unit<Q> modelUnit, Set<Unit<Q>> validUnits, Unit<Q> initialUnit) {
		final Label label = new Label(parent, SWT.NONE);
		GridDataFactory.swtDefaults().applyTo(label);
		label.setText(text);

		final NumberAndUnitsComposite<Q> numberAndUnitsComposite = new NumberAndUnitsComposite<>(parent, SWT.NONE,
				modelUnit, validUnits, initialUnit);
		GridDataFactory.fillDefaults().hint(COMPOSITE_WIDTH, SWT.DEFAULT).grab(true, false).applyTo(numberAndUnitsComposite);

		return numberAndUnitsComposite;
	}
}
