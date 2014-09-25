/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.beanutils.BeanUtils;


public class XspressDeadTimeParameters {
	private List<DetectorDeadTimeElement> detectorDTList;
	static public final URL mappingURL = XspressParameters.class.getResource("XspressMapping.xml");

	static public final URL schemaURL  = XspressParameters.class.getResource("XspressMapping.xsd");
	
	public XspressDeadTimeParameters() {
		detectorDTList = new ArrayList<DetectorDeadTimeElement>();
	}
		
	public void clear() {
		if (detectorDTList!=null) detectorDTList.clear();
	}
	
	public void addDetectorDeadTimeElement(DetectorDeadTimeElement detectorDeadTimeElement) {
		detectorDTList.add(detectorDeadTimeElement);
	}

	public List<DetectorDeadTimeElement> getDetectorDeadTimeElementList() {
		return detectorDTList;
	}

	public void setDetectorDeadTimeElementList(List<DetectorDeadTimeElement> detectorList) {
		this.detectorDTList = detectorList;
	}

	public DetectorDeadTimeElement getDetectorDT(int index) {
		return detectorDTList.get(index);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((detectorDTList == null) ? 0 : detectorDTList.hashCode());
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
		XspressDeadTimeParameters other = (XspressDeadTimeParameters) obj;
		if (detectorDTList == null) {
			if (other.detectorDTList != null)
				return false;
		} else if (!detectorDTList.equals(other.detectorDTList))
			return false;
		
		return true;
	}

	@Override
	public String toString() {
		try {
			return BeanUtils.describe(this).toString();
		} catch (Exception e) {
			return e.getMessage();
		}
	}


}
