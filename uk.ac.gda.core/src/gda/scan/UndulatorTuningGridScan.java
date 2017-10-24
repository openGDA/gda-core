/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council Daresbury Laboratory
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

package gda.scan;

import gda.device.Scannable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to control the stepped movement of a Scannable object.
 * <p>
 * At each step, after movement, the readout() method of all object in the DetectorBase.activeDetectors arraylist is
 * called.
 */
public class UndulatorTuningGridScan extends GridScanMoveToOnly {

	private static final Logger logger = LoggerFactory.getLogger(UndulatorTuningGridScan.class);

	private Scannable extraScannable;

	private double freq;

	private double incr;

	private double monoMoved;

	/**
	 * @param ve
	 * @param start
	 * @param stop
	 * @param step
	 * @param time
	 * @param units
	 * @param extraScannable
	 * @param frequency
	 * @param toBeReportedOne
	 * @param toBeReportedTwo
	 */
	public UndulatorTuningGridScan(Scannable ve, Object start, Object stop, Object step, Object time, Object units,
			Scannable extraScannable, Object frequency, Scannable toBeReportedOne, Scannable toBeReportedTwo) {
		super(ve, start, stop, step, time, units);
		// This extraScannable is the one that moves - usually the
		// UndulatorEnergy
		this.extraScannable = extraScannable;

		allScannables.add(toBeReportedOne);
		allScannables.add(toBeReportedTwo);

		incr = Double.parseDouble(step.toString());
		if (frequency.equals("EveryPoint"))
			freq = incr;
		else {
			freq = Double.parseDouble(frequency.toString());
			// freq must have the same sign as incr
			freq = freq * (incr / Math.abs(incr));
		}
	}

	@Override
	public void moveStepIncrement(int index) throws Exception {
		try {
			super.moveStepIncrement(index);
			monoMoved += incr;
			if (extraScannable != null && Math.abs(monoMoved) >= Math.abs(freq)) {
				extraScannable.moveTo(allScannables.get(0).getPosition());
				logger.info("Undulator retuned to  " + allScannables.get(0).getPosition());
				monoMoved -= freq;
			}
		} catch (Exception e) {
			if (e instanceof InterruptedException) {
				throw e;
			}
			throw new Exception("Could not move step increment. Index: " + index, e);
		}
	}

	@Override
	public void moveToStart() throws Exception {
		try {
			super.moveToStart();
			monoMoved = 0.0;
			if (extraScannable != null)
				extraScannable.moveTo(allScannables.get(0).getPosition());
		} catch (Exception e) {
			if (e instanceof InterruptedException) {
				throw e;
			}
			throw new Exception("Could not move to start", e);
		}
	}
}
