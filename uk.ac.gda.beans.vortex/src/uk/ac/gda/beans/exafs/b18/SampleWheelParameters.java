/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

package uk.ac.gda.beans.exafs.b18;

import java.io.Serializable;
import java.net.URL;

import uk.ac.gda.beans.exafs.XasScanParameters;

public class SampleWheelParameters implements Serializable {

	static public final URL mappingURL = XasScanParameters.class.getResource("B18ParameterMapping.xml");
	static public final URL schemaUrl = XasScanParameters.class.getResource("B18ParameterMapping.xsd");

	double demand;
	String filter;
	boolean manual;
	boolean wheelEnabled;

	public double getDemand() {
		return demand;
	}

	public void setDemand(double demand) {
		this.demand = demand;
	}

	public String getFilter() {
		return filter;
	}

	public void setFilter(String filter) {
		this.filter = filter;
	}

	public void clear() {
		demand = 0;
		filter = null;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(demand);
		result = prime * result + (int) (temp ^ (temp >>> 32));
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
		SampleWheelParameters other = (SampleWheelParameters) obj;
		if (Double.doubleToLongBits(demand) != Double.doubleToLongBits(other.demand))
			return false;
		if(filter!=null)
			if (!filter.equals(other.filter))
				return false;
		return true;
	}

	public boolean isManual() {
		return manual;
	}

	public void setManual(boolean manual) {
		this.manual = manual;
	}

	public boolean isWheelEnabled() {
		return wheelEnabled;
	}

	public void setWheelEnabled(boolean enabled) {
		this.wheelEnabled = enabled;
	}
	
	
}
