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

import static gda.jython.InterfaceProvider.getCurrentScanInformationHolder;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.dawnsci.nexus.IWritableNexusDevice;
import org.eclipse.dawnsci.nexus.NXdetector;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusNodeFactory;
import org.eclipse.dawnsci.nexus.NexusScanInfo;
import org.eclipse.dawnsci.nexus.builder.NexusObjectProvider;
import org.eclipse.dawnsci.nexus.builder.NexusObjectWrapper;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.january.DatasetException;
import org.eclipse.january.dataset.SliceND;
import org.eclipse.january.dataset.SliceNDIterator;
import org.opengda.detector.electronanalyser.event.ScanStartEvent;
import org.opengda.detector.electronanalyser.event.SequenceFileChangeEvent;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.Region;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.Sequence;
import org.opengda.detector.electronanalyser.server.VGScientaAnalyser;
import org.opengda.detector.electronanalyser.utils.RegionDefinitionResourceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;
import gda.data.fileregistrar.ClientFileAnnouncer;
import gda.data.scan.datawriter.NexusScanDataWriter;
import gda.data.scan.nexus.device.GDADeviceNexusConstants;
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.detector.NXDetector;
import gda.device.detector.areadetector.v17.ADBase;
import gda.device.detector.areadetector.v17.NDPluginBase;
import gda.device.detector.areadetector.v17.NDStats;
import gda.device.detector.nxdetector.NXCollectionStrategyPlugin;
import gda.jython.InterfaceProvider;
import gda.scan.ScanInformation;

/*
 * A wrapper detector for VGScienta Electron Analyser, which takes a sequence file defining a list of
 * regions as input and collects analyser data: image, spectrum, and external IO data for each enabled
 * region. This uses a collection strategy to setup, collect, and save this region data immediately.
 *
 * @author Oli Wenman
 *
 */
public class EW4000 extends NXDetector implements IWritableNexusDevice<NXdetector> {

	private static final Logger logger = LoggerFactory.getLogger(EW4000.class);
	private static final long serialVersionUID = -222459754772057676L;
	private static final String REGION_LIST = "region_list";
	private static final String INVALID_REGION_LIST = "invalid_region_list";

	private transient RegionDefinitionResourceUtil regionDefinitionResourceUtil;
	private transient EW4000CollectionStrategy collectionStrategy;
	private transient Scannable topup;
	private transient ClientFileAnnouncer clientFileAnnouncer;

	private boolean cachedCreateFileAtScanStart;

	private transient NXdetector detector = null;
	private transient SliceNDIterator invalidRegionSliceIterator = null;

	@Override
	public void setCollectionStrategy(NXCollectionStrategyPlugin nxCollectionStrategyPlugin) {
		if (!(nxCollectionStrategyPlugin instanceof EW4000CollectionStrategy)) {
			throw new IllegalArgumentException("Invalid collection strategy used. Only " + EW4000CollectionStrategy.class + " is compatible.");
		}
		super.setCollectionStrategy(nxCollectionStrategyPlugin);
		collectionStrategy = (EW4000CollectionStrategy) nxCollectionStrategyPlugin;
	}

	public void loadSequenceData(String sequenceFilename) throws DeviceException, FileNotFoundException {
		if (sequenceFilename == null) {
			throw new IllegalArgumentException("sequenceFilename cannot be null");
		}
		if (!Paths.get(sequenceFilename).isAbsolute()) {
			sequenceFilename = InterfaceProvider.getPathConstructor().createFromProperty("gda.ses.electronanalyser.seq.dir")
				+ File.separator
				+ sequenceFilename;
		}
		if (! new File(sequenceFilename).isFile()) {
			throw new FileNotFoundException("Sequence file \"" + sequenceFilename + "\" doesn't exist!");
		}
		Sequence sequence = null;
		try {
			regionDefinitionResourceUtil.setFileName(sequenceFilename);
			Resource resource = regionDefinitionResourceUtil.getResource();
			//Must remove existing resource first for new one to be loaded in
			resource.unload();
			resource.load(Collections.emptyMap());
			sequence = regionDefinitionResourceUtil.getSequence(resource);
		} catch (Exception e) {
			logger.error("Cannot load sequence file {}", sequenceFilename);
			throw new DeviceException("Cannot load sequence file: " + sequenceFilename, e);
		}

		if (sequence == null) {
			throw new DeviceException("Sequence is null");
		}
		List<Region> regions = sequence.getRegion().stream().filter(Region::isEnabled).toList();
		if(regions.isEmpty()) {
			throw new DeviceException("No enabled regions found!");
		}
		collectionStrategy.setSequence(sequence);
		logger.debug("Sequence file changed to {}{}", FilenameUtils.getFullPath(sequenceFilename), FilenameUtils.getName(sequenceFilename));
	}

	@Override
	public void prepareForCollection() throws DeviceException{
		//ToDo - change to spring bean decoratee?
		try {
			NDPluginBase pluginBase = collectionStrategy.getAnalyser().getNdArray().getPluginBase();
			ADBase adBase = collectionStrategy.getAnalyser().getAdBase();
			if (!pluginBase.isCallbackEnabled()) {
				pluginBase.setNDArrayPort(adBase.getPortName_RBV());
				pluginBase.enableCallbacks();
				pluginBase.setBlockingCallbacks(1);
			}
			NDStats ndStats = collectionStrategy.getAnalyser().getNdStats();
			NDPluginBase pluginBase2 = ndStats.getPluginBase();
			if (!pluginBase2.isCallbackEnabled()) {
				pluginBase2.setNDArrayPort(adBase.getPortName_RBV());
				pluginBase2.enableCallbacks();
				pluginBase2.setBlockingCallbacks(1);
				ndStats.setComputeStatistics((short) 1);
				ndStats.setComputeCentroid((short) 1);
			}
		} catch (Exception e) {
			logger.error("Failed to initialise ADArray and ADStats Plugins", e);
		}
		super.prepareForCollection();
	}

	private NexusObjectWrapper<NXdetector> initialiseNXdetector(NexusScanInfo info) {
		detector = NexusNodeFactory.createNXdetector();
		detector.setAttribute(null, NXdetector.NX_LOCAL_NAME, getName());
		detector.setAttribute(null, GDADeviceNexusConstants.ATTRIBUTE_NAME_SCAN_ROLE, "detector");
		int[] scanDimensions = info.getOverallShape();
		int numberOfRegions = collectionStrategy.getEnabledRegionNames().size();

		AnalyserRegionDatasetUtil.createOneDimensionalStructure(REGION_LIST, detector, new int[] {numberOfRegions}, String.class);
		invalidRegionSliceIterator = AnalyserRegionDatasetUtil.createMultiDimensionalDatasetAndSliceIterator(
			INVALID_REGION_LIST, scanDimensions, detector, new int[] {numberOfRegions}, String.class
		);

		String psuMode = "unknown";
		try {
			psuMode = collectionStrategy.getAnalyser().getPsuMode();
		} catch (Exception e) {
			logger.error("Unable to get {} mode to write to file",VGScientaAnalyser.PSU_MODE, e);
		}
		detector.setField(VGScientaAnalyser.PSU_MODE, psuMode);

		NexusObjectWrapper<NXdetector>  nexusWrapper = new NexusObjectWrapper<>(getName(), detector);
		//Set up axes as [scannables, ..., region_list]
		int regionAxisIndex = info.getOverallRank();
		nexusWrapper.setPrimaryDataFieldName(INVALID_REGION_LIST);
		nexusWrapper.addAxisDataFieldForPrimaryDataField(REGION_LIST, INVALID_REGION_LIST, regionAxisIndex, regionAxisIndex);
		return nexusWrapper;
	}

	@Override
	public List<NexusObjectProvider<?>> getNexusProviders(NexusScanInfo info) throws NexusException {
		List<NexusObjectProvider<?>> nexusProviders = collectionStrategy.getNexusProviders(info);
		NexusObjectWrapper<?> nxObject = initialiseNXdetector(info);
		nexusProviders.add(nxObject);
		return nexusProviders;
	}

	@Override
	public void atScanStart() throws DeviceException {
		super.atScanStart();
		//Refresh the data folder to make file appear at start of scan in GUI as it can be viewed while being written to
		ScanInformation scanInfo = getCurrentScanInformationHolder().getCurrentScanInformation();
		if(scanInfo == null) {
			logger.warn("scanInfo is null, can't peform atScanStart() setup.");
			return;
		}
		int scanNumber = scanInfo.getScanNumber();
		String fileName = scanInfo.getFilename();
		int totalNumberOfPoints = scanInfo.getNumberOfPoints();
		collectionStrategy.updateScriptController(
			new ScanStartEvent(scanNumber, totalNumberOfPoints, fileName)
		);
		if (clientFileAnnouncer == null) {
			logger.info("Unable to refresh project explorer as clientFileAnnouncer is null.");
		}
		else {
			clientFileAnnouncer.notifyFilesAvailable(new String[] {fileName});
			logger.info("Refreshed project explorer so scan file {} is visible", fileName);
		}
		cachedCreateFileAtScanStart = LocalProperties.check(NexusScanDataWriter.PROPERTY_NAME_CREATE_FILE_AT_SCAN_START, false);
		if(!cachedCreateFileAtScanStart) {
			logger.info("Property {} is false. Switching to true for this scan as detector {} must use it.", NexusScanDataWriter.PROPERTY_NAME_CREATE_FILE_AT_SCAN_START, getName());
		}
		LocalProperties.set(NexusScanDataWriter.PROPERTY_NAME_CREATE_FILE_AT_SCAN_START, true);

		logger.info("Updating clients to sequence file: {}", getSequenceFilename());
		collectionStrategy.updateScriptController(new SequenceFileChangeEvent(getSequenceFilename()));
	}

	@Override
	public void atPointStart() throws DeviceException {
		super.atPointStart();
		if (topup == null) {
			logger.warn("topup is null");
		} else {
			//Block and wait for top-up injection to finish + topup.tolerance time.
			topup.atPointStart();
		}
	}

	@Override
	public void writePosition(Object data, SliceND scanSlice) throws NexusException {
		//Not used as collection strategy writes own data when it needs to.
		//This class also writes own data before collectData of collectionStrategy.
	}

	public String getSequenceFilename() {
		return regionDefinitionResourceUtil.getFileName();
	}

	@Override
	public void collectData() throws DeviceException{
		try {
			//Validate regions to skip over any during data collection. Get invalid region names to save after.
			List<String> invalidRegionNames = collectionStrategy.validateRegions();
			AnalyserRegionDatasetUtil.overridePosition(
				detector.getLazyWritableDataset(REGION_LIST), collectionStrategy.getEnabledRegionNames()
			);
			AnalyserRegionDatasetUtil.writeNewPosition(
				detector.getLazyWritableDataset(INVALID_REGION_LIST), invalidRegionSliceIterator, invalidRegionNames
			);
		} catch (DatasetException e) {
			throw new DeviceException(e);
		}
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

	@Override
	public boolean isBusy() {
		return collectionStrategy.isBusy();
	}

	@Override
	public String getDescription() throws DeviceException {
		return "VGH Scienta Electron Analyser EW4000";
	}

	@Override
	public String getDetectorID() throws DeviceException {
		return "EW4000";
	}

	@Override
	public String getDetectorType() throws DeviceException {
		return "Electron Analyser";
	}

	public Region getCurrentRegion() {
		return collectionStrategy.getCurrentRegion();
	}

	public RegionDefinitionResourceUtil getRegionDefinitionResourceUtil() {
		return regionDefinitionResourceUtil;
	}

	public void setRegionDefinitionResourceUtil(RegionDefinitionResourceUtil regionDefinitionResourceUtil) {
		this.regionDefinitionResourceUtil = regionDefinitionResourceUtil;
	}

	public Scannable getTopup() {
		return topup;
	}

	public void setTopup(Scannable topup) {
		this.topup = topup;
	}

	public void setClientFileAnnouncer(ClientFileAnnouncer clientFileAnnouncer) {
		this.clientFileAnnouncer = clientFileAnnouncer;
	}
}