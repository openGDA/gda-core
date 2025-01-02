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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.eclipse.dawnsci.analysis.dataset.impl.FFT;
import org.eclipse.dawnsci.nexus.NXdetector;
import org.eclipse.dawnsci.nexus.NexusConstants;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusScanInfo;
import org.eclipse.dawnsci.nexus.NexusUtils;
import org.eclipse.dawnsci.nexus.builder.NexusObjectWrapper;
import org.eclipse.january.DatasetException;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.SliceND;
import org.opengda.detector.electronanalyser.api.SESRegion;
import org.opengda.detector.electronanalyser.api.SESSequence;
import org.opengda.detector.electronanalyser.event.RegionChangeEvent;
import org.opengda.detector.electronanalyser.event.RegionStatusEvent;
import org.opengda.detector.electronanalyser.event.ScanPointStartEvent;
import org.opengda.detector.electronanalyser.lenstable.IRegionValidator;
import org.opengda.detector.electronanalyser.server.VGScientaAnalyser;
import org.opengda.detector.electronanalyser.utils.AnalyserRegionDatasetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.Scannable;
import gda.jython.InterfaceProvider;
import gda.scan.ScanInformation;


/*
 * A collection strategy for VGScienta Electron Analyser, which takes a sequence file defining a list of
 * regions as input and collects analyser data. This class is able to setup, collect, and save this region
 * data immediately. It holds NXdetectors and SliceNDIterators which allows it save data whenever it is needed
 * while also appending the data to the correct slice and dimensions via the {@link AbstractWriteRegionsImmediatelyCollectionStrategy}.
 *
 * @author Oli Wenman
 */
public class EW4000CollectionStrategy extends AbstractWriteRegionsImmediatelyCollectionStrategy<SESRegion> {

	private static final Logger logger = LoggerFactory.getLogger(EW4000CollectionStrategy.class);
	private enum RegionFileStatus {QUEUED, RUNNING, COMPLETED, COMPLETED_EARLY, ABORTED}
	private enum ShutterPosition {IN, OUT;
		@Override public String toString() {
			return StringUtils.capitalize(super.toString().toLowerCase());
		}
	}
	private static final String REGION_STATUS = "status";

	//Springbean settings
	private Map<Scannable, Scannable> energySourceToShutterMap = new HashMap<>();
	private VGScientaAnalyser analyser;
	private IRegionValidator regionValidator;
	private boolean generateCallBacks = false;

	private SESSequence sequence;
	private List<SESRegion> invalidRegions = new ArrayList<>();
	private int scanDataPoint = 0;
	private int totalNumberOfPoints = 0;

	@Override
	protected NexusObjectWrapper<NXdetector> initialiseNXdetectorRegion(final SESRegion region, final NXdetector detector, final NexusScanInfo info) throws NexusException {
		final String regionName = region.getName();
		detector.setField(VGScientaAnalyser.REGION_NAME, regionName);
		detector.setField(VGScientaAnalyser.LENS_MODE_STR, region.getLensMode());
		detector.setField(VGScientaAnalyser.ACQUISITION_MODE_STR, region.getAcquisitionMode());
		detector.setField(VGScientaAnalyser.DETECTOR_MODE_STR, region.getDetectorMode());
		detector.setField(VGScientaAnalyser.PASS_ENERGY, region.getPassEnergy());
		detector.setField(VGScientaAnalyser.ENERGY_MODE_STR, region.getEnergyMode());
		try {
			final double excitationEnergy = (region.isEnergyModeBinding() ? (double) region.getExcitationEnergySourceScannable().getPosition() : 0.0);
			final double lowEnergy   = excitationEnergy + region.getLowEnergy();
			final double highEnergy  = excitationEnergy + region.getHighEnergy();
			final double fixedEnergy = excitationEnergy + region.getFixEnergy();
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

		final double energyStep = region.getEnergyStep() / 1000.;
		final int numIterations = region.getIterations();
		detector.setField(VGScientaAnalyser.ENERGY_STEP, energyStep);
		detector.setField(VGScientaAnalyser.NUMBER_OF_ITERATIONS, numIterations);
		detector.setAttribute(VGScientaAnalyser.ENERGY_STEP, NexusConstants.UNITS, VGScientaAnalyser.ELECTRON_VOLTS);
		detector.setField(VGScientaAnalyser.NUMBER_OF_SLICES, region.getSlices());

		final int energyAxisSize = calculateEnergyAxisSize(region);
		final int angleAxisSize = calculateAngleAxisSize(region);
		final int externalIOSize = calculateExternalIOSize(region);
		final int[] scanDimensions = info.getOverallShape();
		getDataStorage().setupMultiDimensionalData(regionName, VGScientaAnalyser.IMAGE, scanDimensions, detector, new int[] {angleAxisSize, energyAxisSize}, Double.class);
		getDataStorage().setupMultiDimensionalData(regionName, VGScientaAnalyser.SPECTRUM, scanDimensions, detector, new int[] {energyAxisSize}, Double.class);
		getDataStorage().setupMultiDimensionalData(regionName, VGScientaAnalyser.EXTERNAL_IO, scanDimensions, detector, new int[] {externalIOSize}, Double.class);
		getDataStorage().setupMultiDimensionalData(regionName, VGScientaAnalyser.INTENSITY, scanDimensions, detector, new int[] {1}, Double.class);
		getDataStorage().setupMultiDimensionalData(regionName, VGScientaAnalyser.EXCITATION_ENERGY, scanDimensions, detector, new int[] {1}, Double.class, VGScientaAnalyser.ELECTRON_VOLTS);
		getDataStorage().setupMultiDimensionalData(regionName, VGScientaAnalyser.REGION_VALID, scanDimensions, detector, new int[] {1}, Boolean.class);

		final  String angleUnits = region.getLensMode().equals("Transmission") ? VGScientaAnalyser.PIXEL : VGScientaAnalyser.ANGLES;
		AnalyserRegionDatasetUtil.createOneDimensionalStructure(
			VGScientaAnalyser.ANGLES, detector, new int[] {angleAxisSize}, Double.class, angleUnits
		);
		AnalyserRegionDatasetUtil.createOneDimensionalStructure(
			VGScientaAnalyser.ENERGIES, detector, new int[] {energyAxisSize}, Double.class, VGScientaAnalyser.ELECTRON_VOLTS
		);
		//Step time and total steps give slightly different results when received from the detector compared to region
		//Therefore we will populate this data later with accurate data
		AnalyserRegionDatasetUtil.createOneDimensionalStructure(
			VGScientaAnalyser.TOTAL_STEPS, detector, AnalyserRegionDatasetUtil.SCALAR_SHAPE, Integer.class
		);
		AnalyserRegionDatasetUtil.createOneDimensionalStructure(
			VGScientaAnalyser.TOTAL_TIME, detector, AnalyserRegionDatasetUtil.SCALAR_SHAPE, Double.class, "s"
		);
		AnalyserRegionDatasetUtil.createOneDimensionalStructure(
			VGScientaAnalyser.STEP_TIME, detector, AnalyserRegionDatasetUtil.SCALAR_SHAPE, Double.class, "s"
		);
		AnalyserRegionDatasetUtil.createOneDimensionalStructure(
			REGION_STATUS, detector, AnalyserRegionDatasetUtil.SCALAR_SHAPE, String.class
		);
		return new NexusObjectWrapper<>(region.getName(), detector);
	}

	@Override
	protected void setupAxisFields(final SESRegion region, NexusObjectWrapper<NXdetector> nexusWrapper, int scanRank) {
		//Set up axes as [scannables, ..., angles, energies]
		final int angleAxisIndex = scanRank;
		final int energyAxisIndex = angleAxisIndex +1;
		final int[] energyDimensionalMappings = AnalyserRegionDatasetUtil.calculateAxisDimensionMappings(scanRank, energyAxisIndex);
		nexusWrapper.setPrimaryDataFieldName(VGScientaAnalyser.IMAGE);
		nexusWrapper.addAxisDataFieldForPrimaryDataField(VGScientaAnalyser.ANGLES, VGScientaAnalyser.IMAGE, angleAxisIndex, angleAxisIndex);
		nexusWrapper.addAxisDataFieldForPrimaryDataField(VGScientaAnalyser.SPECTRUM, VGScientaAnalyser.IMAGE, energyAxisIndex, energyDimensionalMappings);
		nexusWrapper.addAxisDataFieldForPrimaryDataField(VGScientaAnalyser.EXTERNAL_IO, VGScientaAnalyser.IMAGE, energyAxisIndex, energyDimensionalMappings);
		nexusWrapper.addAxisDataFieldForPrimaryDataField(VGScientaAnalyser.ENERGIES, VGScientaAnalyser.IMAGE, energyAxisIndex, energyAxisIndex);
		nexusWrapper.addAxisDataFieldName(VGScientaAnalyser.EXCITATION_ENERGY);
		nexusWrapper.addAxisDataFieldName(VGScientaAnalyser.INTENSITY);
	}

	@Override
	protected int calculateEnergyAxisSize(SESRegion region) {
		if (region.isAcquisitionModeFixed()) {
			return region.getLastXChannel() - region.getFirstXChannel() + 1;
		} else {
			final double energyStep = region.getEnergyStep() / 1000.;
			final double rawValue = (region.getHighEnergy() - region.getLowEnergy()) / energyStep;
			return (int) roundHalfDown(rawValue) + 1;
		}
	}

	/**
	* Function to round number to nearest integer with exception of .5 being round down rather than up.
	*/
	private double roundHalfDown(double d) {
		double i = Math.floor(d); // integer portion
		double f = d - i; // fractional portion
		// round integer portion based on fractional portion
		return f <= 0.5 ? i : i + 1D;
	}

	@Override
	protected int calculateAngleAxisSize(SESRegion region) {
		return region.getSlices();
	}

	private int calculateExternalIOSize(SESRegion region) {
		return region.isAcquisitionModeFixed() ? 1 : calculateEnergyAxisSize(region);
	}

	@Override
	public void prepareForCollection(int numberImagesPerCollection, ScanInformation scanInfo) throws Exception {
		super.prepareForCollection(numberImagesPerCollection, scanInfo);
		if(getEnabledRegionNames().isEmpty()) {
			if(sequence == null) {
				throw new DeviceException("No sequence file is loaded, sequence is null.");
			}
			throw new DeviceException("There are no enabled regions in the sequence.");
		}
		scanDataPoint = 0;
		totalNumberOfPoints = scanInfo.getNumberOfPoints();
		print("Found the following regions enabled:");
		for (SESRegion region : getEnabledValidRegions()) {
			print(" - " + region.getName());
		}
	}

	@Override
	public void prepareForCollection(double collectionTime, int numberImagesPerCollection, ScanInformation scanInfo) throws Exception {
		prepareForCollection(numberImagesPerCollection, scanInfo);
	}

	public List<String> validateRegions() throws DeviceException {
		logger.debug("Validating regions at scanDataPoint = {}...", scanDataPoint + 1);
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
		for (SESRegion region : getEnabledRegions()) {
			final Scannable scannable = region.getExcitationEnergySourceScannable();
			if (!regionValidator.isValidRegion(region, elementSet, (double) scannable.getPosition())) {
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
			final String errorMessage = "DETECTED INVALID REGIONS";
			final String skipMessage = "Skipping the following regions at scanpoint = " + Integer.toString(scanDataPoint + 1) + ": ";
			print("\n" + "*".repeat(skipMessage.length()));
			print(errorMessage);
			print(skipMessage);
			invalidRegions.stream().forEach(r -> print(" - " + r.getName()));
			print("*".repeat(skipMessage.length()) + "\n");
		}
		return invalidRegionNames;
	}

	@Override
	protected void beforeCollectData() throws DeviceException {
		super.beforeCollectData();
		scanDataPoint++;
		if (isScanFirstRegion() && isScanFirstScanDataPoint()) {
			try {
				for (SESRegion region : getEnabledRegions()) {
					updateRegionFileStatus(region, RegionFileStatus.QUEUED);
				}
			}
			catch (DatasetException e) {
				logger.warn("Unable to update initial region values", e);
			}
		}
		updateScriptController(new ScanPointStartEvent(scanDataPoint));
	}

	@Override
	protected void handleCollectDataInterrupted() throws DeviceException {
		getAnalyser().stop();
		updateScriptController(new RegionStatusEvent(getCurrentRegion().getRegionId(), SESRegion.Status.ABORTED));
		updateAllRegionStatusThatDidNotReachMaxIterations(RegionFileStatus.ABORTED);
	}

	@Override
	protected void handleCleanupAfterCollectData() {
		if (InterfaceProvider.getCurrentScanController().isFinishEarlyRequested()) {
			logger.warn("Finish early detected, updating region data with new status.");
			updateAllRegionStatusThatDidNotReachMaxIterations(RegionFileStatus.COMPLETED_EARLY);
		}
	}

	@Override
	protected void regionCollectData(final SESRegion region) throws Exception {
		final boolean regionValid = isRegionValid(region);
		updateRegionFileStatus(region, RegionFileStatus.RUNNING);
		// Update GUI to let it know region has changed
		updateScriptController(new RegionChangeEvent(region.getRegionId(), region.getName()));
		if (regionValid) {
			updateScriptController(new RegionStatusEvent(region.getRegionId(), SESRegion.Status.RUNNING));
			getAnalyser().configureWithNewRegion(region);
			//open/close fast shutter according to beam used
			for (Map.Entry<Scannable, Scannable> entry : getEnergySourceToShutterMap().entrySet()) {
				final Scannable energySource = entry.getKey();
				final Scannable shutter = entry.getValue();
				final ShutterPosition shutterPos = energySource == region.getExcitationEnergySourceScannable() ? ShutterPosition.OUT : ShutterPosition.IN;
				shutter.asynchronousMoveTo(shutterPos);
			}
			getAnalyser().collectData();
			getAnalyser().waitWhileBusy();
		}
		else {
			logger.warn("Skipping data collection for region {} as it is invalid. Writing blank data.", region.getName());
		}
	}

	@Override
	protected double regionSaveData(final SESRegion region) throws Exception {
		final String regionName = region.getName();
		logger.info("Saving data for region {}", regionName);
		final boolean isRegionValid = isRegionValid(region);

		final int calculatedEnergySize = calculateEnergyAxisSize(region);
		final int calculatedAngleSize = calculateAngleAxisSize(region);
		final int calculatedExternalIOSize = calculateExternalIOSize(region);

		final double excitationEnergy = isRegionValid ? getAnalyser().getExcitationEnergy() : (double) region.getExcitationEnergySourceScannable().getPosition();

		final double intensity = isRegionValid ? getAnalyser().getTotalIntensity() : 0;

		final double[] angleAxis  = isRegionValid ? getAnalyser().getAngleAxis() : new double[calculatedAngleSize];
		final double[] energyAxis = isRegionValid ? getAnalyser().getEnergyAxis(): new double[calculatedEnergySize];
		if (isRegionValid) formatEnergyAxis(energyAxis, excitationEnergy, region.isEnergyModeBinding());

		final int imageSize = energyAxis.length  * angleAxis.length;
		final double[] image = isRegionValid ? getAnalyser().getImage(imageSize) : new double[imageSize];
		final double[] spectrum = isRegionValid ? getAnalyser().getSpectrum() : energyAxis;
		final double[] externalIO = isRegionValid ? getAnalyser().getExternalIODataFormatted() : new double[calculatedExternalIOSize];

		final double stepTime = getAnalyser().getStepTime();
		final double totalSteps = getAnalyser().getTotalSteps();

		final int[] calculatedExternalIOAxes = new int[] {calculatedExternalIOSize};
		final int[] calculatedEnergyAxes = new int[] {calculatedEnergySize};
		final int[] calculatedAngleEnergyAxes = new int[] {calculatedAngleSize, calculatedEnergySize};
		final int[] analyserExternalIOAxes = new int[] {externalIO.length};
		final int[] analyserEnergyAxes = new int[] {energyAxis.length};
		final int[] analyserAngleEnergyAxes = new int[] {angleAxis.length, energyAxis.length};

		if (calculatedEnergySize != energyAxis.length) {
			logger.warn("calculatedEnergySize ({}) != energyAxis.length ({}). Will have to adjust detector data to fit.", calculatedEnergySize, energyAxis.length);
		}
		getDataStorage().writeNewPosition(regionName, VGScientaAnalyser.SPECTRUM, checkAxesMatch(spectrum ,analyserEnergyAxes, calculatedEnergyAxes));
		getDataStorage().writeNewPosition(regionName, VGScientaAnalyser.IMAGE, checkAxesMatch(image ,analyserAngleEnergyAxes, calculatedAngleEnergyAxes));
		getDataStorage().writeNewPosition(regionName, VGScientaAnalyser.EXTERNAL_IO, checkAxesMatch(externalIO ,analyserExternalIOAxes, calculatedExternalIOAxes));
		getDataStorage().writeNewPosition(regionName, VGScientaAnalyser.EXCITATION_ENERGY, new double[] {excitationEnergy});
		getDataStorage().writeNewPosition(regionName, VGScientaAnalyser.INTENSITY, new double[] {intensity});
		getDataStorage().writeNewPosition(regionName, VGScientaAnalyser.REGION_VALID, !getInvalidRegionNames().contains(regionName));
		if(isRegionValid) {
			getDataStorage().overridePosition(regionName,  VGScientaAnalyser.ANGLES, angleAxis);
			getDataStorage().overridePosition(regionName, VGScientaAnalyser.ENERGIES, checkAxesMatch(energyAxis, analyserEnergyAxes, calculatedEnergyAxes));
			//Write over as analyser gives slightly different results to region object
			final double totalTime = stepTime * totalSteps;
			getDataStorage().overridePosition(regionName, VGScientaAnalyser.STEP_TIME, stepTime);
			getDataStorage().overridePosition(regionName, VGScientaAnalyser.TOTAL_STEPS, totalSteps);
			getDataStorage().overridePosition(regionName, VGScientaAnalyser.TOTAL_TIME, totalTime);
		}
		else if(isScanFirstScanDataPoint()){
			//If region is invalid and this is the first scanDataPoint, save this instead
			getDataStorage().overridePosition(regionName, VGScientaAnalyser.STEP_TIME, region.getStepTime());
			getDataStorage().overridePosition(regionName, VGScientaAnalyser.TOTAL_STEPS, region.getTotalSteps());
			getDataStorage().overridePosition(regionName, VGScientaAnalyser.TOTAL_TIME, region.getTotalTime());
		}
		final boolean isLastScanDataPoint = scanDataPoint == totalNumberOfPoints;
		updateRegionFileStatus(region, isLastScanDataPoint ? RegionFileStatus.COMPLETED : RegionFileStatus.QUEUED);

		//Send an update completed message to client so that it gets added to the regionscompleted calculation.
		updateScriptController(new RegionStatusEvent(region.getRegionId(), SESRegion.Status.COMPLETED));

		if (!isRegionValid) {
			Thread.sleep(100); //Needed as otherwise the client seems to disregard the second message due to timing issue.
			updateScriptController(new RegionStatusEvent(region.getRegionId(), SESRegion.Status.INVALID));
		}
		return intensity;
	}

	//I09-600 - There are some edge cases where the calculated detector dimensions are different by 1 point (probably
	//due to some internal rounding). To prevent entire scan / script from crashing, pad out data or slice one point off
	//until we can find a more accurate way to calculate the size from epics.
	private Dataset checkAxesMatch(double[] data, int[] axes, int[] calculatedAxes) {
		Dataset dataSet = NexusUtils.createFromObject(data, null);
		final int axesSize = Arrays.stream(axes).sum();
		final int calculatedAxesSize = Arrays.stream(calculatedAxes).sum();
		if (axesSize == calculatedAxesSize) return dataSet;
		dataSet = dataSet.reshape(axes);
		return axesSize < calculatedAxesSize ?
			FFT.extendWithPad(dataSet, calculatedAxes, Double.NaN) :
			dataSet.getSlice(new SliceND(calculatedAxes));
	}

	private void formatEnergyAxis(double[] energyAxis, double excitationEnergy, boolean isBindingEnergy) {
		if (isBindingEnergy) {
			for (int e = 0; e < energyAxis.length; e++) energyAxis[e] = excitationEnergy - e;
		}
	}

	private void updateAllRegionStatusThatDidNotReachMaxIterations(RegionFileStatus status) {
		logger.info("Updating all regions that did not fully complete with status {}", status);
		if(getEnabledRegionNames().isEmpty()) {
			return;
		}
		int startingIndex = scanDataPoint == totalNumberOfPoints ? getRegionIndex() : 0;
		List<SESRegion> regions = getEnabledRegions();
		try {
			for (int i = startingIndex ; i < regions.size(); i++) {
				SESRegion region = regions.get(i);
				updateRegionFileStatus(region, status);
			}
		} catch (DatasetException e) {
			logger.error("Unable to update region status to {}", status, e);
		}
	}

	private void updateRegionFileStatus(SESRegion region, RegionFileStatus status) throws DatasetException {
		logger.debug("updating region {} to status {}", region.getName(), status);
		if (!getDataStorage().getDetectorMap().isEmpty()) {
			getDataStorage().overridePosition(region.getName(), REGION_STATUS, status.toString());
		}
		else {
			logger.error("Unable to update region file status as detector data is empty.");
		}
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
		//Not needed
	}

	@Override
	public void atCommandFailure() throws Exception {
		//Not needed
	}

	@Override
	public void stop() throws DeviceException, InterruptedException {
		try {
			getAnalyser().stop();
		}
		catch(DeviceException e) {
			logger.error("An error occured on stop ", e);
		}
		finally {
			super.stop();
		}
		for (Scannable shutter : getEnergySourceToShutterMap().values()) {
			shutter.asynchronousMoveTo(ShutterPosition.IN);
		}
		final SESRegion currentRegion = getCurrentRegion();
		if (currentRegion != null) updateScriptController(new RegionStatusEvent(currentRegion.getRegionId(), SESRegion.Status.ABORTED));
		updateAllRegionStatusThatDidNotReachMaxIterations(RegionFileStatus.ABORTED);
	}

	@Override
	public double getAcquireTime() throws DeviceException {
		return getEnabledRegions().stream().mapToDouble(SESRegion::getTotalTime).sum();
	}

	@Override
	public double getAcquirePeriod() throws DeviceException {
		return getAcquireTime();
	}

	@Override
	public void configureAcquireAndPeriodTimes(double collectionTime) throws Exception {
		//Deprecated, same as prepareForCollection
	}

	@Override
	public List<SESRegion> getEnabledRegions() {
		if (sequence != null) {
			return sequence.getRegions().stream().filter(SESRegion::isEnabled).toList();
		}
		return Collections.emptyList();
	}

	@Override
	public List<String> getEnabledRegionNames() {
		return getEnabledRegions().stream().map(SESRegion::getName).toList();
	}

	public List<SESRegion> getEnabledValidRegions() {
		List<SESRegion> regions = new ArrayList<>(getEnabledRegions());
		regions.removeAll(invalidRegions);
		return regions;
	}

	public List<String> getEnabledValidRegionNames() {
		return getEnabledValidRegions().stream().map(SESRegion::getName).toList();
	}

	private List<String> getInvalidRegionNames() {
		return invalidRegions.stream().map(SESRegion::getName).toList();
	}

	private void print(String message) {
		getTerminalPrinter().print(message);
	}

	@Override
	public SESRegion getCurrentRegion() {
		final List<SESRegion> regions = getEnabledRegions();
		if (!getEnabledRegions().isEmpty()) {
			return regions.get(getRegionIndex());
		}
		return null;
	}

	private boolean isScanFirstScanDataPoint() {
		return scanDataPoint == 1;
	}

	public Map<Scannable, Scannable> getEnergySourceToShutterMap() {
		return energySourceToShutterMap;
	}

	public void setEnergySourceToShutterMap(Map<Scannable, Scannable> energySourceToShutterMap) {
		this.energySourceToShutterMap = energySourceToShutterMap;
	}

	public VGScientaAnalyser getAnalyser() {
		return analyser;
	}

	public void setAnalyser(VGScientaAnalyser analyser) {
		this.analyser = analyser;
	}

	public IRegionValidator getRegionValidator() {
		return regionValidator;
	}

	public void setRegionValidator(IRegionValidator regionValidator) {
		this.regionValidator = regionValidator;
	}

	public void setSequence(SESSequence sequence) {
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
	protected final boolean isRegionValid(SESRegion region) {
		return !getInvalidRegionNames().contains(region.getName());
	}

	@Override
	protected NexusObjectWrapper<NXdetector> initialiseAdditionalNXdetectorData(NXdetector detector, NexusScanInfo info) throws NexusException {
		//Not needed
		return null;
	}
}
