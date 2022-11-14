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
import uk.ac.diamond.daq.experiment.api.plan.ITrigger;
import uk.ac.diamond.daq.experiment.api.plan.Payload;

/**
 * Will trigger only once per segment: when SEV signal is at triggerSignal ± tolerance.
 */
public class SingleTrigger extends TriggerBase {

	private boolean hasTriggered;
	private BigDecimal lowerLimit;
	private BigDecimal upperLimit;

	/**
	 * Create an {@link ITrigger} that will trigger a {@link Payload} only once,
	 * when signal from {@code sev} = {@code triggerSignal} ± {@code tolerance}
	 * @param registrar to report to
	 * @param sev providing the triggering signal source
	 * @param payload to trigger
	 * @param target signal from sev which should trigger us
	 * @param tolerance
	 */
	public SingleTrigger(IPlanRegistrar registrar, ISampleEnvironmentVariable sev, Payload payload, double target, double tolerance) {
		super(registrar, payload, sev);
		BigDecimal position = BigDecimal.valueOf(target);
		BigDecimal positiveTolerance = BigDecimal.valueOf(tolerance).abs();
		lowerLimit = position.subtract(positiveTolerance);
		upperLimit = position.add(positiveTolerance);
	}

	@Override
	protected void enable() {
		hasTriggered = false;
		super.enable();
	}

	@Override
	protected boolean evaluateTriggerCondition(double signal) {
		if (hasTriggered) return false;
		BigDecimal preciseSignal = BigDecimal.valueOf(signal);
		hasTriggered = preciseSignal.compareTo(lowerLimit) >=0 && preciseSignal.compareTo(upperLimit) <=0;
		return hasTriggered;
	}

	@Override
	public String toString() {
		return "SingleTrigger [SEV="+getSEV()+", payload="+getPayload()
			+", triggering when "+lowerLimit.doubleValue()+" <= signal <= "+upperLimit.doubleValue()+"]";
	}

}
