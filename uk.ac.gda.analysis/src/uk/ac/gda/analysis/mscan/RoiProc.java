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
package uk.ac.gda.analysis.mscan;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import org.eclipse.dawnsci.analysis.dataset.slicer.SliceFromSeriesMetadata;
import org.eclipse.dawnsci.nexus.NXdetector;
import org.eclipse.dawnsci.nexus.NexusScanInfo;
import org.eclipse.dawnsci.nexus.builder.NexusObjectWrapper;
import org.eclipse.january.DatasetException;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.LazyWriteableDataset;
import org.eclipse.january.dataset.Slice;
import org.eclipse.january.dataset.SliceND;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.detector.nexusprocessor.roistats.RegionOfInterest;
/**
 * Read ROIs from the plotting system and calculate stats for each ROI
 */
public class RoiProc implements MalcolmSwmrProcessor {

	private static final Logger logger = LoggerFactory.getLogger(RoiProc.class);

	private Set<RegionOfInterest> rois = new HashSet<>();
	private String plotName = "Area Detector";
	private NexusObjectWrapper<NXdetector> nexusProvider;
	private Map<RegionOfInterest, List<LazyWriteableDataset>> datasets = new HashMap<>();
	private Map<RegionOfInterest, Double> lastestStatForRoi = new HashMap<>();
	private int[] scanShape;

	@Override
	public void initialise(NexusScanInfo info, NexusObjectWrapper<NXdetector> nexusWrapper) {
		this.nexusProvider = nexusWrapper;
		createDetectorNexusObj(info);
	}

	/**
	 * Get a bean from the plot view and update list of rois
	 */
	private void updateRois() {
		rois.clear();
		rois.addAll(RegionOfInterest.getRoisForPlot(plotName));
	}

	private void createDetectorNexusObj(NexusScanInfo info) {
		updateRois();
		scanShape = info.getShape();
		datasets.clear();
		for (RegionOfInterest roi : rois) {
			LazyWriteableDataset sumData = makeEmptyDataset(roi.getName() + "_sum");
			// Currently only recording the sum
			//LazyWriteableDataset meanData = makeEmptyDataset(roi.getNamePrefix() + "_mean");
			//LazyWriteableDataset maxData = makeEmptyDataset(roi.getNamePrefix() + "_max");
			datasets.put(roi, Arrays.asList(sumData));
		}
	}

	private LazyWriteableDataset makeEmptyDataset(String name) {
		int[] ones = new int[scanShape.length];
		Arrays.fill(ones, 1);
		LazyWriteableDataset dataset = new LazyWriteableDataset(name, Double.class, ones, scanShape, null, null);
		dataset.setChunking(scanShape);
		nexusProvider.getNexusObject().createDataNode(name, dataset);
		nexusProvider.addAdditionalPrimaryDataFieldName(name);
		return dataset;
	}

	@Override
	public void processFrame(Dataset data, SliceFromSeriesMetadata metaSlice) {
		logger.debug("Start of processFrame");
		for (RegionOfInterest roi : rois) {
			Dataset roiDataset = data.squeeze().getSliceView(roi.getSlice());
			writeRoiStat(roi, roiDataset, metaSlice, Dataset::sum, datasets.get(roi).get(0));
			//writeRoiStat(roi, roiDataset, metaSlice, Dataset::mean, datasets.get(roi).get(1));
			//writeRoiStat(roi, roiDataset, metaSlice, Dataset::max, datasets.get(roi).get(2));
		}
		logger.debug("End of processFrame");
	}

	public int getNRois() {
		return rois.size();
	}

	public void processFrame(ILazyDataset data, SliceFromSeriesMetadata metaSlice) {
		logger.debug("Start of processFrame");
		for (RegionOfInterest roi : rois) {
			Dataset roiDataset;
			try {
				roiDataset = DatasetUtils.convertToDataset(data.squeezeEnds().getSlice(roi.getSlice()));
			} catch (DatasetException e) {
				logger.error("Could not slice data", e);
				return;
			}
			writeRoiStat(roi, roiDataset, metaSlice, Dataset::sum, datasets.get(roi).get(0));
			//writeRoiStat(roi, roiDataset, metaSlice, Dataset::mean, datasets.get(roi).get(1));
			//writeRoiStat(roi, roiDataset, metaSlice, Dataset::max, datasets.get(roi).get(2));
		}
		logger.debug("End of processFrame");
	}

	private void writeRoiStat(RegionOfInterest roi, Dataset roiData, SliceFromSeriesMetadata meta,
			Function<Dataset, Object> stat, LazyWriteableDataset statDataset) {
		Object statResult = stat.apply(roiData);
		lastestStatForRoi.put(roi, ((Number)statResult).doubleValue());
		logger.debug("Statistic: {}, ROI: {},  value: {}", statDataset.getName(), roi.getName(), statResult);
		Dataset newStatData = DatasetFactory.createFromObject(statResult);
		SliceND statSlice = new SliceND(statDataset.getShape(), statDataset.getMaxShape(), (Slice[]) null);
		Slice[] inputSlice = meta.getSliceFromInput();
		for (int i = 0; i < statDataset.getRank(); i++) {
			statSlice.setSlice(i, inputSlice[i]);
		}
		try {
			statDataset.setSlice(null, newStatData, statSlice);
		} catch (DatasetException e) {
			logger.error("Error setting slice", e);
		}
	}

	public Set<RegionOfInterest> getRois() {
		return rois;
	}

	public Double latestStatForRoi(RegionOfInterest roi) {
		return lastestStatForRoi.get(roi);
	}

	public String getPlotName() {
		return plotName;
	}

	public void setPlotName(String plotName) {
		this.plotName = plotName;
	}
}
