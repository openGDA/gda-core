/*-
 * Copyright © 2018 Diamond Light Source Ltd.
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

import org.apache.commons.lang.StringUtils;
import org.eclipse.dawnsci.analysis.api.io.ScanFileHolderException;
import org.eclipse.dawnsci.nexus.NexusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.data.nexus.tree.NexusTreeProvider;
import gda.data.swmr.SwmrFileReader;
import gda.device.ContinuousParameters;
import gda.device.DeviceException;
import gda.device.detector.BufferedDetector;
import gda.device.detector.DetectorBase;
import gda.device.detector.NXDetectorData;
import gda.device.detector.NexusDetector;
import gda.device.detector.NexusTreeWriterHelper;
import gda.factory.FactoryException;
import gda.observable.IObserver;
import uk.ac.gda.devices.detector.FluorescenceDetector;
import uk.ac.gda.devices.detector.FluorescenceDetectorParameters;
import uk.ac.gda.devices.detector.xspress4.Xspress4Detector.TriggerMode;

public class Xspress4BufferedDetector extends DetectorBase implements BufferedDetector, NexusDetector, FluorescenceDetector {

	private static final Logger logger = LoggerFactory.getLogger(Xspress4BufferedDetector.class);

	private Xspress4Detector xspressDetector;
	private Xspress4NexusTree nexusTree;
	private ContinuousParameters parameters;
	private boolean isContinuousModeOn;
	private TriggerMode triggerModeForContinuousScan = TriggerMode.TtlVeto;

	private String pathToAttributeDataGroup = "/entry/instrument/NDAttributes/";
	private String scalerDataNameFormat = "Chan%02dSca%d";
	private String dtcFactorDataNameFormat = "Chan%02dDTCFactor";

	private SwmrFileReader fileReader;
	private boolean useSwmrFileReading = false;

	private boolean useNexusTreeWriter = false;
	private transient NexusTreeWriterHelper nexusTreeWriter = new NexusTreeWriterHelper();
	private int maxFramesToReadAtOnce = 500;

	@Override
	public void clearMemory() throws DeviceException {
		// Don't need to manually clear the memory. This is done by Xspress4Detector::atScanLineStart()
		// when arming the detector at the start of each scan.
	}

	@Override
	public void setContinuousMode(boolean on) throws DeviceException {
		this.isContinuousModeOn = on;
		if (on) {
			// xspressDetector.atScanStart() has already set the number of frames and reset the array counter

			// Set number of frame again - number of frames from ContinuousParameters is 1 more than is reported in the
			// current ScanInformation object used in Xspress4Detector#atScanStart to set the number of points
			xspressDetector.setupNumFramesToCollect(parameters.getNumberDataPoints());

			// Set the trigger mode
			xspressDetector.setTriggerMode(triggerModeForContinuousScan);

			// Set time per point when using internal trigger mode
			if (triggerModeForContinuousScan == TriggerMode.Burst) {
				double timePerFrame = parameters.getTotalTime()/parameters.getNumberDataPoints();
				xspressDetector.setAcquireTime(timePerFrame);
			}

			if (xspressDetector.isWriteHDF5Files() && useSwmrFileReading) {
				setupSwmrFileReader();
			}
			xspressDetector.atScanLineStart();
		}
	}

	/**
	 *
	 * @param element
	 * @param scaler
	 * @return Name of scaler dataset in hdf file for given detector element and scaler number
	 */
	public String getScalerName(int element, int scaler) {
		return String.format(scalerDataNameFormat, element + 1, scaler);
	}

	/**
	 *
	 * @param element
	 * @return Name of 'deadtime correction factor' dataset in hdf file for specified detector element
	 */
	public String getDeadtimeCorrectionFactorName(int element) {
		return String.format(dtcFactorDataNameFormat, element+1);
	}

	private List<String> getAllDatanames(int element) {
		List<String> names = new ArrayList<>();
		for(int i=0; i< xspressDetector.getController().getNumScalers(); i++) {
			names.add(getScalerName(element, i));
		}
		names.add(getDeadtimeCorrectionFactorName(element));
		return names;
	}

	/**
	 * Create new SwmrFileReader setup list of dataset in hdf file to be read with names of scaler datasets for each detector element
	 * (i.e. "Chan..Sca.." datasets in attribute data group of detector hdf file).
	 */
	private void setupSwmrFileReader() {
		fileReader = new SwmrFileReader();

		// Setup list of dataset to be read for each detector element
		for (int element = 0; element < xspressDetector.getNumberOfElements(); element++) {
			for(String datasetName : getAllDatanames(element)) {
				fileReader.addDatasetToRead(datasetName, Paths.get(pathToAttributeDataGroup, datasetName).toString());
			}
		}
		nexusTree.setSwmrFileReader(fileReader);
	}

	@Override
	public boolean isContinuousMode() throws DeviceException {
		return isContinuousModeOn;
	}

	@Override
	public void setContinuousParameters(ContinuousParameters parameters) throws DeviceException {
		this.parameters = parameters;
	}

	@Override
	public ContinuousParameters getContinuousParameters() {
		return parameters;
	}

	@Override
	public int getNumberFrames() throws DeviceException {
		final int minFramesBeforeHdfRead = 10;
		try {
			if (!useSwmrFileReading) {
				int numFrames = xspressDetector.getController().getTimeSeriesNumPoints();
				logger.debug("getNumFrames() : {} from scaler time series array length", numFrames);
				return numFrames;
			}

			// Return 0 for first few frames, to give detector a chance to create the Hdf file and put some data in it.
			int numFramesArrayCounter = xspressDetector.getController().getTotalFramesAvailable();
			logger.debug("getNumFrames() : {} from array counter", numFramesArrayCounter);
			if (!xspressDetector.isWriteHDF5Files()) {
				return numFramesArrayCounter;
			} else if (numFramesArrayCounter < minFramesBeforeHdfRead) {
				logger.debug("getNumFrame() : < {} frames, returning 0", minFramesBeforeHdfRead);
				return 0;
			}

			// Open hdf file
			if (useSwmrFileReading && fileReader != null) {
				if (fileReader.getFilename().isEmpty()) {
					logger.debug("Opening detector hdf file for reading...");
					fileReader.openFile(xspressDetector.getXspress3Controller().getFullFileName());
				}
				int numFramesHdf = fileReader.getNumAvailableFrames();
				logger.debug("getNumFrames() : {} from Hdf file", numFramesHdf);
				return numFramesHdf;
			} else {
				logger.warn("getNumFrames() : Using Hdf file but SwmrFileReader has not been set. Using num frames from array counter");
				return numFramesArrayCounter;
			}
		} catch (NexusException | ScanFileHolderException e) {
			throw new DeviceException(e);
		}
	}

	@Override
	public NXDetectorData[] readFrames(int startFrame, int finalFrame) throws DeviceException {
		try {
			NXDetectorData[] detectorData = nexusTree.getDetectorData(startFrame, finalFrame);
			if (useNexusTreeWriter) {
				int startIndex = 0;
				// Skip first frame if writing to scan nexus file - NexusDatawriter should write this point, so
				// dataset has correct attributes etc.
				if (nexusTreeWriter.isWriteToScanNexusFile()) {
					startIndex = startFrame == 0 ? 1 : 0;
				}
				NexusTreeProvider[] detData = nexusTreeWriter.getCopy(detectorData, startIndex);
				if (detData.length > 0) {
					nexusTreeWriter.addDetectorData(detData, startFrame, finalFrame);
					if (startFrame > 0) {
						nexusTreeWriter.writeData();
					}
				}
			}
			return detectorData;
		} catch (NexusException | ScanFileHolderException e) {
			throw new DeviceException("Problem getting detector data in Xspress4BufferedDetector.readFrames", e);
		}
	}

	@Override
	public NXDetectorData[] readAllFrames() throws DeviceException {
		return readFrames(0, getNumberFrames());
	}

	@Override
	public int maximumReadFrames() throws DeviceException {
		return maxFramesToReadAtOnce;
	}

	public void setMaximumReadFrames(int maxFramesToReadAtOnce) {
		this.maxFramesToReadAtOnce = maxFramesToReadAtOnce;
	}

	public TriggerMode getTriggerModeForContinuousScan() {
		return triggerModeForContinuousScan;
	}

	public void setTriggerModeForContinuousScan(TriggerMode triggerModeForContinuousScan) {
		this.triggerModeForContinuousScan = triggerModeForContinuousScan;
	}

	public Xspress4Detector getXspress4Detector() {
		return xspressDetector;
	}

	public void setXspress4Detector(Xspress4Detector xspressDetector) {
		this.xspressDetector = xspressDetector;
		nexusTree = new Xspress4NexusTree(this);
	}

	@Override
	public double getCollectionTime() throws DeviceException {
		return xspressDetector.getCollectionTime();
	}

	@Override
	public void setCollectionTime(double collectionTime) throws DeviceException {
		xspressDetector.setCollectionTime(collectionTime);
	}

	@Override
	public int[] getDataDimensions() throws DeviceException {
		return xspressDetector.getDataDimensions();
	}

	@Override
	public int getProtectionLevel() throws DeviceException {
		return xspressDetector.getProtectionLevel();
	}

	@Override
	public void endCollection() throws DeviceException {
		xspressDetector.endCollection();
	}

	@Override
	public void setProtectionLevel(int permissionLevel) throws DeviceException {
		xspressDetector.setProtectionLevel(permissionLevel);
	}

	@Override
	public void prepareForCollection() throws DeviceException {
		xspressDetector.prepareForCollection();
	}

	@Override
	public void atScanStart() throws DeviceException {
		xspressDetector.atScanStart();
		if (useNexusTreeWriter) {
			nexusTreeWriter.atScanStart();
		}
	}

	@Override
	public void asynchronousMoveTo(Object collectionTime) throws DeviceException {
		xspressDetector.asynchronousMoveTo(collectionTime);
	}

	@Override
	public void atScanLineStart() throws DeviceException {
		//done by setContinuousMode instead
//		xspressDetector.atScanLineStart();
	}

	@Override
	public Object getPosition() throws DeviceException {
		return xspressDetector.getPosition();
	}

	@Override
	public void reconfigure() throws FactoryException {
		xspressDetector.reconfigure();
	}

	@Override
	public void atScanEnd() throws DeviceException {
		xspressDetector.atScanEnd();
		// Try to release handle to detector hdf file.
		if (fileReader != null && fileReader.isFileOpen()) {
			try {
				fileReader.releaseFile();
			} catch (ScanFileHolderException e) {
				logger.error("Problem closing detector hdf file at scan end : {}", e.getMessage(), e);
				throw new DeviceException(e);
			}
		}
		if (useNexusTreeWriter) {
			nexusTreeWriter.atScanEnd();
		}
	}

	@Override
	public void atPointEnd() throws DeviceException {
		xspressDetector.atPointEnd();
	}

	@Override
	public void close() throws DeviceException {
		xspressDetector.close();
	}

	@Override
	public void setAttribute(String attributeName, Object value) throws DeviceException {
		xspressDetector.setAttribute(attributeName, value);
	}

	@Override
	public Object getAttribute(String attributeName) throws DeviceException {
		return xspressDetector.getAttribute(attributeName);
	}

	@Override
	public boolean isBusy() throws DeviceException {
		return xspressDetector.isBusy();
	}

	@Override
	public String toFormattedString() {
		return xspressDetector.toFormattedString();
	}

	@Override
	public void addIObserver(IObserver observer) {
		xspressDetector.addIObserver(observer);
	}

	@Override
	public void collectData() throws DeviceException {
		xspressDetector.collectData();
	}

	@Override
	public void deleteIObserver(IObserver observer) {
		xspressDetector.deleteIObserver(observer);
	}

	@Override
	public void deleteIObservers() {
		xspressDetector.deleteIObservers();
	}

	@Override
	public void stop() throws DeviceException {
		xspressDetector.stop();
	}

	@Override
	public int getStatus() throws DeviceException {
		return xspressDetector.getStatus();
	}

	@Override
	public String getDescription() throws DeviceException {
		return xspressDetector.getDescription();
	}

	@Override
	public String getDetectorID() throws DeviceException {
		return xspressDetector.getDetectorID();
	}

	@Override
	public String getDetectorType() throws DeviceException {
		return xspressDetector.getDetectorType();
	}

	@Override
	public boolean createsOwnFiles() throws DeviceException {
		return xspressDetector.createsOwnFiles();
	}

	@Override
	public NexusTreeProvider readout() throws DeviceException {
		return xspressDetector.readout();
	}

	@Override
	public String[] getExtraNames() {
		return xspressDetector.getExtraNames();
	}

	@Override
	public void atPointStart() throws DeviceException {
		xspressDetector.atPointStart();
	}

	@Override
	public void atScanLineEnd() throws DeviceException {
		xspressDetector.atScanLineEnd();
	}

	@Override
	public void atLevelStart() throws DeviceException {
		xspressDetector.atLevelStart();
	}

	@Override
	public void atLevelMoveStart() throws DeviceException {
		xspressDetector.atLevelMoveStart();
	}

	@Override
	public double[][] getMCAData(double time) throws DeviceException {
		return xspressDetector.getMCAData(time);
	}

	@Override
	public void atLevelEnd() throws DeviceException {
		xspressDetector.atLevelEnd();
	}

	@Override
	public void atCommandFailure() throws DeviceException {
		xspressDetector.atCommandFailure();
	}

	@Override
	public String[] getInputNames() {
		return xspressDetector.getInputNames();
	}

	@Override
	public int getLevel() {
		return xspressDetector.getLevel();
	}

	@Override
	public String[] getOutputFormat() {
		return xspressDetector.getOutputFormat();
	}

	@Override
	public void moveTo(Object position) throws DeviceException {
		xspressDetector.moveTo(position);
	}

	@Override
	public void setExtraNames(String[] names) {
		xspressDetector.setExtraNames(names);
	}

	@Override
	public void setInputNames(String[] names) {
		xspressDetector.setInputNames(names);
	}

	@Override
	public void setLevel(int level) {
		xspressDetector.setLevel(level);
	}

	@Override
	public void setOutputFormat(String[] names) {
		xspressDetector.setOutputFormat(names);
	}

	@Override
	public void waitWhileBusy() throws DeviceException, InterruptedException {
		xspressDetector.waitWhileBusy();
	}

	@Override
	public boolean isAt(Object positionToTest) throws DeviceException {
		return xspressDetector.isAt(positionToTest);
	}

	public Xspress4Controller getController() {
		return xspressDetector.getController();
	}

	@Override
	public int getNumberOfElements() {
		return xspressDetector.getNumberOfElements();
	}

	@Override
	public int getMCASize() {
		return xspressDetector.getMCASize();
	}

	@Override
	public int getMaxNumberOfRois() {
		return xspressDetector.getMaxNumberOfRois();
	}

	@Override
	public void applyConfigurationParameters(FluorescenceDetectorParameters parameters) throws Exception {
		xspressDetector.applyConfigurationParameters(parameters);
	}

	@Override
	public FluorescenceDetectorParameters getConfigurationParameters() {
		return xspressDetector.getConfigurationParameters();
	}

	@Override
	public boolean isWriteHDF5Files() {
		return xspressDetector.isWriteHDF5Files();
	}

	@Override
	public void setWriteHDF5Files(boolean writeHdfFiles) {
		xspressDetector.setWriteHDF5Files(writeHdfFiles);
	}

	public String getPathToAttributeDataGroup() {
		return pathToAttributeDataGroup;
	}

	public void setPathToAttributeDataGroup(String pathToAttributeDataGroup) {
		this.pathToAttributeDataGroup = pathToAttributeDataGroup;
	}

	public String getScalerDataNameFormat() {
		return scalerDataNameFormat;
	}

	public void setScalerDataNameFormat(String scalerDataNameFormat) {
		this.scalerDataNameFormat = scalerDataNameFormat;
	}

	public String getDtcFactorDataNameFormat() {
		return dtcFactorDataNameFormat;
	}

	public void setDtcFactorDataNameFormat(String dtcFactorDataNameFormat) {
		this.dtcFactorDataNameFormat = dtcFactorDataNameFormat;
	}

	public boolean isUseSwmrFileReading() {
		return useSwmrFileReading;
	}

	public void setUseSwmrFileReading(boolean useSwmrFileReading) {
		this.useSwmrFileReading = useSwmrFileReading;
	}

	public boolean isUseNexusTreeWriter() {
		return useNexusTreeWriter;
	}

	public void setUseNexusTreeWriter(boolean useNexusTreeWriter) {
		this.useNexusTreeWriter = useNexusTreeWriter;
	}

	public void setDetectorNexusFilename(String detectorNexusFilename) {
		if (StringUtils.isEmpty(detectorNexusFilename)) {
			nexusTreeWriter.setWriteToScanNexusFile(true);
		} else {
			nexusTreeWriter.setWriteToScanNexusFile(false);
			nexusTreeWriter.setDetectorNexusFilename(detectorNexusFilename);
		}
	}

	public String getDetectorNexusFilename() {
		return nexusTreeWriter.getDetectorNexusFilename();
	}
}
