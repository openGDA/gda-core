package uk.ac.diamond.daq.experiment.plan;

import uk.ac.diamond.daq.experiment.api.plan.SignalSource;

/**
 * SEVSignal which gives current time in seconds
 */
public class SystemTimerSignal implements SignalSource {
	
	@Override
	public double read() {
		return System.currentTimeMillis() / 1000.0;
	}

}
