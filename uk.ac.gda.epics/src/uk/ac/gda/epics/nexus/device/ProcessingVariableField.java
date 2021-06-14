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

package uk.ac.gda.epics.nexus.device;

import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.scanning.device.AbstractMetadataField;
import org.eclipse.scanning.device.AbstractMetadataNode;

import gda.epics.CAClient;
import gov.aps.jca.CAException;
import gov.aps.jca.TimeoutException;

/**
 * A {@link AbstractMetadataNode} field that is written as the position of a Processing Variable - PV.
 */
public class ProcessingVariableField extends AbstractMetadataField {

	public ProcessingVariableField() {
		// no-arg constructor for spring initialization
	}

	public ProcessingVariableField(String fieldName, String pvName) {
		super(fieldName);
		setPvName(pvName);
	}

	private String pvName;

	public String getPvName() {
		return pvName;
	}

	public void setPvName(String scannableName) {
		this.pvName = scannableName;
	}

	@Override
	protected DataNode createDataNode() throws NexusException {
		final Object value = getPvValue(pvName);
		return createDataNode(value);
	}

	private Object getPvValue(String pvName) throws NexusException {
		try {
			return CAClient.get(pvName);
		} catch (CAException | TimeoutException e) {
			throw new NexusException("Could not get data from: " + pvName, e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new NexusException("Interrupted when getting data from: " + pvName);
		}
	}

}
