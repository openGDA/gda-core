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

package org.eclipse.scanning.connector.epics.custommarshallers;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.points.CompoundGenerator;
import org.eclipse.scanning.points.NoModelGenerator;
import org.eclipse.scanning.points.PPointGenerator;
import org.eclipse.scanning.points.ScanPointGeneratorFactory;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvmarshaller.marshaller.api.IPVStructureDeserialiser;
import org.epics.pvmarshaller.marshaller.deserialisers.Deserialiser;
import org.python.core.Py;
import org.python.core.PyDictionary;
import org.python.core.PyList;
import org.python.core.PyObject;
import org.python.core.PyString;

/**
 * Deserialiser for a {@link CompoundGenerator}. The compound generator is first deserialised
 * back to a {@link PyDictionary} and then to an {@link IPointGenerator}. Therefore this class does the inverse
 * of the combination of both {@link IPointGeneratorSerialiser} and {@link PyDictionarySerialiser}.
 */
public class CompoundGeneratorDeserialiser implements IPVStructureDeserialiser {

	@Override
	public Object fromPVStructure(Deserialiser deserialiser, PVStructure pvStructure) throws Exception {
		final Map<?, ?> map = deserialiser.getMapDeserialiser().createMapFromPVStructure(pvStructure, LinkedHashMap.class, Object.class);
		final PyDictionary pyDictionary = convertToPyDictionary(map);

		// TODO, find a way to not call this directly. Create a PyDictionaryModel? see DAQ-2678
		final PPointGenerator pPointGen = ScanPointGeneratorFactory.JPyDictionaryGeneratorFactory().createObject(pyDictionary);

		return new NoModelGenerator(pPointGen);
	}

	private PyDictionary convertToPyDictionary(Map<?, ?> map) {
		final PyDictionary pyDict = new PyDictionary();
		for (Map.Entry<?, ?> entry : map.entrySet()) {
			if (!(entry.getKey() instanceof String))
				throw new IllegalArgumentException("Key values must be String, was: " + entry.getKey());
			final PyString key = Py.newString(entry.getKey().toString());
			final PyObject value = convertToPyObject(entry.getValue());
			pyDict.put(key, value);
		}

		return pyDict;
	}

	private PyObject convertToPyObject(Object obj) {
		if (obj instanceof Collection<?>) {
			return convertToPyList((Collection<?>) obj);
		} else if (obj instanceof Map) {
			return convertToPyDictionary((Map<?, ?>) obj);
		} else if (obj instanceof String) {
			return Py.newString((String) obj);
		} else if (obj instanceof Integer) {
			return Py.newInteger((Integer) obj);
		} else if (obj instanceof Float) {
			return Py.newFloat((Float) obj);
		} else if (obj instanceof Double) {
			return Py.newFloat((Double) obj);
		} else if (obj instanceof Long) {
			return Py.newLong((Long) obj);
		} else if (obj instanceof Boolean) {
			return Py.newBoolean((Boolean) obj);
		}
		throw new IllegalArgumentException("Field of type " + obj.getClass() + " is not supported");
	}

	private PyList convertToPyList(Collection<?> collection) {
		final PyObject[] pyObjectArray = collection.stream().map(this::convertToPyObject).toArray(PyObject[]::new);
		return new PyList(pyObjectArray);
	}

}
