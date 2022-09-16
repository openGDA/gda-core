/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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


import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static tec.units.indriya.unit.MetricPrefix.MICRO;
import static tec.units.indriya.unit.Units.METRE;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import gda.device.DeviceException;
import gda.device.ScannableMotion;
import gda.device.ScannableMotionUnits;
import gda.device.scannable.component.ScannableOffsetAndScalingComponent;
import tec.units.indriya.quantity.Quantities;

public class ScannableMotionUnitsBaseTest extends ScannableMotionBaseTest {
	// Tolerance for imprecision of floating-point calculations
	private static final double FP_TOLERANCE = 0.00001;

	private static class TestableScannableMotionUnitsBase extends ScannableMotionUnitsBase implements Testable {

		public ScannableBase delegate = mock(ScannableBase.class);

		@Override
		public void rawAsynchronousMoveTo(Object position) throws DeviceException {
			delegate.rawAsynchronousMoveTo(position);
		}

		@Override
		public Object rawGetPosition() throws DeviceException {
			return delegate.rawGetPosition();
		}

		@Override
		public boolean isBusy() throws DeviceException {
			return delegate.isBusy();
		}
	}

	private TestableScannableMotionUnitsBase scannable;

	@Override
	public ScannableBase getDelegate() {
		return scannable.delegate;
	}

	@Override
	public ScannableBase getSB() {
		return scannable;
	}

	@Override
	public ScannableMotion getSM() {
		return scannable;
	}

	public ScannableMotionUnits getSMU() {
		return scannable;
	}

	@Override
	public ScannableMotionUnitsBase getSMB() {
		return scannable;
	}

	public ScannableMotionUnitsBase getSMUB() {
		return scannable;
	}

	@Override
	public void createScannableToTest() {
		scannable = new TestableScannableMotionUnitsBase();
	}

	/**
	 * Behaviour here is very odd!
	 */
	@Override
	public void testDefaultValues() throws DeviceException {
		assertArrayEquals(new String[] { "one", "" }, getSMU().getAcceptableUnits());
		assertEquals("", getSMU().getHardwareUnitString());
		assertEquals("", getSMU().getUserUnits());
		super.testDefaultValues();
	}

//	@Test (expected=DeviceException.class)
//	public void testSetUserUnitsWhenNoHardwareUnitsExplitelySet() throws DeviceException {
//		getSMU().setUserUnits("mm");
//	}

	@Test
	public void testSetUserUnitsWithWrongUnit() throws DeviceException {
		getSMU().setHardwareUnitString("eV");
		try {
			getSMU().setUserUnits("mm");
			org.junit.Assert.fail("DeviceException expected");
		} catch (DeviceException e) {
			assertEquals("User unit mm is not acceptable. Try one of [J, keV, eV, GeV]", e.getMessage());
		}
	}

	@Test
	public void testSetUserUnitsWithWrongUnit2() throws DeviceException {
		getSMU().setHardwareUnitString("");
		try {
			getSMU().setUserUnits("mm");
			org.junit.Assert.fail("DeviceException expected");
		} catch (DeviceException e) {
			assertEquals("User unit mm is not acceptable. Try one of [one, ]", e.getMessage());
		}
	}

	@Test
	public void testSetGetUserUnits () throws DeviceException {
		getSMU().setHardwareUnitString("m");
		getSMU().setUserUnits("mm");
		assertEquals("mm", getSMU().getUserUnits());
	}

	@Test
	public void testSetHardwareUnit() throws DeviceException {
		getSMU().setHardwareUnitString("keV");
		assertArrayEquals(new String[] { "J", "keV", "eV", "GeV" }, getSMU().getAcceptableUnits());
	}

	@Test
	public void testSetInitialUserUnits() throws DeviceException{
		getSMUB().setInitialUserUnits("");
		testDefaultValues(); // Should be unchanged from default which also ""
		getSMU().setHardwareUnitString("eV");
		getSMUB().setInitialUserUnits("eV");
		assertEquals("eV", getSMU().getUserUnits());
		assertEquals("eV", getSMUB().getInitialUserUnits());
	}

	public static  void assertAlmostEqual(Double [] expected, Double [] actual){
		for( int i=0; i< expected.length;i++){
			Double expectVal = expected[i];
			Double actualVal = actual[i];
			if( expectVal == null)
				Assert.assertNull(actualVal);
			else
				Assert.assertEquals(expectVal.doubleValue(), actualVal.doubleValue(), 0.000001);
		}
	}
	@Override
	@Test
	public void testGetAttribute() throws Exception {
		super.testGetAttribute();
		getSM().setLowerGdaLimits(new Double[] { 1., 2. });
		getSM().setUpperGdaLimits(new Double[] { 3., 4. });
		getSMU().setHardwareUnitString("eV");
		getSMU().setUserUnits("keV");
		assertEquals("eV", getSMUB().getAttribute("hardwareunits"));
		assertEquals("keV", getSMUB().getAttribute("userunits"));
		assertAlmostEqual(new Double[] { .001, .003 }, (Double[]) getSMB().getAttribute(ScannableMotion.FIRSTINPUTLIMITS));
		assertAlmostEqual(new Double[] { .001, .003 }, getSMB().getInputLimits(0));
		assertAlmostEqual(new Double[] { .002, .004 }, getSMB().getInputLimits(1));
	}

	@Test
	public void testSetGetHardwareUnits() throws DeviceException {
		getSMU().setHardwareUnitString("mm");
		assertEquals("mm", getSMU().getHardwareUnitString());
		getSMU().setHardwareUnitString("m");
		assertEquals("m", getSMU().getHardwareUnitString());
	}

	@Test
	public void testSetHardwareUnitIfUserUnitNotExplicitelySet() throws DeviceException {
		getSMU().setHardwareUnitString("mm");
		assertEquals("mm", getSMU().getHardwareUnitString());
		assertEquals("mm", getSMU().getUserUnits());
	}

	@Test
	public void testSetHardwareUnitIfUserUnitExplicitelySet() throws DeviceException {
		getSMU().setUserUnits("m");
		getSMU().setHardwareUnitString("mm");
		assertEquals("mm", getSMU().getHardwareUnitString());
		assertEquals("m", getSMU().getUserUnits());
	}

	@Test
	public void testSetUserUnitIfHardwareUnitNotExplicitelySet() throws DeviceException {
		getSMU().setUserUnits("m");
		assertEquals("m", getSMU().getHardwareUnitString());
		assertEquals("m", getSMU().getUserUnits());
	}

	@Test
	public void testSetUserUnitIfHardwareUnitExplicitelySet() throws DeviceException {
		getSMU().setHardwareUnitString("mm");
		getSMU().setUserUnits("m");
		assertEquals("mm", getSMU().getHardwareUnitString());
		assertEquals("m", getSMU().getUserUnits());
	}

	@Override
	@Test(expected = IllegalArgumentException.class)
	public void testIsAtWithStrings() throws DeviceException {
		super.testIsAtWithStrings();
	}

	@Test
	public void testGetOffset() throws DeviceException {
		ScannableOffsetAndScalingComponent mock = mock(ScannableOffsetAndScalingComponent.class);
		getSMUB().setOffsetAndScalingComponent(mock);
		when(mock.getOffset()).thenReturn(new Double[]{1000.});
		getSMU().setHardwareUnitString("mm");
		getSMU().setUserUnits("m");

		// We expect the offset array to be {1.0}, allowing for floating point imprecision
		final Double[] offset = getSMU().getOffset();
		assertEquals(1, offset.length);
		assertEquals(1.0, offset[0], FP_TOLERANCE);
	}

	@Test
	public void testGetOffsetMultipleFields() throws DeviceException {
		ScannableOffsetAndScalingComponent mock = mock(ScannableOffsetAndScalingComponent.class);
		getSMUB().setOffsetAndScalingComponent(mock);
		when(mock.getOffset()).thenReturn(new Double[]{1000., 2000., null});
		getSMU().setHardwareUnitString("mm");
		getSMU().setUserUnits("m");

		// We expect the offset array to be {1.0, 2.0, null}, allowing for floating point imprecision
		final Double[] offset = getSMU().getOffset();
		assertEquals(3, offset.length);
		assertEquals(1.0, offset[0], FP_TOLERANCE);
		assertEquals(2.0, offset[1], FP_TOLERANCE);
		assertNull(offset[2]);
	}

	@Test
	public void testSetOffsetMultipleFields() throws DeviceException {
		// Mockito fails with varargs
		ScannableMotionUnitsBase smub = new TestableScannableMotionUnitsBase();
		ScannableOffsetAndScalingComponent mock = mock(ScannableOffsetAndScalingComponent.class);
		smub.setOffsetAndScalingComponent(mock);
		smub.setHardwareUnitString("mm");
		smub.setUserUnits("m");
		smub.setInputNames(new String[]{"i1", "i2", "i3"});
		smub.setOffset(1., 2., null);
		smub.setOffset(new Double[]{1., 2., null});

		// We expect setOffset() to be called twice with array parameter {1000.0, 2000.0, null} allowing for floating point imprecision
		final ArgumentCaptor<Double[]> offsetCaptor = ArgumentCaptor.forClass(Double[].class);
		verify(mock, times(2)).setOffset(offsetCaptor.capture());
		for (Double[] parameter : offsetCaptor.getAllValues()) {
			assertEquals(3, parameter.length);
			assertEquals(1000.0, parameter[0], FP_TOLERANCE);
			assertEquals(2000.0, parameter[1], FP_TOLERANCE);
			assertNull(parameter[2]);
		}
	}

	@Test
	public void testSetOffsetMultipleFieldsViaObject() throws DeviceException {
		// Mockito fails with varargs
		ScannableMotionUnitsBase smub = new TestableScannableMotionUnitsBase();
		ScannableOffsetAndScalingComponent mock = mock(ScannableOffsetAndScalingComponent.class);
		smub.setOffsetAndScalingComponent(mock);
		smub.setHardwareUnitString("mm");
		smub.setUserUnits("m");
		smub.setInputNames(new String[]{"i1", "i2", "i3"});
		smub.setOffset(new Object[]{Quantities.getQuantity(1000000, MICRO(METRE)), "2m", null});

		// We expect setOffset() to be called once with array parameter {1000., 2000., null}
		final ArgumentCaptor<Double[]> offsetCaptor = ArgumentCaptor.forClass(Double[].class);
		verify(mock, times(1)).setOffset(offsetCaptor.capture());
		final Double[] parameter = offsetCaptor.getValue();
		assertEquals(3, parameter.length);
		assertEquals(1000.0, parameter[0], FP_TOLERANCE);
		assertEquals(2000.0, parameter[1], FP_TOLERANCE);
		assertNull(parameter[2]);
	}

	@Test
	public void testSetOffsetSingleField() throws DeviceException {
		// Mockito fails with varargs
		ScannableMotionUnitsBase smub = new TestableScannableMotionUnitsBase();
		ScannableOffsetAndScalingComponent mock = mock(ScannableOffsetAndScalingComponent.class);
		smub.setOffsetAndScalingComponent(mock);
		smub.setHardwareUnitString("mm");
		smub.setUserUnits("m");
		smub.setInputNames(new String[]{"i1"});
		smub.setOffset(new Object[]{Quantities.getQuantity(1000000, MICRO(METRE))});
		smub.setOffset(Quantities.getQuantity(1000000, MICRO(METRE)));
		smub.setOffset(1);
		smub.setOffset("1");
		smub.setOffset("1m");

		// We expect setOffset() to be called 5 times with a value of 1000.0 allowing for floating point imprecision
		final ArgumentCaptor<Double[]> offsetCaptor = ArgumentCaptor.forClass(Double[].class);
		verify(mock, times(5)).setOffset(offsetCaptor.capture());

		for (Double[] parameter : offsetCaptor.getAllValues()) {
			assertEquals(1, parameter.length);
			assertEquals(1000.0, parameter[0], FP_TOLERANCE);
		}
	}

	@Override
	@Test
	public void testToStringWithOffsets() throws DeviceException {
		configureTwoExtraFields();
		getSMUB().setUserUnits("mm");
		getSMUB().setOffset(.1, .2);
		when(getDelegate().rawGetPosition()).thenReturn(new Double[] { 1., 2., 3., 4. });
		getSB().setOutputFormat(new String[] { "%1.2g", "%1.2g", "%1.3g", "%1.4g" });
		assertEquals("name : i1: 1.1mm(+0.10) i2: 2.2mm(+0.20) e1: 3.00mm e2: 4.000mm", getSMUB().toFormattedString());
	}

	@Override
	@Test
	public void testToStringWithOffsetsSingleInput() throws DeviceException {
		configureOneInputField();
		getSB().setName("i1");
		getSMUB().setUserUnits("mm");
		getSMUB().setOffset(.1);
		when(getDelegate().rawGetPosition()).thenReturn(new Double[] { 1.});
		getSB().setOutputFormat(new String[] { "%1.2g"});
		assertEquals("i1 : 1.1mm(+0.10)", getSMUB().toFormattedString());
	}


}
//	@Test
//	public void testIsAtWithUnits() throws DeviceException {
//		createScannableToTest();
//		configureOneInputField();
//		getSMU().setHardwareUnitString("eV");
//		getSMU().setUserUnits("keV");
//		assertTrue(getSMU().isAt(1.));
//	}


//	@Test
//	public void testAddGetAcceptableUnit() throws DeviceException {
//		getSMU().addAcceptableUnit("m");
//		assertArrayEquals(new String[]{"m"}, getSMU().getAcceptableUnits());
//		getSMU().addAcceptableUnit("mm");
//		assertArrayEquals(new String[]{"m", "mm"}, getSMU().getAcceptableUnits());
//	}
