package uk.ac.diamond.daq.experiment.ui.plan.preview;

import java.util.Optional;

/**
 * Analyses a subsection of 2 1D datasets bound in x to calculate
 * how a particular trigger's rules would respond to the signals
 *
 * @param <T> the coordinate/s type
 */
public interface TriggerLocator<T> {
	
	
	/**
	 * Find the trigger point/s from the datasets bound by xStart and xEnd
	 */
	void search(double xStart, double xEnd);
	
	
	/**
	 * Get x coordinate/s, if found, after {@link #search(double, double)} has returned;
	 */
	Optional<T> getX();
	
	
	/**
	 * Get y coordinate/s, if found, after {@link #search(double, double)} has returned;
	 */
	Optional<T> getY();
}
