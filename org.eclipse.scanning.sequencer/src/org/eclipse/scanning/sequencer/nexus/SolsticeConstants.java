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

public final class SolsticeConstants {

	public static final String SCANNABLE_NAME_SOLSTICE_SCAN_MONITOR = "solsticeScanMonitor";
	public static final String GROUP_NAME_SOLSTICE_SCAN = "solstice_scan";
	public static final String GROUP_NAME_KEYS          = "keys";
	public static final String FIELD_NAME_UNIQUE_KEYS   = "uniqueKeys";
	public static final String FIELD_NAME_SCAN_RANK     = "scanRank";
	public static final String FIELD_NAME_SCAN_FINISHED = "scan_finished";
	public static final String FIELD_NAME_SCAN_CMD      = "scan_cmd";
	public static final String FIELD_NAME_SCAN_MODELS   = "scan_models";
	public static final String FIELD_NAME_SCAN_DURATION = "scan_duration";
	public static final String FIELD_NAME_SCAN_ESTIMATED_DURATION = "scan_estimated_duration";
	public static final String FIELD_NAME_SCAN_DEAD_TIME = "scan_dead_time";
	public static final String FIELD_NAME_SCAN_DEAD_TIME_PERCENT = "scan_dead_time_percent";
	public static final String FIELD_NAME_SCAN_SHAPE    = "scan_shape";

	/**
	 * Property name for the path within an external (linked) nexus file to the unique keys dataset.
	 */
	public static final String PROPERTY_NAME_UNIQUE_KEYS_PATH = "uniqueKeys";

	/**
	 * Property name for a property used by GDA9+ to suppress writing the global unique keys dataset for a scan.
	 * This dataset is not created if the value of this property is equal to {@link Boolean#TRUE} for any device a scan.
	 * The global unique keys dataset is normally written to at the end of each position in a scan. It is used
	 * to indicate that that position in a scan has been written to and processing can be performed. It should
	 * be suppressed if processing is required in between points of the scan. This can be the case if a device in the
	 * scan performs an inner scan, e.g. a malcolm device, or a detector writes data while a position in the scan is
	 * being performed that required processing before the position is completed.
	 */
	public static final String PROPERTY_NAME_SUPPRESS_GLOBAL_UNIQUE_KEYS = "suppressGlobalUniqueKeys";

	private SolsticeConstants() {
		// private constructor to prevent instantiation
	}

}
