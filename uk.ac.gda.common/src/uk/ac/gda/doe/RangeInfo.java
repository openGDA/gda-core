/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package uk.ac.gda.doe;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Contains a list of parameters that are part of the range.
 */
public class RangeInfo implements Serializable {

	private List<FieldValue> experiments;

	public RangeInfo() {
		experiments = new ArrayList<FieldValue>(7);
	}
		
	public void clear() {
		experiments.clear();
	}

	/**
	 * @return Returns the experiments.
	 */
	public List<FieldValue> getExperiments() {
		return experiments;
	}

	/**
	 * @param e The experiments to set.
	 */
	public void setExperiments(List<FieldValue> e) {
		if (e == null) {
			experiments.clear();
			return;
		}
		this.experiments = e;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((experiments == null) ? 0 : experiments.hashCode());
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
		RangeInfo other = (RangeInfo) obj;
		if (experiments == null) {
			if (other.experiments != null)
				return false;
		} else if (!experiments.equals(other.experiments))
			return false;
		return true;
	}

	public void set(FieldValue fieldValue) {
		final int index = experiments.indexOf(fieldValue);
		if (index>-1) {
			experiments.set(index, fieldValue);
			return;
		}
		experiments.add(fieldValue);
	}

	public static String format(final List<RangeInfo> info) {

		final StringBuilder buf = new StringBuilder();
		
		// Do header
		buf.append(info.get(0).getHeader());
		buf.append("\n");
		
		// Do values
		for (RangeInfo rangeInfo : info) {
			buf.append(rangeInfo.getValues());
			buf.append("\n");
		}
		
		return buf.toString();
	}

	public String getHeader() {
		final StringBuilder buf = new StringBuilder();
		for (FieldValue field : getExperiments()) {
			buf.append(field.getName());
			buf.append("\t");
		}
		return buf.toString();
	}
	
	public String getValues() {
		final StringBuilder buf = new StringBuilder();
		for (FieldValue field : getExperiments()) {
			buf.append(field.getValue());
			buf.append("\t");
		}
		return buf.toString();
	}

	public boolean isEmpty() {
		return experiments.isEmpty();
	}

	public Map<String, Class<?>> getColumnClasses() {
		final Map<String, Class<?>> columns = new LinkedHashMap<String, Class<?>>(31);
		for (FieldValue fv : experiments) {
			columns.put(fv.getName(), fv.getOriginalObject().getClass());
		}
		return columns;
	}

	/**
	 * Fields should not be large so a loop is ok, could
	 * replace with a map one day.
	 * 
	 * @param name
	 * @return value or null
	 */
	public String getColumnValue(final String name) {
		if (experiments.isEmpty()) return null;
		if (name == null)          return null;
		for (FieldValue fv : experiments) {
			if (name.equals(fv.getName())) return fv.getValue();
		}
		
		return null;
	}
}
