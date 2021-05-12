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

import java.util.List;

import org.eclipse.dawnsci.nexus.INexusDevice;
import org.eclipse.dawnsci.nexus.NXobject;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.appender.NexusObjectAppender;
import org.eclipse.dawnsci.nexus.device.INexusDeviceService;

/**
 * A {@link NexusObjectAppender} that appends nodes according to the {@link MetadataNode}s
 * that it is configured with. The appender must be configured with the same name as the {@link INexusDevice}
 * whose nexus object is to be appended, and the appender must also be registered with the
 * {@link INexusDeviceService} - this can be done by calling the {@link #register()} method.
 * <p>
 * This class is intended to be configured in spring. For example:
 * <pre>
 * {@code
 * <bean id="detectorMetadataAppender" class="org.eclipse.scanning.ui.device.NexusMetadataAppender">
 *    <property name="name" value="det1"/>
 *    <property name="childNodes">
 *       <list>
 *          <bean class="org.eclipse.scanning.device.ScalarField">
 *             <property name="description" value="A description of the detector"/>
 *          </bean>
 *          <bean class="org.eclipse.scanning.device.ScannableField">
 *             <property name="distance" scannableName="det1Distance"/>
 *          </bean>
 *       </list>
 *    </property>
 * </bean>}
 * </pre>
 *
 * @param <N>
 */
public class NexusMetadataAppender<N extends NXobject> extends NexusObjectAppender<N> {

	protected final GroupMetadataNode<N> metadataNode = new GroupMetadataNode<>();

	// addFields methods are useful for tests, but not spring configuration
	public void addField(MetadataNode node) {
		metadataNode.addChildNode(node);
	}

	public void addScannableField(String fieldName, String scannableName) {
		metadataNode.addChildNode(new ScannableField(fieldName, scannableName));
	}

	public void addScalarField(String fieldName, Object fieldValue) {
		metadataNode.addChildNode(new ScalarField(fieldName, fieldValue));
	}

	public void addLinkedField(String fieldName, String linkPath) {
		metadataNode.addChildNode(new LinkedField(fieldName, linkPath));
	}

	public void setChildNodes(List<MetadataNode> customNodes) {
		// this is the method used by spring
		metadataNode.addChildNodes(customNodes);
	}

	@Override
	protected void appendNexusObject(N nexusObject) throws NexusException {
		metadataNode.appendNodes(nexusObject);
	}

}
