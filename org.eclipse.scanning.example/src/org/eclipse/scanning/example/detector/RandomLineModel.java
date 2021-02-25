package org.eclipse.scanning.example.detector;

import org.eclipse.scanning.api.device.models.AbstractDetectorModel;

public class RandomLineModel extends AbstractDetectorModel {

	private int lineSize=32;

	public RandomLineModel() {
		setName("line");
		setExposureTime(0.001);
		setTimeout(-1);
	}

	public int getLineSize() {
		return lineSize;
	}
	public void setLineSize(int lineSize) {
		this.lineSize = lineSize;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + lineSize;
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
		RandomLineModel other = (RandomLineModel) obj;
		if (lineSize != other.lineSize)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "RandomLineModel [lineSize=" + lineSize + "]";
	}
}
