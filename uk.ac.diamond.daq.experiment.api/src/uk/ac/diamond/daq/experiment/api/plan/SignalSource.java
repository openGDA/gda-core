package uk.ac.diamond.daq.experiment.api.plan;

/**
 * Provides a signal which can be polled by a {@link SampleEnvironmentVariable}.
 */
@FunctionalInterface
public interface SignalSource {
	
	double read();

}
