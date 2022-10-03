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

import static org.eclipse.dawnsci.analysis.tree.TreeFactory.createAttribute;
import static org.eclipse.dawnsci.nexus.NXtransformations.NX_AXISNAME_ATTRIBUTE_DEPENDS_ON;
import static org.eclipse.dawnsci.nexus.NXtransformations.NX_AXISNAME_ATTRIBUTE_OFFSET;
import static org.eclipse.dawnsci.nexus.NXtransformations.NX_AXISNAME_ATTRIBUTE_OFFSET_UNITS;
import static org.eclipse.dawnsci.nexus.NXtransformations.NX_AXISNAME_ATTRIBUTE_TRANSFORMATION_TYPE;
import static org.eclipse.dawnsci.nexus.NXtransformations.NX_AXISNAME_ATTRIBUTE_VECTOR;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.nexus.NXobject;
import org.eclipse.dawnsci.nexus.appender.NexusObjectAppender;

/**
 * Abstract superclass of appenders that add attributes to one or more
 * {@link DataNode}s according to the configured {@link Transformation}s.
 *
 * @param <N> type of nexus object to append to
 */
public abstract class AbstractTransformationsAppender<N extends NXobject> extends NexusObjectAppender<N> {

	public static final String GROUP_NAME_TRANSFORMATIONS = "transformations";

	// if set to true (the default, the vector is normalized, i.e. so that sqrt(x^2 + y^2 + z^2) == 1
	private boolean normalize = true;

	protected List<Transformation> transformations = null;

	public void setNormalize(boolean normalize) {
		this.normalize = normalize;
	}

	public void setTransformations(List<Transformation> transformations) {
		this.transformations = transformations;
	}

	public void setTransformation(Transformation transformation) {
		this.transformations = List.of(transformation);
	}

	public List<Transformation> getTransformations() {
		return transformations;
	}

	protected void addAttributesForTransformation(final DataNode axisDataNode, Transformation transformation) {
		final String axisName = transformation.getAxisName();
		Objects.requireNonNull(transformation.getDependsOn(), "dependsOn not set for axisname: " + axisName);
		axisDataNode.addAttribute(createAttribute(NX_AXISNAME_ATTRIBUTE_DEPENDS_ON, transformation.getDependsOn()));

		if (transformation.getType() != null) {
			axisDataNode.addAttribute(createAttribute(NX_AXISNAME_ATTRIBUTE_TRANSFORMATION_TYPE, transformation.getType().toString()));
		}

		Objects.requireNonNull(getVector(transformation), "vector not set for axisname: " + axisName);
		if (getVector(transformation).length != 3) {
			throw new IllegalArgumentException("vector must be array of size 3 for axisname: " + axisName);
		}

		axisDataNode.addAttribute(createAttribute(NX_AXISNAME_ATTRIBUTE_VECTOR, getVector(transformation)));

		if (transformation.getOffset() != null) {
			if (transformation.getOffset().length != 3) {
				throw new IllegalArgumentException("offset must be array of size 3 for axisname: " + axisName);
			}

			axisDataNode.addAttribute(createAttribute(NX_AXISNAME_ATTRIBUTE_OFFSET, transformation.getOffset()));
			if (transformation.getOffsetUnits() != null) {
				axisDataNode.addAttribute(createAttribute(NX_AXISNAME_ATTRIBUTE_OFFSET_UNITS, transformation.getOffsetUnits()));
			}
		}
	}

	private double[] getVector(Transformation transformation) {
		final double[] vector = transformation.getVector();

		if (normalize) {
			final double normalizationFactor = Math.sqrt(Arrays.stream(vector).map(x -> x * x).sum());
			return Arrays.stream(vector).map(x -> x / normalizationFactor).toArray();
		}

		return transformation.getVector();
	}

}
