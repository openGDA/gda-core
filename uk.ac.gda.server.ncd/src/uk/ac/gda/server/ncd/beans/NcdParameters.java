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

package uk.ac.gda.server.ncd.beans;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.beanutils.BeanUtils;

import uk.ac.gda.util.beans.xml.XMLHelpers;

public class NcdParameters {

	// private DetectorParameters detectorParameters;
	private List<TimeProfileParameters> timeProfileParameters;

	/** */
	static public final URL mappingURL = NcdParameters.class.getResource("NcdParameterMapping.xml");

	/** */
	static public final URL schemaURL = NcdParameters.class.getResource("NcdParameterMapping.xsd");

	/**
	 * @param filename
	 * @return SampleParameters
	 * @throws Exception
	 */
	public static NcdParameters createFromXML(String filename) throws Exception {
		return XMLHelpers.createFromXML(mappingURL, NcdParameters.class, schemaURL, filename);
	}

	/**
	 * @param parameters
	 * @param filename
	 * @throws Exception
	 */
	public static void writeToXML(NcdParameters parameters, String filename) throws Exception {
		XMLHelpers.writeToXML(mappingURL, parameters, filename);
	}

	/**
	 * 
	 */
	public NcdParameters() {
		timeProfileParameters = new ArrayList<TimeProfileParameters>();
	}
	
	/**
	 * @return the timeProfileParameters
	 */
	public List<TimeProfileParameters> getTimeProfileParameters() {
		return timeProfileParameters;
	}

	/**
	 * @param timeProfileParameters the timeProfileParameters to set
	 */
	public void addTimeProfileParameter(TimeProfileParameters timeProfileParameters) {
		this.timeProfileParameters.add(timeProfileParameters);
	}
	
	/**
	 * @param timeProfileParameters the timeProfileParameters to set
	 */
	public void setTimeProfileParameters(List<TimeProfileParameters> timeProfileParameters) {
		this.timeProfileParameters = timeProfileParameters;
	}

	public void clear() {
		timeProfileParameters.clear();
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
		result = prime
				* result
				+ ((timeProfileParameters == null) ? 0 : timeProfileParameters
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
		NcdParameters other = (NcdParameters) obj;
		if (timeProfileParameters == null) {
			if (other.timeProfileParameters != null) {
				return false;
			}
		} else if (!timeProfileParameters.equals(other.timeProfileParameters)) {
			return false;
		}
		return true;
	}
}
