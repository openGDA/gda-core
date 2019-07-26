/*-
 *******************************************************************************
 * Copyright (c) 2011, 2016 Diamond Light Source Ltd.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthew Gerring - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.scanning.api.scan;

import java.io.Serializable;

import org.eclipse.scanning.api.annotation.ui.DeviceType;
import org.eclipse.scanning.api.annotation.ui.FieldDescriptor;
import org.eclipse.scanning.api.annotation.ui.FileType;

public class AxisConfiguration implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = -3907146006857647918L;

	@FieldDescriptor(file=FileType.EXISTING_FILE,
			         hint="The microscope image to load into the scan as a background.",
			         fieldPosition=-2)
	private String microscopeImage;

	@FieldDescriptor(label="Random", hint="If there is no image, create some random noise for one instead.", fieldPosition=-1, enableif="microscopeImage==null")
	private boolean randomNoise = false;

	public boolean isRandomNoise() {
		return randomNoise;
	}
	public void setRandomNoise(boolean randomNoise) {
		this.randomNoise = randomNoise;
	}
	public String getMicroscopeImage() {
		return microscopeImage;
	}
	public void setMicroscopeImage(String microscopeImage) {
		this.microscopeImage = microscopeImage;
	}
	@FieldDescriptor(device=DeviceType.SCANNABLE, hint="The name of the scannable to be plotted as the x-axis, e.g. the x motor of the sample stage.", fieldPosition=0)
	private String xAxisName;

	@FieldDescriptor(scannable="xAxisName", fieldPosition=1)
	private double xAxisStart;

	@FieldDescriptor(scannable="xAxisName", fieldPosition=2)
	private double xAxisEnd;

	@FieldDescriptor(device=DeviceType.SCANNABLE, hint="The name of the scannable to be plotted as the y-axis, e.g. the x motor of the sample stage.", fieldPosition=3)
	private String yAxisName;

	@FieldDescriptor(scannable="yAxisName", fieldPosition=4)
	private double yAxisStart;

	@FieldDescriptor(scannable="yAxisName", fieldPosition=5)
	private double yAxisEnd;

	@FieldDescriptor(label="Apply to Regions", hint="Find any scan regions and set their axis names to those on this form.", fieldPosition=6)
	private boolean applyRegions = false;

	@FieldDescriptor(label="Apply to Scan", hint="Find any scan models and set their axis names to those on this form.", fieldPosition=7)
	private boolean applyModels = false;

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (applyModels ? 1231 : 1237);
		result = prime * result + (applyRegions ? 1231 : 1237);
		long temp;
		temp = Double.doubleToLongBits(xAxisEnd);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((xAxisName == null) ? 0 : xAxisName.hashCode());
		temp = Double.doubleToLongBits(xAxisStart);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((microscopeImage == null) ? 0 : microscopeImage.hashCode());
		result = prime * result + (randomNoise ? 1231 : 1237);
		temp = Double.doubleToLongBits(yAxisEnd);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((yAxisName == null) ? 0 : yAxisName.hashCode());
		temp = Double.doubleToLongBits(yAxisStart);
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
		AxisConfiguration other = (AxisConfiguration) obj;
		if (applyModels != other.applyModels)
			return false;
		if (applyRegions != other.applyRegions)
			return false;
		if (Double.doubleToLongBits(xAxisEnd) != Double.doubleToLongBits(other.xAxisEnd))
			return false;
		if (xAxisName == null) {
			if (other.xAxisName != null)
				return false;
		} else if (!xAxisName.equals(other.xAxisName))
			return false;
		if (Double.doubleToLongBits(xAxisStart) != Double.doubleToLongBits(other.xAxisStart))
			return false;
		if (microscopeImage == null) {
			if (other.microscopeImage != null)
				return false;
		} else if (!microscopeImage.equals(other.microscopeImage))
			return false;
		if (randomNoise != other.randomNoise)
			return false;
		if (Double.doubleToLongBits(yAxisEnd) != Double.doubleToLongBits(other.yAxisEnd))
			return false;
		if (yAxisName == null) {
			if (other.yAxisName != null)
				return false;
		} else if (!yAxisName.equals(other.yAxisName))
			return false;
		if (Double.doubleToLongBits(yAxisStart) != Double.doubleToLongBits(other.yAxisStart))
			return false;
		return true;
	}
	public String getXAxisName() {
		return xAxisName;
	}
	public void setXAxisName(String xAxisName) {
		this.xAxisName = xAxisName;
	}
	public double getXAxisStart() {
		return xAxisStart;
	}
	public void setXAxisStart(double xAxisStart) {
		this.xAxisStart = xAxisStart;
	}
	public double getXAxisEnd() {
		return xAxisEnd;
	}
	public void setXAxisEnd(double xAxisEnd) {
		this.xAxisEnd = xAxisEnd;
	}
	public String getYAxisName() {
		return yAxisName;
	}
	public void setYAxisName(String yAxisName) {
		this.yAxisName = yAxisName;
	}
	public double getYAxisStart() {
		return yAxisStart;
	}
	public void setYAxisStart(double yAxisStart) {
		this.yAxisStart = yAxisStart;
	}
	public double getYAxisEnd() {
		return yAxisEnd;
	}
	public void setYAxisEnd(double yAxisEnd) {
		this.yAxisEnd = yAxisEnd;
	}

	public boolean isApplyRegions() {
		return applyRegions;
	}
	public void setApplyRegions(boolean applyRegions) {
		this.applyRegions = applyRegions;
	}
	public boolean isApplyModels() {
		return applyModels;
	}
	public void setApplyModels(boolean applyModels) {
		this.applyModels = applyModels;
	}

}
