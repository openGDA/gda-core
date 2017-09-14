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

package uk.ac.diamond.daq.devices.specs.phoibos.ui.helpers;

import java.util.List;

import org.apache.commons.lang.time.DurationFormatUtils;

import gda.factory.Finder;
import uk.ac.diamond.daq.devices.specs.phoibos.api.ISpecsPhoibosAnalyser;
import uk.ac.diamond.daq.devices.specs.phoibos.api.SpecsPhoibosRegion;
import uk.ac.diamond.daq.devices.specs.phoibos.api.SpecsPhoibosSequence;

/**
 * Helper class for estimating the running time of SPECS regions and sequences for displaying to the user in the GUI. It
 * also handles formatting the time nicely.
 *
 * @author James Mudd
 */
public final class SpecsPhoibosTimeEstimator {

	private static final double detectorEnergyWidth;

	private SpecsPhoibosTimeEstimator() {
		// Prevent instances
	}

	static {
		// Get an analyser
		List<ISpecsPhoibosAnalyser> analysers = Finder.getInstance()
				.listLocalFindablesOfType(ISpecsPhoibosAnalyser.class);
		if (analysers.size() != 1) {
			throw new RuntimeException("No Analyser was found! (Or more than 1)");
		}
		detectorEnergyWidth =  analysers.get(0).getDetectorEnergyWidth();
	}

	/**
	 * Estimates the run time of the region and provides a formatted string to display to the user
	 *
	 * @param region The region to estimate
	 * @return Formatted string of the region run time
	 */
	public static String estimateRegionTime(SpecsPhoibosRegion region) {
		long timeInMs = estimateRegionTimeMs(region);

		// Convert ms to HH:MM:SS string
		return formatDuration(timeInMs);
	}

	public static String estimateSequenceRunTime(SpecsPhoibosSequence sequence) {
		long totalTimeMs = sequence.getEnabledRegions().stream(). // Only consider enabled regions
				mapToLong(SpecsPhoibosTimeEstimator::estimateRegionTimeMs). // Get the time per region (ms)
				sum(); // Add them up

		// Format the time and return
		return formatDuration(totalTimeMs);
	}

	private static String formatDuration(long timeInMs) {
		return DurationFormatUtils.formatDuration(timeInMs, "H:mm:ss");
	}

	/**
	 * Estimate the run time of the region in ms
	 *
	 * @param region The region to estimate
	 * @return The estimated run time of the region in ms
	 */
	private static long estimateRegionTimeMs(SpecsPhoibosRegion region) {
		long timeInMs = Math.round(region.getExposureTime()*1000);

		if("Fixed Transmission".equals(region.getAcquisitionMode())) { // i.e. a swept scan
			// Add one detectorEnergyWidth for the pre-scan
			long numberOfEnergySteps = Math.round((getEnergyWidth(region) + detectorEnergyWidth ) / region.getStepEnergy());
			timeInMs *= numberOfEnergySteps;
		}

		// Multiply by iterations
		timeInMs *= region.getIterations();
		return timeInMs;
	}

	private static double getEnergyWidth(SpecsPhoibosRegion region) {
		return Math.abs(region.getEndEnergy() - region.getStartEnergy());
	}

}
