/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council
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

package gda.data.scan.datawriter;

import java.util.Map;
import java.util.Map.Entry;

import org.nfunk.jep.JEP;

/**
 *	 A Config class to pass to asciiWriterExtender
 */
public class AsciiWriterExtenderConfig {
	String label;
	String format;
	String expression;
	
	/**
	 * 
	 */
	public AsciiWriterExtenderConfig(){
	}
	
	/**
	 * Should not be called with a null expression
	 * @param label
	 * @param format
	 * @param expression
	 */
	public AsciiWriterExtenderConfig(String label, String format, String expression){
		this(label,format,expression,expression);
	}

	/**
	 * Either expression or scannableName should not be null, expression is taken in precedence to scannable name.
	 * @param label
	 * @param format
	 * @param expression JEP expression
	 * @param scannableName 
	 */
	public AsciiWriterExtenderConfig(String label, String format, String expression, String scannableName){
		this.label = label;
		this.format = format;
		this.expression = expression==null ? scannableName : expression;
	}
	
	/**
	 * @return Returns the label.
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * @param label The label to set.
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	/**
	 * @return Returns the format.
	 */
	public String getFormat() {
		return format;
	}

	/**
	 * @param format The format to set.
	 */
	public void setFormat(String format) {
		this.format = format;
	}

	/**
	 * @return Returns the expression.
	 */
	public String getExpression() {
		return expression;
	}

	/**
	 * @param expression The expression to set.
	 */
	public void setExpression(String expression) {
		this.expression = expression;
	}

	String getBufferEntry( Map<String, Double>variablesToDoubleMap){
		JEP jep = CreateJEP(expression, variablesToDoubleMap);
		for( Entry<String,Double> entry : variablesToDoubleMap.entrySet()){
			String s = entry.getKey();
			Double d = entry.getValue();
			jep.addVariable(s,d);
		}
		Double val = jep.getValue();
		if (format == null){
			return val.toString();
		}
		return String.format(format, val);
	}
	
	private JEP CreateJEP(String expression, Map<String, Double>variablesToDoubleMap) {
		JEP jep = new JEP();
		jep.addStandardConstants();
		jep.addStandardFunctions();
		jep.setImplicitMul(true);
		for( Entry<String,Double> entry : variablesToDoubleMap.entrySet()){
			jep.addVariable(entry.getKey(), entry.getValue());
		}

		try {
			jep.parseExpression(expression );
		} catch (Exception e) {
			throw new IllegalArgumentException(
					"JEPQuantityConverter.CreateJEP: Error parsing expresson\n" + expression, e);
		}
		return jep;
	}

}
