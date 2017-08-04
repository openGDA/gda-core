/*-
 * Copyright © 2017 Diamond Light Source Ltd.
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

package gda.device.detector.xmap.api;

import org.eclipse.scanning.api.annotation.ui.FieldDescriptor;

/**
 * <p>
 * Model to support an XMAP detector that uses a custom controller for acquisition but Area Detector to write data.
 * </p>
 * This model should be used in conjunction with (Dummy)XmapControllerAreaDetector.<br>
 * If you wish to use XMAP purely through its custom interface, use XmapRunnableDeviceModel.
 */
public class XmapWritingFilesRunnableDeviceModel extends XmapRunnableDeviceModel {
	/**
	 * The name of the area detector (ADDetector object)
	 */
	@FieldDescriptor(label="Area detector name", editable=false)
	private String areaDetectorName;

	/**
	 * The name of the XMAP detector device
	 */
	@FieldDescriptor(label="XMAP detector name", editable=false)
	private String xmapDetectorName;

	public String getAreaDetectorName() {
		return areaDetectorName;
	}

	public void setAreaDetectorName(String areaDetectorName) {
		this.areaDetectorName = areaDetectorName;
	}

	public String getXmapDetectorName() {
		return xmapDetectorName;
	}

	public void setXmapDetectorName(String xmapDetectorName) {
		this.xmapDetectorName = xmapDetectorName;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((areaDetectorName == null) ? 0 : areaDetectorName.hashCode());
		result = prime * result + ((xmapDetectorName == null) ? 0 : xmapDetectorName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		XmapWritingFilesRunnableDeviceModel other = (XmapWritingFilesRunnableDeviceModel) obj;
		if (areaDetectorName == null) {
			if (other.areaDetectorName != null)
				return false;
		} else if (!areaDetectorName.equals(other.areaDetectorName))
			return false;
		if (xmapDetectorName == null) {
			if (other.xmapDetectorName != null)
				return false;
		} else if (!xmapDetectorName.equals(other.xmapDetectorName))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "XmapRunnableDeviceModel [areaDetectorName=" + areaDetectorName + ", xmapDetectorName=" + xmapDetectorName + ", [" + super.toString() + "]";
	}

}
