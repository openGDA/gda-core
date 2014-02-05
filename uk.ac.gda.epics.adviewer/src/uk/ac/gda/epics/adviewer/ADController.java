/*-
 * Copyright © 2012 Diamond Light Source Ltd.
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

package uk.ac.gda.epics.adviewer;

import gda.device.detector.areadetector.v17.ADBase;
import gda.device.detector.areadetector.v17.FfmpegStream;
import gda.device.detector.areadetector.v17.NDArray;
import gda.device.detector.areadetector.v17.NDProcess;
import gda.device.detector.areadetector.v17.NDROI;
import gda.device.detector.areadetector.v17.NDStats;

import org.eclipse.jface.resource.ImageDescriptor;

public interface ADController {

	//unique name used to identify this object - also used to name the service 
	public abstract String getServiceName();
	
	/**
	 * 
	 * @return name used if the config is connected to the plot server -
	 */
	public abstract String getDetectorName();

	public abstract NDStats getImageNDStats() throws Exception;

	public abstract NDProcess getLiveViewNDProc();

	public abstract int getImageHistSize() throws Exception;

	public abstract int getImageMin() throws Exception;

	public abstract int getImageMax() throws Exception;

	public abstract NDArray getImageNDArray();
	
	public abstract String getImageNDArrayPortInput() throws Exception;

	public abstract ImageData getImageData() throws Exception;

	/**
	 * Sets exposure time and puts camera into continuous mode
	 * 
	 * @param d
	 * @throws Exception
	 */
	public abstract void setExposure(double d) throws Exception;

	public abstract ADBase getAdBase() throws Exception;

	public abstract void setLiveViewRange(double d, double e) throws Exception;

	public abstract FfmpegStream getFfmpegStream();

	public abstract int getFfmpegImageOutWidthMax();

	public abstract int getFfmpegImageOutHeightMax();

	/**
	 * 
	 * @return The maximum width of an image that the camera driver can deliver. The actual image width could be
	 * smaller due to setting a region of interest or binning.
	 */
	public abstract int getCameraImageWidthMax();

	/**
	 * 
	 * @return The maximum height of an image that the camera driver can deliver. The actual image width could be
	 * smaller due to setting a region of interest or binning.
	 */
	public abstract int getCameraImageHeightMax();
	
	public abstract void startFfmpegStream() throws Exception;


	public abstract void stopFfmpegStream() throws Exception;

	
	//ImageDescriptor  - return null for standard icon
	@Deprecated
	public abstract ImageDescriptor getTwoDarrayViewImageDescriptor();

	//ImageDescriptor  - return null for standard icon
	@Deprecated
	public abstract ImageDescriptor getLiveViewImageDescriptor();

	//ImageDescriptor  - return null for standard icon
	@Deprecated
	public abstract ImageDescriptor getHistogramViewImageDescriptor();
	
	//height of the array passed to the mjpeg plugin
	public abstract int getFfmpegImageInHeight() throws Exception;

	//width of the array passed to the mjpeg plugin
	public abstract int getFfmpegImageInWidth() throws Exception;

	//due to use of a ROI the image arriving at the ffmpeg plugin may be offset in x and Y from the camera 0,0 position 
	public abstract int getFfmpegImageInOffsetX() throws Exception;
	
	//due to use of a ROI the image arriving at the ffmpeg plugin may be offset in x and Y from the camera 0,0 position 
	public abstract int getFfmpegImageInOffsetY() throws Exception;

	public abstract double getHistogramMinCallbackTime();
	
	public abstract double getArrayMinCallbackTime();


	//NDROI for the ROI plugin that controls the ImageArray
	public abstract NDROI getImageNDROI();

	// stops the continuous exposure
	public abstract void stopExposure() throws Exception;

	// return true is the regions of interest are linked to the plotserver
	public abstract boolean isConnectToPlotServer();

}
