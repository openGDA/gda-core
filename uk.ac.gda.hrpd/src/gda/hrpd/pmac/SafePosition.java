/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

package gda.hrpd.pmac;

import java.util.function.Predicate;

import gda.factory.FindableBase;

/**
 * This data object defines the safe position to park a device when it is not in use while operating other devices around it.
 * It is used to store configurable positions for safe operation of devices on beamline so collision avoidance logic can be implemented elsewhere.
 */
public class SafePosition extends FindableBase implements Predicate<Double> {
	private double position;
	private double tolerance;

	@Override
	public boolean test(Double t) {
		return Math.abs(position - t) < tolerance;
	}

	public void checkPosition(String motorName, double position) {
		if (!test(position)) {
			throw new UnsafeOperationException(position, position,
					"Cannot proceed as " + motorName + "is not at safe position.");
		}
	}

	public double getPosition() {
		return position;
	}
	public void setPosition(double safeposition) {
		this.position = safeposition;
	}
	public double getTolerance() {
		return tolerance;
	}
	public void setTolerance(double safePositionTolerance) {
		if (safePositionTolerance < 0) {
			throw new IllegalArgumentException("Tolerance must be non-negative");
		}
		this.tolerance = safePositionTolerance;
	}

}
