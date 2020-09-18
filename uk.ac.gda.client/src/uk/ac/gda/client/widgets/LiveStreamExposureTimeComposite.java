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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Objects;

import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.jface.databinding.fieldassist.ControlDecorationSupport;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.observable.IObserver;
import uk.ac.gda.api.camera.CameraControl;
import uk.ac.gda.api.camera.CameraControllerEvent;
import uk.ac.gda.api.camera.CameraState;

/**
 * Control the exposure time of a camera, warning the user if they have input an invalid exposure time
 */
public class LiveStreamExposureTimeComposite extends Composite {
	private static final Logger logger = LoggerFactory.getLogger(LiveStreamExposureTimeComposite.class);

	/**
	 * Width of {@link #exposureTimeText}
	 */
	private static final int TEXT_WIDTH = 70;

	/**
	 * Text box to edit the exposure time
	 */
	private final Text exposureTimeText;

	private final CameraControl cameraControl;

	private final CameraControlBinding cameraControlBinding;

	private final DataBindingContext dataBindingContext;

	private final boolean changeExposureWhileCameraAcquiring;

	public LiveStreamExposureTimeComposite(Composite parent, int style, CameraControl cameraControl, boolean changeExposureWhileCameraAcquiring) {
		super(parent, style);
		Objects.requireNonNull(cameraControl, "Camera control must not be null");
		this.cameraControl = cameraControl;
		GridLayoutFactory.swtDefaults().numColumns(3).applyTo(this);

		dataBindingContext = new DataBindingContext();
		cameraControlBinding = new CameraControlBinding();
		this.changeExposureWhileCameraAcquiring = changeExposureWhileCameraAcquiring;

		final Label label = new Label(this, SWT.NONE);
		GridDataFactory.swtDefaults().applyTo(label);
		label.setText("Exposure time");

		exposureTimeText = new Text(this, SWT.BORDER);
		GridDataFactory.swtDefaults().hint(TEXT_WIDTH, SWT.DEFAULT).applyTo(exposureTimeText);

		// Get the initial exposure time from the camera and display in the page
		try {
			final double acquireTime = cameraControl.getAcquireTime();
			exposureTimeText.setText(Double.toString(acquireTime));
			cameraControlBinding.setAcquireTime(acquireTime);
			logger.debug("Acquire time set initially to {}", acquireTime);
		} catch (DeviceException e) {
			final String message = String.format("Error getting exposure time from camera %s", cameraControl.getName());
			logger.error(message, e);
			exposureTimeText.setText("#ERR");
			displayError(message);
		}

		// Check the value entered for exposure time
		final UpdateValueStrategy setAcquireTimeStrategy = new UpdateValueStrategy();
		setAcquireTimeStrategy.setBeforeSetValidator(value -> {
			try {
				final double exposureTime = Double.parseDouble(exposureTimeText.getText());
				return exposureTime > 0.0 ? ValidationStatus.ok() : ValidationStatus.error("Exposure time cannot be zero");
			} catch (Exception e) {
				return ValidationStatus.error("Invalid number for exposure time");
			}
		});

		// Nothing particular to check when binding from hardware value to text box
		final UpdateValueStrategy setTextBoxStrategy = new UpdateValueStrategy();

		// Set up data binding to keep camera and text box in sync
		@SuppressWarnings("unchecked")
		final IObservableValue<Double> cameraControlObservable = BeanProperties.value(CameraControlBinding.class, "acquireTime").observe(cameraControlBinding);
		@SuppressWarnings("unchecked")
		final IObservableValue<String> exposureTimeObservable = WidgetProperties.text(SWT.Modify).observe(exposureTimeText);
		final Binding exposureTimeBinding = dataBindingContext.bindValue(exposureTimeObservable, cameraControlObservable, setAcquireTimeStrategy, setTextBoxStrategy);
		ControlDecorationSupport.create(exposureTimeBinding, SWT.LEFT | SWT.TOP);

		// cameraControlBinding needs to listen for changes from the hardware
		cameraControl.addIObserver(cameraControlBinding);
		parent.addDisposeListener(e -> cameraControl.deleteIObserver(cameraControlBinding));
	}

	private void displayError(final String message) {
		MessageDialog.openError(Display.getDefault().getActiveShell(), "Camera error", message);
	}

	/**
	 * Class for data binding to use to mediate between the camera control and the "Exposure time" text box
	 * <p>
	 * Functions marked as "unused" are in fact required for data binding to work.
	 * <p>
	 * As the acquisition time can also be set in the console or directly in Epics, this class needs to observe the
	 * hardware and respond to {@link CameraControllerEvent}s.
	 */
	private class CameraControlBinding implements IObserver {
		// Allow for inaccuracy in floating point values
		private static final double FP_TOLERANCE = 1e-12;

		private double acquireTime;

		private final PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);

	    @SuppressWarnings("unused")
		public void addPropertyChangeListener(PropertyChangeListener listener) {
	        changeSupport.addPropertyChangeListener(listener);
	    }

	    @SuppressWarnings("unused")
		public void removePropertyChangeListener(PropertyChangeListener listener) {
	        changeSupport.removePropertyChangeListener(listener);
	    }

	    @SuppressWarnings("unused")
		public double getAcquireTime() {
			try {
				// Always refresh acquireTime from the hardware
				acquireTime = cameraControl.getAcquireTime();
			} catch (DeviceException e) {
				final String message = String.format("Error getting acquire time on camera %s", cameraControl.getName());
				logger.error(message, e);
				displayError(message);
			}
			return acquireTime;
		}

		public void setAcquireTime(double acquireTime) {
			try {
				if(!changeExposureWhileCameraAcquiring) {
					if (cameraControl.getAcquireState() == CameraState.IDLE) {
						cameraControl.setAcquireTime(acquireTime);
					} else {
						displayError("Cannot set exposure time\n- camera is busy");
					}
				} else {
					cameraControl.setAcquireTime(acquireTime);
				}
			} catch (Exception e) {
				final String message = String.format("Error setting acquire time on camera %s", cameraControl.getName());
				logger.error(message, e);
				displayError(message);
			}
		}

		@Override
		public void update(Object source, Object arg) {
			if (arg instanceof CameraControllerEvent) {
				final double oldAcquireTime = acquireTime;
				acquireTime = ((CameraControllerEvent) arg).getAcquireTime();
				// Update the text box if the exposure has been changed by an external event e.g. on the command line
				if (Math.abs(acquireTime - oldAcquireTime) > FP_TOLERANCE) {
					logger.debug("Acquire time changed from {} to {}", oldAcquireTime, acquireTime);
					changeSupport.firePropertyChange("acquireTime", oldAcquireTime, acquireTime);
				}
			}
		}

		@Override
		public String toString() {
			return "CameraControlBinding [acquireTime=" + acquireTime + "]";
		}
	}
}
