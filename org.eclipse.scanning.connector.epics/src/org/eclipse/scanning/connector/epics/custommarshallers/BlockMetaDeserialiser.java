/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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

package org.eclipse.scanning.connector.epics.custommarshallers;

import static org.eclipse.scanning.connector.epics.EpicsConnectionConstants.FIELD_NAME_DESCRIPTION;
import static org.eclipse.scanning.connector.epics.EpicsConnectionConstants.FIELD_NAME_FIELDS;
import static org.eclipse.scanning.connector.epics.EpicsConnectionConstants.FIELD_NAME_LABEL;
import static org.eclipse.scanning.connector.epics.EpicsConnectionConstants.FIELD_NAME_TAGS;
import static org.eclipse.scanning.connector.epics.EpicsConnectionConstants.FIELD_NAME_WRITEABLE;

import org.eclipse.scanning.api.malcolm.attributes.StringArrayAttribute;
import org.epics.pvdata.pv.PVStringArray;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.StringArrayData;
import org.epics.pvmarshaller.marshaller.api.IPVStructureDeserialiser;
import org.epics.pvmarshaller.marshaller.deserialisers.Deserialiser;

public class BlockMetaDeserialiser implements IPVStructureDeserialiser {

	@Override
	public Object fromPVStructure(Deserialiser deserialiser, PVStructure pvStructure) throws Exception {
		final String description = pvStructure.getStringField(FIELD_NAME_DESCRIPTION).get();
		final boolean writeable = pvStructure.getBooleanField(FIELD_NAME_WRITEABLE).get();
		final String label = pvStructure.getStringField(FIELD_NAME_LABEL).get();
		final String[] tags = getStringArrayField(pvStructure, FIELD_NAME_TAGS);
		// Note: the only BlockMeta structure we use is for the 'meta' attribute of a MalcolmDevice,
		// which has a field called 'fields' listing the other fields (attributes, methods) of the malcolm device
		final String[] fields = getStringArrayField(pvStructure, FIELD_NAME_FIELDS);

		final StringArrayAttribute result = new StringArrayAttribute(fields);
		result.setDescription(description);
		result.setLabel(label);
		result.setTags(tags);
		result.setWriteable(writeable);
		result.setName(pvStructure.getFullName());
		return result;
	}

	private String[] getStringArrayField(PVStructure pvStructure, String fieldName) {
		final PVStringArray pvStringArray = pvStructure.getSubField(PVStringArray.class, fieldName);
		if (pvStringArray != null) {
			final StringArrayData arrayData = new StringArrayData();
			pvStringArray.get(0, pvStringArray.getLength(), arrayData);
			return arrayData.data;
		}
		return null;
	}

}
