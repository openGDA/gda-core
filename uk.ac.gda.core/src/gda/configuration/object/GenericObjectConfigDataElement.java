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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/**
 * Generic container for holding configuration information for any Castor object.
 */
public class GenericObjectConfigDataElement {
	private String name = new String("blank");

	private StringBuffer text = new StringBuffer("");

	private HashMap<String, String> attributes = new HashMap<String, String>();

	private List<GenericObjectConfigDataElement> children = new ArrayList<GenericObjectConfigDataElement>();

	/**
	 * @return the name of this object
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name of this object to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the text contained in this object
	 */
	public String getText() {
		return text.toString();
	}

	/**
	 * @param text
	 *            the text to set in this object
	 */
	public void setText(String text) {
		this.text = new StringBuffer(text);
	}

	/**
	 * @param theText
	 *            the text to be added to this object
	 */
	public void addText(String theText) {
		this.text.append(theText);
	}

	/**
	 * @param name
	 *            the attribute to set
	 * @param value
	 *            the new value of the attribute
	 */
	public void setAttribute(String name, String value) {
		attributes.put(name, value);
	}

	/**
	 * @param theName
	 *            the name of the attribute
	 * @return the value of the attribute
	 */
	public String getAttribute(String theName) {
		return attributes.get(theName);
	}

	/**
	 * Gets a string collection, containing the values of all the attributes contained in this object.
	 * 
	 * @return a string collection of the attribute values
	 */
	public Collection<String> getAttributes() {
		return attributes.values();
	}

	/**
	 * Adds a GenericObjectConfigDataElement object as a child of this object.
	 * 
	 * @param value
	 *            the child object to be added
	 */
	public void addChild(GenericObjectConfigDataElement value) {
		children.add(value);
	}

	/**
	 * Removes a named object which is a child of this object.
	 * 
	 * @param theName
	 *            the name of the child object to be removed.
	 */
	public void deleteChild(String theName) {
		for (int i = 0; i < children.size(); i++) {
			GenericObjectConfigDataElement child = children.get(i);

			if (child.getName().equalsIgnoreCase(theName)) {
				children.remove(child);
			}
		}
	}

	/**
	 * Removes a child object reference from this object.
	 * 
	 * @param value
	 *            the reference of the child object to be removed.
	 */
	public void deleteChild(GenericObjectConfigDataElement value) {
		children.remove(value);
	}

	/**
	 * Gets a list of GenericObjectConfigDataElement objects which are children of this object.
	 * 
	 * @return a list of GenericObjectConfigDataElement of child objects.
	 */
	public List<GenericObjectConfigDataElement> getChildren() {
		return children;
	}

	/**
	 * implemented so GenericObjectConfigDataElement's can display information in the JTree GUI nodes which they are
	 * attached to.
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		// for top-level objects "name" contains a type-name
		String str = name;

		if (name.equalsIgnoreCase("name")) {
			str += ": " + getText();
		} else {
			// not a "name" element, so check children for "name".
			// see if any of its children is called "name"
			// - if so, its text contains that object's instance name
			List<GenericObjectConfigDataElement> g = getChildren();
			if (g.size() > 0) {
				for (int k = 0; k < g.size(); k++) {
					GenericObjectConfigDataElement child = g.get(k);
					if (child.getName().equalsIgnoreCase("name")) {
						str += ": " + child.getText();
					}
				}
			}
		}

		return str;
	}
}
