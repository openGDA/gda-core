/*-
 * Copyright © 2016 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.devices.specs.phoibos.ui;

/**
 * A class to provide the constants for use with the SPECS UI
 *
 * This includes topic constants for the IEventBroker service and constants for referring to transient part data.
 *
 * You should not create instances of this class
 *
 * @author James Mudd
 *
 */
public final class SpecsUiConstants {

	// IEventBroker topic constants
	public static final String REGION_SELECTED_EVENT = "uk/ac/diamond/daq/devices/specs/phoibos/ui/regionSelectedEvent";
	public static final String OPEN_SEQUENCE_EVENT = "uk/ac/diamond/daq/devices/specs/phoibos/ui/openSequenceEvent";

	// Sequence Editor transient data keys
	public static final String OPEN_SEQUENCE = "uk/ac/diamond/daq/devices/specs/phoibos/ui/editors/openSequence";
	public static final String OPEN_SEQUENCE_FILE_PATH = "uk/ac/diamond/daq/devices/specs/phoibos/ui/editors/openSequenceFilePath";
	public static final String SAVED_SEQUENCE_HASH = "uk/ac/diamond/daq/devices/specs/phoibos/ui/editors/savedFileSequenceHash";
	public static final String SELECTED_REGION = "uk/ac/diamond/daq/devices/specs/phoibos/ui/editors/selectedRegion";

	private SpecsUiConstants() {
		// Prevent instances
	}

}
