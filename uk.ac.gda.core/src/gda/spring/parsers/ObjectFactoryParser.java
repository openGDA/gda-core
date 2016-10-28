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

// TODO make Moveables work. Moveables are of type gda.oe.Moveable which isn't an element in the mapping file :(

package gda.spring.parsers;

import java.util.List;
import java.util.Vector;

import org.exolab.castor.mapping.FieldDescriptor;
import org.exolab.castor.xml.XMLClassDescriptor;
import org.exolab.castor.xml.XMLFieldDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A {@link BeanDefinitionParser} that can parse any element in the GDA schema.
 */
public class ObjectFactoryParser extends AbstractSingleBeanDefinitionParser {

	private static final Logger logger = LoggerFactory.getLogger(ObjectFactoryParser.class);

	/**
	 * The namespace handler for the GDA schema.
	 */
	protected ObjectFactoryNamespaceHandler parent;

	/**
	 * List of fields to ignore. The parser will skip any fields it encounters
	 * that are in this list.
	 */
	protected List<CastorClassField> exclusions;

	/**
	 * Creates a new parser that can handle elements in the GDA schema.
	 *
	 * @param parent the namespace handler for the GDA schema
	 */
	public ObjectFactoryParser(ObjectFactoryNamespaceHandler parent) {
		this.parent = parent;

		exclusions = new Vector<CastorClassField>();
//		exclusions.add(new CastorClassField(SampleChangerBase.class, "samplesPerHolder"));
	}

	@Override
	protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
		logger.debug("Parsing <" + element.getLocalName() + "> element");

		// Should only be parsing 'complex' elements, where the mapping file is
		// needed to understand the meaning of child elements
		if (!parent.doesElementCorrespondToClass(element.getLocalName())) {
			throw new RuntimeException("Cannot handle the \"" + element.getLocalName() + "\" element");
		}

		// Find the class descriptor corresponding to this element
		XMLClassDescriptor classDesc = parent.getClassDescriptorForElement(element.getLocalName());
		builder.getBeanDefinition().setBeanClass(classDesc.getJavaClass());
		logger.debug("Element \"" + element.getLocalName() + "\" corresponds to class " + classDesc.getJavaClass().getName());

		// Parse child elements. What we do with them depends on whether they
		// correspond to a 'simple' type (e.g. a string property) or a 'complex'
		// type (e.g. a <LinearPositioner> nested within a <GenericOE>).
		List<Element> childElements = getChildElements(element);
		for (Element childElement : childElements) {

			// Find the field for this child element
			XMLFieldDescriptor field = findFieldDescriptorForElement(childElement.getLocalName(), classDesc);
			if (field == null) {
				final String msg = "Could not find field information for \"" + childElement.getLocalName() + "\" element";
				logger.error(msg);
				throw new RuntimeException(msg);
			}
			logger.debug("Child element \"" + childElement.getLocalName() + "\" corresponds to field " + field);

			// Ignore the field if it is in the exclusion list
			boolean excluded = false;
			for (CastorClassField exclusion : exclusions) {
				if (exclusion.matches(field)) {
					excluded = true;
					break;
				}
			}
			if (excluded) {
				final String msg = "Skipping excluded field \"" + field.getFieldName() + "\" from class " + classDesc.getJavaClass().getName();
				logger.warn(msg);
			}

			else {
				// Get the field's value
				Object value = null;

				// If the element corresponds to a class in the mapping file,
				// invoke the parser to get its value as a BeanDefinition
				if (parent.doesElementCorrespondToClass(childElement.getLocalName())) {
					value = parserContext.getDelegate().parseCustomElement(childElement, builder.getBeanDefinition());
				}

				// If the element corresponds to a simple type (e.g. string),
				// use the text content of the element. The parser isn't invoked
				// for it because we don't create a BeanDefinition for it
				else if (field.getFieldType().isPrimitive() || field.getFieldType().equals(String.class)) {
					value = childElement.getTextContent();
				}

				// Throw an exception if the above cases did not allow a value
				// to be determined from the child element
				if (value == null) {
					throw new RuntimeException("Could not determine value for field \"" + field.getFieldName() + "\" from class " + classDesc.getJavaClass().getName());
				}

				// Add the value to this bean
				if (!field.isMultivalued()) {
					addProperty(builder.getBeanDefinition(), field.getFieldName(), value);
				} else {
					addMultivaluedProperty(builder.getBeanDefinition(), field.getFieldName(), value);
				}
			}
		}
	}

	/**
	 * Searches for a field descriptor that matches the element with the
	 * specified name, in the context of the given class descriptor. Looks first
	 * for a field that matches the element name; if none can be found, and the
	 * element corresponds to a class in the Castor mapping file, looks for a
	 * field of that type.
	 *
	 * @param elementName the name of the element
	 * @param containingClassDesc the class in which the element appears
	 *
	 * @return a field descriptor for the element, or {@code null}
	 */
	protected XMLFieldDescriptor findFieldDescriptorForElement(String elementName, XMLClassDescriptor containingClassDesc) {
		// Look for a match by name first
		XMLFieldDescriptor f = findFieldDescriptorForElementName(elementName, containingClassDesc);
		if (f != null) {
			return f;
		}

		// If no match found by name, look for a match by type instead
		else if (parent.doesElementCorrespondToClass(elementName)) {
			XMLClassDescriptor elementClassDesc = parent.getClassDescriptorForElement(elementName);
			return findFieldDescriptorForElementType(elementClassDesc.getJavaClass(), containingClassDesc);
		}

		// No match found
		else {
			return null;
		}
	}

	/**
	 * Finds the field descriptor for the element with the specified name, in
	 * the context of the given class. Recursively examines the fields in the
	 * specified class descriptor and its parents.
	 *
	 * @param elementName the name of the element
	 * @param classDesc the class in which the element appears
	 *
	 * @return a field descriptor for the element, or {@code null}
	 */
	protected XMLFieldDescriptor findFieldDescriptorForElementName(String elementName, XMLClassDescriptor classDesc) {
		// Do any of the fields in this class match the element name?
		for (FieldDescriptor f : classDesc.getFields()) {
			XMLFieldDescriptor field = (XMLFieldDescriptor) f;
			if (field.getXMLName().equals(elementName)) {
				return field;
			}
		}

		// If none of this class' fields match, look at its parent class, if
		// it has one
		if (classDesc.getExtends() != null) {
			XMLClassDescriptor parentClass = (XMLClassDescriptor) classDesc.getExtends();
			return findFieldDescriptorForElementName(elementName, parentClass);
		}

		return null;
	}

	/**
	 * Looks for a field descriptor in the specified class descriptor that
	 * matches an element with the given class. Recursively examines the fields
	 * in the specified class descriptor and its parents.
	 *
	 * @param elementClass the class of the nested element
	 * @param classDesc the class descriptor
	 *
	 * @return a field descriptor for the element, or {@code null}
	 */
	protected XMLFieldDescriptor findFieldDescriptorForElementType(Class<?> elementClass, XMLClassDescriptor classDesc) {
		for (FieldDescriptor f : classDesc.getFields()) {
			XMLFieldDescriptor field = (XMLFieldDescriptor) f;
			Class<?> fieldType = field.getFieldType();
			if (field.isMultivalued() && fieldType.isAssignableFrom(elementClass)) {
				return field;
			}
		}

		if (classDesc.getExtends() != null) {
			XMLClassDescriptor parentClass = (XMLClassDescriptor) classDesc.getExtends();
			return findFieldDescriptorForElementType(elementClass, parentClass);
		}

		return null;
	}

	/**
	 * Determines the ID for a bean. Uses the value of the {@code <name>} child
	 * element, if one exists; otherwise uses the default Spring behaviour.
	 */
	@Override
	protected String resolveId(Element element, AbstractBeanDefinition definition, ParserContext parserContext) throws BeanDefinitionStoreException {

		// Look for a <name> child element first
		List<Element> nameElements = getChildElementsByTagName(element, "name");
		if (nameElements.size() > 0) {
			return nameElements.get(0).getTextContent();
		}

		// If there is no <name> child element, use the default behaviour
		return super.resolveId(element, definition, parserContext);
	}

	@Override
	public boolean shouldGenerateIdAsFallback() {
		// Instruct Spring to automatically generate an ID for a bean, if one
		// is not explicitly provided
		return true;
	}

	/**
	 * Ensures the given bean definition has a property values object, and
	 * creates a new {@link MutablePropertyValues} object if it does not.
	 *
	 * @param beanDefinition the bean definition
	 */
	protected static void ensureBeanDefinitionHasPropertyValues(BeanDefinition beanDefinition) {
		if (beanDefinition.getPropertyValues() == null) {
			AbstractBeanDefinition abstractBeanDef = (AbstractBeanDefinition) beanDefinition;
			abstractBeanDef.setPropertyValues(new MutablePropertyValues());
		}
	}

	/**
	 * Adds a property with the specified name to the given bean definition.
	 *
	 * @param beanDefinition the bean definition
	 * @param propertyName the name of the property
	 * @param value the property value
	 */
	protected static void addProperty(BeanDefinition beanDefinition, String propertyName, Object value) {
		ensureBeanDefinitionHasPropertyValues(beanDefinition);
		PropertyValue pv = new PropertyValue(propertyName, value);
		beanDefinition.getPropertyValues().addPropertyValue(pv);
	}

	/**
	 * Adds a new value to a container property for the given bean definition.
	 *
	 * @param beanDefinition the bean definition
	 * @param propertyName the name of the property
	 * @param object the object to add to the property's list of values
	 */
	@SuppressWarnings("unchecked")
	protected static void addMultivaluedProperty(BeanDefinition beanDefinition, String propertyName, Object object) {
		ensureBeanDefinitionHasPropertyValues(beanDefinition);

		// Add a new property (with an empty list as the value) if the bean
		// doesn't already have one
		PropertyValue pv = beanDefinition.getPropertyValues().getPropertyValue(propertyName);
		if (pv == null) {
			pv = new PropertyValue(propertyName, new ManagedList<Object>());
			beanDefinition.getPropertyValues().addPropertyValue(pv);
		}

		// Add new value to the list of values for the property
		ManagedList<Object> ml = (ManagedList<Object>) pv.getValue();
		ml.add(object);
	}

	/**
	 * Returns all child elements of the given element.
	 *
	 * @param element an element
	 *
	 * @return its child elements
	 */
	private List<Element> getChildElements(Element element) {
		List<Element> childElements = new Vector<Element>();
		NodeList childNodes = element.getChildNodes();
		for (int i=0; i<childNodes.getLength(); i++) {
			Node node = childNodes.item(i);
			if (node instanceof Element) {
				childElements.add((Element) node);
			}
		}
		return childElements;
	}

	/**
	 * Returns a list of the specified element's immediate child elements that
	 * have the specified tag name. Works like
	 * {@link Element#getElementsByTagName(String)} but only returns immediate
	 * children of the element.
	 *
	 * @param element an element
	 * @param tagName a tag name
	 *
	 * @return child elements with the specified tag name
	 */
	protected static List<Element> getChildElementsByTagName(Element element, String tagName) {
		List<Element> childElements = new Vector<Element>();
		NodeList childNodes = element.getChildNodes();
		for (int i=0; i<childNodes.getLength(); i++) {
			Node childNode = childNodes.item(i);
			if (childNode instanceof Element) {
				Element childElement = (Element) childNode;
				if (childElement.getTagName().equals(tagName)) {
					childElements.add(childElement);
				}
			}
		}
		return childElements;
	}

}
