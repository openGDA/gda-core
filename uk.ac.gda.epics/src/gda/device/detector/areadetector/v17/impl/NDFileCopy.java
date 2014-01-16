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

public class NDFileCopy extends NDFileImpl {

	private String copyPluginPrefix="BL13I-EA-DET-01:COPY";

	public String getCopyPluginPrefix() {
		return copyPluginPrefix;
	}

	public void setCopyPluginPrefix(String copyPluginPrefix) {
		this.copyPluginPrefix = copyPluginPrefix;
	}

	@Override
	public void setFilePath(String filepath) throws Exception {
		String pathToSend  = getInternalPath(filepath);
		EPICS_CONTROLLER.caputWait(createChannel(copyPluginPrefix+":CreatePath"), (pathToSend + '\0').getBytes());
		EPICS_CONTROLLER.caputWait(createChannel(copyPluginPrefix+":CreatePathDo"),1);
		Boolean success = EPICS_CONTROLLER.cagetInt(createChannel(copyPluginPrefix+":CreateStatus_RBV"))==1;
		if( !success)
			throw new Exception("Unable to create directory:'" + pathToSend +"'");
		super.setFilePath(filepath);
	}


}
