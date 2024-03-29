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

import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.analysis.api.tree.Node;
import org.eclipse.dawnsci.analysis.tree.TreeFactory;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusNodeFactory;
import org.eclipse.dawnsci.nexus.NexusUtils;
import org.eclipse.january.dataset.Dataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractMetadataField extends AbstractMetadataNode implements MetadataField {

	private static final Logger logger = LoggerFactory.getLogger(AbstractMetadataField.class);

	public static final String ATTRIBUTE_NAME_UNITS = "units";
	public static final String ATTRIBUTE_NAME_LOCAL_NAME = "local_name";

	private String units = null;

	private boolean failOnError = true;

	protected AbstractMetadataField() {
		// no-arg constructor for spring initialization
	}

	protected AbstractMetadataField(String name) {
		super(name);
	}

	@Override
	public String getUnits() throws NexusException {
		return units;
	}

	@Override
	public void setUnits(String units) {
		this.units = units;
	}

	public boolean isFailOnError() {
		return failOnError;
	}

	public void setFailOnError(boolean failOnError) {
		this.failOnError = failOnError;
	}

	protected final DataNode createDataNode(final Object value) {
		final DataNode dataNode = NexusNodeFactory.createDataNode();
		final Dataset dataset = NexusUtils.createFromObject(value, getName());
		dataNode.setDataset(dataset);

		return dataNode;
	}

	@Override
	protected void createAttributes(Node node) throws NexusException {
		super.createAttributes(node);

		final DataNode dataNode = (DataNode) node;
		addAttributeIfNonNull(dataNode, ATTRIBUTE_NAME_UNITS, getUnits());
		addAttributeIfNonNull(dataNode, ATTRIBUTE_NAME_LOCAL_NAME, getLocalName());
	}

	@SuppressWarnings("unused") // overridden method in subclasses may throw NexusException
	protected String getLocalName() throws NexusException {
		return null;
	}

	private void addAttributeIfNonNull(final DataNode dataNode, String attrName, String attrValue) {
		if (attrValue != null) {
			dataNode.addAttribute(TreeFactory.createAttribute(attrName, attrValue));
		}
	}

	@Override
	protected final DataNode doCreateNode() throws NexusException {
		final Object fieldValue = getFieldValue();
		return createDataNode(fieldValue);
	}

	@Override
	protected DataNode handleError(NexusException e) throws NexusException {
		// propagate the exception if failOnError is true, or the thread has been interrupted (the exception is a wrapped InterruptedException in this case)
		if (isFailOnError() || Thread.interrupted()) {
			throw e;
		} else {
			logger.error("Could not write field {}", getName(), e);

			final DataNode dataNode = NexusNodeFactory.createDataNode();
			dataNode.setString(e.getMessage());
			return dataNode;
		}
	}

	protected abstract Object getFieldValue() throws NexusException;

}
