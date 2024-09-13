/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

package gda.device.detector.nexusprocessor;

import static gda.data.scan.nexus.device.GDADeviceNexusConstants.ATTRIBUTE_NAME_LOCAL_NAME;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.january.dataset.DTypeUtils;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;

import gda.data.nexus.extractor.NexusExtractor;
import gda.data.nexus.extractor.NexusGroupData;
import gda.data.nexus.tree.INexusTree;
import gda.data.nexus.tree.NexusTreeNode;
import gda.device.detector.GDANexusDetectorData;
import gda.device.detector.NXDetectorData;

/**
 * Processor to take a Dataset and calculate some statistics on it
 * The available statistics are defined in {@link Statistic}. Which
 * statistics are enabled can be configured using {@link #setEnabledStats(List)}.
 */
public class DatasetStats extends DatasetProcessorBase {

	private static final String INT_FORMAT = "%.0f";
	private static final String FLOAT_FORMAT = "%f";
	private static final String GENERAL_FORMAT = "%5.5g";

	private Set<Statistic> enabledStats = new LinkedHashSet<>();
	private Map<Statistic, String> statsNames;
	private Map<Statistic, String> statsFormats;
	private List<String> extraNames = new ArrayList<>();
	private List<String> outputFormats = new ArrayList<>();
	private boolean prefixLocalNameWithDataName = false;
	private boolean useSingleDataGroup = false;
	private Map<String, String> nodeNames = new HashMap<>();

	/**
	 * On creation the names and formats for each {@link Statistic} are set
	 * from defaults but can be changed using e.g. {@link #setStatName(Statistic, String)}.
	 */
	public DatasetStats() {
		this(List.of(Statistic.SUM, Statistic.MEAN));
	}

	public DatasetStats(List<Statistic> enabledStats) {
		statsNames = EnumSet.allOf(Statistic.class).stream()
				.collect(Collectors.toMap(Function.identity(), Statistic::getDefaultName));
		statsFormats = EnumSet.allOf(Statistic.class).stream()
				.collect(Collectors.toMap(Function.identity(), Statistic::getDefaultFormat));

		setNamesAndFormats();
		setEnabledStats(enabledStats);
	}

	public void setUseSingleDataGroup(boolean useSingleDataGroup) {
		this.useSingleDataGroup = useSingleDataGroup;
	}

	@Override
	public GDANexusDetectorData process(String detectorName, String dataName, Dataset dataset) throws Exception {
		if (!enable) {
			return null;
		}
		String nxsDataName = dataName.isEmpty() ? "" : dataName + ".";
		NXDetectorData res = new NXDetectorData(extraNames.stream().map(s -> nxsDataName + s).toArray(String[]::new), outputFormats.stream().toArray(String[]::new), dataName);
		// Cast to larger type otherwise stats can overflow due to InterfaceUtils#toBiggestNumber in AbstractDataset#sum
		Class<? extends Dataset> safeType = DTypeUtils.getLargestDataset(dataset.getClass());
		Dataset promotedDataset = dataset.cast(safeType);
		nodeNames.clear();
		for (Statistic stat : enabledStats) {
			Number statistic = stat.applyFunction(promotedDataset);
			String statName = statsNames.get(stat);
			// Convert to dataset here because there is no NexusGroupData constructor for general Number type
			NexusGroupData data = new NexusGroupData(DatasetFactory.createFromObject(statistic, 1));
			data.isDetectorEntryData = true;
			data.dataGroupName = useSingleDataGroup ? detectorName + "_" + dataName : null;
			final String dataNodeName = nxsDataName + statName;
			final INexusTree dataNode = res.addData(detectorName, dataNodeName, data, null, 1);
			final String name = prefixLocalNameWithDataName ? dataNodeName : statName;
			final String localName = detectorName.isEmpty() ? name : detectorName + "." + name;
			dataNode.addChildNode(new NexusTreeNode(ATTRIBUTE_NAME_LOCAL_NAME, NexusExtractor.AttrClassName,
					dataNode, new NexusGroupData(localName)));
			res.setPlottableValue(dataNodeName, statistic.doubleValue());
			getNodeNames().put(dataNodeName, statName);
		}
		return res;
	}

	@Override
	public void atScanStart() {
		nodeNames.clear();
		super.atScanStart();
	}

	@Override
	protected Collection<String> _getExtraNames() {
		return extraNames;
	}

	@Override
	protected Collection<String> _getOutputFormat() {
		return outputFormats;
	}

	@Override
	public String toString() {
		return "DatasetStats [enable=" + enable + "]";
	}

	private void setNamesAndFormats() {
		extraNames.clear();
		outputFormats.clear();
		for (Statistic stat : enabledStats) {
			extraNames.add(statsNames.get(stat));
			outputFormats.add(statsFormats.get(stat));
		}
	}

	public void setPrefixLocalNameWithDataName(boolean prefixLocalNameWithDataName) {
		// where this processor is used within in RoiStatsProcessor, the local_name
		// attributes need to be prefixed with the roi name so as to match the
		// extra_name of the the NexusDetectorProcessor
		this.prefixLocalNameWithDataName = prefixLocalNameWithDataName;
	}

	/**
	 * Set the {@link Statistic}s that should be computed. Note that
	 * only those included in the provided list parameter will be used,
	 * the defaults will be cleared.
	 * @param stats list of {@link Statistic} to compute for each point
	 */
	public void setEnabledStats(List<Statistic> stats) {
		enabledStats.clear();
		enabledStats.addAll(stats);
		setNamesAndFormats();
	}

	public Set<Statistic> getEnabledStats() {
		return enabledStats;
	}

	public void setStatName(Statistic stat, String name) {
		statsNames.put(stat, name);
		setNamesAndFormats();
	}

	public void setStatFormat(Statistic stat, String format) {
		statsFormats.put(stat, format);
		setNamesAndFormats();
	}

	public void setStatNames(Map<Statistic, String> names) {
		names.forEach(statsNames::put);
		setNamesAndFormats();
	}

	public void setStatFormats(Map<Statistic, String> formats) {
		formats.forEach(statsFormats::put);
		setNamesAndFormats();
	}

	public Map<String, String> getNodeNames() {
		return nodeNames;
	}

	/**
	 * Statistics for Dataset processing. Each is defined with a
	 * default name and output format. The statistics are applied
	 * using {@link #applyFunction(Dataset)}.
	 * <p>
	 * The dataset is assumed to use row major indexing
	 */
	public enum Statistic {
		MIN_X("min_x", INT_FORMAT, dataset -> dataset.minPos()[1]),
		MIN_Y("min_y", INT_FORMAT, dataset -> dataset.minPos()[0]),
		MIN_VAL("min_val", INT_FORMAT, dataset -> dataset.min()),
		MAX_X("max_x", INT_FORMAT, dataset -> dataset.maxPos()[1]),
		MAX_Y("max_y", INT_FORMAT, dataset -> dataset.maxPos()[0]),
		MAX_VAL("max_val", INT_FORMAT,	dataset -> dataset.max()),
		MEAN("average", GENERAL_FORMAT, dataset -> (Number) dataset.mean()),
		STDEV("std", FLOAT_FORMAT, dataset -> dataset.stdDeviation()),
		SUM("total", GENERAL_FORMAT, dataset -> (Number) dataset.sum()),
		PROFILE_X("profileX", INT_FORMAT, dataset -> (Number) dataset.sum(1).sum()),
		PROFILE_Y("profileY", INT_FORMAT, dataset -> (Number) dataset.sum(0).sum());

		private String defaultName;
		private String defaultFormat;
		private Function<Dataset, Number> function;

		private Statistic(String defaultName, String defaultFormat, Function<Dataset, Number> function) {
			this.defaultName = defaultName;
			this.defaultFormat = defaultFormat;
			this.function = function;
		}

		public Number applyFunction(Dataset dataset) {
			return function.apply(dataset);
		}

		public String getDefaultName() {
			return defaultName;
		}

		public String getDefaultFormat() {
			return defaultFormat;
		}
	}
}
