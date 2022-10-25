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

package gda.rcp.ncd.calibration;

import uk.ac.gda.server.ncd.calibration.ExpectedFeature;
import uk.ac.gda.server.ncd.calibration.ExpectedFeature.FeatureType;

public class ObservedFeature {
	private final ExpectedFeature feature;
	private double observation;
	/** Allow some features to be ignored if they aren't clear in the data */
	private boolean active = true;

	public ObservedFeature(ExpectedFeature feature) {
		this.feature = feature;
		observation = feature.getBragg();
	}

	public FeatureType getType() {
		return feature.getType();
	}

	public double getExpected() {
		return feature.getBragg();
	}

	public double getObservation() {
		return observation;
	}

	public void setObservation(double observation) {
		this.observation = observation;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	@Override
	public String toString() {
		return String.format("ObservedFeature[exp=%f, obs=%f%s]", getExpected(), observation, active ? "" : " (inactive)");
	}
}
