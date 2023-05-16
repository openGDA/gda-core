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

package uk.ac.gda.client.widgets;

import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.typed.BeanProperties;
import org.eclipse.core.databinding.conversion.IConverter;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.databinding.fieldassist.ControlDecorationSupport;
import org.eclipse.jface.databinding.swt.typed.WidgetProperties;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.api.camera.CameraControl;
import uk.ac.gda.api.camera.CameraControllerEvent;
import uk.ac.gda.api.camera.CameraState;

public class LiveStreamImagesComposite extends LiveStreamTextComposite {
	private static final Logger logger = LoggerFactory.getLogger(LiveStreamImagesComposite.class);

	private final CameraControlBindingImages cameraControlBinding;

	private static final String PROPERTY_NAME = "numImages";

	public LiveStreamImagesComposite(Composite parent, CameraControl cameraControl, boolean changeExposureWhileCameraAcquiring) {
		super(parent, cameraControl, changeExposureWhileCameraAcquiring, "Number of Images");
		cameraControlBinding = new CameraControlBindingImages();
		bindControls(parent);
	}

	@Override
	protected void bindControls(Composite parent) {
		final UpdateValueStrategy<String, Integer> setNumImagesStrategy = new UpdateValueStrategy<>();
		setNumImagesStrategy.setBeforeSetValidator(this::validateInput);
		final UpdateValueStrategy<Integer, String> setTextBoxStrategy = new UpdateValueStrategy<>();
		// setting converter because default converter would add a comma for numbers greater than a thousand
		setTextBoxStrategy.setConverter(IConverter.create(Integer.class, String.class, Object::toString));

		var cameraControlObservable = BeanProperties.value(CameraControlBindingImages.class, PROPERTY_NAME, Integer.class).observe(cameraControlBinding);
		var numImagesObservable = WidgetProperties.text(SWT.Modify).observe(text);
		var numImagesBinding = dataBindingContext.bindValue(numImagesObservable, cameraControlObservable, setNumImagesStrategy, setTextBoxStrategy);
		ControlDecorationSupport.create(numImagesBinding, SWT.LEFT | SWT.TOP);

		// Initialise number of images
		cameraControlBinding.getNumImages();

		cameraControl.addIObserver(cameraControlBinding);
		parent.addDisposeListener(e -> cameraControl.deleteIObserver(cameraControlBinding));

	}

	@Override
	IStatus validateInput(Object value) {
		try {
			final int numImages = Integer.parseInt(text.getText());
			return numImages > 0 ? ValidationStatus.ok() : ValidationStatus.error("Number of images cannot be less than zero");
		} catch (NumberFormatException e) {
			return ValidationStatus.error("Invalid input for number of images");
		}
	}

	private class CameraControlBindingImages extends CameraControlBinding {

		private int numImages;

		public int getNumImages() {
			try {
				numImages = cameraControl.getNumImages();
			} catch (Exception e) {
				logAndDisplayError(String.format("Error getting number of images on camera %s", cameraControl.getName()), e);
			}
			return numImages;
		}

		@SuppressWarnings("unused")
		public void setNumImages(int newNumImages) {
			try {
				if (!modifyWhileCameraAcquiring || cameraControl.getAcquireState() == CameraState.IDLE) {
					cameraControl.setNumImages(newNumImages);
				} else {
					int currentNumImages = cameraControl.getNumImages();
					text.setText(String.valueOf(currentNumImages));
					if (currentNumImages != newNumImages) {
						displayError("Cannot set number of images\n- camera is busy");
					}
				}
			} catch(Exception e) {
				logAndDisplayError(String.format("Error getting number of images on camera %s", cameraControl.getName()), e);
			}
		}

		@Override
		public void update(Object source, Object arg) {
			if (arg instanceof CameraControllerEvent event) {
				final double oldNumImages = numImages;
				numImages = event.getNumImages();
				// Update the text box if the exposure has been changed by an external event e.g. on the command line
				logger.debug("Number of images changed from {} to {}", oldNumImages, numImages);
				changeSupport.firePropertyChange(PROPERTY_NAME, oldNumImages, numImages);
			}
		}

		@Override
		public String toString() {
			return "CameraControlBinding [numImages=" + numImages + "]";
		}

	}


}
