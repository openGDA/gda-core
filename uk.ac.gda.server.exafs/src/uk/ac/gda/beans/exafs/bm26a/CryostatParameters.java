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

import org.apache.commons.beanutils.BeanUtils;

import uk.ac.gda.doe.DOEField;

/**
 * Class to represent changeable parameters for a cryostat device.
 *
 */
public class CryostatParameters implements Serializable {
		
	@DOEField(10)
	private String  temperature;
	private Integer heaterRange;
	private Double  time;
	private String  profileType;
	private Double  p,i,d,ramp;
	private Double  tolerance;
	private String  sampleHolder;
	
	/**
	 * NOTE the field sampleNumber was an integer and should be marked
	 * using the optional type argument as follows:
	 */
	@DOEField(value=4, type=Integer.class)
	private String sampleNumber;
	
	@DOEField(3)
	private String  position;
	@DOEField(2)
	private String  finePosition;

	/**
	 * @return the temperature
	 */
	public String getTemperature() {
		return temperature;
	}

	/**
	 * @param temperature
	 *            the temperature to set
	 */
	public void setTemperature(String temperature) {
		this.temperature = temperature;
	}

	/**
	 * @return the tolerance
	 */
	public Integer getHeaterRange() {
		return heaterRange;
	}

	/**
	 * @param tolerance
	 *            the tolerance to set
	 */
	public void setHeaterRange(Integer tolerance) {
		this.heaterRange = tolerance;
	}

	/**
	 * @return the time
	 */
	public Double getTime() {
		return time;
	}

	/**
	 * @param time
	 *            the time to set
	 */
	public void setTime(Double time) {
		this.time = time;
	}

	/**
	 * @return the samplePosition
	 */
	public String getSampleNumber() {
		return sampleNumber;
	}

	/**
	 * @param samplePosition
	 *            the samplePosition to set
	 */
	public void setSampleNumber(String samplePosition) {
		this.sampleNumber = samplePosition;
	}

	/**
	 * @return Returns the profileType.
	 */
	public String getProfileType() {
		return profileType;
	}

	/**
	 * @param profileType The profileType to set.
	 */
	public void setProfileType(String profileType) {
		this.profileType = profileType;
	}

	/**
	 * @return Returns the p.
	 */
	public Double getP() {
		return p;
	}

	/**
	 * @param p The p to set.
	 */
	public void setP(Double p) {
		this.p = p;
	}

	/**
	 * @return Returns the i.
	 */
	public Double getI() {
		return i;
	}

	/**
	 * @param i The i to set.
	 */
	public void setI(Double i) {
		this.i = i;
	}

	/**
	 * @return Returns the d.
	 */
	public Double getD() {
		return d;
	}

	/**
	 * @param d The d to set.
	 */
	public void setD(Double d) {
		this.d = d;
	}

	/**
	 * @return Returns the ramp.
	 */
	public Double getRamp() {
		return ramp;
	}

	/**
	 * @param ramp The ramp to set.
	 */
	public void setRamp(Double ramp) {
		this.ramp = ramp;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((d == null) ? 0 : d.hashCode());
		result = prime * result
				+ ((finePosition == null) ? 0 : finePosition.hashCode());
		result = prime * result
				+ ((heaterRange == null) ? 0 : heaterRange.hashCode());
		result = prime * result + ((i == null) ? 0 : i.hashCode());
		result = prime * result + ((p == null) ? 0 : p.hashCode());
		result = prime * result
				+ ((position == null) ? 0 : position.hashCode());
		result = prime * result
				+ ((profileType == null) ? 0 : profileType.hashCode());
		result = prime * result + ((ramp == null) ? 0 : ramp.hashCode());
		result = prime * result
				+ ((sampleHolder == null) ? 0 : sampleHolder.hashCode());
		result = prime * result
				+ ((sampleNumber == null) ? 0 : sampleNumber.hashCode());
		result = prime * result
				+ ((temperature == null) ? 0 : temperature.hashCode());
		result = prime * result + ((time == null) ? 0 : time.hashCode());
		result = prime * result
				+ ((tolerance == null) ? 0 : tolerance.hashCode());
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
		CryostatParameters other = (CryostatParameters) obj;
		if (d == null) {
			if (other.d != null) {
				return false;
			}
		} else if (!d.equals(other.d)) {
			return false;
		}
		if (finePosition == null) {
			if (other.finePosition != null) {
				return false;
			}
		} else if (!finePosition.equals(other.finePosition)) {
			return false;
		}
		if (heaterRange == null) {
			if (other.heaterRange != null) {
				return false;
			}
		} else if (!heaterRange.equals(other.heaterRange)) {
			return false;
		}
		if (i == null) {
			if (other.i != null) {
				return false;
			}
		} else if (!i.equals(other.i)) {
			return false;
		}
		if (p == null) {
			if (other.p != null) {
				return false;
			}
		} else if (!p.equals(other.p)) {
			return false;
		}
		if (position == null) {
			if (other.position != null) {
				return false;
			}
		} else if (!position.equals(other.position)) {
			return false;
		}
		if (profileType == null) {
			if (other.profileType != null) {
				return false;
			}
		} else if (!profileType.equals(other.profileType)) {
			return false;
		}
		if (ramp == null) {
			if (other.ramp != null) {
				return false;
			}
		} else if (!ramp.equals(other.ramp)) {
			return false;
		}
		if (sampleHolder == null) {
			if (other.sampleHolder != null) {
				return false;
			}
		} else if (!sampleHolder.equals(other.sampleHolder)) {
			return false;
		}
		if (sampleNumber == null) {
			if (other.sampleNumber != null) {
				return false;
			}
		} else if (!sampleNumber.equals(other.sampleNumber)) {
			return false;
		}
		if (temperature == null) {
			if (other.temperature != null) {
				return false;
			}
		} else if (!temperature.equals(other.temperature)) {
			return false;
		}
		if (time == null) {
			if (other.time != null) {
				return false;
			}
		} else if (!time.equals(other.time)) {
			return false;
		}
		if (tolerance == null) {
			if (other.tolerance != null) {
				return false;
			}
		} else if (!tolerance.equals(other.tolerance)) {
			return false;
		}
		return true;
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

	/**
	 * @return Returns the tolerance.
	 */
	public Double getTolerance() {
		return tolerance;
	}

	/**
	 * @param tolerance The tolerance to set.
	 */
	public void setTolerance(Double tolerance) {
		this.tolerance = tolerance;
	}

	/**
	 * @return Returns the sampleHolder.
	 */
	public String getSampleHolder() {
		return sampleHolder;
	}

	/**
	 * @param sampleHolder The sampleHolder to set.
	 */
	public void setSampleHolder(String sampleHolder) {
		this.sampleHolder = sampleHolder;
	}

	/**
	 * @return Returns the position.
	 */
	public String getPosition() {
		return position;
	}

	/**
	 * @param position The position to set.
	 */
	public void setPosition(String position) {
		this.position = position;
	}

	/**
	 * @return Returns the finePosition.
	 */
	public String getFinePosition() {
		return finePosition;
	}

	/**
	 * @param finePosition The finePosition to set.
	 */
	public void setFinePosition(String finePosition) {
		this.finePosition = finePosition;
	}

}
