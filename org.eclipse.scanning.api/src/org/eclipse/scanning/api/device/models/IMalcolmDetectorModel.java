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

/**
 * A model used to configure a detector controlled by a malcolm devices.
 */
public interface IMalcolmDetectorModel extends IDetectorModel {

	/**
	 * Returns the number of frames (exposures) of this detector per step, i.e. scan point.
	 * @return frames per step
	 */
	public int getFramesPerStep();

	public void setFramesPerStep(int framesPerStep);

	/**
	 * Returns whether this detectors is enabled, i.e. is to be used in a scan.
	 * @return <code>true</code> if the detector is enabled, <code>false</code> otherwise
	 */
	public boolean isEnabled();

	public void setEnabled(boolean enabled);

}
