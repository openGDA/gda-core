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
import gda.data.nexus.extractor.NexusExtractor;
import gda.data.nexus.extractor.NexusGroupData;
import gda.data.nexus.tree.INexusTree;
import gda.data.nexus.tree.NexusTreeNode;
import gda.data.nexus.tree.NexusTreeProvider;
import gda.device.DeviceException;
import gda.device.detector.NXDetectorData;
import gda.device.detector.xmap.util.XmapNexusFileLoader;
import gda.device.scannable.PositionInputStream;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Vector;

import org.nexusformat.NexusFile;

import uk.ac.gda.beans.vortex.DetectorElement;
import uk.ac.gda.beans.vortex.RegionOfInterest;
import uk.ac.gda.util.CorrectionUtils;

class XmapPositionInputStream implements PositionInputStream<NexusTreeProvider>
{
	/**
	 * 
	 */
	private final HardwareTriggeredNexusXmap hardwareTriggeredNexusXmap;

	/**
	 * @param hardwareTriggeredNexusXmap
	 */
	XmapPositionInputStream(HardwareTriggeredNexusXmap hardwareTriggeredNexusXmap) {
		this.hardwareTriggeredNexusXmap = hardwareTriggeredNexusXmap;
	}

	private int readSoFar =0;
	private XmapNexusFileLoader fileLoader ;
	
	@Override
	public Vector<NexusTreeProvider> read(int maxToRead) throws NoSuchElementException, InterruptedException, DeviceException  {
		String fileName=null;
		try {
			System.out.println("wating for file");
			 fileName= this.hardwareTriggeredNexusXmap.getController().getHDFFileName();
			 
			Vector <NexusTreeProvider> container = new Vector<NexusTreeProvider>();
			hardwareTriggeredNexusXmap.waitForFile(fileName);
			//change to linux format
			String beamline = LocalProperties.get("gda.factory.factoryName","");
			beamline = beamline.toLowerCase();
			fileName = fileName.replace("X:/", "/dls");
			fileLoader = new XmapNexusFileLoader(fileName);			
			fileLoader.loadFile();
		
		int totalToRead = readSoFar + maxToRead;
		int numOfPoints = fileLoader.getNumberOfDataPoints();
		if(totalToRead < numOfPoints)
			totalToRead = numOfPoints;
		for(int i =readSoFar  ; i <  totalToRead; i++)
		{
			container.add(writeToNexusFile(i, fileLoader.getData(i)));
		}
		readSoFar = totalToRead;
		return container;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			HardwareTriggeredNexusXmap.logger.error("TODO put description of error here", e);
			throw new DeviceException("Unable to load file " + fileName , e);
		}
	}

	


	public NXDetectorData writeToNexusFile(int dataPointNumber, short[][] s) throws DeviceException {
		NXDetectorData output = new NXDetectorData(this.hardwareTriggeredNexusXmap);
		INexusTree detTree = output.getDetTree(this.hardwareTriggeredNexusXmap.getName());

		int numberOfElements = this.hardwareTriggeredNexusXmap.getXmap().vortexParameters.getDetectorList().size();
		int numberOfROIs = this.hardwareTriggeredNexusXmap.getXmap().vortexParameters.getDetectorList().get(0).getRegionList().size();

		// items to write to nexus
		double[] summation = null;
		double[] correctedAllCounts = new double[numberOfElements];
		final List<Double> times = this.hardwareTriggeredNexusXmap.getXmap().getEventProcessingTimes();
		double[] ocrs = new double[numberOfElements];
		double[] icrs = new double[numberOfElements];
		double[][] roiCounts = new double[numberOfROIs][numberOfElements];
		String[] roiNames = new String[numberOfROIs];
		short detectorData[][] = s;

		for (int element = 0; element < this.hardwareTriggeredNexusXmap.getXmap().vortexParameters.getDetectorList().size(); element++) {

			DetectorElement thisElement = this.hardwareTriggeredNexusXmap.getXmap().vortexParameters.getDetectorList().get(element);
			if (thisElement.isExcluded())
				continue;

			//TODO replacae
			final double ocr = getOCR(dataPointNumber,element);
			ocrs[element] = ocr;
			final double icr = getICR(dataPointNumber,element);
			icrs[element] = icr;
			Double deadTimeCorrectionFactor = CorrectionUtils.getK(times.get(element), icr, ocr);

			if (deadTimeCorrectionFactor.isInfinite() || deadTimeCorrectionFactor.isNaN()) {
				deadTimeCorrectionFactor = 0.0;
			}

			// Total counts
			double allCounts = getEvents(dataPointNumber,element);
			correctedAllCounts[element] = allCounts * deadTimeCorrectionFactor;

			// REGIONS
			for (int iroi = 0; iroi < thisElement.getRegionList().size(); iroi++) {

				final RegionOfInterest roi = thisElement.getRegionList().get(iroi);
				
				//TODO calculate roi from the full spectrum data
				double count = calculateROICounts(iroi, element, detectorData[element]);
				count *= deadTimeCorrectionFactor;
				roiCounts[iroi][element] = count;
				roiNames[iroi] = roi.getRoiName();
			}

			if (this.hardwareTriggeredNexusXmap.getXmap().isSumAllElementData()) {
				if (summation == null)
					summation = new double[detectorData[element].length];
				for (int i = 0; i < detectorData[element].length; i++) {
					summation[i] += detectorData[element][i];
				}
			}

		}

		// add total counts
		final INexusTree counts = output.addData(detTree, "totalCounts", new int[] { numberOfElements },
				NexusFile.NX_FLOAT64, correctedAllCounts, "counts", 1);
		for (int element = 0; element < numberOfElements; element++) {
			DetectorElement thisElement = this.hardwareTriggeredNexusXmap.getXmap().vortexParameters.getDetectorList().get(element);
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
		output.addData(detTree, "icr", new int[] { numberOfElements }, NexusFile.NX_FLOAT64, icrs, "Hz", 1);

		// OCR
		output.addData(detTree, "ocr", new int[] { numberOfElements }, NexusFile.NX_FLOAT64, ocrs, "Hz", 1);

		// roicounts
		for (int iroi = 0; iroi < numberOfROIs; iroi++) {
			String roiName = roiNames[iroi];
			output.addData(detTree, roiName, new int[] { numberOfElements }, NexusFile.NX_FLOAT64, roiCounts[iroi],
					"Hz", 1);
			for (int element = 0; element < numberOfElements; element++) {
				String elementName = this.hardwareTriggeredNexusXmap.getXmap().vortexParameters.getDetectorList().get(element).getName();
				output.setPlottableValue(elementName + "_" + roiName, roiCounts[iroi][element]);
			}
		}

		// add the full spectrum
		output.addData(detTree, "fullSpectrum", new int[] { numberOfElements, detectorData[0].length },
				NexusFile.NX_INT16, detectorData, "counts", 1);

		//ToDo implement the getROI and readout scanler data
		double ff = calculateScalerData(dataPointNumber, numberOfROIs,detectorData);
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


	private double calculateScalerData(int dataPointNumber, int numberOfROIs, short[][] detectorData) throws DeviceException {
		final double[]k = new double[this.hardwareTriggeredNexusXmap.getXmap().vortexParameters.getDetectorList().size()];
		final List<Double> times = this.hardwareTriggeredNexusXmap.getXmap().getEventProcessingTimes();
		for (int i = 0; i < k.length; i++) {
			Double correctionFactor = CorrectionUtils.getK(times.get(i), getICR(dataPointNumber, i),getOCR(dataPointNumber, i));
			if (correctionFactor.isInfinite() || correctionFactor.isNaN() || correctionFactor == 0.0){
				correctionFactor = 1.0;
			}
			k[i] = correctionFactor;
		}

		
		// Correct mca counts using K as we go
		Double[] rois = new Double[numberOfROIs];
		for (int i = 0; i < rois.length; i++) {
			rois[i] = 0.0;
			for (int j = 0; j < k.length; j++) {
				if (j>=this.hardwareTriggeredNexusXmap.getXmap().vortexParameters.getDetectorList().size()) continue;
				DetectorElement element = this.hardwareTriggeredNexusXmap.getXmap().vortexParameters.getDetectorList().get(j);
				if (element.isExcluded()) continue;
				double correctedMCA = calculateROICounts(i, j,detectorData[j])*k[j];
				rois[i]+=correctedMCA;
			}
 		}
		double ff = 0;
		for (double roi : rois) {
			ff += roi;
		}
		return ff;
	}


	private double getICR(int dataPointNumber,int element) {
		double trigger =0.0;
		double liveTime = 0.0;
		
			trigger = fileLoader.getTrigger(dataPointNumber, element);
			liveTime = fileLoader.getLiveTime(dataPointNumber, element);
			if(trigger != 0.0 && liveTime != 0.0)
				return trigger / liveTime;
		return 0.0;
	}
	private double getOCR(int dataPointNumber, int element) {
		double event = fileLoader.getEvents(dataPointNumber, element);
		double realTime = fileLoader.getRealTime(dataPointNumber, element);
		if(event != 0.0 && realTime != 0.0)
			return event / realTime;
		return 0;
	}
	
	
	private double calculateROICounts(int roiIndex, int elementIndex, short[]data) throws DeviceException
	{
		int regionLow = (int)this.hardwareTriggeredNexusXmap.getController().getSubDetector(elementIndex).getLowROIs()[roiIndex];
		int regionHigh = (int)this.hardwareTriggeredNexusXmap.getController().getSubDetector(elementIndex).getHighROIs()[roiIndex];
		double count =0.0;
		for (int i = regionLow; i<= regionHigh; i++)
			count += data[i];
		return count;
	}
	
	
}