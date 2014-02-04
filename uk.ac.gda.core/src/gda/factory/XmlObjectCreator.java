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

package gda.factory;

import static gda.util.xml.XmlUtils.filterXml;
import static gda.util.xml.XmlUtils.readXmlDocument;
import static gda.util.xml.XmlUtils.stringInputSource;
import gda.configuration.properties.LocalProperties;
import gda.jscience.physics.units.NonSIext;
import gda.util.xml.FailFastErrorHandler;
import gda.util.xml.SchemaDeclarationRemovingXmlFilter;

import java.io.CharArrayWriter;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.XMLReaderFactory;

import uk.ac.gda.util.beans.xml.XMLHelpers;
import uk.ac.gda.util.io.MacroSupplier;
import uk.ac.gda.util.io.StreamMacroSubstitutor;

/**
 * Parent class for object creators that use a Castor
 * 
 * @deprecated Adding Spring beans directly to the application context is the
 * preferred method for instantiating objects. Encapsulating objects within an
 * {@link ObjectFactory} created by this class results in objects that
 * cannot be referenced from the application context.
 */
@Deprecated
public abstract class XmlObjectCreator implements IObjectCreator {

	private static final String SCHEMA_LANGUAGE = "http://www.w3.org/2001/XMLSchema";
	
	private static final Logger logger = LoggerFactory.getLogger(XmlObjectCreator.class);

	static {
		NonSIext.initializeClass();
	}

	protected String xmlFile;

	protected URL mappingUrl;

	protected String mappingFile;

	protected boolean useDefaultMapping = true;

	protected URL schemaUrl;

	protected String schemaFile;

	protected boolean useDefaultSchema = false;

	protected MacroSupplier macroSupplier;

	boolean doPropertySubstitution = false;

	/**
	 * @param xmlFile
	 *            the XML file that defines the objects to be created by this factory if mappingUrl is null
	 */
	public void setXmlFile(String xmlFile) {
		this.xmlFile = xmlFile;
	}

	/**
	 * Sets the mapping XML file used when building this factory.
	 * 
	 * @param mappingFile
	 *            the XML mapping file
	 */
	public void setMappingFile(String mappingFile) {
		this.mappingFile = mappingFile;
	}

	/**
	 * @param mappingUrl
	 *            The URL to the input source used as a mapping file
	 */
	public void setMappingUrl(URL mappingUrl) {
		this.mappingUrl = mappingUrl;
	}

	/**
	 * @param macroSupplier
	 *            If null then the xmlFile can contain macros of the form ${xxx} where the replacement for xxx is found
	 *            by calling the macroSupplier.get method
	 */
	public void setMacroSupplier(MacroSupplier macroSupplier) {
		this.macroSupplier = macroSupplier;
	}

	/**
	 * @param schemaFile
	 *            Path to the schema to be used if schemaUrl is null
	 */
	public void setSchemaFile(String schemaFile) {
		this.schemaFile = schemaFile;
	}

	/**
	 * @return schema URL
	 */
	public URL getSchemaUrl() {
		return schemaUrl;
	}

	/**
	 * @param schemaUrl
	 */
	public void setSchemaUrl(URL schemaUrl) {
		this.schemaUrl = schemaUrl;
	}

	/**
	 * @param doPropertySubstitution
	 *            If True and macroSupplier is null then LocalProperties.get is used to provide a macroSupplier
	 */
	public void setDoPropertySubstitution(boolean doPropertySubstitution) {
		this.doPropertySubstitution = doPropertySubstitution;
	}

	/**
	 * Constructor . xmlFile, mappingUrl, mappingFile, schemaUrl, schemaFile, macroSupplier are all null
	 * useDefaultSchema and doPropertySubstitution are false useDefaultMapping=true
	 */
	public XmlObjectCreator() {
	}

	/**
	 * @param useDefaultMapping
	 *            If True and mappingUrl is null and mappingFile is null then resource mapping.xml in the same
	 *            gda.facory package is used
	 */
	public void setUseDefaultMapping(boolean useDefaultMapping) {
		this.useDefaultMapping = useDefaultMapping;
	}

	/**
	 * @param useDefaultSchema
	 *            If True and schemaUrl is null then the schema read from property "gda.factory.gdaSchemaLocation" is
	 *            used , or if this is not set then gda/configuration/object/schema/GDASchema.xsd is used
	 */
	public void setUseDefaultSchema(boolean useDefaultSchema) {
		this.useDefaultSchema = useDefaultSchema;
	}

	@Override
	public ObjectFactory getFactory() throws FactoryException {
		try {
			InputSource source = new InputSource(new FileReader(xmlFile));
			return getFactory(source);
		} catch (Exception e) {
			throw new FactoryException("Could not create ObjectFactory from XML file " + xmlFile, e);
		}
	}

	/**
	 * Return the instance of object factory.
	 * 
	 * @param source
	 *            The xml to be parsed
	 * @return the object factory
	 * @throws Exception
	 */
	public ObjectFactory getFactory(InputSource source) throws Exception {
		/* use local variables so that objects created in this method do not persist */
		URL mapUrl = mappingUrl;
		URL schUrl = schemaUrl;
		if (mapUrl == null && mappingFile != null) {
			if(mappingFile.startsWith("classpath:")){
				String classFile = mappingFile.substring(10);
				mapUrl = ClassLoader.getSystemResource(classFile);
			} else {
				mapUrl = new File(mappingFile).toURI().toURL();
			}
			if (mapUrl == null)
				throw new IllegalArgumentException("Failure in getResource for mapping.xml " + mappingFile);
		}
		if (mapUrl == null && useDefaultMapping) {
			mapUrl = this.getClass().getResource("mapping.xml");
			if( mapUrl == null)
				throw new IllegalArgumentException("Failure in getResource for mapping.xml for class " + getClass().getName());
		}

		if (schUrl == null && schemaFile != null) {
			schUrl = (new File(schemaFile)).toURI().toURL();
		}
		if (schUrl == null && useDefaultSchema) {
			String gdaSchemaPathName = LocalProperties.get("gda.factory.gdaSchemaLocation", null);
			if (gdaSchemaPathName == null) {
				schUrl = ClassLoader.getSystemResource("gda/configuration/object/schema/GDASchema.xsd");
				if( schUrl == null)
					throw new IllegalArgumentException("Failure in getSystemResource for gda/configuration/object/schema/GDASchema.xsd");
			} else {
				schUrl = (new File(gdaSchemaPathName)).toURI().toURL();
			}
		}
		
		// If a macroSupplier has been specified, use that one
		MacroSupplier macroS = macroSupplier;
		// If one hasn't been specified, and property substitution is required,
		// use a default macro supplier that gets values from LocalProperties
		if (macroS == null && doPropertySubstitution) {
			macroS = new MacroSupplier() {
				@Override
				public String get(String key) {
					return LocalProperties.get(key);
				}
			};
		}
		
		// An InputSource cannot be read more than once, but we may need to
		// parse the XML twice (once to validate, once to unmarshal). Get the
		// XML document as a String so that a new InputSource can be created
		// each time it needs to be parsed.
		String xmlDocument = readXmlDocument(source);
		
		if (macroS != null) {
			Reader in = new StringReader(xmlDocument);
			CharArrayWriter out = new CharArrayWriter();
			StreamMacroSubstitutor.process(in, out, macroS);
			xmlDocument = out.toString();
		}
		
		// If a schema has been specified, validate the XML against it
		if (schUrl != null) {
			validateXmlUsingSchema(stringInputSource(xmlDocument), schUrl);
		}
		
		// Remove all schema declarations from the XML - Castor doesn't like them
		xmlDocument = removeSchemaDeclarations(xmlDocument);
		
		return (ObjectFactory) XMLHelpers.createFromXML(mapUrl, ObjectFactory.class, schUrl, stringInputSource(xmlDocument), false);
	}
	
	private static void validateXmlUsingSchema(InputSource xml, URL schemaUrl) throws Exception {
		// Read source XML, and strip out all schema declarations
		String xmlDoc = readXmlDocument(xml);
		xmlDoc = removeSchemaDeclarations(xmlDoc);

		// Read XSD, and strip out all schema declarations
		String schemaDoc = readXmlDocument(schemaUrl);
		schemaDoc = removeSchemaDeclarations(schemaDoc);

		// Read the schema
		Source schemaSource = new SAXSource(XMLReaderFactory.createXMLReader(), stringInputSource(schemaDoc));
		SchemaFactory schemaFactory = SchemaFactory.newInstance(SCHEMA_LANGUAGE);
		Schema schema = schemaFactory.newSchema(schemaSource);
		
		// Create DocumentBuilder
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		docBuilderFactory.setSchema(schema);
		DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
		docBuilder.setErrorHandler(new FailFastErrorHandler(logger));
		
		// Parse the XML... if this succeeds, it must be valid
		docBuilder.parse(stringInputSource(xmlDoc));
	}
	
	private static String removeSchemaDeclarations(String xmlDocument) throws Exception {
		return readXmlDocument(filterXml(stringInputSource(xmlDocument), new SchemaDeclarationRemovingXmlFilter()));
	}
	
	@Override
	public boolean isLocal() {
		return true;
	}

}
