/*
 * Copyright 2012 Diamond Light Source Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.ac.diamond.scisoft.analysis.slice;

import java.util.Arrays;

import org.junit.Test;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.DatasetUtils;
import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import uk.ac.diamond.scisoft.analysis.dataset.ILazyDataset;
import uk.ac.diamond.scisoft.analysis.io.DataHolder;
import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;
import uk.ac.diamond.scisoft.analysis.io.SliceObject;
import uk.ac.diamond.scisoft.analysis.rcp.views.nexus.SliceUtils;

public class SliceTest {

	/**
	 * Tests doing a slice using LoaderFactory, the slice is every 10 using the step parameter and the test checks
	 * that this parameter results in the correct shape.
	 * @throws Exception
	 */
	@Test
	public void testSlice() throws Exception {
		
		final SliceObject currentSlice = new SliceObject();
		currentSlice.setAxes(Arrays.asList(new IDataset[]{SliceUtils.createAxisDataset(61), SliceUtils.createAxisDataset(171)}));
		currentSlice.setName("NXdata.data");
        currentSlice.setPath(System.getProperty("GDALargeTestFilesLocation")+"NexusUITest/DCT_201006-good.h5");
		currentSlice.setSliceStart(new int[]{0, 0, 500});
		currentSlice.setSliceStop(new int[]{61, 171, 600});
		currentSlice.setSliceStep(new int[]{1, 1, 10});
		currentSlice.setX(0);
		currentSlice.setY(1);
		
		final DataHolder   dh = LoaderFactory.getData(currentSlice.getPath());
		final ILazyDataset lz = dh.getLazyDataset(currentSlice.getName());
		final IDataset  slice = lz.getSlice(currentSlice.getSliceStart(), currentSlice.getSliceStop(), currentSlice.getSliceStep());
		AbstractDataset       trans = DatasetUtils.transpose(slice, new int[]{0, 1, 2});
		
		if (trans.getShape()[2]!=10) {
			throw new Exception("Incorrect shape of slice returned! Expected 10 but was "+trans.getShape()[2]);
		}
		
		System.out.println("Slice test passed.");

	}

	/**
	 * Tests doing a slice using LoaderFactory, the slice is every -5using the step parameter and the test checks
	 * that this parameter results in the correct shape.
	 * @throws Exception
	 */
	@Test
	public void testSlice2() throws Exception {
		
		final SliceObject currentSlice = new SliceObject();
		currentSlice.setAxes(Arrays.asList(new IDataset[]{SliceUtils.createAxisDataset(61), SliceUtils.createAxisDataset(171)}));
		currentSlice.setName("NXdata.data");
		currentSlice.setPath(System.getProperty("GDALargeTestFilesLocation")+"NexusUITest/DCT_201006-good.h5");
		currentSlice.setSliceStart(new int[]{0, 0, 600});
		currentSlice.setSliceStop(new int[]{61, 171, 500});
		currentSlice.setSliceStep(new int[]{1, 1, -5});
		currentSlice.setX(0);
		currentSlice.setY(1);

		final DataHolder   dh = LoaderFactory.getData(currentSlice.getPath());
		final ILazyDataset lz = dh.getLazyDataset(currentSlice.getName());
		final IDataset  slice = lz.getSlice(currentSlice.getSliceStart(), currentSlice.getSliceStop(), currentSlice.getSliceStep());
		AbstractDataset       trans = DatasetUtils.transpose(slice, new int[]{0, 1, 2});

		if (trans.getShape()[2]!=20) {
			throw new Exception("Incorrect shape of slice returned! Expected 20 but was "+trans.getShape()[2]);
		}

		System.out.println("Slice test passed.");

	}
	
	

	/**
	 * Tests doing a slice using LoaderFactory, the slice is every -5using the step parameter and the test checks
	 * that this parameter results in the correct shape.
	 * @throws Exception
	 */
	@Test
	public void testSlice3() throws Exception {
		
		final SliceObject currentSlice = new SliceObject();
		currentSlice.setAxes(Arrays.asList(new IDataset[]{SliceUtils.createAxisDataset(225), SliceUtils.createAxisDataset(1481)}));
		currentSlice.setName("NXdata.data");
		currentSlice.setPath(System.getProperty("GDALargeTestFilesLocation")+"NexusUITest/sino.h5");
		currentSlice.setSliceStart(new int[]{4, 0, 0});
		currentSlice.setSliceStop(new int[]{5, 225, 1481});
		currentSlice.setSliceStep(new int[]{1, 1, 1});
		currentSlice.setX(1);
		currentSlice.setY(2);

		final DataHolder   dh = LoaderFactory.getData(currentSlice.getPath());
		final ILazyDataset lz = dh.getLazyDataset(currentSlice.getName());
		final IDataset  slice = lz.getSlice(currentSlice.getSliceStart(), currentSlice.getSliceStop(), currentSlice.getSliceStep());
		AbstractDataset       trans = DatasetUtils.transpose(slice, new int[]{0, 1, 2});

		// We sum the data in the dimensions that are not axes
		AbstractDataset sum    = trans;
		final int[]     dataShape = new int[]{62, 225, 1481};
		final int       len    = dataShape.length;

		for (int i = len - 1; i >= 0; i--) {
			if (!currentSlice.isAxis(i) && dataShape[i]>1)
				sum = sum.sum(i);
		}
		System.out.println(Arrays.toString(sum.getShape()));

	}

}
