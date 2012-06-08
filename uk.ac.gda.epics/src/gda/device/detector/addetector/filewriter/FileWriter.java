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



public interface FileWriter{

	public boolean isSetFileNameAndNumber();

	public void setSetFileNameAndNumber(boolean setFileWriterNameNumber);
	

	/*
	 * Setup fileWriter if this and isReadFilePath are true
	 */
	public void setEnable(boolean enable);
	public boolean getEnable();
	
	/**
	 * 
	 * @param numberImagesPerCollection - number of images per scan data point
	 * @throws Exception
	 */
	void prepareForCollection(int numberImagesPerCollection) throws Exception;
	
	void enableCallback(boolean enable) throws Exception;

	void endCollection() throws Exception;

	void disableFileWriter() throws Exception;

	boolean isLinkFilepath();

	/*
	 * Called to abort scan
	 */
	void stop() throws Exception;
	
	public void atCommandFailure() throws Exception;

	public String getFullFileName_RBV()  throws Exception;
}
