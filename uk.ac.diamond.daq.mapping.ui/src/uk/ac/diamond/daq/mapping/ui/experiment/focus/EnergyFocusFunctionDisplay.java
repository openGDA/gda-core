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

import static javax.measure.unit.NonSI.ELECTRON_VOLT;
import static javax.measure.unit.SI.KILO;
import static javax.measure.unit.SI.METER;
import static javax.measure.unit.SI.MICRO;
import static javax.measure.unit.SI.MILLI;
import static javax.measure.unit.SI.NANO;
import static uk.ac.diamond.daq.mapping.ui.experiment.focus.FocusScanUtils.displayError;
import static uk.ac.diamond.daq.mapping.ui.experiment.focus.FocusScanUtils.getInitialLengthUnit;

import java.util.List;

import javax.measure.quantity.Energy;
import javax.measure.quantity.Length;
import javax.measure.quantity.Quantity;
import javax.measure.unit.Unit;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.jscience.physics.amount.Amount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;

import gda.function.ILinearFunction;
import uk.ac.gda.client.NumberAndUnitsComposite;

/**
 * Display and allow editing of a linear function representing an energy/focus mapping
 */
public class EnergyFocusFunctionDisplay {
	private static final Logger logger = LoggerFactory.getLogger(EnergyFocusFunctionDisplay.class);

	private static final int COMPOSITE_WIDTH = 200;

	private static final Unit<Length> MODEL_LENGTH_UNIT = MILLI(METER);
	private static final List<Unit<Length>> LENGTH_UNITS = ImmutableList.of(MILLI(METER), MICRO(METER), NANO(METER));
	private static final Unit<Length> INITIAL_LENGTH_UNIT = getInitialLengthUnit();

	private static final Unit<Energy> MODEL_ENERGY_UNIT = ELECTRON_VOLT;
	private static final List<Unit<Energy>> ENERGY_UNITS = ImmutableList.of(KILO(ELECTRON_VOLT), ELECTRON_VOLT);
	private static final Unit<Energy> INITIAL_ENERGY_UNIT = ELECTRON_VOLT;

	private final ILinearFunction energyFocusFunction;

	private final NumberAndUnitsComposite<Length> slopeDividendComposite;
	private final NumberAndUnitsComposite<Length> interceptionComposite;
	private final NumberAndUnitsComposite<Energy> slopeDivisorComposite;

	public EnergyFocusFunctionDisplay(Composite parent, ILinearFunction energyFocusFunction) {
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
		final Amount<? extends Quantity> slopeDividend = energyFocusFunction.getSlopeDividend();
		slopeDividendComposite.setValue(slopeDividend.to(MODEL_LENGTH_UNIT).getEstimatedValue());

		final Amount<? extends Quantity> interception = energyFocusFunction.getInterception();
		interceptionComposite.setValue(interception.to(MODEL_LENGTH_UNIT).getEstimatedValue());

		final Amount<? extends Quantity> slopeDivisor = energyFocusFunction.getSlopeDivisor();
		slopeDivisorComposite.setValue(slopeDivisor.to(MODEL_ENERGY_UNIT).getEstimatedValue());
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
	private static <Q extends Quantity> NumberAndUnitsComposite<Q> createRow(Composite parent, String text,
			Unit<Q> modelUnit, List<Unit<Q>> validUnits, Unit<Q> initialUnit) {
		final Label label = new Label(parent, SWT.NONE);
		GridDataFactory.swtDefaults().applyTo(label);
		label.setText(text);

		final NumberAndUnitsComposite<Q> numberAndUnitsComposite = new NumberAndUnitsComposite<>(parent, SWT.NONE,
				modelUnit, validUnits, initialUnit);
		GridDataFactory.fillDefaults().hint(COMPOSITE_WIDTH, SWT.DEFAULT).grab(true, false).applyTo(numberAndUnitsComposite);

		return numberAndUnitsComposite;
	}
}
