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

import gda.analysis.numerical.straightline.Result;
import gda.analysis.numerical.straightline.Results;
import gda.analysis.numerical.straightline.StraightLineFit;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.apache.commons.lang.ArrayUtils;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.DoubleDataset;
import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import uk.ac.diamond.scisoft.analysis.dataset.ILazyDataset;
import uk.ac.diamond.scisoft.analysis.dataset.IndexIterator;
import uk.ac.diamond.scisoft.analysis.dataset.ShortDataset;
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
 * A class to provide analysis functions for use by the equalisation script
 * 
 * 
 */
public class ExcaliburEqualizationHelper {

	private static final double FWHM_OVER_SIGMA = 2.3548;

	/*
	 * Name of attribute in hdf file for equalisation target used in generating THRESHOLD_NOPT and THRESHOLD_0OPT
	 */
	public static final String THRESHOLD_N_EQ_TARGET_ATTR = "thresholdNopt_eq_target";

	/*
	 * Name of dataset in hdf file for optimised thresholdN values. One value per chip. Type short[]
	 */
	public static final String THRESHOLD_NOPT = "thresholdNopt"; 

	/*
	 * Name of dataset in hdf file for optimised DAC pixel values. One value per chip. Type short[]
	 */
	public static final String DACPIXEL_OPT = "DACPixelopt"; 

	/*
	 * Name of dataset in hdf file for threshold0opt values. 
	 * Calculated when generating thresholdN values:
	 * 3.2 * Array.getDouble(fwhmData.data, i)/2.35 + equalisationTarget;
	 * One value per chip. Type double[]
	 */
	public static final String THRESHOLD_0OPT = "threshold0opt"; 
	
	/*
	 * Name of dataset in hdf file for mask to indicate pixel that are to use thresholdN
	 * if value is 1 the thresholdN value is to be used.
	 * One value per pixel. Type short[]
	 */
	public static final String THRESHOLDN_MASK = "thresholdN_mask";
	
	/*
	 * Value for chip averaged population gaussian fit failure
	 */
	public static final double FIT_FAILED_WIDTH = -1.0;

	/*
	 * Value for threshold edge position when counts are below threshold for all threshold0 values = -10
	 * valid values are between 0 and 511
	 */
	public static final short EDGE_POSITION_IF_ALL_BELOW_THRESHOLD = -10; 
	
	/*
	 * Value for threshold edge position when counts are over threshold for all threshold0 values = 515
	 * valid values are between 0 and 511
	 */
	public static final short EDGE_POSITION_IF_ALL_ABOVE_THRESHOLD = 515;

	/*
	 * Value for threshold edge position if pixel is masked out = -20
	 * valid values are between 0 and 511
	 */
	public static final short EDGE_POSITION_IF_PIXEL_MASKED_OUT = -20;

	/*
	 * Name of attribute in hdf file for threshold target used in generating thresholdA values
	 * Type int
	 */
	public static final String THRESHOLD_TARGET_ATTR = "thresholdTarget";

	/*
	 * Name of dataset in hdf file for thresholdA values evaluated for either thresholdN on or off
	 * One value per pixel. Type short[]
	 */
	public static final String THRESHOLD_FROM_THRESHOLD_RESPONSE_DATASET = "thresholdFromThresholdResponse";

	public static short THRESHOLD_FROM_THRESHOLD_INVALID = -1 ; //valid values are 0-31 see createThresholdAdjAndMask
	
	/*
	 * Name of dataset in hdf file for thresholdA values evaluated from thresholdA value for thresholdN on and off
	 * One value per pixel. Type short[]
	 */
	public static final String THRESHOLDADJ_DATASET = "thresholdAdj";

	/*
	 * Name of dataset in hdf file for mask to indicate pixel that no valid value for thresholdA could be found
	 * if value is 1 no valid value for the thresholdA could be found
	 * One value per pixel. Type short[]
	 */
	public static final String THRESHOLDADJ_MASK_DATASET = "thresholdAdjMask";
	
	
	public static final String THRESHOLD_RESPONSE_OFFSETS_DATASET = "edgeThresholdResponseOffsets";
	public static final String THRESHOLD_RESPONSE_SLOPES_DATASET = "edgeThresholdResponseSlopes";
	public static final String THRESHOLD_RESPONSE_FITOK_DATASET = "edgeThresholdResponseFitOK";
	public static final short THRESHOLD_RESPONSE_FITOK_TRUE = 1;
	public static final String THRESHOLDABNVAL_DATASET = "thresholdABNVal";
	public static final String THRESHOLDABNVAL_ATTR = "thresholdABNVal"; // identifies the DAC being set outside the
																			// scan
	public static final String THRESHOLD_LIMIT_ATTR = "thresholdLimit";
	public static final String THRESHOLD_LIMIT_DATASET = "thresholdLimit";
	// public static final String THRESHOLD_ATTRIBUTE = "threshold";
	public static final String THRESHOLD_STDDEV_ATTRIBUTE = "threshold_mean";
	public static final String THRESHOLD_MEAN_ATTRIBUTE = "threshold_stddev";
	public static final String THRESHOLD_DATASET = "edgeThresholds";
	public static final String CHIP_PRESENT_DATASET = "chip_present";
	public static final String CHIP_THRESHOLD_GAUSSIAN_POSITION_DATASET = "chip_threshold_gaussian_position";
	public static final String CHIP_THRESHOLD_GAUSSIAN_SIGMA_DATASET = "chip_threshold_gaussian_sigma";
	public static final String CHIP_THRESHOLD_GAUSSIAN_HEIGHT_DATASET = "chip_threshold_gaussian_height";
	private static final ExcaliburEqualizationHelper INSTANCE = new ExcaliburEqualizationHelper();

	public static ExcaliburEqualizationHelper getInstance() {
		return INSTANCE;
	}

	public Hdf5HelperData getConfigFromFile(String fileName) throws Exception {
		return Hdf5Helper.getInstance().readDataSetAll(fileName, getEqualisationLocation().getLocationForOpen(),
				THRESHOLD_FROM_THRESHOLD_RESPONSE_DATASET, true);
	}

	/**
	 * Calls getEdgeThresholdABResponseFromThresholdFiles and stores the data in the resultFile The result file has a
	 * set of slope values in dataset EDGE_THRESHOLD_RESPONSE_SLOPES_DATASET and a set of offset values in dataset
	 * EDGE_THRESHOLD_RESPONSE_OFFSETS_DATASET
	 */
	public void createEdgeThresholdABResponseFromThresholdFiles(List<String> edgeThresholdFilenames, String resultFile)
			throws Exception {
		Results responses = getEdgeThresholdABResponseFromThresholdFiles(edgeThresholdFilenames);
		Hdf5HelperData hdSlopes = new Hdf5HelperData(responses.getDims(), responses.getSlopes());
		Hdf5Helper.getInstance().writeToFileSimple(hdSlopes, resultFile, getEqualisationLocation(),
				THRESHOLD_RESPONSE_SLOPES_DATASET);
		Hdf5HelperData hdOffsets = new Hdf5HelperData(responses.getDims(), responses.getOffsets());
		Hdf5Helper.getInstance().writeToFileSimple(hdOffsets, resultFile, getEqualisationLocation(),
				THRESHOLD_RESPONSE_OFFSETS_DATASET);
		Hdf5HelperData hdfitok = new Hdf5HelperData(responses.getDims(), responses.getFitok());
		Hdf5Helper.getInstance().writeToFileSimple(hdfitok, resultFile, getEqualisationLocation(),
				THRESHOLD_RESPONSE_FITOK_DATASET);
	}

	/**
	 * Read the set of threshold values recorded in a set of files by calls to createThresholdFileFromScanData and
	 * return a set of straight line fits to the axis recorded in the attribute THRESHOLDABVAL_ATTR
	 * 
	 * @param edgeThresholdFilenames
	 * @throws Exception
	 */
	public Results getEdgeThresholdABResponseFromThresholdFiles(List<String> edgeThresholdFilenames) throws Exception {

		return getStraightLineFit(edgeThresholdFilenames, THRESHOLDABNVAL_ATTR, THRESHOLD_DATASET);
	}

	/**
	 * Calls getEdgeThresholdNResponseFromThresholdFiles and stores the data in the resultFile The result file has a set
	 * of slope values in dataset EDGE_THRESHOLD_RESPONSE_SLOPES_DATASET and a set of offset values in dataset
	 * EDGE_THRESHOLD_RESPONSE_OFFSETS_DATASET
	 */
	public void createDACResponseFromThresholdFiles(List<String> edgeThresholdFilenames, String resultFile)
			throws Exception {
		Results responses = getDACResponseFromThresholdFiles(edgeThresholdFilenames);
		Hdf5HelperData hdSlopes = new Hdf5HelperData(responses.getDims(), responses.getSlopes());
		Hdf5Helper.getInstance().writeToFileSimple(hdSlopes, resultFile, getEqualisationLocation(),
				THRESHOLD_RESPONSE_SLOPES_DATASET);
		Hdf5HelperData hdOffsets = new Hdf5HelperData(responses.getDims(), responses.getOffsets());
		Hdf5Helper.getInstance().writeToFileSimple(hdOffsets, resultFile, getEqualisationLocation(),
				THRESHOLD_RESPONSE_OFFSETS_DATASET);
		Hdf5HelperData hdfitok = new Hdf5HelperData(responses.getDims(), responses.getFitok());
		Hdf5Helper.getInstance().writeToFileSimple(hdfitok, resultFile, getEqualisationLocation(),
				THRESHOLD_RESPONSE_FITOK_DATASET);		
	}

	/**
	 * Read the set of threshold values recorded in a set of files by calls to
	 * createChipAveragedThresholdFileFromScanData and return a set of straight line fits to the axis recorded in the
	 * attribute THRESHOLDABVAL_ATTR
	 * 
	 * @param edgeThresholdFilenames
	 * @throws Exception
	 */
	public Results getDACResponseFromThresholdFiles(List<String> edgeThresholdFilenames) throws Exception {

		return getStraightLineFit(edgeThresholdFilenames, THRESHOLDABNVAL_ATTR,
				CHIP_THRESHOLD_GAUSSIAN_POSITION_DATASET);
	}

	public Results getStraightLineFitValidsOnly(List<Object> dataWithInvalid, long [] dims, double[] xWithInvalids){
		Object object = dataWithInvalid.get(0);
		if( ! object.getClass().isArray()) {
			throw new IllegalArgumentException("fitInt can only accept arrays");
		}
		
		int numLines = ArrayUtils.getLength(object);
		int pointsPerLine = xWithInvalids.length;
		if( dataWithInvalid.size() != pointsPerLine)
			throw new IllegalArgumentException("data.size() != pointsPerLine");
			
		for( int i=0; i< pointsPerLine; i++){
			if( ArrayUtils.getLength(dataWithInvalid.get(i)) != numLines)
				throw new IllegalArgumentException("data.get(i).length != numLines");
			
		}
		double [] slopes = new double[numLines];
		double [] offsets = new double[numLines];
		short [] fitoks = new short[numLines];
		Arrays.fill(fitoks, (short)0);
		for (int line = 0; line < numLines; line++) {
			double[] y = new double[pointsPerLine];
			double[] x = new double[pointsPerLine];
			int nvalid=0;
			for( int point=0; point<pointsPerLine; point++){
				short d = Array.getShort(dataWithInvalid.get(point),line);
				if(thresholdEdgePosIsValid(d) ){
					y[nvalid] = d;
					x[nvalid]=xWithInvalids[point];
					nvalid++;
				}
			}
			if( nvalid >= 2){
				y=Arrays.copyOf(y, nvalid);
				x=Arrays.copyOf(x, nvalid);
				Result fit[] = StraightLineFit.fit(new double[][]{y}, x);
				slopes[line]=fit[0].getSlope();
				offsets[line]=fit[0].getOffset();
				fitoks[line] = 1;
			}
		}
		return new Results(offsets, slopes, dims, fitoks);				
	}
	public Results getStraightLineFit(List<String> datasetFileNames, String axisAttributeName, String datasetName)
			throws Exception {

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
			if (ArrayUtils.getLength(data.get(data.size() - 1)) != ArrayUtils.getLength(data.get(0)))
				throw new Exception("Data lengths in different files are not equal");
			dims = edgeThresholds.dims;
		}
		boolean removeInvalids = ( data.get(0) instanceof short[]);

		return removeInvalids? getStraightLineFitValidsOnly(data, dims, thresholdABArray): StraightLineFit.fitInt(data, dims, thresholdABArray);
	}

	/**
	 * Combine the threshold data stored in files by calling createThresholdFileFromScanData multiple times into a
	 * single file. Each call of createThresholdFileFromScanData creates a file with containing a 2d dataset giving the
	 * threshold edge values for each pixel. This dataset has an attribute THRESHOLDAVAL_ATTR containing the value of
	 * either thresholdA or thresholdB for the scan. The values of THRESHOLDAVAL_ATTR also combined into a dataset that
	 * is stored in the result file as dataset THRESHOLDAB_DATASET which acts as the 3rd axis for the conbined 3d data
	 */
	public void combineThresholdsFromThresholdFiles(List<String> edgeThresholdFiles, String resultFile,
			boolean includeChipData) throws Exception {
		Hdf5Helper hdf = Hdf5Helper.getInstance();
		hdf.concatenateDataSetsFromFiles(edgeThresholdFiles, getEqualisationLocation(), THRESHOLD_DATASET, resultFile);
		if (includeChipData) {
			hdf.concatenateDataSetsFromFiles(edgeThresholdFiles, getEqualisationLocation(), CHIP_PRESENT_DATASET,
					resultFile);
			hdf.concatenateDataSetsFromFiles(edgeThresholdFiles, getEqualisationLocation(),
					CHIP_THRESHOLD_GAUSSIAN_SIGMA_DATASET, resultFile);
			hdf.concatenateDataSetsFromFiles(edgeThresholdFiles, getEqualisationLocation(),
					CHIP_THRESHOLD_GAUSSIAN_HEIGHT_DATASET, resultFile);
			hdf.concatenateDataSetsFromFiles(edgeThresholdFiles, getEqualisationLocation(),
					CHIP_THRESHOLD_GAUSSIAN_POSITION_DATASET, resultFile);
		}

		HDF5HelperLocations edgeloc = getEdgeThresholdsLocation();
		hdf.writeAttribute(resultFile, Hdf5Helper.TYPE.DATASET, edgeloc, "signal", 1);
		Vector<Hdf5HelperData> data = new Vector<Hdf5HelperData>();
		for (String f : edgeThresholdFiles) {
			data.add(hdf.readAttribute(f, Hdf5Helper.TYPE.DATASET, edgeloc.getLocationForOpen(), THRESHOLDABNVAL_ATTR));
		}
		hdf.concatenateDataSets(data, getEqualisationLocation(), THRESHOLDABNVAL_DATASET, resultFile);

		HDF5HelperLocations thresholdLoc = getEqualisationLocation();
		thresholdLoc.add(THRESHOLDABNVAL_DATASET);

		hdf.writeAttribute(resultFile, Hdf5Helper.TYPE.DATASET, thresholdLoc, "axis", 1);
	}

	/**
	 * Calls getConfigFromThresholdResponseFile and stores result in the result file The file contains the set of values
	 * of thesholdA or thresholdB for a given thresholdTarget in the dataset CONFIG_FROM_THRESHOLD_RESPONSE_DATASET.
	 * This dataset has an attribute THRESHOLD_TARGET_ATTR that gives the targetThreshold
	 * 
	 * @param edgeThresholdABResponseFile
	 * @param thresholdTarget
	 * @param resultFileName
	 * @throws Exception
	 */
	public void createThresholdFromThresholdResponseFile(String edgeThresholdABResponseFile, int thresholdTarget,
			String resultFileName) throws Exception {

		Hdf5HelperData thresholdABData = getThresholdFromThresholdResponseFile(edgeThresholdABResponseFile,
				thresholdTarget);
		Hdf5Helper.getInstance().writeToFileSimple(thresholdABData, resultFileName, getEqualisationLocation(),
				THRESHOLD_FROM_THRESHOLD_RESPONSE_DATASET);
		Hdf5Helper.getInstance().writeAttribute(resultFileName, Hdf5Helper.TYPE.DATASET,
				getEqualisationLocation().add(THRESHOLD_FROM_THRESHOLD_RESPONSE_DATASET), THRESHOLD_TARGET_ATTR,
				thresholdTarget);

	}

	/**
	 * From the thresholdAB response stored in the file thresholdResponseFilename calculate the value of thresholdA or B
	 * for each pixel that gives the desired thresholdTarget For each pixel: thresholdEdge = slope* thresholdA + offset
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
		Hdf5HelperData fitokData = Hdf5Helper.getInstance().readDataSetAll(thresholdResponseFilename,
				getEqualisationLocation().getLocationForOpen(), THRESHOLD_RESPONSE_FITOK_DATASET, true);
		double[] offsets = (double[]) offsetData.data;
		double[] slopes = (double[]) slopeData.data;
		short[] fitOK = (short[]) fitokData.data;
		if (offsets.length != slopes.length)
			throw new IllegalArgumentException("offsets.length != slopes.length");
		short[] thresholdAB = new short[slopes.length];
		Arrays.fill(thresholdAB, THRESHOLD_FROM_THRESHOLD_INVALID);
		for (int i = 0; i < thresholdAB.length; i++) {
			if( fitOK[i] == THRESHOLD_RESPONSE_FITOK_TRUE){
				double thresholdABDbl = (thresholdTarget - offsets[i]) / slopes[i];
				thresholdAB[i] = (short) Math.round(thresholdABDbl);
			}
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
	 * Calls getThresholdFromScanData and stores the result in the result file The edge threshold values are stored in
	 * dataset EDGE_THRESHOLDS_DATASET This dataset has the edgeThreshold limit stored in the attribute
	 * THRESHOLD_LIMIT_ATTR and the value of detector thresholdABNName stored in attribute THRESHOLDABVAL_ATTR
	 */
	public void createThresholdFileFromScanData(String filename, String detectorName, String threshold0Name,
			String thresholdABNName, int edgeThreshold, int sizeOfSlice, String resultfilename) throws Exception {
		String detectorLocation = "entry1/" + detectorName;
		Hdf5HelperData hd = getThresholdFromScanData(edgeThreshold, sizeOfSlice, filename, detectorLocation,
				threshold0Name, "data");
		HDF5HelperLocations equalisationLocation = getEqualisationLocation();
		Hdf5Helper hdf = Hdf5Helper.getInstance();
		hdf.writeToFileSimple(hd, resultfilename, equalisationLocation, THRESHOLD_DATASET);
		hdf.writeAttribute(resultfilename, Hdf5Helper.TYPE.DATASET, getEdgeThresholdsLocation(), THRESHOLD_LIMIT_ATTR,
				edgeThreshold);
		if( thresholdABNName != null && thresholdABNName.length() > 0){
			Hdf5HelperData thresholdAValData = hdf.readDataSetAll(filename, detectorLocation, thresholdABNName, true);
			hdf.writeAttribute(resultfilename, Hdf5Helper.TYPE.DATASET, getEdgeThresholdsLocation(), THRESHOLDABNVAL_ATTR,
					((double[]) thresholdAValData.data)[0]);
		}
	}

	/**
	 * From the scan of detector name against threshold0 calculate the edge threshold for each pixel and save results to
	 * the file. As the data for a complete scan can be too large for the memepry available the data is treated in
	 * slices by calling getThresholdFromSliceofScanData.
	 */
	public Hdf5HelperData getThresholdFromScanData(int threshold, int sizeOfSlice, String scanFilename,
			String groupName, String dataSetNameThreshold0, String dataSetNameDetector) throws Exception {

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
			if (!(slice instanceof ShortDataset))
				throw new Exception("data is not of type ShortDataset");
			ShortDataset ds = ((ShortDataset) slice);
			short[] edgeThresholdsSlice = getThresholdFromSliceofScanData((short[]) ds.getBuffer(), ds.getShape(),
					null, threshold, 0, false, threshold0ValsAsInt);
			System.arraycopy(edgeThresholdsSlice, 0, edgeThresholds, iEdgeThresholdProcessed,
					edgeThresholdsSlice.length);
			start[shape.length - 2] = Math.min(start[shape.length - 2] + sizeOfSlice, shape[shape.length - 2]);
			stop[shape.length - 2] = Math.min(stop[shape.length - 2] + sizeOfSlice, shape[shape.length - 2]);
			iEdgeThresholdProcessed += shape[2] * sizeOfSlice;
		}
		return new Hdf5HelperData(new long[] { shape[1], shape[2] }, edgeThresholds);
	}

	/*
	 * Not to be called by scripts.
	 */
	public short[] getThresholdFromSliceofScanData(short[] data, int[] shape, int[] activePixels, int thresholdVal,
			int dimensionToTraverse, boolean isForward, short[] lookupTable) throws Exception {
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
	 * Calls getChipAveragedThresholdFromThresholdFile and writes result to resultFileName The resultFile has 3
	 * datasets: CHIP_PRESENT_DATASET - dataset of shorts - value ==1 if chip is present THRESHOLD_DATASET - dataset of
	 * chip averaged threshold position CHIP_THRESHOLD_GAUSSIAN_FWHM_DATASET - - dataset of fwhm of chip threshold
	 * population
	 * 
	 * @param thresholdFile
	 * @param rows
	 * @param columns
	 * @param chipPresent
	 * @param resultFileName
	 * @throws Exception
	 */
	public void createChipAveragedThresholdFileFromThresholdFile(String thresholdFile, int rows, int columns,
			boolean chipPresent[], String resultFileName) throws Exception {
		Hdf5Helper hdf = Hdf5Helper.getInstance();
		long dims[] = new long[] { rows, columns };
		int numItems = (int) hdf.lenFromDims(dims);
		if (numItems != chipPresent.length)
			throw new IllegalArgumentException("numItems != chipPresent.length");

		ChipAveragedResult[] aPeaks = getChipAveragedThresholdFromThresholdFile(thresholdFile, rows, columns,
				chipPresent);
		if (numItems != aPeaks.length)
			throw new IllegalArgumentException("numItems != aPeaks.length");

		short[] present = new short[numItems];
		double[] positions = new double[numItems];
		double[] sigmas = new double[numItems];
		double[] height = new double[numItems];
		Arrays.fill(sigmas, FIT_FAILED_WIDTH);
		for (int i = 0; i < numItems; i++) {
			present[i] = (short) (chipPresent[i] ? 1 : 0);
			if (chipPresent[i]) {
				if (aPeaks[i].function != null) { 
					IPeak function = aPeaks[i].function.getPeak(0);
					positions[i] = function.getPosition();
					sigmas[i] = function.getFWHM()/FWHM_OVER_SIGMA;
					height[i] = function.getHeight();
				}
			}
			hdf.writeToFileSimple(new Hdf5HelperData(aPeaks[i].xvals), resultFileName, getEqualisationLocation(),
					"Population"+i+"_xvals");
			hdf.writeToFileSimple(new Hdf5HelperData(aPeaks[i].yvals), resultFileName, getEqualisationLocation(),
					"Population"+i+"_yvals");
		}
		hdf.writeToFileSimple(new Hdf5HelperData(dims, present), resultFileName, getEqualisationLocation(),
				CHIP_PRESENT_DATASET);
		hdf.writeToFileSimple(new Hdf5HelperData(dims, positions), resultFileName, getEqualisationLocation(),
				CHIP_THRESHOLD_GAUSSIAN_POSITION_DATASET);
		hdf.writeToFileSimple(new Hdf5HelperData(dims, sigmas), resultFileName, getEqualisationLocation(),
				CHIP_THRESHOLD_GAUSSIAN_SIGMA_DATASET);
		hdf.writeToFileSimple(new Hdf5HelperData(dims, height), resultFileName, getEqualisationLocation(),
				CHIP_THRESHOLD_GAUSSIAN_HEIGHT_DATASET);
	}

	/**
	 * Gets the threshold values averaged over a population of pixels on a chip read from a threshold file previously
	 * created by calling createThresholdFileFromScanData . Loads the edge threshold calc for each pixel in a set of
	 * chips. Gets population of values for each chip and fits a gaussian to get average and fwhm
	 * 
	 * @param fileName
	 * @return A 2d array of APeaks that match the structure of the chips in the detector
	 * @throws Exception
	 */
	public ChipAveragedResult[] getChipAveragedThresholdFromThresholdFile(String fileName, int rows, int columns,
			boolean chipPresent[]) throws Exception {
		String groupName = getEqualisationLocation().getLocationForOpen();
		String datasetName = THRESHOLD_DATASET;
		Hdf5HelperLazyLoader loader = new Hdf5HelperLazyLoader(fileName, groupName, datasetName, false);
		int[] shape = loader.getLazyDataSet().getShape();

		ChipSet chipset = new ChipSet(rows, columns, chipPresent);
		chipset.checkLoaderShape(shape);

		ChipAveragedResult[] aPeaks = new ChipAveragedResult[(chipset.rows * chipset.columns)];
		Arrays.fill(aPeaks, null);

		for (Chip chip : chipset.getChips()) {
			AbstractDataset dataset = chip.getDataset(loader);
			ShortDataset dataset2 = getDatasetWithValidPixels(dataset);
			if (dataset2 != null) {
				double[][] xyvals = createBinnedPopulation(dataset2);
				ChipAveragedResult chipAveragedResult = new ChipAveragedResult();
				chipAveragedResult.xvals = xyvals[0];
				chipAveragedResult.yvals = xyvals[1];
				chipAveragedResult.function = fitGaussianToBinnedPopulation(chipAveragedResult.xvals, chipAveragedResult.yvals);
				aPeaks[chip.index] = chipAveragedResult;
			}
		}
		return aPeaks;
	}

	boolean thresholdEdgePosIsValid(short value) {
		return (value != EDGE_POSITION_IF_ALL_ABOVE_THRESHOLD && value != EDGE_POSITION_IF_ALL_BELOW_THRESHOLD && value != EDGE_POSITION_IF_PIXEL_MASKED_OUT);
	}

	ShortDataset getDatasetWithValidPixels(AbstractDataset dataset) {
		short[] validData = new short[dataset.getSize()];
		int numValidData = 0;
		IndexIterator iter = dataset.getIterator();

		while (iter.hasNext()) {
			short value = (short) dataset.getElementLongAbs(iter.index);
			if (thresholdEdgePosIsValid(value)) {
				validData[numValidData] = value;
				numValidData++;
			}
		}
		return numValidData > 0 ? new ShortDataset(Arrays.copyOf(validData, numValidData), numValidData) : null;
	}

	/**
	 * @return result of fitting Gaussian over the population within the slice given by start, stop, step
	 */
	public CompositeFunction fitGaussianToBinnedPopulation(double[] xvals, double [] yvals) {

		if (xvals.length > 0 && xvals.length==yvals.length) {
			DoubleDataset xvals_ds = new DoubleDataset(xvals);
			DoubleDataset yvals_ds = new DoubleDataset(yvals);

			double xmax = xvals_ds.max().doubleValue();
			double xmin = xvals_ds.min().doubleValue();
			double ymax = yvals_ds.max().doubleValue();
			double xavg = (xmax + xmin) / 2;
			double xfwhm = (xmax - xmin) / 4;
			
			Gaussian peakFunction = new Gaussian(xavg, xfwhm, (Double)yvals_ds.sum());
			peakFunction.getParameter(0).setLimits(0., xmax*2);
			peakFunction.getParameter(1).setLimits(0., xfwhm*4);
			peakFunction.getParameter(2).setLimits(-ymax*xfwhm*4*2, ymax*xfwhm*4*2);
			

			
			CompositeFunction cmpF = new CompositeFunction();
			cmpF.addFunction(peakFunction);
			
			GeneticAlg geneticAlg = new GeneticAlg(0.01); 
			geneticAlg.optimize(new IDataset[]{xvals_ds}, yvals_ds, cmpF);
			
			return cmpF;
		}
		return null;
	}

	double[][] createBinnedPopulation(AbstractDataset ids) {
		int minVal = ids.min().intValue();
		// range of values is from minVal to max. Num of bins is range +1
		// first bin contains population with val = minVal
		int[] numInBins = new int[ids.max().intValue() - minVal + 1];
		Arrays.fill(numInBins, 0);
		IndexIterator iter = ids.getIterator();

		while (iter.hasNext()) {
			int value = (int) ids.getElementLongAbs(iter.index);
			numInBins[value - minVal] += 1;
		}
		
		double[] xvals = new double[numInBins.length];
		double[] yvals = new double[numInBins.length];
		int numFound = 0;
		for (int i = 0; i < numInBins.length; i++) {
			if (numInBins[i] > 0) {
				xvals[numFound] = i + minVal;
				yvals[numFound] = numInBins[i];
				numFound += 1;
			}
		}
		xvals = Arrays.copyOf(xvals, numFound);
		yvals = Arrays.copyOf(yvals, numFound);
		double[][] xyvals = new double[][]{xvals, yvals};
		return xyvals;
	}

	public void createThresholdNOpt(String thresholdNChipResponseFile, String thresholdN50ChipThresholdFile,
			double equalisationTarget, String resultFile) throws Exception {
		Hdf5Helper hdf = Hdf5Helper.getInstance();
		// read the offset and slope - a1, a2
		Hdf5HelperData offsetData = hdf.readDataSetAll(thresholdNChipResponseFile, getEqualisationLocation()
				.getLocationForOpen(), THRESHOLD_RESPONSE_OFFSETS_DATASET, true);
		Hdf5HelperData slopeData = hdf.readDataSetAll(thresholdNChipResponseFile, getEqualisationLocation()
				.getLocationForOpen(), THRESHOLD_RESPONSE_SLOPES_DATASET, true);

		// read the fwhm for thresholdN=50
		Hdf5HelperData sigmaData = hdf.readDataSetAll(thresholdN50ChipThresholdFile, getEqualisationLocation()
				.getLocationForOpen(), CHIP_THRESHOLD_GAUSSIAN_SIGMA_DATASET, true);

		long lenFromOffsetData = hdf.lenFromDims(offsetData.dims);
		long lenFromSlopeData = hdf.lenFromDims(slopeData.dims);
		long lenFromSigmaData = hdf.lenFromDims(sigmaData.dims);

		if (lenFromOffsetData != lenFromSlopeData || lenFromSlopeData != lenFromSigmaData)
			throw new IllegalArgumentException(
					"lenFromOffsetData !=  lenFromSlopeData || lenFromSlopeData != lenFromSigmaData");
		short[] thresholdNOpt = new short[(int) lenFromOffsetData];
		double[] threshold0opt = new double[(int) lenFromOffsetData];
		for (int i = 0; i < lenFromOffsetData; i++) {
			threshold0opt[i] = 3.2 * Array.getDouble(sigmaData.data, i) + equalisationTarget;
			double val = (threshold0opt[i] - Array.getDouble(offsetData.data, i)) / (Array.getDouble(
					slopeData.data, i));
			thresholdNOpt[i] = (short) Math.round( val);
		}
		Hdf5HelperData data = new Hdf5HelperData(offsetData.dims, threshold0opt);
		hdf.writeToFileSimple(data, resultFile, getEqualisationLocation(), THRESHOLD_0OPT);
		hdf.writeAttribute(resultFile, Hdf5Helper.TYPE.DATASET, getEqualisationLocation().add(THRESHOLD_0OPT),
				THRESHOLD_N_EQ_TARGET_ATTR, equalisationTarget);
		data = new Hdf5HelperData(offsetData.dims, thresholdNOpt);
		hdf.writeToFileSimple(data, resultFile, getEqualisationLocation(), THRESHOLD_NOPT);
		hdf.writeAttribute(resultFile, Hdf5Helper.TYPE.DATASET, getEqualisationLocation().add(THRESHOLD_NOPT),
				THRESHOLD_N_EQ_TARGET_ATTR, equalisationTarget);
	}

	/**
	 * Set thresholdN in the hardware to the values in the edgeThresholdNResponseFile If setThresholdAFromMask is true
	 * set then set thresholdA on each pixel to either 0 or 16 dependent on value in mask
	 * 
	 * @param edgeThresholdNResponseFile
	 * @param readoutFems
	 * @throws Exception
	 */
	public void setThresholdNFromFile(String edgeThresholdNResponseFile, List<ExcaliburReadoutNodeFem> readoutFems,
			int rows, int columns, boolean[] chipPresent, boolean setThresholdAFromMask) throws Exception {

		Hdf5Helper hdf = Hdf5Helper.getInstance();
		Hdf5HelperData hdata = hdf.readDataSetAll(edgeThresholdNResponseFile, getEqualisationLocation()
				.getLocationForOpen(), THRESHOLD_NOPT, true);
		if (hdata.dims.length != 2)
			throw new IllegalArgumentException("data.dims.length!=2");
		if (hdata.dims[0] != readoutFems.size())
			throw new IllegalArgumentException("hdata.dims[0]!= readoutFems.size()(" + hdata.dims[0] + " != "
					+ readoutFems.size());
		if (hdata.dims[1] != ExcaliburReadoutNodeFem.CHIPS_PER_FEM)
			throw new IllegalArgumentException("data.dims[1]!=CHIPS_PER_FEM");

		if (columns != ExcaliburReadoutNodeFem.CHIPS_PER_FEM)
			throw new IllegalArgumentException("columns != ExcaliburReadoutNodeFem.CHIPS_PER_FEM");
		short[] thresholdNOpt = (short[]) hdata.data;

		ChipSet chipset = new ChipSet(rows, columns, chipPresent);
		Hdf5HelperLazyLoader loader = null;
		if (setThresholdAFromMask) {
			loader = new Hdf5HelperLazyLoader(edgeThresholdNResponseFile, getEqualisationLocation()
					.getLocationForOpen(), THRESHOLDN_MASK, false);
			int[] shape = loader.getLazyDataSet().getShape();
			chipset.checkLoaderShape(shape);
		}
		for (Chip chip : chipset.getChips()) {
			ExcaliburReadoutNodeFem fem = readoutFems.get(chip.row);
			MpxiiiChipReg chipReg = fem.getIndexedMpxiiiChipReg(chip.column);
			chipReg.getAnper().setThresholdn(thresholdNOpt[chip.index]);
			chipReg.loadDacConfig();

			if (setThresholdAFromMask) {
				ShortDataset dataset = (ShortDataset) chip.getDataset(loader);
				short[] mask = (short[]) dataset.getBuffer();
				short[] thresholdA = new short[mask.length];
				for (int i = 0; i < mask.length; i++) {
					thresholdA[i] = (short) (mask[i] == 1 ? 16 : 0);
				}
				chipReg.getPixel().setThresholdA(thresholdA);
				chipReg.loadPixelConfig();

			}

		}
	}

	/**
	 * Creates a useThresholdN mask. For each pixel decide whether the threshold is closest to the optimim( read from
	 * optThresholdFile) with or without use of thresholdN. The result is a 2d array of shorts (1=use, 0= do not use)
	 * written into the result file The optThresholdFile contains 1 value per chip
	 * 
	 * @throws Exception
	 */
	public void createThresholdNOptMask(String optThresholdFile, String notUsingThresholdNFile,
			String usingThresholdNFile, String resultFile) throws Exception {
		Hdf5Helper hdf = Hdf5Helper.getInstance();
		Hdf5HelperData hthresholdOpt = hdf.readDataSetAll(optThresholdFile, getEqualisationLocation()
				.getLocationForOpen(), THRESHOLD_0OPT, true);
		Hdf5HelperData hthresholdNotUsingThresholdN = hdf.readDataSetAll(notUsingThresholdNFile,
				getEqualisationLocation().getLocationForOpen(), THRESHOLD_DATASET, true);
		Hdf5HelperData hthresholdUsingThresholdN = hdf.readDataSetAll(usingThresholdNFile, getEqualisationLocation()
				.getLocationForOpen(), THRESHOLD_DATASET, true);
		if (hthresholdUsingThresholdN.dims.length != hthresholdNotUsingThresholdN.dims.length)
			throw new IllegalArgumentException(
					"hthresholdUsingThresholdN.dims.length!=hthresholdNotUsingThresholdN.dims.length");

		if (hthresholdOpt.dims.length != 2)
			throw new IllegalArgumentException("hthresholdOpt.dims.length!=2");
		long totalHeight = hthresholdNotUsingThresholdN.dims[0];
		long totalWidth = hthresholdNotUsingThresholdN.dims[1];
		short[] notUsingThresholdN = (short[]) hthresholdNotUsingThresholdN.data;
		short[] usingThresholdN = (short[]) hthresholdUsingThresholdN.data;
		short[] mask = new short[(int) (totalHeight * totalWidth)];

		int numChipsRows = (int) hthresholdOpt.dims[0];
		int numChipsAcross = (int) hthresholdOpt.dims[1];

		ChipSet chipset = new ChipSet(numChipsRows, numChipsAcross);

		for (Chip chip : chipset.getChips()) {
			double thresholdOpt = Array.getDouble(hthresholdOpt.data, chip.index);
			Iterator<Long> iterator = chip.getPixelIndexIterator();
			while (iterator.hasNext()) {
				long index = iterator.next();
				short using = usingThresholdN[(int) index];
				short notusing = notUsingThresholdN[(int) index];
				short maskVal = 0;
				if (thresholdEdgePosIsValid(using) && thresholdEdgePosIsValid(notusing))
					maskVal = (short) (Math.abs(thresholdOpt - using) < Math.abs(thresholdOpt - notusing) ? 1 : 0);
				else if (thresholdEdgePosIsValid(using))
					maskVal = 1;
				else if (thresholdEdgePosIsValid(notusing))
					maskVal = 0;
				mask[(int) index] = maskVal;
			}

		}

		Hdf5HelperData hmask = new Hdf5HelperData(hthresholdNotUsingThresholdN.dims, mask);
		hdf.writeToFileSimple(hmask, resultFile, getEqualisationLocation(), THRESHOLDN_MASK);

	}

	public void createThresholdNOptMaskFromUnequalisedScan(String unequalisedThresholdNFile, 
			int numChipsRows, int numChipsAcross, String resultFile) throws Exception {
		Hdf5Helper hdf = Hdf5Helper.getInstance();
		Hdf5HelperData hthresholdNotUsingThresholdN = hdf.readDataSetAll(unequalisedThresholdNFile,
				getEqualisationLocation().getLocationForOpen(), THRESHOLD_DATASET, true);

		if (hthresholdNotUsingThresholdN.dims.length != 2)
			throw new IllegalArgumentException("hthresholdNotUsingThresholdN.dims.length!=2");

		long totalHeight = hthresholdNotUsingThresholdN.dims[0];
		long totalWidth = hthresholdNotUsingThresholdN.dims[1];
		short[] notUsingThresholdN = (short[]) hthresholdNotUsingThresholdN.data;
		short[] mask = new short[(int) (totalHeight * totalWidth)];
		Arrays.fill(mask, (short)0); //fill with no mask by default
		ChipSet chipset = new ChipSet(numChipsRows, numChipsAcross);

		for (Chip chip : chipset.getChips()) {
			Iterator<Long> iterator = chip.getPixelIndexIterator();
			while (iterator.hasNext()) {
				long index = iterator.next();
				short using = notUsingThresholdN[(int) index];
				short maskVal = 0;
				if (!thresholdEdgePosIsValid(using))
					mask[(int) index] = 1;
			}

		}

		Hdf5HelperData hmask = new Hdf5HelperData(hthresholdNotUsingThresholdN.dims, mask);
		hdf.writeToFileSimple(hmask, resultFile, getEqualisationLocation(), THRESHOLDN_MASK);

	}

	
	/**
	 * Gets the max threshold0 limit required so that all but numPixelsOutSide pixels have a value within the limit. The
	 * threshold0 limit is chip specific. Write results into selectThresholdTargetThresholdFile as long[]. return max
	 * 
	 * @throws Exception
	 */
	public long createThresholdTarget(String thresholdFile, int rows, int columns, boolean chipPresent[],
			long numPixelsOutSide, String resultFile) throws Exception {
		// need to get binned populations and then get value to include all but 100 values
		String groupName = getEqualisationLocation().getLocationForOpen();
		String datasetName = THRESHOLD_DATASET;
		Hdf5HelperLazyLoader loader = new Hdf5HelperLazyLoader(thresholdFile, groupName, datasetName, false);
		int[] shape = loader.getLazyDataSet().getShape();

		ChipSet chipset = new ChipSet(rows, columns, chipPresent);
		chipset.checkLoaderShape(shape);

		short[] thresholdLimit = new short[chipset.numChips];

		for (Chip chip : chipset.getChips()) {
			AbstractDataset dataset = chip.getDataset(loader);
			ShortDataset dataset2 = getDatasetWithValidPixels(dataset);
			if (dataset2 != null) {
				double[][] xyvals = createBinnedPopulation(dataset2);
				double [] xvals=xyvals[0];

				double [] yvals=xyvals[1];
				thresholdLimit[chip.index] = 0;
				// go backwards until we have removed numPixelsOutSide
				int numOutside = 0;
				int i = yvals.length - 1;
				while (numOutside < numPixelsOutSide && i >= 0) {
					numOutside += yvals[i];
					i--;
				}
				short thresholdlimit = (short) xvals[i];
				thresholdLimit[chip.index] = thresholdlimit; // if numPixelsOutSide==0 then use all i=
																// population.length-1
			}

		}

		// write results into file.
		ShortDataset dataset = new ShortDataset(thresholdLimit, thresholdLimit.length);
		long maxThresholdLimit = dataset.max().longValue();
		Hdf5Helper.getInstance().writeToFileSimple(new Hdf5HelperData(chipset.getDims(), thresholdLimit), resultFile,
				getEqualisationLocation(), THRESHOLD_LIMIT_DATASET);
		Hdf5Helper.getInstance().writeAttribute(resultFile, Hdf5Helper.TYPE.DATASET,
				getEqualisationLocation().add(THRESHOLD_LIMIT_DATASET), THRESHOLD_LIMIT_ATTR, maxThresholdLimit);
		return maxThresholdLimit;
	}

	public void createDACPixelOpt(String dacChipResponseFile, double tmax, double param1, String resultFile)
			throws Exception {
		Hdf5Helper hdf = Hdf5Helper.getInstance();
		// read the offset and slope - a1, a2
		Hdf5HelperData offsetData = hdf.readDataSetAll(dacChipResponseFile, getEqualisationLocation()
				.getLocationForOpen(), THRESHOLD_RESPONSE_OFFSETS_DATASET, true);
		Hdf5HelperData slopeData = hdf.readDataSetAll(dacChipResponseFile, getEqualisationLocation()
				.getLocationForOpen(), THRESHOLD_RESPONSE_SLOPES_DATASET, true);

		long lenFromOffsetData = hdf.lenFromDims(offsetData.dims);
		long lenFromSlopeData = hdf.lenFromDims(slopeData.dims);

		if (lenFromOffsetData != lenFromSlopeData)
			throw new IllegalArgumentException("lenFromOffsetData !=  lenFromSlopeData ");
		short[] dacPixelOpt = new short[(int) lenFromOffsetData];
		double[] threshold0opt = new double[(int) lenFromOffsetData];
		for (int i = 0; i < lenFromOffsetData; i++) {
			threshold0opt[i] = tmax;
			dacPixelOpt[i] = (short) ((threshold0opt[i] - Array.getDouble(offsetData.data, i)) / (Array.getDouble(
					slopeData.data, i)));
		}
		Hdf5HelperData data = new Hdf5HelperData(offsetData.dims, threshold0opt);
		hdf.writeToFileSimple(data, resultFile, getEqualisationLocation(), THRESHOLD_0OPT);
		hdf.writeAttribute(resultFile, Hdf5Helper.TYPE.DATASET, getEqualisationLocation().add(THRESHOLD_0OPT),
				THRESHOLD_N_EQ_TARGET_ATTR, param1);
		data = new Hdf5HelperData(offsetData.dims, dacPixelOpt);
		hdf.writeToFileSimple(data, resultFile, getEqualisationLocation(), DACPIXEL_OPT);
		hdf.writeAttribute(resultFile, Hdf5Helper.TYPE.DATASET, getEqualisationLocation().add(DACPIXEL_OPT),
				THRESHOLD_N_EQ_TARGET_ATTR, param1);
	}

	public void setDACPixelFromFile(String edgeThresholdNResponseFile, List<ExcaliburReadoutNodeFem> readoutFems,
			int rows, int columns, boolean[] chipPresent) throws Exception {

		Hdf5Helper hdf = Hdf5Helper.getInstance();
		Hdf5HelperData hdata = hdf.readDataSetAll(edgeThresholdNResponseFile, getEqualisationLocation()
				.getLocationForOpen(), DACPIXEL_OPT, true);
		if (hdata.dims.length != 2)
			throw new IllegalArgumentException("data.dims.length!=2");
		if (hdata.dims[0] != readoutFems.size())
			throw new IllegalArgumentException("data.dims[0]!= readoutFems.size()");
		if (hdata.dims[1] != ExcaliburReadoutNodeFem.CHIPS_PER_FEM)
			throw new IllegalArgumentException("data.dims[1]!=CHIPS_PER_FEM");

		if (columns != ExcaliburReadoutNodeFem.CHIPS_PER_FEM)
			throw new IllegalArgumentException("columns != ExcaliburReadoutNodeFem.CHIPS_PER_FEM");
		short[] dacPixelOpt = (short[]) hdata.data;

		ChipSet chipset = new ChipSet(rows, columns, chipPresent);
		for (Chip chip : chipset.getChips()) {
			ExcaliburReadoutNodeFem fem = readoutFems.get(chip.row);
			MpxiiiChipReg chipReg = fem.getIndexedMpxiiiChipReg(chip.column);
			chipReg.getAnper().setDacPixel(dacPixelOpt[chip.index]);
			chipReg.loadDacConfig();
		}
	}

	/**
	 * Creates the final thresholdAjd data and a mask to indicate if a no valid value for a pixel can be found
	 * mask =1 - value is invalid
	 */
	public void createThresholdAdjAndMask(String configThresholdNOn, String configThresholdNOff, String resultFile)
			throws Exception {

		Hdf5Helper hdf = Hdf5Helper.getInstance();
		Hdf5HelperData hdataOn = getConfigFromFile(configThresholdNOn);
		Hdf5HelperData hdataOff = getConfigFromFile(configThresholdNOff);

		long lenFromOn = hdf.lenFromDims(hdataOn.dims);
		long lenFromOff = hdf.lenFromDims(hdataOff.dims);

		if (lenFromOn != lenFromOff)
			throw new IllegalArgumentException("lenFromOn != lenFromOff ");

		short[] onA = (short[]) hdataOn.data;
		short[] offA = (short[]) hdataOff.data;
		short[] configA = new short[onA.length];
		short[] maskA = new short[onA.length];
		for (int i = 0; i < (int) lenFromOn; i++) {
			short mask = 0;
			short config = 0;
			short on = onA[i];
			short off = offA[i];

			mask = 1;
			if (on >= 16 && on < 32) { // on is valid
				config = on;// thresholdN on
				mask = 0;
			}
			if (off >= 0 && off < 16) { // off is valid
				config = off; // thresholdN off
				mask = 0;
			}
			maskA[i] = mask;
			configA[i] = config;

		}
		Hdf5Helper.getInstance().writeToFileSimple(new Hdf5HelperData(hdataOn.dims, maskA), resultFile,
				getEqualisationLocation(), THRESHOLDADJ_MASK_DATASET);
		Hdf5Helper.getInstance().writeToFileSimple(new Hdf5HelperData(hdataOn.dims, configA), resultFile,
				getEqualisationLocation(), THRESHOLDADJ_DATASET);
	}
	/**
	 * Set thresholdAdj in the hardware to the values in the thresholdAdjFile
	 * 
	 * @param thresholdAdjFile
	 * @param readoutFems
	 * @throws Exception
	 */
	
	/**
	 * Set thresholdAdj in the hardware to the values in the thresholdAdjFile
	 * 
	 * @param thresholdAdjFile  file containing thresholdAjd values in dataset THRESHOLDADJ_DATASET
	 * @param readoutFems list of readout fems
	 * @param rows  - number of rows of chips
	 * @param columns - number of columns of chips
	 * @param chipPresent - array of booleans to indicate which chips are to have thresholdAdj set
	 * if null then it is assumed that all chips are to be set
	 * @throws Exception
	 */
	public void setThresholAdjFromFile(String thresholdAdjFile, List<ExcaliburReadoutNodeFem> readoutFems,
			int rows, int columns, boolean[] chipPresent) throws Exception {
	
		ChipSet chipset = new ChipSet(rows, columns, chipPresent);
		Hdf5HelperLazyLoader loader = new Hdf5HelperLazyLoader(thresholdAdjFile, getEqualisationLocation()
					.getLocationForOpen(), THRESHOLDADJ_DATASET, false);
		
		Hdf5HelperLazyLoader maskloader = new Hdf5HelperLazyLoader(thresholdAdjFile, getEqualisationLocation()
				.getLocationForOpen(), THRESHOLDADJ_MASK_DATASET, false);
		
		int[] shape = loader.getLazyDataSet().getShape();
		chipset.checkLoaderShape(shape);
		shape = maskloader.getLazyDataSet().getShape();
		chipset.checkLoaderShape(shape);
		for (Chip chip : chipset.getChips()) {
			ExcaliburReadoutNodeFem fem = readoutFems.get(chip.row);
			MpxiiiChipReg chipReg = fem.getIndexedMpxiiiChipReg(chip.column);
			
			ShortDataset dataset = (ShortDataset) chip.getDataset(loader);
			short[] thresholdA = (short[]) dataset.getBuffer();
			chipReg.getPixel().setThresholdA(thresholdA);

			dataset = (ShortDataset) chip.getDataset(maskloader);
			short[] mask = (short[]) dataset.getBuffer();
			chipReg.getPixel().setMask(mask);
			
			chipReg.loadPixelConfig();
		}
		
	}
	
}
class ChipAveragedResult{
	public CompositeFunction function;
	public double [] xvals;
	public double [] yvals;
}