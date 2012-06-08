package uk.ac.gda.devices.excalibur.test;

import java.util.Arrays;

import org.junit.Test;

import uk.ac.gda.devices.excalibur.equalization.ExcaliburEqualizationHelper;

/**
 * @author rsr31645
 * 
 */
public class EqualizationAnalysisTest {

	@Test
	public void testEqualization2ForwardsDim2() throws Exception {

		int[] data = { 0, 6, 3, 6, 3, 5, 1, 6, 7, 3, 4, 5, 4, 4, 2, 4, 5, 6, 5,
				8, 4, 3, 4, 5 };
		int[] pixels = { 1, 0, 0, 0, 0, 0 };
		int[] actuals = ExcaliburEqualizationHelper.INSTANCE.getEqualizedData(
				data, new int[] { 2, 3, 4 }, pixels, 4, 2, true);
		int[] expecteds = new int[6];
		Arrays.fill(expecteds, 0);
		expecteds[0] = 2;
		org.junit.Assert.assertArrayEquals(expecteds, actuals);
	}

	@Test
	public void testEqualization2BackwardsDim2() throws Exception {

		int[] data = { 0, 6, 3, 6, 3, 5, 1, 6, 7, 3, 4, 4, 4, 4, 2, 4, 5, 6, 5,
				8, 4, 3, 4, 5 };
		int[] pixels = new int[6];
		Arrays.fill(pixels, 0);
		pixels[0] = 1; // starts at data[0]
		pixels[5] = 1; // starts at data[5]
		int[] actuals = ExcaliburEqualizationHelper.INSTANCE.getEqualizedData(
				data, new int[] { 2, 3, 4 }, pixels, 4, 2, false);
		int[] expecteds = new int[6];
		Arrays.fill(expecteds, 0);
		expecteds[0] = 2;
		expecteds[5] = 1;
		org.junit.Assert.assertArrayEquals(expecteds, actuals);
	}

	@Test
	public void testEqualization2ForwardsDim2TraverseDim0() throws Exception {

		int[] data = { 0, 4, 3, 6, 3, 5, 1, 6, 7, 3, 4, 5, 4, 4, 2, 4, 5, 6, 5,
				8, 4, 3, 4, 5 };
		int[] pixels = new int[12];
		Arrays.fill(pixels, 0);
		pixels[0] = 1;
		int[] actuals = ExcaliburEqualizationHelper.INSTANCE.getEqualizedData(
				data, new int[] { 2, 3, 4 }, pixels, 4, 0, true);
		int[] expecteds = new int[12];
		Arrays.fill(expecteds, 0);
		expecteds[0] = 1;
		org.junit.Assert.assertArrayEquals(expecteds, actuals);
	}

	@Test
	public void testEqualization2ForwardsDim2TraverseDim0Pixel1()
			throws Exception {

		int[] data = { 0, 4, 3, 6, 3, 5, 1, 4, 7, 3, 4, 5, 4, 4, 2, 4, 5, 6, 5,
				8, 4, 3, 0, 4 };
		int[] pixels = new int[12];
		Arrays.fill(pixels, 0);
		pixels[0] = 1; // starts at data[0]
		pixels[1] = 1; // starts at data[2]
		pixels[11] = 1; // starts at data[22]
		int[] actuals = ExcaliburEqualizationHelper.INSTANCE.getEqualizedData(
				data, new int[] { 2, 3, 4 }, pixels, 4, 0, true);
		int[] expecteds = new int[12];
		Arrays.fill(expecteds, 0);
		expecteds[0] = 1;
		expecteds[1] = 1;
		expecteds[11] = 1;
		for (int i = 0; i < actuals.length; i++) {
			System.out.println(i + " - " + actuals[i]);
		}
		org.junit.Assert.assertArrayEquals(expecteds, actuals);
	}

}
