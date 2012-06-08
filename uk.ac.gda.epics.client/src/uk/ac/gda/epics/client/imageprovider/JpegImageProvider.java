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

package uk.ac.gda.epics.client.imageprovider;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.widgets.Canvas;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Image provider that reads from the Jpeg stream of epics.
 * <p>
 * PLEASE NOTE THIS WILL NOT WORK FOR MJPEG STREAMS
 * </p>
 * The reason for streaming JPEG through the JPEG stream instead of the MJPEG stream is so that the GDA wouldn't have to
 * manipulate byte arrays. <br>
 * <br>
 * It has been observed that manipulating byte arrays has caused ir-recoverable memory leaks.<br>
 * The byte array manipulation is avoided in this class by passing the stream to the {@link ImageLoader} class which
 * does holds that data in the {@link ImageData} format. The {@link ImageData} format is the appropriate format that is
 * used to display the image on the {@link Canvas}.<br>
 * <br>
 * Images are not queued up and the latest decoded image is returned to the listener.
 */
public class JpegImageProvider {
	private boolean streamRunning = false;
	private static final Logger logger = LoggerFactory.getLogger(JpegImageProvider.class);
	private ImageLoader imageLoader;
	private Thread imgDecoderThread;

	/**
	 * Image listener interface whose method will be invoked each time an image is decoded.
	 */
	public interface JpegImageListener {

		/**
		 * @param img
		 */
		public void processImage(ImageData img);

		/**
		 * Informs the listener that image streaming has stopped.
		 */
		public void imageStreamingStopped();
	}

	private Set<JpegImageListener> imgListeners;
	private String urlStr;

	/**
	 * Add listener to the images
	 * 
	 * @param imgListener
	 */
	public void addJpegImageListener(JpegImageListener imgListener) {
		imgListeners.add(imgListener);
	}

	/**
	 * Remove image listener from the list
	 * 
	 * @param imgListener
	 */
	public void removeJpegImageListener(JpegImageListener imgListener) {
		imgListeners.remove(imgListener);
	}

	/**
	 * @param url
	 */
	public JpegImageProvider(String url) {
		this.urlStr = url;
		imgListeners = new HashSet<JpegImageProvider.JpegImageListener>();
		imageLoader = new ImageLoader();
	}

	/**
	 */
	public JpegImageProvider() {
		this(null);
	}

	public void setUrl(String urlStr) {
		this.urlStr = urlStr;
	}

	public void startStream() throws IllegalArgumentException {
		if (!streamRunning) {
			if (urlStr == null) {
				throw new IllegalArgumentException("URL should not be null");
			}
			setStreamingRunning(true);
			imgDecoderThread = new Thread(imgStreamer);
			imgDecoderThread.start();
		}
	}

	protected synchronized boolean getStreamRunning() {
		return streamRunning;
	}

	protected synchronized void setStreamingRunning(boolean streamRunning) {
		this.streamRunning = streamRunning;
	}

	public void stopStream() {
		if (streamRunning) {
			setStreamingRunning(false);
		}
	}

	private Runnable imgStreamer = new Runnable() {

		@Override
		public void run() {
			InputStream stream = null;
			ImageData mainImgData = null;
			try {
				while (getStreamRunning()) {
					URL url = new URL(urlStr);
					stream = url.openStream();
					mainImgData = imageLoader.load(stream)[0];
					stream.close();
					if (mainImgData.data.length < 1) {
						streamRunning = false;
						for (JpegImageListener iL : imgListeners) {
							iL.imageStreamingStopped();
						}
						break;
					}

					for (JpegImageListener iL : imgListeners) {
						iL.processImage(mainImgData);
					}
				}
			} catch (MalformedURLException e) {
				logger.error("URL invalid");
			} catch (IOException e) {
				logger.error("Cannot read stream");
			} catch (IllegalMonitorStateException ex) {
				logger.error("Illegal monitor state exception");
			}
		}
	};

}
