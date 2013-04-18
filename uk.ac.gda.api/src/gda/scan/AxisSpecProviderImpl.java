/*-
 * Copyright © 2013 Diamond Light Source Ltd.
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

package gda.scan;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * Implementation of AxisSpecProvider
 * 
 * To have a separate yaxis for each signal plotted simply
 * use the constructor AxisSpecProviderImpl(b). Or else
 * use setyAxesMap to provide a map.
 * 
 * An example of use from Jython
 * setyAxesMap({AxisSpec("y"):["iy", "iy_nosiy"]})
 *
 */
public class AxisSpecProviderImpl implements AxisSpecProvider {
	private HashMap<AxisSpec, Vector<String>> yAxesMap;
	private boolean useSeparateYAxes=false;
	
	public AxisSpecProviderImpl(boolean b) {
		useSeparateYAxes = b;
	}

	@Override
	public AxisSpec getAxisSpec(String name) {
		AxisSpec axisSpec = null;
		if (yAxesMap != null) {
			for (Map.Entry<AxisSpec, Vector<String>> e : yAxesMap.entrySet()) {
				if (e.getValue().contains(name)) {
					axisSpec = e.getKey();
					break;
				}
			}
		} else {
			if( useSeparateYAxes)
				axisSpec = new AxisSpec(name);
		}
		return axisSpec;
	}

	public HashMap<AxisSpec, Vector<String>> getyAxesMap() {
		return yAxesMap;
	}

	public void setyAxesMap(Map<AxisSpec, List<String>> map) {
		yAxesMap = new HashMap<AxisSpec, Vector<String>>();
		for (Map.Entry<AxisSpec, List<String>> e : map.entrySet()) {
			Vector<String> v = new Vector<String>();
			List<String> names = e.getValue();
			for (String name : names) {
				v.add(name);
			}
			yAxesMap.put(e.getKey(), v);
		}
	}

	public boolean isUseSeparateYAxes() {
		return useSeparateYAxes;
	}

	public void setUseSeparateYAxes(boolean useSeparateYAxes) {
		this.useSeparateYAxes = useSeparateYAxes;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (useSeparateYAxes ? 1231 : 1237);
		result = prime * result + ((yAxesMap == null) ? 0 : yAxesMap.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AxisSpecProviderImpl other = (AxisSpecProviderImpl) obj;
		if (useSeparateYAxes != other.useSeparateYAxes)
			return false;
		if (yAxesMap == null) {
			if (other.yAxesMap != null)
				return false;
		} else if (!yAxesMap.equals(other.yAxesMap))
			return false;
		return true;
	}
	
}
