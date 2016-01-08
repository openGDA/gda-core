/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council
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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.data.nexus.extractor.NexusExtractor;
import gda.data.nexus.extractor.NexusGroupData;
import gda.data.nexus.tree.INexusTree;
import gda.data.nexus.tree.NexusTreeNode;
import gda.data.nexus.tree.NexusTreeProvider;
import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.detector.NXDetectorData;
import gda.device.detector.NexusDetector;
import uk.ac.gda.beans.DetectorROI;
import uk.ac.gda.beans.vortex.DetectorElement;
import uk.ac.gda.beans.vortex.VortexParameters;
import uk.ac.gda.util.CorrectionUtils;

/**
 * Version of Xmap which returns its data in a format which works with the NexusFileWriter better than raw data
 */
public class NexusXmap extends XmapwithSlaveMode implements NexusDetector {
	private static final long serialVersionUID = 7628345757698564553L;
	private static final Logger logger = LoggerFactory.getLogger(NexusXmap.class);
	private boolean sumAllElementData = false;
	private int numberOfElements;
	private boolean calculateUnifiedRois = false;

	/**
	 * If true, then always write non-deadtime corrected MCAs to nexus file, irrespective of any other settings.
	 * <p>
	 * So this is an override of the saveRawSpectrum which is temporarily stopping deadtime corrections for diagnostic
	 * purposes.
	 */
	private boolean alwaysRecordRawMCAs = false;

	@Override
	public NexusTreeProvider readout() throws DeviceException {

		if (controller.getStatus() == Detector.BUSY) {
			// We must call stop before reading out.
			controller.stop();
			// We must wait here if the controller is still busy.
			int total = 0;
			while (controller.getStatus() == Detector.BUSY) {
				try {
					logger.info("detector busy waiting to stop");
					Thread.sleep(100);
					total += 100;
					// We don't wait more than 5seconds.
					if (total >= 5000)
						break;
				} catch (InterruptedException e) {
					// logger.error("Sleep interrupted", e);
				}
			}
		}

		NXDetectorData output = new NXDetectorData(this);
		INexusTree detTree = output.getDetTree(getName());

		int numFilteredElements = getNumberOfIncludedDetectors();
		int originalNumberOfElements = vortexParameters.getDetectorList().size();
		int numberOfROIs = vortexParameters.getDetectorList().get(0).getRegionList().size();

		int detectorData[][] = controller.getData();

		// items to write to nexus
		double[] summation = null;
		double[] correctedAllCounts = new double[numFilteredElements];
		final List<Double> times = getEventProcessingTimes();
		double[] ocrs = new double[numFilteredElements];
		double[] icrs = new double[numFilteredElements];
		double[][] roiCounts = new double[numberOfROIs][numFilteredElements];
		String[] roiNames = new String[numberOfROIs];
		double correctedDetData[][] = new double[numFilteredElements][];
		double reducedDetectorData[][] = new double[numFilteredElements][];
		double[] unifiedRegionCounts = new double[numberOfROIs];

		// create deadtime corrected values
		int index = -1;
		for (int element = 0; element < originalNumberOfElements; element++) {

			DetectorElement thisElement = vortexParameters.getDetectorList().get(element);
			if (thisElement.isExcluded())
				continue;
			index ++;

			final double ocr = controller.getOCR(element);
			ocrs[index] = ocr;
			final double icr = controller.getICR(element);
			icrs[index] = icr;
			Double deadTimeCorrectionFactor = CorrectionUtils.getK(times.get(element), icr, ocr);

			if (deadTimeCorrectionFactor.isInfinite() || deadTimeCorrectionFactor.isNaN() || saveRawSpectrum)
				deadTimeCorrectionFactor = 1.0;

			// Total counts
			int allCounts = controller.getEvents(element);
			correctedAllCounts[index] = allCounts * deadTimeCorrectionFactor;

			// REGIONS
			double[] elementROIs = controller.getROIs(element, detectorData);
			for (int iroi = 0; iroi < thisElement.getRegionList().size(); iroi++) {
				final DetectorROI roi = thisElement.getRegionList().get(iroi);
				double count = elementROIs[iroi];//getROICounts(iroi)[element];
				count *= deadTimeCorrectionFactor;
				roiCounts[iroi][index] = count;
				roiNames[iroi] = roi.getRoiName();
			}

			// full mca
			correctedDetData[index] = new double[detectorData[element].length];
			reducedDetectorData[index] = new double[detectorData[element].length];
			for (int specElement = 0; specElement < detectorData[element].length; specElement++) {
				correctedDetData[index][specElement] = detectorData[index][specElement] * deadTimeCorrectionFactor;
				reducedDetectorData[index][specElement] = detectorData[index][specElement];
			}

			if (sumAllElementData) {
				if (summation == null)
					summation = new double[correctedDetData[index].length];
				for (int i = 0; i < correctedDetData[index].length; i++) {
					summation[i] += correctedDetData[index][i];
				}
			}
			if (calculateUnifiedRois) {
				for (int iroi = 0; iroi < numberOfROIs; iroi++) {
					unifiedRegionCounts[iroi] += roiCounts[iroi][index];
				}
			}
		}

		// add total counts
		final INexusTree counts = NXDetectorData.addData(detTree, "totalCounts", new NexusGroupData(correctedAllCounts), "counts", 1);
		index = -1;
		for (int element = 0; element < originalNumberOfElements; element++) {
			DetectorElement thisElement = vortexParameters.getDetectorList().get(element);
			if (thisElement.isExcluded())
				continue;
			index++;
			output.setPlottableValue(thisElement.getName(), correctedAllCounts[index]);
		}

		// event processing time.
		String evtProcessTimeAsString = "";
		for (int element = 0; element < originalNumberOfElements; element++) {
			DetectorElement thisElement = vortexParameters.getDetectorList().get(element);
			if (thisElement.isExcluded())
				continue;
			evtProcessTimeAsString += times.get(element) + " ";
		}
		evtProcessTimeAsString = evtProcessTimeAsString.trim();
		counts.addChildNode(new NexusTreeNode("eventProcessingTime", NexusExtractor.AttrClassName, counts, new NexusGroupData(evtProcessTimeAsString)));

		// ICR
		NXDetectorData.addData(detTree, "icr", new NexusGroupData(icrs), "Hz", 1);

		// OCR
		NXDetectorData.addData(detTree, "ocr", new NexusGroupData(ocrs), "Hz", 1);

		// roicounts
		double ffFromRoi = 0.0;
		for (int iroi = 0; iroi < numberOfROIs; iroi++) {
			String roiName = roiNames[iroi];
			NXDetectorData.addData(detTree, roiName, new NexusGroupData(roiCounts[iroi]), "Hz", 1);
			index = -1;
			for (int element = 0; element < originalNumberOfElements; element++) {
				DetectorElement detElement = vortexParameters.getDetectorList().get(element);
				if (detElement.isExcluded())
					continue;
				index++;
				String elementName = detElement.getName();
				output.setPlottableValue(elementName + "_" + roiName, roiCounts[iroi][index]);
				ffFromRoi +=roiCounts[iroi][index];
			}
		}

		// add the full spectrum
		if (saveRawSpectrum || alwaysRecordRawMCAs){
			if (originalNumberOfElements == 1) {
				NXDetectorData.addData(detTree, "fullSpectrum", new NexusGroupData(reducedDetectorData[0]), "counts", 1);
			} else {
				NXDetectorData.addData(detTree, "fullSpectrum", new NexusGroupData(reducedDetectorData), "counts", 1);
			}

		} else {
			if (originalNumberOfElements == 1) {
				NXDetectorData.addData(detTree, "fullSpectrum", new NexusGroupData(correctedDetData[0]), "counts", 1);
			} else {
				NXDetectorData.addData(detTree, "fullSpectrum", new NexusGroupData(correctedDetData), "counts", 1);
			}
		}
		double ff = ffFromRoi;
		NXDetectorData.addData(detTree, "FF", new NexusGroupData(ff), "counts", 1);
		output.setPlottableValue("FF", ff);
		if (saveRawSpectrum) {
			for (int element = 0; element < originalNumberOfElements; element++) {
				DetectorElement thisElement = vortexParameters.getDetectorList().get(element);
				if (thisElement.isExcluded())
					continue;
				output.setPlottableValue(getIcrColumnName(element), icrs[element]);
				output.setPlottableValue(getOcrColumnName(element), ocrs[element]);
			}
		}
		if (calculateUnifiedRois) {
			for (int iRoi = 0; iRoi < numberOfROIs; iRoi++) {
				DetectorROI roi = vortexParameters.getDetector(0).getRegionList().get(iRoi);
				NXDetectorData.addData(detTree, roi.getRoiName() + "_ElementSum",
						new NexusGroupData(unifiedRegionCounts[iRoi]), "counts", 1);
				output.setPlottableValue(roi.getRoiName(), unifiedRegionCounts[iRoi]);
			}
		}
		if (summation != null)
			NXDetectorData.addData(detTree, "allElementSum", new NexusGroupData(summation), "counts", 1);
		return output;
	}

	@Override
	public Object getPosition() throws DeviceException {
		NXDetectorData readout = (NXDetectorData) readout();
		Double[] position = readout.getDoubleVals();
		assert position.length == getOutputFormat().length :
			"getPosition().length != getOutputFormat().length";
		return position;
	}

	@Override
	public void loadConfigurationFromFile() throws Exception {
		super.loadConfigurationFromFile();
		// Define all the extra names
		final List<String> names = new ArrayList<String>(31);
		for (int element = 0; element < vortexParameters.getDetectorList().size(); element++) {
			DetectorElement thisElement = vortexParameters.getDetectorList().get(element);
			if (thisElement.isExcluded())
				continue;
			final String elementName = thisElement.getName();
			names.add(elementName);
			for (int iroi = 0; iroi < thisElement.getRegionList().size(); iroi++) {
				final DetectorROI roi = thisElement.getRegionList().get(iroi);
				names.add(elementName + "_" + roi.getRoiName());
			}
		}
	}

	@Override
	public String[] getExtraNames() {
		// rebuild as the ROIs could have changed
		String[] extraNames = new String[0];
		for (int element = 0; element < vortexParameters.getDetectorList().size(); element++) {
			DetectorElement thisElement = vortexParameters.getDetectorList().get(element);
			if (thisElement.isExcluded())
				continue;
			final String elementName = thisElement.getName();
			extraNames = (String[]) ArrayUtils.add(extraNames, elementName);
			for (DetectorROI roi : thisElement.getRegionList())
				extraNames = (String[]) ArrayUtils.add(extraNames, elementName + "_" + roi.getRoiName());
		}
		extraNames = (String[]) ArrayUtils.add(extraNames, "FF");
		if (saveRawSpectrum) {
			// previously numberOfElements was not initialised
			numberOfElements = vortexParameters.getDetectorList().size();
			for (int element = 0; element < numberOfElements; element++) {
				extraNames = (String[]) ArrayUtils.add(extraNames, getIcrColumnName(element));
				extraNames = (String[]) ArrayUtils.add(extraNames,  getOcrColumnName(element));
			}
		}
		if (calculateUnifiedRois) {
			DetectorElement repElement = vortexParameters.getDetector(0);
			int numberOfROIs = repElement.getRegionList().size();
			for (int i = 0; i < numberOfROIs; i++) {
				final DetectorROI roi = repElement.getRegionList().get(i);
				extraNames = (String[]) ArrayUtils.add(extraNames, roi.getRoiName());
			}
		}
		return extraNames;
	}

	@Override
	public String[] getOutputFormat() {
		String[] basicFormat = super.getOutputFormat();
		int formatLength = getInputNames().length + getExtraNames().length;
		String[] format = new String[formatLength];
		for (int i  = 0; i < formatLength; i++){
			format[i] = basicFormat[0];
		}
		return format;
	}

	@Override
	public int[] getDataDimensions() throws DeviceException {
		return new int[] { getExtraNames().length };
	}

	public void setSumAllElementData(boolean sumAllElementData) {
		this.sumAllElementData = sumAllElementData;
	}

	public boolean isSumAllElementData() {
		return sumAllElementData;
	}

	public boolean isCaclulateUnifiedRois() {
		return calculateUnifiedRois;
	}

	public void setCalculateUnifiedRois(boolean calculateUnifiedRois) {
		this.calculateUnifiedRois = calculateUnifiedRois;
	}

	public boolean isAlwaysRecordRawMCAs() {
		return alwaysRecordRawMCAs;
	}

	public void setAlwaysRecordRawMCAs(boolean alwaysRecordRawMCAs) {
		this.alwaysRecordRawMCAs = alwaysRecordRawMCAs;
	}

	private String[] getElementNames() {
		String[] names = new String[vortexParameters.getDetectorList().size()];
		for(int i = 0; i < names.length; i++)
			names[i] = vortexParameters.getDetectorList().get(i).getName();
		return names;
	}

	private String getIcrColumnName(int elementNumber){
		return getElementNames()[elementNumber]+ "_icr";
	}

	private String getOcrColumnName(int elementNumber){
		return getElementNames()[elementNumber]+ "_ocr";
	}

	public int getNumberOfIncludedDetectors() {
		int numFilteredDetectors = 0;
		for (int element = 0; element < vortexParameters.getDetectorList().size(); element++)
			if (!vortexParameters.getDetectorList().get(element).isExcluded())
				numFilteredDetectors++;
		return numFilteredDetectors;
	}

	public VortexParameters getVortexParameters(){
		return this.vortexParameters;
	}


}
