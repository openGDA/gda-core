/*-
 * Copyright © 2022 Diamond Light Source Ltd.
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

/*-
 * Copyright © 2022 Diamond Light Source Ltd.
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

package uk.ac.gda.devices.detector.xspress4;

/**
 * Logical names and default PV names and patterns to be used for generating XSpress PVs.
 */
public enum XspressPvName {

	// Detector control PVs
	UPDATE_ARRAYS_TEMPLATE(":UPDATE_ARRAYS"),
	ACQUIRE_TIME_TEMPLATE(":AcquireTime"),
	ACQUIRE(":Acquire"),
	NUM_IMAGES(":NumImages"),
	TRIGGER_MODE_TEMPLATE(":TriggerMode"),
	ARRAY_COUNTER(":ArrayCounter"),
	ARRAY_COUNTER_RBV(":ArrayCounter_RBV"),
	ROI_RES_GRADE_BIN(":ROI:BinY"),
	DTC_ENERGY_KEV(":DTC_ENERGY"),

	// Scalars, DTC value, MCA data PVs
	SCA_ARRAY_TEMPLATE(":C%d_SCAS"),
	SCA_TEMPLATE(":C%d_SCA%d:Value_RBV"),
	RES_GRADE_TEMPLATE(":C%d_SCA%d_RESGRADES"),
	ARRAY_DATA_TEMPLATE(":ARR%d:ArrayData"),
	DTC_FACTORS(":DTC_FACTORS"),

	// Time series array PVs
	SCA_TIMESERIES_TEMPLATE(":C%d_SCAS:%d:TSArrayValue"),
	SCA_TIMESERIES_ACQUIRE_TEMPLATE(":C%d_SCAS:TS:TSAcquire"),
	SCA_TIMESERIES_CURRENTPOINT_TEMPLATE(":C%d_SCAS:TS:TSCurrentPoint");

	private String pvName;

	private XspressPvName(String pvName) {
		this.pvName = pvName;
	}

	public String pvName() {
		return pvName;
	}
}
