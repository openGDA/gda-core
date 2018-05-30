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
import java.util.Vector;

import org.apache.commons.beanutils.BeanUtils;

import uk.ac.gda.beans.DetectorROI;
import uk.ac.gda.devices.detector.FluorescenceDetectorParameters;
import uk.ac.gda.util.beans.xml.XMLHelpers;

public class VortexParameters implements Serializable, FluorescenceDetectorParameters {

	private List<DetectorElement> detectorList;
	private String countType;
	private double collectionTime;
	private String detectorName;
	private String tfgName;
	private boolean saveRawSpectrum = false;
	static public final URL mappingURL = VortexParameters.class.getResource("VortexMapping.xml");

	static public final URL schemaURL = VortexParameters.class.getResource("VortexMapping.xsd");

	private int selectedRegionNumber =0;

	public static VortexParameters createFromXML(String filename) throws Exception {
		return XMLHelpers.createFromXML(mappingURL, VortexParameters.class, schemaURL, filename);
	}

	public static void writeToXML(VortexParameters xspressParameters, String filename) throws Exception {
		XMLHelpers.writeToXML(mappingURL, xspressParameters, filename);
	}

	public VortexParameters() {
		detectorList = new ArrayList<>();
	}

	public void addDetectorElement(DetectorElement detectorElement) {
		detectorList.add(detectorElement);
	}

	@Override
	public List<DetectorElement> getDetectorList() {
		return detectorList;
	}

	@Override
	public void setDetectorList(List<DetectorElement> detectorList) {
		this.detectorList = detectorList;
	}

	@Override
	public DetectorElement getDetector(int index) {
		return detectorList.get(index);
	}

	public void clear(){
		if (detectorList!=null) detectorList.clear();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(collectionTime);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((countType == null) ? 0 : countType.hashCode());
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

		if (Double.doubleToLongBits(collectionTime) != Double.doubleToLongBits(objectCast(obj).collectionTime)) {
			return false;
		}
		if (countType == null) {
			if (objectCast(obj).countType != null) {
				return false;
			}
		} else if (!countType.equals(objectCast(obj).countType)) {
			return false;
		}
		if (detectorList == null) {
			if (objectCast(obj).detectorList != null) {
				return false;
			}
		} else if (!detectorList.equals(objectCast(obj).detectorList)) {
			return false;
		}
		if (detectorName == null) {
			if (objectCast(obj).detectorName != null) {
				return false;
			}
		} else if (!detectorName.equals(objectCast(obj).detectorName)) {
			return false;
		}
		if (tfgName == null) {
			if (objectCast(obj).tfgName != null) {
				return false;
			}
		} else if (!tfgName.equals(objectCast(obj).tfgName)) {
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

	@Override
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

	/**
	 * Copy constructor.
	 */
	public VortexParameters(VortexParameters vp) {
		setDetectorName(vp.detectorName);
		setCountType(vp.countType);
		setSaveRawSpectrum(vp.saveRawSpectrum);
		setCollectionTime(vp.collectionTime);

		Vector<DetectorElement> des = new Vector<>();
		for (DetectorElement d : vp.getDetectorList()) {
			des.add(new DetectorElement(d));
		}
		setDetectorList(des);
		setTfgName(vp.tfgName);
	}

	public List<DetectorROI> getRois() {
		List<DetectorROI> list = new Vector<>();
		for (DetectorElement d : getDetectorList()) {
			list.addAll(d.getRegionList());
		}
		return list;
	}

	public int getSelectedRegionNumber() {
		return selectedRegionNumber;
	}

	public void setSelectedRegionNumber(int selectedRegionNumber) {
		this.selectedRegionNumber = selectedRegionNumber;
	}

	public VortexParameters objectCast (Object obj){
		return (VortexParameters) obj;
	}

}
