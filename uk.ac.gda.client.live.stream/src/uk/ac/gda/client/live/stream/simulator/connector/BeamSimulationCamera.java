/*-
 * Copyright © 2020 Diamond Light Source Ltd.
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

package uk.ac.gda.client.live.stream.simulator.connector;

import uk.ac.gda.client.live.stream.view.CameraConfiguration;

/**
 * The configuration class for a beam driven stream connector.
 *
 * @see BeamSimulationCameraConnector
 * @author Maurizio Nagni
 */
public class BeamSimulationCamera extends CameraConfiguration {

	/**
	 * The camera array width
	 */
	private int cameraWidth;

	/**
	 * The camera array height
	 */
	private int cameraHeight;

	/**
	 * The name, (bean, id or other) associated with the X axis motor
	 */
	private String driverX;

	/**
	 * The ratio between number of pixels on the camera and the smallest step on X axis
	 */
	private double scaleX;

	/**
	 * The name, (bean, id or other) associated with the Y axis motor
	 */
	private String driverY;

	/**
	 * The ratio between number of pixels on the camera and the smallest step on Y axis
	 */
	private double scaleY;

	/**
	 * @return Returns the name, (bean, id or other) associated with the X axis motor
	 */
	public String getDriverX() {
		return driverX;
	}

	public void setDriverX(String driverX) {
		this.driverX = driverX;
	}

	/**
	 * @return Returns the name, (bean, id or other) associated with the Y axis motor
	 */
	public String getDriverY() {
		return driverY;
	}

	public void setDriverY(String driverY) {
		this.driverY = driverY;
	}

	/**
	 * @return The ratio between number of pixels on the camera and the smallest step on X axis
	 */
	public double getScaleX() {
		return scaleX;
	}

	public void setScaleX(double scaleX) {
		this.scaleX = scaleX;
	}

	/**
	 * @return The ratio between number of pixels on the camera and the smallest step on Y axis
	 */
	public double getScaleY() {
		return scaleY;
	}

	public void setScaleY(double scaleY) {
		this.scaleY = scaleY;
	}

	/**
	 * @return the camera width
	 */
	public int getCameraWidth() {
		return cameraWidth;
	}

	public void setCameraWidth(int cameraWidth) {
		this.cameraWidth = cameraWidth;
	}

	/**
	 * @return the camera height
	 */
	public int getCameraHeight() {
		return cameraHeight;
	}

	public void setCameraHeight(int cameraHeight) {
		this.cameraHeight = cameraHeight;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + cameraHeight;
		result = prime * result + cameraWidth;
		result = prime * result + ((driverX == null) ? 0 : driverX.hashCode());
		result = prime * result + ((driverY == null) ? 0 : driverY.hashCode());
		long temp;
		temp = Double.doubleToLongBits(scaleX);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(scaleY);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		BeamSimulationCamera other = (BeamSimulationCamera) obj;
		if (cameraHeight != other.cameraHeight)
			return false;
		if (cameraWidth != other.cameraWidth)
			return false;
		if (driverX == null) {
			if (other.driverX != null)
				return false;
		} else if (!driverX.equals(other.driverX))
			return false;
		if (driverY == null) {
			if (other.driverY != null)
				return false;
		} else if (!driverY.equals(other.driverY))
			return false;
		if (Double.doubleToLongBits(scaleX) != Double.doubleToLongBits(other.scaleX))
			return false;
		if (Double.doubleToLongBits(scaleY) != Double.doubleToLongBits(other.scaleY))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "BeamDrivenCamera [cameraWidth=" + cameraWidth + ", cameraHeight=" + cameraHeight + ", driverX="
				+ driverX + ", scaleX=" + scaleX + ", driverY=" + driverY + ", scaleY=" + scaleY + "]";
	}
}
