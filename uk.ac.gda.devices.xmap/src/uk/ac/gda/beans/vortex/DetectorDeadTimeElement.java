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

package uk.ac.gda.beans.vortex;

import java.io.Serializable;

import org.apache.commons.beanutils.BeanUtils;

/**
 * Class which holds the data (window, gain etc) for a single detector element in an Xspress or Xspress2 system.
 * Communication with the real detector element is left in the XspressSystem class because it makes sense to speak to
 * all detector elements at once sometimes.
 */
public class DetectorDeadTimeElement  implements Serializable{

	private String name;
	
	// Each detector knows its own number (counting from 0). This has little
	// use within the software but helps to make the configuration file human
	// readable and writeable.
	private int number;	
	private double processDeadTimeAllEvent;
	private double processDeadTimeInWindow;
	/**
	 * default constructor for Castor
	 */
	public DetectorDeadTimeElement() {
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
	 * @return Returns the processDeadTimeAllEvent.
	 */
	public double getProcessDeadTimeAllEvent() {
		return processDeadTimeAllEvent;
	}

	/**
	 * @param processDeadTimeAllEvent The processDeadTimeAllEvent to set.
	 */
	public void setProcessDeadTimeAllEvent(double processDeadTimeAllEvent) {
		this.processDeadTimeAllEvent = processDeadTimeAllEvent;
	}

	/**
	 * @return Returns the processDeadTimeInWindow.
	 */
	public double getProcessDeadTimeInWindow() {
		return processDeadTimeInWindow;
	}

	/**
	 * @param processDeadTimeInWindow The processDeadTimeInWindow to set.
	 */
	public void setProcessDeadTimeInWindow(double processDeadTimeInWindow) {
		this.processDeadTimeInWindow = processDeadTimeInWindow;
	}

	/**
	 * @return Returns the name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name The name to set.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return number
	 */
	public int getNumber() {
		return number;
	}
	/**
	 * @param number The number to set.
	 */
	public void setNumber(int number) {
		this.number = number;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;		
		temp = Double.doubleToLongBits(processDeadTimeAllEvent);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(processDeadTimeInWindow);
		result = prime * result + (int) (temp ^ (temp >>> 32));
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
		DetectorDeadTimeElement other = (DetectorDeadTimeElement) obj;
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		if (number != other.number) {
			return false;
		}
		if (Double.doubleToLongBits(processDeadTimeAllEvent) != Double
				.doubleToLongBits(other.processDeadTimeAllEvent)) {
			return false;
		}
		if (Double.doubleToLongBits(processDeadTimeInWindow) != Double
				.doubleToLongBits(other.processDeadTimeInWindow)) {
			return false;
		}
		return true;
	}	
}
