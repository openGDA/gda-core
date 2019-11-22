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

package org.eclipse.scanning.api.device.models;

public class MalcolmDetectorModel extends AbstractDetectorModel implements IMalcolmDetectorModel {

	private int framesPerStep = 1;

	private boolean enabled = true;

	public MalcolmDetectorModel() {
		// no-arg constructor required for json and epics deserialization
	}

	public MalcolmDetectorModel(String name, double exposureTime, int framesPerStep, boolean enabled) {
		setName(name);
		setExposureTime(exposureTime);
		setFramesPerStep(framesPerStep);
		setEnabled(enabled);
	}

	@Override
	public int getFramesPerStep() {
		return framesPerStep;
	}

	@Override
	public void setFramesPerStep(int framesPerStep) {
		this.framesPerStep = framesPerStep;
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	@Override
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (enabled ? 1231 : 1237);
		result = prime * result + framesPerStep;
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
		MalcolmDetectorModel other = (MalcolmDetectorModel) obj;
		if (enabled != other.enabled)
			return false;
		if (framesPerStep != other.framesPerStep)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "MalcolmDetectorModel [framesPerStep=" + framesPerStep +
				", enabled=" + enabled + ", " + super.toString() + "]";
	}

}
