/*-
 * Copyright © 2021 Diamond Light Source Ltd.
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

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.concat;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.data.nexus.extractor.NexusGroupData;
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.detector.GDANexusDetectorData;
import gda.device.detector.NXDetectorData;
import gda.device.detector.nexusprocessor.DatasetProcessorBase;
import gda.device.scannable.ScannableUtils;
import gda.jython.JythonServerFacade;

/**
 * Processor which calculates a background subtracted signal using ROIs and normalises using values from
 * transmission and attenuator scannables.  Both of these are optional but by default background subtraction
 * is enabled but normalisation is disabled.
 * <p>
 * Adapted from beamline provided Jython script.
 */
public class NormalisingRegionProcessor extends DatasetProcessorBase {

	private static final Logger logger = LoggerFactory.getLogger(NormalisingRegionProcessor.class);

	private int signalRoiIndex;
	private List<Integer> backgroundRoiIndices = List.of(1);
	private boolean normEnabled;
	private boolean backgroundSubtractionEnabled = true;
	private String attenuatorScannableName;
	private Scannable attenuatorScannable;
	private String transmissionFieldName;
	private double scale = 1.0;
	private RoiStatsProcessor roiStats;
	private int requiredRoiCount = 2;

	@Override
	public void atScanStart() {
		// Need to check that the sufficient number of rois are actually here
		try {
			attenuatorScannable = (Scannable) JythonServerFacade.getInstance()
					.getFromJythonNamespace(attenuatorScannableName);
		} catch (Exception e) {
			attenuatorScannable = null;
			normEnabled = false;
		}
	}

	@Override
	public GDANexusDetectorData process(String detectorName, String dataName, Dataset dataset) throws Exception {
		// TODO probably avoid calling process twice but would need a way to ensure that roiStats is processed first
		verifyRois();
		GDANexusDetectorData nxsData = roiStats.process(detectorName, dataName, dataset);
		// signal sum
		String signalPrefix = roiStats.getRoiList().get(signalRoiIndex).getName();
		double sum = getDoubleValueFromNxsData(nxsData, signalPrefix + ".total");

		double result;
		if (backgroundSubtractionEnabled) {
			// signal area
			var area = roiStats.getRoiList().get(signalRoiIndex).getArea();

			// mean background, background area
			var rois = backgroundRoiIndices.stream().map(i -> roiStats.getRoiList().get(i))
					.collect(Collectors.toList());
			double backgroundSum = rois.stream().map(RegionOfInterest::getName)
					.mapToDouble(rs -> getDoubleValueFromNxsData(nxsData, rs + ".total")).sum();

			int backgroundArea = rois.stream().mapToInt(RegionOfInterest::getArea).sum();

			double meanbg = 0;
			if (backgroundArea != 0) {
				meanbg = (backgroundSum * area) / backgroundArea;
			}

			// normalise by filter transmission

			result = normalise((sum - meanbg) * scale);

		} else {
			result = normalise(sum * scale);
		}

		NXDetectorData res = new NXDetectorData(_getExtraNames().toArray(String[]::new),
				_getOutputFormat().stream().toArray(String[]::new), dataName);
		NexusGroupData data = new NexusGroupData(DatasetFactory.createFromObject(result, 1));
		data.isDetectorEntryData = true;
		res.addData(detectorName, "norm", data, null, 1);
		res.setPlottableValue("norm", result);

		return res;
	}

	/**
	 * Verify that there are at least the minimum number of ROIs defined on the plot for this processor to work.
	 * <p>
	 * Ideally this would be called in atScanStart but at the moment we don't have guarantee that atScanStart is called
	 * first for the roiStats processor (which is where the ROIs are updated from the plot)
	 *
	 * @throws IllegalStateException
	 *             if an index for signal or background ROI is not present in the ROI list
	 */
	private void verifyRois() {
		var rois = roiStats.getRoiList().size();
		if (rois < requiredRoiCount) {
			throw new IllegalStateException("Insufficient active ROIs for normalisation");
		}
	}

	private double normalise(double input) {
		if (normEnabled) {
			Double[] attenPos;
			try {
				attenPos = ScannableUtils.objectToArray(attenuatorScannable.getPosition());
			} catch (DeviceException e) {
				logger.error("Error, not going to normalise by transmision", e);
				return input;
			}
			var allNames = concat(stream(attenuatorScannable.getInputNames()),
					stream(attenuatorScannable.getExtraNames())).collect(toList());
			double transmissionFactor = attenPos[allNames.indexOf(transmissionFieldName)];
			return input / transmissionFactor;
		} else {
			return input;
		}
	}

	private double getDoubleValueFromNxsData(GDANexusDetectorData data, String name) {
		var sigIndex = Arrays.asList(data.getExtraNames()).indexOf(name);
		return data.getDoubleVals()[sigIndex];
	}

	private void updateRequiredRoiCount() {
		int maxRoiIndex = Math.max(signalRoiIndex,
				backgroundRoiIndices.stream().mapToInt(Integer::intValue).max().orElse(0));
		requiredRoiCount = backgroundSubtractionEnabled ?  maxRoiIndex + 1 : signalRoiIndex;
	}

	@Override
	protected Collection<String> _getExtraNames() {
		return Collections.singletonList("norm");
	}

	@Override
	protected Collection<String> _getOutputFormat() {
		return Collections.singleton("%5.5g");
	}

	public RoiStatsProcessor getRoiStats() {
		return roiStats;
	}

	public void setRoiStats(RoiStatsProcessor roiStats) {
		this.roiStats = roiStats;
	}

	public int getSignalRoiIndex() {
		return signalRoiIndex;
	}

	public void setSignalRoiIndex(int signalRoiIndex) {
		this.signalRoiIndex = signalRoiIndex;
		updateRequiredRoiCount();
	}

	public List<Integer> getBackgroundRoiIndices() {
		return backgroundRoiIndices;
	}

	public void setBackgroundRoiIndices(List<Integer> backgroundRoiIndices) {
		this.backgroundRoiIndices = backgroundRoiIndices;
		updateRequiredRoiCount();
	}

	public boolean isNorm() {
		return normEnabled;
	}

	public void setNorm(boolean norm) {
		this.normEnabled = norm;
	}

	public String getAttenuatorScannableName() {
		return attenuatorScannableName;
	}

	public void setAttenuatorScannableName(String attenuatorScannableName) {
		this.attenuatorScannableName = attenuatorScannableName;
	}

	public Scannable getAttenuatorScannable() {
		return attenuatorScannable;
	}

	public void setAttenuatorScannable(Scannable attenuatorScannable) {
		this.attenuatorScannable = attenuatorScannable;
	}

	public String getTransmissionFieldName() {
		return transmissionFieldName;
	}

	public void setTransmissionFieldName(String transmissionFieldName) {
		this.transmissionFieldName = transmissionFieldName;
	}

	public double getScale() {
		return scale;
	}

	public void setScale(double scale) {
		this.scale = scale;
	}

	public boolean isBackgroundSubtractionEnabled() {
		return backgroundSubtractionEnabled;
	}

	public void setBackgroundSubtractionEnabled(boolean backgroundSubtractionEnabled) {
		this.backgroundSubtractionEnabled = backgroundSubtractionEnabled;
		updateRequiredRoiCount();
	}

}
