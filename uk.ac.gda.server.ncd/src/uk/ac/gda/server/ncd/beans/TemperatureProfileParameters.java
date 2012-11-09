/*-
 * Copyright © 2010 Diamond Light Source Ltd.
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

package uk.ac.gda.server.ncd.beans;

import gda.device.TemperatureRamp;

import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.beanutils.BeanUtils;

import uk.ac.gda.util.beans.xml.XMLHelpers;

public class TemperatureProfileParameters implements Serializable {
	private List<TemperatureRamp> rampList;
	private String name;
	
	/** */
	static public final URL mappingURL = TemperatureProfileParameters.class.getResource("TemperatureProfileMapping.xml");

	/** */
	static public final URL schemaUrl = TemperatureProfileParameters.class.getResource("TemperatureProfileMapping.xsd");

	/**
	 * @param filename
	 * @return SampleParameters
	 * @throws Exception
	 */
	public static TemperatureProfileParameters createFromXML(String filename) throws Exception {
		return (TemperatureProfileParameters) XMLHelpers.createFromXML(mappingURL, TemperatureProfileParameters.class, schemaUrl, filename);
	}

	/**
	 * @param parameters
	 * @param filename
	 * @throws Exception
	 */
	public static void writeToXML(TemperatureProfileParameters parameters, String filename) throws Exception {
		XMLHelpers.writeToXML(mappingURL, parameters, filename);
	}

	/**
	 * 
	 */
	public TemperatureProfileParameters() {
		rampList = new ArrayList<TemperatureRamp>();
	}

	/**
	 * @return the RampParameters
	 */
	public List<TemperatureRamp> getTemperatureRampList() {
		return rampList;
	}

	/**
	 * @param ramp the temperature ramp to set
	 */
	public void addTemperatureRamp(TemperatureRamp ramp) {
		this.rampList.add(ramp);
	}

	/**
	 * @param rampList the RampParameters to set
	 */
	public void setTemperatureRampList(List<TemperatureRamp> rampList) {
		this.rampList = rampList;
	}

	public void clear() {
		rampList.clear();
	}

	/**
	 *
	 */
	@Override
	public String toString() {
		try {
			return BeanUtils.describe(this).toString();
		} catch (Exception e) {
			return e.getMessage();
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime
				* result
				+ ((rampList == null) ? 0 : rampList
						.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		TemperatureProfileParameters other = (TemperatureProfileParameters) obj;
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		if (rampList == null) {
			if (other.rampList != null) {
				return false;
			}
		} else if (!rampList.equals(other.rampList)) {
			return false;
		}
		return true;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
