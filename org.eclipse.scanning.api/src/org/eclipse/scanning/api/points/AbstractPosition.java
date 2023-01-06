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
package org.eclipse.scanning.api.points;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

public abstract class AbstractPosition implements IPosition, Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 8555957478192358365L;

	private int stepIndex = -1;
	private double exposureTime;
	private List<List<String>> dimensionNames = List.of(); // Dimension->Names@dimension

	@Override
	public IPosition compound(IPosition parent) {
		if (parent==null) return this; // this+null = this
		final MapPosition ret = new MapPosition();
		ret.putAll(parent);
		ret.putAll(this);
		ret.putAllIndices(parent);
		ret.putAllIndices(this);
		ret.setStepIndex(getStepIndex());
		ret.setExposureTime(getExposureTime());

		List<List<String>> dNames = new ArrayList<>();
		dNames.addAll(parent.getDimensionNames());
		dNames.addAll(getDimensionNames());
		ret.setDimensionNames(dNames);

		return ret;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		final List<String> names = new ArrayList<>(getNames());
		Collections.sort(names);
		for (String name : names) {
			Object val = get(name);
			if (val instanceof Number) {
			    temp = Double.doubleToLongBits(((Number)val).doubleValue());
			} else {
				temp = val.hashCode();
			}
			result = prime * result + (int) (temp ^ (temp >>> 32));
		}
	    return result+stepIndex;
	}

	/**
	 * Do not override equals.
	 *
	 * MapPostion("x:1,y:1") should equal Point("x", 1, "y",1)
	 * because they represent the same motor values.
	 *
	 * @param obj
	 * @param checkStep
	 * @return
	 */
	public final boolean equals(Object obj, boolean checkStep) {

		if (this == obj)
			return true;

		if (!(obj instanceof IPosition pos))
			return false;

		if (checkStep) {
			if (stepIndex != pos.getStepIndex())
				return false;
			if (exposureTime != pos.getExposureTime())
				return false;
		}

		var ours = new HashSet<>(getNames());
		if (!ours.equals(new HashSet<>(pos.getNames()))) return false;
		for (String name : getNames()) {
			Object val1 = get(name);
			Object val2 = pos.get(name);
			if (val1 == null && val2 == null) continue;
			if (val1 == null || val2 == null) return false;
			if (!val1.equals(val2)) return false;
		}

		return getIndices().equals(pos.getIndices());
	}

	@Override
	public boolean equals(Object obj) {
		return equals(obj, true);
	}

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder("[");
		final Collection<String> names   = getNames();
        for (Iterator<String> it = names.iterator(); it.hasNext();) {
			String name = it.next();
		buf.append(name);
		int index = getIndex(name);
		if(index>-1) {
			buf.append("(");
			buf.append(index);
			buf.append(")");
		}
		buf.append("=");
		buf.append(get(name));
		if (it.hasNext()) buf.append(", ");
		}
        buf.append(", step=");
        buf.append(getStepIndex());

        if (getExposureTime()>0) {
	        buf.append(", exp=");
	        buf.append(getExposureTime());
        }

	buf.append("]");
	return buf.toString();
	}

	public List<String> getDimensionNames(int dimension) {
		return dimensionNames.get(dimension);
	}

	/**
	 * This method makes dimensionNames if they are null.
	 * It must be synchronized because getDimensionNames()
	 * is called within the thread pool, for instance when
	 * neXus writing positions.
	 *
	 * Quite a lot of tests were intermittently failing the
	 * tests because of this issue. Be careful when creating
	 * member data in this class that things are thread safe.
	 *
	 * @return dimensionNames- a List with an entry for each
	 * dimension, where each entry is a Set of the axes in
	 * that dimension
	 */

	@Override
	public synchronized List<List<String>> getDimensionNames() {
		return new ArrayList<>(dimensionNames);
	}

	@Override
	public synchronized void setDimensionNames(List<List<String>> dNames) {
		this.dimensionNames = dNames;
	}

	@Override
	public int getScanRank() {
		return getDimensionNames().size();
	}

	@Override
	public int getIndex(int dimension) {
		final String name = getDimensionNames(dimension).get(0);
		return getIndex(name);
	}


	@Override
	public int getStepIndex() {
		return stepIndex;
	}

	@Override
	public void setStepIndex(int stepIndex) {
		this.stepIndex = stepIndex;
	}

	@Override
	public double getExposureTime() {
		return exposureTime;
	}

	@Override
	public void setExposureTime(double exposureTime) {
		this.exposureTime = exposureTime;
	}

}
