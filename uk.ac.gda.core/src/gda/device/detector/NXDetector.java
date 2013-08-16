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

package gda.device.detector;

import gda.data.nexus.tree.NexusTreeProvider;
import gda.device.DeviceException;
import gda.device.detector.nxdata.NXDetectorDataAppender;
import gda.device.detector.nxdetector.NXCollectionStrategyPlugin;
import gda.device.detector.nxdetector.NXFileWriterPlugin;
import gda.device.detector.nxdetector.NXPlugin;
import gda.device.scannable.PositionCallableProvider;
import gda.device.scannable.PositionInputStream;
import gda.device.scannable.PositionInputStreamCombiner;
import gda.device.scannable.PositionStreamIndexer;
import gda.jython.InterfaceProvider;
import gda.scan.ScanInformation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.commons.lang.StringUtils;
import org.python.core.Py;
import org.python.core.PyException;
import org.python.core.PyString;
import org.springframework.beans.factory.InitializingBean;

public class NXDetector extends DetectorBase implements InitializingBean, NexusDetector,
		PositionCallableProvider<NexusTreeProvider> {

	protected static final String UNSUPPORTED_PART_OF_SCANNABLE_INTERFACE = "ADDetector does not support operation through its Scannable interface. Do not use pos until pos supports detectors as Detectors rather than Scannables";

	private NXCollectionStrategyPlugin collectionStrategy;

	private List<NXPlugin> additionalPluginList = new ArrayList<NXPlugin>();

	private HashMap<String, NXPlugin> additionalPluginMap;

	private PositionStreamIndexer<List<NXDetectorDataAppender>> pluginStreamsIndexer;

	public NexusTreeProvider lastReadoutValue = null;

	public NXDetector(String name, NXCollectionStrategyPlugin collectionStrategy, List<NXPlugin> additionalPluginList) {
		setName(name);
		setCollectionStrategy(collectionStrategy);
		setAdditionalPluginList(additionalPluginList);
		afterPropertiesSet();
	}

	/**
	 * Creates an ADDetector with name, collectionStrategy and probably additionalPluginList yet to configure. A
	 * metadataProvider could also be provided.
	 */
	public NXDetector() {

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

	public void setCollectionStrategy(NXCollectionStrategyPlugin collectionStrategy) {
		if (namesOf(getAdditionalPluginList()).contains(collectionStrategy.getName())) {
			throw new IllegalArgumentException("The configured collectionStrategy named '" + collectionStrategy.getName()
					+ "' duplicates the name of an additionalPlugin");
		}
		this.collectionStrategy = collectionStrategy;
	}

	/**
	 * Set plugins in addition to the collection-strategy plugin.
	 * 
	 * @param additionalPluginList
	 */
	public void setAdditionalPluginList(List<NXPlugin> additionalPluginList) {
		if ((getCollectionStrategy() != null)
				&& (namesOf(additionalPluginList).contains(getCollectionStrategy().getName()))) {
			throw new IllegalArgumentException("The configured plugin '" + getCollectionStrategy().getName()
					+ "' duplicates the name of the configured collectionStrategy");
		}
		if ((new HashSet<String>(namesOf(additionalPluginList))).size() < namesOf(additionalPluginList).size()) {
			throw new IllegalArgumentException("The configured plugins contain duplicate names. Plugin names: "
					+ StringUtils.join(namesOf(additionalPluginList), ", ") + ".");
		}

		this.additionalPluginList = additionalPluginList;

		additionalPluginMap = new HashMap<String, NXPlugin>();
		for (NXPlugin plugin : additionalPluginList) {
			additionalPluginMap.put(plugin.getName(), plugin);
		}
	}

	public NXCollectionStrategyPlugin getCollectionStrategy() {
		return collectionStrategy;
	}

	public List<NXPlugin> getAdditionalPluginList() {
		return additionalPluginList;
	}

	/**
	 * Return all plugins: collection-strategy and then additional plugins
	 */
	public List<NXPlugin> getPluginList() {
		List<NXPlugin> allPlugins = new ArrayList<NXPlugin>();
		allPlugins.add(getCollectionStrategy());
		allPlugins.addAll(getAdditionalPluginList());
		return allPlugins;
	}

	/**
	 * Return map of all plugins: collection-strategy and then additional plugins
	 */
	public Map<String, NXPlugin> getPluginMap() {
		Map<String, NXPlugin> pluginMap = new HashMap<String, NXPlugin>(additionalPluginMap);
		if (getCollectionStrategy() != null) {
			pluginMap.put(getCollectionStrategy().getName(), getCollectionStrategy());
		}
		return pluginMap;
	}

	/**
	 * Return a plugin by its name (configuration methods will have prevented
	 * duplicate plugin names).
	 * @param name
	 * @return plugin
	 * @raises IllegalArgumentException if no plugin with specified name exists
	 */
	public NXPlugin getPlugin(String name) {
		NXPlugin plugin = getPluginMap().get(name);
		if (plugin == null) {
			throw new IllegalArgumentException();
		}
		return plugin;
	}

	/**
	 * When printed from Jython show the standard detector state followed by a representation
	 * of all the plugins.
	 */
	@Override
	public PyString __str__() {
		String string = toFormattedString();
		for (NXPlugin plugin : getPluginList()) {
			string += "  " + plugin.getName() + ": " + plugin.toString() + "\n";
		}
		return new PyString(string);
	}
	
	/**
	 * Returns the NXPlugin with the given name (configuration methods will have prevented
	 * duplicate plugin names).
	 * 
	 * @param name
	 */
	public NXPlugin __getattr__(String name) {
		
		// As DetectorBase does not extend PyObject, it is an 'old style' class and the more
		// appropriate __getattribute__ method won't get called. The problem with __getattr__
		// is that defined methods (such as Scannable.a()) will block it. Then again, maybe
		// this is not a problem!
		NXPlugin plugin = getPluginMap().get(name);
		if (plugin == null) {
			throw new PyException(Py.AttributeError, "");
		}
		return plugin;

	}

	@Override
	public void setInputNames(String[] names) {
		// getInputNames generates these dynamically
		throw new RuntimeException(UNSUPPORTED_PART_OF_SCANNABLE_INTERFACE);
	}

	@Override
	public void setExtraNames(String[] names) {
		// getExtraNames generates these dynamically
		throw new RuntimeException(UNSUPPORTED_PART_OF_SCANNABLE_INTERFACE);
	}

	@Override
	public void setOutputFormat(String[] names) {
		// getOutputFormats generates these dynamically
		throw new RuntimeException(UNSUPPORTED_PART_OF_SCANNABLE_INTERFACE);
	}

	@Override
	public void asynchronousMoveTo(Object collectionTime) throws DeviceException {
		// should be operated only in a scan
		throw new RuntimeException(UNSUPPORTED_PART_OF_SCANNABLE_INTERFACE);
	}

	@Override
	public boolean isBusy() {
		// should be operated only in a scan
		throw new RuntimeException(UNSUPPORTED_PART_OF_SCANNABLE_INTERFACE);
	}

	@Override
	public Object getPosition() throws DeviceException {
		// should be operated only in a scan
		throw new RuntimeException(UNSUPPORTED_PART_OF_SCANNABLE_INTERFACE);
	}
	
	@Override
	final public void prepareForCollection() throws DeviceException {
		// atScanLineStart implemented instead
	}

	@Override
	final public void endCollection() throws DeviceException {
		// atScanLineEnd implemented instead
	}

	@Override
	public boolean createsOwnFiles() throws DeviceException {
		return false; // always return nexus data (name of method is misleading)
	}

	@Override
	public String[] getInputNames() {
		return new String[] {};
	}

	@Override
	public String[] getExtraNames() {
		// These are likely to change dynamically, although this value should probably be cached in atScanStart
		List<String> extraNames = new ArrayList<String>();
		for (NXPlugin plugin : getPluginList()) {
			extraNames.addAll(plugin.getInputStreamNames());
		}
		if (new HashSet<String>(extraNames).size() < extraNames.size()) {
			String namesString = StringUtils.join(extraNames.toArray(), ", ");
			throw new IllegalStateException("The configured plugins returned duplicate extra names: '" + namesString + "'.");
		}
		return extraNames.toArray(new String[] {});
	}

	@Override
	public String[] getOutputFormat() {
		List<String> formats = new ArrayList<String>();
		for (NXPlugin plugin : getPluginList()) {
			formats.addAll(plugin.getInputStreamFormats());
		}
		return formats.toArray(new String[] {});
	}

	@Override
	public void atScanStart() throws DeviceException {

		ScanInformation scanInfo = InterfaceProvider.getCurrentScanInformationHolder().getCurrentScanInformation();
		try {
			int numberImagesPerCollection = getCollectionStrategy().getNumberImagesPerCollection(getCollectionTime());
			lastReadoutValue = null;
			prepareCollectionStrategyAtScanStart(numberImagesPerCollection, scanInfo);
			for (NXPlugin plugin : getAdditionalPluginList()) {
				plugin.prepareForCollection(numberImagesPerCollection, scanInfo);
			}
		} catch (Exception e) {
			throw new DeviceException(e);
		}
		@SuppressWarnings("unchecked")
		List<PositionInputStream<NXDetectorDataAppender>> plugins = (List<PositionInputStream<NXDetectorDataAppender>>) (List<?>) getPluginList();
		
		PositionInputStreamCombiner<NXDetectorDataAppender> combinedStream = new PositionInputStreamCombiner<NXDetectorDataAppender>(plugins);
		pluginStreamsIndexer = new PositionStreamIndexer<List<NXDetectorDataAppender>>(combinedStream);
	}

	protected void prepareCollectionStrategyAtScanStart(int numberImagesPerCollection, ScanInformation scanInfo) throws Exception, DeviceException {
		getCollectionStrategy().setGenerateCallbacks(areCallbacksRequired());
		getCollectionStrategy().prepareForCollection(getCollectionTime(), numberImagesPerCollection, scanInfo);
	}

	boolean areCallbacksRequired() {
		for (NXPlugin chain : getAdditionalPluginList()) {
			if (chain.willRequireCallbacks()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void atScanLineStart() throws DeviceException {
		for (NXPlugin plugin : getPluginList()) {
			try {
				plugin.prepareForLine();
			} catch (Exception e) {
				throw new DeviceException(e);
			}
		}
	}
	
	@Override
	public void collectData() throws DeviceException {
		lastReadoutValue  = null;
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
		if (lastReadoutValue == null) {
			try {
				lastReadoutValue =  getPositionCallable().call();
			} catch (Exception e) {
				throw new DeviceException("Error during '" + getName() + "' readout.", e);
			}
		}
		return lastReadoutValue;
	}

	@Override
	public Callable<NexusTreeProvider> getPositionCallable() throws DeviceException {

		if (pluginStreamsIndexer == null) {
			throw new IllegalStateException(
					"No pluginStreamsIndexer set --- atScanStart() must be called before getPositionCallable()");
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

		Callable<NexusTreeProvider> callable = new NXDetectorDataCompletingCallable(nxdata, appendersCallable,
				getName());
		return callable;
	}

	private boolean isFilepathRequiredInNxDetectorData() {
		for (NXPlugin plugin : getPluginList()) {
			if (plugin instanceof NXFileWriterPlugin) {
				NXFileWriterPlugin writer = (NXFileWriterPlugin) plugin;
				if (writer.appendsFilepathStrings()) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public void atScanLineEnd() throws DeviceException {
		for (NXPlugin plugin : getPluginList()) {
			try {
				plugin.completeLine();
			} catch (Exception e) {
				throw new DeviceException(e);
			}
		}
	}

	@Override
	public void atScanEnd() throws DeviceException {
		for (NXPlugin plugin : getPluginList()) {
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
		for (NXPlugin plugin : getPluginList()) {
			try {
				plugin.stop();
			} catch (Exception e) {
				throw new DeviceException(e);
			}
		}
	}

	@Override
	public void atCommandFailure() throws DeviceException {
		for (NXPlugin plugin : getPluginList()) {
			try {
				plugin.atCommandFailure();
			} catch (Exception e) {
				throw new DeviceException(e);
			}
		}
	}

	static private List<String> namesOf(List<NXPlugin> pluginList) {
		List<String> pluginNames = new ArrayList<String>();
		for (NXPlugin plugin : pluginList) {
			pluginNames.add(plugin.getName());
		}
		return pluginNames;
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
