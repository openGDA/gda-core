package uk.ac.diamond.daq.experiment.plan;

import uk.ac.diamond.daq.experiment.api.plan.Triggerable;

/**
 * A {@link Triggerable} can be a black box. For standard behaviour however, such as Solstice scans,
 * it is preferable to separate data from logic. In such cases the logic can implemented as a TriggerProcessor
 * and mapped to the appropriate Triggerable in the {@link TriggerProcessorFactory}.
 * <p>
 * Implementations must have a default argument constructor as they are instantiated by reflection. 
 */
public interface TriggerProcessor {
	
	/**
	 * Process the payload of the given triggerable
	 * <p>
	 * Can return anything to the caller e.g. for updates on a long-running procedure 
	 */
	Object process(Triggerable triggerable);

}
