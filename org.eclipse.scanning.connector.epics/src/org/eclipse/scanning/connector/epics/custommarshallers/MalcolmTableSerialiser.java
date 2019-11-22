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

import static org.eclipse.scanning.connector.epics.EpicsConnectionConstants.TYPE_ID_TABLE;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.scanning.api.malcolm.MalcolmTable;
import org.epics.pvdata.factory.FieldFactory;
import org.epics.pvdata.pv.FieldBuilder;
import org.epics.pvdata.pv.FieldCreate;
import org.epics.pvdata.pv.PVBooleanArray;
import org.epics.pvdata.pv.PVByteArray;
import org.epics.pvdata.pv.PVDoubleArray;
import org.epics.pvdata.pv.PVFloatArray;
import org.epics.pvdata.pv.PVIntArray;
import org.epics.pvdata.pv.PVLongArray;
import org.epics.pvdata.pv.PVScalarArray;
import org.epics.pvdata.pv.PVShortArray;
import org.epics.pvdata.pv.PVStringArray;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.ScalarType;
import org.epics.pvdata.pv.Structure;
import org.epics.pvmarshaller.marshaller.api.IPVStructureSerialiser;
import org.epics.pvmarshaller.marshaller.serialisers.Serialiser;

import com.google.common.primitives.Booleans;
import com.google.common.primitives.Bytes;
import com.google.common.primitives.Doubles;
import com.google.common.primitives.Floats;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import com.google.common.primitives.Shorts;

/**
 * Custom serialiser for a {@link MalcolmTable} to a PV structure.
 */
public class MalcolmTableSerialiser implements IPVStructureSerialiser<MalcolmTable> {

	private static final Map<Class<?>, ScalarType> CLASS_TO_SCALAR_TYPE_MAP;

	private static final Map<Class<?>, Class<? extends PVScalarArray>> CLASS_TO_PV_ARRAY_CLASS_MAP;

	static {
		final Map<Class<?>, ScalarType> wrapperClassToScalarTypeMap = new HashMap<>();
		wrapperClassToScalarTypeMap.put(Integer.class, ScalarType.pvInt);
		wrapperClassToScalarTypeMap.put(Short.class, ScalarType.pvShort);
		wrapperClassToScalarTypeMap.put(Long.class, ScalarType.pvLong);
		wrapperClassToScalarTypeMap.put(Byte.class, ScalarType.pvByte);
		wrapperClassToScalarTypeMap.put(Boolean.class, ScalarType.pvBoolean);
		wrapperClassToScalarTypeMap.put(Float.class, ScalarType.pvFloat);
		wrapperClassToScalarTypeMap.put(Double.class, ScalarType.pvDouble);
		wrapperClassToScalarTypeMap.put(String.class, ScalarType.pvString);
		CLASS_TO_SCALAR_TYPE_MAP = wrapperClassToScalarTypeMap;

		final Map<Class<?>, Class<? extends PVScalarArray>> classToPvArrayClassMap = new HashMap<>();
		classToPvArrayClassMap.put(Integer.class, PVIntArray.class);
		classToPvArrayClassMap.put(Short.class, PVShortArray.class);
		classToPvArrayClassMap.put(Long.class, PVLongArray.class);
		classToPvArrayClassMap.put(Byte.class, PVByteArray.class);
		classToPvArrayClassMap.put(Boolean.class, PVBooleanArray.class);
		classToPvArrayClassMap.put(Float.class, PVFloatArray.class);
		classToPvArrayClassMap.put(Double.class, PVDoubleArray.class);
		classToPvArrayClassMap.put(String.class, PVStringArray.class);
		CLASS_TO_PV_ARRAY_CLASS_MAP = classToPvArrayClassMap;
	}

	@Override
	public Structure buildStructure(Serialiser serialiser, MalcolmTable malcolmTable) throws Exception {
		final FieldCreate fieldCreate = FieldFactory.getFieldCreate();
		final FieldBuilder fieldBuilder = fieldCreate.createFieldBuilder();
		for (Map.Entry<String, Class<?>> entry : malcolmTable.getTableDataTypes().entrySet()) {
			fieldBuilder.addArray(entry.getKey(), CLASS_TO_SCALAR_TYPE_MAP.get(entry.getValue()));
		}
		fieldBuilder.setId(TYPE_ID_TABLE);

		return fieldBuilder.createStructure();
	}

	@Override
	public void populatePVStructure(Serialiser serialiser, MalcolmTable malcolmTable, PVStructure pvStructure) throws Exception {
		for (Map.Entry<String, Class<?>> entry : malcolmTable.getTableDataTypes().entrySet()) {
			final String columnName = entry.getKey();
			final Class<?> scalarClass = entry.getValue();
			populateField(malcolmTable, pvStructure, columnName, scalarClass);
		}
	}

	private <T> void populateField(MalcolmTable malcolmTable, PVStructure pvStructure, final String columnName,
			final Class<T> scalarClass) {
		final List<T> values = malcolmTable.getColumn(columnName);
		populateField(pvStructure, scalarClass, columnName, values);
	}

	@SuppressWarnings("unchecked")
	private <T, A extends PVScalarArray> void populateField(PVStructure pvStructure, Class<T> scalarClass,
			String fieldName, List<T> values) {
		final Class<A> pvArrayClass = (Class<A>) CLASS_TO_PV_ARRAY_CLASS_MAP.get(scalarClass);
		final A pvArrayField = pvStructure.getSubField(pvArrayClass, fieldName);

		// one alternative to using is to use reflection to get the put method on the pvarray subclass
		// another is to use a BiConsumer<PVScalarArray, List<T>> that could be looked up in a map
		if (scalarClass == Integer.class) {
			final int[] intValues = Ints.toArray((List<Integer>) values);
			((PVIntArray) pvArrayField).put(0, intValues.length, intValues, 0);
		} else if (scalarClass == Short.class) {
			final short[] shortValues = Shorts.toArray((List<Short>) values);
			((PVShortArray) pvArrayField).put(0, shortValues.length, shortValues, 0);
		} else if (scalarClass == Long.class) {
			final long[] longValues = Longs.toArray((List<Long>) values);
			((PVLongArray) pvArrayField).put(0, longValues.length, longValues, 0);
		} else if (scalarClass == Byte.class) {
			final byte[] byteValues = Bytes.toArray((List<Byte>) values);
			((PVByteArray) pvArrayField).put(0, byteValues.length, byteValues, 0);
		} else if (scalarClass == Boolean.class) {
			final boolean[] booleanValues = Booleans.toArray((List<Boolean>) values);
			((PVBooleanArray) pvArrayField).put(0, booleanValues.length, booleanValues, 0);
		} else if (scalarClass == Float.class) {
			final float[] floatValues = Floats.toArray((List<Float>) values);
			((PVFloatArray) pvArrayField).put(0, floatValues.length, floatValues, 0);
		} else if (scalarClass == Double.class) {
			final double[] doubleValues = Doubles.toArray((List<Double>) values);
			((PVDoubleArray) pvArrayField).put(0, doubleValues.length, doubleValues, 0);
		} else if (scalarClass == String.class) {
			final String[] stringValues = ((List<String>) values).toArray(new String[values.size()]);
			((PVStringArray) pvArrayField).put(0, stringValues.length, stringValues, 0);
		} else {
			throw new IllegalArgumentException("Not a scalar class: " + scalarClass);
		}
	}

}
