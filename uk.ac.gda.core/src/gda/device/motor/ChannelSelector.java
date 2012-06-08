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

package gda.device.motor;

import gda.device.MotorException;

/**
 * Interface to provide a channel selection mechanism for McLennan motion controllers
 * 
 * @see DummyChannelSelector also
 * @see McLennanChannelSelector also
 * @see McLennanChannelSelectorFactory
 */
public interface ChannelSelector {
	/**
	 * Select the motor channel to use
	 * 
	 * @param motor
	 *            the motor
	 * @param channel
	 *            the channel number to use
	 * @return true if selection successful, false if channel already in use
	 * @throws MotorException
	 */
	public boolean selectChannel(Object motor, int channel) throws MotorException;

	/**
	 * Release the motor channel previously selected
	 * 
	 * @param motor
	 *            the motor
	 * @throws MotorException
	 */
	public void releaseChannel(Object motor) throws MotorException;

	/**
	 * Get the name of the channel selector
	 * 
	 * @return the name of the channel selector
	 */
	public String getName();
}
