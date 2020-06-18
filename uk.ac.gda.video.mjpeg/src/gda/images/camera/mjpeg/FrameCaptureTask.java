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

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.images.camera.FrameStatistics;
import gda.images.camera.MotionJpegOverHttpReceiverBase;

/**
 * Task that captures frames from the MJPEG stream. A task is created to decode each frame, and the frame is added to a
 * queue of received frames for later dispatch.
 */
public abstract class FrameCaptureTask<E> implements Runnable {

	private static final Logger logger = LoggerFactory.getLogger(FrameCaptureTask.class);

	private String urlSpec;

	private ExecutorService imageDecodingService;

	private BlockingQueue<Future<E>> receivedImages;

	private final int readTimeout;

	private final boolean acceptReadTimeout;

	private MjpegInputStream mjpegStream;

	private volatile boolean keepRunning;

	private FrameStatistics frameCaptureStatistics = new FrameStatistics("MJPEG capture", 300, 15);

	private FrameStatistics frameDecodeStatistics = new FrameStatistics("MJPEG decoding", 300, 15);

	public FrameCaptureTask(String urlSpec, ExecutorService imageDecodingService,
			BlockingQueue<Future<E>> receivedImages) {
		this(urlSpec, imageDecodingService, receivedImages,0,false);
	}
	/**
	 *
	 * @param urlSpec
	 * @param imageDecodingService
	 * @param receivedImages
	 * @param readTimeout connection read timeout in ms. If 0 then no timeout. Less than 0 is unacceptable
	 * @param acceptReadTimeout if true socketReadTimeouts do not cause the thread to abort
	 */
	public FrameCaptureTask(String urlSpec, ExecutorService imageDecodingService,
			BlockingQueue<Future<E>> receivedImages, int readTimeout, boolean acceptReadTimeout) {
		this.urlSpec = urlSpec;
		this.imageDecodingService = imageDecodingService;
		this.receivedImages = receivedImages;
		this.readTimeout = readTimeout;
		this.acceptReadTimeout = acceptReadTimeout;
	}

	@Override
	public void run() {
		try {
			while(true){
				try{
					receiveImages();
				} catch(SocketTimeoutException e){
					if (acceptReadTimeout){
						logger.debug("Ignoring SocketTimeOutException");
					} else {
						throw e;
					}
				}
			}
		} catch (Throwable e) {
			logger.error("Unable to capture frames from the MJPEG stream", e);
			shutdown();
		}
	}

	private void receiveImages() throws Exception {
		logger.debug("Frame capture starting");
		ImageIO.setUseCache(false); // Provides a slight performance increase
		frameCaptureStatistics.reset();
		frameDecodeStatistics.reset();
		URL url = new URL(urlSpec);
		URLConnection conn = url.openConnection();
		conn.setReadTimeout(readTimeout);
		keepRunning = true;
		try {
			mjpegStream = new MjpegInputStream(conn.getInputStream());
			while (keepRunning) {
				if (MotionJpegOverHttpReceiverBase.SHOW_STATS) {
					frameCaptureStatistics.startProcessingFrame();
				}
				try (ByteArrayInputStream jpegBytes = new ByteArrayInputStream(mjpegStream.getNextFrame())) {
					Runnable decodeTask = () -> convertBytesToImage(jpegBytes);

					// We might be in the middle of capturing a frame from the stream when the image decoding service
					// is shut down
					if (!imageDecodingService.isShutdown()) {
						imageDecodingService.submit(decodeTask);
					}

					if (MotionJpegOverHttpReceiverBase.SHOW_STATS) {
						frameCaptureStatistics.finishProcessingFrame();
					}
				}
			}
		} finally {
			logger.debug("Frame capture stopping");
			shutdown();

		}
	}

	/**
	 * The resulting image is not returned but rather added to the received images queue.
	 * <p>
	 * Once the frame has been decoded add it to the receivedImages queue Do it here rather
	 * than adding the result of imageDecodingService.submit(decodeTask);
	 * to allow the decode task to be cancelled without the need to clear the associated
	 * future on the receivedImages queue.
	 */
	private void convertBytesToImage(final ByteArrayInputStream bias) {
		if (MotionJpegOverHttpReceiverBase.SHOW_STATS) {
			frameDecodeStatistics.startProcessingFrame();
		}
		try {
			BufferedImage bufferedImage = ImageIO.read(bias);
			E decodedImage = convertImage(bufferedImage);
			receivedImages.offer(CompletableFuture.completedFuture(decodedImage));
		} catch (IOException e) {
			// Shutdown if we get an unencodable image
			logger.error("Failed to convert image", e);
			shutdown();
		}

		if (MotionJpegOverHttpReceiverBase.SHOW_STATS) {
			frameDecodeStatistics.finishProcessingFrame();
		}
	}

	/**
	 * Should be implemented by subclasses to convert the specified {@link BufferedImage} to desired image type.
	 */
	protected abstract E convertImage(BufferedImage imageData);


	public void shutdown() {
		keepRunning = false;
		if (mjpegStream != null) {
			try {
				mjpegStream.close();
			} catch (Exception e) {
				logger.error("Error closing mjpeg sream", e);
			} finally {
				mjpegStream = null;
			}
		}
	}
}
