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

import static org.eclipse.scanning.api.annotation.ui.FileType.EXISTING_FILE;
import static org.eclipse.scanning.test.api.FieldValueTestConstants.NO_FIELD;
import static org.eclipse.scanning.test.api.FieldValueTestConstants.SAMPLE_BACKGROUND_LABEL;
import static org.eclipse.scanning.test.api.FieldValueTestConstants.TEST_SAMPLE_LABEL;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyString;
import static org.hamcrest.Matchers.nullValue;

import org.eclipse.scanning.api.annotation.ui.FieldDescriptor;
import org.eclipse.scanning.api.annotation.ui.FieldValue;
import org.junit.Before;
import org.junit.Test;

/**
 * Test the behaviour of the {@link FieldValue} class, ensuring that fields in a class are also accessible via a
 * subclass.
 */
public class FieldValueTest {
	// Allow for inaccuracy in floating point values
	private static final double FP_TOLERANCE = 1e-12;

	private FieldValueTestObject fieldValueObject;
	private FieldValueTestObjectSub fieldValueObjectSub;

	// Field values on superclass
	private FieldValue fieldValueBackground;
	private FieldValue fieldValueNoField;
	private FieldValue fieldValueSampleName;
	private FieldValue fieldValueSampleThickness;
	private FieldValue fieldValueFilePath;

	// Field values on subclass
	private FieldValue fieldValueBackgroundSub;
	private FieldValue fieldValueNoFieldSub;
	private FieldValue fieldValueSampleNameSub;
	private FieldValue fieldValueSampleThicknessSub;
	private FieldValue fieldValueFilePathSub;
	private FieldValue fieldValueSampleIdSub;

	@Before
	public void setUp() {
		fieldValueObject = new FieldValueTestObject();
		fieldValueObjectSub = new FieldValueTestObjectSub();

		fieldValueBackground = new FieldValue(fieldValueObject, FieldValueTestConstants.BACKGROUND_FIELD);
		fieldValueNoField = new FieldValue(fieldValueObject, FieldValueTestConstants.NO_FIELD);
		fieldValueSampleName = new FieldValue(fieldValueObject, FieldValueTestConstants.SAMPLE_NAME_FIELD);
		fieldValueSampleThickness = new FieldValue(fieldValueObject, FieldValueTestConstants.SAMPLE_THICKNESS_FIELD);
		fieldValueFilePath = new FieldValue(fieldValueObject, FieldValueTestConstants.FILE_PATH_FIELD);

		fieldValueBackgroundSub = new FieldValue(fieldValueObjectSub, FieldValueTestConstants.BACKGROUND_FIELD);
		fieldValueNoFieldSub = new FieldValue(fieldValueObjectSub, FieldValueTestConstants.NO_FIELD);
		fieldValueSampleNameSub = new FieldValue(fieldValueObjectSub, FieldValueTestConstants.SAMPLE_NAME_FIELD);
		fieldValueSampleThicknessSub = new FieldValue(fieldValueObjectSub, FieldValueTestConstants.SAMPLE_THICKNESS_FIELD);
		fieldValueFilePathSub = new FieldValue(fieldValueObjectSub, FieldValueTestConstants.FILE_PATH_FIELD);
		fieldValueSampleIdSub = new FieldValue(fieldValueObjectSub, FieldValueTestConstants.SAMPLE_ID_FIELD);
	}

	@Test
	public void testGetDisplayName() {
		assertThat(fieldValueBackground.getDisplayName(), equalTo(SAMPLE_BACKGROUND_LABEL));
		assertThat(fieldValueBackgroundSub.getDisplayName(), equalTo(SAMPLE_BACKGROUND_LABEL));
	}

	@Test
	public void testGetDisplayNameInvalidField() {
		// getDisplayName() returns the message in NoSuchFieldException, which happens to be the name of the
		// (non-existent) field we are trying to access
		assertThat(fieldValueNoField.getDisplayName(), is(equalTo(NO_FIELD)));
		assertThat(fieldValueNoFieldSub.getDisplayName(), is(equalTo(NO_FIELD)));
	}

	@Test
	public void testGetDisplayNameBlankLabel() {
		// If the label annotation in the field is blank, getDisplayName() will construct a value from the field name,
		// including an extra space at the end
		assertThat(fieldValueSampleName.getDisplayName(), is(equalTo("Sample Name ")));
		assertThat(fieldValueSampleNameSub.getDisplayName(), is(equalTo("Sample Name ")));
	}

	@Test
	public void testGetType() throws Exception {
		assertThat(fieldValueBackground.getType(), equalTo(String.class));
		assertThat(fieldValueSampleThickness.getType(), equalTo(double.class));

		assertThat(fieldValueBackgroundSub.getType(), equalTo(String.class));
		assertThat(fieldValueSampleThicknessSub.getType(), equalTo(double.class));
	}

	@Test(expected = NoSuchFieldException.class)
	public void testGetTypeNoField() throws Exception {
		fieldValueNoField.getType();
	}

	@Test(expected = NoSuchFieldException.class)
	public void testGetTypeNoFieldSub() throws Exception {
		fieldValueNoFieldSub.getType();
	}

	@Test
	public void testGetAnnotation() {
		final FieldDescriptor backgroundAnnotation = fieldValueBackground.getAnnotation();
		assertThat(backgroundAnnotation.label(), is(equalTo(SAMPLE_BACKGROUND_LABEL)));
		assertThat(backgroundAnnotation.file(), is(equalTo(EXISTING_FILE)));

		final FieldDescriptor sampleNameAnnotation = fieldValueSampleName.getAnnotation();
		assertThat(sampleNameAnnotation.label(), isEmptyString());

		final FieldDescriptor backgroundAnnotationSub = fieldValueBackground.getAnnotation();
		assertThat(backgroundAnnotationSub.label(), is(equalTo(SAMPLE_BACKGROUND_LABEL)));
		assertThat(backgroundAnnotationSub.file(), is(equalTo(EXISTING_FILE)));

		final FieldDescriptor sampleNameAnnotationSub = fieldValueSampleName.getAnnotation();
		assertThat(sampleNameAnnotationSub.label(), isEmptyString());

		assertThat(fieldValueSampleIdSub.getAnnotation(), is(nullValue()));
	}

	@Test
	public void testGetAnnotationNoField() {
		// Getting annotation for a non-existent field returns null: it doesn't throw a NoSuchFieldException
		assertThat(fieldValueNoField.getAnnotation(), is(nullValue()));
		assertThat(fieldValueNoFieldSub.getAnnotation(), is(nullValue()));
	}

	@Test
	public void testIsFileProperty() {
		assertThat(fieldValueBackground.isFileProperty(), is(true));
		assertThat(fieldValueFilePath.isFileProperty(), is(true));
		assertThat(fieldValueSampleThickness.isFileProperty(), is(false));
		assertThat(fieldValueNoField.isFileProperty(), is(false));

		assertThat(fieldValueBackgroundSub.isFileProperty(), is(true));
		assertThat(fieldValueFilePathSub.isFileProperty(), is(true));
		assertThat(fieldValueSampleThicknessSub.isFileProperty(), is(false));
		assertThat(fieldValueNoFieldSub.isFileProperty(), is(false));
		assertThat(fieldValueSampleIdSub.isFileProperty(), is(false));
	}

	@Test
	public void testSet() throws Exception {
		final double sampleThickness = 3.65;

		assertThat(fieldValueObject.getSampleName(), is(nullValue()));
		fieldValueSampleName.set(TEST_SAMPLE_LABEL);
		assertThat(fieldValueObject.getSampleName(), is(equalTo(TEST_SAMPLE_LABEL)));

		assertThat(fieldValueObject.getSampleThickness(), is(closeTo(0.0, FP_TOLERANCE)));
		fieldValueSampleThickness.set(sampleThickness);
		assertThat(fieldValueObject.getSampleThickness(), is(closeTo(sampleThickness, FP_TOLERANCE)));

		assertThat(fieldValueObjectSub.getSampleName(), is(nullValue()));
		fieldValueSampleNameSub.set(TEST_SAMPLE_LABEL);
		assertThat(fieldValueObjectSub.getSampleName(), is(equalTo(TEST_SAMPLE_LABEL)));

		assertThat(fieldValueObjectSub.getSampleThickness(), is(closeTo(0.0, FP_TOLERANCE)));
		fieldValueSampleThicknessSub.set(sampleThickness);
		assertThat(fieldValueObjectSub.getSampleThickness(), is(closeTo(sampleThickness, FP_TOLERANCE)));

		final Integer sampleId = 8273;
		assertThat(fieldValueObjectSub.getSampleId(), is(nullValue()));
		fieldValueSampleIdSub.set(sampleId);
		assertThat(fieldValueObjectSub.getSampleId(), is(equalTo(sampleId)));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSetWrongValueType() throws Exception {
		fieldValueSampleThickness.set("Thickness");
	}

	@Test
	public void testSetWrongNoField() throws Exception {
		// Does nothing, throws no exception
		fieldValueNoField.set(1.0);
	}

	@Test
	public void testGet() {
		final double sampleThickness = 9.32;

		fieldValueObject.setSampleName(TEST_SAMPLE_LABEL);
		fieldValueObject.setSampleThickness(sampleThickness);

		assertThat(fieldValueSampleName.get(), is(equalTo(TEST_SAMPLE_LABEL)));
		assertThat((double) fieldValueSampleThickness.get(), is(closeTo(sampleThickness, FP_TOLERANCE)));
		assertThat(fieldValueNoField.get(), is(nullValue()));

		fieldValueObjectSub.setSampleName(TEST_SAMPLE_LABEL);
		fieldValueObjectSub.setSampleThickness(sampleThickness);

		assertThat(fieldValueSampleNameSub.get(), is(equalTo(TEST_SAMPLE_LABEL)));
		assertThat((double) fieldValueSampleThicknessSub.get(), is(closeTo(sampleThickness, FP_TOLERANCE)));
		assertThat(fieldValueNoFieldSub.get(), is(nullValue()));
		assertThat(fieldValueSampleIdSub.get(), is(nullValue()));
	}

	@Test
	public void testGetNoCreate() throws Exception {
		// get(false) behaves the same as get()
		final double sampleThickness = 9.32;

		fieldValueObject.setSampleName(TEST_SAMPLE_LABEL);
		fieldValueObject.setSampleThickness(sampleThickness);

		assertThat(fieldValueSampleName.get(false), is(equalTo(TEST_SAMPLE_LABEL)));
		assertThat((double) fieldValueSampleThickness.get(false), is(closeTo(sampleThickness, FP_TOLERANCE)));
		assertThat(fieldValueBackground.get(false), is(nullValue()));
		assertThat(fieldValueNoField.get(false), is(nullValue()));

		fieldValueObjectSub.setSampleName(TEST_SAMPLE_LABEL);
		fieldValueObjectSub.setSampleThickness(sampleThickness);

		assertThat(fieldValueSampleNameSub.get(false), is(equalTo(TEST_SAMPLE_LABEL)));
		assertThat((double) fieldValueSampleThicknessSub.get(false), is(closeTo(sampleThickness, FP_TOLERANCE)));
		assertThat(fieldValueBackgroundSub.get(false), is(nullValue()));
		assertThat(fieldValueNoFieldSub.get(false), is(nullValue()));
		assertThat(fieldValueSampleIdSub.get(false), is(nullValue()));
	}

	@Test
	public void testGetCreate() throws Exception {
		// get(true) will return an empty string instead of null
		final double sampleThickness = 9.32;

		fieldValueObject.setSampleName(TEST_SAMPLE_LABEL);
		fieldValueObject.setSampleThickness(sampleThickness);

		assertThat(fieldValueSampleName.get(true), is(equalTo(TEST_SAMPLE_LABEL)));
		assertThat((double) fieldValueSampleThickness.get(true), is(closeTo(sampleThickness, FP_TOLERANCE)));
		assertThat((String) fieldValueBackground.get(true), isEmptyString());
		assertThat(fieldValueNoField.get(true), is(nullValue()));

		fieldValueObjectSub.setSampleName(TEST_SAMPLE_LABEL);
		fieldValueObjectSub.setSampleThickness(sampleThickness);

		assertThat(fieldValueSampleNameSub.get(true), is(equalTo(TEST_SAMPLE_LABEL)));
		assertThat((double) fieldValueSampleThicknessSub.get(true), is(closeTo(sampleThickness, FP_TOLERANCE)));
		assertThat((String) fieldValueBackgroundSub.get(true), isEmptyString());
		assertThat(fieldValueNoFieldSub.get(true), is(nullValue()));
	}

	@Test(expected = InstantiationException.class)
	public void testGetCreateFailsForInteger() throws Exception {
		// Fails because Integer has no zero-argument constructor
		fieldValueSampleIdSub.get(true);
	}

	@Test
	public void testGetStatic() throws Exception {
		// Test static form of get()
		final double sampleThickness = 9.32;

		fieldValueObject.setSampleName(TEST_SAMPLE_LABEL);
		fieldValueObject.setSampleThickness(sampleThickness);

		assertThat(FieldValue.get(fieldValueObject, FieldValueTestConstants.SAMPLE_NAME_FIELD), is(equalTo(TEST_SAMPLE_LABEL)));
		assertThat((double) FieldValue.get(fieldValueObject, FieldValueTestConstants.SAMPLE_THICKNESS_FIELD), is(closeTo(sampleThickness, FP_TOLERANCE)));

		final int sampleId = 8792;

		fieldValueObjectSub.setSampleName(TEST_SAMPLE_LABEL);
		fieldValueObjectSub.setSampleThickness(sampleThickness);
		fieldValueObjectSub.setSampleId(sampleId);

		assertThat(FieldValue.get(fieldValueObjectSub, FieldValueTestConstants.SAMPLE_NAME_FIELD), is(equalTo(TEST_SAMPLE_LABEL)));
		assertThat((double) FieldValue.get(fieldValueObjectSub, FieldValueTestConstants.SAMPLE_THICKNESS_FIELD), is(closeTo(sampleThickness, FP_TOLERANCE)));
		assertThat(FieldValue.get(fieldValueObjectSub, FieldValueTestConstants.SAMPLE_ID_FIELD), is(equalTo(sampleId)));
	}

	@Test
	public void testIsModelField() throws Exception {
		assertThat(FieldValue.isModelField(fieldValueObject, FieldValueTestConstants.SAMPLE_NAME_FIELD), is(true));
		assertThat(FieldValue.isModelField(fieldValueObjectSub, FieldValueTestConstants.SAMPLE_NAME_FIELD), is(true));

		// Not model field because annotated as not visible
		assertThat(FieldValue.isModelField(fieldValueObject, FieldValueTestConstants.FILE_PATH_FIELD), is(false));
		assertThat(FieldValue.isModelField(fieldValueObjectSub, FieldValueTestConstants.FILE_PATH_FIELD), is(false));

		// sampleId is a model field because is has a getter, even though it is not annotated
		assertThat(FieldValue.isModelField(fieldValueObjectSub, FieldValueTestConstants.SAMPLE_ID_FIELD), is(true));

		// likewise, confidential has an isser
		assertThat(FieldValue.isModelField(fieldValueObjectSub, FieldValueTestConstants.CONFIDENTIAL_FIELD), is(true));

		// initialised has neither annotation nor getter/isser
		assertThat(FieldValue.isModelField(fieldValueObjectSub, FieldValueTestConstants.INITIALISED_FIELD), is(false));
	}

	@Test(expected = NullPointerException.class)
	public void testIsModelFieldNonExistent() throws Exception {
		// isModelField() for field that doesn't exist throws a NPE: this may not be the intended behaviour
		FieldValue.isModelField(fieldValueObject, FieldValueTestConstants.NO_FIELD);
	}
}
