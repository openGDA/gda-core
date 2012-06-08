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

package uk.ac.gda.components;

import gda.device.Scannable;
import gda.device.scannable.ScannableMotor;
import gda.factory.Finder;
import uk.ac.gda.richbeans.components.scalebox.NumberBox;

/**
 * Where rich bean widget is linked to specific hardware this utility provides
 * method to set widget attributes from the hardware.
 */
public class HardwareUI {

	/**
	 * Sets up the limits using the Finder
	 * @param box
	 * @param motorName
	 * @throws Exception
	 */
	public static void setHardwareLimits(final NumberBox box, final String motorName) throws Exception {
		HardwareUI.setHardwareLimits(box, Finder.getInstance().find(motorName));
	}
	
	/**
	 * Sets up the limits from the hardware object
	 * @param box
	 * @param motor
	 * @throws Exception
	 */
	public static void setHardwareLimits(final NumberBox box, final Object motor) throws Exception {
		
		double lowerLimit = Double.NaN;
		double upperLimit = Double.NaN;
		
		// NOTE: Could use reflection instead of casting to do this...
		if (motor instanceof ScannableMotor) {
			final ScannableMotor sm = (ScannableMotor)motor;
			lowerLimit = sm.getLowerMotorLimit();
			upperLimit = sm.getUpperMotorLimit();
			
		} else if (motor instanceof Scannable){
			final Scannable sm = (Scannable)motor;
			Object ll = sm.getAttribute("lowerGdaLimits");
			if (ll != null && ll instanceof Double[]){
				lowerLimit = ((Double[])ll)[0];
			}			
			Object ul = sm.getAttribute("upperGdaLimits");
			if (ul != null && ul instanceof Double[]){
				upperLimit = ((Double[])ul)[0];
			}
		} else {
			throw new Exception("Unsupported hardware "+motor.getClass().getName());
		}
		
		if (!Double.isNaN(lowerLimit)) box.setMinimum(lowerLimit);
		if (!Double.isNaN(upperLimit)) box.setMaximum(upperLimit);

	}
}
