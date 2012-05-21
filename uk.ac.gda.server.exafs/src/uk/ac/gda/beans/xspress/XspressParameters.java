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

import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.beanutils.BeanUtils;

import uk.ac.gda.beans.ElementCountsData;
import uk.ac.gda.beans.IRichBean;

/**
 *
 */
public class XspressParameters implements Serializable,IRichBean {	
	/**
	 * The region type when the ROI returns the sum of counts in the region, so acts like a scaler.
	 */
	public static final String VIRTUALSCALER = "Virtual Scaler";

	private String                detectorName;
	private String                resGrade;
	private String regionType = VIRTUALSCALER;
	private List<DetectorElement> detectorList;
	private ElementCountsData[]   data;
	private String                readoutMode;
	private boolean               editIndividualElements;

	static public final URL mappingURL = XspressParameters.class.getResource("XspressMapping.xml");

	static public final URL schemaURL  = XspressParameters.class.getResource("XspressMapping.xsd");
	
	private boolean xspressOnlyShowFF = false;
	private boolean xspressShowDTRawValues = false;
	private boolean saveRawSpectrum = false;
	
	public XspressParameters() {
		detectorList = new ArrayList<DetectorElement>();
	}
	
	@Override
	public void clear() {
		if (detectorList!=null) detectorList.clear();
		editIndividualElements = true;
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
		result = prime * result + ((detectorList == null) ? 0 : detectorList.hashCode());
		result = prime * result + ((detectorName == null) ? 0 : detectorName.hashCode());
		result = prime * result + ((readoutMode == null) ? 0 : readoutMode.hashCode());
		result = prime * result + ((resGrade == null) ? 0 : resGrade.hashCode());
		result = prime * result + ((regionType == null) ? 0 : regionType.hashCode());
		result = prime * result + (editIndividualElements ? 1231 : 1237);
		result = prime * result + (xspressOnlyShowFF ? 1231 : 1237);
		result = prime * result + (xspressShowDTRawValues ? 1231 : 1237);
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
		XspressParameters other = (XspressParameters) obj;
		if (editIndividualElements != other.editIndividualElements)
			return false;
		if (detectorList == null) {
			if (other.detectorList != null)
				return false;
		} else if (!detectorList.equals(other.detectorList))
			return false;
		if (detectorName == null) {
			if (other.detectorName != null)
				return false;
		} else if (!detectorName.equals(other.detectorName))
			return false;
		if (readoutMode == null) {
			if (other.readoutMode != null)
				return false;
		} else if (!readoutMode.equals(other.readoutMode))
			return false;
		if (resGrade == null) {
			if (other.resGrade != null)
				return false;
		} else if (!resGrade.equals(other.resGrade))
			return false;
		if (regionType == null) {
			if (other.regionType != null)
				return false;
		} else if (!regionType.equals(other.regionType))
			return false;
		if (xspressOnlyShowFF != other.xspressOnlyShowFF)
			return false;
		if (xspressShowDTRawValues != other.xspressShowDTRawValues)
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

	public String getDetectorName() {
		return detectorName;
	}

	public void setDetectorName(String detectorName) {
		this.detectorName = detectorName;
	}

	public String getResGrade() {
		return resGrade;
	}

	public void setResGrade(String resGrade) {
		this.resGrade = resGrade;
	}

	public ElementCountsData[] getData() {
		return data;
	}

	public void setData(ElementCountsData[] data) {
		this.data = data;
	}

	public String getReadoutMode() {
		return readoutMode;
	}

	public void setReadoutMode(String readoutMode) {
		this.readoutMode = readoutMode;
	}

	public void setEditIndividualElements(boolean editIndividualElements) {
		this.editIndividualElements = editIndividualElements;
	}

	public boolean isEditIndividualElements() {
		return editIndividualElements;
	}

	public void setXspressOnlyShowFF(boolean xspressOnlyShowFF) {
		this.xspressOnlyShowFF = xspressOnlyShowFF;
	}

	public boolean isXspressOnlyShowFF() {
		return xspressOnlyShowFF;
	}
	
	public void setXspressShowDTRawValues(boolean xspressShowDTRawValues) {
		this.xspressShowDTRawValues = xspressShowDTRawValues;
	}

	public boolean isXspressShowDTRawValues() {
		return xspressShowDTRawValues;
	}

	public void setSaveRawSpectrum(boolean saveRawSpectrum) {
		this.saveRawSpectrum = saveRawSpectrum;
	}

	public boolean isSaveRawSpectrum() {
		return saveRawSpectrum;
	}
	
	/**
	 * @return Returns the regionType.
	 */
	public String getRegionType() {
		return regionType;
	}

	/**
	 * @param regionType
	 *            The regionType to set.
	 */
	public void setRegionType(String regionType) {
		this.regionType = regionType;
	}

}
