/*-
 * Copyright © 2022 Diamond Light Source Ltd.
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

import static java.util.function.Predicate.not;

import java.util.Arrays;

import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.scanning.api.points.models.AxialArrayModel;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

public class AxialArrayPathEditor extends AbstractAxialPathEditor<AxialArrayModel> {

	private static final String PROPERTY_NAME_POSITIONS = "positions";
	private Text axisText;

	@Override
	public Composite createEditorPart(Composite parent) {
		composite = makeComposite(parent, 1);

		axisText = new Text(composite, SWT.BORDER);
		axisText.setToolTipText("Specify a list of positions, separated by commas");
		grabHorizontalSpace.applyTo(axisText);

		createBinding(axisText, PROPERTY_NAME_POSITIONS, double[].class, this::stringToDoubleArray, this::doubleArrayToString, this::validatePositions);

		return composite;
	}

	private String doubleArrayToString(double[] positions) {
		if (positions == null) return "";

		final String[] posStrings = Arrays.stream(positions)
				.mapToObj(this::doubleToString)
				.toArray(String[]::new);
		return String.join(",", posStrings);
	}

	private double[] stringToDoubleArray(String positionsString) {
		final String[] strings = positionsString.split(",");
		try {
			return Arrays.stream(strings)
					.map(String::trim)
					.filter(not(String::isEmpty))
					.mapToDouble(Double::parseDouble)
					.toArray();
		} catch (Exception e) {
			// return null for no value, a BeforeSetValidator will prevent this being set
			return null; // NOSONAR cannot convert to double array, validation should return an error
		}
	}

	private IStatus validatePositions(double[] positions) {
		return (positions != null || axisText.getText().isEmpty()) ? ValidationStatus.ok() :
			ValidationStatus.error("Text is incorrectly formatted");
	}

}
