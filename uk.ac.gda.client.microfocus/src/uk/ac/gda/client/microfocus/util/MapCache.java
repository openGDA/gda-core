/*-
 * Copyright © 2014 Diamond Light Source Ltd.
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

package uk.ac.gda.client.microfocus.util;

import java.util.HashMap;
import java.util.List;

import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetException;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.DoubleDataset;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;

import uk.ac.gda.beans.DetectorROI;


/**
 * Holds in memory data from a map file, in the form which is useful for plotting in the Microfocus perpsective:
 * [element/roi][detector channel][y][x]
 */
public class MapCache {

	private double[][][][] mapdata; // element (ROI),detector channel,y,x
	private HashMap<String, Integer> roiNameMap;
	private ILazyDataset allMCAs;  // four dimensional dataset as collected: y,x,detector channel, MCA spectrum

	public MapCache(HashMap<String, Integer> roiNameMap, List<? extends DetectorROI>[] elementRois, ILazyDataset lazyDataset) throws DatasetException {

		this.roiNameMap = roiNameMap;
		this.allMCAs = lazyDataset;
		deriveMapData(elementRois);
	}

	public double[][] getMap(String element, int channel) {
		Integer elementIndex = roiNameMap.get(element);
		return mapdata[elementIndex][channel];
	}

	public double[] getSpectrum(int detectorNo, int x, int y) throws DatasetException {

		int mcaSize = allMCAs.getShape()[3];
		DoubleDataset mcaDataset = (DoubleDataset) DatasetUtils.cast(
				allMCAs.getSlice(new int[] { y, x, detectorNo, 0 }, new int[] { y + 1, x + 1, detectorNo + 1, mcaSize }, new int[] { 1, 1, 1, 1 }),
				Dataset.FLOAT64);
		return mcaDataset.getData();
	}


	private double[][][] getAllMCAForOneLine(int y) throws DatasetException {
		IDataset pointData = allMCAs.getSlice(new int[]{y,0,0,0},new int[]{y+1,allMCAs.getShape()[1],allMCAs.getShape()[2],allMCAs.getShape()[3]},null);

		int numberXPixels = pointData.getShape()[1];
		int numberChannels = pointData.getShape()[2];
		int mcaSize = pointData.getShape()[3];

		double[][][] data = new double[numberXPixels][numberChannels][mcaSize];

		for (int xIndex = 0; xIndex < numberXPixels; xIndex++) {
			for (int chaIndex = 0; chaIndex < numberChannels; chaIndex++) {
				for (int mcaIndex = 0; mcaIndex < mcaSize; mcaIndex++) {
					data[xIndex][chaIndex][mcaIndex] = pointData.getDouble(0,xIndex,chaIndex,mcaIndex);
				}
			}
		}

		return data;

	}

	private void deriveMapData(List<? extends DetectorROI>[] elementRois) throws DatasetException {

		int shape[] = allMCAs.getShape();
		int numY = shape[0];
		int numX = shape[1];
		int numberChannels = shape[2];

		// TODO could speed this up by getting the complete slice for each roi...

		mapdata = new double[roiNameMap.size()][numberChannels][numY][numX];
		for (int yIndex = 0; yIndex < numY; yIndex++) {

//			System.out.println(yIndex);
			double[][][] buffer = getAllMCAForOneLine(yIndex);
//			logger.info("Reading Nexus Xmap line " + yIndex);

			for (int xIndex = 0; xIndex < numX; xIndex++) {

				// this should not be too much to hold in memory
//				double[][] buffer = getAllMCAForOnePoint(xIndex,yIndex);

				for (int chaIndex = 0; chaIndex < numberChannels; chaIndex++) {
					List<? extends DetectorROI> roiList = elementRois[chaIndex];
					for (DetectorROI roi : roiList) {
						Integer elementIndex = roiNameMap.get(roi.getRoiName());
						int windowStart = roi.getRoiStart();
						int windowEnd = roi.getRoiEnd();
						int roiCount = 0;
						for (int mcaChannel = windowStart; mcaChannel <= windowEnd; mcaChannel++) {
							roiCount += buffer[xIndex][chaIndex][mcaChannel];
						}
						mapdata[elementIndex][chaIndex][yIndex][xIndex] = roiCount;
					}
				}
			}
		}
	}
}
