package uk.ac.diamond.daq.experiment.plan;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.daq.experiment.api.plan.Triggerable;
import uk.ac.diamond.daq.experiment.scans.mapping.TriggerableMap;

/**
 * Where standard {@link Triggerable}s are mapped to standard {@link TriggerableProcessor}s
 */
public class TriggerProcessorFactory {

	private static final Map<Class<? extends Triggerable>, Class<? extends TriggerProcessor>> processors;
	
	private static final Logger logger = LoggerFactory.getLogger(TriggerProcessorFactory.class);
	
	private TriggerProcessorFactory() {
		throw new IllegalAccessError("For static access only");
	}
	
	static {
		Map<Class<? extends Triggerable>, Class<? extends TriggerProcessor>> ret = new HashMap<>();
		ret.put(TriggerableMap.class, MappingTriggerProcessor.class);
		
		processors = Collections.unmodifiableMap(ret);
	}
	
	/**
	 * Return the processor associated with the given triggerable class.
	 * If {@code null}, the triggerable is essentially a black box which executes its own logic.
	 */
	public static <T extends Triggerable> TriggerProcessor getProcessor(Class<T> triggerableClass) {
		Class<? extends TriggerProcessor> processorClass = processors.get(triggerableClass);
		try {
			return processorClass == null ? null : processorClass.newInstance();
		} catch (IllegalAccessException | InstantiationException e) {
			logger.error("Could not instantiate processor for triggerable of class '{}'", triggerableClass.getName(), e);
			return null;
		}
	}
}
