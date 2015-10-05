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
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.beanutils.BeanUtils;

public class CryostatParameters implements Serializable {
	private static final long serialVersionUID = -1237848111297463940L;
	private String loopChoice;
	private Double tolerance; // temperature deadband (GDA-level concept, this is not in EPICS)
	private Integer waitTime = 0; // timeout while waiting for heat to reach desired value
	private String desiredTemperature = "";
	private String controlMode = "";
	private String heaterRange = "";
	private Double p, i, d, manualOutput;
	private List<CryostatSampleDetails> samples = new ArrayList<CryostatSampleDetails>();

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

	public Integer getWaitTime() {
		return waitTime;
	}

	public void setWaitTime(Integer waitTime) {
		this.waitTime = waitTime;
	}

	public String getTemperature() {
		return desiredTemperature;
	}

	public void setTemperature(String temperature) {
		this.desiredTemperature = temperature;
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

	public List<CryostatSampleDetails> getSamples() {
		return samples;
	}

	public void setSamples(List<CryostatSampleDetails> samples) {
		this.samples = samples;
	}

	public void addSample(CryostatSampleDetails sample) {
		samples.add(sample);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((controlMode == null) ? 0 : controlMode.hashCode());
		result = prime * result + ((d == null) ? 0 : d.hashCode());
		result = prime * result + ((heaterRange == null) ? 0 : heaterRange.hashCode());
		result = prime * result + ((i == null) ? 0 : i.hashCode());
		result = prime * result + ((loopChoice == null) ? 0 : loopChoice.hashCode());
		result = prime * result + ((manualOutput == null) ? 0 : manualOutput.hashCode());
		result = prime * result + ((p == null) ? 0 : p.hashCode());
		result = prime * result + ((samples == null) ? 0 : samples.hashCode());
		result = prime * result + ((desiredTemperature == null) ? 0 : desiredTemperature.hashCode());
		result = prime * result + ((tolerance == null) ? 0 : tolerance.hashCode());
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
		if (samples == null) {
			if (other.samples != null)
				return false;
		} else if (!samples.equals(other.samples))
			return false;
		if (desiredTemperature == null) {
			if (other.desiredTemperature != null)
				return false;
		} else if (!desiredTemperature.equals(other.desiredTemperature))
			return false;
		if (tolerance == null) {
			if (other.tolerance != null)
				return false;
		} else if (!tolerance.equals(other.tolerance))
			return false;
		if (waitTime == null) {
			if (other.waitTime != null)
				return false;
		} else if (!waitTime.equals(other.waitTime))
			return false;
		return true;
	}

}