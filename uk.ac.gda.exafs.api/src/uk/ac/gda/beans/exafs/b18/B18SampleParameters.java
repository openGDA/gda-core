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

package uk.ac.gda.beans.exafs.b18;

import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import uk.ac.gda.beans.exafs.ISampleParametersWithMotorPositions;
import uk.ac.gda.beans.exafs.SampleParameterMotorPosition;

public class B18SampleParameters implements Serializable, ISampleParametersWithMotorPositions {
	private static final long serialVersionUID = 7285061621724049474L;

	static public final URL mappingURL = B18SampleParameters.class.getResource("B18ParameterMapping.xml");

	static public final URL schemaURL = B18SampleParameters.class.getResource("B18ParameterMapping.xsd");

	/**
	 * Valid sample environments.
	 */
	public static final String[] TEMP_CONTROL = new String[] { "none", "pulsetubecryostat", "furnace", "lakeshore" };

	/**
	 * Valid sample stages.
	 */
	public static final String[] STAGE = new String[] { "none", "xythetastage", "ln2cryostage", "sxcryostage", "userstage" };

	String name = "";
	String description1 = "";
	String description2 = "";
	String stage = "none";
	List<String> selectedSampleStages = new ArrayList<>();

	String temperatureControl = "none";
	XYThetaStageParameters xythetaParameters = new XYThetaStageParameters();
	LN2CryoStageParameters ln2CryoStageParameters = new LN2CryoStageParameters();
	SXCryoStageParameters sxCryoStageParameters = new SXCryoStageParameters();
	PulseTubeCryostatParameters lheParameters = new PulseTubeCryostatParameters();
	FurnaceParameters furnaceParameters = new FurnaceParameters();
	LakeshoreParameters lakeshoreParameters = new LakeshoreParameters();
	SampleWheelParameters sampleWheelParameters = new SampleWheelParameters();
	UserStageParameters userStageParameters = new UserStageParameters();
	private List<SampleParameterMotorPosition> sampleParameterMotorPositions = new ArrayList<>();

	boolean shouldValidate = true;

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((description1 == null) ? 0 : description1.hashCode());
		result = prime * result + ((description2 == null) ? 0 : description2.hashCode());
		result = prime * result + ((furnaceParameters == null) ? 0 : furnaceParameters.hashCode());
		result = prime * result + ((lakeshoreParameters == null) ? 0 : lakeshoreParameters.hashCode());
		result = prime * result + ((lheParameters == null) ? 0 : lheParameters.hashCode());
		result = prime * result + ((ln2CryoStageParameters == null) ? 0 : ln2CryoStageParameters.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result
				+ ((sampleParameterMotorPositions == null) ? 0 : sampleParameterMotorPositions.hashCode());
		result = prime * result + ((sampleWheelParameters == null) ? 0 : sampleWheelParameters.hashCode());
		result = prime * result + ((selectedSampleStages == null) ? 0 : selectedSampleStages.hashCode());
		result = prime * result + (shouldValidate ? 1231 : 1237);
		result = prime * result + ((stage == null) ? 0 : stage.hashCode());
		result = prime * result + ((sxCryoStageParameters == null) ? 0 : sxCryoStageParameters.hashCode());
		result = prime * result + ((temperatureControl == null) ? 0 : temperatureControl.hashCode());
		result = prime * result + ((userStageParameters == null) ? 0 : userStageParameters.hashCode());
		result = prime * result + ((xythetaParameters == null) ? 0 : xythetaParameters.hashCode());
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
		B18SampleParameters other = (B18SampleParameters) obj;
		if (description1 == null) {
			if (other.description1 != null)
				return false;
		} else if (!description1.equals(other.description1))
			return false;
		if (description2 == null) {
			if (other.description2 != null)
				return false;
		} else if (!description2.equals(other.description2))
			return false;
		if (furnaceParameters == null) {
			if (other.furnaceParameters != null)
				return false;
		} else if (!furnaceParameters.equals(other.furnaceParameters))
			return false;
		if (lakeshoreParameters == null) {
			if (other.lakeshoreParameters != null)
				return false;
		} else if (!lakeshoreParameters.equals(other.lakeshoreParameters))
			return false;
		if (lheParameters == null) {
			if (other.lheParameters != null)
				return false;
		} else if (!lheParameters.equals(other.lheParameters))
			return false;
		if (ln2CryoStageParameters == null) {
			if (other.ln2CryoStageParameters != null)
				return false;
		} else if (!ln2CryoStageParameters.equals(other.ln2CryoStageParameters))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (sampleParameterMotorPositions == null) {
			if (other.sampleParameterMotorPositions != null)
				return false;
		} else if (!sampleParameterMotorPositions.equals(other.sampleParameterMotorPositions))
			return false;
		if (sampleWheelParameters == null) {
			if (other.sampleWheelParameters != null)
				return false;
		} else if (!sampleWheelParameters.equals(other.sampleWheelParameters))
			return false;
		if (selectedSampleStages == null) {
			if (other.selectedSampleStages != null)
				return false;
		} else if (!selectedSampleStages.equals(other.selectedSampleStages))
			return false;
		if (shouldValidate != other.shouldValidate)
			return false;
		if (stage == null) {
			if (other.stage != null)
				return false;
		} else if (!stage.equals(other.stage))
			return false;
		if (sxCryoStageParameters == null) {
			if (other.sxCryoStageParameters != null)
				return false;
		} else if (!sxCryoStageParameters.equals(other.sxCryoStageParameters))
			return false;
		if (temperatureControl == null) {
			if (other.temperatureControl != null)
				return false;
		} else if (!temperatureControl.equals(other.temperatureControl))
			return false;
		if (userStageParameters == null) {
			if (other.userStageParameters != null)
				return false;
		} else if (!userStageParameters.equals(other.userStageParameters))
			return false;
		if (xythetaParameters == null) {
			if (other.xythetaParameters != null)
				return false;
		} else if (!xythetaParameters.equals(other.xythetaParameters))
			return false;
		return true;
	}

	@Override
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription1() {
		return description1;
	}

	public void setDescription1(String description) {
		this.description1 = description;
	}

	public String getDescription2() {
		return description2;
	}

	public void setDescription2(String description) {
		this.description2 = description;
	}

	public String getTemperatureControl() {
		return temperatureControl;
	}

	public void setTemperatureControl(String tempertaureControl) {
		this.temperatureControl = tempertaureControl;
	}

	public XYThetaStageParameters getXYThetaStageParameters() {
		return xythetaParameters;
	}

	public void setXYThetaStageParameters(XYThetaStageParameters xythetaParameters) {
		this.xythetaParameters = xythetaParameters;
	}

	public UserStageParameters getUserStageParameters() {
		return userStageParameters;
	}

	public void setUserStageParameters(UserStageParameters userStageParameters) {
		this.userStageParameters = userStageParameters;
	}

	public LN2CryoStageParameters getLN2CryoStageParameters() {
		return ln2CryoStageParameters;
	}

	public void setLN2CryoStageParameters(LN2CryoStageParameters ln2CryoStageParameters) {
		this.ln2CryoStageParameters = ln2CryoStageParameters;
	}

	public SXCryoStageParameters getSXCryoStageParameters() {
		return sxCryoStageParameters;
	}

	public void setSXCryoStageParameters(SXCryoStageParameters sxCryoStageParameters) {
		this.sxCryoStageParameters = sxCryoStageParameters;
	}

	public PulseTubeCryostatParameters getPulseTubeCryostatParameters() {
		return lheParameters;
	}

	public void setPulseTubeCryostatParameters(PulseTubeCryostatParameters lheParameters) {
		this.lheParameters = lheParameters;
	}

	public FurnaceParameters getFurnaceParameters() {
		return furnaceParameters;
	}

	public void setFurnaceParameters(FurnaceParameters furnaceParameters) {
		this.furnaceParameters = furnaceParameters;
	}

	public boolean isShouldValidate() {
		return shouldValidate;
	}

	public void setShouldValidate(boolean shouldValidate) {
		this.shouldValidate = shouldValidate;
	}

	public String getStage() {
		return stage;
	}

	public void setStage(String stage) {
		this.stage = stage;
	}

	public List<String> getSelectedSampleStages() {
		return selectedSampleStages;
	}

	public void setSelectedSampleStages(List<String> selectedStages) {
		this.selectedSampleStages = selectedStages;
	}

	public void addSelectedSampleStage(String stage) {
		this.selectedSampleStages.add( stage );
	}

	public LakeshoreParameters getLakeshoreParameters() {
		return lakeshoreParameters;
	}

	public void setLakeshoreParameters(LakeshoreParameters lakeshoreParameters) {
		this.lakeshoreParameters = lakeshoreParameters;
	}

	@Override
	public List<String> getDescriptions() {
		return Arrays.asList(new String[] { description1, description2 });
	}

	public SampleWheelParameters getSampleWheelParameters() {
		return sampleWheelParameters;
	}

	public void setSampleWheelParameters(SampleWheelParameters sampleWheelParameters) {
		this.sampleWheelParameters = sampleWheelParameters;
	}

	public void clear() {
		selectedSampleStages.clear();
		sampleParameterMotorPositions.clear();
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
