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
	BEAM_SELECTOR("beam_selector", "selector", String.class),
	// A hutch shutter is an object which can open or close the beam either through a filter or a mechanical device.
	EH_SHUTTER("shutter", "shutter", String.class);

	public final String groupId;
	public final String scannableId;
	public final Class<String> scannableType;
	private DefaultManagedScannable(String groupId, String scannableId, Class<String> scannableType) {
		this.groupId = groupId;
		this.scannableId = scannableId;
		this.scannableType = scannableType;
	}
	public String getGroupId() {
		return groupId;
	}
	public String getScannableId() {
		return scannableId;
	}
	public Class<String> getScannableType() {
		return scannableType;
	}
}