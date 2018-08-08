/*-
 * Copyright © 2010 Diamond Light Source Ltd.
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

package gda.device.detector.mythen.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class MythenSum {
	
	/**
	 * Number of channels in a single Mythen module.
	 */
	private static int CHANNELS_PER_MODULE = 1280;
	
	/**
	 * Sums the specified datasets.
	 * 
	 * @param datasets the data to sum - each element should be an array of
	 *        (angle, count, error) arrays
	 * @param numModules number of modules in the detector
	 * @param badChannels bad channel provider
	 * @param step angle step
	 * 
	 * @return summed data
	 */
	public static double[][] sum(double[][][] datasets, int numModules, BadChannelProvider badChannels, double step) {
		Map<Integer, List<AlignedData>> alignedData = alignData(datasets, numModules, badChannels, step);
		double[][] combinedData = combineData(alignedData, step);
		return combinedData;
	}
	
	/**
	 * Aligns the given data onto a 'grid' where each data point's angle is a
	 * multiple of the given step.
	 * 
	 * @param allData all processed data
	 * @param numModules number of modules in the detector
	 * @param badChannels bad channel provider
	 * @param step the angle step
	 * 
	 * @return aligned data
	 */
	static Map<Integer, List<AlignedData>> alignData(double[][][] allData, int numModules, BadChannelProvider badChannels, double step) {
		final ModuleBoundary[] boundaries = determineModuleBoundaries(numModules, badChannels);
		final int numDatasets = allData.length;
		
		Map<Integer, List<AlignedData>> alignedData = new TreeMap<Integer, List<AlignedData>>();
		
		for (int d=0; d<numDatasets; d++) {
			final double[][] dataset = allData[d];
			
			// Each module's data is aligned individually
			for (int m=0; m<numModules; m++) {
				ModuleBoundary boundary = boundaries[m];
				
				// Find min/max angle for this module's data
				final double minAngle = allData[d][boundary.first][0];
				final double maxAngle = allData[d][boundary.last][0];
				
				// Find the grid points that cover the range
				final int[] minAndMaxIndices = findMinMaxIndices(minAngle, maxAngle, step);
				final int minIndex = minAndMaxIndices[0];
				final int maxIndex = minAndMaxIndices[1];
				
				// Calculate an appropriate count for each point on the grid
				int upperChannel = boundary.first;
				for (int gridIndex=minIndex; gridIndex<=maxIndex; gridIndex++) {
					final double gridAngle = gridIndex * step;
					
					// Advance upperChannel, to satisfy this:
					//   upperChannel-1 ≤ gridAngle ≤ upperChannel
					while (dataset[upperChannel][0] < gridAngle) {
						upperChannel++;
					}
					
					// Calculate interpolated count, using the real data point either side
					final double[] lowerData = dataset[upperChannel-1];
					final double[] upperData = dataset[upperChannel];
					final double interpolatedCount = interpolate(gridAngle, lowerData, upperData);
					
					// Create new aligned data point
					AlignedData aligned = new AlignedData();
					aligned.count = interpolatedCount;
					aligned.source = d;
					
					addAlignedData(aligned, gridIndex, alignedData);
				}
			}
		}
		
		return alignedData;
	}
	
	/**
	 * Determines the first and last channel for each module, given a list of
	 * the bad channels.
	 * 
	 * @param numModules the number of modules in the detector
	 * @param badChannelProvider bad channel provider
	 * @return boundary for each module
	 */
	static ModuleBoundary[] determineModuleBoundaries(int numModules, BadChannelProvider badChannelProvider) {
		
		// determine number of bad channels for each module
		final Set<Integer> badChannels = badChannelProvider.getBadChannels();
		int[] badModulesPerChannel = new int[numModules];
		for (int badChannel : badChannels) {
			final int module = moduleForChannel(badChannel);
			badModulesPerChannel[module]++;
		}
		
		// determine channel boundaries for each module
		ModuleBoundary[] boundaries = new ModuleBoundary[numModules];
		int cumulative = -1;
		for (int module=0; module<numModules; module++) {
			boundaries[module] = new ModuleBoundary();
			boundaries[module].first  = (cumulative + 1);
			cumulative += CHANNELS_PER_MODULE - badModulesPerChannel[module];
			boundaries[module].last = cumulative;
		}
		
		return boundaries;
	}
	
	/**
	 * Returns the module number for the specified channel.
	 * 
	 * @param channel channel number (zero-based)
	 * @return module number (zero-based)
	 */
	static int moduleForChannel(int channel) {
		return channel / CHANNELS_PER_MODULE;
	}
	
	static int[] findMinMaxIndices(double minAngle, double maxAngle, double step) {
		
		// Round up/down as appropriate to satisfy this:
		//   minAngle ≤ (minIndex*step) ≤ (maxIndex*step) ≤ maxAngle
		
		int minIndex = (int) Math.ceil(minAngle / step);
		int maxIndex = (int) Math.floor(maxAngle / step);
		return new int[] {minIndex, maxIndex};
	}
	
	/**
	 * Calculates an interpolated count for the specified angle, using the two
	 * given data points.
	 * 
	 * @param gridAngle angle to calculate an interpolated count for
	 * @param lowerData lower data point; angle must be ≤ the requested angle
	 * @param upperData upper data point; angle must be ≥ the request angle
	 * @return interpolated count
	 */
	static double interpolate(double gridAngle, double[] lowerData, double[] upperData) {
		final double lowerAngle = lowerData[0];
		final double upperAngle = upperData[0];
		final double angleDiff = upperAngle - lowerAngle;
		
		final double lowerCount = lowerData[1];
		final double upperCount = upperData[1];
		final double countDiff = upperCount - lowerCount;
		
		final double angleOffset = gridAngle - lowerAngle;
		
		double interpolatedCount = lowerCount + (countDiff / angleDiff) * angleOffset;
		
		return interpolatedCount;
	}
	
	/**
	 * Adds the specified piece of aligned data to the full set of all aligned
	 * data.
	 * 
	 * @param dataPoint new data point
	 * @param gridIndex grid index for the given data point
	 * @param alignedData full set of aligned data
	 */
	static void addAlignedData(AlignedData dataPoint, int gridIndex, Map<Integer, List<AlignedData>> alignedData) {
		List<AlignedData> alignedDataForIndex = alignedData.get(gridIndex);
		if (alignedDataForIndex == null) {
			alignedDataForIndex = new ArrayList<AlignedData>();
			alignedData.put(gridIndex, alignedDataForIndex);
		}
		alignedDataForIndex.add(dataPoint);
	}
	
	/**
	 * Holds the channel boundary (first/last channel) for a module.
	 */
	static class ModuleBoundary {
		
		/** First channel in the module (inclusive). */
		int first;
		
		/** Last channel in the module (inclusive). */
		int last;
		
		@Override
		public String toString() {
			return String.format("ModuleBoundary(first=%d, last=%d)", first, last);
		}
	}
	
	/**
	 * Holds a piece of aligned data.
	 */
	static class AlignedData {
		
		/** Interpolated count for this angle. */
		double count;
		
		/** Source dataset. */
		int source;
		
		@Override
		public String toString() {
			return String.format("AlignedData(count=%f, source=%d)", count, source);
		}
	}
	
	/**
	 * Combines the given aligned data.
	 * 
	 * @param alignedData the aligned data
	 * @param step the angle step
	 * @return combined data - array of (angle, count, error) arrays
	 */
	static double[][] combineData(Map<Integer, List<AlignedData>> alignedData, double step) {
		int numPoints = alignedData.size();
		double[][] combinedData = new double[numPoints][];
		
		int maxDataPointsPerGridIndex = 0;
		for (List<AlignedData> dataPoints : alignedData.values()) {
			maxDataPointsPerGridIndex = Math.max(maxDataPointsPerGridIndex, dataPoints.size());
		}
		
		int i = 0;
		for (Map.Entry<Integer, List<AlignedData>> entry : alignedData.entrySet()) {
			final int gridIndex = entry.getKey();
			final double gridAngle = gridIndex * step;
			final List<AlignedData> dataPoints = entry.getValue();
			final double[] countAndError = sumDataPointsForGridAngle(dataPoints, maxDataPointsPerGridIndex);
			combinedData[i++] = new double[] {gridAngle, countAndError[0], countAndError[1]};
		}
		
		return combinedData;
	}
	
	/**
	 * Sums the data points for the specified grid angle, returning a count
	 * and error value.
	 * 
	 * @param dataPoints the data points for the grid angle
	 * @param maxDataPointsPerGridIndex the largest number of data points that
	 *        exist for any grid angle
	 * @return count and error
	 */
	static double[] sumDataPointsForGridAngle(List<AlignedData> dataPoints, int maxDataPointsPerGridIndex) {
		double totalCount = 0;
		for (AlignedData point : dataPoints) {
			totalCount += point.count;
		}
		totalCount *= (1.0 * maxDataPointsPerGridIndex / dataPoints.size());
		
		return new double[] {totalCount, Math.sqrt(totalCount)};
	}
	
}
