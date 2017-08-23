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

import static org.jscience.physics.units.SI.METER;
import static org.jscience.physics.units.SI.MILLI;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.jscience.physics.quantities.Quantity;
import org.junit.Before;
import org.junit.Test;
import org.python.core.PyFloat;
import org.python.core.PyTuple;

import gda.device.DeviceException;
import gda.device.scannable.component.UnitsComponent;

/**
 * Note: this is largely tested through ScannableMotionUnitsBaseTeast
 */
public class UnitsComponentTest {


	UnitsComponent uc;
	static Quantity q1m = Quantity.valueOf(1, METER);
	static Quantity q2m = Quantity.valueOf(2, METER);
	static Quantity q3m = Quantity.valueOf(3, METER);
	static Quantity q1000mm = Quantity.valueOf(1000, MILLI(METER));
	static Quantity q2000mm = Quantity.valueOf(2000, MILLI(METER));
	static Quantity q3000mm = Quantity.valueOf(3000, MILLI(METER));

	@Before
	public void setUp() {
		uc = new UnitsComponent();
	}

	@Test
	public void testExternalTowardInternalNoUnitsSet() {
		Object object = new Object();
		assertEquals(object, uc.externalTowardInternal(object));
	}

	@Test
	public void testInternalTowardExternal() {
		Object object = new Object();
		assertEquals(object, uc.internalTowardExternal(object));
	}

	@Test
	public void testExternalTowardInternalWithObjectArray() throws DeviceException {
		uc.setUserUnits("m");
		uc.setHardwareUnitString("mm");
		Double[] internalExpected = new Double[]{1000., 2000., 3000., 4000., 5000.};
		assertArrayEquals(internalExpected, (Double[]) uc.externalTowardInternal(new Object[]{1, q2m, q3000mm, "4m", "5000 mm"}));
	}

	@Test
	public void testExternalTowardInternalWithPyTuple() throws DeviceException {
		uc.setUserUnits("m");
		uc.setHardwareUnitString("mm");
		PyTuple internalExpectedTuple = new PyTuple(new PyFloat(1000.), new PyFloat(2000.));
		assertEquals(internalExpectedTuple, uc.externalTowardInternal(new PyTuple(new PyFloat(1.), new PyFloat(2.))));
	}

	@Test
	public void testInternalTowardExternalWithPyTuple() throws DeviceException {
		uc.setHardwareUnitString("mm");
		uc.setUserUnits("m");
		PyTuple externalExpectedTuple = new PyTuple(new PyFloat(1.), new PyFloat(2.));
		assertEquals(externalExpectedTuple, uc.internalTowardExternal(new PyTuple(new PyFloat(1000.), new PyFloat(2000.))));
	}

	@Test
	public void testExternalTowardInternalWithObject() throws DeviceException {
		uc.setUserUnits("m");
		uc.setHardwareUnitString("mm");
		assertEquals(new Double(1000.), uc.externalTowardInternal(1));
		assertEquals(new Double(1000.), uc.externalTowardInternal(1.));
		assertEquals(new Double(1000.), uc.externalTowardInternal(q1m));
		assertEquals(new Double(1000.), uc.externalTowardInternal(q1000mm));
		assertEquals(new Double(1000.), uc.externalTowardInternal("1"));
		assertEquals(new Double(1000.), uc.externalTowardInternal("1m"));
		assertEquals(new Double(1000.), uc.externalTowardInternal("1000mm"));
	}

	@Test
	public void testInternalTowardExternalWithObjectArray() throws DeviceException {
		uc.setHardwareUnitString("mm");
		uc.setUserUnits("m");
		Double[] externalExpected = new Double[]{1., 2., 3., 4., 5.};
		assertArrayEquals(externalExpected, (Double[]) uc.internalTowardExternal(new Object[]{1000, q2m, q3000mm, "4m", "5000 mm"}));
	}

	@Test
	public void testInternalTowardExternalWithObject() throws DeviceException {
		uc.setHardwareUnitString("mm");
		uc.setUserUnits("m");
		assertEquals(new Double(1.), uc.internalTowardExternal(1000));
		assertEquals(new Double(1.), uc.internalTowardExternal(1000.));
		assertEquals(new Double(1.), uc.internalTowardExternal(q1m));
		assertEquals(new Double(1.), uc.internalTowardExternal(q1000mm));
		assertEquals(new Double(1.), uc.internalTowardExternal("1000"));
		assertEquals(new Double(1.), uc.internalTowardExternal("1m"));
		assertEquals(new Double(1.), uc.internalTowardExternal("1000mm"));
	}

}
