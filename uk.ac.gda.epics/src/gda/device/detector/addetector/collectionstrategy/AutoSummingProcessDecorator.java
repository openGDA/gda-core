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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.detector.areadetector.v17.NDProcess;
import gda.scan.ScanInformation;

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

	private static final Logger logger = LoggerFactory.getLogger(AutoSummingProcessDecorator.class);

	private NDProcess ndProcess=null;

	private int processDataTypeOut=5; // UINT32
	private int filterType = NDProcess.FilterTypeV1_8_Sum;
	private int imagesPerCollectionMultiplier;
	private int enableHighClip = 0;
	private int enableLowClip = 0;
	private int enableOffsetScale = 0;
	private int enableFlatField = 0;
	private int enableBackground = 0;
	private int autoResetFilter = 1;
	private int UID;

	private boolean outputEveryArray = false;
	private boolean restoreState = false;
	private boolean applyFlatFieldSettings = true;
	private boolean applyProcessDataTypeOutSettings = true;
	private boolean skipFrame = false; // Only works in Continuous mode
	private boolean useFramesNumber = false; // If true then exposure will be treated as number of frames

	private int 	initialNumFilter;
	private int 	initialFilterCallback;
	private int 	initialFilterType;
	private short 	initialDataTypeOut;
	private boolean initialProcCallbackEnabled;
	private short 	initialEnableFlatField;

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
	protected void rawPrepareForCollection(double collectionTime, int numberImagesPerCollection, ScanInformation scanInfo) throws Exception {
		logger.trace("rawPrepareForCollection({}, {}, {})", collectionTime, numberImagesPerCollection, scanInfo);
		if (numberImagesPerCollection != getDecoratee().getNumberImagesPerCollection(collectionTime))
			logger.warn("numberImagesPerCollection {} not equal to getDecoratee().getNumberImagesPerCollection({})", numberImagesPerCollection, collectionTime,
					getDecoratee().getNumberImagesPerCollection(collectionTime));

		int totalImagesPerCollection = calcNumberImagesPerCollection(collectionTime);

		if (ndProcess != null) {
			ndProcess.setFilterType(getFilterType());
			ndProcess.setNumFilter(totalImagesPerCollection);
			ndProcess.setAutoResetFilter(getAutoResetFilter());
			if (isOutputEveryArray()) ndProcess.setFilterCallbacks(NDProcess.FilterCallback_EveryArray);
				else ndProcess.setFilterCallbacks(NDProcess.FilterCallback_ArrayNOnly);
			ndProcess.setEnableFilter(1);
			ndProcess.setEnableHighClip(getEnableHighClip());
			ndProcess.setEnableLowClip(getEnableLowClip());
			ndProcess.setEnableOffsetScale(getEnableOffsetScale());
			ndProcess.setEnableBackground(getEnableBackground());
			ndProcess.getPluginBase().setArrayCounter(0);
			ndProcess.getPluginBase().setDroppedArrays(0);
			ndProcess.getPluginBase().disableCallbacks();
			if (applyFlatFieldSettings) ndProcess.setEnableFlatField(getEnableFlatField());
			if (applyProcessDataTypeOutSettings) ndProcess.setDataTypeOut(getProcessDataTypeOut());
		}
		getDecoratee().prepareForCollection(collectionTime, totalImagesPerCollection, scanInfo);
	}

	@Override
	public void collectData() throws Exception {
		// autoreset only works in numFiltered== numFilter which is not the case as we have just reset numFilter
		ndProcess.setResetFilter(1);
		// Wait until next frame to eliminate possible moving scannables effect
		// Will only work in Continuous Mode and acquire state = 1 - no Acquire must be triggered in decoratee!!!
		if ((skipFrame) && (getAdBase().getAcquireState() == 1) && (getAdBase().getImageMode_RBV() == 2)){
			UID = getAdBase().getArrayCounter_RBV();
			logger.trace("Skipping frame with unique ID {}", UID);
			while (UID==getAdBase().getArrayCounter_RBV()) {
				Thread.sleep(50);
			}
		}
		ndProcess.getPluginBase().enableCallbacks();
		getDecoratee().collectData();
	}

	// InitializingBean interface

	@Override
	public void afterPropertiesSet() throws Exception {
		if (ndProcess == null) throw new RuntimeException("ndProcess is not set");
		super.afterPropertiesSet();
	}

	@Override
	public void saveState() throws Exception {
		getDecoratee().saveState();
		if (isRestoreState()) {
			initialDataTypeOut 		 	= ndProcess.getDataTypeOut();
			initialNumFilter 		 	= ndProcess.getNumFilter_RBV();
			initialFilterCallback	 	= ndProcess.getFilterCallbacks();
			initialProcCallbackEnabled 	= ndProcess.getPluginBase().isCallbackEnabled();
			initialFilterType 			= ndProcess.getFilterType();
			initialEnableFlatField 		= ndProcess.getEnableFlatField();
		}
	}

	@Override
	public void restoreState() throws Exception {
		if (isRestoreState()) {
			logger.trace("restoreState() called");
			if (ndProcess != null) {
				ndProcess.reset();
				ndProcess.setDataTypeOut(initialDataTypeOut);
				ndProcess.setNumFilter(initialNumFilter);
				ndProcess.setFilterCallbacks(initialFilterCallback);
				ndProcess.setFilterType(initialFilterType);
				ndProcess.setEnableFlatField(initialEnableFlatField);
				if (initialProcCallbackEnabled) ndProcess.getPluginBase().enableCallbacks();
					else ndProcess.getPluginBase().disableCallbacks();
			}
		}
		getDecoratee().restoreState();
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
		int imagesPerCollection;
		if (isUseFramesNumber()) {
			logger.debug("Using total acquire time as number of frames");
			imagesPerCollectionMultiplier = (int) collectionTime;
		} else {
			logger.debug("calcNumberImagesPerCollection({})...", collectionTime);
			double acquireTime = getDecoratee().getAcquireTime();
			imagesPerCollectionMultiplier = (int)Math.ceil(collectionTime/acquireTime);
		}
		imagesPerCollection = getDecoratee().getNumberImagesPerCollection(collectionTime) * imagesPerCollectionMultiplier;
		logger.debug("...imagesPerCollectionMultiplier={}, returning {}", imagesPerCollectionMultiplier, imagesPerCollection);
		logger.debug("Number of frames {}", imagesPerCollection);
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

	public boolean isOutputEveryArray() {
		return outputEveryArray;
	}

	public void setOutputEveryArray(boolean outputEveryArray) {
		this.outputEveryArray = outputEveryArray;
	}

	public boolean isRestoreState() {
		return restoreState;
	}

	public void setRestoreState(boolean restoreState) {
		this.restoreState = restoreState;
	}

	public int getEnableHighClip() {
		return enableHighClip;
	}

	public void setEnableHighClip(int enableHighClip) {
		this.enableHighClip = enableHighClip;
	}

	public int getEnableLowClip() {
		return enableLowClip;
	}

	public void setEnableLowClip(int enableLowClip) {
		this.enableLowClip = enableLowClip;
	}

	public int getEnableOffsetScale() {
		return enableOffsetScale;
	}

	public void setEnableOffsetScale(int enableOffsetScale) {
		this.enableOffsetScale = enableOffsetScale;
	}

	public int getEnableFlatField() {
		return enableFlatField;
	}

	public void setEnableFlatField(int enableFlatField) {
		this.enableFlatField = enableFlatField;
	}

	public int getEnableBackground() {
		return enableBackground;
	}

	public void setEnableBackground(int enableBackground) {
		this.enableBackground = enableBackground;
	}


	public int getFilterType() {
		return filterType;
	}

	public void setFilterType(int filterType) {
		this.filterType = filterType;
	}

	public int getAutoResetFilter() {
		return autoResetFilter;
	}

	public void setAutoResetFilter(int autoResetFilter) {
		this.autoResetFilter = autoResetFilter;
	}

	public void setApplyFlatFieldSettings(boolean applyFlatFieldSettings) {
		this.applyFlatFieldSettings = applyFlatFieldSettings;
	}

	public void setApplyProcessDataTypeOutSettings(boolean applyProcessDataTypeOutSettings) {
		this.applyProcessDataTypeOutSettings = applyProcessDataTypeOutSettings;
	}

	public boolean isSkipFrame() {
		return skipFrame;
	}

	public void setSkipFrame(boolean skipFrame) {
		this.skipFrame = skipFrame;
	}

	public boolean isUseFramesNumber() {
		return useFramesNumber;
	}

	public void setUseFramesNumber(boolean useFramesNumber) {
		this.useFramesNumber = useFramesNumber;
	}

}
