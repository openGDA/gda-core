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

import gda.data.fileregistrar.FileRegistrarHelper;
import gda.device.DeviceException;
import gda.device.detector.areadetector.v17.NDFile.FileWriteMode;
import gda.device.detector.areadetector.v17.NDParallelHDF;
import gda.device.detector.areadetector.v17.NDParallelHDF.DsetSizeMode;
import gda.device.detector.areadetector.v17.NDParallelHDF.RoiPosMode;
import gda.device.detector.nxdata.NXDetectorDataAppender;
import gda.device.detector.nxdata.NXDetectorDataFileLinkAppender;
import gda.device.detector.nxdata.NXDetectorDataNullAppender;
import gda.device.detector.nxdetector.NXPlugin;
import gda.jython.InterfaceProvider;
import gda.scan.ScanInformation;
import gov.aps.jca.TimeoutException;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MultipleImagesPerParallelHDF5FileWriter extends FileWriterBase implements NXPlugin{

	private static Logger logger = LoggerFactory.getLogger(MultipleImagesPerParallelHDF5FileWriter.class);

	private NDParallelHDF ndFilePHDF5;

	private boolean blocking = true;

	private boolean firstReadoutInScan;

	@Override
	public String getName() {
		return "hdfwriter"; // TODO: Multiple filewriters require different names.
	}
	private boolean alreadyPrepared=false;

	public NDParallelHDF getNdFilePHDF5() {
		return ndFilePHDF5;
	}


	public void setNdFilePHDF5(NDParallelHDF ndFilePHDF5) {
		this.ndFilePHDF5 = ndFilePHDF5;
		super.setNdFile(ndFilePHDF5);
	}



	@Override
	public void afterPropertiesSet() throws Exception {
		if (ndFilePHDF5 == null)
			throw new IllegalStateException("ndFilePHDF5 is null");
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

	private boolean storeAttr=false;

	private boolean storePerform=false;

	private NDParallelHDF.ChunkSizeMode chunkSizeMode;

	private int chunkSize0;
	private int chunkSize1;
	private int chunkSize2;

	private DsetSizeMode dsetSizeMode;

	private int dsetSize0;
	private int dsetSize1;
	private int dsetSize2;

	private RoiPosMode roisPosMode;

	private int roiPos0;
	private int roiPos1;
	private int roiPos2;

	private String expectedFullFileName;

	private boolean checkPathExists=true;

	public boolean isCheckPathExists() {
		return checkPathExists;
	}


	public void setCheckPathExists(boolean checkPathExists) {
		this.checkPathExists = checkPathExists;
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





	public NDParallelHDF.ChunkSizeMode getChunkSizeMode() {
		return chunkSizeMode;
	}


	public void setChunkSizeMode(NDParallelHDF.ChunkSizeMode chunkSizeMode) {
		this.chunkSizeMode = chunkSizeMode;
	}


	public int getChunkSize0() {
		return chunkSize0;
	}


	public void setChunkSize0(int chunkSize0) {
		this.chunkSize0 = chunkSize0;
	}


	public int getChunkSize1() {
		return chunkSize1;
	}


	public void setChunkSize1(int chunkSize1) {
		this.chunkSize1 = chunkSize1;
	}


	public int getChunkSize2() {
		return chunkSize2;
	}


	public void setChunkSize2(int chunkSize2) {
		this.chunkSize2 = chunkSize2;
	}


	public DsetSizeMode getDsetSizeMode() {
		return dsetSizeMode;
	}


	public void setDsetSizeMode(DsetSizeMode dsetSizeMode) {
		this.dsetSizeMode = dsetSizeMode;
	}


	public int getDsetSize0() {
		return dsetSize0;
	}


	public void setDsetSize0(int dsetSize0) {
		this.dsetSize0 = dsetSize0;
	}


	public int getDsetSize1() {
		return dsetSize1;
	}


	public void setDsetSize1(int dsetSize1) {
		this.dsetSize1 = dsetSize1;
	}


	public int getDsetSize2() {
		return dsetSize2;
	}


	public void setDsetSize2(int dsetSize2) {
		this.dsetSize2 = dsetSize2;
	}


	public RoiPosMode getRoisPosMode() {
		return roisPosMode;
	}


	public void setRoisPosMode(RoiPosMode roisPosMode) {
		this.roisPosMode = roisPosMode;
	}


	public int getRoiPos0() {
		return roiPos0;
	}


	public void setRoiPos0(int roiPos0) {
		this.roiPos0 = roiPos0;
	}


	public int getRoiPos1() {
		return roiPos1;
	}


	public void setRoiPos1(int roiPos1) {
		this.roiPos1 = roiPos1;
	}


	public int getRoiPos2() {
		return roiPos2;
	}


	public void setRoiPos2(int roiPos2) {
		this.roiPos2 = roiPos2;
	}


	@Override
	public void prepareForCollection(int numberImagesPerCollection, ScanInformation scanInfo) throws Exception {
		if(!isEnabled())
			return;
		if( alreadyPrepared)
			return;

		setNDArrayPortAndAddress();
		getNdFile().getPluginBase().disableCallbacks();
		getNdFile().getPluginBase().setBlockingCallbacks(blocking ? 1:0); //use camera memory
//		getNdFilePHDF5().setStoreAttr(storeAttr? 1:0);
//		getNdFilePHDF5().setStorePerform(storePerform?1:0);
		getNdFile().setFileWriteMode(FileWriteMode.STREAM);
		ScanInformation scanInformation = InterfaceProvider.getCurrentScanInformationHolder().getCurrentScanInformation();
		//if not scan setup then act as if this is a 1 point scan
		setScanDimensions(scanInformation == null ? new int []{1}: scanInformation.getDimensions(), numberImagesPerCollection);
		if( isSetFileNameAndNumber()){
			setupFilename();
		}
		expectedFullFileName = String.format(getNdFile().getFileTemplate_RBV(), getNdFile().getFilePath_RBV(), getNdFile().getFileName_RBV(), getNdFile().getFileNumber_RBV());
		resetCounters();
		startRecording();
		getNdFile().getPluginBase().enableCallbacks();
		firstReadoutInScan = true;
		alreadyPrepared=true;

	}

	private void setScanDimensions(int[] dimensions, int numberImagesPerCollection) throws Exception {
		int [] actualDims = dimensions;
		if( numberImagesPerCollection > 1){
			actualDims = Arrays.copyOf(dimensions, dimensions.length+1);
			actualDims[dimensions.length] = numberImagesPerCollection;
		}
/*		if( actualDims.length > 3)
			throw new Exception("Maximum dimensions for storing in hdf is currently 3. Value specified = " + actualDims.length);
		if( actualDims.length==1 ){
			getNdFilePHDF5().setNumExtraDims(0);
		} else	if( actualDims.length==2 ){
			getNdFileHDF5().setNumExtraDims(1);
			getNdFileHDF5().setExtraDimSizeN(actualDims[1]);
			getNdFileHDF5().setExtraDimSizeX(actualDims[0]);
		} else	if( actualDims.length==3 ){
			getNdFileHDF5().setNumExtraDims(2);
			getNdFileHDF5().setExtraDimSizeN(actualDims[2]);
			getNdFileHDF5().setExtraDimSizeX(actualDims[1]);
			getNdFileHDF5().setExtraDimSizeY(actualDims[0]);
		}
*/		int numberOfAcquires=1;
		for( int dim : actualDims ){
			numberOfAcquires *= dim;
		}
		getNdFilePHDF5().setChunkSizeMode(chunkSizeMode.ordinal());
		if( chunkSizeMode == NDParallelHDF.ChunkSizeMode.MANUAL){
			getNdFilePHDF5().setChunkSize0(chunkSize0);
			getNdFilePHDF5().setChunkSize1(chunkSize1);
			getNdFilePHDF5().setChunkSize2(chunkSize2);
		}
		getNdFilePHDF5().setDsetSizeMode(dsetSizeMode.ordinal());
		if( dsetSizeMode == NDParallelHDF.DsetSizeMode.MANUAL){
			getNdFilePHDF5().setDsetSize0(dsetSize0);
			getNdFilePHDF5().setDsetSize1(dsetSize1);
			getNdFilePHDF5().setDsetSize2(dsetSize2);
		}
		getNdFilePHDF5().setRoiPosMode(roisPosMode.ordinal());
		if( roisPosMode == NDParallelHDF.RoiPosMode.MANUAL){
			getNdFilePHDF5().setRoiPos0(roiPos0);
			getNdFilePHDF5().setRoiPos1(roiPos1);
			getNdFilePHDF5().setRoiPos2(roiPos2);
		}
		getNdFilePHDF5().setNumCapture(numberOfAcquires);
	}

	private void setupFilename() throws Exception {
		getNdFile().setFileName(getFileName());
		getNdFile().setFileTemplate(getFileTemplate());
		String filePath = getFilePath();
		if (!filePath.endsWith(File.separator))
			filePath += File.separator;
		getNdFile().setFilePath(filePath);
		if( checkPathExists && !getNdFile().filePathExists())
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
		if (getNdFilePHDF5().getCapture() == 1)
				throw new DeviceException("detector found already saving data when it should not be");

		getNdFilePHDF5().startCapture();
		int totalmillis = 60 * 1000;
		int grain = 25;
		for (int i = 0; i < totalmillis/grain; i++) {
			if (getNdFilePHDF5().getCapture_RBV() == 1) return;
			Thread.sleep(grain);
		}
		throw new TimeoutException("Timeout waiting for phdf file creation.");
	}


	private void resetCounters() throws Exception {
		getNdFile().getPluginBase().setDroppedArrays(0);
		getNdFile().getPluginBase().setArrayCounter(0);
	}




	@Override
	public void disableFileWriting() throws Exception {
		getNdFile().getPluginBase().disableCallbacks();
		getNdFile().getPluginBase().setBlockingCallbacks((short) 0);
//		getNdFile().setFileWriteMode(FileWriteMode.STREAM);
	}


	@Override
	public void completeCollection() throws Exception{
		alreadyPrepared=false;
		if(!isEnabled())
			return;
		FileRegistrarHelper.registerFile(getNdFilePHDF5().getFullFileName_RBV());
		endRecording();
		disableFileWriting();
	}

	private void endRecording() throws Exception {
		while (getNdFilePHDF5().getCapture_RBV() != 0) {
			Thread.sleep(1000);
		}
		getNdFilePHDF5().stopCapture();

		if (getNdFilePHDF5().getPluginBase().getDroppedArrays_RBV() > 0)
			throw new DeviceException("sorry, we missed some frames");
	}

	@Override
	public boolean appendsFilepathStrings() {
		return false;
	}

	@Override
	public void stop() throws Exception {
		alreadyPrepared=false;
		if(!isEnabled())
			return;
		getNdFilePHDF5().stopCapture();

	}

	@Override
	public void atCommandFailure() throws Exception {
		alreadyPrepared=false;
		if(!isEnabled())
			return;
		stop();
	}

	@Override
	public List<String> getInputStreamNames() {
		return Arrays.asList();
	}

	@Override
	public List<String> getInputStreamFormats() {
		return Arrays.asList();
	}

	private int numToBeCaptured;

	private int numCaptured;


	@Override
	public Vector<NXDetectorDataAppender> read(int maxToRead) throws NoSuchElementException, InterruptedException,
			DeviceException {
		NXDetectorDataAppender dataAppender;
		if(isEnabled())
		{
			//wait until the NumCaptured_RBV is equal to or exceeds maxToRead.
			checkErrorStatus();
			try {
				getNdFile().getPluginBase().checkDroppedFrames();
			} catch (Exception e) {
				throw new DeviceException("Error in " + getName(), e);
			}
			if (firstReadoutInScan) {
				dataAppender = new NXDetectorDataFileLinkAppender(expectedFullFileName);
				numToBeCaptured=1;
				numCaptured=0;
			}
			else {
				dataAppender = new NXDetectorDataNullAppender();
				numToBeCaptured++;
			}
			while( numCaptured< numToBeCaptured){
				try {
					numCaptured = getNdFilePHDF5().getNumCaptured_RBV();
				} catch (Exception e) {
					throw new DeviceException("Error in getCapture_RBV" + getName(), e);
				}
				Thread.sleep(50);
			}
		} else {
			dataAppender = new NXDetectorDataNullAppender();
		}
		firstReadoutInScan = false;
		Vector<NXDetectorDataAppender> appenders = new Vector<NXDetectorDataAppender>();
		appenders.add(dataAppender);
		return appenders;
	}

}
