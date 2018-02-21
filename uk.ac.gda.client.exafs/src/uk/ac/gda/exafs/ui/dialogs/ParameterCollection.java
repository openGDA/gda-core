/*-
 * Copyright © 2017 Diamond Light Source Ltd.
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

package uk.ac.gda.exafs.ui.dialogs;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.XStream;


public class ParameterCollection {

	private List<ParametersForScan> parametersForScans;

	public ParameterCollection() {
		parametersForScans = new ArrayList<ParametersForScan>();
	}

	public ParameterCollection(List<ParametersForScan> overridesForScans) {
		this.parametersForScans = overridesForScans;
	}

	public List<ParametersForScan> getOverridesForScans() {
		return parametersForScans;
	}

	static public String toXML(List<ParametersForScan> overrideForScans) {
		ParameterCollection scans = new ParameterCollection(overrideForScans);
		return getXStream().toXML(scans);
	}

	public static final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";

	static public void saveToFile(List<ParametersForScan> overrideForScans, String filePath) throws IOException {
		String xmlString = XML_HEADER+toXML(overrideForScans);
		try(BufferedWriter bufWriter = new BufferedWriter(new FileWriter(filePath))) {
			bufWriter.write( xmlString );
		}
	}

	static public List<ParametersForScan> loadFromFile(String filePath) throws IOException {
		InputStream in = new FileInputStream(filePath);
		XStream xstream = getXStream();
		ParameterCollection newParams = (ParameterCollection)xstream.fromXML(in);
		return newParams.getOverridesForScans();
	}

	static public XStream getXStream() {
		XStream xstream = new XStream();
		// set the class loader - this fixes 'class not found' exception when de-serialising inside gda client.
		xstream.setClassLoader(ParameterCollection.class.getClassLoader());

		ParameterValuesForBean.addAliases(xstream);

		xstream.alias("ParametersForScan", ParametersForScan.class);
		xstream.alias("OverrideCollection", ParameterCollection.class);

		xstream.addImplicitCollection(ParametersForScan.class, "valuesForBeans");
		xstream.addImplicitCollection(ParameterCollection.class, "parametersForScans");
		return xstream;
	}

	public String toXML() {
		return getXStream().toXML(this);
	}
}
