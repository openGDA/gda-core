/*-
 * Copyright © 2022 Diamond Light Source Ltd.
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

package gda.device.detector.nexusprocessor.roistats;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.stream.Stream;

import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import gda.device.detector.nexusprocessor.DatasetStats;

class NormalisingRegionProcessorTest {

	private NormalisingRegionProcessor proc;
	private RoiStatsProcessor roiStats;

	@BeforeEach
	void setup() {
		proc = new NormalisingRegionProcessor();
		roiStats = new RoiStatsProcessor();
		var stats = new DatasetStats();
		roiStats.setStatsProcessor(stats);
		proc.setRoiStats(roiStats);

		var roi1 = new RectangularROI(100, 100, 5, 5, 0);
		roi1.setName("signal");
		var roi2 = new RectangularROI(0, 0, 80, 80, 0);
		roi2.setName("background");
		roiStats.setRois(Stream.of(roi1, roi2).map(RegionOfInterest::new).collect(toList()));
		roiStats.updateNames();

	}

	@Test
	void testNormalisationWithDefaults() throws Exception {

		Dataset testDataset = DatasetFactory.createFromObject(new int[] { 100, 100 });
		testDataset.resize(new int[] { 1000, 1000 });

		var result = proc.process("det", "data", testDataset);
		var normResult = result.getDoubleVals()[asList(result.getExtraNames()).indexOf("norm")];
		assertThat(normResult, is(closeTo(-0.78125, 1e-8)));

	}

	@Test
	void testInsufficientRoisThrows() {
		// setup for two background ROIs
		proc.setSignalRoiIndex(0);
		proc.setBackgroundRoiIndices(List.of(1, 2));

		Dataset testDataset = DatasetFactory.createFromObject(new int[] { 100, 100 });
		testDataset.resize(new int[] { 1000, 1000 });

		assertThrows(IllegalStateException.class, () -> proc.process("det", "data", testDataset));
	}

	@Test
	void testBackgroundRegionsCanBeDisabled() throws Exception {
		proc.setBackgroundSubtractionEnabled(false);

		Dataset testDataset = DatasetFactory.createFromObject(new int[] { 100, 100 });
		testDataset.resize(new int[] { 1000, 1000 });

		var result = proc.process("det", "data", testDataset);
		var normResult = result.getDoubleVals()[asList(result.getExtraNames()).indexOf("norm")];
		assertThat(normResult, is(closeTo(0, 1e-8)));
	}

}
