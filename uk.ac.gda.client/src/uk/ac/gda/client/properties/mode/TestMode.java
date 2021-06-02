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

import java.util.List;

/**
 * Configures the client execution test mode.
 * <p>
 * The goal of this mode is to filter out from the acquisition request a subset of scannable.
 * One possible use is when the hutch is open; in this case the acquisition the safety inhibits the eh_shutter to be opened
 * and consequently the acquisition fails. Setting a configuraiton like
 * <pre>
 * client.modes.test.active = true
 * client.modes.test.elements[0].device = eh_shutter
 * client.modes.test.elements[0].exclude = true
 * </pre>
 *
 * Allows to execute the acquisition despite no bean is in the hutch.
 * </p>
 *
 * @author Maurizio Nagni
 */
public class TestMode {

	private List<TestModeElement> elements;

	/**
	 * Activate the mode
	 */
	private boolean active;

	/**
	 * The scannables considered by this mode
	 * @return a set of elements
	 */
	public List<TestModeElement> getElements() {
		return elements;
	}

	public void setElements(List<TestModeElement> elements) {
		this.elements = elements;
	}

	/**
	 * Declares the status of the mode
	 *
	 * @return {@code true} if the mode is active, {@code false} otherwise
	 */
	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

}
