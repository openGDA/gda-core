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

package gda.rcp.views.dashboard;

import gda.jython.JythonServerFacade;

public class SimpleScannableObject {

	private String scannableName, toolTip;
	private String lastPosition;
	private boolean valid = false;

	public SimpleScannableObject() {
	}

	public SimpleScannableObject(String name) {
		this.setScannableName(name);
	}

	public String getScannableName() {
		return scannableName;
	}

	public void setScannableName(String scannableName) {
		this.scannableName = scannableName;
		valid = true;
	}

	public String getToolTip() {
		return toolTip;
	}

	public void setToolTip(String toolTip) {
		this.toolTip = toolTip;
	}

	public String getFormattedLastPosition() {
		if (lastPosition == null)
			refresh();
		return lastPosition;
	}

	public void refresh() {
		if (!valid) {
			//don't keep trying (and logging) when server can't be contacted
			return;
		}
		try {
			lastPosition = JythonServerFacade.getInstance().evaluateCommand("gda.device.scannable.ScannableUtils.getFormattedCurrentPosition("
				+ scannableName + ")");
		} catch (Exception e) {

		}
		if (lastPosition == null) {
			//if it failed this time - don't try again
			lastPosition = "not available";
			valid = false;
		}
	}
}