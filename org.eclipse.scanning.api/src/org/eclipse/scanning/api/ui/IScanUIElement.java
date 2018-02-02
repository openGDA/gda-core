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

package org.eclipse.scanning.api.ui;

import org.eclipse.scanning.api.INameable;

/**
 * Represents an UI element with an enablement, a name and an icon path.
 */
public interface IScanUIElement extends INameable {

	/**
	 * Get the icon path of the scan element.
	 * @return icon path
	 */
	public String getIconPath();

	/**
	 * Get whether the scan element is enabled, i.e. will be included in the next scan
	 * @return <code>true</code> if the element will be included in the next scan
	 */
	public boolean isEnabled();

	/**
	 * Set the enablement of the scan element.
	 * @param enabled <code>true</code> to enable the element, <code>false</code> to disable it
	 */
	public void setEnabled(boolean enabled);

}
