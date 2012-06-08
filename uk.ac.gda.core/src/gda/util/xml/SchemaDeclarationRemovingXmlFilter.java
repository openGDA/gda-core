/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council
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

package gda.util.xml;

import java.util.List;
import java.util.Vector;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLFilter;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.XMLFilterImpl;

/**
 * An {@link XMLFilter} that removes schema declarations from an XML document.
 */
public class SchemaDeclarationRemovingXmlFilter extends XMLFilterImpl {
	
	/** Has the first element been seen? */
	private boolean seenFirstElement;
	
	@Override
	public void startDocument() throws SAXException {
		seenFirstElement = false;
		super.startDocument();
	}

	@Override
	public void startPrefixMapping(String prefix, String uri) throws SAXException {
		// ignore
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
		if (!seenFirstElement) {
			List<String> attrNames = new Vector<String>();
			for (int i=0; i<atts.getLength(); i++) {
				attrNames.add(atts.getQName(i));
			}
			
			// Create new list of attributes; only include xmlns:xsd, if present
			AttributesImpl newAttrs = new AttributesImpl();
			for (int i=0; i<atts.getLength(); i++) {
				if (atts.getQName(i).equals("xmlns:xsd")) {
					newAttrs.addAttribute(atts.getURI(i), atts.getLocalName(i), atts.getQName(i), atts.getType(i), atts.getValue(i));
				}
			}
			atts = newAttrs;
			
			seenFirstElement = true;
		}
		super.startElement(null, localName, qName, atts);
	}
	
	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		super.endElement(null, localName, qName);
	}

}
