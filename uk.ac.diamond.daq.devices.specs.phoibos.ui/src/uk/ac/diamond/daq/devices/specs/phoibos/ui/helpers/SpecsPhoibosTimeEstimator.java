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

import org.apache.commons.lang.time.DurationFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;
import uk.ac.diamond.daq.devices.specs.phoibos.api.SpecsPhoibosRegion;
import uk.ac.diamond.daq.devices.specs.phoibos.api.SpecsPhoibosSequence;

/**
 * Helper class for estimating the running time of SPECS regions and sequences for displaying to the user in the GUI. It
 * also handles formatting the time nicely.
 *
 * @author James Mudd
 */
public final class SpecsPhoibosTimeEstimator {
	private static final Logger log = LoggerFactory.getLogger(SpecsPhoibosTimeEstimator.class);

	private static final String TIME_OFFSET_KEY = "gda.specs.time.estimator.time.offset";
	private static final String DETECTOR_SHIFT_KEY = "gda.specs.time.estimator.detector.shift";
	private static final String DETECTOR_DEADTIME_KEY = "gda.specs.time.estimator.detector.deadtime";

	private static final double TIME_OFFSET;
	private static final double DETECTOR_SHIFT;
	private static final double DETECTOR_DEADTIME;

	static {
		TIME_OFFSET = LocalProperties.getDouble(TIME_OFFSET_KEY, 2.4);
		DETECTOR_SHIFT = LocalProperties.getDouble(DETECTOR_SHIFT_KEY, 0.11);
		DETECTOR_DEADTIME = LocalProperties.getDouble(DETECTOR_DEADTIME_KEY, 0.082);
	}

	private SpecsPhoibosTimeEstimator() {
		// Prevent instances
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

	private static final String SNAPSHOT_ACQUISTION_MODE = "Snapshot";

	private static boolean isNotSnapshotMode (SpecsPhoibosRegion region) {
		return !SNAPSHOT_ACQUISTION_MODE.equals(region.getAcquisitionMode());
	}

	/**
	 * Estimated scan(s) time
	 *
	 * From ticket B07-435
	 * TotalTime = NumberOfIteration*(Time_Offset+(EndEnergy-StartEnergy+Detector_Shift*PassEnergy)/energyStep*(ExposureTime+Detector_DeadTime))
	 * Note detector motion time ignored if in snapshot mode
	 *
	 * @param region The region to estimate
	 * @return time in H:mm:ss format
	 */
	public static String estimateRegionTime(SpecsPhoibosRegion region) {
		return formatDuration(estimateRegionTimeMs(region));
	}


	/**
	 * Estimate the run time of the region in ms
	 *
	 * @param region The region to estimate
	 * @return The estimated run time of the region in ms
	 */
	private static long estimateRegionTimeMs(SpecsPhoibosRegion region) {
		double detectorMoveTime = 1;
		if (isNotSnapshotMode(region)) {
			detectorMoveTime = getEnergyWidth(region) + DETECTOR_SHIFT * region.getPassEnergy() / region.getStepEnergy();
		}
		double imagingTime  = region.getExposureTime() + DETECTOR_DEADTIME;
		int iterations = region.getIterations();
		double totalTime = iterations * (TIME_OFFSET + detectorMoveTime * imagingTime);

		if (log.isTraceEnabled()) {
			StringBuilder message = new StringBuilder();
			message.append(String.format("Estimated scan time(s): %6.3f", totalTime));
			if (isNotSnapshotMode(region)) {
				message.append(String.format(", Detector Move Time(s): %6.3f", detectorMoveTime));
			}
			message.append(String.format(", imaging time(s): %6.3f, iterations: %d", imagingTime, iterations));
			log.trace(message.toString());
		}

		return Math.round(totalTime * 1000.0);
	}

	private static double getEnergyWidth(SpecsPhoibosRegion region) {
		return Math.abs(region.getEndEnergy() - region.getStartEnergy());
	}

}
