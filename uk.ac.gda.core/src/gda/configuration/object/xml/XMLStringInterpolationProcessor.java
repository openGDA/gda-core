/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council Daresbury Laboratory
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

package gda.configuration.object.xml;

import gda.configuration.properties.LocalProperties;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.CharArrayReader;
import java.io.CharArrayWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;

/**
 * This parses an XML file (ie Castor instance) and applies string interpolation (a.k.a. variable substitution. Every
 * XML field in the XML document is examined during processing. Fields which contain a pattern beginning with "${",
 * followed by a property key name and terminating with a "}", will trigger string interpolation.
 * <p>
 * The LocalProperties class is queried to find an existing property which matches the key name. If a value is returned,
 * the "${key-name}" matched pattern is replaced with the value found.
 * <p>
 * e.g. if "gda.root=/home/gda" is an existing system property, then a field containing:-
 * <p>
 * ${gda.root}/params/properties/my_property_file.xml
 * <p>
 * ...would be replaced with:-
 * <p>
 * /home/gda/params/properties/my_property_file.xml
 */
public class XMLStringInterpolationProcessor {
	private static final Logger logger = LoggerFactory.getLogger(XMLStringInterpolationProcessor.class);

	private Reader in = null;

	private Writer out = null;

	/**
	 * Reads byte from in and write to out until start pattern found.
	 * 
	 * @return true if next header found
	 * @throws IOException
	 */
	private boolean processToNextHeader() throws IOException {
		// two characters read in - so can read ahead to detect "${" pair
		int i1 = in.read();
		int i2 = in.read();

		char c1 = (char) i1;
		char c2 = (char) i2;

		while (!(c1 == '$' && c2 == '{') && (i1 != -1 && i2 != -1)) {
			out.write(c1);

			i1 = i2;
			c1 = c2;

			i2 = in.read();
			c2 = (char) i2;

			if (i2 == -1) {
				// flush previous character read to output
				if (i1 != -1) {
					out.write(c1);
				}

				return false;
			}
		}

		return true;
	}

	/**
	 * Reads in text into a string until a terminator character found, thus forming a property key to be substituted
	 * with its value.
	 * 
	 * @return property key name if found. null if not found.
	 * @throws IOException
	 */
	private String readPropertyKeyUntilTerminator() throws IOException {
		String key = "";

		int i = in.read();
		char c = (char) i;

		if (i == -1) {
			return null;
		}

		while (c != '}') {
			key += c;

			i = in.read();
			c = (char) i;

			if (i == -1) {
				return null;
			}
		}

		return key;
	}

	/**
	 * Scans through XML instance file looking for Property keys inside ${} patterns. Each instance should contain a
	 * reference to a named java property (e.g. "${gda.src.java}"). If any found, look for a matching java property of
	 * same name and replace the pattern instance with the value of that property using string interpolation. N.B.
	 * Should handle multiple pattern instances per property value, but doesnt handle recursive interpolation yet (i.e.
	 * a pattern containing a property with a value containing further patterns).
	 * 
	 * @throws IOException
	 */
	private void doProcessing() throws IOException {
		boolean done = false;

		while (done == false) {
			// scan for pattern start in buffer
			boolean rval = processToNextHeader();

			if (rval == false) {
				return;
			}

			// find end of current pattern
			String value = readPropertyKeyUntilTerminator();

			if (value == null) {
				return;
			}

			// extract the referenced property from the pattern
			String propertyName = value;

			// try to fetch the property
			String propertyValue = LocalProperties.get(propertyName);

			if (propertyValue != null && propertyValue != "null") {
				// replace matched pattern with referenced property value
				value = propertyValue;

				// README The original property value could be replaced with the
				// new
				// interpolated value using the following line:-
				// properties.setProperty(key, value);

				// Found match so increment past interpolated value.
				// start next search at end of pattern instance

			} else {
				// Failed to find interpolated value
				// so increment past original pattern.
				// start next search at end of interpolated value

				// just re-emit uninterpolated property key if not found
				value = "${" + propertyName + "}";
			}

			// dump buffer up to point of first pattern
			// out.write(value.getBytes(), 0, value.length());
			out.write(value.toCharArray());
		}
	}

	/**
	 * Property String interpolation in an XML input stream, eg Castor instance file.
	 * 
	 * @param xmlSourceName
	 *            name of XML data source to be processed
	 * @param input
	 *            XML data source to be processed - assumed to be unbuffered
	 * @return InputSource containing processed output
	 */
	private InputSource doStringInterpolationXMLFileInternal(String xmlSourceName, Reader input) {
		in = new BufferedReader(input);
		out = new CharArrayWriter();

		try {
			// preprocess in to out, doing string interpolation
			doProcessing();

			// cast back to CharArrayWriter, so we can call toCharArray
			CharArrayWriter o = (CharArrayWriter) out;

			// turn preprocessed XML instance file into InputSource (for
			// Castor
			// unmarshalling)
			InputSource source = new InputSource(new CharArrayReader(o.toCharArray()));

			// Enable this property to dump processed XML to output file
			// for debugging/diagnostics
			boolean dumpProcessedXML = LocalProperties.check("gda.configuration.object.xml.dumpProcessedXML", false);

			if (dumpProcessedXML == true) {
				Writer outFile = new BufferedWriter(new FileWriter(xmlSourceName + ".processed.xml"));
				outFile.write(out.toString());

				outFile.flush();
				outFile.close();
			}

			return source;
		} catch (IOException ie) {
			logger.error(ie.getMessage());
			logger.debug(ie.getStackTrace().toString());
		}

		return null;
	}

	/**
	 * Property String interpolation in an XML file, eg Castor instance file.
	 * 
	 * @param xmlFile
	 *            XML file to be processed
	 * @return InputSource containing processed output
	 * @throws FileNotFoundException
	 */
	public InputSource doStringInterpolationXMLFile(String xmlFile) throws FileNotFoundException {
		Reader r = new FileReader(xmlFile);
		// Reader r = new InputStreamReader(new FileInputStream(xmlFile),
		// "UTF-8");
		return doStringInterpolationXMLFileInternal(xmlFile, r);
	}

	/**
	 * Property String interpolation in an XML data source, eg Castor instance file.
	 * 
	 * @param input
	 *            XML InputSource to be processed
	 * @return InputSource containing processed output
	 */
	public InputSource doStringInterpolationXMLFile(InputSource input) {
		return doStringInterpolationXMLFileInternal(input.getPublicId(), input.getCharacterStream());
	}

	/**
	 * Pass the following args on the command line to use with the test file listed in the code for main
	 * (test_instancefile.xml). (N.B. replace "\\" with single "\" )
	 * <p>
	 * -Dgda.root=D:\\GDA\\dev
	 * <p>
	 * -Dgda.data=d:\\gda\\users\\data
	 * <p>
	 * -Dgda.jython.userScriptDir=d:\\gda\\users\\scripts
	 * <p>
	 * -Dgda.station=stn7_6
	 * <p>
	 * -Dgda.src.java=D:\\GDA\\dev\\src\\java
	 * <p>
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		XMLStringInterpolationProcessor thisInstance = new XMLStringInterpolationProcessor();

		thisInstance
				.doStringInterpolationXMLFile("D:\\GDA\\gda-trunk\\src\\tests\\gda\\configuration\\object\\xml\\test_instancefile.xml");
		// thisInstance.doStringInterpolationXMLFile("C:\\Documents and
		// Settings\\msd43.DL\\Desktop\\stnBase_Server.xml");
	}

}
