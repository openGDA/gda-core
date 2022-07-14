/*-
 * Copyright © 2021 Diamond Light Source Ltd.
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

package org.eclipse.scanning.api.points.models;

import java.math.BigDecimal;

public interface IBoundsToFit {

	static final String PROPERTY_NAME_BOUNDS_TO_FIT = "boundsToFit";
	static final String PROPERTY_DEFAULT_BOUNDS_FIT = "org.eclipse.mapping.boundsToFit";

	public boolean isBoundsToFit();

	public void setBoundsToFit(boolean boundsToFit);

	default void defaultBoundsToFit() {
		setBoundsToFit(Boolean.getBoolean(PROPERTY_DEFAULT_BOUNDS_FIT));
	}

	public default int getPointsOnLine(double length, double step) {
		final int points = BigDecimal.valueOf(0.01 * step + length).divideToIntegralValue(BigDecimal.valueOf(step)).intValue();
		// To allow for EnforcedShape to return the correct (but invalid) negative number of steps if required.
		// + 1 step allows for the point at the end of the model when not boundsToFit
		return isBoundsToFit() ? points : points + (int) (1 * Math.signum(points));
	}

	public default double getStart(double start, double step) {
		if (isBoundsToFit()) start += step / 2.0;
		return start;
	}

	public default double getStop(double start, double length, double step) {
		if (length == 0 && step == 0) return start;
		// Trim region that would not have been stepped in, -1 because of point at edge
		return start + (getPointsOnLine(length, step) - 1) * step;
	}

}
