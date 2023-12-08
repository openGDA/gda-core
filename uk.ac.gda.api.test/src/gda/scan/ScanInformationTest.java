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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.emptyArray;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyString;
import static org.hamcrest.Matchers.sameInstance;

import org.junit.jupiter.api.Test;

import gda.scan.ScanInformation.ScanInformationBuilder;

class ScanInformationTest {

	@Test
	void testBuilderFromScanInformation() {
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

		assertThat(si2.getFilename(), is(equalTo("/path/to/file.nxs")));
		assertThat(si2.getInstrument(), is(equalTo("inst")));
		assertThat(si2.getNumberOfPoints(), is(12));
		assertThat(si2.getScanNumber(), is(1234));
		assertThat(si2.getDetectorNames(), is(arrayContaining("det1", "det2")));
		assertThat(si2.getScannableNames(), is(arrayContaining("scan1")));
		assertThat(si2.getDimensions(), is(equalTo(new int[] { 1, 2, 3 })));
	}

	@Test
	void testEmptyScanInformation() {
		assertThat(EMPTY.getFilename(), isEmptyString());
		assertThat(EMPTY.getInstrument(), isEmptyString());
		assertThat(EMPTY.getNumberOfPoints(), is(-1));
		assertThat(EMPTY.getScanNumber(), is(-1));
		assertThat(EMPTY.getDetectorNames(), is(emptyArray()));
		assertThat(EMPTY.getScannableNames(), is(emptyArray()));
		assertThat(EMPTY.getDimensions(), is(equalTo(new int[0])));
	}

	@Test
	void testToString() {
		ScanInformation si = new ScanInformationBuilder()
				.detectorNames("det1", "det2")
				.scannableNames("scan1")
				.filename("/path/to/file.nxs")
				.scanNumber(1234)
				.instrument("inst")
				.dimensions(1, 2, 3)
				.numberOfPoints(12)
				.scanCommand("scan scan1 1 12 1 det1 det2")
				.build();

		assertThat(si.toString(), is(equalTo("Scan 1234 : A Scan of rank 3 with the dimensions: 1x2x3 over scannables: scan1 using detectors: det1, det2 with scan command: scan scan1 1 12 1 det1 det2")));
		// test caching
		assertThat(si.toString(), is(sameInstance(si.toString())));
	}

}
