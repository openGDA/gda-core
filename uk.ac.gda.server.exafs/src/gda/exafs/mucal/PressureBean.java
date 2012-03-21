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

package gda.exafs.mucal;

/**
 * Bean to hold result of pressure calculation.
 */
public class PressureBean {

	private double pressure;
	private String errorMessage;
	private String errorTooltip;
	/**
	 * @return Returns the pressure.
	 */
	public double getPressure() {
		return pressure;
	}
	/**
	 * @param pressure The pressure to set.
	 */
	public void setPressure(double pressure) {
		this.pressure = pressure;
	}
	/**
	 * @return Returns the errorMessage.
	 */
	public String getErrorMessage() {
		return errorMessage;
	}
	/**
	 * @param errorMessage The errorMessage to set.
	 */
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
	/**
	 * @return Returns the errorTooltip.
	 */
	public String getErrorTooltip() {
		return errorTooltip;
	}
	/**
	 * @param errorTooltip The errorTooltip to set.
	 */
	public void setErrorTooltip(String errorTooltip) {
		this.errorTooltip = errorTooltip;
	}
}
