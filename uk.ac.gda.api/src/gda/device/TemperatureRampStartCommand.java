/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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

import java.io.Serializable;

/**
 * Message class to trigger a controlled temperature ramp
 */
public class TemperatureRampStartCommand implements Serializable {

	private double target;
	private double rate;
	private double dwellTime;

	public TemperatureRampStartCommand(double target, double rate, double dwellTime) {
		this.target = target;
		this.rate = rate;
		this.dwellTime = dwellTime;
	}

	public double getTarget() {
		return target;
	}

	public double getRate() {
		return rate;
	}

	public double getDwellTime() {
		return dwellTime;
	}

}
