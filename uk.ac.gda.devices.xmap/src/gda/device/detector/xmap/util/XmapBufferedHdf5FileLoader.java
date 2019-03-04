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

package gda.device.detector.xmap.util;

import java.nio.file.Paths;

import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.io.DataHolder;
import uk.ac.diamond.scisoft.analysis.io.HDF5Loader;

public class XmapBufferedHdf5FileLoader implements XmapFileLoader {

	static Logger logger = LoggerFactory.getLogger(XmapBufferedHdf5FileLoader.class);
	private HDF5Loader hdf5Loader;
	protected DataHolder dataHolder;
	protected ILazyDataset lazyDataset;
	private String fileName;
	private String detectorDataPath = "/entry/instrument/detector/data";
	private String attributeDataGroup = "/entry/instrument/NDAttributes";

	public XmapBufferedHdf5FileLoader(String fileName) {
		super();
		this.fileName = fileName;
	}

	@Override
	public void loadFile() throws Exception {
		hdf5Loader = new HDF5Loader(fileName);
		dataHolder = hdf5Loader.loadFile();
		lazyDataset = dataHolder.getLazyDataset(detectorDataPath);
	}

	@Override
	public short[][] getData(int dataPointNumber) {
		lazyDataset = dataHolder.getLazyDataset(detectorDataPath);
		int channelNumbers = lazyDataset.getShape()[2];
		int numberOfDetectorElements = lazyDataset.getShape()[1];
		Dataset slice;
		try {
			slice = (Dataset) lazyDataset.getSlice(new int[] { dataPointNumber, 0, 0 }, new int[] { dataPointNumber + 1,
					numberOfDetectorElements, channelNumbers }, new int[] { 1, 1, 1 }).squeeze();
		} catch (Exception e) {
			logger.error("Could not get data from lazy dataset", e);
			return null;
		}

		short allData[][] = new short[numberOfDetectorElements][channelNumbers];
		for (int i = 0; i < numberOfDetectorElements; i++) {
			// set all channel counts for this element by taking a slice
			allData[i] = (short[]) slice.getSlice(new int[] {i, 0}, new int[] {i+1, channelNumbers}, null).getBuffer();
		}
		return allData;
	}

	@Override
	public short[][][] getData(int fromDataPointNumber, int toDataPointNumber) throws Exception {
		lazyDataset = dataHolder.getLazyDataset(detectorDataPath);
		int numberOfAvailableDataPoints = lazyDataset.getShape()[0];
		int channelNumbers = lazyDataset.getShape()[2];
		int numberOfDetectorElements = lazyDataset.getShape()[1];
		if (lazyDataset == null || fromDataPointNumber > numberOfAvailableDataPoints
				|| toDataPointNumber >= numberOfAvailableDataPoints) {
			throw new Exception("Data not available for the requested range " + fromDataPointNumber + " - "
					+ toDataPointNumber);
		}
		if (fromDataPointNumber > toDataPointNumber) {
			int temp = toDataPointNumber;
			toDataPointNumber = fromDataPointNumber;
			fromDataPointNumber = temp;
		}
		Dataset slice = (Dataset) lazyDataset.getSlice(new int[] { fromDataPointNumber, 0, 0 }, new int[] { toDataPointNumber+1,
				numberOfDetectorElements, channelNumbers }, new int[] { 1, 1, 1 }).squeeze();

		int noDataPtsToRead = (toDataPointNumber - fromDataPointNumber) + 1;
		short allData[][][] = new short[noDataPtsToRead][numberOfDetectorElements][channelNumbers];
		for (int k = 0; k < noDataPtsToRead; k++) {
			for (int i = 0; i < numberOfDetectorElements; i++) {
				// set all channel counts for this datapoint and element by taking a slice
				allData[k][i] = (short[]) slice.getSlice(new int[] {k, i, 0}, new int[] {k+1, i+1, channelNumbers}, null).getBuffer();
			}
		}
		return allData;
	}

	@Override
	public int getNumberOfDataPoints() {
		return lazyDataset.getShape()[0];
	}

	@Override
	public double getTrigger(int dataPointNumber, int element) {
		return getAttributeValue("triggers_ch_" + element, dataPointNumber);
	}

	@Override
	public double getRealTime(int dataPointNumber, int element) {
		return getAttributeValue("real_time_ch_" + element, dataPointNumber);
	}

	@Override
	public double getLiveTime(int dataPointNumber, int element) {
		return getAttributeValue("trigger_live_time_ch_" + element, dataPointNumber);
	}

	@Override
	public double getEvents(int dataPointNumber, int element) {
		return getAttributeValue("events_ch_" + element, dataPointNumber);
	}

	private double getAttributeValue(String attributeName, int dataPointNumber) {
		String attributePath = Paths.get(attributeDataGroup, attributeName).toString();
		lazyDataset = dataHolder.getLazyDataset(attributePath);
		IDataset slice;
		try {
			slice = lazyDataset.getSlice(new int[] { dataPointNumber }, new int[] { dataPointNumber + 1 },	new int[] { 1 });
		} catch (Exception e) {
			logger.error("Could not get data from lazy dataset", e);
			return Double.NaN;
		}
		return slice.getDouble(0);
	}

	public String getDetectorDataPath() {
		return detectorDataPath;
	}

	/**
	 * Path to detector dataset in the hdf file
	 * @param detectorDataPath
	 */
	public void setDetectorDataPath(String detectorDataPath) {
		this.detectorDataPath = detectorDataPath;
	}

	public String getAttributeDataGroup() {
		return attributeDataGroup;
	}

	/**
	 * Path to group containing attribute data
	 * @param attributeDataGroup
	 */
	public void setAttributeDataGroup(String attributeDataGroup) {
		this.attributeDataGroup = attributeDataGroup;
	}
}
