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

package gda.util.converters;

import java.util.List;

import javax.measure.Quantity;
import javax.measure.Unit;

import org.nfunk.jep.JEP;

import gda.util.QuantityFactory;

/**
 * Class to perform conversion between a Source and Target quantity using a Java Expression Parser expression The
 * conversion is defined in an expression file.
 *
 * @see gda.util.converters.JEPConverterHolder
 * @see org.nfunk.jep.JEP
 */
final class JEPQuantityConverter<S extends Quantity<S>, T extends Quantity<T>> implements IQuantityConverter<S, T> {
	private final JEP jepStoT, jepTtoS;

	private final JEPQuantityConverterParameters expressionParameters;

	private static final String VariableName = "X";

	private final String expressionFileName;

	private JEP CreateJEP(String expression) {
		JEP jep = new JEP();
		jep.addStandardConstants();
		jep.addStandardFunctions();
		jep.setImplicitMul(true);
		jep.addVariable(VariableName, 0.); // dummy value to allow
		// evaluation
		try {
			jep.parseExpression(expression);
		} catch (Exception e) {
			throw new IllegalArgumentException(
					"JEPQuantityConverter.CreateJEP: Error parsing expresson\n" + expression, e);
		}
		return jep;
	}

	JEPQuantityConverter(String expressionFileName) {
		this.expressionFileName = expressionFileName;
		expressionParameters = JEPQuantityConverterParameters
				.GetJEPQuantityConverterParametersFromFile(expressionFileName);
		jepStoT = CreateJEP(expressionParameters.getExpressionStoT());
		jepTtoS = CreateJEP(expressionParameters.getExpressionTtoS());
	}

	@Override
	public List<String> getAcceptableSourceUnits() {
		return expressionParameters.getAcceptableSourceUnits();
	}

	@Override
	public List<String> getAcceptableTargetUnits() {
		return expressionParameters.getAcceptableTargetUnits();
	}

	/*
	 * Need to be synchronized as we change the JEP during the call
	 */
	@Override
	public synchronized Quantity<S> toSource(Quantity<T> target) {
		final Unit<S> acceptableSourceUnits = QuantityFactory.createUnitFromString(getAcceptableSourceUnits().get(0));
		final Unit<T> acceptableTargetUnits = QuantityFactory.createUnitFromString(getAcceptableTargetUnits().get(0));

		if (!target.getUnit().equals(acceptableTargetUnits)) {
			throw new IllegalArgumentException("JEPQuantityConverter.ToSource: target units (" + target.getUnit()
					+ ") do not match acceptableUnits (" + acceptableTargetUnits + ")" + this.toString());
		}

		jepTtoS.addVariable(VariableName, target.getValue().doubleValue());
		// returns in current units doubleValue converts it
		double val = jepTtoS.getValue();
		// Infinite is a valid value for 1/X when X is 0. so only protect against Nan.
		if (Double.isNaN(val) /* || Double.isInfinite(val) */) {
			throw new IllegalArgumentException("JEPQuantityConverter.ToSource: Error. Result = " + val + " target = "
					+ target.getValue().doubleValue() + " expression = " + expressionParameters.getExpressionTtoS() + " "
					+ this.toString());
		}
		return QuantityFactory.createFromObject(val, acceptableSourceUnits);
	}

	/*
	 * Need to be synchronized as we change the JEP during the call
	 */
	@Override
	public synchronized Quantity<T> toTarget(Quantity<S> source) {
		final Unit<S> acceptableSourceUnits = QuantityFactory.createUnitFromString(getAcceptableSourceUnits().get(0));
		final Unit<T> acceptableTargetUnits = QuantityFactory.createUnitFromString(getAcceptableTargetUnits().get(0));

		if (!source.getUnit().equals(acceptableSourceUnits)) {
			throw new IllegalArgumentException("JEPQuantityConverter.ToTarget: source units (" + source.getUnit()
					+ ") do not match acceptableUnits (" + acceptableSourceUnits + ") " + this.toString());
		}

		jepStoT.addVariable(VariableName, source.getValue().doubleValue());
		double val = jepStoT.getValue();
		// Infinite is a valid value for 1/X when X is 0. so only protect against Nan.
		if (Double.isNaN(val) /* || Double.isInfinite(val) */) {
			throw new IllegalArgumentException("JEPQuantityConverter.ToTarget: Error. Result = " + val + " source = "
					+ source.getValue().doubleValue() + " expression = " + expressionParameters.getExpressionStoT() + " "
					+ this.toString());
		}
		return QuantityFactory.createFromObject(val, acceptableTargetUnits);
	}

	@Override
	public String toString() {
		return "JEPQuantityConverter using details in " + expressionFileName;
	}

	/**
	 * @return expressionFileName
	 */
	public String getExpressionFileName() {
		return expressionFileName;
	}

	@Override
	public boolean sourceMinIsTargetMax() {
		return expressionParameters.sourceMinIsTargetMax;
	}
	@Override
	public boolean handlesStoT() {
		return true;
	}

	@Override
	public boolean handlesTtoS() {
		return true;
	}

}
