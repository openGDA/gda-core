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
package org.eclipse.scanning.sequencer.nexus;

import static java.util.stream.Collectors.partitioningBy;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.eclipse.scanning.sequencer.nexus.SolsticeConstants.DEFAULT_BEAMLINE_NAME;
import static org.eclipse.scanning.sequencer.nexus.SolsticeConstants.FIELD_NAME_BEAMLINE;
import static org.eclipse.scanning.sequencer.nexus.SolsticeConstants.FIELD_NAME_END_STATION;
import static org.eclipse.scanning.sequencer.nexus.SolsticeConstants.SYSTEM_PROPERTY_NAME_END_STATION;
import static org.eclipse.scanning.sequencer.nexus.SolsticeConstants.SYSTEM_PROPERTY_NAME_INSTRUMENT;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.eclipse.dawnsci.nexus.INexusDevice;
import org.eclipse.dawnsci.nexus.NXentry;
import org.eclipse.dawnsci.nexus.NXinstrument;
import org.eclipse.dawnsci.nexus.NexusBaseClass;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusScanInfo;
import org.eclipse.dawnsci.nexus.NexusScanInfo.ScanRole;
import org.eclipse.dawnsci.nexus.builder.NexusMetadataProvider;
import org.eclipse.dawnsci.nexus.builder.impl.MapBasedMetadataProvider;
import org.eclipse.dawnsci.nexus.scan.NexusScanFile;
import org.eclipse.dawnsci.nexus.scan.NexusScanModel;
import org.eclipse.scanning.api.INameable;
import org.eclipse.scanning.api.IScannable;
import org.eclipse.scanning.api.device.AbstractRunnableDevice;
import org.eclipse.scanning.api.device.IScanDevice;
import org.eclipse.scanning.api.device.IScannableDeviceService;
import org.eclipse.scanning.api.malcolm.IMalcolmDevice;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.api.scan.models.ScanMetadata;
import org.eclipse.scanning.api.scan.models.ScanMetadata.MetadataType;
import org.eclipse.scanning.api.scan.models.ScanModel;
import org.eclipse.scanning.device.CommonBeamlineDevicesConfiguration;
import org.eclipse.scanning.sequencer.ServiceHolder;
import org.eclipse.scanning.sequencer.SubscanModerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.util.Version;

/**
 * Builds and manages the NeXus file for a scan given a {@link ScanModel}.
 */
public class NexusScanFileManager {

	private static final Logger logger = LoggerFactory.getLogger(NexusScanFileManager.class);

	private static Map<MetadataType, NexusBaseClass> METADATA_TYPE_TO_NEXUS_CLASS_MAP;

	static {
		METADATA_TYPE_TO_NEXUS_CLASS_MAP = new EnumMap<>(MetadataType.class);
		METADATA_TYPE_TO_NEXUS_CLASS_MAP.put(MetadataType.ENTRY, NexusBaseClass.NX_ENTRY);
		METADATA_TYPE_TO_NEXUS_CLASS_MAP.put(MetadataType.INSTRUMENT, NexusBaseClass.NX_INSTRUMENT);
		METADATA_TYPE_TO_NEXUS_CLASS_MAP.put(MetadataType.SAMPLE, NexusBaseClass.NX_SAMPLE);
		METADATA_TYPE_TO_NEXUS_CLASS_MAP.put(MetadataType.USER, NexusBaseClass.NX_USER);
	}

	private final IScannableDeviceService scannableDeviceService;
	private final IScanDevice scanDevice;
	private boolean isWritingNexus;
	private NexusScanFile nexusScanFile = null;
	private SolsticeScanMetadataWriter scanMetadataWriter;

	public NexusScanFileManager(IScanDevice scanDevice) {
		this.scanDevice = scanDevice;
		scannableDeviceService = ((AbstractRunnableDevice<?>) scanDevice).getConnectorService();
	}

	/**
	 * Configures this {@link NexusScanFileManager} with the given {@link ScanModel}. This
	 * determines the structure of the nexus file that will be created when {@link #createNexusFile(boolean)}
	 * is called.
	 * @param scanModel the {@link ScanModel} describing the scan
	 * @throws ScanningException
	 */
	public void configure(ScanModel scanModel) throws ScanningException {
		isWritingNexus = scanModel.getFilePath() != null && ServiceHolder.getNexusScanFileService() != null;

		addGlobalMetadataDevices(scanModel);

		if (isWritingNexus) {
			final NexusScanModel nexusScanModel = createNexusScanModel(scanModel);
			scanMetadataWriter = new SolsticeScanMetadataWriter(scanDevice, scanModel);
			nexusScanModel.setMetadataWriter(scanMetadataWriter);
			try {
				nexusScanFile = ServiceHolder.getNexusScanFileService().newNexusScanFile(nexusScanModel);
			} catch (NexusException e) {
				throw new ScanningException("Error creating nexus file: " + e.getMessage(), e);
			}
		}
	}

	private void addGlobalMetadataDevices(ScanModel scanModel) throws ScanningException {
		final Set<String> newPerScanMonitorNames = new HashSet<>();

		// add legacy per-scan monitor names
		newPerScanMonitorNames.addAll(getLegacyPerScanMonitorNames(scanModel));

		// add CommonBeamlineDevices, if configured
		final CommonBeamlineDevicesConfiguration devicesConfig = ServiceHolder.getCommonBeamlineDevicesConfiguration();
		if (devicesConfig != null) {
			newPerScanMonitorNames.addAll(devicesConfig.getCommonDeviceNames());
		}

		// if there are any names of scannables to add, get the scannables for them,
		// setting them to be per scan monitors
		if (!newPerScanMonitorNames.isEmpty()) {
			final Set<String> allScannableNames = new HashSet<>(scannableDeviceService.getScannableNames());
			final Map<Boolean, List<String>> newPerScanMonitorNamesByIsScannable = newPerScanMonitorNames.stream()
					.collect(partitioningBy(allScannableNames::contains));

			// add new per scan monitors to the scan model's list of per scan monitors
			if (newPerScanMonitorNamesByIsScannable.get(true) != null) {
				final List<IScannable<?>> monitorsPerScan = new ArrayList<>(scanModel.getMonitorsPerScan());
				monitorsPerScan.addAll(newPerScanMonitorNamesByIsScannable.get(true).stream().map(this::getPerScanMonitor).collect(toList()));
				scanModel.setMonitorsPerScan(monitorsPerScan);
			}

			// add new nexus devices to the scan model's list of additional scan objects
			if (newPerScanMonitorNamesByIsScannable.get(false) != null) {
				final List<Object> additionalScanObjects = new ArrayList<>(scanModel.getAdditionalScanObjects());
				additionalScanObjects.addAll(newPerScanMonitorNamesByIsScannable.get(false).stream()
						.map(this::getNexusDevice).filter(Objects::nonNull).collect(toList()));
				scanModel.setAdditionalScanObjects(additionalScanObjects);
			}
		}
	}

	private INexusDevice<?> getNexusDevice(String name) {
		try {
			return ServiceHolder.getNexusDeviceService().getNexusDevice(name);
		} catch (NexusException e) {
			logger.error("No such scannable or nexus device '{}'. It will not be written", name);
			return null;
		}
	}

	/**
	 * Augments the set of per-scan monitors in the model with: <ul>
	 * <li>any metadata scannables (called per-scan monitors in GDA9) from the legacy spring configuration;</li>
	 * <li>the required scannables (as per-scan monitors) point of any scannables in the scan;</li>
	 * </ul>
	 * The scannable services gets these from {@code gda.data.scan.datawriter.NexusDataWriter}
	 * @param scanModel the scan model
	 */
	private Set<String> getLegacyPerScanMonitorNames(ScanModel model) {
		final Set<String> newPerScanMonitorNames = new HashSet<>();

		// get the global metadata scannables, and the required metadata scannables for
		// each scannable in the scan. These are from the legacy GDA8 location map
		final Set<String> legacyPerScanMonitorNames = scannableDeviceService.getGlobalPerScanMonitorNames();
		newPerScanMonitorNames.addAll(legacyPerScanMonitorNames);

		// create a set of the names of the metadata scannables already in the model
		final Set<String> existingScannableNames = new HashSet<>();
		existingScannableNames.addAll(model.getPointGenerator().getNames());
		existingScannableNames.addAll(model.getMonitorsPerScan().stream().map(INameable::getName).collect(toSet()));
		existingScannableNames.addAll(model.getMonitorsPerPoint().stream().map(INameable::getName).collect(toSet()));

		// the set of scannable names to check for dependencies
		Set<String> scannableNamesToCheck = new HashSet<>();
		scannableNamesToCheck.addAll(existingScannableNames);
		scannableNamesToCheck.addAll(legacyPerScanMonitorNames);
		do {
			// check the given set of scannable names for dependencies
			// each iteration checks the scannable names added in the previous one
			final Set<String> requiredScannableNames = scannableNamesToCheck.stream()
					.flatMap(name -> scannableDeviceService.getRequiredPerScanMonitorNames(name).stream())
					.filter(name -> !newPerScanMonitorNames.contains(name))
					.collect(toSet());

			newPerScanMonitorNames.addAll(requiredScannableNames);
			scannableNamesToCheck = requiredScannableNames;
		} while (!scannableNamesToCheck.isEmpty());

		// remove any scannable names in the scan from the list of per scan monitors,
		// as a scannable can only have one role within the scan
		newPerScanMonitorNames.removeAll(existingScannableNames);
		return newPerScanMonitorNames;
	}

	private IScannable<?> getPerScanMonitor(String monitorName) {
		try {
			return ((AbstractRunnableDevice<?>) scanDevice).getConnectorService().getScannable(monitorName);
		} catch (ScanningException e) {
			logger.error("No such scannable ''{}''", monitorName);
			return null;
		}
	}

	private List<NexusMetadataProvider> createScanMetadataProviders(ScanModel scanModel) {
		final List<NexusMetadataProvider> metadataProviders = new ArrayList<>();

		// add an metadata provider for the NXentry group to write 'experiment_identifier' and 'program_name'
		final MapBasedMetadataProvider entryMetadata = new MapBasedMetadataProvider(NexusBaseClass.NX_ENTRY);
		entryMetadata.addMetadataEntry(NXentry.NX_EXPERIMENT_IDENTIFIER, scanModel.getBean().getExperimentId());
		entryMetadata.addMetadataEntry(NXentry.NX_PROGRAM_NAME, "GDA " + Version.getRelease());
		metadataProviders.add(entryMetadata);

		// add a metadata provider for the NXinstrument to add 'name, 'beamline' and 'end_station'
		final MapBasedMetadataProvider instrumentMetadata = new MapBasedMetadataProvider(NexusBaseClass.NX_INSTRUMENT);
		final String beamlineName = System.getProperty(SYSTEM_PROPERTY_NAME_INSTRUMENT, DEFAULT_BEAMLINE_NAME);
		instrumentMetadata.addMetadataEntry(FIELD_NAME_BEAMLINE, beamlineName);
		final String endStationName = System.getProperty(SYSTEM_PROPERTY_NAME_END_STATION);
		if (endStationName != null) instrumentMetadata.addMetadataEntry(FIELD_NAME_END_STATION, endStationName);
		final String instrumentName = endStationName != null ? endStationName : beamlineName;
		instrumentMetadata.addMetadataEntry(NXinstrument.NX_NAME, instrumentName);
		metadataProviders.add(instrumentMetadata);

		// add metadata from the scan model afterwards, so that the can overwrite the built-in ones above
		metadataProviders.addAll(scanModel.getScanMetadata().stream()
				.map(this::toNexusMetadataProvider)
				.collect(toCollection(ArrayList::new)));

		return metadataProviders;
	}

	private NexusMetadataProvider toNexusMetadataProvider(ScanMetadata scanMetadata) {
		final NexusBaseClass nexusBaseClass = METADATA_TYPE_TO_NEXUS_CLASS_MAP.get(scanMetadata.getType());
		final MapBasedMetadataProvider metadataProvider = new MapBasedMetadataProvider(nexusBaseClass);
		scanMetadata.getFields().entrySet().stream().forEach(
				entry -> metadataProvider.addMetadataEntry(entry.getKey(), entry.getValue()));
		return metadataProvider;
	}

	private String getAbsoluteFilePath(String templateFilePath) {
		final Path filePath = Paths.get(templateFilePath);
		if (filePath.isAbsolute()) {
			return templateFilePath;
		}

		final String templateRoot = ServiceHolder.getFilePathService().getPersistenceDir();
		return Paths.get(templateRoot).resolve(templateFilePath).toString();
	}

	private NexusScanModel createNexusScanModel(ScanModel scanModel) throws ScanningException {
		final Map<ScanRole, List<INexusDevice<?>>> nexusDevices = extractNexusDevices(scanModel);

		final NexusScanModel nexusScanModel = new NexusScanModel(nexusDevices);
		nexusScanModel.setEntryName(getEntryName());
		nexusScanModel.setFilePath(scanModel.getFilePath());
		nexusScanModel.setTemplateFilePaths(scanModel.getTemplateFilePaths().stream().map(this::getAbsoluteFilePath).collect(toSet()));
		nexusScanModel.setNexusScanInfo(createScanInfo(scanModel));
		nexusScanModel.setDimensionNamesByIndex(scanModel.getPointGenerator().getDimensionNames());
		nexusScanModel.setNexusMetadataProviders(createScanMetadataProviders(scanModel));

		return nexusScanModel;
	}

	private NexusScanInfo createScanInfo(ScanModel scanModel) throws ScanningException {
		final NexusScanInfo nexusScanInfo = new NexusScanInfo();
		nexusScanInfo.setScannableNames(scanModel.getPointGenerator().getNames());
		nexusScanInfo.setOverallShape(scanModel.getScanInformation().getShape());
		nexusScanInfo.setOuterShape(getOuterScanShape(scanModel));
		nexusScanInfo.setDetectorNames(getDeviceNames(scanModel.getDetectors()));
        nexusScanInfo.setPerPointMonitorNames(getDeviceNames(scanModel.getMonitorsPerPoint()));
		nexusScanInfo.setPerScanMonitorNames(getDeviceNames(scanModel.getMonitorsPerScan()));
		nexusScanInfo.setFilePath(scanModel.getFilePath());
		nexusScanInfo.setEstimatedScanTime(scanModel.getScanInformation().getEstimatedScanTime());

		return nexusScanInfo;
	}

	public static String getEntryName() {
		String entryName = System.getProperty(SolsticeConstants.SYSTEM_PROPERTY_NAME_ENTRY_NAME);
		if (entryName == null) {
			entryName = System.getProperty("GDA/gda.nexus.entryName");
		}
		return entryName; // will be null if neither property set, uses default entry name
	}

	private Set<String> getDeviceNames(Collection<? extends INameable> devices) {
		return devices.stream().map(INameable::getName).collect(toSet());
	}

	protected int[] getOuterScanShape(ScanModel scanModel) throws ScanningException {
		if (isMalcolmScan(scanModel)) {
			SubscanModerator moderator = new SubscanModerator(scanModel);
			return moderator.getOuterPointGenerator().getShape();
		}

		// we have a method for this as it needs to be overriden by MalcolmNexusScanFileManager
		return scanModel.getScanInformation().getShape();
	}

	private Optional<IMalcolmDevice> getMalcolmDevice(ScanModel scanModel) {
		return scanModel.getDetectors().stream()
				.filter(IMalcolmDevice.class::isInstance)
				.map(IMalcolmDevice.class::cast)
				.findFirst();
	}

	private boolean isMalcolmScan(ScanModel scanModel) {
		return getMalcolmDevice(scanModel).isPresent();
	}

	protected Map<ScanRole, List<INexusDevice<?>>> extractNexusDevices(ScanModel model) {
		final Map<ScanRole, List<INexusDevice<?>>> nexusDevices = new EnumMap<>(ScanRole.class);
		nexusDevices.put(ScanRole.DETECTOR, extractNexusDevices(model.getDetectors()));
		nexusDevices.put(ScanRole.SCANNABLE, extractNexusDevices(model.getScannables()));
		nexusDevices.put(ScanRole.MONITOR_PER_POINT, extractNexusDevices(model.getMonitorsPerPoint()));
		nexusDevices.put(ScanRole.MONITOR_PER_SCAN, extractNexusDevices(model.getMonitorsPerScan()));
		nexusDevices.put(ScanRole.NONE, extractNexusDevices(model.getAdditionalScanObjects()));

		return nexusDevices;
	}

	private List<INexusDevice<?>> extractNexusDevices(Collection<?> devices) {
		return devices.stream()
				.filter(INexusDevice.class::isInstance)
				.map(INexusDevice.class::cast).collect(toList());
	}

	/**
	 * Creates the nexus file.
	 * The structure of the nexus file is determined by the devices contained within the
	 * {@link ScanModel} that {@link #configure(ScanModel)} was previously called with.
	 *
	 * @throws ScanningException if the nexus file could not be created for any reason
	 */
	public String createNexusFile(boolean async) throws ScanningException {
		if (!isWritingNexus) return null;

		try {
			nexusScanFile.createNexusFile(async);
		} catch (NexusException e) {
			throw new ScanningException("Could not create nexus file", e);
		}
		return nexusScanFile.getFilePath();
	}

	/**
	 * Flushes any pending data into the nexus file.
	 * @throws ScanningException
	 */
	public void flushNexusFile() throws ScanningException {
		if (!isWritingNexus) return;

		try {
			int code = nexusScanFile.flush();
			if (code < 0) {
				logger.warn("Problem flushing nexus file, error code = {}", code);
			}
		} catch (NexusException e) {
			throw new ScanningException("Could not flush nexus file", e);
		}
	}

	/**
	 * Informs the manager that the scan has finished. This will
	 * cause the scanFinished dataset to be updated and the nexus file to be closed.
	 * @throws ScanningException
	 */
	public void scanFinished() throws ScanningException {
		if (!isWritingNexus) return;

		try {
			scanMetadataWriter.scanFinished();
		} catch (NexusException e) {
			throw new ScanningException(e);
		}

		try {
			nexusScanFile.scanFinished(); // writes final timestamps into NXentry and closes file
		} catch (NexusException e) {
			throw new ScanningException("Could not close nexus file", e);
		}
	}

	/**
	 * Returns whether this nexus file manager actually writes a nexus files.
	 * @return <code>true</code> if a nexus file is written, <code>false</code> otherwise
	 */
	public boolean isNexusWritingEnabled() {
		return isWritingNexus;
	}

	/**
	 * The file paths of all the external files written to during this scan. For example, this
	 * may be used to trigger archiving.
	 * @return file paths of external files written to during this scan
	 */
	public Set<String> getExternalFilePaths() {
		if (!isWritingNexus) return null;

		return nexusScanFile.getExternalFilePaths();
	}


}
