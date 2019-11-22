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

import static java.util.stream.Collectors.toMap;

import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import org.eclipse.scanning.api.malcolm.MalcolmTable;
import org.epics.pvdata.pv.Field;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.ScalarArray;
import org.epics.pvdata.pv.ScalarType;
import org.epics.pvmarshaller.marshaller.api.IPVStructureDeserialiser;
import org.epics.pvmarshaller.marshaller.deserialisers.Deserialiser;

/**
 * Custom deserializer to a {@link MalcolmTable} from a PV structure.
 */
public class MalcolmTableDeserialiser implements IPVStructureDeserialiser {

	private static final Map<ScalarType, Class<?>> SCALAR_TYPE_TO_CLASS_MAP;

	static {
		final Map<ScalarType, Class<?>> scalarTypeToClassMap = new EnumMap<>(ScalarType.class);
		scalarTypeToClassMap.put(ScalarType.pvInt, Integer.class);
		scalarTypeToClassMap.put(ScalarType.pvShort, Short.class);
		scalarTypeToClassMap.put(ScalarType.pvLong, Long.class);
		scalarTypeToClassMap.put(ScalarType.pvByte, Byte.class);
		scalarTypeToClassMap.put(ScalarType.pvBoolean, Boolean.class);
		scalarTypeToClassMap.put(ScalarType.pvFloat, Float.class);
		scalarTypeToClassMap.put(ScalarType.pvDouble, Double.class);
		scalarTypeToClassMap.put(ScalarType.pvString, String.class);
		SCALAR_TYPE_TO_CLASS_MAP = Collections.unmodifiableMap(scalarTypeToClassMap);
	}

	@Override
	public Object fromPVStructure(Deserialiser deserialiser, PVStructure pvStructure) throws Exception {
		@SuppressWarnings("unchecked")
		final LinkedHashMap<String, List<?>> valueMap = (LinkedHashMap<String, List<?>>) deserialiser.getMapDeserialiser()
					.createMapFromPVStructure(pvStructure, LinkedHashMap.class, Object.class);
		final LinkedHashMap<String, Class<?>> dataTypeMap = createDataTypeMap(pvStructure, valueMap.keySet());

		return new MalcolmTable(valueMap, dataTypeMap);
	}

	private LinkedHashMap<String, Class<?>> createDataTypeMap(PVStructure pvStructure, Set<String> columnNames) {
		final Function<String, Class<?>> columnNameToClass = columnName -> {
			final Field field = pvStructure.getSubField(columnName).getField();
			// all fields in a table should be a subclass of ScalarArray
			final ScalarType type = ((ScalarArray) field).getElementType();
			return SCALAR_TYPE_TO_CLASS_MAP.get(type);
		};

		return columnNames.stream().collect(toMap(Function.identity(), columnNameToClass, (x, y) -> y, LinkedHashMap::new));
	}

}
