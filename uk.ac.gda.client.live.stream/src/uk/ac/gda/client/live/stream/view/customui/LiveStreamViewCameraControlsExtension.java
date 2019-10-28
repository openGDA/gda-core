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

package uk.ac.gda.client.live.stream.view.customui;

import org.eclipse.swt.widgets.Composite;

import uk.ac.gda.api.camera.CameraControl;

public interface LiveStreamViewCameraControlsExtension {

	/**
	 * Create the UI associated with this extension
	 * <p>
	 * The parent composite will allocate one column to the UI. This class may, of course, subdivide this into multiple
	 * columns.
	 *
	 * @param composite
	 *            The parent composite on which to create thus UI
	 * @param cameraControl
	 *            Object to control the camera
	 */
	void createUi(Composite composite, CameraControl cameraControl);
}
