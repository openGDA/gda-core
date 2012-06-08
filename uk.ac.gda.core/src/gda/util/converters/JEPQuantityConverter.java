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

import java.util.ArrayList;
import org.nfunk.jep.JEP;
import org.jscience.physics.quantities.Quantity;
import org.jscience.physics.units.Unit;

/**
 * Class to perform conversion between a Source and Target quantity using a Java Expression Parser expression The
 * conversion is defined in an expression file.
 * 
 * @see gda.util.converters.JEPConverterHolder
 * @see org.nfunk.jep.JEP
 */
final class JEPQuantityConverter implements IQuantityConverter {
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
	public ArrayList<Unit<? extends Quantity>> getAcceptableSourceUnits() {
		return expressionParameters.getAcceptableSourceUnits();
	}

	@Override
	public ArrayList<Unit<? extends Quantity>> getAcceptableTargetUnits() {
		return expressionParameters.getAcceptableTargetUnits();
	}

	/*
	 * Need to be synchronized as we change the JEP during the call
	 */
	@Override
	public synchronized Quantity toSource(Quantity target) {
		if (!target.getUnit().equals(getAcceptableTargetUnits().get(0))) {
			throw new IllegalArgumentException("JEPQuantityConverter.ToSource: target units (" + target.getUnit()
					+ ") do not match acceptableUnits (" + getAcceptableTargetUnits().get(0) + ")" + this.toString());
		}

		jepTtoS.addVariable(VariableName, target.getAmount()); // getAmount
		// returns in
		// current units
		// doubleValue
		// converts it
		double val = jepTtoS.getValue();
		// Infinite is a valid value for 1/X when X is 0. so only protect
		// against Nan.
		if (Double.isNaN(val) /* || Double.isInfinite(val) */) {
			throw new IllegalArgumentException("JEPQuantityConverter.ToSource: Error. Result = " + val + " target = "
					+ target.getAmount() + " expression = " + expressionParameters.getExpressionTtoS() + " "
					+ this.toString());
		}
		return Quantity.valueOf(val, getAcceptableSourceUnits().get(0));
	}

	/*
	 * Need to be synchronized as we change the JEP during the call
	 */
	@Override
	public synchronized Quantity toTarget(Quantity source) {
		if (!source.getUnit().equals(getAcceptableSourceUnits().get(0))) {
			throw new IllegalArgumentException("JEPQuantityConverter.ToTarget: source units (" + source.getUnit()
					+ ") do not match acceptableUnits (" + getAcceptableSourceUnits().get(0) + ") " + this.toString());
		}

		jepStoT.addVariable(VariableName, source.getAmount());
		double val = jepStoT.getValue();
		// Infinite is a valid value for 1/X when X is 0. so only protect
		// against Nan.
		if (Double.isNaN(val) /* || Double.isInfinite(val) */) {
			throw new IllegalArgumentException("JEPQuantityConverter.ToTarget: Error. Result = " + val + " source = "
					+ source.getAmount() + " expression = " + expressionParameters.getExpressionStoT() + " "
					+ this.toString());
		}
		return Quantity.valueOf(val, getAcceptableTargetUnits().get(0));
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
