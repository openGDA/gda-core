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

package uk.ac.gda.util.beans.xml;

import java.io.BufferedReader;
import java.io.CharArrayReader;
import java.io.CharArrayWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.xerces.parsers.SAXParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * XML instance file validation.
 * <p>
 * This class enables validation of an XML instance file, via JAXP or Xerces2.
 * <p>
 * Castor can do both high-level Java Bean validation (in setters/getters) and parser-level validation against a schema
 * (using Xerces2). However this validation only occurs during (un)marshalling.
 * <p>
 * We also want to be able to validate an XML instance file against a schema, without having to do it during the
 * unmarshalling process.
 */
public class XMLObjectConfigFileValidator {
	private static final Logger logger = LoggerFactory.getLogger(XMLObjectConfigFileValidator.class);

	private static final String SAX_REPORT_VALIDATION_ERRORS = "http://xml.org/sax/features/validation";

	private static final String XERCES_REPORT_SCHEMA_ERRORS = "http://apache.org/xml/features/validation/schema";

	private static final String XERCES_FULL_SCHEMA_GRAMMAR_CHECKING = "http://apache.org/xml/features/validation/schema-full-checking";

	// private static final String XERCES_SCHEMA_WITH_NAMESPACE =
	// "http://apache.org/xml/properties/schema/external-schemaLocation";
	private static final String XERCES_SCHEMA_WITHOUT_NAMESPACE = "http://apache.org/xml/properties/schema/external-noNamespaceSchemaLocation";

	private static final String JAXP_DOCBUILDER_FACTORY = "javax.xml.parsers.DocumentBuilderFactory";

	private static final String JAXP_DOCBUILDER_FACTORY_XERCES_IMPL = "org.apache.xerces.jaxp.DocumentBuilderFactoryImpl";

	private static final String JAXP_PROPS_SCHEMA_LANGUAGE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";

	private static final String JAXP_PROPS_SCHEMA_SOURCE = "http://java.sun.com/xml/jaxp/properties/schemaSource";

	private static final String W3_SCHEMA_LANGUAGE_URI = "http://www.w3.org/2001/XMLSchema";

	// private static final String W3_SCHEMA_LANGUAGE_URL =
	// "http://www.w3.org/2001/XMLSchema.xsd";

	/**
	 * URL pathname to XML schema XSD file
	 */
	private String SchemaUrl = null;

	/**
	 * URL pathname to XML instance file
	 */
	private String xmlInstanceDocumentURL = null;

	/**
	 * SAX InputSource to XML instance file
	 */
	private InputSource xmlInstanceDocumentSource = null;

	/**
	 * Extends default handler to do some error handling. Implements the DefaultHandler's ErrorHandler interface. So it
	 * can be registered with a Xerces XML parser or the JAXP DocumentBuilder, and used during parsing to provide error
	 * handling support.
	 */
	private class Validator extends DefaultHandler {
		private SAXParseException saxParseException = null;
		@Override
		public void error(SAXParseException exception) throws SAXException {
			saxParseException = exception;
		}

		@Override
		public void fatalError(SAXParseException exception) throws SAXException {
			saxParseException = exception;
		}

		@Override
		public void warning(SAXParseException exception) throws SAXException {
			saxParseException = exception;
		}
		
		public void throwIfRequired() throws Exception {
			if (saxParseException!=null) throw saxParseException;
		}
	}

	/**
	 * Uses Xerces to validate an XML document against a specified XML schema. Should be able to pass in
	 * "file://d:/gda/dev/blah.xml" type of URL.
	 * 
	 * @return True if document has validated successfully. False if it fails to validate.
	 */
	private boolean xerces2Validate() throws Exception {
		SAXParser parser = new SAXParser();

		// enable reporting of validation errors - using schema or DTD
		parser.setFeature(SAX_REPORT_VALIDATION_ERRORS, true);

		// report validation errors against a schema
		parser.setFeature(XERCES_REPORT_SCHEMA_ERRORS, true);

		// enable full schema, grammar-constraint checking
		parser.setFeature(XERCES_FULL_SCHEMA_GRAMMAR_CHECKING, true);

		// Specify a validation schema for the parser to use.
		// N.B. parser is not required to locate any schema specified
		// here.
		parser.setProperty(
		// schema with namespace - may supply a list to SchemaUrl
				// XERCES_SCHEMA_WITH_NAMESPACE,
				// schema without a namespace
				XERCES_SCHEMA_WITHOUT_NAMESPACE, SchemaUrl);

		// README - could use this to locate schema so GDASchema.xsd can
		// be
		// validated on loadup
		// parser.setProperty(XERCES_SCHEMA_WITH_NAMESPACE,
		// W3_SCHEMA_LANGUAGE_URI + " " +
		// W3_SCHEMA_LANGUAGE_URL);

		// register our custom handler for the parser's error handling.
		// ie override the default handler which ignores errors.
		// Exceptions occurring are stored in the Validator's class
		// attributes.
		Validator handler = new Validator();
		parser.setErrorHandler(handler);

		if (xmlInstanceDocumentSource != null) {
			parser.parse(xmlInstanceDocumentSource);
		} else {
			parser.parse(xmlInstanceDocumentURL);
		}

		handler.throwIfRequired();

		return true;

	}

	/**
	 * Uses JAXP to validate an XML document against a specified XML schema.
	 * 
	 * @return True if document has validated successfully. False if it fails to validate.
	 */
	private boolean jaxpValidate() throws Exception {
		System.setProperty(JAXP_DOCBUILDER_FACTORY, JAXP_DOCBUILDER_FACTORY_XERCES_IMPL);

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

		// do this if XML doc has a namespace
		// factory.setNamespaceAware(true);

		// make parser validating
		factory.setValidating(true);

		factory.setAttribute(JAXP_PROPS_SCHEMA_LANGUAGE, W3_SCHEMA_LANGUAGE_URI);
		factory.setAttribute(JAXP_PROPS_SCHEMA_SOURCE, SchemaUrl);

		// README - could use this to locate schema, so GDASchema.xsd can be
		// validated on loadup
		// factory.setAttribute(XERCES_SCHEMA_WITH_NAMESPACE,
		// W3_SCHEMA_LANGUAGE_URI + " " +
		// W3_SCHEMA_LANGUAGE_URL);

		DocumentBuilder builder = factory.newDocumentBuilder();

		// register our custom handler for the parser's error handling.
		// ie override the default handler which ignores errors.
		// Exceptions occurring are stored in the Validator's class
		// attributes.
		Validator handler = new Validator();
		builder.setErrorHandler(handler);

		// parse XML instance document using DocumentBuilder parser.
		if (xmlInstanceDocumentSource != null) {
			builder.parse(xmlInstanceDocumentSource);
		} else {
			builder.parse(xmlInstanceDocumentURL);
		}

		handler.throwIfRequired();
		return false;
	}

	/**
	 * Validate an XML document file against a specified XML schema. Caller can select between JAXP and Xerces to do the
	 * validation. Should be able to pass in "file://d:/gda/dev/blah.xml" type of URL.
	 * <p>
	 * 
	 * @param SchemaUrl
	 *            URL pathname to XML schema XSD file
	 * @param xmlInstanceDocumentURL
	 *            URL pathname to XML instance file
	 * @param useXercesValidation
	 *            true to use Xerces, false to use JAXP
	 * @return True if document has validated successfully. False if it fails to validate.
	 * @throws Exception 
	 */
	public boolean validateFile(String SchemaUrl, String xmlInstanceDocumentURL, boolean useXercesValidation) throws Exception {
		this.SchemaUrl              = getPath(SchemaUrl);
		this.xmlInstanceDocumentURL = getPath(xmlInstanceDocumentURL);
		this.xmlInstanceDocumentSource = null;

		// perform validation using requested method
		if (useXercesValidation) {
			return this.xerces2Validate();
		}

		return this.jaxpValidate();
	}

	private String getPath(String fileOrUrl) {
		if (fileOrUrl==null)             return null;
		if (fileOrUrl.indexOf(":")>-1)   return fileOrUrl;
		if (fileOrUrl.startsWith("/"))   return fileOrUrl;
		return new File(fileOrUrl).getAbsolutePath();
	}

	/**
	 * Read in all data from InputSource, storing it in a CharArrayWriter, so we can create multiple instances of
	 * InputSource from it. Helper method used by validateSource()
	 * 
	 * @param source
	 *            InputSource to read data from
	 * @return CharArrayWriter containing copy of data read in from source
	 * @throws UnsupportedEncodingException 
	 */
	private CharArrayWriter getCharArrayWriterFromInputSource(InputSource source) throws UnsupportedEncodingException {
		
		// Create a BufferedReader, for reading from a SAX InputSource
		final Reader cs = source.getCharacterStream();
		Reader in = null;
		if (cs == null) {
			final InputStream i = source.getByteStream();
			in = new BufferedReader(new InputStreamReader(i, "UTF-8"));
		} else{
		    in = new BufferedReader(cs);
		}

		try {
			// Data read in is to be stored in a CharArrayWriter
			CharArrayWriter out = new CharArrayWriter();
	
			try {
				while (true) {
					// Read in data from InputSource using BufferedReader
					int charRead = in.read();
	
					if (charRead > -1) {
						// store data in the CharArrayWriter
						out.write(charRead);
					} else {
						break;
					}
				}
			} catch (IOException e) {
				logger.error("Cannot read file", e);
				out = null;
			}
	
			return out;
		} finally {
			// Must close stream or file system errors can occur.
			try {
				in.close();
			} catch (IOException e) {
				logger.error("Cannot close stream", e);
			}
		}
	}
	
	/**
	 * Validate an XML document SAX InputSource against a specified XML schema. Caller can select between JAXP and
	 * Xerces to do the validation. Should be able to pass in "file://d:/gda/dev/blah.xml" type of URL.
	 * <p>
	 * Returns the original source if validation didn't occur. Returns a new character array InputSource, containing the
	 * same data, if validation has occurred. This is necessary, since an InputSource cannot be re-read, so a cloned
	 * source must be passed back if the original was read in.
	 * <p>
	 * 
	 * @param SchemaUrl
	 *            URL pathname to XML schema XSD file
	 * @param xmlInstanceDocumentSource
	 *            SAX InputSource to XML instance file
	 * @param useXercesValidation
	 *            true to use Xerces, false to use JAXP
	 * @return a useable InputSource guaranteed to contain same data as input, if validation passed. null if validation
	 *         failed.
	 * @throws Exception 
	 * @throws SAXException
	 */
	public InputSource validateSource(String      SchemaUrl, 
						              InputSource xmlInstanceDocumentSource, 
						              boolean     useXercesValidation) throws Exception {
		// README - InputSource could be based on a Reader (characters) or
		// InputStream (bytes) - ideally, validation needs to cope with both!

		// Read in all data from InputSource, storing it in a CharArrayWriter,
		// so
		// we can create multiple instances of InputSource from it.
		CharArrayWriter data = getCharArrayWriterFromInputSource(xmlInstanceDocumentSource);

		// Create a SAX InputSource from a new CharArrayReader, which
		// contains a copy of the data stored in CharArrayWriter.

		return validateSource(SchemaUrl, data.toCharArray(), useXercesValidation);
	}

	/**
	 * 
	 * @param SchemaUrl
	 * @param xmlCharacters
	 * @param useXercesValidation
	 * @return InputSource
	 * @throws Exception
	 */
	public InputSource validateSource(String      SchemaUrl, 
							          char []     xmlCharacters, 
	                                  boolean     useXercesValidation) throws Exception {

		InputSource source = new InputSource(new CharArrayReader(xmlCharacters));

		this.SchemaUrl                 = SchemaUrl;
		this.xmlInstanceDocumentURL    = null;
		this.xmlInstanceDocumentSource = source;

		boolean valid = false;

		// perform validation using requested method
		if (useXercesValidation) {
			valid = this.xerces2Validate();
		} else {
			valid = this.jaxpValidate();
		}

		if (valid == true) {
			// README since the original InputSource
			// "xmlInstanceDocumentSource"
			// has been read once, it may not be possible to reset() &
			// re-read it.
			// "source" created for validation parse has been read, so can't
			// reuse
			// it either. So create new InputSource, to replace original
			// InputSource with, so user can still use it.

			// Create a SAX InputSource from a new CharArrayReader, which
			// contains a copy of the data stored in CharArrayWriter.
			return new InputSource(new CharArrayReader(xmlCharacters));
		}

		return null;
	}
}
