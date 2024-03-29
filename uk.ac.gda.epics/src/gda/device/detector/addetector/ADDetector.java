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

import static gda.device.detector.nexusprocessor.SwmrHdfDatasetProviderProcessor.FRAME_NO_DATASET_NAME;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Vector;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.StringUtils;

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
import gda.device.detector.addetector.filewriter.FileWriterBase;
import gda.device.detector.addetector.filewriter.NonAsyncSingleImagePerFileWriter;
import gda.device.detector.addetector.filewriter.SingleImagePerFileWriter;
import gda.device.detector.addetector.triggering.SimpleAcquire;
import gda.device.detector.areadetector.NDStatsGroup;
import gda.device.detector.areadetector.NDStatsGroupFactory;
import gda.device.detector.areadetector.v17.ADBase;
import gda.device.detector.areadetector.v17.NDArray;
import gda.device.detector.areadetector.v17.NDFile;
import gda.device.detector.areadetector.v17.NDOverlaySimple;
import gda.device.detector.areadetector.v17.NDPython;
import gda.device.detector.areadetector.v17.NDStats;
import gda.device.detector.nxdata.NXDetectorDataAppender;
import gda.device.detector.nxdata.NXDetectorDataNullAppender;
import gda.device.detector.nxdetector.NXCollectionStrategyPlugin;
import gda.device.detector.nxdetector.NXFileWriterPlugin;
import gda.device.scannable.PositionCallableProvider;
import gda.factory.FactoryException;
import gda.jython.InterfaceProvider;
import gda.scan.Scan;
import gda.scan.ScanInformation;

/**
 * A {@link Scannable} {@link Detector} driver for Epics Area Detectors suitable for use within {@link Scan}s that support detectors implementing
 * {@link PositionCallableProvider}.
 * </p>
 * <p>
 * The {@link NexusTreeProvider} returned by the call method of the PositionCallableProvider implements {@link GDANexusDetectorData} which provides plottable
 * and printable data as well as binary detector data and detector metadata. If configured to return filepaths the object supports
 * {@link NXDetectorDataWithFilepathForSrs} which will result in the filepath being returned as the first Scannable extra field and printed to the terminal and
 * SRS files.
 * </p>
 * The Epics AreaDetector software is very modular and this is reflected in the structure of this class. As far as ADDetector is concerned an EPICS AreaDetector
 * consists of 2 or 3 parts:
 * <ol>
 * <li>A Base plugin that supports the source of the data, e.g. camera, The base is used to setup the acquisition, exposure time trigger mode, and start/stop
 * acquisition. The result of an acquisition is a chunk of binary data.</li>
 * <li>A NDArray plugin that makes the binary data available over channel access.</li>
 * <li>A file writer plugin that will write the binary data to a file of some format. Either 1 file per acquisition or 1 file per collection of acquisitions.</li>
 * </ol>
 * This camera can be used by GDA in 2 different modes:
 * <ol>
 * <li>Camera base + PV</li>
 * <li>Camera base + file writer</li>
 * </ol>
 * This structure is represented in ADDetector by 3 main components:
 * <ul>
 * <li>{@link NXCollectionStrategyPlugin} - used to handle the camera base</li>
 * <li>{@link NDArray} - handles the PV to read the binary data</li>
 * <li>{@link NXFileWriterPlugin} - used to handle the file writer plugin</li>
 * </ul>
 * The result of {@link #getPositionCallable()} is the creation of an object that has sufficient information to allow creation of a {@link NexusTreeProvider} in
 * its call method. This object can take data from:
 * <ol>
 * <li>The NDArray object if present and selected</li>
 * <li>An NDStats, that represents the NDStats plugin, if present and selected</li>
 * <li>The ADTriggeringStrategy object if selected</li>
 * <li>The FileWriter if selected</li>
 * <li>Another NexusTreeProvider that provides meta data if present.</li>
 * </ol>
 */
public class ADDetector extends DetectorBase implements InitializingBean, NexusDetector, PositionCallableProvider<NexusTreeProvider> {

	public class NullFileWriter implements NXFileWriterPlugin {

		static final String DUMMY_FILE_WRITER_GET_FULL_FILE_NAME_RBV = "DummyFileWriter - getFullFileName_RBV";

		@Override
		public void prepareForCollection(int numberImagesPerCollection, ScanInformation scanInfo) throws Exception {
		}

		@Override
		public boolean appendsFilepathStrings() {
			return true;
		}

		@Override
		public void stop() throws Exception {
		}

		@Override
		public void atCommandFailure() throws Exception {
		}

		@Override
		public String toString() {
			return "DummyFileWriter";
		}

		@Override
		public String getFullFileName() throws Exception {
			return DUMMY_FILE_WRITER_GET_FULL_FILE_NAME_RBV;
		}

		@Override
		public String getName() {
			return "filewriter";
		}

		@Override
		public boolean willRequireCallbacks() {
			return false;
		}

		@Override
		public void prepareForLine() throws Exception {
		}

		@Override
		public void completeLine() throws Exception {
		}

		@Override
		public void completeCollection() throws Exception {
		}

		@Override
		public List<String> getInputStreamNames() {
			return Arrays.asList();
		}

		@Override
		public List<String> getInputStreamFormats() {
			return Arrays.asList();
		}

		@Override
		public Vector<NXDetectorDataAppender> read(int maxToRead) throws NoSuchElementException, InterruptedException,
				DeviceException {
			Vector<NXDetectorDataAppender> appenders = new Vector<NXDetectorDataAppender>();
			appenders.add(new NXDetectorDataNullAppender());
			return appenders;
		}


	}

	private static final String[] A = new String[] {};

	protected static final String FILEPATH_EXTRANAME = "filepath";

	private static Logger logger = LoggerFactory.getLogger(ADDetector.class);

	private ADBase adBase;

	private NDStats ndStats;

	private NDArray ndArray;

	private NDFile ndFile;

	private NDPython ndPython;

	private List<NDOverlaySimple> ndOverlays;

	private NXCollectionStrategyPlugin collectionStrategy;

	private String description = "ADDetector";

	private String detectorType = "ADDetector";

	private boolean computeStats = false;

	private boolean computeCentroid = false;

	private boolean readArray = true;

	private boolean readAcquisitionTime = true;

	private boolean readAcquisitionPeriod = false;

	private boolean readFilepath = false;

	/** At each point record (in NXDetectorData) the total frames captured so far from the filewriter plugin */
	private boolean readCapturedFramesCount = false;

	/**
	 * Controls whether the driver does callbacks with the array data to registered plugins. 0=No, 1=Yes. Setting this
	 * to 0 can reduce overhead in the case that the driver is being used only to control the device, and not to make
	 * the data available to plugins or to EPICS clients.
	 */
	private boolean disableCallbacks = false;

	private NDStatsGroup statsGroup;

	private NDStatsGroup centroidGroup;

	public void setCollectionStrategy(NXCollectionStrategyPlugin collectionStrategy) {
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

	public void setNdPython(NDPython ndPython) {
		this.ndPython = ndPython;
	}

	public void setNdOverlays(List<NDOverlaySimple> ndOverlays) {
		this.ndOverlays = ndOverlays;
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

	public NXCollectionStrategyPlugin getCollectionStrategy() {
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

	public NDPython getNdPython() {
		return ndPython;
	}

	public List<NDOverlaySimple> getNdOverlays() {
		return ndOverlays;
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
	 * If true && !filewriter.isLinkedPath report filepath in readout If true && filewriter.isLinkedPath add external
	 * link
	 */
	public boolean isReadFilepath() {
		return readFilepath;
	}

	@Override
	public boolean createsOwnFiles() throws DeviceException {
		return false; // always return nexus data
	}

	protected boolean afterPropertiesSetCalled = false;

	// Controls when to put non point specific items in the nexusdetectordata
	protected boolean firstReadoutInScan = true;

	// provides metadata about the detector. This is added as a child of the detector element of the tree
	private NexusTreeProvider metaDataProvider;

	private String detectorID;

	public NexusTreeProvider getMetaDataProvider() {
		return metaDataProvider;
	}

	public void setMetaDataProvider(NexusTreeProvider metaDataProvider) {
		this.metaDataProvider = metaDataProvider;
	}

	private NXFileWriterPlugin fileWriter;

	public NXFileWriterPlugin getFileWriter() {
		return fileWriter;
	}

	public void setFileWriter(NXFileWriterPlugin fileWriter) {
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
			if (ndFile != null) {
				SingleImagePerFileWriter fileW = new NonAsyncSingleImagePerFileWriter(getName());
				fileW.setWriteErrorStatusSupported(false);
				fileW.setNdFile(ndFile);
				fileW.setEnabled(true);
				fileW.afterPropertiesSet();
				fileWriter = fileW;
			} else
				fileWriter = new NullFileWriter();
		}
		afterPropertiesSetCalled = true;
	}

	@Override
	public void configure() throws FactoryException {
		if (isConfigured()) {
			return;
		}
		super.configure();
		if (!afterPropertiesSetCalled)
			throw new FactoryException("afterPropertiesSet not yet called");
		if (!StringUtils.hasLength(getName()))
			throw new FactoryException("name is not defined");

		try {
			createStatsGroups();
			setInputNames(A);
			configureExtraNamesAndOutputFormat();
			checkPythonPlugin();
			reset();
		} catch (Exception e) {
			throw new FactoryException(String.format("Configuring %s failed! Device may not work as expected", getName()), e);
		}
		setConfigured(true);
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

	private void checkPythonPlugin() throws Exception {
		if (ndPython != null) {
			logger.info("Checking Python plugin for {}", getName());
			final int status = ndPython.getStatus();
			if (status != 0) {
				throw new IllegalStateException(String.format("Python plugin is in error state %d", status));
			}
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
		if (isReadFilepath() && getFileWriter().appendsFilepathStrings()) {
			extraNames.add(FILEPATH_EXTRANAME);
			// used to format the double that is put into the doubleVals array in this case
			formats.add("%.2f");
		}
		if (isReadCapturedFramesCount()) {
			formats.add("%.0f");
			extraNames.add(FRAME_NO_DATASET_NAME);
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
		throw new DeviceException("ADDetector does not support operation through its Scannable interface.");
	}

	@Override
	public boolean isBusy() throws DeviceException {
		return getStatus() == BUSY;
	}

	@Override
	public Object getPosition() throws DeviceException {
		throw new RuntimeException("ADDetector does not support operation through its Scannable interface.");
	}

	@Override
	final public void prepareForCollection() throws DeviceException {
	}

	@Override
	public void atScanStart() throws DeviceException {
		ScanInformation scanInfo = InterfaceProvider.getCurrentScanInformationHolder().getCurrentScanInformation();
		firstReadoutInScan = true;
		try {
			// disable or enable callbacks if required by class variable first to allow collectionStrategy or fileWriter
			// to turn back on needed
			getAdBase().setArrayCallbacks(isDisableCallbacks() ? 0 : 1);
			int numberImagesPerCollection = getCollectionStrategy().getNumberImagesPerCollection(getCollectionTime());
			getCollectionStrategy().prepareForCollection(getCollectionTime(), 1, null);
			if (isReadFilepath()) {
				getFileWriter().prepareForCollection(numberImagesPerCollection, scanInfo);
			}
			prepareForArrayAndStatsCollection();
		} catch (Exception e) {
			throw new DeviceException(e);
		}
	}

	@Override
	public void collectData() throws DeviceException {
		latestPositionCallable = null;
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
			if (latestPositionCallable != null) {
				// do not close down plugins as the callable could involve waiting for the last file to arrive
				// on disk
				latestPositionCallable.call(); // TODO: Should not be needed!
			}
			if (isReadFilepath()) {
				getFileWriter().completeCollection();
			}
			getCollectionStrategy().completeCollection();
		} catch (Exception e) {
			throw new DeviceException(e);
		}
	}

	@Override
	public void stop() throws DeviceException {
		try {
			if (isReadFilepath()) {
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
			if (isReadFilepath()) {
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

	/*
	 * To allow multiple calls to getPositionCallable for the same point hold onto the result of the first call. Clear
	 * at atPointStart
	 */
	private ADDetectorPositionCallable latestPositionCallable = null;

	protected void clearCachedCallable() {
		latestPositionCallable = null;
	}

	protected void addDoubleItemToNXData(NXDetectorData data, String name, Double val) {
		INexusTree valdata = data.addData(getName(), name, new NexusGroupData(val));
		valdata.addChildNode(new NexusTreeNode("local_name",NexusExtractor.AttrClassName, valdata, new NexusGroupData(String.format("%s.%s",  getName(), name))));
		data.setPlottableValue(name, val);
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
			throw new DeviceException("Error in readout of " + getName(), e);
		}
	}

	protected String createFileName() throws Exception {
		return getFileWriter().getFullFileName();
	}

	public void setDisableCallbacks(boolean disableCallbacks) {
		this.disableCallbacks = disableCallbacks;
	}

	public boolean isDisableCallbacks() {
		return disableCallbacks;
	}

	/*
	 * If false getPositionCallable will return an object that simply returns the NexusProviderTree inside it with no
	 * delay waiting for it to be valid ( no file existence checking) If true the getPositionCallable will return an
	 * object with a call method will only return a NExusTreeProvider once it is valid if checkFileExists is true.
	 * Default = false.
	 */
	boolean usePipeline = false;

	boolean checkFileExists = false;

	private int[] dataChunking;

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
		Callable<NexusTreeProvider> thisCallable = null;
		if (latestPositionCallable == null) {
			latestPositionCallable = new ADDetectorPositionCallable(getPositionCallableData());
		}
		thisCallable = latestPositionCallable;

		if (usePipeline) {
			return thisCallable;
		}
		try {
			final NexusTreeProvider treeProvider = thisCallable.call();
			return new Callable<NexusTreeProvider>() {
				@Override
				public NexusTreeProvider call() throws Exception {
					return treeProvider;
				}

			};
		} catch (Exception e) {
			throw new DeviceException("Error getting data", e);
		}
	}

	private PositionCallableData getPositionCallableData() throws DeviceException {
		try {
			NXDetectorData data = createNXDetectorData();

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

	protected NXDetectorData createNXDetectorData() throws Exception, DeviceException {
		NXDetectorData data;
		if (isReadFilepath() && getFileWriter().appendsFilepathStrings()) {
			data = new NXDetectorDataWithFilepathForSrs(this);
		} else {
			data = new NXDetectorData(this);
		}
		if (isReadArray()) {
			readoutArrayIntoNXDetectorData(data, ndArray, getName());
		}

		appendDataAxes(data);
		// data.setDoubleVals(new Double[0]);
		appendNXDetectorDataFromCollectionStrategy(data);
		appendNXDetectorDataFromFileWriter(data);
		appendNXDetectorDataFromPlugins(data);
		return data;
	}

	/**
	 * This stub adds rather stupid axes. Overriding classes are highly encouraged to do better.
	 *
	 * @param data
	 * @throws Exception
	 */
	@SuppressWarnings("unused")
	protected void appendDataAxes(NXDetectorData data) throws Exception {
		if (firstReadoutInScan) {
			for (int i = 0; i < dims.length; i++) {
				int[] axis = new int[dims[i]];
				for (int j = 1; j < dims[i]; j++) {
					axis[j - 1] = j;
				}
				data.addAxis(getName(), getName() + "_axis" + (i + 1), new NexusGroupData(axis), i + 1, 1, "pixels", false);
			}
		}
	}

	protected void readoutArrayIntoNXDetectorData(NXDetectorData data, NDArray ndArray, String detectorName)
			throws Exception, DeviceException {
		NexusGroupData ngd = ArrayData.readArrayData(ndArray);
		ngd.isDetectorEntryData = true;
		if (dataChunking != null) {
			ngd.chunkDimensions = dataChunking;
		}
		data.addData(detectorName, "data", ngd, null, 1);
	}

	public int[] getDataChunking() {
		return dataChunking;
	}

	public void setDataChunking(int[] dataChunking) {
		this.dataChunking = dataChunking;
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

	private void appendNXDetectorDataFromFileWriter(NXDetectorData data) throws Exception {
		if (isReadFilepath()) {
			String filename = createFileName();
			if (!StringUtils.hasLength(filename))
				throw new IllegalArgumentException("filename is null or zero length");
			// add reference to external file
			if (getFileWriter().appendsFilepathStrings()) {
				assert (data instanceof NXDetectorDataWithFilepathForSrs);
				NXDetectorDataWithFilepathForSrs dataForSrs = (NXDetectorDataWithFilepathForSrs) data;

				NexusTreeNode fileNameNode = dataForSrs.addFileNames(getName(), "image_data",
						new String[] { filename }, true, true);
				fileNameNode.addChildNode(new NexusTreeNode("signal", NexusExtractor.AttrClassName, fileNameNode,
						new NexusGroupData(1)));
				// add filename as an NXNote
				dataForSrs.addFileName(getName(), filename);
				int indexOf = Arrays.asList(getExtraNames()).indexOf(FILEPATH_EXTRANAME);
				dataForSrs.setFilepathOutputFieldIndex(indexOf);
				data.setPlottableValue(FILEPATH_EXTRANAME, 0.0); // we have a test that checks for this pointless zero,
																	// so it must be important
			} else {
				if (firstReadoutInScan) {
					data.addExternalFileLink(getName(), "data", "nxfile://" + filename + "#entry/instrument/detector/data", getNumDimensions());
				}
			}
		}

		if (readCapturedFramesCount) {
			int frameNo = getNdFile().getNumCaptured_RBV();
			NexusGroupData frameGrp =  new NexusGroupData(frameNo);
			frameGrp.isDetectorEntryData = true;
			data.addData(getName(), FRAME_NO_DATASET_NAME, frameGrp, null, null, null, true);
			data.setPlottableValue(FRAME_NO_DATASET_NAME, (double) frameNo);
		}

	}

	private int getNumDimensions() throws Exception {
		if (getNdFile() != null) {
			return getNdFile().getPluginBase().getNDimensions_RBV();
		} else if (getFileWriter() instanceof FileWriterBase) {
			return ((FileWriterBase) getFileWriter()).getNdFile().getPluginBase().getNDimensions_RBV();
		}
		// We don't know - default in NexusGroupData
		return -1;
	}

	private void appendNXDetectorDataFromPlugins(NXDetectorData data) throws Exception {
		if (isComputeStats()) {
			Double[] currentDoubleVals = statsGroup.getCurrentDoubleVals();
			addMultipleDoubleItemsToNXData(data, statsGroup.getFieldNames(), currentDoubleVals);
		}

		if (isComputeCentroid()) {
			Double[] currentDoubleVals = centroidGroup.getCurrentDoubleVals();
			addMultipleDoubleItemsToNXData(data, centroidGroup.getFieldNames(), currentDoubleVals);
		}
	}

	public boolean isReadCapturedFramesCount() {
		return readCapturedFramesCount;
	}

	public void setReadCapturedFramesCount(boolean readCapturedFramesCount) {
		this.readCapturedFramesCount = readCapturedFramesCount;
		configureExtraNamesAndOutputFormat();
	}
}

class PositionCallableData {

	final NXDetectorData data;
	boolean checkFileExists = false;

	public PositionCallableData(NXDetectorData data, boolean checkFileExists) {
		this.data = data;
		this.checkFileExists = checkFileExists;
	}
}

class ADDetectorPositionCallable implements Callable<NexusTreeProvider> {

	private final PositionCallableData data;

	ADDetectorPositionCallable(PositionCallableData data) {
		this.data = data;
	}

	@Override
	public NexusTreeProvider call() throws Exception {
		// if file given then check its existence
		NXDetectorData detectorData = data.data;
		if (detectorData instanceof NXDetectorDataWithFilepathForSrs) {
			String filepath = ((NXDetectorDataWithFilepathForSrs) detectorData).getFilepath();
			if (data.checkFileExists
					&& !filepath.equals(ADDetector.NullFileWriter.DUMMY_FILE_WRITER_GET_FULL_FILE_NAME_RBV)) {
				File f = new File(filepath);
				long numChecks = 0;
				while (!f.exists()) {
					numChecks++;
					Thread.sleep(1000);
					if (numChecks > 10) {
						// Inform user every 10 seconds
						InterfaceProvider.getTerminalPrinter().print("Waiting for file " + filepath + " to be created");
						numChecks = 0;
					}
				}
			}
		}
		return detectorData;
	}
}
