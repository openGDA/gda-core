/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
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

import static org.jscience.physics.units.SI.METER;
import static org.jscience.physics.units.SI.MILLI;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import gda.configuration.properties.LocalProperties;
import gda.device.DeviceException;
import gda.device.Motor;
import gda.device.MotorStatus;
import gda.device.ScannableMotion;
import gda.device.motor.TotalDummyMotor;
import gda.factory.Finder;
import gda.factory.ObjectFactory;
import junit.framework.Assert;
import junitx.framework.ArrayAssert;

import org.jscience.physics.quantities.Quantity;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * These tests have been copied from ScannableMotorTest to try and ensure ScannableMotorWithDemandPosition behaves the same where it makes sense.
 * <p>
 * All the tests which set returnDemandPosition = true have been removed as this option is not supported for ScannableMotorWithDemandPosition
 * <p>
 * Note basic limits testing and offset/scaling testing is performed in SMBT and SMUBT. Enough is done here to make sure things are tied together properly only.
 */
public class ScannableMotorWithDemandPositionTest {

	ScannableMotor sm;
	Motor motor;

	String gdaRoot = System.getProperty("gda.src.java");
	String motorDir = gdaRoot + "/tests/oe/motpos";

	/**
	 * @throws Exception
	 */
	@Before
	public void setUp() throws Exception {

		motor = mock(Motor.class);
		when(motor.getStatus()).thenReturn(MotorStatus.READY);
		when(motor.getMinPosition()).thenReturn(Double.NaN);
		when(motor.getMaxPosition()).thenReturn(Double.NaN);

		sm = new ScannableMotorWithDemandPosition();
		sm.setMotor(motor);
		sm.setName("sm");
		sm.setHardwareUnitString("mm"); // hardware will return mm's
		sm.setInitialUserUnits("micron"); // we talk in microns
		sm.setUpperGdaLimits(1000.0); // limits are +/- 1000 microns
		sm.setLowerGdaLimits(-1000.0);
		sm.configure();
		assertFalse(sm.isReturningDemandPosition());
	}

	@Test
	public void testConfigureWithMotorName() throws Exception {
		ObjectFactory factory = new ObjectFactory();
		Finder finder = Finder.getInstance();
		finder.addFactory(factory);

		TotalDummyMotor motor = new TotalDummyMotor();
		motor.setName("motor");
		factory.addFindable(motor);

		sm = new ScannableMotorWithDemandPosition();
		sm.setMotorName("motor");
		sm.configure();
		assertEquals(motor, sm.getMotor());
		assertEquals(sm.getMotorLimitsComponent(), sm.getAdditionalPositionValidators().get(0));
	}

	@Test
	public void testInputAndExtraNames() throws Exception {
		assertEquals("sm", sm.getInputNames()[0]);
		assertEquals(1, sm.getInputNames().length);
		// Should have 1 extra name for demand position
		assertEquals(1, sm.getExtraNames().length);
		// Check the extra_name is correct
		String extra_name = sm.getExtraNames()[0];
		String input_name = sm.getInputNames()[0];
		assertTrue("Extra name for demand is wrong", extra_name.equals(input_name + "_demand"));
	}

	@Test(expected = DeviceException.class)
	public void testSetUserUnitsWithWrongUnit() throws Exception {
		sm.setUserUnits("eV");
	}

	@Test
	public void testGetPosition() throws Exception {
		when(motor.getPosition()).thenReturn(1.); // mm
		// For the motor which also returns demand only check the first value returned by getPosition()
		double actualPosition = (double) ((Object[]) sm.getPosition())[0];
		assertEquals("Value returned from get position was wrong", 1000.0, actualPosition, 0.00001); // micron
	}

	@Test
	public void testGetPositionWithOffset() throws Exception {
		sm.setOffset(1.);
		when(motor.getPosition()).thenReturn(1.); // mm
		double actualPosition = (double) ((Object[]) sm.getPosition())[0];
		assertEquals(1001., actualPosition, .00001); // micron
	}

	@Test
	public void testChangeMotorStatusTechniqueForFollowingTests() throws Exception {
		assertFalse(sm.isBusy());
		when(motor.getStatus()).thenReturn(MotorStatus.BUSY);
		assertTrue(sm.isBusy());
		when(motor.getStatus()).thenReturn(MotorStatus.READY);
		assertFalse(sm.isBusy());
	}

	@Test
	public void testGetLimitsWithOfset() throws Exception {
		sm.setOffset(1.);
		assertEquals(1001., sm.getUpperGdaLimits()[0], .000001); // micron
		assertEquals(1001., sm.getUpperGdaLimits()[0], .000001); // micron
		assertEquals(-999., sm.getLowerGdaLimits()[0], .000001); // micron
		assertEquals(-999., sm.getLowerGdaLimits()[0], .000001); // micron
	}

	@Test
	public void testToFormattedString() throws Exception {
		when(motor.getPosition()).thenReturn(.01); // mm
		assertEquals("sm : sm: 10.000micron sm_demand: unknownmicron (-1000.0:1000.0)", sm.toFormattedString());
	}

	@Test(expected = RuntimeException.class)
	public void testToStringWithNoMotorSet() throws Exception {
		sm = new ScannableMotorWithDemandPosition();
		sm.setName("sm");
		sm.toFormattedString(); // check this throws RuntimeException
	}

	@Test
	public void testToStringWithNoLimitsComponent() throws Exception {
		sm = new ScannableMotorWithDemandPosition();
		sm.setName("sm");
		sm.setMotor(motor);
		sm.setMotorLimitsComponent(null);
		sm.configure();
		assertEquals("sm : sm: 0.0000 sm_demand: unknown", sm.toFormattedString());
	}

	@Test
	public void testToStringAfterMove() throws Exception {
		sm = new ScannableMotorWithDemandPosition();
		sm.setName("sm");
		sm.setMotor(motor);
		sm.configure();
		sm.asynchronousMoveTo(10.0);
		when(motor.getPosition()).thenReturn(.01);
		assertEquals("sm : sm: 0.010000 sm_demand: 10.000", sm.toFormattedString());
	}

	@Test
	public void testToStringWithMotorLimits() throws Exception {
		when(motor.getPosition()).thenReturn(.01); // mm
		when(motor.getMinPosition()).thenReturn(-.02);// mm
		when(motor.getMaxPosition()).thenReturn(.02);// mm
		assertEquals("sm : sm: 10.000micron sm_demand: unknownmicron (-1000.0:1000.0) mot(-20.000:20.000)", sm.toFormattedString());
	}

	@Test
	public void testToStringWithOffset() throws Exception {
		when(motor.getPosition()).thenReturn(.01); // mm
		sm.setOffset(1.);
		assertEquals(1001., sm.getUpperGdaLimits()[0], .000001); // micron
		assertEquals(-999., sm.getLowerGdaLimits()[0], .000001); // micron
		assertEquals("sm : sm: 11.000micron(+1.0000) sm_demand: unknownmicron (-999.00:1001.0)", sm.toFormattedString());
	}

	@Test
	public void testToStringWithDifferentOutputFormatsForActualAndDemand() throws Exception {
		when(motor.getPosition()).thenReturn(.01); // mm
		sm.setOffset(1.);
		assertEquals(1001., sm.getUpperGdaLimits()[0], .000001); // micron
		assertEquals(-999., sm.getLowerGdaLimits()[0], .000001); // micron
		assertEquals("sm : sm: 11.000micron(+1.0000) sm_demand: unknownmicron (-999.00:1001.0)", sm.toFormattedString());
	}

	@Test
	public void testIsPositionValid() throws Exception {
		assertTrue(sm.checkPositionValid(2000) != null);
		assertTrue(sm.checkPositionValid("2000 mm") != null);
		assertTrue(sm.checkPositionValid("2000 nm") == null);
		assertTrue(sm.checkPositionValid(99) == null);
		assertTrue(sm.checkPositionValid(-49) == null);
	}

	@Test
	public void testIsBusy() throws Exception {
		assertFalse(sm.isBusy());
		when(motor.getStatus()).thenReturn(MotorStatus.BUSY);
		assertTrue(sm.isBusy());
	}

	@Test
	public void testAsynchronousMoveTo() throws Exception {
		sm.asynchronousMoveTo(50); // micron
		verify(motor).moveTo(.049999999999999996);
		// Check demand position is also set correctly
		assertEquals("Demand position was wrong", 50.0, (double) sm.getDemandPosition(), 0.00000001);
	}

	@Test
	public void testAsynchronousMoveTo2() throws Exception {
		sm.moveTo("0.9 mm");
		verify(motor).moveTo(0.9000000000000001);
		// Check demand position is also set correctly
		assertEquals("Demand position was wrong", 900.0, (double) sm.getDemandPosition(), 0.00000001);
	}

	@Test
	public void testAsynchronousMoveTo3() throws Exception {
		sm.moveTo("5 micron");
		verify(motor).moveTo(.004999999999999999);
		// Check demand position is also set correctly
		assertEquals("Demand position was wrong", 5.0, (double) sm.getDemandPosition(), 0.00000001);
	}

	@Test
	public void testIsAt1() throws Exception {
		sm.setTolerance(.1); // micron
		when(motor.getPosition()).thenReturn(.001); // mm
		assertTrue(sm.isAt(1)); // micron
		assertFalse(sm.isAt(1.2)); // micron
	}

	@Test
	public void testIsAt2() throws Exception {
		sm.setTolerance(.1); // micron
		when(motor.getPosition()).thenReturn(.001); // mm
		assertTrue(sm.isAt(Quantity.valueOf(0.001, MILLI(METER))));
		assertFalse(sm.isAt(Quantity.valueOf(0.0012, MILLI(METER))));

	}

	@Test
	public void testIsAt3() throws Exception {
		sm.setTolerance(.1); // micron
		when(motor.getPosition()).thenReturn(.001); // mm
		assertTrue(sm.isAt("0.001 mm"));
		assertFalse(sm.isAt("0.0012 mm"));
	}

	@Test
	public void testSetLowerGdaLimitsDouble() throws Exception {
		assertTrue(sm.checkPositionValid(-99) == null);
		sm.setLowerGdaLimits(-10.0);
		assertTrue(sm.checkPositionValid(-99) != null);
		sm.setUserUnits("mm");
		sm.setLowerGdaLimits(-10.0);
		assertTrue(sm.checkPositionValid("-9 mm") == null);
	}

	@Test
	public void testSetUpperGdaLimitsDouble() throws Exception {
		assertTrue(sm.checkPositionValid(99) == null);
		sm.setUpperGdaLimits(10.0);
		assertTrue(sm.checkPositionValid(99) != null);
		sm.setUserUnits("mm");
		sm.setUpperGdaLimits(10.0);
		assertTrue(sm.checkPositionValid("9 mm") == null);
	}

	@Test
	public void testSetOffset() throws Exception {
		sm.moveTo(10); // move to 10 microns
		when(motor.getPosition()).thenReturn(0.01); // the mocked motor must be moved manually to 0.01mm
		sm.setOffset(-10);
		sm.setTolerance(0.00000001);
		assertTrue(sm.isAt(0));
	}

	@Test
	public void testSetScalingFactor() throws Exception {
		sm.moveTo(50);
		when(motor.getPosition()).thenReturn(0.05); // the mocked motor must be moved to 0.05mm

		sm.setScalingFactor(-1.);
		sm.setTolerance(0.00000001); // cannot use isAt with doubles unless a tolerence is set
		assertTrue(sm.isAt(-50));

		sm.setScalingFactor(2.);
		sm.setTolerance(0.00000001);
		assertTrue(sm.isAt(100));
	}

	@Test
	public void testSetHardwareUnitString() throws Exception {
		sm.setHardwareUnitString("micron");
		assertEquals("micron", sm.getHardwareUnitString());
	}

	/**
	 * Test with regular (dummy for example) motor.
	 *
	 * @throws Exception
	 */
	@Test
	public void testConfigureWithMotor_setHwOnly() throws Exception {

		Motor mockedMotor = mock(Motor.class);
		ScannableMotor scannable = new ScannableMotorWithDemandPosition();
		scannable.setMotor(mockedMotor);
		scannable.setName("scn_name");
		scannable.setHardwareUnitString("eV");
		scannable.configure();
		assertEquals("eV", scannable.getHardwareUnitString());
		assertEquals("eV", scannable.getUserUnits());
		ArrayAssert.assertEquals(new String[] { "keV", "eV", "GeV" }, scannable.getAcceptableUnits());
	}

	@Test
	public void testConfigureWithMotor_setUserOnly() throws Exception {
		Motor mockedMotor = mock(Motor.class);
		ScannableMotor scannable = new ScannableMotorWithDemandPosition();
		scannable.setMotor(mockedMotor);
		scannable.setName("scn_name");
		scannable.setUserUnits("eV");
		scannable.configure();
		assertEquals("eV", scannable.getHardwareUnitString());
		assertEquals("eV", scannable.getUserUnits());
		ArrayAssert.assertEquals(new String[] { "keV", "eV", "GeV" }, scannable.getAcceptableUnits());
	}

	@Test
	public void testConfigureWithMotor_setUserAndHardware() throws Exception {
		Motor mockedMotor = mock(Motor.class);
		ScannableMotor scannable = new ScannableMotorWithDemandPosition();
		scannable.setMotor(mockedMotor);
		scannable.setName("scn_name");
		scannable.setUserUnits("eV");
		scannable.setHardwareUnitString("keV");
		scannable.configure();
		assertEquals("keV", scannable.getHardwareUnitString());
		assertEquals("eV", scannable.getUserUnits());
		ArrayAssert.assertEquals(new String[] { "keV", "eV", "GeV" }, scannable.getAcceptableUnits());
	}

	@Test
	public void testConfigureWithMotorThatImplementsMotorUnitStringSupplier() throws Exception {
		UnitStringProvidingMotor mockedMotor = mock(UnitStringProvidingMotor.class);
		when(mockedMotor.getUnitString()).thenReturn("eV");
		ScannableMotor scannable = new ScannableMotorWithDemandPosition();
		scannable.setMotor(mockedMotor);
		scannable.setUserUnits("keV");
		scannable.configure();
		assertEquals("eV", scannable.getHardwareUnitString());
		assertEquals("keV", scannable.getUserUnits());
		ArrayAssert.assertEquals(new String[] { "keV", "eV", "GeV" }, scannable.getAcceptableUnits());
	}

	@Test(expected = DeviceException.class)
	public void testAsyncronousMoveToExceptionIfMoving() throws Exception {
		when(motor.getStatus()).thenReturn(MotorStatus.BUSY);
		sm.asynchronousMoveTo(50); // Should throw DeviceException as motor is already moving
	}

	@Test
	public void testCheckLimitCodeIntegration() throws Exception {
		when(motor.getMinPosition()).thenReturn(-1.); // mm
		when(motor.getMaxPosition()).thenReturn(.5); // mm
		assertEquals(null, sm.checkPositionValid(499)); // micron
		assertEquals("Motor limit violation on motor null: 0.501000 > 0.500000 (internal/hardware/dial values).", sm.checkPositionValid(501)); // micron
	}

	@Test
	public void testGetLowerInnerLimit() throws Exception {
		Double nullDouble = null;
		sm.setLowerGdaLimits(nullDouble);
		assertEquals(null, sm.getLowerInnerLimit());
		sm.setLowerGdaLimits(-1000.); // microns
		assertEquals(new Double(-1000.), sm.getLowerInnerLimit()); // microns
		when(motor.getMinPosition()).thenReturn(-1.001); // mm
		assertEquals(new Double(-1000.), sm.getLowerInnerLimit()); // microns
		when(motor.getMinPosition()).thenReturn(-.999); // mm
		assertEquals(-999., sm.getLowerInnerLimit(), .00000001); // microns
	}

	@Test
	public void testGetUpperInnerLimit() throws Exception {
		Double nullDouble = null;
		sm.setUpperGdaLimits(nullDouble);
		assertEquals(null, sm.getUpperInnerLimit());
		sm.setUpperGdaLimits(1000.); // microns
		assertEquals(new Double(1000.), sm.getUpperInnerLimit()); // microns
		when(motor.getMinPosition()).thenReturn(-1.001); // mm
		assertEquals(new Double(1000.), sm.getUpperInnerLimit()); // microns
		when(motor.getMaxPosition()).thenReturn(.999); // mm
		assertEquals(999., sm.getUpperInnerLimit(), .00000001); // microns
	}

	@Test
	public void testGetAttributeLimitsMin() throws Exception {
		Double nullDouble = null;
		sm.setLowerGdaLimits(nullDouble);// microns
		sm.setUpperGdaLimits(nullDouble);// microns
		when(motor.getMinPosition()).thenReturn(Double.NaN); // mm
		when(motor.getMaxPosition()).thenReturn(Double.NaN); // mm
		Assert.assertNull(sm.getAttribute(ScannableMotion.FIRSTINPUTLIMITS));

		Double val1mm = new Double(-1.001);
		when(motor.getMinPosition()).thenReturn(val1mm); // mm
		Double val1microns = new Double(-1001.);

		ScannableMotionUnitsBaseTest.assertAlmostEqual(new Double[] { val1microns, null }, (Double[]) sm.getAttribute(ScannableMotion.FIRSTINPUTLIMITS));

		sm.setLowerGdaLimits(-1002.);// microns
		ScannableMotionUnitsBaseTest.assertAlmostEqual(new Double[] { val1microns, null }, (Double[]) sm.getAttribute(ScannableMotion.FIRSTINPUTLIMITS));

		Double val2 = new Double(-999.);
		sm.setLowerGdaLimits(val2);// microns
		ScannableMotionUnitsBaseTest.assertAlmostEqual(new Double[] { val2, null }, (Double[]) sm.getAttribute(ScannableMotion.FIRSTINPUTLIMITS));

		when(motor.getMinPosition()).thenReturn(Double.NaN); // mm
		ScannableMotionUnitsBaseTest.assertAlmostEqual(new Double[] { val2, null }, (Double[]) sm.getAttribute(ScannableMotion.FIRSTINPUTLIMITS));
		ScannableMotionUnitsBaseTest.assertAlmostEqual(new Double[] { val2, null }, (Double[]) sm.getAttribute("limits"));
		ScannableMotionUnitsBaseTest.assertAlmostEqual(new Double[] { val2, null }, sm.getFirstInputLimits());

	}

	@Test
	public void testGetAttributeLimitsMax() throws Exception {
		Double nullDouble = null;
		sm.setLowerGdaLimits(nullDouble);// microns
		sm.setUpperGdaLimits(nullDouble);// microns
		when(motor.getMinPosition()).thenReturn(Double.NaN); // mm
		when(motor.getMaxPosition()).thenReturn(Double.NaN); // mm

		Double val1mm = new Double(1.001);
		when(motor.getMaxPosition()).thenReturn(val1mm); // mm
		Double val1microns = new Double(1001.);

		ScannableMotionUnitsBaseTest.assertAlmostEqual(new Double[] { null, val1microns }, (Double[]) sm.getAttribute(ScannableMotion.FIRSTINPUTLIMITS));

		sm.setUpperGdaLimits(1002.);// microns
		ScannableMotionUnitsBaseTest.assertAlmostEqual(new Double[] { null, val1microns }, (Double[]) sm.getAttribute(ScannableMotion.FIRSTINPUTLIMITS));

		Double val2 = new Double(999.);
		sm.setUpperGdaLimits(val2);// microns
		ScannableMotionUnitsBaseTest.assertAlmostEqual(new Double[] { null, val2 }, (Double[]) sm.getAttribute(ScannableMotion.FIRSTINPUTLIMITS));

		when(motor.getMaxPosition()).thenReturn(Double.NaN); // mm
		ScannableMotionUnitsBaseTest.assertAlmostEqual(new Double[] { null, val2 }, (Double[]) sm.getAttribute(ScannableMotion.FIRSTINPUTLIMITS));
		ScannableMotionUnitsBaseTest.assertAlmostEqual(new Double[] { null, val2 }, (Double[]) sm.getAttribute("limits"));
		ScannableMotionUnitsBaseTest.assertAlmostEqual(new Double[] { null, val2 }, sm.getFirstInputLimits());
	}

	@Test
	public void testGetMotorLimit() throws Exception {
		when(motor.getMinPosition()).thenReturn(-1.); // mm
		when(motor.getMaxPosition()).thenReturn(.8); // mm
		assertEquals(-1000., sm.getLowerMotorLimit(), .00001); // micron
		assertEquals(800., sm.getUpperMotorLimit(), .00001); // micron
	}

	@Test(expected = DeviceException.class)
	public void testGetLowerMotorLimitWithNoMotorLimitsComponent() throws Exception {
		sm.setMotorLimitsComponent(null);
		sm.getLowerMotorLimit();
	}

	@Test(expected = DeviceException.class)
	public void testGetUpperMotorLimitWithNoMotorLimitsComponent() throws Exception {
		sm.setMotorLimitsComponent(null);
		sm.getLowerMotorLimit();
	}

	@Test
	public void testGetMotorLimitWithNegativeScale() throws Exception {
		when(motor.getMinPosition()).thenReturn(-1.); // mm
		when(motor.getMaxPosition()).thenReturn(.8); // mm
		sm.setScalingFactor(-1.);
		assertEquals(-800., sm.getLowerMotorLimit(), .00001); // micron
		assertEquals(1000., sm.getUpperMotorLimit(), .00001); // micron
	}

	@Test
	public void testToStringWithNegativeScalingFactor() throws Exception {
		when(motor.getPosition()).thenReturn(.01); // mm
		when(motor.getMinPosition()).thenReturn(-.02);// mm
		when(motor.getMaxPosition()).thenReturn(.01);// mm
		sm.setScalingFactor(-1.);
		sm.setLowerGdaLimits(-1000.);
		sm.setUpperGdaLimits(800.);
		assertEquals("sm : sm: -10.000micron sm_demand: unknownmicron (-1000.0:800.00) mot(-10.000:20.000)", sm.toFormattedString());
	}

	@Test
	public void testCheckPositionWithNegativeScalingFactor_MotorLimits() throws Exception {
		Double nullDouble = null;
		sm.setLowerGdaLimits(nullDouble);
		sm.setUpperGdaLimits(nullDouble);
		when(motor.getMinPosition()).thenReturn(-.02);// mm
		when(motor.getMaxPosition()).thenReturn(.01);// mm
		when(motor.getName()).thenReturn("motname");
		sm.setScalingFactor(-1.);

		assertEquals(null, sm.checkPositionValid(0.));// micron
		assertEquals(null, sm.checkPositionValid(19.));// micron
		assertEquals(null, sm.checkPositionValid(-9.));// micron
		assertEquals("Motor limit violation on motor motname: -0.021000 < -0.020000 (internal/hardware/dial values).", sm.checkPositionValid(21.));// micron
		assertEquals("Motor limit violation on motor motname: 0.011000 > 0.010000 (internal/hardware/dial values).", sm.checkPositionValid(-11.));// micron
	}

	@Test
	public void testCheckPositionWithNegativeScalingFactor_ScannableLimits() throws Exception {
		when(motor.getMinPosition()).thenReturn(Double.NaN);// mm
		when(motor.getMaxPosition()).thenReturn(Double.NaN);// mm
		sm.setScalingFactor(-1.);
		sm.setLowerGdaLimits(-1000.);
		sm.setUpperGdaLimits(800.);

		assertEquals(null, sm.checkPositionValid(0.));// micron
		assertEquals(null, sm.checkPositionValid(-999));// micron
		assertEquals(null, sm.checkPositionValid(799));// micron
		assertEquals("Scannable limit violation on sm.sm: 1.001 > 1.0 (internal/hardware/dial values).", sm.checkPositionValid(-1001));// micron
		assertEquals("Scannable limit violation on sm.sm: -0.8009999999999999 < -0.7999999999999999 (internal/hardware/dial values).",
				sm.checkPositionValid(801));// micron
	}

	@Test
	public void testCopyMotorLimitsIntoScannableLimitsAtConfiguration_Off() throws Exception {
		LocalProperties.set(ScannableMotor.COPY_MOTOR_LIMITS_INTO_SCANNABLE_LIMITS, "false");
		when(motor.getMinPosition()).thenReturn(-10.);
		when(motor.getMaxPosition()).thenReturn(10.);
		sm = new ScannableMotorWithDemandPosition();
		sm.setMotor(motor);
		sm.configure();
		Double nullDouble = null;
		assertEquals(nullDouble, sm.getLowerGdaLimits());
		assertEquals(nullDouble, sm.getUpperGdaLimits());
	}

	@Test
	public void testCopyMotorLimitsIntoScannableLimitsAtConfiguration_Limits_ManuallySet() throws Exception {
		LocalProperties.set(ScannableMotor.COPY_MOTOR_LIMITS_INTO_SCANNABLE_LIMITS, "true");
		when(motor.getMinPosition()).thenReturn(-10.);
		when(motor.getMaxPosition()).thenReturn(10.);
		sm = new ScannableMotorWithDemandPosition();
		sm.setMotor(motor);
		sm.setLowerGdaLimits(-91.);
		sm.setUpperGdaLimits(89.);
		sm.configure();
		ArrayAssert.assertEquals(new Double[] { -91. }, sm.getLowerGdaLimits());
		ArrayAssert.assertEquals(new Double[] { 89. }, sm.getUpperGdaLimits());
	}

	@Test
	public void testCopyMotorLimitsIntoScannable_ScaleNegative() throws Exception {
		LocalProperties.set(ScannableMotor.COPY_MOTOR_LIMITS_INTO_SCANNABLE_LIMITS, "true");
		when(motor.getMinPosition()).thenReturn(-10.);
		when(motor.getMaxPosition()).thenReturn(20.);
		sm = new ScannableMotorWithDemandPosition();
		sm.setMotor(motor);
		sm.setScalingFactor(-1.0);
		sm.configure();
		assertEquals(-20., sm.getLowerGdaLimits()[0], 0.001);
		assertEquals(10., sm.getUpperGdaLimits()[0], 0.001);
	}

	@Test
	public void testCopyMotorLimitsIntoScannableLimitsAtConfiguration_OnUnitsAndOffset() throws Exception {
		LocalProperties.set("gda.device.scannable.ScannableMotor.copyMotorLimitsIntoScannableLimits", "true");
		when(motor.getMinPosition()).thenReturn(-10.);// mm
		when(motor.getMaxPosition()).thenReturn(6.);// mm
		sm = new ScannableMotorWithDemandPosition();
		sm.setMotor(motor);
		sm.setHardwareUnitString("mm");
		sm.setInitialUserUnits("micron");
		sm.configure();
		sm.setOffset(1000.);// micron
		assertEquals(-9000., sm.getLowerGdaLimits()[0], .0001);// micron
		assertEquals(7000., sm.getUpperGdaLimits()[0], .00001);// micron
	}

	@Test
	public void testCopyMotorLimitsIntoScannableLimitsAtConfiguration_OnUnitsAndScale() throws Exception {
		LocalProperties.set("gda.device.scannable.ScannableMotor.copyMotorLimitsIntoScannableLimits", "true");
		when(motor.getMinPosition()).thenReturn(-10.);// mm
		when(motor.getMaxPosition()).thenReturn(6.);// mm
		sm = new ScannableMotorWithDemandPosition();
		sm.setMotor(motor);
		sm.setHardwareUnitString("mm");
		sm.setInitialUserUnits("micron");
		sm.configure();
		sm.setScalingFactor(-1.);// micron
		assertEquals(-6000., sm.getLowerGdaLimits()[0], .0001);// micron
		assertEquals(10000., sm.getUpperGdaLimits()[0], .00001);// micron
	}

	@Test
	public void testCopyMotorLimitsIntoScannableLimitsAtConfiguration_NoMotorLimits() throws Exception {
		LocalProperties.set("gda.device.scannable.ScannableMotor.copyMotorLimitsIntoScannableLimits", "true");
		when(motor.getMinPosition()).thenReturn(Double.NaN);
		when(motor.getMaxPosition()).thenReturn(Double.NaN);
		sm = new ScannableMotorWithDemandPosition();
		sm.setMotor(motor);
		sm.configure();
		assertEquals(-Double.MAX_VALUE, sm.getLowerGdaLimits()[0], .001);
		assertEquals(Double.MAX_VALUE, sm.getUpperGdaLimits()[0], .001);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testSettingReturnDemandPositionFlagThrows() throws Exception {
		sm = new ScannableMotorWithDemandPosition();
		sm.setMotor(motor);
		sm.configure();
		assertEquals(false, sm.isReturningDemandPosition());
		// Changing to true should throw as this ScannableMotorWithDemandPosition doesn't support it
		sm.setReturnDemandPosition(true);
	}

	@Test(expected = IllegalStateException.class)
	public void testSettingExtraNamesThrows() throws Exception {
		sm = new ScannableMotorWithDemandPosition();
		sm.setMotor(motor);
		sm.setExtraNames(new String[] { "test extra name" });
		sm.configure(); // Should throw as extra names should not be set
	}

	@Test(expected = IllegalStateException.class)
	public void testSettingThreeOutputFormatsThrows() throws Exception {
		sm = new ScannableMotorWithDemandPosition();
		sm.setMotor(motor);
		sm.setOutputFormat(new String[] { "%5.5g", "%5.5g", "%5.5g" });
		sm.configure(); // Should throw as only one or two output formats should be set
	}

	@After
	public void tearDown() {
		LocalProperties.clearProperty("gda.device.scannable.ScannableMotor.copyMotorLimitsIntoScannableLimits");
	}

}
