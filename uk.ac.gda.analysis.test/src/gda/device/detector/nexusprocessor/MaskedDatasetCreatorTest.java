/*-
 * Copyright © 2021 Diamond Light Source Ltd.
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

package gda.device.detector.nexusprocessor;

import static org.hamcrest.MatcherAssert.assertThat;

import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.IntegerDataset;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class MaskedDatasetCreatorTest {

	private MaskedDatasetCreator maskProc;

	@BeforeEach
	public void setUp() {
		maskProc = new MaskedDatasetCreator();
	}

	@Test
	public void testExternalMask() throws Exception {
		maskProc.setExternalMask(DatasetFactory.createFromObject(new int[] { 0, 0, 0, 0, 1, 4, 0, 0, 0 }, 3, 3));
		var frame = DatasetFactory
				.createFromObject(new int[] { 5938, 2389, 5998, 3432, 3456, 494944949, 5893, 8913, 1294 }, 3, 3);
		var processed = maskProc.createDataSet(frame);
		assertThat(processed.getInt(0, 0), Matchers.is(5938));
		assertThat(processed.getInt(1, 2), Matchers.is(0));

	}

	@Test
	public void testThresholdMask() throws Exception {
		maskProc.setThreshold(25, 45);
		var frame = DatasetFactory.createFromObject(new int[] { 10, 20, 30, 40, 50, 60, 70, 80, 90 }, 3, 3);
		var processed = maskProc.createDataSet(frame);
		assertThat(processed.sum(), Matchers.is(70));
	}


	@Test
	public void testNoMask() throws Exception {
		var frame = DatasetFactory.createFromObject(new int[] { 10, 20, 30, 40, 50, 60, 70, 80, 90 }, 3, 3);
		var processed = maskProc.createDataSet(frame);
		assertThat(processed.sum(), Matchers.is(450));
	}

	@Test
	public void testSettingPixels() throws Exception {
		// [[1,2,3,4],[....11,12]]
		var frame = DatasetFactory.createRange(IntegerDataset.class, 1, 13, 1).reshape(3,4);
		maskProc.addMaskedPixel(1, 2); // has value 7
		var processed = maskProc.createDataSet(frame);
		assertThat(processed.sum(), Matchers.is(71));
	}

	@Test
	public void testAllConditions() throws Exception {
		// E.g. a detector frame with a gap border and hot pixels
		var frame = DatasetFactory.createFromObject(new int[] {-1,-1,-1,-1,-1,-1,
				-1,2659,225,195,763,-1,
				-1,64,9999,823,2846,-1,
				-1,484,585,3500,9999,-1,
				-1,-1,-1,-1,-1,-1,
		}, 5,6);
		maskProc.setThreshold(0, 5000); // removes -1s and 9999s
		maskProc.addMaskedPixel(3, 3); // removes 3500
		var externalMask= DatasetFactory.zeros(5,6);
		// These mask the pixels in the two thousands (by setting mask dataset to anything non zero)
		externalMask.set(1, 1, 1);
		externalMask.set(1, 2, 4);
		maskProc.setExternalMask(externalMask);
		var processed = maskProc.createDataSet(frame);
		assertThat(processed.sum(), Matchers.is(3139));
	}

	@Test
	public void testMaskAdaptsToChange() throws Exception {
		var frame = DatasetFactory.ones(IntegerDataset.class,5, 5);
		frame.set(1000, 2, 3);
		maskProc.setThreshold(0, 10);
		var processed = maskProc.createDataSet(frame);
		assertThat(processed.sum(), Matchers.is(24));
		// Another out of range pixel
		frame.set(1000, 2,2);

		processed = maskProc.createDataSet(frame);
		// mask is not regenerated
		assertThat(processed.sum(), Matchers.is(1023));

		maskProc.regenerateMask();
		processed = maskProc.createDataSet(frame);
		assertThat(processed.sum(), Matchers.is(23));
	}

}
