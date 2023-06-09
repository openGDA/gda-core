/*-
 * Copyright © 2023 Diamond Light Source Ltd.
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

package uk.ac.gda.client.live.stream.connector;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.ClosedByInterruptException;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import org.eclipse.january.DatasetException;
import org.eclipse.january.dataset.DataEvent;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.IDataListener;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.IDatasetChangeChecker;
import org.eclipse.january.dataset.IDatasetConnector;
import org.eclipse.january.dataset.ILazyDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.RateLimiter;

import gda.images.camera.mjpeg.MjpegInputStream;
import uk.ac.diamond.daq.concurrent.Async;
import uk.ac.diamond.scisoft.analysis.io.AWTImageUtils;

public class MjpegDynamicDatasetConnector implements IDatasetConnector {

	private static final Logger logger = LoggerFactory.getLogger(MjpegDynamicDatasetConnector.class);

	private int height = 0;
	private int width = 0;
	private IDataset dataset;
	private String url;
	private final Set<IDataListener> listeners = new CopyOnWriteArraySet<>();
	private volatile boolean streamActive = false;

	/** Limit decoding to 20 fps */
	private final RateLimiter frameRateLimiter = RateLimiter.create(20);
	private Future<?> streamTask;

	public MjpegDynamicDatasetConnector(String url) {
		this.url = url;
	}

	@Override
	public String getPath() {
		// N/A
		return null;
	}

	@Override
	public void setPath(String path) {
		throw new UnsupportedOperationException("Cannot setPath on MJPEG stream");

	}

	@Override
	public ILazyDataset getDataset() {
		return dataset;
	}

	@Override
	public boolean resize(int... newShape) {
		throw new UnsupportedOperationException("Cannot resize an MJPEG stream");
	}

	@Override
	public int[] getMaxShape() {
		return getDataShape();
	}

	@Override
	public void setMaxShape(int... maxShape) {
		throw new UnsupportedOperationException("Cannot setMaxShape MJPEG stream");
	}

	@Override
	public void startUpdateChecker(int milliseconds, IDatasetChangeChecker checker) {
		throw new UnsupportedOperationException("MJPEG stream does not support update checker");

	}

	@Override
	public boolean refreshShape() {
		return false; // There is no file so false
	}

	@Override
	public void addDataListener(IDataListener listener) {
		listeners.add(listener);

	}

	@Override
	public void removeDataListener(IDataListener listener) {
		listeners.remove(listener);

	}

	@Override
	public void fireDataListeners() {
		final DataEvent dataEvent = new DataEvent(getDatasetName(), getDataShape());
		listeners.forEach(listener -> listener.dataChangePerformed(dataEvent));
	}

	@Override
	public String getDatasetName() {
		return url;
	}

	@Override
	public void setDatasetName(String datasetName) {
		throw new UnsupportedOperationException("Cannot setDatasetName on MJPEG stream");

	}

	@Override
	public void setWritingExpected(boolean expectWrite) {
		throw new UnsupportedOperationException("Cant write to MJPEG stream");
	}

	@Override
	public boolean isWritingExpected() {
		return false;
	}

	@Override
	public String connect() throws DatasetException {
		return connect(5, TimeUnit.SECONDS);
	}

	@Override
	public String connect(long time, TimeUnit unit) throws DatasetException {
		try {
			// Initialise the dataset. This is needed so when the trace is
			//added to the plotting system it's actually drawn
			// SCI-8921 Must be of non 0 x 0 shape
			dataset = DatasetFactory.zeros(10, 10);
			streamTask = Async.submit(this::runStream, "MJPEG Stream %s".formatted(url));
		} catch (Exception ex) {
			logger.error("Could not connect to : {} {}", url, ex.getMessage());
			throw new DatasetException(ex);
		}
		return null;
	}

	@Override
	public void disconnect() throws DatasetException {
		streamActive = false;
		if (streamTask != null) {
			try {
				// Interrupt reading thread as it could be blocked waiting for new frames
				streamTask.cancel(true);
			} catch (Exception e) {
				throw new DatasetException("Could not close stream", e);
			}
		}
	}

	private int[] getDataShape() {
		return new int[] { height, width };
	}

	private void runStream() {
		URL conn;
		try {
			conn = new URL(url);
		} catch (MalformedURLException e) {
			logger.error("Malformed MJPEG URL: {}", url);
			return;
		}
		streamActive = true;
		try (MjpegInputStream mjpegStream = new MjpegInputStream(conn)) {
			mjpegStream.connect();
			while (streamActive) {
				readFrameFromStream(mjpegStream);
			}
		} catch (ClosedByInterruptException e) {
			logger.debug("Connection to {} interrupted", url);
		} catch (IOException e) {
			logger.error("Could not read stream {}", url, e);
		}
		logger.debug("Connection closed for MJPEG Stream {}", url);
	}

	private void readFrameFromStream(MjpegInputStream mjpegStream) throws IOException {
		final ByteArrayInputStream jpegBytes = new ByteArrayInputStream(mjpegStream.getNextFrame());
		if (frameRateLimiter.tryAcquire()) {
			BufferedImage bufferedImage = ImageIO.read(jpegBytes);
			dataset = AWTImageUtils.makeDatasets(bufferedImage, true)[0];
			height = dataset.getShape()[0];
			width = dataset.getShape()[1];
			fireDataListeners();
		} else {
			logger.trace("Frame dropped");
		}
	}

}
