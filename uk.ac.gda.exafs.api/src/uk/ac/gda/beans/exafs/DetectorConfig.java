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

package uk.ac.gda.beans.exafs;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Generic configuration describing how a detector should be setup before running a scan and if it should be used during
 * scan. Optional parameters :
 * <li>configName - name xml file that can be applied to detector to configure it
 * <li>scriptCommand - name of jython script file, or jython command to be run before configuring detector (run once
 * only, before first scan)
 * <li>several 'use' flags to control whether to use detector during scan, configure it with a file, run script/command.
 */
public class DetectorConfig implements Serializable {

	private String detectorName = ""; // name of detector/scannable on server
	private String description; // description of detector (visible to user in GUI)
	private String configFilename; // name of file that can be used to configure detector (e.g. xml file for fluo detectors)
	private String scriptCommand; // name of jython script/command to be run before configuring detector with xml file;
									// run just once, before first scan
	private boolean useDetectorInScan; // whether detector should be used in scan

	private Boolean useScriptCommand; // if true, then run script/command before config. detector (and allow user to
									  // edit 'beforeConfigScript' param the GUI.)
	private Boolean useConfigFile; // if true, then configure detector with xml file (and allow user to edit configFile
								   // in GUI)

	private List<String> extraDetectorNames;

	public DetectorConfig() {
	}

	public DetectorConfig(String detectorName) {
		this.detectorName = detectorName;
	}

	public String getDetectorName() {
		return detectorName;
	}

	public void setDetectorName(String detectorName) {
		this.detectorName = detectorName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getConfigFileName() {
		return configFilename;
	}

	public void setConfigFileName(String configFilename) {
		this.configFilename = configFilename;
	}

	public String getScriptCommand() {
		return scriptCommand;
	}

	public void setScriptCommand(String scriptCommand) {
		this.scriptCommand = scriptCommand;
	}

	public boolean isUseDetectorInScan() {
		return useDetectorInScan;
	}

	public void setUseDetectorInScan(boolean useDetectorInScan) {
		this.useDetectorInScan = useDetectorInScan;
	}

	public Boolean isUseScriptCommand() {
		return useScriptCommand;
	}

	public void setUseScriptCommand(Boolean useScriptCommand) {
		this.useScriptCommand = useScriptCommand;
	}

	public Boolean isUseConfigFile() {
		return useConfigFile;
	}

	public void setUseConfigFile(Boolean useConfigFile) {
		this.useConfigFile = useConfigFile;
	}

	public String[] getExtraDetectorNames() {
		return extraDetectorNames == null ? new String[]{} : extraDetectorNames.toArray(new String[] {});
	}

	public void setExtraDetectorNames(String[] extraDetectorNames) {
		this.extraDetectorNames = extraDetectorNames == null ? new ArrayList<>() : Arrays.asList(extraDetectorNames);
	}

	public List<String> getAllDetectorNames() {
		List<String> names = new ArrayList<>();
		names.add(detectorName);
		Collections.addAll(names, getExtraDetectorNames());
		return names;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((configFilename == null) ? 0 : configFilename.hashCode());
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((detectorName == null) ? 0 : detectorName.hashCode());
		result = prime * result + ((extraDetectorNames == null) ? 0 : extraDetectorNames.hashCode());
		result = prime * result + ((scriptCommand == null) ? 0 : scriptCommand.hashCode());
		result = prime * result + ((useConfigFile == null) ? 0 : useConfigFile.hashCode());
		result = prime * result + (useDetectorInScan ? 1231 : 1237);
		result = prime * result + ((useScriptCommand == null) ? 0 : useScriptCommand.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DetectorConfig other = (DetectorConfig) obj;
		if (configFilename == null) {
			if (other.configFilename != null)
				return false;
		} else if (!configFilename.equals(other.configFilename))
			return false;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		if (detectorName == null) {
			if (other.detectorName != null)
				return false;
		} else if (!detectorName.equals(other.detectorName))
			return false;
		if (extraDetectorNames == null) {
			if (other.extraDetectorNames != null)
				return false;
		} else if (!extraDetectorNames.equals(other.extraDetectorNames))
			return false;
		if (scriptCommand == null) {
			if (other.scriptCommand != null)
				return false;
		} else if (!scriptCommand.equals(other.scriptCommand))
			return false;
		if (useConfigFile == null) {
			if (other.useConfigFile != null)
				return false;
		} else if (!useConfigFile.equals(other.useConfigFile))
			return false;
		if (useDetectorInScan != other.useDetectorInScan)
			return false;
		if (useScriptCommand == null) {
			if (other.useScriptCommand != null)
				return false;
		} else if (!useScriptCommand.equals(other.useScriptCommand))
			return false;
		return true;
	}
}
