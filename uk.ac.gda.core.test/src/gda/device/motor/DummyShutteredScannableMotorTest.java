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

package gda.device.motor;

import gda.device.scannable.ScannableMotor;
import junit.framework.TestCase;

/**
 *
 */
public class DummyShutteredScannableMotorTest extends TestCase {
	private DummyMotor dummyMotor;
	private ScannableMotor scannableMotor;
	private ShutteredScannableMotor shutteredMotor;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		dummyMotor = new DummyMotor();
		dummyMotor.configure();
		scannableMotor = new ScannableMotor();
		scannableMotor.setMotor(dummyMotor);
		scannableMotor.configure();
		shutteredMotor = new ShutteredScannableMotor();
		shutteredMotor.setMotor(scannableMotor);
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	/**
	 *
	 */
	public void testSetUp() {
		assertNotNull(dummyMotor);
		assertNotNull(scannableMotor);
		assertNotNull(shutteredMotor);
	}

	public void testDoCalculations() {
		//three cases:
		//1. very long exposure - slow movement
		System.out.println("Test 1");
		shutteredMotor.setOpenPosition(0.1);
		shutteredMotor.setClosePosition(0.2);
		assertEquals(0.1, shutteredMotor.getMoveDistance(), 0.001);
		//alternatively, do setSpeed()
		shutteredMotor.setExposureTime(10);
		shutteredMotor.setDesiredTimeToVelocity(1.);
		shutteredMotor.setStartTimeFudgeFactor(0.2);
		shutteredMotor.setEndTimeFudgeFactor(0.2);
		assertEquals(0.01, shutteredMotor.getStartPositionOffset(), 0.001);
		assertEquals(0.09, shutteredMotor.getStartPosition(), 0.001);
		assertEquals(0.01, shutteredMotor.getEndPositionOffset(), 0.001);
		assertEquals(0.21, shutteredMotor.getEndPosition(), 0.001);

		//2. very short, fast exposure
		System.out.println("Test 2");
		shutteredMotor.setOpenPosition(1);
		shutteredMotor.setClosePosition(20);
		//alternatively, do setSpeed()
		shutteredMotor.setExposureTime(1);
		shutteredMotor.setDesiredTimeToVelocity(2.);
		shutteredMotor.setStartTimeFudgeFactor(0.2);
		shutteredMotor.setEndTimeFudgeFactor(0.2);
		assertEquals(21.85, shutteredMotor.getStartPositionOffset(), 0.01);
		assertEquals(-20.85, shutteredMotor.getStartPosition(), 0.01);
		assertEquals(21.85, shutteredMotor.getEndPositionOffset(), 0.01);
		assertEquals(41.85, shutteredMotor.getEndPosition(), 0.01);

		//3. normal (1 degree/sec) exposure
		System.out.println("Test 3");
		shutteredMotor.setOpenPosition(0);
		shutteredMotor.setClosePosition(1);
		//alternatively, do setSpeed()
		shutteredMotor.setExposureTime(1);
		shutteredMotor.setDesiredTimeToVelocity(0.1);
		shutteredMotor.setStartTimeFudgeFactor(0.2);
		shutteredMotor.setEndTimeFudgeFactor(0.2);
		assertEquals(0.25, shutteredMotor.getStartPositionOffset(), 0.01);
		assertEquals(-0.25, shutteredMotor.getStartPosition(), 0.01);
		assertEquals(0.25, shutteredMotor.getEndPositionOffset(), 0.01);
		assertEquals(1.25, shutteredMotor.getEndPosition(), 0.01);

		//4. faster (5 degree/sec) exposure
		System.out.println("test 4");
		shutteredMotor.setOpenPosition(0);
		shutteredMotor.setClosePosition(5);
		//alternatively, do setSpeed()
		shutteredMotor.setExposureTime(1);
		shutteredMotor.setDesiredTimeToVelocity(0.5);
		shutteredMotor.setStartTimeFudgeFactor(0.2);
		shutteredMotor.setEndTimeFudgeFactor(0.2);
		assertEquals(2.25, shutteredMotor.getStartPositionOffset(), 0.01);
		assertEquals(-2.25, shutteredMotor.getStartPosition(), 0.01);
		assertEquals(2.25, shutteredMotor.getEndPositionOffset(), 0.01);
		assertEquals(7.25, shutteredMotor.getEndPosition(), 0.01);

		//MXCamera test - same parameters as
		shutteredMotor.setOpenPosition(0);
		shutteredMotor.setClosePosition(5);
		shutteredMotor.setExposureTime(1); //setImageTime(2) / setNumberPasses(2)
		shutteredMotor.setStartTimeFudgeFactor(0.2044); // count shutter open time (0.0044) into extra open time
		shutteredMotor.setEndTimeFudgeFactor(0.2);
		assertEquals(2.272, shutteredMotor.getStartPositionOffset(), 0.01);
		assertEquals(-2.272, shutteredMotor.getStartPosition(), 0.01);
		assertEquals(2.25, shutteredMotor.getEndPositionOffset(),0.01);
		assertEquals(7.25, shutteredMotor.getEndPosition(),0.01);

		//use the same sequence of commands used by MXCameraTest. Should be the same results as the previous test
		shutteredMotor = new ShutteredScannableMotor();
		shutteredMotor.setMotor(scannableMotor);
		shutteredMotor.setShutterOpenTime(0.0044);
		shutteredMotor.setStartTimeFudgeFactor(0.2); // use like MX, separate shutter open time and fudge factor
		shutteredMotor.setEndTimeFudgeFactor(0.2);
		shutteredMotor.setMoveDistance(5);
		shutteredMotor.setExposureTime(1);
		shutteredMotor.setOpenPosition(0);
		assertEquals(-2.272, shutteredMotor.getStartPosition(), 0.01);
		assertEquals(7.25, shutteredMotor.getEndPosition(), 0.01);
		assertEquals(2.4044, shutteredMotor.getMinExposeTime(), 0.01);
		System.out.println(shutteredMotor.getClosePosition());

		System.out.println("all done!");
	}
}