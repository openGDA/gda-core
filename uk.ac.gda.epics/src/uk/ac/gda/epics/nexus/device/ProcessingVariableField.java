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
	private Channel ch;

	public ProcessingVariableField() {
		// no-arg constructor for spring initialization
		setFailOnError(false);
	}

	public ProcessingVariableField(String fieldName, String pvName) {
		super(fieldName);
		setFailOnError(false);
		setPvName(pvName);
	}

	public ProcessingVariableField(String fieldName, String pvName, String units) {
		this(fieldName, pvName);
		setUnits(units);
	}

	public String getPvName() {
		return pvName;
	}

	public void setPvName(String pvName) {
		this.pvName = pvName;
	}

	private Object getPvValue(String pvName) throws NexusException {
		try {
			if (ch == null) {
				ch = EPICS_CONTROLLER.createChannel(pvName);
			}
			return EPICS_CONTROLLER.getValue(ch);
		} catch (CAException | TimeoutException e) {
			throw new NexusException(MessageFormat.format("{0}: Could not get data from {1} due to {2}", getName(), pvName, e.getMessage()), e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new NexusException(MessageFormat.format("{0}: Interrupted when getting data from {1} due to {2}", getName(), pvName, e.getMessage()), e);
		}
	}

	@Override
	protected Object getFieldValue() throws NexusException {
		return getPvValue(getPvName());
	}

}
