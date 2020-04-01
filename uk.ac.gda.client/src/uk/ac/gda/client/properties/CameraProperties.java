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

package uk.ac.gda.client.properties;

import java.util.List;
import java.util.Optional;

/**
 * Defines the camera properties required by the GUI to expose a camera
 *
 * @author Maurizio Nagni
 */
public interface CameraProperties {

	/**
	 * Identifies a camera in its context.
	 *
	 * @return the camera index number
	 */
	int getIndex();

	/**
	 * Identifies a camera by its ID. As the {@link Optional} return type indicate, this is still not a required
	 * property
	 *
	 * @return the camera index number
	 */
	Optional<String> getId();

	/**
	 * The context specific camera label.
	 *
	 * @return the label used for the camera in the GUI
	 */
	String getName();

	/**
	 * The client side Spring bean associated with a <i>uk.ac.gda.client.live.stream.view.CameraConfiguration</i>
	 * (already defined in XML or programmatically)
	 *
	 * @return the bean id
	 */
	String getCameraConfiguration();

	/**
	 * The server side Spring bean associated with a <i>uk.ac.gda.epics.camera.EpicsCameraControl</i> (already defined
	 * in XML or programmatically)
	 *
	 * @return the bean id
	 */
	String getCameraControl();

	/**
	 * The motors associated with the camera
	 *
	 * @return a list of motors in the camera context
	 */
	List<MotorProperties> getMotorProperties();

	/**
	 * Makes the camera eligible for beam mapping. For more information read
	 * <a href="https://confluence.diamond.ac.uk/display/DIAD/Beam-Camera+mapping">this documentation</a>.
	 *
	 * @return {@code true} if enabled, {@code false} otherwise
	 */
	boolean isBeamMappingActive();
}
