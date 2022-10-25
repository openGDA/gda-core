/*-
 * Copyright © 2020 Diamond Light Source Ltd.
 *
 * This file is part of GDA.
 *
 * GDA is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License version 3 as published by the Free
 * Software Foundation.
 *
 * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along
 * with GDA. If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.diamond.daq.experiment.api;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.dawnsci.analysis.api.persistence.IClassRegistry;

import uk.ac.diamond.daq.experiment.api.plan.event.PlanStatusBean;
import uk.ac.diamond.daq.experiment.api.plan.event.SegmentRecord;
import uk.ac.diamond.daq.experiment.api.plan.event.TriggerEvent;
import uk.ac.diamond.daq.experiment.api.plan.event.TriggerRecord;
import uk.ac.diamond.daq.experiment.api.structure.ExperimentEvent;
import uk.ac.diamond.daq.experiment.api.structure.NodeInsertionRequest;

public class ExperimentClassRegistry implements IClassRegistry {

	private static final Map<String, Class<?>> idToClassMap;

	static {
		Map<String, Class<?>> tmp = new HashMap<>();

		tmp.put(ExperimentEvent.class.getCanonicalName(), ExperimentEvent.class);

		tmp.put(PlanStatusBean.class.getCanonicalName(), PlanStatusBean.class);
		tmp.put(SegmentRecord.class.getCanonicalName(), SegmentRecord.class);
		tmp.put(TriggerRecord.class.getCanonicalName(), TriggerRecord.class);
		tmp.put(TriggerEvent.class.getCanonicalName(), TriggerEvent.class);

		tmp.put(NodeInsertionRequest.class.getCanonicalName(), NodeInsertionRequest.class);

		idToClassMap = Collections.unmodifiableMap(tmp);
	}

	@Override
	public Map<String, Class<?>> getIdToClassMap() {
		return idToClassMap;
	}
}
