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

package org.eclipse.scanning.api.points.models;

import static org.eclipse.scanning.api.constants.PathConstants.RANDOM_OFFSET;

import java.util.Map;
import java.util.Random;

/**
 * Previously PtychographyGridModel
 */
public class TwoAxisPtychographyModel extends AbstractOverlapModel {

	private int seed = new Random().nextInt();
	private double randomOffset = 0.05;

	public TwoAxisPtychographyModel() {
		setName("Ptychography Grid");
		setOverlap(0.5);
	}

	/**
	 * @return maximum offset as percentage of step size
	 */
	public double getRandomOffset() {
		return randomOffset;
	}

	public void setRandomOffset(double offset) {
		double oldOffset = randomOffset;
		randomOffset = offset;
		pcs.firePropertyChange(RANDOM_OFFSET, oldOffset, randomOffset);
	}

	public int getSeed() {
		return seed;
	}

	@Override
	public void updateFromPropertiesMap(Map<String, Object> properties) {
		super.updateFromPropertiesMap(properties);
		if (properties.containsKey(RANDOM_OFFSET)) {
			setRandomOffset(((Number) properties.get(RANDOM_OFFSET)).doubleValue());
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		long temp;
		temp = Double.doubleToLongBits(randomOffset);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + seed;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (!super.equals(obj))
			return false;
		TwoAxisPtychographyModel other = (TwoAxisPtychographyModel) obj;
		if (Double.doubleToLongBits(randomOffset) != Double.doubleToLongBits(other.randomOffset))
			return false;
		if (seed != other.seed)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " [seed=" + seed + ", randomOffset=" + randomOffset + ", "
				+ super.toString()+ "]";
	}
}
