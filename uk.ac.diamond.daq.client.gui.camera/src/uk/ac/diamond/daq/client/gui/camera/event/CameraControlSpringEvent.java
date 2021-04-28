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
import uk.ac.gda.client.properties.camera.CameraConfigurationProperties;
import uk.ac.gda.ui.tool.spring.ClientSpringProperties;

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
	private final String cameraId;
	private final double acquireTime;
	private final BinningFormat binningFormat;
	private final CameraState cameraState;

	/**
	 * @param source the object generating the event
	 * @param event a camera control event
	 * @param cameraId the {@link CameraConfigurationProperties#getId()} to the event is referred
	 */
	public CameraControlSpringEvent(Object source, CameraControllerEvent event, String cameraId) {
		super(source);
		this.cameraId = cameraId;
		this.acquireTime = event.getAcquireTime();
		this.binningFormat = event.getBinningFormat();
		this.cameraState = event.getCameraState();
	}

	/**
	 * Identifies the camera.
	 * <p>
	 * Each GDA client has a configuration which assigns to each camera a unique id.
	 * This id may be used by different GUI components to retrieve details or publish events regarding that camera.
	 * </p>
	 * @return an identification id
	 *
	 * @see ClientSpringProperties
	 */
	public String getCameraId() {
		return cameraId;
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