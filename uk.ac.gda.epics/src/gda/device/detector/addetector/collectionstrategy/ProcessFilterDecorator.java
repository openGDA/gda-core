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
import gda.device.detector.nxdetector.plugin.areadetector.ADProcPlugin.DataType;
import gda.scan.ScanInformation;

/**
 * A decorator for configuring the Recursive Filter parameters of the EPICS Process plugin using the decorated collection strategy.
 *
 * It enables a recursive process over multiple images collected from the camera using selected algorithm specific in {@link NDProcess} filter type,
 * it then passes the output image to next plugin in the pipeline (e.g.filewriters, image stream, etc).
 *
 * Note that any receiving plugin need to set their input port to this ndProcess port name.
 *<p>
 * Example bean definition:
 * <pre>
 * {@code
 <bean id="pcoprocfilter" class="gda.device.detector.addetector.collectionstrategy.ProcessFilterDecorator">
	<property name="restoreState" value="true"/>
	<property name="ndProcess" ref="pco_proc"/>
	<property name="processDataTypeOut" value="DatatypeOut_UInt8"/>
	<property name="outputEveryArray" value="true"/>
	<property name="resetFilterAtStart" value="true"/>
	<property name="autoReset" value="false"/>
	<property name="filterType">
		<util:constant static-field="gda.device.detector.areadetector.v17.NDProcess.FilterTypeV1_8_RecursiveAve"/>
	</property>
	<property name="numberOfImagesToFilter" value="3"/>
	<property name="enableFilter" value="true"/>
	<property name="decoratee" ref="pcoacquireperiod"/>
</bean>
}
</pre>
 */
public class ProcessFilterDecorator extends AbstractADCollectionStrategyDecorator {

	private static final Logger logger = LoggerFactory.getLogger(ProcessFilterDecorator.class);

	private NDProcess ndProcess=null;
	private DataType processDataTypeOut=DataType.UINT8; // UINT32
	private boolean outputEveryArray = false;
	private boolean resetFilterAtStart=false;
	private boolean autoReset=false;
	private int filterType=NDProcess.FilterTypeV1_8_RecursiveAve;
	private int numberOfImagesToFilter;
	private boolean enableFilter=false;
	private boolean restoreState=false;

	private short filterTypeSaved;

	private int numFilterSaved;

	private short resetFilterSaved;

	private int autoResetFilterSaved;

	private int filterCallbacksSaved;

	private short enableFilterSaved;

	private short dataTypeOutSaved;

	private boolean callbackEnabledSaved;

	// NXCollectionStrategyPlugin interface
	@Override
	protected void rawPrepareForCollection(double collectionTime, int numberImagesPerCollection, ScanInformation scanInfo) throws Exception {
		logger.trace("rawPrepareForCollection({}, {}, {})", collectionTime, numberImagesPerCollection, scanInfo);

		ndProcess.setFilterType(getFilterType());
		ndProcess.setNumFilter(getNumberOfImagesToFilter());
		ndProcess.setResetFilter(isResetFilterAtStart() ? 1 : 0);
		ndProcess.setAutoResetFilter(isAutoReset() ? 1 : 0);
		if (isOutputEveryArray()) {
			ndProcess.setFilterCallbacks(NDProcess.FilterCallback_EveryArray);
		} else {
			ndProcess.setFilterCallbacks(NDProcess.FilterCallback_ArrayNOnly);
		}
		ndProcess.setEnableFilter(isEnableFilter() ? 1 : 0);
		ndProcess.setDataTypeOut(processDataTypeOut.getValue());
		ndProcess.getPluginBase().setArrayCounter(0);
		ndProcess.getPluginBase().setDroppedArrays(0);
		ndProcess.getPluginBase().enableCallbacks();
		getDecoratee().prepareForCollection(collectionTime, numberImagesPerCollection, scanInfo);
	}

	// CollectionStrategyBeanInterface interface
	@Override
	public void saveState() throws Exception {
		logger.trace("saveState() called, restoreState={}", restoreState);
		getDecoratee().saveState();
		if (isRestoreState()) {
			filterTypeSaved = ndProcess.getFilterType();
			numFilterSaved = ndProcess.getNumFilter();
			resetFilterSaved = ndProcess.getResetFilter();
			autoResetFilterSaved = ndProcess.getAutoResetFilter();
			filterCallbacksSaved = ndProcess.getFilterCallbacks();
			enableFilterSaved = ndProcess.getEnableFilter();
			dataTypeOutSaved = ndProcess.getDataTypeOut();
			callbackEnabledSaved = ndProcess.getPluginBase().isCallbackEnabled();
			logger.debug("Saved State now filterTypeSaved={}, numFilterSaved={}, resetFilterSaved={}, autoResetFilterSaved={}, filterCallbacksSaved={}, enableFilterSaved={}, dataTypeOutSaved={}, callbackEnabledSaved={}", filterTypeSaved, numFilterSaved, numFilterSaved, autoResetFilterSaved,filterCallbacksSaved,enableFilterSaved,dataTypeOutSaved,callbackEnabledSaved);
		}
	}

	@Override
	public void restoreState() throws Exception {
		logger.trace("restoreState() called, restoreState={}", restoreState);
		if (isRestoreState()) {
			ndProcess.setFilterType(filterTypeSaved);
			ndProcess.setNumFilter(numFilterSaved);
			ndProcess.setResetFilter(resetFilterSaved);
			ndProcess.setAutoResetFilter(autoResetFilterSaved);
			ndProcess.setFilterCallbacks(filterCallbacksSaved);
			ndProcess.setEnableFilter(enableFilterSaved);
			ndProcess.setDataTypeOut(dataTypeOutSaved);
			if (callbackEnabledSaved) {
				ndProcess.getPluginBase().enableCallbacks();
			} else {
				 ndProcess.getPluginBase().disableCallbacks();
			}

			logger.debug("Restored state to filterTypeSaved={}, numFilterSaved={}, resetFilterSaved={}, autoResetFilterSaved={}, filterCallbacksSaved={}, enableFilterSaved={}, dataTypeOutSaved={}, callbackEnabledSaved={}", filterTypeSaved, numFilterSaved, numFilterSaved, autoResetFilterSaved,filterCallbacksSaved,enableFilterSaved,dataTypeOutSaved,callbackEnabledSaved);
		}
		getDecoratee().restoreState();
	}

	// InitializingBean interface
	@Override
	public void afterPropertiesSet() throws Exception {
		if (ndProcess == null) throw new IllegalStateException("ndProcess is not set!");
		super.afterPropertiesSet();
	}

	// Class properties
	public NDProcess getNdProcess() {
		return ndProcess;
	}

	public void setNdProcess(NDProcess ndProcess) {
		errorIfPropertySetAfterBeanConfigured("ndProcess");
		this.ndProcess = ndProcess;
	}

	public DataType getProcessDataTypeOut() {
		return processDataTypeOut;
	}

	public void setProcessDataTypeOut(DataType processDataTypeOut) {
		this.processDataTypeOut = processDataTypeOut;
	}

	public boolean isOutputEveryArray() {
		return outputEveryArray;
	}

	public void setOutputEveryArray(boolean outputEveryArray) {
		this.outputEveryArray = outputEveryArray;
	}

	public int getNumberOfImagesToFilter() {
		return numberOfImagesToFilter;
	}

	public void setNumberOfImagesToFilter(int numberOfImagesToFilter) {
		this.numberOfImagesToFilter = numberOfImagesToFilter;
	}

	public boolean isAutoReset() {
		return autoReset;
	}

	public void setAutoReset(boolean autoReset) {
		this.autoReset = autoReset;
	}

	public boolean isEnableFilter() {
		return enableFilter;
	}

	public void setEnableFilter(boolean enableFilter) {
		this.enableFilter = enableFilter;
	}

	public boolean isResetFilterAtStart() {
		return resetFilterAtStart;
	}

	public void setResetFilterAtStart(boolean resetFilterAtStart) {
		this.resetFilterAtStart = resetFilterAtStart;
	}

	public boolean isRestoreState() {
		return restoreState;
	}

	public void setRestoreState(boolean restoreState) {
		this.restoreState = restoreState;
	}

	public int getFilterType() {
		return filterType;
	}

	public void setFilterType(int filterType) {
		this.filterType = filterType;
	}
}
