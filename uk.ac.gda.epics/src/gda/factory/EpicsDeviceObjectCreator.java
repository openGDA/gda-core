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

package gda.factory;

import gda.device.epicsdevice.EpicsDeviceFactory;

/**
 * An object creator that creates an {@link EpicsDeviceFactory}.
 * 
 * @deprecated Adding Spring beans directly to the application context is the
 * preferred method for instantiating objects. A Spring bean for adding EPICS
 * devices directly to the Spring application context will be added in a future
 * GDA release.
 */
@Deprecated
public class EpicsDeviceObjectCreator implements IObjectCreator {

	// TODO add parameter to EpicsDeviceObjectCreator to allow the XML file used to create the EpicsDeviceFactory to be specified
	boolean simulated=false;	
	/**
	 * @return True if devices run in dummy mode
	 */
	public boolean isSimulated() {
		return simulated;
	}

	/**
	 * @param simulated
	 */
	public void setSimulated(boolean simulated) {
		this.simulated = simulated;
	}

	/**
	 * @return name
	 */
	public String getName() {
		return name;
	}

	private String name;
	
	/**
	 * Sets the name to use when creating the EPICS device factory.
	 * 
	 * @param name the factory name
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	@Override
	public EpicsDeviceFactory getFactory() throws FactoryException {
		EpicsDeviceFactory epicsDeviceFactory = new EpicsDeviceFactory();
		epicsDeviceFactory.setName(name);
		epicsDeviceFactory.setSimulated(simulated);
		epicsDeviceFactory.setNameSuffix(nameSuffix);
		return epicsDeviceFactory;
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName() + "[name=" + name + "]";
	}

	@Override
	public boolean isLocal() {
		return true;
	}

	private String nameSuffix="";
	public void setNameSuffix(String nameSuffix){
		this.nameSuffix = nameSuffix;
	}
		
}
