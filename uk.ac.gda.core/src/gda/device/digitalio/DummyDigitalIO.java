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

import gda.device.DeviceException;
import gda.device.DigitalIO;

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dummy implementation of the DigitalIO class providing simulation
 */
// FIXME remove implements DigitalIO not needed
public class DummyDigitalIO extends DigitalIOBase implements DigitalIO {
	
	private static final Logger logger = LoggerFactory.getLogger(DummyDigitalIO.class);
	
	// 3 lines are provided which are input and output and have their
	// state maintained between calls. They are stored in a HashMap
	// with the channel name as the key and Integer state as the value
	// FIXME requires private/protected/public.
	HashMap<String, Integer> hMap = new HashMap<String, Integer>();

	/**
	 * Constructor.
	 */
	public DummyDigitalIO() {
		// load up the mashMap guys!
		hMap.put("chan0", new Integer(LOW_STATE));
		hMap.put("chan1", new Integer(HIGH_STATE));
		hMap.put("tfgStart", new Integer(LOW_STATE));
	}
	
	@Override
	public void configure(){
		// no configuration required
	}

	/**
	 * @return port number -1
	 */
	public int getPortNumber() {
		return -1;
	}

	@Override
	public int getState(String channelName) throws DeviceException {
		int currentState = -1;

		try {
			currentState = hMap.get(channelName);
			logger.debug("=== getState() called : " + channelName + " state is " + currentState);
		} catch (NullPointerException e) {
			throw new DeviceException("valid digital i/o names are " + hMap.keySet());
		}
		return (currentState);
	}

	@Override
	public void setState(String channelName, int state) throws DeviceException {
		try {
			int currentState = (hMap.get(channelName)).intValue();
			logger.debug("=== setState() called : " + channelName + " state is " + currentState
					+ " requested state is " + state);
			hMap.put(channelName, new Integer(state));
		} catch (NullPointerException e) {
			throw new DeviceException("valid digital i/o names are " + hMap.keySet());
		}
	}
}
