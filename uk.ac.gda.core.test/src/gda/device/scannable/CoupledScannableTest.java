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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.jscience.physics.quantities.Quantity;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;

import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.ScannableMotionUnits;
import gda.factory.FactoryException;
import gda.function.Function;
import gda.util.QuantityFactory;

public class CoupledScannableTest {
	/*
	 * test that position returned takes into account changes in the user units of the first scannable
	 */
	@Test
	public void testUseFirstScannableUnits() throws DeviceException, FactoryException {
		ScannableMotionUnits bsx = new DummyUnitsScannable("bsx", 0, "mm", "mm");

		CoupledScannable scannable = new CoupledScannable();
		scannable.setScannables(new Scannable[]{bsx});
		scannable.setName("test");
		scannable.configure();

		bsx.moveTo(1.0);
		Object posWithUserUnits_mm = scannable.getPosition();
		assertEquals("Should have same value as position of bsx as user unit set to that of bsx", 1.0, posWithUserUnits_mm);

		bsx.setUserUnits("m");

		Object posWithUserUnits_m = scannable.getPosition();

		assertEquals(posWithUserUnits_mm, posWithUserUnits_m);
	}

	@Test
	public void testUseFirstScannableUnitsButWithInitialUnitsSet() throws DeviceException, FactoryException {
		ScannableMotionUnits bsx = new DummyUnitsScannable("bsx", 0, "mm", "mm");

		CoupledScannable scannable = new CoupledScannable();
		scannable.setScannables(new Scannable[]{bsx});
		scannable.setName("test");
		scannable.setInitialUserUnits("micron");
		scannable.configure();

		bsx.moveTo(1.0);
		Object posWithUserUnits_mm = scannable.getPosition();
		assertEquals("Should have value of 1000 user unit set to micro whilst bsx is mm", 1000.0, posWithUserUnits_mm);

		bsx.setUserUnits("m");

		Object posWithUserUnits_m = scannable.getPosition();

		assertEquals(posWithUserUnits_mm, posWithUserUnits_m);
	}

	/*
	 * When the movement of the component scannables are coupled to the position of the overall CoupledScannable by a
	 * function, the function must know the units of the CoupledScannable in order to calculate the position of the
	 * component.
	 */
	@Test
	public void testPassCorrectUnits() throws DeviceException, FactoryException {
		final ScannableMotionUnits scannable = new DummyUnitsScannable("s1", 0, "mm", "mm");

		final Function function = mock(Function.class);
		when(function.evaluate(Matchers.any(Quantity.class))).thenReturn(QuantityFactory.createFromString("12.3 cm"));

		final CoupledScannable coupled = new CoupledScannable();
		coupled.setName("testCoupledScannable");
		coupled.setUserUnits("eV");
		coupled.setScannables(new Scannable[] { scannable });
		coupled.setFunctions(new Function[] { function });
		coupled.configure();

		coupled.moveTo(798.34);

		final ArgumentCaptor<Quantity> quantityCaptor = ArgumentCaptor.forClass(Quantity.class);

		// Verify that the input units of the coupled scannable (i.e. eV) are passed to the evaluation function for the
		// component scannable
		verify(function).evaluate(quantityCaptor.capture());
		assertEquals(798.34, quantityCaptor.getValue().getAmount(), 0.0001);
		assertEquals("eV", quantityCaptor.getValue().getUnit().toString());

		// Verify that the units for the component move have been handled correctly
		assertEquals("mm", scannable.getHardwareUnitString());
		assertEquals(123.0, (Double) scannable.getPosition(), 0.0001);
	}
}
