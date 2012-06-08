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

package gda.device.digitalio;

import gda.factory.Findable;

/**
 * A container for digital i/o (DIO) channel information. In this basic version just a channel name and default state is
 * held. Real DIO classes will need to keep a collection of these for the required channels.
 */
public class ChannelSetup implements Findable {
	// name and default state for DIO channels as set by Castor from XML
	// subclasses may like to add other parameters as well as default state.
	protected String name;

	protected String channelName;

	protected Integer defaultChannelState;

	// get and set methods for XML Castor i/o
	/**
	 * @param channelName
	 *            name set for channel
	 */
	public void setChannelName(String channelName) {
		this.channelName = channelName;
	}

	/**
	 * @return channel name to set for channel
	 */
	public String getChannelName() {
		return channelName;
	}

	/**
	 * @param defaultChannelState
	 *            default DIO state to set for channel
	 */
	public void setDefaultChannelState(int defaultChannelState) {
		this.defaultChannelState = defaultChannelState;
	}

	/**
	 * @return Default DIO state to set for channel
	 */
	public int getDefaultChannelState() {
		return defaultChannelState;
	}

	/**
	 * @param name
	 *            of this object
	 */
	@Override
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return name of this object
	 */
	@Override
	public String getName() {
		return name;
	}

}
