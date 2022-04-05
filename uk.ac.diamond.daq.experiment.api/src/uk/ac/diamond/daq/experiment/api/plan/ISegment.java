package uk.ac.diamond.daq.experiment.api.plan;

import java.util.List;

import gda.factory.Findable;

/**
 * A DIAD experiment consists of one or more ISegmentLimiters chained together, one active at a time.
 * When active, each one enables and/or disables {@link ITrigger}s
 *
 */
public interface ISegment extends SEVListener, Findable {

	/**
	 * Enables the given trigger when the segment is activated
	 * @param trigger
	 */
	void enable(ITrigger trigger);

	/**
	 * @return list of triggers enabled within this segment
	 */
	List<ITrigger> getTriggers();

	/**
	 * Called by the Plan
	 */
	void activate();

	/**
	 * Abort and deactivate all its triggers
	 */
	void abort();

	boolean isActivated();

}
