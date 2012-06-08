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

package gda.util.converters;

import gda.util.QuantityFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.jscience.physics.quantities.Quantity;
import org.jscience.physics.units.Unit;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Class to hold the contents of the JEPConverter file.
 */
final class JEPQuantityConverterParameters {
	final String expressionTtoS, expressionStoT;

	final ArrayList<Unit<? extends Quantity>> acceptableSourceUnits, acceptableTargetUnits;

	final boolean sourceMinIsTargetMax;

	JEPQuantityConverterParameters(String expressionTtoS, String expressionStoT,
			ArrayList<Unit<? extends Quantity>> acceptableSourceUnits,
			ArrayList<Unit<? extends Quantity>> acceptableTargetUnits, boolean sourceMinIsTargetMax) {
		this.expressionTtoS = expressionTtoS;
		this.expressionStoT = expressionStoT;
		this.acceptableSourceUnits = acceptableSourceUnits;
		this.acceptableTargetUnits = acceptableTargetUnits;
		this.sourceMinIsTargetMax = sourceMinIsTargetMax;
	}

	/**
	 * @return The JEP expression to convert from Target to Source. Within the expression the Target value is
	 *         represented to the character X
	 */
	String getExpressionTtoS() {
		return expressionTtoS;
	}

	/**
	 * @return The JEP expression to convert from Source to Target. Within the expression the Source value is
	 *         represented to the character X
	 */
	String getExpressionStoT() {
		return expressionStoT;
	}

	// todo should make a copy but as this is a private class I can ensure
	// the returned value is not changed
	ArrayList<Unit<? extends Quantity>> getAcceptableSourceUnits() {
		return acceptableSourceUnits;
	}

	// todo should make a copy but as this is a private class I can ensure
	// the returned value is not changed
	ArrayList<Unit<? extends Quantity>> getAcceptableTargetUnits() {
		return acceptableTargetUnits;
	}

	/**
	 * 
	 */
	static public final String jUnitTestFileName = "JUnitTest";

	static JEPQuantityConverterParameters GetJEPQuantityConverterParametersFromFile(String filename) {
		JEPQuantityConverterParameters jepQuantityConverterParameters = null;
		String filePath = filename;

		if (filename.equals(jUnitTestFileName)) {
			ArrayList<Unit<? extends Quantity>> acceptableSourceUnits = new ArrayList<Unit<? extends Quantity>>();
			acceptableSourceUnits.add(QuantityFactory.createUnitFromString("Ang"));
			ArrayList<Unit<? extends Quantity>> acceptableTargetUnits = new ArrayList<Unit<? extends Quantity>>();
			acceptableTargetUnits.add(QuantityFactory.createUnitFromString("mm"));
			return new JEPQuantityConverterParameters("2*X", "X/2", acceptableSourceUnits, acceptableTargetUnits, false);
		}
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			Document document = null;

			DocumentBuilder builder = factory.newDocumentBuilder();
			factory.setValidating(true);
			factory.setNamespaceAware(true);

			builder.setErrorHandler(new org.xml.sax.ErrorHandler() {
				// ignore fatal errors (an exception is guaranteed)
				@Override
				public void fatalError(SAXParseException exception) throws SAXException {
				}

				// treat validation errors as fatal
				@Override
				public void error(SAXParseException e) throws SAXParseException {
					throw e;
				}

				// dump warnings too
				@Override
				public void warning(SAXParseException err) throws SAXParseException {
					System.out.println("** Warning" + ", line " + err.getLineNumber() + ", uri " + err.getSystemId());
					System.out.println("   " + err.getMessage());
				}
			});
			document = builder.parse(new File(filePath));
			jepQuantityConverterParameters = ReadFromDOM(new DOMSource(document));

		} catch (SAXException sxe) {
			throw new IllegalArgumentException("Error reading file " + filePath + " " + sxe.getMessage(), sxe);

		} catch (ParserConfigurationException pce) {
			// Parser with specified options can't be built
			throw new IllegalArgumentException("Error reading file " + filePath + " " + pce.getMessage(), pce);

		} catch (IOException ioe) {
			// I/O error
			throw new IllegalArgumentException("Error reading file " + filePath + " " + ioe.getMessage(), ioe);
		}
		return jepQuantityConverterParameters;
	}

	static private String getStringFromNodeList(Node node, String element) throws XPathExpressionException,
			IllegalArgumentException {
		XPath xpath = XPathFactory.newInstance().newXPath();
		String valueString = xpath.evaluate(element, node);
		if (valueString == null || valueString.equals("")) {
			throw new IllegalArgumentException("No " + element + " element in node " + node.toString());
		}
		return valueString;
	}

	static private boolean getBooleanFromNodeList(Node node, String element, boolean defValue)
			throws XPathExpressionException, IllegalArgumentException {
		XPath xpath = XPathFactory.newInstance().newXPath();
		String expression = "(" + element + "='true' or " + element + "='1')";
		Boolean val = (Boolean) xpath.evaluate(expression, node, XPathConstants.BOOLEAN);
		return val == null ? defValue : val;
	}

	static JEPQuantityConverterParameters ReadFromDOM(DOMSource source) {
		final String topNodeString = "JEPQuantityConverter";
		final String expressionTtoSString = "ExpressionTtoS";
		final String expressionStoTString = "ExpressionStoT";
		final String acceptableSourceUnitsString = "AcceptableSourceUnits";
		final String acceptableTargetUnitsString = "AcceptableTargetUnits";
		final String sourceMinIsTargetMaxString = "SourceMinIsTargetMax";

		JEPQuantityConverterParameters jepQuantityConverterParameters = null;
		XPath xpath = XPathFactory.newInstance().newXPath();
		try {
			NodeList topNodeList = (NodeList) xpath.evaluate("//" + topNodeString, source.getNode(),
					XPathConstants.NODESET);
			if (topNodeList.getLength() != 1) {
				throw new IllegalArgumentException("DOM must contain 1 and only 1 " + topNodeString + " section.");
			}
			Node topNode = topNodeList.item(0);
			String expressionTtoS = getStringFromNodeList(topNode, expressionTtoSString);
			String expressionStoT = getStringFromNodeList(topNode, expressionStoTString);
			ArrayList<Unit<? extends Quantity>> acceptableSourceUnits = new ArrayList<Unit<? extends Quantity>>();
			acceptableSourceUnits.add(QuantityFactory.createUnitFromString(getStringFromNodeList(topNode,
					acceptableSourceUnitsString)));
			ArrayList<Unit<? extends Quantity>> acceptableTargetUnits = new ArrayList<Unit<? extends Quantity>>();
			acceptableTargetUnits.add(QuantityFactory.createUnitFromString(getStringFromNodeList(topNode,
					acceptableTargetUnitsString)));
			boolean sourceMinIsTargetMax = getBooleanFromNodeList(topNode, sourceMinIsTargetMaxString, false);

			jepQuantityConverterParameters = new JEPQuantityConverterParameters(expressionTtoS, expressionStoT,
					acceptableSourceUnits, acceptableTargetUnits, sourceMinIsTargetMax);
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}
		return jepQuantityConverterParameters;
	}
}
