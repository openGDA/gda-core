package uk.ac.diamond.daq.experiment.api.plan;

import gda.factory.Findable;

/**
 * A DIAD experiment consists of one or more ISegmentLimiters chained together, one active at a time.
 * When active, each one enables and/or disables {@link ITrigger}s 
 *
 */
public interface ISegment extends Findable {
	
	/**
	 * Enables the given trigger when the segment is activated
	 * @param trigger
	 */
	void enable(ITrigger trigger);
	
	/**
	 * Called by the Plan
	 */
	void activate();
	boolean isActivated();
	
}
