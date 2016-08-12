/*-
 * Copyright © 2016 Diamond Light Source Ltd.
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

package gda.commandqueue;

import java.util.List;

import uk.ac.gda.client.experimentdefinition.IExperimentObject;

/**
 * Extends {@link CommandBean} to add additional fields describing the scan. This
 * bean should be used when the command in the command bean wraps an {@link IExperimentObject}.
 * Since the {@link IExperimentObject} cannot be easily serialized, useful information about
 * it is added here instead. In particular, the folderName, multiScanName and runName can
 * be used to retrieve the {@link IExperimentObject} on the client, e.g. in a handler.
 */
public class ExperimentCommandBean extends CommandBean {

	private String folderPath;

	private String multiScanName;

	private String runName;

	private List<String> files;

	/**
	 * Gets the folder path for the experiment. See {@link IExperimentObject#getFolder()}
	 * @return folder path
	 */
	public String getFolderPath() {
		return folderPath;
	}

	public void setFolderPath(String folderPath) {
		this.folderPath = folderPath;
	}

	/**
	 * Get the multi-scan name for the experiment. See {@link IExperimentObject#getMultiScanName()}
	 * @return multi-scan name
	 */
	public String getMultiScanName() {
		return multiScanName;
	}

	public void setMultiScanName(String multiScanName) {
		this.multiScanName = multiScanName;
	}

	/**
	 * Get the run name for the experiment. See {@link IExperimentObject#getRunName()}
	 * @return run name
	 */
	public String getRunName() {
		return runName;
	}

	public void setRunName(String runName) {
		this.runName = runName;
	}

	/**
	 * Get the file names for the experiment. See {@link IExperimentObject#getFiles()}
	 * @return file names
	 */
	public List<String> getFiles() {
		return files;
	}

	public void setFiles(List<String> files) {
		this.files = files;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((files == null) ? 0 : files.hashCode());
		result = prime * result + ((folderPath == null) ? 0 : folderPath.hashCode());
		result = prime * result + ((multiScanName == null) ? 0 : multiScanName.hashCode());
		result = prime * result + ((runName == null) ? 0 : runName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		ExperimentCommandBean other = (ExperimentCommandBean) obj;
		if (files == null) {
			if (other.files != null)
				return false;
		} else if (!files.equals(other.files))
			return false;
		if (folderPath == null) {
			if (other.folderPath != null)
				return false;
		} else if (!folderPath.equals(other.folderPath))
			return false;
		if (multiScanName == null) {
			if (other.multiScanName != null)
				return false;
		} else if (!multiScanName.equals(other.multiScanName))
			return false;
		if (runName == null) {
			if (other.runName != null)
				return false;
		} else if (!runName.equals(other.runName))
			return false;
		return true;
	}

}
