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
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class CalibrationEdge implements Serializable {
	private final UUID uid = UUID.randomUUID();
	private final String name;
	private final List<ExpectedFeature> features;
	private final double edgeEnergy;

	public CalibrationEdge(String name, double energy, List<ExpectedFeature> features) {
		this.name = name;
		this.edgeEnergy = energy;
		this.features = features;
	}

	@Override
	public String toString() {
		return "CalibrationEdge [name=" + getName() + ", features=" + getFeatures() + "]";
	}

	public String getName() {
		return name;
	}

	public List<ExpectedFeature> getFeatures() {
		return features;
	}

	public double getEdgeEnergy() {
		return edgeEnergy;
	}

	public UUID getUid() {
		return uid;
	}

	@Override
	public int hashCode() {
		return Objects.hash(uid);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CalibrationEdge other = (CalibrationEdge) obj;
		return Objects.equals(uid, other.uid);
	}

}
