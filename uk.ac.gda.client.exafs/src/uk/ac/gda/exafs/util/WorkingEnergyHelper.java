/*-
 * Copyright © 2021 Diamond Light Source Ltd.
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

package uk.ac.gda.exafs.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.exafs.scan.ScanObject;
import uk.ac.gda.beans.exafs.IScanParameters;
import uk.ac.gda.client.experimentdefinition.ExperimentFactory;

public final class WorkingEnergyHelper {

	private static final Logger logger = LoggerFactory.getLogger(WorkingEnergyHelper.class);

	private WorkingEnergyHelper() {
		// This is a utility class, so hide the constructor
	}

	/**
	 * Returns a new {@link WorkingEnergyParams} object for the given scan parameters
	 * - see {@link WorkingEnergyParams#getWorkingEnergyParams(IScanParameters, String)}.
	 * @param params
	 * @param folderPath
	 * @return WorkingEnergyParams
	 * @throws Exception
	 */
	public static WorkingEnergyParams createFromScanParameters(IScanParameters params, String folderPath) throws Exception {
		WorkingEnergyParams p = new WorkingEnergyParams();
		return p.getWorkingEnergyParams(params, folderPath);
	}

	/**
	 * Get scan parameters from currently open set of experiment parameters, and return the
	 * start, end and 'working energy' by calling {@link #createFromScanParameters(IScanParameters, String)}.
	 *
	 * @return WorkingEnergy
	 * @throws Exception
	 */
	public static WorkingEnergyParams createFromScanParameters() throws Exception {
		final ScanObject ob = (ScanObject) ExperimentFactory.getExperimentEditorManager().getSelectedScan();
		if (ob != null) {
			final IScanParameters scanParams = ob.getScanParameters();
			String containingFolder = ob.getFolder().getLocation().toFile().getAbsolutePath();
			logger.debug("Getting working energy parameters from scan parameters {} in {}", ob.getScanFileName(), containingFolder);
			return createFromScanParameters(scanParams, containingFolder);
		}
		throw new Exception("Could not get scan data from Experiment Editor - unable to get working energy parameters");
	}

}
