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
package org.eclipse.scanning.example.detector;

import org.eclipse.scanning.api.device.models.AbstractDetectorModel;

public class DarkImageModel extends AbstractDetectorModel {

	private int columns;
	private int rows;
	private int frequency;

	public DarkImageModel() {
		setName("dkExmpl");
		setExposureTime(0.001);
		columns = 64;
		rows = 60;
		frequency = 10;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + columns;
		result = prime * result + frequency;
		result = prime * result + rows;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		DarkImageModel other = (DarkImageModel) obj;
		if (columns != other.columns)
			return false;
		if (frequency != other.frequency)
			return false;
		if (rows != other.rows)
			return false;
		return true;
	}

	public int getColumns() {
		return columns;
	}

	public void setColumns(int columns) {
		this.columns = columns;
	}

	public int getRows() {
		return rows;
	}

	public void setRows(int points) {
		this.rows = points;
	}

	public int getFrequency() {
		return frequency;
	}

	public void setFrequency(int frequency) {
		this.frequency = frequency;
	}
}
