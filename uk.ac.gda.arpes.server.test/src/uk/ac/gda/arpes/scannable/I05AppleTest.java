/*-
 * Copyright © 2013 Diamond Light Source Ltd.
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

package uk.ac.gda.arpes.scannable;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import gda.device.DeviceException;
import gda.device.ScannableMotion;
import gda.device.scannable.DummyScannable;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;

import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;
import org.junit.Before;
import org.junit.Test;

import uk.ac.gda.arpes.scannable.I05Apple.PGMove;
import uk.ac.gda.arpes.scannable.I05Apple.TrajectorySolver;

public class I05AppleTest {

	ScannableMotion gap, lower, upper;
	I05Apple apple;
	Rectangle2D[] rectangles;

	@Before
	public void setup() throws Exception {
		apple = new I05Apple();
		gap = new DummyScannable("gap", 100.0);
		gap.setTolerances(new Double[] { 0.1});
		lower = new DummyScannable("lower", 0.0);
		lower.setTolerances(new Double[] { 0.1});
		upper = new DummyScannable("upper", 0.0);
		upper.setTolerances(new Double[] { 0.1});
		apple.setLowerPhaseScannable(lower);
		apple.setUpperPhaseScannable(upper);
		apple.setGapScannable(gap);
		apple.setPhaseTolerance(0.01);
		apple.setVerticalGapPolynomial(new PolynomialFunction(new double[] {0, 1}));
		apple.setHorizontalGapPolynomial(new PolynomialFunction(new double[] {0, 1}));
		apple.setCircularGapPolynomial(new PolynomialFunction(new double[] {0, 1}));
		apple.setCircularPhasePolynomial(new PolynomialFunction(new double[] {0, 1}));

		rectangles = new Rectangle2D[] { new Rectangle2D.Double(-65, 0, 130, 25),
										 new Rectangle2D.Double(-10, 0,  20, 38)};
	}

	@Test
	public void testH100Pol() throws Exception {
		String pol = apple.getCurrentPolarisation();
		assertEquals("H100Pol does not match", I05Apple.HORIZONTAL, pol);
	}

	@Test
	public void testV100_1Pol() throws Exception {
		lower.moveTo(70.0);
		upper.moveTo(70.0);
		String pol = apple.getCurrentPolarisation();
		assertEquals("V100_1Pol does not match", I05Apple.VERTICAL, pol);
	}

	@Test
	public void testV100_2Pol() throws Exception {
		lower.moveTo(-70.0);
		upper.moveTo(-70.0);
		String pol = apple.getCurrentPolarisation();
		assertEquals("V100_2Pol does not match", I05Apple.VERTICAL, pol);
	}

	@Test
	public void testCRPol() throws Exception {
		lower.moveTo(30.0);
		upper.moveTo(30.0);
		gap.moveTo(30.0);
		String pol = apple.getCurrentPolarisation();
		assertEquals("CRPol does not match", I05Apple.CIRCULAR_RIGHT, pol);
	}


	@Test
	public void testCLPol() throws Exception {
		lower.moveTo(-30.0);
		upper.moveTo(-30.0);
		gap.moveTo(30.0);
		String pol = apple.getCurrentPolarisation();
		assertEquals("CLPol does not match", I05Apple.CIRCULAR_LEFT, pol);
	}

	@Test(expected = DeviceException.class)
	public void testFailForMismatch() throws Exception {
		lower.moveTo(-7.0);
		upper.moveTo(17.0);

		// Should throw DeviceException as phases are outside tolerance;
		apple.getCurrentPolarisation();
	}

	@Test
	public void testLeftCircPhaseCalc() throws Exception {
		double phase = apple.getPhaseForGap(32.0, I05Apple.CIRCULAR_LEFT);
		assertEquals("testCircPhaseCalc phase does not match", -32.0, phase, 0.2);
	}

	@Test
	public void testRightCircPhaseCalc() throws Exception {
		double phase = apple.getPhaseForGap(32.0, I05Apple.CIRCULAR_RIGHT);
		assertEquals("testCircPhaseCalc phase does not match", 32.0, phase, 0.2);
	}

	@Test
	public void testTowerTop() throws Exception {
		TrajectorySolver ts = new I05Apple().new TrajectorySolver(rectangles);
		assertEquals(ts.towerTop, 38.0, 0.0);  // zero tolerance, should be identical
	}
	/*
	 * Trajectory solver tests
	 * - a better test would be be a double loop to intersection test each orthogonal step move against the exclusion zone
	 */
	@Test
	public void testAvoidA() throws Exception {
		Line2D line = new Line2D.Double(-60, 70,  -10, 40);   // tA
		TrajectorySolver ts = new I05Apple().new TrajectorySolver(rectangles);
		List<PGMove> list = ts.getGapPhaseOrder(line, 0);

		assertEquals(list.get(0).moveOrder ,"GP");
		assertArrayEquals(new Point2D[] { new Point2D.Double(-60, 70),    new Point2D.Double(-10, 40)},
				          new Point2D[] { list.get(0).moveVector.getP1(), list.get(0).moveVector.getP2()});
	}

	@Test
	public void testAvoidB() throws Exception {
		Line2D line = new Line2D.Double(-50, 70,   20, 30);   // tB
		TrajectorySolver ts = new I05Apple().new TrajectorySolver(rectangles);
		List<PGMove> list = ts.getGapPhaseOrder(line, 0);

		assertEquals(list.get(0).moveOrder ,"GP");
		assertArrayEquals(new Point2D[] { new Point2D.Double(-50, 70),    new Point2D.Double(0, 50)},  // 50 is half way between 70 & 30
				          new Point2D[] { list.get(0).moveVector.getP1(), list.get(0).moveVector.getP2()});
		assertEquals(list.get(1).moveOrder ,"PG");
		assertArrayEquals(new Point2D[] { new Point2D.Double(0, 50),    new Point2D.Double(20, 30)},
				          new Point2D[] { list.get(1).moveVector.getP1(), list.get(1).moveVector.getP2()});
	}

	@Test
	public void testAvoidC() throws Exception {
		Line2D line = new Line2D.Double( 67, 10,  -68, 20);   // tC
		TrajectorySolver ts = new I05Apple().new TrajectorySolver(rectangles);
		List<PGMove> list = ts.getGapPhaseOrder(line, 0);

		assertEquals(list.get(0).moveOrder ,"GP");
		assertArrayEquals(new Point2D[] { new Point2D.Double(67, 10),    new Point2D.Double(0, 38)},
				          new Point2D[] { list.get(0).moveVector.getP1(), list.get(0).moveVector.getP2()});
		assertEquals(list.get(1).moveOrder ,"PG");
		assertArrayEquals(new Point2D[] { new Point2D.Double(0, 38),    new Point2D.Double(-68, 20)},
				          new Point2D[] { list.get(1).moveVector.getP1(), list.get(1).moveVector.getP2()});
	}

	@Test
	public void testAvoidD() throws Exception {
		Line2D line = new Line2D.Double(-70, 10,   69, 60);      // tD
		// Line2D line = new Line2D.Double(-20, 40,   70, 25);
		TrajectorySolver ts = new I05Apple().new TrajectorySolver(rectangles);
		List<PGMove> list = ts.getGapPhaseOrder(line, 0);

		assertEquals(list.get(0).moveOrder ,"GP");
		assertArrayEquals(new Point2D[] { new Point2D.Double(-70, 10),    new Point2D.Double(0, 38)},
				          new Point2D[] { list.get(0).moveVector.getP1(), list.get(0).moveVector.getP2()});
		assertEquals(list.get(1).moveOrder ,"GP");
		assertArrayEquals(new Point2D[] { new Point2D.Double(0, 38),    new Point2D.Double(69, 60)},
				          new Point2D[] { list.get(1).moveVector.getP1(), list.get(1).moveVector.getP2()});
	}
}
