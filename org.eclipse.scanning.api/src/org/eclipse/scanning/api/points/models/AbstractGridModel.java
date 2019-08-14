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

	@FieldDescriptor(label="Snake")
	private boolean snake = false;

	@FieldDescriptor(label="Vertical Orientation")
	private boolean verticalOrientation = false;

	/**
	 * A snake scan is a scan where each line of the scan is performed in the opposite direction
	 * to the previous one as show below:
	 * <pre>
	 * -------------------->
	 * <--------------------
	 * -------------------->
     * </pre>
     * Otherwise all lines of the scan are performed in the same direction
     * <pre>
     * -------------------->
     * -------------------->
     * -------------------->
     * </pre>
     * @return <code>true</code> if the scan is a snake scan, <code>false</code> otherwise
 	 */
	public boolean isSnake() {
		return snake;
	}

	public void setSnake(boolean snake) {
		boolean oldValue = this.snake;
		this.snake = snake;
		this.pcs.firePropertyChange("snake", oldValue, snake);
	}

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
		result = prime * result + (snake ? 1231 : 1237);
		result = prime * result + (verticalOrientation ? 1231 : 1237);
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
		AbstractGridModel other = (AbstractGridModel) obj;
		if (snake != other.snake)
			return false;
		if (verticalOrientation != other.verticalOrientation)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "AbstractGridModel [snake=" + snake + ", verticalOrientation=" + verticalOrientation
				+ ", " + super.toString() + "]";
	}

}
