/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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

package org.eclipse.scanning.api.malcolm;

import org.eclipse.january.INameable;
import org.eclipse.scanning.api.device.models.IMalcolmDetectorModel;
import org.eclipse.scanning.api.device.models.IMalcolmModel;

/**
 * An object of this class describes the current state of a detector controlled by a malcolm device.
 * Note: this object should not be used to configure the malcolm device, to do that modify the
 * corresponding {@link IMalcolmDetectorModel} in the {@link IMalcolmModel} and call
 * {@link IMalcolmDevice#configure(IMalcolmModel)} with that model. Client code should not
 * modify this object, the setter methods are required for JSON deserialisation.
 * <p>
 * TODO: It is possible to create a Epics connection directly to this detector using its
 * mri. In the future we may wish to do this an create something similar to {@link IMalcolmDevice}
 * to represent a malcolm controlled detector that takes advantage of this. For now, this
 * class is a simple dumb-bean describing the current state of the detector that is retrieved
 * from the parent malcolm device.
 */
public class MalcolmDetectorInfo implements INameable {

	private String id;

	private String name;

	private boolean enabled;

	private double exposureTime;

	private int framesPerStep;

	public MalcolmDetectorInfo() {
		// default constructor
	}

	public MalcolmDetectorInfo(String id, String name, int framesPerStep, double exposureTime, boolean enabled) {
		// constructor for more consice test code
		this.id = id;
		this.name = name;
		this.framesPerStep = framesPerStep;
		this.exposureTime = exposureTime;
		this.enabled = enabled;
	}

	/**
	 * The id of this detector. This is the Malcolm Resource Identifier (MRI) for this detector.
	 * @return id
	 */
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	/**
	 * Returns the name of this detector
	 */
	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * Whether this detector is enabled
	 * @param enabled <code>true</code> if the detector is enabled, <code>false</code> otherwise
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	/**
	 * Get the exposure time to be used for the detector, in seconds.
	 *
	 * @return the exposure time in seconds. Can be zero but not negative.
	 */
	public double getExposureTime() {
		return exposureTime;
	}

	public void setExposureTime(double exposureTime) {
		this.exposureTime = exposureTime;
	}

	public int getFramesPerStep() {
		return framesPerStep;
	}

	public void setFramesPerStep(int framesPerStep) {
		this.framesPerStep = framesPerStep;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (enabled ? 1231 : 1237);
		long temp;
		temp = Double.doubleToLongBits(exposureTime);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + framesPerStep;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
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
		MalcolmDetectorInfo other = (MalcolmDetectorInfo) obj;
		if (enabled != other.enabled)
			return false;
		if (Double.doubleToLongBits(exposureTime) != Double.doubleToLongBits(other.exposureTime))
			return false;
		if (framesPerStep != other.framesPerStep)
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "MalcolmDetectorInfo [id=" + id + ", name=" + name + ", enabled=" + enabled + ", exposureTime="
				+ exposureTime + ", framesPerStep=" + framesPerStep + "]";
	}

}
