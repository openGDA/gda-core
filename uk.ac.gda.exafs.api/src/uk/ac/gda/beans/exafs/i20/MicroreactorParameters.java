/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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
import java.util.Arrays;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.ArrayUtils;

import uk.ac.gda.util.beans.xml.XMLRichBean;

public class MicroreactorParameters implements Serializable, XMLRichBean {
	private int gas0Rate = 0;
	private int gas1Rate = 0;
	private int gas2Rate = 0;
	private int gas3Rate = 0;
	private int gas4Rate = 0;
	private int gas5Rate = 0;
	private int gas6Rate = 0;
	private int gas7Rate = 0;
	private int temperature = 150;
	private String masses = "2,32"; // comma separated masses

	public int getGas0Rate() {
		return gas0Rate;
	}

	public void setGas0Rate(int gas0Rate) {
		this.gas0Rate = gas0Rate;
	}

	public int getGas1Rate() {
		return gas1Rate;
	}

	public void setGas1Rate(int gas1Rate) {
		this.gas1Rate = gas1Rate;
	}

	public int getGas2Rate() {
		return gas2Rate;
	}

	public void setGas2Rate(int gas2Rate) {
		this.gas2Rate = gas2Rate;
	}

	public int getGas3Rate() {
		return gas3Rate;
	}

	public void setGas3Rate(int gas3Rate) {
		this.gas3Rate = gas3Rate;
	}

	public int getGas4Rate() {
		return gas4Rate;
	}

	public void setGas4Rate(int gas4Rate) {
		this.gas4Rate = gas4Rate;
	}

	public int getGas5Rate() {
		return gas5Rate;
	}

	public void setGas5Rate(int gas5Rate) {
		this.gas5Rate = gas5Rate;
	}

	public int getGas6Rate() {
		return gas6Rate;
	}

	public void setGas6Rate(int gas6Rate) {
		this.gas6Rate = gas6Rate;
	}

	public int getGas7Rate() {
		return gas7Rate;
	}

	public void setGas7Rate(int gas7Rate) {
		this.gas7Rate = gas7Rate;
	}

	public void setTemperature(int temperature) {
		this.temperature = temperature;
	}

	public int getTemperature() {
		return temperature;
	}

	public String getMasses() {
		return masses;
	}

	public Integer[] getIntegerMasses(){
		String[] parts = masses.split(",");
		Integer[] values = new Integer[0];
		for (String part : parts){
			try {
				Integer value = Integer.parseInt(part);
				values = (Integer[]) ArrayUtils.add(values, value);
			} catch (NumberFormatException e) {
				// ignore any carry on regardless to add as many masses as possible
			}
		}
		Arrays.sort(values);
		return values;
	}

	public void setMasses(String masses) {
		this.masses = masses;
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
		result = prime * result + gas0Rate;
		result = prime * result + gas1Rate;
		result = prime * result + gas2Rate;
		result = prime * result + gas3Rate;
		result = prime * result + gas4Rate;
		result = prime * result + gas5Rate;
		result = prime * result + gas6Rate;
		result = prime * result + gas7Rate;
		result = prime * result + ((masses == null) ? 0 : masses.hashCode());
		result = prime * result + temperature;
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
		MicroreactorParameters other = (MicroreactorParameters) obj;
		if (gas0Rate != other.gas0Rate)
			return false;
		if (gas1Rate != other.gas1Rate)
			return false;
		if (gas2Rate != other.gas2Rate)
			return false;
		if (gas3Rate != other.gas3Rate)
			return false;
		if (gas4Rate != other.gas4Rate)
			return false;
		if (gas5Rate != other.gas5Rate)
			return false;
		if (gas6Rate != other.gas6Rate)
			return false;
		if (gas7Rate != other.gas7Rate)
			return false;
		if (masses == null) {
			if (other.masses != null)
				return false;
		} else if (!masses.equals(other.masses))
			return false;
		if (temperature != other.temperature)
			return false;
		return true;
	}

}