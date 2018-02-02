/*-
 * Copyright © 2018 Diamond Light Source Ltd.
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

package org.eclipse.scanning.api.scan.ui;

import org.eclipse.scanning.api.ui.IScanUIElement;

/**
 * A default implemementation of {@link IScanUIElement}, an element in the scan ui.
 */
public class ScanUIElement implements IScanUIElement {

	private String name;
	private String iconPath;
	private boolean enabled;

	public ScanUIElement() {
		// nothing to do
	}

	public ScanUIElement(String name, String iconPath) {
		this.name = name;
		this.iconPath = iconPath;
		this.enabled = false;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getIconPath() {
		return iconPath;
	}

	public void setIconPath(String iconPath) {
		this.iconPath = iconPath;
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	@Override
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	@Override
	public String toString() {
		return "ScanUIElement [name=" + name + ", iconPath=" + iconPath + ", enabled=" + enabled + "]";
	}

}
