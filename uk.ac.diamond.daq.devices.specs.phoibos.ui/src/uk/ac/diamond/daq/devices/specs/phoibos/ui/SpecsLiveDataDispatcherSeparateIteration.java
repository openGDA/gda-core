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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.observable.IObserver;
import uk.ac.diamond.daq.devices.specs.phoibos.api.SpecsIterationNumberUpdate;
import uk.ac.diamond.daq.devices.specs.phoibos.api.SpecsPhoibosLiveDataUpdate;
import uk.ac.diamond.daq.devices.specs.phoibos.api.SpecsPhoibosLiveIterationSpectraUpdate;
import uk.ac.diamond.daq.devices.specs.phoibos.api.SpecsRegionStartUpdate;

/**
 * This class is instantiated via Spring and used as a client side dispatcher
 * of analyser related data for the purpose of data visualisation. To avoid the
 * synchronisation issues that appear when getting data from both server and
 * client.
 */
public class SpecsLiveDataDispatcherSeparateIteration extends SpecsLiveDataDispatcher implements ISpecsLiveDataDispatcher, IObserver {
	private static final Logger logger = LoggerFactory.getLogger(SpecsLiveDataDispatcherSeparateIteration.class);
	private int currentIteration;
	private int requestedIterations;
	private double[] summedSpectrum;
	private double[] fullIterationSummedSpectrum;
	private int cachedIteration;

	@Override
	protected void getIntialValues() {
		super.getIntialValues();
		requestedIterations = analyser.getIterations();
		currentIteration = analyser.getCurrentIteration();
	}

	@Override
	protected SpecsPhoibosLiveDataUpdate getDataUpdate(double[] spectrum, int currentPointFromEvent) {
		final int pointInIteration = getPointInIteration();
		final int totalPoints = getTotalPoints();
		// Create an identical but separate object for event to work
		final double[] spectrumCopy = spectrum.clone();
		final double[] summedSpectrumCopy = calculateSummedSpectrum(spectrum).clone();

		final double[] keEnergyAxis = generateEnergyAxis(getLowEnergy(), getHighEnergy(), getTotalPointsIteration());
		final double[] beEnergyAxis = convertToBindingEnergy(keEnergyAxis, getCurrentPhotonEnergy(), getWorkFunction());

		return new SpecsPhoibosLiveIterationSpectraUpdate.Builder()
				.iterationNumber(currentIteration+1)
				.iterationSpectrum(spectrumCopy)
				.acquisitionMode(acquisitionMode)
				.regionName(currentRegionName)
				.positionString(positionString)
				.totalPoints(totalPoints * requestedIterations)
				.currentPoint(currentPointFromEvent + (totalPoints * currentIteration))
				.totalIterations(requestedIterations)
				.currentPointInIteration(pointInIteration)
				.spectrum(summedSpectrumCopy)
				.keEnergyAxis(keEnergyAxis)
				.beEnergyAxis(beEnergyAxis).build();
				}

	private double[] calculateSummedSpectrum(double[] latestSpectrum) {
		if (isNewIteration()) {
			fullIterationSummedSpectrum = summedSpectrum.clone();
			cachedIteration = currentIteration;
		}
		if (fullIterationSummedSpectrum == null) {
			summedSpectrum = latestSpectrum;
		} else {
			for (int i=0; i< latestSpectrum.length;i++) {
				summedSpectrum[i] = fullIterationSummedSpectrum[i] + latestSpectrum[i];
			}
		}
		return summedSpectrum;
	}

	private boolean isNewIteration() {
		return currentIteration!=cachedIteration;
	}

	/**
	 * Get updates from the analyser about iteration
	 */
	@Override
	public void update(Object source, Object arg) {
		if(arg instanceof SpecsIterationNumberUpdate specsIterationNumberUpdate) {
			currentIteration = specsIterationNumberUpdate.getIterationNumber();
			updateCurrentPhotonEnergy();
		}
		else {
			super.update(source, arg);
		}
	}

	@Override
	protected void handleSpecsRegionStartUpdate(SpecsRegionStartUpdate specsRegionStartUpdate) {
		super.handleSpecsRegionStartUpdate(specsRegionStartUpdate);
		requestedIterations = specsRegionStartUpdate.getRequestedIterations();
		summedSpectrum = null;
		fullIterationSummedSpectrum = null;
		cachedIteration = 0;
		currentIteration = 0;
	}
}
