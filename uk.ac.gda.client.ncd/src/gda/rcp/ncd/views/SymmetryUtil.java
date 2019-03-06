/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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

package gda.rcp.ncd.views;

import static java.lang.Math.abs;
import static java.lang.Math.max;

import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.IntegerDataset;
import org.eclipse.january.dataset.Slice;

/** Utility class to reflect and overlay 2D datasets */
public final class SymmetryUtil {
	private SymmetryUtil() {}

	/**
	 * Reflect and overlay the given dataset with it flipped vertically.
	 * Overlap is counted from the top.
	 * @param data The original data
	 * @param overlap The offset (from the top) of the value that should be overlaid on itself
	 * @return Combined dataset
	 */
	public static Dataset reflectVertical(Dataset data, int overlap) {
 		return reflectData(data, overlap, 1);
	}

	/**
	 * Reflect and overlay the given dataset with it flipped horizontally.
	 * Overlap is counted from the left.
	 * @param data The original data
	 * @param overlap The offset (from the left) of the value that should be overlaid on itself
	 * @return Combined dataset
	 */
	public static Dataset reflectHorizontal(Dataset data, int overlap) {
		return reflectData(data, overlap, 0);
	}

	/**
	 * Reflect dataset in both directions. See {@link #reflectHorizontal(Dataset, int)} and
	 * {@link #reflectVertical(Dataset, int)}.
	 */
	public static Dataset reflectBoth(Dataset data, int vOverlap, int hOverlap) {
		return reflectHorizontal(reflectVertical(data, vOverlap), hOverlap);
	}

	private static Dataset reflectData(Dataset data, int overlap, int axis) {
		if (axis < 0 || axis > 1) throw new IllegalArgumentException("Only 2D reflections are currently supported");
		int[] dims = data.getShape(); // height, width
		int[] newDims = data.getShape();
		newDims[axis] = newLength(dims[axis], overlap);
		Slice[] slices = new Slice[] {new Slice(null, null, -1), new Slice(null, null, -1)};
		slices[1-axis] = null;
		Dataset reverse = data.getSlice(slices);

		// Ensure data is leftmost dataset
		if (overlap < dims[axis] / 2) {
			Dataset tmp = data;
			data = reverse;
			reverse = tmp;
		}
		Dataset combined = DatasetFactory.zeros(IntegerDataset.class, newDims);
		int offset = abs(2 * overlap - (dims[axis]-1));
		for (int row=0; row<newDims[0]; row++) {
			for (int col=0; col<newDims[1]; col++) {
				int[] current = new int[] {row, col};
				int[] rCurrent = current.clone();
				rCurrent[axis] = current[axis] - offset;
				if (current[axis] < offset) {
					combined.set(data.getInt(row, col), row, col);
				} else if (current[axis] < dims[axis]) {
					int o = data.getInt(current);
					int r = reverse.getInt(rCurrent);
					combined.set(combineValues(o, r), row, col);
				} else {
					combined.set(reverse.getInt(rCurrent), row, col);
				}
			}
		}
		return combined;
	}

	/**
	 * Combine the two values where the datasets overlap
	 *
	 * Where the reflected datasets overlap, the resulting value is
	 * <ul>
	 * <li>The average of the two values if they are both positive</li>
	 * <li>The larger of the two if either is negative.</li>
	 * </ul>
	 *
	 * @param o original value
	 * @param r reflected value
	 * @return The result of combining two values
	 */
	private static int combineValues(int o, int r) {
		if (o < 0 || r < 0) {
			return max(o, r);
		} else {
			return (o+r)/2;
		}
	}

	/**
	 * Calculate the new size of a dataset when it is reflected and overlaid on itself so that
	 * the value in the offset position is overlaid on itself.
	 * <br>
	 * eg original=8, offset=6, result=13
	 * <pre>
	 *     0 1 2 3 4 5 6 7
	 *               7 6 5 4 3  2  1  0
	 *     0 1 2 3 4 5 6 7 8 9 10 11 12
	 * <pre>
	 * @param original The size of the original (and therefore reflected) dataset.
	 * @param offset The index of the value that is overlaid on itself.
	 * @return The new size of the dataset
	 */
	private static int newLength(int original, int offset) {
		if (offset < 0 || offset >= original) {
			throw new IllegalArgumentException("Overlap offset out of range (0-" + (original-1) + ")");
		}
		int left = offset;
		int right = original - offset - 1;
		return 2 * max(left, right) + 1;
	}

}
