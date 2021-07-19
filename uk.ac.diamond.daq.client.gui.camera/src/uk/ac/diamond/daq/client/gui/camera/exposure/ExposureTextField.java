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
import org.eclipse.swt.widgets.Widget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;

import uk.ac.diamond.daq.client.gui.camera.ICameraConfiguration;
import uk.ac.diamond.daq.client.gui.camera.event.CameraControlSpringEvent;
import uk.ac.gda.api.camera.CameraState;
import uk.ac.gda.client.UIHelper;
import uk.ac.gda.client.exception.GDAClientRestException;
import uk.ac.gda.core.tool.spring.SpringApplicationContextFacade;
import uk.ac.gda.ui.tool.ClientVerifyListener;
import uk.ac.gda.ui.tool.WidgetUtilities;
import uk.ac.gda.ui.tool.rest.CameraControlClient;

/**
 * A text field to edit a camera exposure.
 *
 * <p>
 * The field is listening at {@link CameraControlSpringEvent} published by the control associated with the camera
 * </p>
 *
 * @author Maurizio Nagni
 */
public class ExposureTextField {

	private static final Logger logger = LoggerFactory.getLogger(ExposureTextField.class);
	public static final  DecimalFormat decimalFormat = new DecimalFormat("#0.000");


	private final Text exposureText;

	private final Supplier<ICameraConfiguration> iCameraConfigurationSupplier;


	/**
	 * Instantiates a text field to edit a camera exposure
	 * @param parent the {@code Composite} parent of the text field to build
	 * @param style the text field style
	 * @param cameraControlSupplier the supplier of the camera control for this camera
	 */
	public ExposureTextField(Composite parent, int style, Supplier<ICameraConfiguration> cameraControlSupplier) {
		exposureText = createClientText(parent, style, EMPTY_MESSAGE, ClientVerifyListener.verifyOnlyDoubleText);
		this.iCameraConfigurationSupplier = cameraControlSupplier;
		SpringApplicationContextFacade.addDisposableApplicationListener(this, cameraControlSpringEventListener);
		bindElements();
	}

	private void bindElements() {
		// Sets the acquire time when user pushes return
		WidgetUtilities.addWidgetDisposableListener(exposureText, SWT.DefaultSelection,
				event -> setAcquireTime(event.widget));

		// Set the acquire time when exposureText looses focus
		WidgetUtilities.addControlDisposableFocusListener(exposureText, event -> setAcquireTime(event.widget),
				event -> {
				});
	}

	private void setAcquireTime(Widget widget) {
		iCameraConfigurationSupplier.get()
			.getCameraControlClient().ifPresent(c -> {
				String text = Text.class.cast(widget).getText();
				if (!text.isEmpty())
					setAcquireTime(c, Double.parseDouble(text));
			});
	}

	private void setAcquireTime(CameraControlClient cc, double exposure) {
		try {
			cc.setAcquireTime(exposure);
			if (CameraState.ACQUIRING.equals(cc.getAcquireState())) {
				cc.stopAcquiring();
				cc.startAcquiring();
			}
		} catch (NumberFormatException | GDAClientRestException e) {
			UIHelper.showError("Cannot update acquisition time", e, logger);
		}
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
			Display.getDefault().asyncExec(() -> {
				if (iCameraConfigurationSupplier.get().getCameraConfigurationProperties().getId().equals(event.getCameraId()))
					updateModelToGUI(event);
			});
		}

		private void updateModelToGUI(CameraControlSpringEvent e) {
			updateGUI(e.getAcquireTime());
		}

		private void updateGUI(double exposure) {
			if (exposureText.isDisposed() || exposureText.isFocusControl() && Double.parseDouble(exposureText.getText()) != exposure) {
				return;
			}
			exposureText.setText(decimalFormat.format(exposure));
			exposureText.getParent().layout(true, true);
		}
	};
}
