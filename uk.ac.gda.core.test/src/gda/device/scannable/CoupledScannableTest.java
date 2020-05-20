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

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.any;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static si.uom.NonSI.ELECTRON_VOLT;
import static tec.units.indriya.AbstractUnit.ONE;
import static tec.units.indriya.unit.MetricPrefix.CENTI;
import static tec.units.indriya.unit.MetricPrefix.NANO;
import static tec.units.indriya.unit.Units.METRE;

import java.util.function.Function;

import javax.measure.Quantity;
import javax.measure.quantity.Length;

import org.junit.Before;
import org.junit.Test;

import gda.device.DeviceException;
import gda.factory.FactoryException;
import gda.function.IdentityFunction;
import gda.observable.IObserver;
import tec.units.indriya.quantity.Quantities;

public class CoupledScannableTest {
	// Allow for inaccuracy in floating point values
	private static final double FP_TOLERANCE = 0.00000001;

	private DummyUnitsScannable<Length> dummyScannable1;
	private DummyUnitsScannable<Length> dummyScannable2;
	private DummyScannable dummyScannable3;

	private MockFunction mockFunction1;
	private MockFunction mockFunction2;
	private MockFunction mockFunction3;

	private final IdentityFunction identityFunction = new IdentityFunction();

	@Before
	public void setUp() throws Exception {
		dummyScannable1 = new DummyUnitsScannable<>("s1", 0, "mm", "mm");
		dummyScannable2 = new DummyUnitsScannable<>("s2", 0, "nm", "nm");
		dummyScannable3 = new DummyScannable("s3", 15.3);

		mockFunction1 = new MockFunction(Quantities.getQuantity(12.3, CENTI(METRE)));
		mockFunction2 = new MockFunction(Quantities.getQuantity(78.9, NANO(METRE)));
		mockFunction3 = new MockFunction(Quantities.getQuantity(93.4, ONE));
	}

	/*
	 * test that position returned takes into account changes in the user units of the first scannable
	 */
	@Test
	public void testUseFirstScannableUnits() throws DeviceException, FactoryException {
		final CoupledScannable scannable = new CoupledScannable();
		scannable.setScannables(asList(dummyScannable1, dummyScannable2));
		scannable.setFunctions(asList(identityFunction, identityFunction));
		scannable.setName("test");
		scannable.configure();

		dummyScannable1.moveTo(1.0);
		final double posWithUserUnits_mm = (double) scannable.getPosition();
		assertEquals("Should have same value as position of scannable1 as user unit set to that of scannable1", 1.0, posWithUserUnits_mm, FP_TOLERANCE);

		dummyScannable1.setUserUnits("m");

		final double posWithUserUnits_m = (double) scannable.getPosition();

		assertEquals(posWithUserUnits_mm, posWithUserUnits_m, FP_TOLERANCE);
	}

	@Test
	public void testUseFirstScannableUnitsButWithInitialUnitsSet() throws DeviceException, FactoryException {
		final CoupledScannable scannable = new CoupledScannable();
		scannable.setScannables(asList(dummyScannable1, dummyScannable2));
		scannable.setFunctions(asList(identityFunction, identityFunction));
		scannable.setName("test");
		scannable.setInitialUserUnits("µm");
		scannable.configure();

		dummyScannable1.moveTo(1.0);
		final double posWithUserUnits_mm = (double) scannable.getPosition();
		assertEquals("Should have value of 1000 user unit set to micro whilst scannable1 is mm", 1000.0, posWithUserUnits_mm, FP_TOLERANCE);

		dummyScannable1.setUserUnits("m");

		final double posWithUserUnits_m = (double) scannable.getPosition();

		assertEquals(posWithUserUnits_mm, posWithUserUnits_m, FP_TOLERANCE);
	}

	/*
	 * When the movement of the component scannables are coupled to the position of the overall CoupledScannable by a
	 * function, the function must know the units of the CoupledScannable in order to calculate the position of the
	 * component.
	 */
	@Test
	public void testPassCorrectUnits() throws DeviceException, FactoryException {
		final CoupledScannable coupled = new CoupledScannable();
		coupled.setName("testCoupledScannable");
		coupled.setUserUnits("eV");
		coupled.setScannables(asList(dummyScannable1));
		coupled.setFunctions(asList(mockFunction1));
		coupled.configure();

		coupled.moveTo(798.34);

		// Verify that the input units of the coupled scannable (i.e. eV) are passed to the evaluation function for the
		// component scannable
		assertEquals(798.34, mockFunction1.parameterPassed.getValue().doubleValue(), FP_TOLERANCE);
		assertEquals(ELECTRON_VOLT, mockFunction1.parameterPassed.getUnit());

		// Verify that the units for the component move have been handled correctly
		assertEquals("mm", dummyScannable1.getHardwareUnitString());
		assertEquals(123.0, (double) dummyScannable1.getPosition(), FP_TOLERANCE);
	}

	@Test
	public void testSetScannablesDirectly() throws Exception {
		final CoupledScannable coupled = new CoupledScannable();
		coupled.setName("testCoupledScannable");
		coupled.setUserUnits("mm");
		coupled.setScannables(asList(dummyScannable1, dummyScannable2));
		coupled.setFunctions(asList(mockFunction1, mockFunction2));
		coupled.configure();

		coupled.moveTo(15.7);

		assertEquals("mm", dummyScannable1.getHardwareUnitString());
		assertEquals(123.0, (double) dummyScannable1.getPosition(), FP_TOLERANCE);

		assertEquals("nm", dummyScannable2.getHardwareUnitString());
		assertEquals(78.9, (double) dummyScannable2.getPosition(), FP_TOLERANCE);
	}

	@Test(expected = FactoryException.class)
	public void testUnequalNumbersOfScannablesAndFunctions() throws Exception {
		final CoupledScannable coupled = new CoupledScannable();
		coupled.setName("testCoupledScannable");
		coupled.setUserUnits("mm");
		coupled.setScannables(asList(dummyScannable1, dummyScannable2));
		coupled.setFunctions(asList(mockFunction1));
		coupled.configure();
	}

	@Test(expected = FactoryException.class)
	public void testNoFunctionsThrowsException() throws Exception {
		final CoupledScannable coupled = new CoupledScannable();
		coupled.setName("testCoupledScannable");
		coupled.setUserUnits("mm");
		coupled.setScannables(asList(dummyScannable1, dummyScannable2));
		coupled.configure();
	}

	@Test
	public void testIdentityFunctionMovesAllScannableToSamePositionRespectingUnits() throws Exception {
		final CoupledScannable coupled = new CoupledScannable();
		coupled.setName("testCoupledScannable");
		coupled.setUserUnits("mm");
		coupled.setScannables(asList(dummyScannable1, dummyScannable2));
		coupled.setFunctions(asList(identityFunction, identityFunction));
		coupled.configure();

		coupled.moveTo(15.7);

		// With identity functions, all scannables are moved to the same absolute position
		assertEquals("mm", dummyScannable1.getHardwareUnitString());
		assertEquals(15.7, (double) dummyScannable1.getPosition(), FP_TOLERANCE);

		assertEquals("nm", dummyScannable2.getHardwareUnitString());
		assertEquals(1.57e7, (double) dummyScannable2.getPosition(), FP_TOLERANCE);
	}

	@Test
	public void testScannableWithoutUnits() throws Exception {
		final CoupledScannable coupled = new CoupledScannable();
		coupled.setName("testCoupledScannable");
		coupled.setUserUnits("mm");
		coupled.setScannables(asList(dummyScannable1, dummyScannable3));
		coupled.setFunctions(asList(identityFunction, mockFunction3));
		coupled.configure();

		coupled.moveTo(15.7);

		assertEquals("mm", dummyScannable1.getHardwareUnitString());
		assertEquals(15.7, (double) dummyScannable1.getPosition(), FP_TOLERANCE);

		assertEquals(93.4, (double) dummyScannable3.getPosition(), FP_TOLERANCE);
	}

	@Test(expected = DeviceException.class)
	public void testThrowsExceptionIfMoveFails() throws Exception {
		dummyScannable1.setUpperGdaLimits(120.0); // so move to 123.0 will fail

		final CoupledScannable coupled = new CoupledScannable();
		coupled.setName("testCoupledScannable");
		coupled.setUserUnits("mm");
		coupled.setScannables(asList(dummyScannable1, dummyScannable2));
		coupled.setFunctions(asList(mockFunction1, mockFunction2));
		coupled.configure();

		coupled.moveTo(15.7);
	}

	@Test
	public void testNotifiesObservers() throws Exception {
		final IObserver observer = mock(IObserver.class);

		final CoupledScannable coupled = new CoupledScannable();
		coupled.setName("testCoupledScannable");
		coupled.setUserUnits("mm");
		coupled.setScannables(asList(dummyScannable1, dummyScannable2));
		coupled.setFunctions(asList(identityFunction, identityFunction));
		coupled.addIObserver(observer);
		coupled.configure();

		coupled.moveTo(15.7);

		// Notifies observers only when all scannables are idle
		dummyScannable1.notifyIObservers(dummyScannable1, ScannableStatus.IDLE);
		verify(observer, never()).update(any(Object.class), any(Object.class));

		dummyScannable2.notifyIObservers(dummyScannable2, ScannableStatus.IDLE);
		verify(observer).update(coupled, ScannableStatus.IDLE);
	}

	/**
	 * Mockito mocks do not work well with generics (at least with the Eclipse Mars compiler currently we are using with
	 * Buckminster), so create a class to do something similar.
	 */
	private class MockFunction implements Function<Quantity<? extends Quantity<?>>, Quantity<? extends Quantity<?>>> {
		private final Quantity<? extends Quantity<?>> valueToReturn;
		private Quantity<? extends Quantity<?>> parameterPassed;

		public MockFunction(Quantity<? extends Quantity<?>> valueToReturn) {
			this.valueToReturn = valueToReturn;
		}

		@Override
		public Quantity<? extends Quantity<?>> apply(Quantity<? extends Quantity<?>> t) {
			parameterPassed = t;
			return valueToReturn;
		}
	}
}
