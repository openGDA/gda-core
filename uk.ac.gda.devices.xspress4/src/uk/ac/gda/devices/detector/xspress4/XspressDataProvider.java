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

		logger.debug("Creating new Swmr file reader");
		fileReader = new SwmrFileReader();

		// Setup list of dataset to be read for each detector element
		logger.debug("Datasets to be read : ");
		for (int element = 0; element < xspressController.getNumElements(); element++) {
			for(String datasetName : getAllDatanames(element)) {
				logger.debug("{}", datasetName);
				fileReader.addDatasetToRead(datasetName, Paths.get(pathToAttributeDataGroup, datasetName).toString());
			}
		}
	}

	/**
	 * @param element
	 * @return List of scaler and DTC factor dataset names for given detector element number
	 */
	private List<String> getAllDatanames(int element) {
		List<String> names = new ArrayList<>();
		for(int i=0; i<xspressController.getNumScalers(); i++) {
			names.add(getScalerName(element, i));
		}
		names.add(getDeadtimeCorrectionFactorName(element));
		return names;
	}

	/**
	 * Read scaler data from detector SWMR file or array PVs across specified range of frames.<p>
	 * Scaler data for each channel is its own dataset with shape = [numFrames, numScalers]
	 *
	 * @param lowFrame
	 * @param highFrame
	 * @return List of datasets, one per detector element.
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
	 * @return List of datasets (1 per detector channel/element). Shape [numFrames, numScalers]
	 * @throws NexusException
	 */
	private List<Dataset> getScalerDataFromSwmr(int lowFrame, int highFrame) throws NexusException {
		int[] start = new int[] { lowFrame };
		int[] shape = new int[] { highFrame - lowFrame + 1 };
		int[] step = new int[] { 1 };

		int totalFramesAvailable = fileReader.getNumAvailableFrames();
		int numFrames = shape[0];

		logger.debug("{} frames of data available in hdf file, reading {} frames of scaler data", totalFramesAvailable, numFrames);
		logger.info("Getting scaler values from SWMR file for frames {} to {}", lowFrame, highFrame);

		int numScalers = xspressController.getNumScalers();

		// Read shape of the first dataset and see if scaler data is stored in 2d blocks
		int[] dataShape = fileReader.getCurrentShape(getScalerDataPath(0,0));
		boolean dataIsTwoD = dataShape.length == 2;

		logger.debug("Reading data two dimension scaler datasets : {}", dataIsTwoD);

		List<Dataset> allDatasets = new ArrayList<>();
		for(int i=0; i<xspressController.getNumElements(); i++) {

			Dataset dataForChannel;
			if (dataIsTwoD) {
				dataForChannel = fileReader.readDataset(getScalerDataPath(i), new int[] {lowFrame, 0}, new int[] {numFrames, numScalers}, new int[] {1,1});
			} else {
				// Read all the scaler datasets for detector element and put into a single 2d dataset
				dataForChannel = DatasetFactory.zeros(numFrames, numScalers);
				for(int j=0; j<numScalers; j++) {
					String scalerDataPath = getScalerDataPath(i, j);
					Dataset scalerData = fileReader.readDataset(scalerDataPath, start, shape, step);
					scalerData.setShape(numFrames, 1);
					dataForChannel.setSlice(scalerData, new int[] {0, j}, new int[] {numFrames, j+1}, null);
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
	 * @return List of datasets, one per detector element. Shape = [numFrames]
	 * @throws NexusException
	 */
	public List<Dataset> getDtcFactorData(int lowFrame, int highFrame) throws NexusException {
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
	private List<Dataset> getDtcDataFromPvs(int lowFrame, int highFrame){
		logger.info("Getting DTC factor values from PVs for frames {} to {}", lowFrame, highFrame);
		List<Dataset> values = new ArrayList<>();
		int numFrames = highFrame - lowFrame + 1;
		for(int i=0; i<xspressController.getNumElements(); i++) {
			values.add(DatasetFactory.ones(numFrames));
		}
		return values;
	}

	/**
	 * Read deadtime correction factors from SWMR file. <p>
 	 * These might not be present in old versions of IOC, in which case a warning is logger and values as are substituted with 1s.
	 * @param lowFrame
	 * @param highFrame
	 * @return list of datasets (1 per channel/detector element). Shape = [numFrames]
	 * @throws NexusException
	 */
	private List<Dataset> getDtcDataFromSwmr(int lowFrame, int highFrame) throws NexusException{
		logger.info("Getting DTC factor values from SWMR file for frames {} to {}", lowFrame, highFrame);
		List<Dataset> values = new ArrayList<>();
		int numFrames = highFrame - lowFrame + 1;
		for(int i=0; i<xspressController.getNumElements(); i++) {
			Dataset dtcFactorData = fileReader.readDataset(getDtcFactorDataPath(i), new int[] {lowFrame}, new int[] {numFrames}, new int[] {1});
			if (dtcFactorData == null) {
				logger.warn("Could not read DTC factor data from {}. Substituting values with 1", getDtcFactorDataPath(i));
				dtcFactorData = DatasetFactory.ones(numFrames);
			}
			values.add(dtcFactorData);
		}
		return values;
	}

	/**
	 * Generate name of scaler dataset in Hdf file, based on the scaler name format (set by {@link #setScalerDataNameFormat(String)}).
	 *
	 * @param element detector element/channel number
	 * @param scaler scaler number
	 * @return
	 */
	private String getScalerName(int element, int scaler) {
		int numSubs = (int) scalerDataNameFormat.chars().filter(c -> c=='%').count();
		if (numSubs == 1 ) {
			return String.format(scalerDataNameFormat, element);
		}else {
			return String.format(scalerDataNameFormat, element+1, scaler);
		}
	}

	/**
	 * @param element
	 * @return Name of 'deadtime correction factor' dataset in hdf file for specified detector element
	 */
	private String getDeadtimeCorrectionFactorName(int element) {
		return String.format(dtcFactorDataNameFormat, element);
	}

	/**
	 * @param channel
	 * @param scaler
	 * @return Path to scaler data in Hdf file for given channel and scaler number.
	 */
	private String getScalerDataPath(int channel, int scaler) {
		return Paths.get(pathToAttributeDataGroup, getScalerName(channel, scaler)).toString();
	}

	private String getScalerDataPath(int channel) {
		return getScalerDataPath(channel, 0);
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
