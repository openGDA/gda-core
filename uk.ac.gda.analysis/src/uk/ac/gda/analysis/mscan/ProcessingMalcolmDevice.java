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

import static org.eclipse.dawnsci.nexus.NexusBaseClass.NX_DETECTOR;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.eclipse.dawnsci.nexus.NXdetector;
import org.eclipse.dawnsci.nexus.NXpositioner;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusNodeFactory;
import org.eclipse.dawnsci.nexus.NexusScanInfo;
import org.eclipse.dawnsci.nexus.builder.NexusObjectProvider;
import org.eclipse.dawnsci.nexus.builder.NexusObjectWrapper;
import org.eclipse.january.dataset.LazyWriteableDataset;
import org.eclipse.scanning.api.annotation.scan.FileDeclared;
import org.eclipse.scanning.api.annotation.scan.PointStart;
import org.eclipse.scanning.api.annotation.scan.ScanEnd;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.malcolm.core.MalcolmDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.detector.nexusprocessor.DatasetCreator;
import gda.device.detector.nexusprocessor.MaskedDatasetCreator;

/**
 * Extension of Malcolm device to allow live processing to be performed within GDA
 * This is achieved using {@link SwmrMalcolmProcessingReader} responsible for reading
 * frames as they are available and passing the datasets into any defined processors
 */
public class ProcessingMalcolmDevice extends MalcolmDevice {

	private static final Logger logger = LoggerFactory.getLogger(ProcessingMalcolmDevice.class);

	private SwmrMalcolmProcessingReader swmrReader;

	private Collection<MalcolmSwmrProcessor> processors = Arrays.asList((MalcolmSwmrProcessor) new RoiProc(), new PlotProc());

	private String detFileNameSuffix = "-DIFFRACTION.h5";
	private String detFrameEntry = "/entry/data";
	private String detUidEntry = "/entry/uid";
	private String primaryDataName = "DETECTOR";

	private HklAdapter hklProvider;

	private LazyWriteableDataset hDataset;

	private LazyWriteableDataset kDataset;

	private LazyWriteableDataset lDataset;

	private DatasetCreator datasetCreator;

	@FileDeclared
	public void startSwmrReader() {
		if (swmrReader != null) {
			swmrReader.startAsyncReading();
		}
	}

	@Override
	public List<NexusObjectProvider<?>> getNexusProviders(NexusScanInfo info) throws NexusException {

		List<NexusObjectProvider<?>> malcNxs = super.getNexusProviders(info);

		Optional<NexusObjectProvider<?>> interesting = malcNxs.stream()
				.filter(pr -> pr.getNexusBaseClass().equals(NX_DETECTOR))
				.filter(pr -> pr.getName().contains(primaryDataName))
				.findFirst();
		if (interesting.isEmpty()) {
			return malcNxs;
		}
		@SuppressWarnings("unchecked")
		final NexusObjectWrapper<NXdetector> mDetWrapper = (NexusObjectWrapper<NXdetector>) interesting.get();

		// Constructing the detector data filename from the scan file name
		Path scanDirName = Paths.get(info.getFilePath().replace(".nxs", "")).getFileName();
		Path detDatafile = Paths.get(info.getFilePath()).getParent().resolve(scanDirName).resolve(scanDirName + detFileNameSuffix);

		int[] shape = info.getShape();
		int count = Arrays.stream(shape).reduce(1, (l, r) -> l * r);



		processors.forEach(p -> p.initialise(info, mDetWrapper));
		swmrReader = new SwmrMalcolmProcessingReader(detDatafile, count, processors, detFrameEntry, detUidEntry, datasetCreator);

		if (hklProvider != null) {
			prepareHkl(info, malcNxs);
		}

		return malcNxs;
	}

	private void prepareHkl(NexusScanInfo info, List<NexusObjectProvider<?>> main) {
		NXpositioner hklDet = NexusNodeFactory.createNXpositioner();
		NexusObjectWrapper<NXpositioner> hklWrapper = new NexusObjectWrapper<>("hkl", hklDet);
		createHklDatasets(info, hklWrapper);
		main.add(hklWrapper);
	}


	private void createHklDatasets(NexusScanInfo info, NexusObjectWrapper<NXpositioner> wrapper) {
		int[] ones = new int[info.getShape().length];
		Arrays.fill(ones, 1);
		hDataset = new LazyWriteableDataset("h", Double.class, ones, info.getShape(), null, null);
		kDataset = new LazyWriteableDataset("k", Double.class, ones, info.getShape(), null, null);
		lDataset = new LazyWriteableDataset("l", Double.class, ones, info.getShape(), null, null);
		hDataset.setChunking(info.getShape());
		kDataset.setChunking(info.getShape());
		lDataset.setChunking(info.getShape());
		hDataset.setFillValue(null);
		kDataset.setFillValue(null);
		lDataset.setFillValue(null);
		wrapper.getNexusObject().createDataNode("h", hDataset);
		wrapper.getNexusObject().createDataNode("k", kDataset);
		wrapper.getNexusObject().createDataNode("l", lDataset);
		wrapper.addAxisDataFieldNames("h", "k", "l");

	}

	@PointStart
	public void writeHkl() {
		if (hklProvider == null) {
			return;
		}
		List<IPosition> positions = StreamSupport.stream(pointGenerator.spliterator(), false).collect(Collectors.toList());
		Set<String> axesInScan = pointGenerator.getDimensionNames().stream().flatMap(Set::stream).collect(Collectors.toSet());
		hklProvider.populateHkl(hDataset, kDataset, lDataset, positions, axesInScan);
	}

	@ScanEnd
	public void closeProc() {
		long t = System.currentTimeMillis();
		logger.debug("Scan complete, waiting for processing to complete");
		if (swmrReader != null) {
			swmrReader.waitUntilComplete();
		}
		logger.debug("End of scan processing complete");

		logger.debug("Wait time: {}", System.currentTimeMillis() - t);
	}

	public Collection<MalcolmSwmrProcessor> getProcessors() {
		return processors;
	}

	public void setProcessors(Collection<MalcolmSwmrProcessor> processors) {
		this.processors = processors;
	}

	public String getDetFileNameSuffix() {
		return detFileNameSuffix;
	}

	public void setDetFileNameSuffix(String detFileNameSuffix) {
		this.detFileNameSuffix = detFileNameSuffix;
	}

	public String getDetFrameEntry() {
		return detFrameEntry;
	}

	public void setDetFrameEntry(String detFrameEntry) {
		this.detFrameEntry = detFrameEntry;
	}

	public String getDetUidEntry() {
		return detUidEntry;
	}

	public void setDetUidEntry(String detUidEntry) {
		this.detUidEntry = detUidEntry;
	}

	public HklAdapter getHklProvider() {
		return hklProvider;
	}

	public void setHklProvider(HklAdapter hklProvider) {
		this.hklProvider = hklProvider;
	}

	public String getPrimaryDataName() {
		return primaryDataName;
	}

	public void setPrimaryDataName(String primaryDataName) {
		this.primaryDataName = primaryDataName;
	}

	public void setDatasetCreator(MaskedDatasetCreator datasetCreator) {
		this.datasetCreator = datasetCreator;
	}

}
