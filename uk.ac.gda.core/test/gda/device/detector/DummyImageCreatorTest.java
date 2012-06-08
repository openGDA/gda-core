/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council Daresbury Laboratory
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

package gda.device.detector;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;

import gda.configuration.properties.LocalProperties;
import gda.device.DeviceException;
import gda.util.TestUtils;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

// tests methods in DummyImageCreator particularly those of DetectorBase
/**
 *
 */
public class DummyImageCreatorTest {
	private DummyImageCreator imageDetector;

	/**
	 */
	@Before
	public void setUp() {
		imageDetector = new DummyImageCreator();
		final File testScratchDirectoryName = TestUtils.createClassScratchDirectory(DummyImageCreatorTest.class);
		LocalProperties.set(LocalProperties.GDA_DATAWRITER_DIR, testScratchDirectoryName + "/Data");
	}

	/**
	 * @throws DeviceException 
	 * 
	 */
	@Test
	public void testGetCollectionTime() throws DeviceException {
		double collectTime = imageDetector.getCollectionTime();
		assertEquals("default collection time should be zero", collectTime, 0., 0);
	}

	/**
	 * @throws DeviceException 
	 * 
	 */
	@Test
	public void testSetCollectionTime() throws DeviceException {
		double newTime = 10.;

		imageDetector.setCollectionTime(newTime);
		assertEquals("collection time not as set", imageDetector.getCollectionTime(), newTime, 0);

		// boundary tests
		newTime = Double.MAX_VALUE;
		imageDetector.setCollectionTime(newTime);
		assertEquals("collection time not as set", imageDetector.getCollectionTime(), newTime, 0);

		newTime = Double.MIN_VALUE;
		imageDetector.setCollectionTime(newTime);
		assertEquals("collection time not as set", imageDetector.getCollectionTime(), newTime, 0);

		newTime = -Double.MAX_VALUE;
		imageDetector.setCollectionTime(newTime);
		assertEquals("collection time not as set",imageDetector.getCollectionTime(),newTime, 0);

		newTime = -Double.MIN_VALUE;
		imageDetector.setCollectionTime(newTime);
		assertEquals("collection time not as set", imageDetector.getCollectionTime(), newTime, 0);
	}

	/**
	 * @throws DeviceException
	 */
	@Test
	public void testGetDataDimensions() throws DeviceException {
		int[] defaultDimension = { 100, 100 };
		assertEquals("default dimensions not as expected", imageDetector.getDataDimensions()[0], defaultDimension[0]);
		assertEquals("default dimensions not as expected", imageDetector.getDataDimensions()[1], defaultDimension[1]);
	}

	/**
	 * @throws DeviceException
	 */
	@Test
	public void testCreatesOwnFiles() throws DeviceException {
		assertEquals("default creates own files should be true", imageDetector.createsOwnFiles(), true);
	}

	/**
	 * @throws DeviceException
	 */
	@Test
	public void testReadout() throws DeviceException {
		// check works and returns valid filename
		String imageFileName;

		imageFileName = (String) imageDetector.readout();
		assertNotNull("image file name invalid", imageFileName);
		// uncomment this for failure on a Windows PC
		// assertFalse("image file not created",
		// imageFileName.compareTo("notcreated") == 0);
	}

	/**
	 * @throws DeviceException
	 */
	@Test
	public void testCollectData() throws DeviceException {
		// should do nothing
		imageDetector.collectData();
	}

	/**
	 * @throws DeviceException
	 */
	@Test
	@Ignore("2010/06/07 Test ignored since not passing GDA-3282")
	public void testGetStatus() throws DeviceException {
		assertEquals("default status is false", imageDetector.getStatus(), false);

		imageDetector.collectData();
		// uncomment this for failure on a Windows PC
		// assertEquals("status after collect should be IDLE",
		// imageDetector.getStatus(), Detector.IDLE);
	}

	/**
	 * @throws DeviceException
	 */
	@Test
	public void testEndCollection() throws DeviceException {
		// should do nothing
		imageDetector.endCollection();
	}

	/**
	 * @throws DeviceException
	 */
	@Test
	public void testPrepareForCollection() throws DeviceException {
		// should do nothing
		imageDetector.prepareForCollection();
	}

	/**
	 * @throws DeviceException
	 */
	@Test
	public void testSetWidth() throws DeviceException {
		int width;

		width = 99;
		imageDetector.setWidth(width);
		assertEquals("image width not as set", imageDetector.getDataDimensions()[0], width);

		// silly widths are ok but will fail in readout()
		width = -1;
		imageDetector.setWidth(width);
		assertEquals("image width not as set", imageDetector.getDataDimensions()[0], width);

		// this will fail with runtime exception IllegalArgumentException
		// imageDetector.readout();

		width = 0;
		imageDetector.setWidth(width);
		assertEquals("image width not as set", imageDetector.getDataDimensions()[0], width);

		// this will fail with runtime exception IllegalArgumentException
		// imageDetector.readout();
	}

	/**
	 * @throws DeviceException
	 */
	@Test
	public void testSetHeight() throws DeviceException {
		int height;

		height = 512;
		imageDetector.setHeight(height);
		assertEquals("image height not as set", imageDetector.getDataDimensions()[1], height);

		// silly widths are ok but will fail in readout()
		height = -2;
		imageDetector.setHeight(height);
		assertEquals("image height not as set", imageDetector.getDataDimensions()[1], height);

		// this will fail with runtime exception IllegalArgumentException
		// imageDetector.readout();

		height = -100;
		imageDetector.setHeight(height);
		assertEquals("image height not as set", imageDetector.getDataDimensions()[1], height);

		// this will fail with runtime exception IllegalArgumentException
		// imageDetector.readout();

	}

	/**
	 * 
	 */
	@Test
	public void testGetFormat() {
		assertEquals("default format should be blank", imageDetector.getFormat(), "");
	}

	/**
	 */
	@Test
	public void testSetFormat() {
		String format = "";

		format = "png";
		imageDetector.setFormat(format);
		assertEquals("format not as set", imageDetector.getFormat(), format);

		format = "jpg";
		imageDetector.setFormat(format);
		assertEquals("format not as set", imageDetector.getFormat(), format);

		// silly formats are ok but will fail in readout()
		format = "bananas";
		imageDetector.setFormat(format);
		assertEquals("format not as set", imageDetector.getFormat(), format);
	}
}
