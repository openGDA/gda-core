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

package uk.ac.diamond.daq.mapping.api;

/**
 * Class used in the mapping bean and the GUI define a Nexus template file and its activation state i.e. whether it is
 * to be applied to the current scan.
 */
public class TemplateFileWrapper {

	private String filePath;

	private boolean active;

	public TemplateFileWrapper() {
		// constructor for deserialisation
	}

	public TemplateFileWrapper(String filePath, boolean active) {
		this.filePath = filePath;
		this.active = active;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	@Override
	public String toString() {
		return "TemplateFileWrapper [filePath=" + filePath + ", active=" + active + "]";
	}
}
