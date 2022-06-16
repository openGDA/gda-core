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

import static gda.data.nexus.extractor.NexusExtractor.SDSClassName;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.dawnsci.nexus.NexusBaseClass;
import org.eclipse.january.dataset.Dataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.data.nexus.extractor.NexusGroupData;
import gda.data.nexus.tree.INexusTree;
import gda.data.nexus.tree.NexusTreeNode;
import gda.device.detector.GDANexusDetectorData;
import gda.device.detector.NXDetectorData;
import gda.device.detector.nexusprocessor.DatasetProcessorBase;
import gda.device.detector.nexusprocessor.DatasetStats;
import gda.factory.FactoryException;

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

	@Override
	public GDANexusDetectorData process(String detectorName, String dataName, Dataset dataset) throws Exception {
		NXDetectorData result = new NXDetectorData();
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
		var nXRoi = new NexusTreeNode(roi.getName(), NexusBaseClass.NX_COLLECTION.toString(), null);

		addToNode("x", roi.getX(), Integer.class, nXRoi);
		addToNode("y", roi.getY(), Integer.class, nXRoi);
		addToNode("width", roi.getWidth(), Integer.class, nXRoi);
		addToNode("height", roi.getHeight(), Integer.class, nXRoi);
		addToNode("angle", roi.getAngle(), Double.class, nXRoi);

		parent.addChildNode(nXRoi);
	}

	/**
	 * Write a name-value pair into a NexusTreeNode
	 * @param name metadata name
	 * @param value metadata value
	 * @param numClass class of Number
	 * @param node node to add to
	 */
	private void addToNode(String name, Number value, Class<? extends Number> numClass, NexusTreeNode node) {
		NexusGroupData dataGroup;
		if (numClass.equals(Integer.class)) {
			dataGroup = new NexusGroupData(value.intValue());
		} else if (numClass.equals(Double.class)) {
			dataGroup = new NexusGroupData(value.doubleValue());
		} else {
			logger.error("Unsupported metadata type: {} will not be written", numClass);
			return;
		}
		node.addChildNode(new NexusTreeNode(name, SDSClassName, null, dataGroup));
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

	@Override
	public void atScanStart() {
		updateRois();
		updateNames();
		statsProcessor.atScanStart();
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

}
