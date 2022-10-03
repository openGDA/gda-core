/*-
 *******************************************************************************
 * Copyright (c) 2011, 2016 Diamond Light Source Ltd.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthew Gerring - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.scanning.sequencer.nexus;

import org.eclipse.dawnsci.nexus.NXentry;

public final class SolsticeConstants {

	/**
	 * System property used to determine the name of the main {@link NXentry} within the nexus file.
	 */
	public static final String SYSTEM_PROPERTY_NAME_ENTRY_NAME = "org.eclipse.scanning.nexusEntryName";

	public static final String SYSTEM_PROPERTY_NAME_INSTRUMENT = "GDA/gda.instrument";

	public static final String SYSTEM_PROPERTY_NAME_END_STATION = "GDA/gda.endstation.name";

	public static final String DEFAULT_ENTRY_NAME = "entry";

	public static final String DEFAULT_BEAMLINE_NAME = "base";

	/**
	 * Property name for the path within an external (linked) nexus file to the unique keys dataset.
	 */
	public static final String PROPERTY_NAME_UNIQUE_KEYS_PATH = "uniqueKeys";

	public static final String FIELD_NAME_SCAN_REQUEST  = "scan_request";

	public static final String FIELD_NAME_SCAN_MODELS   = "scan_models";

	public static final String FIELD_NAME_BEAMLINE = "beamline";

	public static final String FIELD_NAME_END_STATION = "end_station";

	private SolsticeConstants() {
		// private constructor to prevent instantiation
	}

}
