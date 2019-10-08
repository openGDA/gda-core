package uk.ac.diamond.daq.experiment.scans;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.dawnsci.analysis.api.persistence.IClassRegistry;

import uk.ac.diamond.daq.experiment.scans.mapping.TriggerableMap;
import uk.ac.diamond.daq.experiment.scans.tomography.TriggerableTomography;

/**
 * Enables marshalling of the triggerable scans defined in this plugin
 */
public class TriggerableScansClassRegistry implements IClassRegistry {

	private static final Map<String, Class<?>> REGISTRY;

	static {
		Map<String, Class<?>> registry = new HashMap<>();
		register(registry, TriggerableMap.class);
		register(registry, TriggerableTomography.class);

		REGISTRY = Collections.unmodifiableMap(registry);
	}

	private static void register(Map<String, Class<?>> registry, Class<?> triggerableScanClass) {
		registry.put(triggerableScanClass.getCanonicalName(), triggerableScanClass);
	}

	@Override
	public Map<String, Class<?>> getIdToClassMap() {
		return REGISTRY;
	}

}
