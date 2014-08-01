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

package uk.ac.gda.beans.exafs.bm26a;

import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.beanutils.BeanUtils;

import uk.ac.gda.beans.exafs.ISampleParameters;

public class SampleParameters implements Serializable, ISampleParameters {

	static public final URL mappingURL = SampleParameters.class.getResource("SampleParametersMapping.xml");
	static public final URL schemaURL = SampleParameters.class.getResource("SampleParametersMapping.xsd");
	/**
	 * Valid sample stages.
	 */
	public static final String[] STAGE = new String[] { "none", "xyzstage", "cryostage"};

	private String name = "";
	private String description1 = "";
	private String description2 = "";
	private String stage = "None";
	private XYZStageParameters xyzStageParameters = new XYZStageParameters();
	private XYZStageParameters cryoStageParameters = new XYZStageParameters();
	private boolean shouldValidate = true;

	/**
	 * Method required to use with BeanUI. Called using reflection.
	 */
	@Override
	public void clear() {
		name = description1 = description2 = "";
		stage = "None";
		if (xyzStageParameters != null) {
			xyzStageParameters.clear();
		}
		if (cryoStageParameters != null) {
			cryoStageParameters.clear();
		}
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

	public XYZStageParameters getXyzStageParameters() {
		return xyzStageParameters;
	}

	public void setXyzStageParameters(XYZStageParameters xyzStageParameters) {
		this.xyzStageParameters = xyzStageParameters;
	}

	public XYZStageParameters getCryoStageParameters() {
		return cryoStageParameters;
	}

	public void setCryoStageParameters(XYZStageParameters cryoStageParameters) {
		this.cryoStageParameters = cryoStageParameters;
	}

	public NoneParameters getNoneParameters() {
		return new NoneParameters();
	}

	@Override
	public List<String> getDescriptions() {
		return Arrays.asList(new String[] { description1, description2 });
	}

	public String getStage() {
		return stage;
	}

	public void setStage(String stage) {
		this.stage = stage;
	}

	public boolean isShouldValidate() {
		return shouldValidate;
	}

	public void setShouldValidate(boolean shouldValidate) {
		this.shouldValidate = shouldValidate;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((description1 == null) ? 0 : description1.hashCode());
		result = prime * result + ((description2 == null) ? 0 : description2.hashCode());
		result = prime * result + ((xyzStageParameters == null) ? 0 : xyzStageParameters.hashCode());
		result = prime * result + ((cryoStageParameters == null) ? 0 : cryoStageParameters.hashCode());
		result = prime * result + ((stage == null) ? 0 : stage.hashCode());
		result = prime * result + (shouldValidate ? 1231 : 1237);
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
		SampleParameters other = (SampleParameters) obj;
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
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (xyzStageParameters == null) {
			if (other.xyzStageParameters != null)
				return false;
		} else if (!xyzStageParameters.equals(other.xyzStageParameters))
			return false;
		if (cryoStageParameters == null) {
			if (other.xyzStageParameters != null)
				return false;
		} else if (!cryoStageParameters.equals(other.cryoStageParameters))
			return false;
		if (stage == null) {
			if (other.stage != null)
				return false;
		} else if (!stage.equals(other.stage))
			return false;
		if (shouldValidate != other.shouldValidate)
			return false;
		return true;
	}

	@Override
	public String toString() {
		try {
			return BeanUtils.describe(this).toString();
		} catch (Exception e) {
			return e.getMessage();
		}
	}
}
