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

	/**
	 * An enumeration of the types of image in a tomograpy scan. The values of the
	 * image key are as specified in the
	 * <a href="https://manual.nexusformat.org/classes/applications/NXtomo.html#nxtomo">
	 * NXtomo application definition</a> for the <code>image_key</code> field of the
	 * <code>NXdetector</code> and should not be changed.
	 */
	public enum ImageType {
		NORMAL(0), FLAT(1), DARK(2), INVALID(3);

		int imageKey;

		private ImageType(int imageKey) {
			this.imageKey = imageKey;
		}

		public int getImageKey() {
			return imageKey;
		}
	}

	public static final String PROPERTY_NAME_INTERPOLATED_POSITIONS = "interpolatedPositions";

	private List<IPosition> interpolatedPositions = new ArrayList<>();

	private List<ImageType> imageTypes = new ArrayList<>();

	public List<IPosition> getInterpolatedPositions() {
		return normalize(interpolatedPositions);
	}

	public void setInterpolatedPositions(List<IPosition> interpolatedPositions) {
		pcs.firePropertyChange(PROPERTY_NAME_INTERPOLATED_POSITIONS, this.interpolatedPositions, interpolatedPositions);
		this.interpolatedPositions = interpolatedPositions;
	}

	public void addInterpolatedPosition(IPosition position) {
		final List<IPosition> newInterpolatedPositions = new ArrayList<>(interpolatedPositions);
		newInterpolatedPositions.add(position);
		setInterpolatedPositions(newInterpolatedPositions);
	}

	public void setInterpolatedPosition(int index, IPosition position) {
		if (index < 0 || index >= getModels().size()) {
			throw new IndexOutOfBoundsException("Index must be between 0 and " + getModels().size());
		}

		final List<IPosition> newPositions = normalize(interpolatedPositions);
		newPositions.set(index, position);
		setInterpolatedPositions(newPositions);
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

	public List<ImageType> getImageTypes() {
		if (imageTypes.size() != getModels().size()) {
			throw new IllegalStateException("The image types list must be the same size ");
		}
		return imageTypes;
	}

	public void setImageTypes(List<ImageType> imageTypes) {
		this.imageTypes = imageTypes;
	}

	public void addImageType(ImageType imageType) {
		imageTypes.add(imageType);
	}

	public void setImageType(ImageType imageType, int index) {
		if (index < 0 || index >= getModels().size()) {
			throw new IndexOutOfBoundsException("Index must be between 0 and " + getModels().size());
		}

		if (index == imageTypes.size()) {
			addImageType(imageType);
		} else {
			imageTypes.set(index, imageType);
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((interpolatedPositions == null) ? 0 : interpolatedPositions.hashCode());
		result = prime * result + ((imageTypes == null) ? 0 : imageTypes.hashCode());
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
		if (interpolatedPositions == null) {
			if (other.interpolatedPositions != null)
				return false;
		} else if (!interpolatedPositions.equals(other.interpolatedPositions))
			return false;
		if (imageTypes == null) {
			if (other.imageTypes != null)
				return false;
		} else if (!imageTypes.equals(other.imageTypes)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() +
				" [interpolationPositions=" + interpolatedPositions +
				", [imageTypes=" + imageTypes +
				", getModels()=" + getModels() +
				"]";
	}

}
