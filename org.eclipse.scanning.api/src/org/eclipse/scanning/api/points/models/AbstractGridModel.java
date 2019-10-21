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

	@FieldDescriptor(label="Vertical Orientation")
	private boolean verticalOrientation = false;

	/**
	 * By default the horizontal axis is the scanned first, i.e. is the fast axis. If this
	 * property is set the vertical axis is the scanned first.
	 *
	 * @return <code>true</code> if the vertical axis is scanned first, <code>false</code> if
	 * the horizontal axis is scanned first.
	 */

	public boolean isVerticalOrientation() {
		return verticalOrientation;
	}

	public void setVerticalOrientation(boolean verticalOrientation) {
		boolean oldValue = this.verticalOrientation;
		this.verticalOrientation = verticalOrientation;
		this.pcs.firePropertyChange("verticalOrientation", oldValue, verticalOrientation);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (verticalOrientation ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (!super.equals(obj))
			return false;
		AbstractGridModel other = (AbstractGridModel) obj;
		return (verticalOrientation == other.verticalOrientation);
	}

	@Override
	public String toString() {
		return "AbstractGridModel [verticalOrientation=" + verticalOrientation
				+ ", " + super.toString() + "]";
	}

}
