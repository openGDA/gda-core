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

package uk.ac.gda.beans.exafs;

import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.commons.beanutils.BeanUtils;

public class XesScanParameters implements Serializable, IScanParameters {

	static public final URL mappingURL = XesScanParameters.class.getResource("ExafsParameterMapping.xml");

	static public final URL schemaURL = XesScanParameters.class.getResource("ExafsParameterMapping.xsd");

	public static final String EF_OUTER_MONO_INNER = "Ef outer, E0 inner";
	public static final String MONO_OUTER_EF_INNER = "E0 outer, Ef inner";

	public static String[] LOOPOPTIONS = new String[] { EF_OUTER_MONO_INNER, MONO_OUTER_EF_INNER };

	// The types of scan performed under XES
	public static final int SCAN_XES_FIXED_MONO = 1;
	public static final int FIXED_XES_SCAN_XAS = 2;
	public static final int FIXED_XES_SCAN_XANES = 3;
	public static final int SCAN_XES_SCAN_MONO = 4;
	public static final int SCAN_XES_REGION_FIXED_MONO = 5;

	private boolean shouldValidate = true;

	// Type of scan
	private int scanType;

	private ScanColourType scanColourType;

	// SCAN_XES_FIXED_MONO
	private String element;
	private String edge;
	private Double monoEnergy;

	// SCAN_XES_FIXED_MONO and SCAN_XES_SCAN_MONO
	private Double xesInitialEnergy;
	private Double xesFinalEnergy;
	private Double xesStepSize;
	private Double xesIntegrationTime;

	// SCAN_XES_SCAN_MONO
	private Double monoInitialEnergy;
	private Double monoFinalEnergy;
	private Double monoStepSize;
	private Double xesEnergy;

	// Reference to XAS or XANEs file. Only required for FIXED_XES_SCAN_XAS and
	// FIXED_XES_SCAN_XANES
	private String scanFileName;

	// NOTE *NOT* energy probably it is the Bragg Angle of the XES
	private String scannableName;

	private boolean additionalCrystal0;
	private boolean additionalCrystal1;
	private boolean additionalCrystal2;
	private boolean additionalCrystal3;

	// order of the 2D scan
	private String loopChoice;

	// the name of the set of offsets (the 'store') which to apply to the XES spectrometer for this scan
	private String offsetsStoreName;

	private List<SpectrometerScanParameters> spectrometerScanParameters;

	@Override
	public String toString() {
		try {
			return BeanUtils.describe(this).toString();
		} catch (Exception e) {
			return e.getMessage();
		}
	}

	public int getScanType() {
		return scanType;
	}

	public void setScanType(int scanType) {
		this.scanType = scanType;
	}

	public boolean isShouldValidate() {
		return shouldValidate;
	}

	public void setShouldValidate(boolean shouldValidate) {
		this.shouldValidate = shouldValidate;
	}

	public Double getXesIntegrationTime() {
		return xesIntegrationTime;
	}

	public void setXesIntegrationTime(Double integrationTime) {
		this.xesIntegrationTime = integrationTime;
	}

	public String getScanFileName() {
		return scanFileName;
	}

	public void setScanFileName(String fileName) {
		this.scanFileName = fileName;
	}

	@Override
	public String getScannableName() {
		return scannableName;
	}

	public void setScannableName(String scannableName) {
		this.scannableName = scannableName;
	}

	public String getElement() {
		return element;
	}

	public void setElement(String element) {
		this.element = element;
	}

	public String getEdge() {
		return edge;
	}

	public void setEdge(String edge) {
		this.edge = edge;
	}

	public Double getXesInitialEnergy() {
		return xesInitialEnergy;
	}

	public void setXesInitialEnergy(Double xesInitialEnergy) {
		this.xesInitialEnergy = xesInitialEnergy;
	}

	public Double getXesFinalEnergy() {
		return xesFinalEnergy;
	}

	public void setXesFinalEnergy(Double xesFinalEnergy) {
		this.xesFinalEnergy = xesFinalEnergy;
	}

	public Double getXesStepSize() {
		return xesStepSize;
	}

	public void setXesStepSize(Double xesStepSize) {
		this.xesStepSize = xesStepSize;
	}

	public String getLoopChoice() {
		return loopChoice;
	}

	public void setLoopChoice(String loopChoice) {
		this.loopChoice = loopChoice;
	}

	public Double getMonoInitialEnergy() {
		return monoInitialEnergy;
	}

	public void setMonoInitialEnergy(Double monoInitialEnergy) {
		this.monoInitialEnergy = monoInitialEnergy;
	}

	public Double getMonoFinalEnergy() {
		return monoFinalEnergy;
	}

	public void setMonoFinalEnergy(Double monoFinalEnergy) {
		this.monoFinalEnergy = monoFinalEnergy;
	}

	public Double getMonoStepSize() {
		return monoStepSize;
	}

	public void setMonoStepSize(Double monoStepSize) {
		this.monoStepSize = monoStepSize;
	}

	public Double getXesEnergy() {
		return xesEnergy;
	}

	public void setXesEnergy(Double xesEnergy) {
		this.xesEnergy = xesEnergy;
	}

	public Double getMonoEnergy() {
		return monoEnergy;
	}

	public void setMonoEnergy(Double monoEnergy) {
		this.monoEnergy = monoEnergy;
	}

	public boolean isAdditionalCrystal0() {
		return additionalCrystal0;
	}

	public void setAdditionalCrystal0(boolean additionalCrystal0) {
		this.additionalCrystal0 = additionalCrystal0;
	}

	public boolean isAdditionalCrystal1() {
		return additionalCrystal1;
	}

	public void setAdditionalCrystal1(boolean additionalCrystal1) {
		this.additionalCrystal1 = additionalCrystal1;
	}

	public boolean isAdditionalCrystal2() {
		return additionalCrystal2;
	}

	public void setAdditionalCrystal2(boolean additionalCrystal2) {
		this.additionalCrystal2 = additionalCrystal2;
	}

	public boolean isAdditionalCrystal3() {
		return additionalCrystal3;
	}

	public void setAdditionalCrystal3(boolean additionalCrystal3) {
		this.additionalCrystal3 = additionalCrystal3;
	}

	public String getOffsetsStoreName() {
		return offsetsStoreName;
	}

	public void setOffsetsStoreName(String offsetsStoreName) {
		this.offsetsStoreName = offsetsStoreName;
	}

	public List<SpectrometerScanParameters> getSpectrometerScanParameters() {
		return spectrometerScanParameters;
	}

	public void addSpectrometerScanParameter(SpectrometerScanParameters p) {
		if (spectrometerScanParameters == null) {
			spectrometerScanParameters = new ArrayList<>();
		}
		spectrometerScanParameters.add(p);
	}

	public void setSpectrometerScanParameters(List<SpectrometerScanParameters> spectrometerScanParameters) {
		this.spectrometerScanParameters = new ArrayList<>(spectrometerScanParameters);
	}

	public void clear() {
		if (spectrometerScanParameters != null) {
			spectrometerScanParameters = null;
		}
	}

	public ScanColourType getScanColourType() {
		return scanColourType;
	}

	public void setScanColourType(ScanColourType scanColourType) {
		this.scanColourType = scanColourType;
	}

	/**
	 * Used by Castor XML during serialization to convert {@link ScanColourType} to integer index
	 *
	 * @return index of ScanColourType enum object; null if not set
	 */
	public Integer getScanColourTypeIndex() {
		return scanColourType == null ? null : scanColourType.getIndex();
	}

	/**
	 * Used by Castor XML during deserialization to set {@link ScanColourType} from an integer index.
	 * @param scanColourTypeIndex
	 */
	public void setScanColourTypeIndex(int scanColourTypeIndex) {
		this.scanColourType = ScanColourType.fromIndex(scanColourTypeIndex);
	}

	@Override
	public int hashCode() {
		return Objects.hash(additionalCrystal0, additionalCrystal1, additionalCrystal2, additionalCrystal3, edge,
				element, loopChoice, monoEnergy, monoFinalEnergy, monoInitialEnergy, monoStepSize, offsetsStoreName,
				scanColourType, scanFileName, scanType, scannableName, shouldValidate, spectrometerScanParameters,
				xesEnergy, xesFinalEnergy, xesInitialEnergy, xesIntegrationTime, xesStepSize);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		XesScanParameters other = (XesScanParameters) obj;
		return additionalCrystal0 == other.additionalCrystal0 && additionalCrystal1 == other.additionalCrystal1
				&& additionalCrystal2 == other.additionalCrystal2 && additionalCrystal3 == other.additionalCrystal3
				&& Objects.equals(edge, other.edge) && Objects.equals(element, other.element)
				&& Objects.equals(loopChoice, other.loopChoice) && Objects.equals(monoEnergy, other.monoEnergy)
				&& Objects.equals(monoFinalEnergy, other.monoFinalEnergy)
				&& Objects.equals(monoInitialEnergy, other.monoInitialEnergy)
				&& Objects.equals(monoStepSize, other.monoStepSize)
				&& Objects.equals(offsetsStoreName, other.offsetsStoreName) && scanColourType == other.scanColourType
				&& Objects.equals(scanFileName, other.scanFileName) && scanType == other.scanType
				&& Objects.equals(scannableName, other.scannableName) && shouldValidate == other.shouldValidate
				&& Objects.equals(spectrometerScanParameters, other.spectrometerScanParameters)
				&& Objects.equals(xesEnergy, other.xesEnergy) && Objects.equals(xesFinalEnergy, other.xesFinalEnergy)
				&& Objects.equals(xesInitialEnergy, other.xesInitialEnergy)
				&& Objects.equals(xesIntegrationTime, other.xesIntegrationTime)
				&& Objects.equals(xesStepSize, other.xesStepSize);
	}
}
