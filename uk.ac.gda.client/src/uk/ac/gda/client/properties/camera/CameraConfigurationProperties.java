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

package uk.ac.gda.client.properties.camera;

import java.util.List;
import java.util.Map;

import uk.ac.gda.api.camera.TriggerMode;
import uk.ac.gda.client.properties.controller.ControllerConfiguration;

/**
 * Defines the camera properties required by the GUI to expose a camera
 *
 * @author Maurizio Nagni
 */
public class CameraConfigurationProperties {

	private String configuration;

	private String id;

	private String name;

	private String cameraControl;

	private boolean pixelBinningEditable;

	private boolean withMonitor;

	private boolean beamMappingActive;

	private double readoutTime;

	private List<ControllerConfiguration> motors;

	private Map<TriggerMode, Short> triggerMode;

	private double[][] cameraToBeam;

	public double[][] getCameraToBeam() {
		return cameraToBeam;
	}

	public void setCameraToBeam(double[][] cameraToBeam) {
		this.cameraToBeam = cameraToBeam;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getConfiguration() {
		return configuration;
	}

	public void setConfiguration(String configuration) {
		this.configuration = configuration;
	}

	public String getCameraControl() {
		return cameraControl;
	}

	public void setCameraControl(String cameraControl) {
		this.cameraControl = cameraControl;
	}

	public boolean isPixelBinningEditable() {
		return pixelBinningEditable;
	}

	public void setPixelBinningEditable(boolean pixelBinningEditable) {
		this.pixelBinningEditable = pixelBinningEditable;
	}

	public boolean isWithMonitor() {
		return withMonitor;
	}

	public void setWithMonitor(boolean withMonitor) {
		this.withMonitor = withMonitor;
	}

	public boolean isBeamMappingActive() {
		return beamMappingActive;
	}

	public void setBeamMappingActive(boolean beamMappingActive) {
		this.beamMappingActive = beamMappingActive;
	}

	public double getReadoutTime() {
		return readoutTime;
	}

	public void setReadoutTime(double readoutTime) {
		this.readoutTime = readoutTime;
	}

	public List<ControllerConfiguration> getMotors() {
		return motors;
	}

	public void setMotors(List<ControllerConfiguration> motors) {
		this.motors = motors;
	}

	public Map<TriggerMode, Short> getTriggerMode() {
		return triggerMode;
	}

	public void setTriggerMode(Map<TriggerMode, Short> triggerMode) {
		this.triggerMode = triggerMode;
	}
}
