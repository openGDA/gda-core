/*-
 * Copyright © 2018 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.beamcondition;

import java.util.function.BooleanSupplier;

import gda.device.EnumPositioner;
import gda.device.Scannable;

public final class Conditions {

	private Conditions() {}

	public static BeamCondition threshold(Scannable scannable, double threshold) {
		ScannableThresholdCheck check = new ScannableThresholdCheck();
		check.setScannable(scannable);
		check.setLowerLimit(threshold);
		return check;
	}

	public static BeamCondition limit(Scannable scannable, double limit) {
		ScannableThresholdCheck check = new ScannableThresholdCheck();
		check.setScannable(scannable);
		check.setUpperLimit(limit);
		return check;
	}

	public static BeamCondition between(Scannable scannable, double lowerLimit, double upperLimit) {
		ScannableThresholdCheck check = new ScannableThresholdCheck();
		check.setScannable(scannable);
		check.setLowerLimit(lowerLimit);
		check.setUpperLimit(upperLimit);
		return check;
	}

	public static BeamCondition isAt(Scannable scannable, double position) {
		return isAt(scannable, position, 0.01);
	}

	public static BeamCondition isAt(Scannable scannable, double position, double delta) {
		if (delta < 0) {
			throw new IllegalArgumentException("Precision error must be non-negative");
		}
		return between(scannable, position-delta, position+delta);
	}

	public static BeamCondition isAt(EnumPositioner positioner, String... positions) {
		return EnumPositionCheck.isAt(positioner, positions);
	}

	public static BeamCondition isNotAt(EnumPositioner positioner, String... positions) {
		return EnumPositionCheck.isNotAt(positioner, positions);
	}

	public static BeamCondition check(BooleanSupplier check) {
		return new BeamConditionWrapper("User function", check);
	}

	public static BeamCondition check(String name, BooleanSupplier check) {
		return new BeamConditionWrapper(name, check);
	}
}
