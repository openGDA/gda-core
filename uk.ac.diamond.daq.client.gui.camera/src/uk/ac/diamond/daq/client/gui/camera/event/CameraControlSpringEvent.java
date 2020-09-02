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

package uk.ac.diamond.daq.client.gui.camera.event;

import uk.ac.gda.api.camera.BinningFormat;
import uk.ac.gda.api.camera.CameraControllerEvent;
import uk.ac.gda.api.camera.CameraState;

/**
 *  Publishes a CameraControl event through Spring
 *
 * @author Maurizio Nagni
 */
public class CameraControlSpringEvent extends CameraEvent {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -5829080723329070564L;
	private final String name;
	private final double acquireTime;
	private final BinningFormat binningFormat;
	private final CameraState cameraState;
	
	public CameraControlSpringEvent(Object source, String name, double acquireTime, BinningFormat binningFormat,
			CameraState cameraState) {
		super(source);
		this.name = name;
		this.acquireTime = acquireTime;
		this.binningFormat = binningFormat;
		this.cameraState = cameraState;
	}

	public CameraControlSpringEvent(Object source, CameraControllerEvent event) {
		super(source);
		this.name = event.getName();
		this.acquireTime = event.getAcquireTime();
		this.binningFormat = event.getBinningFormat();
		this.cameraState = event.getCameraState();
	}
	
	public String getName() {
		return name;
	}

	public double getAcquireTime() {
		return acquireTime;
	}

	public BinningFormat getBinningFormat() {
		return binningFormat;
	}

	public CameraState getCameraState() {
		return cameraState;
	}
}
