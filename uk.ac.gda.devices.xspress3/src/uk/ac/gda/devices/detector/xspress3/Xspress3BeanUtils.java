/*-
 * Copyright © 2014 Diamond Light Source Ltd.
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

package uk.ac.gda.devices.detector.xspress3;

import java.io.File;

import uk.ac.gda.beans.vortex.VortexParameters;
import uk.ac.gda.beans.vortex.Xspress3Parameters;
import uk.ac.gda.util.beans.xml.XMLHelpers;

public class Xspress3BeanUtils {
	
	public Xspress3Parameters createBeanFromXML(String xmlPath) throws Exception{
		return (Xspress3Parameters) XMLHelpers.createFromXML(Xspress3Parameters.mappingURL, Xspress3Parameters.class, VortexParameters.schemaURL, new File(xmlPath));
	}
	
	public void createXMLfromBean(Xspress3Detector xmap, Xspress3Parameters vortexBean) throws Exception{
		File file = new File(xmap.getConfigFileName());
		XMLHelpers.writeToXML(Xspress3Parameters.mappingURL, vortexBean, file);
	}
	
}