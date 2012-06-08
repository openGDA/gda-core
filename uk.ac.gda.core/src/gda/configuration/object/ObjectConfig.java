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

package gda.configuration.object;

import javax.swing.tree.DefaultTreeModel;

/**
 * An interface for objects responsible for manipulating Castor XML object models. This includes loading the object
 * model into memory (ie in configuration mode).
 */
public interface ObjectConfig {
	// public void setReloadingStrategy();

	// object model persistence

	/**
	 * Load in an object model from a specified Castor XML instance file.
	 * 
	 * @param modelName
	 *            the file pathname of the Castor XML instance file containing the object model to load in.
	 */
	public void loadObjectModel(String modelName);

	// public void storeObjectModel(String modelName);

	// public void deleteObjectModel();
	// public void importObjectModel(String modelName);
	// public void exportObjectModel(String modelName);
	// public void InstantiateObjectModel();
	// public / *List* /ObjectFactory loadAndInstantiateObjectModel(String
	// modelName);

	/**
	 * Builds a Java Swing tree model from the object model. For use with GUI applications.
	 * 
	 * @return the Java Swing tree model of the object model data.
	 */
	public DefaultTreeModel buildGUITreeModel();

	/**
	 * @return the root element of the object model
	 */
	public GenericObjectConfigDataElement getObjectModelRoot();

	// object creation/destruction

	/**
	 * Creates a new element to be attached to the object model. The element is inserted as a child of the specified
	 * parent object.
	 * 
	 * @param parent
	 *            the parent element in the object model to attach the new object to.
	 * @param name
	 *            the name of the element to be created.
	 * @return the element which has been created.
	 */
	public GenericObjectConfigDataElement createObject(GenericObjectConfigDataElement parent,/* String type, */
	String name);

	/**
	 * Delete an element from the object model. The element must be removed by specifying the name of the parent of the
	 * element to be deleted.
	 * 
	 * @param parent
	 *            the parent element of the object to be deleted
	 * @param name
	 *            the name of the element to be deleted
	 */
	public void deleteObject(GenericObjectConfigDataElement parent,
	// String type,
			String name);

	/**
	 * Delete an element from the object model. The element must be removed by specifying the name of the parent of the
	 * element to be deleted.
	 * 
	 * @param parent
	 *            the parent element of the object to be deleted
	 * @param node
	 *            the reference of the element to be deleted
	 */
	public void deleteObject(GenericObjectConfigDataElement parent,
	// String type,
			GenericObjectConfigDataElement node);

	// schema for metadata & validation

	/**
	 * Load an XML Schema XSD file to represent a Castor data model.
	 * 
	 * @param fileName
	 *            the file pathname of the XSD schema file
	 */
	public void loadSchema(String fileName);

	// public void importObjectSchema(String name);

	// object metadata
	// public Object getObjectAttribute(String name);
	// public void setObjectAttribute(String name, Object value);

	/**
	 * @return a list of available object types which can be created (from the schema)
	 */
	public String[] getAvailableObjectTypesList();

	/**
	 * Gets the metadata for an object's attribute (field).
	 * 
	 * @param name
	 *            the name of the object's attribute.
	 * @return the metadata for the object's attribute.
	 */
	public ObjectAttributeMetaData getObjectAttributeMetaData(String name);

	// object model validation

	// public boolean validateObjectModel();
	// public boolean validateObject(String name);
	// public void validateObjectAttribute(String objectName, String
	// attributeName);

}
