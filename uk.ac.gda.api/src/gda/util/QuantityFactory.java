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

package gda.util;

import java.util.StringTokenizer;

import org.jscience.physics.quantities.Dimensionless;
import org.jscience.physics.quantities.Quantity;
import org.jscience.physics.units.Unit;
import org.python.core.PyFloat;
import org.python.core.PyInteger;
import org.python.core.PyString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.jscience.physics.units.NonSIext;

/**
 * A factory class to create Quantities from Strings. Uses a combination of BFI and Trickery.
 */
public class QuantityFactory {
	static final Logger logger = LoggerFactory.getLogger(QuantityFactory.class);
	static {
		NonSIext.initializeClass();
	}

	/**
	 * Takes an object and converts it into a quantity of the requested unit.
	 * <p>
	 * This can take a variety of objects, including Jython native types.
	 *
	 * @param position
	 * @param targetQuantity
	 * @return the Quantity created (or null)
	 */
	public static Quantity createFromObject(Object position, Unit<? extends Quantity> targetQuantity) {
		// position must be a double or a string which can be converted in a
		// suitable quantity for this object
		Quantity targetPosition = null;

		// convert input into Quantity objects
		if (position instanceof String) {
			Quantity newPosition = QuantityFactory.createFromString((String) position).to(targetQuantity);
			if (newPosition != null){
				position = newPosition;
			}
		}
		if (position instanceof PyString) {
			targetPosition = QuantityFactory.createFromString(((PyString) position).toString()).to(targetQuantity);
		} else if (position instanceof Double) {
			targetPosition = Quantity.valueOf((Double) position, targetQuantity);
		} else if (position instanceof Integer) {
			targetPosition = Quantity.valueOf((Integer) position, targetQuantity);
		} else if (position instanceof PyFloat) {
			targetPosition = Quantity.valueOf(((PyFloat) position).getValue(), targetQuantity);
		} else if (position instanceof PyInteger) {
			targetPosition = Quantity.valueOf(Double.valueOf(((PyInteger) position).getValue()), targetQuantity);
		} else if (position instanceof Quantity) {
			targetPosition = ((Quantity) position).to(targetQuantity);
		} else if (position instanceof Dimensionless) {
			targetPosition = Quantity.valueOf(((Dimensionless) position).getAmount(),targetQuantity);
		} else {
			return null;
		}
		return targetPosition;

	}

	/**
	 * Creates a Quantity from a single string specifying value and units. This should be useful, for example, in
	 * creating Quantities from scripts.
	 *
	 * @param string
	 *            of the form '1.0 mm'
	 * @return the Quantity created (or null)
	 */
	public static Quantity createFromString(String string) {
		Quantity q = null;
		String valueString;
		String unitString;
		if (string != null) {
			StringTokenizer strtok = new StringTokenizer(string);

			if (strtok.countTokens() == 2) {
				// The string is assumed to be of the form "1.0 mm"
				valueString = strtok.nextToken();
				unitString = strtok.nextToken();
			} else {
				// The string is assumed to be of the form "1.0mm"
				// and needs to be split at the boundary of the number and unit.

				// It is easiest to search the string backwards because we
				// can't use isLetter() to find the end of a number because it
				// might contain an 'e' or an 'E'
				StringBuffer sb = new StringBuffer(string);
				sb.reverse();

				StringBuffer unitStringBuffer = new StringBuffer();
				StringBuffer valueStringBuffer = new StringBuffer();

				// Go through reversed string appending to the unitStringBuffer
				// until we find a character (digit or '.') which must be part
				// of the value. NB this was formerly a do {...} while loop but
				// went wrong for strings such as "1.0". With the loop this way
				// round "1.0" produces Quantity 1.0 Dimensionless.
				int k = 0;
				while (k < sb.length() && sb.charAt(k) != '.' && !Character.isDigit(sb.charAt(k))) {
					unitStringBuffer.append(sb.charAt(k));
					k++;
				}

				// Use the rest of the string as the value
				for (; k < sb.length(); k++) {
					valueStringBuffer.append(sb.charAt(k));
				}

				// Reverse the two StringBuffers and use them to create a
				// quantity. NB If the string was not of the correct form
				// then one or other of the two parts will not be correct
				// and createFromTwoStrings will be null.
				valueString = new String(valueStringBuffer.reverse());
				unitString = new String(unitStringBuffer.reverse());
			}
			if (valueString.isEmpty()) return q;
			q = createFromTwoStrings(valueString, unitString);
		}
		return q;
	}

	/**
	 * Creates a Quantity from two strings. This should be useful, for example, in creating Quantities from GUIs.
	 *
	 * @param valueString
	 *            the numerical value e.g '1.0'
	 * @param unitString
	 *            the units - e.g. 'mm'
	 * @return the Quantity created (or null)
	 */
	public static Quantity createFromTwoStrings(String valueString, String unitString) {
		Quantity q = null;
		if (valueString != null && unitString != null) {
			try {
				q = Quantity.valueOf(valueString + " " + unitString);
			} catch (NumberFormatException e) {
				logger.warn("QuantityFactory: invalid number specified. valueString = '"+valueString +
						"' unitString = '" + unitString + "'");
			}
		}
		return q;
	}

	/**
	 * Creates a Unit from a string by searching through the subclasses of quantity to find a predefined field. For
	 * example given 'mm' it will return the public static Length.Unit MM from the Length class. A blank unit string
	 * will return a dimensionless unit.
	 *
	 * @param string
	 *            the name of a unit.
	 * @return the corresponding Unit, or null if not found
	 */
	@SuppressWarnings("unchecked")
	public static Unit<? extends Quantity> createUnitFromString(String string) {
		Unit<? extends Quantity> unit = null;
		// This method is tagged with @suppressWarnings as we can't predict at
		// compile time which type of unit will be created from a string.
		try {
			unit = (string != null) ? Unit.valueOf(string) : null;
		} catch (IllegalArgumentException e) {
			logger.warn("QuantityFactory: Unknown unit " + string + " requested");
		}
		return unit;
	}
}