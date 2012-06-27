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

import gda.device.detector.areadetector.v17.NDFile;
import gda.jython.InterfaceProvider;
import gda.scan.ScanInformation;

import org.springframework.beans.factory.InitializingBean;

public abstract class FileWriterBase implements FileWriter, InitializingBean{

	private final NDFile ndFile;
	private final String fileTemplate;
	private final String fileName;
	private boolean setFileNumberToZero=true;
	private boolean enableDuringScan = true;
	
	/**
	 * Value of Input Array port in plugin
	 */
	private String ndArrayPortVal="";
	

	public String getNdArrayPortVal() {
		return ndArrayPortVal;
	}


	public void setNdArrayPortVal(String ndArrayPortVal) {
		this.ndArrayPortVal = ndArrayPortVal;
	}


	protected NDFile getNdFile() {
		return ndFile;
	}

	
	public FileWriterBase(NDFile ndFile, String fileName, String fileTemplate, boolean setFileNumberToZero, boolean setFileNameAndNumber) {
		this.ndFile = ndFile;
		this.fileName = fileName;
		this.fileTemplate = fileTemplate;
		this.setFileNumberToZero = setFileNumberToZero;
		this.setFileNameAndNumber = setFileNameAndNumber;
	}

	@Override
	public void stop() throws Exception {
		if(getEnable())
			disableFileWriter();
	}


	@Override
	public void atCommandFailure() throws Exception {
		if(getEnable())
			stop();
	}
	

	protected boolean isSetFileWriterNameNumber() {
		return setFileNameAndNumber;
	}


	protected void setSetFileWriterNameNumber(boolean setFileWriterNameNumber) {
		this.setFileNameAndNumber = setFileWriterNameNumber;
	}


	protected String getFileTemplate() {
		return fileTemplate;
	}


	protected String getFileName() {
		return fileName;
	}


	protected boolean isSetFileNumberToZero() {
		return setFileNumberToZero;
	}


	protected void setSetFileNumberToZero(boolean setFileNumberToZero) {
		this.setFileNumberToZero = setFileNumberToZero;
	}


	// If true file writer name and number is configured in prepareCollection
	private boolean setFileNameAndNumber = true;
	
	@Override
	public boolean isSetFileNameAndNumber() {
		return setFileNameAndNumber;
	}

	@Override
	public void setSetFileNameAndNumber(boolean setFileWriterNameNumber) {
		this.setFileNameAndNumber = setFileWriterNameNumber;
	}

	@Override
	public void setEnable(boolean enableDuringScan) {
		this.enableDuringScan = enableDuringScan;
	}


	/*
	 * Setup fileWriter if this and isReadFilePath are true
	 */
	@Override
	public boolean getEnable() {
		return enableDuringScan;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if( ndFile == null)
			throw new IllegalStateException("NDFile is null");
	}

	@Override
	public void enableCallback(boolean enable) throws Exception {
		if( enable )
			ndFile.getPluginBase().enableCallbacks();
		else
			ndFile.getPluginBase().disableCallbacks();
	}	
	
	/**
	 * 
	 * @return The unique scanNubmer from the current scan.
	 * @throws Exception 
	 */
	protected long getScanNumber() throws Exception{
		ScanInformation scanInformation = InterfaceProvider.getCurrentScanInformationHolder().getCurrentScanInformation();
		if (scanInformation == null || scanInformation.getScanNumber() == null ){
			throw new Exception("ScanNumber not available");
		}
		return scanInformation.getScanNumber();

	}


	@Override
	public String getFullFileName_RBV() throws Exception {
		return ndFile.getFullFileName_RBV();
	}
	
	
	protected void setNDArrayPortAndAddress() throws Exception {
		if( ndArrayPortVal != null && ndArrayPortVal.length()>0)
			ndFile.getPluginBase().setNDArrayPort(ndArrayPortVal);
	}
}
