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

package uk.ac.gda.beans.exafs;

import java.io.Serializable;
import java.net.URL;

public class XanesRegionParameters implements Serializable{
	
	private int regionNumber;
	private double startEnergy;
	private double stepEnergy;
	private double time;
	
	static public final URL mappingURL = XasScanParameters.class.getResource("ExafsParameterMapping.xml");
	static public final URL schemaUrl = XasScanParameters.class.getResource("ExafsParameterMapping.xsd");
	
	public XanesRegionParameters(){
		
	}
	
	public XanesRegionParameters(int region, double startEnergy, double stepEnergy, double time){
		super();
		this.regionNumber = region;
		this.startEnergy = startEnergy;
		this.stepEnergy = stepEnergy;
		this.time = time;
	}

	public int getRegion() {
		return regionNumber;
	}

	public void setRegion(int string) {
		this.regionNumber = string;
	}

	public double getStartEnergy() {
		return startEnergy;
	}

	public void setStartEnergy(double startEnergy) {
		this.startEnergy = startEnergy;
	}

	public double getStepEnergy() {
		return stepEnergy;
	}

	public void setStepEnergy(double stopEnergy) {
		this.stepEnergy = stopEnergy;
	}

	public double getTime() {
		return time;
	}

	public void setTime(double time) {
		this.time = time;
	}
	
	

}
