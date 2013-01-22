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
import gda.device.detector.areadetector.v17.NDStats;

import org.eclipse.jface.resource.ImageDescriptor;

public interface ADController {

	public abstract NDStats getImageNDStats();

	public abstract NDProcess getLiveViewNDProc();

	public abstract int getImageHistSize();

	public abstract int getImageMin();

	public abstract int getImageMax();

	public abstract String getDetectorName();

	public abstract NDArray getImageNDArray();

	public abstract ImageData getImageData() throws Exception;

	public abstract void setExposure(double d);

	public abstract ADBase getAdBase();

	public abstract void setLiveViewRange(double d, double e) throws Exception;

	public abstract FfmpegStream getFfmpegStream();

	public abstract int getfFMpegImgWidthRequired();

	public abstract int getfFMpegImgHeightRequired();

	public abstract void startFfmpegStream() throws Exception;


	public abstract void stopFfmpegStream() throws Exception;

	public abstract ImageDescriptor getTwoDarrayViewImageDescriptor();

	public abstract ImageDescriptor getLiveViewImageDescriptor();

	public abstract ImageDescriptor getHistogramViewImageDescriptor();
	
	//height of the array passed to the mjpeg plugin
	public abstract int getImageHeight() throws Exception;

	//width of the array passed to the mjpeg plugin
	public abstract int getImageWidth() throws Exception;
	

}