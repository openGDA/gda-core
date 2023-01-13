/*-
 * Copyright © 2010 Diamond Light Source Ltd.
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

package gda.device.detector.mythen.client;

import java.math.BigDecimal;

/**
 * Methods to work around Java bug #6321015.
 * 
 * @see <a href="http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6321015">Java bug #6321015</a>
 */
public class BigDecimalUtils {
	
	public static boolean isLess(BigDecimal a, BigDecimal b) {
		return a.compareTo(b) < 0;
	}
	
	public static boolean isLessOrEqual(BigDecimal a, BigDecimal b) {
		return a.compareTo(b) <= 0;
	}
	
	public static boolean isGreaterOrEqual(BigDecimal a, BigDecimal b) {
		return a.compareTo(b) >= 0;
	}
	
	public static boolean isGreater(BigDecimal a, BigDecimal b) {
		return a.compareTo(b) > 0;
	}

}
