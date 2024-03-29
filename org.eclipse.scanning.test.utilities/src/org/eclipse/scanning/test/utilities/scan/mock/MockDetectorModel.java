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
package org.eclipse.scanning.test.utilities.scan.mock;

import java.util.Arrays;

import org.eclipse.scanning.api.AbstractNameable;
import org.eclipse.scanning.api.device.models.IDetectorModel;


public class MockDetectorModel extends AbstractNameable implements IDetectorModel {

	public MockDetectorModel() {
		// for deserialisation
	}

	public MockDetectorModel(double exposureTime) {
		super();
		this.exposureTime = exposureTime;
	}

	private double exposureTime;

	private int ran=0;
	private int written=0;
	private int abortCount=-1;
    private boolean createImage = true;
    private int[] imageSize = new int[]{64, 64};

	@Override
	public double getExposureTime() {
		return exposureTime;
	}

	@Override
	public void setExposureTime(double exposureTime) {
		this.exposureTime = exposureTime;
	}

	public int getRan() {
		return ran;
	}

	public void setRan(int ran) {
		this.ran = ran;
	}

	public int getWritten() {
		return written;
	}

	public void setWritten(int read) {
		this.written = read;
	}


	public int getAbortCount() {
		return abortCount;
	}

	public void setAbortCount(int abortCount) {
		this.abortCount = abortCount;
	}

	public boolean isCreateImage() {
		return createImage;
	}

	public void setCreateImage(boolean createImage) {
		this.createImage = createImage;
	}

	public int[] getImageSize() {
		return imageSize;
	}

	public void setImageSize(int[] imageSize) {
		this.imageSize = imageSize;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + abortCount;
		result = prime * result + (createImage ? 1231 : 1237);
		long temp;
		temp = Double.doubleToLongBits(exposureTime);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + Arrays.hashCode(imageSize);
		result = prime * result + ran;
		result = prime * result + written;
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
		MockDetectorModel other = (MockDetectorModel) obj;
		if (abortCount != other.abortCount)
			return false;
		if (createImage != other.createImage)
			return false;
		if (Double.doubleToLongBits(exposureTime) != Double.doubleToLongBits(other.exposureTime))
			return false;
		if (!Arrays.equals(imageSize, other.imageSize))
			return false;
		if (ran != other.ran)
			return false;
		if (written != other.written)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "MockDetectorModel [exposureTime=" + exposureTime + ", ran=" + ran + ", written=" + written
				+ ", abortCount=" + abortCount + ", createImage=" + createImage + ", imageSize="
				+ Arrays.toString(imageSize) + "]";
	}
}
