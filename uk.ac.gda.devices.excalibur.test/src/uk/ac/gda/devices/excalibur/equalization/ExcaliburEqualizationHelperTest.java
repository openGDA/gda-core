/*-
 * Copyright © 2012 Diamond Light Source Ltd.
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

package uk.ac.gda.devices.excalibur.equalization;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Random;
import java.util.Vector;

import org.eclipse.dawnsci.analysis.api.fitting.functions.IPeak;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.ShortDataset;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import gda.TestHelpers;
import uk.ac.diamond.scisoft.analysis.fitting.functions.CompositeFunction;
import uk.ac.gda.analysis.hdf5.HDF5HelperLocations;
import uk.ac.gda.analysis.hdf5.Hdf5Helper;
import uk.ac.gda.analysis.hdf5.Hdf5HelperData;

public class ExcaliburEqualizationHelperTest {
	static String TestFileFolder;

	@BeforeClass
	static public void setUpClass() {
/*		TestFileFolder = TestUtils.getGDALargeTestFilesLocation();
		if (TestFileFolder == null) {
			Assert.fail("TestUtils.getGDALargeTestFilesLocation() returned null - test aborted");
		}
*/	}

	@Test
	public void testGetEqualizedDataForwards() throws Exception {
		final int thresholdPoint = 3;
		int[] dims = new int[] { 5, 10, 10 };
		short[] data = new short[dims[0] * dims[1] * dims[2]];
		for (int i = 0; i < dims[0]; i++) {
			for (int j = 0; j < dims[1]; j++) {
				for (int k = 0; k < dims[2]; k++) {
					data[i * (dims[1] * dims[2]) + j * dims[2] + k] = 0;
					if (i >= thresholdPoint)
						data[i * (dims[1] * dims[2]) + j * dims[2] + k] = 10;
					if (j == 0 && k == 5 && i >= thresholdPoint - 1)
						data[i * (dims[1] * dims[2]) + j * dims[2] + k] = 10;
				}

			}

		}
		short[] lookupTable = new short[] { 0, 1, 2, 3, 4 };
		short[] data2 = ExcaliburEqualizationHelper.getInstance().getThresholdFromSliceofScanData(data, dims, null, 5, 0, true,
				lookupTable);
		assertEquals(dims[1] * dims[2], data2.length);
		assertEquals(lookupTable[thresholdPoint], data2[0], 1e-6);
		assertEquals(lookupTable[thresholdPoint - 1], data2[5], 1e-6);

	}

	@Test
	public void testGetEqualizedDataBackwards() throws Exception {
		final int thresholdPoint = 3;
		int[] dims = new int[] { 5, 10, 10 };
		short[] data = new short[dims[0] * dims[1] * dims[2]];
		for (int i = 0; i < dims[0]; i++) {
			for (int j = 0; j < dims[1]; j++) {
				for (int k = 0; k < dims[2]; k++) {
					data[i * (dims[1] * dims[2]) + j * dims[2] + k] = 0;
					if (i <= thresholdPoint)
						data[i * (dims[1] * dims[2]) + j * dims[2] + k] = 10;
					if (j == 0 && k == 5 && i <= thresholdPoint + 1)
						data[i * (dims[1] * dims[2]) + j * dims[2] + k] = 10;
				}

			}

		}
		short[] lookupTable = new short[] { 0, 1, 2, 3, 4 };
		short[] data2 = ExcaliburEqualizationHelper.getInstance().getThresholdFromSliceofScanData(data, dims, null, 5, 0, false,
				lookupTable);
		assertEquals(dims[1] * dims[2], data2.length);
		assertEquals(lookupTable[thresholdPoint], data2[0], 1e-6);
		assertEquals(ExcaliburEqualizationHelper.EDGE_POSITION_IF_ALL_ABOVE_THRESHOLD,data2[5],1e-6 );

	}


	@Ignore
	public void testGetEqualisationData() throws Exception {
		String testScratchDirectoryName = TestHelpers.setUpTest(ExcaliburEqualizationHelperTest.class,
				"testEqualisationProcess", true);
		int threshold = 70000;
		int sizeOfSlice = 50; //slice in 3rd dimension x or y axis of image

		String scanFilename = TestFileFolder + "ExcaliburEqualizationHelperTest/12998.nxs";

		String groupName = "entry1/excalibur_summary_ad";
		String dataSetNameThreshold0 = "threshold0";
		String dataSetNameDetector = "data";


		Hdf5HelperData threshold0Vals = Hdf5Helper.getInstance().readDataSetAll(scanFilename, groupName,
				dataSetNameThreshold0, true);
		double[] tmp = (double[]) threshold0Vals.data;
		short[] threshold0ValsAsInt = new short[tmp.length];
		for (int i = 0; i < tmp.length; i++) {
			threshold0ValsAsInt[i] = (short) tmp[i];
		}



		long before = System.currentTimeMillis();
		Hdf5HelperData hd = ExcaliburEqualizationHelper.getInstance().getThresholdFromScanData(threshold, sizeOfSlice, scanFilename, groupName, threshold0ValsAsInt,
				dataSetNameDetector);
		short[] data = (short[])hd.data;
		Assert.assertEquals(11800, data[1000],1e-6);
		Assert.assertEquals(ExcaliburEqualizationHelper.EDGE_POSITION_IF_ALL_BELOW_THRESHOLD, data[999],1e-6);
		Assert.assertEquals(11200, data[999998],1e-6);
		Assert.assertEquals(ExcaliburEqualizationHelper.EDGE_POSITION_IF_ALL_ABOVE_THRESHOLD, data[999999],1e-6);
		Hdf5Helper.getInstance().writeToFileSimple(hd, testScratchDirectoryName + "/results.hdf", new HDF5HelperLocations("equalisation"), "edgeThresholds");
		System.out.print("testEqualisationProcess time taken = " + (System.currentTimeMillis()-before));
		System.out.println();
	}

	@Test
	public void testGetConfigFromThresholdResponse() throws Exception{

		String testScratchDirectoryName = TestHelpers.setUpTest(ExcaliburEqualizationHelperTest.class,
				"testGetConfigFromThresholdResponse", true);

		ExcaliburEqualizationHelper equalizationHelper = ExcaliburEqualizationHelper.getInstance();
		HDF5HelperLocations equalisationLocation = equalizationHelper.getEqualisationLocation();

		final int numPixels = 10;
		double[] slopes = new double[numPixels];
		double[] offset = new double[numPixels];
		short [] fitok = new short[numPixels];
		for( int i=0; i< numPixels; i++){
			offset[i] = i;
			slopes[i] = 1;
			fitok[i]=ExcaliburEqualizationHelper.THRESHOLD_RESPONSE_FITOK_TRUE;
		}
		Hdf5HelperData hd = new Hdf5HelperData(new long[] { numPixels}, slopes);
		String thresholdResponseFilename = testScratchDirectoryName + "/response.hdf";
		Hdf5Helper.getInstance().writeToFileSimple(hd, thresholdResponseFilename, equalisationLocation, ExcaliburEqualizationHelper.THRESHOLD_RESPONSE_SLOPES_DATASET);

		hd = new Hdf5HelperData(new long[] { numPixels}, offset);
		Hdf5Helper.getInstance().writeToFileSimple(hd, thresholdResponseFilename, equalisationLocation, ExcaliburEqualizationHelper.THRESHOLD_RESPONSE_OFFSETS_DATASET);

		hd = new Hdf5HelperData(new long[] { numPixels}, fitok);
		Hdf5Helper.getInstance().writeToFileSimple(hd, thresholdResponseFilename, equalisationLocation, ExcaliburEqualizationHelper.THRESHOLD_RESPONSE_FITOK_DATASET);


		double thresholdTarget=10;
		Hdf5HelperData response = equalizationHelper.getThresholdFromThresholdResponseFile(thresholdResponseFilename, thresholdTarget);
		Assert.assertEquals(numPixels, ((short[])response.data).length);
		for( int i=0; i< numPixels; i++){
			Assert.assertEquals((thresholdTarget-offset[i])/slopes[i],  ((short[])response.data)[i], 1e-6);
		}
	}

	@Test
	public void testCombineThresholdResults() throws Exception{
		String testScratchDirectoryName = TestHelpers.setUpTest(ExcaliburEqualizationHelperTest.class,
				"testCombineThresholdResults", true);
		Hdf5Helper hdf = Hdf5Helper.getInstance();
		ExcaliburEqualizationHelper equalizationHelper = ExcaliburEqualizationHelper.getInstance();
		HDF5HelperLocations equalisationLocation = equalizationHelper.getEqualisationLocation();
		HDF5HelperLocations edgeThresholdLocation = equalizationHelper.getEqualisationLocation().add(ExcaliburEqualizationHelper.THRESHOLD_DATASET);
		Vector<String> fileNames = new Vector<String>();
		for(int i=0; i< 8; i++){
			int[] data = new int[100];
			Arrays.fill(data, i);
			long[] dims = new long[]{1,data.length};
			Hdf5HelperData data2 = new Hdf5HelperData(dims, data);
			String fileName = testScratchDirectoryName + "/" + i + ".hdf";
			hdf.writeToFileSimple(data2,fileName , equalisationLocation, ExcaliburEqualizationHelper.THRESHOLD_DATASET);

			Hdf5HelperData thresholdAttr = new Hdf5HelperData(i*2);
			hdf.writeAttribute(fileName, Hdf5Helper.TYPE.DATASET, edgeThresholdLocation,
					ExcaliburEqualizationHelper.THRESHOLDABNVAL_ATTR, thresholdAttr );
			fileNames.add(fileName);
		}
		ExcaliburEqualizationHelper excalibur = ExcaliburEqualizationHelper.getInstance();
		excalibur.combineThresholdsFromThresholdFiles(fileNames,  testScratchDirectoryName + "/combined.hdf", false);

	}

	@Test
	public void testFitGaussianSingleGaussian() throws Exception{
		ExcaliburEqualizationHelper equalizationHelper = ExcaliburEqualizationHelper.getInstance();
		int width=200;
		int height=100;
		Random r = new Random();
		double gaussianWidth= 1.0;
		double mean=15;
		short[] data = new short[width*height];
		for( int ih=0; ih< height; ih++){
			for( int iw=0; iw< width; iw++){
				data[ih*width + iw] = (short)(mean + gaussianWidth*r.nextGaussian()+0.5);
			}
		}
		ShortDataset shortDataset = DatasetFactory.createFromObject(ShortDataset.class, data, height, width);
		double[][] population = equalizationHelper.createBinnedPopulation( shortDataset);
		CompositeFunction aPeak = equalizationHelper.fitGaussianToBinnedPopulation(population[0], population[1]);
		Assert.assertEquals(2.4*gaussianWidth, aPeak.getPeak(0).getFWHM(), 1.0);
		Assert.assertEquals(mean, aPeak.getPeak(0).getPosition(), .1);
	}

	@Test
	public void testgetChipEdgeThreshold() throws Exception{
		String testScratchDirectoryName = TestHelpers.setUpTest(ExcaliburEqualizationHelperTest.class,
				"testgetChipEdgeThreshold", true);
		ExcaliburEqualizationHelper equalisation = ExcaliburEqualizationHelper.getInstance();
		ExcaliburEqualizationHelper equalizationHelper = equalisation;
		HDF5HelperLocations equalisationLocation = equalizationHelper.getEqualisationLocation();
		int numChipsAcross=2;
		int numChipsDown= 3;
		String fileName = testScratchDirectoryName + "/data.hdf";
		for( int ih =0; ih < numChipsDown; ih++){
			for( int iw =0; iw < numChipsAcross; iw++){
				short[] data = new short[ChipSet.chipPixels];
				Random r = new Random();
				double gaussianWidth= 1.0;
				int index = ih*numChipsAcross+iw;
				double mean=10 + index;
				for( int i=0; i< data.length; i++){
					data[i] = (short)(mean + gaussianWidth*r.nextGaussian()+0.5);
					//set pixel 0 of each to ExcaliburEqualizationHelper.ED
					if( i==0)
						data[i] = ExcaliburEqualizationHelper.EDGE_POSITION_IF_ALL_ABOVE_THRESHOLD;
					if( i==1)
						data[i] = ExcaliburEqualizationHelper.EDGE_POSITION_IF_ALL_BELOW_THRESHOLD;
				}
				Hdf5HelperData hData = new Hdf5HelperData(new long[]{ChipSet.chipHeight,
						ChipSet.chipWidth},data);
				long[] chunk_dims = hData.dims;
				boolean[] extendible = new boolean[chunk_dims.length];
				Arrays.fill(extendible, true); //both directions can be extendible
				long[] offset = new long[chunk_dims.length];
				offset[0] = ChipSet.getChipTopPixel(ih);
				offset[1] = ChipSet.getChipLeftPixel(iw);
				Hdf5Helper hdf = Hdf5Helper.getInstance();
				hdf.writeToFile(hData, fileName, equalisationLocation, ExcaliburEqualizationHelper.THRESHOLD_DATASET,
						chunk_dims, extendible, offset);

			}
		}

		//indicate all chips are present
		boolean [] chipPresent = new boolean[numChipsDown*numChipsAcross];
		Arrays.fill(chipPresent, true);
		equalisation.addChipPopulationsToThresholdFile(fileName, numChipsDown, numChipsAcross, chipPresent, fileName);


		ChipAveragedResult[] chipEdgeThreshold = equalisation.getChipAveragedThresholdFromThresholdFile(fileName, numChipsDown, numChipsAcross, chipPresent);
		for( int ih =0; ih < numChipsDown; ih++){
			for( int iw =0; iw < numChipsAcross; iw++){
				double gaussianWidth= 1.0;
				int index = ih*numChipsAcross+iw;
				double mean=10 + index;
				CompositeFunction function = chipEdgeThreshold[index].function;
				IPeak g = function.getPeak(0);
				Assert.assertEquals(mean, g.getPosition(), 1.0);
				Assert.assertEquals(2.355*gaussianWidth, g.getFWHM(), 0.5);
			}
		}
		//indicate only chip numChipsAcross is present
		Arrays.fill(chipPresent, false);
		chipPresent[numChipsAcross] = true;
		chipEdgeThreshold = equalisation.getChipAveragedThresholdFromThresholdFile(fileName, numChipsDown, numChipsAcross, chipPresent);
		for( int ih =0; ih < numChipsDown; ih++){
			for( int iw =0; iw < numChipsAcross; iw++){
				int index = ih*numChipsAcross+iw;
				if( index != numChipsAcross){
					Assert.assertNull(chipEdgeThreshold[index]);
				}else {
					double gaussianWidth= 1.0;
					double mean=10 + index;
					Assert.assertEquals(mean, chipEdgeThreshold[index].function.getPeak(0).getPosition(), 0.3);
					Assert.assertEquals(2.4*gaussianWidth, chipEdgeThreshold[index].function.getPeak(0).getFWHM(), 0.1);
				}
			}
		}
	}



	/**
	 * Check that use of iterators means that we cover all pixels and the index returned takes account fo spaces between chips
	 */
	@Test
	public void testPixelIterator(){
		ChipSet cs2 = new ChipSet(2, 2);
		long maxIndex=-1;
		long numPixels=0;
		for( Chip chip : cs2.getChips()){
			Iterator<Long> pixelIndexIterator = chip.getPixelIndexIterator();
			while( pixelIndexIterator.hasNext()){
				maxIndex = Math.max(maxIndex, pixelIndexIterator.next());
				numPixels ++;
			}
		}
		assertEquals(265224, maxIndex); // 4 chips with layout given by chipset spacing
		assertEquals(262144, numPixels); // 4 chips * 256 *256
	}
}
