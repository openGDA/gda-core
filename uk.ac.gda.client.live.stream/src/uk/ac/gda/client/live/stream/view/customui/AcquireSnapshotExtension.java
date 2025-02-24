/*-
 * Copyright © 2024 Diamond Light Source Ltd.
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

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.api.camera.CameraControl;
import uk.ac.gda.api.camera.ImageMode;
import uk.ac.gda.client.live.stream.Activator;

/**
 * An extension to trigger the acquisition of a single frame.
 * This class is constructed with its own CameraControl instance
 * because it is common for the parent controls to be using an instance
 * which internally forces image mode on capture.
 */
public class AcquireSnapshotExtension implements LiveStreamViewCameraControlsExtension {

	private static final Logger logger = LoggerFactory.getLogger(AcquireSnapshotExtension.class);

	private final CameraControl snapshotCameraControl;

	public AcquireSnapshotExtension(CameraControl snapshotCameraControl) {
		this.snapshotCameraControl = snapshotCameraControl;
	}

	@Override
	public void createUi(Composite composite, CameraControl cameraControl) {

		var snapshot = new Button(composite, SWT.PUSH);

		snapshot.setText("Snapshot");
		var image = Activator.createImage("picture-sunset.png");
		snapshot.setImage(image);
		composite.addDisposeListener(dispose -> image.dispose());

		snapshot.addListener(SWT.Selection, event -> snap());
	}

	private void snap() {
		try {
			snapshotCameraControl.stopAcquiring();
			snapshotCameraControl.setImageMode(ImageMode.SINGLE);
			snapshotCameraControl.startAcquiring();
		} catch (Exception e) {
			logger.error("Failed to take snapshot!", e);
		}
	}

}
