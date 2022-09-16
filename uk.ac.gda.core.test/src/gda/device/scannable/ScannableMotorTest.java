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

import static gda.device.MotorStatus.BUSY;
import static gda.device.MotorStatus.FAULT;
import static gda.device.MotorStatus.LOWER_LIMIT;
import static gda.device.MotorStatus.READY;
import static gda.device.MotorStatus.SOFT_LIMIT_VIOLATION;
import static gda.device.MotorStatus.UPPER_LIMIT;
import static gda.device.scannable.ScannableMotor.COPY_MOTOR_LIMITS_INTO_SCANNABLE_LIMITS;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static tec.units.indriya.unit.MetricPrefix.MILLI;
import static tec.units.indriya.unit.Units.METRE;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import gda.TestHelpers;
import gda.configuration.properties.LocalProperties;
import gda.device.DeviceException;
import gda.device.Motor;
import gda.device.MotorException;
import gda.device.ScannableMotion;
import gda.factory.Factory;
import gda.factory.Finder;
import gda.jython.ITerminalPrinter;
import gda.jython.InterfaceProvider;
import tec.units.indriya.quantity.Quantities;

/**
 * Note basic limits testing and offset/scaling testing is performed in SMBT and SMUBT.<br>
 * Enough is done here to make sure things are tied together properly only.
 */
public class ScannableMotorTest {
	// Tolerance allowed for inaccuracies in floating point calculations
	private static final double FP_TOLERANCE = 0.0000001;

	private static final String[] ENERGY_UNITS = new String[] { "J", "keV", "eV", "GeV" };

	private ScannableMotor sm;
	private Motor motor;

	/**
	 * @throws Exception
	 */
	@BeforeEach
	public void setUp() throws Exception {
		motor = mock(Motor.class);
		when(motor.getStatus()).thenReturn(READY);
		when(motor.getMinPosition()).thenReturn(Double.NaN);
		when(motor.getMaxPosition()).thenReturn(Double.NaN);

		sm = new ScannableMotor();
		sm.setMotor(motor);
		sm.setName("sm");
		sm.setHardwareUnitString("mm"); // hardware will return mm's
		sm.setInitialUserUnits("micron"); // we talk in microns
		sm.setUpperGdaLimits(1000.0); // limits are +/- 1000 microns
		sm.setLowerGdaLimits(-1000.0);
		sm.configure();
		assertFalse(sm.isReturningDemandPosition());
	}

	@AfterEach
	public void tearDown() {
		LocalProperties.clearProperty("gda.device.scannable.ScannableMotor.copyMotorLimitsIntoScannableLimits");
		// Remove factories from Finder so they do not affect other tests
		Finder.removeAllFactories();
	}

	@Test
	public void testConfigureWithMotorName() throws Exception {
		final Factory factory = TestHelpers.createTestFactory();
		Finder.addFactory(factory);

		final Motor mockMotor = mock(Motor.class);
		when(mockMotor.getName()).thenReturn("motor");
		factory.addFindable(mockMotor);

		sm = new ScannableMotor();
		sm.setMotorName("motor");
		sm.configure();
		assertEquals(mockMotor, sm.getMotor());
		assertEquals(1, sm.getAdditionalPositionValidators().size());
		assertTrue(sm.getAdditionalPositionValidators().contains(sm.getMotorLimitsComponent()));
	}

	@Test
	public void testInputNames() throws Exception {
		assertEquals("sm", sm.getInputNames()[0]);
		assertEquals(1, sm.getInputNames().length);
		assertEquals(0, sm.getExtraNames().length);
	}

	@Test
	public void testSetUserUnitsWithWrongUnit() throws Exception {
		try {
			sm.setUserUnits("eV");
			org.junit.Assert.fail("DeviceException expected");
		} catch (DeviceException e) {
			assertEquals(
					"User unit eV is not acceptable. Try one of [m, nm, mm, \u00b5m, micron, um, \u03bcm, microns, Ang, Angstrom, \u00c5, \u212b]",
					e.getMessage());
		}
	}

	@Test
	public void testGetPosition() throws Exception {
		when(motor.getPosition()).thenReturn(1.); // mm
		assertEquals(1000., (double) sm.getPosition(), FP_TOLERANCE); // micron
	}

	@Test
	public void testGetPositionWithOffset() throws Exception {
		sm.setOffset(1.);
		when(motor.getPosition()).thenReturn(1.); // mm
		assertEquals(1001., (double) sm.getPosition(), FP_TOLERANCE); // micron
	}

	@Test
	public void testChangeMotorStatusTechniqueForFollowingTests() throws Exception {
		assertFalse(sm.isBusy());
		when(motor.getStatus()).thenReturn(BUSY);
		assertTrue(sm.isBusy());
		when(motor.getStatus()).thenReturn(READY);
		assertFalse(sm.isBusy());
	}

	@Test
	public void testGetDemandPositionNoneSet() throws Exception {
		sm.setReturnDemandPosition(true);
		sm.setDemandPositionTolerance(.1);
		when(motor.getPosition()).thenReturn(1.01);
		assertEquals(1.01, (double) sm.rawGetDemandPosition(), FP_TOLERANCE);
		assertEquals(1010., (double) sm.getDemandPosition(), FP_TOLERANCE);
		when(motor.getStatus()).thenReturn(BUSY);
		assertEquals(1.01, (double) sm.rawGetDemandPosition(), FP_TOLERANCE);
		assertEquals(1010., (double) sm.getDemandPosition(), FP_TOLERANCE);
	}

	@Test
	public void testGetDemandPositionAtTarget() throws Exception {
		sm.setReturnDemandPosition(true);
		sm.setDemandPositionTolerance(.1);
		sm.rawAsynchronousMoveTo(1.);
		when(motor.getPosition()).thenReturn(1.01);
		assertEquals(1., (double) sm.rawGetDemandPosition(), FP_TOLERANCE);
		assertEquals(1000., (double) sm.getDemandPosition(), FP_TOLERANCE);
		when(motor.getStatus()).thenReturn(BUSY);
		assertEquals(1., (double) sm.rawGetDemandPosition(), FP_TOLERANCE);
		assertEquals(1000., (double) sm.getDemandPosition(), FP_TOLERANCE);
	}

	@Test
	public void testGetDemandPositionNotAtTargetMoving() throws Exception {
		sm.setReturnDemandPosition(true);
		sm.setDemandPositionTolerance(.1);
		sm.rawAsynchronousMoveTo(1.);
		when(motor.getPosition()).thenReturn(1.11);
		when(motor.getStatus()).thenReturn(BUSY);
		assertEquals(1., (double) sm.rawGetDemandPosition(), FP_TOLERANCE);
		assertEquals(1000., (double) sm.getDemandPosition(), FP_TOLERANCE);
	}

	@Test
	public void testGetDemandPositionNotAtTargetStationary() throws Exception {
		final ITerminalPrinter mockedTerminalPrinter = mock(ITerminalPrinter.class);
		InterfaceProvider.setTerminalPrinterForTesting(mockedTerminalPrinter);
		sm.setReturnDemandPosition(true);
		sm.setDemandPositionTolerance(.1);
		sm.rawAsynchronousMoveTo(1.);
		when(motor.getPosition()).thenReturn(1.11);
		assertEquals(1110., (double) sm.getDemandPosition(), FP_TOLERANCE);
		assertEquals(1110., (double) sm.getDemandPosition(), FP_TOLERANCE);
		verify(mockedTerminalPrinter, times(1)).print(
				"WARNING: sm is returning a position based on its real motor position (1.11) rather than its last demanded position(1),\n"
						+ "as these differ by more than the configured demand position tolerance (0.1).");
	}

	@Test
	public void testGetPositionWithDemandRequestedNoneSet() throws Exception {
		sm.setReturnDemandPosition(true);
		sm.setDemandPositionTolerance(.1);
		when(motor.getPosition()).thenReturn(1.01);
		assertEquals(1010., (double) sm.getPosition(), FP_TOLERANCE);
		when(motor.getStatus()).thenReturn(BUSY);
		assertEquals(1010., (double) sm.getPosition(), FP_TOLERANCE);
	}

	@Test
	public void testGetPositionWithDemandRequestedAtTargetStationary() throws Exception {
		sm.setReturnDemandPosition(true);
		sm.setDemandPositionTolerance(.1);
		sm.asynchronousMoveTo(1000.);
		when(motor.getPosition()).thenReturn(1.01);
		assertEquals(1000., (double) sm.getPosition(), FP_TOLERANCE);
	}

	@Test
	public void testGetPositionWithDemandRequestedAtTargetMoving() throws Exception {
		sm.setReturnDemandPosition(true);
		sm.setDemandPositionTolerance(.1);
		sm.asynchronousMoveTo(1000.);
		when(motor.getStatus()).thenReturn(BUSY);
		when(motor.getPosition()).thenReturn(1.01);
		assertEquals(1010., (double) sm.getPosition(), FP_TOLERANCE);
	}

	@Test
	public void testGetPositionWithDemandRequestedNotAtTargetMoving() throws Exception {
		sm.setReturnDemandPosition(true);
		sm.setDemandPositionTolerance(.1);
		sm.asynchronousMoveTo(1000.);
		when(motor.getPosition()).thenReturn(1.11);
		when(motor.getStatus()).thenReturn(BUSY);
		assertEquals(1110., (double) sm.getPosition(), FP_TOLERANCE);
	}

	@Test
	public void testGetPositionWithDemandRequestedNotAtTargetStationary() throws Exception {
		final ITerminalPrinter mockedTerminalPrinter = mock(ITerminalPrinter.class);
		InterfaceProvider.setTerminalPrinterForTesting(mockedTerminalPrinter);
		sm.setReturnDemandPosition(true);
		sm.setDemandPositionTolerance(.1);
		sm.asynchronousMoveTo(1000.);
		when(motor.getPosition()).thenReturn(1.11);
		assertEquals(1110., (double) sm.getPosition(), FP_TOLERANCE);
		verify(mockedTerminalPrinter).print(
				"WARNING: sm is returning a position based on its real motor position (1.11) rather than its last demanded position(1),\n"
						+ "as these differ by more than the configured demand position tolerance (0.1).");
	}

	@Test
	public void testGetLimitsWithOfset() throws Exception {
		sm.setOffset(1.);
		assertEquals(1001., sm.getUpperGdaLimits()[0], FP_TOLERANCE); // micron
		assertEquals(1001., sm.getUpperGdaLimits()[0], FP_TOLERANCE); // micron
		assertEquals(-999., sm.getLowerGdaLimits()[0], FP_TOLERANCE); // micron
		assertEquals(-999., sm.getLowerGdaLimits()[0], FP_TOLERANCE); // micron
	}

	@Test
	public void testToFormattedString() throws Exception {
		when(motor.getPosition()).thenReturn(.01); // mm
		assertEquals("sm : 10.000µm (-1000.0:1000.0)", sm.toFormattedString());
	}

	@Test
	public void testToFormattedStringWhileReturningDemandPositionNoTarget() throws Exception {
		when(motor.getPosition()).thenReturn(.01); // mm
		sm.setReturnDemandPosition(true);
		sm.setDemandPositionTolerance(.001);// mm
		assertEquals("sm : 10.000µm (-1000.0:1000.0)", sm.toFormattedString());
		when(motor.getStatus()).thenReturn(BUSY);
		assertEquals("sm : 10.000µm (-1000.0:1000.0)", sm.toFormattedString());
	}

	@Test
	public void testToFormattedStringWhileReturningDemandPositionAtTargetStationary() throws Exception {
		sm.asynchronousMoveTo(10); // um
		when(motor.getPosition()).thenReturn(.0101); // mm
		sm.setReturnDemandPosition(true);
		sm.setDemandPositionTolerance(.001);// mm
		assertEquals("sm : 10.000µm (-1000.0:1000.0) demand", sm.toFormattedString());
	}

	@Test
	public void testToFormattedStringWhileReturningDemandPositionAtTargetMoving() throws Exception {
		sm.asynchronousMoveTo(10); // um
		when(motor.getPosition()).thenReturn(.0101); // mm
		sm.setReturnDemandPosition(true);
		sm.setDemandPositionTolerance(.001);// mm
		when(motor.getStatus()).thenReturn(BUSY);
		assertEquals("sm : 10.100µm (-1000.0:1000.0)", sm.toFormattedString());
	}

	@Test
	public void testToFormattedStringWhileReturningDemandPositionNotAtTargetAndStopped() throws Exception {
		final ITerminalPrinter mockedTerminalPrinter = mock(ITerminalPrinter.class);
		InterfaceProvider.setTerminalPrinterForTesting(mockedTerminalPrinter);
		sm.asynchronousMoveTo(200); // um
		when(motor.getPosition()).thenReturn(.01); // mm
		sm.setReturnDemandPosition(true);
		sm.setDemandPositionTolerance(.001);// mm
		assertEquals("sm : 10.000µm (-1000.0:1000.0) *demand=200.00*", sm.toFormattedString());
	}

	@Test
	public void testToFormattedStringWhileReturningDemandPositionNotAtTargetAndMoving() throws Exception {
		sm.asynchronousMoveTo(20); // um
		when(motor.getPosition()).thenReturn(.01); // mm
		sm.setReturnDemandPosition(true);
		sm.setDemandPositionTolerance(.001);// mm
		when(motor.getStatus()).thenReturn(BUSY);
		assertEquals("sm : 10.000µm (-1000.0:1000.0)", sm.toFormattedString());
	}

	@Test
	public void testToStringWithNoMotorSet() throws Exception {
		sm = new ScannableMotor();
		sm.setName("sm");
		assertEquals("sm : UNAVAILABLE", sm.toFormattedString());
	}

	@Test
	public void testToStringWithNoLimitsComponent() throws Exception {
		sm = new ScannableMotor();
		sm.setName("sm");
		sm.setMotor(motor);
		sm.setMotorLimitsComponent(null);
		sm.configure();
		assertEquals("sm : 0.0000", sm.toFormattedString());
	}

	@Test
	public void testToStringWithMotorLimits() throws Exception {
		when(motor.getPosition()).thenReturn(.01); // mm
		when(motor.getMinPosition()).thenReturn(-.02);// mm
		when(motor.getMaxPosition()).thenReturn(.02);// mm
		assertEquals("sm : 10.000µm (-1000.0:1000.0) mot(-20.000:20.000)", sm.toFormattedString());
	}

	@Test
	public void testToStringWithOffset() throws Exception {
		when(motor.getPosition()).thenReturn(.01); // mm
		sm.setOffset(1.);
		assertEquals(1001., sm.getUpperGdaLimits()[0], FP_TOLERANCE); // micron
		assertEquals(-999., sm.getLowerGdaLimits()[0], FP_TOLERANCE); // micron
		assertEquals("sm : 11.000µm(+1.0000) (-999.00:1001.0)", sm.toFormattedString());
	}

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
		when(motor.getStatus()).thenReturn(BUSY);
		assertTrue(sm.isBusy());
	}

	@Test
	public void testAsynchronousMoveTo() throws Exception {
		final ArgumentCaptor<Double> captor = ArgumentCaptor.forClass(Double.class);
		sm.asynchronousMoveTo(50); // micron
		verify(motor).moveTo(captor.capture());
		assertEquals(0.05, captor.getValue(), FP_TOLERANCE);
	}

	@Test
	public void testAsynchronousMoveTo2() throws Exception {
		final ArgumentCaptor<Double> captor = ArgumentCaptor.forClass(Double.class);
		sm.moveTo("0.9 mm");
		verify(motor).moveTo(captor.capture());
		assertEquals(0.9,  captor.getValue(), FP_TOLERANCE);
	}

	@Test
	public void testAsynchronousMoveTo3() throws Exception {
		final ArgumentCaptor<Double> captor = ArgumentCaptor.forClass(Double.class);
		sm.moveTo("5 µm");
		verify(motor).moveTo(captor.capture());
		assertEquals(0.005, captor.getValue(), FP_TOLERANCE);
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
		assertTrue(sm.isAt(Quantities.getQuantity(0.001, MILLI(METRE))));
		assertFalse(sm.isAt(Quantities.getQuantity(0.0012, MILLI(METRE))));
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
		assertEquals("µm", sm.getHardwareUnitString());
	}

	/**
	 * Test with regular (dummy for example) motor.
	 *
	 * @throws Exception
	 */
	@Test
	public void testConfigureWithMotor_setHwOnly() throws Exception {
		final Motor mockedMotor = mock(Motor.class);
		final ScannableMotor scannable = new ScannableMotor();
		scannable.setMotor(mockedMotor);
		scannable.setName("scn_name");
		scannable.setHardwareUnitString("eV");
		scannable.configure();
		assertEquals("eV", scannable.getHardwareUnitString());
		assertEquals("eV", scannable.getUserUnits());
		assertArrayEquals(ENERGY_UNITS, scannable.getAcceptableUnits());
	}

	@Test
	public void testConfigureWithMotor_setUserOnly() throws Exception {
		final Motor mockedMotor = mock(Motor.class);
		final ScannableMotor scannable = new ScannableMotor();
		scannable.setMotor(mockedMotor);
		scannable.setName("scn_name");
		scannable.setUserUnits("eV");
		scannable.configure();
		assertEquals("eV", scannable.getHardwareUnitString());
		assertEquals("eV", scannable.getUserUnits());
		assertArrayEquals(ENERGY_UNITS, scannable.getAcceptableUnits());
	}

	@Test
	public void testConfigureWithMotor_setUserAndHardware() throws Exception {
		final Motor mockedMotor = mock(Motor.class);
		final ScannableMotor scannable = new ScannableMotor();
		scannable.setMotor(mockedMotor);
		scannable.setName("scn_name");
		scannable.setUserUnits("eV");
		scannable.setHardwareUnitString("keV");
		scannable.configure();
		assertEquals("keV", scannable.getHardwareUnitString());
		assertEquals("eV", scannable.getUserUnits());
		assertArrayEquals(ENERGY_UNITS, scannable.getAcceptableUnits());
	}

	@Test
	public void testConfigureWithMotorThatProvidesUnits() throws Exception {
		final Motor mockedMotor = mock(Motor.class);
		when(mockedMotor.getUnitString()).thenReturn("eV");
		final ScannableMotor scannable = new ScannableMotor();
		scannable.setMotor(mockedMotor);
		scannable.setUserUnits("keV");
		scannable.configure();
		assertEquals("eV", scannable.getHardwareUnitString());
		assertEquals("keV", scannable.getUserUnits());
		assertArrayEquals(ENERGY_UNITS, scannable.getAcceptableUnits());
	}

	@Test
	public void testAsynchronousMoveToExceptionIfMoving() throws Exception {
		when(motor.getStatus()).thenReturn(BUSY);

		try {
			sm.asynchronousMoveTo(50);
			fail("DeviceException expected.");
		} catch (DeviceException e) {
			assertEquals("Problem triggering sm move to 50: The scannable motor sm (null) was already busy so could not be moved", e.getMessage());
		}
	}

	@Test
	public void testCheckLimitCodeIntegration() throws Exception {
		when(motor.getMinPosition()).thenReturn(-1.); // mm
		when(motor.getMaxPosition()).thenReturn(.5); // mm
		assertNull(sm.checkPositionValid(499)); // micron
		assertEquals("Motor limit violation on motor null: 0.501000 > 0.500000 (internal/hardware/dial values).", sm.checkPositionValid(501)); // micron
	}

	@Test
	public void testGetLowerInnerLimit() throws Exception {
		final Double nullDouble = null;
		sm.setLowerGdaLimits(nullDouble);
		assertNull(sm.getLowerInnerLimit());
		sm.setLowerGdaLimits(-1000.); // microns
		assertEquals(-1000., sm.getLowerInnerLimit(), FP_TOLERANCE); // microns
		when(motor.getMinPosition()).thenReturn(-1.001); // mm
		assertEquals(-1000., sm.getLowerInnerLimit(), FP_TOLERANCE); // microns
		when(motor.getMinPosition()).thenReturn(-.999); // mm
		assertEquals(-999., sm.getLowerInnerLimit(), FP_TOLERANCE); // microns
	}

	@Test
	public void testGetUpperInnerLimit() throws Exception {
		final Double nullDouble = null;
		sm.setUpperGdaLimits(nullDouble);
		assertNull(sm.getUpperInnerLimit());
		sm.setUpperGdaLimits(1000.); // microns
		assertEquals(1000., sm.getUpperInnerLimit(), FP_TOLERANCE); // microns
		when(motor.getMinPosition()).thenReturn(-1.001); // mm
		assertEquals(1000., sm.getUpperInnerLimit(), FP_TOLERANCE); // microns
		when(motor.getMaxPosition()).thenReturn(.999); // mm
		assertEquals(999., sm.getUpperInnerLimit(), FP_TOLERANCE); // microns
	}

	@Test
	public void testGetAttributeLimitsMin() throws Exception {
		final Double nullDouble = null;
		sm.setLowerGdaLimits(nullDouble);// microns
		sm.setUpperGdaLimits(nullDouble);// microns
		when(motor.getMinPosition()).thenReturn(Double.NaN); // mm
		when(motor.getMaxPosition()).thenReturn(Double.NaN); // mm
		assertNull(sm.getAttribute(ScannableMotion.FIRSTINPUTLIMITS));

		final Double val1mm = new Double(-1.001);
		when(motor.getMinPosition()).thenReturn(val1mm); // mm
		final Double val1microns = new Double(-1001.);

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
		final Double nullDouble = null;
		sm.setLowerGdaLimits(nullDouble);// microns
		sm.setUpperGdaLimits(nullDouble);// microns
		when(motor.getMinPosition()).thenReturn(Double.NaN); // mm
		when(motor.getMaxPosition()).thenReturn(Double.NaN); // mm

		final Double val1mm = new Double(1.001);
		when(motor.getMaxPosition()).thenReturn(val1mm); // mm
		final Double val1microns = new Double(1001.);

		ScannableMotionUnitsBaseTest.assertAlmostEqual(new Double[] { null, val1microns }, (Double[]) sm.getAttribute(ScannableMotion.FIRSTINPUTLIMITS));

		sm.setUpperGdaLimits(1002.);// microns
		ScannableMotionUnitsBaseTest.assertAlmostEqual(new Double[] { null, val1microns }, (Double[]) sm.getAttribute(ScannableMotion.FIRSTINPUTLIMITS));

		final Double val2 = new Double(999.);
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
		assertEquals(-1000., sm.getLowerMotorLimit(), FP_TOLERANCE); // microns
		assertEquals(800., sm.getUpperMotorLimit(), FP_TOLERANCE); // microns
	}

	@Test(expected = DeviceException.class)
	public void testGetLowerMotorLimitWithNoMotorLimitsComponent() throws Exception {
		sm.setMotorLimitsComponent(null);
		sm.getLowerMotorLimit();
	}

	@Test(expected = DeviceException.class)
	public void testGetUpperMotorLimitWithNoMotorLimitsComponent() throws Exception {
		sm.setMotorLimitsComponent(null);
		sm.getUpperMotorLimit();
	}

	@Test
	public void testGetMotorLimitWithNegativeScale() throws Exception {
		when(motor.getMinPosition()).thenReturn(-1.); // mm
		when(motor.getMaxPosition()).thenReturn(.8); // mm
		sm.setScalingFactor(-1.);
		assertEquals(-800., sm.getLowerMotorLimit(), FP_TOLERANCE); // microns
		assertEquals(1000., sm.getUpperMotorLimit(), FP_TOLERANCE); // microns
	}

	@Test
	public void testToStringWithNegativeScalingFactor() throws Exception {
		when(motor.getPosition()).thenReturn(.01); // mm
		when(motor.getMinPosition()).thenReturn(-.02);// mm
		when(motor.getMaxPosition()).thenReturn(.01);// mm
		sm.setScalingFactor(-1.);
		sm.setLowerGdaLimits(-1000.);
		sm.setUpperGdaLimits(800.);
		assertEquals("sm : -10.000µm (-1000.0:800.00) mot(-10.000:20.000)", sm.toFormattedString());
	}

	@Test
	public void testCheckPositionWithNegativeScalingFactor_MotorLimits() throws Exception {
		final Double nullDouble = null;
		sm.setLowerGdaLimits(nullDouble);
		sm.setUpperGdaLimits(nullDouble);
		when(motor.getMinPosition()).thenReturn(-.02);// mm
		when(motor.getMaxPosition()).thenReturn(.01);// mm
		when(motor.getName()).thenReturn("motname");
		sm.setScalingFactor(-1.);

		assertNull(sm.checkPositionValid(0.));// micron
		assertNull(sm.checkPositionValid(19.));// micron
		assertNull(sm.checkPositionValid(-9.));// micron
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

		assertNull(sm.checkPositionValid(0.));// micron
		assertNull(sm.checkPositionValid(-999));// micron
		assertNull(sm.checkPositionValid(799));// micron
		assertEquals("Scannable limit violation on sm.sm: 1.001 > 1.0 (internal/hardware/dial values).", sm.checkPositionValid(-1001));// micron
		assertEquals("Scannable limit violation on sm.sm: -0.801 < -0.8 (internal/hardware/dial values).", sm.checkPositionValid(801));// micron
	}

	@Test
	public void testCopyMotorLimitsIntoScannableLimitsAtConfiguration_Off() throws Exception {
		LocalProperties.set(COPY_MOTOR_LIMITS_INTO_SCANNABLE_LIMITS, "false");
		when(motor.getMinPosition()).thenReturn(-10.);
		when(motor.getMaxPosition()).thenReturn(10.);
		sm = new ScannableMotor();
		sm.setMotor(motor);
		sm.configure();
		assertNull(sm.getLowerGdaLimits());
		assertNull(sm.getUpperGdaLimits());
	}

	@Test
	public void testCopyMotorLimitsIntoScannableLimitsAtConfiguration_Limits_ManuallySet() throws Exception {
		LocalProperties.set(COPY_MOTOR_LIMITS_INTO_SCANNABLE_LIMITS, "true");
		when(motor.getMinPosition()).thenReturn(-10.);
		when(motor.getMaxPosition()).thenReturn(10.);
		sm = new ScannableMotor();
		sm.setMotor(motor);
		sm.setLowerGdaLimits(-91.);
		sm.setUpperGdaLimits(89.);
		sm.configure();
		assertEquals(1, sm.getLowerGdaLimits().length);
		assertEquals(-91., sm.getLowerGdaLimits()[0], FP_TOLERANCE);
		assertEquals(1, sm.getUpperGdaLimits().length);
		assertEquals(89., sm.getUpperGdaLimits()[0], FP_TOLERANCE);
	}

	@Test
	public void testCopyMotorLimitsIntoScannable_ScaleNegative() throws Exception {
		LocalProperties.set(COPY_MOTOR_LIMITS_INTO_SCANNABLE_LIMITS, "true");
		when(motor.getMinPosition()).thenReturn(-10.);
		when(motor.getMaxPosition()).thenReturn(20.);
		sm = new ScannableMotor();
		sm.setMotor(motor);
		sm.setScalingFactor(-1.0);
		sm.configure();
		assertEquals(-20., sm.getLowerGdaLimits()[0], FP_TOLERANCE);
		assertEquals(10., sm.getUpperGdaLimits()[0], FP_TOLERANCE);
	}

	@Test
	public void testCopyMotorLimitsIntoScannableLimitsAtConfiguration_OnUnitsAndOffset() throws Exception {
		LocalProperties.set("gda.device.scannable.ScannableMotor.copyMotorLimitsIntoScannableLimits", "true");
		when(motor.getMinPosition()).thenReturn(-10.);// mm
		when(motor.getMaxPosition()).thenReturn(6.);// mm
		sm = new ScannableMotor();
		sm.setMotor(motor);
		sm.setHardwareUnitString("mm");
		sm.setInitialUserUnits("micron");
		sm.configure();
		sm.setOffset(1000.);// micron
		assertEquals(-9000., sm.getLowerGdaLimits()[0], .0001);// micron
		assertEquals(7000., sm.getUpperGdaLimits()[0], FP_TOLERANCE);// micron
	}

	@Test
	public void testCopyMotorLimitsIntoScannableLimitsAtConfiguration_OnUnitsAndScale() throws Exception {
		LocalProperties.set("gda.device.scannable.ScannableMotor.copyMotorLimitsIntoScannableLimits", "true");
		when(motor.getMinPosition()).thenReturn(-10.);// mm
		when(motor.getMaxPosition()).thenReturn(6.);// mm
		sm = new ScannableMotor();
		sm.setMotor(motor);
		sm.setHardwareUnitString("mm");
		sm.setInitialUserUnits("micron");
		sm.configure();
		sm.setScalingFactor(-1.);// micron
		assertEquals(-6000., sm.getLowerGdaLimits()[0], .0001);// micron
		assertEquals(10000., sm.getUpperGdaLimits()[0], FP_TOLERANCE);// micron
	}

	@Test
	public void testCopyMotorLimitsIntoScannableLimitsAtConfiguration_NoMotorLimits() throws Exception {
		LocalProperties.set("gda.device.scannable.ScannableMotor.copyMotorLimitsIntoScannableLimits", "true");
		when(motor.getMinPosition()).thenReturn(Double.NaN);
		when(motor.getMaxPosition()).thenReturn(Double.NaN);
		sm = new ScannableMotor();
		sm.setMotor(motor);
		sm.configure();
		assertEquals(-Double.MAX_VALUE, sm.getLowerGdaLimits()[0], FP_TOLERANCE);
		assertEquals(Double.MAX_VALUE, sm.getUpperGdaLimits()[0], FP_TOLERANCE);
	}

	@Test(expected = DeviceException.class)
	public void testIsBusyCannotGetStatus() throws Exception {
		when(motor.getStatus()).thenThrow(new MotorException(FAULT, "Error getting motor status"));
		sm.isBusy();
	}

	@Test
	public void testIsBusyMotorAtFault() throws Exception {
		when(motor.getStatus()).thenReturn(FAULT);
		assertFalse(sm.isBusy());
	}

	@Test
	public void testIsBusyMotorAtUpperLimit() throws Exception {
		when(motor.getStatus()).thenReturn(UPPER_LIMIT);
		assertFalse(sm.isBusy());
	}

	@Test
	public void testIsBusyMotorAtLowerLimit() throws Exception {
		when(motor.getStatus()).thenReturn(LOWER_LIMIT);
		assertFalse(sm.isBusy());
	}

	@Test
	public void testIsBusyMotorAtSoftLimitViolation() throws Exception {
		when(motor.getStatus()).thenReturn(SOFT_LIMIT_VIOLATION);
		assertFalse(sm.isBusy());
	}

	@Test(expected = MotorException.class, timeout = 500)
	public void testWaitWhileBusyCannotGetMotorStatus() throws Exception {
		when(motor.getStatus()).thenThrow(new MotorException(FAULT, "Error getting motor status"));
		sm.waitWhileBusy();
	}

	@Test(expected = DeviceException.class, timeout = 500)
	public void testWaitWhileBusyMotorAtFault() throws Exception {
		when(motor.getStatus()).thenReturn(FAULT);
		sm.waitWhileBusy();
	}

	@Test(expected = DeviceException.class, timeout = 500)
	public void testWaitWhileBusyMotorAtUpperLimit() throws Exception {
		when(motor.getStatus()).thenReturn(UPPER_LIMIT);
		sm.waitWhileBusy();
	}

	@Test(expected = DeviceException.class, timeout = 500)
	public void testWaitWhileBusyMotorAtLowerLimit() throws Exception {
		when(motor.getStatus()).thenReturn(LOWER_LIMIT);
		sm.waitWhileBusy();
	}

	@Test(expected = DeviceException.class, timeout = 500)
	public void testWaitWhileBusyMotorAtSoftLimitViolation() throws Exception {
		when(motor.getStatus()).thenReturn(SOFT_LIMIT_VIOLATION);
		sm.waitWhileBusy();
	}
}
