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

package org.eclipse.scanning.test.api;

import org.eclipse.scanning.api.annotation.ui.FieldValue;

/**
 * Constants for testing {@link FieldValue} and associated classes
 */
class FieldValueTestConstants {
	// Field names
	public static final String FILE_PATH_FIELD = "filePath";
	public static final String SAMPLE_THICKNESS_FIELD = "sampleThickness";
	public static final String SAMPLE_NAME_FIELD = "sampleName";
	public static final String NO_FIELD = "nofield";
	public static final String BACKGROUND_FIELD = "background";
	public static final String SAMPLE_ID_FIELD = "sampleId";
	public static final String CONFIDENTIAL_FIELD = "confidential";
	public static final String INITIALISED_FIELD = "initialised";
	public static final String OWNER_FIELD = "owner";

	// Values for FieldDescriptor label parameters
	public static final String SAMPLE_BACKGROUND_LABEL = "Sample Background";
	public static final String SAMPLE_THICKNESS_LABEL = "Sample Thickness";
	public static final String TEST_SAMPLE_LABEL = "Test Sample";
	public static final String OWNER_LABEL = "Sample Owner";

	private FieldValueTestConstants() {
		// prevent instantiation
	}
}
