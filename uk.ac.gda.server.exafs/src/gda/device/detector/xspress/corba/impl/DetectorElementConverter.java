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

package gda.device.detector.xspress.corba.impl;

import gda.device.detector.xspress.corba.CorbaDetectorElement;
import gda.device.detector.xspress.corba.CorbaXspressROI;

import java.util.ArrayList;

import uk.ac.gda.beans.xspress.DetectorElement;
import uk.ac.gda.beans.xspress.XspressROI;

/**
 * Contains methods which convert Detector to CorbaDetector and vice versa
 */
public class DetectorElementConverter {
	/**
	 * Convert CorbaDetectorElement to DetectorElement
	 * 
	 * @param cd
	 *            corba detector
	 * @return DetectorElement Object
	 * @see uk.ac.gda.beans.xspress.DetectorElement
	 */
	public static DetectorElement toDetectorElement(CorbaDetectorElement cd) {
		ArrayList<XspressROI> regionList = new ArrayList<XspressROI>();
		for (CorbaXspressROI corbaRegion : cd.regions) {

			regionList.add(new XspressROI(corbaRegion.regionName, corbaRegion.regionStart, corbaRegion.regionEnd,
					corbaRegion.regionType));
		}

		DetectorElement d = new DetectorElement(cd.name, cd.number, cd.windowStart, cd.windowEnd,cd.excluded, regionList);
		return d;
	}

	/**
	 * Convert Detector to CorbaDetector
	 * 
	 * @param d
	 *            detector
	 * @return CorbaDetectorElement object
	 * @see gda.device.detector.xspress.corba.CorbaDetectorElement
	 */
	public static CorbaDetectorElement toCorbaDetectorElement(DetectorElement d) {
		ArrayList<XspressROI> regionList = (ArrayList<XspressROI>) d.getRegionList();
		CorbaXspressROI corbaRegionList[] = new CorbaXspressROI[regionList.size()];
		int i = 0;
		for (XspressROI region : regionList) {
			corbaRegionList[i++] = new CorbaXspressROI(region.getRoiName(), region.getRegionStart(), region
					.getRegionEnd(), region.getRegionType());
		}
		return new CorbaDetectorElement(d.getName(), d.getNumber(), d.getWindowStart(), d.getWindowEnd(), d.isExcluded(),
				corbaRegionList);
	}
}
