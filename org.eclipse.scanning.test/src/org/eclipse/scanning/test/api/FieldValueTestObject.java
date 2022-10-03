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
import static org.eclipse.scanning.test.api.FieldValueTestConstants.OWNER_LABEL;
import static org.eclipse.scanning.test.api.FieldValueTestConstants.SAMPLE_BACKGROUND_LABEL;
import static org.eclipse.scanning.test.api.FieldValueTestConstants.SAMPLE_THICKNESS_LABEL;

import java.nio.file.Path;

import org.eclipse.scanning.api.annotation.ui.FieldDescriptor;
import org.eclipse.scanning.api.annotation.ui.FieldUtils;
import org.eclipse.scanning.api.annotation.ui.FieldValue;

/**
 * Classes for testing {@link FieldValue} & {@link FieldUtils}, containing various annotated fields
 */
class FieldValueTestObject {

	@FieldDescriptor(label = "")
	private String sampleName;

	@FieldDescriptor(label = SAMPLE_BACKGROUND_LABEL, file = EXISTING_FILE)
	private String background;

	@FieldDescriptor(label = SAMPLE_THICKNESS_LABEL, unit = "mm", hint = "Thickness of sample", minimum = 0.04, numberFormat = "#0.0")
	private double sampleThickness;

	@FieldDescriptor(label = "File Path", visible = false)
	private Path filePath;

	public String getBackground() {
		return background;
	}

	public void setBackground(String background) {
		this.background = background;
	}

	public String getSampleName() {
		return sampleName;
	}

	public void setSampleName(String sampleName) {
		this.sampleName = sampleName;
	}

	public double getSampleThickness() {
		return sampleThickness;
	}

	public void setSampleThickness(double sampleThickness) {
		this.sampleThickness = sampleThickness;
	}

	public Path getFilePath() {
		return filePath;
	}

	public void setFilePath(Path filePath) {
		this.filePath = filePath;
	}
}

/**
 * Subclass of the {@link FieldValueTestObject}, to check that fields in a superclass can be accessed via a subclass
 */
class FieldValueTestObjectSub extends FieldValueTestObject {

	private Integer sampleId;

	private boolean confidential;

	@FieldDescriptor(label = OWNER_LABEL)
	private String owner;

	@SuppressWarnings("unused")
	private boolean initialised;

	public Integer getSampleId() {
		return sampleId;
	}

	public void setSampleId(int sampleId) {
		this.sampleId = sampleId;
		initialised = true;
	}

	public boolean isConfidential() {
		return confidential;
	}

	public void setConfidential(boolean confidential) {
		this.confidential = confidential;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}
}
