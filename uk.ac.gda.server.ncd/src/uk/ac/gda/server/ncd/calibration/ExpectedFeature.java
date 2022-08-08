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

package uk.ac.gda.server.ncd.calibration;

import java.io.Serializable;

public class ExpectedFeature implements Serializable {
	public enum FeatureType {
		SCAN, DERIVATIVE, BOTH;
	}

	private final double bragg;
	private final FeatureType type;
	public ExpectedFeature(double bragg, FeatureType type) {
		this.bragg = bragg;
		this.type = type;
	}

	public double getBragg() {
		return bragg;
	}

	public FeatureType getType() {
		return type;
	}

	@Override
	public String toString() {
		return "Feature(" + bragg + ", " + type + ")";
	}
}
