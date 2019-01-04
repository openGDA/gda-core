package uk.ac.diamond.daq.experiment.api.plan;

import gda.factory.Findable;

/**
 * Listens to a {@link SampleEnvironmentVariable} when enabled. Triggers an operation based on the SEV signal.
 * Should be enabled/disabled by {@link ISegment}s.
 *
 */
public interface ITrigger extends Findable {
	
	void setEnabled(boolean enabled);
	boolean isEnabled();
	
}
