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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.images.camera.FrameStatistics;
import gda.images.camera.ImageListener;
import gda.images.camera.MotionJpegOverHttpReceiverBase;

/**
 * Takes decoded frames from a queue and dispatches them to {@link ImageListener}s.
 */
public class FrameDispatchTask<E> implements Runnable {

	private static final Logger logger = LoggerFactory.getLogger(FrameDispatchTask.class);

	private BlockingQueue<E> receivedImages;

	private Set<ImageListener<E>> listeners;

	/**
	 * Last image received from the stream.
	 */
	private AtomicReference<E> lastImage;

	/**
	 * Creates a frame dispatch task.
	 *
	 * @param receivedImages
	 *            queue containing received frames
	 * @param listeners
	 *            objects to which images should be dispatched
	 * @param lastImage
	 *            a reference to the latest image received from the stream; updated as each image is removed from the
	 *            queue
	 */
	public FrameDispatchTask(BlockingQueue<E> receivedImages, Set<ImageListener<E>> listeners,
			AtomicReference<E> lastImage) {
		this.receivedImages = receivedImages;
		this.listeners = listeners;
		this.lastImage = lastImage;
	}

	private volatile boolean keepRunning;

	private FrameStatistics frameDispatchStatistics = new FrameStatistics("MJPEG dispatch", 300, 15);

	@Override
	public void run() {
		logger.debug("Frame dispatch starting");

		frameDispatchStatistics.reset();

		keepRunning = true;
		while (keepRunning) {
			try {
				E image = receivedImages.take();
				// We are only interested the latest image
				List<E> c = new ArrayList<>();
				receivedImages.drainTo(c);
				if (!c.isEmpty()) {
					image = c.get(c.size() - 1);
				}

				if (MotionJpegOverHttpReceiverBase.SHOW_STATS) {
					frameDispatchStatistics.startProcessingFrame();
				}

				lastImage.set(image);
				dispatchImage(image);

				if (MotionJpegOverHttpReceiverBase.SHOW_STATS) {
					frameDispatchStatistics.finishProcessingFrame();
				}
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}

		logger.debug("Frame dispatch stopping");
	}

	void dispatchImage(E image) {
		if (keepRunning) {
			for (ImageListener<E> listener : listeners) {
				listener.processImage(image);
			}
		}
		// frameDispatchStatistics.frameProcessed();
	}

	public void shutdown() {
		this.keepRunning = false;
	}
}
