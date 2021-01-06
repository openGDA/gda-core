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

import org.eclipse.scanning.api.annotation.ui.DeviceType;
import org.eclipse.scanning.api.annotation.ui.FieldDescriptor;

public class AxialPointsModel extends AbstractPointsModel {

	@FieldDescriptor(label="Device", device=DeviceType.SCANNABLE, fieldPosition=0)
	private String name;

	@FieldDescriptor(label="Start", scannable="name", hint="This is the start position for the scan", fieldPosition=1) // The scannable lookup gets the units
	private double start;

	@FieldDescriptor(label="Stop", scannable="name", hint="This is the stop position for the scan", fieldPosition=2) // The scannable lookup gets the units
	private double stop;

	@FieldDescriptor(label="Points", scannable="name", hint="This is the number of steps during the scan", fieldPosition=3) // The scannable lookup gets the units
	private int points;

	public AxialPointsModel() {

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

	public void setStart(double start) {
		this.start = start;
	}

	public double getStop() {
		return stop;
	}

	public void setStop(double stop) {
		this.stop = stop;
	}

	public int getPoints() {
		return points;
	}

	public void setPoints(int points) {
		this.points = points;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		if (!super.equals(obj))
			return false;
		AxialPointsModel other = (AxialPointsModel) obj;
		if (Double.doubleToLongBits(start) != Double.doubleToLongBits(other.start))
			return false;
		if (points != other.points)
			return false;
		return (Double.doubleToLongBits(stop) == Double.doubleToLongBits(other.stop));
	}



}
