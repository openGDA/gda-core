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
import java.util.Optional;
import java.util.function.Supplier;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;

import gda.device.DeviceException;
import uk.ac.diamond.daq.client.gui.camera.event.CameraControlSpringEvent;
import uk.ac.gda.api.camera.CameraControl;
import uk.ac.gda.api.camera.CameraState;
import uk.ac.gda.client.UIHelper;
import uk.ac.gda.core.tool.spring.SpringApplicationContextFacade;
import uk.ac.gda.ui.tool.ClientVerifyListener;
import uk.ac.gda.ui.tool.WidgetUtilities;

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

	private final Supplier<Optional<CameraControl>> cameraControlSupplier;


	/**
	 * Instantiates a text field to edit a camera exposure
	 * @param parent the {@code Composite} parent of the text field to build
	 * @param style the text field style
	 * @param cameraControlSupplier the supplier of the camera control for this camera
	 */
	public ExposureTextField(Composite parent, int style, Supplier<Optional<CameraControl>> cameraControlSupplier) {
		exposureText = createClientText(parent, style, EMPTY_MESSAGE, ClientVerifyListener.verifyOnlyDoubleText);
		this.cameraControlSupplier = cameraControlSupplier;
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
		getCameraControl().ifPresent(c ->
			setAcquireTime(c, Double.parseDouble(Text.class.cast(widget).getText()))
		);
	}

	private void setAcquireTime(CameraControl cc, double exposure) {
		try {
			cc.setAcquireTime(exposure);
			if (CameraState.ACQUIRING.equals(cc.getAcquireState())) {
				cc.stopAcquiring();
				cc.startAcquiring();
			}
		} catch (NumberFormatException | DeviceException e) {
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
			 getCameraControl().ifPresent(cc -> {
				if (event.getName().equals(cc.getName())) {
					Display.getDefault().asyncExec(() -> updateModelToGUI(event));
				}
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

	private Optional<CameraControl> getCameraControl() {
		return cameraControlSupplier.get();
	}
}