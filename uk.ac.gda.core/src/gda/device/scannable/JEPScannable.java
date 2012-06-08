/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package gda.device.scannable;

import gda.device.DeviceException;
import gda.device.Scannable;
import gda.jython.InterfaceProvider;
import gda.jython.JythonServerFacade;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.nfunk.jep.JEP;
import org.nfunk.jep.ParseException;
import org.nfunk.jep.SymbolTable;

/**
 * Wraps a Scannable and/or an expression based on Scannables and/or values in the Jythonnamespace and/or parts of the
 * Scannable.
 * <p>
 * This is for reporting values only. It will only return a single double value from getPosition().
 */
public class JEPScannable extends ScannableBase {
	
	
	public static JEPScannable createJEPScannable(String label,String scannableName, String format, String variableName, String expression) throws ParseException{
		JEPScannable newOne = new JEPScannable();
		if (label != null )	newOne.setLabel(label);
		if (scannableName != null )	newOne.setScannableName(scannableName);
		if (format != null )	newOne.setGivenFormat(format);
		if (variableName != null )	newOne.setVariableName(variableName);
		if (expression != null )	newOne.setExpression(expression);
		return newOne;		
	}

	private String scannableName;
	private String expression;
	private String label;
	private String givenFormat;
	private String variableName;
	private HashMap<String, Double> map;
	private SymbolTable symbolTable;

	public JEPScannable() {
		this.inputNames = new String[] {};
		this.outputFormat = new String[] { "%6.2f" };
	}

	@Override
	public void asynchronousMoveTo(Object position) throws DeviceException {
		// do nothing
	}

	@Override
	public boolean isBusy() throws DeviceException {
		// do nothing
		return false;
	}

	public String getScannableName() {
		return scannableName;
	}

	public void setScannableName(String scannableName) {
		this.scannableName = scannableName;
		if (!expressionDefined() && !labelDefined()) {
			this.extraNames = new String[] { scannableName };
			this.setName(scannableName);
		}
	}

	public String getExpression() {
		return expression;
	}

	public void setExpression(String expression) throws ParseException {
		this.expression = expression;
		if (!scannableNameDefined() && !labelDefined()) {
			this.extraNames = new String[] { expression };
			this.setName(expression);
		}
		this.symbolTable = parseSymbols(expression);
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
		this.extraNames = new String[] { label };
		this.setName(label);
	}

	public String getGivenFormat() {
		return givenFormat;
	}

	public void setGivenFormat(String givenFormat) {
		if (givenFormat!=null&&!givenFormat.isEmpty()&&!givenFormat.startsWith("%")) givenFormat="%"+givenFormat;
		this.givenFormat = givenFormat;
		this.outputFormat = new String[] { givenFormat };
	}

	public String getVariableName() {
		return variableName;
	}

	public void setVariableName(String variableName) {
		this.variableName = variableName;
	}
	
	@Override
	public String toString(){
		try {
			Object currentPosition = getPosition();
			
			// if its not a string then assume its a double or something which can be formatted directly 
			if (!(currentPosition instanceof String)){
				return label + " : " + String.format(outputFormat[0], getPosition());
			}
			
			// else are we expecting a String?
			if (outputFormat[0].contains("d")){
				return label + " : " + String.format(outputFormat[0], getPosition());
			}
			
			// else its a double which needs converting to a number first			
			Double dblPosition = Double.valueOf((String) currentPosition);
			return label + " : " + String.format(outputFormat[0], dblPosition);
			
		} catch (DeviceException e) {
			return label;
		}
	}

	@Override
	public Object getPosition() throws DeviceException {
		// if scannable only
		if (!expressionDefined() && !variableDefined()) {
			Object scannable = InterfaceProvider.getJythonNamespace().getFromJythonNamespace(scannableName);
			if (scannable == null || !(scannable instanceof Scannable)){
				throw new DeviceException("Scannable " + scannableName + " not found in Jython namespace");
			}
			return ((Scannable)scannable).getPosition();
		}
		// if scannable and variable name
		else if (!expressionDefined()) {
			return JythonServerFacade.getInstance().evaluateCommand(scannableName + "." + variableName + "()");
		}
		// if expression has been defined
		else if (expressionDefined()) {
			updateValueToVariableMap();
			return determineJEP();
		}
		// else this is not configured correctly
		return null;
	}

	private double determineJEP() {
		JEP jep = new JEP();
		jep.addStandardConstants();
		jep.addStandardFunctions();
		jep.setImplicitMul(true);
		for (Entry<String, Double> entry : map.entrySet()) {
			jep.addVariable(entry.getKey(), entry.getValue());
		}

		try {
			jep.parseExpression(expression);
		} catch (Exception e) {
			throw new IllegalArgumentException(
					"JEPQuantityConverter.CreateJEP: Error parsing expresson\n" + expression, e);
		}

		return jep.getValue();
	}

	private void updateValueToVariableMap() {

		map = new HashMap<String, Double>();

		// get the value of each variable and store in a map
		if (symbolTable!=null) for (Object key : symbolTable.keySet()) {
			try {
				final String var = key.toString().trim();
				if (symbolTable.getVar(var).isConstant()) continue;
				Object position = InterfaceProvider.getJythonNamespace().getFromJythonNamespace(var);
				// 1. its a scannable
				if (position != null && position instanceof Scannable) {
					Double value = ScannableUtils.getCurrentPositionArray((Scannable) position)[0];
					map.put(var, value);
				} else if (position == null) {
						
					if (scannableInfinder()) {

						Scannable scannble = (Scannable)InterfaceProvider.getJythonNamespace().getFromJythonNamespace(scannableName);
					    final List<String> extraNames = Arrays.asList(scannble.getExtraNames());
					    if (extraNames.contains(var)) {
					    	// 2. Pick up from extra names
					    	final Double[] da = ScannableUtils.objectToArray(scannble.getPosition());
					    	map.put(var, da[extraNames.indexOf(var)]);
					    } else {
							// 3. try it could be an attribute of the given scannable, casuses an exception if does not work in jython.
							position = InterfaceProvider.getJythonNamespace().getFromJythonNamespace(
									scannableName + "." + var + "()");
							if (position != null) {
								Double value = ScannableUtils.objectToArray(position)[0];
								map.put(var, value);
							}
					    }
					} else {
						// 4. its a scannable but couldn't get it from namespace so eval using () works, can cause unwanted exception in server log.
						position = JythonServerFacade.getCurrentInstance().evaluateCommand(var + "()");
						if (position != null) {
							Double value = ScannableUtils.objectToArray(position)[0];
							map.put(var, value);
						}
					}
				} else {
					// 4. its simply a variable in the jython namespace (want this as last choice over being a part of
					// the scannable
					Double value = ScannableUtils.objectToArray(position)[0];
					map.put(var, value);
				}
			} catch (Exception e) {
				// simply skip this part
			}
		}

	}

	/**
	 * Do not call in loop as new parser is constructed.
	 * @param expression
	 * @return SymbolTable
	 * @throws ParseException
	 */
	private SymbolTable parseSymbols(String expression) throws ParseException {
		
		JEP jepParser = new JEP();
		jepParser.addStandardFunctions();
		jepParser.addStandardConstants();
		jepParser.setAllowUndeclared(true);
		jepParser.setImplicitMul(true);
		
	    jepParser.parse(expression);
	    return jepParser.getSymbolTable();

	}

	private boolean scannableNameDefined() {
		return scannableName != null && !scannableName.equals("");
	}

	private boolean expressionDefined() {
		return expression != null && !expression.equals("");
	}
	
	private boolean variableDefined(){
		return variableName != null && !variableName.equals("");
	}

	private boolean labelDefined() {
		return label != null && !label.equals("");
	}

	private boolean scannableInfinder() {
		if (!scannableNameDefined()) {
			return false;
		}
		try {
			return JythonServerFacade.getCurrentInstance().getFromJythonNamespace(scannableName) != null;
		} catch (Exception e) {
			return false;
		}
	}

}
