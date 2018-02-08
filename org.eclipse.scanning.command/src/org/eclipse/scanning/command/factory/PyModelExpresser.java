/*-
 *******************************************************************************
 * Copyright (c) 2011, 2016 Diamond Light Source Ltd.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthew Gerring - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.scanning.command.factory;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.Objects;

import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.scanning.command.PyExpressionNotImplementedException;

/**
 * You must override at least one of the pyExpress(...) methods.
 *
 * @author Matthew Gerring
 *
 * @param <T> the model for which we are expressing.
 */
abstract class PyModelExpresser<T> {

	protected PyExpressionFactory factory;
	void setFactory(PyExpressionFactory f) {
		factory = f;
	}
	/**
	 * Call to express the model T as a python command.
	 * @param model
	 * @param rois
	 * @param verbose
	 * @return py string
	 */
	String pyExpress(T model, boolean verbose) throws Exception {
		return pyExpress(model, null, verbose);
	}
	/**
	 * Call to express the model T as a python command.
	 * @param model
	 * @param rois
	 * @param verbose
	 * @return py string
	 */
	String pyExpress(T model, Collection<IROI> rois, boolean verbose) throws Exception {
		throw new PyExpressionNotImplementedException("Cannot express "+model+" with rois "+rois);
	}

	/**
	 * Call from your implementation of pyExpress to get a verbose or concise expression for a boolean field
	 * e.g. getPythonBooleanExpression("continuous", model.isContinuous(), verbose);
	 * @param keyword
	 * @param isTrue
	 * @param verbose
	 * @return concise/verbose pyExpression fragment for keyword
	 */
	protected String getBooleanPyExpression(String keyword, boolean isTrue, boolean verbose) {
		String pythonBoolean = isTrue ? "True" : "False";
		return (verbose ? keyword + "=" : "") + pythonBoolean;
	}


	/**
	 * Call from your implementation of pyExpress to get a verbose or concise expression for rois.
	 * @param rois
	 * @param verbose
	 * @return pyExpression for ROIs, unless null or empty
	 * @throws Exception
	 */
	protected String getROIPyExpression(Collection<IROI> rois, boolean verbose) throws Exception {
		if (Objects.nonNull(rois) && !rois.isEmpty()) {
			StringBuilder sb = new StringBuilder();
			sb.append(", ");
			sb.append(verbose?"roi=":"");
			sb.append(factory.pyExpress(rois, verbose));
			return sb.toString();
		}
		return "";
	}

	/** Used to format when the absolute number in the current units is <=1e-3 or >=1e3 */
	private final NumberFormat scientificFormat = new DecimalFormat("0.#####E0");
	/** Used to format when the absolute number in the current units is 1e-3< number <1e3 */
	private final NumberFormat decimalFormat = new DecimalFormat("0.#####");

	/**
	 * Uses a decimal format with 5 decimal places when 1e-3 <= |value| <= 1e3 (or value == zero),
	 * otherwise a scientific format.
	 * @param value with full precision
	 * @return sensibly-formatted String representation of the value
	 */
	protected String formatValue(double value) {
		final double absoluteValue = Math.abs(value);
		if (absoluteValue != 0.0 && (absoluteValue <= 1e-3 || absoluteValue >= 1e3)) {
			return scientificFormat.format(value);
		} else {
			return decimalFormat.format(value);
		}
	}
}
