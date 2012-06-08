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
 * AbstractParameter Class
 */
public abstract class AbstractParameter {
	/**
	 * the name of the parameter
	 */
	private String name = null;

	/**
	 * The value of the parameter
	 */
	private double value = 0.0;

	/**
	 * The upper limit default : Double.MAX_VALUE
	 */
	private double upperLimit = Double.MAX_VALUE;

	/**
	 * the lower limit of the parameter default : Double.MIN_VALUE
	 */
	private double lowerLimit = Double.MIN_VALUE;

	/**
	 * parameter fixed ? default : false
	 */
	private boolean fixed = false;

	/**
	 * Constructor.
	 */
	public AbstractParameter() {
	}

	/**
	 * Constructor.
	 * 
	 * @param p
	 */
	public AbstractParameter(IParameter p) {
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
	public AbstractParameter(double value) {
		this.value = value;
	}

	/**
	 * Constructor.
	 * 
	 * @param value
	 * @param lowerLimit
	 * @param upperLimit
	 */
	public AbstractParameter(double value, double lowerLimit, double upperLimit) {
		this.value = value;
		this.lowerLimit = lowerLimit;
		this.upperLimit = upperLimit;
	}

	/**
	 * @return The value of the parameter
	 */
	public double getValue() {
		return value;
	}

	/**
	 * @return The upper limit of the parameter
	 */
	public double getUpperLimit() {
		return this.upperLimit;
	}

	/**
	 * @return The lower limit of the parameter
	 */
	public double getLowerLimit() {
		return this.lowerLimit;
	}

	/**
	 * @return Is the parameter fixed ?
	 */
	public boolean isFixed() {
		return fixed;
	}

	/**
	 * Set the parameter to be fixed or free
	 * 
	 * @param b
	 */
	public void setFixed(boolean b) {
		this.fixed = b;

	}

	/**
	 * Set the lower limit on the parameter
	 * 
	 * @param lowerLimit
	 */
	public void setLowerLimit(double lowerLimit) {
		if (lowerLimit > this.upperLimit) {
			System.out.println("Cannot set lower limit : You are trying to "
					+ "set the lower bound to greater than the upper limit");
			return;
		}

		if (value < lowerLimit) {
			System.out.println("Parameter value is lower than this new lower "
					+ "bound - Adjusting value to equal new lower bound value ");
			value = lowerLimit;
		}
		this.lowerLimit = lowerLimit;
	}

	/**
	 * Set the upper limit of the parameter
	 * 
	 * @param upperLimit
	 */
	public void setUpperLimit(double upperLimit) {
		if (upperLimit < this.lowerLimit) {
			System.out.println("Cannot set upper limit : You are trying to set "
					+ "the upper bound to lower than the lower limit");
			return;
		}

		if (value < upperLimit) {
			// A basic message
			System.out.println("Parameter value is higher than this " + "new upper bound - Adjusting value to "
					+ "equal new upper bound value ");
			value = upperLimit;
		}

		this.upperLimit = upperLimit;
	}

	/**
	 * Set the value of the parameter
	 * 
	 * @param value
	 */
	public void setValue(double value) {
		if (value > upperLimit || value < lowerLimit) {
			// A basic message
			System.out.println("Cannot set value for this parameter as " + "it is outside the set bounds ");

			return;
		}
		this.value = value;
	}

	/**
	 * @return The name of the parameter
	 */
	public String getName() {
		return name;
	}
}
