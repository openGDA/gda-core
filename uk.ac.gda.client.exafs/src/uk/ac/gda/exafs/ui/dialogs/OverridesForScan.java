/*-
 * Copyright © 2017 Diamond Light Source Ltd.
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

package uk.ac.gda.exafs.ui.dialogs;

import java.util.ArrayList;
import java.util.List;

public class OverridesForScan {

	private List<OverridesForParametersFile> overridesForParametersFile;

	public OverridesForScan() {
		overridesForParametersFile = new ArrayList<OverridesForParametersFile>();
	}

	public void addOverride(String xmlName, String containingClassName) {
		OverridesForParametersFile overrideForFile = new OverridesForParametersFile(xmlName, containingClassName);
		addOverride(overrideForFile);
	}

	public void addOverride(OverridesForParametersFile f) {
		overridesForParametersFile.add(f);
	}

	public void setOverrides(List<OverridesForParametersFile> overridesForScanFiles) {
		overridesForParametersFile = overridesForScanFiles;
	}

	public List<OverridesForParametersFile> getOverrides() {
		return overridesForParametersFile;
	}

	public void clearOverrides() {
		overridesForParametersFile.clear();
	}
}
