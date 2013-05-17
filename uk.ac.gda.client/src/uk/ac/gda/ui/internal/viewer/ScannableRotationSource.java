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

package uk.ac.gda.ui.internal.viewer;

import gda.device.DeviceException;
import gda.device.ScannableMotionUnits;
import gda.device.scannable.ScannableUtils;
import uk.ac.gda.ui.viewer.IRotationSource;

public class ScannableRotationSource extends ScannableMotionUnitsPositionSource implements IRotationSource{

	public ScannableRotationSource(ScannableMotionUnits scannable) {
		super(scannable);
	}

	@Override
	public double calcMoveMinusRelative(double value) throws DeviceException {
		double currentPosition = ScannableUtils.getCurrentPositionArray(scannable)[0];
		return currentPosition - value;
		
	}

	@Override
	public double calcMovePlusRelative(double value) throws DeviceException {
		double currentPosition = ScannableUtils.getCurrentPositionArray(scannable)[0];
		return currentPosition + value;	
	}

}
