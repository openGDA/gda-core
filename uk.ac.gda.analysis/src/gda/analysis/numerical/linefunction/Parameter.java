/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council Daresbury Laboratory
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

package gda.analysis.numerical.linefunction;

/**
 * Basic user-level interface class for holding a single "measurement" with positive and negative errors (to allow for
 * asymmetric errors). "IMeasurement" = "value" + "errorPlus" - "errorMinus"
 */
public class Parameter implements IParameter {

	private double value = 0.0;

	private double upperLimit = Double.MAX_VALUE;

	private double lowerLimit = Double.MIN_VALUE;

	private boolean fixed = false;

	private int group = -1;

	/**
	 * Constructor.
	 */
	public Parameter() {
	}

	/**
	 * Constructor.
	 * 
	 * @param p
	 */
	public Parameter(IParameter p) {
		this.value = p.getValue();
		this.upperLimit = p.getUpperLimit();
		this.lowerLimit = p.getLowerLimit();
		this.fixed = p.isFixed();
	}

	/**
	 * Constructor.
	 * 
	 * @param value
	 */
	public Parameter(double value) {
		// this.name = name;
		this.value = value;
	}

	/**
	 * Constructor.
	 * 
	 * @param value
	 * @param lowerLimit
	 * @param upperLimit
	 */
	public Parameter(double value, double lowerLimit, double upperLimit) {
		// this.name = name;
		this.value = value;
		this.lowerLimit = lowerLimit;
		this.upperLimit = upperLimit;
	}

	@Override
	public double getValue() {
		return value;
	}

	@Override
	public double getUpperLimit() {
		return this.upperLimit;
	}

	@Override
	public double getLowerLimit() {
		return this.lowerLimit;
	}

	@Override
	public boolean isFixed() {
		return fixed;
	}

	@Override
	public void setFixed(boolean b) {
		this.fixed = b;

	}

	@Override
	public void setLowerLimit(double lowerLimit) {
		if (lowerLimit > this.upperLimit) {
			System.out
					.println("Cannot set lower limit : You are trying to set the lower bound to greater than the upper limit");
			return;
		}

		if (value < lowerLimit) {
			System.out
					.println("Parameter value is lower than this new lower bound - Adjusting value to equal new lower bound value ");
			value = lowerLimit;
		}
		this.lowerLimit = lowerLimit;
	}

	@Override
	public void setUpperLimit(double upperLimit) {
		if (upperLimit < this.lowerLimit) {
			System.out
					.println("Cannot set upper limit : You are trying to set the upper bound to lower than the lower limit");
			return;
		}

		if (value > upperLimit) {
			System.out
					.println("Parameter value is higher than this new upper bound - Adjusting value to equal new upper bound value ");
			value = upperLimit;
		}

		this.upperLimit = upperLimit;
	}

	@Override
	public void setValue(double value) {
		if (value > upperLimit || value < lowerLimit) {
			System.out.println("Warning : The value you wish to set for this "
					+ "parameter as it is outside the set bounds ");
			// return;
		}
		this.value = value;
	}

	@Override
	public void setGroup(int newGroupNumber) {
		this.group = newGroupNumber;
	}

	@Override
	public int getGroup() {
		return this.group;
	}

}
