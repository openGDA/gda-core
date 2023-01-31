/*-
 * Copyright © 2023 Diamond Light Source Ltd.
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

import static uk.ac.gda.ui.tool.ClientSWTElements.STRETCH;
import static uk.ac.gda.ui.tool.ClientSWTElements.composite;
import static uk.ac.gda.ui.tool.ClientSWTElements.getImage;

import org.eclipse.scanning.api.device.IRunnableDeviceService;
import org.eclipse.scanning.api.device.models.IDetectorModel;
import org.eclipse.scanning.api.device.models.MalcolmModel;
import org.eclipse.scanning.api.event.scan.AcquireRequest;
import org.eclipse.scanning.api.event.status.Status;
import org.eclipse.scanning.device.ui.util.ScanningUiUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.rcp.views.CompositeFactory;
import uk.ac.gda.api.camera.CameraControl;
import uk.ac.gda.client.properties.camera.CameraConfigurationProperties;
import uk.ac.gda.core.tool.spring.SpringApplicationContextFacade;
import uk.ac.gda.ui.tool.images.ClientImages;
import uk.ac.gda.ui.tool.spring.ClientRemoteServices;

public class AcquireComposite implements CompositeFactory {

	private static final Logger logger = LoggerFactory.getLogger(AcquireComposite.class);
	private static final String ACQUISITION_FAILED_MESSAGE = "Acquisition failed - see logs for details";

	private final CameraConfigurationProperties detectorProperties;
	private final CameraControl cameraControl;

	public AcquireComposite(CameraConfigurationProperties detectorProperties, CameraControl cameraControl) {
		this.detectorProperties = detectorProperties;
		this.cameraControl = cameraControl;
	}

	@Override
	public Composite createComposite(Composite parent, int style) {
		var composite = composite(parent, 2, false);
		var button = new Button(composite, SWT.PUSH);
		var output = new Text(composite, SWT.BORDER | SWT.READ_ONLY);
		STRETCH.applyTo(output);
		button.setText("Acquire");
		var image = getImage(ClientImages.CAMERA);
		button.setImage(image);
		button.addSelectionListener(SelectionListener.widgetSelectedAdapter(event -> {
			var filePath = acquire();
			Display.getDefault().asyncExec(() ->
				output.setText(filePath));
		}));
		button.addDisposeListener(dispose -> image.dispose());

		return composite;
	}

	/**
	 * Submits an {@link AcquireRequest} which performs a single static point scan,
	 * and returns the output file path if successful, or error message otherwise
	 */
	private String acquire() {
		try {
			var detector = getRunnableDeviceService().getRunnableDevice(detectorProperties.getAcquisitionDeviceName());
			var model = (IDetectorModel) detector.getModel();

			setExposure(model, cameraControl.getAcquireTime());

			var request = ScanningUiUtils.acquireData(model);
			if (request.getStatus().equals(Status.COMPLETE)) {
				return request.getFilePath();
			} else {
				logger.error("Acquire request did not complete: {}", request.getMessage());
				return ACQUISITION_FAILED_MESSAGE;
			}
		} catch (Exception e) {
			logger.error("Error acquiring data: {}", e.getMessage());
			return ACQUISITION_FAILED_MESSAGE;
		}
	}

	private void setExposure(IDetectorModel model, double exposure) {
		if (model instanceof MalcolmModel malcolmModel) {
			malcolmModel.setExposureTime(0);
			malcolmModel.getDetectorModels().stream()
				.filter(det -> det.getName().equals(detectorProperties.getMalcolmDetectorName()))
				.findFirst().orElseThrow().setExposureTime(exposure);
		} else {
			model.setExposureTime(exposure);
		}
	}

	private IRunnableDeviceService getRunnableDeviceService() {
		return SpringApplicationContextFacade.getBean(ClientRemoteServices.class).getIRunnableDeviceService();
	}

}
