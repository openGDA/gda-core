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

package uk.ac.gda.beans.exafs.i18;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.beanutils.BeanUtils;

import uk.ac.gda.beans.exafs.ISampleParameters;

public class I18SampleParameters implements ISampleParameters {

	public static final URL mappingURL = I18SampleParameters.class.getResource("I18SampleParametersMapping.xml");
	public static final URL schemaURL = I18SampleParameters.class.getResource("I18SampleParametersMapping.xsd");

	private String name = "";
	private SampleStageParameters sampleStageParameters;
	private List<AttenuatorParameters> attenuators = new ArrayList<>();
	private String description = "";
	private double vfmx;
	private boolean vfmxActive;

	public void setSampleStageParameters(SampleStageParameters sampleStageParameters) {
		this.sampleStageParameters = sampleStageParameters;
	}

	public SampleStageParameters getSampleStageParameters() {
		return sampleStageParameters;
	}

	public List<AttenuatorParameters> getAttenuators() {
		return attenuators;
	}

	public void addAttenuator(AttenuatorParameters bean) {
		if (!attenuators.contains(bean)) attenuators.add(bean);
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public List<String> getDescriptions() {
		return Arrays.asList(description);
	}

	@Override
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getVfmx() {
		return vfmx;
	}

	public void setVfmx(double vfmx) {
		this.vfmx = vfmx;
	}

	public boolean isVfmxActive() {
		return vfmxActive;
	}

	public void setVfmxActive(boolean vfmxActive) {
		this.vfmxActive = vfmxActive;
	}

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
		result = prime * result + ((attenuators == null) ? 0 : attenuators.hashCode());
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((sampleStageParameters == null) ? 0 : sampleStageParameters.hashCode());
		long temp;
		temp = Double.doubleToLongBits(vfmx);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + (vfmxActive ? 1231 : 1237);
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
		I18SampleParameters other = (I18SampleParameters) obj;
		if (attenuators == null) {
			if (other.attenuators != null)
				return false;
		} else if (!attenuators.equals(other.attenuators))
			return false;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (sampleStageParameters == null) {
			if (other.sampleStageParameters != null)
				return false;
		} else if (!sampleStageParameters.equals(other.sampleStageParameters))
			return false;
		if (Double.doubleToLongBits(vfmx) != Double.doubleToLongBits(other.vfmx))
			return false;
		return vfmxActive == other.vfmxActive;
	}

}
