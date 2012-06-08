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

package gda.configuration.object.schema;

import gda.util.ObjectServer;

import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import junit.framework.TestCase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import uk.ac.gda.util.io.FileUtils;

/**
 * Validates all ObjectFactory XML files beneath a directory.
 */
public class ObjectFactoryValidationTest extends TestCase {

	/**
	 * Set this to allow this test to run successfully on your own workstation.
	 * If this isn't set, it will assume you have a 'configurations' folder
	 * alongside the 'plugins' folder.
	 */
	private static final File DIRECTORY_TO_SEARCH_FOR_XML_FILES = null;
	/* private static final File DIRECTORY_TO_SEARCH_FOR_XML_FILES = new File("/scratch/workspace/configurations/diamond"); */
	
	private static final Logger logger = LoggerFactory.getLogger(ObjectFactoryValidationTest.class);
	
	private static final String SCHEMA_LOCATION = ObjectServer.class.getResource("../configuration/object/schema/GDASchema.xsd").getFile();
	
	private static final String SCHEMA_LANGUAGE_ATTRIBUTE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";
	
	private static final String XSD_SCHEMA_LANGUAGE = "http://www.w3.org/2001/XMLSchema";
	
	private DocumentBuilder nonValidatingDocumentBuilder;
	
	private DocumentBuilder validatingDocumentBuilder;
	
	private List<String> ignoredFiles;
	
	private static class Results {
		public int totalFiles = 0;
		
		public int totalObjectFactoryFiles = 0;
		
		public List<String> failed = new Vector<String>();
		
		public List<String> valid = new Vector<String>();
		
		public List<String> invalid = new Vector<String>();
	}
	
	private static Results results = new Results();
	
	private File directoryToSearch;
	
	@Override
	protected void setUp() throws Exception {
		ignoredFiles = readListOfIgnoredObjectFactoryFiles();
		nonValidatingDocumentBuilder = createDocumentBuilder(false);
		validatingDocumentBuilder = createDocumentBuilder(true);
	}
	
	private static List<String> readListOfIgnoredObjectFactoryFiles() throws Exception {
		String invalidFilesFile = ObjectFactoryValidationTest.class.getResource("ignored-ObjectFactory-files.txt").getFile();
		List<String> lines= FileUtils.readFileAsList(new File(invalidFilesFile));
		List<String> ignoredFiles = new Vector<String>();
		for (String line : lines) {
			if (!line.trim().startsWith("#")) {
				ignoredFiles.add(line.trim());
			}
		}
		return ignoredFiles;
	}
	
	/**
	 * Ensures that all ObjectFactory XML files (except those known to fail)
	 * validate against the GDA schema.
	 * 
	 * @throws Exception
	 */
	public void testThatAllObjectFactoryFilesValidate() throws Exception {
		String basedir = System.getProperty("basedir");
		if (basedir != null) {
			directoryToSearch = new File(basedir, "../../configurations/diamond").getCanonicalFile();
		}
		
		if (basedir == null) {
			assertNotNull("Could not automatically determine the configurations directory. You need to set DIRECTORY_TO_SEARCH_FOR_XML_FILES in " + getClass().getSimpleName(), DIRECTORY_TO_SEARCH_FOR_XML_FILES);
			directoryToSearch = DIRECTORY_TO_SEARCH_FOR_XML_FILES;
		}
		
		logger.info("Searching for XML files in " + directoryToSearch);
		searchDirectory(directoryToSearch);
		
		logger.info(results.totalFiles + " XML files checked");
		
		if (!results.failed.isEmpty()) {
			logger.error(results.failed.size() + " file(s) failed:");
			for (String file : results.failed) {
				logger.error("  " + file);
			}
		}
		
		logger.info(results.totalObjectFactoryFiles + " ObjectFactory files checked");
		
		logger.info(ignoredFiles.size() + " file(s) skipped");
		
		if (!results.invalid.isEmpty()) {
			logger.error(results.invalid.size() + " invalid ObjectFactory files:");
			for (String file : results.invalid) {
				logger.error("  " + file);
			}
		}
		
		logger.info(results.valid.size() + " valid ObjectFactory files");
		
		assertTrue("One or more XML files failed: " + results.failed.toString() + " in " + directoryToSearch + "/",
			results.failed.isEmpty());
		assertTrue("One or more ObjectFactory files were invalid: " + results.invalid.toString()  + " in " + directoryToSearch + "/",
			results.invalid.isEmpty());
	}
	
	private static DocumentBuilder createDocumentBuilder(boolean validating) throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		factory.setValidating(validating);
		factory.setAttribute(SCHEMA_LANGUAGE_ATTRIBUTE, XSD_SCHEMA_LANGUAGE);
		DocumentBuilder builder = factory.newDocumentBuilder();
		builder.setErrorHandler(new SimpleErrorHandler());
		return builder;
	}
	
	private void searchDirectory(File dir) throws Exception {
		File[] contents = dir.listFiles();
		assertNotNull("Couldn't get list of files in " + dir + " - is the pathname valid?", contents);
		
		for (File f : contents) {
			
			if (f.isDirectory()) {
				if (!f.getName().equals(".svn")) {
					searchDirectory(f);
				}
			}
			
			else if (f.isFile()) {
				if (f.getName().toLowerCase().endsWith(".xml")) {
					doXmlFile(f);
				}
			}
		}
	}

	private void doXmlFile(File file) throws Exception {
		results.totalFiles++;
		
		final String relativeFilePath = getRelativePath(file, directoryToSearch);
		
		Document doc;
		
		try {
			doc = nonValidatingDocumentBuilder.parse(file);
		} catch (Exception e) {
			logger.error("Could not determine type of " + file, e);
			results.failed.add(relativeFilePath);
			return;
		}
		
		if (isObjectFactoryFile(doc)) {
			
			results.totalObjectFactoryFiles++;
			
			if (ignoredFiles.contains(relativeFilePath)) {
				logger.info("Skipping " + file);
				return;
			}
			
			String newDocument;
			try {
				removeAllRootElementAttributes(doc);
				setNamespaceAttributes(doc);
				newDocument = convertDocumentToText(doc);
			} catch (Exception e) {
				logger.error("Unable to set up namespace attributes for " + file, e);
				results.failed.add(relativeFilePath);
				return;
			}
			
			try {
				doc = validatingDocumentBuilder.parse(new InputSource(new StringReader(newDocument)));
				results.valid.add(relativeFilePath);
			} catch (SAXException e) {
				logger.error("Could not validate " + file, e);
				results.invalid.add(relativeFilePath);
			}
		}
	}
	
	private static boolean isObjectFactoryFile(Document doc) {
		return doc.getDocumentElement().getLocalName().equals("ObjectFactory");
	}
	
	private void removeAllRootElementAttributes(Document doc) {
		final Element rootElement = doc.getDocumentElement();
		
		// Get names of all attributes
		Set<String> attributeNames = new LinkedHashSet<String>();
		NamedNodeMap rootElementAttributes = rootElement.getAttributes();
		for (int i=0; i<rootElementAttributes.getLength(); i++) {
			attributeNames.add(rootElementAttributes.item(i).getNodeName());
		}
		
		// Remove each attribute
		for (String attributeName : attributeNames) {
			rootElement.removeAttribute(attributeName);
		}
		
		assertTrue("Root element still has attributes after they were all removed?!", rootElement.getAttributes().getLength() == 0);
	}
	
	private void setNamespaceAttributes(Document doc) {
		doc.getDocumentElement().setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
		doc.getDocumentElement().setAttribute("xsi:noNamespaceSchemaLocation", SCHEMA_LOCATION);
	}
	
	private String convertDocumentToText(Document doc) throws Exception {
		DOMSource originalDocument = new DOMSource(doc);
		StringWriter newDocument = new StringWriter();
		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		transformer.transform(originalDocument, new StreamResult(newDocument));
		return newDocument.getBuffer().toString();
	}
	
	private static String getRelativePath(File file, File relativeTo) {
		return file.getAbsolutePath().substring(relativeTo.getAbsolutePath().length() + 1);
	}
}

class SimpleErrorHandler implements ErrorHandler {
	
	private void rethrow(SAXParseException exception) throws SAXException {
		throw new SAXException("Error at line " + exception.getLineNumber() + " column " + exception.getColumnNumber(), exception);
	}
	
	@Override
	public void error(SAXParseException exception) throws SAXException {
		rethrow(exception);
	}
	
	@Override
	public void fatalError(SAXParseException exception) throws SAXException {
		rethrow(exception);
	}
	
	@Override
	public void warning(SAXParseException exception) throws SAXException {
		rethrow(exception);
	}
}
