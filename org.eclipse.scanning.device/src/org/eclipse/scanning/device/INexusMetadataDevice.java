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

import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.nexus.INexusDevice;
import org.eclipse.dawnsci.nexus.NXobject;
import org.eclipse.scanning.api.IScannable;

/**
 * A generic metadata device that adds creates a nexus object depending on the {@link MetadataNode}s
 * that it is configured with.
 *
 * @param <N> the type of nexus object created by this device
 */
public interface INexusMetadataDevice<N extends NXobject> extends INexusDevice<N> {

	/**
	 * Adds the given {@link MetadataNode} to this node. This can be {@link MetadataField}
	 * or a {@link GroupMetadataNode}.
	 * @param childNode child node to add
	 */
	public void addNode(MetadataNode childNode);

	/**
	 * Adds the given {@link MetadataField} to this device.
	 * @param field
	 */
	public void addField(MetadataField field);

	/**
	 * Creates and adds a {@link ScannableField} to this device with the given name
	 * and for the {@link IScannable} with the given name.
	 * @param fieldName field name
	 * @param scannableName name of scannable
	 */
	public void addScannableField(String fieldName, String scannableName);

	/**
	 * Creates and adds a {@link ScalarField} to this device with the given name
	 * and value.
	 * @param fieldName field name
	 * @param fieldValue value
	 */
	public void addScalarField(String fieldName, Object fieldValue);

	/**
	 * Creates and adds a {@link ScalarField} to this device with the given name,
	 * value and units.
	 * @param fieldName field name
	 * @param fieldValue value
	 * @param units units
	 */
	public void addScalarField(String fieldName, Object fieldValue, String units);

	/**
	 * Creates and adds a {@link ScalarField} to this device, which will set the default value
	 * of the field with the given name to the given value.
	 * @param fieldName field name
	 * @param defaultValue default value
	 */
	public void setDefaultValue(String fieldName, Object defaultValue);

	/**
	 * Creates and adds a {@link LinkedField} to this device, which will add a link with the
	 * given name to the {@link DataNode} with the path within the nexus file.
	 * @param fieldName field name
	 * @param linkPath path to link to within entry
	 */
	public void addLinkedField(String fieldName, String linkPath);

	/**
	 * Creates and adds a {@link LinkedField} to this device, which will add a link with the
	 * given name to the dataset at the given path within the external nexus or hdf5 file at the given file path.
	 * @param fieldName field name
	 * @param externalFilePath path to external nexus file to link to
	 * @param linkPath path to link to within external nexus file
	 */
	public void addExternalLinkedField(String fieldName, String externalFilePath, String linkPath);

	/**
	 * Adds all given {@link MetadataNode}s to this device. This method is named so that it to make
	 * spring configuration easier. Note: fields added using other methods in this class will not
	 * be removed, although they may be replaced if there is a name clash with a field in this list.
	 * @param childNodes nodes to add
	 */
	public void setChildNodes(List<MetadataNode> childNodes);

	/**
	 * Gets the node with the given name
	 * @param nodeName name of node to get
	 * @return the node with the given name
	 */
	public MetadataNode getNode(String nodeName);

	/**
	 * Removes the node with the given name from this device.
	 * @param nodeName
	 */
	public void removeNode(String nodeName);

	/**
	 * Removes all nodes.
	 */
	public void clearNodes();

}
