/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package gda.images.camera.mjpeg;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;

public class AwtFrameCaptureTask extends FrameCaptureTask<Image> {

	public AwtFrameCaptureTask(String urlSpec, ExecutorService imageDecodingService, BlockingQueue<Image> receivedImages) {
		super(urlSpec, imageDecodingService, receivedImages);
	}

	@Override
	protected Image convertImage(BufferedImage imageData) {
		return imageData;
	}

}
