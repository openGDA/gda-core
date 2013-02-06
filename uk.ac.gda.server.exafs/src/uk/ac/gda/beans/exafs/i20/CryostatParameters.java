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

import org.apache.commons.beanutils.BeanUtils;

/**
 * Class to represent changeable parameters for a cryostat device.
 */
public class CryostatParameters implements Serializable {

	public static final String[] LOOP_OPTION = new String[] { "Loop over sample, then temperature",
			"Loop over temperature, then sample" };
	public static final String[] CONTROL_MODE = new String[] { "Manual PID", "Zone control", "Open Loop",
			"Auto-tune PID" };
	public static final String[] HEATER_RANGE = new String[] { "4mW", "40mW", "400mW", "4W", "40W" };

	private String loopChoice;

	private Double tolerance; // temperature deadband (GDA-level concept, this is not in EPICS)
	private Double waitTime; // timeout while waiting for heat to reach desired value
	private String temperature = ""; // desired temp

	private String controlMode = "";
	private String heaterRange = "";
	private Double p, i, d, manualOutput;

	private Boolean useSample1 = false;
	private Double position1 = 0.0;
	private Double finePosition1 = 0.0;
	private String sampleDescription1 = "";

	private Boolean useSample2 = false;
	private Double position2 = 0.0;
	private Double finePosition2 = 0.0;
	private String sampleDescription2 = "";

	private Boolean useSample3 = false;
	private Double position3 = 0.0;
	private Double finePosition3 = 0.0;
	private String sampleDescription3 = "";
	
	/*
	 * useful methods when used in scripts
	 */
	
	public Boolean[] getUses() {
		return new Boolean[]{useSample1,useSample2,useSample3};
	}

	public Double[] getYs() {
		return new Double[] { position1, position2, position3 };
	}

	public Double[] getFinePositions() {
		return new Double[] { finePosition3, finePosition3, finePosition3 };
	}

	public String[] getSampleDescriptions() {
		return new String[] { sampleDescription1, sampleDescription2, sampleDescription3 };
	}

	@Override
	public String toString() {
		try {
			return BeanUtils.describe(this).toString();
		} catch (Exception e) {
			return e.getMessage();
		}
	}

	public String getLoopChoice() {
		return loopChoice;
	}

	public void setLoopChoice(String loopChoice) {
		this.loopChoice = loopChoice;
	}

	public Double getTolerance() {
		return tolerance;
	}

	public void setTolerance(Double tolerance) {
		this.tolerance = tolerance;
	}

	public Double getWaitTime() {
		return waitTime;
	}

	public void setWaitTime(Double waitTime) {
		this.waitTime = waitTime;
	}

	public String getTemperature() {
		return temperature;
	}

	public void setTemperature(String temperature) {
		this.temperature = temperature;
	}

	public String getControlMode() {
		return controlMode;
	}

	public void setControlMode(String controlMode) {
		this.controlMode = controlMode;
	}

	public String getHeaterRange() {
		return heaterRange;
	}

	public void setHeaterRange(String heaterRange) {
		this.heaterRange = heaterRange;
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

	public Double getManualOutput() {
		return manualOutput;
	}

	public void setManualOutput(Double manualOutput) {
		this.manualOutput = manualOutput;
	}

	public Boolean getUseSample1() {
		return useSample1;
	}

	public void setUseSample1(Boolean useSample1) {
		this.useSample1 = useSample1;
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

	public Boolean getUseSample2() {
		return useSample2;
	}

	public void setUseSample2(Boolean useSample2) {
		this.useSample2 = useSample2;
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

	public Boolean getUseSample3() {
		return useSample3;
	}

	public void setUseSample3(Boolean useSample3) {
		this.useSample3 = useSample3;
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((controlMode == null) ? 0 : controlMode.hashCode());
		result = prime * result + ((d == null) ? 0 : d.hashCode());
		result = prime * result + ((finePosition1 == null) ? 0 : finePosition1.hashCode());
		result = prime * result + ((finePosition2 == null) ? 0 : finePosition2.hashCode());
		result = prime * result + ((finePosition3 == null) ? 0 : finePosition3.hashCode());
		result = prime * result + ((heaterRange == null) ? 0 : heaterRange.hashCode());
		result = prime * result + ((i == null) ? 0 : i.hashCode());
		result = prime * result + ((loopChoice == null) ? 0 : loopChoice.hashCode());
		result = prime * result + ((manualOutput == null) ? 0 : manualOutput.hashCode());
		result = prime * result + ((p == null) ? 0 : p.hashCode());
		result = prime * result + ((position1 == null) ? 0 : position1.hashCode());
		result = prime * result + ((position2 == null) ? 0 : position2.hashCode());
		result = prime * result + ((position3 == null) ? 0 : position3.hashCode());
		result = prime * result + ((sampleDescription1 == null) ? 0 : sampleDescription1.hashCode());
		result = prime * result + ((sampleDescription2 == null) ? 0 : sampleDescription2.hashCode());
		result = prime * result + ((sampleDescription3 == null) ? 0 : sampleDescription3.hashCode());
		result = prime * result + ((temperature == null) ? 0 : temperature.hashCode());
		result = prime * result + ((tolerance == null) ? 0 : tolerance.hashCode());
		result = prime * result + ((useSample1 == null) ? 0 : useSample1.hashCode());
		result = prime * result + ((useSample2 == null) ? 0 : useSample2.hashCode());
		result = prime * result + ((useSample3 == null) ? 0 : useSample3.hashCode());
		result = prime * result + ((waitTime == null) ? 0 : waitTime.hashCode());
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
		if (controlMode == null) {
			if (other.controlMode != null)
				return false;
		} else if (!controlMode.equals(other.controlMode))
			return false;
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
		if (manualOutput == null) {
			if (other.manualOutput != null)
				return false;
		} else if (!manualOutput.equals(other.manualOutput))
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
		if (temperature == null) {
			if (other.temperature != null)
				return false;
		} else if (!temperature.equals(other.temperature))
			return false;
		if (tolerance == null) {
			if (other.tolerance != null)
				return false;
		} else if (!tolerance.equals(other.tolerance))
			return false;
		if (useSample1 == null) {
			if (other.useSample1 != null)
				return false;
		} else if (!useSample1.equals(other.useSample1))
			return false;
		if (useSample2 == null) {
			if (other.useSample2 != null)
				return false;
		} else if (!useSample2.equals(other.useSample2))
			return false;
		if (useSample3 == null) {
			if (other.useSample3 != null)
				return false;
		} else if (!useSample3.equals(other.useSample3))
			return false;
		if (waitTime == null) {
			if (other.waitTime != null)
				return false;
		} else if (!waitTime.equals(other.waitTime))
			return false;
		return true;
	}

}