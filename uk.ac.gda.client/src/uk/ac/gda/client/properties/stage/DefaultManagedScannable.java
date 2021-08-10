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

package uk.ac.gda.client.properties.stage;

import uk.ac.gda.client.properties.stage.position.ScannableKeys;

/**
 * A predefined set of scannable.
 *
 * <p>
 * These definitions help to parametrise the GUI assuming that they are property configured in the client properties configuration.
 * This approach allows to loosely couple the GUI and the scannables but at the same time allows to build a client focused more on
 * the functionality than on a specific implementation.
 * </p>
 * <p>
 *  An example could be the experimental hutch shutter (EH_SHUTTER) which is an element common to most of the beamlines. Instead of directly binding the client code
 *  with a scannable name, {@link DefaultManagedScannable#EH_SHUTTER}, is an object which express, in the client, a specific functionality which can be
 *  configured in the properties.
 * </p>
 *
 * @see ScannableGroupProperties
 * @see ScannableProperties
 *
 * @author Maurizio Nagni
 */
public enum DefaultManagedScannable {
	// A beam selector is an object which can select a special type of beam (by energy, by size or other) either through a filter or a mechanical device.
	BEAM_SELECTOR("beam_selector", "selector"),
	// A hutch shutter is an object which can open or close the beam either through a filter or a mechanical device.
	EH_SHUTTER("shutter", "shutter"),
	// A moving workbench with multiple stages
	BASE_X("base_x", "selector");

	private final String groupId;
	private final String scannableId;
	private DefaultManagedScannable(String groupId, String scannableId) {
		this.groupId = groupId;
		this.scannableId = scannableId;
	}

	public ScannableKeys getScannableKey() {
		var result = new ScannableKeys();
		result.setGroupId(groupId);
		result.setScannableId(scannableId);
		return result;
	}
}