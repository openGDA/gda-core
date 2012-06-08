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

package gda.device.detector.countertimer;


/**
 * Base class for CounterTimers which are operating the TFG timer
 */
public abstract class TFGCounterTimer extends CounterTimerBase {

	protected boolean isTFGv2 = false;

	/**
	 * This affects whether the output always contains the live time in the first scaler channel.
	 * <p>
	 * The GdHist object will also need its width set to one more than the number of scalers to be read.
	 * 
	 * @return true if this is talking to a TFG2
	 */
	public boolean isTFGv2() {
		return isTFGv2;
	}

	/**
	 * @param isTFGv2
	 */
	public void setTFGv2(boolean isTFGv2) {
		this.isTFGv2 = isTFGv2;
	}
	
}
