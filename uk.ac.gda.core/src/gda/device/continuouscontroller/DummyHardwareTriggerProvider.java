/*-
 * Copyright © 2016 Diamond Light Source Ltd.
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

package gda.device.continuouscontroller;

import gda.device.DeviceException;
import gda.device.detector.DummyDetector;

public class DummyHardwareTriggerProvider extends DummyDetector implements HardwareTriggerProvider {

	private int numberTriggers = 0;
	private double totalTime = 30;

	@Override
	public void setTriggerPeriod(double seconds) throws DeviceException {
	}

	public void setNumberTriggers(int numberTriggers) {
		this.numberTriggers = numberTriggers;
	}

	@Override
	public int getNumberTriggers() {
		return numberTriggers;
	}

	public void setTotalTime(double totalTime) {
		this.totalTime = totalTime;
	}

	@Override
	public double getTotalTime() throws DeviceException {
		return totalTime;
	}

}
