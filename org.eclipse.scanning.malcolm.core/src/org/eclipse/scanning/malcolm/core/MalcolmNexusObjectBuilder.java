/*-
 *******************************************************************************
 * Copyright (c) 2011, 2016 Diamond Light Source Ltd.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthew Gerring - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.scanning.malcolm.core;

import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static org.eclipse.scanning.api.malcolm.MalcolmConstants.DATASETS_TABLE_COLUMN_FILENAME;
import static org.eclipse.scanning.api.malcolm.MalcolmConstants.DATASETS_TABLE_COLUMN_NAME;
import static org.eclipse.scanning.api.malcolm.MalcolmConstants.DATASETS_TABLE_COLUMN_PATH;
import static org.eclipse.scanning.api.malcolm.MalcolmConstants.DATASETS_TABLE_COLUMN_RANK;
import static org.eclipse.scanning.api.malcolm.MalcolmConstants.DATASETS_TABLE_COLUMN_TYPE;
import static org.eclipse.scanning.api.malcolm.MalcolmConstants.DATASETS_TABLE_COLUMN_UNIQUEID;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.nexus.NXdetector;
import org.eclipse.dawnsci.nexus.NXmonitor;
import org.eclipse.dawnsci.nexus.NXobject;
import org.eclipse.dawnsci.nexus.NexusBaseClass;
import org.eclipse.dawnsci.nexus.NexusNodeFactory;
import org.eclipse.dawnsci.nexus.NexusScanInfo;
import org.eclipse.dawnsci.nexus.builder.NexusObjectProvider;
import org.eclipse.dawnsci.nexus.builder.NexusObjectWrapper;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.IntegerDataset;
import org.eclipse.january.dataset.PositionIterator;
import org.eclipse.scanning.api.malcolm.IMalcolmDevice;
import org.eclipse.scanning.api.malcolm.MalcolmDetectorInfo;
import org.eclipse.scanning.api.malcolm.MalcolmDeviceException;
import org.eclipse.scanning.api.malcolm.MalcolmTable;
import org.eclipse.scanning.api.malcolm.attributes.MalcolmDatasetType;
import org.eclipse.scanning.api.points.models.InterpolatedMultiScanModel;
import org.eclipse.scanning.api.points.models.InterpolatedMultiScanModel.ImageType;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.api.scan.models.ScanModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A helper class that knows how to build the NeXus objects and the {@link NexusObjectProvider}s
 * that wrap and describe them for an {@link IMalcolmDevice}. Each instance of this object
 * should only be used once.
 *
 * @author Matthew Dickie
 */
class MalcolmNexusObjectBuilder {

	private static final class DatasetInfo {
		private final String deviceName;
		private final String datasetName;
		private final String fileName;
		private final String path;
		private final int rank;
		private final MalcolmDatasetType type;
		private final String uniqueIdPath;

		private DatasetInfo(String deviceName, String datasetName, String fileName, String path, int rank,
				MalcolmDatasetType type, String uniqueIdPath) {
			this.deviceName = deviceName;
			this.datasetName = datasetName;
			this.fileName = fileName;
			this.path = path;
			this.rank = rank;
			this.type = type;
			this.uniqueIdPath = uniqueIdPath;
		}

		@Override
		public String toString() {
			return "deviceName=" + deviceName + ", datasetName=" + datasetName + ", fileName=" + fileName
					+ ", path=" + path + ", rank=" + rank + ", type=" + type + ", uniqueIdPath=" + uniqueIdPath + "]";
		}

		protected static DatasetInfo fromMap(Map<String, Object> datasetRow) {
			final String fullName = (String) datasetRow.get(DATASETS_TABLE_COLUMN_NAME);
			final String[] nameSegments = fullName.split("\\.");
			final String deviceName = nameSegments[0];
			final String datasetName = nameSegments[1];

			final String fileName = (String) datasetRow.get(DATASETS_TABLE_COLUMN_FILENAME);
			final String path = (String) datasetRow.get(DATASETS_TABLE_COLUMN_PATH);
			final int rank = ((Integer) datasetRow.get(DATASETS_TABLE_COLUMN_RANK)).intValue();
			final MalcolmDatasetType type = getDatasetType(datasetRow);
			final String uniqueIdPath = (String) datasetRow.get(DATASETS_TABLE_COLUMN_UNIQUEID);

			return new DatasetInfo(deviceName, datasetName, fileName, path, rank, type, uniqueIdPath);
		}

		private static MalcolmDatasetType getDatasetType(Map<String, Object> datasetRow) {
			final String datasetTypeStr = (String) datasetRow.get(DATASETS_TABLE_COLUMN_TYPE);
			final MalcolmDatasetType datasetType = MalcolmDatasetType.fromString(datasetTypeStr);
			if (datasetType == MalcolmDatasetType.UNKNOWN) { // a warning only allows new datatypes to be added
				logger.warn("Unknown malcolm dataset type '{}'", datasetTypeStr);
			}
			return datasetType;
		}

	}

	private static final Logger logger = LoggerFactory.getLogger(MalcolmNexusObjectBuilder.class);

	private static final String PROPERTY_NAME_UNIQUE_KEYS = "uniqueKeys";

	private static final String FIELD_NAME_IMAGE_KEY = "image_key";

	private final AbstractMalcolmDevice malcolmDevice;

	private final Optional<InterpolatedMultiScanModel> tomoModel;

	// The name (last segment only) of the malcolm output dir. This is used to create the relative path, as the main
	// scan file is in the parent directory of the malcolm output dir.
	private final String malcolmOutputDirName;

	private final Set<String> malcolmAxes;

	private final Map<String, NexusObjectWrapper<NXobject>> nexusWrappers;

	private Map<String, Double> detectorExposureTimes = null;

	MalcolmNexusObjectBuilder(AbstractMalcolmDevice malcolmDevice) throws ScanningException {
		this.malcolmDevice = malcolmDevice;
		nexusWrappers = new HashMap<>();
		malcolmOutputDirName = new File(malcolmDevice.getOutputDir()).getName();
		tomoModel = malcolmDevice.getMultiScanModel();
		malcolmAxes = getMalcolmAxes();
	}

	/**
	 * Build the nexus objects for a malcolm device according to the value of the
	 * "datasets" attribute.
	 * @param scanInfo
	 * @return nexus object
	 * @throws ScanningException
	 */
	public List<NexusObjectProvider<?>> buildNexusObjects(@SuppressWarnings("unused") NexusScanInfo scanInfo) throws ScanningException {
		logger.debug("Creating nexus objects from datasets table for malcolm device {}", malcolmDevice.getName());
		final MalcolmTable datasetsTable = malcolmDevice.getDatasets();

		if (datasetsTable.isEmpty()) {
			throw new ScanningException("No datasets found for malcolm device: " + malcolmDevice.getName());
		}

		// process the datasets, creating nexus wrappers for each device
		datasetsTable.stream().map(DatasetInfo::fromMap).filter(this::canProcess).forEach(this::addDataset);

		return new ArrayList<>(nexusWrappers.values());
	}

	private void addDataset(DatasetInfo dataset) {
		// get the nexus object and its wrapper, creating it if necessary
		final NexusObjectWrapper<NXobject> nexusWrapper = getNexusProvider(dataset);

		logger.debug("Adding dataset: {} to {} {}", dataset, nexusWrapper.getNexusBaseClass(), dataset.deviceName);
		final NXobject nexusObject = nexusWrapper.getNexusObject();

		// create the external link to the hdf5 file written by the malcolm device
		final String externalFilePath = malcolmOutputDirName + "/" + dataset.fileName; // path relative to parent dir of scan file
		nexusWrapper.addExternalLink(nexusObject, dataset.datasetName, externalFilePath,
				dataset.path, dataset.rank);

		if (dataset.uniqueIdPath != null && !dataset.uniqueIdPath.isEmpty()) {
			nexusWrapper.setPropertyValue(PROPERTY_NAME_UNIQUE_KEYS, dataset.uniqueIdPath);
		}
		if (dataset.type == MalcolmDatasetType.PRIMARY && tomoModel.isPresent()) {
			writeImageKey((NXdetector) nexusObject, tomoModel.get());
		}

		// configure the nexus wrapper for the dataset
		configureNexusWrapperForDataset(dataset.type, dataset.datasetName, nexusWrapper);
	}

	/**
	 * Configure the nexus wrapper to describe the wrapped nexus object appropriately.
	 * @param datasetType
	 * @param datasetName
	 * @param nexusWrapper
	 */
	private void configureNexusWrapperForDataset(final MalcolmDatasetType datasetType,
			final String datasetName, final NexusObjectWrapper<NXobject> nexusWrapper) {
		switch (datasetType) {
			case PRIMARY:
				nexusWrapper.setPrimaryDataFieldName(datasetName);
				break;
			case SECONDARY:
				nexusWrapper.addAdditionalPrimaryDataFieldName(datasetName);
				break;
			case MONITOR:
				nexusWrapper.addAxisDataFieldName(datasetName);
				if (nexusWrapper.getPrimaryDataFieldName() == null) {
					nexusWrapper.setPrimaryDataFieldName(datasetName);
				}
				break;
			case POSITION_VALUE:
				nexusWrapper.addAxisDataFieldName(datasetName);
				nexusWrapper.setPrimaryDataFieldName(datasetName);
				break;
			case POSITION_SET:
				nexusWrapper.addAxisDataFieldName(datasetName);
				nexusWrapper.setDefaultAxisDataFieldName(datasetName);
				break;
			case POSITION_MIN:
			case POSITION_MAX:
				break; // do nothing, fields already added to Nexus object & not axis or primary/secondary
			case UNKNOWN:
				break; // do nothing (warning already logged)
		}
	}

	private boolean canProcess(DatasetInfo dataset) {
		if (nexusWrappers.containsKey(dataset.deviceName)) {
			return true; // we've already processed another dataset for this device type
		}

		// malcolm creates position_set datasets for all axes in the point generator. Ignore those for axes that
		// malcolm doesn't control, otherwise there would be a clash with the nexus object for the GDA scannable.
		if (dataset.type == MalcolmDatasetType.POSITION_SET && !malcolmAxes.contains(dataset.deviceName)) {
			logger.debug("Skipping dataset {}.{} as it is for an axis not controlled by malcolm", dataset.deviceName, dataset.datasetName);
			return false;
		}

		// otherwise, see if we know how to write this kind of dataset
		final NexusBaseClass nexusBaseClass = dataset.type.getNexusBaseClass();
		if (nexusBaseClass == null) {
			logger.warn("Unknown malcolm dataset type: {}", dataset.type);
			return false;
		}

		return true;
	}

	private Set<String> getMalcolmAxes() throws ScanningException {
		// the malcolm model may have been explicitly configured with axes to move
		if (malcolmDevice.getModel().getAxesToMove() != null) {
			return new HashSet<>(malcolmDevice.getModel().getAxesToMove());
		}

		// otherwise malcolm controls the scan axes that are in its list of available axes
		final List<String> allMalcolmAxes = malcolmDevice.getAvailableAxes();
		return malcolmDevice.getPointGenerator().getNames().stream()
					.filter(allMalcolmAxes::contains)
					.collect(toSet());
	}

	private NexusObjectWrapper<NXobject> getNexusProvider(DatasetInfo dataset) {
		if (nexusWrappers.containsKey(dataset.deviceName)) {
			return nexusWrappers.get(dataset.deviceName);
		}

		final NexusBaseClass nexusBaseClass = dataset.type.getNexusBaseClass();
		final NXobject nexusObject = NexusNodeFactory.createNXobjectForClass(nexusBaseClass);
		if (nexusBaseClass == NexusBaseClass.NX_DETECTOR) {
			((NXdetector) nexusObject).setCount_timeScalar(getDetectorExposureTime(dataset.deviceName));
		} else if (nexusBaseClass == NexusBaseClass.NX_MONITOR) {
			((NXmonitor) nexusObject).setCount_timeScalar(malcolmDevice.getModel().getExposureTime());
		}

		final NexusObjectWrapper<NXobject> nexusWrapper = new NexusObjectWrapper<>(dataset.deviceName, nexusObject);
		nexusWrapper.setScanRole(dataset.type.getScanRole().toNexusScanRole());
		nexusWrappers.put(dataset.deviceName, nexusWrapper);

		return nexusWrapper;
	}

	private double getDetectorExposureTime(String detectorName) {
		if (detectorExposureTimes == null) {
			detectorExposureTimes = createExposureTimesMap();
		}

		return detectorExposureTimes.getOrDefault(detectorName, malcolmDevice.getModel().getExposureTime());
	}

	private Map<String, Double> createExposureTimesMap() {
		try {
			final List<MalcolmDetectorInfo> detInfos = malcolmDevice.getDetectorInfos();
			if (detInfos != null) {
				return detInfos.stream().collect(
						toMap(MalcolmDetectorInfo::getName, MalcolmDetectorInfo::getExposureTime));
			}
		} catch (MalcolmDeviceException e) {
			logger.error("Could not get detector information from the malcolm device", e);
		}
		return emptyMap();
	}

	private void writeImageKey(NXdetector detector, InterpolatedMultiScanModel tomoModel) {
		final ScanModel scanModel = malcolmDevice.getConfiguredScan();
		final int[] shape = scanModel.getScanInformation().getShape();

		final DataNode imageKeyDataNode = NexusNodeFactory.createDataNode();
		final IntegerDataset imageKeyDataset = DatasetFactory.zeros(IntegerDataset.class, shape);
		imageKeyDataNode.setDataset(imageKeyDataset);
		detector.addDataNode(FIELD_NAME_IMAGE_KEY, imageKeyDataNode);

		final int[] breakpoints = malcolmDevice.getBreakpoints();
		final List<ImageType> imageTypes = tomoModel.getImageTypes();
		if (breakpoints.length != imageTypes.size()) {
			throw new IllegalArgumentException(String.format("Num of imageTypes was %d, but num of breakpoints was %d",
					imageTypes.size(), breakpoints.length));
		}

		final PositionIterator posIter = new PositionIterator(shape);
		for (int scanIndex = 0; scanIndex < breakpoints.length; scanIndex++) {
			final int imageKey = imageTypes.get(scanIndex).getImageKey();
			for (int pointIndex = 0; pointIndex < breakpoints[scanIndex]; pointIndex++) {
				if (!posIter.hasNext()) throw new IllegalStateException("No points left!"); // posIter.hasNext actually increments the position!
				imageKeyDataset.setItem(imageKey, posIter.getPos());
			}
		}
	}

}
