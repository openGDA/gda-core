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

/**
 * Base class for path models which require knowledge of beam size
 * <p>
 * Note: this class extends {@link AbstractTwoAxisGridModel} as its only subclass is a type
 * of grid model. This prevents having to duplicate properties as Java does not
 * support multiple inheritance. If this were to change then this class should
 * extend {@link AbstractBoundingBoxModel} directly and duplication of
 * {@link AbstractTwoAxisGridModel} properties would be unavoidable.
 * Note: x and y must be in lower case in getter and setter names for JFace bindings to work correctly
 */
public abstract class AbstractOverlapModel extends AbstractTwoAxisGridModel {

	private double overlap;

	private double xBeamSize;
	private double yBeamSize;

	public double getxBeamSize() {
		return xBeamSize;
	}
	public void setxBeamSize(double xBeamSize) {
		this.xBeamSize = xBeamSize;
	}
	public double getyBeamSize() {
		return yBeamSize;
	}
	public void setyBeamSize(double yBeamSize) {
		this.yBeamSize = yBeamSize;
	}
	public void setBeamSize(Object[] position) {
		xBeamSize = (double) position[0];
		yBeamSize = (double) position[1];
	}

	/**
	 * Set the required beam overlap produced by adjacent points
	 * @param overlap between 0 (inclusive) and 1 (exclusive)
	 */
	public void setOverlap(double overlap) {
		double oldValue = this.overlap;
		this.overlap = overlap;
		pcs.firePropertyChange("overlap", oldValue, overlap);
	}

	public double getOverlap() {
		return overlap;
	}

	@Override
	public String toString() {
		return "overlap=" + overlap + ", xBeamSize=" + xBeamSize + ", yBeamSize" + yBeamSize;
	}

}
