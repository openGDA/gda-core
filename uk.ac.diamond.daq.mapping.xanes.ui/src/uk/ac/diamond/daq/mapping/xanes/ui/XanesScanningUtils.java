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

package uk.ac.diamond.daq.mapping.xanes.ui;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.scanning.api.points.models.AxialMultiStepModel;
import org.eclipse.scanning.api.points.models.AxialStepModel;
import org.eclipse.scanning.api.points.models.IScanPathModel;

import uk.ac.diamond.daq.mapping.api.IMappingExperimentBean;
import uk.ac.diamond.daq.mapping.api.IScanModelWrapper;

public class XanesScanningUtils {
	/**
	 * The energy steps around the edge energy (EE)
	 * <p>
	 * For example, <code>-0.1, -0.020, 0.008</code> means "from (EE - 0.1) to (EE - 0.020), move in steps of 0.008"
	 */
	private static final double[][] xanesStepRanges = {
			{ -0.1, -0.020, 0.008 },
			{ -0.019, +0.020, 0.0005 },
			{ +0.021, +0.040, 0.001 },
			{ +0.041, +0.080, 0.002 },
			{ +0.084, +0.130, 0.004 },
			{ +0.136, +0.200, 0.006 } };

	private XanesScanningUtils() {
		// prevent instantiation
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
	public static IScanModelWrapper<IScanPathModel> getOuterScannable(IMappingExperimentBean mappingBean, String scannableName) {
		if (scannableName != null && scannableName.length() > 0) {
			final List<IScanModelWrapper<IScanPathModel>> outerScannables = mappingBean.getScanDefinition().getOuterScannables();
			for (IScanModelWrapper<IScanPathModel> scannable : outerScannables) {
				if (scannable.getName().equals(scannableName)) {
					return scannable;
				}
			}
		}
		return null;
	}

	/**
	 * Create a step model for each range of energies around the edge
	 *
	 * @param edgeEnergy
	 *            energy of the edge to be scanned
	 * @param energyScannableName
	 *            name of the scannable to control the energy
	 * @return corresponding step model
	 */
	public static IScanPathModel createModelFromEdgeSelection(double edgeEnergy, String energyScannableName) {
		final List<AxialStepModel> stepModels = new ArrayList<>(xanesStepRanges.length);

		// Create a step model for each range of energies around the edge
		for (double[] range : xanesStepRanges) {
			stepModels.add(new AxialStepModel(energyScannableName, roundDouble(edgeEnergy + range[0]), roundDouble(edgeEnergy + range[1]), range[2]));
		}

		// Create a multi-step model containing these step models
		return new AxialMultiStepModel(energyScannableName, stepModels);
	}

	private static double roundDouble(double input) {
		return BigDecimal.valueOf(input).setScale(7, RoundingMode.HALF_UP).doubleValue();
	}

}
