/*-
 * Copyright © 2020 Diamond Light Source Ltd.
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

package gda.device.detector.nexusprocessor.roistats;

import static gda.data.nexus.extractor.NexusExtractor.AttrClassName;
import static gda.data.nexus.extractor.NexusExtractor.SDSClassName;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.eclipse.dawnsci.analysis.api.tree.Node;
import org.eclipse.dawnsci.nexus.NXdata;
import org.eclipse.dawnsci.nexus.NXpositioner;
import org.eclipse.dawnsci.nexus.NXregion;
import org.eclipse.dawnsci.nexus.NexusBaseClass;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;
import gda.data.nexus.extractor.NexusExtractor;
import gda.data.nexus.extractor.NexusGroupData;
import gda.data.nexus.tree.INexusTree;
import gda.data.nexus.tree.NexusTreeNode;
import gda.data.scan.datawriter.NXLinkCreator;
import gda.data.scan.datawriter.NexusScanDataWriter;
import gda.device.detector.GDANexusDetectorData;
import gda.device.detector.NXDetectorData;
import gda.device.detector.nexusprocessor.DatasetProcessorBase;
import gda.device.detector.nexusprocessor.DatasetStats;
import gda.factory.FactoryException;
import gda.jython.InterfaceProvider;
import gda.scan.ScanInformation;

/**
 * Receive dataset from processing Nexus Detector Retrieve rois from gui.
 * <p>
 * For each roi, create a dataset for it and pass into a {@link DatasetStats} processor.
 */
public class RoiStatsProcessor extends DatasetProcessorBase {

	private static final Logger logger = LoggerFactory.getLogger(RoiStatsProcessor.class);

	private List<String> extraNames = new ArrayList<>();
	private List<String> outputFormats = new ArrayList<>();
	private List<RegionOfInterest> roiList = new ArrayList<>();
	private String plotName;
	private DatasetStats statsProcessor;
	private boolean useSingleDataGroupPerRoi = false;

	private String detectorNodePath;
	private String detectorName;

	private NexusTreeNode statistics;

	@Override
	public GDANexusDetectorData process(String detectorName, String dataName, Dataset dataset) throws Exception {
		NXDetectorData result = new NXDetectorData();
		statsProcessor.setPrefixLocalNameWithDataName(true);
		statsProcessor.setUseSingleDataGroup(useSingleDataGroupPerRoi);
		for (RegionOfInterest roi : roiList) {
			NXDetectorData roiData = new NXDetectorData();
			roiData.mergeIn(statsProcessor.process(detectorName, roi.getName(), dataset.getSliceView(roi.getSlice())));
			writeNXRegionForRoi(roiData.getDetTree(detectorName), roi);
			result.mergeIn(roiData);
		}
		return result;
	}

	/**
	 * At the moment this is just writing metadata to an NXcollection.
	 * Once the NXregion has been added to the Nexus standard it should
	 * be modified.
	 *
	 * @see <a href="https://github.com/nexusformat/definitions/issues/944">GitHub issue</a>
	 */
	private void writeNXRegionForRoi(INexusTree parent, RegionOfInterest roi) {
		var nXRoi = new NexusTreeNode(roi.getName(), NexusBaseClass.NX_REGION.toString(), null);

		Dataset start = DatasetFactory.createFromObject(new Integer[] {roi.getY(), roi.getX()});
		Dataset count = DatasetFactory.createFromObject(new Integer[] {roi.getHeight(), roi.getWidth()});

		nXRoi.addChildNode(new NexusTreeNode(NXregion.NX_ATTRIBUTE_REGION_TYPE, AttrClassName, null, new NexusGroupData("rectanglar")));
		nXRoi.addChildNode(new NexusTreeNode(NXregion.NX_START, SDSClassName, null, new NexusGroupData(start)));
		nXRoi.addChildNode(new NexusTreeNode(NXregion.NX_COUNT, SDSClassName, null, new NexusGroupData(count)));
		statistics = new NexusTreeNode("statistics", NexusExtractor.NXDataClassName, null);
		nXRoi.addChildNode(statistics);

		parent.addChildNode(nXRoi);
	}

	private void createStatisticsDataLinks() {
		if (detectorName == null || detectorName.isEmpty()) detectorName = getDetectorName();
		if (detectorNodePath == null) detectorNodePath = getDetectorNodePath(detectorName);

		final ScanInformation currentScanInformation = InterfaceProvider.getCurrentScanInformationHolder().getCurrentScanInformation();
		final List<String> scannableNames = Arrays.asList(currentScanInformation.getScannableNames());
		final NXLinkCreator nxLinkCreator = new NXLinkCreator();

		for (RegionOfInterest roi : roiList) {
			String statisticsNodePath = detectorNodePath + Node.SEPARATOR + roi.getName() + ":" + NexusBaseClass.NX_REGION.toString() + Node.SEPARATOR + "statistics:NXdata" + Node.SEPARATOR;
			Optional<String> first = statsProcessor.getNodeNames().values().contains("total") ? Optional.of("total") : statsProcessor.getNodeNames().values().stream().findFirst();
			if (first.isPresent()) {
				statistics.addChildNode(new NexusTreeNode(NXdata.NX_ATTRIBUTE_SIGNAL, NexusExtractor.AttrClassName, statistics, new NexusGroupData(first.get())));
				List<String> collect = statsProcessor.getExtraNames().stream().filter(e -> !e.equals(first.get())).sorted().toList();
				if (!collect.isEmpty()) {
					statistics.addChildNode(new NexusTreeNode(NXdata.NX_ATTRIBUTE_AUXILIARY_SIGNALS, NexusExtractor.AttrClassName, statistics, new NexusGroupData(DatasetFactory.createFromObject(collect))));
				}
				statistics.addChildNode(new NexusTreeNode(NXdata.NX_ATTRIBUTE_AXES, NexusExtractor.AttrClassName, statistics, new NexusGroupData(DatasetFactory.createFromObject(scannableNames))));
			}

			scannableNames.stream().forEach(e -> nxLinkCreator.addLink(statisticsNodePath + e, getInstrumentNodePath() + Node.SEPARATOR + e + ":NXpositioner" + Node.SEPARATOR + NXpositioner.NX_VALUE +":SDS"));
			statsProcessor.getNodeNames().entrySet().stream().forEach(e -> nxLinkCreator.addLink(statisticsNodePath + e.getValue(), detectorNodePath + Node.SEPARATOR + e.getKey() + ":SDS"));
		}
		try {
			nxLinkCreator.makelinks(currentScanInformation.getFilename());
		} catch (Exception e) {
			logger.error("Failed to create hard link to ROI statistics data", e);
		}
	}

	private String getInstrumentNodePath() {
		String entryName = LocalProperties.get(NexusScanDataWriter.PROPERTY_NAME_ENTRY_NAME, NexusScanDataWriter.DEFAULT_ENTRY_NAME);
		String entryType = NexusBaseClass.NX_ENTRY.toString();
		String instrument = NexusScanDataWriter.METADATA_ENTRY_NAME_INSTRUMENT;
		String instrumentType =  NexusBaseClass.NX_INSTRUMENT.toString();
		return Node.SEPARATOR + entryName + ":" + entryType + Node.SEPARATOR + instrument + ":" + instrumentType;
	}

	private String getDetectorNodePath(String detectorName) {
		String detectorType = NexusBaseClass.NX_DETECTOR.toString();
		return getInstrumentNodePath() + Node.SEPARATOR + detectorName + ":" +detectorType;
	}

	public String getPlotName() {
		return plotName;
	}

	public void setPlotName(String plotName) {
		this.plotName = plotName;
	}

	@Override
	protected Collection<String> _getExtraNames() {
		return extraNames;
	}

	@Override
	protected Collection<String> _getOutputFormat() {
		return outputFormats;
	}

	/**
	 * Get a bean from the plot view and update list of rois
	 */
	private void updateRois() {
		roiList = RegionOfInterest.getRoisForPlot(plotName);
		for (RegionOfInterest roi : roiList) {
			if (Math.abs(roi.getAngle()) > RegionOfInterest.MAX_ROTATION_ANGLE ) {
				throw new IllegalStateException("Cannot take slice of rotated RegionOfInterest.");
			}
		}
	}

	public void updateNames() {
		createNames();
		createFormats();
	}
	public void setRois(List<RegionOfInterest> rois) {
		this.roiList.clear();
		this.roiList.addAll(rois);
	}

	public void setUseSingleDataGroupPerRoi(boolean useSingleDataGroupPerRoi) {
		this.useSingleDataGroupPerRoi = useSingleDataGroupPerRoi;
	}

	@Override
	public void atScanStart() {
		updateRois();
		updateNames();
		statsProcessor.atScanStart();
	}

	@Override
	public void atScanEnd() {
		createStatisticsDataLinks();
		statsProcessor.atScanEnd();
	}
	/**
	 * Roi names are only prefixed to the extraNames elements on this objet
	 * i.e. don't change the names on the statsProcessor itself
	 */
	private void createNames() {
		extraNames.clear();
		for (RegionOfInterest roi : roiList) {
			statsProcessor.getExtraNames().stream().map(name -> roi.getName() + "." + name).forEach(extraNames::add);
		}
	}

	private void createFormats() {
		outputFormats.clear();
		roiList.forEach(r -> outputFormats.addAll(statsProcessor.getOutputFormat()));
	}

	public DatasetStats getStatsProcessor() {
		return statsProcessor;
	}

	public void setStatsProcessor(DatasetStats statsProcessor) {
		this.statsProcessor = statsProcessor;
	}

	@Override
	public void configure() throws FactoryException {
		if (statsProcessor == null) {
			throw new FactoryException("Must have a stats processor set");
		}
		setConfigured(true);
	}

	public List<RegionOfInterest> getRoiList() {
		return new ArrayList<>(roiList);
	}

	public String getDetectorName() {
		return detectorName;
	}

	public void setDetectorName(String detectorName) {
		this.detectorName = detectorName;
	}

}
