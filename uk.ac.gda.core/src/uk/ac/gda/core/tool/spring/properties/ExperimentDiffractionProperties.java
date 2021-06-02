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
 * Contains properties to configure the diffraction file system supporting the experiment model.
 *
 * @author Maurizio Nagni
 *
 * @see <a href="https://confluence.diamond.ac.uk/display/DIAD/File+System">DIAD File System</a>
 */
public class ExperimentDiffractionProperties {

	private String directory;
	private String configurations;
	private String calibrations;

	public String getDirectory() {
		return directory;
	}

	public void setDirectory(String directory) {
		this.directory = directory;
	}

	public String getConfigurations() {
		return configurations;
	}

	public void setConfigurations(String configurations) {
		this.configurations = configurations;
	}

	public String getCalibrations() {
		return calibrations;
	}

	public void setCalibrations(String calibrations) {
		this.calibrations = calibrations;
	}
}
