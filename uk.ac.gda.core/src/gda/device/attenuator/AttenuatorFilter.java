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

package gda.device.attenuator;

import org.springframework.beans.factory.InitializingBean;

/**
 * Represents an individual filter of an attenuator. Either the transmission or absorption can be set (when one is set,
 * the other will be automatically set).
 */
public class AttenuatorFilter implements InitializingBean {
	
	protected String name;
	
	protected double transmissionPercentage = -1;
	
	protected double absorptionPercentage = -1;
	
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Sets the transmission percentage for this filter (and automatically sets the absorption percentage).
	 */
	public void setTransmissionPercentage(double transmissionPercentage) {
		this.transmissionPercentage = transmissionPercentage;
		this.absorptionPercentage = (100 - transmissionPercentage);
	}
	
	/**
	 * Sets the absorption percentage for this filter (and automatically sets the transmission percentage).
	 */
	public void setAbsorptionPercentage(double absorptionPercentage) {
		this.absorptionPercentage = absorptionPercentage;
		this.transmissionPercentage = (100 - absorptionPercentage);
	}
	
	@Override
	public void afterPropertiesSet() throws Exception {
		if (name == null) {
			throw new IllegalArgumentException("The 'name' property is required");
		}
		if (transmissionPercentage == -1 || absorptionPercentage == -1) {
			throw new IllegalArgumentException("You must set either the 'transmissionPercentage' or 'absorptionPercentage' property (setting one will also set the other)");
		}
	}
	
	public String getName() {
		return name;
	}
	
	public double getTransmissionPercentage() {
		return transmissionPercentage;
	}
	
	public double getAbsorptionPercentage() {
		return absorptionPercentage;
	}
	
	@Override
	public String toString() {
		return String.format("AttenuatorFilter(name='%s', transmission=%.6f%%)", name, transmissionPercentage);
	}
	
}
