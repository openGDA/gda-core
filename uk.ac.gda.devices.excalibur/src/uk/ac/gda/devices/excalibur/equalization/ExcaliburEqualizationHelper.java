/*-
 * Copyright © 2012 Diamond Light Source Ltd.
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

package uk.ac.gda.devices.excalibur.equalization;

import gda.analysis.io.ScanFileHolderException;
import gda.analysis.numerical.straightline.Results;
import gda.analysis.numerical.straightline.StraightLineFit;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import org.apache.commons.lang.ArrayUtils;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.DoubleDataset;
import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import uk.ac.diamond.scisoft.analysis.dataset.ILazyDataset;
import uk.ac.diamond.scisoft.analysis.dataset.IndexIterator;
import uk.ac.diamond.scisoft.analysis.dataset.IntegerDataset;
import uk.ac.diamond.scisoft.analysis.fitting.Generic1DFitter;
import uk.ac.diamond.scisoft.analysis.fitting.functions.APeak;
import uk.ac.diamond.scisoft.analysis.fitting.functions.CompositeFunction;
import uk.ac.diamond.scisoft.analysis.fitting.functions.Gaussian;
import uk.ac.diamond.scisoft.analysis.fitting.functions.IPeak;
import uk.ac.diamond.scisoft.analysis.optimize.GeneticAlg;
import uk.ac.gda.analysis.hdf5.HDF5HelperLocations;
import uk.ac.gda.analysis.hdf5.HDF5NexusLocation;
import uk.ac.gda.analysis.hdf5.Hdf5Helper;
import uk.ac.gda.analysis.hdf5.Hdf5Helper.TYPE;
import uk.ac.gda.analysis.hdf5.Hdf5HelperData;
import uk.ac.gda.analysis.hdf5.Hdf5HelperLazyLoader;
import uk.ac.gda.devices.excalibur.ExcaliburReadoutNodeFem;
import uk.ac.gda.devices.excalibur.MpxiiiChipReg;

/**
 *
 */
public class ExcaliburEqualizationHelper {

	public static final String THRESHOLD_N_EQ_TARGET_ATTR = "thresholdNopt_eq_target";
	public static final String THRESHOLD_NOPT = "thresholdNopt"; // short[]
	public static final String THRESHOLD_0OPT = "threshold0opt"; //double []
	public static final String THRESHOLDN_MASK = "thresholdN_mask"; //short []
	public static final double FIT_FAILED_WIDTH = -1.0;
	public static final short EDGE_POSITION_IF_ALL_BELOW_THRESHOLD = Short.MIN_VALUE;
	public static final short EDGE_POSITION_IF_ALL_ABOVE_THRESHOLD = Short.MAX_VALUE;
	public static final short EDGE_POSITION_IF_PIXEL_MASKED_OUT = Short.MAX_VALUE-1;
	public static final String THRESHOLD_TARGET_ATTR = "thresholdTarget";
	public static final String THRESHOLD_FROM_THRESHOLD_RESPONSE_DATASET = "thresholdFromThresholdResponse";
	public static final String THRESHOLD_RESPONSE_OFFSETS_DATASET = "edgeThresholdResponseOffsets";
	public static final String THRESHOLD_RESPONSE_SLOPES_DATASET = "edgeThresholdResponseSlopes";
	public static final String THRESHOLDABNVAL_DATASET = "thresholdABNVal";
	public static final String THRESHOLDABNVAL_ATTR = "thresholdABNVal";
	public static final String THRESHOLD_LIMIT_ATTR = "thresholdLimit";
//	public static final String THRESHOLD_ATTRIBUTE = "threshold";
	public static final String THRESHOLD_STDDEV_ATTRIBUTE = "threshold_mean";
	public static final String THRESHOLD_MEAN_ATTRIBUTE = "threshold_stddev";
	public static final String THRESHOLD_DATASET = "edgeThresholds";
	public static final String CHIP_PRESENT_DATASET="chip_present";
	public static final String CHIP_THRESHOLD_GAUSSIAN_POSITION_DATASET = "chip_threshold_gaussian_position";
	public static final String CHIP_THRESHOLD_GAUSSIAN_FWHM_DATASET = "chip_threshold_gaussian_fwhm";
	private static final ExcaliburEqualizationHelper INSTANCE = new ExcaliburEqualizationHelper();
	public static final int chipHeight = 256;
	public static final int chipWidth = 256;
	public static final int chipPixels = chipHeight*chipWidth;

	long getChipTopPixel(long chipRow){
		switch ((int)chipRow){
		case 0:
			return 0;
		case 1:
			return chipHeight+3;
		case 2: 
			return 2*chipHeight + 3 + 124;
		case 3:
			return 3*chipHeight + 3 + 124 + 3;
		case 4:
			return 4*chipHeight + 3 + 124 + 3 + 124;
		case 5:
			return 5*chipHeight + 3 + 124 + 3 + 124 + 3;
		default:
			throw new IllegalArgumentException("chipRow must be between 0 and 5");
		}
	}
	
	long getChipBottomPixel(long chipRow){
		return getChipTopPixel(chipRow)+chipHeight-1;
	}

	long getChipLeftPixel(long chipColumn){
		return chipColumn*(chipWidth+3);
	}
	long getChipRightPixel(long chipColumn){
		return getChipLeftPixel(chipColumn) + chipWidth-1;
	}
	
	
	public static ExcaliburEqualizationHelper getInstance() {
		return INSTANCE;
	}

/*	double[] convertUsingAxis(int[] indexData, double[] lookupTable) {
		double[] data = new double[indexData.length];
		for (int i = 0; i < data.length; i++) {
			data[i] = lookupTable[indexData[i]];
		}
		return data;
	}*/

	public Hdf5HelperData getConfigFromFile(String fileName) throws Exception{
		return Hdf5Helper.getInstance().readDataSetAll(fileName, getEqualisationLocation().getLocationForOpen(), THRESHOLD_FROM_THRESHOLD_RESPONSE_DATASET, true);
	}
	
	/**
	 * Calls getEdgeThresholdABResponseFromThresholdFiles and stores the data in the resultFile
	 * The result file has a set of slope values in dataset EDGE_THRESHOLD_RESPONSE_SLOPES_DATASET
	 * and a set of offset values in dataset EDGE_THRESHOLD_RESPONSE_OFFSETS_DATASET
	 */
	public void createEdgeThresholdABResponseFromThresholdFiles(List<String> edgeThresholdFilenames, String resultFile) throws Exception {
		Results responses = getEdgeThresholdABResponseFromThresholdFiles(edgeThresholdFilenames);
		Hdf5HelperData hdSlopes = new Hdf5HelperData(responses.getDims(), responses.getSlopes());
		Hdf5Helper.getInstance().writeToFileSimple(hdSlopes, resultFile, getEqualisationLocation(),
				THRESHOLD_RESPONSE_SLOPES_DATASET);
		Hdf5HelperData hdOffsets = new Hdf5HelperData(responses.getDims(), responses.getOffsets());
		Hdf5Helper.getInstance().writeToFileSimple(hdOffsets, resultFile, getEqualisationLocation(),
				THRESHOLD_RESPONSE_OFFSETS_DATASET);
	}

	/**
	 * Read the set of threshold values recorded in a set of files by calls to createThresholdFileFromScanData
	 * and return a set of straight line fits to the axis recorded in the attribute THRESHOLDABVAL_ATTR
	 * @param edgeThresholdFilenames
	 * @throws Exception
	 */
	public Results getEdgeThresholdABResponseFromThresholdFiles(List<String> edgeThresholdFilenames) throws Exception {

		return getStraightLineFit(edgeThresholdFilenames,THRESHOLDABNVAL_ATTR, THRESHOLD_DATASET);
	}

	/**
	 * Calls getEdgeThresholdNResponseFromThresholdFiles and stores the data in the resultFile
	 * The result file has a set of slope values in dataset EDGE_THRESHOLD_RESPONSE_SLOPES_DATASET
	 * and a set of offset values in dataset EDGE_THRESHOLD_RESPONSE_OFFSETS_DATASET
	 */
	public void createThresholdNResponseFromThresholdFiles(List<String> edgeThresholdFilenames, String resultFile) throws Exception {
		Results responses = getEdgeThresholdNResponseFromThresholdFiles(edgeThresholdFilenames);
		Hdf5HelperData hdSlopes = new Hdf5HelperData(responses.getDims(), responses.getSlopes());
		Hdf5Helper.getInstance().writeToFileSimple(hdSlopes, resultFile, getEqualisationLocation(),
				THRESHOLD_RESPONSE_SLOPES_DATASET);
		Hdf5HelperData hdOffsets = new Hdf5HelperData(responses.getDims(), responses.getOffsets());
		Hdf5Helper.getInstance().writeToFileSimple(hdOffsets, resultFile, getEqualisationLocation(),
				THRESHOLD_RESPONSE_OFFSETS_DATASET);
	}
	
	/**
	 * Read the set of threshold values recorded in a set of files by calls to createChipAveragedThresholdFileFromScanData
	 * and return a set of straight line fits to the axis recorded in the attribute THRESHOLDABVAL_ATTR
	 * @param edgeThresholdFilenames
	 * @throws Exception
	 */
	public Results getEdgeThresholdNResponseFromThresholdFiles(List<String> edgeThresholdFilenames) throws Exception {

		return getStraightLineFit(edgeThresholdFilenames,THRESHOLDABNVAL_ATTR, CHIP_THRESHOLD_GAUSSIAN_POSITION_DATASET);
	}
	
	public Results getStraightLineFit(List<String> datasetFileNames, String axisAttributeName, String datasetName) throws Exception {

		double[] thresholdABArray = new double[datasetFileNames.size()];
		Vector<Object> data = new Vector<Object>();
		long[] dims = null; 
		for (int i = 0; i < datasetFileNames.size(); i++) {
			// get data for filename
			Hdf5HelperData readAttribute = Hdf5Helper.getInstance().readAttribute(datasetFileNames.get(i),
					TYPE.DATASET, getEdgeThresholdsLocation().getLocationForOpen(), axisAttributeName);
			thresholdABArray[i] = ((double[]) readAttribute.data)[0];
			Hdf5HelperData edgeThresholds = Hdf5Helper.getInstance().readDataSetAll(datasetFileNames.get(i),
					getEqualisationLocation().getLocationForOpen(), datasetName, true);

			data.add(edgeThresholds.data);
			if (ArrayUtils.getLength(data.get(data.size()-1)) != ArrayUtils.getLength(data.get(0)))
				throw new Exception("Data lengths in different files are not equal");
			dims = edgeThresholds.dims;
		}
		return StraightLineFit.fitInt(data, dims, thresholdABArray);
	}
	
	/**
	 * 
	 * Combine the threshold data stored in files by calling createThresholdFileFromScanData multiple times
	 * into a single file.
	 * Each call of createThresholdFileFromScanData creates a file with containing a 2d dataset giving the threshold edge values
	 * for each pixel. This dataset has an attribute THRESHOLDAVAL_ATTR containing the value of
	 * either thresholdA or thresholdB for the scan.
	 * The values of THRESHOLDAVAL_ATTR also combined into a dataset that is stored in the result file
	 * as dataset THRESHOLDAB_DATASET which acts as the 3rd axis for the conbined 3d data
	 */
	public void combineThresholdsFromThresholdFiles(List<String> edgeThresholdFiles, 
			String resultFile, boolean includeChipData) throws Exception {
		Hdf5Helper hdf =Hdf5Helper.getInstance();
		hdf.concatenateDataSetsFromFiles(edgeThresholdFiles, getEqualisationLocation(), THRESHOLD_DATASET,
				resultFile);
		if(includeChipData ){
			hdf.concatenateDataSetsFromFiles(edgeThresholdFiles, getEqualisationLocation(), CHIP_PRESENT_DATASET,resultFile);
			hdf.concatenateDataSetsFromFiles(edgeThresholdFiles, getEqualisationLocation(), CHIP_THRESHOLD_GAUSSIAN_FWHM_DATASET,resultFile);
			hdf.concatenateDataSetsFromFiles(edgeThresholdFiles, getEqualisationLocation(), CHIP_THRESHOLD_GAUSSIAN_POSITION_DATASET,resultFile);
		}

		@SuppressWarnings("cast")
		Hdf5HelperData signalData = new Hdf5HelperData((int) 1);

		HDF5HelperLocations edgeloc = getEdgeThresholdsLocation();
		hdf.writeAttribute(resultFile, Hdf5Helper.TYPE.DATASET, edgeloc, "signal", signalData);
		Vector<Hdf5HelperData> data = new Vector<Hdf5HelperData>();
		for (String f : edgeThresholdFiles) {
			data.add(hdf.readAttribute(f, Hdf5Helper.TYPE.DATASET, edgeloc.getLocationForOpen(),
					THRESHOLDABNVAL_ATTR));
		}
		hdf.concatenateDataSets(data, getEqualisationLocation(), THRESHOLDABNVAL_DATASET, resultFile);
		@SuppressWarnings("cast")
		Hdf5HelperData axisData = new Hdf5HelperData((int) 1);

		HDF5HelperLocations thresholdLoc = getEqualisationLocation();
		thresholdLoc.add(THRESHOLDABNVAL_DATASET);

		hdf.writeAttribute(resultFile, Hdf5Helper.TYPE.DATASET, thresholdLoc, "axis", axisData);
	}
	

	
	/**
	 * Calls getConfigFromThresholdResponseFile and stores result in the result file
	 * The file contains the set of values of thesholdA or thresholdB for a given thresholdTarget in the dataset
	 * CONFIG_FROM_THRESHOLD_RESPONSE_DATASET. This dataset has an attribute THRESHOLD_TARGET_ATTR that
	 * gives the targetThreshold
	 * 
	 * @param edgeThresholdABResponseFile
	 * @param thresholdTarget
	 * @param resultFileName
	 * @throws Exception
	 */
	public void createThresholdFromThresholdResponseFile(String edgeThresholdABResponseFile, int thresholdTarget,
			String resultFileName) throws Exception {

		Hdf5HelperData thresholdABData = getThresholdFromThresholdResponseFile(edgeThresholdABResponseFile, thresholdTarget);
		Hdf5Helper.getInstance().writeToFileSimple(thresholdABData, resultFileName, getEqualisationLocation(),
				THRESHOLD_FROM_THRESHOLD_RESPONSE_DATASET); 
		Hdf5Helper.getInstance().writeAttribute(resultFileName, Hdf5Helper.TYPE.DATASET,
				getEqualisationLocation().add(THRESHOLD_FROM_THRESHOLD_RESPONSE_DATASET), THRESHOLD_TARGET_ATTR,
				new Hdf5HelperData(thresholdTarget));

	}

	
	/**
	 * From the thresholdAB response stored in the file thresholdResponseFilename calculate the
	 * value of thresholdA or B for each pixel that gives the desired thresholdTarget
	 * 
	 * For each pixel:
	 * thresholdEdge = slope* thresholdA + offset
	 * 
	 * for a given thresholdEdge, slope and offset this function returns the value of thresholdA
	 *  
	 * @param thresholdResponseFilename
	 *            file containing the slope and offsets for each pixel on the detector of the threshold edge to
	 *            ThresholdA ( or B) value
	 * @param thresholdTarget
	 *            the value for the edge response for which the ThresholdA is to be calc based on the slope and ofset of
	 *            each pixel
	 * @return array of size equal to slope and offset. Each entry is the value of ThresholdA or B that gives the
	 *         desired thresholdTarget given the slope and offset values
	 * @throws Exception
	 */
	public Hdf5HelperData getThresholdFromThresholdResponseFile(String thresholdResponseFilename, double thresholdTarget)
			throws Exception {
		Hdf5HelperData offsetData = Hdf5Helper.getInstance().readDataSetAll(thresholdResponseFilename,
				getEqualisationLocation().getLocationForOpen(), THRESHOLD_RESPONSE_OFFSETS_DATASET, true);
		Hdf5HelperData slopeData = Hdf5Helper.getInstance().readDataSetAll(thresholdResponseFilename,
				getEqualisationLocation().getLocationForOpen(), THRESHOLD_RESPONSE_SLOPES_DATASET, true);
		double[] offsets = (double[]) offsetData.data;
		double[] slopes = (double[]) slopeData.data;
		if (offsets.length != slopes.length)
			throw new IllegalArgumentException("offsets.length != slopes.length");
		short[] thresholdAB = new short[slopes.length];
		for (int i = 0; i < thresholdAB.length; i++) {
			double thresholdABDbl = (thresholdTarget - offsets[i]) / slopes[i];
			thresholdAB[i] = (short) Math.round(thresholdABDbl);
		}
		return new Hdf5HelperData(offsetData.dims, thresholdAB);
	}

	public HDF5HelperLocations getEqualisationLocation() {
		HDF5HelperLocations loc = new HDF5HelperLocations();
		loc.add(new HDF5NexusLocation("entry1", "NXentry"));
		loc.add(new HDF5NexusLocation("equalisation", "NXdata"));
		return loc;
	}

	public HDF5HelperLocations getEdgeThresholdsLocation() {
		return getEqualisationLocation().add(THRESHOLD_DATASET);
	}

	/**
	 * Calls getThresholdFromScanData and stores the result in the result file
	 * 
	 * The edge threshold values are stored in dataset EDGE_THRESHOLDS_DATASET
	 * This dataset has the edgeThreshold limit stored in the attribute THRESHOLD_LIMIT_ATTR and
	 * the value of detector thresholdABNName stored in attribute THRESHOLDABVAL_ATTR
	 * 
	 * 
	 */
	public void createThresholdFileFromScanData(String filename, String detectorName, String threshold0Name,
			String thresholdABNName, int edgeThreshold, int sizeOfSlice, String resultfilename) throws Exception {
		String detectorLocation = "entry1/" + detectorName;
		Hdf5HelperData hd = getThresholdFromScanData(edgeThreshold, sizeOfSlice, filename, detectorLocation, threshold0Name,
				"data");
		HDF5HelperLocations equalisationLocation = getEqualisationLocation();
		Hdf5Helper hdf = Hdf5Helper.getInstance();
		hdf.writeToFileSimple(hd, resultfilename, equalisationLocation, THRESHOLD_DATASET);
		hdf.writeAttribute(resultfilename, Hdf5Helper.TYPE.DATASET, getEdgeThresholdsLocation(),
				THRESHOLD_LIMIT_ATTR, new Hdf5HelperData(edgeThreshold));
		Hdf5HelperData thresholdAValData = hdf.readDataSetAll(filename, detectorLocation,
				thresholdABNName, true);
		thresholdAValData.dims = new long[] { 1 };
		hdf.writeAttribute(resultfilename, Hdf5Helper.TYPE.DATASET, getEdgeThresholdsLocation(),
				THRESHOLDABNVAL_ATTR, thresholdAValData);
	}

	/**
	 * From the scan of detector name against threshold0 calculate the edge threshold for each pixel
	 * and save results to the file.
	 * As the data for a complete scan can be too large for the memepry available the data is treated
	 * in slices by calling getThresholdFromSliceofScanData.
	 */
	public Hdf5HelperData getThresholdFromScanData(int threshold, int sizeOfSlice, String scanFilename, String groupName,
			String dataSetNameThreshold0, String dataSetNameDetector) throws Exception {

		Hdf5HelperLazyLoader loader = new Hdf5HelperLazyLoader(scanFilename, groupName, dataSetNameDetector, false);
		ILazyDataset dataset = loader.getLazyDataSet();
		int[] shape = dataset.getShape();
		int[] stop = dataset.getShape();
		int[] start = new int[stop.length];
		for (int i = 0; i < shape.length; i++) {
			start[i] = 0;
			stop[i] = shape[i];
		}
		start[shape.length - 2] = 0;
		stop[shape.length - 2] = sizeOfSlice;

		short[] edgeThresholds = new short[shape[1] * shape[2]];
		int iEdgeThresholdProcessed = 0;
		Hdf5HelperData threshold0Vals = Hdf5Helper.getInstance().readDataSetAll(scanFilename, groupName,
				dataSetNameThreshold0, true);
		double[] tmp = (double[]) threshold0Vals.data;
		short[] threshold0ValsAsInt = new short[tmp.length];
		for (int i = 0; i < tmp.length; i++) {
			threshold0ValsAsInt[i] = (short) tmp[i];
		}
		while (start[shape.length - 2] < shape[shape.length - 2]) {
			IDataset slice = dataset.getSlice(null, start, stop, null);
			if (!(slice instanceof IntegerDataset))
				throw new Exception("data is not of type IntegerDataset");
			IntegerDataset ds = ((IntegerDataset) slice);
			short[] edgeThresholdsSlice = getThresholdFromSliceofScanData((int[]) ds.getBuffer(),
					ds.getShape(), null, threshold, 0, false, threshold0ValsAsInt);
			System.arraycopy(edgeThresholdsSlice, 0, edgeThresholds, iEdgeThresholdProcessed,
					edgeThresholdsSlice.length);
			start[shape.length - 2] = Math.min(start[shape.length - 2]+sizeOfSlice,shape[shape.length - 2]);
			stop[shape.length - 2] = Math.min(stop[shape.length - 2]+sizeOfSlice,shape[shape.length - 2]);
			iEdgeThresholdProcessed += shape[2] * sizeOfSlice;//TODO surely shape[2] as we are slicing over shape[1]
		}
		return new Hdf5HelperData(new long[] { shape[1], shape[2] }, edgeThresholds);
	}
	/*
	 * Not to be called by scripts.
	 */
	public short[] getThresholdFromSliceofScanData(int[] data, int[] shape, int[] activePixels, int thresholdVal,
			int dimensionToTraverse, boolean isForward, short[] lookupTable)
			throws Exception {
		// assume dimensionToTraverse ==2
		if (shape.length != 3)
			throw new Exception("Invalid shape");
		short[] res = null;
		if (dimensionToTraverse == 2) {
			if (shape[2] != lookupTable.length)
				throw new Exception("shape[2] != lookupTable.length");
			int sizeOfResult = shape[0] * shape[1];
			res = new short[sizeOfResult];
			Arrays.fill(res, EDGE_POSITION_IF_PIXEL_MASKED_OUT);
			for (int pixelInMask = 0; pixelInMask < sizeOfResult; pixelInMask++) {
				if (activePixels == null || activePixels[pixelInMask] == 1) {
					res[pixelInMask] = EDGE_POSITION_IF_ALL_BELOW_THRESHOLD;
					int numberPointsPerPixel = shape[dimensionToTraverse];
					for (int iy = 0; iy < numberPointsPerPixel; iy++) {
						int pixel = isForward ? iy : numberPointsPerPixel - iy - 1;
						int val = data[pixel * sizeOfResult + pixelInMask];
						if (val >= thresholdVal) {
							if (iy == 0) {
								// all above
								res[pixelInMask] = EDGE_POSITION_IF_ALL_ABOVE_THRESHOLD;
							} else {
								res[pixelInMask] = lookupTable[pixel];
							}
							break;
						}
					}
				}
			}
		} else if (dimensionToTraverse == 0) {
			if (shape[0] != lookupTable.length)
				throw new Exception("shape[0] != lookupTable.length");
			int sizeOfResult = shape[1] * shape[2];
			res = new short[sizeOfResult];
			Arrays.fill(res, EDGE_POSITION_IF_PIXEL_MASKED_OUT);
			int numberPointsPerPixel = shape[dimensionToTraverse];
			/*
			 * for (int i = 0; i < shape[1]; i++) { for (int j = 0; j < shape[2]; j++) { int pixelInMask = i * shape[2]
			 * + j;
			 */for (int pixelInMask = 0; pixelInMask < sizeOfResult; pixelInMask++) {

				if (activePixels == null || activePixels[pixelInMask] == 1) {
					res[pixelInMask] = EDGE_POSITION_IF_ALL_BELOW_THRESHOLD;
					for (int iy = 0; iy < numberPointsPerPixel; iy++) {
						int pixel = isForward ? iy : numberPointsPerPixel - iy - 1;
						int index = pixelInMask + pixel * sizeOfResult;

						int val = data[index];
						if (val >= thresholdVal) {
							if (iy == 0) {
								// all above
								res[pixelInMask] = EDGE_POSITION_IF_ALL_ABOVE_THRESHOLD;
							} else {
								res[pixelInMask] = lookupTable[pixel];
							}
							break;
						}
					}
				}

			}
			// }
		}
		return res;
	}



	/**
	 * Calls getChipAveragedThresholdFromThresholdFile and writes result to resultFileName
	 * The resultFile has 3 datasets:
	 * CHIP_PRESENT_DATASET - dataset of shorts - value ==1 if chip is present
	 * THRESHOLD_DATASET - dataset of chip averaged threshold position
	 * CHIP_THRESHOLD_GAUSSIAN_FWHM_DATASET -  - dataset of fwhm of chip threshold population
	 * @param thresholdFile
	 * @param dims
	 * @param chipPresent
	 * @param resultFileName
	 * @throws Exception
	 */
	public void createChipAveragedThresholdFileFromThresholdFile(String thresholdFile, long[] dims, boolean chipPresent[], String resultFileName) throws Exception{
		Hdf5Helper hdf = Hdf5Helper.getInstance();
		int numItems = (int) hdf.lenFromDims(dims);
		if( numItems != chipPresent.length)
			throw new IllegalArgumentException("numItems != chipPresent.length");
		
		CompositeFunction[] aPeaks = getChipAveragedThresholdFromThresholdFile(thresholdFile, dims, chipPresent);
		if( numItems != aPeaks.length)
			throw new IllegalArgumentException("numItems != aPeaks.length");

		short [] present = new short[numItems];
		double [] positions = new double[numItems];
		double [] fwhms = new double[numItems];
		Arrays.fill(fwhms, FIT_FAILED_WIDTH);
		for( int i=0; i< numItems; i++){
			present[i] = (short) (chipPresent[i] ? 1 : 0);
			if( chipPresent[i]){
				if( aPeaks[i] != null){
					IPeak function = aPeaks[i].getPeak(0);
					positions[i] = function.getPosition();
					fwhms[i] =function.getFWHM();
				}
			}
			
		}
		hdf.writeToFileSimple(new Hdf5HelperData(dims, present), resultFileName, getEqualisationLocation(), CHIP_PRESENT_DATASET);
		hdf.writeToFileSimple(new Hdf5HelperData(dims, positions), resultFileName, getEqualisationLocation(), CHIP_THRESHOLD_GAUSSIAN_POSITION_DATASET);
		hdf.writeToFileSimple(new Hdf5HelperData(dims, fwhms), resultFileName, getEqualisationLocation(), CHIP_THRESHOLD_GAUSSIAN_FWHM_DATASET);
	}
	
	
	/**
	 * 
	 * Gets the threshold values averaged over a population of pixels on a chip read from a threshold file previously
	 * created by calling createThresholdFileFromScanData
	 * .
	 * Loads the edge threshold calc for each pixel in a set of chips.
	 * Gets population of values for each chip and fits a gaussian to get average and fwhm
	 * @param fileName
	 * @return A 2d array of APeaks that match the structure of the chips in the detector
	 * @throws Exception 
	 */
	public CompositeFunction[] getChipAveragedThresholdFromThresholdFile(String fileName,  long[] chipDims, boolean chipPresent[]) throws Exception{
		if( chipDims.length!= 2)
			throw new IllegalArgumentException("chipDims.length!= 2");
		String groupName = getEqualisationLocation().getLocationForOpen();
		String datasetName = THRESHOLD_DATASET;
		Hdf5HelperLazyLoader loader = new Hdf5HelperLazyLoader(fileName, groupName, datasetName, false );
		int[] shape = loader.getLazyDataSet().getShape();
		if( shape.length != 2){
			throw new IllegalArgumentException("shape.length != 2");
		}
		long fullHeight = shape[0];
		long numChipsHigh = chipDims[0];
		long reqdHeight = getChipBottomPixel(numChipsHigh-1)+1;
		if(fullHeight != reqdHeight){
			throw new IllegalArgumentException("fullHeight != reqdHeight: " + reqdHeight);
		}
		long fullWidth = shape[1];
		long numChipsAcross = chipDims[1];
		long reqdWidth = getChipRightPixel(numChipsAcross-1)+1;
		if(fullWidth != reqdWidth){
			throw new IllegalArgumentException("fullWidth != reqdWidth: " + reqdWidth);
		}
			
		CompositeFunction [] aPeaks = new CompositeFunction[(int) (numChipsHigh * numChipsAcross)];
		Arrays.fill(aPeaks, null);
		int[] start = new int[2]; 
		int[] stop = new int[2]; 
		int[] step= new int[2];
		step[0]=step[1]=1;
		for( int ih=0; ih< numChipsHigh; ih++){
			start[0] = (int) getChipTopPixel(ih);
			stop[0] = (start[0]+chipHeight); //exclusive
			for( int iw=0; iw< numChipsAcross; iw++){
				int chipIndex = (int) (ih *numChipsAcross + iw);
				if( chipPresent[chipIndex]){
					start[1] = (int) getChipLeftPixel(iw);
					stop[1] = (start[1] + chipWidth); //exclusive
					AbstractDataset dataset = loader.getDataset(null, null, start, stop, step);
					IntegerDataset dataset2 = getDatasetWithValidPixels(dataset);
					if( dataset2 != null){
						int offset = dataset2.min().intValue();
						int[] population = createBinnedPopulation( dataset2);						
						aPeaks[chipIndex] = fitGaussianToBinnedPopulation(population, offset);
					}
				}
			}
		}
		return aPeaks;
	}
	
	boolean thresholdEdgePosIsValid(short value){
		return 	( value != EDGE_POSITION_IF_ALL_ABOVE_THRESHOLD && value != EDGE_POSITION_IF_ALL_BELOW_THRESHOLD && value != EDGE_POSITION_IF_PIXEL_MASKED_OUT);
	}
	
	IntegerDataset getDatasetWithValidPixels(AbstractDataset dataset){
		int [] validData = new int[dataset.getSize()];
		int numValidData = 0;
		IndexIterator iter = dataset.getIterator();

		while (iter.hasNext()){
			short value = (short) dataset.getElementLongAbs(iter.index);
			if( thresholdEdgePosIsValid(value)){
				validData[numValidData] = value;
				numValidData++;
			}
		}
		return numValidData > 0 ? new IntegerDataset(Arrays.copyOf(validData, numValidData),numValidData) : null;
	}
	/**
	 * 
	 * @return result of fitting gaussian over the population within the slice given by start, stop, step
	 */
	public CompositeFunction fitGaussianToBinnedPopulation(int[] population, int offset){ 
		

		double [] xvals = new double[population.length]; 
		double [] yvals = new double[population.length]; 
		int numFound=0;
		for( int i=0; i< population.length; i++){
			if( population[i]>0){
				xvals[numFound]=i + offset;
				yvals[numFound] = population[i];
				numFound +=1;
			}
		}
		xvals = Arrays.copyOf(xvals, numFound);
		yvals = Arrays.copyOf(yvals, numFound);
		if( numFound > 0){
			xvals = Arrays.copyOf(xvals, numFound);
			yvals = Arrays.copyOf(yvals, numFound);
			DoubleDataset xvals_ds = new DoubleDataset(xvals);
			DoubleDataset yvals_ds = new DoubleDataset(yvals);
			boolean backgroundDominated = false;
			boolean autoStopping = true;
			double threshold = 0.10;
			int numPeaks = 1;
			int smoothing = 5;
			
			double xmax = xvals_ds.max().doubleValue();
			double xmin = xvals_ds.min().doubleValue();
			double ymax = yvals_ds.max().doubleValue();
			double ymin = yvals_ds.min().doubleValue();
			double xavg = (xmax+xmin)/2;
			double xfwhm = (xmax-xmin)/4;
			double yheight = ymax-ymin;
			APeak peakFunction = new Gaussian(xavg,xfwhm,yheight*xfwhm);
			List<CompositeFunction> fittedPeakList = Generic1DFitter.fitPeakFunctions(xvals_ds, yvals_ds, peakFunction, new GeneticAlg(0.0001),
					smoothing, numPeaks, threshold, autoStopping, backgroundDominated);
			if( fittedPeakList != null && fittedPeakList.size() ==1){
				return fittedPeakList.get(0);
			}
		}
		return null;
	}
	
	int[] createBinnedPopulation(AbstractDataset ids) {
		int minVal = ids.min().intValue();
		//range of values is from minVal to max. Num of bins is range +1
		//first bin contains population with val = minVal
		int [] numInBins  = new int[ids.max().intValue()-minVal+1];
		Arrays.fill(numInBins, 0);
		IndexIterator iter = ids.getIterator();

		while (iter.hasNext()){
			int value = (int) ids.getElementLongAbs(iter.index);
			numInBins[value-minVal] += 1;
		}
		return numInBins;
	}

	public void createThresholdNOpt(String thresholdNChipResponseFile, String thresholdN50ChipThresholdFile, double equalisationTarget, String resultFile) throws Exception{
		Hdf5Helper hdf = Hdf5Helper.getInstance();
		//read the offset and slope - a1, a2
		Hdf5HelperData offsetData = hdf.readDataSetAll(thresholdNChipResponseFile, getEqualisationLocation().getLocationForOpen(), THRESHOLD_RESPONSE_OFFSETS_DATASET, true);
		Hdf5HelperData slopeData = hdf.readDataSetAll(thresholdNChipResponseFile, getEqualisationLocation().getLocationForOpen(), THRESHOLD_RESPONSE_SLOPES_DATASET, true);

		//read the fwhm for thresholdN=50
		Hdf5HelperData fwhmData = hdf.readDataSetAll(thresholdN50ChipThresholdFile, getEqualisationLocation().getLocationForOpen(), CHIP_THRESHOLD_GAUSSIAN_FWHM_DATASET, true);

		long lenFromOffsetData = hdf.lenFromDims(offsetData.dims);
		long lenFromSlopeData = hdf.lenFromDims(slopeData.dims);
		long lenFromFwhmData = hdf.lenFromDims(fwhmData.dims);
		
		if(lenFromOffsetData !=  lenFromSlopeData || lenFromSlopeData != lenFromFwhmData)
			throw new IllegalArgumentException("lenFromOffsetData !=  lenFromSlopeData || lenFromSlopeData != lenFromFwhmData");
		short [] thresholdNOpt = new short[(int) lenFromOffsetData];
		double [] threshold0opt = new double[(int) lenFromOffsetData];
		for( int i=0; i< lenFromOffsetData; i++){
			threshold0opt[i] = 3.2* Array.getDouble(fwhmData.data, i) + equalisationTarget;
			thresholdNOpt[i] = (short)((threshold0opt[i] - Array.getDouble(offsetData.data,i))/(Array.getDouble(slopeData.data,i)));
		}
		Hdf5HelperData data = new Hdf5HelperData(offsetData.dims, threshold0opt);
		hdf.writeToFileSimple(data, resultFile, getEqualisationLocation(), THRESHOLD_0OPT);
		hdf.writeAttribute(resultFile, Hdf5Helper.TYPE.DATASET, getEqualisationLocation().add(THRESHOLD_0OPT),
				THRESHOLD_N_EQ_TARGET_ATTR, new Hdf5HelperData(equalisationTarget));		
		data = new Hdf5HelperData(offsetData.dims, thresholdNOpt);
		hdf.writeToFileSimple(data, resultFile, getEqualisationLocation(), THRESHOLD_NOPT);
		hdf.writeAttribute(resultFile, Hdf5Helper.TYPE.DATASET, getEqualisationLocation().add(THRESHOLD_NOPT),
				THRESHOLD_N_EQ_TARGET_ATTR, new Hdf5HelperData(equalisationTarget));		
	}
	
	/**
	 * Set thresholdN in the hardware to the values in the edgeThresholdNResponseFile
	 * If setThresholdAFromMask is true set then set thresholdA on each pixel to either 0 or 16 dependent on value in mask
	 * @param edgeThresholdNResponseFile
	 * @param readoutFems
	 * @throws Exception
	 */
	public void setThresholdNFromFile(String edgeThresholdNResponseFile, List<ExcaliburReadoutNodeFem> readoutFems, boolean setThresholdAFromMask) throws Exception{
		Hdf5Helper hdf = Hdf5Helper.getInstance();
		Hdf5HelperData hdata = hdf.readDataSetAll(edgeThresholdNResponseFile, getEqualisationLocation().getLocationForOpen(), THRESHOLD_NOPT, true);
		if( hdata.dims.length!=2)
			throw new IllegalArgumentException("data.dims.length!=2");
		if( hdata.dims[0]!= readoutFems.size())
			throw new IllegalArgumentException("data.dims[0]!= readoutFems.size()");
		if( hdata.dims[1]!=ExcaliburReadoutNodeFem.CHIPS_PER_FEM)
			throw new IllegalArgumentException("data.dims[1]!=CHIPS_PER_FEM");
		
		short [] thresholdNOpt = (short[]) hdata.data;
		for( int ifem=0; ifem<readoutFems.size(); ifem++ ){
			ExcaliburReadoutNodeFem fem = readoutFems.get(ifem);
			for( int ichip=0; ichip< ExcaliburReadoutNodeFem.CHIPS_PER_FEM; ichip++){
				int index = ifem*ExcaliburReadoutNodeFem.CHIPS_PER_FEM + ichip;
				MpxiiiChipReg chip = fem.getIndexedMpxiiiChipReg(ichip);
				chip.getAnper().setThresholdn(thresholdNOpt[index]);

				if(setThresholdAFromMask){
					long chipTopPixel = getChipTopPixel(ifem);
					long chipLeftPixel = getChipLeftPixel(ichip);
					long[] sstride = new long[]{ 1,1};
					long[] sstart = new long[]{chipTopPixel, chipLeftPixel};
					long[] dsize = new long[]{chipHeight, chipWidth};
					Hdf5HelperData hmask = hdf.readDataSet(edgeThresholdNResponseFile, getEqualisationLocation().getLocationForOpen(), THRESHOLDN_MASK, 
							sstart, sstride, dsize);
					short[] mask = (short[])hmask.data;
					short[] thresholdA = new short[mask.length];
					for( int i=0; i< mask.length;i++){
						thresholdA[i] = (short) (mask[i]==1 ? 16 : 0);
					}
					chip.getPixel().setThresholdA(thresholdA);
				}
				
			}
		}
	}
	/**
	 * Creates a useThresholdN mask. For each pixel decide whether the threshold is closest to the optimim( read from
	 * optThresholdFile) with or without use of thresholdN. The result is a 2d array of shorts (1=use, 0= do not use)
	 * written into the result file
	 * 
	 * The optThresholdFile contains 1 value per chip
	 * @throws Exception 
	 */
	public void createThresholdNOptMask(String optThresholdFile, String notUsingThresholdNFile, String usingThresholdNFile, String resultFile) throws Exception{
		Hdf5Helper hdf = Hdf5Helper.getInstance();
		Hdf5HelperData hthresholdOpt = hdf.readDataSetAll(optThresholdFile, getEqualisationLocation().getLocationForOpen(), THRESHOLD_0OPT, true);
		Hdf5HelperData hthresholdNotUsingThresholdN = hdf.readDataSetAll(notUsingThresholdNFile, getEqualisationLocation().getLocationForOpen(), THRESHOLD_DATASET, true);
		Hdf5HelperData hthresholdUsingThresholdN = hdf.readDataSetAll(usingThresholdNFile, getEqualisationLocation().getLocationForOpen(), THRESHOLD_DATASET, true);
		if( hthresholdUsingThresholdN.dims.length!=hthresholdNotUsingThresholdN.dims.length)
			throw new IllegalArgumentException("hthresholdUsingThresholdN.dims.length!=hthresholdNotUsingThresholdN.dims.length");
			
		if( hthresholdOpt.dims.length!=2)
			throw new IllegalArgumentException("hthresholdOpt.dims.length!=2");
		long totalHeight = hthresholdNotUsingThresholdN.dims[0];
		long totalWidth = hthresholdNotUsingThresholdN.dims[1];
		short[] notUsingThresholdN = (short[]) hthresholdNotUsingThresholdN.data;
		short[] usingThresholdN = (short[]) hthresholdUsingThresholdN.data;
		short[] mask = new short[(int) (totalHeight*totalWidth)];

		long numChipsRows = hthresholdOpt.dims[0];
		long numChipsAcross = hthresholdOpt.dims[1];
		for( int iChipRow=0; iChipRow< numChipsRows; iChipRow++){
			long chipTopPixel = getChipTopPixel(iChipRow);
			long chipBottomPixel = getChipBottomPixel(iChipRow);
			for( int iChipCol=0; iChipCol< numChipsAcross; iChipCol++){
				//get thresholdOpt for this chip
				double thresholdOpt = Array.getDouble(hthresholdOpt.data, (int) (iChipRow*numChipsAcross + iChipCol));
				long chipLeftPixel = getChipLeftPixel(iChipCol);
				long chipRightPixel = getChipRightPixel(iChipCol);
				for( long ix= chipTopPixel; ix<chipBottomPixel; ix++){
					for( long iy= chipLeftPixel; iy<chipRightPixel; iy++){
						long index = ix * totalWidth + iy;
						short using = usingThresholdN[(int) index];
						short notusing = notUsingThresholdN[(int) index];
						short maskVal=0;
						if( thresholdEdgePosIsValid(using) && thresholdEdgePosIsValid(notusing))
							maskVal = (short) (Math.abs(thresholdOpt -using) < Math.abs(thresholdOpt - notusing) ? 1:0);
						else if ( thresholdEdgePosIsValid(using))
							maskVal = 1;
						else if ( thresholdEdgePosIsValid(notusing))
							maskVal = 0;
						mask[(int) index] = maskVal;
					}
					
				}
			}
		}
		Hdf5HelperData hmask = new Hdf5HelperData(hthresholdNotUsingThresholdN.dims, mask);
		hdf.writeToFileSimple(hmask, resultFile, getEqualisationLocation(), THRESHOLDN_MASK);
		
	}
	
	/**
	 * Gets the max threshold0 limit required so that all but numPixelsOutSide pixels have a value within the limit.
	 * The threshold0 limit is chip specific. Write results into selectThresholdTargetThresholdFile as long[].
	 * return max
	 * @throws ScanFileHolderException 
	 */
	public long createThresholdTarget(String thresholdFile, long[] chipDims, boolean chipPresent[], long numPixelsOutSide, String resultFile) throws ScanFileHolderException{
		//need to get binned populations and then get value to include all but 100 values
		if( chipDims.length!= 2)
			throw new IllegalArgumentException("chipDims.length!= 2");
		String groupName = getEqualisationLocation().getLocationForOpen();
		String datasetName = THRESHOLD_DATASET;
		Hdf5HelperLazyLoader loader = new Hdf5HelperLazyLoader(thresholdFile, groupName, datasetName, false );
		int[] shape = loader.getLazyDataSet().getShape();
		if( shape.length != 2){
			throw new IllegalArgumentException("shape.length != 2");
		}
		long fullHeight = shape[0];
		long numChipsHigh = chipDims[0];
		long reqdHeight = getChipBottomPixel(numChipsHigh-1)+1;
		if(fullHeight != reqdHeight){
			throw new IllegalArgumentException("fullHeight != reqdHeight: " + reqdHeight);
		}
		long fullWidth = shape[1];
		long numChipsAcross = chipDims[1];
		long reqdWidth = getChipRightPixel(numChipsAcross-1)+1;
		if(fullWidth != reqdWidth){
			throw new IllegalArgumentException("fullWidth != reqdWidth: " + reqdWidth);
		}
			
		int [] thresholdLimit = new int[(int) (numChipsHigh * numChipsAcross)];
		int[] start = new int[2]; 
		int[] stop = new int[2]; 
		int[] step= new int[2];
		step[0]=step[1]=1;
		for( int ih=0; ih< numChipsHigh; ih++){
			start[0] = (int) getChipTopPixel(ih);
			stop[0] = (start[0]+chipHeight); //exclusive
			for( int iw=0; iw< numChipsAcross; iw++){
				int chipIndex = (int) (ih *numChipsAcross + iw);
				if( chipPresent[chipIndex]){
					start[1] = (int) getChipLeftPixel(iw);
					stop[1] = (start[1] + chipWidth); //exclusive
					AbstractDataset dataset = loader.getDataset(null, null, start, stop, step);
					IntegerDataset dataset2 = getDatasetWithValidPixels(dataset);
					if( dataset2 != null){
						int offset = dataset2.min().intValue();
						int[] population = createBinnedPopulation( dataset2);						
						thresholdLimit[chipIndex] = 0;//TODO find thresholdLimitfitGaussianToBinnedPopulation(population, offset);
					}
				}
			}
		}
		//write results into file.
		IntegerDataset dataset = new IntegerDataset(thresholdLimit, thresholdLimit.length);
		return dataset.max().longValue();

	}
}
