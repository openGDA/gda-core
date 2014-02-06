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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

public class ADControllerImpl extends ADControllerBase implements ADController, InitializingBean {
	@SuppressWarnings("hiding")
	static final Logger logger = LoggerFactory.getLogger(ADControllerImpl.class);

	public NDStats imageNDStats;
	public NDProcess liveViewNDProc;
	NDArray imageNDArray;
	ADBase adBase;

	private FfmpegStream ffmpegStream;

	private NDROI imageNDROI;

	@Override
	public NDStats getImageNDStats() {
		return imageNDStats;
	}

	@Override
	public NDProcess getLiveViewNDProc() {
		return liveViewNDProc;
	}

	public void setLiveViewNDProc(NDProcess ndProc) {
		this.liveViewNDProc = ndProc;
	}

	public void setImageNDStats(NDStats ndStats) {
		this.imageNDStats = ndStats;
	}

	public void setImageNDArray(NDArray imageNDArray) {
		this.imageNDArray = imageNDArray;
	}

	@Override
	public NDArray getImageNDArray() {
		return imageNDArray;
	}

	@Override
	public ADBase getAdBase() {
		return adBase;
	}

	public void setAdBase(ADBase adBase) {
		this.adBase = adBase;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (adBase == null)
			throw new Exception("adBase is null");
	}

	@Override
	public FfmpegStream getFfmpegStream() {
		return ffmpegStream;
	}

	public void setFfmpegStream(FfmpegStream ffmpegStream) {
		this.ffmpegStream = ffmpegStream;
	}

	@Override
	public ImageDescriptor getTwoDarrayViewImageDescriptor() {
		return Activator.getImageDescriptor("icons/AreaDetectorImageView.gif");
	}

	@Override
	public ImageDescriptor getLiveViewImageDescriptor() {
		return Activator.getImageDescriptor("icons/AreaDetectorLiveView.gif");
	}

	@Override
	public ImageDescriptor getHistogramViewImageDescriptor() {
		return Activator.getImageDescriptor("icons/AreaDetectorProfileView.gif");
	}

	@Override
	public int getFfmpegImageInHeight() throws Exception {
		int[] dimensions = getDataDimensions(getFfmpegStream().getPluginBase());
		return dimensions.length == 2 ? dimensions[0] : 1;
	}

	@Override
	public int getFfmpegImageInWidth() throws Exception {
		int[] dimensions = getDataDimensions(getFfmpegStream().getPluginBase());
		return dimensions.length == 2 ? dimensions[1] : 1;
	}

/*	@Override
	public int getFfmpegImageInOffsetX() throws Exception {
		return ffmpegImageInOffsetX;
	}

	@Override
	public int getFfmpegImageInOffsetY() throws Exception {
		return ffmpegImageInOffsetY;
	}

	public void setFfmpegImageInOffsetX(int ffmpegImageInOffsetX) {
		this.ffmpegImageInOffsetX = ffmpegImageInOffsetX;
	}

	public void setFfmpegImageInOffsetY(int ffmpegImageInOffsetY) {
		this.ffmpegImageInOffsetY = ffmpegImageInOffsetY;
	}
*/


	
	@Override
	public NDROI getImageNDROI() {
		return imageNDROI;
	}

	public void setImageNDROI(NDROI imageNDROI) {
		this.imageNDROI = imageNDROI;
	}

}
