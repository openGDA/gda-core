package gda.device.detector.xspress.xspress2data;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Vector;

import org.apache.commons.lang.ArrayUtils;

import gda.data.nexus.extractor.NexusGroupData;
import gda.data.nexus.tree.INexusTree;
import gda.data.nexus.tree.NexusTreeProvider;
import gda.device.detector.NXDetectorData;
import gda.device.detector.xspress.Xspress2Detector;
import uk.ac.gda.beans.DetectorROI;
import uk.ac.gda.beans.vortex.DetectorElement;
import uk.ac.gda.beans.xspress.XspressDeadTimeParameters;
import uk.ac.gda.beans.xspress.XspressParameters;

/**
 * Provides tools to convert raw data from the Xspress2 electronics into
 * structured Nexus tree. Uses the settings in {@link Xspress2CurrentSettings}
 * to define the structure and contents of the Nexus tree.
 *
 * @author rjw82
 *
 */
public class Xspress2NexusTreeProvider {

	private Xspress2CurrentSettings currentSettings;
	private Xspress2DeadtimeTools xspress2SystemDeadtime;
	private XspressDeadTimeParameters xspressDeadTimeParameters;
	private String detectorName;
	private boolean sumAllElementData = false;
	/*
	 * If true, then always write non-deadtime corrected MCAs to nexus file,
	 * irrespective of any other settings.
	 */
	private boolean alwaysRecordRawMCAs = false;

	public Xspress2NexusTreeProvider(String detectorName, Xspress2CurrentSettings detectorSettings) {
		this.detectorName = detectorName;
		xspress2SystemDeadtime = new Xspress2DeadtimeTools();
		currentSettings = detectorSettings;
	}

	/**
	 * When only one res garde, removes the single dimension to convert
	 * [element][resGrade][mca] into a true 2D array of [element][mca]
	 *
	 * @param correctedMCAArrays
	 * @return double[][]
	 */
	public double[][] removeSingleDimensionFromArray(double[][][] correctedMCAArrays) {
		double[][] out = new double[correctedMCAArrays.length][correctedMCAArrays[0][0].length];

		for (int element = 0; element < correctedMCAArrays.length; element++)
			for (int mcaChannel = 0; mcaChannel < correctedMCAArrays[0][0].length; mcaChannel++)
				out[element][mcaChannel] = correctedMCAArrays[element][0][mcaChannel];
		return out;
	}

	public int[][] removeSingleDimensionFromArray(int[][][] rawMCAArrays) {
		int[][] out = new int[rawMCAArrays.length][rawMCAArrays[0][0].length];

		for (int element = 0; element < rawMCAArrays.length; element++)
			for (int mcaChannel = 0; mcaChannel < rawMCAArrays[0][0].length; mcaChannel++)
				out[element][mcaChannel] = rawMCAArrays[element][0][mcaChannel];
		return out;
	}

	public int[][] unpackRawScalerDataToFrames(int[] scalerData, int numFrames) {
		int numberDataPerFrame = 4 * currentSettings.getNumberOfDetectors();
		int[][] unpacked = new int[numFrames][numberDataPerFrame];
		int iterator = 0;
		for (int frame = 0; frame < numFrames; frame++) {
			for (int datum = 0; datum < numberDataPerFrame; datum++) {
				unpacked[frame][datum] = scalerData[iterator];
				iterator++;
			}
		}
		return unpacked;
	}

	int[][] getResGradesSlice(int[][] elementData, int startSlice, int endSlice, int startMCAPosition,
			int endMCAPosition) {
		int[][] out = new int[elementData.length][];

		for (int i = 0; i < elementData.length; i++) {
			int[] data = elementData[i];
			int[] slice = Arrays.copyOfRange(data, startSlice, endSlice + 1);

			int[] paddedArray = new int[currentSettings.getMcaSize()];
			Arrays.fill(paddedArray, 0);

			int j = 0;
			for (int cell = startMCAPosition; cell < endMCAPosition; cell++) {
				paddedArray[cell] = slice[j];
				j++;
			}

			out[i] = paddedArray;
		}

		return out;
	}

	/**
	 * Convert the raw 1D array from DAServer into the 4D data it represents.
	 * <p>
	 * Assumes the packing order: frame,channel,res-grade,energy
	 *
	 * @param rawData
	 * @param numFrames
	 * @param numResGrades
	 * @return int[][][][]
	 */
	public int[][][][] unpackRawDataTo4D(int[] rawData, int numFrames, int numResGrades, int mcaSize, int numberOfDetectors) {
		int[][][][] output = new int[numFrames][numberOfDetectors][numResGrades][mcaSize];
		int i = 0;
		for (int frame = 0; frame < numFrames; frame++) {
			for (int channel = 0; channel < numberOfDetectors; channel++) {
				for (int res_grade = 0; res_grade < numResGrades; res_grade++) {
					for (int energy = 0; energy < mcaSize; energy++) {
						output[frame][channel][res_grade][energy] = rawData[i];
						i++;
					}
				}
			}
		}
		return output;
	}

	/**
	 * Flips the dimensions of an array, so if it was 2 by 4096 it is now 4096
	 * by 2
	 *
	 * @param array
	 * @return double[][]
	 */
	double[][] flip2DArray(double[][] array) {
		int firstDim = array.length; // 2
		int secondDim = array[0].length; // 4096
		double[][] newArray = new double[secondDim][firstDim];
		for (int i = 0; i < secondDim; i++)
			for (int j = 0; j < firstDim; j++)
				newArray[i][j] = array[j][i];
		return newArray;
	}

	int[][] flip2DArray(int[][] array) {
		int firstDim = array.length; // 2
		int secondDim = array[0].length; // 4096
		int[][] newArray = new int[secondDim][firstDim];
		for (int i = 0; i < secondDim; i++)
			for (int j = 0; j < firstDim; j++)
				newArray[i][j] = array[j][i];
		return newArray;
	}

	/**
	 * Correct an array of [element][resGrade][mca] for deadtime where there is
	 * a single deadtime for each element which is used on every resGrade.
	 *
	 * @param arrays
	 * @param deadtimeFactors
	 * @return double[][][]
	 */
	public double[][][] correctMCAArrays(int[][][] arrays, double[] deadtimeFactors) {
		double[][][] correctedValues = new double[arrays.length][][];
		for (int element = 0; element < arrays.length; element++)
			correctedValues[element] = correctMCAArray(arrays[element], deadtimeFactors[element]);
		return correctedValues;
	}

	double[][] correctMCAArray(int[][] array, double deadtimeFactor) {
		double[][] out = new double[array.length][];
		for (int i = 0; i < array.length; i++)
			out[i] = correctScalerArray(array[i], deadtimeFactor);
		return out;
	}

	double[] correctScalerArray(int[] array, double deadtimeFactor) {
		double[] out = new double[array.length];
		for (int i = 0; i < array.length; i++)
			out[i] = array[i] * deadtimeFactor;
		return out;
	}

	public long[] convertUnsignedIntToLong(int[] data) {
		long[] convertedData = new long[data.length];
		for (int i = 0; i < data.length; i++) {
			long value = data[i];
			// Now remove the sign extension caused by casting from int to long
			// as the data from the detector is really 32bit unsigned, but
			// Java's int is 32bit signed!!
			if (value < 0)
				value = (value << 32) >>> 32;
			convertedData[i] = value;
		}
		return convertedData;
	}

	public NexusTreeProvider[] readoutFullMCA(String detectorName, int numberOfFrames, int[] rawScalerData, int[] rawMcaData ) {
		NexusTreeProvider[] results = new NexusTreeProvider[numberOfFrames];

		int[][] unpackedScalerData = unpackRawScalerDataToFrames(rawScalerData, numberOfFrames);

		int[][][][] unpackedMcaData = unpackRawDataTo4D(rawMcaData, numberOfFrames, 1, currentSettings.getMcaSize(), currentSettings.getNumberOfDetectors());

		for (int frameIndex = 0; frameIndex < numberOfFrames; frameIndex++) {

			double[] deadtimeCorrectionFactors =  determineDeadtimeCorrectionFactors(unpackedScalerData[frameIndex]);
			double[][][] correctedMCAArrays = correctMCAArrays(unpackedMcaData[frameIndex], deadtimeCorrectionFactors);

			NXDetectorData thisFrame = new NXDetectorData(currentSettings.getExtraNames(), currentSettings.getOutputFormat(), detectorName);
			INexusTree detTree = thisFrame.getDetTree(detectorName);

			// add all MCA data in bulk
			if (currentSettings.isAlwaysRecordRawMCAs() || currentSettings.getParameters().isSaveRawSpectrum()) {
				int[][] raw_mcasSingleResGrade = removeSingleDimensionFromArray(unpackedMcaData[frameIndex]);
				NXDetectorData.addData(detTree, "MCAs", new NexusGroupData(raw_mcasSingleResGrade), "counts", 1);
			} else {
				double[][] mcasSingleResGrade = removeSingleDimensionFromArray(correctedMCAArrays);
				NXDetectorData.addData(detTree, "MCAs", new NexusGroupData(mcasSingleResGrade), "counts", 1);
			}

			// add all in-window scaler counts in bulk
			double [][] deadtimeCorrectedScalerData = readoutScalerDataUsingScalerMemory(numberOfFrames, rawScalerData, true);

			// Add Deadtime corrected scaler values, *without* FF (i.e. last value)..
			double[] dtCorrectedScalerValues = ArrayUtils.subarray(deadtimeCorrectedScalerData[frameIndex], 0, currentSettings.getNumberOfDetectors());
			NXDetectorData.addData(detTree, "scalers", new NexusGroupData(dtCorrectedScalerValues), "counts", 1);

			// optionally create a sum of all MCAs together
			if (currentSettings.isSumAllElementData()) {
				double[] summation = new double[correctedMCAArrays[0][0].length];
				for (int element = 0; element < currentSettings.getNumberOfDetectors(); element++) {
					double[][] out = correctedMCAArrays[element];
					for (int i = 0; i < out[0].length; i++) {
						summation[i] += out[0][i];
					}
				}
				NXDetectorData.addData(detTree, "allElementSum", new NexusGroupData(summation), "counts", 1);
			}

			thisFrame = addExtraInformationToNexusTree(detectorName, unpackedScalerData, deadtimeCorrectedScalerData, frameIndex, thisFrame, detTree);
			results[frameIndex] = thisFrame;
		}
		return results;
	}

	private double[] determineDeadtimeCorrectionFactors(int[] unpackedScalerData){
		double[] deadtimeCorrectionFactor = new double[currentSettings.getNumberOfDetectors()];
		Arrays.fill(deadtimeCorrectionFactor, 1.0);
		if (unpackedScalerData != null && !currentSettings.getParameters().isSaveRawSpectrum()) {
			long[] hardwareScalerReadings = convertUnsignedIntToLong(unpackedScalerData);
			deadtimeCorrectionFactor = getDeadtimeCorrectionFactors(hardwareScalerReadings);
		}
		return deadtimeCorrectionFactor;
	}

	public double[] getDeadtimeCorrectionFactors(long[] hardwareScalerReadings) {
		return xspress2SystemDeadtime.calculateDeadtimeCorrectionFactors(hardwareScalerReadings,
				currentSettings.getNumberOfDetectors(), currentSettings.getParameters(), getDeadTimeParameters(),
				currentSettings.getDeadtimeEnergy());
	}

	public NXDetectorData addExtraInformationToNexusTree(String detectorName, int[][] unpackedScalerData, double[][] scalerData, int frame,
			NXDetectorData thisFrame, INexusTree detTree) {
		thisFrame = addFFIfPossible(detTree, thisFrame, scalerData[frame]);
		thisFrame = fillNXDetectorDataWithScalerData(thisFrame, scalerData[frame], unpackedScalerData[frame]);
		thisFrame = addDTValuesToNXDetectorData(detectorName, thisFrame, unpackedScalerData[frame]);
		return thisFrame;
	}

	/**
	 * Adds FF to the Nexus data.
	 * FF data is last element of deadtime corrected in window scaler counts array
	 * @param detTree
	 * @param thisFrame
	 * @param ds - deadtime corrected in window scaler counts
	 */
	private NXDetectorData addFFIfPossible(INexusTree detTree, NXDetectorData thisFrame, double[] ds) {
		int ffColumn = ds.length-1;
		int ffBadColumn = -1;
		if (currentSettings.getMcaGrades() == Xspress2Detector.RES_THRES) {
			ffBadColumn = ds.length-1;
			ffColumn = ffBadColumn-1;
		}
		if (ffColumn > -1) {
			NXDetectorData.addData(detTree, "FF", new NexusGroupData(ds[ffColumn]), "counts", 1);
		}
		if (ffBadColumn > -1) {
				NXDetectorData.addData(detTree, "FF_bad", new NexusGroupData(ds[ffBadColumn]), "counts", 1);
		}
		return thisFrame;
	}

	public NexusTreeProvider[] unpackScalerData(String detectorName, int numberOfFrames, int[] rawscalerData) {

		int[][] unpackedScalerData = unpackRawScalerDataToFrames(rawscalerData, numberOfFrames);
		double[][] scalerData = readoutScalerDataUsingScalerMemory(numberOfFrames, rawscalerData, true);

		NexusTreeProvider[] results = new NexusTreeProvider[numberOfFrames];
		for (int frame = 0; frame < numberOfFrames; frame++) {
			NXDetectorData thisFrame = new NXDetectorData(currentSettings.getExtraNames(),
					currentSettings.getOutputFormat(), detectorName);
			INexusTree detTree = thisFrame.getDetTree(detectorName);
			// remove the FF value which readoutScalerData would have added
			double[] scalerValues = ArrayUtils.subarray(scalerData[frame], 0, currentSettings.getNumberOfDetectors());
			// do not use numberOfDetectors here so all information in the array
			// is added to Nexus (i.e. FF)
			NXDetectorData.addData(detTree, "scalers", new NexusGroupData(scalerValues), "counts", 1);
			thisFrame = addExtraInformationToNexusTree(detectorName, unpackedScalerData, scalerData, frame, thisFrame, detTree);
			results[frame] = thisFrame;
		}
		return results;
	}

	/**
	 * Return deadtime corrected in-window scaler counts. Values for excluded detector elements are set to zero.
	 * The last value in each frame is the sum of counts over all elements (i.e. 'FF')..
	 * @param numFrames
	 * @param rawScalerData
	 * @param performCorrections
	 * @return deadtime corrected in-window scaler counts
	 */
	public double[][] readoutScalerDataUsingScalerMemory(int numFrames, int[] rawScalerData, boolean performCorrections) {

		double[][] scalerData = new double[numFrames][];
		int[][] unpackedScalerData = unpackRawScalerDataToFrames(rawScalerData, numFrames);

		for (int frame = 0; frame < numFrames; frame++) {
			double[] deadtimeCorrectionFactors =  determineDeadtimeCorrectionFactors(unpackedScalerData[frame]);

			// allocate space in frame for corrected scaler values for all elements :
			int numElements = currentSettings.getNumberOfDetectors();
			scalerData[frame] = new double[numElements];

			int counter = 2;
			for (int element = 0; element < numElements; element++) {
				if (!currentSettings.isDetectorExcluded(element))
					scalerData[frame][element] = unpackedScalerData[frame][counter]*deadtimeCorrectionFactors[element];

				counter += 4;
			}
			double ff = 0;
			for (double value : scalerData[frame])
				ff += value;
			scalerData[frame] = ArrayUtils.add(scalerData[frame], ff);
		}
		return scalerData;
	}

	/*
	 * Basically what goes into the Ascii file. Columns should match the values
	 * from getExtraNames() or getUnFilteredChannelLabels()
	 */
	public double[][] readoutScalerDataUsingMCAMemory(String detectorName, int numFrames, int[] rawScalerData, int[] mcaData,
			boolean performCorrections, Double I0) {

		double[][] scalerData = new double[numFrames][];

		Reading[][] readings = getROIs(detectorName, numFrames, rawScalerData, true, mcaData);
		Reading[][] readingsUncorrected = null;
		if (currentSettings.getMcaGrades() == Xspress2Detector.ALL_RES) {
			readingsUncorrected = getROIs(detectorName, numFrames, null, false, mcaData);
		}

		for (int frame = 0; frame < numFrames; frame++) {
			scalerData[frame] = new double[0];
			// when ALL_RES then make a sum of each resGrade over all the
			// elements/ROIs in each resGrade bin i.e.
			// 15, 15+14...; add to this array the sum of best 8 res grades, not
			// corrected for DT
			if (currentSettings.getMcaGrades() == Xspress2Detector.ALL_RES)
				scalerData[frame] = new double[16];

			double ff = 0;
			double ff_bad = 0;

			for (int vs = 0; vs < readings[frame].length; vs++) {
				if (readings[frame][vs] instanceof VSReading) {
					VSReading vsreading = (VSReading) readings[frame][vs];
					if (currentSettings.getMcaGrades() != Xspress2Detector.ALL_RES && vsreading.contributesToFF) {
						scalerData[frame] = ArrayUtils.addAll(scalerData[frame], vsreading.counts);
					}
					switch (currentSettings.getMcaGrades()) {
					case Xspress2Detector.NO_RES_GRADE:
						if (vsreading.contributesToFF)
							ff += vsreading.counts[0];
						break;
					case Xspress2Detector.RES_THRES:
						if (vsreading.contributesToFF)
							ff += vsreading.counts[0];
						else if (vsreading.getRoiName().contains("bad") && !vsreading.getRoiName().contains("OUT"))
							ff_bad += vsreading.counts[0];
						break;
					case Xspress2Detector.ALL_RES:
						// sum of resGrade bins over all ROIs
						for (int resGrade = 0; resGrade < 16; resGrade++)
							for (int outBin = 0; outBin < 16; outBin++)
								if (15 - outBin <= resGrade)
									scalerData[frame][outBin] += vsreading.counts[resGrade];

						// if a normalisation is possible, then normalise all to
						// I0
						if (I0 != null && !I0.isNaN() && !I0.isInfinite() && I0 > 0)
							for (int outBin = 0; outBin < 16; outBin++)
								scalerData[frame][outBin] /= I0;

						// best 8 resGrades for each element, but not corrected
						// for deadtime
						if (readingsUncorrected != null)
							// in this case, add to the end of the scalerData
							// array (initally this array has 16
							// elements, so add on counts of included channels
							// to the end)
							scalerData[frame] = ArrayUtils.add(scalerData[frame],
									((VSReading) readingsUncorrected[frame][vs]).counts[7]);

						if (vsreading.contributesToFF)
							ff += vsreading.counts[7];
						break;
					}
				} else {
					MCAReading mcareading = (MCAReading) readings[frame][vs];
					if (currentSettings.getMcaGrades() != Xspress2Detector.ALL_RES) {
						scalerData[frame] = ArrayUtils.add(scalerData[frame], mcareading.peakArea);
						ff += mcareading.peakArea;
						ff_bad += mcareading.peakArea_bad; // only really relevant when mcaGrades == RES_THRES
					} else {
						// sum of resGrade bins over all ROIs
						for (int resGrade = 0; resGrade < 16; resGrade++) {
							double sumCounts = sumPartialMCACounts(mcareading.mcacounts[resGrade]);
							for (int outBin = 0; outBin < 16; outBin++)
								if (15 - outBin <= resGrade)
									scalerData[frame][outBin] += sumCounts;
						}

						// if a normalisation is possible, then normalise
						// all to I0
						if (I0 != null && !I0.isNaN() && !I0.isInfinite() && I0 > 0)
							for (int outBin = 0; outBin < 16; outBin++)
								scalerData[frame][outBin] /= I0;

						// best 8 resGrades for each element, but not
						// corrected for deadtime
						if (readingsUncorrected != null)
							scalerData[frame][mcareading.getElementNumber() + 16] = ((MCAReading) readingsUncorrected[frame][vs]).peakArea;
						ff += mcareading.peakArea;
					}
				}
			}

			// append the sum (FF) to the array
			scalerData[frame] = ArrayUtils.add(scalerData[frame], ff);

			if (currentSettings.getMcaGrades() == Xspress2Detector.RES_THRES) {
				scalerData[frame] = ArrayUtils.add(scalerData[frame], ff_bad);
			}
		}
		return scalerData;
	}

	public NexusTreeProvider[] readoutROIData(String detectorName, int numberOfFrames, int[] rawscalerData, int[] mcaData,
			double[][] scalerData) {

		int[][] unpackedScalerData = unpackRawScalerDataToFrames(rawscalerData, numberOfFrames);

		NexusTreeProvider[] results = new NexusTreeProvider[numberOfFrames];
		// if ROI readout mode then
		Reading[][] readings = getROIs(detectorName, numberOfFrames, rawscalerData, true, mcaData);

		// loop over frames
		for (int frame = 0; frame < numberOfFrames; frame++) {

			NXDetectorData thisFrame = new NXDetectorData(currentSettings.getExtraNames(),
					currentSettings.getOutputFormat(), detectorName);
			INexusTree detTree = thisFrame.getDetTree(detectorName);

			// for each frame, group MCA rois by name and add to Nexus by each
			// ROI
			HashMap<String, Vector<MCAReading>> mcaROIs = groupMCAROIs(readings[frame]);
			for (String roiName : mcaROIs.keySet()) {
				Vector<MCAReading> readingsInThisROI = mcaROIs.get(roiName);

				// output depends on the number of resgrades and if the MCA are
				// to be saved deadtime corrected or raw
				switch (currentSettings.getMcaGrades()) {
				case (Xspress2Detector.NO_RES_GRADE):
					if (currentSettings.isAlwaysRecordRawMCAs() || currentSettings.getParameters().isSaveRawSpectrum()) {
						int[][] mcaDataInThisROI = new int[readingsInThisROI.size()][];
						int i = 0;
						for (MCAReading thisReading : readingsInThisROI) {
							mcaDataInThisROI[i] = thisReading.mcacounts_uncorrected[0];
							i++;
						}
						NXDetectorData.addData(detTree, roiName, new NexusGroupData(mcaDataInThisROI), "counts", 1);
					} else {
						double[][] mcaDataInThisROI = new double[readingsInThisROI.size()][];
						int i = 0;
						for (MCAReading thisReading : readingsInThisROI) {
							mcaDataInThisROI[i] = thisReading.mcacounts[0];
							i++;
						}
						NXDetectorData.addData(detTree, roiName, new NexusGroupData(mcaDataInThisROI), "counts", 1);
					}
					break;
				case (Xspress2Detector.RES_THRES):
					if (currentSettings.isAlwaysRecordRawMCAs() || currentSettings.getParameters().isSaveRawSpectrum()) {
						int[][][] mcaDataInThisROI = new int[readingsInThisROI.size()][currentSettings.getMcaGrades()][];
						int i = 0;
						for (MCAReading thisReading : readingsInThisROI) {
							// data from detector comes out BAD,GOOD, but more
							// intuitive for users to have GOOD,BAD
							mcaDataInThisROI[i][0] = thisReading.mcacounts_uncorrected[1];
							mcaDataInThisROI[i][1] = thisReading.mcacounts_uncorrected[0];
							i++;
						}
						NXDetectorData.addData(detTree, roiName, new NexusGroupData(mcaDataInThisROI), "counts", 1);
					} else {
						double[][][] mcaDataInThisROI = new double[readingsInThisROI.size()][currentSettings
								.getMcaGrades()][];
						int i = 0;
						for (MCAReading thisReading : readingsInThisROI) {
							// data from detector comes out BAD,GOOD, but more
							// intuitive for users to have GOOD,BAD
							mcaDataInThisROI[i][0] = thisReading.mcacounts[1];
							mcaDataInThisROI[i][1] = thisReading.mcacounts[0];
							i++;
						}
						NXDetectorData.addData(detTree, roiName, new NexusGroupData(mcaDataInThisROI), "counts", 1);
					}
					break;
				default:
					if (currentSettings.isAlwaysRecordRawMCAs() || currentSettings.getParameters().isSaveRawSpectrum()) {
						int[][][] mcaDataInThisROI = new int[readingsInThisROI.size()][currentSettings.getMcaGrades()][];
						int i = 0;
						for (MCAReading thisReading : readingsInThisROI) {
							mcaDataInThisROI[i] = flip2DArray(thisReading.mcacounts_uncorrected);
							i++;
						}
						NXDetectorData.addData(detTree, roiName, new NexusGroupData(mcaDataInThisROI), "counts", 1);
					} else {
						double[][][] mcaDataInThisROI = new double[readingsInThisROI.size()][currentSettings
								.getMcaGrades()][];
						int i = 0;
						for (MCAReading thisReading : readingsInThisROI) {
							mcaDataInThisROI[i] = flip2DArray(thisReading.mcacounts);
							i++;
						}
						NXDetectorData.addData(detTree, roiName, new NexusGroupData(mcaDataInThisROI), "counts", 1);
					}
				}
			}
			mcaROIs = null;

			// for each frame, group VS rois by name and add to Nexus by each
			// ROI
			HashMap<String, Vector<VSReading>> vsROIs = groupVSROIs(readings[frame]);
			for (String roiName : vsROIs.keySet()) {
				Vector<VSReading> readingsInThisROI = vsROIs.get(roiName);

				if (readingsInThisROI.get(0).counts.length == 1) {
					double[] vsDataInThisROI = new double[readingsInThisROI.size()];
					int i = 0;
					for (VSReading thisReading : readingsInThisROI) {
						vsDataInThisROI[i] = thisReading.counts[0];
						i++;
					}
					NXDetectorData.addData(detTree, roiName, new NexusGroupData(vsDataInThisROI), "counts", 1);
				} else if (readingsInThisROI.get(0).counts.length == 2) {
					double[][] vsDataInThisROI = new double[readingsInThisROI.size()][2];
					int i = 0;
					for (VSReading thisReading : readingsInThisROI) {
						vsDataInThisROI[i] = thisReading.counts;
						i++;
					}
					NXDetectorData.addData(detTree, roiName, new NexusGroupData(vsDataInThisROI), "counts", 1);
				} else {
					// all 16 grades
					double[][] vsDataInThisROI = new double[readingsInThisROI.size()][currentSettings.getMcaGrades()];
					int i = 0;
					for (VSReading thisReading : readingsInThisROI) {
						vsDataInThisROI[i] = thisReading.counts;
						i++;
					}
					NXDetectorData.addData(detTree, roiName, new NexusGroupData(vsDataInThisROI), "counts", 1);
				}
			}

			thisFrame = addExtraInformationToNexusTree(detectorName, unpackedScalerData, scalerData, frame, thisFrame, detTree);
			results[frame] = thisFrame;
		}
		return results;
	}

	private HashMap<String, Vector<MCAReading>> groupMCAROIs(Reading[] readings) {
		HashMap<String, Vector<MCAReading>> groupedROIs = new HashMap<String, Vector<MCAReading>>();

		for (int reading = 0; reading < readings.length; reading++) {
			if (readings[reading] instanceof MCAReading) {
				String thisROIName = readings[reading].getRoiName();
				MCAReading thisReading = (MCAReading) readings[reading];
				if (!groupedROIs.containsKey(thisROIName)) {
					Vector<MCAReading> vectorOfReadings = new Vector<MCAReading>();
					vectorOfReadings.add(thisReading.elementNumber, thisReading);
					groupedROIs.put(thisROIName, vectorOfReadings);
				} else {
					Vector<MCAReading> vectorOfReadings = groupedROIs.get(thisROIName);
					vectorOfReadings.add(thisReading.elementNumber, thisReading);
				}
			}
		}
		return groupedROIs;
	}

	private HashMap<String, Vector<VSReading>> groupVSROIs(Reading[] readings) {
		HashMap<String, Vector<VSReading>> groupedROIs = new HashMap<String, Vector<VSReading>>();

		for (int reading = 0; reading < readings.length; reading++) {
			if (readings[reading] instanceof VSReading) {
				String thisROIName = readings[reading].getRoiName();
				VSReading thisReading = (VSReading) readings[reading];
				if (!groupedROIs.containsKey(thisROIName)) {
					Vector<VSReading> vectorOfReadings = new Vector<VSReading>();
					vectorOfReadings.add(thisReading);
					groupedROIs.put(thisROIName, vectorOfReadings);
				} else {
					Vector<VSReading> vectorOfReadings = groupedROIs.get(thisROIName);
					vectorOfReadings.add(thisReading);
				}
			}
		}
		return groupedROIs;
	}

	private NXDetectorData addDTValuesToNXDetectorData(String detectorName, NXDetectorData thisFrame, int[] unpackedScalerData) {
		// always add raw scaler values to nexus data

		if (unpackedScalerData.length != currentSettings.getNumberOfDetectors() * 4) {
			// logger.warn("Amount of scaler data inconsistent with the number of elements in Xspress2 detector. Raw scaler data will not be recorded.");
			return thisFrame;
		}

		int numDetectors = currentSettings.getNumberOfDetectors(); //always write Deadtime values for all elements

		int[] totalCounts = new int[numDetectors];
		int[] numResets = new int[numDetectors];
		int[] inWinCounts = new int[numDetectors];
		int[] numClockCounts = new int[numDetectors];

		for (int element = 0; element < currentSettings.getNumberOfDetectors(); element++) {
			totalCounts[element] = unpackedScalerData[element * 4];
			numResets[element] = unpackedScalerData[element * 4 + 1];
			inWinCounts[element] = unpackedScalerData[element * 4 + 2];
			numClockCounts[element] = unpackedScalerData[element * 4 + 3];
		}

		INexusTree detTree = thisFrame.getDetTree(detectorName);
		NXDetectorData.addData(detTree, "raw scaler total", new NexusGroupData(totalCounts), "counts", 1);
		NXDetectorData.addData(detTree, "tfg resets", new NexusGroupData(numResets), "counts", 1);
		NXDetectorData.addData(detTree, "raw scaler in-window", new NexusGroupData(inWinCounts), "counts", 1);
		NXDetectorData.addData(detTree, "tfg clock cycles", new NexusGroupData(numClockCounts), "counts", 1);

		return thisFrame;
	}

	/**
	 * Adds to the output the 'ascii' data which is the values which will be
	 * displayed in the Jython Terminal, plotting and ascii file.
	 * @param thisFrame
	 * @param scalerData - deadtime corrected in window scaler counts
	 */
	private NXDetectorData fillNXDetectorDataWithScalerData(NXDetectorData thisFrame, double[] scalerData,
			int[] rawScalervalues) {

		double[] dataToPlot;

		// Set indices of FF and FF_bad value
		int ffColumn = scalerData.length-1;
		int ffBadColumn = -1;
		if (currentSettings.getMcaGrades() == Xspress2Detector.RES_THRES) {
			ffBadColumn = scalerData.length-1;
			ffColumn = ffBadColumn-1;
		}

		if (currentSettings.getParameters().isOnlyShowFF())
			// only add FF, so filter out rest of scalerdata
			dataToPlot = new double[] { scalerData[ffColumn] };
		else if (currentSettings.getMcaGrades() == Xspress2Detector.ALL_RES)
			dataToPlot = scalerData;
		else {
			double[] plottableData = new double[0];
			for (int i = 0; i < scalerData.length; i++)
				if (i == ffColumn || i==ffBadColumn || !currentSettings.getDetectorElements().get(i).isExcluded())
					plottableData = ArrayUtils.add(plottableData, scalerData[i]);
			dataToPlot = plottableData;
		}

		// add the raw scaler values used to calculate the deadtime to ascii
		if (currentSettings.isAddDTScalerValuesToAscii()) {
			double[] dblRawScalerValues = new double[0];
			for (int i = 0; i < currentSettings.getNumberOfDetectors(); i++) {
				if (!currentSettings.getDetectorElements().get(i).isExcluded()) {
					int firstRawScalerValueElement = i * 4;
					dblRawScalerValues = ArrayUtils
							.add(dblRawScalerValues, rawScalervalues[firstRawScalerValueElement]);
					dblRawScalerValues = ArrayUtils.add(dblRawScalerValues,
							rawScalervalues[firstRawScalerValueElement + 1]);
					dblRawScalerValues = ArrayUtils.add(dblRawScalerValues,
							rawScalervalues[firstRawScalerValueElement + 2]);
					dblRawScalerValues = ArrayUtils.add(dblRawScalerValues,
							rawScalervalues[firstRawScalerValueElement + 3]);
				}
			}
			dataToPlot = ArrayUtils.addAll(scalerData, dblRawScalerValues);
		}

		// by now, the scalerData array should match the extraNames.
		String[] names = currentSettings.getExtraNames();
		for (int i = 0; i < names.length; i++)
			thisFrame.setPlottableValue(names[i], dataToPlot[i]);

		return thisFrame;
	}

	/**
	 * @param numberOfFrames
	 * @param scalerData
	 *            - the raw data from the scalers or null if no corrections are
	 *            to be performed
	 * @param addOutOfWindowROI
	 *            - if true then add a virtual scaler at the end of the array or
	 *            readings for each element of the OUT counts
	 * @param mcaData
	 *            - the raw mca data in the 1D array that da.server supplies
	 * @return Reading[frame][element]
	 */
	private Reading[][] getROIs(String detectorName, int numberOfFrames, int[] scalerData, boolean addOutOfWindowROI, int[] mcaData) {
		// create structure to fill
		Reading[][] results = new Reading[numberOfFrames][];

		int[][] unpackedScalerData = new int[numberOfFrames][currentSettings.getNumberOfDetectors() * 4];
		if (scalerData != null)
			// get the raw hardware scaler data
			unpackedScalerData = unpackRawScalerDataToFrames(scalerData, numberOfFrames);

		// readout the mca memory and unpack into a 3D form  [frame][channel][res_grade][energy]
		int[][][][] unpackedMCAData = unpackRawDataTo4D(mcaData, numberOfFrames, currentSettings.getMcaGrades(),
				currentSettings.getMcaSize(), currentSettings.getNumberOfDetectors());

		for (int frame = 0; frame < numberOfFrames; frame++) {
			Reading[] value = new Reading[0];
			double[] deadtimeCorrectionFactor =  determineDeadtimeCorrectionFactors(unpackedScalerData[frame]);

			// loop over all the elements
			for (int element = 0; element < currentSettings.getNumberOfDetectors(); element++) {
				DetectorElement thisElement = currentSettings.getDetectorElements().get(element);
//				if (thisElement.isExcluded())
//					continue;

				// calculate the windowingCorrectionFactor based on good and bad

				// loop over all the ROIs for this element
				int mcaPosition = 0;
				for (int roi = 0; roi < thisElement.getRegionList().size(); roi++) {
					DetectorROI thisRoi = thisElement.getRegionList().get(roi);

					// three options at this point: we are in 'regions of interest' mode
					// and are using virtual scalers; or we are in 'regions of interest' mode and are using partial MCAs; we are in
					// 'regions of interest' mode and are using full MCAs
					String regionType = currentSettings.getParameters().getRegionType();
					if (regionType.equals(XspressParameters.VIRTUALSCALER)) {
						// if a virtual scaler return 1,2 or 16 numbers
						double[] out = extractVirtualScaler(unpackedMCAData, frame, deadtimeCorrectionFactor, element,
								mcaPosition);

						// Replace data for excluded elements with zeros
						if (thisElement.isExcluded()) {
							out = new double[out.length];
						}

						String elementName = detectorName + "_element" + element + "_" + thisRoi.getRoiName();

						if (out.length == 2) {
							// if threshold, then split the good and the bad
							if (addOutOfWindowROI)
								value = (Reading[]) ArrayUtils.add(value, new VSReading(thisRoi.getRoiName() + "_bad",
										element, new double[] { out[0] }, elementName + "_bad", false));
							value = (Reading[]) ArrayUtils.add(value, new VSReading(thisRoi.getRoiName(), element,
									new double[] { out[1] }, elementName, true));
						} else
							value = (Reading[]) ArrayUtils.add(value, new VSReading(thisRoi.getRoiName(), element, out,
									elementName, true));
						// increment the position in the raw data array
						mcaPosition++;

					} else {
						// MCAReadings - but could be the full MCA, not only a partial mca
						boolean isFullMCA = unpackedMCAData[frame][element][0].length == currentSettings.getFullMCASize();
						if (isFullMCA){
							MCAReading reading = extractPartialMCA(unpackedMCAData, frame, deadtimeCorrectionFactor,
									element, 0, thisRoi, unpackedMCAData[frame][element][0].length);
							value = (Reading[]) ArrayUtils.add(value, reading);
						} else {
							int mcaEndPosition = mcaPosition + thisRoi.getRoiEnd() - thisRoi.getRoiStart();
							MCAReading reading = extractPartialMCA(unpackedMCAData, frame, deadtimeCorrectionFactor,
									element, mcaPosition, thisRoi, mcaEndPosition);
							value = (Reading[]) ArrayUtils.add(value, reading);
							// increment the position in the raw data array
							mcaPosition = mcaEndPosition + 1;
						}
					}
				}

				if (currentSettings.getMcaGrades() != Xspress2Detector.ALL_RES
						&& mcaPosition < currentSettings.getMcaSize() - 1) {
					double[] out = extractVirtualScaler(unpackedMCAData, frame, deadtimeCorrectionFactor, element,
							unpackedMCAData[0][0][0].length - 1);
					String elementName = detectorName + "_element" + element + "_OUT";
					String outName = "OUT";

					if (out.length == 2) {
						// if threshold, then split the good and the bad
						if (addOutOfWindowROI)
							value = (Reading[]) ArrayUtils.add(value, new VSReading(outName + "_bad", element,
									new double[] { out[0] }, elementName + "_bad", false));
						value = (Reading[]) ArrayUtils.add(value, new VSReading(outName, element,
								new double[] { out[1] }, elementName, false));
					} else
						value = (Reading[]) ArrayUtils.add(value, new VSReading(outName, element, out, elementName,
								false));
				}
			}
			results[frame] = value;
		}
		return results;
	}

	/*
	 * returns an MCA object for a single ROI in a single element. Applies to
	 * appropriate corrections.
	 */
	private MCAReading extractPartialMCA(int[][][][] unpackedMCAData, int frame, double[] deadtimeCorrectionFactor,
			int element, int mcaPosition, DetectorROI thisRoi, int mcaEndPosition) {
		// get the raw data. Only interested in good counts if threshold set
		boolean isFullMCA = unpackedMCAData[frame][element][0].length == currentSettings.getFullMCASize();
		int[][] mcas_raw;
		int[][] mcaDataForThisElement = unpackedMCAData[frame][element];
		switch (currentSettings.getMcaGrades()) {
		case Xspress2Detector.NO_RES_GRADE:
			if (isFullMCA){
				mcas_raw = mcaDataForThisElement;
			} else {
				// we are using partial MCAs
				mcas_raw = new int[][] { getResGradesSlice(mcaDataForThisElement, mcaPosition, mcaEndPosition,
					thisRoi.getRoiStart(), thisRoi.getRoiEnd())[0] };
			}
			break;
		case Xspress2Detector.RES_THRES:
			// all res grades
		default:
			mcas_raw = getResGradesSlice(mcaDataForThisElement, mcaPosition, mcaEndPosition, thisRoi.getRoiStart(),
					thisRoi.getRoiEnd());
			break;
		}

		// add to the array of readings
		double dctFactor = 1.0;
		if (deadtimeCorrectionFactor != null)
			dctFactor = deadtimeCorrectionFactor[element];
		double[][] mcas_corrected = correctMCAArray(mcas_raw, dctFactor);

		double peakArea = 0;
		double peakArea_bad = 0;

		switch (currentSettings.getMcaGrades()) {
		case Xspress2Detector.NO_RES_GRADE:
			double[] corrctedSpectrum = mcas_corrected[0];
			if (isFullMCA){
				corrctedSpectrum = ArrayUtils.subarray(corrctedSpectrum, thisRoi.getRoiStart(), thisRoi.getRoiEnd());
			}
			peakArea = sumPartialMCACounts(corrctedSpectrum);
			break;
		case Xspress2Detector.RES_THRES:
			// correct for good events thrown away in badIn
			double goodIn = sumPartialMCACounts(mcas_raw[1]);
			double badIn = sumPartialMCACounts(mcas_raw[0]);
			double goodOut = mcas_raw[1][mcas_raw[1].length - 1];
			// account for other ROIs by summing everything in the arrays
			double allEvents = sumArrayContents(mcaDataForThisElement[0]) + sumArrayContents(mcaDataForThisElement[1]);
			// so corrected value = goodIn * dct * (all/good)
			peakArea = goodIn * (allEvents / (goodIn + goodOut)) * dctFactor;
			peakArea_bad = badIn * dctFactor;
			break;
		// all res grades
		default:
			for (int grade = 15; grade >= 8; grade--)
				peakArea += sumPartialMCACounts(mcas_corrected[grade]);
			break;
		}
		String elementName = "element_" + element + "_" + thisRoi.getRoiName();

		// so MCAs are the deadtime corrected partial MCAs, but the scaler
		// values are the sums: 15, 15+14 etc.
		MCAReading reading = new MCAReading(thisRoi.getRoiName(), element, mcas_corrected, mcas_raw, elementName,
				thisRoi.getRoiStart(), thisRoi.getRoiEnd(), peakArea, peakArea_bad);
		return reading;
	}

	/*
	 * returns the VS values for a single ROI in a single element. Applies the
	 * appropriate corrections.
	 */
	private double[] extractVirtualScaler(int[][][][] unpackedMCAData, int frame, double[] deadtimeCorrectionFactor,
			int element, int mcaPosition) {
		// get the raw data. Only interested in good counts (second element) if
		// threshold set
		int[] vsCounts;
		int[][] mcaDataForThisElement = unpackedMCAData[frame][element];
		double dctFactor = deadtimeCorrectionFactor[element];
		switch (currentSettings.getMcaGrades()) {
		case Xspress2Detector.NO_RES_GRADE:
			vsCounts = new int[] { getResGradesValues(mcaDataForThisElement, mcaPosition)[0] };
			break;
		case Xspress2Detector.RES_THRES: // BAD, GOOD
			vsCounts = new int[] { getResGradesValues(mcaDataForThisElement, mcaPosition)[0],
					getResGradesValues(mcaDataForThisElement, mcaPosition)[1] };
			break;
		// all res grades
		default:
			vsCounts = getResGradesValues(mcaDataForThisElement, mcaPosition);
			break;
		}

		// then perform corrections based on values thrown away
		switch (currentSettings.getMcaGrades()) {
		case Xspress2Detector.NO_RES_GRADE:
			return correctScalerArray(vsCounts, deadtimeCorrectionFactor[element]);
		case Xspress2Detector.RES_THRES: // BAD, GOOD
			// correct for good events thrown away in badIn
			double goodIn = vsCounts[1];
			double badIn = vsCounts[0];
			double goodOut = mcaDataForThisElement[1][mcaDataForThisElement[1].length - 1];
			// account for other ROIs by summing everything in array
			// double badOut =
			// mcaDataForThisElement[0][mcaDataForThisElement[0].length - 1];
			double allEvents = sumArrayContents(mcaDataForThisElement[0]) + sumArrayContents(mcaDataForThisElement[1]);
			// so corrected value = goodIn * dct * (all/good)
			double goodIn_corrected = goodIn * (allEvents / (goodIn + goodOut)) * dctFactor;
			double badIn_corrected = badIn; // return the raw value as this is
											// just for interests sake...
			return new double[] { badIn_corrected, goodIn_corrected };
			// all res grades
		default:
			int[] sumsInWindow = new int[16];
			for (int resGrade = 0; resGrade < 16; resGrade++)
				for (int outBin = 0; outBin < 16; outBin++)
					if (15 - outBin <= resGrade)
						sumsInWindow[outBin] += mcaDataForThisElement[resGrade][0];

			int[] sumsOutWindow = new int[16];
			for (int resGrade = 0; resGrade < 16; resGrade++)
				for (int outBin = 0; outBin < 16; outBin++)
					if (15 - outBin <= resGrade)
						sumsOutWindow[outBin] += mcaDataForThisElement[resGrade][mcaDataForThisElement[resGrade].length - 1];

			int totalCounts = sumsInWindow[15] + sumsOutWindow[15]; // so this
																	// is sum of
																	// all OUT
																	// and IN
																	// counts
																	// over all

			double[] correctedResGrades = new double[16];
			for (int i = 0; i < 16; i++) {
				if (sumsInWindow[i] + sumsOutWindow[i] <= 0)
					continue;
				correctedResGrades[i] = sumsInWindow[i] * (totalCounts / (sumsInWindow[i] + sumsOutWindow[i]))
						* dctFactor;
			}
			return correctedResGrades;
		}
	}

	private int sumArrayContents(int[] is) {
		int total = 0;
		for (int element : is)
			total += element;
		return total;
	}

	private double sumPartialMCACounts(double[] ds) {
		double total = 0;
		for (double channel : ds)
			total += channel;
		return total;
	}

	private int sumPartialMCACounts(int[] ds) {
		int total = 0;
		for (int channel : ds)
			total += channel;
		return total;
	}

	private int[] getResGradesValues(int[][] elementData, int startSlice) {
		int[] out = new int[elementData.length];
		for (int i = 0; i < elementData.length; i++)
			out[i] = elementData[i][startSlice];
		return out;
	}

	public XspressDeadTimeParameters getDeadTimeParameters() {
		return xspressDeadTimeParameters;
	}

	public void setDeadTimeParameters(XspressDeadTimeParameters xspressDeadTimeParameters) {
		this.xspressDeadTimeParameters = xspressDeadTimeParameters;
	}
}
