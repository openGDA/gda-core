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

package gda.device.detector.xmap;

import java.io.File;

import uk.ac.gda.beans.vortex.VortexParameters;
import uk.ac.gda.util.beans.xml.XMLHelpers;

public class VortexBeanUtils {

	public static VortexParameters createBeanFromXML(String xmlPath) throws Exception{
		return (VortexParameters) XMLHelpers.createFromXML(VortexParameters.mappingURL, VortexParameters.class, VortexParameters.schemaURL, new File(xmlPath));
	}

	public static void createXMLfromBean(Xmap xmap, VortexParameters vortexBean) throws Exception{
		File file = new File(xmap.getConfigFileName());
		XMLHelpers.writeToXML(VortexParameters.mappingURL, vortexBean, file);
	}

}