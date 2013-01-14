/*-
 * Copyright © 2012 Diamond Light Source Ltd.
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

import org.apache.commons.beanutils.BeanUtils;

/**
 * Class to represent changeable parameters for a cryostat device.

 */
public class CryostatParameters implements Serializable {
		
	public static final String[] LOOP_OPTION = new String[]{"Loop over sample, then temperature", "Loop over temperature, then sample"};
	public static final String[] SAMPLE_HOLDER_OPTION = new String[]{"3 Samples", "Liquid Cell"};

	private Double  temperature;	// desired temp
	private Double  time;			// timeout while waiting for heat to reach desired value
	private Double  tolerance;		// temperature deadband (GDA-level concept, this is not in EPICS)
	private Double  p,i,d,ramp;		// ramp rate unused in UI
	private Integer heaterRange;	// power output 1-5
	
	// TODO add ramp rate, ramp enable, 

	private String  profileType;	// unused  TODO remove
	
	private String  sampleHolder;	// liquid cell or '4 samples' TODO stic final string[]
	private String  sampleNumbers = "";
	
	private Double  position1 = 0.0;
	private Double  finePosition1 = 0.0;
	private String  sampleDescription1 = "";
	
	private Double  position2 = 0.0;
	private Double  finePosition2 = 0.0;
	private String  sampleDescription2 = "";
	
	private Double  position3 = 0.0;
	private Double  finePosition3 = 0.0;
	private String  sampleDescription3 = "";
	
	private Double  position4 = 0.0;				// TODO remove 4th
	private Double  finePosition4 = 0.0;
	private String  sampleDescription4 = "";
	private String loopChoice;

	public Double getTemperature() {
		return temperature;
	}

	public void setTemperature(Double temperature) {
		this.temperature = temperature;
	}

	public Integer getHeaterRange() {
		return heaterRange;
	}

	public void setHeaterRange(Integer tolerance) {
		this.heaterRange = tolerance;
	}

	public Double getTime() {
		return time;
	}

	public void setTime(Double time) {
		this.time = time;
	}

	public String getSampleNumbers() {
		return sampleNumbers;
	}

	public void setSampleNumbers(String sampleNumbers) {
		this.sampleNumbers = sampleNumbers;
	}

	public String getProfileType() {
		return profileType;
	}

	public void setProfileType(String profileType) {
		this.profileType = profileType;
	}
	public Double getP() {
		return p;
	}
	public void setP(Double p) {
		this.p = p;
	}
	
	public Double getI() {
		return i;
	}

	public void setI(Double i) {
		this.i = i;
	}

	public Double getD() {
		return d;
	}

	public void setD(Double d) {
		this.d = d;
	}

	public Double getRamp() {
		return ramp;
	}

	public void setRamp(Double ramp) {
		this.ramp = ramp;
	}

	@Override
	public String toString() {
		try {
			return BeanUtils.describe(this).toString();
		} catch (Exception e) {
			return e.getMessage();
		}
	}

	public Double getTolerance() {
		return tolerance;
	}

	public void setTolerance(Double tolerance) {
		this.tolerance = tolerance;
	}

	public String getSampleHolder() {
		return sampleHolder;
	}

	public void setSampleHolder(String sampleHolder) {
		this.sampleHolder = sampleHolder;
	}

	public Double getPosition1() {
		return position1;
	}

	public void setPosition1(Double position1) {
		this.position1 = position1;
	}

	public Double getFinePosition1() {
		return finePosition1;
	}

	public void setFinePosition1(Double finePosition1) {
		this.finePosition1 = finePosition1;
	}

	public String getSampleDescription1() {
		return sampleDescription1;
	}

	public void setSampleDescription1(String sampleDescription1) {
		this.sampleDescription1 = sampleDescription1;
	}

	public Double getPosition2() {
		return position2;
	}

	public void setPosition2(Double position2) {
		this.position2 = position2;
	}

	public Double getFinePosition2() {
		return finePosition2;
	}

	public void setFinePosition2(Double finePosition2) {
		this.finePosition2 = finePosition2;
	}

	public String getSampleDescription2() {
		return sampleDescription2;
	}

	public void setSampleDescription2(String sampleDescription2) {
		this.sampleDescription2 = sampleDescription2;
	}

	public Double getPosition3() {
		return position3;
	}

	public void setPosition3(Double position3) {
		this.position3 = position3;
	}

	public Double getFinePosition3() {
		return finePosition3;
	}

	public void setFinePosition3(Double finePosition3) {
		this.finePosition3 = finePosition3;
	}

	public String getSampleDescription3() {
		return sampleDescription3;
	}

	public void setSampleDescription3(String sampleDescription3) {
		this.sampleDescription3 = sampleDescription3;
	}

	public Double getPosition4() {
		return position4;
	}

	public void setPosition4(Double position4) {
		this.position4 = position4;
	}

	public Double getFinePosition4() {
		return finePosition4;
	}

	public void setFinePosition4(Double finePosition4) {
		this.finePosition4 = finePosition4;
	}

	public String getSampleDescription4() {
		return sampleDescription4;
	}

	public void setSampleDescription4(String sampleDescription4) {
		this.sampleDescription4 = sampleDescription4;
	}

	public String getLoopChoice() {
		return loopChoice;
	}

	public void setLoopChoice(String loopChoice) {
		this.loopChoice = loopChoice;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((d == null) ? 0 : d.hashCode());
		result = prime * result + ((finePosition1 == null) ? 0 : finePosition1.hashCode());
		result = prime * result + ((finePosition2 == null) ? 0 : finePosition2.hashCode());
		result = prime * result + ((finePosition3 == null) ? 0 : finePosition3.hashCode());
		result = prime * result + ((finePosition4 == null) ? 0 : finePosition4.hashCode());
		result = prime * result + ((heaterRange == null) ? 0 : heaterRange.hashCode());
		result = prime * result + ((i == null) ? 0 : i.hashCode());
		result = prime * result + ((loopChoice == null) ? 0 : loopChoice.hashCode());
		result = prime * result + ((p == null) ? 0 : p.hashCode());
		result = prime * result + ((position1 == null) ? 0 : position1.hashCode());
		result = prime * result + ((position2 == null) ? 0 : position2.hashCode());
		result = prime * result + ((position3 == null) ? 0 : position3.hashCode());
		result = prime * result + ((position4 == null) ? 0 : position4.hashCode());
		result = prime * result + ((profileType == null) ? 0 : profileType.hashCode());
		result = prime * result + ((ramp == null) ? 0 : ramp.hashCode());
		result = prime * result + ((sampleDescription1 == null) ? 0 : sampleDescription1.hashCode());
		result = prime * result + ((sampleDescription2 == null) ? 0 : sampleDescription2.hashCode());
		result = prime * result + ((sampleDescription3 == null) ? 0 : sampleDescription3.hashCode());
		result = prime * result + ((sampleDescription4 == null) ? 0 : sampleDescription4.hashCode());
		result = prime * result + ((sampleHolder == null) ? 0 : sampleHolder.hashCode());
		result = prime * result + ((sampleNumbers == null) ? 0 : sampleNumbers.hashCode());
		result = prime * result + ((temperature == null) ? 0 : temperature.hashCode());
		result = prime * result + ((time == null) ? 0 : time.hashCode());
		result = prime * result + ((tolerance == null) ? 0 : tolerance.hashCode());
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
		CryostatParameters other = (CryostatParameters) obj;
		if (d == null) {
			if (other.d != null)
				return false;
		} else if (!d.equals(other.d))
			return false;
		if (finePosition1 == null) {
			if (other.finePosition1 != null)
				return false;
		} else if (!finePosition1.equals(other.finePosition1))
			return false;
		if (finePosition2 == null) {
			if (other.finePosition2 != null)
				return false;
		} else if (!finePosition2.equals(other.finePosition2))
			return false;
		if (finePosition3 == null) {
			if (other.finePosition3 != null)
				return false;
		} else if (!finePosition3.equals(other.finePosition3))
			return false;
		if (finePosition4 == null) {
			if (other.finePosition4 != null)
				return false;
		} else if (!finePosition4.equals(other.finePosition4))
			return false;
		if (heaterRange == null) {
			if (other.heaterRange != null)
				return false;
		} else if (!heaterRange.equals(other.heaterRange))
			return false;
		if (i == null) {
			if (other.i != null)
				return false;
		} else if (!i.equals(other.i))
			return false;
		if (loopChoice == null) {
			if (other.loopChoice != null)
				return false;
		} else if (!loopChoice.equals(other.loopChoice))
			return false;
		if (p == null) {
			if (other.p != null)
				return false;
		} else if (!p.equals(other.p))
			return false;
		if (position1 == null) {
			if (other.position1 != null)
				return false;
		} else if (!position1.equals(other.position1))
			return false;
		if (position2 == null) {
			if (other.position2 != null)
				return false;
		} else if (!position2.equals(other.position2))
			return false;
		if (position3 == null) {
			if (other.position3 != null)
				return false;
		} else if (!position3.equals(other.position3))
			return false;
		if (position4 == null) {
			if (other.position4 != null)
				return false;
		} else if (!position4.equals(other.position4))
			return false;
		if (profileType == null) {
			if (other.profileType != null)
				return false;
		} else if (!profileType.equals(other.profileType))
			return false;
		if (ramp == null) {
			if (other.ramp != null)
				return false;
		} else if (!ramp.equals(other.ramp))
			return false;
		if (sampleDescription1 == null) {
			if (other.sampleDescription1 != null)
				return false;
		} else if (!sampleDescription1.equals(other.sampleDescription1))
			return false;
		if (sampleDescription2 == null) {
			if (other.sampleDescription2 != null)
				return false;
		} else if (!sampleDescription2.equals(other.sampleDescription2))
			return false;
		if (sampleDescription3 == null) {
			if (other.sampleDescription3 != null)
				return false;
		} else if (!sampleDescription3.equals(other.sampleDescription3))
			return false;
		if (sampleDescription4 == null) {
			if (other.sampleDescription4 != null)
				return false;
		} else if (!sampleDescription4.equals(other.sampleDescription4))
			return false;
		if (sampleHolder == null) {
			if (other.sampleHolder != null)
				return false;
		} else if (!sampleHolder.equals(other.sampleHolder))
			return false;
		if (sampleNumbers == null) {
			if (other.sampleNumbers != null)
				return false;
		} else if (!sampleNumbers.equals(other.sampleNumbers))
			return false;
		if (temperature == null) {
			if (other.temperature != null)
				return false;
		} else if (!temperature.equals(other.temperature))
			return false;
		if (time == null) {
			if (other.time != null)
				return false;
		} else if (!time.equals(other.time))
			return false;
		if (tolerance == null) {
			if (other.tolerance != null)
				return false;
		} else if (!tolerance.equals(other.tolerance))
			return false;
		return true;
	}

}
