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

package uk.ac.gda.ui.utils;



/**
 * @author fcp94556
 *
 */
public class StringUtils {

	/**
	 * Returns a StringBuilder with only the digits and . contained
	 * in the original string.
	 * 
	 * @param text
	 * @param decimalPlaces 
	 * @return StringBuilder
	 */
	public static final StringBuilder keepDigits(final String text,
			                                           int    decimalPlaces) {
		
		// Used to make algorithm below simpler, bit of a hack.
		if (decimalPlaces==0) decimalPlaces = -1;
		
		final StringBuilder buf = new StringBuilder();
		// Remove non digits
		final char [] ca   = text.toCharArray();		
		int decCount = 0;
		for (int i =0;i<ca.length;++i) {
			if (i==0&&ca[i]=='-') {
				buf.append(ca[i]);
				continue;
			}
	        if (StringUtils.isDigit(ca[i])) {
				if ('.'==ca[i]||decCount>0) {
					++decCount;
				}
	        	if (decCount<=decimalPlaces+1) buf.append(ca[i]);
	        } else {
	        	break;
	        }
		}
        return buf;
	}
	
	/**
	 * Returns true if digit or .
	 * @param c
	 * @return boolean
	 */
	public static final boolean isDigit(final char c) {
		if (Character.isDigit(c)) return true;
		if ('.'==c) return true;
		return false;
	}

}

	