/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council Daresbury Laboratory
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

package gda.device;

import org.apache.commons.beanutils.BeanUtils;

/**
 * A class that defines a temperature ramp.
 */
public class TemperatureRamp {

	private Integer rampNumber;
	private Double startTemperature = 20.0;
	private Double endTemperature = 25.0;
	private Double rate = 1.0;
	private Double dwellTime = 1.0;
	private Integer coolingSpeed = 0;

	/**
	 * default constructor for castor
	 */
	public TemperatureRamp() {
	}
	
	/**
	 * Constructor to create a ramp with default data
	 * 
	 * @param rampNumber
	 *            the ramp number, starting with 0
	 */
	public TemperatureRamp(int rampNumber) {
		this.rampNumber = rampNumber;
	}

	/**
	 * Constructor to create a temperature ramp
	 * 
	 * @param rampNumber
	 *            the ramp number
	 * @param startTemperature
	 *            the start temperature (assuming Celsius)
	 * @param endTemperature
	 *            the end temperature (assuming Celsius)
	 * @param rate
	 *            the rate (assuming Celsius / s)
	 * @param dwellTime
	 *            the dwell time (assuming s)
	 * @param coolingSpeed
	 *            the cooling speed (could be Celsius / s)
	 */
	public TemperatureRamp(int rampNumber, double startTemperature, double endTemperature, double rate,
			double dwellTime, int coolingSpeed) {
		this.rampNumber = rampNumber;
		this.startTemperature = startTemperature;
		this.endTemperature = endTemperature;
		this.rate = rate;
		this.dwellTime = dwellTime;
		this.coolingSpeed = coolingSpeed;
	}

	/**
	 * Make an exact copy of an existing temperature ramp
	 * 
	 * @return a duplicate temperature ramp
	 */
	public TemperatureRamp copy() {
		return new TemperatureRamp(rampNumber, startTemperature, endTemperature, rate, dwellTime, coolingSpeed);
	}

	/**
	 * Get the ramp number 0 to n
	 * 
	 * @return the ramp number
	 */
	public Integer getRampNumber() {
		return rampNumber;
	}

	/**
	 * Get the starting temperature for a particular ramp
	 * 
	 * @return the start temperature
	 */
	public Double getStartTemperature() {
		return startTemperature;
	}

	/**
	 * Get the ending temperature for a particular ramp
	 * 
	 * @return the end temperature
	 */
	public Double getEndTemperature() {
		return endTemperature;
	}

	/**
	 * Get the rate of change in temperature for a particular ramp
	 * 
	 * @return rate of temperature change
	 */
	public Double getRate() {
		return rate;
	}

	/**
	 * Get the time to dwell and the ending temperature before proceeding to the next ramp
	 * 
	 * @return the dwell time
	 */
	public Double getDwellTime() {
		return dwellTime;
	}

	/**
	 * Get the cooling speed setting for a cooling ramp
	 * 
	 * @return the cooling speed
	 */
	public Integer getCoolingSpeed() {
		return coolingSpeed;
	}

	/**
	 * Set the ramp number
	 * 
	 * @param rampNumber
	 *            the ramp number 0 to n
	 */
	public void setRampNumber(Integer rampNumber) {
		this.rampNumber = rampNumber;
	}

	/**
	 * Set the starting temperature for a particular ramp.
	 * 
	 * @param startTemperature
	 *            the start temperature
	 */
	public void setStartTemperature(Double startTemperature) {
		this.startTemperature = startTemperature;
	}

	/**
	 * Set the ending temperature for a particular ramp.
	 * 
	 * @param endTemperature
	 *            the end temperature
	 */
	public void setEndTemperature(Double endTemperature) {
		this.endTemperature = endTemperature;
	}

	/**
	 * set the rate of change in temperature for a particular ramp
	 * 
	 * @param rate
	 *            ramp rate
	 */
	public void setRate(Double rate) {
		this.rate = rate;
	}

	/**
	 * Set the time to dwell at the ending temperature before proceeding to the next ramp
	 * 
	 * @param dwellTime
	 *            the dwell time
	 */
	public void setDwellTime(Double dwellTime) {
		this.dwellTime = dwellTime;
	}

	/**
	 * Set the cooling speed setting for a cooling ramp
	 * 
	 * @param coolingSpeed
	 *            the cooling speed
	 */
	public void setCoolingSpeed(Integer coolingSpeed) {
		this.coolingSpeed = coolingSpeed;
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
		result = prime * result + ((rampNumber == null) ? 0 : rampNumber.hashCode());
		result = prime * result + ((startTemperature == null) ? 0 : startTemperature.hashCode());
		result = prime * result + ((endTemperature == null) ? 0 : endTemperature.hashCode());
		result = prime * result + ((rate == null) ? 0 : rate.hashCode());
		result = prime * result + ((dwellTime == null) ? 0 : dwellTime.hashCode());
		result = prime * result + ((coolingSpeed == null) ? 0 : coolingSpeed.hashCode());
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
		TemperatureRamp other = (TemperatureRamp) obj;
		if (rampNumber == null) {
			if (other.rampNumber != null) {
				return false;
			}
		} else if (!rampNumber.equals(other.rampNumber)) {
			return false;
		}
		if (startTemperature == null) {
			if (other.startTemperature != null) {
				return false;
			}
		} else if (!startTemperature.equals(other.startTemperature)) {
			return false;
		}
		if (endTemperature == null) {
			if (other.endTemperature != null) {
				return false;
			}
		} else if (!endTemperature.equals(other.endTemperature)) {
			return false;
		}
		if (rate == null) {
			if (other.rate != null) {
				return false;
			}
		} else if (!rate.equals(other.rate)) {
			return false;
		}
		if (dwellTime == null) {
			if (other.dwellTime != null) {
				return false;
			}
		} else if (!dwellTime.equals(other.dwellTime)) {
			return false;
		}		
		if (coolingSpeed == null) {
			if (other.coolingSpeed != null) {
				return false;
			}
		} else if (!coolingSpeed.equals(other.coolingSpeed)) {
			return false;
		}		
		return true;
	}
}
