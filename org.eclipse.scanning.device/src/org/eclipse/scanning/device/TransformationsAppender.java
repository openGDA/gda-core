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

package org.eclipse.scanning.device;

import static org.eclipse.dawnsci.nexus.NXtransformations.NX_AXISNAME_ATTRIBUTE_OFFSET;
import static org.eclipse.dawnsci.nexus.NXtransformations.NX_AXISNAME_ATTRIBUTE_VECTOR;

import java.util.List;
import java.util.Objects;

import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.analysis.tree.TreeFactory;
import org.eclipse.dawnsci.nexus.NXobject;
import org.eclipse.dawnsci.nexus.NXtransformations;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusNodeFactory;
import org.eclipse.dawnsci.nexus.appender.NexusObjectAppender;

public class TransformationsAppender<N extends NXobject> extends NexusObjectAppender<N> {

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

	public static class Transformation {

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
			this.axisName = axisName;
			this.type = type;
			this.dependsOn = dependsOn;
			this.size = size;
			this.vector = vector;
			this.offset = offset;
			this.offsetUnits = offsetUnits;
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
			this.offset = offset;
		}

		public String getOffsetUnits() {
			return offsetUnits;
		}

		public void setOffsetUnits(String offsetUnits) {
			this.offsetUnits = offsetUnits;
		}

	}

	public static final String GROUP_NAME_TRANSFORMATIONS = "transformations";

	private List<Transformation> transformations = null;

	public void setTransformations(List<Transformation> transformations) {
		this.transformations = transformations;
	}

	public void setTransformation(Transformation transformation) {
		this.transformations = List.of(transformation);
	}

	@Override
	protected void appendNexusObject(N nexusObject) throws NexusException {
		Objects.requireNonNull(transformations, "transformations not specified");

		final NXtransformations transformationsGroup = NexusNodeFactory.createNXtransformations();
		nexusObject.addGroupNode(GROUP_NAME_TRANSFORMATIONS, transformationsGroup);

		for (Transformation transformation : transformations) {
			addTransformation(transformationsGroup, transformation);
		}
	}

	private void addTransformation(final NXtransformations transformationsGroup, Transformation transformation) {
		final String axisName = transformation.getAxisName();
		Objects.requireNonNull(axisName, "axisName not set for Transformation");
		final DataNode axisDataNode = transformationsGroup.setAxisnameScalar(axisName, transformation.getSize());

		Objects.requireNonNull(transformation.getDependsOn(), "dependsOn not set for axisname: " + axisName);
		transformationsGroup.setAxisnameAttributeDepends_on(axisName, transformation.getDependsOn());

		if (transformation.getType() != null) {
			transformationsGroup.setAxisnameAttributeTransformation_type(axisName, transformation.getType().toString());
		}

		Objects.requireNonNull(transformation.getVector(), "vector not set for axisname: " + axisName);
		if (transformation.getVector().length != 3) {
			throw new IllegalArgumentException("vector must be array of size 3 for axisname: " + axisName);
		}
		axisDataNode.addAttribute(TreeFactory.createAttribute(NX_AXISNAME_ATTRIBUTE_VECTOR, transformation.getVector()));

		if (transformation.getOffset() != null) {
			if (transformation.getOffset().length != 3) {
				throw new IllegalArgumentException("offset must be array of size 3 for axisname: " + axisName);
			}
			axisDataNode.addAttribute(TreeFactory.createAttribute(NX_AXISNAME_ATTRIBUTE_OFFSET, transformation.getOffset()));
		}

		if (transformation.getOffsetUnits() != null) {
			transformationsGroup.setAxisnameAttributeOffset_units(axisName, transformation.getOffsetUnits());
		}
	}

}
