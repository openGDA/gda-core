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
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.eclipse.dawnsci.nexus.NXpositioner;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusNodeFactory;
import org.eclipse.dawnsci.nexus.NexusScanInfo;
import org.eclipse.dawnsci.nexus.builder.NexusObjectProvider;
import org.eclipse.dawnsci.nexus.builder.NexusObjectWrapper;
import org.eclipse.january.dataset.LazyWriteableDataset;
import org.eclipse.scanning.api.annotation.scan.FileDeclared;
import org.eclipse.scanning.api.annotation.scan.PointStart;
import org.eclipse.scanning.api.annotation.scan.ScanAbort;
import org.eclipse.scanning.api.annotation.scan.ScanEnd;
import org.eclipse.scanning.api.annotation.scan.ScanFault;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.malcolm.core.MalcolmDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extension of Malcolm device to allow live processing to be performed within GDA
 * This is achieved using {@link SwmrMalcolmProcessingReader} responsible for reading
 * frames as they are available and passing the datasets into any defined processors
 */
public class ProcessingMalcolmDevice extends MalcolmDevice {

	private static final Logger logger = LoggerFactory.getLogger(ProcessingMalcolmDevice.class);

	private MalcolmProcessingManager processing = new MalcolmProcessingManager();

	private HklAdapter hklProvider;

	private LazyWriteableDataset hDataset;

	private LazyWriteableDataset kDataset;

	private LazyWriteableDataset lDataset;


	@FileDeclared
	public void startSwmrReader() {

		processing.startSwmrReading();
	}

	@Override
	public List<NexusObjectProvider<?>> getNexusProviders(NexusScanInfo info) throws NexusException {

		List<NexusObjectProvider<?>> malcNxs = super.getNexusProviders(info);

		processing.initialiseForProcessing(malcNxs, info);

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
		final int[] ones = new int[info.getOverallRank()];
		Arrays.fill(ones, 1);
		final int[] scanShape = info.getOverallShape();
		hDataset = new LazyWriteableDataset("h", Double.class, ones, scanShape, null, null);
		kDataset = new LazyWriteableDataset("k", Double.class, ones, scanShape, null, null);
		lDataset = new LazyWriteableDataset("l", Double.class, ones, scanShape, null, null);
		hDataset.setChunking(scanShape);
		kDataset.setChunking(scanShape);
		lDataset.setChunking(scanShape);
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
		List<IPosition> positions = StreamSupport.stream(pointGenerator.spliterator(), false).toList();
		Set<String> axesInScan = pointGenerator.getDimensionNames().stream().flatMap(List::stream).collect(Collectors.toSet());
		hklProvider.populateHkl(hDataset, kDataset, lDataset, positions, axesInScan);
	}

	@ScanAbort
	@ScanFault
	public void stopSwmr() {
		processing.abortReaders();
	}

	@ScanEnd
	public void closeProc() {
		long t = System.currentTimeMillis();
		logger.debug("Scan complete, waiting for processing to complete");
		processing.waitUntilComplete();
		logger.debug("End of scan processing complete");

		logger.debug("Wait time: {}", System.currentTimeMillis() - t);
	}

	public HklAdapter getHklProvider() {
		return hklProvider;
	}

	public void setHklProvider(HklAdapter hklProvider) {
		this.hklProvider = hklProvider;
	}

	public MalcolmProcessingManager getProcessing() {
		return processing;
	}

	public void setProcessing(MalcolmProcessingManager processing) {
		this.processing = processing;
	}



}
