/*-
 * Copyright © 2021 Diamond Light Source Ltd.
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

import static org.eclipse.scanning.api.constants.PathConstants.POINTS;
import static org.eclipse.scanning.api.constants.PathConstants.START;
import static org.eclipse.scanning.api.constants.PathConstants.STOP;

/**
 * A model for a scan along one axis with start and stop positions and a number of points.
 * M = Number of points - 1
 * Nth point is at Start + Length * (N / M) for N in [0..M] with if boundsToFit is false
 * 	0th point is at Start for Number of points = 1
 * Nth point is at Start + Length * (N + 0.5) / (M + 1) for N in [0..M] if boundsToFit is true
 * 	0th point is at Start + Length/2 for Number of points = 1
 */
public class AxialPointsModel extends AbstractAxialModel implements IAxialModel {

	/** Start position for the scan */
	private double start;

	/** Stop position for the scan */
	private double stop;

	/** Number of steps during the scan */
	private int points;

	public AxialPointsModel() {
		// no-arg constructor for json
	}

	public AxialPointsModel(String name, double start, double stop, int points) {
		setName(name);
		this.start = start;
		this.stop = stop;
		this.points = points;
	}

	/**
	 * Constructor for a 'static' axial points model- one with a number of exposures but no movement
	 * @param name
	 * @param value
	 * @param points
	 */
	public AxialPointsModel(String name, double value, int points) {
		this(name, value, value, points);
	}

	public double getStart() {
		return start;
	}

	public void setStart(double newValue) {
		final double oldValue = this.start;
		this.start = newValue;
		this.pcs.firePropertyChange(START, oldValue, newValue);
	}

	public double getStop() {
		return stop;
	}

	public void setStop(double newValue) {
		final double oldValue = this.stop;
		this.stop = newValue;
		this.pcs.firePropertyChange(STOP, oldValue, newValue);
	}

	public int getPoints() {
		return points;
	}

	public void setPoints(int newValue) {
		final double oldValue = this.points;
		this.points = newValue;
		this.pcs.firePropertyChange(POINTS, oldValue, newValue);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + points;
		long temp;
		temp = Double.doubleToLongBits(start);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(stop);
		result = prime * result + (int) (temp ^ (temp >>> 32));
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
		AxialPointsModel other = (AxialPointsModel) obj;
		if (points != other.points)
			return false;
		if (Double.doubleToLongBits(start) != Double.doubleToLongBits(other.start))
			return false;
		if (Double.doubleToLongBits(stop) != Double.doubleToLongBits(other.stop))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "AxialPointsModel [start=" + start + ", stop=" + stop + ", points=" + points + ", " + super.toString() + "]";
	}

}
