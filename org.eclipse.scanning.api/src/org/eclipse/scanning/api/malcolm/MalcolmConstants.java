/*-
 * Copyright © 2017 Diamond Light Source Ltd.
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

package org.eclipse.scanning.api.malcolm;

public class MalcolmConstants {

	// TODO use enum for malcolm attributes?
	public static final String ATTRIBUTE_NAME_STATE = "state";
	public static final String ATTRIBUTE_NAME_HEALTH = "health";
	public static final String ATTRIBUTE_NAME_TOTAL_STEPS = "totalSteps";
	public static final String ATTRIBUTE_NAME_SIMULTANEOUS_AXES = "simultaneousAxes";
	public static final String ATTRIBUTE_NAME_LAYOUT = "layout";
	public static final String ATTRIBUTE_NAME_DATASETS = "datasets";
	public static final String ATTRIBUTE_NAME_COMPLETED_STEPS = "completedSteps";

	// the column names of the Datasets table,
	public static final String DATASETS_TABLE_COLUMN_NAME = "name";
	public static final String DATASETS_TABLE_COLUMN_FILENAME = "filename";
	public static final String DATASETS_TABLE_COLUMN_PATH = "path";
	public static final String DATASETS_TABLE_COLUMN_TYPE = "type";
	public static final String DATASETS_TABLE_COLUMN_RANK = "rank";
	public static final String DATASETS_TABLE_COLUMN_UNIQUEID = "uniqueid";

	// The names of the fields of the model used to configure a malcolm device
	public static final String FIELD_NAME_DETECTORS = "detectors";
	public static final String FIELD_NAME_GENERATOR = "generator";
	public static final String FIELD_NAME_AXES_TO_MOVE = "axesToMove";
	public static final String FIELD_NAME_FILE_DIR = "fileDir";
	public static final String FIELD_NAME_FILE_TEMPLATE = "fileTemplate";

	// the column names of the detectors table, this is the defaults field of the MethodMeta for the configure and validate methods
	// TODO move to internal constants? unlike the Datasets table, the names of the columns are not required externally
	public static final String DETECTORS_TABLE_COLUMN_NAME = "name";
	public static final String DETECTORS_TABLE_COLUMN_MRI = "mri";
	public static final String DETECTORS_TABLE_COLUMN_EXPOSURE = "exposure";
	public static final String DETECTORS_TABLE_COLUMN_FRAMES_PER_STEP = "framesPerStep";
	public static final String DETECTORS_TABLE_COLUMN_ENABLE = "enable";

	private MalcolmConstants() {
		// private constructor to prevent instantiation
	}

}
