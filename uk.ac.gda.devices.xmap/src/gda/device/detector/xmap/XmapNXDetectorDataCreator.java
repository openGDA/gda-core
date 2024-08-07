package gda.device.detector.xmap;

import java.util.List;

import gda.data.nexus.extractor.NexusExtractor;
import gda.data.nexus.extractor.NexusGroupData;
import gda.data.nexus.tree.INexusTree;
import gda.data.nexus.tree.NexusTreeNode;
import gda.device.detector.NXDetectorData;
import gda.device.detector.xmap.util.XmapFileLoader;
import uk.ac.gda.beans.exafs.DetectorROI;
import uk.ac.gda.beans.vortex.DetectorElement;
import uk.ac.gda.util.CorrectionUtils;

public class XmapNXDetectorDataCreator {

	private String[] extraNames;
	private String[] outputFormat;
	private String detectorName;
	private XmapFileLoader fileLoader;
	private List<DetectorElement> detectorElements;
	private boolean deadTimeEnabled;
	private List<Double> eventProcessingTimes;
	private boolean createAllElementsSum;


	public XmapNXDetectorDataCreator(XmapFileLoader fileloader, List<DetectorElement> detectorElements, String[] extraNames, String[] outputFormat, String detectorName, boolean deadtimeEnabled, List<Double> eventProcessingTimes, boolean createAllElementsSum) {
		this.fileLoader = fileloader;
		this.detectorElements = detectorElements;
		this.extraNames = extraNames;
		this.outputFormat = outputFormat;
		this.detectorName = detectorName;
		this.deadTimeEnabled = deadtimeEnabled;
		this.eventProcessingTimes = eventProcessingTimes;
		this.createAllElementsSum = createAllElementsSum;
	}


	public NXDetectorData writeToNexusFile(int dataPointNumber, short[][] detectorData) {
		NXDetectorData output = new NXDetectorData(extraNames, outputFormat, detectorName);
		INexusTree detTree = output.getDetTree(detectorName);

		int numFilteredElements = deriveNumberOfIncludedDetectors();
		int originalNumberOfElements = detectorElements.size();
		int numberOfROIs = detectorElements.get(0).getRegionList().size();

		// items to write to nexus
		double[] summation = null;
		double[] correctedAllCounts = new double[numFilteredElements];
		final List<Double> times = eventProcessingTimes;
		double[] ocrs = new double[numFilteredElements];
		double[] icrs = new double[numFilteredElements];
		double[][] roiCounts = new double[numberOfROIs][numFilteredElements];
		String[] roiNames = new String[numberOfROIs];
		short reducedData[][] = new short[numFilteredElements][];

		int index = -1;
		for (int element = 0; element < originalNumberOfElements; element++) {

			DetectorElement thisElement = detectorElements.get(element);
			if (thisElement.isExcluded())
				continue;
			index++;

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

				final DetectorROI roi = thisElement.getRegionList().get(iroi);

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

			if (createAllElementsSum) {
				if (summation == null)
					summation = new double[detectorData[element].length];
				for (int i = 0; i < detectorData[element].length; i++) {
					summation[i] += detectorData[element][i];
				}
			}
		}

		// add total counts
		final INexusTree counts = NXDetectorData.addData(detTree, "totalCounts", new NexusGroupData(correctedAllCounts), "counts", 1);
		index = -1;
		for (int element = 0; element < originalNumberOfElements; element++) {
			DetectorElement thisElement = detectorElements.get(element);
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
		NXDetectorData.addData(detTree, "icr", new NexusGroupData(icrs), "Hz", 1);

		// OCR
		NXDetectorData.addData(detTree, "ocr", new NexusGroupData(ocrs), "Hz", 1);

		// roicounts
		for (int iroi = 0; iroi < numberOfROIs; iroi++) {
			String roiName = roiNames[iroi];
			NXDetectorData.addData(detTree, roiName, new NexusGroupData(roiCounts[iroi]), "counts", 1);
			index = -1;
			for (int element = 0; element < originalNumberOfElements; element++) {
				DetectorElement detElement = detectorElements.get(element);
				if (detElement.isExcluded())
					continue;
				index++;
				String elementName = detElement.getName();
				output.setPlottableValue(elementName + "_" + roiName, roiCounts[iroi][index]);
			}
		}

		// add the full spectrum
		NXDetectorData.addData(detTree, "fullSpectrum", new NexusGroupData(reducedData), "counts", 1);

		double ff = calculateScalerData(dataPointNumber, numberOfROIs, detectorData);
		NXDetectorData.addData(detTree, "FF", new NexusGroupData(ff), "counts", 1);
		output.setPlottableValue("FF", ff);

		if (createAllElementsSum)
			NXDetectorData.addData(detTree, "allElementSum", new NexusGroupData(summation),
					"counts", 1);
		return output;
	}

	private int deriveNumberOfIncludedDetectors() {
		int included = 0;
		for (DetectorElement element : detectorElements){
			if (!element.isExcluded()) included++;
		}
		return included;
	}


	private double getEvents(int dataPointNumber, int element) {
		double event = fileLoader.getEvents(dataPointNumber, element);
		return event;
	}

	private double calculateScalerData(int dataPointNumber, int numberOfROIs, short[][] detectorData) {
		final double[] k = new double[detectorElements.size()];
		for (int i = 0; i < k.length; i++) {
			Double correctionFactor = CorrectionUtils.getK(eventProcessingTimes.get(i), getICR(dataPointNumber, i),
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
				if (j >= detectorElements.size())
					continue;
				DetectorElement element = detectorElements.get(j);
				if (element.isExcluded())
					continue;
				DetectorROI region = element.getRegionList().get(0);
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

}
