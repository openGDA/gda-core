package uk.ac.gda.core.classregistry;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.dawnsci.analysis.api.persistence.IClassRegistry;

import gda.commandqueue.ExperimentCommandBean;
import uk.ac.gda.client.experimentdefinition.ui.handlers.ExperimentCommand;

/**
 *
 * A registry of classes that can be marshalled.
 *
 * @author Martin Gaughran
 * @author Matthew Gerring
 *
 */
public class GDACoreClassRegistry implements IClassRegistry {

	private static final Map<String, Class<?>> idToClassMap;
	static {
		Map<String, Class<?>> tmp = new HashMap<String, Class<?>>();

		registerClass(tmp, ExperimentCommandBean.class);
		registerClass(tmp, ExperimentCommand.class);

		idToClassMap = Collections.unmodifiableMap(tmp);
	}

	private static void registerClass(Map<String, Class<?>> map, Class<?> clazz) {
		map.put(clazz.getSimpleName(), clazz);
	}

	@Override
	public Map<String, Class<?>> getIdToClassMap() {
		return idToClassMap;
	}
}
