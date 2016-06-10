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

import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.dataset.ILazyDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetUtils;
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

	public XmapBufferedHdf5FileLoader(String fileName) {
		super();
		this.fileName = fileName;
	}

	@Override
	public void loadFile() throws Exception {
		hdf5Loader = new HDF5Loader(fileName);
		dataHolder = hdf5Loader.loadFile();
		lazyDataset = dataHolder.getLazyDataset("/entry/instrument/detector/data");
	}

	@Override
	public short[][] getData(int dataPointNumber) {
		lazyDataset = dataHolder.getLazyDataset("/entry/instrument/detector/data");
		int channelNumbers = lazyDataset.getShape()[2];
		int numberOfDetectorElements = lazyDataset.getShape()[1];
		IDataset slice;
		try {
			slice = lazyDataset.getSlice(new int[] { dataPointNumber, 0, 0 }, new int[] { dataPointNumber + 1,
					numberOfDetectorElements, channelNumbers }, new int[] { 1, 1, 1 });
		} catch (Exception e) {
			logger.error("Could not get data from lazy dataset", e);
			return null;
		}
		IDataset sqSlice = slice.squeeze();
		int[] data = (int[]) DatasetUtils.cast(sqSlice, Dataset.INT32).getBuffer();
		short allData[][] = new short[numberOfDetectorElements][channelNumbers];
		for (int i = 0; i < numberOfDetectorElements; i++) {
			for (int j = 0; j < channelNumbers; j++) {
				allData[i][j] = (short) data[i * numberOfDetectorElements + j];
			}
		}
		return allData;
	}

	@Override
	public short[][][] getData(int fromDataPointNumber, int toDataPointNumber) throws Exception {
		lazyDataset = dataHolder.getLazyDataset("/entry/instrument/detector/data");
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
		IDataset slice = lazyDataset.getSlice(new int[] { fromDataPointNumber, 0, 0 }, new int[] { toDataPointNumber,
				numberOfDetectorElements, channelNumbers }, new int[] { 1, 1, 1 });
		IDataset sqSlice = slice.squeeze();
		int[] data = (int[]) DatasetUtils.cast(sqSlice, Dataset.INT32).getBuffer();
		int noDataPtsToRead = (toDataPointNumber - fromDataPointNumber) + 1;
		short allData[][][] = new short[noDataPtsToRead][numberOfDetectorElements][channelNumbers];
		for (int k = 0; k < noDataPtsToRead; k++) {
			for (int i = 0; i < numberOfDetectorElements; i++) {
				for (int j = 0; j < channelNumbers; j++) {
					allData[k][i][j] = (short) data[k * noDataPtsToRead + i * numberOfDetectorElements + j];
				}
			}
		}
		return allData;
	}

	@Override
	public int getNumberOfDataPoints() {
		return lazyDataset.getShape().length;
	}

	@Override
	public double getTrigger(int dataPointNumber, int element) {
		return getDouble("/entry/instrument/detector/NDAttributes/triggers_ch_" + element, dataPointNumber);
	}

	@Override
	public double getRealTime(int dataPointNumber, int element) {
		return getDouble("/entry/instrument/detector/NDAttributes/real_time_ch_" + element, dataPointNumber);
	}

	@Override
	public double getLiveTime(int dataPointNumber, int element) {
		return getDouble("/entry/instrument/detector/NDAttributes/trigger_live_time_ch_" + element, dataPointNumber);
	}

	@Override
	public double getEvents(int dataPointNumber, int element) {
		return getDouble("/entry/instrument/detector/NDAttributes/events_ch_" + element, dataPointNumber);
	}

	private double getDouble(String dataPath, int dataPointNumber) {
		lazyDataset = dataHolder.getLazyDataset(dataPath);
		IDataset slice;
		try {
			slice = lazyDataset.getSlice(new int[] { dataPointNumber }, new int[] { dataPointNumber + 1 },
					new int[] { 1 });
		} catch (Exception e) {
			logger.error("Could not get data from lazy dataset", e);
			return Double.NaN;
		}
		return slice.getDouble(0);
	}
}
