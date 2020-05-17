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

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.dawnsci.nexus.IMultipleNexusDevice;
import org.eclipse.dawnsci.nexus.INexusDevice;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusScanInfo;
import org.eclipse.dawnsci.nexus.NexusScanInfo.ScanRole;
import org.eclipse.dawnsci.nexus.builder.NexusScanFile;
import org.eclipse.scanning.api.INameable;
import org.eclipse.scanning.api.IScannable;
import org.eclipse.scanning.api.device.AbstractRunnableDevice;
import org.eclipse.scanning.api.device.IScanDevice;
import org.eclipse.scanning.api.device.IScannableDeviceService;
import org.eclipse.scanning.api.points.AbstractPosition;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.api.scan.models.ScanModel;
import org.eclipse.scanning.sequencer.ServiceHolder;
import org.eclipse.scanning.sequencer.SubscanModerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Builds and manages the NeXus file for a scan given a {@link ScanModel}.
 */
public class NexusScanFileManager {

	private static final Logger logger = LoggerFactory.getLogger(NexusScanFileManager.class);

	private final IScanDevice scanDevice;
	private boolean isWritingNexus;
	private NexusScanFileBuilder nexusScanFileBuilder = null;
	private NexusScanFile nexusScanFile;

	public NexusScanFileManager(IScanDevice scanDevice) {
		this.scanDevice = scanDevice;
	}

	/**
	 * Configures this {@link NexusScanFileManager} with the given {@link ScanModel}. This
	 * determines the structure of the nexus file that will be created when {@link #createNexusFile(boolean)}
	 * is called.
	 * @param scanModel the {@link ScanModel} describing the scan
	 * @throws ScanningException
	 */
	public void configure(ScanModel scanModel) throws ScanningException {
		isWritingNexus = scanModel.getFilePath() != null && ServiceHolder.getFactory() != null;

		addLegacyPerScanMonitors(scanModel);
		if (isWritingNexus) {
			final NexusScanModel nexusScanModel = createNexusScanModel(scanModel);
			nexusScanFileBuilder = new NexusScanFileBuilder(scanDevice, scanModel, nexusScanModel);
		}
	}

	/**
	 * Augments the set of per-scan monitors in the model with: <ul>
	 * <li>any metadata scannables (called per-scan monitors in GDA9) from the legacy spring configuration;</li>
	 * <li>the required scannables (as per-scan monitors) point of any scannables in the scan;</li>
	 * </ul>
	 * The scannable services gets these from {@code gda.data.scan.datawriter.NexusDataWriter}
	 * @param model
	 */
	private void addLegacyPerScanMonitors(ScanModel model) {
		final IScannableDeviceService scannableDeviceService = ((AbstractRunnableDevice<?>) scanDevice).getConnectorService();

		// build up the set of all metadata scannables
		final List<String> scannableNames = model.getPointGenerator().getNames();
		final Set<String> perScanMonitorNames = new HashSet<>();

		// add the names of the metadata scannables already in the model
		final Set<String> existingPerScanMonitorNames = model.getMonitorsPerScan().stream()
				.map(m -> m.getName()).collect(Collectors.toSet());
		perScanMonitorNames.addAll(existingPerScanMonitorNames);

		// add the global metadata scannables, and the required metadata scannables for
		// each scannable in the scan. These are from the legacy GDA8 location map
		perScanMonitorNames.addAll(scannableDeviceService.getGlobalPerScanMonitorNames());

		// the set of scannable names to check for dependencies
		Set<String> scannableNamesToCheck = new HashSet<>();
		scannableNamesToCheck.addAll(perScanMonitorNames);
		scannableNamesToCheck.addAll(scannableNames);
		do {
			// check the given set of scannable names for dependencies
			// each iteration checks the scannable names added in the previous one
			Set<String> requiredScannables = scannableNamesToCheck.stream()
					.flatMap(name -> scannableDeviceService.getRequiredPerScanMonitorNames(name).stream())
					.filter(name -> !perScanMonitorNames.contains(name))
					.collect(Collectors.toSet());

			perScanMonitorNames.addAll(requiredScannables);
			scannableNamesToCheck = requiredScannables;
		} while (!scannableNamesToCheck.isEmpty());

		// remove any scannable names in the scan from the list of per scan monitors,
		// as a scannable can only have one role within the scan
		perScanMonitorNames.removeAll(scannableNames);
		Set<String> scannablesToAdd = new HashSet<>(perScanMonitorNames.stream()
				.filter(name -> !existingPerScanMonitorNames.contains(name))
				.collect(Collectors.toSet()));

		// if there are any names of scannables to add, get the scannables for them,
		// setting them to be per scan monitors
		if (!scannablesToAdd.isEmpty()) {
			final List<IScannable<?>> monitorsPerScan = new ArrayList<>(model.getMonitorsPerScan());
			monitorsPerScan.addAll(perScanMonitorNames.stream().map(this::getPerScanMonitor).collect(toList()));
			model.setMonitorsPerScan(monitorsPerScan);
		}
	}

	private IScannable<?> getPerScanMonitor(String monitorName) {
		try {
			return ((AbstractRunnableDevice<?>) scanDevice).getConnectorService().getScannable(monitorName);
		} catch (ScanningException e) {
			logger.error("No such scannable ''{}''", monitorName);
			return null;
		}
	}

	private NexusScanModel createNexusScanModel(ScanModel scanModel) throws ScanningException {
		final Map<ScanRole, List<INexusDevice<?>>> nexusDevices = extractNexusDevices(scanModel);
		final Optional<IMultipleNexusDevice> multiNexusDevice = scanModel.getDetectors().stream()
				.filter(IMultipleNexusDevice.class::isInstance)
				.map(IMultipleNexusDevice.class::cast).findFirst();

		final NexusScanModel nexusScanModel = new NexusScanModel(nexusDevices);
		nexusScanModel.setFilePath(scanModel.getFilePath());
		nexusScanModel.setMultipleNexusDevice(multiNexusDevice);
		nexusScanModel.setTemplateFilePaths(scanModel.getTemplateFilePaths());
		nexusScanModel.setNexusScanInfo(createScanInfo(scanModel));
		final AbstractPosition firstPosition = (AbstractPosition) scanModel.getPointGenerator().getFirstPoint();
		nexusScanModel.setDimensionNamesByIndex(firstPosition.getDimensionNames());
		nexusScanModel.setScanMetadata(scanModel.getScanMetadata());

		return nexusScanModel;
	}

	private NexusScanInfo createScanInfo(ScanModel scanModel) throws ScanningException {
		final NexusScanInfo nexusScanInfo = new NexusScanInfo();
		nexusScanInfo.setScannableNames(scanModel.getPointGenerator().getNames());
		nexusScanInfo.setRank(getOuterScanRank(scanModel));
		nexusScanInfo.setShape(scanModel.getScanInformation().getShape());
		nexusScanInfo.setDetectorNames(getDeviceNames(scanModel.getDetectors()));
        nexusScanInfo.setPerPointMonitorNames(getDeviceNames(scanModel.getMonitorsPerPoint()));
		nexusScanInfo.setPerScanMonitorNames(getDeviceNames(scanModel.getMonitorsPerScan()));
		nexusScanInfo.setFilePath(scanModel.getFilePath());

		return nexusScanInfo;
	}

	private Set<String> getDeviceNames(Collection<? extends INameable> devices) {
		return devices.stream().map(INameable::getName).collect(toSet());
	}

	protected int getOuterScanRank(ScanModel scanModel) throws ScanningException {
		if (isMalcolmScan(scanModel)) {
			SubscanModerator moderator = new SubscanModerator(scanModel);
			return moderator.getOuterPointGenerator().getRank();
		}

		// we have a method for this as it needs to be overriden by MalcolmNexusScanFileManager
		return scanModel.getScanInformation().getRank();
	}

	private boolean isMalcolmScan(ScanModel scanModel) {
		return scanModel.getDetectors().stream().anyMatch(det -> (det instanceof IMultipleNexusDevice));
	}

	protected Map<ScanRole, List<INexusDevice<?>>> extractNexusDevices(ScanModel model) {
		final Function<Collection<?>, List<INexusDevice<?>>> toNexusDevices =
				devices -> devices.stream()
				.filter(INexusDevice.class::isInstance)
				.map(INexusDevice.class::cast).collect(toList());

		final Map<ScanRole, List<INexusDevice<?>>> nexusDevices = new EnumMap<>(ScanRole.class);
		nexusDevices.put(ScanRole.DETECTOR, toNexusDevices.apply(model.getDetectors()));
		nexusDevices.put(ScanRole.SCANNABLE, toNexusDevices.apply(model.getScannables()));
		nexusDevices.put(ScanRole.MONITOR_PER_POINT, toNexusDevices.apply(model.getMonitorsPerPoint()));
		nexusDevices.put(ScanRole.MONITOR_PER_SCAN, toNexusDevices.apply(model.getMonitorsPerScan()));
		nexusDevices.put(ScanRole.NONE, toNexusDevices.apply(model.getAnnotationParticipants()));

		return nexusDevices;
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

		nexusScanFile = nexusScanFileBuilder.createNexusFile(async);
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

		nexusScanFileBuilder.scanFinished(); // need to write some final data into the file
		try {
			nexusScanFile.close();
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

		return nexusScanFileBuilder.getExternalFilePaths();
	}


}
