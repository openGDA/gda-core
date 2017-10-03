/*-
 * Copyright © 2017 Diamond Light Source Ltd.
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

package uk.ac.gda.client.live.stream.view;

/**
 * An instance of this class describes the calibration of a camera, in particular
 * for each of the x-axis and y-axis (used as shorthand to refer to the horizontal
 * and vertical axis of the camera respectively), this class holds:<ul>
 * <li>the name of the scannable for that axis;</li>
 * <li>the pixel scaling of that axis, i.e. the size of each pixel in mm;</li>
 * <li>The offset of that axis, i.e. the position of the camera relative to the stage.</li>
 * </ul>
 */
public class CameraCalibration {

	/**
	 * The name of the scannable for the horizontal axis of the camera.
	 */
	private String xAxisScannableName;

	/**
	 * The name of the scannable for the vertical axis of the camera.
	 */
	private String yAxisScannableName;

	/**
	 * The width of each pixel of the camera, in the units used by the x-axis scannable.
	 */
	private double xAxisPixelScaling;

	/**
	 * The height of each pixel of the camera in the units used by the y-axis scannable.
	 */
	private double yAxisPixelScaling;

	/**
	 * The position of the camera relative to the x-axis of the sample stage.
	 */
	private double xAxisOffset;

	/**
	 * The position of the camera relative to the y-axis of the sample stage.
	 */
	private double yAxisOffset;

	public String getxAxisScannableName() {
		return xAxisScannableName;
	}

	public void setxAxisScannableName(String xAxisScannableName) {
		this.xAxisScannableName = xAxisScannableName;
	}

	public String getyAxisScannableName() {
		return yAxisScannableName;
	}

	public void setyAxisScannableName(String yAxisScannableName) {
		this.yAxisScannableName = yAxisScannableName;
	}

	public double getxAxisPixelScaling() {
		return xAxisPixelScaling;
	}

	public void setxAxisPixelScaling(double xAxisCalibration) {
		this.xAxisPixelScaling = xAxisCalibration;
	}

	public double getyAxisPixelScaling() {
		return yAxisPixelScaling;
	}

	public void setyAxisPixelScaling(double yAxisPixelScaling) {
		this.yAxisPixelScaling = yAxisPixelScaling;
	}

	public double getxAxisOffset() {
		return xAxisOffset;
	}

	public void setxAxisOffset(double xAxisOffset) {
		this.xAxisOffset = xAxisOffset;
	}

	public double getyAxisOffset() {
		return yAxisOffset;
	}

	public void setyAxisOffset(double yAxisOffset) {
		this.yAxisOffset = yAxisOffset;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(xAxisPixelScaling);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(xAxisOffset);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((xAxisScannableName == null) ? 0 : xAxisScannableName.hashCode());
		temp = Double.doubleToLongBits(yAxisPixelScaling);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(yAxisOffset);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((yAxisScannableName == null) ? 0 : yAxisScannableName.hashCode());
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
		CameraCalibration other = (CameraCalibration) obj;
		if (Double.doubleToLongBits(xAxisPixelScaling) != Double.doubleToLongBits(other.xAxisPixelScaling))
			return false;
		if (Double.doubleToLongBits(xAxisOffset) != Double.doubleToLongBits(other.xAxisOffset))
			return false;
		if (xAxisScannableName == null) {
			if (other.xAxisScannableName != null)
				return false;
		} else if (!xAxisScannableName.equals(other.xAxisScannableName))
			return false;
		if (Double.doubleToLongBits(yAxisPixelScaling) != Double.doubleToLongBits(other.yAxisPixelScaling))
			return false;
		if (Double.doubleToLongBits(yAxisOffset) != Double.doubleToLongBits(other.yAxisOffset))
			return false;
		if (yAxisScannableName == null) {
			if (other.yAxisScannableName != null)
				return false;
		} else if (!yAxisScannableName.equals(other.yAxisScannableName))
			return false;
		return true;
	}

}
