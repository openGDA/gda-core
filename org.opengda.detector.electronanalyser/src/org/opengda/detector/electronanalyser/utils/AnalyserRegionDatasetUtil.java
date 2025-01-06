/*-
 * Copyright © 2024 Diamond Light Source Ltd.
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

package org.opengda.detector.electronanalyser.utils;

import java.util.Arrays;
import java.util.stream.IntStream;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.dawnsci.nexus.NXdetector;
import org.eclipse.dawnsci.nexus.NexusConstants;
import org.eclipse.dawnsci.nexus.NexusUtils;
import org.eclipse.january.DatasetException;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.ILazyWriteableDataset;
import org.eclipse.january.dataset.SliceND;
import org.eclipse.january.dataset.SliceNDIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class AnalyserRegionDatasetUtil {

	private static final Logger logger = LoggerFactory.getLogger(AnalyserRegionDatasetUtil.class);
	public static final int[] SCALAR_SHAPE = { };

	private AnalyserRegionDatasetUtil() {}

	public static int[] calculateAxisDimensionMappings(int scanRank, int axisIndex) {
		//Calculate dimension mappings for scannables, then join index we want to use to
		return IntStream.range(0, scanRank + 1).map(i-> i < scanRank ? i : axisIndex).toArray();
	}

	public static SliceNDIterator createMultiDimensionalDatasetAndSliceIterator(
			String dataName, int[] scanDimensions, NXdetector detector, int[] dimensions, Class<?> clazz, String units) {
		return createMultiDimensionalDatasetAndSliceIterator(dataName,  scanDimensions,  detector, dimensions, clazz, units, 0);
	}

	public static SliceNDIterator createMultiDimensionalDatasetAndSliceIterator(
			String dataName, int[] scanDimensions, NXdetector detector, int[] dimensions, Class<?> clazz, String units, int extraAxesToIgnore) {
		logger.debug("Setting up data structure for data {}",  dataName);
		int[] maxShape = ArrayUtils.addAll(scanDimensions, dimensions);
		int[] axesToIgnore = IntStream.range(maxShape.length - dimensions.length + extraAxesToIgnore, maxShape.length).toArray();
		logger.debug("maxShape = {}, axesToIgnore = {}", Arrays.toString(maxShape), Arrays.toString(axesToIgnore));
		detector.initializeFixedSizeLazyDataset(dataName, maxShape, clazz);
		addUnits(dataName, detector, units);
		SliceND firstSlice = new SliceND(maxShape);
		return new SliceNDIterator(firstSlice, axesToIgnore);
	}

	public static void createOneDimensionalStructure(String dataName, NXdetector detector, int[] dimensions, Class<?> clazz) {
		createOneDimensionalStructure(dataName, detector, dimensions, clazz, null);
	}

	public static void createOneDimensionalStructure(String dataName, NXdetector detector, int[] dimensions, Class<?> clazz, String units) {
		logger.debug("Setting up data structure for data {}",  dataName);
		int[] maxShape = dimensions.clone();
		detector.initializeFixedSizeLazyDataset(dataName, maxShape, clazz);
		addUnits(dataName, detector, units);
	}

	public static void addUnits(String dataName, NXdetector detector, String units) {
		if (units != null) {
			detector.setAttribute(dataName, NexusConstants.UNITS, units);
		}
	}

	public static void writeNewPosition(ILazyWriteableDataset lazyWrittableDataset, SliceNDIterator sliceIterator, Object data) throws DatasetException {
		sliceIterator.hasNext();
		final SliceND scanSlice = sliceIterator.getCurrentSlice();
		final Dataset dataSet = data instanceof Dataset dataCasted ? dataCasted : NexusUtils.createFromObject(data, lazyWrittableDataset.getName());
		logger.debug("writeNewPosition for data \"{}\" has shape {}. This scan slice has shape {}", lazyWrittableDataset.getName(), Arrays.toString(dataSet.getShape()), Arrays.toString(scanSlice.getShape()));
		lazyWrittableDataset.setSlice(null, dataSet, scanSlice);
	}

	public static void overridePosition(ILazyWriteableDataset lazyWrittableDataset, Object data) throws DatasetException {
		Dataset dataSet = NexusUtils.createFromObject(data, lazyWrittableDataset.getName());
		SliceND scanSlice = new SliceND(lazyWrittableDataset.getMaxShape());
		logger.debug("overridePosition for data \"{}\" has shape {}. This scan slice has shape {}", lazyWrittableDataset.getName(), Arrays.toString(dataSet.getShape()), Arrays.toString(scanSlice.getShape()));
		lazyWrittableDataset.setSlice(null, dataSet, scanSlice);
	}
}
