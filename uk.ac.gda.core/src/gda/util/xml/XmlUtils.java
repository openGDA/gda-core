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

import java.io.CharArrayReader;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;

import org.springframework.util.FileCopyUtils;
import org.xml.sax.InputSource;
import org.xml.sax.XMLFilter;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * Utility methods for working with XML documents.
 */
public class XmlUtils {

	/**
	 * Reads the XML document and returns it as a {@link String}.
	 * 
	 * @param url the URL of the XML document
	 * 
	 * @return the XML document, as a {@link String}
	 * 
	 * @throws IOException
	 */
	public static String readXmlDocument(URL url) throws IOException {
		Reader in = new InputStreamReader(url.openStream());
		CharArrayWriter out = new CharArrayWriter();
		FileCopyUtils.copy(in, out);
		return out.toString();
	}
	
	/**
	 * Reads an XML document and returns it as a {@link String}.
	 * 
	 * @param source the input source for the XML document
	 * 
	 * @return the XML document, as a {@link String}
	 * 
	 * @throws IOException
	 */
	public static String readXmlDocument(InputSource source) throws IOException {
		CharArrayWriter out = new CharArrayWriter();
		FileCopyUtils.copy(source.getCharacterStream(), out);
		return out.toString();
	}
	
	/**
	 * Filters an XML document.
	 * 
	 * @param source the input source for the XML document
	 * @param filter the filter
	 * 
	 * @return an input source for the resulting document
	 * 
	 * @throws Exception
	 */
	public static InputSource filterXml(InputSource source, XMLFilter filter) throws Exception {
		// Create filter, which uses a "real" XMLReader but also removes the
		// attributes
		XMLReader reader = XMLReaderFactory.createXMLReader();
		filter.setParent(reader);
		
		// Create a Transformer
		TransformerFactory tf = TransformerFactory.newInstance();
		Transformer t = tf.newTransformer();
		
		// Transform the source tree, applying the filter, and store the result
		// tree in a character array
		CharArrayWriter writer = new CharArrayWriter();
		t.transform(new SAXSource(filter, source), new StreamResult(writer));
		
		// Return a new InputSource using the result tree
		return new InputSource(new CharArrayReader(writer.toCharArray()));
	}

	/**
	 * Creates an {@link InputSource} for the XML document in the given
	 * {@link String}.
	 * 
	 * @param xml the XML document as a {@link String}
	 * 
	 * @return an input source for the XML document
	 */
	public static InputSource stringInputSource(String xml) {
		return new InputSource(new StringReader(xml));
	}

}
