/*-
 * Copyright © 2018 Diamond Light Source Ltd.
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

import gda.device.DeviceException;

/**
 * This class holds the beam dimensions,
 * useful for defining scan paths in terms of beam overlap.
 */
public class BeamDimensions extends ScannableBase {

	private double x = 0.1;
	private double y = 0.1;

	public BeamDimensions() {
		setInputNames(new String[] {"x", "y"});
	}

	/**
	 * Set beam size in dimension of fast axis
	 * @param x in same units of fast axis
	 */
	public void setX(double x) {
		this.x = x;
	}

	/**
	 * Set beam size in dimension of slow axis
	 * @param y in same units of slow axis
	 */
	public void setY(double y) {
		this.y = y;
	}

	@Override
	public Object getPosition() throws DeviceException {
		return new Object[] {x, y};
	}

	@Override
	public boolean isBusy() throws DeviceException {
		return false;
	}

	@Override
	public void rawAsynchronousMoveTo(Object position) throws DeviceException {
		Double[] array = ScannableUtils.objectToArray(position);
		setX(array[0]);
		setY(array[1]);
	}

}
