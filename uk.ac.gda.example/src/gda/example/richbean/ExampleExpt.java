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

package gda.example.richbean;

import java.io.Serializable;
import java.net.URL;

import uk.ac.gda.beans.IRichBean;
import uk.ac.gda.util.beans.xml.XMLHelpers;

public class ExampleExpt implements IRichBean, Serializable {
	
	private double startEnergy;
	private double finalEnergy;

	public void setFinalEnergy(double finalEnergy) {
		this.finalEnergy = finalEnergy;
	}

	public double getFinalEnergy() {
		return finalEnergy;
	}

	public void setStartEnergy(double startEnergy) {
		this.startEnergy = startEnergy;
	}

	public double getStartEnergy() {
		return startEnergy;
	}
	
	// the following associates this bean to the Java to xml mapping files:
	
	static public final URL mappingURL = ExampleExpt.class.getResource("ExampleExptMapping.xml");
	static public final URL schemaURL  = ExampleExpt.class.getResource("ExampleExptMapping.xsd");


	public static ExampleExpt createFromXML(String filename) throws Exception {
		return (ExampleExpt) XMLHelpers.createFromXML(mappingURL, ExampleExpt.class, schemaURL, filename);
	}

	public static void writeToXML(ExampleExpt scanParameters, String filename) throws Exception {
		XMLHelpers.writeToXML(mappingURL, scanParameters, filename);
	}

	@Override
	public void clear() {		
	}
}
