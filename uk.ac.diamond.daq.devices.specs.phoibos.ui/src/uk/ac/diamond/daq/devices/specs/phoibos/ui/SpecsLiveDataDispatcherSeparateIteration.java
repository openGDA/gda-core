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

	private short acquisitionMode;
	private int currentIteration;
	private int requestedIterations;
	private double[] summedSpectrum;

	@Override
	protected void getIntialValues() {
		super.getIntialValues();
		requestedIterations = analyser.getIterations();
		currentIteration = analyser.getCurrentIteration();
	}

	@Override
	protected SpecsPhoibosLiveDataUpdate.Builder createBuilder(double[] keEnergyAxis, double[] beEnergyAxis,int currentPointFromEvent) {
		final int pointInIteration = getPointInIteration();
		final int totalPointsIteration = getTotalPointsIteration();
		final double[] spectrum = getSpectrum(totalPointsIteration);
		// Create an identical but separate object for event to work
		final double[] spectrumCopy = spectrum.clone();
		calculuateSummedSpectrum(spectrum, pointInIteration, currentIteration);

		return new SpecsPhoibosLiveIterationSpectraUpdate.Builder()
			.copy(super.createBuilder(keEnergyAxis, beEnergyAxis, currentPointFromEvent))
			.iterationNumber(currentIteration+1)
			.iterationSpectrum(spectrumCopy)
			.acquisitionMode(acquisitionMode)
			.totalPoints(getTotalPoints() * requestedIterations)
			.totalIterations(requestedIterations)
			.spectrum(summedSpectrum)
			.currentPoint(currentPointFromEvent + (getTotalPoints() * currentIteration));
	}

	private double[] calculuateSummedSpectrum(double[] latestSpectrum, int currentPointIteration, int currentIteration) {
		if(currentIteration > 0) {
			if (acquisitionMode == 1) {
				for (int i=0; i< summedSpectrum.length;i++) {
					summedSpectrum[i] += latestSpectrum[i];
				}
			} else {
				summedSpectrum[currentPointIteration-1] += latestSpectrum[currentPointIteration-1];
			}
		} else {
			summedSpectrum = latestSpectrum;
		}
		return summedSpectrum;
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
	}
}
