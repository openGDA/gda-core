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

package gda.device;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface AmplifierAutoGain extends CurrentAmplifier {

	double getUpperVoltageBound();

	double getLowerVoltageBound();

	double getInstantaneousVoltage() throws DeviceException;

	boolean isSupportsCoupling();

	boolean hasMultipleModes();

	String getCouplingMode() throws DeviceException;

	boolean increaseOrDecreaseGain(int i) throws DeviceException;

	public static final Logger logger = LoggerFactory.getLogger(AmplifierAutoGain.class);

	/**
	 * Adjusts the gain of the current amplifier so the its output the ADC input is within the voltage bounds.
	 *
	 * @throws DeviceException
	 */
	default void optimiseGain() throws DeviceException {
		logger.trace("optimiseGain called");
		// As the femto output range is -10 to +10 V the required gain is dependent on the absolute value.
		double voltage = Math.abs(getInstantaneousVoltage());

		// Check if its already in range if it is return
		if (voltage > getLowerVoltageBound() && voltage < getUpperVoltageBound()) {
			logger.trace("Don't need to adjust gain");
			return; // Don't need to change the gain
		}

		// Need to increase gain
		else if (voltage < getLowerVoltageBound()) {
			logger.trace("Increasing gain...");
			// Keep increasing while still to low
			while (Math.abs(getInstantaneousVoltage()) < getLowerVoltageBound()) {
				// Try to increase the gain if it can't be increased more warn and return
				if (!increaseGain()) {
					logger.warn("Couldn't increase gain further. Input current might be out of range");
					return;
				}
			}
		}

		// Need to decrease gain
		else { // voltage > upperVoltageBound
			logger.trace("Decreasing gain...");
			// Keep decreasing while still to high
			while (Math.abs(getInstantaneousVoltage()) > getUpperVoltageBound()) {
				// Try to increase the gain if it can't be increased more warn and return
				if (!decreaseGain()) {
					logger.warn("Couldn't decrease gain further. Input current might be out of range");
					return;
				}
			}
		}
		logger.debug("Optimised gain");
	}

	/**
	 * Increase the gain by one step in the current mode if possible.
	 *
	 * @return true if the gain was changed
	 * @throws DeviceException
	 */
	default boolean increaseGain() throws DeviceException {
		return increaseOrDecreaseGain(1);
	}

	/**
	 * Decrease the gain by one step in the current mode if possible.
	 *
	 * @return true if the gain was changed
	 * @throws DeviceException
	 */
	default boolean decreaseGain() throws DeviceException {
		return increaseOrDecreaseGain(-1);
	}

}