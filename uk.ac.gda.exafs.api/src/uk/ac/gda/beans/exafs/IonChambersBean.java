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

package uk.ac.gda.beans.exafs;

import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class IonChambersBean implements Serializable {
	static public final URL mappingURL = DetectorParameters.class.getResource("ExafsParameterMapping.xml");
	static public final URL schemaURL = DetectorParameters.class.getResource("ExafsParameterMapping.xsd");

	private double energy;
	private List<IonChamberParameters> ionChambers;

	public IonChambersBean(){
		energy=0;
		ionChambers = new ArrayList<IonChamberParameters>();
	}

	public void clear(){
		energy=0;
		ionChambers.clear();
	}

	public double getEnergy() {
		return energy;
	}

	public void setEnergy(double energy) {
		this.energy = energy;
	}

	public List<IonChamberParameters> getIonChambers() {
		return ionChambers;
	}

	public void setIonChambers(List<IonChamberParameters> ionChambers) {
		this.ionChambers = ionChambers;
	}

	public void addIonChamber(IonChamberParameters ionChamber) {
		ionChambers.add(ionChamber);
	}

}