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
import java.util.Arrays;
import java.util.List;

import org.apache.commons.beanutils.BeanUtils;

import uk.ac.gda.beans.ElementCountsData;
import uk.ac.gda.beans.IRichBean;
import uk.ac.gda.util.beans.xml.XMLHelpers;

public class VortexParameters implements Serializable, IRichBean {

	private List<DetectorElement> detectorList;
	private String countType;
	private double collectionTime;
	private double deadTime;
	private ElementCountsData[] data;
	private String detectorName;
	private String tfgName;
	private boolean saveRawSpectrum = false;
	static public final URL mappingURL = VortexParameters.class.getResource("VortexMapping.xml");

	static public final URL schemaURL = VortexParameters.class.getResource("VortexMapping.xsd");

	public static VortexParameters createFromXML(String filename) throws Exception {
		return (VortexParameters) XMLHelpers.createFromXML(mappingURL, VortexParameters.class, schemaURL, filename);
	}

	public static void writeToXML(VortexParameters xspressParameters, String filename) throws Exception {
		XMLHelpers.writeToXML(mappingURL, xspressParameters, filename);
	}

	public VortexParameters() {
		detectorList = new ArrayList<DetectorElement>();
	}

	@Override
	public void clear() {
		if (detectorList != null)
			detectorList.clear();
	}

	public void addDetectorElement(DetectorElement detectorElement) {
		detectorList.add(detectorElement);
	}

	public List<DetectorElement> getDetectorList() {
		return detectorList;
	}

	public void setDetectorList(List<DetectorElement> detectorList) {
		this.detectorList = detectorList;
	}

	public DetectorElement getDetector(int index) {
		return detectorList.get(index);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(collectionTime);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((countType == null) ? 0 : countType.hashCode());
		result = prime * result + Arrays.hashCode(data);
		temp = Double.doubleToLongBits(deadTime);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((detectorList == null) ? 0 : detectorList.hashCode());
		result = prime * result + ((detectorName == null) ? 0 : detectorName.hashCode());
		result = prime * result + ((tfgName == null) ? 0 : tfgName.hashCode());
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
		VortexParameters other = (VortexParameters) obj;
		if (Double.doubleToLongBits(collectionTime) != Double.doubleToLongBits(other.collectionTime)) {
			return false;
		}
		if (countType == null) {
			if (other.countType != null) {
				return false;
			}
		} else if (!countType.equals(other.countType)) {
			return false;
		}
		if (!Arrays.equals(data, other.data)) {
			return false;
		}
		if (Double.doubleToLongBits(deadTime) != Double.doubleToLongBits(other.deadTime)) {
			return false;
		}
		if (detectorList == null) {
			if (other.detectorList != null) {
				return false;
			}
		} else if (!detectorList.equals(other.detectorList)) {
			return false;
		}
		if (detectorName == null) {
			if (other.detectorName != null) {
				return false;
			}
		} else if (!detectorName.equals(other.detectorName)) {
			return false;
		}
		if (tfgName == null) {
			if (other.tfgName != null) {
				return false;
			}
		} else if (!tfgName.equals(other.tfgName)) {
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

	public String getCountType() {
		return countType;
	}

	public void setCountType(String countType) {
		this.countType = countType;
	}

	public double getCollectionTime() {
		return collectionTime;
	}

	public void setCollectionTime(double collectionTime) {
		this.collectionTime = collectionTime;
	}

	public double getDeadTime() {
		return deadTime;
	}

	public void setDeadTime(double deadTime) {
		this.deadTime = deadTime;
	}

	public void setData(ElementCountsData[] data) {
		this.data = data;
	}

	public ElementCountsData[] getData() {
		return data;
	}

	public String getDetectorName() {
		return detectorName;
	}

	public void setDetectorName(String detectorName) {
		this.detectorName = detectorName;
	}

	public String getTfgName() {
		return tfgName;
	}

	public void setTfgName(String tfgName) {
		this.tfgName = tfgName;
	}
	
	public void setSaveRawSpectrum(boolean saveRawSpectrum) {
		this.saveRawSpectrum = saveRawSpectrum;
	}

	public boolean isSaveRawSpectrum() {
		return saveRawSpectrum;
	}
}
