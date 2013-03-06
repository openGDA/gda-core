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

import gda.images.camera.mjpeg.FrameCaptureTask;
import gda.images.camera.mjpeg.SwtFrameCaptureTaskFalseColours;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.eclipse.swt.graphics.ImageData;

/**
 *
 */
public class MotionJpegOverHttpReceiverSwtFalseColours extends MotionJpegOverHttpReceiverSwt {

	private int redMask;
	private int greenMask;
	private int blueMask;
	private SwtFrameCaptureTaskFalseColours swtFrameCaptureTaskFalseColours;

	public MotionJpegOverHttpReceiverSwtFalseColours() {
	}

	@Override
	protected FrameCaptureTask<ImageData> createFrameCaptureTask(String urlSpec, ExecutorService imageDecodingService,
			BlockingQueue<Future<ImageData>> receivedImages) {
		swtFrameCaptureTaskFalseColours = new SwtFrameCaptureTaskFalseColours(urlSpec, imageDecodingService,
				receivedImages, redMask, greenMask, blueMask);
		return swtFrameCaptureTaskFalseColours;
	}

	public FrameCaptureTask<ImageData> createFrameCaptureTask(String urlSpec, ExecutorService imageDecodingService,
			BlockingQueue<Future<ImageData>> receivedImages, int redMask, int greenMask, int blueMask) {

		this.redMask = redMask;
		this.greenMask = greenMask;
		this.blueMask = blueMask;
		return createFrameCaptureTask(urlSpec, imageDecodingService, receivedImages);

	}

	public void setRedMask(int redMask) {
		this.redMask = redMask;
		swtFrameCaptureTaskFalseColours.setRedMask(redMask);
	}

	public void setGreenMask(int greenMask) {
		this.greenMask = greenMask;
		swtFrameCaptureTaskFalseColours.setGreenMask(greenMask);
	}

	public void setBlueMask(int blueMask) {
		this.blueMask = blueMask;
		swtFrameCaptureTaskFalseColours.setBlueMask(blueMask);
	}

}
