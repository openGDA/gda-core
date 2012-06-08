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

import gda.device.xspress.DetectorReading;
import gda.device.xspress.corba.CorbaDetectorReading;

/**
 * Contains methods to convert DetectorReadings to CorbaDetectorReadings and vice versa
 */
public class DetectorReadingConverter {
	/**
	 * Convert CorbaDetectorReadings to DetectorReadings
	 * 
	 * @param cdr
	 *            corba detector reading
	 * @return DetectorReading
	 * @see gda.device.xspress.DetectorReading
	 */
	public static DetectorReading toDetectorReading(CorbaDetectorReading cdr) {
		DetectorReading dr = new DetectorReading(DetectorConverter.toDetector(cdr.detector), cdr.total, cdr.resets,
				cdr.acc, cdr.originalWindowed);
		return dr;
	}

	/**
	 * Convert DetectorReadings to CorbaDetectorReadings
	 * 
	 * @param dr
	 *            detector reading
	 * @return CorbaDetectorReading
	 * @see gda.device.xspress.corba.CorbaDetectorReading
	 */
	public static CorbaDetectorReading toCorbaDetectorReading(DetectorReading dr) {
		return new CorbaDetectorReading(DetectorConverter.toCorbaDetector(dr.getDetector()), dr.getOriginalWindowed(),
				dr.getTotal(), dr.getResets(), dr.getAcc(), dr.getWindowed());
	}
}
