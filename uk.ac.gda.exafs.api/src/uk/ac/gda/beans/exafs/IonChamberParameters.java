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

import gda.configuration.properties.LocalProperties;

/**
 *
 */
public class IonChamberParameters  implements Serializable{

	private String  name;
	private String  deviceName= LocalProperties.get("gda.exafs.ionchambersName","counterTimer01");
	private Integer channel=1;
	private String  currentAmplifierName="Keithley";
	private boolean useGasProperties = true;
	private String  gain;  // sensitivity of the current amps
	private String  offset; // offset of the current amps
	private Double  pressure;
	private Double  totalPressure = 2d; // Not often changed by user, can default it
	private String  gasType = "0";
	private Double  percentAbsorption;
	private Double  ionChamberLength=30d;
	private Double gas_fill1_period_box = 100d;
	private Double gas_fill2_period_box = 100d;
	private Boolean changeSensitivity = false;
	private Boolean autoFillGas = false;
	private Boolean flush = false;

	// Not persisted but used in pressure calculation.
	private Double  workingEnergy;

	public Double getGas_fill1_period_box() {
		return gas_fill1_period_box;
	}

	public void setGas_fill1_period_box(Double gasFill1PeriodBox) {
		gas_fill1_period_box = gasFill1PeriodBox;
	}

	public Double getGas_fill2_period_box() {
		return gas_fill2_period_box;
	}

	public void setGas_fill2_period_box(Double gasFill2PeriodBox) {
		gas_fill2_period_box = gasFill2PeriodBox;
	}

	public Double getWorkingEnergy() {
		return workingEnergy;
	}

	public void setWorkingEnergy(Double workingEnergy) {
		this.workingEnergy = workingEnergy;
	}

	public Double getIonChamberLength() {
		return ionChamberLength;
	}

	public void setIonChamberLength(Double ionChamberLength) {
		this.ionChamberLength = ionChamberLength;
	}

	public Double getTotalPressure() {
		return totalPressure;
	}

	public void setTotalPressure(Double totalPressure) {
		this.totalPressure = totalPressure;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDeviceName() {
		return deviceName;
	}

	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}

	public Integer getChannel() {
		return channel;
	}

	public void setChannel(Integer channel) {
		this.channel = channel;
	}

	public void setChannel(String channelString) {
		this.channel = Integer.valueOf(channelString);
	}

	public String getCurrentAmplifierName() {
		return currentAmplifierName;
	}

	public void setCurrentAmplifierName(String currentAmplifierName) {
		this.currentAmplifierName = currentAmplifierName;
	}

	public String getGain() {
		return gain;
	}

	public void setGain(String gain) {
		this.gain = gain;
	}

	public String getOffset() {
		return offset;
	}

	public void setOffset(String offset) {
		this.offset = offset;
	}

	public String getGasType() {
		return gasType;
	}

	public void setGasType(String gasType) {
		this.gasType = gasType;
	}

	/**
	 * @return the percentAbsorption
	 */
	public Double getPercentAbsorption() {
		return percentAbsorption;
	}

	/**
	 * @param percentAbsorption
	 *            the percentAbsorption to set
	 */
	public void setPercentAbsorption(Double percentAbsorption) {
		this.percentAbsorption = percentAbsorption;
	}
	/**
	 * @return f
	 */
	public Double getPressure() {
		return pressure;
	}

	/**
	 * @param pressure
	 */
	public void setPressure(Double pressure) {
		this.pressure = pressure;
	}

	public void setPressure(String pressureString) {
		this.pressure = Double.valueOf(pressureString);
	}

	public void setAutoFillGas(Boolean automaticallyFillGas) {
		this.autoFillGas = automaticallyFillGas;
	}

	public void setFlush(Boolean flush) {
		this.flush = flush;
	}

	public void setChangeSensitivity(Boolean changeSensitivity) {
		this.changeSensitivity = changeSensitivity;
	}

	public Boolean getChangeSensitivity() {
		return changeSensitivity;
	}

	public Boolean getAutoFillGas() {
		return autoFillGas;
	}

	public Boolean getFlush() {
		return flush;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((autoFillGas == null) ? 0 : autoFillGas.hashCode());
		result = prime * result + ((changeSensitivity == null) ? 0 : changeSensitivity.hashCode());
		result = prime * result + ((channel == null) ? 0 : channel.hashCode());
		result = prime * result + ((currentAmplifierName == null) ? 0 : currentAmplifierName.hashCode());
		result = prime * result + ((deviceName == null) ? 0 : deviceName.hashCode());
		result = prime * result + ((flush == null) ? 0 : flush.hashCode());
		result = prime * result + ((gain == null) ? 0 : gain.hashCode());
		result = prime * result + ((gasType == null) ? 0 : gasType.hashCode());
		result = prime * result + ((gas_fill1_period_box == null) ? 0 : gas_fill1_period_box.hashCode());
		result = prime * result + ((gas_fill2_period_box == null) ? 0 : gas_fill2_period_box.hashCode());
		result = prime * result + ((ionChamberLength == null) ? 0 : ionChamberLength.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((offset == null) ? 0 : offset.hashCode());
		result = prime * result + ((percentAbsorption == null) ? 0 : percentAbsorption.hashCode());
		result = prime * result + ((pressure == null) ? 0 : pressure.hashCode());
		result = prime * result + ((totalPressure == null) ? 0 : totalPressure.hashCode());
		result = prime * result + (useGasProperties ? 1231 : 1237);
		result = prime * result + ((workingEnergy == null) ? 0 : workingEnergy.hashCode());
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
		IonChamberParameters other = (IonChamberParameters) obj;
		if (autoFillGas == null) {
			if (other.autoFillGas != null)
				return false;
		} else if (!autoFillGas.equals(other.autoFillGas))
			return false;
		if (changeSensitivity == null) {
			if (other.changeSensitivity != null)
				return false;
		} else if (!changeSensitivity.equals(other.changeSensitivity))
			return false;
		if (channel == null) {
			if (other.channel != null)
				return false;
		} else if (!channel.equals(other.channel))
			return false;
		if (currentAmplifierName == null) {
			if (other.currentAmplifierName != null)
				return false;
		} else if (!currentAmplifierName.equals(other.currentAmplifierName))
			return false;
		if (deviceName == null) {
			if (other.deviceName != null)
				return false;
		} else if (!deviceName.equals(other.deviceName))
			return false;
		if (flush == null) {
			if (other.flush != null)
				return false;
		} else if (!flush.equals(other.flush))
			return false;
		if (gain == null) {
			if (other.gain != null)
				return false;
		} else if (!gain.equals(other.gain))
			return false;
		if (gasType == null) {
			if (other.gasType != null)
				return false;
		} else if (!gasType.equals(other.gasType))
			return false;
		if (gas_fill1_period_box == null) {
			if (other.gas_fill1_period_box != null)
				return false;
		} else if (!gas_fill1_period_box.equals(other.gas_fill1_period_box))
			return false;
		if (gas_fill2_period_box == null) {
			if (other.gas_fill2_period_box != null)
				return false;
		} else if (!gas_fill2_period_box.equals(other.gas_fill2_period_box))
			return false;
		if (ionChamberLength == null) {
			if (other.ionChamberLength != null)
				return false;
		} else if (!ionChamberLength.equals(other.ionChamberLength))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (offset == null) {
			if (other.offset != null)
				return false;
		} else if (!offset.equals(other.offset))
			return false;
		if (percentAbsorption == null) {
			if (other.percentAbsorption != null)
				return false;
		} else if (!percentAbsorption.equals(other.percentAbsorption))
			return false;
		if (pressure == null) {
			if (other.pressure != null)
				return false;
		} else if (!pressure.equals(other.pressure))
			return false;
		if (totalPressure == null) {
			if (other.totalPressure != null)
				return false;
		} else if (!totalPressure.equals(other.totalPressure))
			return false;
		if (useGasProperties != other.useGasProperties)
			return false;
		if (workingEnergy == null) {
			if (other.workingEnergy != null)
				return false;
		} else if (!workingEnergy.equals(other.workingEnergy))
			return false;
		return true;
	}

	public void setUseGasProperties(boolean useGasProperties) {
		this.useGasProperties = useGasProperties;
	}

	public boolean isUseGasProperties() {
		return useGasProperties;
	}

}
