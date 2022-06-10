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

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import gda.device.detector.GDANexusDetectorData;
import gda.device.detector.nexusprocessor.DatasetStats;
import gda.factory.FactoryException;

class RoiStatsProcessorTest {
	private DatasetStats stat;
	private RoiStatsProcessor roistats;

	@BeforeEach
	public void setup() throws FactoryException {
		stat = new DatasetStats();
		stat.setEnabledStats(Arrays.asList(DatasetStats.Statistic.SUM));
		roistats = new RoiStatsProcessor();
		roistats.setStatsProcessor(stat);
		roistats.configure();
		roistats.updateNames();
	}

	@Test
	void testRoiStats() throws Exception {
		var roiName = "roi1";
		Dataset testDataset = DatasetFactory.createFromObject(new int[] { 100, 100 });
		testDataset.resize(new int[] { 1000, 1000 });
		var roi = new RectangularROI(1, 0, 5, 5, 0);
		roi.setName(roiName);
		setRois(List.of(new RegionOfInterest(roi)));
		GDANexusDetectorData res = roistats.process("det", "data", testDataset);
		double sumresult = res.getDoubleVals()[Arrays.asList(res.getExtraNames()).indexOf(roiName + ".total")];
		assertEquals(100, sumresult, 1);
	}

	@Test
	void shouldRefuseToProcessRotatedRoi() throws Exception {
		RectangularROI rectangularRoi = new RectangularROI(10, 75);
		rectangularRoi.setName("rotated");

		try (MockedStatic<RegionOfInterest> staticRoi = Mockito.mockStatic(RegionOfInterest.class)) {
			Mockito.when(RegionOfInterest.getRoisForPlot(ArgumentMatchers.isNull())).thenReturn(Arrays.asList(new RegionOfInterest(rectangularRoi)));
			Assertions.assertThrows(IllegalStateException.class,  () -> roistats.atScanStart());
		}

	}

	private void setRois(List<RegionOfInterest> rois) {
		roistats.setRois(rois);
		roistats.updateNames();
	}
}