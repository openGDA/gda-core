package uk.ac.diamond.daq.experiment.ui.plan;

/**
 * Implement to listen to the validation of a {@link ValidatablePart}
 */
public interface ValidationListener {
	
	/**
	 * Called by {@link ValidatablePart} when its validation state may have changed.
	 */
	void handle(boolean valid);

}
