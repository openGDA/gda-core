/*-
 * Copyright © 2020 Diamond Light Source Ltd.
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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.StaticPosition;

/**
 * A model representing a multi-scan with interpolated positions. A point generator created
 * from this model will run the points for each contained model consecutively, but in-between
 * the points for each model the interpolatation position of the same index will be moved to.
 */
public class InterpolatedMultiScanModel extends ConsecutiveMultiModel {

	public static final String PROPERTY_NAME_BETWEEN_SCAN_POSITIONS = "interpolatedPositions";

	private List<IPosition> interpolationPositions = new ArrayList<>();

	public List<IPosition> getInterpolationPositions() {
		return normalize(interpolationPositions);
	}

	public void setInterpolationPositions(List<IPosition> interpolationPositions) {
		pcs.firePropertyChange(PROPERTY_NAME_BETWEEN_SCAN_POSITIONS, this.interpolationPositions, interpolationPositions);
		this.interpolationPositions = interpolationPositions;
	}

	public void addInterpolationPosition(IPosition position) {
		final List<IPosition> newInterpolationPositions = new ArrayList<>(interpolationPositions);
		newInterpolationPositions.add(position);
		pcs.firePropertyChange(PROPERTY_NAME_BETWEEN_SCAN_POSITIONS, interpolationPositions, newInterpolationPositions);
		interpolationPositions = newInterpolationPositions;
	}

	private List<IPosition> normalize(List<IPosition> positions) {
		// return a list the same size as the list of models, with nulls replaced
		// by StaticPositions, and indices set
		final List<IPosition> newPositions = new ArrayList<>(getModels().size());
		newPositions.addAll(positions);
		while (newPositions.size() < getModels().size()) {
			newPositions.add(new StaticPosition());
		}
		for (int i = 0; i < newPositions.size(); i++) {
			IPosition position = newPositions.get(i);
			if (position == null) {
				position = new StaticPosition();
				newPositions.set(i, position);
			}
			position.setStepIndex(i);
		}
		return newPositions;
	}

	public void setInterpolationPosition(int index, IPosition position) {
		if (index < 0 || index >= getModels().size()) {
			throw new IndexOutOfBoundsException("Index must be between 0 and " + getModels().size());
		}

		final List<IPosition> newPositions = normalize(interpolationPositions);
		newPositions.set(index, position);
		pcs.firePropertyChange(PROPERTY_NAME_BETWEEN_SCAN_POSITIONS, interpolationPositions, newPositions);
		interpolationPositions = newPositions;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((interpolationPositions == null) ? 0 : interpolationPositions.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		InterpolatedMultiScanModel other = (InterpolatedMultiScanModel) obj;
		if (interpolationPositions == null) {
			if (other.interpolationPositions != null)
				return false;
		} else if (!interpolationPositions.equals(other.interpolationPositions))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " [interpolationPositions=" + interpolationPositions + ", getModels()="
				+ getModels() + "]";
	}

}
