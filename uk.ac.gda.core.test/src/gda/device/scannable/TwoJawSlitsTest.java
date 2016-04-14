/*-
 * Copyright © 2010 Diamond Light Source Ltd., Science and Technology
 * Facilities Council
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

package gda.device.scannable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import gda.device.DeviceException;
import gda.device.motor.TotalDummyMotor;
import gda.factory.Finder;
import gda.factory.ObjectFactory;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests the two Scannable classes used to operate a slit operated by two motors
 */
public class TwoJawSlitsTest {

	ObjectFactory factory;

	//these names must be unique else they clash with motors in other tests!!!
	String motorName1 = "TwoJawSlitsTestMotor01";
	String motorName2 = "TwoJawSlitsTestMotor02";
	String scannableMotorName1 = "TwoJawSlitsTestscannableMotor1";
	String scannableMotorName2 = "TwoJawSlitsTestscannableMotor2";

	String gapName = "slitGap";
	String positionName = "slitPosition";

	ScannableMotor scannableMotor1;
	ScannableMotor scannableMotor2;

	TwoJawSlitGap gap;
	TwoJawSlitPosition position;


	/**
	 * @throws Exception
	 */
	@Before
	public void setUp() throws Exception {
		factory = new ObjectFactory();
		Finder.getInstance().removeAllFactories();
		Finder finder = Finder.getInstance();
		finder.addFactory(factory);

		TotalDummyMotor motor1 = new TotalDummyMotor();
		motor1.setName(motorName1);
		factory.addFindable(motor1);
		TotalDummyMotor motor2 = new TotalDummyMotor();
		motor2.setName(motorName2);
		factory.addFindable(motor2);

		scannableMotor1 = new ScannableMotor();
		scannableMotor1.setMotorName(motorName1);
		scannableMotor1.setName(scannableMotorName1);
		scannableMotor1.setHardwareUnitString("mm"); // hardware will return mm's
		scannableMotor1.setUserUnits("mm");
		scannableMotor1.setUpperGdaLimits(5.0);
		scannableMotor1.setLowerGdaLimits(-1.0);
		factory.addFindable(scannableMotor1);
		scannableMotor1.configure();


		scannableMotor2 = new ScannableMotor();
		scannableMotor2.setMotorName(motorName2);
		scannableMotor2.setName(scannableMotorName2);
		scannableMotor2.setHardwareUnitString("mm"); // hardware will return mm's
		scannableMotor2.setUserUnits("mm");
		scannableMotor2.setUpperGdaLimits(1.0);
		scannableMotor2.setLowerGdaLimits(-5.0);
		factory.addFindable(scannableMotor2);
		scannableMotor2.configure();

		// so from these limits, position should be +/-5mm and gap should be up to 10 mm when position = 0

		gap = new TwoJawSlitGap();
		gap.setName(gapName);
		gap.setFirstJawName(scannableMotorName1);
		gap.setSecondJawName(scannableMotorName2);
		gap.setHardwareUnitString("mm");
		gap.setInitialUserUnits("mm"); // we talk in mm
		gap.configure();

		position = new TwoJawSlitPosition();
		position.setName(positionName);
		position.setFirstJawName(scannableMotorName1);
		position.setSecondJawName(scannableMotorName2);
		position.setHardwareUnitString("mm");
		position.setInitialUserUnits("mm"); // we talk in mm
		position.configure();
	}

	/**
	 *
	 */
	@Test
	public void testInputNames() {
		assertEquals(gapName, gap.getInputNames()[0]);
		assertEquals(1, gap.getInputNames().length);
		assertEquals(0, gap.getExtraNames().length);

		assertEquals(positionName, position.getInputNames()[0]);
		assertEquals(1, position.getInputNames().length);
		assertEquals(0, position.getExtraNames().length);
	}

	/**
	 *
	 */
	@Test
	public void testToString() {
		assertEquals("slitGap : 0.0000mm (0.0000:10.000)", gap.toFormattedString());
		assertEquals("slitPosition : 0.0000mm (-3.0000:3.0000)", position.toFormattedString());
	}


	@Test
	public void testGetPosition() throws Exception {
		assertEquals(0., gap.getPosition());
		assertEquals(0., position.getPosition());
	}


	/**
	 *
	 */
	@Test
	public void testIsPositionValid() {
		try {
			assertTrue(gap.checkPositionValid(11) != null);
			assertTrue(gap.checkPositionValid(9) == null);
			assertTrue(gap.checkPositionValid("9 mm") == null);
			assertTrue(gap.checkPositionValid("1.1 cm") != null);

			assertTrue(position.checkPositionValid("6 mm") != null);
			assertTrue(position.checkPositionValid("-4500 micron") != null);
		} catch (DeviceException e) {
			fail(e.getMessage());
		}

		double positionMax = position.getUpperGdaLimits()[0];
		double positionMin = position.getLowerGdaLimits()[0];

		double gapMax = gap.getUpperGdaLimits()[0];
		double gapMin = gap.getLowerGdaLimits()[0];

		//asserts here!
		assertEquals(3, positionMax, 0);
		assertEquals(-3, positionMin, 0);
		assertEquals(10, gapMax, 0);
		assertEquals(0, gapMin, 0);
	}

	/**
	 *
	 */
	@Test
	public void testAsynchronousMoveTo() {
		try {
			gap.setTolerance(0.000001);
			position.setTolerance(0.000001);

			gap.moveTo(9);
			assertTrue(gap.isAt(9));

			gap.moveTo("0.9 mm");
			assertTrue(gap.isAt("0.9 mm"));

			gap.moveTo("5 micron");
			assertTrue(gap.isAt("5 micron"));

			position.moveTo(1);
			assertTrue(position.isAt(1));
			assertTrue(gap.isAt("5 micron"));

			position.moveTo("0.9 mm");
			assertTrue(position.isAt("0.9 mm"));
			assertTrue(gap.isAt("5 micron"));

			position.moveTo("5 micron");
			assertTrue(position.isAt("5 micron"));
			assertTrue(gap.isAt("5 micron"));

		} catch (DeviceException e) {
			fail("exception during testAsynchronousMoveTo: " + e.getMessage());
		}

	}

	/**
	 *
	 */
	@Test
	public void testSetHardwareUnitString() {
		try {
			gap.setHardwareUnitString("micron");
			assertEquals("micron",gap.getHardwareUnitString());
			position.setHardwareUnitString("micron");
			assertEquals("micron",position.getHardwareUnitString());
		} catch (Exception e) {
			fail("exception during testSetHardwareUnitString: " + e.getMessage());
		}
	}


}
