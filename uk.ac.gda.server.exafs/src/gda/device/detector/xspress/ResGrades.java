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

package gda.device.detector.xspress;

import java.util.Arrays;
import java.util.List;

/**
 * Class to hold grades.
 */
public class ResGrades {
	
	/**
	 * Means that a full mca for each element will be given i.e. for 9-element you get 9 * 4096 numbers
	 */
	public static final String NONE = "res-none";
	
	/**
	 * Means that one mca per res grade for each element will be given i.e. for 9-element you get 16 * 9 * 4096 numbers
	 */
	public static final String ALLGRADES = "res-min-div-8";
	
	/**
	 * Means that two mca per res grade for each element will be given; above and below the threshold. i.e. for 9-element you get 2 * 9 * 4096 numbers
	 */
	public static final String THRESHOLD = "res-thres";

	/**
	 * The resGrades other then res-none and res-min-div-8 will return 2 mca per element (bad then good) i.e. for 9-element you get 2 * 9 * 4096 numbers
	 */
	private static final List<String> RES_GRADE_NAMES = Arrays.asList(new String[]{ALLGRADES, NONE, THRESHOLD/*, "res-log", "res-top", "res-bot", "res-min"*/});

	/**
	 * Tests if a string is an acceptable Res Grade. "res-thres" should have a floating pint number at the end in the range 0.0 to 16.0
	 * 
	 * @param fullGrade
	 * @return true if string an acceptable string to give to da server as a resgrade.
	 */
	public static final boolean isResGrade(final String fullGrade) {
		if (fullGrade.indexOf(' ') > -1) {
			String[] parts = fullGrade.split(" ");
			return parts.length == 2 && RES_GRADE_NAMES.contains(parts[0]) && isFloatingPoint(parts[1]);
		}
		return RES_GRADE_NAMES.contains(fullGrade);
	}
	
	private static boolean isFloatingPoint(String string){
		return !isInt(string) && isDouble(string);
	}
	
	private static boolean isInt(String string) {
		try {
			Integer.parseInt(string);
		} catch (NumberFormatException e) {
			return false;
		}
		return true;
	}
	
	private static boolean isDouble(String string) {
		try {
			Double.parseDouble(string);
		} catch (NumberFormatException e) {
			return false;
		}
		return true;
	}

	/**
	 * 
	 * @return res grades
	 */
	public static final String[] toStringArray() {
		return RES_GRADE_NAMES.toArray(new String[RES_GRADE_NAMES.size()]);
	}
}
