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
package uk.ac.gda.beans.vortex;

import uk.ac.gda.util.beans.xml.XMLHelpers;

/**
 * @author dfq16044
 *
 */
public class Xspress3Parameters extends VortexParameters {

	private static final long serialVersionUID = -962382978263704423L;

	static public final java.net.URL mappingURL = Xspress3Parameters.class.getResource("Xspress3Mapping.xml");

	static public final java.net.URL schemaURL = Xspress3Parameters.class.getResource("Xspress3Mapping.xsd");

	public static Xspress3Parameters createFromXML(String filename) throws Exception {
		return XMLHelpers.createFromXML(mappingURL, Xspress3Parameters.class, schemaURL, filename);
	}

	public static void writeToXML(Xspress3Parameters xspressParameters, String filename) throws Exception {
		XMLHelpers.writeToXML(mappingURL, xspressParameters, filename);
	}

	public Xspress3Parameters() {
		super();
	}

	@Override
	public Xspress3Parameters objectCast (Object obj){
		return (Xspress3Parameters) obj;
	}
}
