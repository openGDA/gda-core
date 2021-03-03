/*-
 * Copyright © 2020 Diamond Light Source Ltd.
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

package uk.ac.gda.client.live.stream.simulator.connector;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;

import org.eclipse.january.DatasetException;
import org.eclipse.january.dataset.DataEvent;
import org.eclipse.january.dataset.IDataListener;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.IDatasetChangeChecker;
import org.eclipse.january.dataset.IDatasetConnector;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.swt.graphics.Image;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.client.live.stream.simulator.stream.ImageStream;

/**
 * A ready to use connector to stream images using an internal instance of {@link ImageStream}. A controller can use
 * {@link #setImage(Image)} or {@link #setDataset(IDataset)} to change the streamed image.
 *
 * @author Maurizio Nagni
 */
public class ImageDatasetConnector implements IDatasetConnector {
	private final String datasetName;
	private final Set<IDataListener> listeners = new CopyOnWriteArraySet<>();
	private ImageStream internalStream;
	private Thread internalThread;

	private final Logger logger = LoggerFactory.getLogger(ImageDatasetConnector.class);

	public ImageDatasetConnector(String datasetName) {
		super();
		this.datasetName = datasetName;
	}

	@Override
	public void addDataListener(IDataListener l) {
		listeners.add(l);
	}

	@Override
	public String connect() throws DatasetException {
		if (internalThread == null || internalThread.isInterrupted()) {
			internalStream = new ImageStream(this);
			internalThread = new Thread(internalStream);
			internalThread.start();
			logger.info("Connection open with stream {}", internalStream);
		}
		return internalThread.getName();
	}

	@Override
	public String connect(long time, TimeUnit unit) throws DatasetException {
		return connect();
	}

	@Override
	public void fireDataListeners() {
		final DataEvent dataEvent = new DataEvent(getDatasetName(), internalStream.getDataset().getShape());
		listeners.forEach(listener -> listener.dataChangePerformed(dataEvent));
	}

	/**
	 * Sets the image to be streamed
	 * @param image
	 */
	public void setImage(Image image) {
		if (internalStream != null) {
			internalStream.setImage(image);
		}
	}

	/**
	 * Set the Dataset to be streamed
	 * @param dataset
	 */
	public void setDataset(IDataset dataset) {
		if (internalStream != null) {
			internalStream.setDataset(dataset);
		}
	}
	@Override
	public String getDatasetName() {
		return this.datasetName;
	}

	@Override
	public void removeDataListener(IDataListener l) {
		listeners.remove(l);
	}

	@Override
	public ILazyDataset getDataset() {
		return internalStream.getDataset();
	}

	@Override
	public void setDatasetName(String datasetName) {
		// Do Nothing
	}

	@Override
	public String getPath() {
		return null;
	}

	@Override
	public void setPath(String path) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean resize(int... newShape) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int[] getMaxShape() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setMaxShape(int... maxShape) {
		// TODO Auto-generated method stub

	}

	@Override
	public void startUpdateChecker(int milliseconds, IDatasetChangeChecker checker) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean refreshShape() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setWritingExpected(boolean expectWrite) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isWritingExpected() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void disconnect() throws DatasetException {
		internalThread.interrupt();
		logger.info("Connection open with stream {}", internalStream);
	}
}