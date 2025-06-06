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
import org.opengda.detector.electronanalyser.utils.AnalyserRegionConstants;
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
public class VGScientaAnalyserCollectionStrategy extends AbstractWriteRegionsImmediatelyCollectionStrategy<SESRegion> {

	private static final Logger logger = LoggerFactory.getLogger(VGScientaAnalyserCollectionStrategy.class);
	private enum RegionFileStatus {QUEUED, RUNNING, COMPLETED, COMPLETED_EARLY, ABORTED}
	private enum ShutterPosition {IN, OUT;
		@Override public String toString() {
			return StringUtils.capitalize(super.toString().toLowerCase());
		}
	}

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
		detector.setField(AnalyserRegionConstants.REGION_NAME, regionName);
		detector.setField(AnalyserRegionConstants.LENS_MODE, region.getLensMode());
		detector.setField(AnalyserRegionConstants.ACQUISITION_MODE, region.getAcquisitionMode());
		detector.setField(AnalyserRegionConstants.DETECTOR_MODE, region.getDetectorMode());
		detector.setField(AnalyserRegionConstants.PASS_ENERGY, region.getPassEnergy());
		detector.setField(AnalyserRegionConstants.ENERGY_MODE, region.getEnergyMode());
		try {
			final double excitationEnergy = (region.isEnergyModeBinding() ? (double) sequence.getExcitationEnergySourceByRegion(region).getScannable().getPosition() : 0.0);
			final double lowEnergy   = excitationEnergy + region.getLowEnergy();
			final double highEnergy  = excitationEnergy + region.getHighEnergy();
			final double fixedEnergy = excitationEnergy + region.getFixEnergy();
			detector.setField(AnalyserRegionConstants.LOW_ENERGY, lowEnergy);
			detector.setAttribute(AnalyserRegionConstants.LOW_ENERGY, NexusConstants.UNITS, AnalyserRegionConstants.ELECTRON_VOLTS);
			detector.setField(AnalyserRegionConstants.HIGH_ENERGY, highEnergy);
			detector.setAttribute(AnalyserRegionConstants.HIGH_ENERGY, NexusConstants.UNITS, AnalyserRegionConstants.ELECTRON_VOLTS);
			detector.setField(AnalyserRegionConstants.FIXED_ENERGY, fixedEnergy);
			detector.setAttribute(AnalyserRegionConstants.FIXED_ENERGY, NexusConstants.UNITS, AnalyserRegionConstants.ELECTRON_VOLTS);
		} catch (DeviceException e) {
			throw new NexusException("Unable to calculate excitation energy", e);
		}
		detector.setField(AnalyserRegionConstants.DETECTOR_X_FROM, region.getFirstXChannel());
		detector.setField(AnalyserRegionConstants.DETECTOR_X_TO, region.getLastXChannel());
		detector.setField(AnalyserRegionConstants.DETECTOR_X_SIZE, region.getLastXChannel() - region.getFirstXChannel() + 1);
		detector.setField(AnalyserRegionConstants.DETECTOR_Y_FROM, region.getFirstYChannel());
		detector.setField(AnalyserRegionConstants.DETECTOR_Y_TO, region.getLastYChannel());
		detector.setField(AnalyserRegionConstants.DETECTOR_Y_SIZE, region.getLastYChannel() - region.getFirstYChannel() + 1);

		final double energyStep = region.getEnergyStep();
		final int numIterations = region.getIterations();
		detector.setField(AnalyserRegionConstants.ENERGY_STEP, energyStep);
		detector.setAttribute(AnalyserRegionConstants.ENERGY_STEP, NexusConstants.UNITS, AnalyserRegionConstants.ELECTRON_VOLTS);
		detector.setField(AnalyserRegionConstants.NUMBER_OF_ITERATIONS, numIterations);
		detector.setField(AnalyserRegionConstants.NUMBER_OF_SLICES, region.getSlices());

		final String excitationEnergySourceScannablename = sequence.getExcitationEnergySourceByRegion(region).getScannableName();
		detector.setField(AnalyserRegionConstants.EXCITATION_ENERGY_SOURCE, excitationEnergySourceScannablename);

		final int energyAxisSize = calculateEnergyAxisSize(region);
		final int angleAxisSize = calculateAngleAxisSize(region);
		final int externalIOSize = calculateExternalIOSize(region);
		final int[] scanDimensions = info.getOverallShape();
		getDataStorage().setupMultiDimensionalData(regionName, AnalyserRegionConstants.IMAGE_DATA, scanDimensions, detector, new int[] {angleAxisSize, energyAxisSize}, Double.class);
		getDataStorage().setupMultiDimensionalData(regionName, AnalyserRegionConstants.SPECTRUM_DATA, scanDimensions, detector, new int[] {energyAxisSize}, Double.class);
		getDataStorage().setupMultiDimensionalData(regionName, AnalyserRegionConstants.EXTERNAL_IO_DATA, scanDimensions, detector, new int[] {externalIOSize}, Double.class);
		getDataStorage().setupMultiDimensionalData(regionName, AnalyserRegionConstants.INTENSITY, scanDimensions, detector, new int[] {1}, Double.class);
		getDataStorage().setupMultiDimensionalData(regionName, AnalyserRegionConstants.EXCITATION_ENERGY, scanDimensions, detector, new int[] {1}, Double.class, AnalyserRegionConstants.ELECTRON_VOLTS);
		getDataStorage().setupMultiDimensionalData(regionName, AnalyserRegionConstants.REGION_VALID, scanDimensions, detector, new int[] {1}, Boolean.class);

		final  String angleUnits = region.getLensMode().equals("Transmission") ? AnalyserRegionConstants.PIXEL : AnalyserRegionConstants.ANGLES;
		AnalyserRegionDatasetUtil.createOneDimensionalStructure(
			AnalyserRegionConstants.ANGLES, detector, new int[] {angleAxisSize}, Double.class, angleUnits
		);
		AnalyserRegionDatasetUtil.createOneDimensionalStructure(
			AnalyserRegionConstants.ENERGIES, detector, new int[] {energyAxisSize}, Double.class, AnalyserRegionConstants.ELECTRON_VOLTS
		);
		//Step time and total steps give slightly different results when received from the detector compared to region
		//Therefore we will populate this data later with accurate data
		AnalyserRegionDatasetUtil.createOneDimensionalStructure(
			AnalyserRegionConstants.TOTAL_STEPS, detector, AnalyserRegionDatasetUtil.SCALAR_SHAPE, Integer.class
		);
		AnalyserRegionDatasetUtil.createOneDimensionalStructure(
			AnalyserRegionConstants.TOTAL_TIME, detector, AnalyserRegionDatasetUtil.SCALAR_SHAPE, Double.class, "s"
		);
		AnalyserRegionDatasetUtil.createOneDimensionalStructure(
			AnalyserRegionConstants.STEP_TIME, detector, AnalyserRegionDatasetUtil.SCALAR_SHAPE, Double.class, "s"
		);
		AnalyserRegionDatasetUtil.createOneDimensionalStructure(
				AnalyserRegionConstants.REGION_STATUS, detector, AnalyserRegionDatasetUtil.SCALAR_SHAPE, String.class
		);
		return new NexusObjectWrapper<>(region.getName(), detector);
	}

	@Override
	protected void setupAxisFields(final SESRegion region, NexusObjectWrapper<NXdetector> nexusWrapper, int scanRank) {
		//Set up axes as [scannables, ..., angles, energies]
		final int angleAxisIndex = scanRank;
		final int energyAxisIndex = angleAxisIndex +1;
		final int[] energyDimensionalMappings = AnalyserRegionDatasetUtil.calculateAxisDimensionMappings(scanRank, energyAxisIndex);
		nexusWrapper.setPrimaryDataFieldName(AnalyserRegionConstants.IMAGE_DATA);
		nexusWrapper.addAxisDataFieldForPrimaryDataField(AnalyserRegionConstants.ANGLES, AnalyserRegionConstants.IMAGE_DATA, angleAxisIndex, angleAxisIndex);
		nexusWrapper.addAxisDataFieldForPrimaryDataField(AnalyserRegionConstants.SPECTRUM_DATA, AnalyserRegionConstants.IMAGE_DATA, energyAxisIndex, energyDimensionalMappings);
		nexusWrapper.addAxisDataFieldForPrimaryDataField(AnalyserRegionConstants.EXTERNAL_IO_DATA, AnalyserRegionConstants.IMAGE_DATA, energyAxisIndex, energyDimensionalMappings);
		nexusWrapper.addAxisDataFieldForPrimaryDataField(AnalyserRegionConstants.ENERGIES, AnalyserRegionConstants.IMAGE_DATA, energyAxisIndex, energyAxisIndex);
		nexusWrapper.addAxisDataFieldName(AnalyserRegionConstants.EXCITATION_ENERGY);
		nexusWrapper.addAxisDataFieldName(AnalyserRegionConstants.INTENSITY);
	}

	@Override
	public int calculateEnergyAxisSize(SESRegion region) {
		if (region.isAcquisitionModeFixed()) {
			return region.getLastXChannel() - region.getFirstXChannel() + 1;
		} else {
			final double energyStep = region.getEnergyStep();
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
	public int calculateAngleAxisSize(SESRegion region) {
		return region.getSlices();
	}

	public int calculateExternalIOSize(SESRegion region) {
		return region.isAcquisitionModeFixed() ? 1 : calculateEnergyAxisSize(region);
	}

	@Override
	public void prepareForCollection(int numberImagesPerCollection, ScanInformation scanInfo) throws Exception {
		super.prepareForCollection(numberImagesPerCollection, scanInfo);
		scanDataPoint = 0;
		totalNumberOfPoints = scanInfo.getNumberOfPoints();

		if (sequence == null) return;
		if(getEnabledRegionNames().isEmpty()) {
			throw new DeviceException("There are no enabled regions in the sequence.");
		}
		print("Found the following regions enabled:");
		for (final SESRegion region : getEnabledValidRegions()) {
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
			return Collections.nCopies(getEnabledRegions().size(), "-");
		}
		String elementSet = "unknown";
		try {
			elementSet = getAnalyser().getPsuMode();
		}
		catch (Exception e) {
			logger.error("Cannot get element set value for region validation");
			return Collections.nCopies(getEnabledRegions().size(), "-");
		}

		List<String> invalidRegionNames = new ArrayList<>();
		invalidRegions = new ArrayList<>();
		for (SESRegion region : getEnabledRegions()) {
			final Scannable scannable = sequence.getExcitationEnergySourceByRegion(region).getScannable();
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
		if(sequence == null) {
			throw new DeviceException("No sequence file is loaded, sequence is null.");
		}
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
		closeAllShutters();
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
			final double beamEnergy = (double) sequence.getExcitationEnergySourceByRegion(region).getScannable().getPosition();
			getAnalyser().configureWithNewRegion(region, beamEnergy);
			//open/close fast shutter according to beam used
			for (Map.Entry<Scannable, Scannable> entry : getEnergySourceToShutterMap().entrySet()) {
				final Scannable energySource = entry.getKey();
				final Scannable shutter = entry.getValue();
				final ShutterPosition shutterPos = energySource == sequence.getExcitationEnergySourceByRegion(region).getScannable() ? ShutterPosition.OUT : ShutterPosition.IN;
				shutter.moveTo(shutterPos);
			}
			getAnalyser().collectData();
			getAnalyser().waitWhileBusy();
			closeAllShutters();
		} else {
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

		final double excitationEnergy = isRegionValid ? getAnalyser().getExcitationEnergy() : (double) sequence.getExcitationEnergySourceByRegion(region).getScannable().getPosition();
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
		getDataStorage().writeNewPosition(regionName, AnalyserRegionConstants.SPECTRUM_DATA, checkAxesMatch(spectrum ,analyserEnergyAxes, calculatedEnergyAxes));
		getDataStorage().writeNewPosition(regionName, AnalyserRegionConstants.IMAGE_DATA, checkAxesMatch(image ,analyserAngleEnergyAxes, calculatedAngleEnergyAxes));
		getDataStorage().writeNewPosition(regionName, AnalyserRegionConstants.EXTERNAL_IO_DATA, checkAxesMatch(externalIO ,analyserExternalIOAxes, calculatedExternalIOAxes));
		getDataStorage().writeNewPosition(regionName, AnalyserRegionConstants.EXCITATION_ENERGY, new double[] {excitationEnergy});
		getDataStorage().writeNewPosition(regionName, AnalyserRegionConstants.INTENSITY, new double[] {intensity});
		getDataStorage().writeNewPosition(regionName, AnalyserRegionConstants.REGION_VALID, !getInvalidRegionNames().contains(regionName));
		if(isRegionValid) {
			getDataStorage().overridePosition(regionName,  AnalyserRegionConstants.ANGLES, angleAxis);
			getDataStorage().overridePosition(regionName, AnalyserRegionConstants.ENERGIES, checkAxesMatch(energyAxis, analyserEnergyAxes, calculatedEnergyAxes));
			//Write over as analyser gives slightly different results to region object
			final double totalTime = stepTime * totalSteps;
			getDataStorage().overridePosition(regionName, AnalyserRegionConstants.STEP_TIME, stepTime);
			getDataStorage().overridePosition(regionName, AnalyserRegionConstants.TOTAL_STEPS, totalSteps);
			getDataStorage().overridePosition(regionName, AnalyserRegionConstants.TOTAL_TIME, totalTime);
		}
		else if(isScanFirstScanDataPoint()){
			//If region is invalid and this is the first scanDataPoint, save this instead
			getDataStorage().overridePosition(regionName, AnalyserRegionConstants.STEP_TIME, region.getStepTime());
			getDataStorage().overridePosition(regionName, AnalyserRegionConstants.TOTAL_STEPS, region.getTotalSteps());
			getDataStorage().overridePosition(regionName, AnalyserRegionConstants.TOTAL_TIME, region.getTotalTime());
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
			for (int i = 0; i < energyAxis.length; i++) energyAxis[i] = excitationEnergy - energyAxis[i];
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
			getDataStorage().overridePosition(region.getName(), AnalyserRegionConstants.REGION_STATUS, status.toString());
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
		super.stop();
		getAnalyser().stop();
		closeAllShutters();
		final SESRegion currentRegion = getCurrentRegion();
		if (currentRegion != null) updateScriptController(new RegionStatusEvent(currentRegion.getRegionId(), SESRegion.Status.ABORTED));
		updateAllRegionStatusThatDidNotReachMaxIterations(RegionFileStatus.ABORTED);
	}

	private void closeAllShutters() throws DeviceException {
		//I09-609 - Close shutter between regions to stop intensity protection being tripped.
		//Leave shutter open at scan end as requested by PBS
		if (getRegionIndex() >=  getEnabledRegions().size() -1) return;
		for (final Scannable shutter : getEnergySourceToShutterMap().values()) {
			shutter.moveTo(ShutterPosition.IN);
		}
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

	public SESSequence getSequence() {
		return this.sequence;
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
