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

package gda.spring.parsers;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.castor.mapping.BindingType;
import org.castor.mapping.MappingUnmarshaller;
import org.exolab.castor.mapping.Mapping;
import org.exolab.castor.mapping.MappingLoader;
import org.exolab.castor.xml.XMLClassDescriptor;
import org.exolab.castor.xml.util.XMLClassDescriptorAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.xml.NamespaceHandler;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;
import org.xml.sax.InputSource;


/**
 * A {@link NamespaceHandler} that handles the {@code objectfactory} namespace
 * used in Castor XML files.
 */
public class ObjectFactoryNamespaceHandler extends NamespaceHandlerSupport {
	
	private static final Logger logger = LoggerFactory.getLogger(ObjectFactoryNamespaceHandler.class);
	
	/**
	 * Location of the GDA Castor mapping file.
	 */
	protected static final String MAPPING_FILE_LOCATION = "../../factory/mapping.xml";
	
	protected MappingLoader mappingLoader;
	
	protected Map<String, XMLClassDescriptor> classDescsByElementName;
	
	@Override
	public void init() {
		ObjectFactoryParser parser = new ObjectFactoryParser(this);
		
		// Load the Castor mapping file
		try {
			logger.debug("Loading Castor mapping file from " + MAPPING_FILE_LOCATION);
			Mapping mapping = new Mapping();
			InputStream is = ObjectFactoryNamespaceHandler.class.getResourceAsStream(MAPPING_FILE_LOCATION);
			mapping.loadMapping(new InputSource(is));
			MappingUnmarshaller mappingUnmarshaller = new MappingUnmarshaller();
			mappingLoader =  mappingUnmarshaller.getMappingLoader(mapping, BindingType.XML);
		} catch (Exception e) {
			throw new RuntimeException("Could not load Castor mapping file", e);
		}
		
		// Create a map of class descriptors keyed by element name. Also
		// registers the parser to handle each element
		classDescsByElementName = new HashMap<String, XMLClassDescriptor>();
		for (Iterator<?> it = mappingLoader.descriptorIterator(); it.hasNext(); ) {
			XMLClassDescriptorAdapter cd = (XMLClassDescriptorAdapter) it.next();
			classDescsByElementName.put(cd.getXMLName(), cd);
			registerBeanDefinitionParser(cd.getXMLName(), parser);
		}
		
		// TODO discover all elements in the schema and remove the (incomplete) hard-coded list
		final String[] otherElementNames = {
			"name",
			"deviceName",
			"host",
			"port",
			"numberOfHolders",
			"samplesPerHolder",
			"commandPort",
			"statusPort",
			"motorName",
			"stepsPerUnit",
			"softLimitLow",
			"softLimitHigh",
			"poll",
			"protectionLevel",
			"moveableName"
		};
		
		// Register the parser to handle other elements
		for (String elementName : otherElementNames) {
			registerBeanDefinitionParser(elementName, parser);
		}
	}
	
	/**
	 * Determines if the given element corresponds to a class in the Castor
	 * mapping file.
	 * 
	 * @param elementName the name of the element
	 * 
	 * @return {@code true} if the element corresponds to a class
	 */
	protected boolean doesElementCorrespondToClass(String elementName) {
		return classDescsByElementName.containsKey(elementName);
	}
	
	/**
	 * Returns the class descriptor for the class that corresponds to the
	 * given element.
	 * 
	 * @param elementName the name of the element
	 * 
	 * @return the class descriptor for the class that corresponds to the
	 *         element
	 */
	protected XMLClassDescriptor getClassDescriptorForElement(String elementName) {
		return classDescsByElementName.get(elementName);
	}
	
	/**
	 * Returns the class descriptor for the specified class.
	 * 
	 * @param className the class name
	 * 
	 * @return the class descriptor for the class
	 */
	protected XMLClassDescriptor getClassDescriptorForClass(String className) {
		return (XMLClassDescriptor) mappingLoader.getDescriptor(className);
	}
}
