/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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

import org.eclipse.scanning.api.annotation.ui.FieldDescriptor;

/**
 * Abstract superclass for models representing a raster scan within a rectangular box in two-dimensional space.
 */
public abstract class AbstractGridModel extends AbstractBoundingBoxModel {

	@FieldDescriptor(label="Orientation")
	private Orientation orientation = Orientation.HORIZONTAL;

	public enum Orientation {
		HORIZONTAL("Horizontal"), VERTICAL("Vertical");

		private final String orientationString;

		Orientation(String orientationString) {
	        this.orientationString = orientationString;
	    }

	    @Override
		public String toString() {
	        return this.orientationString;
	    }}

	/**
	 * By default the horizontal axis is the scanned first, i.e. is the fast axis. If this
	 * property is set the vertical axis is the scanned first.
	 *
	 * @return <code>true</code> if the vertical axis is scanned first, <code>false</code> if
	 * the horizontal axis is scanned first.
	 */

	public Orientation getOrientation() {
		return orientation;
	}

	public void setOrientation(Orientation orientation) {
		Orientation oldValue = this.orientation;
		this.orientation = orientation;
		this.pcs.firePropertyChange("orientation", oldValue, orientation);
	}

	public boolean isVerticalOrientation() {
		return orientation.equals(Orientation.VERTICAL);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (orientation.equals(Orientation.VERTICAL) ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (!super.equals(obj))
			return false;
		AbstractGridModel other = (AbstractGridModel) obj;
		return orientation.equals(other.orientation);
	}

	@Override
	public String toString() {
		return "AbstractGridModel [orientation=" + orientation
				+ ", " + super.toString() + "]";
	}

}
