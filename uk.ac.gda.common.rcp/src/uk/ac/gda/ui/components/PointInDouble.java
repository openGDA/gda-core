/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

package uk.ac.gda.ui.components;

/**
 * A class that holds x and y for scale. The normal point method wouldn't consider containing double values, but this
 * one does.
 */
public class PointInDouble {
	/**
	 * The x scale
	 */
	public double x;

	/**
	 * The x scale
	 */
	public double y;

	public PointInDouble() {
		setScale(0, 0);
	}

	public PointInDouble(double x, double y) {
		setScale(x, y);
	}

	public void setScale(double x, double y) {
		this.x = x;
		this.y = y;
	}

	public PointInDouble getScale() {
		return new PointInDouble(x, y);
	}
}
