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

package gda.device.bpm;

import gda.device.BPM;

/**
 * A dummy beam position monitor.
 */
public class DummyBPM extends BPMBase implements BPM {
	@Override
	public void configure() {
	}

	@Override
	public double getX() {
		double x = 0.01 + (Math.random() * 0.5);

		return x;
	}

	@Override
	public double getY() {
		double y = 0.01 + (Math.random() * 0.5);

		return y;
	}
}
