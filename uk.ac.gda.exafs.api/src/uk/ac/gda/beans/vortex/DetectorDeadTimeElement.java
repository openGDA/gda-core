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
import java.net.URL;

import org.apache.commons.beanutils.BeanUtils;

/**
 * Class which holds the data (window, gain etc) for a single detector element in an Xspress or Xspress2 system.
 * Communication with the real detector element is left in the XspressSystem class because it makes sense to speak to
 * all detector elements at once sometimes.
 */
public class DetectorDeadTimeElement implements Serializable {
	private static final long serialVersionUID = 5102273536126939949L;

	static public final URL mappingURL = DetectorDeadTimeElement.class.getResource("XspressMapping.xml");

	static public final URL schemaURL  = DetectorDeadTimeElement.class.getResource("XspressMapping.xsd");

	private double processDeadTimeAllEventGradient;
	private double processDeadTimeAllEventOffset;
	private double processDeadTimeInWindow;
	private double processDeadTimeInWindowGradient;
	private String name;
	private int number;

	/**
	 * default constructor for Castor
	 */
	public DetectorDeadTimeElement() {
	}

	public DetectorDeadTimeElement(String name, int number, double processDeadTimeAllEventGradient, double processDeadTimeAllEventOffset, double processDeadTimeInWindow) {
		this.name = name;
		this.number = number;
		this.processDeadTimeAllEventGradient = processDeadTimeAllEventGradient;
		this.processDeadTimeAllEventOffset = processDeadTimeAllEventOffset;
		this.processDeadTimeInWindow = processDeadTimeInWindow;
		this.processDeadTimeInWindowGradient = 0; // only noticed on 64-element so this value is optional
	}

	public DetectorDeadTimeElement(String name, int number, double processDeadTimeAllEventGradient, double processDeadTimeAllEventOffset, double processDeadTimeInWindow,double processDeadTimeInWindowGradient) {
		this.name = name;
		this.number = number;
		this.processDeadTimeAllEventGradient = processDeadTimeAllEventGradient;
		this.processDeadTimeAllEventOffset = processDeadTimeAllEventOffset;
		this.processDeadTimeInWindow = processDeadTimeInWindow;
		this.processDeadTimeInWindowGradient = processDeadTimeInWindowGradient;
	}

	@Override
	public String toString() {
		try {
			return BeanUtils.describe(this).toString();
		} catch (Exception e) {
			return e.getMessage();
		}
	}

	public double getProcessDeadTimeAllEventGradient() {
		return processDeadTimeAllEventGradient;
	}

	public void setProcessDeadTimeAllEventGradient(double processDeadTimeAllEventGradient) {
		this.processDeadTimeAllEventGradient = processDeadTimeAllEventGradient;
	}

	public double getProcessDeadTimeAllEventOffset() {
		return processDeadTimeAllEventOffset;
	}

	public void setProcessDeadTimeAllEventOffset(double processDeadTimeAllEventOffset) {
		this.processDeadTimeAllEventOffset = processDeadTimeAllEventOffset;
	}

	public double getProcessDeadTimeInWindow() {
		return processDeadTimeInWindow;
	}

	public void setProcessDeadTimeInWindow(double processDeadTimeInWindow) {
		this.processDeadTimeInWindow = processDeadTimeInWindow;
	}

	public double getProcessDeadTimeInWindowGradient() {
		return processDeadTimeInWindowGradient;
	}

	public void setProcessDeadTimeInWindowGradient(double processDeadTimeInWindowGradient) {
		this.processDeadTimeInWindowGradient = processDeadTimeInWindowGradient;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	/**
	 * NOTE number = the pixel number
	 *
	 * A value starting at 1 used to reference pixels on the detector.
	 *
	 * @return number
	 */
	public int getNumber() {
		return number;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + number;
		long temp;
		temp = Double.doubleToLongBits(processDeadTimeAllEventGradient);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(processDeadTimeAllEventOffset);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(processDeadTimeInWindow);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(processDeadTimeInWindowGradient);
		result = prime * result + (int) (temp ^ (temp >>> 32));
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
		DetectorDeadTimeElement other = (DetectorDeadTimeElement) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (number != other.number)
			return false;
		if (Double.doubleToLongBits(processDeadTimeAllEventGradient) != Double
				.doubleToLongBits(other.processDeadTimeAllEventGradient))
			return false;
		if (Double.doubleToLongBits(processDeadTimeAllEventOffset) != Double
				.doubleToLongBits(other.processDeadTimeAllEventOffset))
			return false;
		if (Double.doubleToLongBits(processDeadTimeInWindow) != Double.doubleToLongBits(other.processDeadTimeInWindow))
			return false;
		if (Double.doubleToLongBits(processDeadTimeInWindowGradient) != Double
				.doubleToLongBits(other.processDeadTimeInWindowGradient))
			return false;
		return true;
	}
}
