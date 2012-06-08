/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council Daresbury Laboratory
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

package gda.device;

import gda.factory.Findable;
import gda.factory.Reconfigurable;
import gda.jython.accesscontrol.MethodAccessProtected;
import gda.observable.IObservable;

/**
 * Interface to be implemented by all hardware and communication channels. A Device controls a specific type of hardware
 * for example a Motor controls a motor, a Serial controls a serial port.
 */
public interface Device extends Findable, IObservable, Reconfigurable {
	/**
	 * Set any attribute the implementing classes may provide
	 * 
	 * @param attributeName
	 *            is the name of the attribute
	 * @param value
	 *            is the value of the attribute
	 * @throws DeviceException
	 *             if an attribute cannot be set
	 */
	@MethodAccessProtected(isProtected=true)
	public void setAttribute(String attributeName, Object value) throws DeviceException;

	/**
	 * Get the value of the specified attribute
	 * 
	 * @param attributeName
	 *            is the name of the attribute
	 * @return the value of the attribute as an Object type
	 * @throws DeviceException
	 *             if an attribute cannot be retrieved
	 */
	public Object getAttribute(String attributeName) throws DeviceException;

	/**
	 * Close and unconfigure the device.
	 * 
	 * @throws DeviceException
	 */
	public void close() throws DeviceException;
	
	/**
	 * Sets the permission level for this object. If this is not set then a default value will be applied. 
	 * 
	 * @param newLevel
	 * @throws DeviceException
	 */
	@MethodAccessProtected(isProtected=true)	
	public void setProtectionLevel(int newLevel) throws DeviceException;
	
	/**
	 * @return int - the permission level for this object.
	 * @throws DeviceException
	 */
	public int getProtectionLevel() throws DeviceException;
}
