/*-
 * Copyright © 2016 Diamond Light Source Ltd.
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
import java.util.Set;

import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.IDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.RateLimiter;

import gda.data.PathConstructor;
import gda.device.DeviceException;
import gda.device.EnumPositioner;
import gda.device.Scannable;
import gda.device.detector.NXDetector;
import gda.device.detector.areadetector.v17.ImageMode;
import gda.device.detector.nxdetector.NXCollectionStrategyPlugin;
import gda.factory.FactoryException;
import gda.observable.Observer;
import uk.ac.diamond.daq.devices.specs.phoibos.api.ISpecsPhoibosAnalyser;
import uk.ac.diamond.daq.devices.specs.phoibos.api.SpecsPhoibosLiveDataUpdate;
import uk.ac.diamond.daq.devices.specs.phoibos.api.SpecsPhoibosRegion;
import uk.ac.diamond.daq.devices.specs.phoibos.api.SpecsPhoibosSequence;
import uk.ac.diamond.daq.devices.specs.phoibos.api.SpecsPhoibosSequenceHelper;

/**
 * <p>
 * This class is the actual detector used in GDA. It is composed of a {@link SpecsPhoibosController} handling the EPICS
 * communication, and a {@link SpecsPhoibosCollectionStrategy} handling the collection logic.
 * </p>
 * <p>
 * It is exported via RMI using the {@link ISpecsPhoibosAnalyser} interface, to the client GUI to request information
 * available here, like the available {@link #getLensModes()}. It also provides the logic for BE <-> KE conversion using
 * the photon energy.
 * </p>
 * The GUI can be an {@link Observer} of this class to receive live updates of the scan as it is received from EPICS,
 * see {@link SpecsPhoibosLiveDataUpdate}
 *
 * @author James Mudd
 */
public class SpecsPhoibosAnalyser extends NXDetector implements ISpecsPhoibosAnalyser {

	/**
	 * Generated serial ID
	 */
	private static final long serialVersionUID = 4528518322504037015L;

	private static final Logger logger = LoggerFactory.getLogger(SpecsPhoibosAnalyser.class);

	private SpecsPhoibosController controller;

	private SpecsPhoibosCollectionStrategy collectionStrategy;

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
	 * low and high energy for fixed mode.
	 */
	private double detectorEnergyWidth = 1;

	/**
	 * Limit the rate of update events sent to the GUI to a max of 10 per sec
	 */
	private final transient RateLimiter updateLimiter = RateLimiter.create(10.0);

	@Override
	public void configure() throws FactoryException {
		logger.trace("configure called");
		super.configure();

		// Pass through the events from the controller
		controller.addIObserver(this::processEpicsUpdate);

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
	}

	@Override
	public double getCollectionTime() {
		return getExposureTime();
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
	public void setCollectionTime(double collectionTime) {
		setExposureTime(collectionTime);
	}

	@Override
	public void setDwellTime(double dwellTime) {
		setExposureTime(dwellTime);
	}

	public void setIterations(int value) {
		try {
			controller.setIterations(value);
		} catch (Exception e) {
			final String msg = "Error setting itterations to: " + value;
			logger.error(msg, e);
			throw new RuntimeException(msg, e);
		}
	}

	public int getIterations() {
		try {
			return controller.getIterations();
		} catch (Exception e) {
			final String msg = "Error getting itterations";
			logger.error(msg, e);
			throw new RuntimeException(msg, e);
		}
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

	public IDataset getSpectrumAsDataset() {
		return DatasetFactory.createFromObject(getSpectrum());
	}

	@Override
	public double[][] getImage() {
		try {
			double[] image1DArray = controller.getImage();

			int energyChannels = controller.getEnergyChannels();
			int yChannels = controller.getSlices();

			// Check the array lengths
			if (image1DArray.length != energyChannels * yChannels) {
				throw new RuntimeException("The image array lentgh was mismatched");
			}

			double[][] image2DArray = new double[yChannels][energyChannels];

			// Reshape the data
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
		if(region.isBindingEnergy()) {
			logger.trace("Setting energy range from binding energy");
			updatePhotonEnergy(); // Update the photon energy
			startKe = toKineticEnergy(region.getStartEnergy());
			endKe = toKineticEnergy(region.getEndEnergy());
		}
		else {
			logger.trace("Setting energy range from kinetic energy");
			startKe = region.getStartEnergy();
			endKe = region.getEndEnergy();
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
		if("Fixed Transmission".equals(region.getAcquisitionMode())){
			setEnergyStep(region.getStepEnergy());
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
		region.setAcquisitionMode(getAcquisitionMode());
		region.setEndEnergy(getHighEnergy());
		region.setExposureTime(getExposureTime());
		region.setIterations(getIterations());
		region.setLensMode(getLensMode());
		region.setName(getName());
		region.setPassEnergy(getPassEnergy());
		region.setPsuMode(getPsuMode());
		region.setBindingEnergy(false); // Always readback from analyser KE
		region.setStartEnergy(getLowEnergy());
		region.setStepEnergy(getEnergyStep());
		region.setValues(getValues());

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
	public void startContinuous() {
		logger.info("Starting continious acquisition");
		try {
			// Switch off safe state to make continuous performance much better
			setSafeState(false);
			// Change to continuous
			controller.setImageMode(ImageMode.CONTINUOUS);
			// Change to 1 iteration
			controller.setIterations(1);
			// Start acquiring
			controller.startAcquiring();
		} catch (Exception e) {
			final String msg = "Error starting continuous acquire";
			logger.error(msg, e);
			throw new RuntimeException(msg, e);
		}
	}

	public void startAcquiring() {
		logger.trace("startAcquiring called");
		// Check that the prelens valve is open
		checkPrelensValve();

		logger.info("Starting single acquisition");
		try {
			// Update cached photon energy
			updatePhotonEnergy();
			// Change to continuous
			controller.setImageMode(ImageMode.SINGLE);
			// Start acquiring
			controller.startAcquiring();
		} catch (Exception e) {
			final String msg = "Error starting single acquire";
			logger.error(msg, e);
			throw new RuntimeException(msg, e);
		}
	}

	private void checkPrelensValve() {
		if (prelensValve == null) {
			return; // No prelens valve is present.
		}

		final String prelensValvePosition = getPrelensValvePosition();
		// Check if it's not open then throw
		if (!"open".equalsIgnoreCase(prelensValvePosition)) {
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

	public void startAcquiringWait() throws DeviceException {
		startAcquiring();
		try {
			controller.waitWhileStatusBusy();
			// Always update observers when the acquire finishes
			notifyIObservers(this, getLiveDataUpdate());
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
		}
		// Switch off the high voltages
		setSafeState(true);
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
		if (!(collectionStrategy instanceof SpecsPhoibosCollectionStrategy)) {
			throw new IllegalArgumentException("Only SpecsPhoibosCollectionStrategy can be used with SpecsPhoibosAnalyser");
		}
		this.collectionStrategy = (SpecsPhoibosCollectionStrategy) collectionStrategy;
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
	public void setSequenceFile(String filePath) {
		logger.debug("Setting sequence file to: {}", filePath);

		final String absoluteFilePath;
		if (Paths.get(filePath).isAbsolute()) {
			absoluteFilePath = filePath;
		}
		else {
			logger.trace("Assuming file path within visit XML dir");
			String visitXmlDir = PathConstructor.getVisitSubdirectory("xml");
			absoluteFilePath = Paths.get(visitXmlDir, filePath).toString();
		}

		setSequence(SpecsPhoibosSequenceHelper.loadSequence(absoluteFilePath));
	}

	@Override
	public void setSequence(SpecsPhoibosSequence sequence) {
		collectionStrategy.setSequence(sequence);
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
		return currentPhotonEnergy - kineticEnergy - workFunction;
	}

	@Override
	public double toKineticEnergy(double bindingEnergy) {
		return currentPhotonEnergy - bindingEnergy - workFunction;
	}

	@Override
	public double[] toBindingEnergy(double[] kineticEnergy) {
		double[] bindingEnergy = new double[kineticEnergy.length];
		for (int i = 0; i < bindingEnergy.length; i++) {
			bindingEnergy[i] = toBindingEnergy(kineticEnergy[i]);
		}
		return bindingEnergy;
	}

	@Override
	public double[] toKineticEnergy(double[] bindingEnergy) {
		double[] kineticEnergy = new double[bindingEnergy.length];
		for (int i = 0; i < kineticEnergy.length; i++) {
			kineticEnergy[i] = toKineticEnergy(bindingEnergy[i]);
		}
		return kineticEnergy;
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
			return controller.getTotalPoints();
		} catch (Exception e) {
			final String msg = "Error getting total points";
			logger.error(msg, e);
			throw new RuntimeException(msg, e);
		}
	}

	public int getCurrentPoint() {
		try {
			return controller.getCurrentPoint();
		} catch (Exception e) {
			final String msg = "Error getting current points";
			logger.error(msg, e);
			throw new RuntimeException(msg, e);
		}
	}

	private void processEpicsUpdate(Object source, Object arg) {
		// TODO The performance could be improved here the update from EPICS already contains the current point so
		// We could use it but improvement would be minor and we would need an EPICS dependency here.
		logger.trace("Update received from EPICS. source:{}, arg:{}", source, arg);
		if (getCurrentPoint() == 0) {
			// When you start a new region the current channel changes back to 0 but there isn't any data yet.
			logger.trace("Update for first point, no data yet so ignoring");
			return;
		}
		// If the update rate is too high just drop this one
		if(updateLimiter.tryAcquire()) {
			notifyIObservers(this, getLiveDataUpdate());
		}
		else {
			logger.trace("Update suppressed. Rate was too high");
		}
	}

	private SpecsPhoibosLiveDataUpdate getLiveDataUpdate() {
		logger.trace("getLiveDataUpdate called");
		final int totalPoints = getTotalPoints();
		final int currentPoint = getCurrentPoint();

		final double[] spectrum = getSpectrum();
		final double[][] image = getImage();

		final double[] keEnergyAxis = getEnergyAxis();
		final double[] beEnergyAxis = toBindingEnergy(keEnergyAxis);
		final double[] yAxis = getYAxis();

		return new SpecsPhoibosLiveDataUpdate(totalPoints, currentPoint, spectrum, image, keEnergyAxis, beEnergyAxis, yAxis);
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

}
