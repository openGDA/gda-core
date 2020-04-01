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

package uk.ac.gda.client.live.stream.simulator.stream;

import java.util.Optional;
import java.util.stream.IntStream;

import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.IDatasetConnector;
import org.eclipse.swt.graphics.Image;

/**
 * A class able to stream an image. The {@link #setImage(Image)} is thread safe and can be used to stream a sequence of
 * images, ie to simulate an event on the camera array. time. The {@link #getDataset()} returns an element with the same
 * shape of the streamed image.
 *
 * @author Maurizio Nagni
 */
public class ImageStream implements Runnable {
	/**
	 * The image to be streamed.
	 */
	private Optional<Image> image;

	private final IDatasetConnector connector;
	private IDataset dataset = DatasetFactory.zeros(new int[] { 0, 0 });
	private volatile boolean imageChanged = false;
	private Object lock = new Object();

	public ImageStream(IDatasetConnector connector) {
		super();
		this.connector = connector;
	}

	public IDataset getDataset() {
		synchronized (lock) {
			while (imageChanged) {
				try {
					lock.wait();
				} catch (InterruptedException e) {
				}
			}
			return dataset;
		}
	}

	@Override
	public void run() {
		while (!Thread.currentThread().isInterrupted()) {
			synchronized (lock) {
				if (imageChanged) {
					image.ifPresent(this::datasetUpdate);
					connector.fireDataListeners();
				}
				lock.notifyAll();
			}
		}
	}

	/**
	 * Set the image to be streamed
	 *
	 * @param image
	 */
	public void setImage(Image image) {
		synchronized (lock) {
			try {
				lock.wait();
			} catch (InterruptedException e) {
			}
			this.image = Optional.ofNullable(image);
			imageChanged = true;
		}
	}

	private void datasetUpdate(final Image image) {
		try {
			int[] shape = new int[] { image.getImageData().width, image.getImageData().height };
			dataset = DatasetFactory.createFromObject(getImagePixels(image), shape);
		} finally {
			imageChanged = false;
		}
	}

	private int[] getImagePixels(Image image) {
		int[] ret = new int[image.getImageData().width * image.getImageData().height];
		IntStream.range(0, image.getImageData().height).forEach(y -> IntStream.range(0, image.getImageData().width)
				.forEach(x -> ret[y * image.getImageData().height + x] = image.getImageData().getPixel(x, y)));
		return ret;
	}
}
