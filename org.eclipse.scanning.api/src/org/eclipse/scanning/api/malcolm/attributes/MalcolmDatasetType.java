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
package org.eclipse.scanning.api.malcolm.attributes;

import org.eclipse.scanning.api.device.ScanRole;
import org.eclipse.scanning.api.malcolm.IMalcolmDevice;
import org.eclipse.scanning.api.malcolm.MalcolmConstants;
import org.eclipse.scanning.api.malcolm.MalcolmTable;

/**
 * An enumeration of the types of dataset that can be present in the
 * {@link MalcolmTable} which describes the datasets that a malcolm device will write
 * when a scan is run. This table can be accessed by calling
 * {@link IMalcolmDevice#getAttribute(String)} with the value
 * {@link MalcolmConstants#ATTRIBUTE_NAME_DATASETS}. The column
 * {@link MalcolmConstants#DATASETS_TABLE_COLUMN_TYPE} will have a value which is the
 * lower case string representation of one of the enum constants in this enum type.
 * The string can be converted to the appropriate enum constant by calling the
 * method {@link MalcolmDatasetType#fromString(String)}.
 *
 * @author Matthew Dickie
 */
public enum MalcolmDatasetType {

	/**
	 * A primary dataset for a detector. There should be exactly one such field for
	 * each detector controlled by malcolm.
	 */
	PRIMARY(ScanRole.DETECTOR),

	/**
	 * A secondary dataset for a detector. The may be zero or more such fields fo
	 * a detector
	 */
	SECONDARY(ScanRole.DETECTOR),

	/**
	 * The dataset for a monitor. Each monitor has its own dataset of this type.
	 */
	MONITOR(ScanRole.MONITOR_PER_POINT),

	/**
	 * The value dataset for a position. This dataset contains the actual value
	 * of the motor, a.k.a. the read-back value (rbv).
	 */
	POSITION_VALUE(ScanRole.SCANNABLE),

	/**
	 * The set value datsets for a position. This dataset contains the value that the
	 * motor was requested to move do, a.k.a. the demand value.
	 */
	POSITION_SET(ScanRole.SCANNABLE),

	/**
	 * A dataset containing the minimum position for a motor.
	 */
	POSITION_MIN(ScanRole.SCANNABLE),

	/**
	 * A dataset containing the maximum position for a motor.
	 */
	POSITION_MAX(ScanRole.SCANNABLE),

	/**
	 * This constant represents an unknown dataset type. This allows malcolm to add new dataset types
	 * without causing an error in GDA.
	 */
	UNKNOWN(null);

	private final ScanRole scanRole;

	private MalcolmDatasetType(ScanRole scanRole) {
		this.scanRole = scanRole;
	}

	public ScanRole getScanRole() {
		return scanRole;
	}

	public static MalcolmDatasetType fromString(String typeStr) {
		try {
			return valueOf(typeStr.toUpperCase());
		} catch (IllegalArgumentException e) {
			return UNKNOWN;
		}
	}

}
