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
import java.math.RoundingMode;

import uk.ac.diamond.daq.experiment.api.plan.IPlanRegistrar;
import uk.ac.diamond.daq.experiment.api.plan.ISampleEnvironmentVariable;
import uk.ac.diamond.daq.experiment.api.plan.Payload;

public class RepeatingTrigger extends TriggerBase {

	private double interval;
	private double lastTriggeringSignal;
	private double offset;

	public RepeatingTrigger(IPlanRegistrar registrar, ISampleEnvironmentVariable sev, Payload payload, double interval) {
		this(registrar, sev, payload, interval, 0.0);
	}

	public RepeatingTrigger(IPlanRegistrar registrar, ISampleEnvironmentVariable sev, Payload payload, double interval, double offset) {
		super(registrar, payload, sev);
		this.interval = interval;
		this.offset = offset;
	}

	@Override
	protected boolean evaluateTriggerCondition(double signal) {
		if (BigDecimal.valueOf(signal).subtract(BigDecimal.valueOf(lastTriggeringSignal)).abs().divide(BigDecimal.valueOf(interval), 5, RoundingMode.HALF_UP).compareTo(BigDecimal.ONE) < 0) {
			return false;
		}
		lastTriggeringSignal = signal;
		return true;
	}

	@Override
	protected void enable() {
		lastTriggeringSignal = getSEV().read() + offset;
		super.enable();
	}

	@Override
	public String toString() {
		return "RepeatingTrigger [payload="+getPayload()+", SEV="+getSEV()+", interval="+interval+"]";
	}

}
