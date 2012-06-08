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

import gda.device.Motor;
import gda.device.MotorException;
import gda.device.MotorStatus;
import gda.device.motor.TotalDummyMotor;
import junit.framework.TestCase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 */
public class TotalDummyMotorTest extends TestCase {
	private static final Logger logger = LoggerFactory.getLogger(TotalDummyMotorTest.class);

	TotalDummyMotor motor;

	static String gdaRoot = System.getProperty("gda.src.java");

	String motorDir = gdaRoot + "/tests/oe/motpos";

	private String motorName = "Motor01";

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		motor = new TotalDummyMotor();
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	/**
	 * 
	 */
	public void testMotorNotNull() {
		assertNotNull(motor);
	}

	/**
	 * 
	 */
	public void testMoveBy5() {
		double incr = 5.0;

		try {
			double posn1 = motor.getPosition();

			motor.moveBy(incr);
			double posn2 = motor.getPosition();
			assertEquals(posn2, posn1 + incr, 0.0);

		} catch (MotorException e) {
			logger.debug(e.getStackTrace().toString());
			fail();
		}

	}

	/**
	 * 
	 */
	public void testMoveTo10() {
		double posn = 10.0;

		try {
			motor.moveTo(posn);
			double posn2 = motor.getPosition();
			assertEquals(posn2, posn, 0.0);

		} catch (MotorException e) {
			logger.debug(e.getStackTrace().toString());
			fail();
		}

	}

	/**
	 * 
	 */
	public void testMoveContinuouslyAndStop() {
		try {
			assertTrue(!motor.isMoving());
			motor.moveContinuously(1);
			assertTrue(motor.isMoving());
			motor.stop();
			assertTrue(!motor.isMoving());
		} catch (MotorException e) {
			logger.debug(e.getStackTrace().toString());
			fail();
		}
	}

	/**
	 * 
	 */
	public void testMoveContinuouslyAndPanicStop() {
		try {
			assertTrue(!motor.isMoving());
			motor.moveContinuously(1);
			assertTrue(motor.isMoving());
			motor.panicStop();
			assertTrue(!motor.isMoving());
		} catch (MotorException e) {
			logger.debug(e.getStackTrace().toString());
			fail();
		}
	}

	/**
	 * 
	 */
	public void testSetPosition8() {
		double posn = 8.0;
		try {
			motor.setPosition(posn);
			double newPosn = motor.getPosition();
			assertEquals(posn, newPosn, 0.0);
		} catch (MotorException e) {
			logger.debug(e.getStackTrace().toString());
			fail();
		}

	}

	/**
	 * 
	 */
	public void testGetPosition() {
		try {
			motor.getPosition();
		} catch (MotorException e) {
			logger.debug(e.getStackTrace().toString());
			fail();
		}
	}

	/**
	 * 
	 */
	public void testSetSpeed() {
		try {
			double speed = 8.0;
			motor.setSpeed(speed);
			assertEquals(motor.getSpeed(), speed, 0.0);
		} catch (MotorException e) {
			logger.debug(e.getStackTrace().toString());
			fail();
		}
	}

	/**
	 * 
	 */
	public void testGetStatus() {
		MotorStatus status = motor.getStatus();
		assertEquals(status, MotorStatus.READY);
	}

	/**
	 * 
	 */
	public void testSetAndGetFastSpeed() {
		motor.setFastSpeed(50);
		assertEquals(motor.getFastSpeed(), 50);
	}

	/**
	 * 
	 */
	public void testSetandGetMediumSpeed() {
		motor.setMediumSpeed(50);
		assertEquals(motor.getMediumSpeed(), 50);
	}

	/**
	 * 
	 */
	public void testSetAndGetSlowSpeed() {
		motor.setSlowSpeed(50);
		assertEquals(motor.getSlowSpeed(), 50);
	}

	/**
	 * 
	 */
	public void testSetBacklashSteps() {
		motor.setBacklashSteps(2.0);
		assertEquals(motor.getBacklashSteps(), 2.0, 0.0);
	}

	/**
	 * 
	 */
	public void testIsLimitsSettableAndSetLimitsSettable() {
		if (!motor.isLimitsSettable()) {
			motor.setLimitsSettable(true);
			assertTrue(motor.isLimitsSettable());
		} else {
			motor.setLimitsSettable(false);
			assertTrue(!motor.isLimitsSettable());
		}
	}

	/**
	 * 
	 */
	public void testSavePosition() {
		try {
			motor.setPosition(67);
			motor.savePosition(motorName);
		} catch (MotorException e) {
			logger.debug(e.getStackTrace().toString());
			fail();
		}

	}

	/**
	 * 
	 */
	public void testLoadPosition() {
		motor.loadPosition(motorName);
		try {
			assertEquals(motor.getPosition(), 67.0, 0.0);
		} catch (MotorException e) {
			logger.debug(e.getStackTrace().toString());
			fail();
		}
	}

	/**
	 * 
	 */
	public void testSetSpeedLevel() {
		try {
			motor.setSlowSpeed(87);
			motor.setMediumSpeed(95);
			motor.setFastSpeed(104);

			motor.setSpeedLevel(Motor.SLOW);
			assertEquals(motor.getSpeed(), 87.0, 0.0);

			motor.setSpeedLevel(Motor.MEDIUM);
			assertEquals(motor.getSpeed(), 95.0, 0.0);

			motor.setSpeedLevel(Motor.FAST);
			assertEquals(motor.getSpeed(), 104.0, 0.0);
		} catch (MotorException e) {
			logger.debug(e.getStackTrace().toString());
			fail();
		}
	}

	/**
	 * 
	 */
	public void testAddInBacklash() {
		double increment = 10.0;

		motor.setBacklashSteps(-1.0);
		assertEquals(motor.getBacklashSteps(), -1.0, 0.0);
		assertEquals(motor.addInBacklash(increment), increment + 1.0, 0.0);

		motor.setBacklashSteps(1.0);
		assertEquals(motor.getBacklashSteps(), 1.0, 0.0);
		assertEquals(motor.addInBacklash(increment), increment, 0.0);

		increment = -10.0;

		motor.setBacklashSteps(-1.0);
		assertEquals(motor.getBacklashSteps(), -1.0, 0.0);
		assertEquals(motor.addInBacklash(increment), increment, 0.0);

		motor.setBacklashSteps(1.0);
		assertEquals(motor.getBacklashSteps(), 1.0, 0.0);
		assertEquals(motor.addInBacklash(increment), increment - 1.0, 0.0);
	}

	/**
	 * 
	 */
	public void testCorrectBacklash() {

		try {
			// test negative backlash
			motor.setBacklashSteps(-1.0);
			assertEquals(motor.getBacklashSteps(), -1.0, 0.0);

			double position = motor.getPosition();

			double increment = 10.0;

			increment = motor.addInBacklash(increment);

			motor.moveBy(increment);

			assertEquals(motor.getPosition(), position + 11.0, 0.0);

			motor.correctBacklash();

			assertEquals(motor.getPosition(), position + 10.0, 0.0);

			// test positive backlash
			motor.setBacklashSteps(1.0);
			assertEquals(motor.getBacklashSteps(), 1.0, 0.0);

			position = motor.getPosition();

			increment = -10.0;

			increment = motor.addInBacklash(increment);

			motor.moveBy(increment);

			assertEquals(motor.getPosition(), position - 11.0, 0.0);

			motor.correctBacklash();

			assertEquals(motor.getPosition(), position - 10.0, 0.0);
		} catch (MotorException e) {
			logger.debug(e.getStackTrace().toString());
			fail();
		}

	}

	/**
	 * 
	 */
	public void testSetSoftLimits() {
		// TODO soft limits cannot be tested until all the motor code has been
		// reviewed.
	}

	/**
	 * 
	 */
	public void testIsHomeable() {
		try {
			if (motor.isHomeable()) {
				// motor is homeable - make sure isHomed makes sense
				if (motor.isHomed()) {
					motor.moveBy(1.0);
					assertEquals(motor.isHomed(), false);
				}

				// should be not homed now.
				// home it and make sure it does home itself
				motor.home();
				assertEquals(motor.isHomed(), true);
			} else {
				// motor not homeable - make sure home has no effect and isHomed
				// always false
				assertEquals(motor.isHomed(), false);
				motor.home();
				assertEquals(motor.isHomed(), false);
			}
		} catch (MotorException e) {
			logger.debug(e.getStackTrace().toString());
			fail();
		}

	}

	/**
	 * 
	 */
	public void testHome() {
		try {
			if (motor.isHomeable()) {
				motor.home();
				assertEquals(motor.isHomed(), true);
				motor.moveBy(1.0);
				assertEquals(motor.isHomed(), false);
				motor.home();
				assertEquals(motor.isHomed(), true);
			} else {
				// motor not homeable - make sure home has no effect and isHomed
				// always false
				assertEquals(motor.isHomed(), false);
				motor.home();
				assertEquals(motor.isHomed(), false);
			}
		} catch (MotorException e) {
			logger.debug(e.getStackTrace().toString());
			fail();
		}
	}

}
