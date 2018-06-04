/*-
 * Copyright © 2018 Diamond Light Source Ltd.
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

package gda.scan;

import static gda.scan.ScanInformation.EMPTY;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import org.junit.Test;

import gda.scan.ScanInformation.ScanInformationBuilder;

public class ScanInformationTest {

	@Test
	public void testBuilderFromScanInformation() {
		ScanInformation si = new ScanInformationBuilder()
				.detectorNames("det1", "det2")
				.scannableNames("scan1")
				.filename("/path/to/file.nxs")
				.scanNumber(1234)
				.instrument("inst")
				.dimensions(1, 2, 3)
				.numberOfPoints(12)
				.build();
		ScanInformation si2 = ScanInformationBuilder.from(si).build();

		assertEquals("/path/to/file.nxs", si2.getFilename());
		assertEquals("inst", si2.getInstrument());
		assertEquals(12, si2.getNumberOfPoints());
		assertEquals(1234, si2.getScanNumber());
		assertArrayEquals(new String[] {"det1", "det2"}, si2.getDetectorNames());
		assertArrayEquals(new String[] {"scan1"}, si2.getScannableNames());
		assertArrayEquals(new int[] {1, 2,3}, si2.getDimensions());
	}

	@Test
	public void testEmptyScanInformation() {
		assertEquals("", EMPTY.getFilename());
		assertEquals("", EMPTY.getInstrument());
		assertEquals(-1, EMPTY.getNumberOfPoints());
		assertEquals(-1, EMPTY.getScanNumber());
		assertArrayEquals(new String[] {}, EMPTY.getDetectorNames());
		assertArrayEquals(new String[] {}, EMPTY.getScannableNames());
		assertArrayEquals(new int[] {}, EMPTY.getDimensions());
	}

	@Test
	public void testToString() {
		ScanInformation si = new ScanInformationBuilder()
				.detectorNames("det1", "det2")
				.scannableNames("scan1")
				.filename("/path/to/file.nxs")
				.scanNumber(1234)
				.instrument("inst")
				.dimensions(1, 2, 3)
				.numberOfPoints(12)
				.build();
		assertEquals("Scan 1234 : A Scan of rank 3 with the dimensions: 1x2x3 over scannables: scan1 using detectors: det1, det2", si.toString());
		// test caching
		assertSame(si.toString(), si.toString());
	}
}
