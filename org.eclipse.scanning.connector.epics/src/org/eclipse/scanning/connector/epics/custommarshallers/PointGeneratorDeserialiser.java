/*-
 *******************************************************************************
 * Copyright (c) 2011, 2016 Diamond Light Source Ltd.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthew Gerring - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.scanning.connector.epics.custommarshallers;

import static org.eclipse.scanning.connector.epics.EpicsConnectionConstants.FIELD_NAME_DESCRIPTION;
import static org.eclipse.scanning.connector.epics.EpicsConnectionConstants.FIELD_NAME_LABEL;
import static org.eclipse.scanning.connector.epics.EpicsConnectionConstants.FIELD_NAME_META;
import static org.eclipse.scanning.connector.epics.EpicsConnectionConstants.FIELD_NAME_TAGS;
import static org.eclipse.scanning.connector.epics.EpicsConnectionConstants.FIELD_NAME_VALUE;
import static org.eclipse.scanning.connector.epics.EpicsConnectionConstants.FIELD_NAME_WRITEABLE;

import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.scanning.api.malcolm.attributes.PointGeneratorAttribute;
import org.epics.pvdata.pv.PVStringArray;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.StringArrayData;
import org.epics.pvmarshaller.marshaller.api.IPVStructureDeserialiser;
import org.epics.pvmarshaller.marshaller.deserialisers.Deserialiser;

/**
 * Custom deserialiser for PointGenerator
 * @author Matt Taylor
 *
 */
public class PointGeneratorDeserialiser implements IPVStructureDeserialiser {

	@Override
	public Object fromPVStructure(Deserialiser deserialiser, PVStructure pvStructure) throws Exception {
		PVStructure metaStructure = pvStructure.getStructureField(FIELD_NAME_META);
		String description = metaStructure.getStringField(FIELD_NAME_DESCRIPTION).get();
		boolean writeable = metaStructure.getBooleanField(FIELD_NAME_WRITEABLE).get();
		String label = metaStructure.getStringField(FIELD_NAME_LABEL).get();
		PVStringArray tagsArray = metaStructure.getSubField(PVStringArray.class, FIELD_NAME_TAGS);
		StringArrayData tagsArrayData = new StringArrayData();
		tagsArray.get(0, tagsArray.getLength(), tagsArrayData);

		PointGeneratorAttribute attribute = new PointGeneratorAttribute();

		attribute.setDescription(description);
		attribute.setLabel(label);
		attribute.setTags(tagsArrayData.data);
		attribute.setWriteable(writeable);
		attribute.setName(pvStructure.getFullName());

		PVStructure valuePVStructure = pvStructure.getStructureField(FIELD_NAME_VALUE);

		Map<?, ?> valueMap = deserialiser.getMapDeserialiser().createMapFromPVStructure(valuePVStructure, LinkedHashMap.class, Object.class);

		attribute.setValue(valueMap);

		return attribute;

	}
}
