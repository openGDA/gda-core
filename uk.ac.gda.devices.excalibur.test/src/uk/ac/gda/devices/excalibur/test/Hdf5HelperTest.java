package uk.ac.gda.devices.excalibur.test;

import org.junit.Assert;
import org.junit.Test;

import uk.ac.diamond.scisoft.analysis.dataset.IntegerDataset;
import uk.ac.gda.devices.excalibur.equalization.ExcaliburEqualizationHelper;
import uk.ac.gda.devices.excalibur.equalization.Hdf5Helper;

/**
 * @author rsr31645
 * 
 */
public class Hdf5HelperTest {

	/**
	 * 
	 */
	@Test
	public void testWriteHdf5File() throws Exception {

		int[] data = { 0, 6, 3, 6, 3, 5, 1, 6, 7, 3, 4, 5, 4, 4, 2, 4, 5, 6, 5,
				8, 4, 3, 4, 5 };
		int[] pixels = { 1, 1, 1, 1, 1, 1 };
		int[] actuals = ExcaliburEqualizationHelper.INSTANCE.getEqualizedData(
				data, new int[] { 2, 3, 4 }, pixels, 4, 2, true);

		IntegerDataset ds = new IntegerDataset(actuals, new int[] { 2, 3 });
		ds.setName("testDataSet");

		boolean success = Hdf5Helper.INSTANCE.writeToFile(ds,
				"/scratch/testarea/test1.hdf5", "testgroup");

		Assert.assertTrue("Not successful", success);

	}

	@Test
	public void testReadHdf5File() throws Exception {

		int[] data = { 0, 6, 3, 6, 3, 5, 1, 6, 7, 3, 4, 5, 4, 4, 2, 4, 5, 6, 5,
				8, 4, 3, 4, 5 };
		int[] pixels = { 1, 1, 1, 1, 1, 1 };
		int[] actuals = ExcaliburEqualizationHelper.INSTANCE.getEqualizedData(
				data, new int[] { 2, 3, 4 }, pixels, 4, 2, true);

		IntegerDataset ds = new IntegerDataset(actuals, new int[] { 2, 3 });
		ds.setName("testDataSet");

		IntegerDataset readDataSet = Hdf5Helper.INSTANCE.readDataSet(
				"/scratch/testarea/test1.hdf5", "testgroup", "testDataSet",
				new int[] { 2, 3 });
		junit.framework.Assert.assertEquals("Dataset not as expected", ds,
				readDataSet);

	}

}
