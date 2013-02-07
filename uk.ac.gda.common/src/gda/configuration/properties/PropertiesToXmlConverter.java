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

package gda.configuration.properties;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.Properties;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * Converter to convert a Java Properties API compliant property file into an XML representation compatible with Jakarta
 * commons configuration XML properties file format.
 */
public class PropertiesToXmlConverter {
	private static final Logger logger = LoggerFactory.getLogger(PropertiesToXmlConverter.class);

	private Properties props = null;

	private Document doc = null;

	private Element root = null;

	// string to prepend to (a non-XML-compliant) property segment which has
	// a non-alpha character in the first character position
	private static final String prependString = "A";

	/**
	 * From an XML DOM element, try to locate a named child node.
	 * 
	 * @param current
	 *            current element node
	 * @param childName
	 *            name of child node to locate
	 * @return located child node. null if not found.
	 */
	private Element getChild(Element current, String childName) {
		NodeList nodeList = current.getChildNodes();

		for (int i = 0; i < nodeList.getLength(); i++) {
			Node n = nodeList.item(i);

			if (n.getNodeName().equals(childName)) {
				return (Element) n;
			}
		}

		return null;
	}

	/**
	 * Adds a new element as a child of a current element. Assumes existing children are sorted alphabetically. Child is
	 * added at appropriate place in list of children. Repeated use of this routine results in elements with
	 * alphabetically sorted child elements.
	 * 
	 * @param current
	 *            current parent element to insert new element into
	 * @param newChild
	 *            new element to insert as child of current element
	 */
	private void addChildSortedAlphabetically(Element current, Element newChild) {
		NodeList nodeList = current.getChildNodes();

		for (int i = 0; i < nodeList.getLength(); i++) {
			Node n = nodeList.item(i);

			// insert before end of child list if it precedes exisiting
			// nodes
			if (n.getNodeName().compareToIgnoreCase(newChild.getNodeName()) > 0) {
				current.insertBefore(newChild, n);
				return;
			}
		}

		// add to end of list if falls after all others alphabetically
		current.appendChild(newChild);
	}

	/**
	 * Create a XML DOM representation for a property key-value pair (from a Properties object). N.B. Should create a
	 * representation compatible with Jakarta commons configuration XML properties file format.
	 * 
	 * @param root
	 *            the root element of the DOM instance
	 * @param propertyName
	 *            name of a property
	 * @param propertyValue
	 *            value of the property
	 */
	private void addPropertyToXmlDomRepresentation(Element root, String propertyName, String propertyValue) {
		int split;
		String name = propertyName;
		Element current = root;
		Element test = null;

		// split string into dot-separated chunks
		while ((split = name.indexOf(".")) != -1) {
			String subName = name.substring(0, split);
			name = name.substring(split + 1);

			// if no existing element with current name, create an element
			// with
			// this name and attach to parent.
			if ((test = getChild(current, subName)) == null) {
				Element subElement = doc.createElement(subName);

				addChildSortedAlphabetically(current, subElement);
				current = subElement;
			} else {
				// name already exists, so drop down a level
				current = test;
			}
		}

		// When out of loop, what's left is the final element's name.

		// README - if 1st char of property name not alpha, get invalid XML - so
		// workaround to insert name - needs resolving?
		// if first character of name is not alpha, then insert an alpha char
		char c = name.charAt(0);
		if (Character.isLetter(c) == false) {
			logger.debug("Found non-alpha first-character in last segment of property: " + propertyName + " (" + name
					+ ")");
			logger.debug("Converting to: " + prependString + name);
			logger.debug("");
			name = prependString + name;
		}

		// Create the leaf element
		Element last = null;
		Text valueTextNode = null;

		try {
			last = doc.createElement(name);

			// set its text set to the property value.
			valueTextNode = doc.createTextNode(propertyValue);
			last.appendChild(valueTextNode);

			// Attach it to the parent
			addChildSortedAlphabetically(current, last);
		} catch (DOMException e) {
			logger.error(e.getMessage());
		}
	}

	/**
	 * Write an XML DOM instance to an XML file. Using Generic Java Transform API, so it is not tied to using 3rd-party
	 * code. And keeps it using generic APIs, so Impl can still be swapped?
	 * 
	 * @param outputFileName
	 *            The name of the XML properties file to write out.
	 */
	private void writeDomToXmlFileTransform(String outputFileName) {
		String encoding = "UTF-8";
		int indentAmount = 3;
		Transformer t = null;

		try {
			// Implemention using javax.xml.transform.Transformer
			// README - could this use an XSL(T) stylesheet for Jakarta
			// format?
			TransformerFactory factory = TransformerFactory.newInstance();

			// README - workaround for lack of working "indent-amount" in
			// transformer properties in Java 1.5 (XSLT/Xalan)
			// implementation
			factory.setAttribute("indent-number", new Integer(indentAmount));

			// Create a transformer which can "copy" a DOMSource to a
			// StreamResult
			// ie perform an XML serialization operation.
			t = factory.newTransformer();
		} catch (TransformerConfigurationException e) {
			logger.error(e.getMessage());
		} catch (TransformerFactoryConfigurationError e) {
			logger.error(e.getMessage());
		}

		if (t == null) {
			logger.error("writeDomToXmlFileXalan failed - could not create Transformer");
			return;
		}

		t.setOutputProperty(OutputKeys.METHOD, "xml");
		t.setOutputProperty(OutputKeys.VERSION, "1.0");
		t.setOutputProperty(OutputKeys.ENCODING, encoding);
		// README - need to enable indents to get newlines AND indents!
		t.setOutputProperty(OutputKeys.INDENT, "yes");
		t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
		// t.setOutputProperty(OutputKeys.STANDALONE, "no");
		t.setOutputProperty(OutputKeys.MEDIA_TYPE, "text/xml");

		// README - see workaround in TransformerFactory.setAttribute above.
		// For both instances of setting "indent-amount" below, its getting
		// ignored!! Without the workaround, output does have newlines,
		// but there are NO INDENTS!!!
		// t.setOutputProperty("{http://xml.apache.org/xslt}indent-amount",
		// String.valueOf(indentAmount));
		// t.setOutputProperty("{http://xml.apache.org/xalan}indent-amount",
		// String.valueOf(indentAmount));

		Source s = new DOMSource(doc);
		try {
			// Result r = new StreamResult(System.out);

			// README - Must wrap FileOutputStream with a writer or
			// bufferedwriter,
			// to workaround a "buggy" behavior of the xml handling code.
			// A FileOutputStream on its own causes "indent-number" to be
			// ignored!
			// This method works properly - "indent-number" is not ignored!
			// N.B. However, note that the encoding is forced here.
			OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(outputFileName), encoding);
			Result r = new StreamResult(out);

			t.transform(s, r);
		} catch (UnsupportedEncodingException e) {
			logger.error(e.getMessage());
		} catch (FileNotFoundException e) {
			logger.error(e.getMessage());
		} catch (TransformerException e) {
			logger.error(e.getMessage());
		}
	}

	/**
	 * Write an XML DOM instance to an XML file.
	 * 
	 * @param outputFileName
	 *            The name of the XML properties file to write out.
	 */
	private void writeDomToXmlFile(String outputFileName) {
		// This method uses generic Java Transform APIs - so does not depend on
		// specific Implementation.
		writeDomToXmlFileTransform(outputFileName);
	}

	/**
	 * Creates a XML DOM instance containing all loaded properties.
	 */
	private void convertPropertiesToXml() {
		doc = XmlDomFactory.createDocument(null, "root", null);
		root = doc.getDocumentElement();

		// add every property into the DOM Document instance
		Enumeration<?> propertyNames = props.propertyNames();
		while (propertyNames.hasMoreElements()) {
			String name = (String) propertyNames.nextElement();
			String value = props.getProperty(name);

			addPropertyToXmlDomRepresentation(root, name, value);
		}
	}

	/**
	 * Load a Java Properties API compliant properties file into a Properties object in memory.
	 * 
	 * @param inputFileName
	 *            The name of the properties file to load in.
	 */
	private void loadPropertiesFile(String inputFileName) {
		try {
			FileInputStream f = new FileInputStream(inputFileName);

			props = new Properties();
			props.load(f);
		} catch (FileNotFoundException e) {
			logger.error(e.getMessage());
			logger.error("could not load input file: " + inputFileName);
		} catch (IOException e) {
			logger.error(e.getMessage());
			logger.error("could not create properties object from input file: " + inputFileName);
		} catch (Exception e) {
			logger.error(e.getMessage());
			logger.error("could not create properties object from input file: " + inputFileName);
		}
	}

	/**
	 * Converter to convert a Java Properties API compliant property file into an XML representation compatible with
	 * Jakarta commons configuration XML properties file format.
	 * <p>
	 * <p>
	 * Usage:
	 * <p>
	 * java gda.configuration.properties.PropertiesToXmlConverter [properties file] [XML file for output]
	 * <p>
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length != 2) {
			logger.info("Usage: java gda.configuration.properties.PropertiesToXmlConverter "
					+ "[properties file] [XML file for output]");
			System.exit(0);
		}

		String inputFileName = args[0];
		String outputFileName = args[1];

		try {
			PropertiesToXmlConverter converter = new PropertiesToXmlConverter();

			converter.loadPropertiesFile(inputFileName);

			if (converter.props != null) {
				converter.convertPropertiesToXml();

				converter.writeDomToXmlFile(outputFileName);
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
			logger.debug(e.getStackTrace().toString());
		}
	}
}
