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

import gda.data.nexus.extractor.NexusExtractor;
import gda.data.nexus.extractor.NexusGroupData;
import gda.data.nexus.tree.INexusTree;
import gda.data.nexus.tree.NexusTreeNode;
import gda.data.nexus.tree.NexusTreeProvider;
import gda.device.ContinuousParameters;
import gda.device.DeviceException;
import gda.device.detector.BufferedDetector;
import gda.device.detector.DetectorBase;
import gda.device.detector.NXDetectorData;
import gda.device.detector.NexusDetector;
import gda.device.detector.xmap.util.XmapNexusFileLoader;
import gda.factory.FactoryException;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.beans.DetectorROI;
import uk.ac.gda.beans.vortex.DetectorElement;
import uk.ac.gda.util.CorrectionUtils;

public class DummyXmapBufferedDetector extends DetectorBase implements BufferedDetector, NexusDetector {

	private final ExecutorService pool = Executors.newFixedThreadPool(25);
	NexusXmap xmap;
	private XmapNexusFileLoader fileLoader;
	private static final Logger logger = LoggerFactory.getLogger(DummyXmapBufferedDetector.class);
	protected ContinuousParameters continuousParameters = null;
	protected boolean isContinuousMode = false;
	private boolean isSlave = true;
	private String daServerName;
//	private int lastScanNumber=0;
//	private int lastRowNumber =-1;
//	private String lastFileName = null;
//	private boolean lastFileReadStatus = false;
	private List<String> fileNames ;
	public List<String> getFileNames() {
		return fileNames;
	}

	public void setFileNames(List<String> fileNames) {
		this.fileNames = fileNames;
	}

	private long currentTime;
	private long scanStartTime;

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
		// A real system needs a connection to a real da.server via a DAServer object.
	}

	// returns the current frame number
	@Override
	public int getNumberFrames() throws DeviceException {
		if((currentTime - scanStartTime ) >= continuousParameters.getTotalTime()){
				return continuousParameters.getNumberDataPoints();
		}
		return 0;
	}

	@Override
	public boolean isContinuousMode() throws DeviceException {
		return isContinuousMode;
	}

	@Override
	public int maximumReadFrames() throws DeviceException {
		// TODO
		if(continuousParameters != null)
			return continuousParameters.getNumberDataPoints();
		return 100;
	}

	@Override
	public Object[] readAllFrames() throws DeviceException {
		Integer lastFrame = getNumberFrames() - 1;
		return readFrames(0, lastFrame);
	}

	@Override
	public Object[] readFrames(int startFrame, int finalFrame) throws DeviceException {
		String fileName = null;
		NexusTreeProvider[] container =null;
		try {

			fileName = fileNames.get(0);
			waitForFile(fileName);
			fileLoader = new XmapNexusFileLoader(fileName, xmap.getNumberOfMca());
			fileLoader.loadFile();
//			lastFileName = fileName;
//			lastFileReadStatus = true;
			int numOfPoints = fileLoader.getNumberOfDataPoints();
			container =  new  NexusTreeProvider[numOfPoints + 1];
			if(finalFrame < numOfPoints){
				for (int i = startFrame; i <= finalFrame; i++) {
					logger.info("writing point number " + i);
					//container[i] =writeToNexusFile(i, fileLoader.getData(i));
					pool.execute(new NexusDataPointcreator(container, i, fileLoader, i));
				}
			}
			//last two points in the scan will have the same values
			if(finalFrame == numOfPoints ){
					if((finalFrame - startFrame) == 0)
						//container[finalFrame]= (writeToNexusFile(finalFrame -1, fileLoader.getData(finalFrame-1)));
						pool.execute(new NexusDataPointcreator(container, finalFrame, fileLoader, finalFrame -1));
					else{
						//always one less than the number of points

						for (int i = startFrame; i < finalFrame; i++) {
							logger.info("writing point number " + i);
							//container[i] =(writeToNexusFile(i, fileLoader.getData(i)));
							pool.execute(new NexusDataPointcreator(container, i, fileLoader, i));
						}
						//container[finalFrame] = (writeToNexusFile(finalFrame -1, fileLoader.getData(finalFrame-1)));
						pool.execute(new NexusDataPointcreator(container, finalFrame, fileLoader, finalFrame -1));
					}
			}
			// readSoFar = totalToRead;
			return container;
		} catch (Exception e) {
			logger.error("Unable to load file " + fileName, e);
			throw new DeviceException("Unable to load file " + fileName, e);
		}
	}

	@Override
	public void setContinuousMode(boolean on) throws DeviceException {
		isContinuousMode = on;
		if(on)
			setupContinuousOperation();
		if (!isSlave) {
			if (on) {
				setTimeFrames();
			} else {
				switchOffExtTrigger();
			}
		}
	}

//	private void switchOnExtTrigger() {
//	}

	private void switchOffExtTrigger() {
	}

	private void setTimeFrames() {
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
	}

	@Override
	public void atScanEnd() throws DeviceException {
		try {
			stop();
		} catch (Exception e) {
			logger.error("Unalble to end hdf5 capture", e);
			throw new DeviceException("Unalble to end hdf5 capture", e);
		}
	}

	@Override
	public void atCommandFailure() throws DeviceException {
		try {
			stop();
		} catch (Exception e) {
			logger.error("Unalble to end hdf5 capture", e);
			throw new DeviceException("Unalble to end hdf5 capture", e);
		}
	}

	@Override
	public void atScanLineStart() throws DeviceException {
//		lastFileName = null;
//		lastFileReadStatus = false;
	}

	@Override
	public String[] getExtraNames() {
		return xmap.getExtraNames();
	}

//	private void setupFilename() {
//
//	}

	private void setupContinuousOperation() {
		scanStartTime = System.currentTimeMillis();
	}

	@Override
	public void stop() throws DeviceException {
		xmap.stop();

	}

	@Override
	public NexusTreeProvider readout() throws DeviceException {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isStillWriting(@SuppressWarnings("unused") String fileName) {
		currentTime = System.currentTimeMillis();
		if((currentTime - scanStartTime) <= (long)continuousParameters.getTotalTime() )
			return true;
		return false;
	}

	public void waitForFile(String fileName) throws DeviceException, InterruptedException {
		double timeoutMilliSeconds = getCollectionTime() * continuousParameters.getNumberDataPoints() * 1000;
		double waitedSoFarMilliSeconds = 0;
		int waitTime = 1000;
		while (isStillWriting(fileName) || waitedSoFarMilliSeconds <= timeoutMilliSeconds) {
			Thread.sleep(waitTime);
			waitedSoFarMilliSeconds += waitTime;
		}
		// wait for another second to file to be closed
		//Thread.sleep(3000);

	}

	// File r/w methods
	public NXDetectorData writeToNexusFile(int dataPointNumber, short[][] s) {
		NXDetectorData output = new NXDetectorData(xmap);
		INexusTree detTree = output.getDetTree(xmap.getName());

		int numberOfElements = this.xmap.vortexParameters.getDetectorList().size();
		int numberOfROIs = this.xmap.vortexParameters.getDetectorList().get(0).getRegionList().size();

		// items to write to nexus
		double[] summation = null;
		double[] correctedAllCounts = new double[numberOfElements];
		final List<Double> times = this.xmap.getEventProcessingTimes();
		double[] ocrs = new double[numberOfElements];
		double[] icrs = new double[numberOfElements];
		double[][] roiCounts = new double[numberOfROIs][numberOfElements];
		String[] roiNames = new String[numberOfROIs];
		short detectorData[][] = s;

		for (int element = 0; element < this.xmap.vortexParameters.getDetectorList().size(); element++) {

			DetectorElement thisElement = this.xmap.vortexParameters.getDetectorList().get(element);
			if (thisElement.isExcluded())
				continue;

			// TODO replacae
			final double ocr = getOCR(dataPointNumber, element);
			ocrs[element] = ocr;
			final double icr = getICR(dataPointNumber, element);
			icrs[element] = icr;
			Double deadTimeCorrectionFactor = CorrectionUtils.getK(times.get(element), icr, ocr);

			if (deadTimeCorrectionFactor.isInfinite() || deadTimeCorrectionFactor.isNaN()) {
				deadTimeCorrectionFactor = 0.0;
			}

			// Total counts
			double allCounts = getEvents(dataPointNumber, element);
			correctedAllCounts[element] = allCounts * deadTimeCorrectionFactor;

			// REGIONS
			for (int iroi = 0; iroi < thisElement.getRegionList().size(); iroi++) {

				final DetectorROI roi = thisElement.getRegionList().get(iroi);

				// TODO calculate roi from the full spectrum data
				double count = calculateROICounts(roi, detectorData[element]);
				count *= deadTimeCorrectionFactor;
				roiCounts[iroi][element] = count;
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
		final INexusTree counts = NXDetectorData.addData(detTree, "totalCounts", new NexusGroupData(correctedAllCounts), "counts", 1);
		for (int element = 0; element < numberOfElements; element++) {
			DetectorElement thisElement = this.xmap.vortexParameters.getDetectorList().get(element);
			output.setPlottableValue(thisElement.getName(), correctedAllCounts[element]);
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
		NXDetectorData.addData(detTree, "icr", new NexusGroupData(icrs), "Hz", 1);

		// OCR
		NXDetectorData.addData(detTree, "ocr", new NexusGroupData(ocrs), "Hz", 1);

		// roicounts
		for (int iroi = 0; iroi < numberOfROIs; iroi++) {
			String roiName = roiNames[iroi];
			NXDetectorData.addData(detTree, roiName, new NexusGroupData(roiCounts[iroi]), "counts", 1);
			for (int element = 0; element < numberOfElements; element++) {
				String elementName = this.xmap.vortexParameters.getDetectorList().get(element).getName();
				output.setPlottableValue(elementName + "_" + roiName, roiCounts[iroi][element]);
			}
		}

		// add the full spectrum
		NXDetectorData.addData(detTree, "fullSpectrum", new NexusGroupData(detectorData), "counts", 1);

		// ToDo implement the getROI and readout scanler data
		double ff = calculateScalerData(dataPointNumber, numberOfROIs, detectorData);
		NXDetectorData.addData(detTree, "FF", new NexusGroupData(ff), "counts", 1);
		output.setPlottableValue("FF", ff);

		if (summation != null)
			NXDetectorData.addData(detTree, "allElementSum", new NexusGroupData(summation),
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
			Double correctionFactor = CorrectionUtils.getK(times.get(i), getICR(dataPointNumber, i), getOCR(
					dataPointNumber, i));
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
				double correctedMCA = calculateROICounts(element.getRegionList().get(i),  detectorData[j]) * k[j];
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



	private double calculateROICounts(DetectorROI roi, short[]data)
	{
		int regionLow = roi.getRoiStart();
		int regionHigh = roi.getRoiEnd();
		double count =0.0;
		for (int i = regionLow; i<= regionHigh; i++)
			count += data[i];
		return count;
	}

	public boolean isSlave() {
		return isSlave;
	}

	public void setSlave(boolean isSlave) {
		this.isSlave = isSlave;
	}

	class NexusDataPointcreator implements Runnable{

		private NexusTreeProvider[] dataContainer;
		private int dataIndex;
		private XmapNexusFileLoader filePointer;
		private int fileIndex;
		public NexusDataPointcreator(NexusTreeProvider[] dataContainer, int dataIndex, XmapNexusFileLoader filePointer, int fileIndex)
		{
			this.dataContainer = dataContainer;
			this.dataIndex = dataIndex;
			this.filePointer = filePointer;
			this.fileIndex = fileIndex;
		}
		@Override
		public void run() {
			dataContainer[dataIndex] = writeToNexusFile(fileIndex, filePointer.getData(fileIndex));
		}

	}

}
