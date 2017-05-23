/*-
 * Copyright © 2016 Diamond Light Source Ltd.
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

package uk.ac.gda.beans.exafs.i20;

import java.io.Serializable;

import uk.ac.gda.beans.DetectorROI;

public class ROIRegion implements Serializable {

	private String roiName;
	private DetectorROI xRoi, yRoi;

	public ROIRegion() {

	}
	/**
	 * Constructor
	 * @param name
	 * @param minX
	 * @param minY
	 * @param maxX
	 * @param maxY
	 */
	public ROIRegion( String name, int minX, int minY, int maxX, int maxY ) {
		roiName = name;
		xRoi  = new DetectorROI( "xRoi", minX, maxX );
		yRoi  = new DetectorROI( "yRoi", minY, maxY );
	}

	public String getRoiName() {
		return roiName;
	}
	public void setRoiName(String roiName) {
		this.roiName = roiName;
	}

	public DetectorROI getXRoi() {
		return xRoi;
	}
	public void setXRoi(DetectorROI xRoi) {
		this.xRoi = xRoi;
	}

	public DetectorROI getYRoi() {
		return yRoi;
	}
	public void setYRoi(DetectorROI yRoi) {
		this.yRoi = yRoi;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((roiName == null) ? 0 : roiName.hashCode());
		result = prime * result + ((xRoi == null) ? 0 : xRoi.hashCode());
		result = prime * result + ((yRoi == null) ? 0 : yRoi.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ROIRegion other = (ROIRegion) obj;
		if (roiName == null) {
			if (other.roiName != null)
				return false;
		} else if (!roiName.equals(other.roiName))
			return false;
		if (xRoi == null) {
			if (other.xRoi != null)
				return false;
		} else if (!xRoi.equals(other.xRoi))
			return false;
		if (yRoi == null) {
			if (other.yRoi != null)
				return false;
		} else if (!yRoi.equals(other.yRoi))
			return false;
		return true;
	}
}
