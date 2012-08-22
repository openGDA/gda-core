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

package gda.device.detector.addetectorprovisional;

import gda.data.nexus.tree.INexusTree;
import gda.data.nexus.tree.NexusTreeProvider;
import gda.device.DeviceException;
import gda.device.detector.NXDetectorData;
import gda.device.detector.NXDetectorDataWithFilepathForSrs;
import gda.device.detector.NexusDetector;
import gda.device.detector.addetector.filewriter.FileWriter;
import gda.device.detector.addetector.triggering.ADTriggeringStrategy;
import gda.device.detector.addetector.triggering.SimpleAcquire;
import gda.device.detector.addetectorprovisional.data.NXDetectorDataAppender;
import gda.device.detector.areadetector.v17.ADBase;
import gda.device.detector.areadetector.v17.NDArray;
import gda.device.detector.areadetector.v17.NDFile;
import gda.device.detector.areadetector.v17.NDStats;
import gda.device.scannable.MultiplePositionStreamIndexer;
import gda.device.scannable.PositionCallableProvider;
import gda.device.scannable.PositionInputStream;
import gda.factory.FactoryException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

// TODO Check for duplicate extraNames returned by the plugins
// TODO Deliberate change: does not read arrays by default
// TODO Check for duplicate plugin names
// TODO Move 'waiting for file' behaviour into plugin

public class ProvisionalADDetector extends gda.device.detector.addetector.ADDetector implements NexusDetector,
		PositionCallableProvider<NexusTreeProvider> {

	private static final String REMOVED_FROM_PROVISIONAL_ADDETECTOR = "Not supported by provisional ADDetector";
	private List<ADDetectorPlugin> additionalPluginList = new ArrayList<ADDetectorPlugin>();
	private ADArrayPlugin adArrayPlugin;
	private MultiplePositionStreamIndexer<NXDetectorDataAppender> pluginStreamsIndexer;

	public ProvisionalADDetector(String name, ADBase adBase, ADTriggeringStrategy collectionStrategy,
			FileWriter fileWriter) throws Exception {
		setName(name);
		setAdBase(adBase);
		setCollectionStrategy(collectionStrategy);
		setFileWriter(fileWriter);
		afterPropertiesSet();
		configure();
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (afterPropertiesSetCalled)
			throw new RuntimeException("afterPropertiesSet already called");
		if (getAdBase() == null)
			throw new IllegalStateException("adBase is not defined");
		if (getCollectionStrategy() == null) {
			setCollectionStrategy(new SimpleAcquire(getAdBase(), 0.));
		}
		// TODO Removed FileWriter autocreation from ndFile
		afterPropertiesSetCalled = true;
	}

	// Configuration options:

	/**
	 * Set plugins in addition to the collection-strategy, array and file-writer plugins.
	 * 
	 * @param adDetectorPluginList
	 */
	public void setAdditionalPluginList(List<ADDetectorPlugin> adDetectorPluginList) {
		this.additionalPluginList = adDetectorPluginList;
	}

	public void setAdArrayPlugin(ADArrayPlugin adArrayPlugin) {
		this.adArrayPlugin = adArrayPlugin;
	}

	// public void setReadAcquisitionTime(boolean readAcquisitionTime) {

	// public void setReadAcquisitionPeriod(boolean readAcquisitionPeriod) {

	// public void setCheckFileExists(boolean checkFileExists) {

	// public void setDescription(String description) {

	// public void setDetectorType(String detectorType) {

	// public void setDetectorID(String detectorID) {

	// Getters:

	public List<ADDetectorPlugin> getAdditionalPluginList() {
		return additionalPluginList;
	}

	/**
	 * Return all plugins: collection-strategy, array, filewriter and then additional plugins
	 */
	public List<ADDetectorPlugin> getPluginList() {
		List<ADDetectorPlugin> allPlugins = new ArrayList<ADDetectorPlugin>();
		if (getCollectionStrategy() != null) {
			allPlugins.add(getCollectionStrategy());
		}
		if (getAdArrayPlugin() != null) {
			allPlugins.add(getAdArrayPlugin());
		}
		if (getFileWriter() != null) {
			allPlugins.add(getFileWriter());
		}
		allPlugins.addAll(getAdditionalPluginList());
		return allPlugins;
	}

	public ADArrayPlugin getAdArrayPlugin() {
		return adArrayPlugin;
	}

	// return getAdBase();

	// public ADTriggeringStrategy getCollectionStrategy() {

	// public FileWriter getFileWriter() {

	// public NDFile getNdFile() {

	// public String getDescription() throws DeviceException {

	// public String getDetectorID() throws DeviceException {

	// public String getDetectorType() throws DeviceException {

	// public boolean isCheckFileExists() {

	// public boolean createsOwnFiles() throws DeviceException {

	// Removed:

	@Override
	public void setNdStats(NDStats ndStats) {
		throw new RuntimeException(REMOVED_FROM_PROVISIONAL_ADDETECTOR);
	}

	@Override
	public void setNdArray(NDArray ndArray) {
		throw new RuntimeException(REMOVED_FROM_PROVISIONAL_ADDETECTOR);
	}

	@Override
	public NDArray getNdArray() {
		throw new RuntimeException(REMOVED_FROM_PROVISIONAL_ADDETECTOR);
	}

	@Override
	public void setComputeStats(boolean computeStats) {
		throw new RuntimeException(REMOVED_FROM_PROVISIONAL_ADDETECTOR);
	}

	@Override
	public void setComputeCentroid(boolean computeCentroid) {
		throw new RuntimeException(REMOVED_FROM_PROVISIONAL_ADDETECTOR);
	}

	@Override
	public void setReadArray(boolean readArray) {
		throw new RuntimeException(REMOVED_FROM_PROVISIONAL_ADDETECTOR);
	}

	@Override
	public void setReadFilepath(boolean readFilepath) {
		throw new RuntimeException(REMOVED_FROM_PROVISIONAL_ADDETECTOR);
	}

	@Override
	public void setDisableCallbacks(boolean disableCallbacks) {
		throw new RuntimeException(REMOVED_FROM_PROVISIONAL_ADDETECTOR);
	}

	@Override
	public void setUsePipeline(boolean usePipeline) {
		throw new RuntimeException(REMOVED_FROM_PROVISIONAL_ADDETECTOR);
	}

	@Override
	public void reset() throws Exception {
		throw new RuntimeException(REMOVED_FROM_PROVISIONAL_ADDETECTOR);
	}

	@Override
	public boolean isReadAcquisitionTime() {
		throw new RuntimeException(REMOVED_FROM_PROVISIONAL_ADDETECTOR);
	}

	@Override
	public boolean isReadAcquisitionPeriod() {
		throw new RuntimeException(REMOVED_FROM_PROVISIONAL_ADDETECTOR);
	}

	@Override
	public boolean isReadFilepath() {
		throw new RuntimeException(REMOVED_FROM_PROVISIONAL_ADDETECTOR);
	}

	@Override
	public void setNdFile(NDFile ndFile) {
		throw new RuntimeException(REMOVED_FROM_PROVISIONAL_ADDETECTOR);
	}

	// ////

	@Override
	public void configure() throws FactoryException {
		// Do nothing
	}

	@Override
	public void setInputNames(String[] names) {
		throw new RuntimeException(UNSUPPORTED_PART_OF_SCANNABLE_INTERFACE);
	}

	@Override
	public void setExtraNames(String[] names) {
		throw new RuntimeException(UNSUPPORTED_PART_OF_SCANNABLE_INTERFACE);
	}

	@Override
	public void setOutputFormat(String[] names) {
		throw new RuntimeException(UNSUPPORTED_PART_OF_SCANNABLE_INTERFACE);
	}

	@Override
	protected void configureExtraNamesAndOutputFormat() {
		// Not used in provisional version
	}

	@Override
	public String[] getInputNames() {
		return new String[] {};
	}

	@Override
	public String[] getExtraNames() {
		List<String> extraNames = new ArrayList<String>();
		for (ADDetectorPlugin plugin : getPluginList()) {
			extraNames.addAll(plugin.getInputStreamExtraNames());
		}
		return extraNames.toArray(new String[] {});
	}

	@Override
	public String[] getOutputFormat() {
		List<String> formats = new ArrayList<String>();
		for (ADDetectorPlugin plugin : getPluginList()) {
			formats.addAll(plugin.getInputStreamFormats());
		}
		return formats.toArray(new String[] {});
	}

	@Override
	public void atScanStart() throws DeviceException {

		int numberImagesPerCollection = getCollectionStrategy().getNumberImagesPerCollection(getCollectionTime());
		try {
			for (ADDetectorPlugin plugin : getPluginList()) {
				if (plugin instanceof ADTriggeringStrategy) {
					((ADTriggeringStrategy) plugin)
							.prepareForCollection(getCollectionTime(), numberImagesPerCollection);
				} else {
					plugin.prepareForCollection(numberImagesPerCollection);
				}
			}
			getAdBase().setArrayCallbacks(areCallbacksRequired() ? 1 : 0);
		} catch (Exception e) {
			throw new DeviceException(e);
		}
		@SuppressWarnings("unchecked")
		List<PositionInputStream<NXDetectorDataAppender>> plugins = (List<PositionInputStream<NXDetectorDataAppender>>) (List<?>) getPluginList();
		pluginStreamsIndexer = new MultiplePositionStreamIndexer<NXDetectorDataAppender>(plugins);
	}

	boolean areCallbacksRequired() {
		for (ADDetectorPlugin chain : getPluginList()) {
			if (chain.willRequireCallbacks()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void atScanLineStart() throws DeviceException {
		for (ADDetectorPlugin plugin : getPluginList()) {
			try {
				plugin.prepareForLine();
			} catch (Exception e) {
				throw new DeviceException(e);
			}
		}
	}

	@Override
	public Callable<NexusTreeProvider> getPositionCallable() throws DeviceException {
		
		if (pluginStreamsIndexer == null) {
			throw new IllegalStateException("No pluginStreamsIndexer set --- atScanStart() must be called before getPositionCallable()");
		}
		Callable<List<NXDetectorDataAppender>> appendersCallable = pluginStreamsIndexer.getPositionCallable();

		NXDetectorData emptyNXData;
		if ((getFileWriter() != null) && !getFileWriter().isLinkFilepath()) {
			if (getExtraNames().length == 0) {
				emptyNXData = new NXDetectorDataWithFilepathForSrs();
			} else {
				emptyNXData = new NXDetectorDataWithFilepathForSrs(this);
			}
		} else {
			if (getExtraNames().length == 0) {
				emptyNXData = new NXDetectorData();
			} else {
				emptyNXData = new NXDetectorData(this);
			}
		}

		if (getMetaDataProvider() != null && firstReadoutInScan) {
			INexusTree nexusTree = getMetaDataProvider().getNexusTree();
			INexusTree detTree = emptyNXData.getDetTree(getName());
			detTree.addChildNode(nexusTree);
		}
		Callable<NexusTreeProvider> callable = new NXDetectorDataCompletingCallable(emptyNXData, appendersCallable, getName());
		return callable;
	}


	@Override
	public void atScanLineEnd() throws DeviceException {
		for (ADDetectorPlugin plugin : getPluginList()) {
			try {
				plugin.completeLine();
			} catch (Exception e) {
				throw new DeviceException(e);
			}
		}
	}

	@Override
	public void atScanEnd() throws DeviceException {
		// TODO super runs: latestPositionCallable.call(), which I think is not necessary.
		for (ADDetectorPlugin plugin : getPluginList()) {
			try {
				plugin.completeCollection();
			} catch (Exception e) {
				throw new DeviceException(e);
			}
		}
		pluginStreamsIndexer = null; // to avoid later confusion
	}

	@Override
	public void stop() throws DeviceException {
		for (ADDetectorPlugin plugin : getPluginList()) {
			try {
				plugin.stop();
			} catch (Exception e) {
				throw new DeviceException(e);
			}
		}
	}

	@Override
	public void atCommandFailure() throws DeviceException {
		for (ADDetectorPlugin plugin : getPluginList()) {
			try {
				plugin.atCommandFailure();
			} catch (Exception e) {
				throw new DeviceException(e);
			}
		}
	}

}

class NXDetectorDataCompletingCallable implements Callable<NexusTreeProvider> {

	private final NXDetectorData data;

	private final Callable<List<NXDetectorDataAppender>> appendersCallable;
	
	private final String detectorName;

	public NXDetectorDataCompletingCallable(NXDetectorData emptyNXDetectorData,
			Callable<List<NXDetectorDataAppender>> appendersCallable, String detectorName) {
		this.data = emptyNXDetectorData;
		this.appendersCallable = appendersCallable;
		this.detectorName = detectorName;
	}

	@Override
	public NXDetectorData call() throws Exception {
		List<NXDetectorDataAppender> appenderList = appendersCallable.call();
		for (NXDetectorDataAppender appender : appenderList) {
			appender.appendTo(data, detectorName);
		}
		return data;
	}
	
}
