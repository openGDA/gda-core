/*-
 * Copyright © 2012 Diamond Light Source Ltd.
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

package uk.ac.gda.arpes.beans;

import java.io.File;
import java.io.Serializable;
import java.net.URL;

import uk.ac.gda.util.beans.xml.XMLHelpers;
import uk.ac.gda.util.beans.xml.XMLRichBean;

public class ARPESScanBean implements XMLRichBean, Serializable {

	static public final URL mappingURL = ARPESScanBean.class.getResource("ARPESMapping.xml");
	static public final URL schemaURL  = ARPESScanBean.class.getResource("ARPESMapping.xsd");

	String lensMode = "Angular30";
	int passEnergy = 5;
	double startEnergy = 34.790;
	double endEnergy = 35.210;
	double stepEnergy = 0.40298;
	double timePerStep = 1;
	int iterations = 1;
	boolean sweptMode = false;
	boolean configureOnly = false;

	public static ARPESScanBean createFromXML(String filename) throws Exception {
		return (ARPESScanBean) XMLHelpers.createFromXML(mappingURL, ARPESScanBean.class, schemaURL, new File(filename));
	}

	public static void writeToXML(ARPESScanBean bean, String filename) throws Exception {
		XMLHelpers.writeToXML(mappingURL, bean, filename);
	}

	public String getLensMode() {
		return lensMode;
	}

	public void setLensMode(String lensMode) {
		this.lensMode = lensMode;
	}

	public int getPassEnergy() {
		return passEnergy;
	}

	public void setPassEnergy(int passEnergy) {
		this.passEnergy = passEnergy;
	}

	public double getStartEnergy() {
		return startEnergy;
	}

	public void setStartEnergy(double startEnergy) {
		this.startEnergy = startEnergy;
	}

	public double getEndEnergy() {
		return endEnergy;
	}

	public void setEndEnergy(double endEnergy) {
		this.endEnergy = endEnergy;
	}

	public double getStepEnergy() {
		return stepEnergy;
	}

	public void setStepEnergy(double stepEnergy) {
		this.stepEnergy = stepEnergy;
	}

	public double getTimePerStep() {
		return timePerStep;
	}

	public void setTimePerStep(double timePerStep) {
		this.timePerStep = timePerStep;
	}

	public int getIterations() {
		return iterations;
	}

	public void setIterations(int iterations) {
		this.iterations = iterations;
	}

	public boolean isSweptMode() {
		return sweptMode;
	}

	public void setSweptMode(boolean sweptMode) {
		this.sweptMode = sweptMode;
	}

	public boolean isConfigureOnly() {
		return configureOnly;
	}

	public void setConfigureOnly(boolean configureOnly) {
		this.configureOnly = configureOnly;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (configureOnly ? 1231 : 1237);
		long temp;
		temp = Double.doubleToLongBits(endEnergy);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + iterations;
		result = prime * result + ((lensMode == null) ? 0 : lensMode.hashCode());
		result = prime * result + passEnergy;
		temp = Double.doubleToLongBits(startEnergy);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(stepEnergy);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + (sweptMode ? 1231 : 1237);
		temp = Double.doubleToLongBits(timePerStep);
		result = prime * result + (int) (temp ^ (temp >>> 32));
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
		ARPESScanBean other = (ARPESScanBean) obj;
		if (configureOnly != other.configureOnly)
			return false;
		if (Double.doubleToLongBits(endEnergy) != Double.doubleToLongBits(other.endEnergy))
			return false;
		if (iterations != other.iterations)
			return false;
		if (lensMode == null) {
			if (other.lensMode != null)
				return false;
		} else if (!lensMode.equals(other.lensMode))
			return false;
		if (passEnergy != other.passEnergy)
			return false;
		if (Double.doubleToLongBits(startEnergy) != Double.doubleToLongBits(other.startEnergy))
			return false;
		if (Double.doubleToLongBits(stepEnergy) != Double.doubleToLongBits(other.stepEnergy))
			return false;
		if (sweptMode != other.sweptMode)
			return false;
		if (Double.doubleToLongBits(timePerStep) != Double.doubleToLongBits(other.timePerStep))
			return false;
		return true;
	}


}