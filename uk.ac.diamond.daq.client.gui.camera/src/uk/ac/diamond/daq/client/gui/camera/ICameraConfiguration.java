/*-
 * Copyright © 2020 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.client.gui.camera;

import java.util.Optional;

import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;

import uk.ac.diamond.daq.client.gui.camera.beam.BeamCameraMapping;
import uk.ac.gda.api.camera.CameraControl;
import uk.ac.gda.client.exception.GDAClientException;
import uk.ac.gda.client.live.stream.view.CameraConfiguration;
import uk.ac.gda.client.properties.camera.CameraConfigurationProperties;
import uk.ac.gda.client.properties.camera.CameraToBeamMap;
import uk.ac.gda.ui.tool.rest.CameraControlClient;

/**
 * Provides information about a camera
 *
 * @see CameraHelper
 * @author Maurizio Nagni
 */
public interface ICameraConfiguration {
	/**
	 * The index assigned by the configuration to the camera
	 *
	 * @return the index assigned to this camera
	 */
	int getCameraIndex();

	/**
	 * @return camera configuration related to streaming
	 */
	Optional<CameraConfiguration> getCameraConfiguration();

	/**
	 * @return an interface allowing access to basic camera operations
	 */
	Optional<CameraControl> getCameraControl();

	Optional<CameraControlClient> getCameraControlClient();

	/**
	 * @return the maximum size of ROI permitted
	 * @throws GDAClientException
	 */
	RectangularROI getMaximumSizedROI() throws GDAClientException;

	/**
	 * @return the client camera configuration.<br>
	 *         See <a href= "https://confluence.diamond.ac.uk/display/DIAD/K11+GDA+Properties">
	 */
	CameraConfigurationProperties getCameraConfigurationProperties();

	/**
	 * When the beam illuminating the sample is driven by X/Y motors, this method returns a class containing information
	 * about the transformation between the two spaces defined by the drivers and the camera
	 *
	 * @return the transformation map (as described above)
	 */
	CameraToBeamMap getBeamCameraMap();

	BeamCameraMapping getBeamCameraMapping();
}
