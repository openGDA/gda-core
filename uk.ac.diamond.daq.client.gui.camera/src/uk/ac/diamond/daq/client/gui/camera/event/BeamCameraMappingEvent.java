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

/**
 * Communicates that a new camera to beam mapping is available.
 *
 * @author Maurizio Nagni
 */
public class BeamCameraMappingEvent extends CameraEvent {

	private static final long serialVersionUID = -1939646061274410504L;
	private final int cameraIndex;

	/**
	 * @param source       the instance which published this instance
	 * @param cameraIndex  the camera to which this mapping is referring to. See
	 *                     <a href=
	 *                     "https://confluence.diamond.ac.uk/display/DIAD/K11+GDA+Properties">Camera
	 *                     Configuration Properties</a>.
	 */
	public BeamCameraMappingEvent(Object source, int cameraIndex) {
		super(source);
		this.cameraIndex = cameraIndex;
	}

	/**
	 * See <a href=
	 * "https://confluence.diamond.ac.uk/display/DIAD/K11+GDA+Properties">Camera
	 * Configuration Properties</a>.
	 *
	 * @return the camera index
	 */
	public int getCameraIndex() {
		return cameraIndex;
	}
}