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

package gda.device.detectorfilemonitor;





/**
 * Interface of object that can report the canonical path of an existing file 
 * that matches a given template with the highest index.
 * The filename is created by using String.format(template, index)
 * 
 * Useful when a detector takes a file name template and start number and then
 * automatically creates the filename for each frame.
 * 
 * Particularly useful when detectors cannot inform observers of the latest file 
 * 
 * updates observers with HighestExistingFileMonitorData
 */
public interface HighestExistingFileMonitor extends HighestExistingFileMonitorDataProvider{
	public HighestExitingFileMonitorSettings getHighestExitingFileMonitorSettings();

	public void setHighestExitingFileMonitorSettings(HighestExitingFileMonitorSettings highestExitingFileMonitorSettings);


	
	public long getDelayInMS();

	public void setDelayInMS(long delay);

	void setRunning(boolean running);
	boolean isRunning();

}
