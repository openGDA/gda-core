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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROIList;
import org.eclipse.january.dataset.Dataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.data.nexus.extractor.NexusGroupData;
import gda.device.detector.GDANexusDetectorData;
import gda.device.detector.NXDetectorData;
import gda.device.detector.nexusprocessor.DataSetProcessorBase;
import gda.device.detector.nexusprocessor.DatasetStats;
import gda.device.detector.nexusprocessor.roistats.RegionOfInterest.RoiMetadata;
import gda.factory.FactoryException;
import uk.ac.diamond.scisoft.analysis.SDAPlotter;
import uk.ac.diamond.scisoft.analysis.plotserver.GuiBean;
import uk.ac.diamond.scisoft.analysis.plotserver.GuiParameters;

/**
 * Receive dataset from processing Nexus Detector Retrieve rois from gui.
 * <p>
 * For each roi, create a dataset for it and pass into a {@link DatasetStats} processor.
 */
public class RoiStatsProcessor extends DataSetProcessorBase {

	private List<String> extraNames = new ArrayList<>();
	private List<String> outputFormats = new ArrayList<>();
	private List<RegionOfInterest> roiList = new ArrayList<>();
	private String plotName;
	private DatasetStats statsProcessor;

	private static final Logger logger = LoggerFactory.getLogger(RoiStatsProcessor.class);

	@Override
	public GDANexusDetectorData process(String detectorName, String dataName, Dataset dataset) throws Exception {
		NXDetectorData result = new NXDetectorData();
		for (RegionOfInterest roi : roiList) {
			Set<RoiMetadata> rois = roi.getRoiMetadata();
			NXDetectorData roiData = new NXDetectorData(rois.stream().map(RoiMetadata::getName).toArray(String[]::new), rois.stream().map(RoiMetadata::getFormat).toArray(String[]::new), detectorName);
			for (RoiMetadata roiMeta : rois) {
				// in addData the null args are: units, signalVal and interpretation
				roiData.addData(detectorName, roiMeta.getName(), new NexusGroupData(roiMeta.getData()), null, null, null, true);
				roiData.setPlottableValue(roiMeta.getName(), roiMeta.getData());
			}
			roiData.mergeIn(statsProcessor.process(detectorName, roi.getNamePrefix(), dataset.getSliceView(roi.getSlice())));
			result.mergeIn(roiData);
		}
		return result;
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
		GuiBean bean;
		roiList.clear();
		try {
			bean = SDAPlotter.getGuiBean(plotName);
		} catch (Exception e) {
			logger.error("Could not get gui bean for plot: {}", plotName, e);
			return;
		}
		Serializable roiListS = bean.get(GuiParameters.ROIDATALIST);

		if (roiListS instanceof RectangularROIList) {
			ArrayList<RectangularROI> rawRoiList = new ArrayList<>((RectangularROIList) roiListS);
			rawRoiList.sort(Comparator.comparing(RectangularROI::getName));
			// isPlot corresponds to the "Active" property in the GUI (not visible)
			rawRoiList.removeIf(roi -> !roi.isPlot());
			logger.info("Rois defined on {}: {}", plotName, rawRoiList);
			rawRoiList.stream().map(RegionOfInterest::new).forEach(roiList::add);
		} else {
			// It is null or not rectangular rois
			logger.warn("No rois defined");
			roiList.clear();
		}
	}

	@Override
	public void atScanStart() {
		updateRois();
		createNames();
		createFormats();
		statsProcessor.atScanStart();
	}


	/**
	 * Roi names are only prefixed to the extraNames elements on this objet
	 * i.e. don't change the names on the statsProcessor itself
	 */
	private void createNames() {
		extraNames.clear();
		for (RegionOfInterest roi : roiList) {
			roi.getRoiMetadata().stream().map(RoiMetadata::getName).forEach(extraNames::add);
			statsProcessor.getExtraNames().stream().map(name -> roi.getNamePrefix() + "_" + name).forEach(extraNames::add);
		}
	}

	private void createFormats() {
		outputFormats.clear();
		for (RegionOfInterest roi : roiList) {
			roi.getRoiMetadata().stream().map(RoiMetadata::getFormat).forEach(outputFormats::add);
			statsProcessor.getOutputFormat().forEach(outputFormats::add);
		}
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

}
