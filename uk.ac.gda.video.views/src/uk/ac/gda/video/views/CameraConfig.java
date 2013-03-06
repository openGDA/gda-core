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

package uk.ac.gda.video.views;

import org.springframework.beans.factory.InitializingBean;

public class CameraConfig implements ICameraConfig, InitializingBean{

	CameraParameters[] cameras;

	//Eclipse id of PlotView to send image to for analysis 
	String plotViewID;
	
	@Override
	public CameraParameters[] getCameras() {
		return cameras;
	}

	@Override
	public void setCameras(CameraParameters[] cameraList) {
		cameras = cameraList;
		
	}

	@Override
	public String getPlotViewID() {
		return plotViewID;
	}

	public void setPlotViewID(String plotViewID) {
		this.plotViewID = plotViewID;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if( cameras == null || cameras.length==0)
			throw new IllegalArgumentException("cameras is null or empty");
		if( plotViewID == null )
			throw new IllegalArgumentException("plotViewID is null");
		
	}


}
