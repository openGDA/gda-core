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

package uk.ac.diamond.daq.experiment.plan.trigger;

import java.math.BigDecimal;

import uk.ac.diamond.daq.experiment.api.plan.IPlanRegistrar;
import uk.ac.diamond.daq.experiment.api.plan.ISampleEnvironmentVariable;
import uk.ac.diamond.daq.experiment.api.plan.Payload;

/**
 * Provides simple absolute to relative conversion enabling {@link SingleTrigger} to operate
 * with a time {@link ISampleEnvironmentVariable}.
 * <p>
 * Example: "trigger this payload 5 seconds after the start of this segment"
 */
public class SingleTimeBasedTrigger extends SingleTrigger {

	private double startTime;

	public SingleTimeBasedTrigger(IPlanRegistrar registrar, ISampleEnvironmentVariable sev, Payload payload,
			double target, double tolerance) {
		super(registrar, sev, payload, target, tolerance);
	}

	@Override
	protected void enable() {
		startTime = getSEV().read();
		super.enable();
	}

	@Override
	protected boolean evaluateTriggerCondition(double signal) {
		return super.evaluateTriggerCondition(BigDecimal.valueOf(signal).subtract(BigDecimal.valueOf(startTime)).doubleValue());
	}

}
