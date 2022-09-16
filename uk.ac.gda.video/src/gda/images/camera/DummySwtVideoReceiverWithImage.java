/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

package gda.images.camera;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.Display;

import gda.device.DeviceException;

public class DummySwtVideoReceiverWithImage extends DummyVideoReceiverBase<ImageData>{

	private String imageFile;
	private Image image;

	public DummySwtVideoReceiverWithImage(String imageFile) {
		this.imageFile = imageFile;
	}

	@Override
	public ImageData getImage() throws DeviceException {
		return image.getImageData();
	}

	@Override
	protected void createInitialImage() {
		Display display = Display.getDefault();
		image = new Image(display, new ImageData(imageFile));
	}

	@Override
	protected ImageData updateImage() {
		try {
			return getImage();
		} catch (DeviceException e) {
			return null;
		}
	}

}
