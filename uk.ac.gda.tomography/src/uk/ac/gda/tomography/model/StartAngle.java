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

package uk.ac.gda.tomography.model;

/**
 * Defines the start angle for a rotation.
 *
 * @author Maurizio Nagni
 */
public class StartAngle {

	/**
	 *
	 */
	public StartAngle() {
		super();
	}
	/**
	 * @param start
	 * @param useCurrentAngle
	 * @param currentAngle
	 */
	public StartAngle(double start, boolean useCurrentAngle, double currentAngle) {
		super();
		this.start = start;
		this.useCurrentAngle = useCurrentAngle;
		this.currentAngle = currentAngle;
	}

	/**
	 * Deep clones an existing instance
	 * @param startAngle
	 */
	public StartAngle(StartAngle startAngle) {
		this(startAngle.getStart(), startAngle.isUseCurrentAngle(), startAngle.getCurrentAngle());
	}

	/**
	 * The angle from where the rotation starts
	 */
	private double start;
	/**
	 * Declares <code>start</code> and <code>currentAngle</code> to be the same
	 */
	private boolean useCurrentAngle;
	/**
	 * The angle from where the point has to be rotated to reach the start position
	 */
	private double currentAngle;

	/**
	 * @return the angle to start the rotation
	 */
	public double getStart() {
		return start;
	}
	public void setStart(double start) {
		this.start = start;
	}
	public boolean isUseCurrentAngle() {
		return useCurrentAngle;
	}
	public void setUseCurrentAngle(boolean useCurrentAngle) {
		this.useCurrentAngle = useCurrentAngle;
	}
	public double getCurrentAngle() {
		return currentAngle;
	}
	public void setCurrentAngle(double currentAngle) {
		this.currentAngle = currentAngle;
	}
}
