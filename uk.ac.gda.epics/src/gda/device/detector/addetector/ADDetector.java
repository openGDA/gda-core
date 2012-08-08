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

package gda.device.detector.addetector;

import gda.data.nexus.extractor.NexusExtractor;
import gda.data.nexus.extractor.NexusGroupData;
import gda.data.nexus.tree.INexusTree;
import gda.data.nexus.tree.NexusTreeNode;
import gda.data.nexus.tree.NexusTreeProvider;
import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.detector.DetectorBase;
import gda.device.detector.GDANexusDetectorData;
import gda.device.detector.NXDetectorData;
import gda.device.detector.NXDetectorDataWithFilepathForSrs;
import gda.device.detector.NexusDetector;
import gda.device.detector.addetector.filewriter.FileWriter;
import gda.device.detector.addetector.filewriter.SingleImagePerFileWriter;
import gda.device.detector.addetector.triggering.ADTriggeringStrategy;
import gda.device.detector.addetector.triggering.SimpleAcquire;
import gda.device.detector.areadetector.NDStatsGroup;
import gda.device.detector.areadetector.NDStatsGroupFactory;
import gda.device.detector.areadetector.v17.ADBase;
import gda.device.detector.areadetector.v17.NDArray;
import gda.device.detector.areadetector.v17.NDFile;
import gda.device.detector.areadetector.v17.NDPluginBase;
import gda.device.detector.areadetector.v17.NDStats;
import gda.device.scannable.PositionCallableProvider;
import gda.factory.FactoryException;
import gda.jython.InterfaceProvider;
import gda.scan.Scan;
import gda.scan.ScanBase;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.commons.lang.ArrayUtils;
import org.nexusformat.NexusFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.StringUtils;

/**
 * <b>WARNING: This class is very much under development and will likely be until at least GDA 8.26 . <i>Please do use
 * it though</i> so that we can learn from your uses cases, but please let Rob Walton and Paul Gibbons know that you
 * are!</b>. The way it is configured is likely to change.
 * <p>
 * A {@link Scannable} {@link Detector} driver for Epics AreaDetectors suitable for use within {@link Scan}s that
 * support detectors that implement PositionCallableProvider<NexusTreeProvider>. 
 * 
 * The NexusTreeProvider returned by the call method of the PositionCallableProvider implements {@link GDANexusDetectorData}
 * which provides plottable and printable data as well as binary detector data and detector metadata. If configured to return
 * filepaths the object supports {@link NXDetectorDataWithFilepathForSrs} which will result in the filepath being 
 * returned as the first Scannable extra field and printed to the terminal and SRS files.
 * 
 * The Epics AreaDetector software is very modular and this is reflected in the structure of this class. 
 * As far as ADDetector is concerned an EPICS AreaDetector consists of 2 or 3 parts:
 * 1. A Base plugin that supports the source of the data, e.g. camera, The base is used to setup the acquisition, 
 * exposure time trigger mode, and start/stop acquisition. The result of an acquisition is a chunk of binary data.
 * 2. A NDArray plugin that makes the binary data available over channel access 
 * 3. A file writer plugin that will write the binary data to a file of some format. Either 1 file 
 * per acquisition or 1 file per collection of acquisitions
 * 
 * This camera can be used by GDA in 2 different modes:
 * 
 * 1. Camera base  + PV
 * 2. Camera base + file writer
 * 
 * 
 * This structure is represented in ADDetector by 3 main components:
 * 
 * {@link ADTriggeringStrategy} - used to handle the camera base
 * {@link NDArray} - handles the PV to read the binary data
 * {@link FileWriter} - used to handle the file writer plugin
 * 
 * The result of getPositionCallable is  the creation of an object that has sufficient information to allow creation of 
 * a NexusTreeProvider in its call method. This object can take data from:
 * 
 * 1. The NDArray object if present and selected 
 * 2. An NDStats, that represents the NDStats plugin, if present and selected
 * 3. The ADTriggeringStrategy object if selected
 * 4. The FileWriter if selected
 * 5. Another NexusTreeProvider that provides meta data if present.
 * 
 */
public class ADDetector extends DetectorBase implements InitializingBean, NexusDetector, PositionCallableProvider<NexusTreeProvider> {

	public class DummyFileWriter implements FileWriter {

		static final String DUMMY_FILE_WRITER_GET_FULL_FILE_NAME_RBV = "DummyFileWriter - getFullFileName_RBV";

		@Override
		public void prepareForCollection(int numberImagesPerCollection) throws Exception {
		}

		@Override
		public void endCollection() throws Exception {
		}

		@Override
		public void disableFileWriter() throws Exception {
		}

		@Override
		public boolean isLinkFilepath() {
			return false;
		}

		@Override
		public void stop() throws Exception {
		}

		@Override
		public void atCommandFailure() throws Exception {
		}

		@Override
		public boolean isSetFileNameAndNumber() {
			return false;
		}

		@Override
		public void setSetFileNameAndNumber(boolean setFileWriterNameNumber) {
		}

		@Override
		public String toString() {
			return "DummyFileWriter";
		}

		@Override
		public void setEnable(boolean enable) {
		}

		@Override
		public boolean getEnable() {
			return false;
		}

		@Override
		public void enableCallback(boolean enable) throws Exception {
			//do nothing
		}

		@Override
		public String getFullFileName_RBV() throws Exception {
			//we need to return a read filepath otherwise checks later fail.
			return DUMMY_FILE_WRITER_GET_FULL_FILE_NAME_RBV;
		}
		

	}

	private static final String[] A = new String[] {};

	protected static final String UNSUPPORTED_PART_OF_SCANNABLE_INTERFACE = "ADDetector does not support operation through its Scannable interface. Do not use pos until pos supports detectors as Detectors rather than Scannables";

	protected static final String FILEPATH_EXTRANAME = "filepath";

	private static Logger logger = LoggerFactory.getLogger(ADDetector.class);

	private ADBase adBase;

	private NDStats ndStats;

	private NDArray ndArray;

	private NDFile ndFile;

	private ADTriggeringStrategy collectionStrategy;

	private String description = "ADDetector";

	private String detectorType = "ADDetector";

	private String arrayDataName = "arrayData";

	private boolean computeStats = false;

	private boolean computeCentroid = false;

	private boolean readArray = true;

	private boolean readAcquisitionTime = true;

	private boolean readAcquisitionPeriod = false;

	private boolean readFilepath = false;

	/**
	 * Controls whether the driver does callbacks with the array data to registered plugins. 0=No, 1=Yes. 
	 * Setting this to 0 can reduce overhead in the case that the driver is being used only to control the device, and not to make the data available to plugins or to EPICS clients.
	 */
	private boolean disableCallbacks = false;

	private NDStatsGroup statsGroup;

	private NDStatsGroup centroidGroup;

	public ADDetector() {
		logger.warn("DASC developer WARNING ADDetector is under development. The way it is configured is likely to change until at least GDA 8.20. See javadoc.");
		setLocal(true);
	}

	public void setCollectionStrategy(ADTriggeringStrategy collectionStrategy) {
		this.collectionStrategy = collectionStrategy;
	}

	public void setAdBase(ADBase adBase) {
		this.adBase = adBase;
	}

	public void setNdStats(NDStats ndStats) {
		this.ndStats = ndStats;
	}

	public void setNdArray(NDArray ndArray) {
		this.ndArray = ndArray;
	}

	public void setNdFile(NDFile ndFile) {
		this.ndFile = ndFile;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setDetectorType(String detectorType) {
		this.detectorType = detectorType;
	}

	public void setComputeStats(boolean computeStats) {
		this.computeStats = computeStats;
		configureExtraNamesAndOutputFormat();
	}

	public void setComputeCentroid(boolean computeCentroid) {
		this.computeCentroid = computeCentroid;
		configureExtraNamesAndOutputFormat();
	}

	public void setReadArray(boolean readArray) {
		this.readArray = readArray;
	}

	public void setReadAcquisitionTime(boolean readAcquisitionTime) {
		this.readAcquisitionTime = readAcquisitionTime;
		configureExtraNamesAndOutputFormat();
	}

	public void setReadAcquisitionPeriod(boolean readAcquisitionPeriod) {
		this.readAcquisitionPeriod = readAcquisitionPeriod;
		configureExtraNamesAndOutputFormat();
	}

	public void setReadFilepath(boolean readFilepath) {
		this.readFilepath = readFilepath;
		configureExtraNamesAndOutputFormat();
	}

	public ADTriggeringStrategy getCollectionStrategy() {
		return collectionStrategy;
	}

	public ADBase getAdBase() {
		return adBase;
	}

	public NDStats getNdStats() {
		return ndStats;
	}

	public NDArray getNdArray() {
		return ndArray;
	}

	public NDFile getNdFile() {
		return ndFile;
	}

	public boolean isComputeStats() {
		return computeStats;
	}

	public boolean isComputeCentroid() {
		return computeCentroid;
	}

	public boolean isReadArray() {
		return readArray;
	}

	public boolean isReadAcquisitionTime() {
		return readAcquisitionTime;
	}

	public boolean isReadAcquisitionPeriod() {
		return readAcquisitionPeriod;
	}

	/*
	 * If true && !filewriter.isLinkedPath report filepath in readout 
	 * If true && filewriter.isLinkedPath add external link
	 */
	public boolean isReadFilepath() {
		return readFilepath;
	}

	@Override
	public boolean createsOwnFiles() throws DeviceException {
		return false; // always return nexus data
	}



	boolean afterPropertiesSetCalled = false;

	// Controls when to put non point specific items in the nexusdetectordata
	private boolean firstReadoutInScan = true;

	// provides metadata about the detector. This is added as a child of the detector element of the tree
	private NexusTreeProvider metaDataProvider;

	private String detectorID;

	public NexusTreeProvider getMetaDataProvider() {
		return metaDataProvider;
	}

	public void setMetaDataProvider(NexusTreeProvider metaDataProvider) {
		this.metaDataProvider = metaDataProvider;
	}

	private FileWriter fileWriter;

	public FileWriter getFileWriter() {
		return fileWriter;
	}

	public void setFileWriter(FileWriter fileWriter) {
		this.fileWriter = fileWriter;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (afterPropertiesSetCalled)
			throw new RuntimeException("afterPropertiesSet already called");
		if (getAdBase() == null)
			throw new IllegalStateException("adBase is not defined");
		if ((ndStats == null) && (isComputeCentroid()))
			throw new IllegalStateException("ndStats is not defined despite being configured to read centroid");
		if ((ndStats == null) && (isComputeStats()))
			throw new IllegalStateException("ndStats is not defined despite being configured to read stats");
		if ((ndArray == null) && (isReadArray()))
			throw new IllegalStateException("ndArray is not defined despite being configured to readArray");
		if (collectionStrategy == null) {
			collectionStrategy = new SimpleAcquire(getAdBase(), 0.);
		}
		if (fileWriter == null) {
			if( ndFile != null){
				SingleImagePerFileWriter fileW = new SingleImagePerFileWriter(getNdFile(), getName(), "%s%s-%d.tif","%s/"+getName()+"/",
						true,true);
				fileW.setTemplatesRequireScanNumber(false);
				fileW.setEnable(true);
				fileW.afterPropertiesSet();
				fileWriter = fileW;
			}
			else
				fileWriter = new DummyFileWriter();
		}
		afterPropertiesSetCalled = true;
	}

	@Override
	public void configure() throws FactoryException {
		super.configure();
		if (!afterPropertiesSetCalled)
			throw new RuntimeException("afterPropertiesSet not yet called");
		if( !StringUtils.hasLength(getName()))
			throw new RuntimeException("name is not defined");
		createStatsGroups();
		setInputNames(A);
		configureExtraNamesAndOutputFormat();

		try {
			reset();
		} catch (Exception e) {
			throw new FactoryException("Problem reseting a plugin", e);
		}
	}

	public void reset() throws Exception {
		getAdBase().reset();
		if (ndArray != null) {
			ndArray.reset();
		}
		if (getNdStats() != null) {
			getNdStats().reset();
		}
		if (ndFile != null) {
			ndFile.reset();
		}
	}

	private void createStatsGroups() {
		if (getNdStats() != null) {
			statsGroup = NDStatsGroupFactory.getStatsInstance(getNdStats());
			centroidGroup = NDStatsGroupFactory.getCentroidInstance(getNdStats());
		}
	}

	/**
	 * Extra name for each element in the stats and centroid statistics, if the instance is set to compute these.
	 */
	protected void configureExtraNamesAndOutputFormat() {
		if (!afterPropertiesSetCalled)
			return;
		List<String> extraNames = new ArrayList<String>();
		List<String> formats = new ArrayList<String>();
		if (isReadAcquisitionTime()) {
			extraNames.add("count_time");
			formats.add("%.2f");
		}
		if (isReadAcquisitionPeriod()) {
			formats.add("%.2f");
			extraNames.add("period");
		}
		if (isReadFilepath() && !getFileWriter().isLinkFilepath()) {
			extraNames.add(FILEPATH_EXTRANAME);
			// used to format the double that is put into the doubleVals array in this case
			formats.add("%.2f");
		}
		if (isComputeStats() && statsGroup != null) {
			extraNames.addAll(Arrays.asList(statsGroup.getFieldNames()));
			for (int i = 0; i < statsGroup.getFieldNames().length; i++) {
				formats.add("%5.5g");
			}
		}
		if (isComputeCentroid() && centroidGroup != null) {
			extraNames.addAll(Arrays.asList(centroidGroup.getFieldNames()));
			for (int i = 0; i < centroidGroup.getFieldNames().length; i++) {
				formats.add("%5.5g");
			}
		}
		setExtraNames(extraNames.toArray(A));
		setOutputFormat(formats.toArray(A));
	}

	@Override
	public void asynchronousMoveTo(Object collectionTime) throws DeviceException {
		throw new RuntimeException(UNSUPPORTED_PART_OF_SCANNABLE_INTERFACE);
	}

	@Override
	public boolean isBusy() {
		throw new RuntimeException(UNSUPPORTED_PART_OF_SCANNABLE_INTERFACE);
	}

	@Override
	public Object getPosition() throws DeviceException {
		throw new RuntimeException(UNSUPPORTED_PART_OF_SCANNABLE_INTERFACE);
	}

	@Override
	final public void prepareForCollection() throws DeviceException {
	}

	@Override
	public void atScanStart() throws DeviceException {
		firstReadoutInScan = true;
		try {
			//disable or enable callbacks if required by class variable first to allow collectionStrategy or fileWriter to turn back on needed
			getAdBase().setArrayCallbacks( isDisableCallbacks() ? 0 :1);
			int numberImagesPerCollection = getCollectionStrategy().getNumberImagesPerCollection(getCollectionTime());
			getCollectionStrategy().prepareForCollection(getCollectionTime(), 1);
			if(isReadFilepath()){
				getFileWriter().prepareForCollection(numberImagesPerCollection);
			}
			prepareForArrayAndStatsCollection();
		} catch (Exception e) {
			throw new DeviceException(e);
		}
	}

	@Override
	public void collectData() throws DeviceException {
		try {
			getCollectionStrategy().collectData();
		} catch (Exception e) {
			throw new DeviceException(e);
		}
	}

	@Override
	final public void endCollection() throws DeviceException {
	}

	@Override
	public void atScanEnd() throws DeviceException {
		try {
			if( latestPositionCallable != null)
			{
				//do not close down plugins as the callable could involve waiting for the last file to arrive
				//on disk
				latestPositionCallable.call(); // TODO: Should not be needed!
			}
			if(isReadFilepath()){
				getFileWriter().endCollection();
			}
			getCollectionStrategy().endCollection();
		} catch (Exception e) {
			throw new DeviceException(e);
		}
	}

	@Override
	public void stop() throws DeviceException {
		try {
			if(isReadFilepath()){
				getFileWriter().stop();
			}
			getCollectionStrategy().stop();
		} catch (Exception e) {
			throw new DeviceException(e);
		}
	}

	@Override
	public void atCommandFailure() throws DeviceException {
		try {
			if(isReadFilepath()){
				getFileWriter().atCommandFailure();
			}
			getCollectionStrategy().atCommandFailure();
		} catch (Exception e) {
			throw new DeviceException(e);
		}
	}

	public void prepareForArrayAndStatsCollection() throws DeviceException {
		try {
			// ndArray
			if (getNdArray() != null) {
				if (isReadArray()) {
					getNdArray().getPluginBase().enableCallbacks(); // waits
					getNdArray().getPluginBase().setBlockingCallbacks((short) 1);
	
				} else {
					getNdArray().getPluginBase().disableCallbacks();
					getNdArray().getPluginBase().setBlockingCallbacks((short) 0);
				}
			}

			// ndStats
			if (getNdStats() != null) {
				if (isComputeStats() || isComputeCentroid()) {
					logger.warn("The Stats plugin is not synchronized with putCallBack Acquire");
					getNdStats().getPluginBase().enableCallbacks(); // waits
					getNdStats().getPluginBase().setBlockingCallbacks((short) 1);

				} else {
					getNdStats().getPluginBase().disableCallbacks();
					getNdStats().getPluginBase().setBlockingCallbacks((short) 0);
				}
				getNdStats().setComputeStatistics((short) (isComputeStats() ? 1 : 0)); // TODO: DOES NOT wait
				getNdStats().setComputeCentroid((short) (isComputeCentroid() ? 1 : 0)); // TODO: DOES NOT wait
			}

		} catch (DeviceException e) {
			throw e;
		} catch (Exception e) {
			throw new DeviceException(e);
		}
	}

	@Override
	public int getStatus() throws DeviceException {
		try {
			return getCollectionStrategy().getStatus();
		} catch (Exception e) {
			throw new DeviceException(e);
		}
	}

	@Override
	public void waitWhileBusy() throws InterruptedException, DeviceException {
		try {
			getCollectionStrategy().waitWhileBusy();
		} catch (Exception e) {
			throw new DeviceException(e);
		}
	}

	@Override
	public String getDescription() throws DeviceException {
		return description;
	}

	@Override
	public String getDetectorID() throws DeviceException {
		return detectorID;
	}

	public void setDetectorID(String detectorID) {
		this.detectorID = detectorID;
	}

	@Override
	public String getDetectorType() throws DeviceException {
		return detectorType;
	}

	static private int[] dims = new int[] { 1 };

	private ADDetectorPositionCallable latestPositionCallable=null;

	private void addDoubleItemToNXData(NXDetectorData data, String name, Double val) {
		data.addData(getName(), name, dims, NexusFile.NX_FLOAT64, new double[] { val }, null, null);
		data.setDoubleVals((Double[]) ArrayUtils.add(data.getDoubleVals(), val));
	}

	protected final void addMultipleDoubleItemsToNXData(NXDetectorData data, String[] nameArray, Double[] valArray) {
		
		for (int i = 0; i < valArray.length; i++) {
			addDoubleItemToNXData(data, nameArray[i], valArray[i]);
		}
	}
	@Override
	public NexusTreeProvider readout() throws DeviceException {
		try {
			return getPositionCallable().call();
		} catch (Exception e) {
			throw new DeviceException("Error in readout of "+ getName(),e);
		}
	}

	protected String createFileName() throws Exception {
		return getFileWriter().getFullFileName_RBV();
	}

	private int[] determineDataDimensions() throws Exception {
		// only called if configured to readArrays (and hence ndArray is set)
		NDPluginBase pluginBase = ndArray.getPluginBase();
		int nDimensions = pluginBase.getNDimensions_RBV();
		int[] dimFromEpics = new int[3];
		dimFromEpics[0] = pluginBase.getArraySize2_RBV();
		dimFromEpics[1] = pluginBase.getArraySize1_RBV();
		dimFromEpics[2] = pluginBase.getArraySize0_RBV();

		int[] dims = java.util.Arrays.copyOfRange(dimFromEpics, 3 - nDimensions, 3);
		return dims;
	}

	public void setDisableCallbacks(boolean disableCallbacks) {
		this.disableCallbacks = disableCallbacks;
	}

	public boolean isDisableCallbacks() {
		return disableCallbacks;
	}

	/*
	 * If false getPositionCallable will return an object that simply returns the NexusProviderTree inside it with no delay
	 * waiting for it to be valid ( no file existence checking)
	 * If true the getPositionCallable will return an object with a call method will only return a NExusTreeProvider once it
	 * is valid if checkFileExists is true.
	 * 
	 * Default = false.
	 */
	boolean usePipeline=false;
	
	boolean checkFileExists=false;
	
	public boolean isUsePipeline() {
		return usePipeline;
	}

	public void setUsePipeline(boolean usePipeline) {
		this.usePipeline = usePipeline;
	}

	public boolean isCheckFileExists() {
		return checkFileExists;
	}

	public void setCheckFileExists(boolean checkFileExists) {
		this.checkFileExists = checkFileExists;
	}

	@Override
	public Callable<NexusTreeProvider> getPositionCallable() throws DeviceException {
		latestPositionCallable = new ADDetectorPositionCallable(getPositionCallableData());
		if( usePipeline){
			return latestPositionCallable;
		} 
		try {
			final NexusTreeProvider treeProvider = latestPositionCallable.call();
			return new Callable<NexusTreeProvider>(){
				@Override
				public NexusTreeProvider call() throws Exception {
					return treeProvider;
				}
				
			};
		} catch (Exception e) {
			throw new DeviceException("Error getting data", e);
		}
	}

	private PositionCallableData getPositionCallableData() throws DeviceException{
		try{
			NXDetectorData data;
			if (isReadFilepath() && !getFileWriter().isLinkFilepath()) {
				data = new NXDetectorDataWithFilepathForSrs(this);
			} else {
				data = new NXDetectorData(this);
			}

			if (isReadArray()) {
				readoutArrayIntoNXDetectorData(data);
			}
			data.setDoubleVals(new Double[0]);
			appendNXDetectorDataFromCollectionStrategy(data);
			appendNXDetectorDataFromFileWriter(data);
			appendNXDetectorDataFromPlugins(data);

			if (getMetaDataProvider() != null && firstReadoutInScan) {
				INexusTree nexusTree = getMetaDataProvider().getNexusTree();
				INexusTree detTree = data.getDetTree(getName());
				detTree.addChildNode(nexusTree);
			}
			return new PositionCallableData(data, isCheckFileExists());

		} catch (DeviceException e) {
			throw e;
		} catch (Exception e) {
			throw new DeviceException("Error in readout for " + getName(), e);
		} finally {
			firstReadoutInScan = false;
		}
	}

	private void readoutArrayIntoNXDetectorData(NXDetectorData data) throws Exception, DeviceException {
		int[] dims = determineDataDimensions();

		if (dims.length != 0) {
			int expectedNumPixels = dims[0];
			for (int i = 1; i < dims.length; i++) {
				expectedNumPixels = expectedNumPixels * dims[i];
			}
			Serializable dataVals;
			// TODO do only once per scan
			short dataType = ndArray.getPluginBase().getDataType_RBV();
			int nexusType;
			switch (dataType) {
			case NDPluginBase.UInt8: {
				byte[] b = new byte[] {};
				b = ndArray.getByteArrayData(expectedNumPixels);
				if (expectedNumPixels > b.length)
					throw new DeviceException("Data size is not valid");
				{
					short cd[] = new short[expectedNumPixels];
					for (int i = 0; i < expectedNumPixels; i++) {
						cd[i] = (short) (b[i] & 0xff);
					}
					dataVals = cd;
					nexusType = NexusFile.NX_INT16;
				}
			}
				break;
			case NDPluginBase.Int8: {
				byte[] b = ndArray.getByteArrayData(expectedNumPixels);
				if (expectedNumPixels > b.length)
					throw new DeviceException("Data size is not valid");
				dataVals = b;
				nexusType = NexusFile.NX_INT8;
				break;
			}
			case NDPluginBase.Int16: {
				short[] s = ndArray.getShortArrayData(expectedNumPixels);
				if (expectedNumPixels > s.length)
					throw new DeviceException("Data size is not valid length read:" + s.length + " expected:"
							+ expectedNumPixels);

				dataVals = s;
				nexusType = NexusFile.NX_INT16;
			}
				break;
			case NDPluginBase.UInt16: {
				short[] s = ndArray.getShortArrayData(expectedNumPixels);
				if (expectedNumPixels > s.length)
					throw new DeviceException("Data size is not valid length read:" + s.length + " expected:"
							+ expectedNumPixels);

				int cd[] = new int[expectedNumPixels];
				for (int i = 0; i < expectedNumPixels; i++) {
					cd[i] = (s[i] & 0xffff);
				}
				dataVals = cd;
				nexusType = NexusFile.NX_INT32;
			}
				break;
			case NDPluginBase.UInt32: // TODO should convert to INT64 if any numbers are negative
			case NDPluginBase.Int32: {
				int[] s = ndArray.getIntArrayData(expectedNumPixels);
				if (expectedNumPixels > s.length)
					throw new DeviceException("Data size is not valid length read:" + s.length + " expected:"
							+ expectedNumPixels);

				dataVals = s;
				nexusType = NexusFile.NX_INT32;
			}
				break;
			case NDPluginBase.Float32:
			case NDPluginBase.Float64: {
				float[] s = ndArray.getFloatArrayData(expectedNumPixels);
				if (expectedNumPixels > s.length)
					throw new DeviceException("Data size is not valid length read:" + s.length + " expected:"
							+ expectedNumPixels);

				dataVals = s;
				nexusType = NexusFile.NX_FLOAT32;
			}
				break;
			default:
				throw new DeviceException("Type of data is not understood :" + dataType);
			}

			data.addData(getName(), "data", dims, nexusType, dataVals, arrayDataName, 1);
			if (firstReadoutInScan) {
				for (int i = 0; i < dims.length; i++) {
					int[] axis = new int[dims[i]];
					for (int j = 1; j < dims[i]; j++) {
						axis[j - 1] = j;
					}
					data.addAxis(getName(), getName() + "_axis" + (i + 1), new int[] { axis.length },
							NexusFile.NX_INT32, axis, i + 1, 1, "pixels", false);
				}
			}
		} else {
			logger.warn("Dimensions of data from " + getName() + " is zero length");
		}
	}
	protected void appendNXDetectorDataFromCollectionStrategy(NXDetectorData data) throws Exception {

		if (isReadAcquisitionTime()) {
			double acquireTime_RBV = getCollectionStrategy().getAcquireTime(); // TODO: PERFORMANCE, cache or listen
			addDoubleItemToNXData(data, "count_time", acquireTime_RBV);
		}

		if (isReadAcquisitionPeriod()) {
			double acquirePeriod_RBV = getCollectionStrategy().getAcquirePeriod(); // TODO: PERFORMANCE, cache or listen
			addDoubleItemToNXData(data, "period", acquirePeriod_RBV);
		}
	}
	
	protected void appendNXDetectorDataFromFileWriter(NXDetectorData data) throws Exception {
		List<Double> doubleVals = new ArrayList<Double>(Arrays.asList(data.getDoubleVals()));
		if (isReadFilepath()) {
			String filename = createFileName();
			if (!StringUtils.hasLength(filename))
				throw new IllegalArgumentException("filename is null or zero length");
			// add reference to external file
			if( !getFileWriter().isLinkFilepath()){
				assert(data instanceof NXDetectorDataWithFilepathForSrs); 
				NXDetectorDataWithFilepathForSrs dataForSrs = (NXDetectorDataWithFilepathForSrs) data;

				NexusTreeNode fileNameNode = dataForSrs.addFileNames(getName(), "image_data", new String[] { filename },
						true, true);
				fileNameNode.addChildNode(new NexusTreeNode("signal", NexusExtractor.AttrClassName, fileNameNode,
						new NexusGroupData(1)));
				// add filename as an NXNote
				dataForSrs.addFileName(getName(), filename);
				int indexOf = Arrays.asList(getExtraNames()).indexOf(FILEPATH_EXTRANAME);
				dataForSrs.setFilepathOutputFieldIndex(indexOf);

				doubleVals.add(0.); // this is needed as we have added an entry in extraNames

			}else {
				if (firstReadoutInScan) {
					data.addScanFileLink(getName(), "nxfile://" + filename + "#entry/instrument/detector/data");
				}
			}
			data.setDoubleVals(doubleVals.toArray(new Double[] {}));
		}

	}
	protected void appendNXDetectorDataFromPlugins(NXDetectorData data) throws Exception {

		if (isComputeStats()) {
			Double[] currentDoubleVals = statsGroup.getCurrentDoubleVals();
			addMultipleDoubleItemsToNXData(data, statsGroup.getFieldNames(), currentDoubleVals);
		}

		if (isComputeCentroid()) {
			Double[] currentDoubleVals = centroidGroup.getCurrentDoubleVals();
			addMultipleDoubleItemsToNXData(data, centroidGroup.getFieldNames(), currentDoubleVals);
		}

	}
}

class PositionCallableData{

	final NXDetectorData data;
	boolean checkFileExists=false;

	public PositionCallableData(NXDetectorData data, boolean checkFileExists) {
		this.data = data;
		this.checkFileExists = checkFileExists;
	}
	
}

class ADDetectorPositionCallable implements Callable<NexusTreeProvider> {

	private final PositionCallableData data;
	ADDetectorPositionCallable(PositionCallableData data){
		this.data = data;
		
	}
	@Override
	public NexusTreeProvider call() throws Exception {
		//if file given then check its existence
		NXDetectorData detectorData = data.data;
		if( detectorData instanceof NXDetectorDataWithFilepathForSrs){
			String filepath = ((NXDetectorDataWithFilepathForSrs)detectorData).getFilepath();
			if( data.checkFileExists && !filepath.equals(ADDetector.DummyFileWriter.DUMMY_FILE_WRITER_GET_FULL_FILE_NAME_RBV)){
				File f = new File(filepath);
				long numChecks=0;
				while( !f.exists() ){
					numChecks++;
					Thread.sleep(1000);
					ScanBase.checkForInterrupts();
					if( numChecks> 10){
						//Inform user every 10 seconds
						InterfaceProvider.getTerminalPrinter().print("Waiting for file " + filepath + " to be created");
						numChecks=0;
					}
				}
			}
		}
		return detectorData;
	}

}