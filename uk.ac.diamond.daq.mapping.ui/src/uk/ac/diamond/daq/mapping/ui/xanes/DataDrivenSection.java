/*-
 * Copyright © 2025 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.mapping.ui.xanes;

import static uk.ac.diamond.daq.mapping.ui.xanes.XanesScanningUtils.createAxialStepModel;
import static uk.ac.diamond.daq.mapping.ui.xanes.XanesScanningUtils.getOuterScannable;
import static uk.ac.diamond.daq.mapping.ui.xanes.XanesScanningUtils.roundDouble;

import java.util.Optional;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.scanning.api.points.models.IAxialModel;
import org.eclipse.swt.widgets.Composite;

import uk.ac.diamond.daq.mapping.api.IScanModelWrapper;
import uk.ac.diamond.daq.mapping.api.XanesEdgeParameters.EdgeToEnergy;
import uk.ac.diamond.daq.mapping.ui.experiment.OuterScannablesSection;

/**
 * Represents a parameter section within the Mapping UI for XANES experiments,
 * allowing users to select an element and its corresponding absorption edge.
 *
 * <p>This class extends {@link XanesParametersSection} and overrides methods
 * related to handling the element/edge selection combo. When a selection is made,
 * a step scan model is created based on the selected edge energy.</p>
 *
 * <p>For this specific experiment, a fixed number of points (5) is used to define
 * the scan range, centered around the selected edge energy with an optional energy offset.</p>
 */
public class DataDrivenSection extends XanesParametersSection {

	private static final int NUM_INITIAL_POINTS = 5;

	@Override
	protected XanesEdgeCombo createXanesCombo(Composite composite) {
		final XanesEdgeCombo elementsAndEdgeCombo = new XanesEdgeCombo(composite, elementAndEdgesList);
		elementsAndEdgeCombo.addSelectionChangedListener(e -> handleEdgeSelectionChanged(elementsAndEdgeCombo.getSelection()));
		return elementsAndEdgeCombo;
	}

	@Override
	protected void handleEdgeSelectionChanged(IStructuredSelection selection) {
		final EdgeToEnergy selectedEdge = (EdgeToEnergy) selection.getFirstElement();
		if (selectedEdge == null) {
			return;
		}

		final Optional<IScanModelWrapper<IAxialModel>> energyScannable = getOuterScannable(getBean(), energyScannableName);
		if (energyScannable.isPresent()) {
			double energyOffset = 0.0;
			if (energyOffsetSpinner != null ) {
				energyOffset = Double.parseDouble(energyOffsetSpinner.getText()) / 1000;
			}
			double edgeEnergy = roundDouble(selectedEdge.getEnergy() + energyOffset);
			energyScannable.get().setModel(createAxialStepModel(edgeEnergy, energyScannableName, NUM_INITIAL_POINTS));
		}

		// Refresh outer scannables section to update text box
		getView().getSection(OuterScannablesSection.class).updateControls();
	}
}
