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

package uk.ac.gda.analysis.mscan;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.concat;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.dawnsci.analysis.dataset.slicer.SliceFromSeriesMetadata;
import org.eclipse.dawnsci.nexus.NXdetector;
import org.eclipse.dawnsci.nexus.NexusScanInfo;
import org.eclipse.dawnsci.nexus.builder.NexusObjectWrapper;
import org.eclipse.january.DatasetException;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.LazyWriteableDataset;
import org.eclipse.january.dataset.Slice;
import org.eclipse.january.dataset.SliceND;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.detector.nexusprocessor.roistats.RegionOfInterest;
import gda.device.scannable.ScannableUtils;
import gda.jython.JythonServerFacade;

/**
 * Processor for Malcolm scans which calculates a background subtracted signal using ROIs
 * and optionally normalise using values from transmission and attenuator scannables.
 * <p>
 * Adapted from beamline provided Jython script.
 */
public class NormProc extends AbstractMalcolmSwmrProcessor<NXdetector> {

	private static final Logger logger = LoggerFactory.getLogger(NormProc.class);

	private NexusObjectWrapper<NXdetector> nexusProvider;
	private LazyWriteableDataset dataset;

	private RoiProc roiProc;

	private int signalRoiIndex;
	private List<Integer> backgroundRoiIndices = List.of(1);
	private boolean backgroundSubtractionEnabled = true;
	private boolean normEnabled;
	private String attenuatorScannableName;
	private Scannable attenuatorScannable;
	private String transmissionFieldName;
	private double scale = 1.0;

	@Override
	public void initialise(NexusScanInfo info, NexusObjectWrapper<NXdetector> nexusWrapper) {
		this.nexusProvider = nexusWrapper;
		createDetectorNexusObj(info);

		// Need to check that the sufficient number of rois are actually here
		try {
			attenuatorScannable = (Scannable) JythonServerFacade.getInstance().getFromJythonNamespace(attenuatorScannableName);
		} catch (Exception e) {
			attenuatorScannable = null;
			normEnabled = false;
		}

	}

	private void createDetectorNexusObj(NexusScanInfo info) {
		int[] ones = new int[info.getOverallRank()];
		Arrays.fill(ones, 1);

		dataset = new LazyWriteableDataset("norm", Double.class, ones, info.getOverallShape(), null, null);
		dataset.setChunking(info.getOverallShape());
		nexusProvider.getNexusObject().createDataNode("norm", dataset);
		nexusProvider.addAdditionalPrimaryDataFieldName("norm");
	}

	@Override
	public void processFrame(Dataset data, SliceFromSeriesMetadata metaSlice) {
		logger.debug("Start of processFrame");
		writeDouble(doNormWork(), metaSlice);
		logger.debug("End of processFrame");
	}

	private double doNormWork() {
		var signalRoi = getRegionFromSet("Region_" + signalRoiIndex);
		var signalSum = roiProc.latestStatForRoi(signalRoi);

		if(!backgroundSubtractionEnabled) {
			return normalise(signalSum * scale);
		}

		var signalArea = signalRoi.getArea();
		var backgroundRois = backgroundRoiIndices.stream().map(i -> getRegionFromSet("Region_" + i)).collect(Collectors.toList());
		double backgroundSum = backgroundRois.stream().mapToDouble(roiProc::latestStatForRoi).sum();
		int backgroundArea = backgroundRois.stream().mapToInt(RegionOfInterest::getArea).sum();

		double meanbg = 0;
		if (backgroundArea != 0) {
			meanbg = (backgroundSum * signalArea) / backgroundArea;
		}

		// normalise by filter transmission

		return normalise((signalSum - meanbg) * scale);
	}

	private double normalise(double input) {
		if (normEnabled) {
			double[] attenPos;
			try {
				attenPos = ScannableUtils.getCurrentPositionArray(attenuatorScannable);
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

	private RegionOfInterest getRegionFromSet(String prefix) {
		return roiProc.getRois().stream().filter(r -> r.getName().startsWith(prefix)).findFirst().orElseThrow();
	}

	private void writeDouble(double data, SliceFromSeriesMetadata metaSlice) {
		Dataset s = DatasetFactory.createFromObject(data);
		SliceND sl = new SliceND(dataset.getShape(), dataset.getMaxShape(), (Slice[]) null);

		Slice[] si = metaSlice.getSliceFromInput();
		for (int i = 0; i < dataset.getRank(); i++) {
			sl.setSlice(i, si[i]);
		}

		try {
			dataset.setSlice(null, s, sl);
		} catch (DatasetException e) {
			logger.error("Error setting slice", e);
		}
	}

	public RoiProc getRoiProc() {
		return roiProc;
	}

	public void setRoiProc(RoiProc roiProc) {
		this.roiProc = roiProc;
	}

	public int getSignalRoiIndex() {
		return signalRoiIndex;
	}

	public void setSignalRoiIndex(int signalRoiIndex) {
		this.signalRoiIndex = signalRoiIndex;
	}

	public List<Integer> getBackgroundRoiIndices() {
		return backgroundRoiIndices;
	}

	public void setBackgroundRoiIndices(List<Integer> backgroundRoiIndices) {
		this.backgroundRoiIndices = backgroundRoiIndices;
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
	}

}
