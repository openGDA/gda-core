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

package org.eclipse.scanning.device.ui.points;

/**
 * An object holding information about a type of scanpath model (e.g. LissajousModel)
 * A description of the path and its relevant parameters, a label for it in the ui,
 * whether it should be visible in certain UI elements, and a path for an icon for
 * it, usually existing in the rendering bundle
 *
 */

public class PointsModelDescriber {

	private String description;
	private String iconPath;
	private String label;
	private boolean visible;

	PointsModelDescriber(String description, String iconPath, String label, boolean visible){
		this.label = label;
		this.description = description;
		this.iconPath = iconPath;
		this.visible = visible;
	}

	public String getDescription() {
		return description;
	}
	public String getIconPath() {
		return iconPath;
	}
	public String getLabel() {
		return label;
	}
	public boolean isVisible() {
		return visible;
	}
	public void setVisible(boolean visible) {
		this.visible = visible;
	}

}
