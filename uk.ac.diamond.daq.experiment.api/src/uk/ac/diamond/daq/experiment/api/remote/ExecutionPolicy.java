package uk.ac.diamond.daq.experiment.api.remote;

import uk.ac.diamond.daq.experiment.api.plan.ISegment;
import uk.ac.diamond.daq.experiment.api.plan.ITrigger;

/**
 * This type specifies whether an {@link ITrigger} should run only once or periodically
 */
public enum ExecutionPolicy {


	/**
	 * Executes only once per {@link ISegment}
	 */
	SINGLE,


	/**
	 * Executes periodically
	 */
	REPEATING;

}
