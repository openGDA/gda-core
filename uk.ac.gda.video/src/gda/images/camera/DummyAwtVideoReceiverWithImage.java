/*-
 * Copyright © 2017 Diamond Light Source Ltd.
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

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;

public class DummyAwtVideoReceiverWithImage extends DummyVideoReceiverBase<Image> {

	private static final Logger logger = LoggerFactory.getLogger(DummyAwtVideoReceiverWithImage.class);

	private String imageFile;
	private BufferedImage image;

	public DummyAwtVideoReceiverWithImage(String imageFile) {
		this.imageFile = imageFile;
	}

	@Override
	public Image getImage() throws DeviceException {
		return image;
	}

	@Override
	public String getDisplayName() {
		return "DummyAwtVideoWithImage";
	}

	@Override
	protected void createInitialImage() {
		try {
			image = ImageIO.read(new File(imageFile));
		} catch (IOException e) {
			logger.error("Could not load initial image file for " + getDisplayName(), e);
		}
	}

	@Override
	protected Image updateImage() {
		return image;
	}

}
