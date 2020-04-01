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
import static org.mockito.Mockito.mock;

import java.net.URL;
import java.util.Optional;

import org.eclipse.january.dataset.IDatasetConnector;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import uk.ac.gda.client.live.stream.simulator.stream.ImageStream;

/**
 * Tests basic functionalities for the {@link ImageStream} class.
 *
 * @author Maurizio Nagni
 */
public class ImageStreamTest {

	private IDatasetConnector dataConnector;
	private Optional<Thread> streamThread;

	private Display device;

	@Before
	public void setUp() {
		dataConnector = mock(IDatasetConnector.class);
		device = mock(Display.class);
	}

	@After
	public void setDown() {
		streamThread.ifPresent(Thread::interrupt);
	}

	/**
	 * Validates the instance before any use
	 */
	@Test
	public void emptyImageStreamTest() {
		ImageStream is = new ImageStream(dataConnector);
		streamThread = Optional.of(new Thread(is));
		streamThread.get().start();
		assertEquals(0, is.getDataset().getSize());
	}

	/**
	 * Set a new image, returns a dataset of the same size
	 */
	@Test
	public void singleImageStreamTest() {
		ImageStream is = new ImageStream(dataConnector);
		streamThread = Optional.of(new Thread(is));
		streamThread.get().start();
		URL resource = ImageStreamTest.class.getResource("/resources/test_icon.png");
		Image image = new Image(device, resource.getPath());
		is.setImage(image);
		while (is.getDataset().getSize() != 256) {

		}
		assertEquals(256, is.getDataset().getSize());
	}
}
