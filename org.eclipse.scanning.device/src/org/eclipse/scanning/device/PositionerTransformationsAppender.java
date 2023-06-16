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
import org.eclipse.dawnsci.nexus.NXpositioner;
import org.eclipse.dawnsci.nexus.NXsubentry;
import org.eclipse.dawnsci.nexus.NXtransformations;
import org.eclipse.dawnsci.nexus.NexusException;

/**
 * An appender that adds the attributes required by an axis {@link DataNode} within an
 * {@link NXtransformations} group to the {@link NXpositioner#NX_VALUE value} data node
 * of an {@link NXpositioner}.<br><br>
 * An {@link NXtransformations} group should be created separately
 * which contains a link to this {@link DataNode}. For example, This can be done in a Nexus
 * template which create an nexus {@link NXsubentry} for an application definition.<BR><BR>
 * For example:<br><pre>{@code
 * <bean class="org.eclipse.scanning.device.PositionerTransformationsAppender" init-method="register">
 *   <property name="name" value="kphi" />
 *    <property name="transformation">
 *      <bean class="org.eclipse.scanning.device.Transformation">
 *        <property name="type">
 *          <value type="org.eclipse.scanning.device.Transformation.TransformationType">ROTATION</value>
 *        </property>
 *        <property name="dependsOn" value="entry1/instrument/kap" />
 *        <property name="size" value="0" />
 *        <property name="vector">
 *          <list>
 *            <value>0</value>
 *            <value>1</value>
 *            <value>0</value>
 *          </list>
 *        </property>
 *      </bean>
 *    </property>
 *  </property>
 *</bean>
 *}</pre>
 */
public class PositionerTransformationsAppender extends AbstractTransformationsAppender<NXpositioner> {

	@Override
	protected void appendNexusObject(NXpositioner positioner) throws NexusException {
		Objects.requireNonNull(transformations, "transformations not specified");
		if (transformations.size() != 1 && !transformations.get(0).getAxisName().equals(NXpositioner.NX_VALUE)) {
			throw new IllegalArgumentException("A PositionerTransformationAppender must have exactly one transformation, with axisName 'value'");
		}

		final DataNode valueDataNode = positioner.getDataNode(NXpositioner.NX_VALUE);
		addAttributesForTransformation(valueDataNode, transformations.get(0));
	}
}
