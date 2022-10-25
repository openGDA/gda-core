/*-
 * Copyright © 2020 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.experiment.plan;

import uk.ac.diamond.daq.experiment.api.plan.IPlanRegistrar;
import uk.ac.diamond.daq.experiment.api.plan.ISampleEnvironmentVariable;

public class FixedDurationSegment extends SegmentBase {

	private double duration; // in seconds
	private double startTime;

	/**
	 * duration in seconds
	 */
	FixedDurationSegment(IPlanRegistrar registrar, ISampleEnvironmentVariable timer, double duration) {
		super(registrar, timer);
		this.duration = duration;
	}

	@Override
	public void activate() {
		this.startTime = sev.read();
		super.activate();
	}

	@Override
	boolean shouldTerminate(double signal) {
		return signal - startTime >= duration;
	}

	@Override
	public String toString() {
		return "FixedDurationSegment [name=" + getName() + "triggers=" + getTriggers()
			+ "duration=" + duration + " seconds]";
	}

}
