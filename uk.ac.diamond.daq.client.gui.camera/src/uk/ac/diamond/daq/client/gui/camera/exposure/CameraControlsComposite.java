/*-
 * Copyright © 2022 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.client.gui.camera.exposure;

import static uk.ac.gda.ui.tool.ClientSWTElements.createClientCompositeWithGridLayout;
import static uk.ac.gda.ui.tool.ClientSWTElements.createClientGridDataFactory;

import java.util.Optional;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import gda.rcp.views.CompositeFactory;
import uk.ac.diamond.daq.client.gui.camera.CameraHelper;
import uk.ac.diamond.daq.client.gui.camera.ICameraConfiguration;
import uk.ac.diamond.daq.client.gui.camera.binning.BinningCompositeFactory;
import uk.ac.gda.api.camera.CameraControl;
import uk.ac.gda.client.live.stream.view.customui.LiveStreamViewCameraControlsScanListener;
import uk.ac.gda.client.properties.camera.CameraConfigurationProperties;

public class CameraControlsComposite implements CompositeFactory {

	private CameraConfigurationProperties camera;

	public CameraControlsComposite(CameraConfigurationProperties camera) {
		this.camera = camera;
	}

	@Override
	public Composite createComposite(Composite parent, int style) {
		Composite composite = createClientCompositeWithGridLayout(parent, style, 1);
		createClientGridDataFactory().applyTo(composite);

		ICameraConfiguration iCameraConfiguration = CameraHelper.createICameraConfiguration(camera);
		Optional<CameraControl> cameraControl = iCameraConfiguration.getCameraControl();

		if (cameraControl.isPresent()) {
			LiveStreamViewCameraControlsScanListener cameraControls = new LiveStreamViewCameraControlsScanListener(camera, cameraControl.get());
			cameraControls.createUi(composite);
			composite.addDisposeListener(e -> cameraControls.dispose());

			Composite binningCompositeArea = new BinningCompositeFactory(camera).createComposite(composite, style);
			createClientGridDataFactory().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(binningCompositeArea);

		}
		return composite;
	}
}
