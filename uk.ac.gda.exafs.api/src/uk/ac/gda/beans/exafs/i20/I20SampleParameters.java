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

package uk.ac.gda.beans.exafs.i20;

import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.beanutils.BeanUtils;

import uk.ac.gda.beans.exafs.ISampleParametersWithMotorPositions;
import uk.ac.gda.beans.exafs.SampleParameterMotorPosition;
import uk.ac.gda.util.beans.xml.XMLHelpers;

public class I20SampleParameters implements Serializable, ISampleParametersWithMotorPositions {
	static public final URL mappingURL = I20SampleParameters.class.getResource("I20SampleParametersMapping.xml");
	static public final URL schemaURL = I20SampleParameters.class.getResource("I20SampleParametersMapping.xsd");
	public static final String SAMPLE_WHEEL_NAME = "filterwheel";
	private List<String> descriptions;
	private String name; // use as file prefix
	private String sampleWheelPosition;
	private Boolean useSampleWheel = false;
	private List<SampleParameterMotorPosition> sampleParameterMotorPositions = new ArrayList<>();
	private boolean shouldValidate = true;

	public static I20SampleParameters createFromXML(String filename) throws Exception {
		return XMLHelpers.createFromXML(mappingURL, I20SampleParameters.class, schemaURL, filename);
	}

	public static void writeToXML(I20SampleParameters sampleParameters, String filename) throws Exception {
		XMLHelpers.writeToXML(mappingURL, sampleParameters, filename);
	}

	/**
	 * Method required to use with BeanUI. Called using reflection.
	 */
	public void clear() {
		if (descriptions != null)
			descriptions.clear();
		sampleParameterMotorPositions.clear();
	}

	public I20SampleParameters() {
		descriptions = new ArrayList<>(7);
	}

	@Override
	public String getName() {
		return name;
	}

	public void setDescriptions(List<String> descriptions) {
		// Castor's implementation for string lists is different to other lists
		// and the set can pass in an unmodifiable list.
		this.descriptions.clear();
		if (descriptions == null)
			return;
		this.descriptions.addAll(descriptions);
	}

	public void setName(String name) {
		this.name = name;
	}

	public void addDescription(String description) {
		descriptions.add(description);
	}

	@Override
	public List<String> getDescriptions() {
		return descriptions;
	}

	public String getDescription() {
		if (descriptions != null && descriptions.size()>0) {
			return descriptions.get(0);
		} else {
			return "";
		}
	}

	public void setDescription(String description) {
		this.descriptions.clear();
		descriptions.add(description);
	}

	public String getSampleWheelPosition() {
		return sampleWheelPosition;
	}

	public void setSampleWheelPosition(String sampleWheelPosition) {
		this.sampleWheelPosition = sampleWheelPosition;
	}

	public NoneParameters getNoneParameters() {
		return new NoneParameters();
	}

	@Override
	public String toString() {
		try {
			return BeanUtils.describe(this).toString();
		} catch (Exception e) {
			return e.getMessage();
		}
	}

	public boolean isShouldValidate() {
		return shouldValidate;
	}

	public void setShouldValidate(boolean shouldValidate) {
		this.shouldValidate = shouldValidate;
	}

	public Boolean getUseSampleWheel() {
		return useSampleWheel;
	}

	public void setUseSampleWheel(Boolean useSampleWheel) {
		this.useSampleWheel = useSampleWheel;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((descriptions == null) ? 0 : descriptions.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((sampleWheelPosition == null) ? 0 : sampleWheelPosition.hashCode());
		result = prime * result + (shouldValidate ? 1231 : 1237);
		result = prime * result + ((useSampleWheel == null) ? 0 : useSampleWheel.hashCode());
		result = prime * result	+ ((sampleParameterMotorPositions == null) ? 0 : sampleParameterMotorPositions.hashCode());
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
		I20SampleParameters other = (I20SampleParameters) obj;
		if (descriptions == null) {
			if (other.descriptions != null)
				return false;
		}
		else if (!descriptions.equals(other.descriptions))
			return false;
		else if (!sampleWheelPosition.equals(other.sampleWheelPosition))
			return false;
		if (shouldValidate != other.shouldValidate)
			return false;
		if (useSampleWheel == null) {
			if (other.useSampleWheel != null)
				return false;
		}
		else if (!useSampleWheel.equals(other.useSampleWheel))
			return false;
		if (sampleParameterMotorPositions == null) {
			if (other.sampleParameterMotorPositions != null)
				return false;
		} else if (!sampleParameterMotorPositions.equals(other.sampleParameterMotorPositions))
			return false;
		return true;
	}

	@Override
	public SampleParameterMotorPosition getSampleParameterMotorPosition(String scannableName) {
		for(SampleParameterMotorPosition sampleMotor : sampleParameterMotorPositions) {
			if (sampleMotor.getScannableName().equalsIgnoreCase(scannableName)) {
				return sampleMotor;
			}
		}
		return null;
	}

	@Override
	public List<SampleParameterMotorPosition> getSampleParameterMotorPositions() {
		return sampleParameterMotorPositions;
	}

	@Override
	public void setSampleParameterMotorPositions(List<SampleParameterMotorPosition> sampleParameterMotorPositions) {
		this.sampleParameterMotorPositions = sampleParameterMotorPositions;
	}

	@Override
	public void addSampleParameterMotorPosition(SampleParameterMotorPosition sampleParameterMotorPosition) {
		sampleParameterMotorPositions.add(sampleParameterMotorPosition);
	}

}