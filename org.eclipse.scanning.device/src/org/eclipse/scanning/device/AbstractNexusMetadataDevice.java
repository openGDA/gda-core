/*-
 * Copyright © 2020 Diamond Light Source Ltd.
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
import org.eclipse.dawnsci.nexus.NexusBaseClass;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusScanInfo;
import org.eclipse.dawnsci.nexus.builder.NexusObjectProvider;
import org.eclipse.dawnsci.nexus.builder.NexusObjectWrapper;

public abstract class AbstractNexusMetadataDevice<N extends NXobject> implements INexusMetadataDevice<N>, INexusDevice<N> {

	private NexusBaseClass nexusCategory = null;

	private String collectionName;

	private final GroupMetadataNode<N> metadataNode;

	private String deviceName;

	protected AbstractNexusMetadataDevice() {
		// no-arg constructor for spring initialization
		metadataNode = new GroupMetadataNode<>();
	}

	protected AbstractNexusMetadataDevice(NexusBaseClass nexusBaseClass) {
		this();
		setNexusBaseClass(nexusBaseClass);
	}

	@Override
	public String getName() {
		return deviceName != null ? deviceName : metadataNode.getName();
	}

	public void setName(String name) {
		this.deviceName = name;
		if (metadataNode.getName() == null) {
			metadataNode.setName(name);
		}
	}

	@Override
	public String getNodeName() {
		return metadataNode.getName();
	}

	@Override
	public void setNodeName(String nodeName) {
		metadataNode.setName(nodeName);
	}

	@Override
	public void addField(MetadataField field) {
		addNode(field);
	}

	@Override
	public void addNode(MetadataNode node) {
		metadataNode.addChildNode(node);
	}

	@Override
	public MetadataNode getNode(String nodeName) {
		return metadataNode.getChildNode(nodeName);
	}

	@Override
	public void addScannableField(String fieldName, String scannableName) {
		metadataNode.addChildNode(new ScannableField(fieldName, scannableName));
	}

	@Override
	public void addScalarField(String fieldName, Object fieldValue) {
		metadataNode.addChildNode(new ScalarField(fieldName, fieldValue));
	}

	@Override
	public void addScalarField(String fieldName, Object fieldValue, String units) {
		metadataNode.addChildNode(new ScalarField(fieldName, fieldValue, units));
	}

	@Override
	public void setDefaultValue(String fieldName, Object defaultValue) {
		metadataNode.addChildNode(new ScalarField(fieldName, defaultValue, true));
	}

	@Override
	public void setScalarField(String fieldName, Object fieldValue) {
		metadataNode.setChildNode(new ScalarField(fieldName, fieldValue));
	}

	@Override
	public void setScalarField(String fieldName, Object fieldValue, String units) {
		metadataNode.setChildNode(new ScalarField(fieldName, fieldValue, units));
	}

	@Override
	public void addLinkedField(String fieldName, String linkPath) {
		metadataNode.addChildNode(new LinkedField(fieldName, linkPath));
	}

	@Override
	public void setLinkedField(String fieldName, String linkPath) {
		metadataNode.setChildNode(new LinkedField(fieldName, linkPath));
	}

	@Override
	public void addExternalLinkedField(String fieldName, String externalFilePath, String linkPath) {
		metadataNode.addChildNode(new LinkedField(fieldName, externalFilePath, linkPath));
	}

	@Override
	public void setExternalLinkedField(String fieldName, String externalFilePath, String linkPath) {
		metadataNode.setChildNode(new LinkedField(fieldName, externalFilePath, linkPath));
	}

	@Override
	public void setChildNodes(List<MetadataNode> customNodes) {
		metadataNode.addChildNodes(customNodes);
	}

	/**
	 * Custom nodes are those for which there is not an explicit set method, such as one that
	 * takes a scannable name like {@link BeamNexusDevice#setFluxScannableName(String)} or a value
	 * like {@link SourceNexusDevice#setProbe(String)}.
	 * <p>
	 * Note: this method isn't actually a setter, the name is for ease of use with spring. The
	 * given nodes are added to any existing nodes rather than overwriting them.
	 * <p>
	 * Also note: this method simply invokes {@link #setChildNodes(List)}. This name custom nodes
	 * makes more sense for subclasses that have explicit setters for particular nodes.
	 *
	 * @param customNodes list of nodes to add
	 */
	public void setCustomNodes(List<MetadataNode> customNodes) {
		setChildNodes(customNodes);
	}

	/**
	 * Removes the node (field or child group node) with the given name.
	 * @param nodeName name of node to remove
	 */
	@Override
	public void removeNode(String nodeName) {
		metadataNode.removeChildNode(nodeName);
	}

	public boolean hasChildNodes() {
		return metadataNode.hasChildNodes();
	}

	@Override
	public void register() {
//		INexusDevice.super.register(); // no longer compiles after adding INexusMetadataDevice interface
		INexusMetadataDevice.super.register(); // actual implementation is in INexusDevice.register
	}

	@Override
	public NexusObjectProvider<N> getNexusProvider(NexusScanInfo info) throws NexusException {
		final N nexusObject = createNexusObject(info);
		return createAndConfigureNexusWrapper(nexusObject);
	}

	protected NexusObjectWrapper<N> createAndConfigureNexusWrapper(final N nexusObject) {
		final NexusObjectWrapper<N> nexusWrapper = new NexusObjectWrapper<>(getNodeName(), nexusObject);
		nexusWrapper.setCategory(getCategory());
		nexusWrapper.setCollectionName(collectionName);
		return nexusWrapper;
	}

	@SuppressWarnings("unchecked")
	protected N createNexusObject(@SuppressWarnings("unused") NexusScanInfo info) throws NexusException {
		return (N) metadataNode.createNode();
	}

	public void setCategory(String nexusCategoryStr) {
		setCategory(NexusBaseClass.getBaseClassForName(nexusCategoryStr));
	}

	public void setCategory(NexusBaseClass nexusCategory) {
		this.nexusCategory = nexusCategory;
	}

	public NexusBaseClass getCategory() {
		return nexusCategory;
	}

	public void setCollectionName(String collectionName) {
		this.collectionName = collectionName;
	}

	public final NexusBaseClass getNexusBaseClass() {
		return metadataNode.getNexusBaseClass();
	}

	protected void setNexusBaseClass(NexusBaseClass nexusClass) {
		metadataNode.setNexusBaseClass(nexusClass);
	}

	protected void setNexusClass(String nxClass) {
		metadataNode.setNexusClass(nxClass);
	}

	@Override
	public void clearNodes() {
		metadataNode.clearChildNodes();
	}

	@Override
	public void addAttribute(MetadataAttribute attr) {
		metadataNode.addAttribute(attr);
	}

	@Override
	public void removeAttribute(String attrName) {
		metadataNode.removeAttribute(attrName);
	}

	@Override
	public void clearAttributes() {
		metadataNode.clearAttributes();
	}

	@Override
	public void setAttributes(List<MetadataAttribute> attributes) {
		metadataNode.addAttributes(attributes);
	}

	@Override
	public boolean hasAttributes() {
		return metadataNode.hasAttributes();
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + "[name=\"" + getName() + "\"]";
	}

}
