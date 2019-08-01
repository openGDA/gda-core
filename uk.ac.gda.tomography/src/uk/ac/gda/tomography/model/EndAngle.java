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
 * Defines the end angle for a rotation.
 */
public class EndAngle {

	/**
	 *
	 */
	public EndAngle() {
		super();
		// TODO Auto-generated constructor stub
	}
	/**
	 * @param rangeType
	 * @param numberRotation
	 * @param customAngle
	 */
	public EndAngle(RangeType rangeType, int numberRotation, double customAngle) {
		super();
		this.rangeType = rangeType;
		this.numberRotation = numberRotation;
		this.customAngle = customAngle;
	}

	public EndAngle(EndAngle endAngle) {
		this(endAngle.getRangeType(), endAngle.getNumberRotation(), endAngle.getCustomAngle());
	}

	private RangeType rangeType;

	/**
	 * Field for {@link RangeType#RANGE_360}
	 */
	private int numberRotation = Integer.MIN_VALUE;

	/**
	 * Felds for {@link RangeType#CUSTOM} case
	 */
	private double customAngle;

	public RangeType getRangeType() {
		return rangeType;
	}
	public void setRangeType(RangeType rangeType) {
		this.rangeType = rangeType;
	}
	public int getNumberRotation() {
		return numberRotation;
	}
	public void setNumberRotation(int numberRotation) {
		this.numberRotation = numberRotation;
	}
	public double getCustomAngle() {
		return customAngle;
	}
	public void setCustomAngle(double customAngle) {
		this.customAngle = customAngle;
	}
}
