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

package gda.data.swmr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.dawnsci.analysis.api.io.ScanFileHolderException;
import org.eclipse.dawnsci.hdf5.HDF5File;
import org.eclipse.dawnsci.hdf5.HDF5FileFactory;
import org.eclipse.dawnsci.hdf5.HDF5Utils;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.january.dataset.Dataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import hdf.hdf5lib.H5;

/**
 * Simple class to facilitate reading of datasets from SWMR hdf file, and for determining the current shape of datasets.
 */
public class SwmrFileReader {
	private static final Logger logger = LoggerFactory.getLogger(SwmrFileReader.class);

	private String filename = "";
	private HDF5File hdfFile;

	/** Names of data in be read from the HDF file : This is map from 'user readable' data label to full path to the dataset in the file. */
	private Map<String, String> dataToRead = new HashMap<String, String>();

	public SwmrFileReader() {
	}

	public void addDatasetToRead(String label, String attribute) {
		dataToRead.put(label, attribute);
	}

	public void clearDatasetsToRead(){
		dataToRead.clear();
	}

	public void openFile(String filename) throws ScanFileHolderException {
		this.filename = filename;
		hdfFile = HDF5FileFactory.acquireFile(filename, false, true);
	}

	public boolean isFileOpen() {
		if (hdfFile==null || filename.isEmpty()) {
			return false;
		} else
			return true; // no obvious method calls in hdf5file to see if file has been opened...
	}

	public void releaseFile() throws ScanFileHolderException {
		HDF5FileFactory.releaseFile(filename);
	}

	public String getFilename() {
		return filename;
	}

	/**
	 * Get current shape of dataset in hdf file. This also updates dataset information via. a call to {@link H5#H5Drefresh(long)}.
	 * @param node
	 * @return shape of dataset
	 * @throws DeviceException
	 * @throws ScanFileHolderException
	 * @throws NexusException
	 */
	public int[] getCurrentShape(String node) throws DeviceException, ScanFileHolderException, NexusException {
		int[][] currentAndMaxshape = HDF5Utils.readDatasetShape(hdfFile, node);
		logger.debug("Shape of dataset {} : {}", node, ArrayUtils.toString(currentAndMaxshape[0]));
		return currentAndMaxshape[0];
	}

	/**
	 * Read single dataset from HDF file.
	 * @param dataNodePath
	 * @param start
	 * @param shape
	 * @param step
	 * @return dataset
	 * @throws NexusException
	 */
	public Dataset readDataset(String dataNodePath, int[] start, int[] shape, int[] step) throws NexusException {
		return HDF5Utils.readDataset(hdfFile, dataNodePath, start, shape, step, -1, -1, false);
	}

	/**
	 * Read multiple datasets from HDF file; the names and paths of datasets to be read are stored in 'dataToRead' map.
	 * @param start
	 * @param shape
	 * @param step
	 * @return List<DataSet>
	 * @throws NexusException
	 */
	public List<Dataset> readDatasets(int[] start, int[] shape, int[] step) throws NexusException {
		List<Dataset> datasets = new ArrayList<Dataset>();
		for(String datasetLabel : dataToRead.keySet()) {
			String datasetName = dataToRead.get(datasetLabel);
			Dataset dataset = readDataset(datasetName, start, shape, step);
			dataset.setName(datasetLabel);
			datasets.add(dataset);
		}
		return datasets;
	}

	/**
	 * Return maximum number of frames of data that can be read (across all datasets in 'dataToRead'}.)
	 * See {@link #getCurrentShape(String)}
	 * @return number of frames available
	 * @throws NexusException
	 * @throws DeviceException
	 * @throws ScanFileHolderException
	 */
	public int getNumAvailableFrames() throws NexusException, DeviceException, ScanFileHolderException {
		List<Integer> vals = new ArrayList<Integer>();
		for(String datasetName : dataToRead.values() ) {
			vals.add(getCurrentShape(datasetName)[0]);
		}
		return Collections.min(vals);
	}
}
