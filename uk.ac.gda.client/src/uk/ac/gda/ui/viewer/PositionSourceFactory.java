/*-
 * Copyright © 2010 Diamond Light Source Ltd.
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

package uk.ac.gda.ui.viewer;

import gda.device.EnumPositioner;
import gda.device.Scannable;
import gda.device.ScannableMotionUnits;
import uk.ac.gda.ui.internal.viewer.EnumPositionerSource;
import uk.ac.gda.ui.internal.viewer.ScannableMotionUnitsPositionSource;
import uk.ac.gda.ui.internal.viewer.ScannablePositionSource;

public class PositionSourceFactory {

	/**
	 * Returns an IPositionSource based on the scannable
	 * @param scannable 
	 * @return a suitable implementation of IPositionSource
	 */
	public static IPositionSource<? extends Object> getPositionSource(Scannable scannable){
		IPositionSource<? extends Object> motor;
		if (scannable instanceof ScannableMotionUnits) {
			motor = new ScannableMotionUnitsPositionSource((ScannableMotionUnits)scannable);
		} else if (scannable instanceof EnumPositioner) {
			motor = new EnumPositionerSource((EnumPositioner)scannable);
		} else {
			motor = new ScannablePositionSource(scannable);
		}
		return motor;
	}
	
}
