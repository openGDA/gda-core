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
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGImageDecoder;

import gda.images.camera.FrameStatistics;
import gda.images.camera.MotionJpegOverHttpReceiverBase;

/**
 * Task that captures frames from the MJPEG stream. A task is created to decode each frame, and the frame is added to a
 * queue of received frames for later dispatch.
 * <p>
 * This class was originally written to receive MJPEG streams from Axis network cameras, specifically an Axis 214 PTZ.
 * As there is no standard specification for Motion JPEG, this may not work for all MJPEG streams. The <a
 * href="http://www.axis.com/techsup/cam_servers/dev/cam_http_api.php">Axis documentation</a> has several useful
 * sections:
 * <ul>
 * <li><a href="http://www.axis.com/techsup/cam_servers/dev/cam_http_api.php#sw_requirements_image_requests_mjpg">5.2.5
 * MJPG video request</a></li>
 * <li><a href="http://www.axis.com/techsup/cam_servers/dev/cam_http_api.php#api_blocks_jpeg_mjpg_mjpg_request">5.2.6
 * MJPG video CGI request</a></li>
 * <li><a href="http://www.axis.com/techsup/cam_servers/dev/cam_http_api.php#api_blocks_jpeg_mjpg_mjpg_response">5.2.7
 * MJPG video response</a></li>
 * </ul>
 */
public abstract class FrameCaptureTask<E> implements Runnable {

	private static final Logger logger = LoggerFactory.getLogger(FrameCaptureTask.class);

	private String urlSpec;

	private ExecutorService imageDecodingService;

	private BlockingQueue<Future<E>> receivedImages;

	/** The byte array is to represent the string - Content-Length:&nbsp */
	private static final byte[] CONTENT_LENGTH_AS_BYTES = "Content-Length: ".getBytes();

	private final int readTimeout;

	private final boolean acceptReadTimeout;

	private BufferedImage bufferedImage;

	private DataInputStream dis;

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

		frameCaptureStatistics.reset();
		frameDecodeStatistics.reset();

		URL url = new URL(urlSpec);
		URLConnection conn = url.openConnection();
		conn.setReadTimeout(readTimeout);
		InputStream instream = conn.getInputStream();
		BufferedInputStream bis = new BufferedInputStream(instream);
		dis = new DataInputStream(bis);

		// Miss the first image - this is because the first JPEG turns out to be corrupt
		while (true) {
			if (readFirstLineAndIsLegal(dis)) {
				break;
			}
		}
		String cL = getContentLength(dis);
		dis.skipBytes(Integer.parseInt(cL));
		readLine(1, dis);
		//
		keepRunning = true;
		while (keepRunning) {
			readLine(4, dis);

			if (MotionJpegOverHttpReceiverBase.SHOW_STATS) {
				frameCaptureStatistics.startProcessingFrame();
			}

			BufferedImage readJPG = readJPG(dis);
			readLine(1, dis);
			if( readJPG != null){
				//only update the member variable if not null
				bufferedImage = readJPG;
				if (MotionJpegOverHttpReceiverBase.SHOW_STATS) {
					frameCaptureStatistics.finishProcessingFrame();
				}

				Callable<Void> imageDecoderTask = () -> {
					if (MotionJpegOverHttpReceiverBase.SHOW_STATS) {
						frameDecodeStatistics.startProcessingFrame();
					}

					final E decodedImage = convertImage(bufferedImage);

					if (MotionJpegOverHttpReceiverBase.SHOW_STATS) {
						frameDecodeStatistics.finishProcessingFrame();
					}

					// once the frame has been decoded add it to the receivedImages queue
					// Do it here rather than adding the result of imageDecodingService.submit(imageDecoderTask);
					// to allow the decode task to be cancelled without the need to clear the associated future on the
					// receivedImages queue.
					receivedImages.offer(CompletableFuture.completedFuture(decodedImage));
					return null;
				};

				// We might be in the middle of capturing a frame from the stream when the image decoding service
				// is shut down
				if (!imageDecodingService.isShutdown()) {
					imageDecodingService.submit(imageDecoderTask);

				}
			}

		}

		logger.debug("Frame capture stopping");
	}

	private boolean readFirstLineAndIsLegal(DataInputStream dis) throws IOException {
		for (int i = 0; i < CONTENT_LENGTH_AS_BYTES.length; i++) {
			byte readByte = dis.readByte();
			if (readByte != CONTENT_LENGTH_AS_BYTES[i]) {
				return false;
			}
		}

		return true;
	}

	private BufferedImage readJPG(DataInputStream dis) { // read the embedded jpeg image
		BufferedImage image = null;
		try {
			//ImageIO.read fails to decode all images
			//causing the capture frame rate to be v. low
			JPEGImageDecoder decoder = JPEGCodec.createJPEGDecoder(dis);
			image = decoder.decodeAsBufferedImage();
		} catch (Throwable e) {
			logger.error("Problem reading JPG->", e);
			disconnect(dis);
			shutdown();
		}
		return image;
	}

	private void disconnect(DataInputStream dis) {
		try {
			if( dis != null)
				dis.close();

		} catch (Exception e) {
			logger.error("Problem disconnecting ->", e);
		}
	}

	private void readLine(int n, DataInputStream dis) throws Exception { // used to strip out the
		// header lines
		for (int i = 0; i < n; i++) {
			readLine(dis);
		}
	}

	private void readLine(DataInputStream dis) throws Exception {
		boolean end = false;
		String lineEnd = "\n"; // assumes that the end of the line is marked
		// with this
		byte[] lineEndBytes = lineEnd.getBytes();
		byte[] byteBuf = new byte[lineEndBytes.length];

		while (!end) {
			dis.read(byteBuf, 0, lineEndBytes.length);
			String t = new String(byteBuf);
			if (t.equals(lineEnd)) {
				end = true;
			}
		}
	}

	private String getContentLength(DataInputStream dis) {
		StringBuilder strBuffer = new StringBuilder();
		try {
			boolean end = false;
			String lineEnd = "\n"; // assumes that the end of the line is marked
			// with this
			byte[] lineEndBytes = lineEnd.getBytes();
			byte[] byteBuf = new byte[lineEndBytes.length];

			while (!end) {
				dis.read(byteBuf, 0, lineEndBytes.length);
				String t = new String(byteBuf);
				strBuffer.append(t);
				if (t.equals(lineEnd)) {
					end = true;
				}
			}
		} catch (Exception e) {
			logger.error("Cannot readLine", e);
		}
		return strBuffer.substring(0, strBuffer.length() - 2);

	}

	/**
	 * Should be implemented by subclasses to convert the specified {@link BufferedImage} to desired image type.
	 */
	protected abstract E convertImage(BufferedImage imageData);

	public void shutdown() {
		this.keepRunning = false;
		disconnect(dis);
	}
}