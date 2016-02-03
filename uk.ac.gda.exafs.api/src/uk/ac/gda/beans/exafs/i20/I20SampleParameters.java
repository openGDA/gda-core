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

import uk.ac.gda.beans.exafs.ISampleParameters;
import uk.ac.gda.util.beans.xml.XMLHelpers;

public class I20SampleParameters implements Serializable, ISampleParameters {
	public static final String[] SAMPLE_ENV = new String[] { "None", "Room Temperature", "Cryostat" };
	public static final String[] SAMPLE_ENV_XES = new String[] { "None", "Room Temperature" };
	static public final URL mappingURL = I20SampleParameters.class.getResource("I20SampleParametersMapping.xml");
	static public final URL schemaURL = I20SampleParameters.class.getResource("I20SampleParametersMapping.xsd");
	public static final String SAMPLE_WHEEL_NAME = "filterwheel";
	private List<String> descriptions;
	private String name; // use as file prefix
	private String sampleWheelPosition;
	private Boolean useSampleWheel = false;
	private String sampleEnvironment = "None";
	private List<SampleStageParameters> roomTemperatureParameters;
	private CryostatParameters cryostatParameters;
	private FurnaceParameters furnaceParameters;
	private MicroreactorParameters microreactorParameters;
	private List<CustomXYZParameter> customXYZParameters;
	private List<CustomParameter> customParameters;
	private boolean shouldValidate = true;

	public static I20SampleParameters createFromXML(String filename) throws Exception {
		return (I20SampleParameters) XMLHelpers.createFromXML(mappingURL, I20SampleParameters.class, schemaURL,
				filename);
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
		if (customParameters != null)
			customParameters.clear();
		if (customXYZParameters != null)
			customXYZParameters.clear();
		if (roomTemperatureParameters != null)
			roomTemperatureParameters.clear();
	}

	public I20SampleParameters() {
		descriptions = new ArrayList<String>(7);
		customParameters = new ArrayList<CustomParameter>(7);
		customXYZParameters = new ArrayList<CustomXYZParameter>(7);
		roomTemperatureParameters = new ArrayList<SampleStageParameters>(7);
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

	public String getSampleWheelPosition() {
		return sampleWheelPosition;
	}

	public void setSampleWheelPosition(String sampleWheelPosition) {
		this.sampleWheelPosition = sampleWheelPosition;
	}

	public List<SampleStageParameters> getRoomTemperatureParameters() {
		return roomTemperatureParameters;
	}

	public void setRoomTemperatureParameters(List<SampleStageParameters> roomTemperatureParameters) {
		this.roomTemperatureParameters = roomTemperatureParameters;
	}

	public void addRoomTemperatureParameter(SampleStageParameters roomTempParameter) {
		roomTemperatureParameters.add(roomTempParameter);
	}

	public CryostatParameters getCryostatParameters() {
		return cryostatParameters;
	}

	public NoneParameters getNoneParameters() {
		return new NoneParameters();
	}

	public void setCryostatParameters(CryostatParameters cryostatParameters) {
		this.cryostatParameters = cryostatParameters;
	}

	public FurnaceParameters getFurnaceParameters() {
		return furnaceParameters;
	}

	public void setFurnaceParameters(FurnaceParameters furnaceParameters) {
		this.furnaceParameters = furnaceParameters;
	}

	public void setMicroreactorParameters(MicroreactorParameters microreactorParameters) {
		this.microreactorParameters = microreactorParameters;
	}

	public MicroreactorParameters getMicroreactorParameters() {
		return microreactorParameters;
	}

	public List<CustomParameter> getCustomParameters() {
		return customParameters;
	}

	public void addCustomParameter(CustomParameter customParameter) {
		customParameters.add(customParameter);
	}

	public void setCustomParameters(List<CustomParameter> customParameters) {
		this.customParameters = customParameters;
	}

	public List<CustomXYZParameter> getCustomXYZParameters() {
		return customXYZParameters;
	}

	public void addCustomXYZParameter(CustomXYZParameter customParameter) {
		customXYZParameters.add(customParameter);
	}

	public void setCustomXYZParameters(List<CustomXYZParameter> customParameters) {
		this.customXYZParameters = customParameters;
	}

	@Override
	public String toString() {
		try {
			return BeanUtils.describe(this).toString();
		} catch (Exception e) {
			return e.getMessage();
		}
	}

	public String getSampleEnvironment() {
		return sampleEnvironment;
	}

	public void setSampleEnvironment(String sampleEnvironment) {
		this.sampleEnvironment = sampleEnvironment;
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
		result = prime * result + ((cryostatParameters == null) ? 0 : cryostatParameters.hashCode());
		result = prime * result + ((customParameters == null) ? 0 : customParameters.hashCode());
		result = prime * result + ((customXYZParameters == null) ? 0 : customXYZParameters.hashCode());
		result = prime * result + ((descriptions == null) ? 0 : descriptions.hashCode());
		result = prime * result + ((furnaceParameters == null) ? 0 : furnaceParameters.hashCode());
		result = prime * result + ((microreactorParameters == null) ? 0 : microreactorParameters.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((roomTemperatureParameters == null) ? 0 : roomTemperatureParameters.hashCode());
		result = prime * result + ((sampleEnvironment == null) ? 0 : sampleEnvironment.hashCode());
		result = prime * result + ((sampleWheelPosition == null) ? 0 : sampleWheelPosition.hashCode());
		result = prime * result + (shouldValidate ? 1231 : 1237);
		result = prime * result + ((useSampleWheel == null) ? 0 : useSampleWheel.hashCode());
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
		if (cryostatParameters == null) {
			if (other.cryostatParameters != null)
				return false;
		}
		else if (!cryostatParameters.equals(other.cryostatParameters))
			return false;
		if (customParameters == null) {
			if (other.customParameters != null)
				return false;
		}
		else if (!customParameters.equals(other.customParameters))
			return false;
		if (customXYZParameters == null) {
			if (other.customXYZParameters != null)
				return false;
		}
		else if (!customXYZParameters.equals(other.customXYZParameters))
			return false;
		if (descriptions == null) {
			if (other.descriptions != null)
				return false;
		}
		else if (!descriptions.equals(other.descriptions))
			return false;
		if (furnaceParameters == null) {
			if (other.furnaceParameters != null)
				return false;
		}
		else if (!furnaceParameters.equals(other.furnaceParameters))
			return false;
		if (microreactorParameters == null) {
			if (other.microreactorParameters != null)
				return false;
		}
		else if (!microreactorParameters.equals(other.microreactorParameters))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		}
		else if (!name.equals(other.name))
			return false;
		if (roomTemperatureParameters == null) {
			if (other.roomTemperatureParameters != null)
				return false;
		}
		else if (!listEquals(roomTemperatureParameters, other.roomTemperatureParameters))
			return false;
		if (sampleEnvironment == null) {
			if (other.sampleEnvironment != null)
				return false;
		}
		else if (!sampleEnvironment.equals(other.sampleEnvironment))
			return false;
		if (sampleWheelPosition == null) {
			if (other.sampleWheelPosition != null)
				return false;
		}
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
		return true;
	}

	private boolean listEquals(List<?> list1, List<?> list2) {
		if (list1.size() != list2.size())
			return false;
		for (int element = 0; element < list1.size(); element++)
			if (!list1.get(element).equals(list2.get(element)))
				return false;
		return true;
	}

}