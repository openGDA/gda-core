package uk.ac.diamond.daq.experiment.plan;

import java.util.function.DoubleSupplier;

/**
 * Gives current time in seconds
 */
public class SystemTimerSignal implements DoubleSupplier {
	
	@Override
	public double getAsDouble() {
		return System.currentTimeMillis() / 1000.0;
	}

}
