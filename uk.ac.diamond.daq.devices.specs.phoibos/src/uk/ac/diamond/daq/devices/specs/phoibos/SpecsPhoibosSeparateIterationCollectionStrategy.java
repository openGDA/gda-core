/*-
 * Copyright © 2017 Diamond Light Source Ltd.
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

import gda.data.nexus.extractor.NexusGroupData;
import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.detector.NXDetectorData;
import gda.device.detector.nxdata.NXDetectorDataAppender;
import gda.observable.IObserver;
import gda.observable.ObservableComponent;
import gda.scan.ScanInformation;
import uk.ac.diamond.daq.devices.specs.phoibos.api.SpecsPhoibosRegion;
import uk.ac.diamond.daq.devices.specs.phoibos.api.SpecsPhoibosSequence;
import uk.ac.diamond.daq.util.logging.deprecation.DeprecationLogger;

/**
 * This is the collection strategy used to acquire data from SPECS Phoibos electron analysers
 *
 * Its purpose is to handle acquiring sequences of regions and writing them to NeXus files with useful meta-data.
 *
 * @author James Mudd
 */
public class SpecsPhoibosSeparateIterationCollectionStrategy implements ISpecsPhoibosCollectionStrategy {

	private static final DeprecationLogger logger = DeprecationLogger.getLogger(SpecsPhoibosSeparateIterationCollectionStrategy.class);

	private static final String REGION_OUTPUT_FORMAT = "%5.5g";

	private final ObservableComponent observableComponent = new ObservableComponent();

	// This is used to execute the data collection
	private final ExecutorService executorService = Executors.newSingleThreadExecutor();

	private SpecsPhoibosSequence sequence;
	private SpecsPhoibosAnalyserSeparateIterations analyser;
	private volatile int status = Detector.IDLE;
	private boolean safeStateAfterScan;

	// Variable to store previous region to compare with current region
	private SpecsPhoibosRegion previousRegion = null;

	private double cachedPhotonEnergy;

	// The future will return status of the detector
	private Future<Integer> runningAcquisition;

	// This queue allows the collection to get ahead of the file writing by making them asynchronous
	// Using a LinkedBlockingQueue to allow the queue to dynamically resize
	private final BlockingQueue<List<SpecsPhoibosCompletedRegionWithSeperateIterations>> regionsAwaitingWriting = new LinkedBlockingQueue<>();

	private List<SpecsPhoibosRegion> regionsToAcquire;

	@Override
	public void setSequence(SpecsPhoibosSequence sequence) {
		this.sequence = new SpecsPhoibosSequence(sequence);
	}

	@Override
	public SpecsPhoibosSequence getSequence() {
		return new SpecsPhoibosSequence(sequence);
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
		logger.trace("completeCollection called");
		// Clear running acquisition
		runningAcquisition = null;

		// update previous region
		previousRegion = null;

		// Set if the analyser HV will be switched off at the end of the scan
		analyser.setSafeState(safeStateAfterScan);

		logger.debug("Finished complete collection");
		setStatus(Detector.IDLE);
	}

	public void setSafeStateAfterScan(boolean safeStateAfterScan) {
		this.safeStateAfterScan = safeStateAfterScan;
	}

	@Override
	public void atCommandFailure() throws Exception {
		logger.error("Command failure. Stopping analyser");
		analyser.stopAcquiring();
		// Need to call complete collection to ensure cleanup is done
		completeCollection();
	}

	@Override
	public void stop() throws Exception {
		logger.info("Stopping analyser as requested");
		analyser.stopAcquiring();
		// Need to call complete collection to ensure cleanup is done
		completeCollection();
	}

	private boolean isUsingSequence() {
		if (sequence == null) return false;
		return sequence.getEnabledRegions().stream().count()!=0;
	}

	@Override
	public List<String> getInputStreamNames() {
		// If using a sequence return the region names else return the analyser name
		if (isUsingSequence()) {
			// There is a sequence so give the region names
			return sequence.getEnabledRegions().stream().map(SpecsPhoibosRegion::getName).toList();
		}
		// Use the name of the analyser default region name or last run region name
		return Arrays.asList(analyser.getRegion().getName());
	}

	@Override
	public List<String> getInputStreamFormats() {
		// Check if using a sequence
		if (isUsingSequence()) {
			// Return the number of REGION_OUTPUT_FORMAT's equal to number of regions
			return Collections.nCopies(sequence.getEnabledRegions().size(), REGION_OUTPUT_FORMAT);
		}
		// Else in single region mode so just return one REGION_OUTPUT_FORMAT
		return Arrays.asList(REGION_OUTPUT_FORMAT);
	}

	@Override
	public double getAcquireTime() throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getAcquirePeriod() throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void configureAcquireAndPeriodTimes(double collectionTime) throws Exception {
		logger.deprecatedMethod("configureAcquireAndPeriodTimes(double)");
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

		// Do the setup of the sequence so the collectData logic can be the same for single region and sequence mode
		if(isUsingSequence()) {
			logger.info("Configuring analyser for sequence mode");
			// Copy the sequence into the sequenceToAcquire
			regionsToAcquire = new ArrayList<>(sequence.getEnabledRegions());
			// Check there are actually regions
			if (regionsToAcquire.isEmpty()) {
				String msg = "There are no regions to be acquired";
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
		logger.debug("Setup to acquire {} regions", regionsToAcquire.size());

		// Switch off safe state while we are in a scan
		analyser.setSafeState(false);

		logger.trace("Finished prepareForCollection");
	}

	/**
	 * This should start the scan but not block. It should return quickly and the rest of the logic be handled elsewhere.
	 * <p>
	 * This is achieved by setting up an ExecutorService then submitting the acquisition job to execute it.
	 * <p>
	 * {@link #waitWhileBusy()} blocks by waiting on the future to return the final detector status.
	 */
	@Override
	public void collectData() throws Exception {
		logger.trace("collectData called");
		// Create a new array to hold this points completed regions
		final List<SpecsPhoibosCompletedRegionWithSeperateIterations> completedRegions = new ArrayList<>();

		double currentPhotonEnergy = (double)analyser.getPhotonEnergyProvider().getPosition();
		boolean photonEnergyChanged = cachedPhotonEnergy != currentPhotonEnergy;

		// Set the detector to busy
		setStatus(Detector.BUSY);

		logger.debug("Starting acquisition");

		runningAcquisition = executorService.submit(() -> {
			// Loop through all the regions
			for (SpecsPhoibosRegion region : regionsToAcquire) {

				logger.debug("Starting region: {} (Region {} of {})", region.getName(), regionsToAcquire.indexOf(region) + 1, regionsToAcquire.size());

				// Compare former and current regions to skip setting analyser in case they match
				// Always set region when the scan command is incrementing photon energy
				if (!region.equals(previousRegion) || photonEnergyChanged) {
					analyser.setRegion(region);
				} else {
					logger.debug("Same region detected as previous one: Setting analyser region skipped");
				}
				// Copy current region to the previous region
				previousRegion = region;

				// Blocks until the region is finished, scan is blocked by waitWhileStatusBusy()
				analyser.startAcquiringWait();
				// Check that the region completed correctly
				if (analyser.getDetectorStatus() != SpecsPhoibosStatus.IDLE) {
					return Detector.FAULT;
				}
				// Get the data back into a buffer
				completedRegions.add(analyser.getCurrentOrLastRegion());
				logger.debug("Finished region: {} (Region {} of {})", region.getName(), regionsToAcquire.indexOf(region) + 1, regionsToAcquire.size());
			}

			cachedPhotonEnergy = currentPhotonEnergy;

			// Add the completed sequence to the queue to be written
			regionsAwaitingWriting.add(completedRegions);
			// Update the status
			return Detector.IDLE;
		});
	}

	@Override
	public int getStatus() {
		return status;
	}

	@Override
	public void waitWhileBusy() throws DeviceException, InterruptedException, ExecutionException {
		logger.trace("waitWhileBusy called");
		// If no acquisition is running were not busy
		if (runningAcquisition == null) {
			return;
		}
		// Block while the acquisition is running and set the status when it returns
		status = runningAcquisition.get();
		setStatus(status);

		// Throw if status is not idle to abort the scan. Something went wrong with the analyser
		if (status != Detector.IDLE) {
			logger.error("After region completed detector was not idle, Status is: {}, Message is: {}", analyser.getDetectorStatus(), analyser.getStatusMessage());
			throw new DeviceException("Analyser was not idle, Status is: " + analyser.getDetectorStatus() + ", Message is: " + analyser.getStatusMessage());
		}
	}

	@Override
	public void setGenerateCallbacks(boolean b) {
		// TODO What is this?
		// No-op
	}

	@Override
	public boolean isGenerateCallbacks() {
		// TODO What is this?
		return false;
	}

	@Override
	public int getNumberImagesPerCollection(double collectionTime) {
		// This method doesn't make much sense here just return the number of regions
		logger.trace("getNumberImagesPerCollection called");
		if (isUsingSequence()) {
			return sequence.getEnabledRegions().size();
		}
		// Single region mode
		return 1;
	}

	@Override
	public boolean requiresAsynchronousPlugins() {
		// TODO What is this?
		return false;
	}

	@Override
	public List<NXDetectorDataAppender> read(int maxToRead) throws InterruptedException, DeviceException {
		// Get the points that are currently ready on the queue up to maxToRead (which is usually Integer.MAX_VALUE)
		final List<List<SpecsPhoibosCompletedRegionWithSeperateIterations>> completedPoints = new ArrayList<>();
		final int points = regionsAwaitingWriting.drainTo(completedPoints, maxToRead);
		logger.trace("Completed points about to be written: {}", points);

		// For all the completed points make an appender and return it
		List<NXDetectorDataAppender> appenders = new ArrayList<>();
		for (List<SpecsPhoibosCompletedRegionWithSeperateIterations> pointData : completedPoints) {
			// Always want to use a SpecsNXDetectorDataAppender
			appenders.add(new SpecsSeperateIterationNXDetectorDataAppender(pointData));
		}
		return appenders;
	}

	@Override
	public String toString() {
		logger.trace("toString called");
		if (isUsingSequence()) {
			return "Sequence mode";
		}
		return "Single region mode";
	}

	public SpecsPhoibosAnalyserSeparateIterations getAnalyser() {
		return analyser;
	}

	public void setAnalyser(SpecsPhoibosAnalyserSeparateIterations analyser) {
		this.analyser = analyser;
	}

	@Override
	public void addIObserver(IObserver anIObserver) {
		observableComponent.addIObserver(anIObserver);
	}

	@Override
	public void deleteIObserver(IObserver anIObserver) {
		observableComponent.deleteIObserver(anIObserver);
	}

	@Override
	public void deleteIObservers() {
		observableComponent.deleteIObservers();
	}

	private void notifyListeners(Object evt) {
		observableComponent.notifyIObservers(this, evt);
	}

	private void setStatus(int statusChange) {
		status = statusChange;
		notifyListeners(statusChange);
	}


	/**
	 * This class handles writing the analyser data to NeXus it also supplies the region totals for the command line feedback.
	 *
	 * One appender should be created for each scan point by calling the constructor with the list of regions for that point.
	 */
	private class SpecsSeperateIterationNXDetectorDataAppender implements NXDetectorDataAppender {

		// Each appender writes all the regions for one point
		private final List<SpecsPhoibosCompletedRegionWithSeperateIterations> regions;

		private SpecsSeperateIterationNXDetectorDataAppender(List<SpecsPhoibosCompletedRegionWithSeperateIterations> regions) {
			logger.trace("Created appender for region: {}", regions);
			this.regions = Collections.unmodifiableList(regions);
		}

		@Override
		public void appendTo(NXDetectorData data, String detectorName) throws DeviceException {
			logger.debug("Appending to NeXus file. data: {}, detectorName: {}", data, detectorName);

			// Loop through all the regions
			for (SpecsPhoibosCompletedRegionWithSeperateIterations region : regions) {
				// This is used to plot during the scan
				data.setPlottableValue(region.getName(), region.getSpectrumSum());

				// Write to the file
				writeRegionToNexus(region, data);
			}

			// Write the regions_list to allow script to parse the file easier
			writeRegionList(data, detectorName);
		}


		/**
		 * <p>
		 * This writes a region_list dataset to /entry1/instrument/analyser/region_list
		 * </p>
		 * <p>
		 * The idea is to make the NeXus file easier to parse with loading scripts see B07-167
		 * </p>
		 * @param data The data to write with
		 * @param detectorName Name of the detector (probably analyser)
		 */
		private void writeRegionList(NXDetectorData data, String detectorName) {
			String[] regionNames = regions.stream().
					map(SpecsPhoibosRegion::getName). // Get the names
					collect(Collectors.toList()). // Put into a List
					toArray(new String[regions.size()]); // Convert to an array
			NexusGroupData regionList = new NexusGroupData(NexusGroupData.MAX_TEXT_LENGTH, regionNames);
			regionList.isDetectorEntryData = true; // Make true so its copied to NXData
			data.addElement(detectorName, "region_list", regionList, null, false);
		}

		private void writeRegionToNexus(final SpecsPhoibosCompletedRegionWithSeperateIterations regionCompleted, final NXDetectorData data) {
			logger.trace("Writing region to NeXus: {}", regionCompleted);

			// If in single region mode the name should be the name of the detector
			final String regionName = regionCompleted.getName();

			data.addData(regionName, "images", NexusGroupData.createFromDataset(regionCompleted.getImagesDataset()));
			data.addData(regionName, "spectra", NexusGroupData.createFromDataset(regionCompleted.getSpectraDataset()));
			for (int i=0;i<regionCompleted.getSpectra().size();i++) {
				data.addData(regionName, String.format("spectrum_%d", i+1), new NexusGroupData(regionCompleted.getSpectra().get(i)));
			}

			data.addData(regionName, "spectrum", new NexusGroupData(regionCompleted.getSummedSpectrum()));
			data.addData(regionName, "image", new NexusGroupData(regionCompleted.getSummedImage()));

			// Write the sum as the detector name to allow processing to work
			data.addData(regionName, regionName, new NexusGroupData(regionCompleted.getSpectrumSum()));

			// Add the data axis
			NexusGroupData kineticEnergyScale = new NexusGroupData(regionCompleted.getKineticEnergyScale());
			kineticEnergyScale.isDetectorEntryData = true; // Make true so its copied to NXData
			data.addElement(regionName, "kinetic_energy", kineticEnergyScale, "eV", true);

			NexusGroupData bindingEnergyScale = new NexusGroupData(analyser.toBindingEnergy(regionCompleted.getKineticEnergyScale()));
			bindingEnergyScale.isDetectorEntryData = true; // Make true so its copied to NXData
			data.addElement(regionName, "binding_energy", bindingEnergyScale, "eV", true);

			NexusGroupData yScale = new NexusGroupData(regionCompleted.getYAxisScale());
			yScale.isDetectorEntryData = true; // Make true so its copied to NXData
			data.addElement(regionName, "y_scale", yScale, regionCompleted.getYAxisUnits(), false);

			// Add region metadata
			data.addElement(regionName, "acquisition_mode", new NexusGroupData(regionCompleted.getAcquisitionMode()), null, false); // No units so set null
			data.addElement(regionName, "lens_mode", new NexusGroupData(regionCompleted.getLensMode()), null, false); // No units so set null
			data.addElement(regionName, "iterations", new NexusGroupData(regionCompleted.getIterations()), null, false); // No units so set null
			data.addElement(regionName, "psu_mode", new NexusGroupData(regionCompleted.getPsuMode()), null, false); // No units so set null
			data.addElement(regionName, "count_time", new NexusGroupData(regionCompleted.getExposureTime()), "sec", false);
			data.addElement(regionName, "pass_energy", new NexusGroupData(regionCompleted.getPassEnergy()), "eV", false);
			data.addElement(regionName, "step_energy", new NexusGroupData(regionCompleted.getStepEnergy()), "eV", false);
			// Save the photon energy and work function used for KE <-> BE conversions.
			data.addElement(regionName, "photon_energy", new NexusGroupData(analyser.getCurrentPhotonEnergy()), "eV", true);
			data.addElement(regionName, "work_function", new NexusGroupData(analyser.getWorkFunction()), "eV", false);
			logger.debug("Finished writing to NeXus region: {}", regionName);
		}
	}

	public List<SpecsPhoibosRegion> getRegionsToAcquire() {
		return regionsToAcquire;
	}
}
