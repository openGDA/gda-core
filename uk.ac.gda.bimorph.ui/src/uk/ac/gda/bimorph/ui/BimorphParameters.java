/*-
 * Copyright © 2010 Diamond Light Source Ltd.
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

package uk.ac.gda.bimorph.ui;

import java.io.Serializable;
import java.net.URL;

public class BimorphParameters implements Serializable{
	private static final long serialVersionUID = 1L;
	static public final URL mappingURL = BimorphParameters.class.getResource("BimorphParametersMapping.xml");
	static public final URL schemaURL  = BimorphParameters.class.getResource("BimorphParametersMapping.xsd");
	private String mirrorScannableName = "";
	private String mirrorType = "";
	private int numberOfElectrodes;
	private double mirrorLength;
	private double voltageIncrement;
	private String slitSizeScannable = "";
	private String slitPosScannable = "";
	private double slitSize;
	private double slitStart;
	private double slitEnd;
	private double slitStep;
	private String otherSlitSizeScannable = "";
	private String otherSlitPosScannable = "";
	private double otherSlitPosValue;
	private double otherSlitSize;
	private String detectorName = "";
	private double exposureTime;
	private double settleTime;
	private String scanDirectory = "";
	private String scanNumberInputs = "";
	private String errorFile = "";
	private double beamOffset;
	private String bimorphScannableName = "";
	private String bimorphVoltages = "";
	private double pixelSize;
	private double detectorDistance;
	private String iSign = "";
	private String inv = "";
	private String method = "";
	private double presentSourceMirrorDistance;
	private double presentMirrorFocusDistance;
	private double presentAngleOfIncidence;
	private double newSourceMirrorDistance;
	private double newMirrorFocusDistance;
	private double newAngleOfIncidence;
	private double focusSize;
	private boolean calculateErrorFile;
	private boolean returnVoltagesToOriginalValues;
	private boolean autoOffset;
	private boolean autoDist;
	private double presentDetDist;
	private double slitScanDetDist;
	private String bimorphGroups = "";
	private boolean btnGroupElectrodesTogether;
	private double minSlitPos;
	private double maxSlitPos;
	
	public Double getPixelSize() {
		return pixelSize;
	}
	
	public void setPixelSize(Double pixelSize) {
		this.pixelSize = pixelSize;
	}
	
	public String getMirrorScannableName() {
		return mirrorScannableName;
	}
	
	public void setMirrorScannableName(String mirrorScannableName) {
		this.mirrorScannableName = mirrorScannableName;
	}
	
	public String getMirrorType() {
		return mirrorType;
	}
	
	public void setMirrorType(String mirrorType) {
		this.mirrorType = mirrorType;
	}
	
	public Integer getNumberOfElectrodes() {
		return numberOfElectrodes;
	}
	
	public void setNumberOfElectrodes(Integer numberOfElectrodes) {
		this.numberOfElectrodes = numberOfElectrodes;
	}
	
	public Double getMirrorLength() {
		return mirrorLength;
	}
	
	public void setMirrorLength(double mirrorLength) {
		this.mirrorLength = mirrorLength;
	}
	
	public Double getVoltageIncrement() {
		return voltageIncrement;
	}
	
	public void setVoltageIncrement(Double voltageIncrement) {
		this.voltageIncrement = voltageIncrement;
	}
	
	public String getSlitSizeScannable() {
		return slitSizeScannable;
	}
	
	public void setSlitSizeScannable(String slitSizeScannable) {
		this.slitSizeScannable = slitSizeScannable;
	}
	
	public String getSlitPosScannable() {
		return slitPosScannable;
	}
	
	public void setSlitPosScannable(String slitPosScannable) {
		this.slitPosScannable = slitPosScannable;
	}
	
	public Double getSlitSize() {
		return slitSize;
	}
	
	public void setSlitSize(Double slitSize) {
		this.slitSize = slitSize;
	}
	
	public Double getSlitStart() {
		return slitStart;
	}
	
	public void setSlitStart(Double slitStart) {
		this.slitStart = slitStart;
	}
	
	public Double getSlitEnd() {
		return slitEnd;
	}
	
	public void setSlitEnd(Double slitEnd) {
		this.slitEnd = slitEnd;
	}
	
	public Double getSlitStep() {
		return slitStep;
	}
	
	public void setSlitStep(Double slitStep) {
		this.slitStep = slitStep;
	}
	
	public String getOtherSlitSizeScannable() {
		return otherSlitSizeScannable;
	}
	
	public void setOtherSlitSizeScannable(String otherSlitSizeScannable) {
		this.otherSlitSizeScannable = otherSlitSizeScannable;
	}
	
	public String getOtherSlitPosScannable() {
		return otherSlitPosScannable;
	}
	
	public void setOtherSlitPosScannable(String otherSlitPosScannable) {
		this.otherSlitPosScannable = otherSlitPosScannable;
	}
	
	public Double getOtherSlitPosValue() {
		return otherSlitPosValue;
	}
	
	public void setOtherSlitPosValue(Double otherSlitPosValue) {
		this.otherSlitPosValue = otherSlitPosValue;
	}
	
	public Double getOtherSlitSize() {
		return otherSlitSize;
	}
	
	public void setOtherSlitSize(Double otherSlitSize) {
		this.otherSlitSize = otherSlitSize;
	}
	
	public String getDetectorName() {
		return detectorName;
	}
	
	public void setDetectorName(String detectorName) {
		this.detectorName = detectorName;
	}
	
	public Double getExposureTime() {
		return exposureTime;
	}
	
	public void setExposureTime(Double exposureTime) {
		this.exposureTime = exposureTime;
	}
	
	public Double getSettleTime() {
		return settleTime;
	}
	
	public void setSettleTime(Double settleTime) {
		this.settleTime = settleTime;
	}
	
	public String getScanDirectory() {
		return scanDirectory;
	}
	
	public void setScanDirectory(String scanDirectory) {
		this.scanDirectory = scanDirectory;
	}
	
	public String getScanNumberInputs() {
		return scanNumberInputs;
	}
	
	public void setScanNumberInputs(String scanNumberInputs) {
		this.scanNumberInputs = scanNumberInputs;
	}
	
	public String getErrorFile() {
		return errorFile;
	}
	
	public void setErrorFile(String errorFile) {
		this.errorFile = errorFile;
	}
	
	public boolean isAutoOffset() {
		return autoOffset;
	}
	
	public void setAutoOffset(boolean autoOffset) {
		this.autoOffset = autoOffset;
	}
	
	public Double getBeamOffset() {
		return beamOffset;
	}
	
	public void setBeamOffset(Double beamOffset) {
		this.beamOffset = beamOffset;
	}
	
	public String getBimorphScannableName() {
		return bimorphScannableName;
	}
	
	public void setBimorphScannableName(String bimorphScannableName) {
		this.bimorphScannableName = bimorphScannableName;
	}
	
	public String getBimorphVoltages() {
		return bimorphVoltages;
	}
	
	public void setBimorphVoltages(String bimorphVoltages) {
		this.bimorphVoltages = bimorphVoltages;
	}
	
	public static URL getMappingurl() {
		return mappingURL;
	}
	
	public static URL getSchemaurl() {
		return schemaURL;
	}
	
	public Double getDetectorDistance() {
		return detectorDistance;
	}
	
	public void setDetectorDistance(Double detectorDistance) {
		this.detectorDistance = detectorDistance;
	}
	
	public boolean isCalculateErrorFile() {
		return calculateErrorFile;
	}
	
	public void setCalculateErrorFile(boolean calculateErrorFile) {
		this.calculateErrorFile = calculateErrorFile;
	}
	
	public String getISign() {
		return iSign;
	}
	
	public void setISign(String iSign) {
		this.iSign = iSign;
	}
	
	public Double getPresentSourceMirrorDistance() {
		return presentSourceMirrorDistance;
	}
	
	public void setPresentSourceMirrorDistance(Double presentSourceMirrorDistance) {
		this.presentSourceMirrorDistance = presentSourceMirrorDistance;
	}
	
	public Double getPresentMirrorFocusDistance() {
		return presentMirrorFocusDistance;
	}
	
	public void setPresentMirrorFocusDistance(Double presentMirrorFocusDistance) {
		this.presentMirrorFocusDistance = presentMirrorFocusDistance;
	}
	
	public Double getPresentAngleOfIncidence() {
		return presentAngleOfIncidence;
	}
	
	public void setPresentAngleOfIncidence(Double presentAngleOfIncidence) {
		this.presentAngleOfIncidence = presentAngleOfIncidence;
	}
	
	public Double getNewSourceMirrorDistance() {
		return newSourceMirrorDistance;
	}
	
	public void setNewSourceMirrorDistance(Double newSourceMirrorDistance) {
		this.newSourceMirrorDistance = newSourceMirrorDistance;
	}
	
	public Double getNewMirrorFocusDistance() {
		return newMirrorFocusDistance;
	}
	
	public void setNewMirrorFocusDistance(Double newMirrorFocusDistance) {
		this.newMirrorFocusDistance = newMirrorFocusDistance;
	}
	
	public Double getNewAngleOfIncidence() {
		return newAngleOfIncidence;
	}
	
	public void setNewAngleOfIncidence(Double newAngleOfIncidence) {
		this.newAngleOfIncidence = newAngleOfIncidence;
	}
	
	public Double getFocusSize() {
		return focusSize;
	}
	
	public void setFocusSize(Double focusSize) {
		this.focusSize = focusSize;
	}

	public boolean isReturnVoltagesToOriginalValues() {
		return returnVoltagesToOriginalValues;
	}
	
	public void setReturnVoltagesToOriginalValues(boolean returnVoltagesToOriginalValues) {
		this.returnVoltagesToOriginalValues = returnVoltagesToOriginalValues;
	}
	
	public void setMirrorLength(Double mirrorLength) {
		this.mirrorLength = mirrorLength;
	}
	
	public String getBimorphGroups() {
		return bimorphGroups;
	}
	
	public void setBimorphGroups(String bimorphGroups) {
		this.bimorphGroups = bimorphGroups;
	}
	
	public boolean isBtnGroupElectrodesTogether() {
		return btnGroupElectrodesTogether;
	}
	
	public void setBtnGroupElectrodesTogether(boolean btnGroupElectrodesTogether) {
		this.btnGroupElectrodesTogether = btnGroupElectrodesTogether;
	}
	
	public Double getPresentDetDist() {
		return this.presentDetDist;
	}
	
	public void setPresentDetDist(Double presentDetDist) {
		this.presentDetDist = presentDetDist;
	}
	
	public Double getSlitScanDetDist() {
		return slitScanDetDist;
	}
	
	public void setSlitScanDetDist(Double slitScanDetDist) {
		this.slitScanDetDist = slitScanDetDist;
	}
	
	public boolean isAutoDist() {
		return autoDist;
	}
	
	public void setAutoDist(boolean autoDist) {
		this.autoDist = autoDist;
	}
	
	public Double getMinSlitPos() {
		return minSlitPos;
	}
	
	public void setMinSlitPos(Double minSlitPos) {
		this.minSlitPos = minSlitPos;
	}
	
	public Double getMaxSlitPos() {
		return maxSlitPos;
	}
	
	public void setMaxSlitPos(Double maxSlitPos) {
		this.maxSlitPos = maxSlitPos;
	}
	
	public void clear(){
		mirrorScannableName = "";
		mirrorType = "";
		numberOfElectrodes = 0;
		mirrorLength = 0;
		voltageIncrement = 0;
		slitSizeScannable = "";
		slitPosScannable = "";
		slitSize = 0;
		slitStart = 0;
		slitEnd = 0;
		slitStep = 0;
		otherSlitSizeScannable = "";
		otherSlitPosScannable = "";
		otherSlitPosValue = 0;
		otherSlitSize = 0;
		detectorName = "";
		exposureTime = 0;
		settleTime = 0;
		scanDirectory = "";
		scanNumberInputs = "";
		errorFile = "";
		beamOffset = 0;
		bimorphScannableName = "";
		bimorphVoltages = "";
		pixelSize = 0;
		detectorDistance = 0;
		iSign = "";
		presentSourceMirrorDistance = 0;
		presentMirrorFocusDistance = 0;
		presentAngleOfIncidence = 0;
		newSourceMirrorDistance = 0;
		newMirrorFocusDistance = 0;
		newAngleOfIncidence = 0;
		focusSize = 0;
		calculateErrorFile = false;
		returnVoltagesToOriginalValues = false;
		autoOffset = false;
		bimorphGroups = "";
		btnGroupElectrodesTogether = false;
		autoDist = false;
		slitScanDetDist = 0;
		presentDetDist = 0;
		minSlitPos = 0;
		maxSlitPos = 0;
		inv = null;
		method = null;
	}
	
	public String getInv() {
		return inv;
	}
	public void setInv(String inv) {
		this.inv = inv;
	}
	public String getMethod() {
		return method;
	}
	public void setMethod(String method) {
		this.method = method;
	}

}
