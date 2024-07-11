/*-
 * Copyright © 2024 Diamond Light Source Ltd.
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

package gda.data.nexus;

import static java.util.stream.Collectors.toMap;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.analysis.api.tree.Node;
import org.eclipse.dawnsci.nexus.INexusFileFactory;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusFile;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.scanning.api.scan.IFilePathService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.scan.ScanEvent.EventType;
import uk.ac.diamond.daq.api.messaging.messages.INexusMetadataExtractor;
import uk.ac.diamond.daq.api.messaging.messages.ScanMetadataMessage;
import uk.ac.diamond.daq.api.messaging.messages.ScanStatus;
import uk.ac.diamond.osgi.services.ServiceProvider;

public class NexusMetadataExtractor implements INexusMetadataExtractor {

	private static final Logger logger = LoggerFactory.getLogger(NexusMetadataExtractor.class);

	public static Path getPropertiesFilePath(ScanStatus scanStatus) {
		final String fileName = switch (scanStatus) {
			case STARTED -> START_SCAN_METADATA_NODE_PATHS_FILE_NAME;
			case FINISHED -> END_SCAN_METADATA_NODE_PATHS_FILE_NAME;
			default -> throw new IllegalArgumentException("Event type must be one of " + EventType.STARTED + " or " + EventType.FINISHED);
		};

		final String varDir = ServiceProvider.getService(IFilePathService.class).getPersistenceDir();
		return Path.of(varDir, fileName);
	}

	@Override
	public boolean isEnabled() {
		return Files.exists(getPropertiesFilePath(ScanStatus.STARTED));
	}

	@Override
	public ScanMetadataMessage createScanMetadataMessage(ScanStatus scanStatus, String nexusFilePath) {
		if (scanStatus != ScanStatus.STARTED && scanStatus != ScanStatus.FINISHED)
			throw new IllegalArgumentException("Event type must be one of " + EventType.STARTED + " or " + EventType.FINISHED);

		final Path metadataPathsFilePath = getPropertiesFilePath(scanStatus);
		if (Files.exists(metadataPathsFilePath)) {
			try {
				final List<String> metadataNodePaths = Files.readAllLines(metadataPathsFilePath);
				final Map<String, Object> scanMetadata = loadMetadataFromNexusFile(metadataNodePaths, nexusFilePath);
				return new ScanMetadataMessage(scanStatus, nexusFilePath, scanMetadata);
			} catch (IOException e) {
				logger.error("Could not load metadata node paths: {}", metadataPathsFilePath, e);
			} catch (NexusException e) {
				logger.error("Could not load nexus file: {}", nexusFilePath, e);
			}
		}
		return null;
	}

	private Map<String, Object> loadMetadataFromNexusFile(List<String> metadataPaths, String nexusFilePath) throws NexusException {
		try (NexusFile nexusFile = ServiceProvider.getService(INexusFileFactory.class).newNexusFile(nexusFilePath)) {
			nexusFile.openToRead();

			// Note: cannot collect to map immediately and return null values as the value mapper cannot produce null values:
			// https://bugs.openjdk.org/browse/JDK-8148463
			return metadataPaths.stream()
					.map(nodePath -> Pair.of(nodePath, getMetadataNodeValue(nexusFile, nodePath)))
					.filter(pathValuePair -> pathValuePair.getValue() != null)
					.collect(toMap(Pair::getKey, Pair::getValue));
		}
	}

	private Object getMetadataNodeValue(NexusFile nexusFile, String metadataNodePath) {
		try {
			final Node node = nexusFile.getNode(metadataNodePath);
			if (node == null) {
				logger.warn("No such dataset: {}", metadataNodePath);
			} else if (!node.isDataNode()) {
				logger.warn("Not a DataNode: {}", metadataNodePath);
			} else {
				final ILazyDataset dataset = ((DataNode) node).getDataset();
				if (dataset.getRank() == 0) {
					return dataset.getSlice().getObject();
				} else if (dataset.getRank() == 1 && dataset.getSize() == 1) {
					return dataset.getSlice().getObject(1);
				} else {
					logger.warn("Not a single-valued dataset: {}", metadataNodePath);
				}
			}
		} catch (Exception e) {
			logger.warn("Could not load dataset: " + metadataNodePath, e);
		}
		return null;
	}

}
