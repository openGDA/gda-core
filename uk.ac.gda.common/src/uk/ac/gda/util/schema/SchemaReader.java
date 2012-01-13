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

package uk.ac.gda.util.schema;

import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A Class that reads a castor objects schema and provides information 
 * about the elements defined in the xsd.
 * 
 * There should be a more elegant way of reading the Schema. There is a
 * Schema object in castor but reading elements appears to involve loops and
 * visitors which is not what we want.
 * 
 * For now this class uses xPath.
 * 
 * @author fcp94556
 *
 */
public class SchemaReader {

	private String nameSpaceURI = "http://www.w3.org/2001/XMLSchema";
	
	private final XPathFactory factory;
	private final Document     doc;

	/**
	 * Class reads the schema and returns properties contained in it.
	 * Specially set up for Castor schemas in xsd namespace.
	 * @param schemaUrl
	 * @throws Exception
	 */
	public SchemaReader(final URL  schemaUrl) throws Exception {
		
		DocumentBuilderFactory docBuilder = DocumentBuilderFactory.newInstance();
		docBuilder.setNamespaceAware(true); // never forget this!
		DocumentBuilder builder = docBuilder.newDocumentBuilder();
		
		this.doc     = builder.parse(schemaUrl.openConnection().getInputStream());
		this.factory = XPathFactory.newInstance();
	}
	
	/**
	 * Gets the legal choices for the element or null if there are none.
	 * @param mainElementName
	 * @param containingElementName
	 * @return List<String>
	 * @throws Exception
	 */
	public List<String> getAllowedChoices(final String mainElementName,
			                              final String containingElementName) throws Exception {
		return processExpression("//xsd:schema/xsd:element[@name='"+mainElementName+"']/xsd:complexType/xsd:sequence/xsd:element[@name='"+containingElementName+"']/xsd:simpleType/xsd:restriction/xsd:enumeration",
				                 "value");
	}
	
	/**
	 * 
	 * @param parentName 
	 * @return list of all elements
	 * @throws Exception
	 */
	public List<String> getChildTags(final String parentName) throws Exception {
		
		if (parentName == null) {
			return processExpression("*/xsd:element", "name");
		} 
		
		return processExpression("*//xsd:element[@name='"+parentName+"']/xsd:complexType/xsd:sequence/xsd:element", "name");
	}
	

	public List<String> getParents(String name) throws Exception {
		
		final String expression = "*//xsd:element";
		final XPath xpath = factory.newXPath();
		xpath.setNamespaceContext(getXSDContext());

		final XPathExpression expr = xpath.compile(expression);
		final Object        result = expr.evaluate(doc, XPathConstants.NODESET);
		if (result==null) return null;
		
		final NodeList nodes = (NodeList) result;
		if (nodes.getLength()<1) return null;

		final List<String> items = new ArrayList<String>(nodes.getLength());
		for (int i = 0; i < nodes.getLength(); i++) {
		    final Node node = nodes.item(i);
		    if (node.getAttributes().getNamedItem("name")!=null && name.equals(node.getAttributes().getNamedItem("name").getNodeValue())) {
			    try {
			    	final Node par  = node.getParentNode().getParentNode().getParentNode();
				    if (par.getAttributes().getNamedItem("name")==null) continue;
				    items.add(par.getAttributes().getNamedItem("name").getNodeValue());
			    } catch (Exception ne) {
			    	continue;
			    }
		    }
		}
		return items;
	}

	private List<String> processExpression(final String expression, final String fieldName) throws Exception{
		
		final XPath xpath = factory.newXPath();
		xpath.setNamespaceContext(getXSDContext());

		final XPathExpression expr = xpath.compile(expression);
		final Object        result = expr.evaluate(doc, XPathConstants.NODESET);
		if (result==null) return null;
		
		final NodeList nodes = (NodeList) result;
		if (nodes.getLength()<1) return null;
	
		final List<String> items = new ArrayList<String>(nodes.getLength());
		for (int i = 0; i < nodes.getLength(); i++) {
		    final Node node = nodes.item(i);
		    items.add(node.getAttributes().getNamedItem(fieldName).getNodeValue());
		}
		return items;

	}
	
	/**
	 * @return the nameSpaceURI
	 */
	public String getNameSpaceURI() {
		return nameSpaceURI;
	}

	/**
	 * @param nameSpaceURI the nameSpaceURI to set default http://www.w3.org/2001/XMLSchema
	 */
	public void setNameSpaceURI(String nameSpaceURI) {
		this.nameSpaceURI = nameSpaceURI;
	}

	private NamespaceContext getXSDContext() {
		return new NamespaceContext() {

			@Override
			public String getNamespaceURI(String prefix) {
				if ("xsd".equals(prefix)) {
					return nameSpaceURI;
				}
				return null;
			}

			@Override
			public String getPrefix(String namespaceURI) {
				// TODO Auto-generated method stub
				return null;
			}

			@SuppressWarnings("rawtypes")
			@Override
			public Iterator getPrefixes(String namespaceURI) {
				// TODO Auto-generated method stub
				return null;
			}
			
		};
	}
}

	