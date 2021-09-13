/*-
 * Copyright © 2021 Diamond Light Source Ltd.
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

package uk.ac.gda.client.properties.acquisition.processing;

import uk.ac.gda.client.properties.camera.CameraConfigurationProperties;

/**
 * Configuration for a frame capture process associated with an acquisition
 *
 * @author Maurizio Nagni
 */
public class FrameCaptureProperties {

	/**
	 * The camera id as from the {@link CameraConfigurationProperties}
	 */
	private String cameraId;
	/**
	 * The name of the Malcolm instance
	 */
	private String malcolm;

	/**
	 * The name of the camera as mapped in Malcolm
	 */
	private String malcolmDetectorName;


	public String getMalcolm() {
		return malcolm;
	}
	public void setMalcolm(String malcolm) {
		this.malcolm = malcolm;
	}
	public String getMalcolmDetectorName() {
		return malcolmDetectorName;
	}
	public void setMalcolmDetectorName(String malcolmDetectorName) {
		this.malcolmDetectorName = malcolmDetectorName;
	}
	public String getCameraId() {
		return cameraId;
	}
	public void setCameraId(String cameraId) {
		this.cameraId = cameraId;
	}
}
