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
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.ScannableMotionUnits;
import gda.factory.FactoryException;

import org.junit.Test;

/**
 *
 */
public class CoupledScannableTest {
	/*
	 * test that position returned takes into account changes in the user untis of the first scannable
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
	public void testUseFirstScannableUnitsButWithInitualUnitsSet() throws DeviceException, FactoryException {
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

}
