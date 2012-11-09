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

import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.beanutils.BeanUtils;

import uk.ac.gda.util.beans.xml.XMLHelpers;

public class CalibrationLabels implements Serializable {
	private List<CalibLabel> calibrationLabels;
	private String name;
	
	/** */
	static public final URL mappingURL = CalibrationLabels.class.getResource("CalibrationLabelsMapping.xml");

	/** */
	static public final URL schemaUrl = CalibrationLabels.class.getResource("CalibrationLabelsMapping.xsd");

	/**
	 * @param filename
	 * @return SampleParameters
	 * @throws Exception
	 */
	public static CalibrationLabels createFromXML(String filename) throws Exception {
		return (CalibrationLabels) XMLHelpers.createFromXML(mappingURL, CalibrationLabels.class, schemaUrl, filename);
	}

	/**
	 * @param parameters
	 * @param filename
	 * @throws Exception
	 */
	public static void writeToXML(CalibrationLabels parameters, String filename) throws Exception {
		XMLHelpers.writeToXML(mappingURL, parameters, filename);
	}

	/**
	 * 
	 */
	public CalibrationLabels() {
		calibrationLabels = new ArrayList<CalibLabel>();
	}

	/**
	 * @return the RampParameters
	 */
	public List<CalibLabel> getCalibrationLabels() {
		return calibrationLabels;
	}

	/**
	 * @param calibLabel
	 *            the calibLabel to set
	 */
	public void addCalibLabel(CalibLabel calibLabel) {
		this.calibrationLabels.add(calibLabel);
	}

	public void clear() {
		calibrationLabels.clear();
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
		result = prime * result + ((calibrationLabels == null) ? 0 : calibrationLabels.hashCode());
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
		CalibrationLabels other = (CalibrationLabels) obj;
		if (calibrationLabels == null) {
			if (other.calibrationLabels != null) {
				return false;
			}
		} else if (!calibrationLabels.equals(other.calibrationLabels)) {
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
