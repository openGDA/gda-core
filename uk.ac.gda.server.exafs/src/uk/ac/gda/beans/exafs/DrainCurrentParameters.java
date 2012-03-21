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

import gda.configuration.properties.LocalProperties;

import java.io.Serializable;

import org.apache.commons.beanutils.BeanUtils;

/**
 *
 */
public class DrainCurrentParameters  implements Serializable{
	
	private String  name;
	private String  deviceName=LocalProperties.get("gda.exafs.ionchambersName","counterTimer01");
	private Integer channel=1;
	private String  currentAmplifierName="Keithley";
	private String  gain;
	private Boolean changeSensitivity = false;
	
	// Not persisted but used in pressure calculation.
	private Double  workingEnergy;
 
	/**
	 * @return Returns the workingEnergy.
	 */
	public Double getWorkingEnergy() {
		return workingEnergy;
	}

	/**
	 * @param workingEnergy The workingEnergy to set.
	 */
	public void setWorkingEnergy(Double workingEnergy) {
		this.workingEnergy = workingEnergy;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the deviceName
	 */
	public String getDeviceName() {
		return deviceName;
	}

	/**
	 * @param deviceName
	 *            the deviceName to set
	 */
	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}

	/**
	 * @return the channel
	 */
	public Integer getChannel() {
		return channel;
	}

	/**
	 * @param channel
	 *            the channel to set
	 */
	public void setChannel(Integer channel) {
		this.channel = channel;
	}

	/**
	 * @return the currentAmplifierName
	 */
	public String getCurrentAmplifierName() {
		return currentAmplifierName;
	}

	/**
	 * @param currentAmplifierName
	 *            the currentAmplifierName to set
	 */
	public void setCurrentAmplifierName(String currentAmplifierName) {
		this.currentAmplifierName = currentAmplifierName;
	}

	/**
	 * @return the gain
	 */
	public String getGain() {
		return gain;
	}

	/**
	 * @param gain
	 *            the gain to set
	 */
	public void setGain(String gain) {
		this.gain = gain;
	}

	public Boolean getChangeSensitivity() {
		return changeSensitivity;
	}

	public void setChangeSensitivity(Boolean changeSensitivity) {
		this.changeSensitivity = changeSensitivity;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((channel == null) ? 0 : channel.hashCode());
		result = prime
				* result
				+ ((currentAmplifierName == null) ? 0 : currentAmplifierName
						.hashCode());
		result = prime * result
				+ ((deviceName == null) ? 0 : deviceName.hashCode());
		result = prime * result + ((gain == null) ? 0 : gain.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		DrainCurrentParameters other = (DrainCurrentParameters) obj;
		if (channel == null) {
			if (other.channel != null) {
				return false;
			}
		} else if (!channel.equals(other.channel)) {
			return false;
		}
		if (currentAmplifierName == null) {
			if (other.currentAmplifierName != null) {
				return false;
			}
		} else if (!currentAmplifierName.equals(other.currentAmplifierName)) {
			return false;
		}
		if (deviceName == null) {
			if (other.deviceName != null) {
				return false;
			}
		} else if (!deviceName.equals(other.deviceName)) {
			return false;
		}
		if (gain == null) {
			if (other.gain != null) {
				return false;
			}
		} else if (!gain.equals(other.gain)) {
			return false;
		}
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
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

}
