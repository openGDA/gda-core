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

import static gda.jython.InterfaceProvider.getTerminalPrinter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.dawnsci.nexus.NXdetector;
import org.eclipse.dawnsci.nexus.NexusConstants;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusNodeFactory;
import org.eclipse.dawnsci.nexus.NexusScanInfo;
import org.eclipse.dawnsci.nexus.NexusUtils;
import org.eclipse.dawnsci.nexus.builder.NexusObjectProvider;
import org.eclipse.dawnsci.nexus.builder.NexusObjectWrapper;
import org.eclipse.january.DatasetException;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.ILazyWriteableDataset;
import org.eclipse.january.dataset.SliceND;
import org.eclipse.january.dataset.SliceNDIterator;
import org.opengda.detector.electronanalyser.event.RegionChangeEvent;
import org.opengda.detector.electronanalyser.event.RegionStatusEvent;
import org.opengda.detector.electronanalyser.event.ScanEndEvent;
import org.opengda.detector.electronanalyser.event.ScanPointStartEvent;
import org.opengda.detector.electronanalyser.lenstable.RegionValidator;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.ACQUISITION_MODE;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.ENERGY_MODE;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.Region;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.STATUS;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.Sequence;
import org.opengda.detector.electronanalyser.nxdata.NXDetectorDataAnalyserRegionAppender;
import org.opengda.detector.electronanalyser.server.VGScientaAnalyser;
import org.opengda.detector.electronanalyser.utils.RegionDefinitionResourceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;
import gda.data.scan.datawriter.NexusScanDataWriter;
import gda.data.scan.nexus.device.GDADeviceNexusConstants;
import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.detector.nxdata.NXDetectorDataAppender;
import gda.device.detector.nxdetector.AsyncNXCollectionStrategy;
import gda.jython.InterfaceProvider;
import gda.jython.scriptcontroller.ScriptControllerBase;
import gda.jython.scriptcontroller.Scriptcontroller;
import gda.scan.ScanInformation;

public class EW4000CollectionStrategy implements AsyncNXCollectionStrategy{

	private static final Logger logger = LoggerFactory.getLogger(EW4000CollectionStrategy.class);
	private static final String REGION_OUTPUT_FORMAT = "%.5E";
	private static final int SLEEP_TIME_MILLISECONDS = 500;
	public static final String REGION_STATUS = "status";
	private enum RegionFileStatus {QUEUED, RUNNING, COMPLETED, COMPLETED_EARLY, ABORTED, ERROR}
	private enum ShutterPosition {IN, OUT}
	private static final int[] SCALAR_SHAPE = {};
	private static final String REGION_LIST = "region_list";
	private static final String INVALID_REGION_LIST = "invalid_region_list";

	private ExecutorService executorService = Executors.newSingleThreadExecutor();

	//Springbean settings
	private RegionDefinitionResourceUtil regionDefinitionResourceUtil;
	private Scriptcontroller scriptcontroller;
	private Scannable softXRayFastShutter;
	private Scannable hardXRayFastShutter;
	private VGScientaAnalyser analyser;
	private RegionValidator regionValidator;
	private String name;
	private boolean generateCallBacks = false;
	private List<Region> invalidRegions = new ArrayList<>();

	private Sequence sequence;
	private int scanDataPoint = 0;
	private int totalNumberOfPoints = 0;
	private int regionIndex = 0;
	private boolean busy = false;
	private double[] intensityValues = null;

	private Map<String, SliceNDIterator> sliceIteratorMap = new LinkedHashMap<>();
	private Map<String, NXdetector> detectorMap = new LinkedHashMap<>();

	private Future<double[]> result;

	private NexusObjectWrapper<NXdetector> initialiseNXdetectorRegion(Region region, NexusScanInfo info)  throws NexusException {
		final NXdetector detector = NexusNodeFactory.createNXdetector();
		String regionName = region.getName();
		detector.setField(VGScientaAnalyser.REGION_NAME, regionName);
		detector.setField(VGScientaAnalyser.LENS_MODE_STR, region.getLensMode());
		detector.setField(VGScientaAnalyser.ACQUISITION_MODE_STR, region.getAcquisitionMode().toString());
		detector.setField(VGScientaAnalyser.DETECTOR_MODE_STR, region.getDetectorMode().toString());
		detector.setField(VGScientaAnalyser.PASS_ENERGY, region.getPassEnergy());
		detector.setField(VGScientaAnalyser.ENERGY_MODE_STR, region.getEnergyMode().toString());
		try {
			double excitationEnergy = (region.getEnergyMode() == ENERGY_MODE.BINDING ? getAnalyser().calculateBeamEnergy(region) : 0.0);
			double lowEnergy   = excitationEnergy + region.getLowEnergy();
			double highEnergy  = excitationEnergy + region.getHighEnergy();
			double fixedEnergy = excitationEnergy + region.getFixEnergy();
			detector.setField(VGScientaAnalyser.LOW_ENERGY, lowEnergy);
			detector.setAttribute(VGScientaAnalyser.LOW_ENERGY, NexusConstants.UNITS, VGScientaAnalyser.ELECTRON_VOLTS);
			detector.setField(VGScientaAnalyser.HIGH_ENERGY, highEnergy);
			detector.setAttribute(VGScientaAnalyser.HIGH_ENERGY, NexusConstants.UNITS, VGScientaAnalyser.ELECTRON_VOLTS);
			detector.setField(VGScientaAnalyser.FIXED_ENERGY, fixedEnergy);
			detector.setAttribute(VGScientaAnalyser.FIXED_ENERGY, NexusConstants.UNITS, VGScientaAnalyser.ELECTRON_VOLTS);
		}
		catch (DeviceException e) {
			throw new NexusException("Unable to calculate excitation energy", e);
		}
		detector.setField(VGScientaAnalyser.DETECTOR_X_FROM, region.getFirstXChannel());
		detector.setField(VGScientaAnalyser.DETECTOR_X_TO, region.getLastXChannel());
		detector.setField(VGScientaAnalyser.DETECTOR_X_SIZE, region.getLastXChannel() - region.getFirstXChannel() + 1);
		detector.setField(VGScientaAnalyser.DETECTOR_Y_FROM, region.getFirstYChannel());
		detector.setField(VGScientaAnalyser.DETECTOR_Y_TO, region.getLastYChannel());
		detector.setField(VGScientaAnalyser.DETECTOR_Y_SIZE, region.getLastYChannel() - region.getFirstYChannel() + 1);

		double energyStep = region.getEnergyStep() / 1000.;
		int numIterations = region.getRunMode().isRepeatUntilStopped() ? 1000000 : region.getRunMode().getNumIterations();
		detector.setField(VGScientaAnalyser.ENERGY_STEP, energyStep);
		detector.setField(VGScientaAnalyser.NUMBER_OF_ITERATIONS, numIterations);
		detector.setAttribute(VGScientaAnalyser.ENERGY_STEP, NexusConstants.UNITS, VGScientaAnalyser.ELECTRON_VOLTS);
		detector.setField(VGScientaAnalyser.NUMBER_OF_SLICES, region.getSlices());

		int[] scanDimensions = info.getOverallShape();
		int energyAxisSize = calculateEnergyAxisSize(region);
		int angleAxisSize = calculateAngleAxisSize(region);
		int externalIOSize = calculateExternalIOSize(region);
		setupMultiDimensionalData(regionName, VGScientaAnalyser.IMAGE, scanDimensions, detector, new int[] {angleAxisSize, energyAxisSize}, Double.class);
		setupMultiDimensionalData(regionName, VGScientaAnalyser.SPECTRUM, scanDimensions, detector, new int[] {energyAxisSize}, Double.class);
		setupMultiDimensionalData(regionName, VGScientaAnalyser.EXTERNAL_IO, scanDimensions, detector, new int[] {externalIOSize}, Double.class);
		setupMultiDimensionalData(regionName, VGScientaAnalyser.INTENSITY, scanDimensions, detector, new int[] {1}, Double.class);
		setupMultiDimensionalData(regionName, VGScientaAnalyser.EXCITATION_ENERGY, scanDimensions, detector, new int[] {1}, Double.class, VGScientaAnalyser.ELECTRON_VOLTS);
		setupMultiDimensionalData(regionName, VGScientaAnalyser.REGION_VALID, scanDimensions, detector, new int[] {1}, Boolean.class);

		String angleUnits = region.getLensMode().equals("Transmission") ? VGScientaAnalyser.PIXEL : VGScientaAnalyser.ANGLES;
		setupOneDimensionalData(regionName, VGScientaAnalyser.ANGLES, detector, new int[] {angleAxisSize}, Double.class, angleUnits);
		setupOneDimensionalData(regionName, VGScientaAnalyser.ENERGIES, detector, new int[] {energyAxisSize}, Double.class, VGScientaAnalyser.ELECTRON_VOLTS);

		//Step time and total steps give slightly different results when received from the detector compared to region
		//Therefore we will populate this data later with accurate data
		setupOneDimensionalData(regionName, VGScientaAnalyser.TOTAL_STEPS, detector, SCALAR_SHAPE, Integer.class);
		setupOneDimensionalData(regionName, VGScientaAnalyser.TOTAL_TIME, detector, SCALAR_SHAPE, Double.class, "s");
		setupOneDimensionalData(regionName, VGScientaAnalyser.STEP_TIME, detector, SCALAR_SHAPE, Double.class, "s");
		setupOneDimensionalData(regionName, REGION_STATUS, detector, SCALAR_SHAPE, String.class);

		detectorMap.put(regionName, detector);

		NexusObjectWrapper<NXdetector>  nexusWrapper = new NexusObjectWrapper<>(region.getName(), detector);
		int scanRank = info.getOverallRank();

		//Axes is now [scannables, ..., angles, energies]
		int angleAxisIndex = scanRank;
		int energyAxisIndex = angleAxisIndex +1;
		int[] energyDimensionalMappings = calculateAxisDimensionMappings(scanRank, energyAxisIndex);
		nexusWrapper.setPrimaryDataFieldName(VGScientaAnalyser.IMAGE);
		nexusWrapper.addAxisDataFieldForPrimaryDataField(VGScientaAnalyser.ANGLES, VGScientaAnalyser.IMAGE, angleAxisIndex, angleAxisIndex);
		nexusWrapper.addAxisDataFieldForPrimaryDataField(VGScientaAnalyser.SPECTRUM, VGScientaAnalyser.IMAGE, energyAxisIndex, energyDimensionalMappings);
		nexusWrapper.addAxisDataFieldForPrimaryDataField(VGScientaAnalyser.EXTERNAL_IO, VGScientaAnalyser.IMAGE, energyAxisIndex, energyDimensionalMappings);
		nexusWrapper.addAxisDataFieldForPrimaryDataField(VGScientaAnalyser.ENERGIES, VGScientaAnalyser.IMAGE, energyAxisIndex, energyAxisIndex);
		nexusWrapper.addAxisDataFieldName(VGScientaAnalyser.EXCITATION_ENERGY);
		nexusWrapper.addAxisDataFieldName(VGScientaAnalyser.INTENSITY);
		return nexusWrapper;
	}

	private void setupOneDimensionalData(String detectorName, String dataName, NXdetector detector, int[] dimensions, Class<?> clazz) {
		setupMultiDimensionalData(detectorName, dataName, null, detector, dimensions, clazz);
	}

	private void setupOneDimensionalData(String detectorName, String dataName, NXdetector detector, int[] dimensions, Class<?> clazz, String units) {
		setupMultiDimensionalData(detectorName, dataName, null, detector, dimensions, clazz, units);
	}

	private ILazyWriteableDataset setupMultiDimensionalData(String detectorName, String dataName, int[] scanDimensions, NXdetector detector, int[] dimensions, Class<?> clazz) {
		return setupMultiDimensionalData(detectorName, dataName, scanDimensions, detector, dimensions, clazz, null);
	}

	private ILazyWriteableDataset setupMultiDimensionalData(String detectorName, String dataName, int[] scanDimensions, NXdetector detector, int[] dimensions, Class<?> clazz, String units) {
		ILazyWriteableDataset lazyWritableDataset;
		if (dimensions == SCALAR_SHAPE || scanDimensions == null) {
			logger.debug("Setting up scalar data structure  for detector {} with data type {}", detectorName, dataName);
			if(scanDimensions == null) {
				logger.debug("shape = {}", Arrays.toString(dimensions));
			}
			int[] maxShape = dimensions.clone();
			lazyWritableDataset = detector.initializeLazyDataset(dataName, maxShape, clazz);
		}
		else {
			logger.debug("Setting up data structure for detector {} with data type {}", detectorName, dataName);
			int[] maxShape = ArrayUtils.addAll(scanDimensions, dimensions);
			int[] axesToIgnore = new int[dimensions.length];
			for (int i = 0; i < dimensions.length; i++) {
				axesToIgnore[axesToIgnore.length - i - 1] = maxShape.length - i - 1;
			}
			logger.debug("maxShape = {}, axesToIgnore = {}", Arrays.toString(maxShape), Arrays.toString(axesToIgnore));
			lazyWritableDataset = detector.initializeLazyDataset(dataName, maxShape, clazz);
			SliceND firstSlice = new SliceND(maxShape);
			SliceNDIterator sliceIterator = new SliceNDIterator(firstSlice, axesToIgnore);
			sliceIteratorMap.put(joinStrings(detectorName, dataName), sliceIterator);
		}
		if (units != null) {
			detector.setAttribute(dataName, NexusConstants.UNITS, units);
		}
		return lazyWritableDataset;
	}

	private int[] calculateAxisDimensionMappings(int scanRank, int axisIndex) {
		//Calculate dimension mappings for scannables, then join index we want to use to
		int[] dimensionMappings = new int[scanRank + 1];
		for (int i=0; i < scanRank; i++) {
			dimensionMappings[i] = i;
		}
		dimensionMappings[scanRank] = axisIndex;
		return dimensionMappings;
	}

	private int calculateEnergyAxisSize(Region region) {
		double energyStep = region.getEnergyStep() / 1000.;
		if (region.getAcquisitionMode() == ACQUISITION_MODE.FIXED) {
			return region.getLastXChannel() - region.getFirstXChannel() + 1;
		} else {
			return (int) roundHalfDown((region.getHighEnergy() - region.getLowEnergy()) / (energyStep)) + 1;
		}
	}

	//Function to round number to nearest integer with exception of .5 being round down rather than up.
	private double roundHalfDown(double d) {
		double i = Math.floor(d); // integer portion
		double f = d - i; // fractional portion
		// round integer portion based on fractional portion
		return f <= 0.5 ? i : i + 1D;
	}

	private int calculateAngleAxisSize(Region region) {
		return region.getSlices();
	}

	private int calculateExternalIOSize(Region region) {
		return region.getAcquisitionMode().toString().equalsIgnoreCase("Fixed") ? 1 : calculateEnergyAxisSize(region);
	}

	private NexusObjectWrapper<NXdetector> initialiseNXdetector(NexusScanInfo info) {
		final NXdetector detector = NexusNodeFactory.createNXdetector();
		detector.setAttribute(null, NXdetector.NX_LOCAL_NAME, getName());
		detector.setAttribute(null, GDADeviceNexusConstants.ATTRIBUTE_NAME_SCAN_ROLE, "detector");
		int[] scanDimensions = info.getOverallShape();
		setupMultiDimensionalData(getName(), REGION_LIST, scanDimensions, detector, new int[] {getEnabledRegionNames(false).size()}, String.class);
		setupMultiDimensionalData(getName(), INVALID_REGION_LIST, scanDimensions, detector, new int[] {getEnabledRegionNames(false).size()}, String.class);
		String psuMode = "unknown";
		try {
			psuMode = getAnalyser().getPsuMode();
		} catch (Exception e) {
			logger.error("Unable to get {} mode to write to file",VGScientaAnalyser.PSU_MODE, e);
		}
		detector.setField(VGScientaAnalyser.PSU_MODE, psuMode);
		detectorMap.put(getName(), detector);
		return new NexusObjectWrapper<>(getName(), detector, REGION_LIST, INVALID_REGION_LIST);
	}

	public List<NexusObjectProvider<?>> getNexusProviders(NexusScanInfo info) throws NexusException {
		detectorMap.clear();
		sliceIteratorMap.clear();

		List<Region> regions = getEnabledRegions(false);
		List<NexusObjectProvider<?>> nexusProviders = new ArrayList<>();
		for (Region r : regions) {
			NexusObjectWrapper<?> nxObject = initialiseNXdetectorRegion(r, info);
			nexusProviders.add(nxObject);
		}
		NexusObjectWrapper<NXdetector> nxObject = initialiseNXdetector(info);
		nexusProviders.add(nxObject);
		return nexusProviders;
	}

	@Override
	public void prepareForCollection(int numberImagesPerCollection, ScanInformation scanInfo) throws Exception {
		if(getEnabledRegionNames(false).isEmpty()) {
			if(sequence == null) {
				throw new DeviceException("No sequence file is loaded, sequence is null.");
			}
			throw new DeviceException("There are no regions in the sequence.");
		}
		if (!LocalProperties.check(NexusScanDataWriter.PROPERTY_NAME_CREATE_FILE_AT_SCAN_START, false)) {
			throw new DeviceException(
				"Detector " + getName() + " must have property '"
				+ NexusScanDataWriter.PROPERTY_NAME_CREATE_FILE_AT_SCAN_START + "' set to true."
			);
		}
		executorService = Executors.newSingleThreadExecutor();
		regionIndex = scanDataPoint = 0;
		totalNumberOfPoints = scanInfo.getNumberOfPoints();
		intensityValues = new double[getEnabledRegions(false).size()];

		print("Found the following regions enabled:");
		for (String region : getEnabledRegionNames()) {
			print(" - " + region);
		}
	}

	@Override
	public void prepareForCollection(double collectionTime, int numberImagesPerCollection, ScanInformation scanInfo) throws Exception {
		prepareForCollection(numberImagesPerCollection, scanInfo);
	}

	public List<String> validateRegions() {
		logger.debug("Validating regions at scanDataPoint = {}...", scanDataPoint);
		invalidRegions.clear();

		if (regionValidator == null) {
			logger.warn("Cannot verify if sequence contains invalid regions, regionValidator is null");
			return Collections.emptyList();
		}
		String elementSet = "unknown";
		try {
			elementSet = getAnalyser().getPsuMode();
		}
		catch (Exception e) {
			logger.error("Cannot get element set value for region validation");
			return Collections.emptyList();
		}

		List<String> invalidRegionNames = new ArrayList<>();
		invalidRegions = new ArrayList<>();
		for (Region region : getEnabledRegions()) {
			if (!regionValidator.isValidRegion(region, elementSet)) {
				invalidRegions.add(region);
				invalidRegionNames.add(region.getName());
			}
			else {
				invalidRegionNames.add("-");
			}
		}
		if (invalidRegions.isEmpty()) {
			logger.debug("All regions are valid.");
		}
		else {
			String errorMessage = "DETECTED INVALID REGIONS";
			String skipMessage = "Skipping the following regions at scanpoint = " + Integer.toString(scanDataPoint + 1) + ": ";
			print("");
			print("*".repeat(skipMessage.length()));
			print(errorMessage);
			print(skipMessage);
			for (Region region : invalidRegions) {
				print(" - " + region.getName());
			}
			print("*".repeat(skipMessage.length()));
			print("");
		}
		return invalidRegionNames;
	}

	private void beforeCollectData() {
		validateRegions();
		if (isScanFirstRegion() && isScanFirstScanDataPoint()) {
			try {
				for (Region region : getEnabledRegions(false)) {
					updateRegionFileStatus(region, RegionFileStatus.QUEUED);
				}
			}
			catch (DatasetException e) {
				logger.warn("unable to update initial region values", e);
			}
		}
		busy = true;
		scanDataPoint++;
		updateScriptController(new ScanPointStartEvent(scanDataPoint));
	}

	@Override
	public void collectData() throws DeviceException {
		beforeCollectData();
		Callable<double[]> analyserJob = () -> {
			regionIndex = 0;
			List<Region> regions = getEnabledRegions(false);
			try {
				collectRegionData();
			}
			catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				getAnalyser().stop();
				updateScriptController(new RegionStatusEvent(regions.get(regionIndex).getRegionId(), STATUS.ABORTED, regionIndex + 1));
				updateAllRegionStatusThatDidNotReachMaxIterations(RegionFileStatus.ABORTED);
			}
			catch (Exception e) {
				throw new DeviceException(e.getMessage());
			}
			finally {
				if (InterfaceProvider.getCurrentScanController().isFinishEarlyRequested()) {
					logger.warn("Finish early detected, updating region data with new status.");
					updateAllRegionStatusThatDidNotReachMaxIterations(RegionFileStatus.COMPLETED_EARLY);
				}
				busy = false;
			}
			return intensityValues;
		};
		result = executorService.submit(analyserJob);
	}

	private void collectRegionData() throws Exception {
		Arrays.fill(intensityValues, 0);
		List<Region> regions = getEnabledRegions(false);
		for (regionIndex = 0; regionIndex < regions.size(); regionIndex++) {
			Region region = regions.get(regionIndex);
			intensityValues[regionIndex] = runDataCollection(region);
			if (Thread.interrupted()) {
				break;
			}
		}
	}

	private double runDataCollection(Region region) throws Exception {
		boolean isRegionValid = !getInvalidRegionNames().contains(region.getName());
		updateRegionFileStatus(region, RegionFileStatus.RUNNING);
		if (isRegionValid) {
			// Update GUI to let it know region has changed
			updateScriptController(new RegionChangeEvent(region.getRegionId(), region.getName()));
			updateScriptController(new RegionStatusEvent(region.getRegionId(), STATUS.RUNNING, regionIndex + 1));

			getAnalyser().configureWithNewRegion(region);

			//open/close fast shutter according to beam used
			boolean sourceHard = regionDefinitionResourceUtil.isSourceHard(region);
			updateFastShutterStatus(sourceHard ? getHardXRayFastShutter() : getSoftXRayFastShutter(), ShutterPosition.OUT);
			updateFastShutterStatus(!sourceHard ? getHardXRayFastShutter() : getSoftXRayFastShutter(), ShutterPosition.IN);
			getAnalyser().collectData();
			getAnalyser().waitWhileBusy();
			updateScriptController(new RegionStatusEvent(region.getRegionId(), STATUS.COMPLETED, regionIndex + 1));
		}
		else {
			updateScriptController(new RegionStatusEvent(region.getRegionId(), STATUS.INVALID, regionIndex + 1));
			logger.warn("Skipping data collection for region {} as it is invalid. Writing blank data.", region.getName());
		}
		return saveData(region, isRegionValid);
	}

	private double saveData(Region region, boolean isRegionValid) throws Exception {
		if(isScanFirstRegion()) {
			List<String> invalidRegionNames = new ArrayList<>(Collections.nCopies(getEnabledRegionNames(false).size(), ""));
			int i =0;
			for (String invalidRegionName :  getInvalidRegionNames()) {
				invalidRegionNames.set(i, invalidRegionName);
				i++;
			}
			writeNewPosition(getName(), INVALID_REGION_LIST,invalidRegionNames);

			if (isScanFirstScanDataPoint()) {
				writeNewPosition(getName(), REGION_LIST, getEnabledRegionNames(false));
			}
		}
		String regionName = region.getName();
		double[] energyAxis = isRegionValid ? getAnalyser().getEnergyAxis() : new double[calculateEnergyAxisSize(region)];
		double[] angleAxis  = isRegionValid ? getAnalyser().getAngleAxis() : new double[calculateAngleAxisSize(region)];
		double[] image = isRegionValid ? getAnalyser().getImage(energyAxis.length  * angleAxis.length) : new double[energyAxis.length * angleAxis.length];
		double[] spectrum = isRegionValid ? getAnalyser().getSpectrum() : new double[energyAxis.length];
		double[] externalIO = isRegionValid ? getAnalyser().getExternalIODataFormatted() : new double[calculateExternalIOSize(region)];
		double excitationEnergy = isRegionValid ? getAnalyser().getExcitationEnergy() : getAnalyser().calculateBeamEnergy(region);
		double intensity = isRegionValid ? getAnalyser().getTotalIntensity() : 0;
		logger.debug("writing data for region {} at scanDataPoint {}", regionName, scanDataPoint);
		writeNewPosition(regionName, VGScientaAnalyser.IMAGE, image);
		writeNewPosition(regionName, VGScientaAnalyser.SPECTRUM, spectrum);
		writeNewPosition(regionName, VGScientaAnalyser.EXTERNAL_IO, externalIO);
		writeNewPosition(regionName, VGScientaAnalyser.EXCITATION_ENERGY, new double[] {excitationEnergy});
		writeNewPosition(regionName, VGScientaAnalyser.INTENSITY, new double[] {intensity});
		writeNewPosition(regionName, VGScientaAnalyser.REGION_VALID, !getInvalidRegionNames().contains(regionName));

		if(isRegionValid) {
			overridePosition(regionName,  VGScientaAnalyser.ANGLES, angleAxis);
			overridePosition(regionName,  VGScientaAnalyser.ENERGIES, energyAxis);
			//Write over as analyser gives slightly different results to region object
			double stepTime = getAnalyser().getStepTime();
			double totalSteps = getAnalyser().getTotalSteps();
			double totalTime = stepTime * totalSteps;
			overridePosition(regionName, VGScientaAnalyser.STEP_TIME, stepTime);
			overridePosition(regionName, VGScientaAnalyser.TOTAL_STEPS, totalSteps);
			overridePosition(regionName, VGScientaAnalyser.TOTAL_TIME, totalTime);
		}
		else if(isScanFirstScanDataPoint()){
			//If region is invalid and this is the first scanDataPoint, save this instead
			overridePosition(region.getName(), VGScientaAnalyser.STEP_TIME, region.getStepTime());
			overridePosition(region.getName(), VGScientaAnalyser.TOTAL_STEPS, region.getTotalSteps());
			overridePosition(region.getName(), VGScientaAnalyser.TOTAL_TIME, region.getTotalTime());
		}

		if (scanDataPoint == totalNumberOfPoints) {
			updateRegionFileStatus(region, RegionFileStatus.COMPLETED);
		}
		else {
			updateRegionFileStatus(region, RegionFileStatus.QUEUED);
		}
		return intensity;
	}

	private void writeNewPosition(String detectorName, String field, Object data) throws DatasetException {
		ILazyWriteableDataset lazyWrittableDataset = detectorMap.get(detectorName).getLazyWritableDataset(field);
		Dataset dataSet = NexusUtils.createFromObject(data, lazyWrittableDataset.getName());
		SliceNDIterator sliceIterator = sliceIteratorMap.get(joinStrings(detectorName, field));
		SliceND scanSlice;
		if (sliceIterator == null) {
			logger.debug("No slice iterator found for {} {}. Adding data by creating slice using maxshape = {}", detectorName, field, Arrays.toString(lazyWrittableDataset.getMaxShape()));
			scanSlice = new SliceND(lazyWrittableDataset.getMaxShape());
		}
		else {
			sliceIterator.hasNext();
			scanSlice = sliceIterator.getCurrentSlice();
		}
		lazyWrittableDataset.setSlice(null, dataSet, scanSlice);
		logger.debug("{} {} has been saved to file", detectorName, field);
	}

	private void overridePosition(String detectorName, String field, Object data) throws DatasetException {
		ILazyWriteableDataset lazyWrittableDataset = detectorMap.get(detectorName).getLazyWritableDataset(field);
		Dataset dataSet = NexusUtils.createFromObject(data, lazyWrittableDataset.getName());
		SliceND scanSlice = new SliceND(lazyWrittableDataset.getMaxShape());
		lazyWrittableDataset.setSlice(null, dataSet, scanSlice);
		logger.debug("{} {} has been saved to file", detectorName, field);
	}

	private void updateAllRegionStatusThatDidNotReachMaxIterations(RegionFileStatus status) {
		logger.info("Updating all regions that did not fully complete with status {}", status);
		if(getEnabledRegionNames(false).isEmpty()) {
			return;
		}
		int startingIndex = scanDataPoint == totalNumberOfPoints ? regionIndex : 0;
		List<Region> regions = getEnabledRegions(false);
		try {
			for (int i = startingIndex ; i < regions.size(); i++) {
				Region region = regions.get(i);
				updateRegionFileStatus(region, status);
			}
		} catch (DatasetException e) {
			logger.error("Unable to update region status to {}", status, e);
		}
	}

	@Override
	public void waitWhileBusy() throws DeviceException, InterruptedException {
		while (getStatus() == Detector.BUSY){
			Thread.sleep(SLEEP_TIME_MILLISECONDS);
		}
	}

	@Override
	public int getStatus() throws DeviceException {
		return busy ? Detector.BUSY : Detector.IDLE;
	}

	public boolean isBusy() {
		return busy;
	}

	//update GUI with scan event
	public void updateScriptController(Serializable event) {
		if (getScriptcontroller() instanceof ScriptControllerBase) {
			getScriptcontroller().update(getScriptcontroller(), event);
		}
	}

	private void updateRegionFileStatus(Region region, RegionFileStatus status) throws DatasetException {
		logger.debug("updating region {} to status {}", region.getName(), status);
		if (!detectorMap.isEmpty()) {
			overridePosition(region.getName(), REGION_STATUS, status.toString());
		}
		else {
			logger.error("Unable to update region file status as detector data is empty.");
		}
	}

	private void updateFastShutterStatus(Scannable shutter, ShutterPosition status) throws DeviceException {
		String softValue = Character.toUpperCase(status.toString().charAt(0)) + status.toString().substring(1).toLowerCase();
		shutter.moveTo(softValue);
	}

	@Override
	public List<String> getInputStreamNames() {
		return getEnabledRegionNames(false);
	}

	@Override
	public List<String> getInputStreamFormats() {
		// Return the number of REGION_OUTPUT_FORMAT's equal to number of regions
		return Collections.nCopies(getEnabledRegionNames().size(), REGION_OUTPUT_FORMAT);
	}

	@Override
	public List<NXDetectorDataAppender> read(int maxToRead) throws NoSuchElementException, InterruptedException, DeviceException {
		try {
			result.get();
		} catch (ExecutionException e) {
			throw new DeviceException(e);
		}
		return Arrays.asList(
			new NXDetectorDataAnalyserRegionAppender(
				getEnabledRegionNames(false).toArray(String[]::new),
				intensityValues
			)
		);
	}

	@Override
	public boolean willRequireCallbacks() {
		return false;
	}

	@Override
	public void prepareForLine() throws Exception {
		//Not needed
	}

	@Override
	public void completeLine() throws Exception {
		//Not needed
	}

	@Override
	public void completeCollection() throws Exception {
		updateScriptController(new ScanEndEvent());
	}

	@Override
	public void atCommandFailure() throws Exception {
		cleanupOnError(STATUS.INVALID);
	}

	@Override
	public void stop() {
		cleanupOnError(STATUS.ABORTED);
	}

	private void cleanupOnError(STATUS status) {
		try {
			if (!executorService.awaitTermination(SLEEP_TIME_MILLISECONDS, TimeUnit.MILLISECONDS)) {
				executorService.shutdownNow();
			}
			getAnalyser().stop();
			updateFastShutterStatus(hardXRayFastShutter, ShutterPosition.IN);
			updateFastShutterStatus(softXRayFastShutter, ShutterPosition.IN);
			boolean isAborted = status == STATUS.ABORTED;
			if(!getEnabledRegionNames(false).isEmpty()) {
				updateScriptController(new RegionStatusEvent(getCurrentRegion().getRegionId(), isAborted ? STATUS.ABORTED : STATUS.INVALID, regionIndex + 1));
				updateAllRegionStatusThatDidNotReachMaxIterations(isAborted ? RegionFileStatus.ABORTED: RegionFileStatus.ERROR);;
			}
		}
		catch(DeviceException e) {
			logger.error("error cleanup ran into an error. {}", e.getMessage());
		}
		catch (InterruptedException e) {
			logger.error("executorService was interrupted when attempting to stop. {}", e.getMessage());
			Thread.currentThread().interrupt();
		}
		finally {
			busy = false;
		}
	}

	@Override
	public double getAcquireTime() throws Exception {
		try {
			return getEnabledRegions().stream().mapToDouble(Region::getTotalTime).sum();
		}
		catch (Exception e) {
			throw new DeviceException("Cannot get regions collection time.", e);
		}
	}

	@Override
	public double getAcquirePeriod() throws Exception {
		return getAcquireTime();
	}

	@Override
	public void configureAcquireAndPeriodTimes(double collectionTime) throws Exception {
		//Deprecated, same as prepareForCollection
	}

	private List<Region> getEnabledRegions(boolean removeInvalidRegions) {
		if (sequence != null) {
			List<Region> regions = sequence.getRegion().stream().filter(Region::isEnabled).collect(Collectors.toList());
			if (removeInvalidRegions) {
				regions.removeAll(invalidRegions);
			}
			return regions;
		}
		return Collections.emptyList();
	}

	private List<Region> getEnabledRegions() {
		return getEnabledRegions(true);
	}

	public List<String> getEnabledRegionNames(boolean removeInvalidRegions) {
		return getEnabledRegions(removeInvalidRegions).stream().map(Region::getName).toList();
	}

	public List<String> getEnabledRegionNames() {
		return getEnabledRegionNames(true);
	}

	private List<String> getInvalidRegionNames() {
		return invalidRegions.stream().map(Region::getName).toList();
	}

	private String joinStrings(String string1, String string2) {
		return string1 + "_" + string2;
	}

	private void print(String message) {
		getTerminalPrinter().print(message);
	}

	public Region getCurrentRegion() {
		List<Region> regions = getEnabledRegions(false);
		if (!regions.isEmpty()) {
			return regions.get(regionIndex);
		}
		return null;
	}

	private boolean isScanFirstRegion() {
		return regionIndex == 0;
	}

	private boolean isScanLastRegion() {
		return regionIndex == getEnabledRegions(false).size() - 1;
	}

	private boolean isScanFirstScanDataPoint() {
		return scanDataPoint == 1;
	}

	public RegionDefinitionResourceUtil getRegionDefinitionResourceUtil() {
		return regionDefinitionResourceUtil;
	}

	public void setRegionDefinitionResourceUtil(RegionDefinitionResourceUtil regionDefinitionResourceUtil) {
		this.regionDefinitionResourceUtil = regionDefinitionResourceUtil;
	}

	public Scriptcontroller getScriptcontroller() {
		return scriptcontroller;
	}

	public void setScriptcontroller(Scriptcontroller scriptcontroller) {
		this.scriptcontroller = scriptcontroller;
	}

	public Scannable getSoftXRayFastShutter() {
		return softXRayFastShutter;
	}

	public void setSoftXRayFastShutter(Scannable softXRayFastShutter) {
		this.softXRayFastShutter = softXRayFastShutter;
	}

	public Scannable getHardXRayFastShutter() {
		return hardXRayFastShutter;
	}

	public void setHardXRayFastShutter(Scannable hardXRayFastShutter) {
		this.hardXRayFastShutter = hardXRayFastShutter;
	}

	public VGScientaAnalyser getAnalyser() {
		return analyser;
	}

	public void setAnalyser(VGScientaAnalyser analyser) {
		this.analyser = analyser;
	}

	public RegionValidator getRegionValidator() {
		return regionValidator;
	}

	public void setRegionValidator(RegionValidator regionValidator) {
		this.regionValidator = regionValidator;
	}

	public void setSequence(Sequence sequence) {
		this.sequence = sequence;
	}

	@Override
	public void setGenerateCallbacks(boolean generateCallBacks) {
		this.generateCallBacks = generateCallBacks;
	}

	@Override
	public boolean isGenerateCallbacks() {
		return generateCallBacks;
	}

	@Override
	public int getNumberImagesPerCollection(double collectionTime) throws Exception {
		return getEnabledRegionNames().size();
	}

	@Override
	public boolean requiresAsynchronousPlugins() {
		return false;
	}

	@Override
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}