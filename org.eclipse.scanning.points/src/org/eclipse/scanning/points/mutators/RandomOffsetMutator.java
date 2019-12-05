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
package org.eclipse.scanning.points.mutators;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.scanning.api.points.IMutator;
import org.eclipse.scanning.jython.JythonObjectFactory;
import org.eclipse.scanning.points.ScanPointGeneratorFactory;
import org.python.core.PyDictionary;
import org.python.core.PyObject;

public class RandomOffsetMutator implements IMutator {

	private int seed;
	private List<String> axes;
	private Map<String, Double> maxOffsets;

	public RandomOffsetMutator(int seed, List<String> axes, Map<String, Double> maxOffsets) {
		this.seed = seed;
		this.axes = axes;
		this.maxOffsets = maxOffsets;
	}

	public int getSeed() {
		return seed;
	}

	public void setSeed(int seed) {
		this.seed = seed;
	}

	public List<String> getAxes() {
		return axes;
	}

	public void setAxes(List<String> axes) {
		this.axes = axes;
	}

	public Map<String, Double> getMaxOffsets() {
		return maxOffsets;
	}

	public void setMaxOffsets(Map<String, Double> maxOffsets) {
		this.maxOffsets = maxOffsets;
	}

	@Override
	public PyObject getMutatorAsJythonObject() {
		JythonObjectFactory<PyObject> randomOffsetMutatorFactory = ScanPointGeneratorFactory.JRandomOffsetMutatorFactory();

        final List<String> axes = getAxes();
        final Map<String, Double> maxOffsets = getMaxOffsets();
        final int seed = getSeed();

        final PyDictionary maxOffset = new PyDictionary();
        for (Entry<String, Double> axis : maxOffsets.entrySet()) {
        	maxOffset.put(axis.getKey(), axis.getValue());
        }

		return randomOffsetMutatorFactory.createObject(seed, axes, maxOffset);
	}
}