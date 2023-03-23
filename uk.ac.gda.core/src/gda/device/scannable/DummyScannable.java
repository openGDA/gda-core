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

package gda.device.scannable;

import gda.device.ControllerRecord;
import gda.device.DeviceException;
import gda.device.ScannableMotion;
import uk.ac.gda.api.remoting.ServiceInterface;

/**
 * Dummy object extending ScannableMotion which represents a single number.
 */
@ServiceInterface(ScannableMotion.class)
public class DummyScannable extends ScannableMotionBase implements ControllerRecord {

	protected double currentPosition = 0;
	protected double increment = 0.0;
	private String controllerRecordName = null; // i.e. pvName, for testing nexus writing

	public DummyScannable() {
		this.setInputNames(new String[]{""});
		try {
			this.setLowerGdaLimits(-Double.MAX_VALUE);
			this.setUpperGdaLimits(Double.MAX_VALUE);
		} catch (Exception e) {
			// do nothing
		}
	}

	public DummyScannable(String name) {
		this();
		setName(name);
	}

	@Override
	public void setName(String name) {
		super.setName(name);
		setInputNames(new String[] {name});
	}

	public DummyScannable(String name, double d) {
		this(name);
		this.currentPosition = d;
	}

	public DummyScannable(String name, double d, String pvName) {
		this(name, d);
		this.controllerRecordName = pvName;
	}

	public void setIncrement(final Double increment) {
		this.increment = increment;
	}

	@Override
	public String getControllerRecordName() {
		return controllerRecordName;
	}

	public void setControllerRecordName(String controllerRecordName) {
		this.controllerRecordName = controllerRecordName;
	}

	@Override
	public void rawAsynchronousMoveTo(Object position) throws DeviceException {
		Double[] positionArray = ScannableUtils.objectToArray(position);
		this.currentPosition = positionArray[0];
		notifyIObservers(this, new ScannablePositionChangeEvent(currentPosition));
	}

	@Override
	public Object rawGetPosition() throws DeviceException {
		return currentPosition;
	}

	@Override
	public boolean isBusy() {
		return false;
	}

	@Override
	public Object getPosition() throws DeviceException {
		if (increment != 0.0) {
			double newPosition = currentPosition + increment;

			// Check that new position is within limits: if not, reverse the direction of change
			if (checkPositionValid(newPosition) != null) {
				increment = -increment;
				newPosition = currentPosition + increment;
			}

			moveTo(newPosition);
		}

		return super.getPosition();
	}
}
