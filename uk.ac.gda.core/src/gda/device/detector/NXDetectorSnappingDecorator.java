/*-
 * Copyright © 2016 Diamond Light Source Ltd.
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
import gda.device.DetectorSnapper;
import gda.device.DeviceException;
import gda.device.detector.nxdata.NXDetectorDataAppender;
import gda.device.detector.nxdetector.FrameCountingNXPlugin;
import gda.device.detector.nxdetector.NXCollectionStrategyPlugin;
import gda.device.detector.nxdetector.NXPlugin;
import gda.device.detector.nxdetector.NXPluginBase;
import gda.scan.ScanInformation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.Callable;

import org.apache.commons.lang.StringUtils;
import org.python.core.PyString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Decorator to extend an NXDetector to allow use in the "pos" command.
 *
 * <p>Uses a separate plugin list and collection strategy for use in the pos command and
 * delegates to the decorated detector when used in a scan.</p>
 *
 * <p>
 * <pre>
 * {@code
 * <bean id="detector" class="NXDetectorSnappingDecorator">
 * 	<constructor-arg ref="_original_nxdetector_to_extend" />
 * 	<property name="snapperCollectionStrategy">
 * 		<bean class="SingleExposureStandard"> ... </bean>
 * 	</property>
 * 	<property name="snapperPluginList">
 * 		<list>
 * 			<bean class="SingleImagePerFileWriterWithNumTracker">
 * 				...
 * 			</bean>
 * 		</list>
 * 	</property>
 * </bean>
 * }
 * </pre>
 * </p>
 *
 */
public class NXDetectorSnappingDecorator extends PassthroughDetectorWrapper implements NXPluginDetector, DetectorSnapper {

	private static final Logger logger = LoggerFactory.getLogger(NXDetectorSnappingDecorator.class);

	private NXPluginDetector decoratedDetector;

	private NXCollectionStrategyPlugin snapperCollectionStrategy;

	private List<NXPluginBase> snapperPluginList;

	private NXDetectorData snappedData;

	private String name;

	private boolean inScan = false;

	NXDetectorSnappingDecorator(NXPluginDetector decoratedDetector) {
		super(decoratedDetector);
		snapperPluginList = new ArrayList<NXPluginBase>();
		this.decoratedDetector = decoratedDetector;
	}

	NXDetectorSnappingDecorator(NXPluginDetector decoratedDetector,
			NXCollectionStrategyPlugin collectionStrategy) {
		this(decoratedDetector);
		snapperCollectionStrategy = collectionStrategy;
	}

	NXDetectorSnappingDecorator(NXPluginDetector decoratedDetector,
			NXCollectionStrategyPlugin collectionStrategy,
			List<NXPluginBase> pluginList) {
		this(decoratedDetector, collectionStrategy);
		snapperPluginList = pluginList;
	}

	public void setSnappingCollectionStrategy(NXCollectionStrategyPlugin collectionStrategy) {
		this.snapperCollectionStrategy = collectionStrategy;
	}

	public void setSnapperPluginList(List<NXPluginBase> pluginList) {
		this.snapperPluginList = pluginList;
	}

	public NXCollectionStrategyPlugin getSnapperCollectionStrategy() {
		return snapperCollectionStrategy;
	}

	public List<NXPluginBase> getSnapperPluginList() {
		return snapperPluginList;
	}

	@Override
	public NXPluginDetector getDelegate() {
		return decoratedDetector;
	}

	private List<NXPluginBase> allPlugins() {
		List<NXPluginBase> plugins = new ArrayList<NXPluginBase>(snapperPluginList.size() + 1);
		plugins.addAll(snapperPluginList);
		plugins.add(snapperCollectionStrategy);
		return plugins;
	}

	private void stopPlugins() {
		List<NXPluginBase> plugins = allPlugins();
		for (NXPluginBase plugin : plugins) {
			try {
				plugin.stop();
			} catch (Exception e) {
				logger.error("Could not stop plugin", e);
			}
		}
	}

	/*
	 * DetectorSnapper methods
	 */

	@Override
	public void prepareForAcquisition(double collectionTime) throws Exception {
		inScan = false;
		int images = snapperCollectionStrategy.getNumberImagesPerCollection(collectionTime);
		snapperCollectionStrategy.prepareForCollection(collectionTime, images, null);
		// some plugins want to know about scan dimensions - could break if any care about the scan number/file
		ScanInformation dummyScanInfo = new ScanInformation(new int[] { 1 }, -1, new String[] {}, new String[] { getName() }, null, null, 1);
		for (NXPluginBase plugin : snapperPluginList) {
			plugin.prepareForCollection(images, dummyScanInfo);
		}
		for (NXPluginBase plugin : snapperPluginList) {
			plugin.prepareForLine();
		}
	}

	@Override
	public double getAcquireTime() throws Exception {
		return snapperCollectionStrategy.getAcquireTime();
	}

	@Override
	public double getAcquirePeriod() throws Exception {
		return snapperCollectionStrategy.getAcquirePeriod();
	}

	@Override
	public String[] acquire() throws InterruptedException, IllegalStateException, Exception {
		snapperCollectionStrategy.collectData();
		List<NXPluginBase> pluginList = allPlugins();
		List<Object> list = new ArrayList<Object>();
		NXDetectorDataWithFilepathForSrs data = new NXDetectorDataWithFilepathForSrs(this);

		// not efficient to synchronously read like this, but for taking a single snapshot it should not matter
		for (NXPluginBase pluginBase : pluginList) {
			if (!(pluginBase instanceof NXPlugin))
				continue;
			NXPlugin plugin = (NXPlugin) pluginBase;
			try {
				// should only be one thing to read
				NXDetectorDataAppender position = plugin.read(1).get(0);
				list.add(position);
				position.appendTo(data, getName());
			} catch (NoSuchElementException | InterruptedException e) {
				throw new DeviceException("Could not read from plugin", e);
			}
		}

		// could roll completeLine and completeCollection into one loop
		// but I would prefer to keep the call order the same as in NXDetector
		for (NXPluginBase plugin : pluginList) {
			if (plugin instanceof FrameCountingNXPlugin) {
				((FrameCountingNXPlugin) plugin).completeLine(1);
			} else {
				plugin.completeLine();
			}
		}

		for (NXPluginBase plugin : pluginList) {
			if (plugin instanceof FrameCountingNXPlugin) {
				((FrameCountingNXPlugin) plugin).completeCollection(1);
			} else {
				plugin.completeCollection();
			}
		}
		snappedData = data;
		return new String[] { data.getFilepath() };
	}

	/*
	 * Things not to just delegate
	 */

	@Override
	public Object getPosition() throws DeviceException {
		// Would like to just return the NXDetectorData but it doesn't substitute file paths properly
		// so we have a messy attempt to do that
		// will only substitute *one* filepath name with the actual filepath (usually asked for by the tif writer plugin)
		if (!(snappedData instanceof NXDetectorDataWithFilepathForSrs) &&
				((NXDetectorDataWithFilepathForSrs) snappedData).getFilepath() != null) {
			return snappedData;
		}
		String filepath = ((NXDetectorDataWithFilepathForSrs) snappedData).getFilepath();
		String[] names = getExtraNames();
		for (int idx = 0; idx < names.length; idx++) {
			if (names[idx].equalsIgnoreCase("filepath")) {
				int length = snappedData.getDoubleVals().length;
				List<Object> position = new ArrayList<Object>(length);
				for (double val : snappedData.getDoubleVals()) {
					position.add(val);
				}
				position.set(idx, filepath);
				return position.toArray();
			}
		}
		return snappedData.getDoubleVals();
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String[] getExtraNames() {
		if (inScan) {
			return getDelegate().getExtraNames();
		} else {
			// unfortunately duplicated from NXDetector
			List<String> extraNames = new ArrayList<String>();
			List<NXPluginBase> plugins = allPlugins();
			for (NXPluginBase plugin : plugins) {
				extraNames.addAll(plugin.getInputStreamNames());
			}
			if (new HashSet<String>(extraNames).size() < extraNames.size()) {
				String namesString = StringUtils.join(extraNames.toArray(), ", ");
				throw new IllegalStateException("The configured plugins returned duplicate extra names: '" + namesString + "'.");
			}
			return extraNames.toArray(new String[extraNames.size()]);
		}
	}

	@Override
	public void atScanStart() throws DeviceException {
		inScan = true;
		getDelegate().atScanStart();
	}

	@Override
	public void atScanEnd() throws DeviceException {
		inScan = false;
		getDelegate().atScanEnd();
	}

	@Override
	public void atCommandFailure() throws DeviceException {
		if (!inScan) {
			stopPlugins();
		}
		inScan = false;
		getDelegate().atCommandFailure();
	}

	@Override
	public void stop() throws DeviceException {
		if (!inScan) {
			stopPlugins();
		}
		inScan = false;
		getDelegate().stop();
	}

	/*
	 * Everything else
	 */

	@Override
	public NexusTreeProvider readout() throws DeviceException {
		return getDelegate().readout();
	}

	@Override
	public Callable<NexusTreeProvider> getPositionCallable() throws DeviceException {
		return getDelegate().getPositionCallable();
	}

	@Override
	public List<NXPluginBase> getPluginList() {
		return getDelegate().getPluginList();
	}

	@Override
	public Map<String, NXPluginBase> getPluginMap() {
		return getDelegate().getPluginMap();
	}

	@Override
	public void setAdditionalPluginList(List<NXPluginBase> additionalPluginList) {
		getDelegate().setAdditionalPluginList(additionalPluginList);
	}

	@Override
	public List<NXPluginBase> getAdditionalPluginList() {
		return getDelegate().getAdditionalPluginList();
	}

	@Override
	public void setCollectionStrategy(NXCollectionStrategyPlugin collectionStrategy) {
		getDelegate().setCollectionStrategy(collectionStrategy);
	}

	@Override
	public NXCollectionStrategyPlugin getCollectionStrategy() {
		return getDelegate().getCollectionStrategy();
	}


	public PyString __str__() {
		StringBuilder str = new StringBuilder(toFormattedString());
		str.append("\nScanning plugins:\n");
		for (NXPluginBase plugin : getDelegate().getPluginList()) {
			str.append("  ");
			str.append(plugin.getName());
			str.append(": ");
			str.append(plugin.toString());
			str.append('\n');
		}
		str.append("Snapshot plugins:\n");
		for (NXPluginBase plugin : allPlugins()) {
			str.append("  ");
			str.append(plugin.getName());
			str.append(": ");
			str.append(plugin.toString());
			str.append('\n');
		}
		return new PyString(str.toString());
	}

	public PyString __repr__() {
		return __str__();
	}
}
