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

import org.eclipse.scanning.api.device.IRunnableDevice;
import org.eclipse.scanning.api.event.scan.AcquireRequest;

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

	private String malcolmDetectorName;
	private String gdaDetectorName;

	/** Name of associated {@link IRunnableDevice} which may be used to submit {@link AcquireRequest}s. */
	private String acquisitionDeviceName;


	private List<ControllerConfiguration> motors;

	private StreamConfiguration streamingConfiguration;

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

	/**
	 * The detector name, from the Malcolm DetectorTable, which accepts <i>exposure</i> property.
	 * (from the web gui, http://beamline-control:8008/gui/MALCOLM_ID --> Malcolm --> Detectors --> Edit)
	 * <p>
	 * <b>NOTE</b> this is a temporary solution to mitigate the case where any element in the Malcolm DetectorTable
	 * is parsed as IMalcolmDetectorModel however not all the element in the MalcolmDetector table contains the properties
	 * defined by the IMalcolmDetectorModel.
	 * One consequence of this is that using IMalcolmDetectorModel.setExposureTime (on the GDA side)
	 * on an element which does not expect that value (on the Malcolm side), makes GDA generate a message that Malcolm will consider invalid.
	 * (caused by bug K11-1228)
	 * </p>
	 * @return this detector name in the Malcolm's detectorTable
	 */
	public String getMalcolmDetectorName() {
		return malcolmDetectorName;
	}

	public void setMalcolmDetectorName(String malcolmDetectorName) {
		this.malcolmDetectorName = malcolmDetectorName;
	}

	/**
	 * Correspond to the prefix used to define the detector in GDA server.
	 * <p>
	 * For example, for camera beans like <i>pilatus_adbase</i> or <i>pilatus_roi</i>, it corresponds to {@code pilatus}.
	 * In ths way the any available service can retrieve the associated services as {@code adbase} or {@code roi}.
	 * </p>
	 * @return a string used to prefix the detector plugins
	 */
	public String getGdaDetectorName() {
		return gdaDetectorName;
	}

	public void setGdaDetectorName(String gdaDetectorName) {
		this.gdaDetectorName = gdaDetectorName;
	}

	public List<ControllerConfiguration> getMotors() {
		return motors;
	}

	public void setMotors(List<ControllerConfiguration> motors) {
		this.motors = motors;
	}

	public StreamConfiguration getStreamingConfiguration() {
		return streamingConfiguration;
	}

	public void setStreamingConfiguration(StreamConfiguration streamingConfiguration) {
		this.streamingConfiguration = streamingConfiguration;
	}

	public String getAcquisitionDeviceName() {
		return acquisitionDeviceName;
	}

	public void setAcquisitionDeviceName(String acquisitionDeviceName) {
		this.acquisitionDeviceName = acquisitionDeviceName;
	}

}
