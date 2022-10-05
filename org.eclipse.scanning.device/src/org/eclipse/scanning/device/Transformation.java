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

package org.eclipse.scanning.device;

import org.eclipse.dawnsci.nexus.NXtransformations;

/**
 * Defines an transformation, i.e. an axis based transformation or rotation.
 * An instance of this class defines the properties required to populate
 * the attributes of an axis data node for an {@link NXtransformations} group.
 */
public class Transformation {

	public enum TransformationType {
		TRANSLATION, ROTATION;

		@Override
		public String toString() {
			return name().toLowerCase();
		}

		public static TransformationType fromString(String string) {
			return TransformationType.valueOf(string.toUpperCase());
		}
	}

	private String axisName;
	private double size = 1.0;
	private String dependsOn;
	private double[] vector;
	private TransformationType type;
	private double[] offset;
	private String offsetUnits;

	public Transformation() {
		// no-arg constructor for spring instantiation
	}

	public Transformation(String axisName, TransformationType type, String dependsOn,
			double size, double[] vector, double[] offset, String offsetUnits) {
		setAxisName(axisName);
		setType(type);
		setDependsOn(dependsOn);
		setSize(size);
		setVector(vector);
		setOffset(offset);
		setOffsetUnits(offsetUnits);
	}

	public String getAxisName() {
		return axisName;
	}

	public void setAxisName(String axisName) {
		this.axisName = axisName;
	}

	public double getSize() {
		return size;
	}

	public void setSize(double size) {
		this.size = size;
	}

	public String getDependsOn() {
		return dependsOn;
	}

	public void setDependsOn(String dependsOn) {
		this.dependsOn = dependsOn;
	}

	public double[] getVector() {
		return vector;
	}

	public void setVector(double... vector) {
		if (vector.length != 3) {
			throw new IllegalArgumentException("Vector must have length 3 for axisName: " + axisName);
		}
		this.vector = vector;
	}

	public TransformationType getType() {
		return type;
	}

	public void setType(TransformationType type) {
		this.type = type;
	}

	public void setType(String typeStr) {
		this.type = TransformationType.fromString(typeStr);
	}

	public double[] getOffset() {
		return offset;
	}

	public void setOffset(double... offset) {
		if (offset.length != 3) {
			throw new IllegalArgumentException("Offset must have length 3 for axisName: " + axisName);
		}
		this.offset = offset;
	}

	public String getOffsetUnits() {
		return offsetUnits;
	}

	public void setOffsetUnits(String offsetUnits) {
		this.offsetUnits = offsetUnits;
	}

}