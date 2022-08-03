/*-
 * Copyright © 2022 Diamond Light Source Ltd.
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

package uk.ac.gda.devices.detector.xspress4;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.dawnsci.analysis.api.io.ScanFileHolderException;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.DoubleDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.data.swmr.SwmrFileReader;
import gda.device.DeviceException;

public class XspressDataProvider {

	private static final Logger logger = LoggerFactory.getLogger(XspressDataProvider.class);

	private Xspress4Controller xspressController;
	private SwmrFileReader fileReader;

	private String pathToAttributeDataGroup = "/entry/instrument/NDAttributes/";
	private String scalerDataNameFormat = "Chan%02dSca%d";
	private String dtcFactorDataNameFormat = "Chan%02dDTCFactor";

	private List<String> datasetNames = Collections.emptyList();

	private boolean twoDHdfData;

	public boolean isTwoDHdfData() {
		return twoDHdfData;
	}

	public void setTwoDHdfData(boolean dataIsTwoD) {
		this.twoDHdfData = dataIsTwoD;
	}

	public void setFileReader(SwmrFileReader fileReader) {
		this.fileReader = fileReader;
	}

	public void setXspressController(Xspress4Controller xspressController) {
		this.xspressController = xspressController;
	}

	public void openFile(String filename) throws ScanFileHolderException {
		if (!fileReader.isFileOpen()) {
			logger.debug("Opening detector hdf file {} for reading...", filename);
			fileReader.openFile(filename);
		}
	}

	public void releaseFile() throws ScanFileHolderException {
		if (fileReader.isFileOpen()) {
			logger.debug("Releading hdf file : {}", fileReader.getFilename());
			fileReader.releaseFile();
		}
	}

	/**
	 * @return Number of frames available to read in the Swmr hdf file
	 * @see {@link SwmrFileReader#getNumAvailableFrames()}
 	 * @throws NexusException
	 */
	public int getNumAvailableHdfFrames() throws NexusException {
		int numFramesHdf = fileReader.getNumAvailableFrames();
		logger.debug("getNumFrames() : {} from Hdf file", numFramesHdf);
		return numFramesHdf;
	}

	/**
	 * Create new SwmrFileReader and setup list of dataset in hdf file to be read.
	 * i.e. the names of scaler datasets and DTC factor datasets for each detector element.
	 * e.g.. "Chan..Sca.." datasets in attribute data group.
	 *
	 * @param useSwmrReading
	 */
	public void setupSwmrFileReader(boolean useSwmrReading) {
		if (!useSwmrReading) {
			logger.debug("Not using Swmr - clearing file reader");
			fileReader = null;
			return;
		}

		datasetNames = new ArrayList<>();
		// Setup list of dataset to be read for each detector element
		if (twoDHdfData) {
			// Data is stored in 2-dimensional datasets, 1 dataset per scalar value type (shape = [numFrames, numChannels]
			for(int i=0; i<xspressController.getNumScalers(); i++) {
				datasetNames.add(getScalerName(i));
			}
			datasetNames.add(getDtcFactorDataNameFormat());
		} else {
			for (int chan = 0; chan < xspressController.getNumElements(); chan++) {
				for(int i=0; i<xspressController.getNumScalers(); i++) {
					datasetNames.add(getScalerName(chan, i));
				}
				datasetNames.add(getDeadtimeCorrectionFactorName(chan));
			}
		}

		logger.debug("Creating new Swmr file reader");
		fileReader = new SwmrFileReader();

		logger.debug("Datasets to be read : ");
		datasetNames.forEach(n -> {
			String pathToData = pathToAttributeDataGroup+n;
			fileReader.addDatasetToRead(n, pathToData);
			logger.debug("{}", pathToData);
		});
	}

	/**
	 * Read scaler data from detector SWMR file or array PVs across specified range of frames.<p>
	 * Scaler data for each channel is its own dataset with shape = [numFrames, numScalers]
	 *
	 * @param lowFrame
	 * @param highFrame
	 * @return List of datasets, one per scalar type (shape = [num frames, num channels]
	 * @throws NexusException
	 * @throws DeviceException
	 * @throws ScanFileHolderException
	 */
	public List<Dataset> getScalerData(int lowFrame, int highFrame) throws NexusException, DeviceException {
		logger.info("getDatasets called : lowFrame = {}, highFrame = {}", lowFrame, highFrame);

		List<Dataset> allDatasets = null;

		if (fileReader == null) {
			logger.info("SwmrFileReader has not been set - using PVs to get scaler data");
			allDatasets = getScalerDataFromPvs(lowFrame, highFrame);
		} else {
			logger.info("Using SwmrFileReader to get scaler data");
			allDatasets = getScalerDataFromSwmr(lowFrame, highFrame);
		}

		logger.info("{} datasets of scaler values collected", allDatasets.size());
		return allDatasets;
	}


	/**
	 * Read scaler data from SWMR file for range of frames for all detector elements/channels.
	 *
	 * @param lowFrame
	 * @param highFrame
	 * @return List of datasets (1 per scalar, Shape [numFrames, numChannels]
	 * @throws NexusException
	 */
	private List<Dataset> getScalerDataFromSwmr(int lowFrame, int highFrame) throws NexusException {
		int[] start = new int[] { lowFrame };
		int[] shape = new int[] { highFrame - lowFrame + 1 };
		int[] step = new int[] { 1 };

		int numFrames = shape[0];
		logger.info("Getting scaler values from SWMR file for frames {} to {}", lowFrame, highFrame);

		int numScalers = xspressController.getNumScalers();
		int numChannels = xspressController.getNumElements();

		// Read shape of the first dataset and see if scaler data is stored in 2d blocks
		int[] dataShape = fileReader.getCurrentShape(pathToAttributeDataGroup+datasetNames.get(0));
		boolean dataIsTwoD = dataShape.length == 2;

		logger.debug("Reading data two dimension scaler datasets : {}", dataIsTwoD);

		List<Dataset> allDatasets = new ArrayList<>();
		for(int j=0; j<numScalers; j++) {
			Dataset dataForChannel;
			if (dataIsTwoD) {
				dataForChannel = fileReader.readDataset(getScalerDataPath(j), new int[] {lowFrame, 0}, new int[] {numFrames, numChannels}, new int[] {1,1});
			} else {
				dataForChannel = DatasetFactory.zeros(numFrames, numChannels);
				// Read scaler type across all channels into 2d dataset
				for(int i=0; i<xspressController.getNumElements(); i++) {
					String scalerDataPath = getScalerDataPath(i, j);
					Dataset scalerData = fileReader.readDataset(scalerDataPath, start, shape, step);
					scalerData.setShape(numFrames, 1);
					dataForChannel.setSlice(scalerData, new int[] {0, i}, new int[] {numFrames, i+1}, null);
				}
			}
			allDatasets.add(dataForChannel);
		}
		return allDatasets;
	}

	/**
	 * Read scaler time series arrays from Epics PVs for all detector elements
	 * @param lowFrame
	 * @param highFrame
	 * @return List of datasets (1 per detector channel/element). Shape [numFrames, numScalers]
	 * @throws DeviceException
	 */
	private List<Dataset> getScalerDataFromPvs(int lowFrame, int highFrame) throws DeviceException {
		logger.info("Getting scaler values from PVs for frames {} to {}", lowFrame, highFrame);
		List<Dataset> datasets = new ArrayList<>();
		for (int i = 0; i < xspressController.getNumElements(); i++) {
			datasets.add(getScalerTimeseriesData(i, lowFrame, highFrame));
		}
		return datasets;
	}

	/**
	 * Return dataset with all scaler values for a detector element for several frames
	 * (from time series array PVs)
	 * @param element
	 * @param lowFrame
	 * @param highFrame
	 * @return List of dataset of scaler values (1 per channel/detector element). Shape [numFrames, numScalers]
	 * @throws DeviceException
	 */
	private Dataset getScalerTimeseriesData(int element, int lowFrame, int highFrame) throws DeviceException {
		// read the timeseries arrays for this detector element [num scalers][numValues]
		double[][] scalerValues = xspressController.getScalerTimeseries(element, lowFrame, highFrame);

		int numFrames = scalerValues[0].length;
		int numScalers = scalerValues.length;

		Dataset scalerData = DatasetFactory.zeros(numFrames, numScalers);

		for (int j = 0; j < numScalers; j++) {
			Dataset sd = DatasetFactory.createFromObject(DoubleDataset.class, scalerValues[j], numFrames, 1);
			scalerData.setSlice(sd, new int[] {0,j}, new int[] {numFrames, j+1}, null);
		}
		return scalerData;
	}

	/**
	 * Read deadtime correction factor datasets from detector SWMR file or array PVs across specified range of frames.<p>
	 * @param lowFrame
	 * @param highFrame
	 * @return Datasets, element. Shape = [numFrames, num channels]
	 * @throws NexusException
	 */
	public Dataset getDtcFactorData(int lowFrame, int highFrame) throws NexusException {
		if (fileReader == null) {
			return getDtcDataFromPvs(lowFrame, highFrame);
		} else {
			return getDtcDataFromSwmr(lowFrame, highFrame);
		}
	}

	/**
	 * Read deadtime correction factors from Epics
	 * Time series array values of Dtc factor not currently available in Epics, so return 1.0
	 *
	 * @param lowFrame
	 * @param highFrame
	 * @return list of datasets (1 per channel/detector element). Shape = [highFrame - lowFrame + 1]
	 */
	private Dataset getDtcDataFromPvs(int lowFrame, int highFrame){
		logger.info("Getting DTC factor values from PVs for frames {} to {}", lowFrame, highFrame);
		int numFrames = highFrame - lowFrame + 1;
		return DatasetFactory.ones(DoubleDataset.class, numFrames, xspressController.getNumElements());
	}

	/**
	 * Read deadtime correction factors from SWMR file. <p>
 	 * These might not be present in old versions of IOC, in which case a warning is logger and values as are substituted with 1s.
	 * @param lowFrame
	 * @param highFrame
	 * @return datasets. Shape = [numFrames, num channels]
	 * @throws NexusException
	 */
	private Dataset getDtcDataFromSwmr(int lowFrame, int highFrame) throws NexusException{
		logger.info("Getting DTC factor values from SWMR file for frames {} to {}", lowFrame, highFrame);
		int numFrames = highFrame - lowFrame + 1;
		if (twoDHdfData) {
			return fileReader.readDataset(getDtcFactorDataPath(0), new int[] {lowFrame, 0}, new int[] {numFrames, xspressController.getNumElements()}, new int[] {1,1});
		} else {
			int[] start = {lowFrame};
			int[] shape = {numFrames};
			int[] step = {1};
			Dataset dataForChannel = DatasetFactory.zeros(numFrames, xspressController.getNumElements());
			for(int i=0; i<xspressController.getNumElements(); i++) {
				String dataPath = getDtcFactorDataPath(i);
				Dataset scalerData = fileReader.readDataset(dataPath, start, shape, step);
				if (scalerData == null) {
					logger.warn("DTC data {} is not present in Hdf file- replacing with 1s", dataPath);
					scalerData = DatasetFactory.ones(numFrames, 1);
				}
				scalerData.setShape(numFrames, 1);
				dataForChannel.setSlice(scalerData, new int[] {0, i}, new int[] {numFrames, i+1}, null);
			}
			return dataForChannel;
		}
	}

	/**
	 * Generate name of scaler dataset in Hdf file, based on the scaler name format (set by {@link #setScalerDataNameFormat(String)}).
	 *
	 * @param element detector element/channel number
	 * @param scaler scaler number
	 * @return
	 */
	private String getScalerName(int... values) {
		if (values.length==1) {
			return String.format(scalerDataNameFormat, values[0]);
		} else if (values.length==2) {
			return String.format(scalerDataNameFormat, values[0]+1, values[1]);
		}
		return scalerDataNameFormat;
	}

	/**
	 * @param element
	 * @return Name of 'deadtime correction factor' dataset in hdf file for specified detector element
	 */
	private String getDeadtimeCorrectionFactorName(int element) {
		return String.format(dtcFactorDataNameFormat, element+1);
	}

	/**
	 * @param channel
	 * @param scaler
	 * @return Path to scaler data in Hdf file for given channel and scaler number.
	 */
	private String getScalerDataPath(int... values) {
		return Paths.get(pathToAttributeDataGroup, getScalerName(values)).toString();
	}

	/**
	 * @param channel
	 * @return Path to DTC factor data in Hdf file for given channel number.
	 */
	private String getDtcFactorDataPath(int channel) {
		return Paths.get(pathToAttributeDataGroup, getDeadtimeCorrectionFactorName(channel)).toString();
	}

	/**
	 * @param pathToAttributeDataGroup path to group in Hdf file containing the scaler and DTC factor data
	 */
	public void setPathToAttributeDataGroup(String pathToAttributeDataGroup) {
		this.pathToAttributeDataGroup = pathToAttributeDataGroup;
	}

	public String getPathToAttributeDataGroup() {
		return pathToAttributeDataGroup;
	}

	/**
	 * Format to be used to create scaler dataset names. e.g. 'Chan%02dSca%d' or 'scalar_chan%d'
	 * @param scalerDataNameFormat
	 */
	public void setScalerDataNameFormat(String scalerDataNameFormat) {
		this.scalerDataNameFormat = scalerDataNameFormat;
	}

	public String getScalerDataNameFormat() {
		return scalerDataNameFormat;
	}

	/**
	 * Format to be used to create scaler dataset names. e.g. 'Chan%02dDTCFactor' or 'dtc_chan%d'
	 * @param scalerDataNameFormat
	 */
	public void setDtcFactorDataNameFormat(String dtcFactorDataNameFormat) {
		this.dtcFactorDataNameFormat = dtcFactorDataNameFormat;
	}

	public String getDtcFactorDataNameFormat() {
		return dtcFactorDataNameFormat;
	}

}
