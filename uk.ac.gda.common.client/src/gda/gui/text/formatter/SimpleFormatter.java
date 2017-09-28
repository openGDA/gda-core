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

package gda.gui.text.formatter;

import java.text.ParseException;

import javax.swing.text.DefaultFormatter;

/**
 * Simple formatter to be used with JFormattedText to provide control over how an object is displayed in a text box The
 * value in the text box is converted to a Double by the stringToValue method.
 */
public class SimpleFormatter extends DefaultFormatter

{
	/**
	 * format string
	 */
	public final String formatString;

	/**
	 * @param formatString
	 *            This is used in the function valueToString as the formatter given to String.format to convert the
	 *            object to text.
	 */
	public SimpleFormatter(String formatString) {
		this(formatString,Double.class);
	}

	/**
	 * @param formatString
	 *            This is used in the function valueToString as the formatter given to String.format to convert the
	 *            object to text.
	 * @param classType class to convert string to value - the class must have a contructor that accepts a String
	 */
	@SuppressWarnings("rawtypes")
	public SimpleFormatter(String formatString, Class classType) {
		this.formatString = formatString;
		setValueClass(classType);
	}


	@Override
	public String valueToString(Object value) throws ParseException {
		if( value != null && value instanceof String)
			return (String) value;
		return (value == null) ? "" : (formatString != null ) ? String.format(formatString, value) : value.toString();
	}

}
