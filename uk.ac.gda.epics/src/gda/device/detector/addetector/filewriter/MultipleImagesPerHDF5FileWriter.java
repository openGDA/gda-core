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

package gda.device.detector.addetector.filewriter;

import java.io.File;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import gda.data.fileregistrar.FileRegistrarHelper;
import gda.device.DeviceException;
import gda.device.detector.EpicsProcessVariableCollection;
import gda.device.detector.NXDetectorData;
import gda.device.detector.areadetector.v17.NDFile;
import gda.device.detector.areadetector.v17.NDFile.FileWriteMode;
import gda.device.detector.areadetector.v17.NDFileHDF5;
import gda.device.detector.nxdata.NXDetectorDataAppender;
import gda.device.detector.nxdata.NXDetectorDataChildNodeAppender;
import gda.device.detector.nxdata.NXDetectorDataFileLinkAppender;
import gda.device.detector.nxdata.NXDetectorDataNullAppender;
import gda.device.detector.nxdata.NXDetectorDataStringAppender;
import gda.device.detector.nxdata.NXDetectorSerialAppender;
import gda.device.detector.nxdetector.FrameCountingNXPlugin;
import gda.jython.InterfaceProvider;
import gda.scan.ScanInformation;
import gov.aps.jca.TimeoutException;

public class MultipleImagesPerHDF5FileWriter extends FileWriterBase implements FrameCountingNXPlugin {

	private static Logger logger = LoggerFactory.getLogger(MultipleImagesPerHDF5FileWriter.class);

	private List<EpicsProcessVariableCollection> pvsToCollectMaps = Collections.emptyList();

	private NDFileHDF5 ndFileHDF5;

	private boolean blocking = true;

	private int expectedFrameCount = 0;

	/*
	 * default chunking is off so we get 1 image per chunk
	 */
	private int rowChunks = 0;
	private int colChunks=0;
	private int framesChunks=1;
	private int framesFlush=1;

	protected boolean firstReadoutInScan;
	private String name;

	@Override // interface NXPluginBase
	public String getName() {
		return name != null && !name.isEmpty() ? name : "hdfwriter";
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override // class FileWriterBase
	public void setNdFile(NDFile ndFile) {
		throw new RuntimeException("Configure ndFileHDF5 instead of ndFile");
	}

	public void setNdFileHDF5(NDFileHDF5 ndFileHDF5) {
		this.ndFileHDF5 = ndFileHDF5;
		super.setNdFile(ndFileHDF5.getFile());
	}

	public NDFileHDF5 getNdFileHDF5() {
		return ndFileHDF5;
	}

	public int getColChunks() {
		return colChunks;
	}

	public void setColChunks(int colChunks) {
		this.colChunks = colChunks;
	}

	public int getFramesChunks() {
		return framesChunks;
	}

	public void setFramesChunks(int framesChunks) {
		this.framesChunks = framesChunks;
	}

	public int getFramesFlush() {
		return framesFlush;
	}

	public void setFramesFlush(int framesFlush) {
		this.framesFlush = framesFlush;
	}

	@Override // interface InitializingBean
	public void afterPropertiesSet() throws Exception {
		if (ndFileHDF5 == null)
			throw new IllegalStateException("ndFileHDF5 is null");
		super.afterPropertiesSet();
	}

	public boolean isBlocking() {
		return blocking;
	}

	/**
	 *
	 * @param blocking If true(default) the file plugin is blocking. It is better to pause the scan someother way than rely on teh buffre which can overrun anyway
	 */
	public void setBlocking(boolean blocking) {
		this.blocking = blocking;
	}

	public int getRowChunks() {
		return rowChunks;
	}

	public void setRowChunks(int rowChunks) {
		this.rowChunks = rowChunks;
	}

	boolean setChunking=true;

	private boolean storeAttr=false;

	private boolean attrByDim = false;

	private boolean attrByDimSupported = false;

	private boolean storePerform=false;

	private boolean alreadyPrepared=false; //use to allow the same fileWriter to be used in the same multiscan
	private boolean alwaysPrepare=false; // Set to true to disable alreadyPrepared mechanism

	private boolean lazyOpen=false;
	private Optional<Boolean> swmrModeEnabled=Optional.empty();

	public boolean isLazyOpen() {
		return lazyOpen;
	}

	/**
	 *
	 * @param lazyOpen If true the HDF5 plugin supports LazyOpen and set it to 1
	 */
	public void setLazyOpen(boolean lazyOpen) {
		this.lazyOpen = lazyOpen;
	}

	private Integer boundaryAlign=null;

	protected String expectedFullFileName;

	protected int numToBeCaptured;

	protected int numCaptured;

	private Double xPixelSize=null;

	private Double yPixelSize=null;

	private String xPixelSizeUnit=null;

	private String yPixelSizeUnit=null;

	protected int dataRank;

	public Integer getBoundaryAlign() {
		return boundaryAlign;
	}

	/**
	 *
	 * @param boundaryAlign value for BounaryAlign PV. Default is null in which case it is not set.
	 * This was added in version 1-9 of areaDetector
	 */
	public void setBoundaryAlign(Integer boundaryAlign) {
		this.boundaryAlign = boundaryAlign;
	}

	public boolean isSetChunking() {
		return setChunking;
	}

	public void setSetChunking(boolean setChunking) {
		this.setChunking = setChunking;
	}

	public boolean isStoreAttr() {
		return storeAttr;
	}

	/**
	 *
	 * @param storeAttr if true the hdf5 plugin stores metadata in image file
	 */
	public void setStoreAttr(boolean storeAttr) {
		this.storeAttr = storeAttr;
	}

	public boolean isAttrByDim() {
		return attrByDim;
	}

	public void setAttrByDim(boolean attrByDim) {
		this.attrByDim = attrByDim;
	}

	public boolean isAttrByDimSupported() {
		return attrByDimSupported;
	}

	public void setAttrByDimSupported(boolean attrByDimSupported) {
		this.attrByDimSupported = attrByDimSupported;
	}

	public boolean isStorePerform() {
		return storePerform;
	}

	/**
	 *
	 * @param storePerform if true the hdf5 plugin stores performance data in image file
	 */
	public void setStorePerform(boolean storePerform) {
		this.storePerform = storePerform;
	}

	@Override // interface NXPluginBase
	public void prepareForCollection(int numberImagesPerCollection, ScanInformation scanInfo) throws Exception {
		logger.trace("prepareForCollection({}, {}) isEnabled={}, alreadyPrepared={}, alwaysPrepare={}", numberImagesPerCollection, scanInfo, isEnabled(), alreadyPrepared, alwaysPrepare);
		logStackTrace(logger, "prepareForCollection(...)");

		if(!isEnabled())
			return;

		/* The alreadyPrepared optimisation prevents SwitchableHardwareTriggerableProcessingDetectorWrapper from
		 * reconfiguring the detector when switching between hardware_triggered_detector and detector_for_snaps
		 * detectors, so the alwaysPrepare property enables this mechanism to be disabled on a per detector basis.
		 */
		if (alreadyPrepared && !alwaysPrepare) {
			return;
		}

		setNDArrayPortAndAddress();
		getNdFile().getPluginBase().disableCallbacks();
		getNdFile().getPluginBase().setBlockingCallbacks(blocking ? 1:0); //use camera memory
		getNdFileHDF5().setStoreAttr(storeAttr? 1:0);
		getNdFileHDF5().setStorePerform(storePerform?1:0);
		if(swmrModeEnabled.isPresent()) {
			getNdFileHDF5().setUseSWMR(swmrModeEnabled.get());
		}
		// save attributes with correct dimensions, add this option as not available in all beamlines yet
		if (isAttrByDimSupported())
			getNdFileHDF5().setStoreAttributesByDimension(attrByDim);
		if( lazyOpen)
			getNdFileHDF5().setLazyOpen(true);
		if( boundaryAlign != null)
			getNdFileHDF5().setBoundaryAlign(boundaryAlign);
		getNdFile().setFileWriteMode(FileWriteMode.STREAM);
		ScanInformation scanInformation = InterfaceProvider.getCurrentScanInformationHolder().getCurrentScanInformation();
		//if not scan setup then act as if this is a 1 point scan
		setScanDimensions(scanInformation == null ? new int []{1}: scanInformation.getDimensions(), numberImagesPerCollection);
		if( isSetFileNameAndNumber()){
			setupFilename();
		}
		deriveFullFileName();
		clearWriteStatusErr();
		resetCounters();
		startRecording();
		getNdFile().getPluginBase().enableCallbacks();
		dataRank = getNdFile().getPluginBase().getNDimensions_RBV();
		firstReadoutInScan = true;
		alreadyPrepared=true;

		logger.trace("...prepareForCollection()");
	}

	protected void deriveFullFileName() throws Exception {
		expectedFullFileName = String.format(getNdFile().getFileTemplate_RBV(), getNdFile().getFilePath_RBV(), getNdFile().getFileName_RBV(), getNdFile().getFileNumber_RBV());
	}

	private void setScanDimensions(int[] dimensions, int numberImagesPerCollection) throws Exception {
		int [] actualDims = dimensions;
		if( numberImagesPerCollection > 1){
			actualDims = Arrays.copyOf(dimensions, dimensions.length+1);
			actualDims[dimensions.length] = numberImagesPerCollection;
		}
		getNdFileHDF5().setExtraDimensions(actualDims);
		int numberOfAcquires=1;
		for( int dim : actualDims ){
			numberOfAcquires *= dim;
		}
		expectedFrameCount = numberOfAcquires;
		getNdFileHDF5().setNumCapture(numberOfAcquires);
		if( isSetChunking()){
			getNdFileHDF5().setNumRowChunks(rowChunks);
			getNdFileHDF5().setNumColChunks(colChunks);
			getNdFileHDF5().setNumFramesChunks(framesChunks);
			getNdFileHDF5().setNumFramesFlush(framesFlush);
		}
	}

	private void setupFilename() throws Exception {
		getNdFile().setFileName(getFileName());
		getNdFile().setFileTemplate(getFileTemplate());
		String filePath = getFilePath();

		if (!filePath.endsWith(File.separator))
			filePath += File.separator;
		File f = new File(filePath);
		if (!f.exists()) {
			if (!f.mkdirs())
				throw new Exception("Folder does not exist and cannot be made:" + filePath);
		}

		getNdFile().setFilePath(filePath);
		if( !getNdFile().filePathExists())
			if (isPathErrorSuppressed())
				logger.warn("Ignoring Path does not exist on IOC '" + filePath + "'");
			else
				throw new Exception("Path does not exist on IOC '" + filePath + "'");
		long scanNumber = getScanNumber();

		getNdFile().setFileNumber((int)scanNumber);
		getNdFile().setAutoSave((short) 0);
		getNdFile().setAutoIncrement((short) 0);

	}

	private void startRecording() throws Exception {
		logStackTrace(logger, "startRecording()");
		//if (getNdFileHDF5().getCapture() == 1)
			//	throw new DeviceException("detector found already saving data when it should not be");

		getNdFileHDF5().startCapture();
		int totalmillis = 60 * 1000;
		int grain = 25;
		for (int i = 0; i < totalmillis/grain; i++) {
			if (getNdFileHDF5().getCapture_RBV() == 1) {
				logger.trace("...startRecording()");
				return;
			}
			Thread.sleep(grain);
		}
		throw new TimeoutException("Timeout waiting for hdf file creation.");
	}


	private void resetCounters() throws Exception {
		getNdFile().getPluginBase().setDroppedArrays(0);
		getNdFile().getPluginBase().setArrayCounter(0);
	}

	@Override // class FileWriterBase
	public void disableFileWriting() throws Exception {
		getNdFile().getPluginBase().disableCallbacks();
		getNdFile().getPluginBase().setBlockingCallbacks((short) 0);
//		getNdFile().setFileWriteMode(FileWriteMode.STREAM);
	}


	@Override // interface NXPluginBase
	public void completeLine() throws Exception {
		logStackTrace(logger, "completeLine()");
		super.completeLine();
	}

	@Override
	public void completeLine(int framesCollected) throws Exception {
		logger.trace("completeLine({})", framesCollected);
		completeLine();
	}

	@Override
	public void completeCollection() throws Exception{
		logger.trace("completeCollection()");
		completeCollection(expectedFrameCount);
	}

	@Override
	public void completeCollection(int framesCollected) throws Exception {
		logger.trace("completeCollection({})", framesCollected);
		logStackTrace(logger, "completeCollection(...)");
		expectedFrameCount = framesCollected;
		alreadyPrepared=false;
		if(!isEnabled())
			return;
		FileRegistrarHelper.registerFile(expectedFullFileName);
		endRecording();
		disableFileWriting();

		logger.trace("...completeCollection()");
	}

	private void endRecording() throws Exception {
		logStackTrace(logger, "endRecording()");
		LogLimiter logLimiter = new LogLimiter(Duration.ofSeconds(10), true);

		short capture_RBV;
		long numCaptured_RBV;

		while (true) {
			capture_RBV = getNdFileHDF5().getFile().getCapture_RBV();
			numCaptured_RBV = getNdFileHDF5().getFile().getNumCaptured_RBV();

			// When streaming with setNumCapture set to -1 (forever) the HDF5 plugin will not stop itself, so we have to also
			// check whether we have captured as many frames as we expected.
			if (capture_RBV == 0) {
				logger.trace("endRecording() ended as the file writer stopped itself");
				break;
			} else if (numCaptured_RBV == expectedFrameCount ) {
				logger.trace("endRecording() ended as the file writer captured as many frames as we expected");
				break;
			} else if (numCaptured_RBV > expectedFrameCount ) {
				if (expectedFrameCount == 0) {
					logger.debug("endRecording() ended as file writer captured frames but we weren't told how many to expect!");
				}else {
					logger.warn("endRecording() ended as file writer captured more frames than we expected!");
				}
				break;
			}

			if (logLimiter.isLogDue()) {
				logger.warn("endRecording() blocked for {} seconds, Capture_RBV={}, NumCaptured_RBV={}, expectedFrameCount={}",
						logLimiter.getTimeSinceStart().getSeconds(), capture_RBV, numCaptured_RBV, expectedFrameCount);
			}
			Thread.sleep(1000);
		}

		getNdFileHDF5().stopCapture();

		logger.trace("...endRecording() stopped capture, Capture_RBV was {} now {}, NumCaptured_RBV={}, expectedFrameCount={}",
				capture_RBV, getNdFileHDF5().getFile().getCapture_RBV(), numCaptured_RBV, expectedFrameCount);

		if (getNdFileHDF5().getFile().getPluginBase().getDroppedArrays_RBV() > 0)
			throw new DeviceException("sorry, we missed some frames");
	}

	@Override // interface NXFileWriterPlugin
	public boolean appendsFilepathStrings() {
		return false;
	}

	@Override // interface NXPluginBase
	public void stop() throws Exception {
		logStackTrace(logger, "stop()");
		alreadyPrepared=false;
		if(!isEnabled())
			return;
		getNdFileHDF5().stopCapture();

	}

	@Override // interface NXPluginBase
	public void atCommandFailure() throws Exception {
		logStackTrace(logger, "atCommandFailure()");
		alreadyPrepared=false;
		if(!isEnabled())
			return;
		stop();
	}

	@Override // interface NXPluginBase
	public List<String> getInputStreamNames() {
		return Arrays.asList();
	}

	@Override // interface NXPluginBase
	public List<String> getInputStreamFormats() {
		return Arrays.asList();
	}

	@Override // interface PositionInputStream<T>
	public Vector<NXDetectorDataAppender> read(int maxToRead) throws NoSuchElementException, InterruptedException,
			DeviceException {
		logger.trace("read({}), firstReadoutInScan={}, numCaptured={}, numToBeCaptured={}", maxToRead, firstReadoutInScan, numCaptured, numToBeCaptured);
		logStackTrace(logger, "read(...)");
		NXDetectorDataAppender dataAppender;
		//wait until the NumCaptured_RBV is equal to or exceeds maxToRead.
		if (isEnabled()) {
			checkErrorStatus();
		}
		try {
			getNdFile().getPluginBase().checkDroppedFrames();
		} catch (Exception e) {
			throw new DeviceException("Error in " + getName(), e);
		}
		if (firstReadoutInScan) {
			dataAppender = createFileLinkAppender();
			numToBeCaptured=1;
			numCaptured=0;
		}
		else {
			dataAppender = new NXDetectorDataNullAppender();
			numToBeCaptured++;
		}
		logger.debug("firstReadoutInScan={}, numCaptured={}, numToBeCaptured={}", firstReadoutInScan, numCaptured, numToBeCaptured);

		LogLimiter logLimiter = new LogLimiter(Duration.ofSeconds(10), true);

		while( numCaptured< numToBeCaptured){
			if (logLimiter.isLogDue()) {
				logger.info("Waiting for {} points, but only {} captured after {} seconds on {}",
						numToBeCaptured, numCaptured, logLimiter.getTimeSinceStart().getSeconds(), getName());
			}
			try {
				getNdFile().getPluginBase().checkDroppedFrames();
			} catch (Exception e) {
				throw new DeviceException("Error in " + getName(), e);
			}
			try {
				numCaptured = getNdFileHDF5().getNumCaptured_RBV();
			} catch (Exception e) {
				throw new DeviceException("Error in getCapture_RBV" + getName(), e);
			}
			Thread.sleep(50);
		}
		firstReadoutInScan = false;
		Vector<NXDetectorDataAppender> appenders = new Vector<>();
		appenders.add(dataAppender);
		return appenders;
	}

	/**
	 * an Appender to add link to external file or files created by EPICS Area Detector
	 *
	 * This method is extracted out of {@link #read(int)} method, so it can be override by child to provide different data set.
	 *
	 * @return instance of {@link NXDetectorDataAppender} subclass.
	 */
	protected NXDetectorDataAppender createFileLinkAppender() {
		logger.trace("create file link appender to link to HDF5 files generated by EPICS Area Detector plus additional PVs value collection");
		var nxDetectorDataFileLinkAppender = new NXDetectorDataFileLinkAppender(expectedFullFileName, getxPixelSize(), getyPixelSize(), getxPixelSizeUnit(), getyPixelSizeUnit(),dataRank);
		Optional<List<NXDetectorDataAppender>> optionalAppenders = getPvsToCollectMaps().map(this::createAppendersFromCollection);
		optionalAppenders.ifPresent(appenders -> appenders.add(nxDetectorDataFileLinkAppender));
		if (optionalAppenders.isPresent()) {
			return new NXDetectorSerialAppender(optionalAppenders.get());
		} else {
			return nxDetectorDataFileLinkAppender;
		}
	}

	protected List<NXDetectorDataAppender> createAppendersFromCollection(List<EpicsProcessVariableCollection> collections) {
		List<NXDetectorDataAppender> appenders = new ArrayList<>();
		collections.stream().forEach(collection -> {
			collection.createNexusTreeFromName2PVNestedMap(collection.getName()).ifPresent(e -> appenders.add(new NXDetectorDataChildNodeAppender(e)));
			collection.createNexusTreeFromName2PVSimpleMap(collection.getName()).ifPresent(e -> appenders.add(new NXDetectorDataChildNodeAppender(e)));
			collection.createNexusTreeFromName2PairMap(collection.getName()).ifPresent(e -> appenders.add(new NXDetectorDataChildNodeAppender(e)));
			collection.createFieldsToAppend().ifPresent(e -> appenders.add(new NXDetectorDataStringAppender(List.copyOf(e.keySet()), List.copyOf(e.values()))));
		});
		return appenders;
	}

	public Optional<List<EpicsProcessVariableCollection>> getPvsToCollectMaps() {
		pvsToCollectMaps.stream().forEach(e -> e.setHDF5Filename(expectedFullFileName));
		return Optional.ofNullable(pvsToCollectMaps);
	}

	public void setPvsToCollectMaps(List<EpicsProcessVariableCollection> pvsToCollectMaps) {
		this.pvsToCollectMaps = pvsToCollectMaps;
	}

	class NXDetectorDataFileLinkAppenderDelayed implements NXDetectorDataAppender {

			@Override
			public void appendTo(NXDetectorData data, String detectorName) throws DeviceException {

				LogLimiter logLimiter = new LogLimiter(Duration.ofSeconds(10), true);

				try{
					String filename = "";
					do {
						if (logLimiter.isLogDue()) {
							logger.info("NXDetectorDataFileLinkAppenderDelayed.appendTo() Waiting for fullFilename for {} seconds from {}",
									logLimiter.getTimeSinceStart().getSeconds(), getName());
						}
						Thread.sleep(1000);
						filename=getFullFileName();
					}
					while(!StringUtils.hasLength(filename));

					data.addScanFileLink(detectorName, "nxfile://" + filename + "#entry/instrument/detector/data");
				}catch(Exception ex){
					throw new DeviceException("Exception getting filename");
				}
			}
		}

	public Double getyPixelSize() {
		return yPixelSize;
	}

	public void setyPixelSize(Double yPixelSize) {
		this.yPixelSize = yPixelSize;
	}

	public Double getxPixelSize() {
		return xPixelSize;
	}

	public void setxPixelSize(Double xPixelSize) {
		this.xPixelSize = xPixelSize;
	}

	public String getxPixelSizeUnit() {
		return xPixelSizeUnit;
	}

	public void setxPixelSizeUnit(String xPixelSizeUnit) {
		this.xPixelSizeUnit = xPixelSizeUnit;
	}

	public void setyPixelSizeUnit(String yPixelSizeUnit) {
		this.yPixelSizeUnit=yPixelSizeUnit;
	}

	public String getyPixelSizeUnit() {
		return yPixelSizeUnit;
	}

	public boolean isAlwaysPrepare() {
		return alwaysPrepare;
	}

	public void setAlwaysPrepare(boolean alwaysPrepare) {
		this.alwaysPrepare = alwaysPrepare;
	}

	public void setSwmrModeEnabled(boolean swmrModeEnabled) {
		this.swmrModeEnabled = Optional.of(swmrModeEnabled);
	}
}
