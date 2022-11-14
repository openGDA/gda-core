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
import uk.ac.diamond.daq.experiment.api.plan.LimitCondition;

public class SimpleSegment extends SegmentBase {

	private LimitCondition limitCondition;

	SimpleSegment(IPlanRegistrar registrar, ISampleEnvironmentVariable sev, LimitCondition limitCondition) {
		super(registrar, sev);
		this.limitCondition = limitCondition;
	}

	@Override
	boolean shouldTerminate(double signal) {
		return limitCondition.limitReached(signal);
	}

	@Override
	public String toString() {
		return "SimpleSegment [name=" + getName() + ", triggers=" + getTriggers() +
				", sev=" + sev + ", limitCondition=" + limitCondition + "]";
	}

}
