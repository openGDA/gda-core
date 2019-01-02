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

package uk.ac.gda.server.ncd.timing.data;

import java.io.Serializable;

public class SimpleTimerConfiguration implements Serializable {
	private static final long serialVersionUID = -4311579344327127281L;

	private double exposure;
	private int numberOfFrames;
	private boolean delay;
	private double delayTime;

	public static void copy (SimpleTimerConfiguration from, SimpleTimerConfiguration to) {
		to.exposure = from.exposure;
		to.numberOfFrames = from.numberOfFrames;
		to.delay = from.delay;
		to.delayTime = from.delayTime;
	}

	public double getExposure() {
		return exposure;
	}

	public void setExposure(double exposure) {
		this.exposure = exposure;
	}

	public int getNumberOfFrames() {
		return numberOfFrames;
	}

	public void setNumberOfFrames(int numberOfFrames) {
		this.numberOfFrames = numberOfFrames;
	}

	public boolean isDelay() {
		return delay;
	}

	public void setDelay(boolean delay) {
		this.delay = delay;
	}

	public double getDelayTime() {
		return delayTime;
	}

	public void setDelayTime(double delayTime) {
		this.delayTime = delayTime;
	}

	@Override
	public String toString() {
		return String.format("exposure: %f, number of frames: %d, delay: %s, delay time: %f",
				exposure, numberOfFrames, delay ? "enabled" : "disabled", delayTime);
	}
}
