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

import org.eclipse.jface.resource.ImageDescriptor;

import gda.device.detector.areadetector.v17.ADBase;
import gda.device.detector.areadetector.v17.FfmpegStream;
import gda.device.detector.areadetector.v17.NDArray;
import gda.device.detector.areadetector.v17.NDProcess;
import gda.device.detector.areadetector.v17.NDROI;
import gda.device.detector.areadetector.v17.NDStats;

public interface ADController {

	// unique name used to identify this object - also used to name the service
	String getServiceName();

	/**
	 * @return name used if the config is connected to the plot server -
	 */
	String getDetectorName();

	NDStats getImageNDStats() throws Exception;

	NDProcess getLiveViewNDProc() throws Exception;

	int getImageHistSize() throws Exception;

	/**
	 * @return Min expected pixel value - should normally be zero
	 * @throws Exception
	 */
	double getImageMin() throws Exception;

	/**
	 * @return Max expected pixel value
	 * @throws Exception
	 */
	double getImageMax() throws Exception;

	NDArray getImageNDArray() throws Exception;

	String getImageNDArrayPortInput() throws Exception;

	ImageData getImageData() throws Exception;

	/**
	 * Sets exposure time and puts camera into continuous mode
	 *
	 * @param d
	 * @throws Exception
	 */
	void setExposure(double d) throws Exception;

	ADBase getAdBase() throws Exception;

	void setLiveViewRange(double d, double e) throws Exception;

	FfmpegStream getFfmpegStream() throws Exception;

	int getFfmpegImageOutWidthMax();

	int getFfmpegImageOutHeightMax();

	void startFfmpegStream() throws Exception;

	void stopFfmpegStream() throws Exception;

	// ImageDescriptor - return null for standard icon
	@Deprecated(since="GDA 8.38")
	ImageDescriptor getTwoDarrayViewImageDescriptor();

	// ImageDescriptor - return null for standard icon
	@Deprecated(since="GDA 8.38")
	ImageDescriptor getLiveViewImageDescriptor();

	// ImageDescriptor - return null for standard icon
	@Deprecated(since="GDA 8.38")
	ImageDescriptor getHistogramViewImageDescriptor();

	// height of the array passed to the mjpeg plugin
	int getFfmpegImageInHeight() throws Exception;

	// width of the array passed to the mjpeg plugin
	int getFfmpegImageInWidth() throws Exception;

	double getHistogramMinCallbackTime();

	double getArrayMinCallbackTime();

	// NDROI for the ROI plugin that controls the ImageArray
	NDROI getImageNDROI()  throws Exception;

	// stops the continuous exposure
	void stopExposure() throws Exception;

	// return true is the regions of interest are linked to the plotserver
	boolean isConnectToPlotServer();

	String getSetExposureTimeCmd();
}
