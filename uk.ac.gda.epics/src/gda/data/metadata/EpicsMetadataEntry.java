/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package gda.data.metadata;

import gda.epics.connection.EpicsController;
import gda.factory.FactoryException;
import gov.aps.jca.Channel;

/**
 * A {@link MetadataEntry} that gets it value by reading an EPICS PV.
 */
public class EpicsMetadataEntry extends MetadataEntry {

	/**
	 * The timeout for reading PVs referenced by metadata objects.
	 */
	protected static double EPICS_TIMEOUT = 0.5;

	private String pvName;
	
	private transient EpicsController controller;
	
	/**
	 * Creates an EPICS metadata entry.
	 */
	public EpicsMetadataEntry() {
		// do nothing
	}
	
	/**
	 * Creates an EPICS metadata entry with the specified name that will
	 * fetch the value of the specified PV.
	 * 
	 * @param name the metadata entry name
	 * @param pvName the EPICS PV name
	 */
	public EpicsMetadataEntry(String name, String pvName) {
		setName(name);
		setPvName(pvName);
	}
	
	/**
	 * Sets the PV from which this metadata entry will retrieve its value.
	 * 
	 * @param pvName the EPICS PV name
	 */
	public void setPvName(String pvName) {
		this.pvName = pvName;
	}
	
	@Override
	public void configure() throws FactoryException {
		super.configure();
		controller = EpicsController.getInstance();
	}

	@Override
	public String readActualValue() throws Exception {
		Channel epicsChannel = controller.createChannel(pvName, EPICS_TIMEOUT);
		String value = controller.cagetString(epicsChannel);
		controller.destroy(epicsChannel);
		return value;
	}

}
