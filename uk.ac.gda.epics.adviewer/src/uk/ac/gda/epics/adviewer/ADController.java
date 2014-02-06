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

	// unique name used to identify this object - also used to name the service
	public String getServiceName();

	/**
	 * @return name used if the config is connected to the plot server -
	 */
	public String getDetectorName();

	public NDStats getImageNDStats() throws Exception;

	public NDProcess getLiveViewNDProc() throws Exception;

	public int getImageHistSize() throws Exception;

	/**
	 * @return Min expected pixel value - should normally be zero
	 * @throws Exception
	 */
	public double getImageMin() throws Exception;

	/**
	 * @return Max expected pixel value
	 * @throws Exception
	 */
	public double getImageMax() throws Exception;

	public NDArray getImageNDArray() throws Exception;

	public String getImageNDArrayPortInput() throws Exception;

	public ImageData getImageData() throws Exception;

	/**
	 * Sets exposure time and puts camera into continuous mode
	 * 
	 * @param d
	 * @throws Exception
	 */
	public void setExposure(double d) throws Exception;

	public ADBase getAdBase() throws Exception;

	public void setLiveViewRange(double d, double e) throws Exception;

	public FfmpegStream getFfmpegStream() throws Exception;

	public int getFfmpegImageOutWidthMax();

	public int getFfmpegImageOutHeightMax();


	public void startFfmpegStream() throws Exception;

	public void stopFfmpegStream() throws Exception;

	// ImageDescriptor - return null for standard icon
	@Deprecated
	public ImageDescriptor getTwoDarrayViewImageDescriptor();

	// ImageDescriptor - return null for standard icon
	@Deprecated
	public ImageDescriptor getLiveViewImageDescriptor();

	// ImageDescriptor - return null for standard icon
	@Deprecated
	public ImageDescriptor getHistogramViewImageDescriptor();

	// height of the array passed to the mjpeg plugin
	public int getFfmpegImageInHeight() throws Exception;

	// width of the array passed to the mjpeg plugin
	public int getFfmpegImageInWidth() throws Exception;

/*	// due to use of a ROI the image arriving at the ffmpeg plugin may be offset in x and Y from the camera 0,0 position
	@Deprecated
	public int getFfmpegImageInOffsetX() throws Exception;

	// due to use of a ROI the image arriving at the ffmpeg plugin may be offset in x and Y from the camera 0,0 position
	public int getFfmpegImageInOffsetY() throws Exception;

*/	public double getHistogramMinCallbackTime();

	public double getArrayMinCallbackTime();

	// NDROI for the ROI plugin that controls the ImageArray
	public NDROI getImageNDROI()  throws Exception;

	// stops the continuous exposure
	public void stopExposure() throws Exception;

	// return true is the regions of interest are linked to the plotserver
	public boolean isConnectToPlotServer();

	public String getSetExposureTimeCmd();
	
}
