/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package gda.epics.interfaceSpec;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import uk.ac.gda.util.beans.xml.XMLObjectConfigFileValidator;

/**
 * SimpleReader Class
 */
public class SimpleReader implements Reader {
	/**
	 * Name of description attribute for a device - normally equals desc
	 */
	public static final String DESC_ATTRIBUTE_NAME = "desc";
	
	
	private static final Logger logger = LoggerFactory.getLogger(SimpleReader.class);

	private final String pathToXML;
	private final DOMSource domSource;

	/**
	 * @param pathToXML
	 * @param pathToSchema
	 * @throws InterfaceException
	 */
	public SimpleReader(String pathToXML, String pathToSchema) throws InterfaceException {
		this.pathToXML = pathToXML;

		if (pathToXML == null || pathToXML.isEmpty()) {
			throw new InterfaceException("SimpleReader. pathToXML is invalid :" + pathToXML, null);
		}

		if (pathToSchema != null && !pathToSchema.isEmpty()) {
			try {
				XMLObjectConfigFileValidator xMLObjectConfigFileValidator = new XMLObjectConfigFileValidator();
				if (!xMLObjectConfigFileValidator.validateFile(pathToSchema, pathToXML, true))
					throw new InterfaceException("XML Validation failed : xml = " + pathToXML + " schema = "
							+ pathToSchema, null);
			} catch (Exception e) {
				throw new InterfaceException(
						"XML Validation failed : xml = " + pathToXML + " schema = " + pathToSchema, e);
			}
		}
		this.domSource = GetDOMSource(this.pathToXML);
	}

	@Override
	public List<String> getAllDeviceNames() throws InterfaceException {
		try {
			Vector<String> deviceNames = new Vector<String>();
			XPath xpath = XPathFactory.newInstance().newXPath();
			NodeList topNodeList = (NodeList) xpath.evaluate(Xml.allDevices, domSource.getNode(),
					XPathConstants.NODESET);
			for (int index = 0; index < topNodeList.getLength(); index++) {
				Node node = topNodeList.item(index);
				deviceNames.add(node.getNodeValue());
			}
			return deviceNames;
		} catch (Exception e) {
			throw new InterfaceException("SimpleReader.getAllDeviceNames failed", e);
		}
	}

	private DOMSource GetDOMSource(String filePath) throws InterfaceException {
		DOMSource domSource = null;
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
					logger.warn("** Warning" + ", line " + err.getLineNumber() + ", uri " + err.getSystemId());
					logger.warn("   " + err.getMessage());
				}
			});

			document = builder.parse(new File(filePath));
			domSource = new DOMSource(document);

		} catch (SAXException sxe) {
			throw new InterfaceException("Error reading file " + filePath + " " + sxe.getMessage(), sxe);

		} catch (ParserConfigurationException pce) {
			// Parser with specified options can't be built
			throw new InterfaceException("Error reading file " + filePath + " " + pce.getMessage(), pce);

		} catch (IOException ioe) {
			// I/O error
			throw new InterfaceException("Error reading file " + filePath + " " + ioe.getMessage(), ioe);
		}
		return domSource;
	}

	private Node getSingleNode(DOMSource source, String nodeString) throws XPathExpressionException, InterfaceException {
		XPath xpath = XPathFactory.newInstance().newXPath();
		NodeList topNodeList = (NodeList) xpath.evaluate(nodeString, source.getNode(), XPathConstants.NODESET);
		if (topNodeList.getLength() == 0) {
			return null;
		}
		if (topNodeList.getLength() > 1) {
			throw new InterfaceException(
					"SimpleReader.getSingleNode:Error. DOM contains more than 1 item that matches " + nodeString, null);
		}
		return topNodeList.item(0);
	}

	@Override
	public Device getDevice(String deviceType, String deviceName) throws InterfaceException {
		try {
			Device device = null;
			String xpath = "";
			if (deviceType != null && !deviceType.isEmpty()) {
				xpath = String.format(Xml.deviceFindByTypeAndName, deviceType, deviceName);
			} else {
				xpath = String.format(Xml.deviceFindByName, deviceName);
			}
			Node deviceNode = getSingleNode(domSource, xpath);
			if (deviceNode == null)
				return null;

			Vector<Attribute> deviceAttributes = new Vector<Attribute>();
			NamedNodeMap attribs = deviceNode.getAttributes();
			if( attribs != null ){
				for(int i=0; i< attribs.getLength(); i++){
					Node attrib = attribs.item(i);
					deviceAttributes.add(new SimpleAttribute(attrib.getNodeName(), attrib.getNodeValue()));
				}
			}
			NodeList fieldNodes = deviceNode.getChildNodes();
			if (fieldNodes != null && fieldNodes.getLength() > 0) {
				SimpleFields fields = new SimpleFields();
				String deviceTypeActual = deviceNode.getNodeName();
				for (int iField = 0; iField < fieldNodes.getLength(); iField++) {
					SimpleAttributes attributes = new SimpleAttributes();
					Node field = fieldNodes.item(iField);
					String fieldName = field.getNodeName();
					NamedNodeMap nodeAtributes = field.getAttributes();
					if (nodeAtributes != null) {
						for (int iAttribute = 0; iAttribute < nodeAtributes.getLength(); iAttribute++) {
							Node attrib = nodeAtributes.item(iAttribute);
							String name = attrib.getNodeName();
							String value = attrib.getNodeValue();
							if (name != null && value != null) {
								attributes.add(new SimpleAttribute(name, value));
							}
						}
						fields.add(new SimpleField(fieldName, attributes));
					}
				}
				device = new SimpleDevice(deviceName, deviceTypeActual, deviceAttributes, fields);
			}
			return device;

		} catch (Exception e) {
			throw new InterfaceException("SimpleReader.getDevice for device " + deviceName + " of type " + deviceType,
					e);
		}
	}
}

class SimpleAttribute implements Attribute {
	private final String name, value;

	SimpleAttribute(String name, String value) {
		this.name = name;
		this.value = value;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getValue() {
		return value;
	}

	@Override
	public String toString() {
		return name + ":" + value;
	}
}

class SimpleAttributes extends Vector<SimpleAttribute> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6722376235262372958L;
}

class SimpleField implements Field {
	private final String name;
	private final SimpleAttributes attributes;
	private final Vector<String> attributeNames;

	SimpleField(String name, SimpleAttributes attributes) {
		this.name = name;
		this.attributes = attributes;
		attributeNames = new Vector<String>();
		for (Attribute attribute : attributes) {
			attributeNames.add(attribute.getName());
		}
	}

	@Override
	public Attribute getAttribute(String attributeName) {
		for (Attribute attribute : attributes) {
			if (attribute.getName().equals(attributeName))
				return attribute;
		}
		return null;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getPV() {
		return getAttribute(Xml.pv_name).getValue();

	}

	@Override
	public boolean isReadOnly() {
		return getAttribute(Xml.ro_name).getValue().equals(Xml.isReadonly_value);
	}
	@Override
	public String getType() {
		return getAttribute(Xml.type_name).getValue();
	}
	@Override
	public Iterator<String> getAttributeNames() {
		return attributeNames.iterator();
	}

	@Override
	public String toString() {
		return name + ":" + getPV() + ":" + (isReadOnly() ? "r" : "rw");
	}
}

class SimpleFields extends Vector<SimpleField> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3170191421245247251L;

}

class SimpleDevice implements Device {
	private final String name;
	private final String type;
	private final SimpleFields fields;
	private final Vector<String> fieldNames;
	private final Vector<Attribute> attributes;

	SimpleDevice(String name, String type,  Vector<Attribute> attributes, SimpleFields fields) {
		this.name = name;
		this.type = type;
		this.fields = fields;
		fieldNames = new Vector<String>();
		for (Field field : fields) {
			fieldNames.add(field.getName());
		}
		this.attributes = new Vector<Attribute>();
		for(Attribute attribute : attributes){
			this.attributes.add(attribute);
		}
	}

	@Override
	public Field getField(String fieldName) {
		for (Field field : fields) {
			if (field.getName().equals(fieldName))
				return field;
		}
		return null;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getType() {
		return type;
	}

	@Override
	public Iterator<String> getFieldNames() {
		return fieldNames.iterator();
	}

	@Override
	public String toString() {
		return name + ":" + type;
	}

	@Override
	public String getAttributeValue(String attributeName) {
		for (Attribute attrib : attributes) {
			if (attrib.getName().equals(attributeName))
				return attrib.getValue();
		}
		return null;
	}

	@Override
	public String getDescription() {
		String desc = getAttributeValue(SimpleReader.DESC_ATTRIBUTE_NAME);
		return desc != null ? desc : "";
	}
}
