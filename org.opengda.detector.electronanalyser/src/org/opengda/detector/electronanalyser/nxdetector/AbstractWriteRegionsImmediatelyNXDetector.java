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

package org.opengda.detector.electronanalyser.nxdetector;

import java.io.FileNotFoundException;
import java.util.List;

import org.eclipse.dawnsci.nexus.IWritableNexusDevice;
import org.eclipse.dawnsci.nexus.NXdetector;
import org.eclipse.dawnsci.nexus.NXobject;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusNodeFactory;
import org.eclipse.dawnsci.nexus.NexusScanInfo;
import org.eclipse.dawnsci.nexus.builder.NexusObjectProvider;
import org.eclipse.dawnsci.nexus.builder.NexusObjectWrapper;
import org.opengda.detector.electronanalyser.event.ScanStartEvent;
import org.opengda.detector.electronanalyser.utils.NXdetectorAndSliceIteratorStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;
import gda.data.fileregistrar.ClientFileAnnouncer;
import gda.data.scan.datawriter.NexusScanDataWriter;
import gda.device.DeviceException;
import gda.device.detector.NXDetector;
import gda.device.detector.nxdetector.NXCollectionStrategyPlugin;
import gda.jython.ICurrentScanInformationHolder;
import gda.jython.InterfaceProvider;
import gda.scan.ScanInformation;

/*
 * Abstract detector that allows for writing electron analyser region data immediately. Only compatible
 * with property {@link NexusScanDataWriter#PROPERTY_NAME_CREATE_FILE_AT_SCAN_START} set to true. All
 * other detectors that take part in a scan with this property must also be compatible. It is only
 * compatible with collection strategy {@code AbstractWriteRegionsImmediatelyCollectionStrategy}. Takes a file
 * as input that defines a list of regions to pass onto collection strategy. This then iterates through each region
 * and saves the data immediately when done before each {@link IScanDataPoint} finishes.
 *
 * @author Oli Wenman
 *
 */
public abstract class AbstractWriteRegionsImmediatelyNXDetector extends NXDetector implements IWritableNexusDevice<NXdetector> {

	private static final Logger logger = LoggerFactory.getLogger(AbstractWriteRegionsImmediatelyNXDetector.class);
	private static final long serialVersionUID = -222459754772057676L;

	private transient AbstractWriteRegionsImmediatelyCollectionStrategy<?> collectionStrategy;
	private transient ClientFileAnnouncer clientFileAnnouncer;
	private String description;
	private String detectorId;

	private boolean cachedCreateFileAtScanStart;

	private transient NXdetectorAndSliceIteratorStorage dataStorage = new NXdetectorAndSliceIteratorStorage();

	@Override
	public void setCollectionStrategy(NXCollectionStrategyPlugin nxCollectionStrategyPlugin) {
		if (!(nxCollectionStrategyPlugin instanceof AbstractWriteRegionsImmediatelyCollectionStrategy)) {
			throw new IllegalArgumentException("Invalid collection strategy used. Only " + AbstractWriteRegionsImmediatelyCollectionStrategy.class + " is compatible.");
		}
		super.setCollectionStrategy(nxCollectionStrategyPlugin);
		collectionStrategy = (AbstractWriteRegionsImmediatelyCollectionStrategy<?>) nxCollectionStrategyPlugin;
	}

	public abstract void setSequenceFile(String sequenceFilename) throws DeviceException, FileNotFoundException;

	protected abstract NexusObjectWrapper<NXdetector> initialiseAdditionalNXdetectorData(final NXdetector detector, final NexusScanInfo info);

	@Override
	public List<NexusObjectProvider<? extends NXobject>> getNexusProviders(NexusScanInfo info) throws NexusException {
		final List<NexusObjectProvider<? extends NXobject>> nexusProviders = collectionStrategy.getNexusProviders(info);
		final NexusObjectWrapper<NXdetector> additionalNexusWrapper = initialiseAdditionalNXdetectorData(NexusNodeFactory.createNXdetector(), info);
		if (additionalNexusWrapper != null) {
			setupAdditionalDataAxisFields(additionalNexusWrapper, info.getOverallRank());
			nexusProviders.add(additionalNexusWrapper);
			//Save in map so data can be written to later
			dataStorage.getDetectorMap().put(additionalNexusWrapper.getName(), additionalNexusWrapper.getNexusObject());
		}
		return nexusProviders;
	}

	/**
	 * Setup the axis fields for the datasets that will be added to NXData groups. Come under <dataset_name>_indicies.
	 * Important so that datasets correctly plot and won't crop an axis.
	 * @param nexusWrapper to add the axis fields too
	 * @param scanRank information about the scan dimensions
	 */
	protected abstract void setupAdditionalDataAxisFields(final NexusObjectWrapper<?> nexusWrapper, final int scanRank);

	@Override
	public void atScanStart() throws DeviceException {
		super.atScanStart();

		cachedCreateFileAtScanStart = LocalProperties.check(NexusScanDataWriter.PROPERTY_NAME_CREATE_FILE_AT_SCAN_START, false);
		if(!cachedCreateFileAtScanStart) {
			logger.info("Property {} is false. Switching to true for this scan as detector {} must use it.", NexusScanDataWriter.PROPERTY_NAME_CREATE_FILE_AT_SCAN_START, getName());
		}
		LocalProperties.set(NexusScanDataWriter.PROPERTY_NAME_CREATE_FILE_AT_SCAN_START, true);

		//Refresh the data folder to make file appear at start of scan in GUI as it can be viewed while being written to
		final ICurrentScanInformationHolder scanInfoHolder = InterfaceProvider.getCurrentScanInformationHolder();
		if (scanInfoHolder == null) return;
		final ScanInformation scanInfo = scanInfoHolder.getCurrentScanInformation();
		if(scanInfo == null) {
			logger.warn("scanInfo is null, can't peform atScanStart() setup.");
			return;
		}
		final int scanNumber = scanInfo.getScanNumber();
		final String fileName = scanInfo.getFilename();
		final int totalNumberOfPoints = scanInfo.getNumberOfPoints();
		collectionStrategy.updateScriptController(new ScanStartEvent(scanNumber, totalNumberOfPoints, fileName));
		if (clientFileAnnouncer == null) {
			logger.info("Unable to refresh project explorer as clientFileAnnouncer is null.");
		}
		else {
			clientFileAnnouncer.notifyFilesAvailable(new String[] {fileName});
			logger.info("Refreshed project explorer so scan file {} is visible", fileName);
		}
	}

	protected abstract void beforeCollectData() throws DeviceException;

	@Override
	public void collectData() throws DeviceException{
		beforeCollectData();
		super.collectData();
	}

	@Override
	public void scanEnd() {
		if(!cachedCreateFileAtScanStart) {
			logger.info("Switching property {} back to false.", NexusScanDataWriter.PROPERTY_NAME_CREATE_FILE_AT_SCAN_START);
			LocalProperties.set(NexusScanDataWriter.PROPERTY_NAME_CREATE_FILE_AT_SCAN_START, cachedCreateFileAtScanStart);
		}
		collectionStrategy.atScanEnd();
	}

	public void setClientFileAnnouncer(ClientFileAnnouncer clientFileAnnouncer) {
		this.clientFileAnnouncer = clientFileAnnouncer;
	}

	protected NXdetectorAndSliceIteratorStorage getDataStorage() {
		return dataStorage;
	}

	protected void setDataStorage(NXdetectorAndSliceIteratorStorage dataStorage) {
		this.dataStorage = dataStorage;
	}

	@Override
	//Override to force class implementing this to define it.
	public abstract double getCollectionTime() throws DeviceException;

	@Override
	public void setCollectionTime(double value) throws DeviceException {
		throw new DeviceException(getName() + ".setCollectionTime(" +  value + ") is not supported. It is calculated dynamically based on sequence file.");
	}

	@Override
	public String getDetectorType() throws DeviceException {
		return "Electron Analyser";
	}

	@Override
	public String getDescription() throws DeviceException {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public String getDetectorID() throws DeviceException {
		return detectorId;
	}

	public void setDetectorID(String detectorID) {
		this.detectorId = detectorID;
	}
}