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

import org.apache.commons.beanutils.BeanUtils;

import uk.ac.gda.util.beans.xml.XMLHelpers;

/**
 * Class to hold all xas scan parameters
 */
public class XasScanParameters implements Serializable, IScanParameters {

	static public final URL mappingURL = XasScanParameters.class.getResource("ExafsParameterMapping.xml");

	static public final URL schemaUrl = XasScanParameters.class.getResource("ExafsParameterMapping.xsd");

	private String scannableName;
	private String element;
	private String edge;
	private Double initialEnergy;
	private Double finalEnergy;
	private Double edgeEnergy;
	private Double coreHole;
	private Double gaf1;
	private Double gaf2;
	private Double gaf3;
	private Double a;
	private Double b;
	private Double c;
	private Double preEdgeStep;
	private Double preEdgeTime;
	private Double edgeStep;
	private Double edgeTime;
	private Double exafsTime;
	private Double exafsFromTime;
	private Double exafsToTime;
	private Double exafsStep;
	private String exafsStepType;
	private String exafsTimeType;
	private String abGafChoice;
	private Double kWeighting;
	private boolean shouldValidate = true;

	/**
	 * Must implement clear() method on beans being used with BeanUI.
	 */
	@Override
	public void clear() {
		// We have to nullify fields here because of the
		// XML the beanline scientists require.
		a = b = c = gaf1 = gaf2 = gaf3 = null;
		exafsTime = exafsFromTime = exafsToTime = null;
	}

	public static XasScanParameters createFromXML(String filename) throws Exception {
		return (XasScanParameters) XMLHelpers.createFromXML(mappingURL, XasScanParameters.class, schemaUrl, filename);
	}

	public static void writeToXML(XasScanParameters scanParameters, String filename) throws Exception {
		XMLHelpers.writeToXML(mappingURL, scanParameters, filename);
	}

	/**
	 * @return start energy in eV
	 */
	public Double getInitialEnergy() {
		return initialEnergy;
	}

	/**
	 * @param startEnergy
	 *            in eV
	 */
	public void setInitialEnergy(Double startEnergy) {
		this.initialEnergy = startEnergy;
	}

	/**
	 * @return stop energy in eV
	 */
	public Double getFinalEnergy() {
		return finalEnergy;
	}

	/**
	 * @param stopEnergy
	 */
	public void setFinalEnergy(Double stopEnergy) {
			this.finalEnergy = stopEnergy;
	}

	/**
	 * @return the edge energy in eV
	 */
	public Double getEdgeEnergy() {
		return edgeEnergy;
	}

	/**
	 * @param edgeEnergy
	 */
	public void setEdgeEnergy(Double edgeEnergy) {
		this.edgeEnergy = edgeEnergy;
	}

	/**
	 * @return core hole
	 */
	public Double getCoreHole() {
		return coreHole;
	}

	/**
	 * @param corehole
	 */
	public void setCoreHole(Double corehole) {
		this.coreHole = corehole;
	}

	/**
	 * @return gaf1
	 */
	public Double getGaf1() {
		return gaf1;
	}

	/**
	 * @param gaf1
	 */
	public void setGaf1(Double gaf1) {
		this.gaf1 = gaf1;
	}

	/**
	 * @return gaf2
	 */
	public Double getGaf2() {
		return gaf2;
	}

	/**
	 * @param gaf2
	 */
	public void setGaf2(Double gaf2) {
		this.gaf2 = gaf2;
	}

	/**
	 * @param element
	 */
	public void setElement(String element) {
		this.element = element;
	}

	/**
	 * @param edge
	 */
	public void setEdge(String edge) {
		this.edge = edge;
	}

	/**
	 * @return element name
	 */
	public String getElement() {
		return element;
	}

	/**
	 * @return edge name
	 */
	public String getEdge() {
		return edge;
	}

	/**
	 * @return A
	 */
	public Double getA() {
		return a;
	}

	/**
	 * @return true is a and b are given
	 */
	public boolean isABGiven() {
		return a != null && b != null;
	}

	/**
	 * @return true if corehole, gaf1 and gaf2
	 */
	public boolean isCoreGiven() {
		return coreHole != null && gaf1 != null && gaf2 != null;
	}

	/**
	 * @param a
	 */
	public void setA(Double a) {
		this.a = a;
	}

	/**
	 * @return B
	 */
	public Double getB() {
		return b;
	}

	/**
	 * @param b
	 */
	public void setB(Double b) {
		this.b = b;
	}

	/**
	 * @return pre-edge step
	 */
	public Double getPreEdgeStep() {
		return preEdgeStep;
	}

	/**
	 * @param preEdgeStep
	 */
	public void setPreEdgeStep(Double preEdgeStep) {
		this.preEdgeStep = preEdgeStep;
	}

	/**
	 * @return pre-edge time
	 */
	public Double getPreEdgeTime() {
		return preEdgeTime;
	}

	/**
	 * @param preEdgeTime
	 */
	public void setPreEdgeTime(Double preEdgeTime) {
		this.preEdgeTime = preEdgeTime;
	}

	/**
	 * @return edge step increment
	 */
	public Double getEdgeStep() {
		return edgeStep;
	}

	/**
	 * @param edgeStep
	 */
	public void setEdgeStep(Double edgeStep) {
		this.edgeStep = edgeStep;
	}

	/**
	 * @return time per point over edge region
	 */
	public Double getEdgeTime() {
		return edgeTime;
	}

	/**
	 * @param edgeTime
	 */
	public void setEdgeTime(Double edgeTime) {
		this.edgeTime = edgeTime;
	}

	/**
	 * @return the constant time per point over post edge scan
	 */
	public Double getExafsTime() {
		return exafsTime;
	}

	/**
	 * @param postEdgeTime
	 */
	public void setExafsTime(Double postEdgeTime) {
		this.exafsTime = postEdgeTime;
	}

	/**
	 * @return start time per point
	 */
	public Double getExafsFromTime() {
		return exafsFromTime;
	}

	/**
	 * @param postEdgeFromTime
	 */
	public void setExafsFromTime(Double postEdgeFromTime) {
		this.exafsFromTime = postEdgeFromTime;
	}

	/**
	 * @return final time per point in post edge scan
	 */
	public Double getExafsToTime() {
		return exafsToTime;
	}

	/**
	 * @param postEdgeToTime
	 */
	public void setExafsToTime(Double postEdgeToTime) {
		this.exafsToTime = postEdgeToTime;
	}

	/**
	 * @return the postEdgeStep
	 */
	public Double getExafsStep() {
		return exafsStep;
	}

	/**
	 * @param postEdgeStep
	 *            the postEdgeStep to set in eV unless constantEnergy is false
	 */
	public void setExafsStep(Double postEdgeStep) {
		this.exafsStep = postEdgeStep;
	}

	public Double getKWeighting() {
		return kWeighting;
	}

	public void setKWeighting(Double kWeighting) {
		this.kWeighting = kWeighting;
	}
	

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((a == null) ? 0 : a.hashCode());
		result = prime * result + ((b == null) ? 0 : b.hashCode());
		result = prime * result + ((c == null) ? 0 : c.hashCode());
		result = prime * result + ((coreHole == null) ? 0 : coreHole.hashCode());
		result = prime * result + ((edge == null) ? 0 : edge.hashCode());
		result = prime * result + ((edgeEnergy == null) ? 0 : edgeEnergy.hashCode());
		result = prime * result + ((edgeStep == null) ? 0 : edgeStep.hashCode());
		result = prime * result + ((edgeTime == null) ? 0 : edgeTime.hashCode());
		result = prime * result + ((element == null) ? 0 : element.hashCode());
		result = prime * result + ((exafsFromTime == null) ? 0 : exafsFromTime.hashCode());
		result = prime * result + ((exafsStep == null) ? 0 : exafsStep.hashCode());
		result = prime * result + ((exafsStepType == null) ? 0 : exafsStepType.hashCode());
		result = prime * result + ((exafsTimeType == null) ? 0 : exafsTimeType.hashCode());
		result = prime * result + ((exafsTime == null) ? 0 : exafsTime.hashCode());
		result = prime * result + ((exafsToTime == null) ? 0 : exafsToTime.hashCode());
		result = prime * result + ((finalEnergy == null) ? 0 : finalEnergy.hashCode());
		result = prime * result + ((gaf1 == null) ? 0 : gaf1.hashCode());
		result = prime * result + ((gaf2 == null) ? 0 : gaf2.hashCode());
		result = prime * result + ((gaf3 == null) ? 0 : gaf3.hashCode());
		result = prime * result + ((initialEnergy == null) ? 0 : initialEnergy.hashCode());
		result = prime * result + ((kWeighting == null) ? 0 : kWeighting.hashCode());
		result = prime * result + ((preEdgeStep == null) ? 0 : preEdgeStep.hashCode());
		result = prime * result + ((preEdgeTime == null) ? 0 : preEdgeTime.hashCode());
		result = prime * result + ((scannableName == null) ? 0 : scannableName.hashCode());
		result = prime * result + (shouldValidate ? 1231 : 1237);
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
		XasScanParameters other = (XasScanParameters) obj;
		if (a == null) {
			if (other.a != null)
				return false;
		} else if (!a.equals(other.a))
			return false;
		if (b == null) {
			if (other.b != null)
				return false;
		} else if (!b.equals(other.b))
			return false;
		if (c == null) {
			if (other.c != null)
				return false;
		} else if (!c.equals(other.c))
			return false;
		if (coreHole == null) {
			if (other.coreHole != null)
				return false;
		} else if (!coreHole.equals(other.coreHole))
			return false;
		if (edge == null) {
			if (other.edge != null)
				return false;
		} else if (!edge.equals(other.edge))
			return false;
		if (edgeEnergy == null) {
			if (other.edgeEnergy != null)
				return false;
		} else if (!edgeEnergy.equals(other.edgeEnergy))
			return false;
		if (edgeStep == null) {
			if (other.edgeStep != null)
				return false;
		} else if (!edgeStep.equals(other.edgeStep))
			return false;
		if (edgeTime == null) {
			if (other.edgeTime != null)
				return false;
		} else if (!edgeTime.equals(other.edgeTime))
			return false;
		if (element == null) {
			if (other.element != null)
				return false;
		} else if (!element.equals(other.element))
			return false;
		if (exafsFromTime == null) {
			if (other.exafsFromTime != null)
				return false;
		} else if (!exafsFromTime.equals(other.exafsFromTime))
			return false;
		if (exafsStep == null) {
			if (other.exafsStep != null)
				return false;
		} else if (!exafsStep.equals(other.exafsStep))
			return false;
		if (exafsStepType == null) {
			if (other.exafsStepType != null)
				return false;
		} else if (!exafsStepType.equals(other.exafsStepType))
			return false;
		
		if (exafsTimeType == null) {
			if (other.exafsTimeType != null)
				return false;
		} else if (!exafsTimeType.equals(other.exafsTimeType))
			return false;
		
		if (exafsTime == null) {
			if (other.exafsTime != null)
				return false;
		} else if (!exafsTime.equals(other.exafsTime))
			return false;
		if (exafsToTime == null) {
			if (other.exafsToTime != null)
				return false;
		} else if (!exafsToTime.equals(other.exafsToTime))
			return false;
		if (finalEnergy == null) {
			if (other.finalEnergy != null)
				return false;
		} else if (!finalEnergy.equals(other.finalEnergy))
			return false;
		if (gaf1 == null) {
			if (other.gaf1 != null)
				return false;
		} else if (!gaf1.equals(other.gaf1))
			return false;
		if (gaf2 == null) {
			if (other.gaf2 != null)
				return false;
		} else if (!gaf2.equals(other.gaf2))
			return false;
		if (gaf3 == null) {
			if (other.gaf3 != null)
				return false;
		} else if (!gaf3.equals(other.gaf3))
			return false;
		if (initialEnergy == null) {
			if (other.initialEnergy != null)
				return false;
		} else if (!initialEnergy.equals(other.initialEnergy))
			return false;
		if (kWeighting == null) {
			if (other.kWeighting != null)
				return false;
		} else if (!kWeighting.equals(other.kWeighting))
			return false;
		if (preEdgeStep == null) {
			if (other.preEdgeStep != null)
				return false;
		} else if (!preEdgeStep.equals(other.preEdgeStep))
			return false;
		if (preEdgeTime == null) {
			if (other.preEdgeTime != null)
				return false;
		} else if (!preEdgeTime.equals(other.preEdgeTime))
			return false;
		if (scannableName == null) {
			if (other.scannableName != null)
				return false;
		} else if (!scannableName.equals(other.scannableName))
			return false;
		if (shouldValidate != other.shouldValidate)
			return false;
		return true;
	}

	/**
	 * @return the postEdgeStepType
	 */
	public String getExafsStepType() {
		return exafsStepType;
	}

	/**
	 * @param postEdgeStepType
	 *            scan at constant energy or constant K
	 */
	public void setExafsStepType(String postEdgeStepType) {
		this.exafsStepType = postEdgeStepType;
	}

	/**
	 *
	 */
	@Override
	public String toString() {
		try {
			return BeanUtils.describe(this).toString();
		} catch (Exception e) {
			return e.getMessage();
		}
	}

	/**
	 * @return Returns the scannableName.
	 */
	@Override
	public String getScannableName() {
		return scannableName;
	}

	/**
	 * @param scannableName
	 *            The scannableName to set.
	 */
	public void setScannableName(String scannableName) {
		this.scannableName = scannableName;
	}

	/**
	 * @return Returns the shouldValidate.
	 */
	public boolean isShouldValidate() {
		return shouldValidate;
	}

	/**
	 * @param shouldValidate
	 *            The shouldValidate to set.
	 */
	public void setShouldValidate(boolean shouldValidate) {
		this.shouldValidate = shouldValidate;
	}

	public Double getC() {
		return c;
	}

	public void setGaf3(Double gaf3) {
		this.gaf3 = gaf3;
	}

	public void setC(Double c) {
		if (c != null){
			this.c = c;
		} else {
			System.out.println("got here!");
		}
	}

	public Double getGaf3() {
		return gaf3;
	}

	public String getExafsTimeType() {
		return exafsTimeType;
	}

	public void setExafsTimeType(String exafsTimeType) {
		this.exafsTimeType = exafsTimeType;
	}

	public String getAbGafChoice() {
		return abGafChoice;
	}

	public void setAbGafChoice(String abGafChoice) {
		this.abGafChoice = abGafChoice;
	}
}
