/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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

package uk.ac.gda.client.properties;

import gda.configuration.properties.LocalProperties;
import uk.ac.gda.ui.tool.ClientMessages;
import uk.ac.gda.ui.tool.ClientMessagesUtility;

/**
 * Utility methods to parse properties as in <code>uk.ac.diamond.daq.client.gui.camera.CameraHelper</code> or
 * <code>uk.ac.diamond.daq.mapping.ui.properties.DetectorHelper</code>
 *
 * @author Maurizio Nagni
 */
public final class ClientPropertiesHelper {
	public static final String PROPERTY_FORMAT = "%s.%s.%s";
	public static final String SIMPLE_FORMAT = "%s.%s";

	private ClientPropertiesHelper() {
	}

	/**
	 * Extracts properties formatted like "PREFIX.INDEX.name"
	 *
	 * @param index
	 *            the element index
	 * @return element name
	 */
	public static String getNameProperty(String prefix, int index) {
		return LocalProperties.get(formatPropertyKey(prefix, index, "name"),
				ClientMessagesUtility.getMessage(ClientMessages.NOT_AVAILABLE));
	}

	/**
	 * Extracts properties formatted like "PREFIX.INDEX.id"
	 *
	 * @param index
	 *            the element index
	 * @return the element id
	 */
	public static String getId(String prefix, int index) {
		return LocalProperties.get(formatPropertyKey(prefix, index, "id"),
				ClientMessagesUtility.getMessage(ClientMessages.NOT_AVAILABLE));
	}

	/**
	 * Extracts properties formatted like "PREFIX.INDEX"
	 *
	 * @param index
	 *            the element index
	 * @return the element bean configuration name
	 */
	public static String getConfigurationBeanProperty(String prefix, int index) {
		return LocalProperties.get(String.format(SIMPLE_FORMAT, prefix, index), null);
	}

	/**
	 * Assemble a string formatted like something like "PREFIX.INDEX.PROPERTY"
	 *
	 * @param prefix
	 *            a string identifying a property
	 * @param index
	 *            an index identifying the prefix index
	 * @param property
	 *            a subproperty of the prefix
	 * @return the property key
	 */
	public static String formatPropertyKey(String prefix, int index, String property) {
		return String.format(PROPERTY_FORMAT, prefix, index, property);
	}
}
