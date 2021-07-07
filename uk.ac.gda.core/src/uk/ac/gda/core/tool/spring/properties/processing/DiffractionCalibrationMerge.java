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

package uk.ac.gda.core.tool.spring.properties.processing;

import uk.ac.gda.api.acquisition.configuration.processing.DiffractionCalibrationMergeRequest;

/**
 * Properties for the  {@code DiffractionCalibrationMergeRequestHandler}
 *
 * @author Maurizio Nagni
 */
public class DiffractionCalibrationMerge {

	private String datasetName;

	/**
	 * Returns the name of the dataset name, defined in Malcolm detectorTable, which, during the acquisition process,
	 * will append a specific calibration file.
	 *
	 * @return the datasetName name
	 *
	 * @see DiffractionCalibrationMergeRequest
	 */
	public String getDatasetName() {
		return datasetName;
	}

	public void setDatasetName(String datasetName) {
		this.datasetName = datasetName;
	}
}
