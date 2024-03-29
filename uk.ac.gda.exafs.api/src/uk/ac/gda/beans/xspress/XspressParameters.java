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

import uk.ac.gda.beans.exafs.IDetectorConfigurationParameters;
import uk.ac.gda.beans.vortex.DetectorElement;
import uk.ac.gda.devices.detector.FluorescenceDetectorParameters;

public class XspressParameters implements Serializable,IDetectorConfigurationParameters, FluorescenceDetectorParameters {
	/** Readout from single channel analyser */
	 public static final String ROI_SCA_WINDOW = "SCA window";

	/** Integrated in software from MCA readout */
	public static final String ROI_VIRTUAL = "Calculated from MCA";

	static public final URL mappingURL = XspressParameters.class.getResource("XspressMapping.xml");
	static public final URL schemaURL = XspressParameters.class.getResource("XspressMapping.xsd");

	static public final String READOUT_MODE_SCALERS_ONLY = "Scalers only";
	static public final String READOUT_MODE_SCALERS_AND_MCA = "Scalers and MCA";
	static public final String READOUT_MODE_REGIONSOFINTEREST = "Regions Of Interest";

	private String detectorName;
	private String resGrade;
	private String regionType = ROI_SCA_WINDOW;
	private List<DetectorElement> detectorList;
	private String readoutMode;

	private double deadtimeCorrectionEnergy = 0;

	private boolean editIndividualElements;
	private boolean onlyShowFF = false;
	private boolean showDTRawValues = false;
	private boolean saveRawSpectrum = false;

	private int selectedRegionNumber = 0;

	public XspressParameters() {
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
		result = prime * result + ((detectorList == null) ? 0 : detectorList.hashCode());
		result = prime * result + ((detectorName == null) ? 0 : detectorName.hashCode());
		result = prime * result + ((readoutMode == null) ? 0 : readoutMode.hashCode());
		result = prime * result + ((resGrade == null) ? 0 : resGrade.hashCode());
		result = prime * result + ((regionType == null) ? 0 : regionType.hashCode());
		result = prime * result + (editIndividualElements ? 1231 : 1237);
		result = prime * result + (onlyShowFF ? 1231 : 1237);
		result = prime * result + (showDTRawValues ? 1231 : 1237);
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
		if (onlyShowFF != other.onlyShowFF)
			return false;
		if (showDTRawValues != other.showDTRawValues)
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

	@Override
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

	public void setOnlyShowFF(boolean onlyShowFF) {
		this.onlyShowFF = onlyShowFF;
	}

	public boolean isOnlyShowFF() {
		return onlyShowFF;
	}

	public void setShowDTRawValues(boolean xspressShowDTRawValues) {
		this.showDTRawValues = xspressShowDTRawValues;
	}

	public boolean isShowDTRawValues() {
		return showDTRawValues;
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
		// the following conversion is for backward compatibility
		if (regionType.equalsIgnoreCase("Virtual scaler")) {
			regionType = ROI_SCA_WINDOW;
		} else if (regionType.equalsIgnoreCase("MCA")) {
			regionType = ROI_VIRTUAL;
		}

		this.regionType = regionType;
	}

	public int getSelectedRegionNumber() {
		return selectedRegionNumber;
	}

	public void setSelectedRegionNumber(int selectedRegionNumber) {
		this.selectedRegionNumber = selectedRegionNumber;
	}

	/** Get the energy to be used for the 'Deadtime correction calculation' (xspress2, xspress4). Energy in keV */
	public double getDeadtimeCorrectionEnergy() {
		return deadtimeCorrectionEnergy;
	}

	/** Set the energy to be used for the 'Deadtime correction calculation' (xspress2, xspress4). Energy in keV */
	public void setDeadtimeCorrectionEnergy(double deadtimeCorrectionEnergy) {
		this.deadtimeCorrectionEnergy = deadtimeCorrectionEnergy;
	}
}
