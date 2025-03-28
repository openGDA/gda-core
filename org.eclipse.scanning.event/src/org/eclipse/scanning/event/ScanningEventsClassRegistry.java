/*-
 * Copyright © 2018 Diamond Light Source Ltd.
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

package org.eclipse.scanning.event;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.dawnsci.analysis.api.persistence.IClassRegistry;
import org.eclipse.scanning.api.device.models.MalcolmDetectorModel;
import org.eclipse.scanning.api.device.models.MalcolmModel;
import org.eclipse.scanning.api.event.queue.QueueCommandBean;
import org.eclipse.scanning.api.event.queue.QueueStatusBean;
import org.eclipse.scanning.api.event.status.StatusBean;
import org.eclipse.scanning.api.event.status.WatchdogStatusRecord;
import org.eclipse.scanning.api.malcolm.MalcolmTable;
import org.eclipse.scanning.api.malcolm.attributes.BooleanArrayAttribute;
import org.eclipse.scanning.api.malcolm.attributes.BooleanAttribute;
import org.eclipse.scanning.api.malcolm.attributes.ChoiceAttribute;
import org.eclipse.scanning.api.malcolm.attributes.MalcolmAttribute;
import org.eclipse.scanning.api.malcolm.attributes.NumberArrayAttribute;
import org.eclipse.scanning.api.malcolm.attributes.NumberAttribute;
import org.eclipse.scanning.api.malcolm.attributes.PointGeneratorAttribute;
import org.eclipse.scanning.api.malcolm.attributes.StringArrayAttribute;
import org.eclipse.scanning.api.malcolm.attributes.StringAttribute;
import org.eclipse.scanning.api.malcolm.attributes.TableAttribute;
import org.eclipse.scanning.api.malcolm.event.MalcolmEvent;

import uk.ac.gda.core.sampletransfer.SequenceCommand;
import uk.ac.gda.core.sampletransfer.SequenceRequest;
import uk.ac.gda.core.sampletransfer.Step;
import uk.ac.gda.core.sampletransfer.StepStatus;

public class ScanningEventsClassRegistry implements IClassRegistry {

	private static final Map<String, Class<?>> idToClassMap;

	static {
		Map<String, Class<?>> tmp = new HashMap<>();

		// event.queue
		registerClass(tmp, QueueStatusBean.class);
		registerClass(tmp, QueueCommandBean.class);

		// event.status
		registerClass(tmp, WatchdogStatusRecord.class);
		registerClass(tmp, StatusBean.class);

		// event.sampletransfer
		registerClass(tmp, Step.class);
		registerClass(tmp, StepStatus.class);
		registerClass(tmp, SequenceCommand.class);
		registerClass(tmp, SequenceRequest.class);

		// malcolm.event
		registerClass(tmp, MalcolmModel.class);
		registerClass(tmp, MalcolmDetectorModel.class);
		registerClass(tmp, Float.class);
		registerClass(tmp, MalcolmEvent.class);
		registerClass(tmp, MalcolmTable.class);
		registerClass(tmp, ChoiceAttribute.class);
		registerClass(tmp, BooleanArrayAttribute.class);
		registerClass(tmp, BooleanAttribute.class);
		registerClass(tmp, MalcolmAttribute.class);
		registerClass(tmp, NumberArrayAttribute.class);
		registerClass(tmp, NumberAttribute.class);
		registerClass(tmp, PointGeneratorAttribute.class);
		registerClass(tmp, StringArrayAttribute.class);
		registerClass(tmp, StringAttribute.class);
		registerClass(tmp, TableAttribute.class);



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
