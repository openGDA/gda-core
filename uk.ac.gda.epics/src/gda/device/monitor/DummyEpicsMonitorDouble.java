/*-
 * Copyright © 2015 Diamond Light Source Ltd.
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

package gda.device.monitor;

import gda.device.DeviceException;
import gda.device.Monitor;
import uk.ac.gda.api.remoting.ServiceInterface;

/**
 * A dummy for EpicsMonitor returning a single double value.<br>
 * Optionally, the value can be set to vary on each call to getPosition(): the increment/decrement on each call, and the bounds on the value, are configurable.
 */
@ServiceInterface(Monitor.class)
public class DummyEpicsMonitorDouble extends DummyEpicsMonitor {

	private double lowerLimit = Double.MIN_VALUE;
	private double upperLimit = Double.MAX_VALUE;
	private double increment = 0.0;

	public DummyEpicsMonitorDouble() {
		super.setValue(0.0);
	}

	public void setLowerLimit(final Double lowerLim) {
		this.lowerLimit = lowerLim;
	}

	public void setUpperLimit(final Double upperLim) {
		this.upperLimit = upperLim;
	}

	public void setIncrement(final Double increment) {
		this.increment = increment;
	}

	@Override
	public void setValue(Object newVal) {
		if (newVal instanceof Number) {
			super.setValue(((Number) newVal).doubleValue());
		} else if (newVal instanceof String) {
			try {
				super.setValue(Double.parseDouble((String) newVal));
			} catch (NumberFormatException ex) {
				handleIllegalValue(newVal);
			}
		} else {
			handleIllegalValue(newVal);
		}
	}

	private void handleIllegalValue(Object value) {
		throw new IllegalArgumentException("Value '" + value + "' is not valid for " + this.getClass().getSimpleName());
	}

	@Override
	public Object getPosition() throws DeviceException {
		if (increment != 0.0) {
			double currentPosition = (double) super.getPosition();
			double newPosition = currentPosition + increment;

			// Check that new position is within limits: if not, reverse the direction of change
			if (newPosition < lowerLimit || newPosition > upperLimit) {
				increment = -increment;
				newPosition = currentPosition + increment;
			}

			super.setValue(newPosition);
		}

		return super.getPosition();
	}
}
