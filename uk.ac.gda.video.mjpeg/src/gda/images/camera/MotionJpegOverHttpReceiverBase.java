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
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.StringUtils;

import gda.device.DeviceException;
import gda.factory.ConfigurableBase;
import gda.factory.FactoryException;
import gda.images.camera.mjpeg.FrameCaptureTask;
import gda.images.camera.mjpeg.FrameDispatchTask;
import uk.ac.diamond.daq.concurrent.Async;

/**
 * Captures an MJPEG stream from a HTTP connection.
 */
public abstract class MotionJpegOverHttpReceiverBase<E> extends ConfigurableBase
	implements VideoReceiver<E>, InitializingBean {

	private static final Logger logger = LoggerFactory.getLogger(MotionJpegOverHttpReceiverBase.class);

	/** Number of threads to use for decoding frames. */
	private static final int NUM_DECODER_THREADS = 3;

	/** Maximum number of images to allow in the received images queue. */
	private static final int MAX_RECEIVED_IMAGES = 100;

	enum ReceiverStatus {

		STOPPED,

		STARTING,

		STARTED,

		STOPPING
	}

	/** Set this to {@code true} to get some statistics about frame processing. */
	public static final boolean SHOW_STATS = false;

	protected final AtomicReference<E> lastImage = new AtomicReference<>();

	/** Defaults true for all OAVs. Should be set to false in Spring config for secondarys */
	private boolean autoConnect = true;
	private ReceiverStatus status = ReceiverStatus.STOPPED;
	private Function<ThreadFactory, ExecutorService> executorServiceFactory = null;
	private Set<ImageListener<E>> listeners = new LinkedHashSet<>();

	/** Service that will manage decoding of frames. */
	private ExecutorService imageDecodingService;

	/** Queue of all decoded images, in the order they were received. */
	private BlockingQueue<E> receivedImages;

	private Future<?> captureTask;
	private FrameDispatchTask<E> dispatchTask;
	private Thread dispatchThread;
	private String urlSpec;
	private String displayName;

	@Override
	public void afterPropertiesSet() throws IllegalStateException {
		if (!isUrlSet()) {
			throw new IllegalStateException("URL has not been specified");
		}
	}

	@Override
	public void addImageListener(ImageListener<E> listener) {
		listeners.add(listener);
	}

	@Override
	public void configure() throws FactoryException {
		if (isConfigured()) {
			return;
		}
		if (autoConnect) {
			createConnection();
		}
		setConfigured(true);
	}

	@Override
	public void removeImageListener(ImageListener<E> listener) {
		if (listeners.contains(listener)) {
			listeners.remove(listener);
		}
	}

	@Override
	public synchronized void createConnection() {
		if (status != ReceiverStatus.STOPPED) {
			return;
		}

		status = ReceiverStatus.STARTING;
		logger.info("Starting MJPEG capture");

		prepareReceiverQueue();
		startDispatchThread();
		prepareDecodingThread();

		var captureTaskName = String.format("MJPEG capture (%s)", urlSpec);
		captureTask = Async.submit(createFrameCaptureTask(urlSpec, imageDecodingService, receivedImages), captureTaskName);

		status = ReceiverStatus.STARTED;
	}

	@Override
	public synchronized void closeConnection() {
		if (status != ReceiverStatus.STARTED) {
			return;
		}

		status = ReceiverStatus.STOPPING;
		logger.info("Stopping MJPEG capture");

		shutdownCaptureTask();
		shutdownDecoding();
		shutdownDispatch();

		receivedImages.clear();

		status = ReceiverStatus.STOPPED;
	}

	@Override
	public void start() {
		// does nothing
	}

	@Override
	public void stop() {
		// does nothing
	}

	@Override
	public E getImage() throws DeviceException {
		return lastImage.get();
	}

	/**
	 * @return Returns the displayName.
	 */
	@Override
	public String getDisplayName() {
		return StringUtils.hasText(displayName) ? displayName : urlSpec;
	}

	/**
	 * @param displayName
	 *            The displayName to set.
	 */
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public void setAutoConnect(boolean auto) {
		autoConnect = auto;
	}

	/**
	 * Identify that the url has been set on the MotionJpegReceiver
	 *
	 * @return {@code true} if the urlspec is not null
	 */
	public boolean isUrlSet() {
		return StringUtils.hasText(urlSpec);
	}

	public void setUrl(String url) {
		urlSpec = url;
	}

	public void setExecutiveServiceFactory(Function<ThreadFactory, ExecutorService> executorServiceFactory) {
		this.executorServiceFactory = executorServiceFactory;
	}

	void setImageQueue(BlockingQueue<E> q){
		receivedImages = q;
	}

	/**
	 * Override this method to create a {@link FrameCaptureTask}
	 * for images of the appropriate type.
	 */
	protected abstract FrameCaptureTask<E> createFrameCaptureTask(String urlSpec, ExecutorService imageDecodingService,
			BlockingQueue<E> receivedImages);

	private void prepareDecodingThread() {
		var decoderThreadNameFormat = String.format("MJPEG decode (%s) #%%d", urlSpec);
		var decoderThreadFactory = new DecoderThreadFactory(decoderThreadNameFormat);
		if( executorServiceFactory == null){
			imageDecodingService =
				new ThreadPoolExecutor(NUM_DECODER_THREADS,
										NUM_DECODER_THREADS,
										1,
										TimeUnit.SECONDS,
										new LinkedBlockingQueue<>(),
										decoderThreadFactory);
		} else {
			imageDecodingService = executorServiceFactory.apply(decoderThreadFactory);
		}
	}

	private void startDispatchThread() {
		dispatchTask = new FrameDispatchTask<>(receivedImages, listeners, lastImage);
		dispatchThread = new Thread(dispatchTask, String.format("MJPEG dispatch (%s)", urlSpec));
		dispatchThread.start();
	}

	private void prepareReceiverQueue() {
		if( receivedImages == null)
			receivedImages = new LinkedBlockingQueue<>(MAX_RECEIVED_IMAGES);
	}

	private void shutdownDispatch() {
		dispatchTask.shutdown();
		dispatchThread.interrupt();
	}

	private void shutdownDecoding() {
		imageDecodingService.shutdownNow();
	}

	private void shutdownCaptureTask() {
		captureTask.cancel(true);
	}

	static final class DecoderThreadFactory implements ThreadFactory {

		private final AtomicInteger threadCount = new AtomicInteger(0);
		private final String format;
		private final ThreadFactory threadFactory;

		public DecoderThreadFactory(String format) {
			this.format = format;
			threadFactory = Executors.defaultThreadFactory();
		}

		@Override
		public Thread newThread(Runnable r) {
			var thread = threadFactory.newThread(r);
			var threadNumber = threadCount.incrementAndGet();
			var nameOfThread = String.format(format, threadNumber);
			thread.setName(nameOfThread);
			return thread;
		}
	}
}
