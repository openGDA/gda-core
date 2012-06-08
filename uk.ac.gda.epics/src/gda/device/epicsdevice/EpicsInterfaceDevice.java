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

package gda.device.epicsdevice;

/**
 * An interface that all EPICS devices that accessed via the GDA/Epics interface must implement. This interface provides method
 * specification for getting and setting EPICS record name(s) and PV name(s) which required by CASTOR marshall/unmarshall framework.
 */
public interface EpicsInterfaceDevice {
	/**
	 * Get the device name from the GDA/Epics interface file
	 * @return deviceName
	 */
	public String getDeviceName(); 
	
	/**
	 * set the device name from the GDA/Epics interface file
	 * 
	 * @param name
	 */
	public void setDeviceName(String name);
}
