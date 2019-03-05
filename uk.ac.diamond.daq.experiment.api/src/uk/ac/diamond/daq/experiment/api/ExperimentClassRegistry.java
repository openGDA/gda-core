package uk.ac.diamond.daq.experiment.api;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.dawnsci.analysis.api.persistence.IClassRegistry;

import uk.ac.diamond.daq.experiment.api.plan.event.PlanStatusBean;
import uk.ac.diamond.daq.experiment.api.plan.event.SegmentRecord;
import uk.ac.diamond.daq.experiment.api.plan.event.TriggerEvent;
import uk.ac.diamond.daq.experiment.api.plan.event.TriggerRecord;

public class ExperimentClassRegistry implements IClassRegistry {

	private static final Map<String, Class<?>> idToClassMap;

	static {
		Map<String, Class<?>> tmp = new HashMap<>();

		tmp.put(PlanStatusBean.class.getCanonicalName(), PlanStatusBean.class);
		tmp.put(SegmentRecord.class.getCanonicalName(), SegmentRecord.class);
		tmp.put(TriggerRecord.class.getCanonicalName(), TriggerRecord.class);
		tmp.put(TriggerEvent.class.getCanonicalName(), TriggerEvent.class);

		idToClassMap = Collections.unmodifiableMap(tmp);
	}

	@Override
	public Map<String, Class<?>> getIdToClassMap() {
		return idToClassMap;
	}
}
