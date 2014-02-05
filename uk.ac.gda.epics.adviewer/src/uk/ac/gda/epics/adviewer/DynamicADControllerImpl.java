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

package uk.ac.gda.epics.adviewer;

import gda.device.detector.areadetector.v17.ADBase;
import gda.device.detector.areadetector.v17.ADBase.ImageMode;
import gda.device.detector.areadetector.v17.FfmpegStream;
import gda.device.detector.areadetector.v17.NDArray;
import gda.device.detector.areadetector.v17.NDProcess;
import gda.device.detector.areadetector.v17.NDROI;
import gda.device.detector.areadetector.v17.NDStats;
import gda.device.detector.areadetector.v17.impl.NDStatsImpl;
import gda.spring.V17ADBaseFactoryBean;
import gda.spring.V17NDStatsFactoryBean;

import org.eclipse.jface.resource.ImageDescriptor;

//<bean id="epgADController"
//class="uk.ac.gda.epics.adviewer.ADControllerImpl">
//<property name="serviceName" value="epg"/>
//<property name="imageNDStats" ref="epg_stat" />
//<property name="liveViewNDProc" ref="epg_proc" />
//<property name="imageNDArray" ref="epg_arr" />
//<property name="imageHistSize" value="256" />
//<property name="imageMin" value="0" />
//<property name="imageMax" value="256" />
//<property name="detectorName" value="epg" />
//<property name="adBase" ref="epg_cam_base" />
//<property name="ffmpegStream" ref="epg_ffmpeg" />
//<property name="ffmpegImageOutHeightMax" value="1200" />
//<property name="ffmpegImageOutWidthMax" value="1600" />
//<property name="cameraImageWidthMax" value="1388" />
//<property name="cameraImageHeightMax" value="1032" />
//<property name="imageNDROI" ref="epg_roi"/>
//</bean>

public class DynamicADControllerImpl implements ADController {

	private NDStatsImpl ndStats;
	final private String pvPrefix;
	final private String statSuffix;
	private String serviceName;
	private ADBase adBase;
	final private String camSuffix;
	int imageHistSize=256;
//	int imageMin=0;
	int imageMax=256;
	int ffmpegImageOutHeightMax=1200;
	int ffmpegImageOutWidthMax=1600;
	int cameraImageWidthMax=1388;
	int cameraImageHeightMax=1032;
	
	public DynamicADControllerImpl(String serviceName, String pvPrefix, String camSuffix, String statSuffix) {
		super();
		this.pvPrefix = pvPrefix;
		this.statSuffix = statSuffix;
		this.serviceName = serviceName;
		this.camSuffix = camSuffix;
	}

	@Override
	public String getServiceName() {
		return serviceName;
	}

	@Override
	public String getDetectorName() {
		return "undefined";
	}

	@Override
	public NDStats getImageNDStats() throws Exception {
		if( ndStats == null){
			V17NDStatsFactoryBean factoryBean = new gda.spring.V17NDStatsFactoryBean();
			factoryBean.setPrefix(pvPrefix+statSuffix);
//			factoryBean.setBeanName(name+"STAT");
			factoryBean.afterPropertiesSet();
			ndStats = (NDStatsImpl)factoryBean.getObject();
		}
		return ndStats;
	}

	@Override
	public NDProcess getLiveViewNDProc() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getImageHistSize() throws Exception {
		int histSize = getImageNDStats().getHistSize();
		if(histSize==0)
			return imageHistSize;
		return getImageNDStats().getHistSize();
	}

	@Override
	public int getImageMin() throws Exception {
		return (int) getImageNDStats().getHistMin();
	}

	@Override
	public int getImageMax() throws Exception {
		double histMax = getImageNDStats().getHistMax();
		if(histMax==0)
			return imageMax;
		return (int)histMax;
	}

	@Override
	public NDArray getImageNDArray() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getImageNDArrayPortInput() throws Exception {
		return getAdBase().getPortName_RBV();
	}

	@Override
	public ImageData getImageData() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setExposure(double d) throws Exception {
		getAdBase().setAcquireTime(d);
		getAdBase().setImageMode(ImageMode.CONTINUOUS.ordinal());
		getAdBase().startAcquiring();
	}

	@Override
	public ADBase getAdBase() throws Exception {
		if( adBase == null){
			V17ADBaseFactoryBean factoryBean = new gda.spring.V17ADBaseFactoryBean();
			factoryBean.setPrefix(pvPrefix+camSuffix);
//			factoryBean.setBeanName(name+"STAT");
			factoryBean.afterPropertiesSet();
			adBase = factoryBean.getObject();
		}
		return adBase;

	}

	@Override
	public void setLiveViewRange(double d, double e) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public FfmpegStream getFfmpegStream() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getFfmpegImageOutWidthMax() {
		return ffmpegImageOutWidthMax;
	}

	@Override
	public int getFfmpegImageOutHeightMax() {
		return ffmpegImageOutHeightMax;
	}

	@Override
	public int getCameraImageWidthMax() {
		return cameraImageWidthMax;
	}

	@Override
	public int getCameraImageHeightMax() {
		return cameraImageHeightMax;
	}

	@Override
	public void startFfmpegStream() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void stopFfmpegStream() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public ImageDescriptor getTwoDarrayViewImageDescriptor() {
		return null;
	}

	@Override
	public ImageDescriptor getLiveViewImageDescriptor() {
		return null;
	}

	@Override
	public ImageDescriptor getHistogramViewImageDescriptor() {
		return null;
	}

	@Override
	public int getFfmpegImageInHeight() throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getFfmpegImageInWidth() throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getFfmpegImageInOffsetX() throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getFfmpegImageInOffsetY() throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getHistogramMinCallbackTime() {
		return 1;
	}

	@Override
	public double getArrayMinCallbackTime() {
		return 1;
	}

	@Override
	public NDROI getImageNDROI() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void stopExposure() throws Exception {
		getAdBase().stopAcquiring();
	}

	@Override
	public boolean isConnectToPlotServer() {
		return false;
	}

}
