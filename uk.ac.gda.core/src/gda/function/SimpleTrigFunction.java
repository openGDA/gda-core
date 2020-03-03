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

package gda.function;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.measure.Quantity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.util.QuantityFactory;

/**
 * Function of form: y = a * trigFunc( b * x) where trigFunc is sin, cos or tan
 */
public class SimpleTrigFunction<T extends Quantity<T>, R extends Quantity<R>> extends FindableFunction<T, R> {
	private static final Logger logger = LoggerFactory.getLogger(SimpleTrigFunction.class);

	// These are the values as specified in the xml
	private String outerConstant;

	private String innerConstant;

	private String trigFunc;

	// These are constructed from the Strings above
	private Quantity<R> constantA;

	private Quantity<?> constantB;

	private Method trigMethod;

	@Override
	public Quantity<R> apply(Quantity<T> xValue) {
		try {
			final double trigValue = (Double) trigMethod.invoke(null, constantB.multiply(xValue).getValue().doubleValue());
			return constantA.multiply(trigValue);
		} catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
			logger.error("Error evaluating {}", xValue, e);
		}

		return null;
	}

	/**
	 * Get the inner constant
	 *
	 * @return the innerConstant
	 */
	public String getInnerConstant() {
		return innerConstant;
	}

	/**
	 * Set the inner constant
	 *
	 * @param innerConstant
	 *            the innerConstant
	 */
	public void setInnerConstant(String innerConstant) {
		this.innerConstant = innerConstant;
		constantB = QuantityFactory.createFromString(innerConstant);
	}

	/**
	 * Get the outer constant
	 *
	 * @return the outerConstant
	 */
	public String getOuterConstant() {
		return outerConstant;
	}

	/**
	 * Set the outer constant
	 *
	 * @param outerConstant
	 *            the outerConstant
	 */
	public void setOuterConstant(String outerConstant) {
		this.outerConstant = outerConstant;
		constantA = QuantityFactory.createFromString(outerConstant);
	}

	/**
	 * Get the trig function
	 *
	 * @return the trig function
	 */
	public String getTrigFunc() {
		return trigFunc;
	}

	/**
	 * Set the trig function
	 *
	 * @param trigFunc
	 *            the trig function
	 */
	public void setTrigFunc(String trigFunc) {
		final Class<?>[] argumentsList = { double.class };
		this.trigFunc = trigFunc;

		try {
			trigMethod = Math.class.getDeclaredMethod(trigFunc, argumentsList);
		} catch (SecurityException | NoSuchMethodException e) {
			logger.error("Error setting trig function to {}", trigFunc, e);
		}
	}
}
