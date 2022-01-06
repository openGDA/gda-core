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

import java.util.Objects;

import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.nexus.INexusDevice;
import org.eclipse.dawnsci.nexus.NXdetector;
import org.eclipse.dawnsci.nexus.NXtransformations;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusNodeFactory;
import org.eclipse.dawnsci.nexus.appender.NexusObjectAppender;

/**
 * An appender {@link NexusObjectAppender} that appends an {@link NXtransformations} group to
 * the {@link NXdetector} group provided by the {@link INexusDevice} configured with the same name.
 *
 * <p>A {@link DataNode} is added to the {@link NXtransformations} for each {@link Transformation}
 * configured in this appender, where the name of the data node is {@link Transformation#getAxisName()},
 * the value is a scalar dataset with the value {@link Transformation#getSize()}, and the attributes
 * are determined by the remaining fields of the {@link Transformation}, e.g. the value of
 * the attribute {@link NXtransformations#NX_AXISNAME_ATTRIBUTE_VECTOR} will be same as the
 * value of the field {@link Transformation#getVector()}.
 *
 * <p>This class is intended for the use case required by the NXmx application definition,
 * which requires an {@link NXtransformations} group inside the {@link NXdetector} group.
 */
public class DetectorTransformationsAppender extends AbstractTransformationsAppender<NXdetector> {

	@Override
	protected void appendNexusObject(NXdetector detector) throws NexusException {
		Objects.requireNonNull(transformations, "transformations not specified");

		// Create a new NXtransformations group and add to the parent NXdetector group
		final NXtransformations transformationsGroup = NexusNodeFactory.createNXtransformations();
		detector.addGroupNode(GROUP_NAME_TRANSFORMATIONS, transformationsGroup);

		for (Transformation transformation : transformations) {
			Objects.requireNonNull(transformation.getAxisName(), "axisName not set for Transformation");
			final DataNode axisDataNode = transformationsGroup.setAxisnameScalar(
					transformation.getAxisName(), transformation.getSize());

			addAttributesForTransformation(axisDataNode, transformation);
		}
	}

}
