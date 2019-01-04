package uk.ac.diamond.daq.experiment.api.plan;

/**
 * Classes must implement this interface to observe {@link SampleEnvironmentVariable}s
 * @see {@link ITrigger}
 * @see {@link ISegment}
 *
 */
public interface SEVListener {
	
	void signalChanged(double signal);

}
