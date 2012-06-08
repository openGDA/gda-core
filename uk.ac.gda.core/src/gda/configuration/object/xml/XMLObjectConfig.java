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
import gda.configuration.object.ObjectAttributeMetaData;
import gda.configuration.object.ObjectConfig;
import gda.configuration.object.schema.CastorSchemaAdapter;
import gda.configuration.properties.LocalProperties;
import gda.factory.ObjectFactory;

import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.exolab.castor.mapping.Mapping;
import org.exolab.castor.mapping.MappingException;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import uk.ac.gda.util.beans.xml.XMLObjectConfigFileValidator;

/**
 * ObjectConfig implementor responsible for owning the object data model.
 */
public class XMLObjectConfig implements ObjectConfig {
	
	private static final Logger logger = LoggerFactory.getLogger(XMLObjectConfig.class);
	
	private GenericObjectConfigDataElement objectModelRoot = null;

	private CastorSchemaAdapter schemaAdapter = null;

	private String schemaPathName = null;

	@Override
	public void loadSchema(String fileName) {
		schemaAdapter = new CastorSchemaAdapter();
		schemaAdapter.LoadSchema(fileName);
		schemaPathName = fileName;
		logger.debug("Loading schema " + schemaPathName);
	}

	@Override
	public ObjectAttributeMetaData getObjectAttributeMetaData(String name) {
		return schemaAdapter.getElementMetaData(name);
	}

	@Override
	public String[] getAvailableObjectTypesList() {
		return schemaAdapter.getRootElementNameList();
	}

	private void buildGUITreeModelChildren(DefaultTreeModel _treeModel, DefaultMutableTreeNode parent,
			GenericObjectConfigDataElement e) {
		List<GenericObjectConfigDataElement> l = e.getChildren();
		for (int i = 0; i < l.size(); i++) {
			GenericObjectConfigDataElement child = l.get(i);

			// create named TreeNode, attach data & put into tree
			DefaultMutableTreeNode node = new DefaultMutableTreeNode(child);
			_treeModel.insertNodeInto(node, parent, i);

			// go to next level and scan any children
			// N.B. doing recursively, since has > 2 levels of nesting.
			buildGUITreeModelChildren(_treeModel, node, child);
		}
	}

	@Override
	public DefaultTreeModel buildGUITreeModel() {
		DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(objectModelRoot);

		DefaultTreeModel _treeModel = new DefaultTreeModel(rootNode);

		buildGUITreeModelChildren(_treeModel, rootNode, objectModelRoot);

		return _treeModel;
	}

	private GenericObjectConfigDataElement loadObjectModelUsingSAXP(String modelName) {
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();
			XMLReader parser = saxParser.getXMLReader();

			ObjectSAXModelBuilder modelbuilder;
			modelbuilder = new ObjectSAXModelBuilder();

			parser.setContentHandler(modelbuilder);
			parser.parse(modelName);

			return modelbuilder.getModel();
		} catch (FactoryConfigurationError e1) {
			logger.debug(e1.getStackTrace().toString());
		} catch (ParserConfigurationException e1) {
			logger.debug(e1.getStackTrace().toString());
		} catch (SAXException e1) {
			logger.debug(e1.getStackTrace().toString());
		} catch (IOException e1) {
			logger.debug(e1.getStackTrace().toString());
		}

		return null;
	}

	/**
	 * JAXP/Xerces Validation before load - using GDA schema. This is only done if the property
	 * dl.configuration.object.xml.doXMLInstanceValidation is set to true. A ValidationException is thrown if the
	 * validation test fails.
	 * 
	 * @param source
	 *            SAX InputSource for XML to be processed
	 * @return a useable InputSource guaranteed to contain same data as input.
	 * @throws ValidationException
	 */
	private InputSource doSchemaValidation(InputSource source) throws ValidationException {
		boolean doXMLInstanceValidation = LocalProperties.check("dl.configuration.object.xml.doXMLInstanceValidation",
				false);

		if (doXMLInstanceValidation == true) {
			if (schemaPathName != null) {
				XMLObjectConfigFileValidator validator = new XMLObjectConfigFileValidator();
				boolean useXercesValidation = true;
				try {
					source = validator.validateSource(schemaPathName, source, useXercesValidation);
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
					throw new ValidationException("Validation Failed: " + schemaPathName, e);
				}


				if (source == null) {
					logger.error("loadObjectModelUsingCastorUnmarshall - XML instance file invalid: "
					/* + xmlFile */);

					throw new ValidationException("loadObjectModelUsingCastorUnmarshall - XML instance file invalid: "
					/* + xmlFile */);
				}
			}
		}

		return source;
	}

	/**
	 * String interpolation of properties in Castor XML instance file. This is only done if the property
	 * dl.configuration.object.xml.doStringInterpolation is set to true.
	 * 
	 * @param source
	 *            SAX InputSource for XML to be processed
	 * @return SAX InputSource of processed XML
	 */
	private InputSource doCastorXMLStringInterpolation(InputSource source) {
		boolean doStringInterpolation = LocalProperties
				.check("dl.configuration.object.xml.doStringInterpolation", true);

		if (doStringInterpolation == true) {
			XMLStringInterpolationProcessor processor = new XMLStringInterpolationProcessor();
			source = processor.doStringInterpolationXMLFile(source);
		}

		return source;
	}

	private GenericObjectConfigDataElement loadObjectModelUsingCastorUnmarshall(String modelName) {

		GenericObjectConfigDataElement e = null;

		// Castor unmarshall - use existing ObjectFactory & ObjectCreator in
		// dl.factory
		// (ie do similar to what objectserver does)
		/* private static */String mappingFile;
		/* private static */String xmlFile;
		// /*private */ObjectCreator objectCreator = null;

		xmlFile = modelName;// LocalProperties.get("dl.objectserver.xml");
		mappingFile = LocalProperties.get("dl.objectserver.mapping");

		// try
		{
			/*
			 * ObjectServer.serverSide = serverSide; ObjectServer.localObjectsOnly = localObjectsOnly; //objectCreator =
			 * new ObjectCreator(xmlFile, mappingFile); objectCreator = new ObjectCreator(xmlFile, null); ObjectFactory
			 * objectFactory = objectCreator.getObjectFactory();
			 */
			// README - replicating what dl.factory.
			ObjectFactory objectFactory = null;

			// locate the dl.factory mapping.xml using getResource on a
			// class in
			// same package
			if (mappingFile == null) {
				/* this.getClass() */
				URL url =
				/* ObjectCreator */ObjectFactory.class.getResource("mapping.xml");
				if (url != null)
					mappingFile = url.toString();
			}

			if (xmlFile != null && mappingFile != null) {
				try {
					logger.debug("Creating objects from: " + xmlFile);
					/*
					 * if (xmlFile.compareTo("database") == 0) { //create objects using JDO objectFactory =
					 * JDOUnmarshaller.unmarshalFromDatabase(); } else if (xmlFile.compareTo("database_gui") == 0) {
					 * //create objects using JDO objectFactory = JDOUnmarshaller.unmarshalGUIFromDatabase(); } else
					 */
					// {
					// create objects using xml
					Mapping mapping = new Mapping();
					mapping.loadMapping(mappingFile);
					logger.debug("Using mapping file: " + mappingFile);

					// README - TO DO - CLASSLOADER - use & write & have
					// paths to map
					// real classpath to fake classpath
					/*
					 * try { //README - fetch autogenerated bean class root folder from properties String
					 * dummyBeanClassRoot = "d:/gda/dev/src/java"; String dummyBeanoffsetPackagePath =
					 * "dl.configuration.object.beanclasses"; ClassLoader loader = new
					 * CastorConfigClassLoader(dummyBeanClassRoot); Class c =
					 * loader.loadClass("org.exolab.castor.xml.Unmarshaller"); Unmarshaller unmarshaller =
					 * (Unmarshaller) c.newInstance(); unmarshaller.setReuseObjects(true);
					 */
					Unmarshaller unmarshaller = new Unmarshaller(mapping);
					unmarshaller.setReuseObjects(true);

					// Create the local objects.
					InputSource source;
					URI uri = new URI(xmlFile);
					if (uri.getScheme() != null && (uri.getScheme().equals("http") || uri.getScheme().equals("file")))
						source = new InputSource(uri.toURL().openStream());
					else
						source = new InputSource(new FileReader(xmlFile));

					// check whether XML instance file is valid
					source = doSchemaValidation(source);

					// String interpolation of properties in XML -
					// preprocess before
					// Castor unmarshalling
					source = doCastorXMLStringInterpolation(source);

					// do the unmarshalling of objects via Castor into
					// ObjectFactory
					objectFactory = (ObjectFactory) unmarshaller.unmarshal(source);
					/*
					 * } catch (Exception e) { e.printStackTrace(); }
					 */
					// }
				} catch (URISyntaxException ex) {
					logger.error("URL error: " + ex.getMessage());
				} catch (IOException ex) {
					logger.error("IO error: " + ex.getMessage());
				} catch (MappingException ex) {
					logger.error("Mapping error: " + ex.getMessage());
				} catch (MarshalException ex) {
					logger.error("Unmarshalling error: " + ex.getMessage());
				} catch (ValidationException ex) {
					logger.error("Validation error: " + ex.getMessage());
				}

			}

			if (objectFactory != null) {
				/*
				 * Finder finder = Finder.getInstance(); finder.addFactory(objectFactory); if (localObjectsOnly) {
				 * objectFactory.configure(); }
				 */

				// README TO DO - create IN-MEMORY objectmodel
				// (GenericObjectConfigDataElement?)
				// ie join up oe-dof/positioner etc. and build GUI tree
				// Debug.out("Server initialisation complete");
			}
		}
		/*
		 * catch (FactoryException ex) { Debug.out("Factory Exception " + ex.getMessage()); }
		 */

		return e;
	}

	@SuppressWarnings("unused")
	@Override
	public void loadObjectModel(String modelName) {
		GenericObjectConfigDataElement e = null;

		if (true) {
			// load in using SAX parser - (to be replaced by Castor
			// unmarshall)
			e = loadObjectModelUsingSAXP(modelName);
		} else {
			// load in using Castor unmarshalling
			e = loadObjectModelUsingCastorUnmarshall(modelName);
		}

		objectModelRoot = e;
	}

	/*
	 * public void storeObjectModel(String modelName) { // README Use Castor for storing object model }
	 */
	@Override
	public GenericObjectConfigDataElement getObjectModelRoot() {
		return objectModelRoot;
	}

	@Override
	public GenericObjectConfigDataElement createObject(GenericObjectConfigDataElement parent,/* String type, */
	String name) {
		GenericObjectConfigDataElement element = new GenericObjectConfigDataElement();

		element.setName(name);
		/*
		 * for(int i = 0; i < attributes.getLength(); i ++) { element.setAttribute(attributes.getName(i),
		 * attributes.getValue(i)); }
		 */
		parent.addChild(element);

		// get all sub-elements
		ObjectAttributeMetaData[] fields = schemaAdapter.getSubElementMetaDataList();

		// add all sub-elements automatically
		// README TO DO - only add mandatory sub-elements - using min/max
		// occurrences
		for (int i = 0; i < fields.length; i++) {
			// README TO DO - GenericObjectConfigDataElement's need parent
			// pointer,
			// so can pass search path to
			// getSubElementList and get subelements of subelements (for
			// nested
			// types)
			// so can do recursive create of mandatory subelements
			// ...so replace code below with following call to createObject
			// GenericObjectConfigDataElement child = createObject(element,
			// fields[i]);

			GenericObjectConfigDataElement child = new GenericObjectConfigDataElement();
			child.setName(fields[i].getName());
			element.addChild(child);

			// README TO DO - create sub-elements of sub-element
			// README TO DO - get auto-creation of sub-elements of
			// sub-element
			// working!
			// if(fields[i].getType().equalsIgnoreCase("CType"))
			// {
			// ObjectAttributeMetaData [] childFields =
			// schemaAdapter.getSubElementMetaDataList(fields[i].getName());
			// for(int j = 0; j < childFields.length; j++)
			// {
			// GenericObjectConfigDataElement grandChild =
			// new GenericObjectConfigDataElement();
			// grandChild.setName(childFields[j].getName());
			// child.addChild(grandChild);
			/*
			 * if(childFields[i].getType().equalsIgnoreCase("CType") { ObjectAttributeMetaData [] grandChildFields =
			 * schemaAdapter.getSubElementMetaDataList( childFields[j].getName()); for(int k = 0; k <
			 * grandChildFields.length; k++) { GenericObjectConfigDataElement greatGrandChild = new
			 * GenericObjectConfigDataElement(); greatGrandChild.setName(grandChildFields[k].getName());
			 * grandChild.addChild(greatGrandChild); } }
			 */
			// }
			// }
		}

		return element;
	}

	@Override
	public void deleteObject(GenericObjectConfigDataElement parent,/*
																	 * String type,
																	 */
	String name) {
		parent.deleteChild(name);
	}

	@Override
	public void deleteObject(GenericObjectConfigDataElement parent, GenericObjectConfigDataElement node) {
		parent.deleteChild(node);
	}
}
