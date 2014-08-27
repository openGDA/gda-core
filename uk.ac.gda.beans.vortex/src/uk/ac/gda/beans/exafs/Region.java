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

import org.apache.commons.beanutils.BeanUtils;

/**
 * @author Matthew Gerring
 */
public class Region  implements Serializable{

	private Double energy, step, time;
	private boolean variableStep = false;

	/**
	 * @return the variableStep
	 */
	public boolean isVariableStep() {
		return variableStep;
	}

	/**
	 * @param variableStep
	 *            the variableStep to set
	 */
	public void setVariableStep(boolean variableStep) {
		this.variableStep = variableStep;
	}

	/**
	 * @return the energy
	 */
	public Double getEnergy() {
		return energy;
	}

	/**
	 * @param energy the energy to set
	 */
	public void setEnergy(Double energy) {
		this.energy = energy;
	}

	/**
	 * Used for beans
	 */
	public Region() {

	}

	/**
	 * Testing
	 * 
	 * @param args
	 * @throws Exception
	 */
	public Region(Double... args) throws Exception {
		if (args.length != 3) {
			throw new Exception("Invalid argument list, must be four; startEnergy,stopEnergy,step,time");
		}
		energy = args[0];
		step = args[1];
		time = args[2];
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((energy == null) ? 0 : energy.hashCode());
		result = prime * result + ((step == null) ? 0 : step.hashCode());
		result = prime * result + ((time == null) ? 0 : time.hashCode());
		result = prime * result + (variableStep ? 1231 : 1237);
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
		Region other = (Region) obj;
		if (energy == null) {
			if (other.energy != null) {
				return false;
			}
		} else if (!energy.equals(other.energy)) {
			return false;
		}
		if (step == null) {
			if (other.step != null) {
				return false;
			}
		} else if (!step.equals(other.step)) {
			return false;
		}
		if (time == null) {
			if (other.time != null) {
				return false;
			}
		} else if (!time.equals(other.time)) {
			return false;
		}
		if (variableStep != other.variableStep) {
			return false;
		}
		return true;
	}

	/**
	 * @return step increment
	 */
	public Double getStep() {
		return step;
	}

	/**
	 * @param step
	 */
	public void setStep(Double step) {
		this.step = step;
	}

	/**
	 * @return time per point
	 */
	public Double getTime() {
		return time;
	}

	/**
	 * @param time
	 */
	public void setTime(Double time) {
		this.time = time;
	}

	/**
	 * {inheritDoc}
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		try {
			return BeanUtils.describe(this).toString();
		} catch (Exception e) {
			return e.getMessage();
		}
	}

}
