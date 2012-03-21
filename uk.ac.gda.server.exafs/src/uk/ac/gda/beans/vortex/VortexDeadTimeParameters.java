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

package uk.ac.gda.beans.vortex;

import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.beanutils.BeanUtils;

import uk.ac.gda.beans.IRichBean;
import uk.ac.gda.util.beans.xml.XMLHelpers;

public class VortexDeadTimeParameters implements Serializable, IRichBean {

	private List<DetectorDeadTimeElement> detectorDTList;
	static public final URL mappingURL = VortexDeadTimeParameters.class.getResource("VortexMapping.xml");

	static public final URL schemaURL = VortexDeadTimeParameters.class.getResource("VortexMapping.xsd");

	public static VortexDeadTimeParameters createFromXML(String filename) throws Exception {
		return (VortexDeadTimeParameters) XMLHelpers.createFromXML(mappingURL, VortexDeadTimeParameters.class, schemaURL, filename);
	}

	public static void writeToXML(VortexDeadTimeParameters xspressParameters, String filename) throws Exception {
		XMLHelpers.writeToXML(mappingURL, xspressParameters, filename);
	}

	public VortexDeadTimeParameters() {
		detectorDTList = new ArrayList<DetectorDeadTimeElement>();
	}

	@Override
	public void clear() {
		if (detectorDTList != null)
			detectorDTList.clear();
	}

	public void addDetectorDeadTimeElement(DetectorDeadTimeElement detectorElement) {
		detectorDTList.add(detectorElement);
	}

	public List<DetectorDeadTimeElement> getDetectorDeadTimeElementList() {
		return detectorDTList;
	}

	public void setDetectorDeadTimeElementList(List<DetectorDeadTimeElement> detectorDTList) {
		this.detectorDTList = detectorDTList;
	}

	public DetectorDeadTimeElement getDetectorDeadTimeElement(int index) {
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
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		VortexDeadTimeParameters other = (VortexDeadTimeParameters) obj;
		
		if (detectorDTList == null) {
			if (other.detectorDTList != null) {
				return false;
			}
		} else if (!detectorDTList.equals(other.detectorDTList)) {
			return false;
		}
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
