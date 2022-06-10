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

package uk.ac.gda.analysis.mscan;

import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.dawnsci.analysis.dataset.slicer.SliceFromSeriesMetadata;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import gda.device.detector.nexusprocessor.roistats.RegionOfInterest;

class RoiProcTest {

	@Test
	void shouldRefuseToProcessRotatedFrame() {
		RectangularROI rectangularRoi = new RectangularROI(1, 30);
		rectangularRoi.setName("rotated");
		RegionOfInterest roi = new RegionOfInterest(rectangularRoi);

		RoiProc systemUnderTest = new RoiProc();
		systemUnderTest.getRois().add(roi);
		Dataset dataset = Mockito.mock(Dataset.class);
		Assertions.assertThrows(IllegalStateException.class,
				() -> systemUnderTest.processFrame(dataset, Mockito.mock(SliceFromSeriesMetadata.class)));

		Mockito.verifyNoInteractions(dataset);
		Assertions.assertNull(systemUnderTest.latestStatForRoi(roi));
	}

	@Test
	void shouldRefuseToProcessRotatedFrameForLazyDataset() {
		RectangularROI rectangularRoi = new RectangularROI(5, 120);
		rectangularRoi.setName("rotated");
		RegionOfInterest roi = new RegionOfInterest(rectangularRoi);

		RoiProc systemUnderTest = new RoiProc();
		systemUnderTest.getRois().add(roi);
		ILazyDataset dataset = Mockito.mock(ILazyDataset.class);
		Assertions.assertThrows(IllegalStateException.class,
				() -> systemUnderTest.processFrame(dataset, Mockito.mock(SliceFromSeriesMetadata.class)));

		Mockito.verifyNoInteractions(dataset);
		Assertions.assertNull(systemUnderTest.latestStatForRoi(roi));
	}

}
