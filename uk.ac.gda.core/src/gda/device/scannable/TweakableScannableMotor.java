/*-
 * Copyright © 2022 Diamond Light Source Ltd.
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

import gda.device.ITweakable;
import gda.device.ITweakableScannableMotor;
import gda.device.MotorException;
import uk.ac.gda.api.remoting.ServiceInterface;

/**
 * add position tweak functions to {@link ScannableMotor}
 */
@ServiceInterface(ITweakableScannableMotor.class)
public class TweakableScannableMotor extends ScannableMotor implements ITweakableScannableMotor {
	private ITweakable tweaker;

	@Override
	public void forward() throws MotorException {
		tweaker.forward();
	}

	@Override
	public void reverse() throws MotorException {
		tweaker.reverse();
	}

	@Override
	public void setIncrement(double value) {
		tweaker.setIncrement(value);
	}

	@Override
	public double getIncrement() {
		return tweaker.getIncrement();
	}

	public ITweakable getTweaker() {
		return tweaker;
	}

	public void setTweaker(ITweakable tweaker) {
		this.tweaker = tweaker;
	}

}
