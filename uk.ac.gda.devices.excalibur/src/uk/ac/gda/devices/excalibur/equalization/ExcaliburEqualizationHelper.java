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

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.DoubleDataset;
import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import uk.ac.diamond.scisoft.analysis.dataset.ILazyDataset;
import uk.ac.diamond.scisoft.analysis.dataset.IndexIterator;
import uk.ac.diamond.scisoft.analysis.dataset.IntegerDataset;
import uk.ac.diamond.scisoft.analysis.fitting.Generic1DFitter;
import uk.ac.diamond.scisoft.analysis.fitting.functions.APeak;
import uk.ac.diamond.scisoft.analysis.fitting.functions.Gaussian;
import uk.ac.diamond.scisoft.analysis.optimize.GeneticAlg;
import uk.ac.gda.analysis.hdf5.HDF5HelperLocations;
import uk.ac.gda.analysis.hdf5.HDF5NexusLocation;
import uk.ac.gda.analysis.hdf5.Hdf5Helper;
import uk.ac.gda.analysis.hdf5.Hdf5Helper.TYPE;
import uk.ac.gda.analysis.hdf5.Hdf5HelperData;
import uk.ac.gda.analysis.hdf5.Hdf5HelperLazyLoader;
import uk.ac.gda.devices.excalibur.ExcaliburReadoutNodeFem;

/**
 *
 */
public class ExcaliburEqualizationHelper {

	public static final String THRESHOLD_TARGET_ATTR = "thresholdTarget";
	private static final String CONFIG_FROM_THRESHOLD_RESPONSE_DATASET = "configFromThresholdResponse";
	public static final String EDGE_THRESHOLD_RESPONSE_OFFSETS_DATASET = "edgeThresholdResponseOffsets";
	public static final String EDGE_THRESHOLD_RESPONSE_SLOPES_DATASET = "edgeThresholdResponseSlopes";
	public static final String THRESHOLDAVAL_DATASET = "thresholdAVal";
	public static final String THRESHOLDAVAL_ATTR = "thresholdAVal";
	public static final String THRESHOLD_LIMIT_ATTR = "thresholdLimit";
	public static final String THRESHOLD_ATTRIBUTE = "threshold";
	public static final String THRESHOLD_STDDEV_ATTRIBUTE = "threshold_mean";
	public static final String THRESHOLD_MEAN_ATTRIBUTE = "threshold_stddev";
	public static final String THRESHOLD_DATASET = "threshold";
	public static final String EDGE_THRESHOLDS_DATASET = "edgeThresholds";
	private static final ExcaliburEqualizationHelper INSTANCE = new ExcaliburEqualizationHelper();

	public static ExcaliburEqualizationHelper getInstance() {
		return INSTANCE;
	}

	/**
	 * @param data
	 *            3d data
	 * @param shape
	 *            The dimension of the data
	 * @param activePixels
	 *            2d mask val = 1 if pixel is to be checked.
	 * @param thresholdVal
	 * @param dimensionToTraverse
	 * @param isForward
	 * @return array of integers - 2D data
	 * @throws Exception
	 */
	public int[] getEqualizedData(int[] data, int[] shape, int[] activePixels, int thresholdVal,
			int dimensionToTraverse, boolean isForward, int[] lookupTable, int valIfAllBelow, int valIfAllAbove)
			throws Exception {
		// assume dimensionToTraverse ==2
		if (shape.length != 3)
			throw new Exception("Invalid shape");
		int[] res = null;
		if (dimensionToTraverse == 2) {
			if (shape[2] != lookupTable.length)
				throw new Exception("shape[2] != lookupTable.length");
			int sizeOfResult = shape[0] * shape[1];
			res = new int[sizeOfResult];
			Arrays.fill(res, 0);
			for (int pixelInMask = 0; pixelInMask < sizeOfResult; pixelInMask++) {
				if (activePixels == null || activePixels[pixelInMask] == 1) {
					res[pixelInMask] = -1;
					int numberPointsPerPixel = shape[dimensionToTraverse];
					for (int iy = 0; iy < numberPointsPerPixel; iy++) {
						int pixel = isForward ? iy : numberPointsPerPixel - iy - 1;
						int val = data[pixel * sizeOfResult + pixelInMask];
						if (val >= thresholdVal) {
							if (iy == 0) {
								// all above
								res[pixelInMask] = valIfAllAbove;
							} else {
								res[pixelInMask] = lookupTable[pixel];
							}
							break;
						}
						if (iy == numberPointsPerPixel - 1)
							res[pixelInMask] = valIfAllBelow;

					}
				}
			}
		} else if (dimensionToTraverse == 0) {
			if (shape[0] != lookupTable.length)
				throw new Exception("shape[0] != lookupTable.length");
			int sizeOfResult = shape[1] * shape[2];
			res = new int[sizeOfResult];
			Arrays.fill(res, 0);
			int numberPointsPerPixel = shape[dimensionToTraverse];
			/*
			 * for (int i = 0; i < shape[1]; i++) { for (int j = 0; j < shape[2]; j++) { int pixelInMask = i * shape[2]
			 * + j;
			 */for (int pixelInMask = 0; pixelInMask < sizeOfResult; pixelInMask++) {

				if (activePixels == null || activePixels[pixelInMask] == 1) {
					res[pixelInMask] = isForward ? numberPointsPerPixel - 1 : 0;
					for (int iy = 0; iy < numberPointsPerPixel; iy++) {
						int pixel = isForward ? iy : numberPointsPerPixel - iy - 1;
						int index = pixelInMask + pixel * sizeOfResult;

						int val = data[index];
						if (val >= thresholdVal) {
							if (iy == 0) {
								// all above
								res[pixelInMask] = valIfAllAbove;
							} else {
								res[pixelInMask] = lookupTable[pixel];
							}
							break;
						}
						if (iy == numberPointsPerPixel - 1)
							res[pixelInMask] = valIfAllBelow;

					}
				}

			}
			// }
		}
		return res;
	}

	/**
	 * @param indexData
	 * @param lookupTable
	 * @return an array of values given an array of indices to a value lookup table
	 */
	public double[] convertUsingAxis(int[] indexData, double[] lookupTable) {
		double[] data = new double[indexData.length];
		for (int i = 0; i < data.length; i++) {
			data[i] = lookupTable[indexData[i]];
		}
		return data;
	}

	public Hdf5HelperData getEqualisationData(int threshold, int sizeOfSlice, String scanFilename, String groupName,
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

		int[] edgeThresholds = new int[shape[1] * shape[2]];
		int iEdgeThresholdProcessed = 0;
		Hdf5HelperData threshold0Vals = Hdf5Helper.getInstance().readDataSetAll(scanFilename, groupName,
				dataSetNameThreshold0, true);
		double[] tmp = (double[]) threshold0Vals.data;
		int[] threshold0ValsAsInt = new int[tmp.length];
		for (int i = 0; i < tmp.length; i++) {
			threshold0ValsAsInt[i] = (int) tmp[i];
		}
		while (start[shape.length - 2] < shape[shape.length - 2]) {
			IDataset slice = dataset.getSlice(null, start, stop, null);
			if (!(slice instanceof IntegerDataset))
				throw new Exception("data is not of type IntegerDataset");
			IntegerDataset ds = ((IntegerDataset) slice);
			int[] edgeThresholdsSlice = getEqualizedData((int[]) ds.getBuffer(),
					ds.getShape(), null, threshold, 0, false, threshold0ValsAsInt, 0, 0);
			System.arraycopy(edgeThresholdsSlice, 0, edgeThresholds, iEdgeThresholdProcessed,
					edgeThresholdsSlice.length);
			start[shape.length - 2] += sizeOfSlice;
			stop[shape.length - 2] += sizeOfSlice;
			iEdgeThresholdProcessed += shape[1] * sizeOfSlice;
		}
		return new Hdf5HelperData(new long[] { shape[1], shape[2] }, edgeThresholds);
	}

	/**
	 * function to read the set of threshold values recorded in a set of files and return a set of straight line fits
	 * 
	 * @param edgeThresholdFilenames
	 * @throws Exception
	 */
	public Results getEdgeThresholdABResponse(List<String> edgeThresholdFilenames) throws Exception {

		double[] thresholdABArray = new double[edgeThresholdFilenames.size()];
		Vector<int[]> data = new Vector<int[]>();
		long[] dims = null; 
		for (int i = 0; i < edgeThresholdFilenames.size(); i++) {
			// get data for filename
			Hdf5HelperData readAttribute = Hdf5Helper.getInstance().readAttribute(edgeThresholdFilenames.get(i),
					TYPE.DATASET, getEdgeThresholdsLocation().getLocationForOpen(), THRESHOLDAVAL_ATTR);
			thresholdABArray[i] = ((double[]) readAttribute.data)[0];
			Hdf5HelperData edgeThresholds = Hdf5Helper.getInstance().readDataSetAll(edgeThresholdFilenames.get(i),
					getEqualisationLocation().getLocationForOpen(), EDGE_THRESHOLDS_DATASET, true);

			int[] data2 = (int[]) edgeThresholds.data;
			data.add(data2);
			if (data2.length != data.get(0).length)
				throw new Exception("Data lengths in different files are not equal");
			dims = edgeThresholds.dims;
		}
		return StraightLineFit.fitInt(data, dims, thresholdABArray);
	}

	public void calcConfigFromThresholdResponse(String edgeThresholdABResponseFile, int thresholdTarget,
			String resultFileName) throws Exception {

		Hdf5HelperData thresholdABData = getConfigFromThresholdResponse(edgeThresholdABResponseFile, thresholdTarget);
		Hdf5Helper.getInstance().writeToFileSimple(thresholdABData, resultFileName, getEqualisationLocation(),
				CONFIG_FROM_THRESHOLD_RESPONSE_DATASET); 
		Hdf5Helper.getInstance().writeAttribute(resultFileName, Hdf5Helper.TYPE.DATASET,
				getEqualisationLocation().add(CONFIG_FROM_THRESHOLD_RESPONSE_DATASET), THRESHOLD_TARGET_ATTR,
				new Hdf5HelperData(thresholdTarget));

	}

	public Hdf5HelperData getConfigFromFile(String fileName) throws Exception{
		return Hdf5Helper.getInstance().readDataSetAll(fileName, getEqualisationLocation().getLocationForOpen(), CONFIG_FROM_THRESHOLD_RESPONSE_DATASET, true);
	}
	public void calEdgeThresholdABResponse(List<String> edgeThresholdFilenames, String resultFile) throws Exception {
		Results responses = getEdgeThresholdABResponse(edgeThresholdFilenames);
		Hdf5HelperData hdSlopes = new Hdf5HelperData(responses.getDims(), responses.getSlopes());
		Hdf5Helper.getInstance().writeToFileSimple(hdSlopes, resultFile, getEqualisationLocation(),
				EDGE_THRESHOLD_RESPONSE_SLOPES_DATASET);
		Hdf5HelperData hdOffsets = new Hdf5HelperData(responses.getDims(), responses.getOffsets());
		Hdf5Helper.getInstance().writeToFileSimple(hdOffsets, resultFile, getEqualisationLocation(),
				EDGE_THRESHOLD_RESPONSE_OFFSETS_DATASET);
	}

	/**
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
	public Hdf5HelperData getConfigFromThresholdResponse(String thresholdResponseFilename, double thresholdTarget)
			throws Exception {
		Hdf5HelperData offsetData = Hdf5Helper.getInstance().readDataSetAll(thresholdResponseFilename,
				getEqualisationLocation().getLocationForOpen(), EDGE_THRESHOLD_RESPONSE_OFFSETS_DATASET, true);
		Hdf5HelperData slopeData = Hdf5Helper.getInstance().readDataSetAll(thresholdResponseFilename,
				getEqualisationLocation().getLocationForOpen(), EDGE_THRESHOLD_RESPONSE_SLOPES_DATASET, true);
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
		return getEqualisationLocation().add(EDGE_THRESHOLDS_DATASET);
	}

	public void calcEqualisationData(String filename, String detectorName, String threshold0Name,
			String thresholdABName, int edgeThreshold, int sizeOfSlice, String resultfilename) throws Exception {
		String detectorLocation = "entry1/" + detectorName;
		Hdf5HelperData hd = getEqualisationData(edgeThreshold, sizeOfSlice, filename, detectorLocation, threshold0Name,
				"data");
		HDF5HelperLocations equalisationLocation = getEqualisationLocation();
		Hdf5Helper hdf = Hdf5Helper.getInstance();
		hdf.writeToFileSimple(hd, resultfilename, equalisationLocation, EDGE_THRESHOLDS_DATASET);
		hdf.writeAttribute(resultfilename, Hdf5Helper.TYPE.DATASET, getEdgeThresholdsLocation(),
				THRESHOLD_LIMIT_ATTR, new Hdf5HelperData(edgeThreshold));
		Hdf5HelperData thresholdAValData = hdf.readDataSetAll(filename, detectorLocation,
				thresholdABName, true);
		thresholdAValData.dims = new long[] { 1 };
		hdf.writeAttribute(resultfilename, Hdf5Helper.TYPE.DATASET, getEdgeThresholdsLocation(),
				THRESHOLDAVAL_ATTR, thresholdAValData);
/*		AbstractDataset ds = hdf.createDataSet(hd, false);
		Number stdDeviation = ds.stdDeviation();
		Object mean = ds.mean();
		hdf.writeAttribute(resultfilename, Hdf5Helper.TYPE.DATASET, getEdgeThresholdsLocation(),
				THRESHOLD_STDDEV_ATTRIBUTE, new Hdf5HelperData(stdDeviation.doubleValue()));
		hdf.writeAttribute(resultfilename, Hdf5Helper.TYPE.DATASET, getEdgeThresholdsLocation(),
				THRESHOLD_MEAN_ATTRIBUTE, new Hdf5HelperData((Double)mean));
		

*/	}

	public int[] createBinnedPopulation(IntegerDataset ids) throws ScanFileHolderException{
		ids.max();
		int [] numInBins  = new int[ids.max().intValue()-1];
		Arrays.fill(numInBins, 0);
		IndexIterator iter = ids.getIterator();

		while (iter.hasNext()){
			int value = ids.getAbs(iter.index);
			numInBins[value] += 1;
		}
		return numInBins;
	}

	
	public APeak fitGaussian(String filename, List<ExcaliburReadoutNodeFem> fems) throws Exception{
		Hdf5HelperLazyLoader loader = new Hdf5HelperLazyLoader(filename, getEqualisationLocation().getLocationForOpen(), EDGE_THRESHOLDS_DATASET, false );
		for(ExcaliburReadoutNodeFem node : fems ){
			node.getMpxiiiChipReg1().getPixel();
		}
		return null;
//		AbstractDataset dataset = loader.getDataset(null, shape, start, stop, step);
	}
	public APeak fitGaussian(String filename, int[] shape, int[] start, int[] stop, int[] step ) throws ScanFileHolderException{
		Hdf5HelperLazyLoader loader = new Hdf5HelperLazyLoader(filename, getEqualisationLocation().getLocationForOpen(), EDGE_THRESHOLDS_DATASET, false );
		AbstractDataset dataset = loader.getDataset(null, shape, start, stop, step);
		
		IntegerDataset ids = new IntegerDataset(dataset);
		int[] population = createBinnedPopulation( ids);
		double [] xvals = new double[population.length]; 
		double [] yvals = new double[population.length]; 
		int numFound=0;
		for( int i=0; i< population.length; i++){
			if( population[i]>0){
				xvals[i]=i;
				yvals[i] = population[i];
				numFound +=1;
			}
		}
		if( numFound > 0){
			xvals = Arrays.copyOf(xvals, numFound);
			yvals = Arrays.copyOf(yvals, numFound);
			DoubleDataset xvals_ds = new DoubleDataset(xvals);
			DoubleDataset yvals_ds = new DoubleDataset(yvals);
			boolean backgroundDominated = true;
			boolean autoStopping = true;
			double threshold = 0.10;
			int numPeaks = 1;
			int smoothing = 5;
			
			APeak peakFunction = new Gaussian(10,10,10);
			List<APeak> fittedPeakList = Generic1DFitter.fitPeaks(xvals_ds, yvals_ds, peakFunction, new GeneticAlg(0.0001),
					smoothing, numPeaks, threshold, autoStopping, backgroundDominated);
			if( fittedPeakList.size() ==1){
				return fittedPeakList.get(0);
			}
		}
		return null;
	}
	
	private void createNewResultFile(String filename) throws Exception{
		File file = new File(filename);
		if (file.exists()) {
			if (!file.delete()) {
				throw new Exception("Unable to delete result file:" + file.getAbsolutePath());
			}
		}
		Hdf5Helper.getInstance().setAxisIndexingToMatchGDA(filename);
	}
	
	public void combineThresholdResults(List<String> edgeThresholdFiles, String attrNameForThresholdAB,
			String resultFile) throws Exception {
		Hdf5Helper hdf =Hdf5Helper.getInstance();
		hdf.concatenateDataSetsFromFiles(edgeThresholdFiles, getEqualisationLocation(), EDGE_THRESHOLDS_DATASET,
				resultFile);
		@SuppressWarnings("cast")
		Hdf5HelperData signalData = new Hdf5HelperData((int) 1);

		HDF5HelperLocations edgeloc = getEdgeThresholdsLocation();
		hdf.writeAttribute(resultFile, Hdf5Helper.TYPE.DATASET, edgeloc, "signal", signalData);
		Vector<Hdf5HelperData> data = new Vector<Hdf5HelperData>();
		for (String f : edgeThresholdFiles) {
			data.add(hdf.readAttribute(f, Hdf5Helper.TYPE.DATASET, edgeloc.getLocationForOpen(),
					attrNameForThresholdAB));
		}
		hdf.concatenateDataSets(data, getEqualisationLocation(), THRESHOLD_DATASET, resultFile);
		@SuppressWarnings("cast")
		Hdf5HelperData axisData = new Hdf5HelperData((int) 1);

		HDF5HelperLocations thresholdLoc = getEqualisationLocation();
		thresholdLoc.add(THRESHOLD_DATASET);

		hdf.writeAttribute(resultFile, Hdf5Helper.TYPE.DATASET, thresholdLoc, "axis", axisData);
	}
	

}
