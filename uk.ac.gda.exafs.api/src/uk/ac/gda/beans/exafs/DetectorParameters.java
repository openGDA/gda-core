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

package uk.ac.gda.beans.exafs;

import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.beanutils.BeanUtils;

public class DetectorParameters implements Serializable, IDetectorParameters {

	static public final URL mappingURL = DetectorParameters.class.getResource("ExafsParameterMapping.xml");

	static public final URL schemaUrl = DetectorParameters.class.getResource("ExafsParameterMapping.xsd");

	static public final String TRANSMISSION_TYPE = "Transmission";
	static public final String FLUORESCENCE_TYPE = "Fluorescence";
	static public final String SOFTXRAYS_TYPE = "soft x-rays";
	static public final String XES_TYPE = "XES";

	private List<DetectorGroup> detectorGroups;
	private String experimentType;
	private boolean shouldValidate = true;
	private TransmissionParameters transmissionParameters;
	private FluorescenceParameters fluorescenceParameters;
	private FluorescenceParameters xesParameters;
	private SoftXRaysParameters softXRaysParameters;
	private ElectronYieldParameters electronYieldParameters;

	@Override
	public String getExperimentType() {
		return experimentType;
	}

	public void setExperimentType(String experimentType) {
		this.experimentType = experimentType;
	}

	public DetectorParameters() {
		super();
		this.detectorGroups = new ArrayList<DetectorGroup>(3);
	}

	@Override
	public TransmissionParameters getTransmissionParameters() {
		return transmissionParameters;
	}

	public void setTransmissionParameters(TransmissionParameters transmissionParameters) {
		this.transmissionParameters = transmissionParameters;
	}

	@Override
	public FluorescenceParameters getFluorescenceParameters() {
		return fluorescenceParameters;
	}

	public void setFluorescenceParameters(FluorescenceParameters fluorescenceParameters) {
		this.fluorescenceParameters = fluorescenceParameters;
	}

	@Override
	public FluorescenceParameters getXesParameters() {
		return xesParameters;
	}

	public void setXesParameters(FluorescenceParameters xesParameters) {
		this.xesParameters = xesParameters;
	}

	@Override
	public SoftXRaysParameters getSoftXRaysParameters() {
		return softXRaysParameters;
	}

	public void setSoftXRaysParameters(SoftXRaysParameters softXRaysParameters) {
		this.softXRaysParameters = softXRaysParameters;
	}

	@Override
	public ElectronYieldParameters getElectronYieldParameters() {
		return electronYieldParameters;
	}

	public void setElectronYieldParameters(ElectronYieldParameters electronYieldParameters) {
		this.electronYieldParameters = electronYieldParameters;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((detectorGroups == null) ? 0 : detectorGroups.hashCode());
		result = prime * result + ((experimentType == null) ? 0 : experimentType.hashCode());
		result = prime * result + ((fluorescenceParameters == null) ? 0 : fluorescenceParameters.hashCode());
		result = prime * result + ((softXRaysParameters == null) ? 0 : softXRaysParameters.hashCode());
		result = prime * result + (shouldValidate ? 1231 : 1237);
		result = prime * result + ((transmissionParameters == null) ? 0 : transmissionParameters.hashCode());
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
		DetectorParameters other = (DetectorParameters) obj;
		if (detectorGroups == null) {
			if (other.detectorGroups != null) {
				return false;
			}
		} else if (!detectorGroups.equals(other.detectorGroups)) {
			return false;
		}
		if (experimentType == null) {
			if (other.experimentType != null) {
				return false;
			}
		} else if (!experimentType.equals(other.experimentType)) {
			return false;
		}
		if (fluorescenceParameters == null) {
			if (other.fluorescenceParameters != null) {
				return false;
			}
		} else if (!fluorescenceParameters.equals(other.fluorescenceParameters)) {
			return false;
		}
		if (softXRaysParameters == null) {
			if (other.softXRaysParameters != null) {
				return false;
			}
		} else if (!softXRaysParameters.equals(other.softXRaysParameters)) {
			return false;
		}
		if (shouldValidate != other.shouldValidate) {
			return false;
		}
		if (transmissionParameters == null) {
			if (other.transmissionParameters != null) {
				return false;
			}
		} else if (!transmissionParameters.equals(other.transmissionParameters)) {
			return false;
		}
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

	@Override
	public List<DetectorGroup> getDetectorGroups() {
		return detectorGroups;
	}

	public void setDetectorGroups(List<DetectorGroup> detectorGroups) {
		this.detectorGroups = detectorGroups;
	}

	public void addDetectorGroup(DetectorGroup dg) {
		detectorGroups.add(dg);
	}

	public void clear() {
		if (detectorGroups!=null) detectorGroups.clear();
	}

	@Override
	public List<IonChamberParameters> getIonChambers() throws Exception {

		final List<IonChamberParameters> chambers;
		if (getExperimentType().equalsIgnoreCase(TRANSMISSION_TYPE)) {
			chambers = getTransmissionParameters().getIonChamberParameters();
		} else if (getExperimentType().equalsIgnoreCase(FLUORESCENCE_TYPE)) {
			chambers = getFluorescenceParameters().getIonChamberParameters();
		} else if (getExperimentType().equalsIgnoreCase(SOFTXRAYS_TYPE)) {
			chambers = Collections.emptyList();
		} else if (getExperimentType().equalsIgnoreCase(XES_TYPE)) {
			chambers = getXesParameters().getIonChamberParameters();
		} else {
			throw new Exception("Cannot determine detector parameters '" + getExperimentType() + "'.");
		}
		return chambers;
	}

	@Override
	public boolean isShouldValidate() {
		return shouldValidate;
	}

	public void setShouldValidate(boolean shouldValidate) {
		this.shouldValidate = shouldValidate;
	}

}
