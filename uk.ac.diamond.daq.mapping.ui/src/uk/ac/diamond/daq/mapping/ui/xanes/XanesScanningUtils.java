/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.scanning.api.points.models.AxialMultiStepModel;
import org.eclipse.scanning.api.points.models.AxialStepModel;
import org.eclipse.scanning.api.points.models.IAxialModel;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import gda.configuration.properties.LocalProperties;
import gda.factory.Finder;
import uk.ac.diamond.daq.mapping.api.IMappingExperimentBean;
import uk.ac.diamond.daq.mapping.api.IScanModelWrapper;

public class XanesScanningUtils {
	/**
	 * The energy steps around the edge energy (EE)
	 * <p>
	 * For example, <code>-0.1, -0.020, 0.008</code> means "from (EE - 0.1) to (EE - 0.020), move in steps of 0.008"
	 */
	private static final double[][] STEP_RANGES = {
			{ -0.1,   -0.020, 0.008  },
			{ -0.019, +0.020, 0.0005 },
			{ +0.021, +0.040, 0.001  },
			{ +0.041, +0.080, 0.002  },
			{ +0.084, +0.130, 0.004  },
			{ +0.136, +0.200, 0.006  } };


	/**
	 * This constant defines the range around the edge energy (e.g., ±0.004 keV)
	 */
	private static final double RANGE_AROUND_EDGE = 0.004;

	public static final int CONTROLS_WIDTH = 70;

	private static final String PROPERTY_ENERGY_DEFAULT_UNITS = "gda.scan.energy.defaultUnits";

	private XanesScanningUtils() {

	}

	/**
	 * Get scan wrapper for scannable in OuterScannables section
	 *
	 * @param mappingBean
	 *            current mapping bean
	 * @param scannableName
	 *            name of the scannable to return
	 * @return {@link IScanModelWrapper} for the scannable
	 */

	public static Optional<IScanModelWrapper<IAxialModel>> getOuterScannable(IMappingExperimentBean mappingBean, String scannableName) {
		return mappingBean.getScanDefinition().getOuterScannables().stream()
				.filter(s -> s.getName().equals(scannableName)).findFirst();
	}

	/**
	 * Create a step model for each range of energies around the edge
	 *
	 * @param edgeEnergy
	 *            energy of the edge to be scanned (in keV units)
	 * @param energyScannableName
	 *            name of the scannable to control the energy
	 * @return corresponding step model
	 */
	public static AxialMultiStepModel createModelFromEdgeSelection(double edgeEnergy, String energyScannableName) {
		String energyUnits = LocalProperties.get(PROPERTY_ENERGY_DEFAULT_UNITS, "keV");

		// Create a step model for each range of energies around the edge
		var multiplier = energyUnits.equals("eV") ? 1000 : 1;
		final var stepModels = Arrays.stream(STEP_RANGES)
		      .map(range -> new AxialStepModel(energyScannableName,
				roundDouble((edgeEnergy + range[0])*multiplier),
				roundDouble((edgeEnergy + range[1])*multiplier),
				range[2]*multiplier))
		      .toList();


		// Create a multi-step model containing these step models
		final AxialMultiStepModel model = new AxialMultiStepModel(energyScannableName, stepModels);
		model.setContinuous(false);
		return model;
	}

	/**
	 *  Create a single step model around the energy of the edge selected,
	 *  with a predefined value for the range around the edge to be scanned,
	 *  and a fixed number of points.
	 *
	 * @param edgeEnergy
	 * 			energy of the edge to be scanned (in keV units)
	 * @param energyScannableName
	 * 			name of the scannable to control the energy
	 * @param numberOfPoints
	 * 			number of points in the step model
	 * @return corresponding step model
	 */
	public static AxialStepModel createAxialStepModel(double edgeEnergy, String energyScannableName, int numberOfPoints) {
	    String energyUnits = LocalProperties.get(PROPERTY_ENERGY_DEFAULT_UNITS, "keV");

	    var multiplier = energyUnits.equals("eV") ? 1000 : 1;

	    double rangeAroundEdge = RANGE_AROUND_EDGE * multiplier;

	    double start = roundDouble(edgeEnergy - rangeAroundEdge);
	    double stop = roundDouble(edgeEnergy + rangeAroundEdge);
	    double step = roundDouble((stop - start) / (numberOfPoints - 1));

	    AxialStepModel stepModel = new AxialStepModel(energyScannableName, start, stop, step);
	    stepModel.setContinuous(false);
	    return stepModel;
	}

	public static double roundDouble(double input) {
		return BigDecimal.valueOf(input).setScale(7, RoundingMode.HALF_UP).doubleValue();
	}

	public static String getComboEntry(XanesElement element) {
		final String entryFormat = element.isRadioactive() ? "*%s*" : "%s";
		return String.format(entryFormat, element.getElementName());
	}

	public static String getComboEntry(XanesElement element, String edge) {
		final String entryFormat = element.isRadioactive() ? "*%s-%s*" : "%s-%s";
		return String.format(entryFormat, element.getElementName(), edge);
	}

	public static Optional<XanesElementsList> getXanesElementsList() {
		final Map<String, XanesElementsList> elementsAndEdgesMap = Finder.getLocalFindablesOfType(XanesElementsList.class);
		return elementsAndEdgesMap.values().stream().findFirst();
	}

	public static Button createButton(Composite parent, int style, String text) {
		final Button button = new Button(parent, style);
		button.setText(text);
		return button;
	}

	public static Label createLabel(Composite parent, String text, int span) {
		final Label label = new Label(parent, SWT.WRAP);
		GridDataFactory.swtDefaults().span(span, 1).applyTo(label);
		label.setText(text);
		return label;
	}
}
