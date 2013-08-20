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

package gda.device.detector.areadetector.v17;

import gda.observable.Observable;

/**
 *
 */
public interface NDFile {

	public enum FileWriteMode {
		SINGLE, CAPTURE, STREAM
	}

	/**
	 * List all the PVs // TODO: Remove from interface
	 */
	public final String FilePath = "FilePath";

	public final String FilePath_RBV = "FilePath_RBV";

	public final String FilePathExists_RBV = "FilePathExists_RBV";

	public final String FileName = "FileName";

	public final String FileName_RBV = "FileName_RBV";

	public final String FileNumber = "FileNumber";

	public final String FileNumber_RBV = "FileNumber_RBV";

	public final String AutoIncrement = "AutoIncrement";

	public final String AutoIncrement_RBV = "AutoIncrement_RBV";

	public final String FileTemplate = "FileTemplate";

	public final String FileTemplate_RBV = "FileTemplate_RBV";

	public final String FullFileName_RBV = "FullFileName_RBV";

	public final String AutoSave = "AutoSave";

	public final String AutoSave_RBV = "AutoSave_RBV";

	public final String WriteFile = "WriteFile";

	public final String WriteFile_RBV = "WriteFile_RBV";

	public final String WriteMessage = "WriteMessage";

	public final String WriteStatus = "WriteStatus";

	public final String ReadFile = "ReadFile";

	public final String ReadFile_RBV = "ReadFile_RBV";

	public final String FileFormat = "FileFormat";

	public final String FileFormat_RBV = "FileFormat_RBV";

	public final String FileWriteMode = "FileWriteMode";

	public final String FileWriteMode_RBV = "FileWriteMode_RBV";

	public final String Capture = "Capture";

	public final String Capture_RBV = "Capture_RBV";

	public final String NumCapture = "NumCapture";

	public final String NumCapture_RBV = "NumCapture_RBV";

	public final String NumCaptured_RBV = "NumCaptured_RBV";

	NDPluginBase getPluginBase();

	String getFilePath() throws Exception;

	void setFilePath(String filepath) throws Exception;

	String getFilePath_RBV() throws Exception;

	Boolean filePathExists() throws Exception;

	String getFileName() throws Exception;

	void setFileName(String filename) throws Exception;

	String getFileName_RBV() throws Exception;

	int getFileNumber() throws Exception;

	void setFileNumber(int filenumber) throws Exception;

	int getFileNumber_RBV() throws Exception;

	short getAutoIncrement() throws Exception;

	void setAutoIncrement(int autoincrement) throws Exception;

	short getAutoIncrement_RBV() throws Exception;

	String getFileTemplate() throws Exception;

	void setFileTemplate(String filetemplate) throws Exception;

	String getFileTemplate_RBV() throws Exception;

	String getFullFileName_RBV() throws Exception;

	short getAutoSave() throws Exception;

	void setAutoSave(int autosave) throws Exception;

	short getAutoSave_RBV() throws Exception;

	short getWriteFile() throws Exception;

	void setWriteFile(int writefile) throws Exception;

	short getWriteFile_RBV() throws Exception;

	short getReadFile() throws Exception;

	void setReadFile(int readfile) throws Exception;

	short getReadFile_RBV() throws Exception;

	short getFileFormat() throws Exception;

	void setFileFormat(int fileformat) throws Exception;

	short getFileFormat_RBV() throws Exception;

	short getFileWriteMode() throws Exception;

	void setFileWriteMode(int filewritemode) throws Exception;

	short getFileWriteMode_RBV() throws Exception;

	short getCapture() throws Exception;

	void startCapture() throws Exception;

	short getCapture_RBV() throws Exception;

	int getNumCapture() throws Exception;

	void setNumCapture(int numcapture) throws Exception;

	int getNumCapture_RBV() throws Exception;

	int getNumCaptured_RBV() throws Exception;

	/**
	 * @return initialFileName
	 */
	String getInitialFileName();

	void reset() throws Exception;

	void stopCapture() throws Exception;

	void setStatus(int status);

	int getStatus();

	void getEPICSStatus() throws Exception;

	String getInitialFileTemplate();

	/**
	 * blocking call to start capturing
	 * 
	 * @throws Exception
	 */
	void startCaptureSynchronously() throws Exception;

	void resetFileTemplate() throws Exception;

	void setFileWriteMode(FileWriteMode mode) throws Exception;

	void waitWhileStatusBusy() throws InterruptedException;

	String getWriteMessage() throws Exception;

	// Observer of the WriteMessage PV
	Observable<String> createWriteMessageObservable() throws Exception;

	Boolean isWriteStatusErr() throws Exception;

	// Observer of the WriteStatus PV
	Observable<Short> createWriteStatusObservable() throws Exception;
}