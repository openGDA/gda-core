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

import gda.data.PathConstructor;
import gda.device.DeviceException;
import gda.device.detector.areadetector.v17.NDFile;
import gda.device.detector.nxdetector.NXFileWriterPlugin;
import gda.device.detector.nxdetector.NXFileWriterWithTemplate;
import gda.jython.InterfaceProvider;
import gda.observable.Observable;
import gda.observable.Observer;
import gda.scan.ScanInformation;

import java.io.File;
import java.text.MessageFormat;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.InitializingBean;

public abstract class FileWriterBase implements NXFileWriterPlugin, NXFileWriterWithTemplate, InitializingBean {

	private NDFile ndFile;

	protected String fileTemplate;

	private String filePathTemplate;

	private String fileNameTemplate;

	private Long fileNumberAtScanStart;

	private boolean enableDuringScan = true;

	private boolean setFileNameAndNumber = true;

	private boolean pathErrorSuppressed=false;

	private Observable<Short> writeStatusObservable;

	private boolean writeStatusErr=false;

	private Observer<Short>  statusObserver;

	abstract protected void disableFileWriting() throws Exception;

	@Override
	public void afterPropertiesSet() throws Exception {
		if (ndFile == null)
			throw new IllegalStateException("NDFile is null");
		if (fileTemplate == null)
			throw new IllegalStateException("fileTemplate is null");
		if (filePathTemplate == null)
			throw new IllegalStateException("filePathTemplate is null");
		if (fileNameTemplate == null)
			throw new IllegalStateException("fileNameTemplate is null");
		if (fileNumberAtScanStart == null)
			throw new IllegalStateException("fileNumberAtScanStart is null");
		writeStatusObservable = getNdFile().createWriteStatusObservable();
	}


	private boolean writeErrorStatusSupported=true;

	public boolean isWriteErrorStatusSupported() {
		return writeErrorStatusSupported;
	}

	public void setWriteErrorStatusSupported(boolean writeErrorStatusSupported) {
		this.writeErrorStatusSupported = writeErrorStatusSupported;
	}

	public Boolean isWriteStatusErr() throws Exception {
		if( !writeErrorStatusSupported)
			return false;

		if( statusObserver == null){
			statusObserver = new Observer<Short>() {

				@Override
				public void update(Observable<Short> source, Short arg) {
					writeStatusErr = arg==1;
				}
			};
			writeStatusObservable.addObserver(statusObserver);
			writeStatusErr = getNdFile().isWriteStatusErr();
		}
		return writeStatusErr;
	}

	protected void checkErrorStatus() throws DeviceException {
		boolean writeStatusErr;
		try {
			writeStatusErr = isWriteStatusErr();
		} catch (Exception e) {
			throw new DeviceException(getName() + " error checking writeStatusErr",e);
		}
		if (writeStatusErr) {
			String writeMessage="";
			try {
				writeMessage = getNdFile().getWriteMessage();
			} catch (Exception e) {
				throw new DeviceException(getName() + " file writer plugin in error. Error getting writeMessage",e);
			}
			throw new DeviceException(getName() + " file writer plugin reports '" + writeMessage + "'");
		}
	}

	/**
	 * Use this method to clear writeStatus ready for next scan.
	 * @throws DeviceException
	 */
	protected void clearWriteStatusErr() throws DeviceException{

		boolean isErr;
		try{
			isErr = isWriteStatusErr();
			if(isErr){
				getNdFile().startCapture();
				getNdFile().stopCapture();
				Thread.sleep(1000);
				isErr = isWriteStatusErr();
			}
		}catch(Exception e){
			throw new DeviceException("Error clearing writeStatus in filewriter plugin",e);
		}
		if( isErr)
			throw new DeviceException("Unable to clear writeStatus in filewriter plugin");
	}
	public void setNdFile(NDFile ndFile) {
		this.ndFile = ndFile;
	}

	/**
	 * File template to pass directly to AreaDetector. AreaDetector's NDFilePlugin
	 * (http://cars.uchicago.edu/software/epics/NDPluginFile.html) will always apply arguments to the template in the
	 * order filepath, filename, filenumber
	 *
	 * @param fileTemplate
	 */
	@Override
	public void setFileTemplate(String fileTemplate) {
		this.fileTemplate = fileTemplate;
	}

	/**
	 * The file path to pass to AreadDetector, with $scan$ resolving to the currently running scan number, and $datadir$
	 * to the current scan file's directory.
	 *
	 * @param filePathTemplate
	 */
	@Override
	public void setFilePathTemplate(String filePathTemplate) {
		this.filePathTemplate = filePathTemplate;
	}

	/**
	 * The file name to pass to AreadDetector, with $scan$ resolving to the currently running scan number, and $datadir$
	 * to the current scan file's directory.
	 *
	 * @param fileNameTemplate
	 */
	@Override
	public void setFileNameTemplate(String fileNameTemplate) {
		this.fileNameTemplate = fileNameTemplate;
	}

	/**
	 * If non-negative, the file number to preset in AreaDetector before staring the exposure
	 * @param fileNumberAtScanStart
	 */
	public void setFileNumberAtScanStart(long fileNumberAtScanStart) {
		this.fileNumberAtScanStart = fileNumberAtScanStart;
	}

	public void setSetFileNameAndNumber(boolean setFileNameAndNumber) {
		this.setFileNameAndNumber = setFileNameAndNumber;
	}

	public NDFile getNdFile() {
		return ndFile;
	}
	/**
	 * @return the file template to configure in AreaDetector
	 */
	public String getFileTemplate() {
		return fileTemplate;
	}


	public String getFilePathTemplate() {
		return filePathTemplate;
	}


	public String getFileNameTemplate() {
		return fileNameTemplate;
	}

	public long getFileNumberAtScanStart() {
		return fileNumberAtScanStart;
	}

	public boolean isSetFileNameAndNumber() {
		return setFileNameAndNumber;
	}

	public boolean isPathErrorSuppressed() {
		return pathErrorSuppressed;
	}

	public void setPathErrorSuppressed(boolean pathErrorSuppressed) {
		this.pathErrorSuppressed = pathErrorSuppressed;
	}

	/**
	 * @return the file path to configure in AreaDetector
	 */
	protected String getFilePath() {
		return substituteScan(substituteDatadir(getFilePathTemplate()));
	}

	/**
	 * Only if the path template starts with the datadir, return a path relative to it, otherwise
	 * return an absolute path.
	 */
	protected String getFileDirRelativeToDataDirIfPossible() {
		String template = getFilePathTemplate();
		if (StringUtils.startsWith(template, "$datadir$")) {
			template = StringUtils.replace(template, "$datadir$","");
			template = StringUtils.removeStart(template, "/");
		} else {
			//return absolute path (after substituting data directory)
			template = substituteDatadir(template);
		}
		return substituteScan(template);
	}

	/**
	 * @return the file path relative to the data directory if it starts with the datadir,
	 * otherwise returns absolute path.
	 * @throws Exception
	 */
	protected String getRelativeFilePath() throws Exception {
		String fullFileName = getFullFileName();
		String datadir = PathConstructor.createFromDefaultProperty();
		if (StringUtils.startsWith(fullFileName, datadir)) {
			String relativeFilename = StringUtils.removeStart(fullFileName, datadir);
			relativeFilename = StringUtils.removeStart(relativeFilename, "/");
			return relativeFilename;
		}
		return fullFileName;
	}

	private String substituteDatadir(String template) {

		if (StringUtils.contains(template, "$datadir$")) {
			template = StringUtils.replace(template, "$datadir$", PathConstructor.createFromDefaultProperty());
		}

		return template;
	}

	protected String getAbsoluteFilePath(String filePathRelativeToDataDir) {
		return PathConstructor.createFromDefaultProperty() + File.separator + filePathRelativeToDataDir;
	}

	private String substituteScan(String template) {

		if (StringUtils.contains(template, "$scan$")) {
			long scanNumber;
			try {
				scanNumber = getScanNumber();
			} catch (Exception e) {
				throw new IllegalStateException("Could not determine current scan number", e);
			}
			template = StringUtils.replace(template, "$scan$", String.valueOf(scanNumber));
		}
		return template;
	}

	/**
	 * @return the file name to configure in AreaDetector
	 */
	protected String getFileName() {
		return substituteScan(substituteDatadir(getFileNameTemplate()));
	}

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

	@Override
	public void stop() throws Exception {
		if(isEnabled())
			disableFileWriting();
	}

	@Override
	public void atCommandFailure() throws Exception {
		if(isEnabled())
			stop();
	}

	public void setEnabled(boolean enableDuringScan) {
		this.enableDuringScan = enableDuringScan;
	}

	public boolean isEnabled() {
		return enableDuringScan;
	}

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
		if (scanInformation == null || scanInformation.getScanNumber() <= 0 ){
			throw new Exception(MessageFormat.format("ScanNumber not available, scanInformation= {0} - If null, check that gda.scan.sets.scannumber = True", scanInformation));
		}
		return scanInformation.getScanNumber();

	}

	@Override
	public String getFullFileName() throws Exception {
		return ndFile.getFullFileName_RBV();
	}


	protected void setNDArrayPortAndAddress() throws Exception {
		if( ndArrayPortVal != null && ndArrayPortVal.length()>0)
			ndFile.getPluginBase().setNDArrayPort(ndArrayPortVal);
	}

	@Override
	public boolean willRequireCallbacks() {
		return ndFile.getPluginBase() != null && isEnabled(); // always requires callbacks if enabled
		// Note: camserver filewriter has no base so cannot require callbacks
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
	public void prepareForCollection(int numberImagesPerCollection, ScanInformation scanInfo) throws Exception {
	}
}
