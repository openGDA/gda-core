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

import gda.data.nexus.tree.NexusTreeProvider;
import gda.device.DeviceException;
import gda.device.detector.DetectorBase;
import gda.device.detector.NXDetectorData;
import gda.device.detector.NXDetectorDataWithFilepathForSrs;
import gda.device.detector.NexusDetector;
import gda.device.detector.addetector.filewriter.FileWriter;
import gda.device.detector.addetector.triggering.ADTriggeringStrategy;
import gda.device.detector.nxdata.NXDetectorDataAppender;
import gda.device.scannable.MultiplePositionStreamIndexer;
import gda.device.scannable.PositionCallableProvider;
import gda.device.scannable.PositionInputStream;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.springframework.beans.factory.InitializingBean;

// TODO Check for duplicate extraNames returned by the plugins
// TODO Deliberate change: does not read arrays by default
// TODO Check for duplicate plugin names

public class ProvisionalADDetector extends DetectorBase implements InitializingBean, NexusDetector, PositionCallableProvider<NexusTreeProvider> {
	
	protected static final String UNSUPPORTED_PART_OF_SCANNABLE_INTERFACE = "ADDetector does not support operation through its Scannable interface. Do not use pos until pos supports detectors as Detectors rather than Scannables";

	private ADTriggeringStrategy collectionStrategy;

	private List<ADDetectorPlugin> additionalPluginList = new ArrayList<ADDetectorPlugin>();
	
	// provides metadata about the detector. This is added as a child of the detector element of the tree
	private NexusTreeProvider metadataProvider;

	private MultiplePositionStreamIndexer<NXDetectorDataAppender> pluginStreamsIndexer;
	
	public ProvisionalADDetector(String name, ADTriggeringStrategy collectionStrategy, List<ADDetectorPlugin> adDetectorPluginList) {
		setName(name);
		setCollectionStrategy(collectionStrategy);
		setAdditionalPluginList(adDetectorPluginList);
		afterPropertiesSet();
	}
	
	/**
	 * Creates an ADDetector with name, collectionStrategy and probably additionalPluginList yet to configure.
	 * A metadataProvider could also be provided.
	 */
	public ProvisionalADDetector() {

	}
	
	@Override
	public void afterPropertiesSet() {
		
		if (getName() == null) {
			throw new IllegalStateException("no name has been configured");
		}
		if (getCollectionStrategy() == null) {
			throw new IllegalStateException("no colectionStrategy has been configured");
		}
	}
	
	public void setCollectionStrategy(ADTriggeringStrategy collectionStrategy) {
		this.collectionStrategy = collectionStrategy;
	}
	
	/**
	 * Set plugins in addition to the collection-strategy, array and file-writer plugins.
	 * 
	 * @param adDetectorPluginList
	 */
	public void setAdditionalPluginList(List<ADDetectorPlugin> adDetectorPluginList) {
		this.additionalPluginList = adDetectorPluginList;
	}

	public void setMetadataProvider(NexusTreeProvider metaDataProvider) {
		this.metadataProvider = metaDataProvider;
	}
	
	public ADTriggeringStrategy getCollectionStrategy() {
		return collectionStrategy;
	}
	
	public List<ADDetectorPlugin> getAdditionalPluginList() {
		return additionalPluginList;
	}
	
	public NexusTreeProvider getMetadataProvider() {
		return metadataProvider;
	}


	/**
	 * Return all plugins: collection-strategy and then additional plugins
	 */
	public List<ADDetectorPlugin> getPluginList() {
		List<ADDetectorPlugin> allPlugins = new ArrayList<ADDetectorPlugin>();
		allPlugins.add(getCollectionStrategy());
		allPlugins.addAll(getAdditionalPluginList());
		return allPlugins;
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
	public void asynchronousMoveTo(Object collectionTime) throws DeviceException {
		throw new RuntimeException(UNSUPPORTED_PART_OF_SCANNABLE_INTERFACE);
	}

	@Override
	public boolean isBusy() {
		throw new RuntimeException(UNSUPPORTED_PART_OF_SCANNABLE_INTERFACE);
	}

	@Override
	public Object getPosition() throws DeviceException {
		throw new RuntimeException(UNSUPPORTED_PART_OF_SCANNABLE_INTERFACE);
	}
	
	@Override
	public boolean createsOwnFiles() throws DeviceException {
		return false; // always return nexus data
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
					((ADTriggeringStrategy) plugin).setGenerateCallbacks(areCallbacksRequired());
					((ADTriggeringStrategy) plugin)
							.prepareForCollection(getCollectionTime(), numberImagesPerCollection);
				} else {
					plugin.prepareForCollection(numberImagesPerCollection);
				}
			}
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
	public void collectData() throws DeviceException {
		try {
			getCollectionStrategy().collectData();
		} catch (Exception e) {
			throw new DeviceException(e);
		}
	}
	
	@Override
	public int getStatus() throws DeviceException {
		try {
			return getCollectionStrategy().getStatus();
		} catch (Exception e) {
			throw new DeviceException(e);
		}
	}
	
	@Override
	public void waitWhileBusy() throws InterruptedException, DeviceException {
		try {
			getCollectionStrategy().waitWhileBusy();
		} catch (Exception e) {
			throw new DeviceException(e);
		}
	}
	
	@Override
	public NexusTreeProvider readout() throws DeviceException {
		try {
			return getPositionCallable().call();
		} catch (Exception e) {
			throw new DeviceException("Error during '"+ getName() +"' readout.", e);
		}
	}
	
	@Override
	public Callable<NexusTreeProvider> getPositionCallable() throws DeviceException {
		
		if (pluginStreamsIndexer == null) {
			throw new IllegalStateException("No pluginStreamsIndexer set --- atScanStart() must be called before getPositionCallable()");
		}
		Callable<List<NXDetectorDataAppender>> appendersCallable = pluginStreamsIndexer.getPositionCallable();

		NXDetectorData nxdata;
		if (isFilepathRequiredInNxDetectorData()) {
			if (getExtraNames().length == 0) {
				nxdata = new NXDetectorDataWithFilepathForSrs();
			} else {
				nxdata = new NXDetectorDataWithFilepathForSrs(this);
			}
		} else {
			if (getExtraNames().length == 0) {
				nxdata = new NXDetectorData();
			} else {
				nxdata = new NXDetectorData(this);
			}
		}

		Callable<NexusTreeProvider> callable = new NXDetectorDataCompletingCallable(nxdata, appendersCallable, getName());
		return callable;
	}

	private boolean isFilepathRequiredInNxDetectorData() {
		for (ADDetectorPlugin plugin : getPluginList()) {
			if (plugin instanceof FileWriter) {
				FileWriter writer = (FileWriter) plugin;
				if (!writer.isLinkFilepath() && writer.getEnable()) {
					return true;
				}
			}
		}
		return false;
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
