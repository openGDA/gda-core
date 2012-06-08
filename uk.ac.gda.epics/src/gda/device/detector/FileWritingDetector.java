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

package gda.device.detector;


public interface FileWritingDetector {

	public void setFilePath(String filePath) throws Exception;
	
	public void setFileName(String fileName) throws Exception;
	
	public void setFileTemplate(String fileTemplate) throws Exception;
	
	public void setFileNumber(int fileNumber) throws Exception;
	
	public void setAutoIncrement(boolean autoIncrement) throws Exception;
	
	public String getFilePath() throws Exception;
	
	public String getFileName() throws Exception;
	
	public String getFileTemplate() throws Exception;
	
	public int getFileNumber() throws Exception;
	
	public boolean getAutoIncrement() throws Exception;
	
	public boolean canWriteFiles();
}
