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

import static org.eclipse.scanning.api.constants.PathConstants.X_AXIS_STEP;
import static org.eclipse.scanning.api.constants.PathConstants.Y_AXIS_STEP;
import static uk.ac.diamond.daq.mapping.ui.experiment.DataBinder.GREATER_THAN_ZERO;

import javax.measure.quantity.Length;

import org.eclipse.scanning.api.points.models.TwoAxisGridStepModel;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import uk.ac.gda.client.NumberAndUnitsComposite;

public class GridStepPathEditor extends AbstractGridPathEditor<TwoAxisGridStepModel> {

	@Override
	public Composite createEditorPart(Composite parent) {
		final Composite composite = super.createEditorPart(parent);

		new Label(composite, SWT.NONE).setText(getXAxisLabel() + " Step");
		final NumberAndUnitsComposite<Length> xStep = createNumberAndUnitsComposite(composite, getXAxisScannableName(), X_AXIS_STEP);
		grabHorizontalSpace.applyTo(xStep);

		binder.bind(xStep, X_AXIS_STEP, getModel(), GREATER_THAN_ZERO);

		new Label(composite, SWT.NONE).setText(getYAxisLabel() + " Step");
		final NumberAndUnitsComposite<Length> yStep = createNumberAndUnitsComposite(composite, getYAxisScannableName(), Y_AXIS_STEP);
		grabHorizontalSpace.applyTo(yStep);

		binder.bind(yStep, Y_AXIS_STEP, getModel(), GREATER_THAN_ZERO);

		makeCommonGridOptionsControls(composite);

		return composite;
	}

}
