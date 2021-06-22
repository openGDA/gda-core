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

package uk.ac.gda.client.properties.camera;

import java.util.List;

import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

/**
 * Defines the mapping from a camera array space to the beam drivers one.
 * <p>
 * The beam illuminates a sample its position is defined by the drivers.
 * The {@link #getMap()} matrix represents the mapping which transformation a 2D vector, typically a camera pixel point
 * to a vector in the drivers 2D space.
 *
 * @author Maurizio Nagni
 *
 */
public class CameraToBeamMap {

	/**
	 * The matrix defining the transformation from the camera space to the beam space
	 */
	private RealMatrix map;

	/**
	 * The offset is defined in the beam space
	 */
	private RealVector offset;
	private List<String> driver;
	private boolean active;

	public RealMatrix getMap() {
		return map;
	}
	public void setMap(RealMatrix map) {
		this.map = map;
	}

	public RealVector getOffset() {
		return offset;
	}
	public void setOffset(RealVector offset) {
		this.offset = offset;
	}
	public List<String> getDriver() {
		return driver;
	}
	public void setDriver(List<String> driver) {
		this.driver = driver;
	}
	public boolean isActive() {
		return active;
	}
	public void setActive(boolean active) {
		this.active = active;
	}
}
