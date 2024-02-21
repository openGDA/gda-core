/*-
 * Copyright © 2023 Diamond Light Source Ltd.
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
import static gda.jython.InterfaceProvider.getTerminalPrinter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.ArrayUtils;
import org.eclipse.dawnsci.nexus.IWritableNexusDevice;
import org.eclipse.dawnsci.nexus.NXdetector;
import org.eclipse.dawnsci.nexus.NexusConstants;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusNodeFactory;
import org.eclipse.dawnsci.nexus.NexusScanInfo;
import org.eclipse.dawnsci.nexus.NexusUtils;
import org.eclipse.dawnsci.nexus.builder.NexusObjectProvider;
import org.eclipse.dawnsci.nexus.builder.NexusObjectWrapper;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.ILazyWriteableDataset;
import org.eclipse.january.dataset.SliceND;
import org.eclipse.january.dataset.SliceNDIterator;
import org.opengda.detector.electronanalyser.event.RegionChangeEvent;
import org.opengda.detector.electronanalyser.event.RegionStatusEvent;
import org.opengda.detector.electronanalyser.event.ScanEndEvent;
import org.opengda.detector.electronanalyser.event.ScanPointStartEvent;
import org.opengda.detector.electronanalyser.event.ScanStartEvent;
import org.opengda.detector.electronanalyser.lenstable.RegionValidator;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.ACQUISITION_MODE;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.ENERGY_MODE;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.Region;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.STATUS;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.Sequence;
import org.opengda.detector.electronanalyser.server.VGScientaAnalyser;
import org.opengda.detector.electronanalyser.utils.RegionDefinitionResourceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ObjectArrays;

import gda.configuration.properties.LocalProperties;
import gda.data.fileregistrar.FileRegistrar;
import gda.data.fileregistrar.FileRegistrarHelper;
import gda.data.scan.datawriter.NexusScanDataWriter;
import gda.data.scan.nexus.device.GDADeviceNexusConstants;
import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.detector.DetectorBase;
import gda.device.detector.areadetector.v17.ADBase;
import gda.device.detector.areadetector.v17.NDPluginBase;
import gda.device.detector.areadetector.v17.NDStats;
import gda.device.scannable.ScannableBase;
import gda.device.scannable.ScannableUtils;
import gda.factory.Finder;
import gda.jython.InterfaceProvider;
import gda.jython.scriptcontroller.ScriptControllerBase;
import gda.jython.scriptcontroller.Scriptcontroller;
import gda.scan.ScanInformation;

/*
 * A class for the VGScienta Electron Analyser, which takes a sequence file defining a list of regions as
 * input and collect analyser data - image, spectrum and external IO data - for each active regions in the
 * listed order. This uses property "gda.nexus.createFileAtScanStart" and saves the region data as soon as
 * it is created. If combined with other detectors in the same scan, they also must be compatible with this
 * property.
 */
public class EW4000 extends DetectorBase implements IWritableNexusDevice<NXdetector>{

	private static final long serialVersionUID = -1155203719584202094L;

	private static final Logger logger = LoggerFactory.getLogger(EW4000.class);

	private transient ExecutorService executorService = Executors.newSingleThreadExecutor();
	private transient Future<double[]> readoutValue;

	//Springbean settings
	private transient RegionDefinitionResourceUtil regionDefinitionResourceUtil;
	private transient Scriptcontroller scriptcontroller;
	private transient Scannable softXRayFastShutter;
	private transient Scannable hardXRayFastShutter;
	private transient Scannable topup;
	private VGScientaAnalyser analyser;
	private transient RegionValidator regionValidator;

	private transient Sequence sequence;
	private transient List<Region> invalidRegions = new ArrayList<>();

	private transient Map<String, SliceNDIterator> sliceIteratorMap = new LinkedHashMap<>();
	private transient Map<String, NXdetector> detectorMap = new LinkedHashMap<>();

	private int scanDataPoint = 0;
	private int totalNumberOfPoints = 0;
	private int regionIndex = 0;
	private boolean busy = false;
	private boolean readyToCollect = false;

	private double[] intensityValues = null;

	private enum RegionFileStatus {QUEUED, RUNNING, COMPLETED, COMPLETED_EARLY, ABORTED, ERROR}
	private enum FastShutter {IN, OUT}

	private static final int SLEEP_TIME_MILLISECONDS = 500;

	public static final int[] SCALAR_SHAPE = { };

	public static final String REGION_STATUS = "status";
	private static final String REGION_LIST = "region_list";
	private static final String INVALID_REGION_LIST = "invalid_region_list";

	private boolean extraRegionPrinting = true;

	private transient EW4000.EW4000ExtraRegionPrintingHelper printerHelper = new EW4000ExtraRegionPrintingHelper();
	private boolean cachedSuppressHeaderValue;

	public Sequence loadSequenceData(String sequenceFilename) throws DeviceException, FileNotFoundException {

		boolean validFile = false;
		if (sequenceFilename != null) {
			if (!Paths.get(sequenceFilename).isAbsolute()) {
				sequenceFilename = InterfaceProvider.getPathConstructor().createFromProperty("gda.ses.electronanalyser.seq.dir")
					+ File.separator
					+ sequenceFilename;
			}
			if (new File(sequenceFilename).isFile()) {
				validFile = true;
			}
		}
		if (!validFile){
			throw new FileNotFoundException("Sequence file \"" + sequenceFilename + "\" doesn't exist!");
		}

		sequence = null;

		try {
			Resource resource = regionDefinitionResourceUtil.getResource(sequenceFilename);
			//Must remove existing resource first for new one to be loaded in
			resource.unload();
			resource.load(Collections.emptyMap());
			sequence = regionDefinitionResourceUtil.getSequence(resource);
		} catch (Exception e) {
			logger.error("Cannot load sequence file {}", sequenceFilename);
			throw new DeviceException("Cannot load sequence file: " + sequenceFilename, e);
		}
		invalidRegions.clear();

		if (getEnabledRegions(false).isEmpty()) {
			throw new DeviceException("No enabled regions found!");
		}

		logger.debug("Sequence file changed to {}{}", FilenameUtils.getFullPath(sequenceFilename), FilenameUtils.getName(sequenceFilename));

		print("Found the following regions enabled:");
		for (String region : getEnabledRegionNames()) {
			print(" - " + region);
		}

		setExtraNames(getEnabledRegionNames().toArray(String[]::new));
		setInputNames(new String[]{});

		String [] outputFormats = new String[getEnabledRegions(false).size()];
		Arrays.fill(outputFormats, "%.5E");
		setOutputFormat(outputFormats);

		return sequence;
	}

	private void print(String message) {
		getTerminalPrinter().print(message);
	}

	//Function to round number to nearest integer with exception of .5 being round down rather than up.
	private double roundHalfDown(double d) {
		double i = Math.floor(d); // integer portion
		double f = d - i; // fractional portion
		// round integer portion based on fractional portion
		return f <= 0.5 ? i : i + 1D;
	}

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
			throw new NexusException("Unable to calculate excitation energy");
		}

		double energyStep = region.getEnergyStep() / 1000.;
		detector.setField(VGScientaAnalyser.ENERGY_STEP, energyStep);
		detector.setAttribute(VGScientaAnalyser.ENERGY_STEP, NexusConstants.UNITS, VGScientaAnalyser.ELECTRON_VOLTS);

		detector.setField(VGScientaAnalyser.NUMBER_OF_SLICES, region.getSlices());

		int numIterations = region.getRunMode().isRepeatUntilStopped() ? 1000000 : region.getRunMode().getNumIterations();
		detector.setField(VGScientaAnalyser.NUMBER_OF_ITERATIONS, numIterations);

		int energyAxisSize = calculateEnergyAxisSize(region);
		int angleAxisSize = calculateAngleAxisSize(region);
		int externalIOSize = calculateExternalIOSize(region);

		detector.setField(VGScientaAnalyser.DETECTOR_X_FROM, region.getFirstXChannel());
		detector.setField(VGScientaAnalyser.DETECTOR_X_TO, region.getLastXChannel());
		detector.setField(VGScientaAnalyser.DETECTOR_X_SIZE, region.getLastXChannel() - region.getFirstXChannel() + 1);

		detector.setField(VGScientaAnalyser.DETECTOR_Y_FROM, region.getFirstYChannel());
		detector.setField(VGScientaAnalyser.DETECTOR_Y_TO, region.getLastYChannel());
		detector.setField(VGScientaAnalyser.DETECTOR_Y_SIZE, region.getLastYChannel() - region.getFirstYChannel() + 1);

		setupDataStructure(regionName, VGScientaAnalyser.IMAGE, info, detector, new int[] {angleAxisSize, energyAxisSize}, Integer.class);
		setupDataStructure(regionName, VGScientaAnalyser.SPECTRUM, info, detector, new int[] {energyAxisSize}, Double.class);
		setupDataStructure(regionName, VGScientaAnalyser.EXTERNAL_IO, info, detector, new int[] {externalIOSize}, Double.class);
		setupDataStructure(regionName, VGScientaAnalyser.INTENSITY, info, detector, new int[] {1}, Double.class);
		setupDataStructure(regionName, VGScientaAnalyser.EXCITATION_ENERGY, info, detector, new int[] {1}, Double.class, VGScientaAnalyser.ELECTRON_VOLTS);

		if(region.getLensMode().equals("Transmission")) {
			setupDataStructure(regionName, VGScientaAnalyser.ANGLES, info, detector, new int[] {angleAxisSize}, Double.class, VGScientaAnalyser.PIXEL);
		}
		else {
			setupDataStructure(regionName, VGScientaAnalyser.ANGLES, info, detector, new int[] {angleAxisSize}, Double.class, VGScientaAnalyser.ANGLES);
		}
		setupDataStructure(regionName, VGScientaAnalyser.ENERGIES, info, detector, new int[] {energyAxisSize}, Double.class, VGScientaAnalyser.ELECTRON_VOLTS);

		//Scalar datasets
		//Step time and total steps give slightly different results when received from the detector compared to region
		//Therefore we will populate this data later with accurate data
		setupDataStructure(regionName, VGScientaAnalyser.TOTAL_STEPS, info, detector, SCALAR_SHAPE, Integer.class);
		setupDataStructure(regionName, VGScientaAnalyser.TOTAL_TIME, info, detector, SCALAR_SHAPE, Double.class, "s");
		setupDataStructure(regionName, VGScientaAnalyser.STEP_TIME, info, detector, SCALAR_SHAPE, Double.class, "s");

		setupDataStructure(regionName, REGION_STATUS, info, detector, SCALAR_SHAPE, String.class);
		setupDataStructure(regionName, VGScientaAnalyser.REGION_VALID, info, detector, new int[] {1}, Boolean.class);

		return new NexusObjectWrapper<>(region.getName(), detector,
			VGScientaAnalyser.IMAGE, VGScientaAnalyser.SPECTRUM, VGScientaAnalyser.EXTERNAL_IO, VGScientaAnalyser.EXCITATION_ENERGY, VGScientaAnalyser.INTENSITY, VGScientaAnalyser.ENERGIES, VGScientaAnalyser.ANGLES
		);
	}

	private int calculateEnergyAxisSize(Region region) {
		double energyStep = region.getEnergyStep() / 1000.;
		if (region.getAcquisitionMode() == ACQUISITION_MODE.FIXED) {
			return region.getLastXChannel() - region.getFirstXChannel() + 1;
		} else {
			return (int) roundHalfDown((region.getHighEnergy() - region.getLowEnergy()) / (energyStep)) + 1;
		}
	}

	private int calculateAngleAxisSize(Region region) {
		return region.getSlices();
	}

	private int calculateExternalIOSize(Region region) {
		return region.getAcquisitionMode().toString().equalsIgnoreCase("Fixed") ? 1 : calculateEnergyAxisSize(region);
	}

	private ILazyWriteableDataset setupDataStructure(String detectorName, String dataName, NexusScanInfo info, NXdetector detector, int[] dimensions, Class<?> clazz) {
		return setupDataStructure(detectorName, dataName, info, detector, dimensions, clazz, null);
	}

	private ILazyWriteableDataset setupDataStructure(String detectorName, String dataName, NexusScanInfo info, NXdetector detector, int[] dimensions, Class<?> clazz, String units) {

		ILazyWriteableDataset lazyWritableDataset;

		if (dimensions == SCALAR_SHAPE) {
			logger.debug("Setting up scalar data structure for detector {} with data type {}", detectorName, dataName);

			int[] maxShape = dimensions.clone();
			lazyWritableDataset = detector.initializeLazyDataset(dataName, maxShape, clazz);
		}
		else {
			logger.debug("Setting up data structure for detector {} with data type {}", detectorName, dataName);

			int[] maxShape = ArrayUtils.addAll(info.getOverallShape(), dimensions);
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

	private NexusObjectWrapper<NXdetector> initialiseNXdetector(NexusScanInfo info) {
		final NXdetector detector = NexusNodeFactory.createNXdetector();
		detector.setAttribute(null, NXdetector.NX_LOCAL_NAME, getName());
		detector.setAttribute(null, GDADeviceNexusConstants.ATTRIBUTE_NAME_SCAN_ROLE, "detector");
		setupDataStructure(getName(), REGION_LIST, info, detector, new int[] {getEnabledRegionNames(false).size()}, String.class);
		setupDataStructure(getName(), INVALID_REGION_LIST, info, detector, new int[] {getEnabledRegionNames(false).size()}, String.class);
		return new NexusObjectWrapper<>(getName(), detector, REGION_LIST, INVALID_REGION_LIST);
	}

	@Override
	public List<NexusObjectProvider<?>> getNexusProviders(NexusScanInfo info) throws NexusException {

		List<Region> regions = getEnabledRegions(false);
		List<NexusObjectProvider<?>> nexusProviders = new ArrayList<>();

		for (Region r : regions) {
			NexusObjectWrapper<NXdetector> nxObject = initialiseNXdetectorRegion(r, info);
			nexusProviders.add(nxObject);
			detectorMap.put(r.getName(), nxObject.getNexusObject());

		}
		NexusObjectWrapper<NXdetector> nxObject = initialiseNXdetector(info);
		nexusProviders.add(nxObject);
		detectorMap.put(getName(), nxObject.getNexusObject());

		printerHelper.setScanFieldNames(info.getScanFieldNames().toArray(String[]::new));

		return nexusProviders;
	}

	@Override
	public void prepareForCollection() {
		executorService = Executors.newSingleThreadExecutor();

		try {
			NDPluginBase pluginBase = getAnalyser().getNdArray().getPluginBase();
			ADBase adBase = getAnalyser().getAdBase();
			if (!pluginBase.isCallbackEnabled()) {
				pluginBase.setNDArrayPort(adBase.getPortName_RBV());
				pluginBase.enableCallbacks();
				pluginBase.setBlockingCallbacks(1);
			}
			NDStats ndStats = getAnalyser().getNdStats();
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
	}

	@Override
	public void atScanStart() throws DeviceException {

		if (!LocalProperties.check(NexusScanDataWriter.PROPERTY_NAME_CREATE_FILE_AT_SCAN_START, false)) {
			throw new DeviceException(
				"Detector " + getName() + " must have property '"
				+ NexusScanDataWriter.PROPERTY_NAME_CREATE_FILE_AT_SCAN_START + "' set to true."
			);
		}
		regionIndex = 0;
		ScanInformation scanInfo = getCurrentScanInformationHolder().getCurrentScanInformation();

		if (scanInfo != null) {
			int scanNumber     = scanInfo.getScanNumber();
			String fileName = scanInfo.getFilename();
			totalNumberOfPoints = scanInfo.getNumberOfPoints();

			updateScriptController(
				new ScanStartEvent(scanNumber, totalNumberOfPoints, fileName)
			);

			//Refresh the data folder to make file appear at start of scan in GUI as it can be viewed while being written to
			FileRegistrarHelper.registerFile(fileName);
			Object fileUpdater = Finder.find("file_registrar");
			FileRegistrar fileHelper = (FileRegistrar)fileUpdater;
			fileHelper.scanEnd();
		}
		else {
			totalNumberOfPoints = getEnabledRegions(false).size();
		}
		intensityValues = new double[getEnabledRegions(false).size()];

		if (isExtraRegionPrinting() && getEnabledRegions(false).size() > 1) {
			cachedSuppressHeaderValue = LocalProperties.check(LocalProperties.GDA_SERVER_SCAN_PRINT_SUPPRESS_HEADER, false);
			LocalProperties.set(LocalProperties.GDA_SERVER_SCAN_PRINT_SUPPRESS_HEADER, true);
		}

		readyToCollect = true;
	}

	@Override
	public void atPointStart() throws DeviceException {
		scanDataPoint++;
		updateScriptController(new ScanPointStartEvent(scanDataPoint));
	}

	private void validateRegions() {
		invalidRegions.clear();

		if (regionValidator != null) {
			logger.debug("Checking all regions from sequence file are valid using live excitation energies at scanpoint = {}...", scanDataPoint);

			invalidRegions = new ArrayList<>();
			for (Region region : getEnabledRegions()) {
				if (!regionValidator.isValidRegion(region, sequence.getElementSet())) {
					invalidRegions.add(region);
				}
			}

			if (!invalidRegions.isEmpty()) {
				String errorMessage = "DETECTED INVALID REGIONS";
				String skipMessage = "Skipping the following regions at scanpoint = " + Integer.toString(scanDataPoint) + ": ";
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
			else {
				logger.debug("All regions are valid.");
			}
		}
		else {
			logger.warn("Cannot verify if sequence contains invalid regions, regionValidator is null");
		}
	}

	private void beforeCollectData() throws DeviceException {

		//Allows us to stop collection when using pos command as it doesn't call atScanStart()
		if (!readyToCollect) {
			throw new DeviceException(getName() + " is not ready to collect. This detector is only comptaible with \"analyserscan\" command for setup.");
		}

		if (regionIndex == 0 && scanDataPoint == 1) {
			try {
				for (Region region : getEnabledRegions(false)) {
					updateRegionFileStatus(region, RegionFileStatus.QUEUED);
				}
			}
			catch (NexusException e) {
				logger.warn("unable to update initial region values", e);
			}
		}
		validateRegions();

		if (topup != null) {
			//Block and wait for top-up injection to finish + topup.tolerance time.
			topup.atPointStart();
		}
		busy = true;
	}

	@Override
	public void collectData() throws DeviceException {
		beforeCollectData();

		Callable<double[]> analyserJob = () -> {
			regionIndex = 0;
			List<Region> regions = getEnabledRegions(false);

			Region region = regions.get(regionIndex);
			try {
				Arrays.fill(intensityValues, 0);
				for (regionIndex = 0; regionIndex < regions.size(); regionIndex++) {

					region = regions.get(regionIndex);
					intensityValues[regionIndex] = runDataCollection(region);

					if (isExtraRegionPrinting()) {
						printerHelper.printExtraRegionProgress();
					}
					if (Thread.interrupted()) {
						break;
					}
				}
			}
			catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				stopAnalyser();
				updateScriptController(new RegionStatusEvent(region.getRegionId(), STATUS.ABORTED, regionIndex + 1));
				updateAllRegionStatusThatDidNotReachMaxIterations(RegionFileStatus.ABORTED);
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
		readoutValue = executorService.submit(analyserJob);
	}

	private double runDataCollection(Region region) throws Exception {
		double totalIntensity = 0;
		updateRegionFileStatus(region, RegionFileStatus.RUNNING);

		if (scanDataPoint == 1) {
			writeScalarData(region.getName(), VGScientaAnalyser.STEP_TIME, region.getStepTime());
			writeScalarData(region.getName(), VGScientaAnalyser.TOTAL_STEPS, region.getTotalSteps());
			writeScalarData(region.getName(), VGScientaAnalyser.TOTAL_TIME, region.getTotalTime());
		}

		if (getInvalidRegionNames().contains(region.getName())) {
			updateScriptController(new RegionStatusEvent(region.getRegionId(), STATUS.INVALID, regionIndex + 1));

			logger.warn("Skipping data collection for region {} as it is invalid. Writing blank data.", region.getName());
			//Populate data fields with zero values for skipped region
			double[] energyAxis = new double[calculateEnergyAxisSize(region)];
			double[] angleAxis  = new double[calculateAngleAxisSize(region)];
			double[] image = new double[energyAxis.length * angleAxis.length];
			double[] spectrum = new double[energyAxis.length];
			double[] externalIO = new double[calculateExternalIOSize(region)];

			double excitationEnergy = getAnalyser().calculateBeamEnergy(region);

			//Write empty datasets if region is invalid to keep the shape consistent
			writeData(region.getName(), energyAxis, angleAxis, image, spectrum, externalIO, excitationEnergy, totalIntensity);
		}
		else {
			// Update GUI to let it know region has changed
			updateScriptController(new RegionChangeEvent(region.getRegionId(), region.getName()));
			updateScriptController(new RegionStatusEvent(region.getRegionId(), STATUS.RUNNING, regionIndex + 1));

			getAnalyser().configureWithNewRegion(region);

			//open/close fast shutter according to beam used
			if (region.getExcitationEnergy() < regionDefinitionResourceUtil.getXRaySourceEnergyLimit()) {
				updateFastShutterStatus(FastShutter.OUT, FastShutter.IN);

			} else {
				updateFastShutterStatus(FastShutter.IN, FastShutter.OUT);
			}
			getAnalyser().collectData();
			getAnalyser().waitWhileBusy();

			String regionName = getAnalyser().getRegionName();

			double[] energyAxis = getAnalyser().getEnergyAxis();
			double[] angleAxis = getAnalyser().getAngleAxis();

			double[] image = getAnalyser().getImage(energyAxis.length  * angleAxis.length);
			double[] spectrum = getAnalyser().getSpectrum();
			double[] externalIO = getAnalyser().getExternalIODataFormatted();
			double excitationEnergy = getAnalyser().getExcitationEnergy();
			totalIntensity = getAnalyser().getTotalIntensity();

			double stepTime = getAnalyser().getStepTime();
			double totalSteps = getAnalyser().getTotalSteps();
			double totalTime = stepTime * totalSteps;

			writeData(regionName, energyAxis, angleAxis, image, spectrum, externalIO, excitationEnergy, totalIntensity);

			//Write over as analyser gives slightly different results to region object
			writeScalarData(regionName, VGScientaAnalyser.STEP_TIME, stepTime);
			writeScalarData(regionName, VGScientaAnalyser.TOTAL_STEPS, totalSteps);
			writeScalarData(regionName, VGScientaAnalyser.TOTAL_TIME, totalTime);

			updateScriptController(new RegionStatusEvent(region.getRegionId(), STATUS.COMPLETED, regionIndex + 1));
		}

		if (scanDataPoint == totalNumberOfPoints) {
			updateRegionFileStatus(region, RegionFileStatus.COMPLETED);
		}
		else {
			updateRegionFileStatus(region, RegionFileStatus.QUEUED);
		}
		return totalIntensity;
	}

	@Override
	public Object readout() throws DeviceException {
		//Block until detector is finished collecting
		try {
			return readoutValue.get();
		} catch (ExecutionException | InterruptedException e) {
			updateAllRegionStatusThatDidNotReachMaxIterations(RegionFileStatus.ERROR);
			updateScriptController(new RegionStatusEvent(getCurrentRegion().getRegionId(), STATUS.INVALID, regionIndex + 1));
			busy = false;
			Thread.currentThread().interrupt();
			throw new DeviceException("Unable to get readout value for region " + getEnabledRegionNames().get(regionIndex) + ".\n" + e.getMessage());
		}
	}

	private void stopAnalyser() {
		try {
			getAnalyser().stop();
		}
		catch (DeviceException e) {
			logger.error("failed to stop the analyser acquisition on interrupt.", e);
		}
	}

	private boolean datasetAlreadyExists(String detectorName, String dataset) {
		return detectorMap.get(detectorName).getLazyWritableDataset(dataset).getShape()[0] >= 1;
	}

	private void writeData(
			String regionName, double[] energyAxis, double[] angleAxis, double[] image,
			double[] spectrum, double[] externalIO, double excitationEnergy, double totalIntensity) throws NexusException {

		logger.debug("writing data from analyser for scanDataPoint = {}, regionName = {}", scanDataPoint, regionName);

		if (scanDataPoint == 1) {
			if (regionIndex == 0) {
				writePosition(getName(), REGION_LIST, getEnabledRegionNames(false));
			}
		}
		if(regionIndex == 0) {
			List<String> invalidRegionNames = new ArrayList<>(Collections.nCopies(getEnabledRegionNames(false).size(), ""));
			int i =0;
			for (String invalidRegionName :  getInvalidRegionNames()) {
				invalidRegionNames.set(i, invalidRegionName);
				i++;
			}
			writePosition(getName(), INVALID_REGION_LIST,invalidRegionNames);
		}

		//Write data for this set only once and only if real data
		if (!datasetAlreadyExists(regionName, VGScientaAnalyser.ANGLES) && Arrays.stream(energyAxis).sum() != 0.) {
			writePosition(regionName, VGScientaAnalyser.ANGLES, angleAxis);
		}
		if (!datasetAlreadyExists(regionName, VGScientaAnalyser.ENERGIES) && Arrays.stream(energyAxis).sum() != 0.) {
			if(getAnalyser().getCachedEnergyMode() == ENERGY_MODE.BINDING) {
				energyAxis = Arrays.stream(energyAxis).map(i -> excitationEnergy - i).toArray();
			}
			writePosition(regionName, VGScientaAnalyser.ENERGIES, energyAxis);
		}
		writePosition(regionName, VGScientaAnalyser.IMAGE, image);
		writePosition(regionName, VGScientaAnalyser.SPECTRUM, spectrum);
		writePosition(regionName, VGScientaAnalyser.EXTERNAL_IO, externalIO);
		writePosition(regionName, VGScientaAnalyser.EXCITATION_ENERGY, new double[] {excitationEnergy});
		writePosition(regionName, VGScientaAnalyser.INTENSITY, new double[] {totalIntensity});
		writePosition(regionName, VGScientaAnalyser.REGION_VALID, !getInvalidRegionNames().contains(regionName));
	}

	@Override
	public void writePosition(Object data, SliceND scanSlice) throws NexusException {
		//Not used as we need to write data more often than this. N number of times for N number of regions
		//per scanDataPoint. However this is only called once per scanDataPoint.
		//Call own writePosition/writeScalarData method rather than use framework.
	}

	private void writePosition(String detectorName, String field, Object data) throws NexusException {
		try {
			ILazyWriteableDataset lazyWrittableDataset = detectorMap.get(detectorName).getLazyWritableDataset(field);
			Dataset dataSet = NexusUtils.createFromObject(data, lazyWrittableDataset.getName());

			SliceNDIterator sliceIterator = sliceIteratorMap.get(joinStrings(detectorName, field));
			sliceIterator.hasNext();
			SliceND scanSlice = sliceIterator.getCurrentSlice();

			lazyWrittableDataset.setSlice(null, dataSet, scanSlice);
			logger.debug("{} {} has been saved to file", detectorName, field);
		}
		catch (Exception e) {
			throw new NexusException("Error writing data for \"" + detectorName + "\" - dataset:" + field + ". " + e.getMessage());
		}
	}

	private void writeScalarData(String detectorName, String field, Object data) throws NexusException {
		try {
			ILazyWriteableDataset lazyWrittableDataset = detectorMap.get(detectorName).getLazyWritableDataset(field);
			Dataset dataSet = NexusUtils.createFromObject(data, lazyWrittableDataset.getName());
			SliceND scanSlice = new SliceND(lazyWrittableDataset.getMaxShape());
			lazyWrittableDataset.setSlice(null, dataSet, scanSlice);
			logger.debug("{} {} has been saved to file", detectorName, field);
		}
		catch (Exception e) {
			throw new NexusException("Error writing data for \"" + detectorName + "\" - " + field + ". " + e.getMessage());
		}
	}

	@Override
	public void scanEnd() throws NexusException {
		busy = false;
		readyToCollect = false;
		scanDataPoint = 0;
		sliceIteratorMap.clear();
		detectorMap.clear();
		printerHelper.getScannables().clear();

		if (isExtraRegionPrinting() && getEnabledRegions(false).size() > 1) {
			LocalProperties.set(LocalProperties.GDA_SERVER_SCAN_PRINT_SUPPRESS_HEADER, cachedSuppressHeaderValue);
		}
		updateScriptController(new ScanEndEvent());
	}

	@Override
	public void stop() {
		try {
			if (!executorService.awaitTermination(SLEEP_TIME_MILLISECONDS, TimeUnit.MILLISECONDS)) {
				executorService.shutdownNow();
			}
			stopAnalyser();
			busy = false;
			updateAllRegionStatusThatDidNotReachMaxIterations(RegionFileStatus.ABORTED);
			updateFastShutterStatus(FastShutter.IN, FastShutter.IN);

			if (!getEnabledRegions(false).isEmpty()) {
				updateScriptController(new RegionStatusEvent(getCurrentRegion().getRegionId(), STATUS.ABORTED, regionIndex + 1));
			}
		}
		catch(DeviceException e) {
			logger.error("Failed to move fast shutter. {}", e.getMessage());
		}
		catch (InterruptedException e) {
			logger.error("executorService was interrupted when attempting to stop. {}", e.getMessage());
			Thread.currentThread().interrupt();
		}
	}

	private void updateAllRegionStatusThatDidNotReachMaxIterations(RegionFileStatus status) {
		try {
			if (scanDataPoint != totalNumberOfPoints) {
				for (Region region : getEnabledRegions(false)) {
					updateRegionFileStatus(region, status);
				}
			}
			else {
				for (int i = regionIndex ; i < getEnabledRegions(false).size(); i++) {
					Region region = getEnabledRegions().get(i);
					updateRegionFileStatus(region, status);
				}
			}
		} catch (NexusException e) {
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

	//update GUI with scan event
	private void updateScriptController(Serializable event) {
		if (getScriptcontroller() instanceof ScriptControllerBase) {
			getScriptcontroller().update(getScriptcontroller(), event);
		}
	}

	private void updateRegionFileStatus(Region region, RegionFileStatus status) throws NexusException {
		logger.debug("updating region {} to status {}", region.getName(), status);

		if (!detectorMap.isEmpty()) {
			writeScalarData(region.getName(), REGION_STATUS, status.toString());
		}
		else {
			logger.error("Unable to update region file status as detector data is empty.");
		}
	}

	private void updateFastShutterStatus(FastShutter softStatus, FastShutter hardStatus) throws DeviceException {
		if (softXRayFastShutter!=null) {
			String softValue = Character.toUpperCase(softStatus.toString().charAt(0)) + softStatus.toString().substring(1).toLowerCase();
			softXRayFastShutter.moveTo(softValue);
		}
		if (hardXRayFastShutter!=null) {
			String hardValue = Character.toUpperCase(hardStatus.toString().charAt(0)) + hardStatus.toString().substring(1).toLowerCase();
			hardXRayFastShutter.moveTo(hardValue);
		}
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

	private List<String> getEnabledRegionNames(boolean removeInvalidRegions) {
		return getEnabledRegions(removeInvalidRegions).stream().map(Region::getName).toList();
	}

	private List<String> getEnabledRegionNames() {
		return getEnabledRegionNames(true);
	}

	private List<String> getInvalidRegionNames() {
		return invalidRegions.stream().map(Region::getName).toList();
	}

	private String joinStrings(String string1, String string2) {
		return string1 + "_" + string2;
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

	@Override
	public boolean createsOwnFiles() throws DeviceException {
		return false;
	}

	@Override
	public double getCollectionTime() throws DeviceException {
		try {
			// total time from all regions
			return getEnabledRegions().stream().mapToDouble(Region::getTotalTime).sum();
		}
		catch (Exception e) {
			throw new DeviceException("Cannot get regions collection time.", e);
		}
	}

	public Region getCurrentRegion() {
		List<Region> regions = getEnabledRegions(false);
		if (!regions.isEmpty()) {
			return regions.get(regionIndex);
		}
		return null;
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

	public Scannable getTopup() {
		return topup;
	}

	public void setTopup(Scannable topup) {
		this.topup = topup;
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

	public String getSequenceFilename() {
		return sequence.getFilename();
	}

	public boolean isExtraRegionPrinting() {
		return extraRegionPrinting;
	}

	public void setExtraRegionPrinting(boolean extraRegionPrinting) {
		this.extraRegionPrinting = extraRegionPrinting;
	}

	public void configureExtraRegionPrinting(List<Scannable> scannables) {
		printerHelper.setScannables(scannables);
	}

	//Helper class responsible for manually printing region progress straight away when a region is done
	class EW4000ExtraRegionPrintingHelper {

		private List<Scannable> scannables = new ArrayList<>();
		private String[] scanFieldNames;
		static final String PLACEHOLDER_DETECTOR_VALUE = "-";
		private static final String DELIMINATOR = "\t";

		private void printExtraRegionProgress() {
			String[] positions = getAllPositions();
			if (regionIndex == 0 && scanDataPoint == 1 && regionIndex != getEnabledRegions(false).size() -1) {
				printFormattedValues(positions, true);
			}
			else if (regionIndex != getEnabledRegions(false).size() -1) {
				printFormattedValues(positions, false);
			}
		}

		private void printFormattedValues(String[] values, boolean printHeader) {
			// work out the lengths of the header string and the lengths of each element from the toString method
			// and pad each to adjust

			String headerString = String.join(DELIMINATOR, scanFieldNames).trim();
			String dataString   = String.join(DELIMINATOR, values);

			String[] headerElements = headerString.split(DELIMINATOR);
			String[] dataElements = dataString.split(DELIMINATOR);

			for (int i = 0; i < headerElements.length; i++) {
				int headerLength = headerElements[i].length();
				int dataLength = dataElements[i].length();

				int maxLength = dataLength > headerLength ? dataLength : headerLength;
				String format = "%" + maxLength + "s";

				headerElements[i] = String.format(format, headerElements[i].trim());
				dataElements[i] = String.format(format, dataElements[i].trim());
			}

			if (printHeader) {
				print(String.join(DELIMINATOR, headerElements));
			}
			print(String.join(DELIMINATOR, dataElements));
		}

		public String[] getAllPositions() {

			String[] positions = getBaseStringPositions();
			try {
				for (int scanFieldNameIndex = 0; scanFieldNameIndex < scanFieldNames.length; scanFieldNameIndex++) {
					for (Object scannable : scannables) {
						if (scannable instanceof ScannableBase scan) {
							Object position = scan.getPosition();

							String[] header = ObjectArrays.concat(scan.getInputNames(), scan.getExtraNames(), String.class);
							String[] scannableFormat = scan.getOutputFormat();

							String[] scannableValues = ScannableUtils.getFormattedCurrentPositionArray(position, scannableFormat.length, scannableFormat);

							int subArrayStartIndex = Collections.indexOfSubList(Arrays.asList(scanFieldNames), Arrays.asList(header));
							int sunArrayEndIndex = subArrayStartIndex + scannableValues.length;

							for (int i = subArrayStartIndex ; i < sunArrayEndIndex; i++) {
								positions[i] = scannableValues[i - subArrayStartIndex];
							}
						}
					}
				}
			}
			catch (DeviceException e) {
				logger.warn("Error getting scannable posiiton", e);
			}
			return positions;
		}

		private int calculateRegionOutputStringLength(String output) {

			Pattern regex = Pattern.compile("(\\d+(?:\\.\\d+)?)");
			Matcher matcher = regex.matcher(output);
			matcher.find();

			//Assumes scientific notation as set via loadSequenceFile().
			//e.g 1.2345E+06, +2 is for number on left side of decimal and decimal
			//                +4 is for E+XY
			return 2 + Integer.valueOf(matcher.group()) + 4;
		}

		private String[] getBaseStringPositions() {
			String[] positions = new String[scanFieldNames.length];

			//Add space adding for any small length input/extra names
			Arrays.fill(positions, "     " + PLACEHOLDER_DETECTOR_VALUE);

			for (int i = 0; i < getEnabledRegions(false).size() ; i++) {

				String regionName = getEnabledRegionNames(false).get(i);
				int allValuesIndex = ArrayUtils.indexOf(scanFieldNames, regionName);

				if (i <= regionIndex) {
					positions[allValuesIndex] = String.format(getOutputFormat()[i], intensityValues[i]);
				}
				else {
					int length = calculateRegionOutputStringLength(getOutputFormat()[i -1]);
					String placeholder = " ".repeat(length);
					placeholder = placeholder.substring(0, placeholder.length() -1) + "-";

					//Calculate string length via output format
					positions[allValuesIndex] = placeholder;
				}
			}
			return positions;
		}

		public void setScannables(List<Scannable> scannables) {
			this.scannables = scannables;
		}

		public List<Scannable> getScannables(){
			return this.scannables;
		}

		public String[] getScanFieldNames() {
			return scanFieldNames;
		}

		public void setScanFieldNames(String[] scanFieldNames) {
			this.scanFieldNames = scanFieldNames;
		}
	}
}
