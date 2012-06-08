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

import gda.device.DeviceBase;
import gda.device.DeviceException;
import gda.device.DigitalIO;

import java.util.Date;

/**
 * An base implementation for all DigitalIO types. This provides all functionality except for the primitive operations
 * getState() and setState() which must be provided by the concrete subclasses. Parameters including Digital I/O names
 * and input or output status must be provided but "iChan0","oChan3" etc. would be suitable for generic names or
 * "tfgStart" as a specific example.
 */
public abstract class DigitalIOBase extends DeviceBase implements DigitalIO {
	// default and current edge time delay and 2 line timeout to be
	// optionally set in XML (msecs)
	protected final int edgeSyncDelayTimeDefault = 0;

	protected final int twoLineSyncTimeoutDefault = 10000;

	protected int edgeSyncDelayTime = edgeSyncDelayTimeDefault;

	protected int twoLineSyncTimeout = twoLineSyncTimeoutDefault;

	/**
	 * Set the required delay after edge sync operations
	 * 
	 * @param edgeSyncDelayTime
	 *            the required 2 line sync timeout in msecs
	 * @throws DeviceException
	 */
	@Override
	public void setEdgeSyncDelayTime(int edgeSyncDelayTime) throws DeviceException {
		this.edgeSyncDelayTime = edgeSyncDelayTime;
	}

	/**
	 * Get the current delay time after edge sync operations
	 * 
	 * @return the current edge sync delay time in msecs
	 * @throws DeviceException
	 */
	@Override
	public int getEdgeSyncDelayTime() throws DeviceException {
		return edgeSyncDelayTime;
	}

	/**
	 * Set the required timeout for 2 line sync operations
	 * 
	 * @param twoLineSyncTimeout
	 *            the required 2 line sync timeout in msecs
	 * @throws DeviceException
	 */
	@Override
	public void setTwoLineSyncTimeout(int twoLineSyncTimeout) throws DeviceException {
		this.twoLineSyncTimeout = twoLineSyncTimeout;
	}

	/**
	 * Get the current timeout for 2 line sync operations
	 * 
	 * @return current 2 line sync timeout in msecs
	 * @throws DeviceException
	 */
	@Override
	public int getTwoLineSyncTimeout() throws DeviceException {
		return twoLineSyncTimeout;
	}

	/**
	 * Get the current logic state for the specified channel
	 * 
	 * @param channelName
	 *            digital i/o channel name to read
	 * @return current TTL line state
	 * @throws DeviceException
	 */
	@Override
	public abstract int getState(String channelName) throws DeviceException;

	/**
	 * Set the current logic state for the specified channel
	 * 
	 * @param channelName
	 *            digital i/o channel name to write to
	 * @param state
	 *            the required TTL line state, 0=low and any other value=high
	 * @throws DeviceException
	 */
	@Override
	public abstract void setState(String channelName, int state) throws DeviceException;

	/**
	 * Carries out a negative edge sync operation
	 * 
	 * @param channelName
	 *            digital i/o channel name to write to
	 * @throws DeviceException
	 */
	@Override
	public synchronized void setNegativeEdgeSync(String channelName) throws DeviceException {
		if (getState(channelName) != HIGH_STATE) {
			setState(channelName, HIGH_STATE);
		}
		setState(channelName, LOW_STATE);
		try {
			Thread.sleep(edgeSyncDelayTime);
		} catch (InterruptedException e) {
		}
		setState(channelName, HIGH_STATE);
	}

	/**
	 * Carries out a positive edge sync operation
	 * 
	 * @param channelName
	 *            digital i/o channel name to write to
	 * @throws DeviceException
	 */
	@Override
	public synchronized void setPositiveEdgeSync(String channelName) throws DeviceException {
		if (getState(channelName) != LOW_STATE) {
			setState(channelName, LOW_STATE);
		}
		setState(channelName, HIGH_STATE);
		try {
			Thread.sleep(edgeSyncDelayTime);
		} catch (InterruptedException e) {
		}
		setState(channelName, LOW_STATE);
	}

	/**
	 * Carries out a 2 line handshake with an external device which obeys the protocol rules.
	 * 
	 * @param inputChannelName
	 *            digital i/o channel name to read input line state
	 * @param outputChannelName
	 *            digital i/o channel name to write output line state
	 * @throws DeviceException
	 */
	@Override
	public synchronized void setNegative2LineSync(String inputChannelName, String outputChannelName)
			throws DeviceException {
		// ensure initial state is resting at high
		if (getState(outputChannelName) != HIGH_STATE) {
			setState(outputChannelName, HIGH_STATE);
		}
		// wait for external unit "non-busy" high state
		waitForState(inputChannelName, HIGH_STATE, "TTL_high");
		// trigger external unit with low state
		setState(outputChannelName, LOW_STATE);
		// wait for external unit "acknowledge"/"busy" low
		waitForState(inputChannelName, LOW_STATE, "TTL_low");
		// acknowledge / signal ready for idle status high
		setState(outputChannelName, HIGH_STATE);
		// wait for external unit "finished" high state
		waitForState(inputChannelName, HIGH_STATE, "TTL_high");
	}

	/**
	 * Carries out a 2 line handshake with an external device which obeys the protocol rules.
	 * 
	 * @param inputChannelName
	 *            digital i/o channel name to read input line state
	 * @param outputChannelName
	 *            digital i/o channel name to write output line state
	 * @throws DeviceException
	 */
	@Override
	public synchronized void setPositive2LineSync(String inputChannelName, String outputChannelName)
			throws DeviceException {
		// ensure initial state is resting at low
		if (getState(outputChannelName) != LOW_STATE) {
			setState(outputChannelName, LOW_STATE);
		}
		// wait for external unit "non-busy" low state
		waitForState(inputChannelName, LOW_STATE, "TTL_low");
		// trigger external unit with high state
		setState(outputChannelName, HIGH_STATE);
		// wait for external unit "acknowledge"/"busy" high
		waitForState(inputChannelName, HIGH_STATE, "TTL_high");
		// acknowledge / signal ready for idle status low
		setState(outputChannelName, LOW_STATE);
		// wait for external unit "finished" low state
		waitForState(inputChannelName, LOW_STATE, "TTL_low");
	}

	@Override
	public String toString() {
		return this.getName();
	}

	/**
	 * Waits for a named input line name to reach a specified state or generate an exception if outside the timeout
	 * period. In this case a message is generated using the string form of the required state.
	 * 
	 * @param inputChannelName
	 *            required input channel name
	 * @param requiredState
	 *            required State to be waited for
	 * @param requiredStateStr
	 *            string form of the required state
	 * @throws DeviceException
	 */
	private void waitForState(String inputChannelName, int requiredState, String requiredStateStr)
			throws DeviceException {
		long msec0 = new Date().getTime(); // starting msec reference time

		// poll line state while still within total timeout period
		while (getState(inputChannelName) != requiredState) {
			if ((new Date().getTime() - msec0) >= twoLineSyncTimeout) {
				throw (new DeviceException("Digital I/O timed out waiting for " + inputChannelName + " to reach state "
						+ requiredStateStr + "\n" + " Timeout period (msecs) currently " + twoLineSyncTimeout));
			}
		}
	}

}
