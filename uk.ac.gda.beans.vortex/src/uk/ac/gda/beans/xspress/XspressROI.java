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

package uk.ac.gda.beans.xspress;

import uk.ac.gda.beans.DetectorROI;

/**
 * a region of interest in an Xspress MCA. Note that the total size of all ROIs
 * in a given MCA must not exceed tthe original MCA size.
 */
public class XspressROI extends DetectorROI {

	/**
	 * The region type when the ROI returns the part of the MCA in this region.
	 */
	public static final String MCA = "MCA";

}
