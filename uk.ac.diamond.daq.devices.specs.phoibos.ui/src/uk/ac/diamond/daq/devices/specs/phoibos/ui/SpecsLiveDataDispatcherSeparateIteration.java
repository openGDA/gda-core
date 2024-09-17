/*-
 * Copyright © 2022 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.devices.specs.phoibos.ui;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.epics.connection.EpicsController;
import gda.factory.FindableConfigurableBase;
import gda.factory.Finder;
import gda.observable.IObserver;
import gda.observable.ObservableComponent;
import gov.aps.jca.CAException;
import gov.aps.jca.Channel;
import gov.aps.jca.TimeoutException;
import gov.aps.jca.dbr.DBR_Enum;
import gov.aps.jca.dbr.DBR_Int;
import uk.ac.diamond.daq.devices.specs.phoibos.api.AnalyserPVProvider;
import uk.ac.diamond.daq.devices.specs.phoibos.api.ISpecsPhoibosAnalyser;
import uk.ac.diamond.daq.devices.specs.phoibos.api.SpecsIterationNumberUpdate;
import uk.ac.diamond.daq.devices.specs.phoibos.api.SpecsPhoibosLiveIterationSpectraUpdate;
import uk.ac.diamond.daq.devices.specs.phoibos.api.SpecsPhoibosLiveUpdate;
import uk.ac.diamond.daq.devices.specs.phoibos.api.SpecsPhoibosSequenceFileUpdate;
import uk.ac.diamond.daq.devices.specs.phoibos.api.SpecsRegionStartUpdate;

/**
 * This class is instantiated via Spring and used as a client side dispatcher
 * of analyser related data for the purpose of data visualisation. To avoid the
 * synchronisation issues that appear when getting data from both server and
 * client.
 */
public class SpecsLiveDataDispatcherSeparateIteration extends FindableConfigurableBase implements ISpecsLiveDataDispatcher, IObserver {

	private static final Logger logger = LoggerFactory.getLogger(SpecsLiveDataDispatcherSeparateIteration.class);

	private String name;
	private final ObservableComponent observableComponent = new ObservableComponent();
	private AnalyserPVProvider pvProvider;
	private EpicsController controller = EpicsController.getInstance();
	protected ISpecsPhoibosAnalyser analyser;
	private short acquisitionMode;
	private double[] summedSpectrum;
	private final Map<String, Channel> channelMap = new HashMap<>();
	private int currentIteration;
	private int requestedIterations;
	private String currentRegionName;
	private String positionString;
	private double currentPhotonEnergy;

	@Override
	public void configure() {
		if (isConfigured()) {
			return;
		}
		try {
			List<ISpecsPhoibosAnalyser> analysers = Finder.listLocalFindablesOfType(ISpecsPhoibosAnalyser.class);
			if (analysers.size() != 1) {
				throw new RuntimeException("No Analyser was found! (Or more than 1)");
			}
			analyser = analysers.get(0);
			analyser.addIObserver(this);

			Channel acquisitionModeChannel = getChannel(pvProvider.getAcquisitionModePV());
			controller.setMonitor(acquisitionModeChannel, evt -> {
				DBR_Enum dbr = (DBR_Enum) evt.getDBR();
				acquisitionMode = dbr.getEnumValue()[0];

			});

			// Properly initialise values
			requestedIterations = analyser.getIterations();
			currentIteration = analyser.getCurrentIteration();
			currentRegionName = analyser.getCurrentRegionName();
			positionString = analyser.getCurrentPositionString();
			acquisitionMode =  controller.cagetEnum(acquisitionModeChannel);

			Channel currentPointChannel = getChannel(pvProvider.getCurrentChannelPV());
			controller.setMonitor(currentPointChannel, evt -> {
				DBR_Int dbr = (DBR_Int) evt.getDBR();
				int point = dbr.getIntValue()[0];
				if (point == 0) {
					return;
				}
				if(acquisitionMode == 3) {
					notifyListeners(createAlignmentEvent());
				} else {
					int pointInIteration = getPointInIteration();
					int totalPointsIteration = getTotalPointsIteration();
					double[] spectrum = getSpectrum(totalPointsIteration);
					// Create an identical but separate object for event to work
					double[] spectrumCopy = spectrum.clone();
					updateSummedSpectrum(spectrum, pointInIteration, currentIteration);
					notifyListeners(getSeparateIterationDataUpdate(pointInIteration, currentIteration, spectrumCopy, totalPointsIteration));
				}
			});
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		} catch (CAException | TimeoutException e) {
			logger.debug("Problem with getChannel or setMonitor");
		}
		setConfigured(true);
	}

	private SpecsPhoibosLiveUpdate createAlignmentEvent() {
		return new SpecsPhoibosLiveUpdate(getSpectrum(0));
	}

	private SpecsPhoibosLiveIterationSpectraUpdate getSeparateIterationDataUpdate(int pointInIteration, int currentIteration, double[] iterSpectrum, int totalPtIter) {
		final double[] keEnergyAxis = generateEnergyAxis(getLowEnergy(), getHighEnergy(), totalPtIter);
		final double[] beEnergyAxis = convertToBindingEnergy(keEnergyAxis, currentPhotonEnergy, getWorkFunction());

		SpecsPhoibosLiveIterationSpectraUpdate.Builder builder = new SpecsPhoibosLiveIterationSpectraUpdate.Builder();

		builder.iterationNumber(currentIteration+1)
			.iterationSpectrum(iterSpectrum)
			.acquisitionMode(acquisitionMode)
			.regionName(currentRegionName)
			.positionString(positionString)
			.totalPoints(getTotalPoints() * requestedIterations)
			.currentPoint(getCurrentPoint())
			.totalIterations(requestedIterations)
			.currentPointInIteration(pointInIteration)
			.spectrum(summedSpectrum)
			.keEnergyAxis(keEnergyAxis)
			.beEnergyAxis(beEnergyAxis);

		return builder.build();
	}


	private double[] getImage(int size) {
		try {
			return controller.cagetDoubleArray(getChannel(pvProvider.getImagePV()), size);
		} catch (Exception e) {
			final String msg = "Error getting image";
			throw new RuntimeException(msg, e);
		}
	}

	private double[] getSpectrum() {
		try {
			return controller.cagetDoubleArray(getChannel(pvProvider.getSpectrumPV()));
		} catch (Exception e) {
			final String msg = "Error getting spectrum";
			throw new RuntimeException(msg, e);
		}
	}

	private double[] getSpectrum(int length) {
		try {
			return controller.cagetDoubleArray(getChannel(pvProvider.getSpectrumPV()), length);
		} catch (Exception e) {
			final String msg = "Error getting spectrum";
			throw new RuntimeException(msg, e);
		}
	}

	private double getLowEnergy() {
		try {
			return controller.cagetDouble(getChannel(pvProvider.getLowEnergyPV()));
		} catch (Exception e) {
			final String msg = "Error getting low energy";
			throw new RuntimeException(msg, e);
		}
	}

	private double getHighEnergy() {
		try {
			return controller.cagetDouble(getChannel(pvProvider.getHighEnergyPV()));
		} catch (Exception e) {
			final String msg = "Error getting high energy";
			throw new RuntimeException(msg, e);
		}
	}

	private int getTotalPointsIteration() {
		try {
			return controller.cagetInt(getChannel(pvProvider.getTotalPointsIterationPV()));
		} catch (Exception e) {
			final String msg = "Error getting low energy";
			throw new RuntimeException(msg, e);
		}
	}

	private int getTotalPoints() {
		try {
			return controller.cagetInt(getChannel(pvProvider.getTotalPointsPV()));
		} catch (Exception e) {
			final String msg = "Error getting total points";
			throw new RuntimeException(msg, e);
		}
	}

	private int getCurrentPoint() {
		try {
			int pointsFromPreviousIterations = getTotalPoints() * currentIteration;
			return controller.cagetInt(getChannel(pvProvider.getCurrentChannelPV())) + pointsFromPreviousIterations;
		} catch (Exception e) {
			final String msg = "Error getting current point";
			throw new RuntimeException(msg, e);
		}
	}

	private int getPointInIteration() {
		try {
			return controller.cagetInt(getChannel(pvProvider.getCurrentPointIterationPV()));
		} catch (Exception e) {
			final String msg = "Error getting point in iteration";
			throw new RuntimeException(msg, e);
		}
	}

	private String getYUnits() {
		try {
			return controller.cagetString(getChannel(pvProvider.getyUnitsPV()));
		} catch (Exception e) {
			final String msg = "Error getting Y units";
			throw new RuntimeException(msg, e);
		}
	}

	private double getStartY() {
		try {
			return controller.cagetDouble(getChannel(pvProvider.getyStartPV()));
		} catch (Exception e) {
			final String msg = "Error getting Y axis start";
			throw new RuntimeException(msg, e);
		}
	}

	private double getEndY() {
		try {
			return controller.cagetDouble(getChannel(pvProvider.getyEndPV()));
		} catch (Exception e) {
			final String msg = "Error getting Y axis end";
			throw new RuntimeException(msg, e);
		}
	}

	private int getSlices() {
		try {
			return controller.cagetInt(getChannel(pvProvider.getSlicesPV()));
		} catch (Exception e) {
			final String msg = "Error getting slices";
			throw new RuntimeException(msg, e);
		}
	}

	private double[] generateYAxis(double yStart, double yEnd, int yChannels) {
		// As SPECS returns the extreme edges of the range not the centre of the pixels need to be careful here
		final double yChannelWidth = (yEnd -yStart) / yChannels;
		final double yOffset = yChannelWidth / 2;
		// Build the axis
		final double[] axis = new double[yChannels];
		for (int i = 0; i < yChannels; i++) {
			axis[i] = yStart + yOffset + i * yChannelWidth;
		}
		return axis;
	}

	private double[] generateEnergyAxis(double startEnergy, double endEnergy, int energyChannels) {
		// Calculate the step
		final double step = (endEnergy - startEnergy) / (energyChannels - 1);
		// Build the axis
		final double[] axis = new double[energyChannels];
		for (int i = 0; i < energyChannels; i++) {
			axis[i] = startEnergy + i * step;
		}
		return axis;
	}

	private double[] convertToBindingEnergy(double[] kineticEnergyAxis, double photonEnergy, double workFunction) {
		double[] bindingEnergy = new double[kineticEnergyAxis.length];
		for (int i = 0; i < bindingEnergy.length; i++) {
			bindingEnergy[i] = photonEnergy - kineticEnergyAxis[i]- workFunction;
		}
		return bindingEnergy;
	}

	private double[][] constructImage(){
		// Get the expected image size
		final int energyChannels = getTotalPointsIteration();
		final int yChannels = getSlices();

		// Get the image data from the IOC
		final double[] image1DArray = getImage(energyChannels*yChannels);

		// Reshape the data
		final double[][] image2DArray = new double[yChannels][energyChannels];
		for (int i = 0; i < yChannels; i++) {
			System.arraycopy(image1DArray, (i * energyChannels), image2DArray[i], 0, energyChannels);
		}
		return image2DArray;
	}

	private void updateSummedSpectrum(double[] latestSpectrum, int currentPointIteration, int currentIteration) {
		if(currentIteration > 0) {
			summedSpectrum[currentPointIteration-1] += latestSpectrum[currentPointIteration-1];
		} else {
			summedSpectrum = latestSpectrum;
		}
	}

	private double getPhotonEnergy() {
		try {
			return (double)pvProvider.getPhotonEnergy().getPosition();
		} catch (DeviceException e) {
			final String msg = "Error getting photon energy";
			throw new RuntimeException(msg, e);
		}
	}

	private double getWorkFunction() {
		return pvProvider.getWorkFunction();
	}

	public void destroy() {

	}

	private Channel getChannel(String fullPvName) throws TimeoutException, CAException {
		Channel channel = channelMap.get(fullPvName);
		if (channel == null) {
			channel = controller.createChannel(fullPvName);
			channelMap.put(fullPvName, channel);
		}
		return channel;
	}

	@Override
	public void addIObserver(IObserver observer) {
		observableComponent.addIObserver(observer);
	}

	@Override
	public void deleteIObserver(IObserver observer) {
		observableComponent.deleteIObserver(observer);
	}

	@Override
	public void deleteIObservers() {
		observableComponent.deleteIObservers();
	}

	private void notifyListeners(Object evt) {
		observableComponent.notifyIObservers(this, evt);
	}

	public AnalyserPVProvider getPvProvider() {
		return pvProvider;
	}

	public void setPvProvider(AnalyserPVProvider pvProvider) {
		this.pvProvider = pvProvider;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	/**
	 * Get updates from the analyser about iteration
	 */
	@Override
	public void update(Object source, Object arg) {
		if(arg instanceof SpecsIterationNumberUpdate specsIterationNumberUpdate) {
			currentIteration = specsIterationNumberUpdate.getIterationNumber();
		} else if(arg instanceof SpecsRegionStartUpdate specsRegionStartUpdate) {
			summedSpectrum = null;
			requestedIterations = specsRegionStartUpdate.getRequestedIterations();
			currentRegionName = specsRegionStartUpdate.getCurrentRegionName();
			positionString = specsRegionStartUpdate.getPositionString();
			currentPhotonEnergy = getPhotonEnergy();
		} else if(arg instanceof SpecsPhoibosSequenceFileUpdate) {
			notifyListeners(arg);
		}
	}

}
