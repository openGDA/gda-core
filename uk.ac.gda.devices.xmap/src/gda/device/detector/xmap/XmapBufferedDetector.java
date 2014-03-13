/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

package gda.device.detector.xmap;

import gda.configuration.properties.LocalProperties;
import gda.data.NumTracker;
import gda.data.PathConstructor;
import gda.data.metadata.GDAMetadataProvider;
import gda.data.nexus.extractor.NexusExtractor;
import gda.data.nexus.extractor.NexusGroupData;
import gda.data.nexus.tree.INexusTree;
import gda.data.nexus.tree.NexusTreeNode;
import gda.data.nexus.tree.NexusTreeProvider;
import gda.device.ContinuousParameters;
import gda.device.DeviceException;
import gda.device.detector.BufferedDetector;
import gda.device.detector.DAServer;
import gda.device.detector.DetectorBase;
import gda.device.detector.NXDetectorData;
import gda.device.detector.NexusDetector;
import gda.device.detector.xmap.edxd.EDXDController.COLLECTION_MODES;
import gda.device.detector.xmap.edxd.EDXDMappingController;
import gda.device.detector.xmap.util.XmapBufferedHdf5FileLoader;
import gda.device.detector.xmap.util.XmapFileLoader;
import gda.device.detector.xmap.util.XmapNexusFileLoader;
import gda.epics.CAClient;
import gda.factory.FactoryException;
import gda.factory.Finder;
import gov.aps.jca.CAException;
import gov.aps.jca.TimeoutException;

import java.io.File;
import java.util.List;

import org.nexusformat.NexusFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.beans.vortex.DetectorElement;
import uk.ac.gda.beans.vortex.VortexROI;
import uk.ac.gda.util.CorrectionUtils;

public class XmapBufferedDetector extends DetectorBase implements BufferedDetector, NexusDetector {
	private static final long serialVersionUID = -361735061750343662L;
	NexusXmap xmap;
	private XmapFileLoader fileLoader;
	private static final Logger logger = LoggerFactory.getLogger(XmapBufferedDetector.class);
	protected ContinuousParameters continuousParameters = null;
	protected boolean isContinuousMode = false;
	private DAServer daServer;
	EDXDMappingController controller;
	private boolean isSlave = true;
	private String daServerName;
	private int lastScanNumber = 0;
	private int lastRowNumber = -1;
	private String lastFileName = null;
	private boolean lastFileReadStatus = false;
	private String capturepv;
	private boolean deadTimeEnabled = true;

	public NexusXmap getXmap() {
		return xmap;
	}

	public void setXmap(NexusXmap xmap) {
		this.xmap = xmap;
	}

	public String getDaServerName() {
		return daServerName;
	}

	public void setDaServerName(String daServerName) {
		this.daServerName = daServerName;
	}

	public EDXDMappingController getController() {
		return controller;
	}

	public void setController(EDXDMappingController controller) {
		this.controller = controller;
	}

	@Override
	public void clearMemory() throws DeviceException {
		xmap.clear();
	}

	@Override
	public ContinuousParameters getContinuousParameters() throws DeviceException {
		return continuousParameters;
	}

	@Override
	public void configure() throws FactoryException {
		if (daServer == null) {
			logger.debug("XmapBuffereddetector configure(): finding: " + daServerName);
			if ((daServer = (DAServer) Finder.getInstance().find(daServerName)) == null) {
				logger.error("XmapBufferedDetector.configure(): Server " + daServerName + " not found");
			}
		}
	}

	@Override
	public int getNumberFrames() throws DeviceException {
		String fileName = null;
		try {
			fileName = controller.getHDFFileName();
		} catch (Exception e) {
			logger.error("Error getting HDF filename", e);
		}
		if (fileName != null && isStillWriting(fileName))
			return -1;
		// wait for another second to file to be closed
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			logger.error("Error performing sleep", e);
		}
		return continuousParameters.getNumberDataPoints();
	}

	@Override
	public boolean isContinuousMode() throws DeviceException {
		return isContinuousMode;
	}

	@Override
	public int maximumReadFrames() throws DeviceException {
		if (continuousParameters != null)
			return continuousParameters.getNumberDataPoints();
		return 9999;
	}

	@Override
	public Object[] readAllFrames() throws DeviceException {
		setCollectionTime(continuousParameters.getTotalTime());
		Integer lastFrame = getNumberFrames() - 1;
		return readFrames(0, lastFrame);
	}

	@Override
	public Object[] readFrames(int startFrame, int finalFrame) throws DeviceException {
//		String fileName = null;
//		NexusTreeProvider[] container = null;
		try {
			// System.out.println("wating for file");
			if (lastFileName == null && !lastFileReadStatus) {
				lastFileName = this.controller.getHDFFileName();

				waitForFile();
				// change to linux format
				String beamline = LocalProperties.get("gda.factory.factoryName", "").toLowerCase();
				//added wed 31st jul 2013 as now need to stop before reading h5 file. don't know why this is the case
				xmap.stop();
				//sleep required for gda to recognise number of arrays has finalized.
				Thread.sleep(1000);
				lastFileName = lastFileName.replace("X:/", "/dls/" + beamline);
				if (controller.isBufferedArrayPort())
					fileLoader = new XmapBufferedHdf5FileLoader(lastFileName);
				else
					fileLoader = new XmapNexusFileLoader(lastFileName,getXmap().getNumberOfMca());
				fileLoader.loadFile();
//				lastFileName = lastFileName;
				lastFileReadStatus = true;
			} else {
				
			}
			int numOfPointsInFile = fileLoader.getNumberOfDataPoints();
			int numPointsToRead = finalFrame - startFrame + 1;
			
			if (numOfPointsInFile < numPointsToRead) {
				throw new DeviceException("Xmap data file "+ lastFileName +  " only has " + numOfPointsInFile + " data point but expected at least " + numPointsToRead ); 
			}
			
			NexusTreeProvider[] container = new NexusTreeProvider[numPointsToRead];
			int frameIndex = startFrame;
			for (int i = 0; i < numPointsToRead; i++) {
				logger.info("writing point number " + frameIndex);
				container[i] = writeToNexusFile(i, fileLoader.getData(i));
				frameIndex++;
			}
			
//			
//			if (finalFrame < numOfPoints) {
//				for (int i = startFrame; i <= finalFrame; i++) {
//					logger.info("writing point number " + i);
//					container[i] = writeToNexusFile(i, fileLoader.getData(i));
//				}
//			}
//			if (finalFrame > numOfPoints) {
//				for (int i = startFrame; i < numOfPoints; i++) {
//					logger.info("writing point number " + i);
//					container[i] = writeToNexusFile(i, fileLoader.getData(i));
//				}
//			}
//			// last two points in the scan will have the same values
//			if (finalFrame == numOfPoints) {
//				if ((finalFrame - startFrame) == 0)
//					container[finalFrame - 1] = writeToNexusFile(finalFrame - 1, fileLoader.getData(finalFrame - 1));
//				else {
//					// always one less than the number of points
//
//					for (int i = startFrame; i < finalFrame; i++) {
//						logger.info("writing point number " + i);
//						container[i] = writeToNexusFile(i, fileLoader.getData(i));
//					}
//					container[finalFrame - 1] = (writeToNexusFile(finalFrame - 1, fileLoader.getData(finalFrame - 1)));
//				}
//			}
			// readSoFar = totalToRead;
			return container;
		} catch (Exception e) {
			logger.error("TODO put description of error here", e);
			try {
				stop();
				controller.endRecording();
			} catch (Exception e1) {
				controller.setCollectionMode(COLLECTION_MODES.MCA_SPECTRA);
				logger.error("Unalble to end hdf5 capture", e1);
				throw new DeviceException("Unalble to end hdf5 capture", e1);
			}
			controller.setCollectionMode(COLLECTION_MODES.MCA_SPECTRA);
			throw new DeviceException("Unable to load file called " + lastFileName, e);
		}
	}

	@Override
	public void setContinuousMode(boolean on) throws DeviceException {
		isContinuousMode = on;
		if (on)
			setupContinuousOperation();
		if (!isSlave) {
			if (on) {
				setTimeFrames();
			} else {
				switchOffExtTrigger();
			}
		}
	}

	private void switchOnExtTrigger() throws DeviceException {
		getDaServer().sendCommand("tfg setup-trig start ttl0");
	}

	private void switchOffExtTrigger() throws DeviceException {
		getDaServer().sendCommand("tfg setup-trig start"); // disables external triggering
	}

	private void setTimeFrames() throws DeviceException {
		switchOnExtTrigger();
		getDaServer().sendCommand("tfg setup-groups ext-start cycles 1");
		getDaServer().sendCommand(continuousParameters.getNumberDataPoints() + " 0.000001 0.00000001 0 0 0 8");
		getDaServer().sendCommand("-1 0 0 0 0 0 0");
		getDaServer().sendCommand("tfg arm");
	}

	@Override
	public void setContinuousParameters(ContinuousParameters parameters) throws DeviceException {
		this.continuousParameters = parameters;
	}

	@Override
	public void collectData() throws DeviceException {
		// donothing collection triggered by hardware

	}

	@Override
	public boolean createsOwnFiles() throws DeviceException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public double getCollectionTime() throws DeviceException {
		return collectionTime;
	}

	@Override
	public String getDescription() throws DeviceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDetectorID() throws DeviceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDetectorType() throws DeviceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getStatus() throws DeviceException {
		return xmap.getStatus();
	}

	@Override
	public void setCollectionTime(double time) throws DeviceException {
		this.collectionTime = time;
	}

	@Override
	public void atScanStart() throws DeviceException {
		stopAcq();
		controller.setCollectionMode(COLLECTION_MODES.MCA_MAPPING);
	}

	@Override
	public void atScanEnd() throws DeviceException {
		try {
			stop();
			controller.endRecording();
		} catch (Exception e) {
			controller.setCollectionMode(COLLECTION_MODES.MCA_SPECTRA);
			logger.error("Unalble to end hdf5 capture", e);
			throw new DeviceException("Unalble to end hdf5 capture", e);
		}
		controller.setCollectionMode(COLLECTION_MODES.MCA_SPECTRA);
		stopAcq();
	}

	@Override
	public void atCommandFailure() throws DeviceException {
		try {
			stop();
			controller.endRecording();
		} catch (Exception e) {
			controller.setCollectionMode(COLLECTION_MODES.MCA_SPECTRA);
			logger.error("cannot set collection mode to mca spectra", e);
			throw new DeviceException("Unalble to end hdf5 capture", e);
		}
		controller.setCollectionMode(COLLECTION_MODES.MCA_SPECTRA);
	}

	@Override
	public void atScanLineStart() throws DeviceException {
		lastFileName = null;
		lastFileReadStatus = false;
	}

	@Override
	public String[] getExtraNames() {
		return xmap.getExtraNames();
	}

	private void setupFilename() throws Exception {
		// filename prefix
		String beamline = "base";
		try {
			beamline = GDAMetadataProvider.getInstance().getMetadataValue("instrument", "gda.instrument", null);
		} catch (DeviceException e1) {
			// don't let an exception stop us here
			logger.warn("Cannot get instrument or gda.instrument property value");
		}
		controller.setFilenamePrefix(beamline);

		// scan number
		NumTracker runNumber = new NumTracker("tmp");
		// Get the current number
		Number scanNumber = runNumber.getCurrentFileNumber();
		if (!(scanNumber.intValue() == lastScanNumber))
			lastRowNumber = -1;
		lastScanNumber = scanNumber.intValue();
		controller.setFileNumber(scanNumber);

		// row number
		lastRowNumber++;
		controller.setFilenamePostfix(lastRowNumber + "-" + getName());

		// set the sub-directory and create if necessary
		String dataDir = PathConstructor.createFromDefaultProperty();
		dataDir = dataDir + "tmp" + File.separator + lastScanNumber;
		if (!(new File(dataDir)).exists()) {
			boolean directoryExists = (new File(dataDir)).mkdirs();
			if (!directoryExists) {
				throw new DeviceException("Failed to create temporary directory to place Xmap HDF5 files: " + dataDir);
			}
		}
		dataDir = dataDir.replace("/dls/" + beamline.toLowerCase(), "X:/");
		controller.setDirectory(dataDir);
	}

	private void setupContinuousOperation() throws DeviceException {
		try {
			setupFilename();
			controller.resetCounters();
			int numberOfPointsPerScan = continuousParameters.getNumberDataPoints();
			//This has a -1 for b18 and not for i18. Need to figure out why.
			controller.setPixelsPerRun(numberOfPointsPerScan);
			//
			controller.setAutoPixelsPerBuffer(true);
			int buffPerRow = (numberOfPointsPerScan) / 124 + 1;
			controller.setHdfNumCapture(buffPerRow);
			controller.startRecording();
		} catch (Exception e) {

			logger.error("Error occurred arming the xmap detector", e);
			throw new DeviceException("Error occurred arming the xmap detector", e);
		}
		xmap.clearAndStart();
	}

	@Override
	public void stop() throws DeviceException {
		xmap.stop();
		stopAcq();
		//controller.setCollectionMode(COLLECTION_MODES.MCA_SPECTRA);
	}

	@Override
	public NexusTreeProvider readout() throws DeviceException {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isStillWriting(String fileName) throws DeviceException {
		try {
			if (controller.getHDFFileName().equals(fileName))
				return controller.getCaptureStatus();
		} catch (Exception e) {
			
			logger.error("CAnnot read the file capture status", e);
			throw new DeviceException("CAnnot read the file capture status", e);
		}
		return false;
	}

	public void waitForFile() throws InterruptedException {
		double timeoutMilliSeconds = 100000;
		double waitedSoFarMilliSeconds = 0;
		int waitTime = 1000;
		CAClient ca_client = new CAClient();
		try {
			while (ca_client.caget(capturepv+"_RBV").equals("Capturing")
					&& waitedSoFarMilliSeconds <= timeoutMilliSeconds) {
				Thread.sleep(waitTime);
				waitedSoFarMilliSeconds += waitTime;
			}
		} catch (CAException e) {
			logger.error("Timeout waiting to connect to xmap capture pv", e);
		} catch (TimeoutException e) {
			logger.error("Timeout waiting to connect to xmap capture pv" , e);
		}
		Thread.sleep(3000);
	}

	// File r/w methods
	public NXDetectorData writeToNexusFile(int dataPointNumber, short[][] detectorData) {
		NXDetectorData output = new NXDetectorData(xmap);
		INexusTree detTree = output.getDetTree(xmap.getName());

		int numFilteredElements = xmap.getNumberOfIncludedDetectors();
//		int numberOfElements = xmap.vortexParameters.getDetectorList().size();
		int originalNumberOfElements = xmap.vortexParameters.getDetectorList().size();
		int numberOfROIs = xmap.vortexParameters.getDetectorList().get(0).getRegionList().size();

		// items to write to nexus
		double[] summation = null;
		double[] correctedAllCounts = new double[numFilteredElements];
		final List<Double> times = this.xmap.getEventProcessingTimes();
		double[] ocrs = new double[numFilteredElements];
		double[] icrs = new double[numFilteredElements];
		double[][] roiCounts = new double[numberOfROIs][numFilteredElements];
		String[] roiNames = new String[numberOfROIs];
		short reducedData[][] = new short[numFilteredElements][];

		int index = -1;
		for (int element = 0; element < originalNumberOfElements; element++) {

			DetectorElement thisElement = this.xmap.vortexParameters.getDetectorList().get(element);
			if (thisElement.isExcluded())
				continue;
			index ++;
			
			reducedData[index] = detectorData[element];
			
			final double ocr = getOCR(dataPointNumber, element);
			ocrs[index] = ocr;
			final double icr = getICR(dataPointNumber, element);
			icrs[index] = icr;

			// Total counts
			double allCounts = getEvents(dataPointNumber, element);
			correctedAllCounts[index] = allCounts;// * deadTimeCorrectionFactor

			// REGIONS
			for (int iroi = 0; iroi < thisElement.getRegionList().size(); iroi++) {

				final VortexROI roi = thisElement.getRegionList().get(iroi);

				// TODO calculate roi from the full spectrum data
				double count = calculateROICounts(roi.getRoiStart(), roi.getRoiEnd(), detectorData[element]);
				if (deadTimeEnabled) {
					Double deadTimeCorrectionFactor = CorrectionUtils.getK(times.get(element), icr, ocr);
					if (deadTimeCorrectionFactor.isInfinite() || deadTimeCorrectionFactor.isNaN())
						deadTimeCorrectionFactor = 0.0;
					else
						count *= deadTimeCorrectionFactor;
				}
				roiCounts[iroi][index] = count;
				roiNames[iroi] = roi.getRoiName();
			}

			if (this.xmap.isSumAllElementData()) {
				if (summation == null)
					summation = new double[detectorData[element].length];
				for (int i = 0; i < detectorData[element].length; i++) {
					summation[i] += detectorData[element][i];
				}
			}
		}

		// add total counts
		final INexusTree counts = output.addData(detTree, "totalCounts", new int[] { numFilteredElements },
				NexusFile.NX_FLOAT64, correctedAllCounts, "counts", 1);
		index = -1;
		for (int element = 0; element < originalNumberOfElements; element++) {
			DetectorElement thisElement = xmap.vortexParameters.getDetectorList().get(element);
			if (thisElement.isExcluded())
				continue;
			index++;
			output.setPlottableValue(thisElement.getName(), correctedAllCounts[index]);
		}

		// event processing time.
		String evtProcessTimeAsString = "";
		for (Double ept : times) {
			evtProcessTimeAsString += ept + " ";
		}
		evtProcessTimeAsString = evtProcessTimeAsString.trim();
		counts.addChildNode(new NexusTreeNode("eventProcessingTime", NexusExtractor.AttrClassName, counts,
				new NexusGroupData(evtProcessTimeAsString)));

		// ICR
		output.addData(detTree, "icr", new int[] { numFilteredElements }, NexusFile.NX_FLOAT64, icrs, "Hz", 1);

		// OCR
		output.addData(detTree, "ocr", new int[] { numFilteredElements }, NexusFile.NX_FLOAT64, ocrs, "Hz", 1);

		// roicounts
		for (int iroi = 0; iroi < numberOfROIs; iroi++) {
			String roiName = roiNames[iroi];
			output.addData(detTree, roiName, new int[] { numFilteredElements }, NexusFile.NX_FLOAT64, roiCounts[iroi],
					"counts", 1);
			index = -1;
			for (int element = 0; element < originalNumberOfElements; element++) {
				DetectorElement detElement = xmap.vortexParameters.getDetectorList().get(element);
				if (detElement.isExcluded()) 
					continue;
				index++;
				String elementName = detElement.getName();
				output.setPlottableValue(elementName + "_" + roiName, roiCounts[iroi][index]);
			}
		}

		// add the full spectrum
		output.addData(detTree, "fullSpectrum", new int[] { numFilteredElements, reducedData[0].length },
				NexusFile.NX_INT16, reducedData, "counts", 1);

		double ff = calculateScalerData(dataPointNumber, numberOfROIs, detectorData);
		output.addData(detTree, "FF", new int[] { 1 }, NexusFile.NX_FLOAT64, new Double[] { ff }, "counts", 1);
		output.setPlottableValue("FF", ff);

		if (summation != null)
			output.addData(detTree, "allElementSum", new int[] { summation.length }, NexusFile.NX_FLOAT64, summation,
					"counts", 1);
		return output;
	}

	private double getEvents(int dataPointNumber, int element) {
		double event = fileLoader.getEvents(dataPointNumber, element);
		return event;
	}

	private double calculateScalerData(int dataPointNumber, int numberOfROIs, short[][] detectorData) {
		final double[] k = new double[this.xmap.vortexParameters.getDetectorList().size()];
		final List<Double> times = this.xmap.getEventProcessingTimes();
		for (int i = 0; i < k.length; i++) {
			Double correctionFactor = CorrectionUtils.getK(times.get(i), getICR(dataPointNumber, i),
					getOCR(dataPointNumber, i));
			if (correctionFactor.isInfinite() || correctionFactor.isNaN() || correctionFactor == 0.0) {
				correctionFactor = 1.0;
			}
			k[i] = correctionFactor;
		}

		// Correct mca counts using K as we go
		Double[] rois = new Double[numberOfROIs];
		for (int i = 0; i < rois.length; i++) {
			rois[i] = 0.0;
			for (int j = 0; j < k.length; j++) {
				if (j >= this.xmap.vortexParameters.getDetectorList().size())
					continue;
				DetectorElement element = this.xmap.vortexParameters.getDetectorList().get(j);
				if (element.isExcluded())
					continue;
				VortexROI region = element.getRegionList().get(0);
				double correctedMCA = calculateROICounts(region.getRoiStart(), region.getRoiEnd(), detectorData[j])
						* k[j];
				rois[i] += correctedMCA;
			}
		}
		double ff = 0;
		for (double roi : rois) {
			ff += roi;
		}
		return ff;
	}

	private double getICR(int dataPointNumber, int element) {
		double trigger = 0.0;
		double liveTime = 0.0;

		trigger = fileLoader.getTrigger(dataPointNumber, element);
		liveTime = fileLoader.getLiveTime(dataPointNumber, element);
		if (trigger != 0.0 && liveTime != 0.0)
			return trigger / liveTime;
		return 0.0;
	}

	private double getOCR(int dataPointNumber, int element) {
		double event = fileLoader.getEvents(dataPointNumber, element);
		double realTime = fileLoader.getRealTime(dataPointNumber, element);
		if (event != 0.0 && realTime != 0.0)
			return event / realTime;
		return 0;
	}

	private double calculateROICounts(int regionLow, int regionHigh, short[] data) {
		double count = 0.0;
		for (int i = regionLow; i <= regionHigh; i++)
			count += data[i];
		return count;
	}

	public boolean isSlave() {
		return isSlave;
	}

	public void setSlave(boolean isSlave) {
		this.isSlave = isSlave;
	}

	public void setDaServer(DAServer daServer) {
		this.daServer = daServer;
	}

	public DAServer getDaServer() {
		return daServer;
	}

	public String getCapturepv() {
		return capturepv;
	}

	public void setCapturepv(String stoppv) {
		this.capturepv = stoppv;
	}

	public void stopAcq() {
		CAClient ca_client = new CAClient();
		try {
			if (capturepv != null)
				ca_client.caput(capturepv, 0);
		} catch (CAException e) {
			logger.error("Could not stop xmap capture", e);
		} catch (InterruptedException e) {
			logger.error("Could not stop xmap capture", e);
		}
	}

	public boolean isDeadTimeEnabled() {
		return deadTimeEnabled;
	}

	public void setDeadTimeEnabled(boolean deadTimeEnabled) {
		this.deadTimeEnabled = deadTimeEnabled;
	}
}
