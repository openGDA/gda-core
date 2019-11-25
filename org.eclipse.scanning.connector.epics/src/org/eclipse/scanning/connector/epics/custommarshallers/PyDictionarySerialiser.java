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

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.Structure;
import org.epics.pvmarshaller.marshaller.api.IPVStructureSerialiser;
import org.epics.pvmarshaller.marshaller.serialisers.Serialiser;
import org.python.core.PyArray;
import org.python.core.PyBoolean;
import org.python.core.PyDictionary;
import org.python.core.PyFloat;
import org.python.core.PyInteger;
import org.python.core.PyList;
import org.python.core.PyString;
import org.python.core.PyUnicode;

/**
 * Custom serialiser for PyDictionary.
 *
 * @author Matt Taylor
 */
public class PyDictionarySerialiser implements IPVStructureSerialiser<PyDictionary> {

	@Override
	public Structure buildStructure(Serialiser serialiser, PyDictionary dictionary) throws Exception {
		// Convert to map first
		final Map<String, Object> dictionaryAsMap = convertPyDictionary(dictionary);
		return serialiser.getMapSerialiser().buildStructureFromMap(dictionaryAsMap);
	}

	@Override
	public void populatePVStructure(Serialiser serialiser, PyDictionary dictionary, PVStructure pvStructure) throws Exception {
		final Map<String, Object> dictionaryAsMap = convertPyDictionary(dictionary);
		serialiser.getMapSerialiser().setMapValues(pvStructure, dictionaryAsMap);
	}

	private Map<String, Object> convertPyDictionary(PyDictionary dictionary) {
		final LinkedHashMap<String, Object> result = new LinkedHashMap<>();
		@SuppressWarnings("unchecked")
		final Set<Map.Entry<Object, Object>> entrySet = dictionary.entrySet();
		for (Map.Entry<Object, Object> entry : entrySet) {
			result.put(entry.getKey().toString(), convertPyObject(entry.getValue()));
		}

		return result;
	}

	/**
	 * Converts a python object into the raw java equivalent
	 * @param pyObj THe python object to convert
	 * @return the raw java equivalent of the python object
	 */
	private Object convertPyObject(Object pyObj) {
		if (pyObj == null) {
			return null;
		}
		if (pyObj instanceof PyList) {
			return convertPyList((PyList) pyObj);
		} else if (pyObj instanceof PyArray) {
			return convertPyObject(((PyArray) pyObj).tolist());
		} else if (pyObj instanceof PyDictionary) {
			PyDictionary pyDict = (PyDictionary) pyObj;
			return convertPyDictionary(pyDict);
		} else if (pyObj instanceof PyUnicode) {
			return ((PyUnicode) pyObj).getString();
		} else if (pyObj instanceof PyInteger) {
			return ((PyInteger) pyObj).getValue();
		} else if (pyObj instanceof PyFloat) {
			return ((PyFloat) pyObj).getValue();
		} else if (pyObj instanceof PyBoolean) {
			return ((PyBoolean) pyObj).getBooleanValue();
		} else if (pyObj instanceof PyString) {
			return ((PyString) pyObj).getString();
		}

		if (pyObj.getClass().toString().contains("py")) {
			System.err.println("NOT CAUGHT [" + pyObj + "] class = " + pyObj.getClass());
		}

		return pyObj;
	}

	private Object convertPyList(PyList pyList) {
		if (pyList.size() == 1) {
			Object first = pyList.get(0);
			if (first.getClass().isArray()) {
				return convertPyObject(pyList.get(0));
			}
		}
		Object[] array = pyList.toArray();
		LinkedList<Object> newList = new LinkedList<>();
		for (Object listElement : array) {
			newList.add(convertPyObject(listElement));
		}
		return newList;
	}

}
