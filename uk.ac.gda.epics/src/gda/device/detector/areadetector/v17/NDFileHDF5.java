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

import java.util.Vector;

public interface NDFileHDF5 {
	/* Note that NDFileHDF5 doesn't extend GetPluginBaseAvailable since it 'contains an' NDFile rather than being an NDFile. */

	int getNumRowChunks() throws Exception;

	void setNumRowChunks(int value) throws Exception;

	int getNumColChunks() throws Exception;

	void setNumColChunks(int value) throws Exception;

	int getNumFramesChunks() throws Exception;

	void setNumFramesChunks(int value) throws Exception;

	int getNumFramesFlush() throws Exception;

	void setNumFramesFlush(int value) throws Exception;

	int getNumExtraDims() throws Exception;

	void setNumExtraDims(int value) throws Exception;

	int getExtraDimSizeN() throws Exception;

	void setExtraDimSizeN(int value) throws Exception;

	int getExtraDimSizeX() throws Exception;

	void setExtraDimSizeX(int value) throws Exception;

	int getExtraDimSizeY() throws Exception;

	void setExtraDimSizeY(int value) throws Exception;

	double getRuntime() throws Exception;

	double getIOSpeed() throws Exception;

	Vector<String> listCompressionTypes();

	String getCompression() throws Exception;

	void setCompression(String type) throws Exception;

	int getNumBitPrecision() throws Exception;

	void setNumBitPrecision(int value) throws Exception;

	int getNumBitOffset() throws Exception;

	void setNumBitOffset(int value) throws Exception;

	int getSzipNumPixels() throws Exception;

	void setSzipNumPixels(int value) throws Exception;

	int getZCompressLevel() throws Exception;

	void setZCompressLevel(int value) throws Exception;

	NDFile getFile();

	void reset() throws Exception;

	int getStatus();

	void startCapture() throws Exception;

	short getCapture() throws Exception;

	void stopCapture() throws Exception;

	String getFilePath() throws Exception;

	void setFilePath(String filepath) throws Exception;

	String getFileName() throws Exception;

	void setFileName(String filename) throws Exception;

	int getFileNumber() throws Exception;

	void setFileNumber(int filenumber) throws Exception;

	short getAutoIncrement() throws Exception;

	void setAutoIncrement(int autoincrement) throws Exception;

	String getFileTemplate() throws Exception;

	void setFileTemplate(String filetemplate) throws Exception;

	short getAutoSave() throws Exception;

	void setAutoSave(int autosave) throws Exception;

	short getWriteFile() throws Exception;

	void setWriteFile(int writefile) throws Exception;

	short getReadFile() throws Exception;

	void setReadFile(int readfile) throws Exception;

	String getFullFileName_RBV() throws Exception;

	void setNumCapture(int numberOfDarks) throws Exception;

	int getStoreAttr() throws Exception;

	void setStoreAttr(int storeAttr) throws Exception;

	int getStorePerform() throws Exception;

	void setStorePerform(int storePerform) throws Exception;

	short getCapture_RBV() throws Exception;

	int getNumCaptured_RBV() throws Exception;

	String getArrayPort() throws Exception ;

	/**
	 * Lazy open specifies if the file will be created when capture is started or if it will be created when the first frame arrives at the HDF5 writer. If true
	 * the file will only be created when the first frame is written.
	 *
	 * @param lazyOpen
	 *            True to enable lazy open
	 * @throws Exception
	 */
	void setLazyOpen(boolean lazyOpen) throws Exception;

	/**
	 * Lazy open specifies if the file will be created when capture is started or if it will be created when the first frame arrives at the HDF5 writer. If true
	 * the file will only be created when the first frame is written.
	 *
	 * @return True if lazy open is enabled
	 * @throws Exception
	 */
	boolean isLazyOpen() throws Exception;


	/**
	 *
	 * @param boundaryAlign - added in AreaDetector 1-9 . Should be set to 1024*1024
	 * @throws Exception
	 */
	void setBoundaryAlign(int boundaryAlign) throws Exception;
	int getBoundaryAlign() throws Exception;

	/**
	 * If true the NDAttributes written to the HDF5 file by Area Detector will be written with the same dimensions as the frames but with a frame size of [1,
	 * 1]. So a 10x12 scan will result in attributes written as [10, 12, 1, 1]. If false the attributes will be unrolled in to a 1D dataset.
	 *
	 * @return True if attributes will be stored by dimension
	 * @throws Exception
	 */
	boolean isStoreAttributesByDimension() throws Exception;

	/**
	 * If true the NDAttributes written to the HDF5 file by Area Detector will be written with the same dimensions as the frames but with a frame size of [1,
	 * 1]. So a 10x12 scan will result in attributes written as [10, 12, 1, 1]. If false the attributes will be unrolled in to a 1D dataset.
	 *
	 * @param storeAttributesByDimension
	 * @throws Exception
	 */
	void setStoreAttributesByDimension(boolean storeAttributesByDimension) throws Exception;

	/**
	 * Single Writer Multiple Reader SWMR is a technology to allow HDF5 files to be read while they are still being written.
	 *
	 * @return True if SWMR is supported
	 * @throws Exception
	 */
	boolean isSWMRSupported() throws Exception;

	void setExtraDimensions(int[] actualDims) throws Exception;

}