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

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;

import org.eclipse.dawnsci.analysis.api.io.ScanFileHolderException;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.DoubleDataset;
import org.eclipse.january.dataset.Slice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.data.nexus.extractor.NexusGroupData;
import gda.data.nexus.tree.INexusTree;
import gda.device.DeviceException;
import gda.device.detector.NXDetectorData;
import gda.device.detector.xspress.Xspress2Detector;
import uk.ac.gda.beans.vortex.DetectorElement;
import uk.ac.gda.beans.xspress.XspressParameters;

/**
 * Class to read out detector data from Xspress4 detector and assemble NXDetectorData object for
 * writing into Nexus file.
 * Refactored from {@link Xspress4Detector}
 * @since 28/9/2018
 */
public class Xspress4NexusTree {

	private static final Logger logger = LoggerFactory.getLogger(Xspress4NexusTree.class);

	protected final Xspress4Detector detector;
	private final Xspress4Controller controller;
	protected final int numberDetectorElements;

	public Xspress4NexusTree(Xspress4Detector detector) {
		this.detector = detector;
		controller = detector.getController();
		numberDetectorElements = detector.getNumberOfElements();
	}

	public NXDetectorData getDetectorData() throws DeviceException {
		// Construct nexus tree, add the detector data to it
		NXDetectorData frame = new NXDetectorData(detector.getExtraNames(), detector.getOutputFormat(), detector.getName());
		INexusTree detTree = frame.getDetTree(detector.getName());

		XspressParameters parameters = (XspressParameters) detector.getConfigurationParameters();

		boolean regionOfInterest = parameters.getReadoutMode().equals(XspressParameters.READOUT_MODE_REGIONSOFINTEREST);
		int numRois = parameters.getDetector(0).getRegionList().size();

		int mcaGrades = detector.getMcaGrades();

		// Get in window scaler counts for each element and calculate the FF sum (for 'included' detector elements only.
		double[] window1CountData = readoutScaler(5);
		double[] window2CountData = null;
		if (regionOfInterest && numRois==2) {
			window2CountData = readoutScaler(6);
		}
		// NB, when using multiple ROIs there seems to be only 1 'plottable' FF value...
		if (regionOfInterest) {
			if (parameters.getRegionType().equals(XspressParameters.ROI_SCA_WINDOW)) {
				if (mcaGrades == Xspress2Detector.ALL_RES) {
					// Resolution grades for multiple ROIs is not supported, (wasn't previously supported by Xspress2,
					// so getExtraNames() doesn't have necessary columns)
					addResolutionGradeData(detTree, frame, 0);
				} else if (mcaGrades == Xspress2Detector.NO_RES_GRADE) {
					for(int i=0; i<Math.min(numRois, 2); i++) {
						addResolutionGradeSumData(detTree, frame, i);
					}
				} else if (mcaGrades == Xspress2Detector.RES_THRES) {
					addResolutionThresholdData(detTree, frame, 0);
				}
			} else if (parameters.getRegionType().equals(XspressParameters.ROI_VIRTUAL)) {
				calculateVirtualRegions(detTree, frame, parameters);
			}
		} else {
			double[] dtcFactors = controller.getDeadtimeCorrectionFactors();
			addScalerData(detTree, frame, window1CountData, window2CountData, dtcFactors);
		}

		// Add deadtime related scaler data to Ascii and Nexus
		addDeadtimeScalerData(detTree, frame);

		return frame;
	}

	/** Scaler number for 'raw scaler in-window' values */
	private int inWindowCountsScalerNumber = 5;

	/**
	 * Convert scalar data and dtc factor data into a NXDetectorData object
	 *
	 * @param scalerData list of datasets containing scalar data (one per scalar type, shape =[numFrames, num channels]
	 * @param dtcFactorData dataset containing DTC data  shape = [num frames, num channels]
	 * @return
	 * @throws DeviceException
	 * @throws NexusException
	 * @throws ScanFileHolderException
	 */
	public NXDetectorData[] getNXDetectorData(List<Dataset> scalerData, Dataset dtcFactorData) throws DeviceException, NexusException, ScanFileHolderException {

		logger.info("Getting NXDetector data from scalar and DTC factor data");
		logger.debug("Scalar data     : {} dataset, shape = {}", scalerData.size(), scalerData.get(0));
		logger.debug("DTC factor data : shape = {}", dtcFactorData);

		if (scalerData.size() != controller.getNumScalers()) {
			throw new IllegalArgumentException("Scaler dataset list does not match expected length. Expected "+controller.getNumScalers()+" values were found "+scalerData.size());
		}

		// Check DTC and scaler datasets all have the same shape
		Optional<Dataset> shapeMismatchData = scalerData.stream()
			.filter(dataSet -> !Arrays.equals(dataSet.getShape(), dtcFactorData.getShape()))
			.findAny();

		if (shapeMismatchData.isPresent()) {
			int ind = scalerData.indexOf(shapeMismatchData.get());
			throw new IllegalArgumentException("Scaler data "+ind+" and DTC data shapes do not match. Expected "+dtcFactorData.toString()+", found "+shapeMismatchData.get());
		}


		int[] dataShape = scalerData.get(0).getShape();
		int numScalers = scalerData.size();
		int numFrames = dataShape[0];

		// Create dataset to store all scaler values from all detector elements for 1 frame
		Dataset scalerDataset = DatasetFactory.zeros(DoubleDataset.class, numberDetectorElements, numScalers);

		String[] extraNames = detector.getExtraNames();
		String[] outputFormat = detector.getOutputFormat();

		// Add data for each frame
		NXDetectorData[] results = new NXDetectorData[numFrames];
		for (int frame = 0; frame < numFrames; frame++) {
			double[] windowCounts = new double[numberDetectorElements];
			double[] dtcFactors = new double[numberDetectorElements];

			// Get the data to be added to the frame.
			for(int i=0; i<numberDetectorElements; i++) {
				// raw in window counts for element
				windowCounts[i] = scalerData.get(inWindowCountsScalerNumber).getDouble(frame, i);

				// Set all scaler values for current frame for detector element
				for(int j=0; j<numScalers; j++) {
					scalerDataset.set( scalerData.get(j).getDouble(frame, i), i, j);
				}
				dtcFactors[i] = dtcFactorData.getDouble(frame, i);
			}

			NXDetectorData thisFrame = new NXDetectorData(extraNames, outputFormat, detector.getName());
			INexusTree detTree = thisFrame.getDetTree(detector.getName());
			addScalerData(detTree, thisFrame, windowCounts, null, dtcFactors);
			addDeadtimeScalerData(detTree, thisFrame, scalerDataset, dtcFactors);
			results[frame] = thisFrame;
		}
		return results;
	}



	/**
	 * Readout scaler values for each detector element
	 *
	 * @param scalerNumber
	 *  index of scaler to get values from (0...7), corresponding to following quantities :
	 *            <li>0 = time
	 *            <li>1 = reset ticks
	 *            <li>2 = reset counts
	 *            <li>3 = all events
	 *            <li>4 = all good events
	 *            <li>5 = counts in window 1
	 *            <li>6 = counts in window 2
	 *            <li>7 = pileup events
	 * @return 1-d array containing scaler the value for each detector element.
	 * @throws DeviceException
	 * @throws IOException
	 */
	private double[] readoutScaler(int scalerNumber) throws DeviceException {
		if (scalerNumber < 0 || scalerNumber > 7) {
			logger.warn("Scaler number {} is outside of expected range 0...7", scalerNumber);
			return null;
		}

		double[] results = new double[numberDetectorElements];
		for (int i = 0; i < numberDetectorElements; i++) {
			results[i] = controller.getScalerValue(i, scalerNumber);
		}

		return results;
	}

	/**
	 * Add in-window resolution grade data (for all 16 resolution grades) to Nexus tree and plottable data
	 * @param detectorTree
	 * @throws IOException
	 */
	private void addResolutionGradeData(INexusTree detectorTree, NXDetectorData frame, int windowNumber) throws DeviceException {
		Dataset thresholdData = getResGradeData(windowNumber);
		double[] dtcFactors = controller.getDeadtimeCorrectionFactors();

		XspressParameters parameters = (XspressParameters) detector.getConfigurationParameters();
		String roiName = parameters.getDetector(0).getRegionList().get(windowNumber).getRoiName();

		NXDetectorData.addData(detectorTree, roiName+"_resgrade", NexusGroupData.createFromDataset(thresholdData), "counts", 1);

		// Following data are plotted and put in Ascii file, but *not* added to Nexus (same as Xspress2)
		int numDetectorElements = thresholdData.getShape()[0];
		int numResgrades = thresholdData.getShape()[1];
		boolean showOnlyFF = parameters.isOnlyShowFF();

		for(int i=0; i<numDetectorElements; i++) {
			logger.debug("Sum over threshold for {} : {}", i, (double) thresholdData.getSlice(new int[]{i, 0}, new int[]{i+1, numResgrades}, null).sum());
		}

		// Compute 'res_bin_norm' values (value0 = 15, value1 = 15+14, value2 = 15+14+13 etc.)
		// (Deadtime corrected)
		String[] extraNames = detector.getExtraNames();
		double[] resgradeSumArray = new double[numResgrades];
		if (!showOnlyFF) {
			double i0Counts = detector.getI0();
			int nameIndex = 0;
			// sum resgrades from n...15, n=15,14,13...1
			for(int numGradesInSum = 1; numGradesInSum<=numResgrades; numGradesInSum++) {
				Dataset gradeSum = getSumOverResgrades(thresholdData, dtcFactors, numResgrades-numGradesInSum, numResgrades);
				resgradeSumArray[nameIndex] = (double)gradeSum.sum();
				frame.setPlottableValue(extraNames[nameIndex], resgradeSumArray[nameIndex]/i0Counts);
				nameIndex++;
			}
		}

		// best 8 resolution grades per detector element (sum taken from Xspress2NexusTreeProvider.extractPartialMCA)
		// (*not* deadtime corrected)
		for(int i=0; i<numDetectorElements; i++) {
			double best8Sum = (double) thresholdData.getSlice(new int[]{i, numResgrades-8}, new int[]{i+1, numResgrades}, null).sum();
			if (!showOnlyFF) {
				frame.setPlottableValue(extraNames[i+numResgrades], best8Sum);
			}
		}

		// FFsum (sum over all resolution grades, over all elements)
		double FFsum = resgradeSumArray[numResgrades-1];
		frame.setPlottableValue("FF", FFsum);
		NXDetectorData.addData(detectorTree, "FF", new NexusGroupData(FFsum), "counts", 1);
	}

	/**
	 * Return non deadtime corrected resolution grade data (in window counts for each detector element for all resolution grades)
	 * @return Dataset [num detector elements, num res grades]
	 * @throws IOException
	 */
	private Dataset getResGradeData(int windowNumber) throws DeviceException {
		Dataset thresholdData;

		// Read resolution grade data from PVs, add to nexus tree
		double[][] resgradeForDetectorElement = new double[numberDetectorElements][];
		for (int i = 0; i < numberDetectorElements; i++) {
			resgradeForDetectorElement[i] = controller.getResGradeArrays(i, windowNumber);
		}
		// Make Dataset to add to nexustree (NexusGroup can't be created for Double[][])
		thresholdData = DatasetFactory.createFromObject(resgradeForDetectorElement);

		return thresholdData;
	}

	/**
	 * Compute sum over specified resolution grades for each detector element :
	 * @param thresholdData resolution grade data [num detector elements, num resolution grades]
	 * @param dtcFactors deadtime correction factors [num detector elements]; set to null to not apply deadtime correction
	 * @param start start grade for sum
	 * @param end end grade for sum (exclusive)
	 * @return Dataset [num detector elements]
	 */
	private Dataset getSumOverResgrades(Dataset thresholdData, double[] dtcFactors, int start, int end) {
		int numDetectorElements = thresholdData.getShape()[0];
		double[] sum = new double[numDetectorElements];
		for (int element = 0; element < numDetectorElements; element++) {
			// sum the resolution grade values
			sum[element] = (double)thresholdData.getSlice(new int[]{element, start}, new int[]{element+1, end}, null).sum();

			// correct for deadtime
			if (dtcFactors != null) {
				sum[element] *= dtcFactors[element];
			}
		}
		return DatasetFactory.createFromObject(sum);
	}

	/**
	 * Add sum over all resolution grade data to Nexus tree and plottable data
	 *
	 * @param detectorTree
	 * @param frame
	 * @param windowNumber
	 * @throws IOException
	 */
	private void addResolutionGradeSumData(INexusTree detectorTree, NXDetectorData frame, int windowNumber) throws DeviceException {
		Dataset thresholdData = getResGradeData(windowNumber);

		XspressParameters parameters = (XspressParameters) detector.getConfigurationParameters();

		boolean showOnlyFF = parameters.isOnlyShowFF();
		int numDetectorElements = thresholdData.getShape()[0];
		int numResGrades = thresholdData.getShape()[1];
		double[] dtcFactors = controller.getDeadtimeCorrectionFactors();

		// Compute FF sum for each detector element
		double[] roiData = new double[numDetectorElements];
		double ffForAllDetElements = 0;
		String roiName = parameters.getDetector(0).getRegionList().get(windowNumber).getRoiName();
		for (int i = 0; i < numDetectorElements; i++) {
			double ffForWindow = 0.0;
			if (!parameters.getDetector(i).isExcluded()) {
				ffForWindow = (double) thresholdData.getSlice(new int[] { i, 0 }, new int[] { i + 1, numResGrades }, null).sum();
				ffForWindow *= dtcFactors[i]; // apply deadtime correction factor
				if (!showOnlyFF) {
					String ffName = parameters.getDetector(i).getName() + "_" + roiName;
					frame.setPlottableValue(ffName, ffForWindow);
				}
			}
			roiData[i] = ffForWindow;
			ffForAllDetElements += ffForWindow;
		}
		// Add FF sum for each element to Nexus
		NXDetectorData.addData(detectorTree, "FF_" + roiName, new NexusGroupData(roiData), "counts", 1);

		// Add the sum over all detector elements
		// (overwrites previously 'plottable' FF value when using multiple ROIs...)
		frame.setPlottableValue("FF", ffForAllDetElements);
		NXDetectorData.addData(detectorTree, "FF", new NexusGroupData(ffForAllDetElements), "counts", 1);
	}

	/**
	 * Add 'good' and 'bad' resolution grade data to Nexus tree and plottable data.
	 * These are the deadtime corrected 'good' and 'bad' in-window counts summed over resolution
	 * grades above and below a user specified threshold :
	 * <li>'Bad' count = sum of grades over range (0, threshold-1).
	 * <li>'Good' count = sum of grades over range (threshold, numResgrades)
	 * @param detectorTree
	 * @param frame
	 * @param windowNumber
	 * @throws IOException
	 */
	public void addResolutionThresholdData(INexusTree detectorTree, NXDetectorData frame, int windowNumber) throws DeviceException {
		Dataset thresholdData = getResGradeData(windowNumber);
		double[] dtcFactors = controller.getDeadtimeCorrectionFactors();
		int numDetectorElements = thresholdData.getShape()[0];
		int numResgrades = thresholdData.getShape()[1];
		int threshold = detector.getResolutionThreshold();

		// Get bad and good grade counts for each detector element (deadtime corrected)

		Dataset badGradeCounts = getSumOverResgrades(thresholdData, dtcFactors, 0, threshold);
		Dataset goodGradeCounts = getSumOverResgrades(thresholdData, dtcFactors, threshold, numResgrades);
		// Sum of bad and good grade counts across all detector elements
		double badGradeCountsAllElements = (double)badGradeCounts.sum();
		double goodGradeCountsAllElements = (double)goodGradeCounts.sum();

		// Add bad and good total counts
		frame.setPlottableValue("FF_bad", badGradeCountsAllElements);
		frame.setPlottableValue("FF", goodGradeCountsAllElements);

		// Good counts for each element
		XspressParameters parameters = (XspressParameters) detector.getConfigurationParameters();
		if (!parameters.isOnlyShowFF()) {
			String[] extraNames = detector.getExtraNames();
			for (int i = 0; i < numDetectorElements; i++) {
				frame.setPlottableValue(extraNames[i], goodGradeCounts.getDouble(i));
			}
		}

		NXDetectorData.addData(detectorTree, "good_counts", NexusGroupData.createFromDataset(goodGradeCounts), "counts", 1);
		NXDetectorData.addData(detectorTree, "bad_counts", NexusGroupData.createFromDataset(badGradeCounts), "counts", 1);
	}

	/**
	 * Add deadtime corrected scaler counts to plottable data; FF to Nexus tree and plottable data
	 * @param detectorTree
	 * @param frame
	 * @param window1CountData
	 * @param window2CountData
	 */
	protected void addScalerData(INexusTree detectorTree, NXDetectorData frame, double[] window1CountData, double[] window2CountData, double[] dtcFactor) {
		String[] extraNames = detector.getExtraNames();

		XspressParameters parameters = (XspressParameters) detector.getConfigurationParameters();
		boolean showOnlyFF = parameters.isOnlyShowFF();

		double FFsum = 0.0;
		int nameIndex = 0;
		for (int i = 0; i < window1CountData.length; i++) {
			if (!parameters.getDetector(i).isExcluded()) {
				// deadtime corrected scaler counts
				double inWindowCount = window1CountData[i]*dtcFactor[i];

				FFsum += inWindowCount;
				if (!showOnlyFF) {
					frame.setPlottableValue(extraNames[nameIndex++], inWindowCount);
				}
				// Add values for window2 if needed
				if (window2CountData!=null && !showOnlyFF) {
					frame.setPlottableValue(extraNames[nameIndex++], window2CountData[i]*dtcFactor[i]);
				}
			}
		}
		frame.setPlottableValue("FF", FFsum);
		NXDetectorData.addData(detectorTree, "FF", new NexusGroupData(FFsum), "counts", 1);

		// Deadtime corrected in-window scaler counts
		double[] dtcWindowCounts = getDtcScalerValues(window1CountData, dtcFactor);
		NXDetectorData.addData(detectorTree, "scalers", new NexusGroupData(dtcWindowCounts), "counts", 1);
	}

	/**
	 * Writes and plots regions specified in XspressParameters as calculated from MCA data.
	 * Additionally, the following datasets are written
	 */
	private void calculateVirtualRegions(INexusTree detTree, NXDetectorData frame, XspressParameters parameters) throws DeviceException {

		var mca = DatasetFactory.createFromObject(controller.getMcaData());

		var elements = controller.getNumElements();

		List<Boolean> elementsIncluded = parameters.getDetectorList().stream().map(element -> !element.isExcluded()).toList();
		var includedElements = (int) elementsIncluded.stream().filter(included -> included).count();
		var rois = parameters.getDetector(0).getRegionList().size();

		Dataset windows = DatasetFactory.zeros(includedElements, rois);

		var extraNamesIterator = Arrays.asList(detector.getExtraNames()).iterator();

		double[] dtcFactors = controller.getDeadtimeCorrectionFactors();

		var includedElementIndex = 0;
		for (var element = 0; element < elements; element++) {
			DetectorElement elementParams = parameters.getDetector(element);
			if (elementParams.isExcluded()) {
				continue;
			}

			var elementMca = mca.getSlice(new Slice(includedElementIndex, includedElementIndex+1)).squeeze();

			var roisList = elementParams.getRegionList();

			double dtcFactor = dtcFactors[element];

			for (int roi = 0; roi < rois; roi++) {
				var roiParams = roisList.get(roi);
				var correctedCounts = (double) elementMca.getSlice(new Slice(roiParams.getRoiStart(), roiParams.getRoiEnd() + 1)).sum() * dtcFactor;

				windows.set(correctedCounts, includedElementIndex, roi);

				plot(frame, extraNamesIterator.next(), correctedCounts);
			}

			includedElementIndex++;
		}

		writeData(detTree, "MCA", NexusGroupData.createFromDataset(mca));

		// sum MCA over elements
		var mcaElementSum = mca.sum(0);
		writeData(detTree, "MCA_ElementSum", NexusGroupData.createFromDataset(mcaElementSum));

		// sum MCA over elements and channels
		var totalCounts = (double) mca.sum();
		writeData(detTree, "totalCounts", new NexusGroupData(totalCounts));

		NXDetectorData.addData(detTree, "elementsIncluded", NexusGroupData.createFromDataset(DatasetFactory.createFromObject(elementsIncluded)), "", null);

		var transposedWindows = windows.transpose();

		// sum windows over elements
		var windowsSum = windows.sum(0);

		for (var roi = 0; roi < parameters.getDetector(0).getRegionList().size(); roi++) {
			var roiName = parameters.getDetector(0).getRegionList().get(roi).getRoiName();
			var slice = new Slice(roi, roi+1);

			writeData(detTree, roiName, NexusGroupData.createFromDataset(transposedWindows.getSlice(slice).squeeze()));
			writeData(detTree, "FF_" + roiName, NexusGroupData.createFromDataset(windowsSum.getSlice(slice)));
		}

		var ff = (double) windows.sum();

		var name = extraNamesIterator.next();
		writeData(detTree, name, new NexusGroupData(ff));
		plot(frame, name, ff);
	}

	private void writeData(INexusTree detectorTree, String name, NexusGroupData data) {
		NXDetectorData.addData(detectorTree, name, data, "counts", null);
	}

	private void plot(NXDetectorData frame, String name, double value) {
		frame.setPlottableValue(name, value);
	}

	/**
	 * Apply deadtime correction factors to 'raw' (i.e. non deadtime corrected) scaler values.
	 * @param rawScalerCounts
	 * @return
	 * @throws IOException
	 */
	private double[] getDtcScalerValues(double[] rawScalerCounts, double[] dtcFactors) {
		double[] dtcScalerCounts = new double[rawScalerCounts.length];
		for(int i=0; i<rawScalerCounts.length; i++) {
			dtcScalerCounts[i] = dtcFactors[i] * rawScalerCounts[i];
		}
		return dtcScalerCounts;
	}

	/**
	 *
	 * @param detectorTree
	 * @param frame
	 * @throws DeviceException
	 */
	private void addDeadtimeScalerData(INexusTree detectorTree, NXDetectorData frame) throws DeviceException {
		Dataset deadtimeScalerData = getDeadtimeScalerData();
		double[] dtcFactors = controller.getDeadtimeCorrectionFactors();
		addDeadtimeScalerData(detectorTree, frame, deadtimeScalerData, dtcFactors);
	}

	/**
	 * Add deadtime correction scaler values to Nexus tree - these are raw, unprocessed scaler values,
	 * @param detectorTree
	 * @param frame
	 * @param deadtimeScalerData 2-dimensional data of scaler values for all detector elements [num detector elements, num scalers],
	 * as returned by {@link #getDeadtimeScalerData()}.
	 * @param dtcFactors array of deadtime correction factors [num detector elements] (e.g. from by {@link Xspress4Controller#getDeadtimeCorrectionFactors()}).
	 * @throws DeviceException
	 */
	protected void addDeadtimeScalerData(INexusTree detectorTree, NXDetectorData frame, Dataset deadtimeScalerData, double [] dtcFactors) throws DeviceException {
		int numElements = deadtimeScalerData.getShape()[0];

		// Copy array of scaler values and add to Nexus tree
		// (each entry is scaler values of particular type for all det. elements)
		for(Entry<String, Integer> entry : detector.getScalerNameIndexMap().entrySet() ) {
			int scalerIndex = entry.getValue();
			double[] scalerData = new double[numElements];
			for(int i=0; i<numElements; i++) {
				scalerData[i] = deadtimeScalerData.getDouble(i, scalerIndex);
			}
			NXDetectorData.addData(detectorTree, entry.getKey(), new NexusGroupData(scalerData), "counts", 1);
		}

		// Add the deadtime factors to Nexus tree
		NXDetectorData.addData(detectorTree, "dtc factors", new NexusGroupData(dtcFactors), "value", 1);

		// Add dead-time correction values for each detector element to to ascii output :
		XspressParameters parameters = (XspressParameters) detector.getConfigurationParameters();
		if (parameters.isShowDTRawValues()) {
			// allEvents, reset ticks, inWindow events, tfgClock
			String[] extraNames = detector.getExtraNames();
//			int nameIndex = ArrayUtils.indexOf(extraNames, "FF")+1;

			//Find index of first column with deadtime correction data (i.e. Element 0_allEvents)
			int nameIndex = 0;
			String firstColumnName = detector.getAsciiScalerNameIndexMap().keySet().iterator().next();
			for(nameIndex=0; nameIndex<extraNames.length; nameIndex++) {
				if (extraNames[nameIndex].endsWith(firstColumnName)) {
					break;
				}
			}

			// Index start for deadtime correction data is immediately after FF column
			for(int i=0; i<numElements; i++) {
				if (!parameters.getDetector(i).isExcluded()) {
					for (int scalerIndex : detector.getAsciiScalerNameIndexMap().values()) {
						frame.setPlottableValue(extraNames[nameIndex++], deadtimeScalerData.getDouble(i, scalerIndex));
					}
				}
			}
		}
	}

	/**
	 * Return deadtime correction data. Num scalers is usually 8;
	 * @return Dataset [num detector elements, num scalers]
	 * @throws IOException
	 */
	private Dataset getDeadtimeScalerData() throws DeviceException {
		double[][] allScalerData = new double[numberDetectorElements][8];  // [num elements][num scalers]

		// Get array of scaler values for each detector element
		for(int i=0; i<numberDetectorElements; i++) {
			allScalerData[i] = controller.getScalerArray(i);
		}
		return DatasetFactory.createFromObject(allScalerData);
	}
}
