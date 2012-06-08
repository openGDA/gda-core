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

package gda.util;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * 
 */
public class GaussianTest {
	private Gaussian gaussianOne;

	/**
	 */
	@BeforeClass
	public static void setUpBeforeClass() {
	}

	/**
	 */
	@AfterClass
	public static void tearDownAfterClass() {
	}

	/**
	 */
	@Before
	public void setUp() {
		gaussianOne = new Gaussian(0.0, 10.0, 10.0);
	}

	/**
	 */
	@After
	public void tearDown() {
	}

	/**
	 * 
	 */
	@Test
	public void testYAtX() {
		assertEquals(10.0, gaussianOne.yAtX(0.0), 0);
	}

	/**
	 * 
	 */
	@Test
	public void testDerivativeAtX() {
		assertEquals(0.0, gaussianOne.derivativeAtX(0.0), 0.00001);
	}

	/**
	 * 
	 */
	@Test
	public void testXAtY() {
		// NB the 'width' in the definition of Gaussian is NOT the FWHM
		// but the width related parameter in the standard gaussian expression.
		// / FWHM = 2 * 'width' * sqrt(2 * ln(2))
		double fwhm = 2.0 * 10.0 * Math.pow(2.0 * Math.log(2), 0.5);

		assertEquals(0.0, gaussianOne.xAtY(10.0, Gaussian.POSITIVE), 0.00001);
		assertEquals(fwhm / 2.0, gaussianOne.xAtY(5.0, Gaussian.POSITIVE), 0.00001);
		assertEquals(-fwhm / 2.0, gaussianOne.xAtY(5.0, Gaussian.NEGATIVE), 0.00001);
	}

	/**
	 * 
	 */
	@Test
	public void testDerivativeAtY() {
		double y = gaussianOne.yAtX(10.0 * 10.0 / 2);
		assertEquals(0.0, gaussianOne.derivativeAtY(10.0, Gaussian.POSITIVE), 0.00001);
		assertEquals(0.5 * y, gaussianOne.derivativeAtY(y, Gaussian.NEGATIVE), 0.00001);
		assertEquals(-0.5 * y, gaussianOne.derivativeAtY(y, Gaussian.POSITIVE), 0.00001);
	}
}
