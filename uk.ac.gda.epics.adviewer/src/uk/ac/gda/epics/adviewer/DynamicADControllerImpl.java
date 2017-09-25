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
import gda.device.detector.areadetector.v17.FfmpegStream;
import gda.device.detector.areadetector.v17.NDArray;
import gda.device.detector.areadetector.v17.NDProcess;
import gda.device.detector.areadetector.v17.NDROI;
import gda.device.detector.areadetector.v17.NDStats;
import gda.spring.V17FactoryBeanBase;

public class DynamicADControllerImpl extends ADControllerBase {

	private final String pvPrefix;
	private ADPVSuffixes adPVSuffixes;

	private ADBase adBase;
	private NDStats ndStats;
	private NDArray imageNDArray;
	private FfmpegStream ffmpegStream;
	private NDROI nDROI;
	private NDProcess ndProc;


	public DynamicADControllerImpl(String serviceName, String detectorName, String pvPrefix, ADPVSuffixes adPVSuffixes) throws Exception {
		this.pvPrefix = pvPrefix;
		this.adPVSuffixes = adPVSuffixes;
		this.detectorName = detectorName;
		setServiceName(serviceName);
		super.afterPropertiesSet();
	}

	@Override
	public String getDetectorName() {
		return detectorName;
	}

	@Override
	public NDStats getImageNDStats() throws Exception {
		if( ndStats == null){
			ndStats = getFromFactory(new gda.spring.V17NDStatsFactoryBean(), adPVSuffixes.getStatSuffix());
		}
		return ndStats;
	}

	@Override
	public NDProcess getLiveViewNDProc() throws Exception {
		if( ndProc == null){
			ndProc = getFromFactory(new gda.spring.V17NDProcessFactoryBean(), adPVSuffixes.getMPGProcSuffix());
		}
		return ndProc;
	}

	@Override
	public NDArray getImageNDArray() throws Exception {
		if( imageNDArray == null){
			imageNDArray = getFromFactory(new gda.spring.V17NDArrayFactoryBean(), adPVSuffixes.getArraySuffix());
		}
		return imageNDArray;
	}


	@Override
	public ADBase getAdBase() throws Exception {
		if( adBase == null){
			adBase = getFromFactory(new gda.spring.V17ADBaseFactoryBean(), adPVSuffixes.getADBaseSuffix());
		}
		return adBase;
	}

	@Override
	public FfmpegStream getFfmpegStream() throws Exception {
		if( ffmpegStream == null){
			ffmpegStream = getFromFactory(new gda.spring.V17FfmpegStreamFactoryBean(), adPVSuffixes.getMPGSuffix());
		}
		return ffmpegStream;
	}
	@Override
	public NDROI getImageNDROI() throws Exception {
		if( nDROI == null){
			nDROI = getFromFactory(new gda.spring.V17NDROIFactoryBean(), adPVSuffixes.getArrayROISuffix());
		}
		return nDROI;
	}

	private <T> T getFromFactory(V17FactoryBeanBase<T> factory, String suffix) throws Exception {
		factory.setPrefix(pvPrefix+suffix);
		factory.afterPropertiesSet();
		return factory.getObject();
	}


}