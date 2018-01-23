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

import java.io.Serializable;

/*
 * Interface which is part of the HighestExistingFileMonitorDataProvider interface
 */
public class HighestExistingFileMonitorData implements Serializable{
	HighestExistingFileMonitorSettings highestExistingFileMonitorSettings;//null if not set
	Integer foundIndex; //null if not found
	public HighestExistingFileMonitorData(HighestExistingFileMonitorSettings highestExistingFileMonitorSettings,
			Integer foundIndex) {
		super();
		this.highestExistingFileMonitorSettings = highestExistingFileMonitorSettings;
		this.foundIndex = foundIndex;
	}
	public HighestExistingFileMonitorSettings getHighestExistingFileMonitorSettings() {
		return highestExistingFileMonitorSettings;
	}
	public Integer getFoundIndex() {
		return foundIndex;
	}
	
	

}
