/*-
 * Copyright © 2020 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.mapping.ui.experiment.copyscan;

import java.io.File;

/**
 * Configuration to be shared between the pages of the {@link CopyScanWizard}
 */
public class CopyScanConfig {

	/**
	 * Name of the Jython class to be generated
	 */
	private String className;

	/**
	 * Directory where the most recent class was saved.<br>
	 * This is used to set the initial directory for the the Save dialog.<br>
	 * This value is not persisted when the client is closed.
	 */
	private File lastSaveLocation;

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public File getLastSaveLocation() {
		return lastSaveLocation;
	}

	public void setLastSaveLocation(File lastSaveLocation) {
		this.lastSaveLocation = lastSaveLocation;
	}

}
