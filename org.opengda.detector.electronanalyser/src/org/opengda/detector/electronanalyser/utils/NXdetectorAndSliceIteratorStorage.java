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

import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.dawnsci.nexus.NXdetector;
import org.eclipse.january.DatasetException;
import org.eclipse.january.dataset.ILazyWriteableDataset;
import org.eclipse.january.dataset.SliceNDIterator;

/*
 * Helper class to store NXdetector and SliceNDIterator objects with easy methods to setup, write, and append datasets.
 * Useful for a detector that implements IWritableNexusDevice<NXdetector> that wants to write data whenever it needs to.
 * @author Oli Wenman
 *
 */
public class NXdetectorAndSliceIteratorStorage {

	private Map<String, SliceNDIterator> sliceIteratorMap = new LinkedHashMap<>();
	private Map<String, NXdetector> detectorMap = new LinkedHashMap<>();

	private String joinStrings(final String string1, final String string2) {
		return string1 + "_" + string2;
	}

	/**
	 * Helper function for setting up datasets. Used to write a new position of existing data (appending data as a new dimension)
	 * @param detectorName name of the detector that the data is to be stored under.
	 * @param field name of the field that the data is to be stored as.
	 * @param scanDimensions dimensions of the scan.
	 * @param detector object to store the datasets in.
	 * @param dimensions dataset dimensions of data to set up.
	 * @param clazz the class type that the data is stored as.
	 * @param units the units of the data (if any).
	 * @param extraAxesToIgnore number of additional axes to ignore on top of the calculated default
	 */
	public void setupMultiDimensionalData(String detectorName, String dataName, int[] scanDimensions, NXdetector detector, int[] dimensions, Class<?> clazz, String units, int extraAxesToIgnore) {
		getSliceIteratorMap().put(
			joinStrings(detectorName, dataName),
			AnalyserRegionDatasetUtil.createMultiDimensionalDatasetAndSliceIterator(dataName, scanDimensions, detector, dimensions, clazz, units, extraAxesToIgnore)
		);
	}

	/**
	 * Helper function for setting up datasets. Used to write a new position of existing data (appending data as a new dimension)
	 * @param detectorName name of the detector that the data is to be stored under.
	 * @param field name of the field that the data is to be stored as.
	 * @param scanDimensions dimensions of the scan.
	 * @param detector object to store the datasets in.
	 * @param dimensions dataset dimensions of data to set up.
	 * @parama clazz the class type that the data is stored as.
	 * @parama units the units of the data (if any).
	 */
	public void setupMultiDimensionalData(String detectorName, String dataName, int[] scanDimensions, NXdetector detector, int[] dimensions, Class<?> clazz, String units) {
		getSliceIteratorMap().put(
			joinStrings(detectorName, dataName),
			AnalyserRegionDatasetUtil.createMultiDimensionalDatasetAndSliceIterator(dataName, scanDimensions, detector, dimensions, clazz, units)
		);
	}

	/**
	 * Helper function for setting up datasets. Used to write a new position of existing data (appending data as a new dimension)
	 * @param detectorName name of the detector that the data is to be stored under.
	 * @param field name of the field that the data is to be stored as.
	 * @param scanDimensions dimensions of the scan.
	 * @param detector object to store the datasets in.
	 * @param dimensions dataset dimensions of data to set up.
	 * @parama clazz the class type that the data is stored as.
	 */
	public void setupMultiDimensionalData(String detectorName, String dataName, int[] scanDimensions, NXdetector detector, int[] dimensions, Class<?> clazz) {
		setupMultiDimensionalData(detectorName, dataName, scanDimensions, detector, dimensions, clazz, null);
	}

	/**
	 * Helper function for saving data. Used to override the current position of existing data.
	 * @param detectorName name of the detector that the data is stored in.
	 * @param field name of the field that the data is stored as.
	 * @param data that you want to write.
	 */
	public void overridePosition(String detectorName, String field, Object data) throws DatasetException {
		ILazyWriteableDataset lazyWrittableDataset = getDetectorMap().get(detectorName).getLazyWritableDataset(field);
		AnalyserRegionDatasetUtil.overridePosition(lazyWrittableDataset, data);
	}

	/**
	 * Helper function for saving data. Used to write a new position of existing data (appending data as a new dimension)
	 * @param detectorName name of the detector that the data is stored in.
	 * @param field name of the field that the data is stored as.
	 * @param data that you want to write.
	 */
	public void writeNewPosition(String detectorName, String field, Object data) throws DatasetException {
		ILazyWriteableDataset lazyWrittableDataset = getDetectorMap().get(detectorName).getLazyWritableDataset(field);
		SliceNDIterator sliceIterator = getSliceIteratorMap().get(joinStrings(detectorName, field));
		AnalyserRegionDatasetUtil.writeNewPosition(lazyWrittableDataset, sliceIterator, data);
	}

	public Map<String, NXdetector> getDetectorMap() {
		return detectorMap;
	}

	public Map<String, SliceNDIterator> getSliceIteratorMap() {
		return sliceIteratorMap;
	}
}