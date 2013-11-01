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

package uk.ac.gda.util;

public class CorrectionUtils {

	
	/**
	 * Documentation from William Helsby is available to explain the maths in this method
	 * 
	 * @param ffr            - fast filter rate or 'Input Count Rate'
	 * @param processDeadTime - T
	 * @return actual count rate
	 */
	public static double correct(double ffr, double processDeadTime) {
		double maxOut;
		double in = 0.0;
		double tryIn;
		long div;
		
		// if processDeadTime set to zero, then simply return the unchanged fast filter rate
		if (Double.valueOf(processDeadTime) == 0.0){ 
			return ffr;
		}

		maxOut = 1 / processDeadTime * Math.exp(-1.0);
		if (ffr < 0) {
			in = 0.0;
		} else if (ffr > maxOut) {
			in = 1 / processDeadTime;
		} else {
			// Gives 50 bit absolute precision, but note low count rate
			for (int i = 0; i < 50; i++) {
				div = 1 << i;
				tryIn = in + 1 / (div * processDeadTime);
				if (ffr >= tryIn * Math.exp(-tryIn * processDeadTime))
					in = tryIn;
			}
		}
		return in;
	}

	/**
	 * Get the dead time correction factor.
	 * @param ppdt
	 * @param ffr
	 * @param sfr
	 * @return K the correction factor for dead time
	 */
	public static double getK(double ppdt, double ffr, double sfr) {
		final double actualCountRate = CorrectionUtils.correct(ffr, ppdt);	
	    return actualCountRate / sfr;
	}

}
