/*-
 * Copyright © 2018 Diamond Light Source Ltd.
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

package uk.ac.gda.client.widgets;

import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.typed.BeanProperties;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.databinding.fieldassist.ControlDecorationSupport;
import org.eclipse.jface.databinding.swt.typed.WidgetProperties;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import uk.ac.gda.api.camera.CameraControl;
import uk.ac.gda.api.camera.CameraControllerEvent;
import uk.ac.gda.api.camera.CameraState;

/**
 * Control the exposure time of a camera, warning the user if they have input an invalid exposure time
 */
public class LiveStreamExposureTimeComposite extends LiveStreamTextComposite {
	private static final Logger logger = LoggerFactory.getLogger(LiveStreamExposureTimeComposite.class);

	private final CameraControlBindingExposure cameraControlBinding;

	private static final String PROPERTY_NAME = "acquireTime";

	public LiveStreamExposureTimeComposite(Composite parent, CameraControl cameraControl, boolean changeExposureWhileCameraAcquiring) {
		super(parent, cameraControl, changeExposureWhileCameraAcquiring, "Exposure Time");
		cameraControlBinding = new CameraControlBindingExposure();
		bindControls(parent);
	}

	@Override
	protected void bindControls(Composite parent) {
		// Check the value entered for exposure time
		final UpdateValueStrategy<String, Double> setAcquireTimeStrategy = new UpdateValueStrategy<>();
		setAcquireTimeStrategy.setBeforeSetValidator(this::validateInput);
		// Nothing particular to check when binding from hardware value to text box
		final UpdateValueStrategy<Double, String> setTextBoxStrategy = new UpdateValueStrategy<>();

		// Set up data binding to keep camera and text box in synct
		final IObservableValue<Double> cameraControlObservable = BeanProperties.value(CameraControlBindingExposure.class, PROPERTY_NAME, Double.class).observe(cameraControlBinding);
		final IObservableValue<String> exposureTimeObservable = WidgetProperties.text(SWT.Modify).observe(text);
		final Binding exposureTimeBinding = dataBindingContext.bindValue(exposureTimeObservable, cameraControlObservable, setAcquireTimeStrategy, setTextBoxStrategy);
		ControlDecorationSupport.create(exposureTimeBinding, SWT.LEFT | SWT.TOP);

		// Initialise exposure time
		cameraControlBinding.getAcquireTime();

		// cameraControlBinding needs to listen for changes from the hardware
		cameraControl.addIObserver(cameraControlBinding);
		parent.addDisposeListener(e -> cameraControl.deleteIObserver(cameraControlBinding));

	}

	@Override
	protected IStatus validateInput(Object value) {
		try {
			final double exposureTime = Double.parseDouble(text.getText());
			return exposureTime > 0.0 ? ValidationStatus.ok() : ValidationStatus.error("Exposure time cannot be zero");
		} catch (Exception e) {
			return ValidationStatus.error("Invalid number for exposure time");
		}
	}

	/**
	 * Class for data binding to use to mediate between the camera control and the "Exposure time" text box
	 * <p>
	 * Functions marked as "unused" are in fact required for data binding to work.
	 * <p>
	 * As the acquisition time can also be set in the console or directly in Epics, this class needs to observe the
	 * hardware and respond to {@link CameraControllerEvent}s.
	 */
	private class CameraControlBindingExposure extends CameraControlBinding {
		// Allow for inaccuracy in floating point values
		private static final double FP_TOLERANCE = 1e-12;

		private double acquireTime;

		public double getAcquireTime() {
			try {
				// Always refresh acquireTime from the hardware
				acquireTime = cameraControl.getAcquireTime();
			} catch (DeviceException e) {
				logAndDisplayError(String.format("Error getting acquire time on camera %s", cameraControl.getName()), e);
			}
			return acquireTime;
		}

		@SuppressWarnings("unused")
		public void setAcquireTime(double newAcquireTime) {

			try {
				if (!modifyWhileCameraAcquiring) {
					if (cameraControl.getAcquireState() == CameraState.IDLE) {
						cameraControl.setAcquireTime(newAcquireTime);
					} else {
						double oldAcquireTime = cameraControl.getAcquireTime();
						text.setText(String.valueOf(oldAcquireTime));
						if (newAcquireTime != oldAcquireTime) {
							displayError("Cannot set exposure time\n- camera is busy");
						}
					}
				} else {
					cameraControl.setAcquireTime(newAcquireTime);
				}
			} catch (Exception e) {
				logAndDisplayError(String.format("Error setting acquire time on camera %s", cameraControl.getName()), e);
			}
		}

		@Override
		public void update(Object source, Object arg) {
			if (arg instanceof CameraControllerEvent event) {
				final double oldAcquireTime = acquireTime;
				acquireTime = event.getAcquireTime();
				// Update the text box if the exposure has been changed by an external event e.g. on the command line
				if (Math.abs(acquireTime - oldAcquireTime) > FP_TOLERANCE) {
					logger.debug("Acquire time changed from {} to {}", oldAcquireTime, acquireTime);
					changeSupport.firePropertyChange(PROPERTY_NAME, oldAcquireTime, acquireTime);
				}
			}
		}

		@Override
		public String toString() {
			return "CameraControlBinding [acquireTime=" + acquireTime + "]";
		}

	}
}
