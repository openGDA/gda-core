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

package uk.ac.diamond.daq.devices.specs.phoibos;

import static gda.jython.InterfaceProvider.getTerminalPrinter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.dawnsci.nexus.NXdetector;
import org.eclipse.dawnsci.nexus.NexusConstants;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusScanInfo;
import org.eclipse.dawnsci.nexus.builder.NexusObjectWrapper;
import org.opengda.detector.electronanalyser.nxdetector.AbstractWriteRegionsImmediatelyCollectionStrategy;
import org.opengda.detector.electronanalyser.utils.AnalyserRegionDatasetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.observable.IObserver;
import gda.observable.ObservableComponent;
import gda.scan.ScanInformation;
import uk.ac.diamond.daq.devices.specs.phoibos.api.SpecsIterationNumberUpdate;
import uk.ac.diamond.daq.devices.specs.phoibos.api.SpecsPhoibosRegion;
import uk.ac.diamond.daq.devices.specs.phoibos.api.SpecsPhoibosSequence;
import uk.ac.diamond.daq.devices.specs.phoibos.api.SpecsRegionStartUpdate;

public class SpecsPhoibosSolsticeCollectionStrategy extends AbstractWriteRegionsImmediatelyCollectionStrategy<SpecsPhoibosRegion> implements ISpecsPhoibosCollectionStrategy{
	private static final Logger logger = LoggerFactory.getLogger(SpecsPhoibosSolsticeCollectionStrategy.class);

	private final ObservableComponent observableComponent = new ObservableComponent();

	private SpecsPhoibosSolsticeAnalyser analyser;
	private SpecsPhoibosSequence sequence;
	private SpecsPhoibosRegion previousRegion = null;

	private double[] summedSpectrum;
	private double[] summedImage;

	private double cachedPhotonEnergy;
	private boolean safeStateAfterScan;
	private double currentRegionTotalIntensity;

	private boolean stopAfterCurrentIteration = false;

	private int currentIteration; // Iterations handled manually here


	@Override
	protected void setStatus(int status) {
		super.setStatus(status);
		notifyListeners(status);
	}

	public void setSafeStateAfterScan(boolean safeStateAfterScan) {
		this.safeStateAfterScan = safeStateAfterScan;
	}

	@Override
	public void atCommandFailure() throws Exception {
		handleCollectDataInterrupted();
	}

	private boolean isUsingSequence() {
		if (sequence == null) return false;
		return sequence.getEnabledRegions().stream().count()!=0;
	}

	@Override
	public void prepareForCollection(double collectionTime, int numberImagesPerCollection, ScanInformation scanInfo)
			throws Exception {
		// Call through to the other prepareForCollection going to ignore the passed in values anyway.
		prepareForCollection(numberImagesPerCollection, scanInfo);
	}

	@Override
	public void prepareForCollection(int numberImagesPerCollection, ScanInformation scanInfo) throws Exception {
		logger.trace("prepareForCollection called");

		// Set previous region to null at the beginning of scan
		previousRegion = null;

		super.prepareForCollection(numberImagesPerCollection,scanInfo);

		analyser.setSafeState(safeStateAfterScan);

		logger.trace("Finished prepareForCollection");
	}

	@Override
	public int getNumberImagesPerCollection(double collectionTime) {
		return getEnabledRegionNames().size();
	}

	@Override
	public String toString() {
		logger.trace("toString called");
		if (isUsingSequence()) {
			return "Sequence mode";
		}
		return "Single region mode";
	}

	public SpecsPhoibosSolsticeAnalyser getAnalyser() {
		return analyser;
	}

	public void setAnalyser(SpecsPhoibosSolsticeAnalyser analyser) {
		this.analyser = analyser;
	}

	@Override public void addIObserver(IObserver anIObserver) {
		observableComponent.addIObserver(anIObserver);
	}

	@Override public void deleteIObserver(IObserver anIObserver) {
		observableComponent.deleteIObserver(anIObserver);
	}

	@Override public void deleteIObservers() {
		observableComponent.deleteIObservers();
	}

	private void notifyListeners(Object evt) {
		observableComponent.notifyIObservers(this, evt);
	}

	@Override
	public void setGenerateCallbacks(boolean b) {
		// Noop
	}

	@Override
	public boolean isGenerateCallbacks() {
		return false;
	}

	@Override
	public boolean requiresAsynchronousPlugins() {
		return false;
	}

	private void print(String message) {
		getTerminalPrinter().print(message);
	}

	@Override
	public double getAcquireTime() throws Exception {
		return getEnabledRegions().stream().mapToLong(SpecsPhoibosRegion::getEstimatedTimeInMs).sum()/1000;
	}

	@Override
	public double getAcquirePeriod() throws Exception {
		return analyser.getController().getExposureTime();
	}

	@Override
	public void configureAcquireAndPeriodTimes(double collectionTime) throws Exception {
		logger.debug("configureAcquireAndPeriodTimes(double)");
	}

	@Override
	protected NexusObjectWrapper<NXdetector> initialiseNXdetectorRegion(SpecsPhoibosRegion region, NXdetector detector, NexusScanInfo info) throws NexusException {
		final String regionName = region.getName();

		detector.setField(SpecsPhoibosSolsticeAnalyser.REGION_NAME, regionName);
		detector.setField(SpecsPhoibosSolsticeAnalyser.LENS_MODE_STR, region.getLensMode());
		detector.setField(SpecsPhoibosSolsticeAnalyser.ACQUISITION_MODE_STR, region.getAcquisitionMode());
		detector.setField(SpecsPhoibosSolsticeAnalyser.PSU_MODE, region.getPsuMode());
		detector.setField(SpecsPhoibosSolsticeAnalyser.ENERGY_MODE_STR, region.isBindingEnergy()? "binding_energy":"kinetic_energy");
		detector.setField(SpecsPhoibosSolsticeAnalyser.VALUES, region.getValues());
		detector.setField(SpecsPhoibosSolsticeAnalyser.NUMBER_OF_SLICES, region.getSlices());
		detector.setField(SpecsPhoibosSolsticeAnalyser.LOW_ENERGY, region.getStartEnergy());
		detector.setAttribute(SpecsPhoibosSolsticeAnalyser.LOW_ENERGY, NexusConstants.UNITS, SpecsPhoibosSolsticeAnalyser.ELECTRON_VOLTS);
		detector.setField(SpecsPhoibosSolsticeAnalyser.HIGH_ENERGY, region.getEndEnergy());
		detector.setAttribute(SpecsPhoibosSolsticeAnalyser.HIGH_ENERGY, NexusConstants.UNITS, SpecsPhoibosSolsticeAnalyser.ELECTRON_VOLTS);
		detector.setField(SpecsPhoibosSolsticeAnalyser.FIXED_ENERGY, region.getCentreEnergy());
		detector.setAttribute(SpecsPhoibosSolsticeAnalyser.FIXED_ENERGY, NexusConstants.UNITS, SpecsPhoibosSolsticeAnalyser.ELECTRON_VOLTS);
		detector.setField(SpecsPhoibosSolsticeAnalyser.ENERGY_STEP, region.getStepEnergy());
		detector.setAttribute(SpecsPhoibosSolsticeAnalyser.ENERGY_STEP, NexusConstants.UNITS, SpecsPhoibosSolsticeAnalyser.ELECTRON_VOLTS);
		detector.setField(SpecsPhoibosSolsticeAnalyser.PASS_ENERGY, region.getPassEnergy());
		detector.setAttribute(SpecsPhoibosSolsticeAnalyser.PASS_ENERGY, NexusConstants.UNITS, SpecsPhoibosSolsticeAnalyser.ELECTRON_VOLTS);

		int energyAxisSize = (region.getAcquisitionMode().contains(SpecsPhoibosSolsticeAnalyser.SNAPSHOT)) ? getAnalyser().getSnapshotImageSizeX() : calculateEnergyAxisSize(region);
		int angleAxisSize = calculateAngleAxisSize(region);
		int[] scanDimensions = info.getOverallShape();

		getDataStorage().setupMultiDimensionalData(regionName, SpecsPhoibosSolsticeAnalyser.IMAGE, scanDimensions, detector, new int[] {angleAxisSize, energyAxisSize}, Double.class);
		getDataStorage().setupMultiDimensionalData(regionName, SpecsPhoibosSolsticeAnalyser.IMAGES, scanDimensions, detector, new int[] {region.getIterations(),angleAxisSize, energyAxisSize}, Double.class, null, 1);

		getDataStorage().setupMultiDimensionalData(regionName, SpecsPhoibosSolsticeAnalyser.SPECTRUM, scanDimensions, detector, new int[] {energyAxisSize}, Double.class);
		getDataStorage().setupMultiDimensionalData(regionName, SpecsPhoibosSolsticeAnalyser.SPECTRA, scanDimensions, detector, new int[] {region.getIterations(),energyAxisSize}, Double.class, null, 1);

		for (int i=0;i<region.getIterations();i++) {
			getDataStorage().setupMultiDimensionalData(regionName, String.join("_",SpecsPhoibosSolsticeAnalyser.IMAGE,String.valueOf(i+1)), scanDimensions, detector, new int[] {angleAxisSize, energyAxisSize}, Double.class);
			getDataStorage().setupMultiDimensionalData(regionName, String.join("_",SpecsPhoibosSolsticeAnalyser.SPECTRUM,String.valueOf(i+1)), scanDimensions, detector, new int[] {energyAxisSize}, Double.class);
		}

		getDataStorage().setupMultiDimensionalData(regionName, SpecsPhoibosSolsticeAnalyser.INTENSITY, scanDimensions, detector, new int[] {1}, Double.class);
		getDataStorage().setupMultiDimensionalData(regionName, SpecsPhoibosSolsticeAnalyser.EXCITATION_ENERGY, scanDimensions, detector, new int[] {1}, Double.class, SpecsPhoibosSolsticeAnalyser.ELECTRON_VOLTS);

		String angleUnits = region.getLensMode().equals("Transmission") ? SpecsPhoibosSolsticeAnalyser.PIXEL : SpecsPhoibosSolsticeAnalyser.ANGLES;
		AnalyserRegionDatasetUtil.createOneDimensionalStructure(SpecsPhoibosSolsticeAnalyser.ANGLES, detector, new int[] {angleAxisSize}, Double.class, angleUnits);

		if (region.isBindingEnergy()) {
			logger.debug("Setting up file structure for 1D binding energy and ND kinetic energy");
			getDataStorage().setupMultiDimensionalData(regionName, SpecsPhoibosSolsticeAnalyser.KINETIC_ENERGY, scanDimensions, detector, new int[] {energyAxisSize}, Double.class, SpecsPhoibosSolsticeAnalyser.ELECTRON_VOLTS);
			AnalyserRegionDatasetUtil.createOneDimensionalStructure(SpecsPhoibosSolsticeAnalyser.BINDING_ENERGY, detector, new int[] {energyAxisSize}, Double.class, SpecsPhoibosSolsticeAnalyser.ELECTRON_VOLTS);
		} else {
			logger.debug("Setting up file structure for 1D kinetic energy and ND binding energy");
			getDataStorage().setupMultiDimensionalData(regionName, SpecsPhoibosSolsticeAnalyser.BINDING_ENERGY, scanDimensions, detector, new int[] {energyAxisSize}, Double.class, SpecsPhoibosSolsticeAnalyser.ELECTRON_VOLTS);
			AnalyserRegionDatasetUtil.createOneDimensionalStructure(SpecsPhoibosSolsticeAnalyser.KINETIC_ENERGY, detector, new int[] {energyAxisSize}, Double.class, SpecsPhoibosSolsticeAnalyser.ELECTRON_VOLTS);
		}

		//Scalar datasets
		//Step time and total steps give slightly different results when received from the detector compared to region
		//Therefore we will populate this data later with accurate data
		AnalyserRegionDatasetUtil.createOneDimensionalStructure(SpecsPhoibosSolsticeAnalyser.TOTAL_STEPS, detector, AnalyserRegionDatasetUtil.SCALAR_SHAPE, Integer.class);
		AnalyserRegionDatasetUtil.createOneDimensionalStructure(SpecsPhoibosSolsticeAnalyser.TOTAL_TIME, detector, AnalyserRegionDatasetUtil.SCALAR_SHAPE, Double.class, "s");
		AnalyserRegionDatasetUtil.createOneDimensionalStructure(SpecsPhoibosSolsticeAnalyser.STEP_TIME, detector, AnalyserRegionDatasetUtil.SCALAR_SHAPE, Double.class, "s");

		AnalyserRegionDatasetUtil.createOneDimensionalStructure(SpecsPhoibosSolsticeAnalyser.NUMBER_OF_ITERATIONS, detector, AnalyserRegionDatasetUtil.SCALAR_SHAPE, Integer.class);

		return new NexusObjectWrapper<>(regionName, detector);
	}

	@Override
	protected NexusObjectWrapper<NXdetector> initialiseAdditionalNXdetectorData(NXdetector detector, NexusScanInfo info)
			throws NexusException {
		return null;
	}

	@Override
	protected void setupAxisFields(SpecsPhoibosRegion region, NexusObjectWrapper<NXdetector> nexusWrapper, int scanRank) {
		//Set up axes [scannables, ..., angles, energies]
		final int angleAxisIndex = scanRank;
		final int energyAxisIndex = angleAxisIndex +1;
		final int[] energyDimensionalMappings = AnalyserRegionDatasetUtil.calculateAxisDimensionMappings(scanRank, energyAxisIndex);

		// order of adding is important!
		nexusWrapper.setPrimaryDataFieldName(SpecsPhoibosSolsticeAnalyser.IMAGE);
		nexusWrapper.addAxisDataFieldForPrimaryDataField(SpecsPhoibosSolsticeAnalyser.ANGLES,SpecsPhoibosSolsticeAnalyser.IMAGE, angleAxisIndex, angleAxisIndex);
		// This seems to be wrong from method names - but it does add correct link nodes to /entry/region node
		// Also note that last set up axis data field will be the actual axis for primary data in DataVis
		nexusWrapper.addAxisDataFieldForPrimaryDataField(SpecsPhoibosSolsticeAnalyser.SPECTRUM, SpecsPhoibosSolsticeAnalyser.IMAGE, energyAxisIndex, energyDimensionalMappings);
		for (int i=0;i<region.getIterations();i++) {
			nexusWrapper.addAxisDataFieldForPrimaryDataField(String.join("_",SpecsPhoibosSolsticeAnalyser.SPECTRUM,String.valueOf(i+1)), SpecsPhoibosSolsticeAnalyser.IMAGE, energyAxisIndex, energyDimensionalMappings);
		}
		if (region.isBindingEnergy()) {
			nexusWrapper.addAxisDataFieldForPrimaryDataField(SpecsPhoibosSolsticeAnalyser.BINDING_ENERGY,SpecsPhoibosSolsticeAnalyser.IMAGE, energyAxisIndex, energyAxisIndex);
		} else {
			nexusWrapper.addAxisDataFieldForPrimaryDataField(SpecsPhoibosSolsticeAnalyser.KINETIC_ENERGY,SpecsPhoibosSolsticeAnalyser.IMAGE, energyAxisIndex, energyAxisIndex);
		}
		nexusWrapper.addAxisDataFieldName(SpecsPhoibosSolsticeAnalyser.EXCITATION_ENERGY);
		nexusWrapper.addAxisDataFieldName(SpecsPhoibosSolsticeAnalyser.INTENSITY);
	}

	@Override
	protected int calculateAngleAxisSize(SpecsPhoibosRegion regionObj) {
		return regionObj.getSlices();
	}

	@Override
	protected void handleCollectDataInterrupted() throws DeviceException {
		getAnalyser().stopAcquiring();
		getAnalyser().setSafeState(safeStateAfterScan);
	}

	@Override
	protected void handleCleanupAfterCollectData() {
		getAnalyser().stopAcquiring();
		getAnalyser().setSafeState(safeStateAfterScan);
	}

	@Override
	protected void regionCollectData(SpecsPhoibosRegion currentRegion) throws Exception {
		// Added here - otherwise Epics refuse to change slices for next region when TEST-SPECS-01:StatusMessage_RBV is "Waiting for the acquire command"
		getAnalyser().getController().validateScanConfiguration();
		currentRegionTotalIntensity = 0;

		final double currentPhotonEnergy = (double)analyser.getPhotonEnergyProvider().getPosition();
		final boolean photonEnergyChanged = cachedPhotonEnergy != currentPhotonEnergy;

		// Compare former and current regions to skip setting analyser in case they match
		// Always set region when the scan command is incrementing photon energy
		if (!currentRegion.equals(previousRegion) || photonEnergyChanged) {
			getAnalyser().setRegion(currentRegion);
		} else {
			logger.debug("Same region detected as previous one: skip setting analyser region");
		}
		// Copy current region to the previous region
		previousRegion = currentRegion;

		StringBuilder positionInSequence = new StringBuilder();
		positionInSequence.append(getEnabledRegions().indexOf(currentRegion) + 1).append(" of ").append(getEnabledRegions().size());
		getAnalyser().notifyIObservers(this, new SpecsRegionStartUpdate(currentRegion.getIterations(), currentRegion.getName(), positionInSequence.toString()));

		for (currentIteration = 0; currentIteration < currentRegion.getIterations(); currentIteration++) {
			//update current iteration on livedata dispatcher
			getAnalyser().notifyIObservers(this, new SpecsIterationNumberUpdate(currentIteration));
			getAnalyser().startAcquiringWait();
			regionIterationSaveData(currentIteration,currentRegion);

			if (stopAfterCurrentIteration) {
				setStopAfterCurrentIteration(false);
				break;
			}
		}
	}

	private void regionIterationSaveData (int currentIteration, SpecsPhoibosRegion currentRegion)  throws Exception{
		final double[] angleAxis = getAnalyser().getYAxis();
		final double[] energyAxis = getAnalyser().getEnergyAxis();
		final double[] image = getAnalyser().getImage(angleAxis.length*energyAxis.length);
		final double[] spectrum = getAnalyser().getSpectrum();
		final String currentRegionName = currentRegion.getName();

		getDataStorage().writeNewPosition(currentRegionName, SpecsPhoibosSolsticeAnalyser.IMAGES, image);
		getDataStorage().writeNewPosition(currentRegionName, String.join("_",SpecsPhoibosSolsticeAnalyser.IMAGE,String.valueOf(currentIteration+1)), image);
		getDataStorage().writeNewPosition(currentRegionName, SpecsPhoibosSolsticeAnalyser.SPECTRA, spectrum);
		getDataStorage().writeNewPosition(currentRegionName, String.join("_",SpecsPhoibosSolsticeAnalyser.SPECTRUM,String.valueOf(currentIteration+1)), spectrum);

		if (currentIteration == 0) {
			summedSpectrum = spectrum;
			summedImage = image;
			currentRegionTotalIntensity = Arrays.stream(spectrum).sum();
			final double[] bindingEnergyAxis = getAnalyser().toBindingEnergy(energyAxis);
			final double excitationEnergy = getAnalyser().getCurrentPhotonEnergy();

			getDataStorage().overridePosition(currentRegionName, SpecsPhoibosSolsticeAnalyser.ANGLES, angleAxis);
			if (currentRegion.isBindingEnergy()) {
				getDataStorage().writeNewPosition(currentRegionName, SpecsPhoibosSolsticeAnalyser.KINETIC_ENERGY, energyAxis);
				getDataStorage().overridePosition(currentRegionName, SpecsPhoibosSolsticeAnalyser.BINDING_ENERGY, bindingEnergyAxis);
			} else {
				getDataStorage().overridePosition(currentRegionName, SpecsPhoibosSolsticeAnalyser.KINETIC_ENERGY, energyAxis);
				getDataStorage().writeNewPosition(currentRegionName, SpecsPhoibosSolsticeAnalyser.BINDING_ENERGY, bindingEnergyAxis);
			}

			// scannable can be photon energy so this may change at each scan point
			getDataStorage().writeNewPosition(currentRegionName, SpecsPhoibosSolsticeAnalyser.EXCITATION_ENERGY, new double[] {excitationEnergy});

			// save iteration summed data
			getDataStorage().writeNewPosition(currentRegionName, SpecsPhoibosSolsticeAnalyser.IMAGE, summedImage);
			getDataStorage().writeNewPosition(currentRegionName, SpecsPhoibosSolsticeAnalyser.SPECTRUM, summedSpectrum);
			getDataStorage().writeNewPosition(currentRegionName, SpecsPhoibosSolsticeAnalyser.INTENSITY, new double[] {currentRegionTotalIntensity});
		} else {
			for (int i=0;i<summedSpectrum.length;i++) {
				summedSpectrum[i]+=spectrum[i];
			}
			for (int i=0;i<summedImage.length;i++) {
				summedImage[i]+=image[i];
			}
			currentRegionTotalIntensity += Arrays.stream(spectrum).sum();

			getDataStorage().overridePosition(currentRegionName, SpecsPhoibosSolsticeAnalyser.IMAGE, summedImage);
			getDataStorage().overridePosition(currentRegionName, SpecsPhoibosSolsticeAnalyser.SPECTRUM, summedSpectrum);
			getDataStorage().overridePosition(currentRegionName, SpecsPhoibosSolsticeAnalyser.INTENSITY, new double[] {currentRegionTotalIntensity});
		}
	}

	@Override
	protected double regionSaveData(SpecsPhoibosRegion region) throws Exception {
		final String currentRegionName =  region.getName();
		final double stepTime = getAnalyser().getStepTime();
		final double totalSteps = getAnalyser().getTotalSteps();
		final double totalTime = stepTime * totalSteps;

		getDataStorage().overridePosition(currentRegionName, SpecsPhoibosSolsticeAnalyser.STEP_TIME, stepTime);
		getDataStorage().overridePosition(currentRegionName, SpecsPhoibosSolsticeAnalyser.TOTAL_STEPS, totalSteps);
		getDataStorage().overridePosition(currentRegionName, SpecsPhoibosSolsticeAnalyser.TOTAL_TIME, totalTime);
		//write only number of completed iterations
		getDataStorage().overridePosition(currentRegionName, SpecsPhoibosSolsticeAnalyser.NUMBER_OF_ITERATIONS, currentIteration+1);
		// Added here - otherwise Epics refuse to change slices for next region when TEST-SPECS-01:StatusMessage_RBV is "Waiting for the acquire command"
		getAnalyser().getController().validateScanConfiguration();

		logger.debug("Finished region: {} (Region {} of {})", currentRegionName, getEnabledRegionNames().indexOf(currentRegionName) + 1, getEnabledRegions().size());
		return currentRegionTotalIntensity;
	}

	@Override
	protected boolean isRegionValid(SpecsPhoibosRegion region) {
		// Not implemented
		return false;
	}

	@Override
	public SpecsPhoibosRegion getCurrentRegion() {
		// Not implemented
		return null;
	}

	@Override
	public List<SpecsPhoibosRegion> getEnabledRegions() {
		ArrayList<SpecsPhoibosRegion> regionsToAcquire;
		// Do the setup of the sequence so the collectData logic can be the same for single region and sequence mode
		if(isUsingSequence()) {
			logger.info("Configuring analyser for sequence mode");
			// Copy the sequence into the sequenceToAcquire
			regionsToAcquire = new ArrayList<>(sequence.getEnabledRegions());
			// Check there are actually regions
			if (regionsToAcquire.isEmpty()) {
				String msg = "There are no regions to be acquired";
				print(msg);
				logger.error(msg);
				throw new IllegalStateException(msg);
			}
		}
		else {
			logger.info("Configuring analyser for single region mode");
			regionsToAcquire = new ArrayList<>();
			// Get the current region from the analyser and set it to acquire
			regionsToAcquire.add(analyser.getRegion());
		}
	return regionsToAcquire;
	}

	@Override
	public List<String> getEnabledRegionNames() {
		return getEnabledRegions().stream().map(SpecsPhoibosRegion::getName).toList();
	}

	@Override
	public void setSequence(SpecsPhoibosSequence sequence) {
		this.sequence = new SpecsPhoibosSequence(sequence);
	}

	@Override
	public SpecsPhoibosSequence getSequence() {
		return sequence;
	}

	@Override
	public String getName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public boolean willRequireCallbacks() {
		// False because it only interacts with the CAM plugin
		return false;
	}

	@Override
	public void prepareForLine() throws Exception {
		// No-op
	}

	@Override
	public void completeLine() throws Exception {
		// No-op
	}

	@Override
	public void completeCollection() throws Exception {
		// No-op
	}

	/**
	 * Note - specific to SPECS analyser!
	 */
	@Override
	protected int calculateEnergyAxisSize(SpecsPhoibosRegion region) {
		return (int) Math.ceil((Math.abs(region.getEndEnergy() - region.getStartEnergy()) / region.getStepEnergy()) + 1);
	}

	public void setStopAfterCurrentIteration(boolean value) {
		stopAfterCurrentIteration = value;
	}
}
