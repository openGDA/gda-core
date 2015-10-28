/*-
 * Copyright © 2015 Diamond Light Source Ltd.
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

package gda.device.detector.addetector.collectionstrategy;

import gda.device.detector.areadetector.v17.ADBase.ImageMode;
import gda.device.detector.areadetector.v17.NDProcess;
import gda.scan.ScanInformation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class causes the decorated collection strategy to request the detector collects multiple images, it then sums them and
 * passes the single image to next step in the pipeline (often one or more filewriters).
 *
 * This is useful when a detector either has a limited duration acquisition, or a limited number of set acquisition
 * times available. It allows GDA to collect for an arbitrary collectionTime.
 *
 * Note that any filewriters will need to set their input to the output of the ndProcess configured for this strategy rather
 * than taking their input from their usual input array port.
 */
public class AutoSummingProcessDecorator extends AbstractADCollectionStrategyDecorator {

	// Class properties
	private NDProcess ndProcess=null;
	private int processDataTypeOut=5; // UINT32	

	// Instance variables
	private static final Logger logger = LoggerFactory.getLogger(AutoSummingProcessDecorator.class);
	private int imagesPerCollectionMultiplier;

	// NXCollectionStrategyPlugin interface

	@Override
	public double getAcquireTime() throws Exception {
		return getDecoratee().getAcquireTime() * imagesPerCollectionMultiplier;
	}

	@Override
	public double getAcquirePeriod() throws Exception {
		return getDecoratee().getAcquirePeriod() * imagesPerCollectionMultiplier;
	}

	@Override
	public void prepareForCollection(double collectionTime, int numberImagesPerCollection, ScanInformation scanInfo) 
			throws Exception {
		logger.trace("prepareForCollection({}, {}, {})", collectionTime, numberImagesPerCollection, scanInfo);
		if (numberImagesPerCollection != getDecoratee().getNumberImagesPerCollection(collectionTime))
			logger.warn("numberImagesPerCollection {} not equal to getDecoratee().getNumberImagesPerCollection({})",
					numberImagesPerCollection, collectionTime, getDecoratee().getNumberImagesPerCollection(collectionTime));

		int totalImagesPerCollection = calcNumberImagesPerCollection(collectionTime);

		if( ndProcess != null){
			ndProcess.setFilterType(NDProcess.FilterTypeV1_8_Sum);
			ndProcess.setNumFilter(totalImagesPerCollection);
			ndProcess.setAutoResetFilter(1);
			ndProcess.setFilterCallbacks(NDProcess.FilterCallback_ArrayNOnly); 
			ndProcess.setEnableFilter(1);
			ndProcess.setEnableHighClip(0);
			ndProcess.setEnableLowClip(0);
			ndProcess.setEnableOffsetScale(0);
			ndProcess.setEnableFlatField(0);
			ndProcess.setEnableBackground(0);
			ndProcess.getPluginBase().setArrayCounter(0);
			ndProcess.getPluginBase().setDroppedArrays(0);
			ndProcess.setDataTypeOut(processDataTypeOut);		
			ndProcess.getPluginBase().disableCallbacks();
		}
		getDecoratee().prepareForCollection(collectionTime, totalImagesPerCollection, scanInfo);
	}

	@Override
	public void collectData() throws Exception {
		// autoreset only works in numFiltered== numFilter which is not the case as we have just reset numFilter
		ndProcess.setResetFilter(1);
		ndProcess.getPluginBase().enableCallbacks();
		getDecoratee().collectData();
	}

	// InitializingBean interface

	@Override
	public void afterPropertiesSet() throws Exception {
		if (ndProcess == null) throw new RuntimeException("ndProcess is not set");
		super.afterPropertiesSet();
	}

	// Class methods

	public void setAcquireTime(double acquireTime) {
		try {
			getAdBase().setAcquireTime(acquireTime);
		} catch (Exception e) {
			logger.error("Unable to set acquireTime on {}", getName(), e);
		}
	}

	public int calcNumberImagesPerCollection(double collectionTime) throws Exception {
		logger.trace("calcNumberImagesPerCollection({})...", collectionTime);
		double acquireTime = getDecoratee().getAcquireTime();
		imagesPerCollectionMultiplier = (int)Math.ceil(collectionTime/acquireTime);
		int imagesPerCollection = getDecoratee().getNumberImagesPerCollection(collectionTime) * imagesPerCollectionMultiplier;
		logger.trace("...imagesPerCollectionMultiplier={}, returning {}", imagesPerCollectionMultiplier, imagesPerCollection);
		return imagesPerCollection;
	}

	// Class properties

	public NDProcess getNdProcess() {
		return ndProcess;
	}

	public void setNdProcess(NDProcess ndProcess) {
		errorIfPropertySetAfterBeanConfigured("ndProcess");
		this.ndProcess = ndProcess;
	}

	public int getProcessDataTypeOut() {
		return processDataTypeOut;
	}

	public void setProcessDataTypeOut(int processDataTypeOut) {
		this.processDataTypeOut = processDataTypeOut;
	}
}
