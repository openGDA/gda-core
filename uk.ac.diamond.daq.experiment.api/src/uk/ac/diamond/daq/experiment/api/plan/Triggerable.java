package uk.ac.diamond.daq.experiment.api.plan;

public interface Triggerable {

	/**
	 * If this Triggerable is a Solstice scan, then this method should return
	 * its IdBean so that we can later find it in the queue and monitor its progress.
	 * <p>
	 * Any other return type will be ignored.
	 */
	Object trigger();

}
