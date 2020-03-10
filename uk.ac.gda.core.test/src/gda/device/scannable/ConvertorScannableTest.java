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

import static gda.function.ColumnDataFile.GDA_FUNCTION_COLUMN_DATA_FILE_LOOKUP_DIR;
import static gda.util.converters.LookupTableQuantityConverter.Mode_Both;
import static gda.util.converters.LookupTableQuantityConverter.Mode_StoT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static tec.units.indriya.unit.MetricPrefix.MILLI;
import static tec.units.indriya.unit.Units.METRE;

import javax.measure.Quantity;
import javax.measure.quantity.Angle;
import javax.measure.quantity.Length;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import gda.MockFactory;
import gda.configuration.properties.LocalProperties;
import gda.device.DeviceException;
import gda.device.ScannableMotionUnits;
import gda.util.converters.IQuantityConverter;
import gda.util.converters.JEPConverterHolder;
import gda.util.converters.LookupTableConverterHolder;
import tec.units.indriya.quantity.Quantities;

public class ConvertorScannableTest {
	// Allow for inaccuracy in floating point values
	private static final double FP_TOLERANCE = 1e-12;

	private String saveLookupDir;

	@Before
	public void setUp() {
		saveLookupDir = LocalProperties.get(GDA_FUNCTION_COLUMN_DATA_FILE_LOOKUP_DIR);
		LocalProperties.set(GDA_FUNCTION_COLUMN_DATA_FILE_LOOKUP_DIR, "testfiles/gda/device/scannable/ConvertorScannableTest");
	}

	@After
	public void tearDown() throws Exception {
		LocalProperties.set(GDA_FUNCTION_COLUMN_DATA_FILE_LOOKUP_DIR, saveLookupDir);
	}

	@Test
	public void testMoveTo() throws DeviceException {
		final ScannableMotionUnits bsx = MockFactory.createMockScannableMotionUnits("bsx");
		when(bsx.getUserUnits()).thenReturn("mm");

		final LookupTableConverterHolder<Length, Length> converterx = new LookupTableConverterHolder<>("converterx", "beamstop_to_sample.txt", 0, 1, null);
		final ConvertorScannable<Length, Length> scannable = createConvertorScannable("beamstopToSample", bsx, converterx);

		scannable.asynchronousMoveTo(2);
		verify(bsx).asynchronousMoveTo(Quantities.getQuantity(4.0, MILLI(METRE)));

		when(bsx.getPosition()).thenReturn(6.0);
		assertEquals("beamstopToSample : 3.0000mm", scannable.toFormattedString());

		when(bsx.getUserUnits()).thenReturn("micron");
		when(bsx.getPosition()).thenReturn(4000);

		assertEquals("beamstopToSample : 2.0000mm", scannable.toFormattedString());

		scannable.setUserUnits("micron");
		assertEquals("beamstopToSample : 2000.0µm", scannable.toFormattedString());
	}

	@Test
	public void testCheckPositionValid() throws DeviceException, Exception {
		final ScannableMotionUnits bsx = new DummyUnitsScannable<Length>("bsx", 0, "mm", "mm");
		bsx.setUpperGdaLimits(10.0);
		bsx.setLowerGdaLimits(0.0);

		final LookupTableConverterHolder<Length, Length> converterx = new LookupTableConverterHolder<>("converterx", "beamstop_to_sample.txt", 0, 1, null);
		final ConvertorScannable<Length, Length> scannable = createConvertorScannable("beamstopToSample", bsx, converterx);
		assertNull(scannable.checkPositionValid(4.5));
		assertNotNull(scannable.checkPositionValid(5.5));

		scannable.setUserUnits("micron");
		assertNull(scannable.checkPositionValid(4500));
		assertNotNull(scannable.checkPositionValid(5500));
	}

	@Test
	public void testWithDifferingUnits() throws DeviceException {
		final ScannableMotionUnits bsx = new DummyUnitsScannable<Length>("bsx", 0, "mm", "mm");

		final JEPConverterHolder<Angle, Length> jepConverterx = new JEPConverterHolder<>("testconverter", "mDeg_dcm_perp_mm_converter.xml");
		final ConvertorScannable<Angle, Length> scannable = createConvertorScannable("beamstopToSample", bsx, jepConverterx);
		// hardware units set to scannable's hw unit already (mm)
		scannable.setInitialUserUnits("mDeg");
		scannable.setConvertorUnitString("mDeg");
	}

	@Test
	public void testMoveWithDifferingUnits() throws DeviceException {
		final ScannableMotionUnits bsx = new DummyUnitsScannable<Length>("bsx", 0, "mm", "mm");

		final JEPConverterHolder<Angle, Length> jepConverterx = new JEPConverterHolder<>("testconverter", "mDeg_dcm_perp_mm_converter.xml");
		final ConvertorScannable<Angle, Length> scannable = createConvertorScannable("beamstopToSample", bsx, jepConverterx);
		// hardware units set to scannable's hw unit already (mm)
		scannable.setInitialUserUnits("mDeg");
		// scannable.setConvertorUnitString("mDeg");
		scannable.asynchronousMoveTo(5000.0);
		assertEquals(0.22419773646390695, (double) bsx.getPosition(), FP_TOLERANCE);
	}

	@Test
	public void testConverterDoesNotHandleTtoS() throws DeviceException {
		final ScannableMotionUnits bsx = new DummyUnitsScannable<Length>("bsx", 0, "mm", "mm");

		final LookupTableConverterHolder<Length, Length> converterx = new LookupTableConverterHolder<>("converterx", "beamstop_to_sample.txt", 0, 1, Mode_StoT);
		final ConvertorScannable<Length, Length> scannable = createConvertorScannable("beamstopToSample", bsx, converterx);

		final double valToSend = 5000.0;
		scannable.asynchronousMoveTo(valToSend);

		// move outside of the convertorscannable
		bsx.asynchronousMoveTo(10.);

		// should return last value sent rather than convert the position of the underlying scannable
		assertEquals(valToSend, (double) scannable.getPosition(), FP_TOLERANCE);
	}

	@Test
	public void testConverterDoesHandleTtoS() throws DeviceException {
		final ScannableMotionUnits bsx = new DummyUnitsScannable<Length>("bsx", 0, "mm", "mm");

		final LookupTableConverterHolder<Length, Length> converterx = new LookupTableConverterHolder<>("converterx", "beamstop_to_sample.txt", 0, 1, Mode_Both);
		final ConvertorScannable<Length, Length> scannable = createConvertorScannable("beamstopToSample", bsx, converterx);

		final double valToSendToBSX = 10.0;
		bsx.asynchronousMoveTo(valToSendToBSX);
		final double valForBSX10 = (double) scannable.getPosition();

		scannable.asynchronousMoveTo(5000.0);

		// move outside of the converter
		bsx.asynchronousMoveTo(valToSendToBSX);

		// should return converted the position of the underlying scannable
		assertEquals(valForBSX10, (double) scannable.getPosition(), FP_TOLERANCE);
	}

	private static <S extends Quantity<S>, T extends Quantity<T>> ConvertorScannable<S, T> createConvertorScannable(String name, ScannableMotionUnits theScannable, IQuantityConverter<S, T> theConvertor) {
		final ConvertorScannable<S, T> scannable = new ConvertorScannable<>();
		scannable.setName(name);
		scannable.setInputNames(new String[] { name });
		scannable.setScannable(theScannable);

		// set up the units component for this object based on the underlying scannable
		scannable.setConvertor(theConvertor);
		scannable.setScannableName(theScannable.getName());

		scannable.configure();
		return scannable;
	}
}
