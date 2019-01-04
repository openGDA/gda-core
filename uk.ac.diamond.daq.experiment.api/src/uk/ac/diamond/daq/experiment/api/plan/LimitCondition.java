package uk.ac.diamond.daq.experiment.api.plan;

/**
 * Functional interface passed to SEV-based {@link ISegment}s to specify the limit.
 */
@FunctionalInterface
public interface LimitCondition {
	
	/**
	 * @param signal the signal to evaluate
	 * @return {@code true} if the signal will terminate the currently active {@link ISegment}; otherwise {@code false}
	 */
	boolean limitReached(double signal);

}
