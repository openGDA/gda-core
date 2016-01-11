/*-
 * Copyright © 2014 Diamond Light Source Ltd.
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

package gda.device.detector.areadetector.v17.impl;

import gda.device.detector.areadetector.v17.NDFile;
import gda.device.detector.areadetector.v17.NDFileHDF5;

import java.util.Vector;

/**
 * Simulation of NDFileHDF5
 */
public class NDFileHDF5Simulator implements NDFileHDF5 {

	private NDFile ndFile;

	@Override
	public int getNumRowChunks() throws Exception {
		return 0;
	}

	@Override
	public void setNumRowChunks(int value) throws Exception {
	}

	@Override
	public int getNumColChunks() throws Exception {
		return 0;
	}

	@Override
	public void setNumColChunks(int value) throws Exception {
	}

	@Override
	public int getNumFramesChunks() throws Exception {
		return 0;
	}

	@Override
	public void setNumFramesChunks(int value) throws Exception {
	}

	@Override
	public int getNumFramesFlush() throws Exception {
		return 0;
	}

	@Override
	public void setNumFramesFlush(int value) throws Exception {
	}

	@Override
	public int getNumExtraDims() throws Exception {
		return 0;
	}

	@Override
	public void setNumExtraDims(int value) throws Exception {
	}

	@Override
	public int getExtraDimSizeN() throws Exception {
		return 0;
	}

	@Override
	public void setExtraDimSizeN(int value) throws Exception {
	}

	@Override
	public int getExtraDimSizeX() throws Exception {
		return 0;
	}

	@Override
	public void setExtraDimSizeX(int value) throws Exception {
	}

	@Override
	public int getExtraDimSizeY() throws Exception {
		return 0;
	}

	@Override
	public void setExtraDimSizeY(int value) throws Exception {
	}

	@Override
	public double getRuntime() throws Exception {
		return 0;
	}

	@Override
	public double getIOSpeed() throws Exception {
		return 0;
	}

	@Override
	public Vector<String> listCompressionTypes() {
		return null;
	}

	@Override
	public String getCompression() throws Exception {
		return null;
	}

	@Override
	public void setCompression(String type) throws Exception {
	}

	@Override
	public int getNumBitPrecision() throws Exception {
		return 0;
	}

	@Override
	public void setNumBitPrecision(int value) throws Exception {
	}

	@Override
	public int getNumBitOffset() throws Exception {
		return 0;
	}

	@Override
	public void setNumBitOffset(int value) throws Exception {
	}

	@Override
	public int getSzipNumPixels() throws Exception {
		return 0;
	}

	@Override
	public void setSzipNumPixels(int value) throws Exception {
	}

	@Override
	public int getZCompressLevel() throws Exception {
		return 0;
	}

	@Override
	public void setZCompressLevel(int value) throws Exception {
	}

	@Override
	public NDFile getFile() {
		return ndFile;
	}

	// used to configure the bean
	public void setFile(NDFile ndFile) {
		this.ndFile = ndFile;
	}

	@Override
	public void reset() throws Exception {
	}

	@Override
	public void startCapture() throws Exception {
		getFile().startCapture();
	}

	@Override
	public short getCapture() throws Exception {
		return getFile().getCapture();
	}

	@Override
	public void stopCapture() throws Exception {
		getFile().stopCapture();
	}

	@Override
	public String getFilePath() throws Exception {
		return ndFile.getFilePath();
	}

	@Override
	public void setFilePath(String filepath) throws Exception {
		ndFile.setFilePath(filepath);
	}

	@Override
	public String getFileName() throws Exception {
		return ndFile.getFileName();
	}

	@Override
	public void setFileName(String filename) throws Exception {
		ndFile.setFileName(filename);
	}

	@Override
	public int getFileNumber() throws Exception {
		return ndFile.getFileNumber();
	}

	@Override
	public void setFileNumber(int filenumber) throws Exception {
		ndFile.setFileNumber(filenumber);
	}

	@Override
	public short getAutoIncrement() throws Exception {
		return ndFile.getAutoIncrement();
	}

	@Override
	public void setAutoIncrement(int autoincrement) throws Exception {
		ndFile.setAutoIncrement(autoincrement);
	}

	@Override
	public String getFileTemplate() throws Exception {
		return ndFile.getFileTemplate();
	}

	@Override
	public void setFileTemplate(String filetemplate) throws Exception {
		ndFile.setFileTemplate(filetemplate);
	}

	@Override
	public String getFullFileName_RBV() throws Exception {
		return ndFile.getFullFileName_RBV();
	}

	@Override
	public short getAutoSave() throws Exception {
		return ndFile.getAutoSave();
	}

	@Override
	public void setAutoSave(int autosave) throws Exception {
		ndFile.setAutoSave(autosave);
	}

	@Override
	public short getWriteFile() throws Exception {
		return ndFile.getWriteFile();
	}

	@Override
	public void setWriteFile(int writefile) throws Exception {
		ndFile.setWriteFile(writefile);
	}

	@Override
	public short getReadFile() throws Exception {
		return ndFile.getReadFile();
	}

	@Override
	public void setReadFile(int readfile) throws Exception {
		ndFile.setReadFile(readfile);
	}

	public short getReadFile_RBV() throws Exception {
		return ndFile.getReadFile_RBV();
	}

	@Override
	public short getCapture_RBV() throws Exception {
		return ndFile.getCapture_RBV();
	}

	@Override
	public void setNumCapture(int numcapture) throws Exception {
		ndFile.setNumCapture(numcapture);
	}

	@Override
	public int getNumCaptured_RBV() throws Exception {
		return ndFile.getNumCaptured_RBV();
	}

	@Override
	public int getStatus() {
		return ndFile.getStatus();
	}

	@Override
	public int getStoreAttr() throws Exception {
		return 0;
	}

	@Override
	public void setStoreAttr(int storeAttr) throws Exception {
	}

	@Override
	public int getStorePerform() throws Exception {
		return 0;
	}

	@Override
	public void setStorePerform(int storePerform) throws Exception {
	}

	@Override
	public String getArrayPort() throws Exception {
		return null;
	}

	@Override
	public void setLazyOpen(boolean open) throws Exception {
	}

	@Override
	public boolean isLazyOpen() throws Exception {
		return false;
	}

	@Override
	public void setBoundaryAlign(int boundaryAlign) throws Exception {
	}

	@Override
	public int getBoundaryAlign() throws Exception {
		return 0;
	}

	@Override
	public int getAttrByDim() throws Exception {
		return 0;
	}

	@Override
	public void setAttrByDim(int attrByDim) throws Exception {
	}
}
