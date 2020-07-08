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
	/**
	 * The name of the camera controller, typically the bean name
	 */
	private String name;
	private double acquireTime;
	private BinningFormat binningFormat;
	private CameraState cameraState;

	/**
	 * Returns the name of the camera controller, typically the bean name
	 * @return the controller name
	 */
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getAcquireTime() {
		return acquireTime;
	}

	public void setAcquireTime(double acquireTime) {
		this.acquireTime = acquireTime;
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

	@Override
	public String toString() {
		return "CameraControllerEvent [acquireTime=" + acquireTime + ", binningFormat=" + binningFormat
				+ ", cameraState=" + cameraState + "]";
	}
}
