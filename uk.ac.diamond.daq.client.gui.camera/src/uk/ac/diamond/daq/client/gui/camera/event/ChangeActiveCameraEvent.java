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

import java.util.Optional;
import java.util.UUID;

import uk.ac.diamond.daq.client.gui.camera.CameraHelper;
import uk.ac.gda.client.properties.camera.CameraConfigurationProperties;

/**
 *  Published when the active camera changes.
 *  The cameraIndex is specified into a CameraComboItem.
 *  See {@link CameraHelper}
 *
 * @author Maurizio Nagni
 */
public class ChangeActiveCameraEvent extends RootCompositeEvent {

	private static final long serialVersionUID = -933748166416333930L;
	private final CameraConfigurationProperties activeCamera;

    /**
     * @param source the component which published the event
     * @param activeCamera the component active camera
     * @param rootComposite the component id
     */
    public ChangeActiveCameraEvent(Object source, CameraConfigurationProperties activeCamera, Optional<UUID> rootComposite) {
		super(source, rootComposite);
		this.activeCamera = activeCamera;
	}

	public CameraConfigurationProperties getActiveCamera() {
		return activeCamera;
	}
}
