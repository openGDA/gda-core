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

import static org.eclipse.scanning.connector.epics.EpicsConnectionConstants.FIELD_NAME_CHOICES;
import static org.eclipse.scanning.connector.epics.EpicsConnectionConstants.FIELD_NAME_DESCRIPTION;
import static org.eclipse.scanning.connector.epics.EpicsConnectionConstants.FIELD_NAME_DTYPE;
import static org.eclipse.scanning.connector.epics.EpicsConnectionConstants.FIELD_NAME_LABEL;
import static org.eclipse.scanning.connector.epics.EpicsConnectionConstants.FIELD_NAME_META;
import static org.eclipse.scanning.connector.epics.EpicsConnectionConstants.FIELD_NAME_TAGS;
import static org.eclipse.scanning.connector.epics.EpicsConnectionConstants.FIELD_NAME_VALUE;
import static org.eclipse.scanning.connector.epics.EpicsConnectionConstants.FIELD_NAME_WRITEABLE;
import static org.eclipse.scanning.connector.epics.EpicsConnectionConstants.TYPE_ID_BOOLEAN_META;
import static org.eclipse.scanning.connector.epics.EpicsConnectionConstants.TYPE_ID_CHOICE_META;
import static org.eclipse.scanning.connector.epics.EpicsConnectionConstants.TYPE_ID_NUMBER_META;
import static org.eclipse.scanning.connector.epics.EpicsConnectionConstants.TYPE_ID_STRING_META;

import org.eclipse.scanning.api.malcolm.attributes.BooleanAttribute;
import org.eclipse.scanning.api.malcolm.attributes.ChoiceAttribute;
import org.eclipse.scanning.api.malcolm.attributes.NumberAttribute;
import org.eclipse.scanning.api.malcolm.attributes.StringAttribute;
import org.epics.pvdata.pv.PVField;
import org.epics.pvdata.pv.PVStringArray;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.StringArrayData;
import org.epics.pvmarshaller.marshaller.api.IPVStructureDeserialiser;
import org.epics.pvmarshaller.marshaller.deserialisers.Deserialiser;

/**
 * Custom deserialiser for NTScalar
 * @author Matt Taylor
 *
 */
public class NTScalarDeserialiser implements IPVStructureDeserialiser {

	@Override
	public Object fromPVStructure(Deserialiser deserialiser, PVStructure pvStructure) throws Exception {

		PVStructure metaStructure = pvStructure.getStructureField(FIELD_NAME_META);
		String metaId = metaStructure.getStructure().getID();
		String description = metaStructure.getStringField(FIELD_NAME_DESCRIPTION).get();
		boolean writeable = metaStructure.getBooleanField(FIELD_NAME_WRITEABLE).get();
		String label = metaStructure.getStringField(FIELD_NAME_LABEL).get();
		PVStringArray tagsArray = metaStructure.getSubField(PVStringArray.class, FIELD_NAME_TAGS);
		StringArrayData tagsArrayData = new StringArrayData();
		tagsArray.get(0, tagsArray.getLength(), tagsArrayData);

		if (metaId.startsWith(TYPE_ID_CHOICE_META)) {
			ChoiceAttribute attribute = new ChoiceAttribute();

			attribute.setDescription(description);
			attribute.setLabel(label);
			attribute.setTags(tagsArrayData.data);
			attribute.setWriteable(writeable);
			attribute.setName(pvStructure.getFullName());

			PVStringArray choicesArray = metaStructure.getSubField(PVStringArray.class, FIELD_NAME_CHOICES);
			StringArrayData choicesArrayData = new StringArrayData();
			choicesArray.get(0, choicesArray.getLength(), choicesArrayData);
			attribute.setChoices(choicesArrayData.data);

			String value = pvStructure.getStringField(FIELD_NAME_VALUE).get();
			attribute.setValue(value);
			return attribute;
		} else if (metaId.startsWith(TYPE_ID_STRING_META)) {
			StringAttribute attribute = new StringAttribute();

			attribute.setDescription(description);
			attribute.setLabel(label);
			attribute.setTags(tagsArrayData.data);
			attribute.setWriteable(writeable);
			attribute.setName(pvStructure.getFullName());

			String value = pvStructure.getStringField(FIELD_NAME_VALUE).get();
			attribute.setValue(value);
			return attribute;
		} else if (metaId.startsWith(TYPE_ID_BOOLEAN_META)) {
			BooleanAttribute attribute = new BooleanAttribute();

			attribute.setDescription(description);
			attribute.setLabel(label);
			attribute.setTags(tagsArrayData.data);
			attribute.setWriteable(writeable);
			attribute.setName(pvStructure.getFullName());

			boolean value = pvStructure.getBooleanField(FIELD_NAME_VALUE).get();
			attribute.setValue(value);
			return attribute;
		} else if (metaId.startsWith(TYPE_ID_NUMBER_META)) {
			NumberAttribute attribute = new NumberAttribute();

			attribute.setDescription(description);
			attribute.setLabel(label);
			attribute.setTags(tagsArrayData.data);
			attribute.setWriteable(writeable);
			attribute.setName(pvStructure.getFullName());

			String numberType = metaStructure.getStringField(FIELD_NAME_DTYPE).get();
			attribute.setDtype(numberType);

			PVField valuePVField = pvStructure.getSubField(FIELD_NAME_VALUE);

			// Use scalar deserialiser to get value. Class passed in can be null as it's only used
			// to determine between String and char, and we 'know' it's a number here
			Object value = deserialiser.getScalarDeserialiser().deserialise(valuePVField, null);

			if (value instanceof Number) {
				Number number = (Number)value;
				attribute.setValue(number);
			} else {
				throw new Exception(pvStructure.getFullName() + " has a number field that isn't a number");
			}

			return attribute;
		}

		throw new Exception("Unrecognised NTScalar type: " + metaId);
	}
}
