/*-
 * Copyright © 2013 Diamond Light Source Ltd.
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

package uk.ac.gda.exafs.ui.detector;

public class Counts {
	
	public int calculateInWindowCounts(Boolean currentEditIndividual, Boolean calculateSingleElement, int[][][] detectorData, DetectorElementComposite detectorElementComposite, int currentSelectedElementIndex) {
		// use last value or store new value;
		if (currentEditIndividual == null)
			currentEditIndividual = calculateSingleElement;
		int start = (Integer) detectorElementComposite.getStart().getValue();
		int end = (Integer) detectorElementComposite.getEnd().getValue();
		return getInWindowsCounts(currentEditIndividual, start, end, currentSelectedElementIndex, detectorData);
	}
	
	public int getInWindowsCounts(Boolean currentEditIndividual, int start, int end, int currentSelectedElementIndex, int[][][] detectorData) {
		int total = 0;
		if (currentEditIndividual)
			total = sumElementInWindowCounts(start, end, currentSelectedElementIndex, detectorData);
		else
			for (int element = 0; element < detectorData.length; element++)
				total = sumElementInWindowCounts(start, end, element, detectorData);
		return total;
	}
	
	protected int sumElementInWindowCounts(int start, int end, int element, int[][][] detectorData) {
		if (start == -1)
			return 0;
		int total = 0;
		final int numGrades = detectorData[element].length;
		for (int igrade = 0; igrade < numGrades; ++igrade)
			for (int icount = start; icount <= end; ++icount) {
				if (icount >= detectorData[element][igrade].length)
					continue;
				total = total + detectorData[element][igrade][icount];
			}
		return total;
	}
	
	public int getTotalElementCounts(int elementNumber, int[][][] detectorData) {
		int sum = 0;
		for (int j = 0; j < detectorData[elementNumber].length; j++)
			for (int k = 0; k < detectorData[elementNumber][j].length; k++)
				sum += detectorData[elementNumber][j][k];
		return sum;
	}
	
	public int getTotalCounts(int[][][] detectorData) {
		int sum = 0;
		for (int i = 0; i < detectorData.length; i++)
			sum += getTotalElementCounts(i, detectorData);
		return sum;
	}
	
}