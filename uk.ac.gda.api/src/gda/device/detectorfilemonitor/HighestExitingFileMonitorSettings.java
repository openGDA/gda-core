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

import java.io.File;
import java.io.Serializable;

/**
 * Settings that are use by the HighestExitingFileMonitor interface
 * 
 * Implementations look for files that match String.format(fileTemplatePrefix + fileTemplate, startNumber)
 */
public class HighestExitingFileMonitorSettings implements Serializable{

	@Override
	public String toString() {
		return "HighestExitingFileMonitorSettings [fileTemplatePrefix=" + fileTemplatePrefix + ", fileTemplate="
				+ fileTemplate + ", startNumber=" + startNumber + "]";
	}

	public String fileTemplatePrefix;
	public String fileTemplate; 
	public int startNumber;

	public HighestExitingFileMonitorSettings(String fileTemplatePrefix, String fileTemplate, int startNumber) {
		super();
		if( fileTemplate == null )
			throw new IllegalArgumentException("fileTemplate  is null ");
		if( fileTemplatePrefix == null )
			throw new IllegalArgumentException("fileTemplatePrefix  is null ");
		this.fileTemplatePrefix = fileTemplatePrefix;
		this.fileTemplate = fileTemplate;
		this.startNumber = startNumber;
	}

	public String getFullTemplate() {
		String templateInUse = fileTemplatePrefix;
		if (!templateInUse.endsWith(File.separator) && fileTemplate.length()>0) {
			templateInUse += File.separator;
		}
		templateInUse += fileTemplate;
		return templateInUse;
	}

	public boolean isEmpty() {
		return fileTemplatePrefix.isEmpty() && fileTemplate.isEmpty();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((fileTemplate == null) ? 0 : fileTemplate.hashCode());
		result = prime * result + ((fileTemplatePrefix == null) ? 0 : fileTemplatePrefix.hashCode());
		result = prime * result + startNumber;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		HighestExitingFileMonitorSettings other = (HighestExitingFileMonitorSettings) obj;
		if (fileTemplate == null) {
			if (other.fileTemplate != null)
				return false;
		} else if (!fileTemplate.equals(other.fileTemplate))
			return false;
		if (fileTemplatePrefix == null) {
			if (other.fileTemplatePrefix != null)
				return false;
		} else if (!fileTemplatePrefix.equals(other.fileTemplatePrefix))
			return false;
		if (startNumber != other.startNumber)
			return false;
		return true;
	}
	
	
}
