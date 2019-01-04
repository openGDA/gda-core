package uk.ac.diamond.daq.experiment.api.plan;

/**
 * This functional interface provides a signal which can be sampled by a {@link SampleEnvironmentVariable}.
 * e.g. {@code SEVSignal voltageSignal = () -> caget(VOLTAGE_PV);} 
 *
 */
@FunctionalInterface
public interface SEVSignal {
	
	double read();

}
