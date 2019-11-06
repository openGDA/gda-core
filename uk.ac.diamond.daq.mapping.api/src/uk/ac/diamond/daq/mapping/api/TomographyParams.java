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

package uk.ac.diamond.daq.mapping.api;

/**
 * Class to hold the data to be passed to a scripted tomography scan
 */
public class TomographyParams {

	private TomographyCalibrationData tomographyCalibration;
	private String[] processingFiles;
	private String visitId = "";

	public TomographyCalibrationData getTomographyCalibration() {
		return tomographyCalibration;
	}

	public void setTomographyCalibration(TomographyCalibrationData tomographyCalibration) {
		this.tomographyCalibration = tomographyCalibration;
	}

	public String[] getProcessingFiles() {
		return processingFiles;
	}

	public void setProcessingFiles(String[] processingFiles) {
		this.processingFiles = processingFiles;
	}

	public String getVisitId() {
		return visitId;
	}

	public void setVisitId(String visitId) {
		this.visitId = visitId;
	}
}
