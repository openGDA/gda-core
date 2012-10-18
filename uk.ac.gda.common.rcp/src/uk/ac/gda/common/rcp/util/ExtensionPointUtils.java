/*-
 * Copyright © 2012 Diamond Light Source Ltd.
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

package uk.ac.gda.common.rcp.util;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;

public class ExtensionPointUtils {
	/**
	 * @param extensionPointString
	 * @return list of configuration elements that use the specified extension point
	 */
	public static ArrayList<IConfigurationElement> getElementsFromExtensionPoint(String extensionPointString) {
		ArrayList<IConfigurationElement> configurationList = new ArrayList<IConfigurationElement>();
		IConfigurationElement[] elems = Platform.getExtensionRegistry().getConfigurationElementsFor(extensionPointString);
		for (IConfigurationElement element : elems) {
			configurationList.add(element);
		}
		return configurationList;
	}
	
	/**
	 * Get elements that have a matching element name and attribute as specified by the arguments
	 * @param extensionPointString
	 * @param extensionElementName
	 * @param attributeName
	 * @param attributeValueString
	 * @return list of configuration elements
	 */
	public static List<IConfigurationElement> getAttributeFilteredElementsFromExtensionPoint(String extensionPointString, String extensionElementName, String attributeName, String attributeValueString) {
		ArrayList<IConfigurationElement> configurationList = new ArrayList<IConfigurationElement>();
		List<IConfigurationElement> elements = getElementsFromExtensionPoint(extensionPointString);
		for (IConfigurationElement e : elements) {
			if ((extensionElementName != null) && extensionElementName.compareTo(e.getName())!=0) {
				continue;
			}
			String actualAttributeValue = e.getAttribute(attributeName);
			if (actualAttributeValue.compareTo(attributeValueString)!=0) {
				continue;
			}
			configurationList.add(e);
		}
		return configurationList;
	}

	/**
	 * Get elements that have a matching element name and attribute as specified by the arguments
	 * @param extensionPointString
	 * @param attributeName
	 * @param attributeValueString
	 * @return list of configuration elements
	 */
	public static List<IConfigurationElement> getAttributeFilteredElementsFromExtensionPoint(String extensionPointString, String attributeName, String attributeValueString) {
		return getAttributeFilteredElementsFromExtensionPoint(extensionPointString, null, attributeName, attributeValueString);
	}
}