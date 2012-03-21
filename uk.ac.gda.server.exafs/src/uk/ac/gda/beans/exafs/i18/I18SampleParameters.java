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

import java.io.Serializable;
import java.net.URL;
import java.util.List;
import java.util.Arrays;

import org.apache.commons.beanutils.BeanUtils;

import uk.ac.gda.beans.exafs.ISampleParameters;
import uk.ac.gda.util.beans.xml.XMLHelpers;

public class I18SampleParameters implements Serializable, ISampleParameters {
	private String name = "";
	private SampleStageParameters sampleStageParameters;
	private AttenuatorParameters attenuatorParameter1;
	private AttenuatorParameters attenuatorParameter2;
	private String description="";

	static public final URL mappingURL = I18SampleParameters.class.getResource("I18SampleParametersMapping.xml");

	static public final URL schemaURL = I18SampleParameters.class.getResource("I18SampleParametersMapping.xsd");

	public I18SampleParameters() {
	}

	public static I18SampleParameters createFromXML(String filename) throws Exception {
		return (I18SampleParameters) XMLHelpers.createFromXML(mappingURL, I18SampleParameters.class, schemaURL,
				filename);
	}

	public static void writeToXML(I18SampleParameters sampleParameters, String filename) throws Exception {
		XMLHelpers.writeToXML(mappingURL, sampleParameters, filename);
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
		I18SampleParameters other = (I18SampleParameters) obj;
		if (sampleStageParameters == null) {
			if (other.sampleStageParameters != null) {
				return false;
			}
		} else if (!sampleStageParameters.equals(other.sampleStageParameters)) {
			return false;
		}
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (attenuatorParameter1 == null) {
			if (other.attenuatorParameter1 != null) {
				return false;
			}
		} else if (!attenuatorParameter1.equals(other.attenuatorParameter1)) {
			return false;
		}
		if (other.attenuatorParameter2 != null) {
			return false;
		} else if (!attenuatorParameter2.equals(other.attenuatorParameter2)) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((attenuatorParameter1 == null) ? 0 : attenuatorParameter1.hashCode());
		result = prime * result + ((attenuatorParameter2 == null) ? 0 : attenuatorParameter2.hashCode());
		result = prime * result + ((sampleStageParameters == null) ? 0 : sampleStageParameters.hashCode());
		return result;
	}

	public void setSampleStageParameters(SampleStageParameters sampleStageParameters) {
		this.sampleStageParameters = sampleStageParameters;
	}

	public SampleStageParameters getSampleStageParameters() {
		return sampleStageParameters;
	}

	public void setAttenuatorParameter1(AttenuatorParameters atp) {
		this.attenuatorParameter1 = atp;
	}

	public AttenuatorParameters getAttenuatorParameter1() {
		return this.attenuatorParameter1;
	}

	public void setAttenuatorParameter2(AttenuatorParameters atp) {
		this.attenuatorParameter2 = atp;
	}

	public AttenuatorParameters getAttenuatorParameter2() {
		return this.attenuatorParameter2;
	}

	@Override
	public void clear() {
		this.attenuatorParameter1 = null;
		this.attenuatorParameter2 = null;
	}
	public String getDescription() { 
		return description; 
    } 

	public void setDescription(String description) { 
		this.description = description; 
	 } 

	@Override
	public List<String> getDescriptions() {
		return Arrays.asList(new String[] { description }); 
	}

	@Override
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
