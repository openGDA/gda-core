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

package gda.device.detector.areadetector;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import gda.device.detector.areadetector.impl.AreaDetectorBinImpl;

import org.junit.Before;
import org.junit.Test;

/**
 * Simple Bean JUnit test - there is no logic in the {@link AreaDetectorBin} so no much to test except for the
 * availability of fields.
 */
public class AreaDetectorBinTest {

	private AreaDetectorBin areaDetectorBin;
	private final int xValue = 12;
	private final int yValue = 15;

	/**
	 */
	@Before
	public void setUp() {
		areaDetectorBin = new AreaDetectorBinImpl(xValue, yValue);

		assertNotNull("Area detector bean did not instantiate properly", areaDetectorBin);

	}

	/**
	 * Test method for {@link gda.device.detector.areadetector.AreaDetectorBin#getBinX()}.
	 */
	@Test
	public final void testGetBinX() {
		assertEquals("BinX value not returned properly", Integer.valueOf(xValue),
				Integer.valueOf(areaDetectorBin.getBinX()));
	}

	/**
	 * Test method for {@link gda.device.detector.areadetector.AreaDetectorBin#setBinX(int)}.
	 */
	@Test
	public final void testSetBinX() {

		final int changedBinX = 25;
		areaDetectorBin.setBinX(changedBinX);

		assertEquals("BinX value not set properly", areaDetectorBin.getBinX(), changedBinX);
	}

	/**
	 * Test method for {@link gda.device.detector.areadetector.AreaDetectorBin#getBinY()}.
	 */
	@Test
	public final void testGetBinY() {
		assertEquals("BinY not returned properly", Integer.valueOf(yValue), Integer.valueOf(areaDetectorBin.getBinY()));
	}

	/**
	 * Test method for {@link gda.device.detector.areadetector.AreaDetectorBin#setBinY(int)}.
	 */
	@Test
	public final void testSetBinY() {
		final int changedBinY = 25;
		areaDetectorBin.setBinY(changedBinY);

		assertEquals("BinY value not set properly", areaDetectorBin.getBinY(), changedBinY);
	}

	/**
	 * Test method for {@link gda.device.detector.areadetector.AreaDetectorBin#toString()}.
	 */
	@Test
	public final void testToString() {
		assertEquals("toString doesn't return the right value",
				"(" + areaDetectorBin.getBinX() + "," + areaDetectorBin.getBinY() + ")", areaDetectorBin.toString());
	}

}
