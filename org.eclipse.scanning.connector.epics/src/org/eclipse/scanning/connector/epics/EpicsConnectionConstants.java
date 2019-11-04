/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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

package org.eclipse.scanning.connector.epics;

/**
 * Constants for communicating with a Malcolm Device over EPICS. For internal use only.
 */
public class EpicsConnectionConstants {

	public static final String TYPE_ID_KEY = "typeid";

	public static final String ID_PREFIX_EPICS = "epics:nt/";
	public static final String TYPE_ID_NT_SCALAR = ID_PREFIX_EPICS + "NTScalar:1.0";
	public static final String TYPE_ID_NT_SCALAR_ARRAY = ID_PREFIX_EPICS + "NTScalarArray:1.0";
	public static final String TYPE_ID_NT_TABLE = ID_PREFIX_EPICS + "NTTable:1.0";

	public static final String ID_PREFIX_MALCOLM = "malcolm:core/";
	public static final String TYPE_ID_ERROR = ID_PREFIX_MALCOLM + "Error:";
	public static final String TYPE_ID_MAP = ID_PREFIX_MALCOLM + "Map:1.0";
	public static final String TYPE_ID_TABLE = ID_PREFIX_MALCOLM + "Table:1.0";
	public static final String TYPE_ID_METHOD_LOG = ID_PREFIX_MALCOLM + "MethodLog:1.0";
	public static final String TYPE_ID_BLOCK = ID_PREFIX_MALCOLM + "Block:1.0";
	public static final String TYPE_ID_BLOCK_META = ID_PREFIX_MALCOLM + "BlockMeta:1.0";
	public static final String TYPE_ID_CHOICE_META = ID_PREFIX_MALCOLM + "ChoiceMeta:1.0";
	public static final String TYPE_ID_MAP_META = ID_PREFIX_MALCOLM + "MapMeta:1.0";
	public static final String TYPE_ID_METHOD = ID_PREFIX_MALCOLM + "Method:1.1";
	public static final String TYPE_ID_METHOD_META = ID_PREFIX_MALCOLM +"MethodMeta:1.1";
	public static final String TYPE_ID_STRING_META = ID_PREFIX_MALCOLM + "StringMeta:1.0";
	public static final String TYPE_ID_BOOLEAN_META = ID_PREFIX_MALCOLM + "BooleanMeta:1.0";
	public static final String TYPE_ID_NUMBER_META = ID_PREFIX_MALCOLM + "NumberMeta:1.0";
	public static final String TYPE_ID_TABLE_META = ID_PREFIX_MALCOLM + "TableMeta:1.0";
	public static final String TYPE_ID_NUMBER_ARRAY_META = ID_PREFIX_MALCOLM + "NumberArrayMeta:1.0";
	public static final String TYPE_ID_STRING_ARRAY_META = ID_PREFIX_MALCOLM + "StringArrayMeta:1.0";
	public static final String TYPE_ID_POINT_GENERATOR = ID_PREFIX_MALCOLM + "PointGenerator:1.0";
	public static final String TYPE_ID_POINT_GENERATOR_META = ID_PREFIX_MALCOLM + "PointGeneratorMeta:1.0";

	// Field names for attributes
	public static final String FIELD_NAME_META = "meta";
	public static final String FIELD_NAME_VALUE = "value";
	public static final String FIELD_NAME_DTYPE = "dtype";
	public static final String FIELD_NAME_DESCRIPTION = "description";
	public static final String FIELD_NAME_WRITEABLE = "writeable";
	public static final String FIELD_NAME_LABEL = "label";
	public static final String FIELD_NAME_TAGS = "tags";
	public static final String FIELD_NAME_METHOD = "method";
	public static final String FIELD_NAME_CHOICES = "choices";

	// Field names for Method and MethodMeta
	public static final String FIELD_NAME_DEFAULTS = "defaults";
	public static final String FIELD_NAME_TOOK = "took";
	public static final String FIELD_NAME_RETURNED = "returned";
	public static final String FIELD_NAME_TAKES = "takes";
	public static final String FIELD_NAME_RETURNS = "returns";
	public static final String FIELD_NAME_REQUIRED = "required";
	public static final String FIELD_NAME_PRESENT = "present";

	// Miscellaneous field names
	public static final String FIELD_NAME_NAME = "name";
	public static final String FIELD_NAME_MESSAGE = "message";
	public static final String FIELD_NAME_LABELS = "labels"; // malcolm table
	public static final String FIELD_NAME_VISIBLE = "visible"; // use by layout
	public static final String FIELD_NAME_MRI = "mri"; // used by layout

	// field names for model to configure malcolm (EpicsMalcolmModel)
	public static final String FIELD_NAME_FILE_DIR = "fileDir";
	public static final String FIELD_NAME_AXES_TO_MOVE = "axesToMove";
	public static final String FIELD_NAME_DETECTORS = "detectors";
	public static final String FIELD_NAME_FILE_TEMPLATE = "fileTemplate";

	private EpicsConnectionConstants() {
		// private constructor to prevent instantation
	}

}
