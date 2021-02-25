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
package org.eclipse.scanning.example.detector;

import org.eclipse.scanning.api.device.models.AbstractDetectorModel;

public class ConstantVelocityModel extends AbstractDetectorModel {

	private static final String DEFAULT_NAME = "cvExmpl";
	private static final long DEFAULT_TIMEOUT = 100;

	private double start,stop,step;
	private int lineSize     = 1;
	private int channelCount = 64;
	private int spectraSize  = 46;

	public ConstantVelocityModel() {
		setName(DEFAULT_NAME);
		setTimeout(DEFAULT_TIMEOUT);
	}

	public ConstantVelocityModel(String name, double start, double stop, double step) {
		setName(name);
		setTimeout(DEFAULT_TIMEOUT);
		this.start = start;
		this.stop  = stop;
		this.step  = step;

		double div = ((stop-start)/step);
		this.lineSize = (int)Math.floor(div+1);
	}

	public int getLineSize() {
		return lineSize;
	}
	public void setLineSize(int lineSize) {
		this.lineSize = lineSize;
	}
	public int getChannelCount() {
		return channelCount;
	}
	public void setChannelCount(int channelCount) {
		this.channelCount = channelCount;
	}
	public int getSpectraSize() {
		return spectraSize;
	}
	public void setSpectraSize(int spectraSize) {
		this.spectraSize = spectraSize;
	}

	public double getStart() {
		return start;
	}

	public void setStart(double start) {
		this.start = start;
	}

	public double getStop() {
		return stop;
	}

	public void setStop(double stop) {
		this.stop = stop;
	}

	public double getStep() {
		return step;
	}

	public void setStep(double step) {
		this.step = step;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + channelCount;
		result = prime * result + lineSize;
		result = prime * result + spectraSize;
		long temp;
		temp = Double.doubleToLongBits(start);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(step);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(stop);
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
		ConstantVelocityModel other = (ConstantVelocityModel) obj;
		if (channelCount != other.channelCount)
			return false;
		if (lineSize != other.lineSize)
			return false;
		if (spectraSize != other.spectraSize)
			return false;
		if (Double.doubleToLongBits(start) != Double.doubleToLongBits(other.start))
			return false;
		if (Double.doubleToLongBits(step) != Double.doubleToLongBits(other.step))
			return false;
		if (Double.doubleToLongBits(stop) != Double.doubleToLongBits(other.stop))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ConstantVelocityModel [start=" + start + ", stop=" + stop + ", step=" + step + ", lineSize=" + lineSize
				+ ", channelCount=" + channelCount + ", spectraSize=" + spectraSize + "]";
	}
}