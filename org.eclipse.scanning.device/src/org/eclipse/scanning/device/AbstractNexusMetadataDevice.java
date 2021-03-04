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

import static java.util.stream.Collectors.toMap;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.eclipse.dawnsci.nexus.INexusDevice;
import org.eclipse.dawnsci.nexus.NXobject;
import org.eclipse.dawnsci.nexus.NexusBaseClass;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusNodeFactory;
import org.eclipse.dawnsci.nexus.NexusScanInfo;
import org.eclipse.dawnsci.nexus.builder.NexusObjectProvider;
import org.eclipse.dawnsci.nexus.builder.NexusObjectWrapper;
import org.eclipse.scanning.api.INameable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractNexusMetadataDevice<N extends NXobject> implements INexusDevice<N> {

	private static final Logger logger = LoggerFactory.getLogger(AbstractNexusMetadataDevice.class);

	private String name;

	private NexusBaseClass nexusClass = null;

	private NexusBaseClass nexusCategory = null;

	/**
	 * A map of the predetermined fields in this device (those with an explicit setter), keyed by name.
	 */
	private Map<String, MetadataNode> predeterminedNodes = new HashMap<>();

	/**
	 * A map of the custom fields in this device (those without an explicit setter), keyed by name.
	 */
	private Map<String, MetadataNode> customNodes = new HashMap<>();

	protected AbstractNexusMetadataDevice(NexusBaseClass nexusBaseClass) {
		nexusClass = nexusBaseClass;
	}

	@Override
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void addField(MetadataNode node) {
		predeterminedNodes.put(node.getName(), node);
	}

	public void addScannableField(String fieldName, String scannableName) {
		addField(new ScannableField(fieldName, scannableName));
	}

	public void addScalarField(String fieldName, Object fieldValue) {
		addField(new ScalarField(fieldName, fieldValue));
	}

	public void addLinkedField(String fieldName, String linkPath) {
		addField(new LinkedField(fieldName, linkPath));
	}

	/**
	 * Custom nodes are those for which there is not an explicit set method, such as one that
	 * takes a scannable name like {@link BeamNexusDevice#setFluxScannableName(String)} or a value
	 * like {@link SourceNexusDevice#setProbe(String)}.
	 * @param customNodes list of nodes to add
	 */
	public void setCustomNodes(List<MetadataNode> customNodes) {
		this.customNodes = customNodes.stream().collect(toMap(INameable::getName, Function.identity()));
	}

	@Override
	public void register() {
		INexusDevice.super.register();
		checkPropertiesSet();
	}

	protected void checkPropertiesSet() {
		for (Field field : getClass().getFields()) {
			field.setAccessible(true);
			try {
				if (field.get(this) == null) {
					logger.warn("property {} not set for {} ''{}''", field.getName(), getClass().getSimpleName(), getName());
				}
			} catch (IllegalArgumentException | IllegalAccessException e) {
				logger.error("TODO put description of error here", e);
			}
		}
	}

	@Override
	public NexusObjectProvider<N> getNexusProvider(NexusScanInfo info) throws NexusException {
		final N nexusObject = createNexusObject(info);
		writeChildNodes(nexusObject);
		final NexusObjectWrapper<N> nexusWrapper = createAndConfigureNexusWrapper(nexusObject);
		return nexusWrapper;
	}

	protected NexusObjectWrapper<N> createAndConfigureNexusWrapper(final N nexusObject) {
		final NexusObjectWrapper<N> nexusWrapper = new NexusObjectWrapper<>(getName(), nexusObject);
		nexusWrapper.setCategory(getCategory());
		return nexusWrapper;
	}

	public void setCategory(NexusBaseClass nexusCategory) {
		this.nexusCategory = nexusCategory;
	}

	public NexusBaseClass getCategory() {
		return nexusCategory;
	}

	@SuppressWarnings("unchecked")
	protected N createNexusObject(@SuppressWarnings("unused") NexusScanInfo info) {
		return (N) NexusNodeFactory.createNXobjectForClass(getNexusBaseClass());
	}

	public final NexusBaseClass getNexusBaseClass() {
		return nexusClass;
	}

	protected void writeChildNodes(N nxObject) throws NexusException {
		for (MetadataNode node : predeterminedNodes.values()) {
			node.writeNode(nxObject);
		}
		for (MetadataNode node : customNodes.values()) {
			node.writeNode(nxObject);
		}
	}

}
