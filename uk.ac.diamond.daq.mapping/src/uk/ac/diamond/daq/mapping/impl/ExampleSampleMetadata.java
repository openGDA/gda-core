/*-
 * Copyright © 2015 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.mapping.impl;

import org.eclipse.scanning.api.annotation.UiComesAfter;
import org.eclipse.scanning.api.annotation.UiLarge;

import uk.ac.diamond.daq.mapping.api.ISampleMetadata;

public class ExampleSampleMetadata implements ISampleMetadata {

	private String sampleName;
	private String description;

	@Override
	public String getSampleName() {
		return sampleName;
	}

	@Override
	public void setSampleName(String sampleName) {
		this.sampleName = sampleName;
	}

	@UiLarge
	@UiComesAfter("sampleName")
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
