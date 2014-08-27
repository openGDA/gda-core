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

import uk.ac.gda.beans.exafs.b18.B18SampleParameters;
import uk.ac.gda.util.beans.xml.XMLHelpers;

public class QEXAFSParameters implements Serializable, IScanParameters {
	Double initialEnergy;
	Double finalEnergy;
	Double speed;
	Double stepSize;
	Double time;
	String element;
	String edge;
	Double coreHole;
	boolean bothWays;
	
	private boolean shouldValidate = true;

	/**
	 * 
	 */
	static public final URL mappingURL = QEXAFSParameters.class.getResource("ExafsParameterMapping.xml");
	/**
	 * 
	 */
	static public final URL schemaURL = QEXAFSParameters.class.getResource("ExafsParameterMapping.xsd");

	/**
	 * @param filename
	 * @return QEXAFSParameters
	 * @throws Exception
	 */
	public static QEXAFSParameters createFromXML(String filename) throws Exception {
		return (QEXAFSParameters) XMLHelpers.createFromXML(mappingURL, QEXAFSParameters.class, schemaURL, filename);
	}

	/**
	 * @param qexafsParams
	 * @param filename
	 * @throws Exception
	 */
	public static void writeToXML(B18SampleParameters qexafsParams, String filename) throws Exception {
		XMLHelpers.writeToXML(mappingURL, qexafsParams, filename);
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
	 * Must implement clear() method on beans being used with BeanUI.
	 */
	@Override
	public void clear() {
		initialEnergy = finalEnergy = speed = stepSize = time = null;
		element = edge = "";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		
		result = prime * result + ((initialEnergy == null) ? 0 : initialEnergy.hashCode());
		result = prime * result + ((finalEnergy == null) ? 0 : finalEnergy.hashCode());
		result = prime * result + ((speed == null) ? 0 : speed.hashCode());
		result = prime * result + ((stepSize == null) ? 0 : stepSize.hashCode());
		result = prime * result + ((time == null) ? 0 : time.hashCode());
		result = prime * result + (shouldValidate ? 1231 : 1237);
		result = prime * result + (bothWays ? 1231 : 1237);
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
		QEXAFSParameters other = (QEXAFSParameters) obj;
		
		if (initialEnergy == null) {
			if (other.initialEnergy != null)
				return false;
		} else if (!initialEnergy.equals(other.initialEnergy))
			return false;
		
		if (finalEnergy == null) {
			if (other.finalEnergy != null)
				return false;
		} else if (!finalEnergy.equals(other.finalEnergy))
			return false;
		
		if (speed == null) {
			if (other.speed != null)
				return false;
		} else if (!speed.equals(other.speed))
			return false;
		
		if (stepSize == null) {
			if (other.stepSize != null)
				return false;
		} else if (!stepSize.equals(other.stepSize))
			return false;
		
		if (time == null) {
			if (other.stepSize != null)
				return false;
		} else if (!time.equals(other.time))
			return false;
		
		if (shouldValidate != other.shouldValidate)
			return false;
		
		if (bothWays != other.bothWays)
			return false;
		
		return true;
	}

	public double getInitialEnergy() {
		return initialEnergy;
	}

	public void setInitialEnergy(double initialEnergy) {
		this.initialEnergy = initialEnergy;
	}

	public double getFinalEnergy() {
		return finalEnergy;
	}

	public void setFinalEnergy(double finalEnergy) {
		this.finalEnergy = finalEnergy;
	}

	public double getSpeed() {
		return speed;
	}

	public void setSpeed(double speed) {
		this.speed = speed;
	}
	
	public double getStepSize() {
		return stepSize;
	}

	public void setStepSize(double stepSize) {
		this.stepSize = stepSize;
	}

	public boolean isShouldValidate() {
		return shouldValidate;
	}

	public void setShouldValidate(boolean shouldValidate) {
		this.shouldValidate = shouldValidate;
	}

	@Override
	public String getScannableName() {
		return null;
	}

	public double getTime() {
		return time;
	}

	public void setTime(double time) {
		this.time = time;
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
	
	public boolean isCoreGiven() {
		return coreHole != null;
	}

	public Double getCoreHole() {
		return coreHole;
	}

	public void setCoreHole(Double coreHole) {
		this.coreHole = coreHole;
	}

	public boolean getBothWays() {
		return bothWays;
	}

	public boolean isBothWays() {
		return bothWays;
	}
	
	public void setBothWays(boolean bothWays) {
		this.bothWays = bothWays;
	}
	
}
