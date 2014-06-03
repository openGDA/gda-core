package gda.device.detector.xspress;

import gda.data.nexus.tree.INexusTree;
import gda.data.nexus.tree.NexusTreeProvider;
import gda.device.detector.NXDetectorData;

import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.lang.ArrayUtils;
import org.nexusformat.NexusFile;

import uk.ac.gda.beans.xspress.XspressDeadTimeParameters;
import uk.ac.gda.beans.xspress.XspressParameters;

public class Xspress2SystemData {
	
	private Xspress2SystemDeadtime xspress2SystemDeadtime;
	
	public Xspress2SystemData(){
		xspress2SystemDeadtime = new Xspress2SystemDeadtime();
	}
	
	/**
	 * When only one res garde, removes the single dimension to convert [element][resGrade][mca] into a true 2D array of
	 * [element][mca]
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
	
	int[][] unpackRawScalerDataToFrames(int[] scalerData, int numFrames, int numberOfDetectors) {
		int numberDataPerFrame = 4 * numberOfDetectors;
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
	
	int[][] getResGradesSlice(int[][] elementData, int startSlice, int endSlice, int startMCAPosition, int endMCAPosition, int fullMCASize) {
		int[][] out = new int[elementData.length][];

		for (int i = 0; i < elementData.length; i++) {
			int[] data = elementData[i];
			int[] slice = Arrays.copyOfRange(data, startSlice, endSlice + 1);

			int[] paddedArray = new int[fullMCASize];
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
	int[][][][] unpackRawDataTo4D(int[] rawData, int numFrames, int numResGrades, int mcaSize, int numberOfDetectors) {
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
	 * Flips the dimensions of an array, so if it was 2 by 4096 it is now 4096 by 2
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
	 * Correct an array of [element][resGrade][mca] for deadtime where there is a single deadtime for each element which
	 * is used on every resGrade.
	 * 
	 * @param arrays
	 * @param deadtimeFactors
	 * @return double[][][]
	 */
	double[][][] correctMCAArrays(int[][][] arrays, double[] deadtimeFactors) {
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
			// Now remove the sign extension caused by casting from int to long as the data
			// from the detector is really 32bit unsigned, but Java's int is 32bit signed!!
			if (value < 0)
				value = (value << 32) >>> 32;
			convertedData[i] = value;
		}
		return convertedData;
	}
	
	public NexusTreeProvider[] readoutFullMCA(int numberOfFrames, int[][] unpackedScalerData, int[] mcaData, double[][] scalerData, int numberOfDetectors, int currentMCASize, boolean saveRawSpectrum, boolean alwaysRecordRawMCAs, boolean sumAllElementData, String name, Double deadtimeEnergy, XspressParameters xspressParameters, XspressDeadTimeParameters xspressDeadTimeParameters){
		NexusTreeProvider[] results = new NexusTreeProvider[numberOfFrames];
		int[][][][] data = unpackRawDataTo4D(mcaData, numberOfFrames, 1, currentMCASize, numberOfDetectors);

		for (int frame = 0; frame < numberOfFrames; frame++) {
			NXDetectorData thisFrame = new NXDetectorData(this);
			INexusTree detTree = thisFrame.getDetTree(name);
			double[] deadtimeCorrectionFactors = new double[numberOfDetectors];
			if (saveRawSpectrum)
				Arrays.fill(deadtimeCorrectionFactors, 1.0);
			else{
				
				long[] hardwareScalerReadings = convertUnsignedIntToLong(unpackedScalerData[frame]);
				deadtimeCorrectionFactors = xspress2SystemDeadtime.calculateDeadtimeCorrectionFactors(hardwareScalerReadings, numberOfDetectors, xspressParameters, xspressDeadTimeParameters, deadtimeEnergy);
			}

			double[][][] correctedMCAArrays = correctMCAArrays(data[frame], deadtimeCorrectionFactors);
			
			// add all MCA data in bulk
			if (alwaysRecordRawMCAs || saveRawSpectrum) {
				int[][] raw_mcasSingleResGrade = removeSingleDimensionFromArray(data[frame]);
				thisFrame.addData(detTree, "MCAs", new int[] { numberOfDetectors, 4096 }, NexusFile.NX_INT32,
						raw_mcasSingleResGrade, "counts", 1);
				
			} else {
				double[][] mcasSingleResGrade = removeSingleDimensionFromArray(correctedMCAArrays);
				thisFrame.addData(detTree, "MCAs", new int[] { numberOfDetectors, 4096 }, NexusFile.NX_FLOAT64,
						mcasSingleResGrade, "counts", 1);
			}
			// add all in-window scaler counts in bulk
			thisFrame.addData(detTree, "scalers", new int[] { numberOfDetectors }, NexusFile.NX_FLOAT64,
					scalerData[frame], "counts", 1);

			// optionally create a sum of all MCAs together
			if (sumAllElementData) {
				double[] summation = new double[correctedMCAArrays[0][0].length];
				for (int element = 0; element < numberOfDetectors; element++) {
					double[][] out = correctedMCAArrays[element];
					for (int i = 0; i < out[0].length; i++)
						summation[i] += out[0][i];
				}
				thisFrame.addData(detTree, "allElementSum", new int[] { 4096 }, NexusFile.NX_FLOAT64, summation, "counts", 1);
			}

			thisFrame = addExtraInformationToNexusTree(unpackedScalerData, scalerData, frame, thisFrame, detTree);
			results[frame] = thisFrame;
		}
		return results;
	}
	
	protected NXDetectorData addExtraInformationToNexusTree(int[][] unpackedScalerData, double[][] scalerData, int frame, NXDetectorData thisFrame, INexusTree detTree) {
		thisFrame = addFFIfPossible(detTree, thisFrame, scalerData[frame]);
		thisFrame = fillNXDetectorDataWithScalerData(thisFrame, scalerData[frame], unpackedScalerData[frame]);
		thisFrame = addDTValuesToNXDetectorData(thisFrame, unpackedScalerData[frame]);
		return thisFrame;
	}
	
	/*
	 * Adds FF to the Nexus data where defined in the ascii data
	 */
	private NXDetectorData addFFIfPossible(INexusTree detTree, NXDetectorData thisFrame, double[] ds) {
		ArrayList<String> elementNames = new ArrayList<String>();
		getChannelLabels(elementNames,true);
		int ffColumn = elementNames.indexOf("FF");
		if (elementNames.size() == ds.length && ffColumn > -1)
			thisFrame.addData(detTree, "FF", new int[] { 1 }, NexusFile.NX_FLOAT64, new double[] { ds[ffColumn] }, "counts", 1);
		if (mcaGrades == RES_THRES) {
			int ffBadColumn = elementNames.indexOf("FF_bad");
			if (ffBadColumn > -1)
				thisFrame.addData(detTree, "FF_bad", new int[] { 1 }, NexusFile.NX_FLOAT64, new double[] { ds[ffBadColumn] }, "counts", 1);
		}
		return thisFrame;
	}
	
	public NexusTreeProvider[] readoutScalerData(int numberOfFrames, int[] rawscalerData, int[][] unpackedScalerData){
		NexusTreeProvider[] results = new NexusTreeProvider[numberOfFrames];
		double[][] scalerData = readoutScalerData(numberOfFrames, rawscalerData, null, true);
		for (int frame = 0; frame < numberOfFrames; frame++) {
			NXDetectorData thisFrame = new NXDetectorData(this);
			INexusTree detTree = thisFrame.getDetTree(getName());
			// remove the FF value which readoutScalerData would have added
			double[] scalerValues = ArrayUtils.subarray(scalerData[frame], 0, numberOfDetectors);
			// do not use numberOfDetectors here so all information in the array is added to Nexus (i.e. FF)
			thisFrame.addData(detTree, "scalers", new int[] { numberOfDetectors }, NexusFile.NX_FLOAT64,
					scalerValues, "counts", 1);
			thisFrame = addExtraInformationToNexusTree(unpackedScalerData, scalerData, frame, thisFrame, detTree);
			results[frame] = thisFrame;
		}
		return results;
	}
	
	private NXDetectorData addDTValuesToNXDetectorData(NXDetectorData thisFrame, int[] unpackedScalerData, int numberOfDetectors, String name) {
		// always add raw scaler values to nexus data
		
		if (unpackedScalerData.length != numberOfDetectors * 4){
			logger.warn("Amount of scaler data inconsistent with the number of elements in Xspress2 detector. Raw scaler data will not be recorded.");
			return thisFrame;
		}
		
		int numFilteredDetectors = getNumberOfIncludedDetectors();
		
		int[] totalCounts = new int[numFilteredDetectors];
		int[] numResets = new int[numFilteredDetectors];
		int[] inWinCounts = new int[numFilteredDetectors];
		int[] numClockCounts = new int[numFilteredDetectors];
		
		int i = 0;
		for (int element = 0; element < numberOfDetectors; element++){
			if (!getDetectorList().get(element).isExcluded()) {
				totalCounts[i] = unpackedScalerData[element*4];
				numResets[i] = unpackedScalerData[element*4 + 1];
				inWinCounts[i] = unpackedScalerData[element*4 + 2];
				numClockCounts[i] = unpackedScalerData[element*4 + 3];
				i++;
			}
		}
		
		thisFrame.addData(thisFrame.getDetTree(name), "raw scaler total",
				new int[] { numFilteredDetectors }, NexusFile.NX_INT32, totalCounts, "counts", 1);
		thisFrame.addData(thisFrame.getDetTree(name), "tfg resets",
				new int[] { numFilteredDetectors }, NexusFile.NX_INT32, numResets, "counts", 1);
		thisFrame.addData(thisFrame.getDetTree(name), "raw scaler in-window",
				new int[] { numFilteredDetectors }, NexusFile.NX_INT32, inWinCounts, "counts", 1);
		thisFrame.addData(thisFrame.getDetTree(name), "tfg clock cycles",
				new int[] { numFilteredDetectors }, NexusFile.NX_INT32, numClockCounts, "counts", 1);

		return thisFrame;
	}
	
}
