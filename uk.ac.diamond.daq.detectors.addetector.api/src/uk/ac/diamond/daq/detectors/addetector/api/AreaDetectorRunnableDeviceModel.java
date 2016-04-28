package uk.ac.diamond.daq.detectors.addetector.api;

import org.eclipse.scanning.api.device.models.IDetectorModel;

/**
 * Very simple model for initially getting a area detector to run in the new scanning.
 *
 * @author James Mudd
 */
public class AreaDetectorRunnableDeviceModel implements IDetectorModel {

	private String name;
	private double exposureTime; // seconds

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public double getExposureTime() {
		return exposureTime;
	}

	public void setExposureTime(double exposureTime) {
		this.exposureTime = exposureTime;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(exposureTime);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		AreaDetectorRunnableDeviceModel other = (AreaDetectorRunnableDeviceModel) obj;
		if (Double.doubleToLongBits(exposureTime) != Double.doubleToLongBits(other.exposureTime))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

}
