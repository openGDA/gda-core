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

package gda.device.controlpoint;

import org.python.core.PyString;

import gda.device.ControlPoint;
import gda.device.DeviceException;
import gda.device.scannable.ScannableMotionBase;
import gda.device.scannable.ScannablePositionChangeEvent;

/**
 * The Class DummyControlPoint.
 */
public class DummyControlPoint extends ScannableMotionBase implements ControlPoint {
	double latestValue;

	@Override
	public double getValue() throws DeviceException {
		return latestValue;
	}

	@Override
	public void setValue(double target) throws DeviceException {
		latestValue = target;
	}

	@Override
	public void asynchronousMoveTo(Object position) throws DeviceException {

		// Have to account whether this method was called in Jython or Java
		// (would not be a problem except the methdo takes an Object, so not
		// casting done within Jython for us).
		double setPoint = Double.NaN;
		if (position instanceof String) {
			setPoint = Double.valueOf((String) position);
		} else if (position instanceof PyString) {
			setPoint = Double.valueOf(((PyString) position).toString());
		} else if (position instanceof Number) {
			setPoint = ((Number) position).doubleValue();
		}
		
		if (Double.isNaN(setPoint)) {
			throw new IllegalArgumentException("ControlPointAdapter.asynchronousMoveTo - invalid position type = "
					+ position.toString());
		}
		setValue(setPoint);
		notifyIObservers(this, new ScannablePositionChangeEvent(new Double(setPoint)));
	}

	@Override
	public Object getPosition() throws DeviceException {
		return getValue();
	}

	@Override
	public boolean isBusy() {
		return false;
	}

}
