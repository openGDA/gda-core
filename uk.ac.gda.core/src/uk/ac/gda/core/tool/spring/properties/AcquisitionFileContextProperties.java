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

package uk.ac.gda.core.tool.spring.properties;

/**
 * Contains properties to configure the file system supporting the beamline operations.
 *
 * @author Maurizio Nagni
 *
 * @see <a href="https://confluence.diamond.ac.uk/display/DIAD/File+System">DIAD File System</a>
 */
public class AcquisitionFileContextProperties {

	private ExperimentDiffractionProperties diffraction;

	private ExperimentImagingProperties imaging;

	private ExperimentProperties experiment;

	public ExperimentDiffractionProperties getDiffraction() {
		return diffraction;
	}

	public void setDiffraction(ExperimentDiffractionProperties diffraction) {
		this.diffraction = diffraction;
	}

	public ExperimentImagingProperties getImaging() {
		return imaging;
	}

	public void setImaging(ExperimentImagingProperties imaging) {
		this.imaging = imaging;
	}

	public ExperimentProperties getExperiment() {
		return experiment;
	}

	public void setExperiment(ExperimentProperties experiment) {
		this.experiment = experiment;
	}
}
