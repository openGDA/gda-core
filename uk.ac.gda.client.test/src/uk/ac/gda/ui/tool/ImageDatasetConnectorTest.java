/*-
 * Copyright © 2010 Diamond Light Source Ltd.
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

package uk.ac.gda.ui.tool;

import static org.junit.Assert.assertEquals;

import java.net.URL;
import java.util.Arrays;

import org.eclipse.january.DatasetException;
import org.eclipse.january.dataset.IDataListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import uk.ac.gda.client.live.stream.simulator.connector.ImageDatasetConnector;

public class ImageDatasetConnectorTest {

	private ImageDatasetConnector dataConnector;

	private Display device;

	@Before
	public void setUp() {
		dataConnector = new ImageDatasetConnector("testDataset");
		device = Display.getDefault();
	}

	@After
	public void setDown() {
		try {
			dataConnector.disconnect();
		} catch (DatasetException e) {

		}
	}

	@Test
	public void doubleImageStreamTest() throws DatasetException {
		dataConnector.connect();
		URL resource = ImageStreamTest.class.getResource("/resources/test_icon.png");
		Image image = new Image(device, resource.getPath());
		dataConnector.setImage(image);
		while (dataConnector.getDataset().getSize() < 256) {

		}
		assertEquals(256, dataConnector.getDataset().getSize());

		resource = ImageStreamTest.class.getResource("/resources/icon_32.gif");
		image = new Image(device, resource.getPath());
		dataConnector.setImage(image);
		while (dataConnector.getDataset().getSize() < 1024) {

		}
		assertEquals(1024, dataConnector.getDataset().getSize());
	}

	@Test
	public void doubleImageStreamWithListenerTest() throws DatasetException {
		Object lock = new Object();
		IDataListener listener = (event) -> {
			synchronized (lock) {
				int size = Arrays.stream(event.getShape()).reduce((a, b) -> a * b).orElse(0);
				assertEquals(256, dataConnector.getDataset().getSize());
				assertEquals(256, size);
				lock.notifyAll();
			}
		};

		dataConnector.addDataListener(listener);
		dataConnector.connect();
		URL resource = ImageStreamTest.class.getResource("/resources/test_icon.png");
		Image image = new Image(device, resource.getPath());
		dataConnector.setImage(image);

		synchronized (lock) {
			try {
				lock.wait();
			} catch (InterruptedException e) {
				Assert.fail();
			}
		}
		dataConnector.removeDataListener(listener);

		listener = (event) -> {
			synchronized (lock) {
				int size = Arrays.stream(event.getShape()).reduce((a, b) -> a * b).orElse(0);
				assertEquals(1024, dataConnector.getDataset().getSize());
				assertEquals(1024, size);
				lock.notifyAll();
			}
		};
		dataConnector.addDataListener(listener);
		resource = ImageStreamTest.class.getResource("/resources/icon_32.gif");
		image = new Image(device, resource.getPath());
		dataConnector.setImage(image);

		synchronized (lock) {
			try {
				lock.wait();
			} catch (InterruptedException e) {
				Assert.fail();
			}
		}
	}
}
