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

package uk.ac.gda.analysis.hdf5;

import java.io.File;
import java.util.Arrays;
import java.util.Vector;

import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.dataset.ILazyDataset;
import org.junit.BeforeClass;
import org.junit.Test;

import gda.TestHelpers;
import gda.util.TestUtils;
import hdf.object.Datatype;
import hdf.object.h5.H5Datatype;
import junit.framework.Assert;
import uk.ac.gda.analysis.hdf5.Hdf5Helper.TYPE;

/**
 *
 */
public class Hdf5HelperTest {
	private static final String ENTRY1_GROUP = "entry1/excalibur_summary_ad";
	private static final String EXCALIBUR_EQUALIZATION_HELPER_TEST_12998_NXS = "ExcaliburEqualizationHelperTest/12998.nxs";
	static String TestFileFolder;
	@BeforeClass
	static public void setUpClass() {
		TestFileFolder = TestUtils.getGDALargeTestFilesLocation()+File.separator;
		if( TestFileFolder == null){
			Assert.fail("TestUtils.getGDALargeTestFilesLocation() returned null - test aborted");
		}
	}
	/**
	 * @throws Exception
	 *
	 */
	@Test
	public void testReadDataSetForMemoryLeak() throws Exception {
		long before = System.currentTimeMillis();
		for (int i = 0; i < 10; i++) {
			Hdf5HelperData helperData = Hdf5Helper.getInstance().readDataSetAll(TestFileFolder + EXCALIBUR_EQUALIZATION_HELPER_TEST_12998_NXS,
					ENTRY1_GROUP, "data", true);
			Assert.assertEquals(Hdf5Helper.getInstance().lenFromDims(helperData.dims), 41000000);
			Assert.assertEquals(92356,((int[])(helperData.data))[41000000 -1]);
		}
		System.out.print("readDataSetAll time taken = " + (System.currentTimeMillis()-before));
		System.out.println();
	}

	@Test
	public void testReadAllDatasetsNames() throws Exception {
		String[] actual = Hdf5Helper.getInstance().getListOfDatasets(TestFileFolder + EXCALIBUR_EQUALIZATION_HELPER_TEST_12998_NXS,
				ENTRY1_GROUP);
		Assert.assertTrue(Arrays.deepEquals(new String[]{"count_time", "data", "excalibur_summary_ad_axis1", "excalibur_summary_ad_axis2", "threshold0"},actual));
	}


	@Test
	public void testReadDataSetHyperSlab() throws Exception {
		Hdf5HelperData data2 = Hdf5Helper.getInstance().readDataSetAll(TestFileFolder + EXCALIBUR_EQUALIZATION_HELPER_TEST_12998_NXS,ENTRY1_GROUP, "data",false);
		long [] dims = data2.dims;
		long[] sstart = new long[dims.length];
		long[] sstride = new long[dims.length];
		long[] dsize = new long[dims.length];
		long[] block=null;
		int length=1;
		for( int i=0;i<dims.length;i++){
			sstart[i]=0;
			sstride[i]=1;
			dsize[i] = dims[i];
			length *= dims[i];
		}
		long[] data_maxdims = null;
		long[] data_dims = new long[]{length};
		long mem_type_id = data2.native_type;
		int [] data = (int[]) H5Datatype.allocateArray(mem_type_id, length);

		long before = System.currentTimeMillis();
		for (int i = 0; i < 2; i++) {
			Hdf5HelperData helperData = Hdf5Helper.getInstance().readDataSet(TestFileFolder + EXCALIBUR_EQUALIZATION_HELPER_TEST_12998_NXS,ENTRY1_GROUP,
					"data", sstart, sstride, dsize, block, data_maxdims, // = null
					data_dims,
					mem_type_id,
					data,
					true);
			Assert.assertEquals(Hdf5Helper.getInstance().lenFromDims(helperData.dims), 41000000);
			Assert.assertEquals(92356,((int[])(helperData.data))[41000000 -1]);

		}
		System.out.print("readDataSet time taken = " + (System.currentTimeMillis()-before));
		System.out.println();
	}


	@Test
	public void testReadDataSetHyperSlabMemDimsEqualsDSDims() throws Exception {
		Hdf5HelperData data2 = Hdf5Helper.getInstance().readDataSetAll(TestFileFolder + EXCALIBUR_EQUALIZATION_HELPER_TEST_12998_NXS,ENTRY1_GROUP, "data",false);
		long [] dims = data2.dims;
		long[] sstart = new long[dims.length];
		long[] sstride = new long[dims.length];
		long[] dsize = new long[dims.length];
		long[] block=null;
		for( int i=0;i<dims.length;i++){
			sstart[i]=0;
			sstride[i]=1;
			dsize[i] = dims[i];
		}
		long[] data_maxdims = null;
		long[] data_dims = dims;
		long mem_type_id = data2.native_type;

		long before = System.currentTimeMillis();
		int []data = (int[]) Hdf5Helper.getInstance().AllocateMemory(mem_type_id, data_dims);
		for (int i = 0; i < 2; i++) {
			Hdf5HelperData helperData = Hdf5Helper.getInstance().readDataSet(TestFileFolder + EXCALIBUR_EQUALIZATION_HELPER_TEST_12998_NXS,ENTRY1_GROUP, "data", sstart, sstride, dsize, block, data_maxdims, // = null
					data_dims,
					mem_type_id,
					data,
					true);
			Assert.assertEquals(Hdf5Helper.getInstance().lenFromDims(helperData.dims), 41000000);
			Assert.assertEquals(92356,((int[])(helperData.data))[41000000 -1]);
		}
		System.out.print("readDataSet time taken = " + (System.currentTimeMillis()-before));
		System.out.println();
	}

	@Test
	public void testReadDataSetHyperSlabSplit() throws Exception {
		Hdf5HelperData data2 = Hdf5Helper.getInstance().readDataSetAll(TestFileFolder + EXCALIBUR_EQUALIZATION_HELPER_TEST_12998_NXS,ENTRY1_GROUP, "data",false);
		//split into stripes
		long [] dims = data2.dims;
		long[] sstart = new long[dims.length];
		long[] sstride = new long[dims.length];
		long[] data_dims = new long[dims.length];
		long[] block=null;
		long[] data_maxdims = null;
		for( int i=0;i<dims.length;i++){
			sstart[i]=0;
			sstride[i]=1;
			data_dims[i] = dims[i];
		}

		int sizeOfSlice = 1000;
		sstart[dims.length-1]=0;
		System.out.print("sizeOfslice = " + sizeOfSlice);
		System.out.println();
		data_dims[dims.length-1]=sizeOfSlice;
		long before = System.currentTimeMillis();
		long mem_type_id = data2.native_type;
		int []data = (int[])  Hdf5Helper.getInstance().AllocateMemory(mem_type_id, data_dims);
		@SuppressWarnings("unused")
		Hdf5HelperData helperData=null;
		while( sstart[dims.length-1] < dims[dims.length-1]){

			helperData = Hdf5Helper.getInstance().readDataSet(TestFileFolder + EXCALIBUR_EQUALIZATION_HELPER_TEST_12998_NXS,ENTRY1_GROUP, "data",
					sstart, sstride, data_dims, block, data_maxdims,
					data_dims,
					mem_type_id,
					data,
					true);
			sstart[dims.length-1]+=sizeOfSlice;
		}
//		Assert.assertEquals(Hdf5Helper.lenFromDims(helperData.dims), 4100000);
//		Assert.assertEquals(92356,((int[])(helperData.data))[4100000 -1]);
		System.out.print("readDataSet time taken = " + (System.currentTimeMillis()-before));
		System.out.println();


	}

	/**
	 * Test method for {@link Hdf5Helper#createDataSet(Hdf5HelperData, boolean)}
	 * @throws Exception
	 *
	 */
	@Test
	public void testCreateDataSet() throws Exception  {
		for (int i = 0; i < 10; i++) {
			Hdf5HelperData readDataSet = Hdf5Helper.getInstance().readDataSetAll(TestFileFolder + EXCALIBUR_EQUALIZATION_HELPER_TEST_12998_NXS,ENTRY1_GROUP, "data",true);
			Hdf5Helper.getInstance().createDataSet(readDataSet, false);
		}
	}

	@Test
	public void testWriteToFileUsingLocations() throws Exception {
		String testScratchDirectoryName = TestHelpers.setUpTest(Hdf5HelperTest.class, "testWriteToFileUsingLocations", true);

		Hdf5HelperData readDataSet = Hdf5Helper.getInstance().readDataSetAll(TestFileFolder + EXCALIBUR_EQUALIZATION_HELPER_TEST_12998_NXS,ENTRY1_GROUP, "data", true);
		HDF5HelperLocations location = new HDF5HelperLocations();
		location.add( new HDF5NexusLocation("entry1", "NXentry"));
		location.add( new HDF5NexusLocation("default", "NXdata"));
		Hdf5Helper.getInstance().writeToFile(readDataSet, testScratchDirectoryName + "/19.nxs",
				location, "det1", null, null, null);
	}

	@Test
	public void testWriteToExtendibleFile() throws Exception {
		String testScratchDirectoryName = TestHelpers.setUpTest(Hdf5HelperTest.class, "testWriteToExtendibleFile", true);

		int[] data = new int[100];
		Arrays.fill(data, 1);
		long[] dims = new long[]{1,data.length};
		Hdf5HelperData data2 = new Hdf5HelperData(dims, data);
		boolean[] extendible = new boolean[data2.dims.length];
		Arrays.fill(extendible, false);
		long[] chunk_dims = data2.dims;
		extendible[0] = true;
		long[] offset = new long[data2.dims.length];
		Arrays.fill(offset, 0);

		String fileName = testScratchDirectoryName + "/19.nxs";
		HDF5HelperLocations location = new HDF5HelperLocations();
		location.add( new HDF5NexusLocation("entry1", "NXentry"));

		Hdf5Helper.getInstance().writeToFile(data2, fileName,location, "data2",chunk_dims, extendible, offset);
		offset[0]=1;
		Arrays.fill(data, 2);

		Hdf5Helper.getInstance().writeToFile(data2, fileName, location, "data2",chunk_dims, extendible, offset);
		Hdf5HelperData readDataSet = Hdf5Helper.getInstance().readDataSetAll(fileName,"entry1", "data2", true);
		Assert.assertEquals(readDataSet.dims[0], 2);
		Assert.assertEquals(((int[])readDataSet.data).length, 200);
		Assert.assertEquals(((int[])readDataSet.data)[99], 1);
		Assert.assertEquals(((int[])readDataSet.data)[199], 2);
	}

	@Test
	public void testReadAttrib0() throws Exception {
		String result = Hdf5Helper.getInstance().readAttributeAsString(TestFileFolder + "Hdf5HelperTest/1.nxs",
				TYPE.GROUP, "entry1/instrument/excalibur_summary_ad", "dim0");
		Assert.assertTrue("attrib value is not threshold0 but is " + result, "threshold0".equals(result));
	}

	@Test
	public void testReadAttrib1() throws Exception {
		String result = Hdf5Helper.getInstance().readAttributeAsString(TestFileFolder + "Hdf5HelperTest/1.nxs",
				TYPE.GROUP, "entry1/instrument/excalibur_summary_ad", "dim1");
		Assert.assertTrue("attrib value is not excalibur_summary_ad_axis1 but is " + result,
				"excalibur_summary_ad_axis1".equals(result));
	}

	@Test
	public void testReadAttrib2() throws Exception {
		String result = Hdf5Helper.getInstance().readAttributeAsString(TestFileFolder + "Hdf5HelperTest/1.nxs",
				TYPE.GROUP, "entry1/instrument/excalibur_summary_ad", "dim2");
		Assert.assertTrue("attrib value is not excalibur_summary_ad_axis2 but is " + result,
				"excalibur_summary_ad_axis2".equals(result));
	}

	@Test
	public void testReadAttributeEx() throws Exception {
		Hdf5HelperData result = Hdf5Helper.getInstance().readAttribute(TestFileFolder + "Hdf5HelperTest/1.nxs",
				TYPE.GROUP, "entry1/instrument/excalibur_summary_ad", "dim2");
		Assert.assertTrue("attrib value is not excalibur_summary_ad_axis2 but is " + result,
				"excalibur_summary_ad_axis2".equals(result.getAsString()));
	}

	@Test
	public void testHdf5HelperLazyLoader() throws Exception{
		Hdf5HelperLazyLoader loader = new Hdf5HelperLazyLoader(TestFileFolder + EXCALIBUR_EQUALIZATION_HELPER_TEST_12998_NXS,ENTRY1_GROUP, "data",false);
		ILazyDataset dataset = loader.getLazyDataSet();

		int[] start = new int[dataset.getShape().length];
		int[] stop = new int[dataset.getShape().length];
		for( int i=0; i< start.length; i++){
			start[i]=0;
			stop[i]=10;
		}
		IDataset slice = dataset.getSlice(start, stop, null);
		int[] pos = new int[dataset.getShape().length];
		for( int i=0; i< pos.length; i++){
			pos[i]=2;
		}

		double double1 = slice.getDouble(pos);
		Assert.assertEquals(10516., double1, 1e-6);
	}

	@Test
	public void testWriteAttribute() throws Exception{
		Hdf5HelperData helperData = new Hdf5HelperData(new long[]{1}, new double[]{1.0});
		String testScratchDirectoryName = TestHelpers.setUpTest(Hdf5HelperTest.class, "testWriteAttribute", true);

		HDF5HelperLocations attr_location = new HDF5HelperLocations("attrGroup");
		Hdf5Helper.getInstance().writeAttribute( testScratchDirectoryName + "/1.hdf", TYPE.GROUP,attr_location , "attr", helperData);
		Hdf5HelperData data = Hdf5Helper.getInstance().readAttribute( testScratchDirectoryName + "/1.hdf", TYPE.GROUP, "attrGroup", "attr");
		Assert.assertEquals(1.0, ((double[])data.data)[0]);
	}

	@Test
	public void testWriteAttributeOnDataSet() throws Exception{
		String testScratchDirectoryName = TestHelpers.setUpTest(Hdf5HelperTest.class, "testWriteAttributeOnDataSet", true);

		int[] data = new int[100];
		Arrays.fill(data, 1);
		long[] dims = new long[]{1,data.length};
		Hdf5HelperData data2 = new Hdf5HelperData(dims, data);
		String fileName = testScratchDirectoryName + "/test.hdf";
		Hdf5Helper hdf = Hdf5Helper.getInstance();
		HDF5HelperLocations location = new HDF5HelperLocations();
		location.add( new HDF5NexusLocation("entry1", "NXentry"));
		hdf.writeToFile(data2,fileName , location, "data2", null, null, null);

		Hdf5HelperData signalData = new Hdf5HelperData(new long[]{1}, new int[]{1});

		HDF5HelperLocations attr_location = new HDF5HelperLocations();
		attr_location.add( "entry1");
		attr_location.add( "data2");

		hdf.writeAttribute(fileName, Hdf5Helper.TYPE.DATASET, attr_location, "signal", signalData);

		Hdf5HelperData data1 = hdf.readAttribute( fileName, TYPE.DATASET, "entry1/data2", "signal");
		Assert.assertEquals(1, ((int[])data1.data)[0]);
	}

	@Test
	public void testConcatenateDataSetsFromFiles() throws Exception{
		String testScratchDirectoryName = TestHelpers.setUpTest(Hdf5HelperTest.class, "testConcatenateDataSetsFromFiles", true);

		Vector<String> fileNames = new Vector<String>();
		HDF5HelperLocations location = new HDF5HelperLocations();
		location.add( new HDF5NexusLocation("entry1", "NXentry"));
		for(int i=0; i< 8; i++){
			int[] data = new int[100];
			Arrays.fill(data, i);
			long[] dims = new long[]{1,data.length};
			Hdf5HelperData data2 = new Hdf5HelperData(dims, data);
			String fileName = testScratchDirectoryName + "/" + i + ".hdf";
			Hdf5Helper.getInstance().writeToFile(data2,fileName , location, "data2", null, null, null);
			fileNames.add(fileName);
		}
		String resultFileName = testScratchDirectoryName + "/concatenated.hdf";
		Hdf5Helper.getInstance().concatenateDataSetsFromFiles(fileNames, location, "data2",resultFileName);
		Hdf5HelperData readDataSet = Hdf5Helper.getInstance().readDataSetAll(resultFileName,"entry1", "data2", true);
		Assert.assertEquals(readDataSet.dims.length, 2);
		Assert.assertEquals(readDataSet.dims[0], 8);
		Assert.assertEquals(readDataSet.dims[1], 100);
		Assert.assertEquals(((int[])readDataSet.data)[799], 7);
		Assert.assertEquals(((int[])readDataSet.data)[0], 0);
	}
	@Test
	public void testConcatenateDataSets1dData() throws Exception{
		String testScratchDirectoryName = TestHelpers.setUpTest(Hdf5HelperTest.class, "testConcatenateDataSets1dData", true);
		HDF5HelperLocations location = new HDF5HelperLocations();
		location.add( new HDF5NexusLocation("entry1", "NXentry"));
		Vector<Hdf5HelperData> dataSets = new Vector<Hdf5HelperData>();
		for(int i=0; i< 8; i++){
			int[] data = new int[100];
			Arrays.fill(data, i);
			long[] dims = new long[]{data.length};
			Hdf5HelperData data2 = new Hdf5HelperData(dims, data);
			dataSets.add(data2);
		}
		String resultFileName = testScratchDirectoryName + "/concatenated.hdf";
		Hdf5Helper.getInstance().concatenateDataSets(dataSets, location, "data2",resultFileName);
		Hdf5HelperData readDataSet = Hdf5Helper.getInstance().readDataSetAll(resultFileName,"entry1", "data2", true);
		Assert.assertEquals(readDataSet.dims.length, 2);
		Assert.assertEquals(readDataSet.dims[0], 8);
		Assert.assertEquals(readDataSet.dims[1], 100);
		Assert.assertEquals(((int[])readDataSet.data)[799], 7);
		Assert.assertEquals(((int[])readDataSet.data)[0], 0);
	}

	/*
	 * Test using 1d data with dim[0] = 1
	 */
	@Test
	public void testConcatenateDataSets1dDataA() throws Exception{
		String testScratchDirectoryName = TestHelpers.setUpTest(Hdf5HelperTest.class, "testConcatenateDataSets1dDataA", true);

		HDF5HelperLocations location = new HDF5HelperLocations();
		location.add( new HDF5NexusLocation("entry1", "NXentry"));

		Vector<Hdf5HelperData> dataSets = new Vector<Hdf5HelperData>();
		for(int i=0; i< 8; i++){
			int[] data = new int[100];
			Arrays.fill(data, i);
			long[] dims = new long[]{1,data.length};
			Hdf5HelperData data2 = new Hdf5HelperData(dims, data);
			dataSets.add(data2);
		}
		String resultFileName = testScratchDirectoryName + "/concatenated.hdf";
		Hdf5Helper.getInstance().concatenateDataSets(dataSets, location, "data2",resultFileName);
		Hdf5HelperData readDataSet = Hdf5Helper.getInstance().readDataSetAll(resultFileName,"entry1", "data2", true);
		Assert.assertEquals(readDataSet.dims.length, 2);
		Assert.assertEquals(readDataSet.dims[0], 8);
		Assert.assertEquals(readDataSet.dims[1], 100);
		Assert.assertEquals(((int[])readDataSet.data)[799], 7);
		Assert.assertEquals(((int[])readDataSet.data)[0], 0);
	}

	@Test
	public void testConcatenateDataSets2dData() throws Exception{
		String testScratchDirectoryName = TestHelpers.setUpTest(Hdf5HelperTest.class, "testConcatenateDataSets2dData", true);

		HDF5HelperLocations location = new HDF5HelperLocations();
		location.add( new HDF5NexusLocation("entry1", "NXentry"));

		Vector<Hdf5HelperData> dataSets = new Vector<Hdf5HelperData>();
		for(int i=0; i< 8; i++){
			int[] data = new int[100];
			Arrays.fill(data, i);
			long[] dims = new long[]{5,20};
			Hdf5HelperData data2 = new Hdf5HelperData(dims, data);
			dataSets.add(data2);
		}
		String resultFileName = testScratchDirectoryName + "/concatenated.hdf";
		Hdf5Helper.getInstance().concatenateDataSets(dataSets, location, "data2",resultFileName);
		Hdf5HelperData readDataSet = Hdf5Helper.getInstance().readDataSetAll(resultFileName,"entry1", "data2", true);
		Assert.assertEquals(readDataSet.dims.length, 3);
		Assert.assertEquals(readDataSet.dims[0], 8);
		Assert.assertEquals(readDataSet.dims[1], 5);
		Assert.assertEquals(readDataSet.dims[2], 20);
		Assert.assertEquals(((int[])readDataSet.data)[799], 7);
		Assert.assertEquals(((int[])readDataSet.data)[0], 0);

	}

	@Test
	public void testWriteToFileSimpleString() throws Exception{
		String testScratchDirectoryName = TestHelpers.setUpTest(Hdf5HelperTest.class, "testWriteToFileSimpleString", true);

		String string = "This is a test string with a new line \nAnd an extra line?";
		Hdf5HelperData helperData = Hdf5HelperData.getInstance(string);
		String fileName = testScratchDirectoryName + "/1.hdf";
		String groupName = "entry1";
		String dataSetName = "program_name";
		Hdf5Helper.getInstance().writeToFileSimple(helperData, fileName, new HDF5HelperLocations(groupName), dataSetName);
		Hdf5HelperData readBack = Hdf5Helper.getInstance().readDataSetAll(fileName, groupName, dataSetName, true);
		String string2 = readBack.getAsString();
		Assert.assertEquals(string, string2);
		Assert.assertEquals(readBack.dims.length, 1);
		Assert.assertEquals(readBack.dims[0], 1);
		Assert.assertEquals(string.getBytes().length,readBack.h5Datatype.getDatatypeSize());
		Assert.assertEquals(Datatype.CLASS_STRING,readBack.h5Datatype.getDatatypeClass());
	}

}
