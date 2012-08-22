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

import gda.device.detector.addetectorprovisional.ADDetectorPlugin;



public interface FileWriter extends ADDetectorPlugin{

	public boolean isSetFileNameAndNumber();

	public void setSetFileNameAndNumber(boolean setFileWriterNameNumber);

	public void setEnable(boolean enable);
	
	public boolean getEnable(); // TODO Rename isEnable to result in Jython enable boolean property
	
	void enableCallback(boolean enable) throws Exception;// TODO Required?

	void disableFileWriter() throws Exception;// TODO Required?

	boolean isLinkFilepath();

	public String getFullFileName_RBV()  throws Exception; // TODO Rename getFullFileName
	
}
