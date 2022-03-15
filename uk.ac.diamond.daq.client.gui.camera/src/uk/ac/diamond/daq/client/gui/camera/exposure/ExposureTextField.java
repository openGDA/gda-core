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

package uk.ac.diamond.daq.client.gui.camera.exposure;

import static uk.ac.gda.ui.tool.ClientMessages.EMPTY_MESSAGE;
import static uk.ac.gda.ui.tool.ClientSWTElements.createClientText;

import java.text.DecimalFormat;
import java.util.function.Supplier;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;

import gda.device.DeviceException;
import uk.ac.diamond.daq.client.gui.camera.ICameraConfiguration;
import uk.ac.diamond.daq.client.gui.camera.event.CameraControlSpringEvent;
import uk.ac.diamond.daq.concurrent.Async;
import uk.ac.gda.api.camera.CameraState;
import uk.ac.gda.core.tool.spring.SpringApplicationContextFacade;
import uk.ac.gda.ui.tool.ClientVerifyListener;
import uk.ac.gda.ui.tool.WidgetUtilities;

/**
 * A text field to edit a camera exposure.
 *
 * <p>
 * The field is listening at {@link CameraControlSpringEvent} published by the control associated with the camera
 * </p>
 */
public class ExposureTextField {

	private static final Logger logger = LoggerFactory.getLogger(ExposureTextField.class);
	public static final  DecimalFormat decimalFormat = new DecimalFormat("#0.000");


	private final Text exposureText;

	private final Supplier<ICameraConfiguration> cameraConfigurationSupplier;


	/**
	 * Instantiates a text field to edit a camera exposure
	 * @param parent the {@code Composite} parent of the text field to build
	 * @param style the text field style
	 * @param cameraControlSupplier the supplier of the camera control for this camera
	 */
	public ExposureTextField(Composite parent, int style, Supplier<ICameraConfiguration> cameraControlSupplier) {
		exposureText = createClientText(parent, style, EMPTY_MESSAGE, ClientVerifyListener.verifyOnlyDoubleText);
		this.cameraConfigurationSupplier = cameraControlSupplier;
		SpringApplicationContextFacade.addDisposableApplicationListener(this, cameraControlSpringEventListener);
		bindElements();
	}

	private void bindElements() {
		// Initialise widget to current exposure
		cameraConfigurationSupplier.get().getCameraControl().ifPresent(control -> {
			try {
				updateGUI(control.getAcquireTime());
			} catch (DeviceException e) {
				logger.error("Could not read exposure time from {}", control.getName(), e);
			}
		});

		// Set the acquire time when user pushes return
		WidgetUtilities.addWidgetDisposableListener(exposureText, SWT.DefaultSelection,
				event -> setAcquireTime());

		// Set the acquire time when exposureText loses focus
		WidgetUtilities.addControlDisposableFocusListener(exposureText, event -> setAcquireTime(),
				event -> {/* do nothing */});
	}

	private void setAcquireTime() {
		var exposure = Double.parseDouble(exposureText.getText());
		Async.execute(() ->
			cameraConfigurationSupplier.get().getCameraControl().ifPresent(control -> {
				try {
					control.setAcquireTime(exposure);
					if (control.getAcquireState().equals(CameraState.ACQUIRING)) {
						control.stopAcquiring();
						control.startAcquiring();
					}
				} catch (DeviceException e) {
					logger.error("Error writing exposure", e);
				}
			})
		);
	}

	/**
	 * Returns the built text field
	 * @return the built text field
	 */
	public Text getExposure() {
		return exposureText;
	}

	// At the moment is not possible to use anonymous lambda expression because it
	// generates a class cast exception
	private ApplicationListener<CameraControlSpringEvent> cameraControlSpringEventListener = new ApplicationListener<CameraControlSpringEvent>() {
		@Override
		public void onApplicationEvent(CameraControlSpringEvent event) {
			if (cameraConfigurationSupplier.get().getCameraConfigurationProperties().getId().equals(event.getCameraId())) {
				Display.getDefault().asyncExec(() -> updateModelToGUI(event));
			}
		}

		private void updateModelToGUI(CameraControlSpringEvent e) {
			updateGUI(e.getAcquireTime());
		}

	};

	private void updateGUI(double exposure) {
		if (exposureText.isDisposed() || exposureText.isFocusControl() && Double.parseDouble(exposureText.getText()) != exposure) {
			return;
		}
		exposureText.setText(decimalFormat.format(exposure));
		exposureText.getParent().layout(true, true);
	}
}
