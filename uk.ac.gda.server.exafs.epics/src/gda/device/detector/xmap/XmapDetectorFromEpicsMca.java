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
import gda.device.Analyser;
import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.XmapDetector;
import gda.device.detector.DetectorBase;
import gda.device.detector.NXDetectorData;
import gda.device.detector.NexusDetector;
import gda.device.detector.analyser.EpicsMCA;
import gda.device.detector.analyser.EpicsMCAPresets;
import gda.device.detector.analyser.EpicsMCARegionOfInterest;
import gda.device.detector.analyser.IEpicsMCA;
import gda.device.scannable.PositionConvertorFunctions;
import gda.factory.FactoryException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.nexusformat.NexusFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.beans.vortex.DetectorElement;
import uk.ac.gda.beans.vortex.RegionOfInterest;
import uk.ac.gda.beans.vortex.VortexParameters;
import uk.ac.gda.util.beans.xml.XMLHelpers;
/**
 * An {@link XmapDetector} made from a number of Mca's (technically {@link Analyser}s).
 * 
 */
public class XmapDetectorFromEpicsMca extends DetectorBase implements XmapDetector, NexusDetector {
	
	private static final Logger logger = LoggerFactory.getLogger(XmapDetectorFromEpicsMca.class);

	private List<Analyser> analysers = new ArrayList<Analyser>();
	
	private String configFileName;
	
	private List<String> roiChannelLabels = Collections.emptyList();
	
	private boolean readNetCounts = false;
	
	private boolean useLiveTime = false;
	
	// NexusDetector
	private boolean sumAllElementData = false;

	private VortexParameters vortexParameters;

	private int actualNumberOfconfigredRois = 0;

	private boolean readConfigFromDeviceAtScanStart=false;

	private boolean prefixExtraNameWithDetElement=true;

	private boolean useConfigFileAtConfigure=true;

	
	
	public boolean isUseConfigFileAtConfigure() {
		return useConfigFileAtConfigure;
	}

	/*
	 * useConfigFileAtConfigure - if True the config file is used to setup the device in the configure method
	 */
	public void setUseConfigFileAtConfigure(boolean useConfigFileAtConfigure) {
		this.useConfigFileAtConfigure = useConfigFileAtConfigure;
	}

	public boolean isPrefixExtraNameWithDetElement() {
		return prefixExtraNameWithDetElement;
	}

	/**
	 * @param prefixExtraNameWithDetElement If true the extraName for each roi is set to detector name + "_" + roiName.
	 * If false the extraName is simply the name of the roi. Default is true
	 * 
	 * if true
	 */
	public void setPrefixExtraNameWithDetElement(boolean prefixExtraNameWithDetElement) {
		this.prefixExtraNameWithDetElement = prefixExtraNameWithDetElement;
	}

	public boolean isReadConfigFromDeviceAtScanStart() {
		return readConfigFromDeviceAtScanStart;
	}

	/**
	 * Readout requires vortexParameters to be defined. If readConfigFromDeviceAtScanStart is true then
	 * this parameter is set aat atScanStart by reading from the device. This allows roi's to be defined ont he
	 * device itself rather than having to edit the configFile. Default value is false;
	 * @param readConfigFromDeviceAtScanStart
	 */
	public void setReadConfigFromDeviceAtScanStart(boolean readConfigFromDeviceAtScanStart) {
		this.readConfigFromDeviceAtScanStart = readConfigFromDeviceAtScanStart;
	}

	/**
	 * Report roi even if start = -1
	 */
	boolean reportInvalidROI=true;
	
	protected boolean isReportInvalidROI() {
		return reportInvalidROI;
	}

	protected void setReportInvalidROI(boolean reportInvalidROI) {
		this.reportInvalidROI = reportInvalidROI;
	}

	/*
	 * if true NexusTree contains a single item called fullspectrum that has the fullspectrum for all analysers in a single 2d object
	 */
	boolean returnFullSpectrum=true;
	
	
	
	protected boolean isReturnFullSpectrum() {
		return returnFullSpectrum;
	}

	protected void setReturnFullSpectrum(boolean returnFullSpectrum) {
		this.returnFullSpectrum = returnFullSpectrum;
	}

	/**
	 * Returns common value if all elements in the given list are equal, otherwise throws an IllegalStateException
	 */
	private static <T> T consensusFrom(List<T> list) {
		Iterator<T> iterator = list.iterator();
		T consensus = iterator.next();
		while (iterator.hasNext()) {
			if ( ! iterator.next().equals(consensus)) {
				throw new IllegalStateException("Values don't match: " + ArrayUtils.toString(list.toArray()));
			}
		}
		return consensus;
	}

	/**
	 * Returns common value if all elements in the given list are equal, otherwise throws an IllegalStateException
	 */
	private static Double consensusFromDoubles(List<Double> list) {
		Iterator<Double> iterator = list.iterator();
		Double consensus = iterator.next();
		while (iterator.hasNext()) {
			Double diff = Math.abs(iterator.next() - consensus);
			if (consensus != 0) {
				// normalise difference
				diff = diff / consensus;
			}
			if (diff > .001) { // .1% or .001
				throw new IllegalStateException("Values don't match: " + ArrayUtils.toString(list.toArray()));
			}
		}
		return consensus;
	}
	
	static private double sum(double[] a) {
		double sum = 0;
		for (int i = 0; i < a.length; i++) {
			sum = sum + a[i];
		}
		return sum;
	}

	/**
	 * To aid debugging with mocks from java
	 */
	public static double[][] toJavaNativeDoubleArray(List<List<Double>> ll) {
		double[][] a = new double[ll.size()][ll.get(0).size()];
		for (int i = 0; i < ll.size(); i++) {
			for (int j = 0; j < ll.get(0).size(); j++) {
				a[i][j] = ll.get(i).get(j);
			}
		}
		return a;
	}
	
	public XmapDetectorFromEpicsMca() {
		setInputNames(new String[0]);
	}
	
	public void setAnalysers(List<Analyser> analysers) {
		this.analysers = analysers;
	}

	public List<Analyser> getAnalysers() {
		return analysers;
	}
	
	public String getConfigFileName() {
		return configFileName;
	}

	public void setConfigFileName(String configFileName) {
		this.configFileName = configFileName;
	}
	
	public void setSumAllElementData(boolean sumAllElementData) {
		this.sumAllElementData = sumAllElementData;
	}

	public boolean isSumAllElementData() {
		return sumAllElementData;
	}
	
	public void setReadNetCounts(boolean readNetCounts) {
		this.readNetCounts = readNetCounts;
	}

	public boolean isReadNetCounts() {
		return readNetCounts;
	}

	public void setUseLiveTime(boolean useLiveTime) {
		this.useLiveTime = useLiveTime;
	}

	public boolean isUseLiveTime() {
		return useLiveTime;
	}

	private void checkConfigured() throws DeviceException{
		if( !configured || ! super.isConfigured())
			throw new DeviceException(getName() + " is not yet configured or configuration failed");
	}
	@Override
	public boolean isConfigured() {
		try {
			checkConfigured();
			return true;
		} catch (DeviceException e) {
			logger.error(e.getMessage(), e);
		}
		return false;
	}

	@Override
	public void configure() throws FactoryException {
		if (!configured) {			
			if (getAnalysers().size() < 1) {
				throw new FactoryException("No anyalsers have been configured");
			}
			try {
				if( useConfigFileAtConfigure){
					loadConfigurationFromFile();
				} else {
					vortexParameters = createVortexParameters();
				}
				configured = true;
			} catch (Exception e) {
				throw new FactoryException("Cannot load xml file "+ getConfigFileName(), e);
			}
		}
	}
	
	@Override
	public int getNumberOfMca() throws DeviceException {
		return analysers.size();
	}
	
	public void loadConfigurationFromFile() throws Exception {
		if (getConfigFileName() == null) {
			logger.warn(getName()  + " has no configFileName configured so the configuration cannot be loaded.");
			return;
		}
		vortexParameters = readConfigFile(getConfigFileName());
		configureHardware(vortexParameters);
	
	}

	public void configureHardware(VortexParameters vortexParameters) throws Exception {
		// Number of ROIs defined in XML file.
		configureRegionsOfInterest(vortexParameters);
		configureChannelLabels(vortexParameters);
		this.vortexParameters=vortexParameters;
	
	}

	public VortexParameters readConfigFile(String fileName) throws Exception {
		return (VortexParameters) XMLHelpers.createFromXML(VortexParameters.mappingURL,
				VortexParameters.class, VortexParameters.schemaURL, fileName);
	}
	
	
	private void configureRegionsOfInterest(final VortexParameters vp) throws Exception {
		try {			
			List<DetectorElement> detectorList = vp.getDetectorList();
			if( detectorList != null ){
				if( detectorList.size() > analysers.size())
					throw new Exception("Number of detectors in vortext parameters > number of analysers");
				int mcaIndex = 0;
				for (DetectorElement e : detectorList) {
					final double[][] rois = new double[e.getRegionList().size()][2];
					int iregion = 0;
					for (RegionOfInterest roi : e.getRegionList()) {
						rois[iregion][0] = roi.getWindowStart();
						rois[iregion][1] = roi.getWindowEnd();
						++iregion;
					}
					setROI(rois, mcaIndex);
					++mcaIndex;
				}
			}
		} catch (DeviceException e) {
			logger.error(e.getMessage(), e);
			throw new Exception("Cannot configure vortex regions of interest.", e);
		}
	}

	/**
	 * Configure the roi names based on the first elements ROI names.
	 * @param vp
	 */
	private void configureChannelLabels(VortexParameters vp) {
		roiChannelLabels = new ArrayList<String>(7);
		int roiNum = 0;
		for (RegionOfInterest roi : vp.getDetectorList().get(0).getRegionList()) {
			String name = roi.getRoiName();
			if(name==null) name = "ROI "+roiNum;
			roiChannelLabels.add(name);
			++roiNum;
		}
	}

	@Override
	public List<String> getChannelLabels() {
		return roiChannelLabels;
	}

	@Override
	public void clear() throws DeviceException {
		for (Analyser analyser : analysers) {
			analyser.clear();
		}
		
	}

	@Override
	public void start() throws DeviceException {
		for (Analyser analyser : analysers) {
			analyser.startAcquisition();
		}
	}

	@Override
	public void clearAndStart() throws DeviceException {
		for (Analyser analyser : analysers) {
			if (analyser instanceof EpicsMCA) {
				((EpicsMCA) analyser).eraseStartAcquisition();
			} else if (analyser instanceof IEpicsMCA) {
				((IEpicsMCA) analyser).eraseStartAcquisition();
			}  else {
				clear();
				start();
			}
		}
	}

	@Override
	public void setCollectionTime(double collectionTime) throws DeviceException {
		setAcquisitionTime(collectionTime);
	}
	
	@Override
	public double getCollectionTime() {
		try {
			return getAcquisitionTime();
		} catch (DeviceException e) {
			logger.error("DeviceException while getting collection time. Returning -999 as temporary bodge:" +e.getMessage(), e);
		}
		return -999;
	}
	
	@Override
	public void collectData() throws DeviceException {
		clearAndStart();
//		for (Analyser analyser : analysers) {
//			analyser.collectData();
//		}
	}

	@Override
	public void stop() throws DeviceException {
		for (Analyser analyser : analysers) {
			analyser.stop();
		}
	}

	@Override
	public void setNumberOfBins(int numberOfBins) throws DeviceException {
		for (Analyser analyser : analysers) {
			analyser.setNumberOfChannels(numberOfBins);
		}
	}

	@Override
	public int getNumberOfBins() throws DeviceException {
		List<Integer> binsFromEachAnalyser = new ArrayList<Integer>(analysers.size());
		for (Analyser analyser : analysers) {
			binsFromEachAnalyser.add((int) analyser.getNumberOfChannels());
		}
		return consensusFrom(binsFromEachAnalyser);
	}

	@Override
	public void setAcquisitionTime(double time) throws DeviceException { // takes in ms
		for (Analyser analyser : analysers) {
			analyser.setCollectionTime(time/1000.);
			if (isUseLiveTime()) {
				analyser.setPresets((new EpicsMCAPresets(0, (float) time/1000, 0, 0, 0, 0)));
			} else { // real time
				analyser.setPresets((new EpicsMCAPresets((float) time/1000, 0, 0, 0, 0, 0)));
			}
		}
	}

	@Override
	public double getAcquisitionTime() throws DeviceException {
		List<Double> acquisitionTimeEachAnalyser = new ArrayList<Double>(analysers.size());
		for (Analyser analyser : analysers) {
			acquisitionTimeEachAnalyser.add(analyser.getCollectionTime());
		}
		return consensusFromDoubles(acquisitionTimeEachAnalyser)*1000;
	}

	/**
	 * This implementation simply returns the acquisition time. The analyser will determine which to use
	 * when its setCollectionTime method is called.
	 */
	@Override
	public double getRealTime() throws DeviceException {
		return getAcquisitionTime();
	}

	@Override
	public boolean createsOwnFiles() throws DeviceException {
		return false;
	}

	@Override
	public int getStatus() throws DeviceException {
		for (Analyser analyser : analysers) {
			int status = analyser.getStatus();
			if (status != Detector.IDLE) {
				return status;
			}
		}
		return Detector.IDLE;
	}

	@Override
	public int[][] getData() throws DeviceException {
		int[][] data = new int[getNumberOfMca()][getNumberOfBins()];
		for (int i = 0; i < getNumberOfMca(); i++) {
			data[i] = getData(i);
		}
		return data;
	}

	@Override
	public int[] getData(int mcaNumber) throws DeviceException {
		Object data = analysers.get(mcaNumber).getData();
		if( data instanceof int[]){
			return (int[])data;
		}
		int[] integerArray = ArrayUtils.toPrimitive(
				PositionConvertorFunctions.toIntegerArray(data));
		return ArrayUtils.subarray(integerArray, 0, (int) analysers.get(mcaNumber).getNumberOfChannels());
	}

	@Override
	public int[] getDataDimensions() throws DeviceException {
		return new int[]{getExtraNames().length}; // no input names
	}

	@Override
	public void atScanStart() throws DeviceException {
		try {
			if( readConfigFromDeviceAtScanStart){
				vortexParameters = createVortexParameters();
			}
			checkConfigured();
			for (Analyser analyser : analysers) {
				analyser.atScanStart();
			}
			firstScanPointDone = false;
		} catch (Exception e) {
			throw new DeviceException("Error in atScanStart",e);
		}
	}
	/**
	 * Flag used to added metadata to nexus data on first point in scan only
	*/
	boolean firstScanPointDone=false;
	@Override
	public NexusTreeProvider readout() throws DeviceException {
	// 		"Element0_realtime", "Element0_livetime", "Element0_Pb_K", "Element0_Fe_K", "Element1_realtime", "Element1_livetime", "Element1_Pb_K", "Element1_Fe_K"}, xmap.getExtraNames());
		long startTime = System.nanoTime();

		
		NXDetectorData output = new NXDetectorData(this);
		INexusTree detTree    = output.getDetTree(getName());
		
		int detectorData[][] = getData();
		double[] summation =null;
		for (int element = 0; element < vortexParameters.getDetectorList().size(); element++) {
			DetectorElement thisElement = vortexParameters.getDetectorList().get(element);

			// Real and live time
			float[] realAndLiveTime = getRealAndLiveTime(element);
			output.addData(detTree, thisElement.getName()+"_realtime", null, NexusFile.NX_FLOAT64, new double[]{realAndLiveTime[0]}, "s", 1);
			output.setPlottableValue(thisElement.getName()+"_realtime", (double) realAndLiveTime[0]);
			output.addData(detTree, thisElement.getName()+"_livetime", null, NexusFile.NX_FLOAT64, new double[]{realAndLiveTime[1]}, "s", 1);
			output.setPlottableValue(thisElement.getName()+"_livetime", (double) realAndLiveTime[1]);
			
			Analyser analyser = analysers.get(element);
			IEpicsMCA epicsMCA = (analyser instanceof IEpicsMCA) ? (IEpicsMCA) analyser : null;
			HashMap<Integer, double[][]> regionsOfInterestCountCache = null;

			// REGIONS
			for (int roiIndex = 0; roiIndex < thisElement.getRegionList().size(); roiIndex++) {
	
				final RegionOfInterest roi = thisElement.getRegionList().get(roiIndex);
				if( isReportInvalidROI() || roi.getRoiStart() != -1){
					double count;
					if( epicsMCA != null){
						/* This causes far less channel access calls than getROICountsUsingCache
						*/
						count = readNetCounts ? epicsMCA.getRoiNetCount(roiIndex):epicsMCA.getRoiCount(roiIndex);
					} else {
						if( regionsOfInterestCountCache == null)
							regionsOfInterestCountCache = new HashMap<Integer, double[][]>();
						count = getROICountsUsingCache(roiIndex, regionsOfInterestCountCache)[element];
					}
					INexusTree roidata = output.addData(detTree, getExtraName(thisElement, roi), null, NexusFile.NX_FLOAT64, new double[]{count}, "counts", 1);
					if(!firstScanPointDone){
						roidata.addChildNode(new NexusTreeNode("start",NexusExtractor.AttrClassName, roidata, new NexusGroupData(roi.getRoiStart())));
						roidata.addChildNode(new NexusTreeNode("end",NexusExtractor.AttrClassName, roidata, new NexusGroupData(roi.getRoiEnd())));
					}
					output.setPlottableValue(getExtraName(thisElement, roi), count);
				}
	
			}
			//add the full spectrum
			output.addData(detTree, thisElement.getName()+"_fullSpectrum", new int[]{detectorData[element].length}, NexusFile.NX_INT32, detectorData[element], "counts", 1);
			if(sumAllElementData)
			{
				if(summation == null)
					summation = new double[detectorData[element].length];
				for(int i =0; i < detectorData[element].length; i++)
				{
					summation[i ] += detectorData[element][i];
				}
			}
		}
		logger.info("xmap - total readout time: " + (System.nanoTime() - startTime)/1000000000.);
		if( isReturnFullSpectrum())
			output.addData(detTree, "fullSpectrum", new int[]{detectorData.length, detectorData[0].length}, NexusFile.NX_INT32, detectorData, "counts", 1);
		
		if(summation != null)
			output.addData(detTree, "allElementSum", new int[]{summation.length}, NexusFile.NX_INT32, summation, "counts",1);
		firstScanPointDone=true;
		return output;
	}

	private float[] getRealAndLiveTime(int detectorElement) throws DeviceException {
		Object elapsedParameters = analysers.get(detectorElement).getElapsedParameters();
		return (float[]) elapsedParameters;
	}
	
	@Override
	public double readoutScalerData() throws DeviceException {
		throw new RuntimeException("Not supported");
	}

	@Override
	public double[] getROIsSum() throws DeviceException {
		double[] roiSum = new double[getNumberOfROIs()];
		for (int regionIndex = 0; regionIndex < getNumberOfROIs(); regionIndex++) {
			roiSum[regionIndex] = sum(getROICounts(regionIndex));
		}
		return roiSum;
	}

	@Override
	public void setNthROI(double[][] rois, int roiIndex) throws DeviceException {
		throw new RuntimeException("Not supported");
	}

	@Override
	public void setROIs(double[][] rois) throws DeviceException {
		for(int i =0; i< getNumberOfMca(); i++)
			setROI(rois, i);
	}

	/**
	 * Set rois the array can be of size [maximum number rois][2] if it is lower for instance
	 * [actual number of rois][2] then the other possible rois will be set to zero.
	 * 
	 * The actual number of rois is also taken from the length of the first dimension of this array
	 * so it should always be passed in with size of the actual number of rois.
	 */
	void setROI(double[][] rois, int mcaIndex) throws DeviceException{
		Analyser mca = analysers.get(mcaIndex);
		int numberOfRoisToActivate = rois.length;
		for (int regionIndex = 0; regionIndex < numberOfRoisToActivate;regionIndex++ ){
			double regionLow = rois[regionIndex][0];
			double regionHigh = rois[regionIndex][1];
			mca.addRegionOfInterest(regionIndex, regionLow, regionHigh, -1, -1, "roi" + regionIndex);
		}
		for (int regionIndex = numberOfRoisToActivate ; regionIndex < mca.getNumberOfRegions(); regionIndex++ ) {
			mca.deleteRegionOfInterest(regionIndex);
		}
		actualNumberOfconfigredRois  = numberOfRoisToActivate;
	}

	@Override
	public int getNumberOfROIs() throws DeviceException {
		return actualNumberOfconfigredRois;
	}

	/* 
	 * performance optimized version 
	 */
	double[] getROICountsUsingCache(int iRoi, Map<Integer,double[][]> regionsOfInterestCountCache) throws DeviceException {
		double[] countsFromEachMca = new double[getNumberOfMca()];
		for (int mcaIndex = 0; mcaIndex < countsFromEachMca.length; mcaIndex++) {
			double[][] ds=null;
			if( regionsOfInterestCountCache != null && regionsOfInterestCountCache.containsKey(mcaIndex)){
				ds = regionsOfInterestCountCache.get(mcaIndex);
			} else {
				ds = analysers.get(mcaIndex).getRegionsOfInterestCount();
				if( regionsOfInterestCountCache != null){
					regionsOfInterestCountCache.put(mcaIndex, ds);
				}
			}
			if( ds != null)
				countsFromEachMca[mcaIndex] = ds[iRoi][(readNetCounts) ? 1 : 0];
		}
		return countsFromEachMca;
	}

	
	@Override
	public double[] getROICounts(int iRoi) throws DeviceException {
		return getROICountsUsingCache(iRoi, null);
	}

	String getExtraName(DetectorElement detEl, RegionOfInterest roi){
		return prefixExtraNameWithDetElement ? detEl.getName()+"_"+roi.getRoiName() : roi.getRoiName();
	}
	@Override
	public String[] getExtraNames() {
		if( !isConfigured()) 
			return new String[]{};
		List<String> extraNames = new ArrayList<String>();
		for (DetectorElement thisElement: vortexParameters.getDetectorList()) {
			if (thisElement.isExcluded()) continue;
			extraNames.add(thisElement.getName() + "_realtime");
			extraNames.add(thisElement.getName() + "_livetime");
			for (RegionOfInterest roi : thisElement.getRegionList()) {
				if( isReportInvalidROI() || roi.getRoiStart() != -1)
					extraNames.add(getExtraName(thisElement, roi));
			}
		}
		return extraNames.toArray(new String[]{});
	}

	@Override
	public String[] getOutputFormat() {
		if( !isConfigured())
			return new String[]{};
		String[] formats = new String[getExtraNames().length];
		for (int i = 0; i < formats.length; i++) {
			formats[i] = "%5.5g";
		}
		return formats;
	}
	
	@Override
	public String getDescription() throws DeviceException {
		return "";
	}

	@Override
	public String getDetectorID() throws DeviceException {
		return "";
	}

	@Override
	public String getDetectorType() throws DeviceException {
		return "";
	}

	@Override
	public void setStatusRate(double statusRate) throws DeviceException {
		throw new RuntimeException("Not supported");
		
	}

	@Override
	public double getStatusRate() throws DeviceException {
		throw new RuntimeException("Not supported");
	}

	@Override
	public void setReadRate(double readRate) throws DeviceException {
		throw new RuntimeException("Not supported");
	}

	@Override
	public double getReadRate() throws DeviceException {
		throw new RuntimeException("Not supported");
	}

	public VortexParameters createVortexParameters() throws Exception{
		VortexParameters vortexParameters = (VortexParameters) XMLHelpers.createFromXML(VortexParameters.mappingURL,
				VortexParameters.class, VortexParameters.schemaURL, getConfigFileName());
		
		//make a deep copy we can then change a little
		VortexParameters vp = new VortexParameters(vortexParameters);
		
		for(int i=0; i< vp.getDetectorList().size(); i++){
			//There is 1 detector element per analyser
			DetectorElement de = vp.getDetectorList().get(i);
			if(!de.isExcluded()){
				//read regionlist from mca
				Analyser anly = analysers.get(i);
				int regions = anly.getNumberOfRegions();
				//either fill in an existing region or create a new one
				List<RegionOfInterest> regionList = de.getRegionList();
				for( int ir=0; ir< regions; ir++){
					if( ir >= regionList.size()){
						regionList.add(new RegionOfInterest());
					}
					RegionOfInterest roi = regionList.get(ir);
					if( anly instanceof IEpicsMCA){
						IEpicsMCA mca =(IEpicsMCA)anly;
						EpicsMCARegionOfInterest mcaRoi = mca.getNthRegionOfInterest(ir);
						String regionName = mcaRoi.getRegionName();
						roi.setRoiName(regionName.isEmpty() ? Integer.toString(ir) : regionName);
						roi.setWindowStart((int)mcaRoi.getRegionLow());
						roi.setWindowEnd((int)mcaRoi.getRegionHigh());
					}
				}
			}
		}
		return vp;
	}
	@Override
	public void waitWhileBusy() throws DeviceException, InterruptedException {
		for (Analyser analyser : analysers) {
			analyser.waitWhileBusy();
		}
	}	
}
