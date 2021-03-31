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

import static java.util.stream.Collectors.toSet;
import static org.eclipse.scanning.api.annotation.ui.FileType.EXISTING_FILE;
import static org.eclipse.scanning.test.api.FieldValueTestConstants.BACKGROUND_FIELD;
import static org.eclipse.scanning.test.api.FieldValueTestConstants.CONFIDENTIAL_FIELD;
import static org.eclipse.scanning.test.api.FieldValueTestConstants.OWNER_FIELD;
import static org.eclipse.scanning.test.api.FieldValueTestConstants.SAMPLE_BACKGROUND_LABEL;
import static org.eclipse.scanning.test.api.FieldValueTestConstants.SAMPLE_ID_FIELD;
import static org.eclipse.scanning.test.api.FieldValueTestConstants.SAMPLE_NAME_FIELD;
import static org.eclipse.scanning.test.api.FieldValueTestConstants.SAMPLE_THICKNESS_FIELD;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyString;
import static org.hamcrest.Matchers.nullValue;

import java.io.File;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Set;

import org.eclipse.scanning.api.annotation.ui.FieldDescriptor;
import org.eclipse.scanning.api.annotation.ui.FieldUtils;
import org.eclipse.scanning.api.annotation.ui.FieldValue;
import org.junit.Before;
import org.junit.Test;

public class FieldUtilsTest {
	private FieldValueTestObject fieldValueObject;
	private FieldValueTestObjectSub fieldValueObjectSub;

	@Before
	public void setUp() {
		fieldValueObject = new FieldValueTestObject();
		fieldValueObjectSub = new FieldValueTestObjectSub();
	}

	@Test
	public void testIsFileType() {
		assertThat(FieldUtils.isFileType(File.class), is(true));
		assertThat(FieldUtils.isFileType(Path.class), is(true));
		assertThat(FieldUtils.isFileType(String.class), is(false));
	}

	@Test
	public void testGetAnnotation() throws Exception {
		final FieldDescriptor backgroundAnnotation = FieldUtils.getAnnotation(fieldValueObject, FieldValueTestConstants.BACKGROUND_FIELD);
		assertThat(backgroundAnnotation.label(), is(equalTo(SAMPLE_BACKGROUND_LABEL)));
		assertThat(backgroundAnnotation.file(), is(equalTo(EXISTING_FILE)));

		final FieldDescriptor sampleNameAnnotation = FieldUtils.getAnnotation(fieldValueObject, FieldValueTestConstants.SAMPLE_NAME_FIELD);
		assertThat(sampleNameAnnotation.label(), isEmptyString());

		final FieldDescriptor backgroundAnnotationSub = FieldUtils.getAnnotation(fieldValueObjectSub, FieldValueTestConstants.BACKGROUND_FIELD);
		assertThat(backgroundAnnotationSub.label(), is(equalTo(SAMPLE_BACKGROUND_LABEL)));
		assertThat(backgroundAnnotationSub.file(), is(equalTo(EXISTING_FILE)));

		final FieldDescriptor sampleNameAnnotationSub = FieldUtils.getAnnotation(fieldValueObjectSub, FieldValueTestConstants.SAMPLE_NAME_FIELD);
		assertThat(sampleNameAnnotationSub.label(), isEmptyString());

		assertThat(FieldUtils.getAnnotation(fieldValueObjectSub, FieldValueTestConstants.SAMPLE_ID_FIELD), is(nullValue()));
	}

	@Test
	public void testGetField() throws Exception {
		assertThat(FieldUtils.getField(fieldValueObject, SAMPLE_NAME_FIELD).getName(), is(equalTo(SAMPLE_NAME_FIELD)));
		assertThat(FieldUtils.getField(fieldValueObject, BACKGROUND_FIELD).getName(), is(equalTo(BACKGROUND_FIELD)));

		assertThat(FieldUtils.getField(fieldValueObjectSub, SAMPLE_NAME_FIELD).getName(), is(equalTo(SAMPLE_NAME_FIELD)));
		assertThat(FieldUtils.getField(fieldValueObjectSub, BACKGROUND_FIELD).getName(), is(equalTo(BACKGROUND_FIELD)));
	}

	@Test(expected = NoSuchFieldException.class)
	public void testGetFieldNonExistent() throws Exception {
		FieldUtils.getField(fieldValueObject, FieldValueTestConstants.NO_FIELD);
	}

	@Test
	public void testGetModelFields() throws Exception {
		final Collection<FieldValue> fieldValues = FieldUtils.getModelFields(fieldValueObject);
		final Set<String> fieldValueNames = fieldValues.stream().map(FieldValue::getName).collect(toSet());
		// filePath is not deemed to be a model field, as it is not visible
		assertThat(fieldValueNames, hasSize(3));
		assertThat(fieldValueNames, containsInAnyOrder(SAMPLE_NAME_FIELD, BACKGROUND_FIELD, SAMPLE_THICKNESS_FIELD));

		final Collection<FieldValue> fieldValuesSub = FieldUtils.getModelFields(fieldValueObjectSub);
		final Set<String> fieldValueNamesSub = fieldValuesSub.stream().map(FieldValue::getName).collect(toSet());
		// initialised is not a model field, as it has no getter/setter
		assertThat(fieldValueNamesSub, hasSize(6));
		assertThat(fieldValueNamesSub,
				containsInAnyOrder(SAMPLE_NAME_FIELD, BACKGROUND_FIELD, SAMPLE_THICKNESS_FIELD, OWNER_FIELD,
						SAMPLE_ID_FIELD, CONFIDENTIAL_FIELD));
	}
}
