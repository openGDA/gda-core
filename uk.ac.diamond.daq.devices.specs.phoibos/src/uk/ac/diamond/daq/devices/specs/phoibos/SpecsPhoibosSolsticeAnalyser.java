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

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.eclipse.dawnsci.nexus.NXdetector;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusScanInfo;
import org.eclipse.dawnsci.nexus.builder.NexusObjectWrapper;
import org.eclipse.january.DatasetException;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.SliceND;
import org.opengda.detector.electronanalyser.nxdetector.AbstractWriteRegionsImmediatelyNXDetector;
import org.opengda.detector.electronanalyser.utils.AnalyserRegionConstants;
import org.opengda.detector.electronanalyser.utils.AnalyserRegionDatasetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.data.scan.nexus.device.GDADeviceNexusConstants;
import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.EnumPositioner;
import gda.device.Scannable;
import gda.device.detector.areadetector.v17.ImageMode;
import gda.device.detector.nxdetector.NXCollectionStrategyPlugin;
import gda.factory.FactoryException;
import gda.jython.InterfaceProvider;
import uk.ac.diamond.daq.devices.specs.phoibos.api.ISpecsPhoibosAnalyser;
import uk.ac.diamond.daq.devices.specs.phoibos.api.SpecsPhoibosRegion;
import uk.ac.diamond.daq.devices.specs.phoibos.api.SpecsPhoibosRegionValidation;
import uk.ac.diamond.daq.devices.specs.phoibos.api.SpecsPhoibosSequence;
import uk.ac.diamond.daq.devices.specs.phoibos.api.SpecsPhoibosSequenceFileUpdate;
import uk.ac.diamond.daq.devices.specs.phoibos.api.SpecsPhoibosSequenceHelper;
import uk.ac.diamond.daq.devices.specs.phoibos.api.SpecsPhoibosSequenceValidation;
import uk.ac.gda.api.remoting.ServiceInterface;

@ServiceInterface(ISpecsPhoibosAnalyser.class)
public class SpecsPhoibosSolsticeAnalyser extends AbstractWriteRegionsImmediatelyNXDetector implements ISpecsPhoibosAnalyser {

	private static final Logger logger = LoggerFactory.getLogger(SpecsPhoibosSolsticeAnalyser.class);

	public static final String FIXED_ENERGY = "Fixed Energy";
	public static final String FIXED_TRANSMISSION = "Fixed Transmission";
	public static final String SNAPSHOT = "Snapshot";

	public static final String KINETIC_ENERGY = "kinetic_energy";
	public static final String BINDING_ENERGY = "binding_energy";
	public static final String VALUES = "snapshot_values";

	public static final String OPEN_VALVE = "open";

	private SpecsPhoibosController controller;
	private SpecsPhoibosSolsticeCollectionStrategy collectionStrategy;
	private SpecsPhoibosCompletedRegionWithSeperateIterations currentOrLastRegion;

	private int requestedIterations = 1;
	private int currentIteration;


	/**
	 * The scannable used to provide the photon energy in eV for KE <-> BE conversions via: BE = hν - KE - Φ
	 * <p>
	 * The scannable will provide the value used for the photon energy (hν) term.
	 *
	 * @see #updatePhotonEnergy()
	 */
	private Scannable photonEnergyProvider; // Provides the photon energy (hν) in eV
	private double currentPhotonEnergy; // Used to cache the photon energy, is updated when the analyser starts a acquisition

	/**
	 * The positioner used to control the prelens valve. Before the analyser starts it is checked to ensure it's open.
	 */
	private EnumPositioner prelensValve;

	/**
	 * The positioner used to control the experimental shutter. Before the analyser starts it is checked to ensure it's open.
	 */
	private EnumPositioner experimentalShutter;

	/**
	 * The analyser work function in eV used for KE <-> BE conversions via: BE = hν - KE - Φ
	 * <p>
	 * This value will be used for the work function (Φ) term.
	 * <p>
	 * This value is empirically determined by measuring the position of know features to calibrate the BE scale. It is
	 * usually close to 4.5 eV.
	 */
	private double workFunction = 0.0; // In eV this is analyser work function (Φ) usually close to 4.5 eV

	/**
	 * This is the width of the detector in snapshot mode at pass energy = 1 eV. It is used to calculate the required
	 * low and high energy for fixed mode. This is a fallback value. Real value will be attempted to read from EPICS.
	 * If this fails, value from Spring will be used. If no value from Spring, this will be used.
	 */
	private double detectorEnergyWidth = 0.12;

	private String currentlyRunningRegionName = "default_region";
	private String currentPositionString;
	private String sequenceFilePath;

	private boolean shouldCheckExperimentalShutter = true;
	private boolean shouldCheckPrelensValve = true;

	private double alignmentTimeout = 475;

	private SpecsPhoibosRegion defaultRegionUi;

	private int snapshotImageSizeX;

	@Override
	public void configure() throws FactoryException {
		logger.trace("configure called");
		if (isConfigured()) {
			return;
		}

		super.configure();

		if (photonEnergyProvider != null) {
			// Test the photon energy provider and intalize the photon energy
			updatePhotonEnergy();
			if (Double.isNaN(currentPhotonEnergy)) {
				logger.warn("Getting photon energy failed! Binding energy conversions will likely fail");
			}
			else {
				logger.debug("Detected current photon energy is: {} eV", currentPhotonEnergy);
			}
		}
		else {
			logger.warn("photonEnergyProvider is null. Binding energy conversions will fail");
		}

		// Check the prelens valve if confgured
		if(prelensValve != null) {
			final String prelensValvePosition = getPrelensValvePosition();
			logger.debug("Prelens valve position is: {}", prelensValvePosition);
		}
		else {
			logger.debug("No prelens valve is configured");
		}

		// If the value is zero, the IOC hasn't auto-saved the record correctly so we won't
		// update the field and will instead use the value that's already been set by spring
		// use the fall-back value from the properties file.
		//
		// If we can't get the value, we will use the default too. A time of writing this has
		// only been implemented on B07-1 and not I09-1 so this will time out there.
		try {
			double detectorEnergyWidthFromController = controller.getDetectorEnergyWidth();
			if (detectorEnergyWidthFromController != 0) {
				setDetectorEnergyWidth(detectorEnergyWidthFromController);
			}

		} catch (Exception exception) {
			logger.warn("An error occured while trying to get the detector range. Fall-back default value will be used", exception);
		}

		setConfigured(true);
	}

	@Override
	public double getCollectionTime() {
		try {
			return getCollectionStrategy().getAcquireTime();
		} catch (Exception e) {
			logger.error("Failed to get collection time ", e);
		}
		return 0.0;
	}

	public double getExposureTime() {
		try {
			return controller.getExposureTime();
		} catch (Exception e) {
			final String msg = "Error getting exposure time";
			logger.error(msg, e);
			throw new RuntimeException(msg, e);
		}
	}

	@Override
	public void setDwellTime(double dwellTime) {
		setExposureTime(dwellTime);
	}

	public void setIterations(int value) {
		try {
			requestedIterations = value;
			controller.setIterations(1);
		} catch (Exception e) {
			final String msg = "Error setting itterations to: " + value;
			logger.error(msg, e);
			throw new RuntimeException(msg, e);
		}
	}

	@Override
	public int getIterations() {
		return requestedIterations;
	}

	@Override
	public int getCurrentIteration() {
		return currentIteration;
	}

	@Override
	public void setLensMode(String value) {
		try {
			controller.setLensMode(value);
		} catch (Exception e) {
			final String msg = "Error setting lens mode to: " + value;
			logger.error(msg, e);
			throw new RuntimeException(msg, e);
		}
	}

	@Override
	public String getLensMode() {
		try {
			return controller.getLensMode();
		} catch (Exception e) {
			final String msg = "Error getting lens mode";
			logger.error(msg, e);
			throw new RuntimeException(msg, e);
		}
	}

	@Override
	public void setAcquisitionMode(String value) {
		try {
			controller.setAcquisitionMode(value);
		} catch (Exception e) {
			final String msg = "Error setting acquisition mode to: " + value;
			logger.error(msg, e);
			throw new RuntimeException(msg, e);
		}
	}

	public String getAcquisitionMode() {
		try {
			return controller.getAcquisitionMode();
		} catch (Exception e) {
			final String msg = "Error getting acquisition mode";
			logger.error(msg, e);
			throw new RuntimeException(msg, e);
		}
	}

	@Override
	public Set<String> getAcquisitionModes() {
		return controller.getAcquisitionModes();
	}

	@Override
	public void setPassEnergy(double value) {
		try {
			controller.setPassEnergy(value);
		} catch (Exception e) {
			final String msg = "Error setting pass energy to: " + value;
			logger.error(msg, e);
			throw new RuntimeException(msg, e);
		}
	}

	@Override
	public double getPassEnergy() {
		try {
			return controller.getPassEnergy();
		} catch (Exception e) {
			final String msg = "Error getting pass energy";
			logger.error(msg, e);
			throw new RuntimeException(msg, e);
		}
	}

	public void setLowEnergy(double value) {
		try {
			controller.setLowEnergy(value);
		} catch (Exception e) {
			final String msg = "Error setting low energy to: " + value;
			logger.error(msg, e);
			throw new RuntimeException(msg, e);
		}
	}

	public double getLowEnergy() {
		try {
			return controller.getLowEnergy();
		} catch (Exception e) {
			final String msg = "Error getting low energy";
			logger.error(msg, e);
			throw new RuntimeException(msg, e);
		}
	}

	public void setHighEnergy(double value) {
		try {
			controller.setHighEnergy(value);
		} catch (Exception e) {
			final String msg = "Error setting high energy to: " + value;
			logger.error(msg, e);
			throw new RuntimeException(msg, e);
		}
	}

	public double getHighEnergy() {
		try {
			return controller.getHighEnergy();
		} catch (Exception e) {
			final String msg = "Error getting high energy";
			logger.error(msg, e);
			throw new RuntimeException(msg, e);
		}
	}

	public void setEnergyStep(double value) {
		try {
			controller.setEnergyStep(value);
		} catch (Exception e) {
			final String msg = "Error setting energy step to: " + value;
			logger.error(msg, e);
			throw new RuntimeException(msg, e);
		}
	}

	public double getEnergyStep() {
		try {
			return controller.getEnergyStep();
		} catch (Exception e) {
			final String msg = "Error getting energy step";
			logger.error(msg, e);
			throw new RuntimeException(msg, e);
		}
	}

	@Override
	public double[] getSpectrum() {
		try {
			return controller.getSpectrum();
		} catch (Exception e) {
			final String msg = "Error getting spectrum data";
			logger.error(msg, e);
			throw new RuntimeException(msg, e);
		}
	}

	@Override
	public double[] getSpectrum(int index) {
		try {
			return controller.getSpectrum(index);
		} catch (Exception e) {
			final String msg = "Error getting spectrum data";
			logger.error(msg, e);
			throw new RuntimeException(msg, e);
		}
	}


	public void setCentreEnergy(double value) {
		try {
			controller.setCentreEnergy(value);
		} catch (Exception e) {
			final String msg = "Error setting centre energy to: " + value;
			logger.error(msg, e);
			throw new RuntimeException(msg, e);
		}
	}

	public IDataset getSpectrumAsDataset() {
		return DatasetFactory.createFromObject(getSpectrum());
	}

	public double[] getImage(int i) throws Exception {
		return controller.getImage(i);
	}

	@Override
	public double[][] getImage() {
		try {
			// Get the expected image size
			final int energyChannels = controller.getEnergyChannels();
			final int yChannels = controller.getSlices();

			// Get the image data from the IOC
			final double[] image1DArray = getImage(energyChannels * yChannels);

			// Reshape the data
			final double[][] image2DArray = new double[yChannels][energyChannels];
			for (int i = 0; i < yChannels; i++) {
				System.arraycopy(image1DArray, (i * energyChannels), image2DArray[i], 0, energyChannels);
			}

			return image2DArray;

		} catch (Exception e) {
			final String msg = "Error getting image";
			logger.error(msg, e);
			throw new RuntimeException(msg, e);
		}
	}

	public IDataset getImageAsDataset() throws Exception {
		final int[] shape = new int[2];
		shape[0] = controller.getEnergyChannels();
		shape[1] = controller.getSlices();
		return DatasetFactory.createFromObject(controller.getImage(shape[0] * shape[1]), shape);
	}

	@Override
	public Set<String> getLensModes() {
		return controller.getLensModes();
	}

	public SpecsPhoibosController getController() {
		return controller;
	}

	public void setController(SpecsPhoibosController controller) {
		this.controller = controller;
	}

	/**
	 * Gets the energy axis from the analyser this is always in kinetic energy
	 */
	@Override
	public double[] getEnergyAxis() {
		try {
			// Note: Don't use the energy step because of the case where the step doesn't exactly fill the range
			final double startEnergy = controller.getLowEnergy();
			final double endEnergy = controller.getHighEnergy();
			final int energyChannels = controller.getEnergyChannels();

			// Calculate the step
			final double step = (endEnergy - startEnergy) / (energyChannels - 1);

			// Build the axis
			final double[] axis = new double[energyChannels];
			for (int i = 0; i < energyChannels; i++) {
				axis[i] = startEnergy + i * step;
			}

			return axis;
		} catch (Exception e) {
			final String msg = "Error getting energy axis";
			logger.error(msg, e);
			throw new RuntimeException(msg, e);
		}
	}

	@Override
	public double[] getYAxis() {
		try {
			final double yStart = controller.getStartY();
			final double yEnd = controller.getEndY();
			final int yChannels = controller.getSlices();

			// As SPECS returns the extreme edges of the range not the centre of the pixels need to be careful here
			final double yChannelWidth = (yEnd -yStart) / yChannels;
			final double yOffset = yChannelWidth / 2;

			// Build the axis
			final double[] axis = new double[yChannels];
			for (int i = 0; i < yChannels; i++) {
				axis[i] = yStart + yOffset + i * yChannelWidth;
			}

			return axis;
		} catch (Exception e) {
			final String msg = "Error getting Y axis";
			logger.error(msg, e);
			throw new RuntimeException(msg, e);
		}
	}

	@Override
	public String getYUnits() {
		try {
			return controller.getYUnits();
		} catch (Exception e) {
			final String msg = "Error getting Y units";
			logger.error(msg, e);
			throw new RuntimeException(msg, e);
		}
	}

	@Override
	public Set<String> getPsuModes() {
		return controller.getPsuModes();
	}

	@Override
	public void setPsuMode(String psuMode) {
		try {
			controller.setPsuMode(psuMode);
		} catch (Exception e) {
			final String msg = "Error setting PSU Mode to: " + psuMode;
			logger.error(msg, e);
			throw new RuntimeException(msg, e);
		}
	}

	@Override
	public double getDetectorEnergyWidth() {
		return detectorEnergyWidth;
	}

	public void setDetectorEnergyWidth(double detectorEnergyWidth) {
		this.detectorEnergyWidth = detectorEnergyWidth;
	}

	@Override
	public void setRegion(SpecsPhoibosRegion region) {
		logger.info("Configuring analyser with region: {}", region);
		currentlyRunningRegionName = region.getName();
		// Setup analyser parameters
		setPsuMode(region.getPsuMode());
		setLensMode(region.getLensMode());
		setAcquisitionMode(region.getAcquisitionMode());
		setSlices(region.getSlices());

		// Energy range - Logic is:
		// - Convert to KE if required
		// - Set the lower of start and end as the low energy and the other as high
		final double startKe;
		final double endKe;
		final double centreKe;
		if(region.isBindingEnergy()) {
			logger.trace("Setting energy range from binding energy");
			updatePhotonEnergy(); // Update the photon energy
			startKe = toKineticEnergy(region.getStartEnergy());
			endKe = toKineticEnergy(region.getEndEnergy());
			centreKe = toKineticEnergy(region.getCentreEnergy());
		}
		else {
			logger.trace("Setting energy range from kinetic energy");
			startKe = region.getStartEnergy();
			endKe = region.getEndEnergy();
			centreKe = region.getCentreEnergy();
		}
		if (startKe <= endKe) { // Use <= here even though the = case is probably an error
			setLowEnergy(startKe);
			setHighEnergy(endKe);
		}
		else { // start > end so need to flip them.
			logger.trace("Reversing start and end energy");
			setLowEnergy(endKe);
			setHighEnergy(startKe);
		}
		// Energy step - for swept style modes
		if(region.getAcquisitionMode().equals(FIXED_TRANSMISSION)){
			setEnergyStep(region.getStepEnergy());
		}
		// Kinetic energy - for alignment mode
		if(region.getAcquisitionMode().equals(FIXED_ENERGY)){
			setCentreEnergy(centreKe);
		}

		// Pass energy settings (~energy resolution)
		setPassEnergy(region.getPassEnergy());
		setValues(region.getValues());

		// Statistics settings
		setIterations(region.getIterations());
		setExposureTime(region.getExposureTime());
		logger.trace("Finished configuring region: {}", region);
	}

	@Override
	public SpecsPhoibosRegion getRegion() {
		logger.info("Getting region from analyser");

		SpecsPhoibosRegion region = new SpecsPhoibosRegion();

		// Fill the region with the current analyser parameters
		region.setName(currentlyRunningRegionName);
		region.setAcquisitionMode(getAcquisitionMode());
		region.setEndEnergy(getHighEnergy());
		region.setExposureTime(getExposureTime());
		region.setIterations(getIterations());
		region.setLensMode(getLensMode());
		region.setPassEnergy(getPassEnergy());
		region.setPsuMode(getPsuMode());
		region.setBindingEnergy(false); // Always readback from analyser KE
		region.setStartEnergy(getLowEnergy());
		region.setStepEnergy(getEnergyStep());
		region.setValues(getValues());
		region.setCentreEnergy(getCenterEnergy());

		return region;
	}

	private void setValues(int value) {
		try {
			controller.setValues(value);
		} catch (Exception e) {
			final String msg = "Error setting values";
			logger.error(msg, e);
			throw new RuntimeException(msg, e);
		}
	}

	public int getValues() {
		try {
			return controller.getValues();
		} catch (Exception e) {
			final String msg = "Error getting values";
			logger.error(msg, e);
			throw new RuntimeException(msg, e);
		}
	}

	@Override
	public String getPsuMode() {
		try {
			return controller.getPsuMode();
		} catch (Exception e) {
			final String msg = "Error getting PSU mode";
			logger.error(msg, e);
			throw new RuntimeException(msg, e);
		}
	}

	@Override
	public double getDwellTime() {
		return getExposureTime();
	}

	public void setSlices(int slices) {
		try {
			controller.setSlices(slices);
		} catch (Exception e) {
			final String msg = "Error setting slices";
			logger.error(msg, e);
			throw new RuntimeException(msg, e);
		}
	}

	@Override
	public int getSlices() {
		try {
			return controller.getSlices();
		} catch (Exception e) {
			final String msg = "Error getting slices";
			logger.error(msg, e);
			throw new RuntimeException(msg, e);
		}
	}

	@Override
	public void startAlignment(double passEnergy, double centreEnergy, double exposureTime, String lensMode) {
		logger.info("Starting alignment");
		try {
			// Switch off safe state to make continuous performance much better
			setSafeState(false);
			// Change to single
			controller.setImageMode(ImageMode.SINGLE);
			// Set parameters of alignment
			controller.setIterations(1);
			controller.setAcquisitionMode("Fixed Energy");
			controller.setLensMode(lensMode);
			controller.setPassEnergy(passEnergy);
			controller.setCentreEnergy(centreEnergy);
			controller.setExposureTime(exposureTime);
			controller.setValues((int)(alignmentTimeout/exposureTime));
			// Start acquiring
			controller.startAcquiring();
		} catch (Exception e) {
			final String msg = "Error starting alignment";
			logger.error(msg, e);
			throw new RuntimeException(msg, e);
		}
	}

	public void startAcquiring() {
		logger.trace("startAcquiring called");
		// Check that the prelens valve is open
		checkPrelensValveIfRequired();
		checkExperimentalShutterIfRequired();

		logger.info("Starting single acquisition");
		try {
			updatePhotonEnergy();
			controller.setImageMode(ImageMode.SINGLE);
			controller.startAcquiring();
		} catch (Exception e) {
			final String msg = "Error starting single acquire";
			logger.error(msg, e);
			throw new RuntimeException(msg, e);
		}
	}

	private void checkPrelensValveIfRequired() {
		if (prelensValve == null || !shouldCheckPrelensValve) {
			return; // No prelens valve is present or not check required
		}

		final String prelensValvePosition = getPrelensValvePosition();
		// Check if it's not open then throw
		if (!OPEN_VALVE.equalsIgnoreCase(prelensValvePosition)) {
			throw new IllegalStateException("Analyser prelens valve is not open!");
		}
	}

	private String getPrelensValvePosition() {
		try {
			final String prelensValvePosition = (String) prelensValve.getPosition();
			logger.trace("Prelens valve position is: {}", prelensValvePosition);
			return prelensValvePosition;
		} catch (DeviceException e) {
			final String msg = "Error getting prelens position";
			logger.error(msg, e);
			throw new RuntimeException(msg, e);
		}
	}

	private void checkExperimentalShutterIfRequired() {
		if (experimentalShutter == null || !shouldCheckExperimentalShutter) {
			return; // No shutter is present or no check required
		}

		final String experimentalShutterPosition = getExperimentalShutterPosition();
		// Check if it's not open then throw
		if (!OPEN_VALVE.equalsIgnoreCase(experimentalShutterPosition)) {
			throw new IllegalStateException("Experimental shutter is not open!");
		}
	}

	private String getExperimentalShutterPosition() {
		try {
			final String experimentalShutterPosition = (String) experimentalShutter.getPosition();
			logger.trace("Experimental position is: {}", experimentalShutterPosition);
			return experimentalShutterPosition;
		} catch (DeviceException e) {
			final String msg = "Error getting experimental shutter position";
			logger.error(msg, e);
			throw new RuntimeException(msg, e);
		}
	}

	public void startAcquiringWait() throws DeviceException {
		try {
			startAcquiring();
			controller.waitWhileStatusBusy();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt(); // Re-interrupt the thread.
			final String msg = "Error waiting for acquisition to complete";
			logger.error(msg, e);
			throw new DeviceException(msg, e);
		}
	}

	@Override
	public void stopAcquiring() {
		logger.info("Stopping analyser");
		try {
			controller.stopAcquiring();
		} catch (Exception e) {
			final String msg = "Error stopping acquiring";
			logger.error(msg, e);
			throw new RuntimeException(msg, e);
		} finally {
			// do normal scan end chores
			scanEnd();
			// Switch off the high voltages immediately
			setSafeState(true);
		}
	}

	@Override
	public double getCenterEnergy() {
		try {
			return (getLowEnergy() + getHighEnergy()) / 2.0;
		} catch (Exception e) {
			final String msg = "Error getting centre energy";
			logger.error(msg, e);
			throw new RuntimeException(msg, e);
		}
	}

	@Override
	public void setCollectionStrategy(NXCollectionStrategyPlugin collectionStrategy) {
		if (!(collectionStrategy instanceof SpecsPhoibosSolsticeCollectionStrategy)) {
			throw new IllegalArgumentException("Only SpecsPhoibosSeparateIterationCollectionStrategy can be used with SpecsPhoibosAnalyserSeparateIterations");
		}
		this.collectionStrategy = (SpecsPhoibosSolsticeCollectionStrategy) collectionStrategy;
		super.setCollectionStrategy(collectionStrategy);
	}

	void setExposureTime(double exposureTime) {
		try {
			controller.setExposureTime(exposureTime);
		} catch (Exception e) {
			final String msg = "Error setting exposure time to: " + exposureTime;
			logger.error(msg, e);
			throw new RuntimeException(msg, e);
		}
	}

	/**
	 * Set the analyser sequence from a sequence file. If the path is absolute it will be used as is else the absolute
	 * path will be constructed from the visit xml directory and the provided file name.
	 *
	 * @param filePath
	 *            Path to a sequence file either absolute or a file inside the visit xml directory
	 */
	@Override
	public void setSequenceFile(String filePath) {
		logger.debug("Setting sequence file to: {}", filePath);

		final String absoluteFilePath;
		if (Paths.get(filePath).isAbsolute()) {
			absoluteFilePath = filePath;
		}
		else {
			logger.trace("Assuming file path within visit XML dir");
			String visitXmlDir = InterfaceProvider.getPathConstructor().getVisitSubdirectory("xml");
			absoluteFilePath = Paths.get(visitXmlDir, filePath).toString();
		}

		setSequence(SpecsPhoibosSequenceHelper.loadSequence(absoluteFilePath), filePath);
	}

	@Override
	public void setSequence(SpecsPhoibosSequence sequence, String filepath) {
		collectionStrategy.setSequence(sequence);
		sequenceFilePath = filepath;
		SpecsPhoibosSequenceFileUpdate liveSequence = new SpecsPhoibosSequenceFileUpdate(filepath);
		notifyIObservers(this, liveSequence);
	}

	public SpecsPhoibosStatus getDetectorStatus() {
		try {
			return controller.getDetectorStatus();
		} catch (Exception e) {
			final String msg = "Error getting detector status";
			logger.error(msg, e);
			throw new RuntimeException(msg, e);
		}
	}

	public String getStatusMessage() {
		try {
			return controller.getStatusMessage();
		} catch (Exception e) {
			final String msg = "Error getting status message";
			logger.error(msg, e);
			throw new RuntimeException(msg, e);
		}
	}

	private void updatePhotonEnergy() {
		logger.trace("updatePhotonEnergy called");
		try {
			currentPhotonEnergy = (double) photonEnergyProvider.getPosition();
			logger.trace("Updated photon energy. New value is {} eV", currentPhotonEnergy);
		} catch (DeviceException e) {
			final String msg = "Getting photon energy failed";
			logger.error(msg, e);
			throw new RuntimeException(msg, e);
		}
	}

	@Override
	public double toBindingEnergy(double kineticEnergy) {
		return toBindingEnergy(kineticEnergy, currentPhotonEnergy);
	}

	private double toBindingEnergy(double kineticEnergy, double photonEnergy) {
		return photonEnergy - kineticEnergy - workFunction;
	}

	@Override
	public double toKineticEnergy(double bindingEnergy) {
		return toKineticEnergy(bindingEnergy, currentPhotonEnergy);
	}

	private double toKineticEnergy(double bindingEnergy, double photonEnergy) {
		return photonEnergy - bindingEnergy - workFunction;
	}

	@Override
	public double[] toBindingEnergy(double[] kineticEnergy) {
		return Arrays.stream(kineticEnergy).map(energy -> toBindingEnergy(energy)).toArray();
	}

	@Override
	public double[] toKineticEnergy(double[] bindingEnergy) {
		return Arrays.stream(bindingEnergy).map(energy -> toKineticEnergy(energy)).toArray();
	}

	public Scannable getPhotonEnergyProvider() {
		return photonEnergyProvider;
	}

	public void setPhotonEnergyProvider(Scannable photonEnergyProvider) {
		this.photonEnergyProvider = photonEnergyProvider;
	}

	public EnumPositioner getPrelensValve() {
		return prelensValve;
	}

	public void setPrelensValve(EnumPositioner prelensValve) {
		this.prelensValve = prelensValve;
	}

	public double getWorkFunction() {
		return workFunction;
	}

	public void setWorkFunction(double workFunction) {
		this.workFunction = workFunction;
	}

	public int getTotalPoints() {
		try {
			return controller.getTotalPoints() * getIterations();
		} catch (Exception e) {
			final String msg = "Error getting total points";
			logger.error(msg, e);
			throw new RuntimeException(msg, e);
		}
	}

	public int getCurrentPoint() {
		try {
			int pointsFromPreviousIterations = controller.getTotalPoints() * currentIteration;
			return controller.getCurrentPoint() + pointsFromPreviousIterations;
		} catch (Exception e) {
			final String msg = "Error getting current points";
			logger.error(msg, e);
			throw new RuntimeException(msg, e);
		}
	}

	public int getPointInIteration() {
		try {
			return controller.getPointInIteration();
		} catch (Exception e) {
			final String msg = "Error getting point in interation";
			logger.error(msg, e);
			throw new RuntimeException(msg, e);
		}
	}

	private SpecsPhoibosRegionValidation validateRegion(SpecsPhoibosRegion region) throws DeviceException {
		SpecsPhoibosRegionValidation validationForRegion = new SpecsPhoibosRegionValidation(region);

		// Update photon energy once so that we don't have to update in each one of the individual validation methods
		if(region.isBindingEnergy()) {
			updatePhotonEnergy();
		}
		// Validate photon energy (applies only to BE mode), if test fails further testing is meaningless
		if (!isPhotonEnergyValid(region, validationForRegion)) {
			return validationForRegion;
		}
		// Check if PSU mode correct
		if (!isPsuModeValid(region, validationForRegion)) {
			return validationForRegion;
		}

		//Second round of validation will run the rest of the tests altogether
		validationForRegion.addErrors(validateRegionEnergy(region));

		//Continue with epics validation
		setRegion(region);

		try {
			controller.validateScanConfiguration();
			String validityStatus = controller.getScanValidityStatus();
			if (validityStatus.equals("INVALID")) {
				validationForRegion.addErrors(Arrays.asList(getStatusMessage()));
			}
			return validationForRegion;
		} catch (Exception e) {
			final String msg = "Error validating scan configuration";
			logger.error(msg, e);
			throw new DeviceException(msg, e);
		}
	}

	@Override
	public SpecsPhoibosSequenceValidation validateSequence(SpecsPhoibosSequence sequence) throws DeviceException {
		ArrayList<SpecsPhoibosRegionValidation> allValidationErrors = new ArrayList<>();
		for (SpecsPhoibosRegion userSpecifiedRegion : sequence.getEnabledRegions()) {
			allValidationErrors.add(validateRegion(userSpecifiedRegion));
		}
		return new SpecsPhoibosSequenceValidation(allValidationErrors);
	}

	/**
	 * Returns true if no scan running or analyser is in aborted state
	 */
	@Override
	public boolean isNotBusy() {
		try {
			return getCollectionStrategy().getStatus() == Detector.IDLE
					&& getDetectorStatus() == SpecsPhoibosStatus.IDLE
					|| getDetectorStatus() == SpecsPhoibosStatus.ABORTED;
		} catch (Exception e) {
			return false;
		}
	}

	private boolean isPhotonEnergyValid(SpecsPhoibosRegion region, SpecsPhoibosRegionValidation validation){
		if (region.isBindingEnergy()) {
			double startBindingEnergy = region.getStartEnergy();
			double endBindingEnergy = region.getEndEnergy();
			double photonEnergy = getCurrentPhotonEnergy();
			if (photonEnergy < startBindingEnergy || photonEnergy < endBindingEnergy) {
				validation.addError("Binding energy exceeds photon energy of value of "+ photonEnergy);
				return false;
			}
		}
		return true;
	}

	/**
	 * Compares PSU mode and start/end energy
	 */
	private boolean isPsuModeValid(SpecsPhoibosRegion region, SpecsPhoibosRegionValidation validation){
		//Extract region energies and convert to KE if needed
		double startEnergy = region.getStartEnergy();
		double endEnergy = region.getEndEnergy();
		if (region.isBindingEnergy()) {
			double photonEnergy = getCurrentPhotonEnergy();
			startEnergy = toKineticEnergy(startEnergy, photonEnergy);
			endEnergy = toKineticEnergy(endEnergy, photonEnergy);
		}

		String currentPsuMode = region.getPsuMode();
		boolean isKilovolts = currentPsuMode.contains("kV");
		currentPsuMode = currentPsuMode.replaceAll("[^\\d.]", "");
		double comparablePsuValue = Double.parseDouble(currentPsuMode);
		if (isKilovolts) {
			comparablePsuValue *= 1000;
		}
		if (startEnergy > comparablePsuValue || endEnergy > comparablePsuValue) {
			validation.addError("Kinetic energy of the scan exceeds limit of the current PSU mode");
			return false;
		}
		return true;
	}

	/**
	 * Tests that energy values for a region are acceptable by comparing
	 * high and low energy limit against the pass energy
	 *
	 * @param region
	 * @return A list of error messages or an empty list if no errors
	 * @throws DeviceException
	 */
	private List<String> validateRegionEnergy(SpecsPhoibosRegion region) {
		double startEnergy = region.getStartEnergy();
		double endEnergy = region.getEndEnergy();
		double passEnergy = region.getPassEnergy();

		logger.debug("Validating energy for region: {}", region.getName());
		logger.debug("Energy mode: {}", region.isBindingEnergy() ? "Binding" : "Kinetic");
		logger.debug("Photon Energy: {}, Start Energy: {}, End Energy: {}, Pass Energy: {}", currentPhotonEnergy, startEnergy, endEnergy, passEnergy);

		// Convert binding to kinetic if needed
		if (region.isBindingEnergy()) {

			// If the analyser has an energy scannable and the region has an enabled value for it, we need to validate with that instead of the current photon energy
			double photonEnergy = getCurrentPhotonEnergy();

			startEnergy = toKineticEnergy(startEnergy, photonEnergy);
			endEnergy = toKineticEnergy(endEnergy, photonEnergy);
			logger.debug("Binding energy mode. Converting values to Kinetic. Start: {}, End: {}", startEnergy, endEnergy);
		}

		List<String> energyValidationErrors = new ArrayList<>();
		if (startEnergy <= passEnergy) {
			logger.debug("Start energy is lower than or equal to pass energy.");
			energyValidationErrors.add("Start (kinetic) energy is lower than or equal to pass energy");
		}

		if (endEnergy <= passEnergy) {
			logger.debug("End energy is lower than or equal to pass energy");
			energyValidationErrors.add("End (kinetic) energy is lower than or equal to pass energy");
		}

		if (energyValidationErrors.isEmpty()) {
			logger.debug("Area has no energy validation errors");
		}

		return energyValidationErrors;
	}

	@Override
	public String getCurrentPositionString() {
		return currentPositionString;
	}

	@Override
	public String getCurrentRegionName() {
		return currentlyRunningRegionName;
	}

	/**
	 * Gets the photon energy that will currently be used when converting KE <-> BE
	 *
	 * @return The cached photon energy
	 */
	public double getCurrentPhotonEnergy() {
		return currentPhotonEnergy;
	}

	public void setSafeState(boolean safe) {
		try {
			controller.setSafeState(safe);
			logger.trace("Set safe state to: {}", safe);
		} catch (Exception e) {
			final String msg;
			if (safe) {
				msg = "Error placing analyser in safe state. HV might still be on";
			}
			else {
				msg = "Error disabling safe state. Performance might be slow";
			}
			logger.error(msg, e);
			throw new RuntimeException(msg, e);
		}
	}

	public EnumPositioner getExperimentalShutter() {
		return experimentalShutter;
	}

	public void setExperimentalShutter(EnumPositioner experimentalShutter) {
		this.experimentalShutter = experimentalShutter;
	}

	public SpecsPhoibosCompletedRegionWithSeperateIterations getCurrentOrLastRegion() {
		return currentOrLastRegion;
	}

	@Override
	public void stopAfterCurrentIteration() {
		collectionStrategy.setStopAfterCurrentIteration(true);
		String message = "Current iteration will be last in this region, remaining regions will still be measured";
		logger.info(message);
		InterfaceProvider.getTerminalPrinter().print(message);
	}

	@Override
	public void stopAfterCurrentRegion() {
		collectionStrategy.setStopAfterCurrentRegion(true);
		String message = "Current region will be last in this sequence, remaining regions will NOT be measured";
		logger.info(message);
		InterfaceProvider.getTerminalPrinter().print(message);
	}

	@Override
	public boolean getShouldCheckExperimentalShutter() {
		return shouldCheckExperimentalShutter;
	}

	@Override
	public void setShouldCheckExperimentalShutter(boolean shouldCheckExperimentalShutter) {
		this.shouldCheckExperimentalShutter = shouldCheckExperimentalShutter;
	}

	@Override
	public boolean getShouldCheckPrelensValve() {
		return shouldCheckPrelensValve;
	}

	@Override
	public void setShouldCheckPrelensValve(boolean shouldCheckPrelensValve) {
		this.shouldCheckPrelensValve = shouldCheckPrelensValve;
	}

	@Override
	public void enableExperimentalShutterCheck() {
		setShouldCheckExperimentalShutter(true);
	}

	@Override
	public void disableExperimentalShutterCheck() {
		setShouldCheckExperimentalShutter(false);
	}

	@Override
	public void enablePrelensValveCheck() {
		setShouldCheckPrelensValve(true);
	}

	@Override
	public void disablePrelensValveCheck() {
		setShouldCheckPrelensValve(false);
	}

	public void setAlignmentTimeout(double alignmentTimeout) {
		this.alignmentTimeout = alignmentTimeout;
	}

	@Override
	public void setDefaultRegionUi(SpecsPhoibosRegion region) {
		this.defaultRegionUi = region;

	}

	@Override
	public SpecsPhoibosRegion getDefaultRegionUi() {
		return defaultRegionUi;
	}

	@Override
	public void writePosition(Object data, SliceND scanSlice) throws NexusException {
		//Not used as we need to write data more often than this. N number of times for N number of regions
		//per scanDataPoint. However this is only called once per scanDataPoint.
		//Call own writePosition/writeScalarData method rather than use framework.
	}

	public double getStepTime() throws Exception {
		return getController().getExposureTime();
	}

	public Integer getTotalSteps() throws Exception {
		return controller.getTotalPoints();
	}

	public void setSnapshotImageSizeX(int snapshotImageSizeX) {
		this.snapshotImageSizeX = snapshotImageSizeX;
	}

	@Override
	public int getSnapshotImageSizeX() {
		return snapshotImageSizeX;
	}

	@Override
	protected NexusObjectWrapper<NXdetector> initialiseAdditionalNXdetectorData(NXdetector detector,
			NexusScanInfo info) {
		String detectorName = getName();
		detector.setAttribute(null, NXdetector.NX_LOCAL_NAME, detectorName);
		detector.setAttribute(null, GDADeviceNexusConstants.ATTRIBUTE_NAME_SCAN_ROLE, "detector");
		AnalyserRegionDatasetUtil.createOneDimensionalStructure(AnalyserRegionConstants.REGION_LIST, detector, new int[] {1, collectionStrategy.getEnabledRegions().size()}, String.class);
		return new NexusObjectWrapper<>(detectorName, detector, AnalyserRegionConstants.REGION_LIST);
	}

	@Override
	protected void setupAdditionalDataAxisFields(NexusObjectWrapper<?> nexusWrapper, int scanRank) {
		//Set up axes as [scannables, ..., region_list]
		final int regionAxisIndex = scanRank;
		nexusWrapper.setPrimaryDataFieldName(AnalyserRegionConstants.REGION_LIST);
		nexusWrapper.addAxisDataFieldForPrimaryDataField(AnalyserRegionConstants.REGION_LIST, AnalyserRegionConstants.REGION_LIST, regionAxisIndex, regionAxisIndex);
	}

	@Override
	protected void beforeCollectData() throws DeviceException {
		try {
			getDataStorage().overridePosition(getName(), AnalyserRegionConstants.REGION_LIST, collectionStrategy.getEnabledRegionNames());
		} catch (DatasetException e) {
			// TODO Auto-generated catch block
			logger.error("Failed to write Region List", e);
			throw new DeviceException(e);
		}
	}

	@Override
	public String getSequenceFile() {
		return sequenceFilePath;
	}
}