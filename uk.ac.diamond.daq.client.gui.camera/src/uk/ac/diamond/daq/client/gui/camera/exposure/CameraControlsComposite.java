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

import static uk.ac.gda.ui.tool.ClientSWTElements.STRETCH;
import static uk.ac.gda.ui.tool.ClientSWTElements.composite;

import java.util.Objects;
import java.util.Optional;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import gda.rcp.views.CompositeFactory;
import uk.ac.diamond.daq.client.gui.camera.AcquireComposite;
import uk.ac.diamond.daq.client.gui.camera.CameraHelper;
import uk.ac.diamond.daq.client.gui.camera.ICameraConfiguration;
import uk.ac.diamond.daq.client.gui.camera.binning.BinningCompositeFactory;
import uk.ac.gda.api.camera.CameraControl;
import uk.ac.gda.api.camera.ImageMode;
import uk.ac.gda.client.live.stream.view.customui.LiveStreamViewCameraControlsScanListener;
import uk.ac.gda.client.properties.camera.CameraConfigurationProperties;
import uk.ac.gda.client.properties.camera.StreamConfiguration;
import uk.ac.gda.client.widgets.LiveStreamImagesComposite;

public class CameraControlsComposite implements CompositeFactory {

	private Composite composite;
	private CameraConfigurationProperties camera;

	private static final int NUM_COLUMNS = 2;

	public CameraControlsComposite(CameraConfigurationProperties camera) {
		this.camera = camera;
	}

	@Override
	public Composite createComposite(Composite parent, int style) {
		composite = composite(parent, NUM_COLUMNS);

		ICameraConfiguration iCameraConfiguration = CameraHelper.createICameraConfiguration(camera);
		Optional<CameraControl> cameraControl = iCameraConfiguration.getCameraControl();

		if (cameraControl.isPresent()) {
			createControls(cameraControl.get());
		}

		return composite;
	}

	private void createControls(CameraControl cameraControl) {
		boolean includeNumImagesComposite = hasMultipleImageMode();
		boolean includeBinningComposite = camera.isPixelBinningEditable();
		boolean includeAcquireComposite = Objects.nonNull(camera.getAcquisitionDeviceName());

		var cameraControls = new LiveStreamViewCameraControlsScanListener(camera, cameraControl);

		if (includeNumImagesComposite) {
			cameraControls.setIncludeExposureTimeControl(false);
			var imagesComposite = new LiveStreamImagesComposite(composite, cameraControl, false);
			GridDataFactory.swtDefaults().applyTo(imagesComposite);
		}

		cameraControls.createUi(composite);

		if (includeBinningComposite) {
			var binningCompositeArea = new BinningCompositeFactory(camera).createComposite(composite, SWT.IGNORE);
			STRETCH.copy().span(2, 0).applyTo(binningCompositeArea);
		}

		if (includeAcquireComposite) {
			new AcquireComposite(camera, cameraControl).createComposite(composite, SWT.IGNORE);
		}

		composite.addDisposeListener(e -> cameraControls.dispose());
	}

	private boolean hasMultipleImageMode() {
		var imageMode = Optional.ofNullable(camera.getStreamingConfiguration()).map(StreamConfiguration::getImageMode);
		return imageMode.isPresent() && imageMode.get().equals(ImageMode.MULTIPLE);
	}


}
