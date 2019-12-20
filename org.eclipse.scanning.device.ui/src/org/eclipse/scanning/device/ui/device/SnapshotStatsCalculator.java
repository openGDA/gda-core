/*-
 * Copyright © 2017 Diamond Light Source Ltd.
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

package org.eclipse.scanning.device.ui.device;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.IndexIterator;
import org.eclipse.january.dataset.Slice;

/**
 * Collection of methods returning simple statistics from a detector snapshot IDataset
 */
public class SnapshotStatsCalculator {

	public double calculateTotalCount(IDataset dataset) {
		double result = 0;
		final int[] shape = dataset.getShape();
		if (shape.length > 0) {
			final int numSlices = dataset.getShape()[0];
			for (int i=0; i < numSlices; i++) {
				result+= calculateSliceCount(dataset, i);
			}
		}
		return result;
	}

	public double calculateSliceCount(IDataset dataset, int index) {
		Dataset data = DatasetUtils.convertToDataset(dataset.getSliceView(new Slice(index, index+1, 1)));
		IndexIterator iterator = data.getIterator();
		double val = 0;
		while (iterator.hasNext()) {
			double value = data.getElementDoubleAbs(iterator.index);
			if (!Double.isNaN(value)) {
				val+=value;
			}
		}
		return val;
	}

	public double calculateStdDev(IDataset dataset) {
		double sumOfSquaresOfPixelMinusMean = 0;
		double mean = calculateMean(dataset);

		Dataset data = DatasetUtils.convertToDataset(dataset);
		IndexIterator iterator = data.getIterator();
		while (iterator.hasNext()) {
			double pixel = data.getElementDoubleAbs(iterator.index);
			if (!Double.isNaN(pixel)) {
				sumOfSquaresOfPixelMinusMean += Math.pow((pixel-mean),2);
			}
		}
		return Math.sqrt(sumOfSquaresOfPixelMinusMean / dataset.getSize());
	}

	public double calculateMean(IDataset dataset) {
		return calculateTotalCount(dataset)/dataset.getSize();
	}

	/**
	 * @param dataset (2D)
	 * @param tolerance
	 * @return number of points in dataset with values > tolerance
	 */
	public int countBadPoints(IDataset dataset, double tolerance) {
		int saturated = 0;
		Dataset data = DatasetUtils.convertToDataset(dataset);
		IndexIterator iterator = data.getIterator();
		while (iterator.hasNext()) {
			if (data.getElementDoubleAbs(iterator.index) > tolerance) saturated++;
		}
		return saturated;
	}

	public Number findMaximumIntensity(IDataset dataset) {
		return dataset.max(true);
	}

	public int[] findMaximumPosition(IDataset dataset) {
		return dataset.maxPos(true);
	}

	public Number findMinimumIntensity(IDataset dataset) {
		return dataset.min(true);
	}

	public int[] findMinimumPosition(IDataset dataset) {
		return dataset.minPos(true);
	}

}
