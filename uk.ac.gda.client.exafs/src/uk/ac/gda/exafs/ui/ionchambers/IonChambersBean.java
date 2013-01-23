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

package uk.ac.gda.exafs.ui.ionchambers;

import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class IonChambersBean implements Serializable {
	
	static public final URL mappingURL = IonChambersBean.class.getResource("ionChamberMapping.xml");
	static public final URL schemaURL = IonChambersBean.class.getResource("ionChamberMapping.xsd");
	
	private String log;
	private double energy;
	
	private List<IonChamberBean> ionChambers;
	
	public IonChambersBean(){
		log="";
		energy=0;
		ionChambers = new ArrayList<IonChamberBean>(50);
	}
	
	public void clear(){
		log="";
		energy=0;
		ionChambers.clear();
	}

	public String getLog() {
		return log;
	}

	public void setLog(String log) {
		this.log = log;
	}

	public double getEnergy() {
		return energy;
	}

	public void setEnergy(double energy) {
		this.energy = energy;
	}

	public List<IonChamberBean> getIonChambers() {
		return ionChambers;
	}

	public void setIonChambers(List<IonChamberBean> ionChambers) {
		this.ionChambers = ionChambers;
	}
	
	public void addIonChamber(IonChamberBean ionChamber) {
		ionChambers.add(ionChamber);
	}
	
}
