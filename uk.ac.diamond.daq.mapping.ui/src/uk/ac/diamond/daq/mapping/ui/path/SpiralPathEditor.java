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

package uk.ac.diamond.daq.mapping.ui.path;

import static tec.units.indriya.unit.MetricPrefix.MILLI;
import static tec.units.indriya.unit.Units.METRE;

import javax.measure.Unit;
import javax.measure.quantity.Length;

import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.scanning.api.points.models.TwoAxisSpiralModel;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import gda.util.QuantityFactory;
import uk.ac.diamond.daq.mapping.ui.experiment.UnitsProvider;
import uk.ac.gda.client.NumberAndUnitsComposite;

public class SpiralPathEditor extends AbstractPathEditor<TwoAxisSpiralModel> {

	@Override
	public Composite createEditorPart(Composite parent) {

		final Composite composite = super.createEditorPart(parent);
		final Label scaleLabel = new Label(composite, SWT.NONE);
		scaleLabel.setText("Scale");

		/*
		 * Scale is not backed by an actual scannable, but its value is treated approximately as millimetres, so simulate
		 * this here.
		 */
		final TwoAxisSpiralModel model = getModel();
		final Unit<Length> modelUnit = MILLI(METRE);
		final Unit<Length> initialScaleUnit = QuantityFactory.createUnitFromString(model.getInitialScaleUnit());
		@SuppressWarnings({ "rawtypes", "unchecked" })
		final NumberAndUnitsComposite<Length> scaleText = new NumberAndUnitsComposite(composite, SWT.NONE, modelUnit,
				UnitsProvider.getCompatibleUnits(modelUnit), initialScaleUnit);

		grabHorizontalSpace.applyTo(scaleText);

		binder.bind(scaleText, "scale", model,
				val -> ((double) val == 0.0) ? ValidationStatus.error("Scale cannot be zero!") : ValidationStatus.ok());

		makeCommonOptionsControls(composite);

		final String scaleDescription = "This parameter gives approximately both "
				+ "the distance between arcs and the arclength between consecutive points.";

		scaleLabel.setToolTipText(scaleDescription);
		scaleText.setToolTipText(scaleDescription);

		return composite;
	}

}
