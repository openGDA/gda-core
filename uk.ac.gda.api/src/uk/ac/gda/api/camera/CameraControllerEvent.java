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

package uk.ac.gda.api.camera;

import java.io.Serializable;

public class CameraControllerEvent implements Serializable {
	private double acquireTime;
	private CameraRegionOfInterest regionOfInterest;
	private BinningFormat binningFormat;
	private CameraState cameraState;

	public double getAcquireTime() {
		return acquireTime;
	}

	public void setAcquireTime(double acquireTime) {
		this.acquireTime = acquireTime;
	}

	public CameraRegionOfInterest getRegionOfInterest() {
		return regionOfInterest;
	}

	public void setRegionOfInterest(CameraRegionOfInterest regionOfInterest) {
		this.regionOfInterest = regionOfInterest;
	}

	public BinningFormat getBinningFormat() {
		return binningFormat;
	}

	public void setBinningFormat(BinningFormat binningFormat) {
		this.binningFormat = binningFormat;
	}

	public CameraState getCameraState() {
		return cameraState;
	}

	public void setCameraState(CameraState cameraState) {
		this.cameraState = cameraState;
	}
}
