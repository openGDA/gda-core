/*-
 * Copyright © 2012 Diamond Light Source Ltd.
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

package gda.gui.scriptcontroller.logging;

import gda.jython.scriptcontroller.logging.ScriptControllerLogResultDetails;
import gda.jython.scriptcontroller.logging.ScriptControllerLogResults;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

public class ScriptControllerLogFilter extends ViewerFilter {

	public static final String ALL = "All types";

	private String selectedType = ALL;

	public String getSelectedType() {
		return selectedType;
	}

	public void setSelectedType(String selectedType) {
		this.selectedType = selectedType;
	}

	@Override
	public boolean select(Viewer arg0, Object arg1, Object arg2) {

		if (selectedType == null || selectedType.isEmpty() || selectedType.equals(ALL)) {
			return true;
		}

		if (arg2 instanceof ScriptControllerLogResults) {
			String scriptName = ((ScriptControllerLogResults) arg2).getScriptName();
			return selectedType.equals(scriptName);
		} else if (arg2 instanceof ScriptControllerLogResultDetails && arg1 instanceof ScriptControllerLogResults) {
			String scriptName = ((ScriptControllerLogResults) arg1).getScriptName();
			return selectedType.equals(scriptName);
		}
		return false;
	}
}
