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
import gda.device.detector.areadetector.v17.NDFile;
import gda.device.detector.areadetector.v17.NDFile.FileWriteMode;
import gda.device.detector.areadetector.v17.NDFileHDF5;
import gda.device.detector.nxdata.NXDetectorDataAppender;
import gda.device.detector.nxdata.NXDetectorDataFileLinkAppender;
import gda.device.detector.nxdata.NXDetectorDataNullAppender;
import gda.jython.InterfaceProvider;
import gda.scan.ScanBase;
import gda.scan.ScanInformation;
import gov.aps.jca.TimeoutException;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MultipleImagesPerHDF5FileWriter extends FileWriterBase {
	
	@SuppressWarnings("unused")
	private static Logger logger = LoggerFactory.getLogger(MultipleImagesPerHDF5FileWriter.class);

	private NDFileHDF5 ndFileHDF5;
	
	private boolean blocking = true;
	
	private int rowChunks = 1;

	private boolean firstReadoutInScan;
	
	@Override
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

	@Override
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


	@Override
	public void prepareForCollection(int numberImagesPerCollection) throws Exception {
		if(!getEnable())
			return;
		setNDArrayPortAndAddress();
		getNdFile().getPluginBase().disableCallbacks();
		getNdFile().getPluginBase().setBlockingCallbacks(blocking ? 1:0); //use camera memory 
		getNdFile().setFileWriteMode(FileWriteMode.STREAM); 
		ScanInformation scanInformation = InterfaceProvider.getCurrentScanInformationHolder().getCurrentScanInformation();
		//if not scan setup then act as if this is a 1 point scan
		setScanDimensions(scanInformation == null ? new int []{1}: scanInformation.getDimensions(), numberImagesPerCollection);
		if( isSetFileNameAndNumber()){
			setupFilename();
		}
		resetCounters();
		startRecording();
		getNdFile().getPluginBase().enableCallbacks();
		firstReadoutInScan = true;
	}
	
	private void setScanDimensions(int[] dimensions, int numberImagesPerCollection) throws Exception {
		int [] actualDims = dimensions;
		if( numberImagesPerCollection > 1){
			actualDims = Arrays.copyOf(dimensions, dimensions.length+1);
			actualDims[dimensions.length] = numberImagesPerCollection;
		}
		if( actualDims.length > 3)
			throw new Exception("Maximum dimensions for storing in hdf is currently 3. Value specified = " + actualDims.length);			
		if( actualDims.length==1 ){
			getNdFileHDF5().setNumExtraDims(0); 
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
		int numberOfAcquires=1;
		for( int dim : actualDims ){
			numberOfAcquires *= dim;
		}
		getNdFileHDF5().setNumCapture(numberOfAcquires);

		getNdFileHDF5().setNumRowChunks(rowChunks);
	}
	
	private void setupFilename() throws Exception {
		getNdFile().setFileName(getFileName());
		getNdFile().setFileTemplate(getFileTemplate());
		getNdFile().setFilePath(getFilePath());
		
		long scanNumber = getScanNumber();

		
		getNdFile().setFileNumber((int)scanNumber);	
		getNdFile().setAutoSave((short) 0);
		getNdFile().setAutoIncrement((short) 0);

	}
	
	private void startRecording() throws Exception {
		if (getNdFileHDF5().getCapture() == 1) 
				throw new DeviceException("detector found already saving data when it should not be");
		
		getNdFileHDF5().startCapture();
		int totalmillis = 60 * 1000;
		int grain = 25;
		for (int i = 0; i < totalmillis/grain; i++) {
			if (getNdFileHDF5().getCapture_RBV() == 1) return;
			Thread.sleep(grain);
		}
		throw new TimeoutException("Timeout waiting for hdf file creation.");
	}

	
	private void resetCounters() throws Exception {
		getNdFile().getPluginBase().setDroppedArrays(0);
		getNdFile().getPluginBase().setArrayCounter(0);
	}

	
	
	
	@Override
	public void disableFileWriter() throws Exception {
		getNdFile().getPluginBase().disableCallbacks();
		getNdFile().getPluginBase().setBlockingCallbacks((short) 0);
//		getNdFile().setFileWriteMode(FileWriteMode.STREAM);
	}
	
	
	@Override
	public void completeCollection() throws Exception{
		if(!getEnable())
			return;
		FileRegistrarHelper.registerFile(getNdFileHDF5().getFullFileName_RBV());
		endRecording();
		disableFileWriter();
	}
	
	private void endRecording() throws Exception {
		while (getNdFileHDF5().getFile().getCapture_RBV() != 0) {
			ScanBase.checkForInterrupts();
			Thread.sleep(1000);
		}
		getNdFileHDF5().stopCapture();
		
//		logger.warn("Waited very long for hdf writing to finish, still not done. Hope all we be ok in the end.");
		if (getNdFileHDF5().getPluginBase().getDroppedArrays_RBV() > 0)
			throw new DeviceException("sorry, we missed some frames");
	}
	
	@Override
	public boolean isLinkFilepath() {
		return true;
	}

	@Override
	public void stop() throws Exception {
		if(!getEnable())
			return;
		getNdFileHDF5().stopCapture();
		
	}

	@Override
	public void atCommandFailure() throws Exception {
		if(!getEnable())
			return;
		stop();
	}
	
	@Override
	public List<String> getInputStreamExtraNames() {
		return Arrays.asList();
	}

	@Override
	public List<String> getInputStreamFormats() {
		return Arrays.asList();
	}

	@Override
	public Vector<NXDetectorDataAppender> read(int maxToRead) throws NoSuchElementException, InterruptedException,
			DeviceException {
		NXDetectorDataAppender dataAppender;
		if (firstReadoutInScan) {
			String filepath;
			try {
				filepath = getFullFileName_RBV();
			} catch (Exception e) {
				throw new DeviceException(e);
			}
			dataAppender = new NXDetectorDataFileLinkAppender(filepath);
		}
		else {
			dataAppender = new NXDetectorDataNullAppender();
		}
		firstReadoutInScan = false;
		Vector<NXDetectorDataAppender> appenders = new Vector<NXDetectorDataAppender>();
		appenders.add(dataAppender);
		return appenders;
	}
}
