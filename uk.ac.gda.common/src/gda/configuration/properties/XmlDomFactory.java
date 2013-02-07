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

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DOMException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;

/**
 * Bootstrap singleton class to supply a DOM implementation, which is either located via a system property or via a JAXP
 * DocumentBuilderFactory. Abstracts client from the details of obtaining a DOM implementation. Can also generate
 * DocumentType and Document instances.
 */
public class XmlDomFactory {
	private static final Logger logger = LoggerFactory.getLogger(XmlDomFactory.class);

	private static final String DOM_IMPL_PROPERTY = "org.w3c.dom.DOMImplementationClass";

	private static DOMImplementation domImpl = null;

	/**
	 * Create DOM implementation. If classname was specified via a (system) property, then try to instantiate that.
	 * Otherwise, create from javax.xml.parsers.DocumentBuilderFactory.
	 * 
	 * @return DOM Implementation - if found. null if none found or not able to create.
	 */
	public static DOMImplementation getDomImpl() {
		// README - This implementation is pre DOM level 3 code. Done in
		// vendor-neutral way, so that code is not tied to any specific XML
		// parser
		// implementation.

		if (domImpl != null) {
			return domImpl;
		}

		// fetch DOM impl class name from existing property - may be passed via
		// -D on command line
		String domImplPropertyValue = System.getProperty(DOM_IMPL_PROPERTY);

		if (domImplPropertyValue != null) {
			try {
				// try to instantiate specified DOM impl class
				domImpl = (DOMImplementation) Class.forName(domImplPropertyValue).newInstance();
			} catch (InstantiationException e) {
				logger.error(e.getMessage());
				logger.debug(e.getStackTrace().toString());
			} catch (IllegalAccessException e) {
				logger.error(e.getMessage());
				logger.debug(e.getStackTrace().toString());
			} catch (ClassNotFoundException e) {
				logger.error(e.getMessage());
				logger.debug(e.getStackTrace().toString());
			}
		} else {
			try {
				// no property found, so create from
				// javax.xml.parsers.DocumentBuilderFactory
				domImpl = DocumentBuilderFactory.newInstance().newDocumentBuilder().getDOMImplementation();
			} catch (ParserConfigurationException e) {
				logger.error(e.getMessage());
				logger.debug(e.getStackTrace().toString());
			}
		}

		return domImpl;
	}

	/**
	 * Create a new XML DOM DocumentType instance.
	 * 
	 * @param qualifiedName
	 *            The qualified name of the document type to be created.
	 * @param publicId
	 *            The external subset public identifier.
	 * @param systemId
	 *            The external subset system identifier
	 * @return A new DocumentType node with Node.ownerDocument set to null.
	 */
	public static DocumentType createDocumentType(String qualifiedName, String publicId, String systemId) {
		if (domImpl == null) {
			getDomImpl();
		}

		if (domImpl != null) {
			DocumentType docType = domImpl.createDocumentType(qualifiedName, publicId, systemId);

			return docType;
		}

		return null;
	}

	/**
	 * Create a new XML DOM Document instance.
	 * 
	 * @param nameSpaceURI
	 *            The namespace URI of the document element to create or null.
	 * @param qualifiedName
	 *            The qualified name of the document element to be created or null.
	 * @param docType
	 *            The type of document to be created or null. When doctype is not null, its Node.ownerDocument attribute
	 *            is set to the document being created.
	 * @return A new Document object with its document element. If the NamespaceURI, qualifiedName, and doctype are
	 *         null, the returned Document is empty with no document element.
	 */
	public static Document createDocument(String nameSpaceURI, String qualifiedName, DocumentType docType) {
		if (domImpl == null) {
			getDomImpl();
		}

		Document theDocument = null;

		if (domImpl != null) {
			try {
				theDocument = domImpl.createDocument(nameSpaceURI, qualifiedName, docType);

			} catch (DOMException e) {
				logger.error(e.getMessage());
				logger.debug(e.getStackTrace().toString());
			}
		}

		return theDocument;
	}

}