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
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.ClosedByInterruptException;
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
 *
 * The stream read has an infinite timeout. To stop the task, it should be interrupted as the
 * socket read will block forever if there are currently no new frames.
 *
 */
public abstract class FrameCaptureTask<E> implements Runnable {

	private static final Logger logger = LoggerFactory.getLogger(FrameCaptureTask.class);

	private String urlSpec;

	private ExecutorService imageDecodingService;

	private BlockingQueue<Future<E>> receivedImages;

	private volatile boolean keepRunning;

	private FrameStatistics frameCaptureStatistics = new FrameStatistics("MJPEG capture", 300, 15);

	private FrameStatistics frameDecodeStatistics = new FrameStatistics("MJPEG decoding", 300, 15);

	protected FrameCaptureTask(String urlSpec, ExecutorService imageDecodingService,
			BlockingQueue<Future<E>> receivedImages) {
		this.urlSpec = urlSpec;
		this.imageDecodingService = imageDecodingService;
		this.receivedImages = receivedImages;
	}

	/**
	 * Create a {@link MjpegInputStream} from a {@link URLConnection}. Read frames from this stream
	 * until thread is interrupted or an error occurs.
	 */
	@Override
	public void run() {
		logger.debug("Frame capture starting for {}", urlSpec);
		ImageIO.setUseCache(false); // Provides a slight performance increase
		frameCaptureStatistics.reset();
		frameDecodeStatistics.reset();
		URL url;
		try {
			// TODO replace urlSpec with a URL field instead
			url = new URL(urlSpec);
		} catch (MalformedURLException e) {
			logger.error("Malformed MJpeg URL: {}", urlSpec);
			return;
		}
		keepRunning = true;
		MjpegInputStream mjpegStream = new MjpegInputStream(url);
		try (mjpegStream) {
			mjpegStream.connect();
			while (keepRunning) {
				readFrameFromStream(mjpegStream);
			}
		} catch (IOException e) {
			// Log any other error before shutting down
			// In some cases conn.getInputStream() can timeout when the Mjpeg server
			// has backed up sockets - in this case the connection will need to be re-attempted
			// after flushing the Mjpeg server with frames
			logger.error("Mjpeg capture shutting down due to error", e);
			keepRunning = false;
		}
		logger.debug("Frame capture shutting down for {}", urlSpec);
	}

	/**
	 * Reads a byte array from stream and pass it to decoding service.
	 * @param mjpegStream to be closed by the caller
	 * @throws IOException
	 */
	private void readFrameFromStream(MjpegInputStream mjpegStream) throws IOException {
		try {
			if (MotionJpegOverHttpReceiverBase.SHOW_STATS) {
				frameCaptureStatistics.startProcessingFrame();
			}
			final ByteArrayInputStream jpegBytes = new ByteArrayInputStream(mjpegStream.getNextFrame());
			if (MotionJpegOverHttpReceiverBase.SHOW_STATS) {
				frameCaptureStatistics.finishProcessingFrame();
			}
			Runnable decodeTask = () -> convertBytesToImage(jpegBytes);

			// We might be in the middle of capturing a frame from the stream when the image decoding service
			// is shut down
			if (!imageDecodingService.isShutdown()) {
				imageDecodingService.submit(decodeTask);
			}
		} catch (ClosedByInterruptException e) {
			logger.debug("Stream closed for {}", urlSpec);
			keepRunning = false;
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
			keepRunning = false;
		}

		if (MotionJpegOverHttpReceiverBase.SHOW_STATS) {
			frameDecodeStatistics.finishProcessingFrame();
		}
	}

	/**
	 * Should be implemented by subclasses to convert the specified {@link BufferedImage} to desired image type.
	 */
	protected abstract E convertImage(BufferedImage imageData);

}
