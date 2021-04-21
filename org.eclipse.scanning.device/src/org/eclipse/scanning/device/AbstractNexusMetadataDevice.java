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

public abstract class AbstractNexusMetadataDevice<N extends NXobject> implements INexusDevice<N> {

	private NexusBaseClass nexusCategory = null;

	private final GroupMetadataNode<N> metadataNode;

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
		return metadataNode.getName();
	}

	public void setName(String name) {
		metadataNode.setName(name);
	}

	public void addField(MetadataNode node) {
		metadataNode.addChildNode(node);
	}

	public void addScannableField(String fieldName, String scannableName) {
		metadataNode.addChildNode(new ScannableField(fieldName, scannableName));
	}

	public void addScalarField(String fieldName, Object fieldValue) {
		metadataNode.addChildNode(new ScalarField(fieldName, fieldValue));
	}

	protected void setDefaultValue(String nxName, Object defaultValue) {
		metadataNode.addChildNode(new ScalarField(nxName, defaultValue, true));
	}

	public void addLinkedField(String fieldName, String linkPath) {
		metadataNode.addChildNode(new LinkedField(fieldName, linkPath));
	}

	/**
	 * Custom nodes are those for which there is not an explicit set method, such as one that
	 * takes a scannable name like {@link BeamNexusDevice#setFluxScannableName(String)} or a value
	 * like {@link SourceNexusDevice#setProbe(String)}.
	 * <p>
	 * Note: this method isn't actually a setter, the name is for ease of use with spring. The
	 * given nodes are added to any existing nodes rather than overwriting them.
	 * TODO: is there a better way of doing this?
	 *
	 * @param customNodes list of nodes to add
	 */
	public void setCustomNodes(List<MetadataNode> customNodes) {
		metadataNode.addChildNodes(customNodes);
	}

	@Override
	public void register() {
		INexusDevice.super.register();
	}

	@Override
	public NexusObjectProvider<N> getNexusProvider(NexusScanInfo info) throws NexusException {
		final N nexusObject = createNexusObject(info);
		return createAndConfigureNexusWrapper(nexusObject);
	}

	protected NexusObjectWrapper<N> createAndConfigureNexusWrapper(final N nexusObject) {
		final NexusObjectWrapper<N> nexusWrapper = new NexusObjectWrapper<>(getName(), nexusObject);
		nexusWrapper.setCategory(getCategory());
		return nexusWrapper;
	}

	protected N createNexusObject(@SuppressWarnings("unused") NexusScanInfo info) throws NexusException {
		return metadataNode.createNode();
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

	public final NexusBaseClass getNexusBaseClass() {
		return metadataNode.getNexusBaseClass();
	}

	protected void setNexusBaseClass(NexusBaseClass nexusClass) {
		metadataNode.setNexusBaseClass(nexusClass);
	}

	protected void setNexusClass(String nxClass) {
		metadataNode.setNexusClass(nxClass);
	}

}
