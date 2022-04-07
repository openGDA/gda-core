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
	NUM_IMAGES_RBV(":NumImages_RBV"),
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
	DTC_FACTOR_TEMPLATE(":C%d_DTC_FACTOR"),

	// Low and high window limits for scaler5 and scaler6
	// (bin range for in-window counts)
	SCA5_WINDOW_LOW(":C%d_SCA5_LLM"),
	SCA5_WINDOW_HIGH(":C%d_SCA5_HLM"),
	SCA6_WINDOW_LOW(":C%d_SCA6_LLM"),
	SCA6_WINDOW_HIGH(":C%d_SCA6_HLM"),

	// Time series array PVs
	SCA_TIMESERIES_TEMPLATE(":C%d_SCAS:%d:TSArrayValue"),
	SCA_TIMESERIES_ACQUIRE_TEMPLATE(":C%d_SCAS:TS:TSAcquire"),
	SCA_TIMESERIES_CURRENTPOINT_TEMPLATE(":C%d_SCAS:TS:TSCurrentPoint"),

	// Detector connection state PVs
	DETECTOR_STATE(":DetectorState_RBV"),
	CONNECT(":CONNECT"),
	DISCONNECT(":DISCONNECT"),
	DET_CONNECTED(":CONNECTED"),

	// Hdf file writing PVs
	NUM_CAPTURE(":NumCapture"),
	NUM_CAPTURE_RBV(":NumCapture_RBV"),
	NUM_CAPTURED_RBV(":NumCaptured_RBV"),
	CAPTURE_CONTOL(":Capture"),
	CAPTURE_RBV(":Capture_RBV"),
	HDF_FILEPATH(":FilePath"),
	HDF_FILEPATH_RBV(":FilePath_RBV"),
	HDF_FILENAME(":FileName"),
	HDF_FULL_FILENAME_RBV(":FullFileName_RBV"),

	// Odin specific PVs
	TRIGGER_DETECTOR (":TRIGGER"),

	//Odin Metawriter PVs (for writing scalar data to Hdf file)
	// In the Odin plugin with :META prefix
	META_FLUSH_RATE(":FlushRate"),
	META_FLUSH_RATE_RBV(":FlushRate_RBV"),
	META_FILENAME(":FileName"),
	META_OUTPUT_FILE(":OutputFile_RBV"), // full path to output file
	META_FRAMES_WRITTEN_RBV(":FramesWritten_RBV"),
	META_STOP(":Stop"),
	META_CONNECTED_RBV(":ProcessConnected_RBV"),
	META_ACQUISITION_ACTIVE_RBV(":AcquisitionActive_RBV"),
	META_WRITING_RBV(":Writing_RBV");

	private String pvName;

	private XspressPvName(String pvName) {
		this.pvName = pvName;
	}

	public String pvName() {
		return pvName;
	}
}

