/*-
 * Copyright © 2021 Diamond Light Source Ltd.
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

package uk.ac.gda.client.properties.mode;

import uk.ac.gda.client.properties.stage.position.ScannableKeys;

/**
 * Defines a device status in a client mode
 *
 * @author Maurizio Nagni
 */
public class TestModeElement {

	/**
	 * Identifies a scannable by its keysID
	 */
	private ScannableKeys device;

	private boolean exclude;

	/**
	 * Identifies a scannable by its keysID
	 *
	 * @return a device name
	 */
	public ScannableKeys getDevice() {
		return device;
	}

	public void setDevice(ScannableKeys device) {
		this.device = device;
	}

	/**
	 * Declares if the element has to be excluded from the client acquisition operations
	 *
	 * @return {@code true} if is excluded, {@code false} otherwise.
	 */
	public boolean isExclude() {
		return exclude;
	}

	public void setExclude(boolean exclude) {
		this.exclude = exclude;
	}
}
