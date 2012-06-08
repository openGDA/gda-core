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

package gda.device.xspress.corba.impl;

import gda.device.xspress.Detector;
import gda.device.xspress.corba.CorbaDetector;

/**
 * Contains methods which convert Detector to CorbaDetector and vice versa
 */
public class DetectorConverter {
	/**
	 * Convert CorbaDetector to Detector
	 * 
	 * @param cd
	 *            corba detector
	 * @return Detector Object
	 * @see gda.device.xspress.Detector
	 */
	public static Detector toDetector(CorbaDetector cd) {
		Detector d = new Detector(cd.number, cd.windowStart, cd.windowEnd, cd.gain, cd.deadTime, cd.offset);
		return d;
	}

	/**
	 * Convert Detector to CorbaDetector
	 * 
	 * @param d
	 *            detector
	 * @return CorbaDetector object
	 * @see gda.device.xspress.corba.CorbaDetector
	 */
	public static CorbaDetector toCorbaDetector(Detector d) {
		return new CorbaDetector(d.getNumber(), d.getWindowStart(), d.getWindowEnd(), d.getGain(), d.getOffset(), d
				.getDeadTime());
	}
}
