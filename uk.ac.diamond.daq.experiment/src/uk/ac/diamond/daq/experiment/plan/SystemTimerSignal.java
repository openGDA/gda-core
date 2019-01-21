package uk.ac.diamond.daq.experiment.plan;

import uk.ac.diamond.daq.experiment.api.plan.SEVSignal;

/**
 * SEVSignal which gives current time in seconds
 */
public class SystemTimerSignal implements SEVSignal {
	
	@Override
	public double read() {
		return System.currentTimeMillis() / 1000.0;
	}

}
