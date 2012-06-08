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

import gda.configuration.object.GenericObjectConfigDataElement;

import java.util.Stack;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * SAX XML parsing handler - builds tree of ConfigDataElements from Castor ObjectFactory instance file
 */
public class ObjectSAXModelBuilder extends DefaultHandler {
	private Stack<GenericObjectConfigDataElement> stack = new Stack<GenericObjectConfigDataElement>();

	private GenericObjectConfigDataElement rootElement = null;

	ObjectSAXModelBuilder() {
		rootElement = null;
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		// super.startElement(uri, localName, qName, attributes);

		GenericObjectConfigDataElement element = new GenericObjectConfigDataElement();

		// store top level element for use by client code
		if (rootElement == null) {
			rootElement = element;
		}

		// element.setAttribute(qName, "");
		element.setName(qName);

		for (int i = 0; i < attributes.getLength(); i++) {
			element.setAttribute(attributes.getQName(i), attributes.getValue(i));
		}

		stack.push(element);
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		// super.endElement(uri, localName, qName);

		GenericObjectConfigDataElement element = stack.pop();
		if (!stack.empty()) {
			stack.peek().addChild(element);
		}
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		// super.characters(ch, start, length);

		String text = new String(ch, start, length);
		stack.peek().addText(text);
	}

	/**
	 * @return root element
	 */
	public GenericObjectConfigDataElement getModel() {
		return rootElement;
	}

}
