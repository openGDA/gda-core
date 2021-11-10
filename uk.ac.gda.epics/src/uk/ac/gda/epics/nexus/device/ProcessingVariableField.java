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

import java.text.MessageFormat;

import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.scanning.device.AbstractMetadataField;
import org.eclipse.scanning.device.AbstractMetadataNode;

import gda.epics.connection.EpicsController;
import gov.aps.jca.CAException;
import gov.aps.jca.Channel;
import gov.aps.jca.TimeoutException;

/**
 * A {@link AbstractMetadataNode} field that is written as the position of a Processing Variable - PV.
 */
public class ProcessingVariableField extends AbstractMetadataField {

	private static final EpicsController EPICS_CONTROLLER = EpicsController.getInstance();
	private String pvName;
	private boolean blockIfGetFail = false;
	private Channel ch;

	public ProcessingVariableField() {
		// no-arg constructor for spring initialization
	}

	public ProcessingVariableField(String fieldName, String pvName) {
		super(fieldName);
		setPvName(pvName);
	}

	public ProcessingVariableField(String fieldName, String pvName, String units) {
		super(fieldName);
		setPvName(pvName);
		setUnits(units);
	}

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
			if (ch == null) {
				ch = EPICS_CONTROLLER.createChannel(pvName);
			}
			return EPICS_CONTROLLER.getValue(ch);
		} catch (CAException | TimeoutException e) {
			if (!blockIfGetFail) {
				//the default behaviour is not blocking if the PV is not reachable.
				return MessageFormat.format("Could not get data from {0} : {1}", pvName, e.getMessage());
			}
			throw new NexusException(MessageFormat.format("{0}: Could not get data from {1}", getName(), pvName), e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			if (!blockIfGetFail) {
				return MessageFormat.format("Interrupted when getting data from {0}", pvName);
			}
			throw new NexusException(MessageFormat.format("{0}: Interrupted when getting data from {1}", getName(), pvName), e);
		}
	}

	public boolean isBlockIfGetFail() {
		return blockIfGetFail;
	}

	public void setBlockIfGetFail(boolean blockIfGetFail) {
		this.blockIfGetFail = blockIfGetFail;
	}


}
