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

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.dawnsci.analysis.dataset.slicer.SliceFromSeriesMetadata;
import org.eclipse.dawnsci.nexus.NXdetector;
import org.eclipse.dawnsci.nexus.NexusScanInfo;
import org.eclipse.dawnsci.nexus.builder.AbstractNexusObjectProvider;
import org.eclipse.january.DatasetException;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.ILazyWriteableDataset;
import org.eclipse.january.dataset.Slice;
import org.eclipse.january.dataset.SliceND;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.detector.nexusprocessor.roistats.RegionOfInterest;
import gda.device.scannable.ScannableUtils;
import gda.jython.InterfaceProvider;
import gda.jython.JythonServerFacade;

/**
 * Processor for Malcolm scans which calculates a background subtracted signal using ROIs
 * and optionally normalise using values from transmission and attenuator scannables.
 * <p>
 * Adapted from beamline provided Jython script.
 */
public class NormProc extends AbstractMalcolmSwmrProcessor<NXdetector> {

	private static final Logger logger = LoggerFactory.getLogger(NormProc.class);

	private static final String FIELD_NAME_NORM = "norm";

	private ILazyWriteableDataset normDataset;

	private RoiProc roiProc;

	private int signalRoiIndex;
	private List<Integer> backgroundRoiIndices = List.of(1);
	private boolean backgroundSubtractionEnabled = true;
	private boolean normEnabled;
	private String attenuatorScannableName;
	private Scannable attenuatorScannable;
	private String transmissionFieldName;
	private double scale = 1.0;
	private int requiredRoiCount = 2;
	private Scannable monitorScannable;

	@Override
	public void initialise(NexusScanInfo info, AbstractNexusObjectProvider<NXdetector> nexusProvider) {
		super.initialise(info, nexusProvider);

		// Need to check that the sufficient number of rois are actually here
		try {
			attenuatorScannable = (Scannable) JythonServerFacade.getInstance().getFromJythonNamespace(attenuatorScannableName);
		} catch (Exception e) {
			attenuatorScannable = null;
			normEnabled = false;
		}

	}

	@Override
	protected void configureNexusProvider(AbstractNexusObjectProvider<NXdetector> nexusObjectProvider) {
		normDataset = createField(FIELD_NAME_NORM, Double.class);
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

		int signalArea = signalRoi.getArea();
		List<RegionOfInterest> backgroundRois = backgroundRoiIndices.stream().map(i -> getRegionFromSet("Region_" + i)).collect(Collectors.toList());
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
			return (input / transmissionFactor) / getMonitorScannableValue();
		} else {
			return input / getMonitorScannableValue();
		}
	}

	private double getMonitorScannableValue() {
		if(monitorScannable != null) {
			try {
				return (double) monitorScannable.getPosition();
			} catch (DeviceException | NumberFormatException e) {
				logger.error("Error getting value from monitored scannable " + monitorScannable.getName(), e);
				InterfaceProvider.getTerminalPrinter().print("Could not get value from monitored scannable " +monitorScannable.getName()
					+", normalisation will not be applied to this point");
			}
		}
		return 1;
	}

	private RegionOfInterest getRegionFromSet(String prefix) {
		return roiProc.getRois().stream().filter(r -> r.getName().startsWith(prefix)).findFirst().orElseThrow();
	}

	private void writeDouble(double data, SliceFromSeriesMetadata metaSlice) {
		Dataset s = DatasetFactory.createFromObject(data);
		SliceND sl = new SliceND(normDataset.getShape(), normDataset.getMaxShape(), (Slice[]) null);

		Slice[] si = metaSlice.getSliceFromInput();
		for (int i = 0; i < normDataset.getRank(); i++) {
			sl.setSlice(i, si[i]);
		}

		try {
			normDataset.setSlice(null, s, sl);
		} catch (DatasetException e) {
			logger.error("Error setting slice", e);
		}
	}

	private void updateRequiredRoiCount() {
		int maxRoiIndex = Math.max(signalRoiIndex, backgroundRoiIndices.stream().mapToInt(Integer::intValue).max().orElse(0));
		requiredRoiCount = backgroundSubtractionEnabled ?  maxRoiIndex + 1 : signalRoiIndex + 1;
	}

	private boolean areThereEnoughRois() {
		return roiProc.getRois().size() >= requiredRoiCount;
	}

	@Override
	public boolean isEnabled() {
		return super.isEnabled() && areThereEnoughRois();
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

	public void setMonitorScannable(Scannable monitorScannable) {
		this.monitorScannable = monitorScannable;
	}
}
