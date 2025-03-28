/*-
 * Copyright © 2022 Diamond Light Source Ltd.
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

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.dawnsci.nexus.NXobject;
import org.eclipse.dawnsci.nexus.NexusBaseClass;
import org.eclipse.dawnsci.nexus.NexusScanInfo;
import org.eclipse.dawnsci.nexus.builder.NexusObjectProvider;
import org.eclipse.dawnsci.nexus.builder.NexusObjectWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.detector.nexusprocessor.DatasetCreator;

/**
 * Defines configuration for which SWMR Malcolm processors should be used
 */
public class MalcolmProcessingManager {

	private static final Logger logger = LoggerFactory.getLogger(MalcolmProcessingManager.class);

	/**
	 * @param dataRank the rank of the data taken at each point of the scan, i.e. 2 for an image
	 * @param detFileNameSuffix the suffix of the file name of the nexus/h5 file to process
	 * @param dataPath the path to the dataset to process
	 * @param uidPath the path to the uid dataset
	 * @param detName the name of the detector/device
	 * @param nexusBaseClass the nexus base class for the
	 * @param datasetConverter applies a transformation to the dataset, if <code>null</code> the
	 * 		original dataset is used as-is
	 */
	public record Config(int dataRank, String detFileNameSuffix, String dataPath, String uidPath,
			String detName, NexusBaseClass nexusBaseClass, DatasetCreator datasetConverter) {
	}

	private Map<Config, Collection<MalcolmSwmrProcessor<?>>> processorMap = new HashMap<>();

	public Map<Config, Collection<MalcolmSwmrProcessor<?>>> getProcessorMap() {
		return processorMap;
	}

	public void setProcessorMap(Map<Config, Collection<MalcolmSwmrProcessor<?>>> processorMap) {
		this.processorMap = processorMap;
	}

	private Map<Config, SwmrMalcolmProcessingReader> activeReaders;

	private String dataGroupName;

	public String getDataGroupName() {
		return dataGroupName;
	}

	public void setDataGroupName(String dataGroupName) {
		this.dataGroupName = dataGroupName;
	}

	public void initialiseForProcessing(List<NexusObjectProvider<NXobject>> nxsFromMalcolm, NexusScanInfo scanInfo) {
		activeReaders = new HashMap<>();

		for (var conf : processorMap.keySet()) {
			// See if there is an nexus object provider for the config in the Nexus structure from the Malcolm Scan
			Optional<NexusObjectProvider<NXobject>> interesting = nxsFromMalcolm.stream()
					.filter(pr -> pr.getNexusBaseClass().equals(conf.nexusBaseClass))
					.filter(pr -> pr.getName().contains(conf.detName)).findFirst();
			interesting.ifPresent(nxs -> createSwmr(conf, nxs, scanInfo));
		}
	}

	private <N extends NXobject> void createSwmr(Config conf, NexusObjectProvider<N> nxs, NexusScanInfo scanInfo) {
		int[] shape = scanInfo.getOverallShape();
		int count = Arrays.stream(shape).reduce(1, (l, r) -> l * r);
		// Derive paths
		Path scanDirName = Paths.get(scanInfo.getFilePath().replace(".nxs", "")).getFileName();
		Path detDataFile = Paths.get(scanInfo.getFilePath()).getParent().resolve(scanDirName)
				.resolve(scanDirName + conf.detFileNameSuffix);

		// If processor is disabled do nothing so that not waiting for file which might not ever be created
		var processors = processorMap.get(conf).stream().filter(MalcolmSwmrProcessor::isEnabled).toList();
		if (processors.isEmpty()) {
			return;
		}

		for (MalcolmSwmrProcessor<?> p : processors) {
			@SuppressWarnings("unchecked")
			final MalcolmSwmrProcessor<N> proc = (MalcolmSwmrProcessor<N>) p;
			if (proc.getDataGroupName() == null) {
				// only set the data group name on the processor if one isn't already set, so that individual processors
				// can use different names via spring configuration
				proc.setDataGroupName(dataGroupName);
			}
			proc.initialise(scanInfo, (NexusObjectWrapper<N>) nxs);
		}

		logger.info("Initialising SWMR reader for {}", conf);
		var swmrReader = new SwmrMalcolmProcessingReader(detDataFile, count, conf.dataRank, processors, conf.dataPath,
				conf.uidPath, conf.datasetConverter);
		activeReaders.put(conf, swmrReader);
	}

	public void startSwmrReading() {
		for (var reader : activeReaders.entrySet()) {
			logger.info("Starting SWMR reading for {}", reader.getKey());
			reader.getValue().startAsyncReading();
		}
	}

	public void waitUntilComplete() {
		for (var reader : activeReaders.entrySet()) {
			logger.info("Waiting on SWMR reader: {} to complete", reader.getKey());
			reader.getValue().waitUntilComplete();
		}
		activeReaders.clear();
	}

	public void abortReaders() {
		for (var reader : activeReaders.entrySet()) {
			logger.info("Aborting SWMR reader for {}", reader.getKey());
			reader.getValue().abortReading();
		}
		activeReaders.clear();
	}

}
