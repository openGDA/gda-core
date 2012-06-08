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
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import gda.MockFactory;
import gda.configuration.properties.LocalProperties;
import gda.device.DeviceException;
import gda.device.ScannableMotionUnits;
import gda.util.converters.LookupTableQuantityConverter;

import org.jscience.physics.quantities.Quantity;
import org.jscience.physics.units.SI;
import org.junit.Before;
import org.junit.Test;

/**
 *
 */
public class ConvertorScannableTest {

	private ConvertorScannable scannable;
	private ScannableMotionUnits bsx;
	private gda.util.converters.LookupTableConverterHolder converterx;

	/**
	 * @throws Exception
	 */
	@Before
	public void setUp() throws Exception {
		bsx = MockFactory.createMockScannableMotionUnits("bsx");

		LocalProperties.set("gda.function.columnDataFile.lookupDir", "test/gda/device/scannable/TestFiles");
		converterx = new gda.util.converters.LookupTableConverterHolder("converterx", "beamstop_to_sample.txt", 0, 1,
				null);
		scannable = new ConvertorScannable("beamstopToSample", bsx, converterx);
	}

	@Test
	public void testMoveTo() throws DeviceException {
		when(bsx.getUserUnits()).thenReturn("mm");

		scannable.asynchronousMoveTo(2);
		verify(bsx).asynchronousMoveTo(Quantity.valueOf(4.0, SI.MILLI(SI.METER)));

		when(bsx.getPosition()).thenReturn(6.0);
		assertEquals("beamstopToSample : 3.0000mm", scannable.toFormattedString());

		when(bsx.getUserUnits()).thenReturn("micron");
		when(bsx.getPosition()).thenReturn(4000);

		assertEquals("beamstopToSample : 2.0000mm", scannable.toFormattedString());

		scannable.setUserUnits("micron");
		assertEquals("beamstopToSample : 2000.0micron", scannable.toFormattedString());

	}

	@Test
	public void testCheckPositionValid() throws DeviceException,Exception {
		bsx = new DummyUnitsScannable("bsx", 0, "mm", "mm");
		bsx.setUpperGdaLimits(10.0);
		bsx.setLowerGdaLimits(0.0);
		scannable = new ConvertorScannable("beamstopToSample", bsx, converterx);
		assertTrue(scannable.checkPositionValid(4.5) == null);
		assertTrue(scannable.checkPositionValid(5.5) != null);
		
		scannable.setUserUnits("micron");
		assertTrue(scannable.checkPositionValid(4500) == null);
		assertTrue(scannable.checkPositionValid(5500) != null);

	}

	@Test
	public void testWithDifferingUnits() throws DeviceException {
		bsx = new DummyUnitsScannable("bsx", 0, "mm", "mm");

		LocalProperties.set("gda.function.columnDataFile.lookupDir", "test/gda/device/scannable/TestFiles");
		gda.util.converters.JEPConverterHolder converterx = new gda.util.converters.JEPConverterHolder("testconverter",
				"mDeg_dcm_perp_mm_converter.xml");
		scannable = new ConvertorScannable("beamstopToSample", bsx, converterx);
		// hardware units set to scannable's hw unit already (mm)
		scannable.setInitialUserUnits("mDeg");
		scannable.setConvertorUnitString("mDeg");

	}

	@Test
	public void testMoveWithDifferingUnits() throws DeviceException {
		bsx = new DummyUnitsScannable("bsx", 0, "mm", "mm");

		LocalProperties.set("gda.function.columnDataFile.lookupDir", "test/gda/device/scannable/TestFiles");
		gda.util.converters.JEPConverterHolder converterx = new gda.util.converters.JEPConverterHolder("testconverter",
				"mDeg_dcm_perp_mm_converter.xml");
		scannable = new ConvertorScannable("beamstopToSample", bsx, converterx);
		// hardware units set to scannable's hw unit already (mm)
		scannable.setInitialUserUnits("mDeg");
		// scannable.setConvertorUnitString("mDeg");
		scannable.asynchronousMoveTo(5000.0);
		assertEquals(0.22419773646390695, bsx.getPosition());

	}
	
	@Test
	public void testConverterDoesNotHandleTtoS() throws DeviceException {
		bsx = new DummyUnitsScannable("bsx", 0, "mm", "mm");

		LocalProperties.set("gda.function.columnDataFile.lookupDir", "test/gda/device/scannable/TestFiles");
		converterx = new gda.util.converters.LookupTableConverterHolder("converterx", "beamstop_to_sample.txt", 0, 1,
				LookupTableQuantityConverter.Mode_StoT);

		scannable = new ConvertorScannable("beamstopToSample", bsx, converterx);
		Double valToSend = new Double(5000.);
		scannable.asynchronousMoveTo(valToSend);
		
		//move outside of the convertorscannable
		bsx.asynchronousMoveTo(10.);

		Object position = scannable.getPosition();
		//should return last value sent rather than convert the position of the underlying scannable
		assertEquals(valToSend, position);
		
	}

	@Test
	public void testConverterDoesHandleTtoS() throws DeviceException {
		bsx = new DummyUnitsScannable("bsx", 0, "mm", "mm");

		LocalProperties.set("gda.function.columnDataFile.lookupDir", "test/gda/device/scannable/TestFiles");
		converterx = new gda.util.converters.LookupTableConverterHolder("converterx", "beamstop_to_sample.txt", 0, 1,
				LookupTableQuantityConverter.Mode_Both);

		scannable = new ConvertorScannable("beamstopToSample", bsx, converterx);

		Double valToSendToBSX = new Double(10.);
		bsx.asynchronousMoveTo(valToSendToBSX);
		Object valForBSX10 = scannable.getPosition();
		
		Double valToSend = new Double(5000.);
		scannable.asynchronousMoveTo(valToSend);

		//move outside of the converter
		bsx.asynchronousMoveTo(valToSendToBSX);
		
		
		Object position = scannable.getPosition();
		//should return converted the position of the underlying scannable
		assertEquals(valForBSX10, position);
		
	}

}
