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
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusNodeFactory;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.scanning.api.IScannable;
import org.eclipse.scanning.api.scan.ScanningException;

/**
 * A {@link AbstractMetadataNode} field that is written as the position of a scannable.
 */
public class ScannableField extends AbstractMetadataNode {

	public ScannableField() {
		// no-arg constructor for spring initialization
	}

	public ScannableField(String fieldName, String scannableName) {
		super(fieldName);
		setScannableName(scannableName);
	}

	private String scannableName;

	public String getScannableName() {
		return scannableName;
	}

	public void setScannableName(String scannableName) {
		this.scannableName = scannableName;
	}

	@Override
	public DataNode createNode() throws NexusException {
		final DataNode dataNode = NexusNodeFactory.createDataNode();
		final Dataset dataset = DatasetFactory.createFromObject(getScannableValue(scannableName));
		dataset.setName(getName());
		dataNode.setDataset(dataset);
		return dataNode;
	}

	@SuppressWarnings("unchecked")
	protected <T> T getScannableValue(String scannableName) throws NexusException {
		try {
			IScannable<?> scannable = Services.getScannableDeviceService().getScannable(scannableName);
			return (T) scannable.getPosition();
		} catch (ScanningException e) {
			throw new NexusException("Could not find scannable with name: " + scannableName);
		}
	}

}
