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

package gda.gui.text.Formatter;

import java.text.ParseException;

import javax.swing.text.MaskFormatter;

/**
 * DecimalLimitCheckedFormatter Class
 */
public class DecimalLimitCheckedFormatter extends MaskFormatter {

	/**
	 * @throws ParseException
	 */
	public DecimalLimitCheckedFormatter() throws ParseException {
		// TODO Auto-generated constructor stub
		super();
		setMask("*********");
		setValidCharacters(".0123456789");
	}

	@Override
	public Object stringToValue(String value) throws ParseException {
		try {
			Object o = super.stringToValue(value);
			if (Double.parseDouble(o.toString()) < 1.0) {
				throw new ParseException("less than 1", 0);
			}
			return o;
		} catch (ParseException e) {
			return 0.;
		}
	}
}
