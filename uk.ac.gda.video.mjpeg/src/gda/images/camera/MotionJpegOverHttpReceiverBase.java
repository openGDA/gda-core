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

package gda.images.camera;

import gda.device.DeviceException;
import gda.factory.FactoryException;
import gda.images.camera.mjpeg.FrameCaptureTask;
import gda.images.camera.mjpeg.FrameDispatchTask;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.StringUtils;

/**
 * Captures an MJPEG stream from a HTTP connection.
 */
public abstract class MotionJpegOverHttpReceiverBase<E> implements VideoReceiver<E>, InitializingBean {
	
	/** Set this to {@code true} to get some statistics about frame processing. */
	public static final boolean SHOW_STATS = false;

	private static final Logger logger = LoggerFactory.getLogger(MotionJpegOverHttpReceiverBase.class);

	/** Number of threads to use for decoding frames. */
	private static final int NUM_DECODER_THREADS = 3;

	/** Maximum number of images to allow in the received images queue. */
	private static final int MAX_RECEIVED_IMAGES = 100;

	private String urlSpec;

	private Set<ImageListener<E>> listeners = new LinkedHashSet<ImageListener<E>>();

	@Override
	public void addImageListener(ImageListener<E> listener) {
		listeners.add(listener);
	}

	@Override
	public void removeImageListener(ImageListener<E> listener) {
		if (listeners.contains(listener)) {
			listeners.remove(listener);
		}
	}

	/**
	 * Method to identify whether the url has been set on the MotionJpegReceiver
	 * 
	 * @return true is the urlspec is not null
	 */
	public boolean isUrlSet() {
		return urlSpec != null;
	}

	public void setUrl(String url) {
		this.urlSpec = url;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (!StringUtils.hasText(urlSpec)) {
			throw new IllegalStateException("URL has not been specified");
		}
	}

	enum ReceiverStatus {

		STOPPED,

		STARTING,

		STARTED,

		STOPPING
	}

	private ReceiverStatus status = ReceiverStatus.STOPPED;

	@Override
	public void configure() throws FactoryException {
		createConnection();
	}

	@Override
	public synchronized void createConnection() {
		if (status != ReceiverStatus.STOPPED) {
			return;
		}

		status = ReceiverStatus.STARTING;
		logger.info("Starting MJPEG capture");

		if( receivedImages == null)
			receivedImages = new LinkedBlockingQueue<Future<E>>(MAX_RECEIVED_IMAGES);

		dispatchTask = new FrameDispatchTask<E>(receivedImages, listeners, lastImage);
		dispatchThread = new Thread(dispatchTask, String.format("MJPEG dispatch (%s)", urlSpec));
		dispatchThread.start();

		final String decoderThreadNameFormat = String.format("MJPEG decode (%s) #%%d", urlSpec);
		final ThreadFactory decoderThreadFactory = new DecoderThreadFactory(decoderThreadNameFormat);
		if( executiveServiceFactory == null){
			imageDecodingService = new ThreadPoolExecutor(NUM_DECODER_THREADS, NUM_DECODER_THREADS, 1, TimeUnit.SECONDS,
					new LinkedBlockingQueue<Runnable>(), decoderThreadFactory);
		} else {
			imageDecodingService = executiveServiceFactory.create(decoderThreadFactory);
		}

		captureTask = createFrameCaptureTask(urlSpec, imageDecodingService, receivedImages);
		new Thread(captureTask, String.format("MJPEG capture (%s)", urlSpec)).start();

		status = ReceiverStatus.STARTED;
	}


	/** Service that will manage decoding of frames. */
	private ExecutorService imageDecodingService;

	private ExecutorServiceFactory executiveServiceFactory=null;

	public void setExecutiveServiceFactory(ExecutorServiceFactory executiveServiceFactory) {
		this.executiveServiceFactory = executiveServiceFactory;
	}


	/** Queue of all decoded images, in the order they were received. */
	private BlockingQueue<Future<E>> receivedImages;
	
	void setImageQueue(BlockingQueue<Future<E>> q){
		receivedImages = q;
	}

	private FrameCaptureTask<E> captureTask;

	private FrameDispatchTask<E> dispatchTask;

	private Thread dispatchThread;

	/**
	 * Subclasses should override this method to create a {@link FrameCaptureTask} that works with images of the
	 * appropriate type.
	 */
	protected abstract FrameCaptureTask<E> createFrameCaptureTask(String urlSpec, ExecutorService imageDecodingService,
			BlockingQueue<Future<E>> receivedImages);

	@Override
	public void start() {
		// does nothing
	}

	@Override
	public void stop() {
		// does nothing
	}

	protected AtomicReference<E> lastImage = new AtomicReference<E>();

	@Override
	public E getImage() throws DeviceException {
		return lastImage.get();
	}

	@Override
	public synchronized void closeConnection() {
		if (status != ReceiverStatus.STARTED) {
			return;
		}

		status = ReceiverStatus.STOPPING;

		logger.info("Stopping MJPEG capture");

		captureTask.shutdown();

		imageDecodingService.shutdownNow();

		dispatchTask.shutdown();
		dispatchThread.interrupt();

		receivedImages.clear();

		status = ReceiverStatus.STOPPED;
	}

	private String displayName;

	/**
	 * @return Returns the displayName.
	 */
	@Override
	public String getDisplayName() {
		return displayName != null ? displayName : urlSpec;
	}

	/**
	 * @param displayName
	 *            The displayName to set.
	 */
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	static class DecoderThreadFactory implements ThreadFactory {

		private final String format;

		private final ThreadFactory threadFactory;

		private final AtomicInteger threadCount = new AtomicInteger(0);

		public DecoderThreadFactory(String format) {
			this.format = format;
			this.threadFactory = Executors.defaultThreadFactory();
		}

		@Override
		public Thread newThread(Runnable r) {
			Thread thread = threadFactory.newThread(r);
			final int threadNumber = threadCount.incrementAndGet();
			thread.setName(String.format(format, threadNumber));
			return thread;
		}
	}

}
