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

package gda.epics.interfaceSpec;

import gda.configuration.epics.Configurator;
import gda.configuration.properties.LocalProperties;
import uk.ac.gda.common.rcp.util.EclipseUtils;

import java.util.List;
import java.util.Vector;
import java.io.IOException;

/**
 * GDAEpicsInterfaceReader Class
 */
public class GDAEpicsInterfaceReader {
	/**
	 *  Name of schema used to validate Configurator.INTERFACE_CONFIGURATION_XML_KEY
	 */
	public static final String GDA_EPICS_INTERFACE_SCHEMA = "gda.epics.interface.schema";
	public static final String DEFAULT_GDA_EPICS_INTERFACE_SCHEMA = getDefaultSchemaPath();

	private Reader uncheckedReader = null;
	private Reader checkedReader = null;
	private static GDAEpicsInterfaceReader instance;

	private static String getDefaultSchemaPath() {
		try {
			return EclipseUtils.resolveBundleFile("uk.ac.gda.epics/schema/genericBeamlineSchema.xsd").getAbsolutePath();
		} catch (IOException e) {
			throw new RuntimeException("Epics default schema missing", e);
		}
	}

	private static GDAEpicsInterfaceReader getInstance() throws InterfaceException {
		if (instance == null) {
			instance = new GDAEpicsInterfaceReader();
		}
		return instance;
	}

	private GDAEpicsInterfaceReader() throws InterfaceException {
		if (uncheckedReader == null) {
			String xmlFile = LocalProperties.get(Configurator.INTERFACE_UNCHECKED_CONFIGURATION_XML_KEY);
			if (xmlFile != null) {
				uncheckedReader = new SimpleReader(xmlFile, null);
			}
		}
		if (checkedReader == null) {
			String xmlFile = LocalProperties.get(Configurator.INTERFACE_CONFIGURATION_XML_KEY);
			if (xmlFile != null) {
				checkedReader = new SimpleReader(xmlFile, LocalProperties.get(GDAEpicsInterfaceReader.GDA_EPICS_INTERFACE_SCHEMA, GDAEpicsInterfaceReader.DEFAULT_GDA_EPICS_INTERFACE_SCHEMA));
			}
		}
	}

	/**
	 * @param deviceType
	 * @param deviceName
	 * @return device
	 * @throws InterfaceException
	 */
	public static Device getDeviceFromType(String deviceType, String deviceName) throws InterfaceException {
		GDAEpicsInterfaceReader reader = getInstance();
		Device dev = reader.uncheckedReader != null ? reader.uncheckedReader.getDevice(deviceType, deviceName) : null;
		if (dev != null)
			return dev;
		dev = reader.checkedReader != null ? reader.checkedReader.getDevice(deviceType, deviceName) : null;
		if (dev == null) {
			throw new InterfaceException(
					"GDAEpicsInterfaceReader.getDeviceFromType:Error. DOM does not contain any items with deviceType="
							+ deviceType + " and deviceName=" + deviceName, null);
		}
		return dev;
	}

	/**
	 * @return list of device names
	 * @throws InterfaceException
	 */
	public static List<String> getAllDeviceNames() throws InterfaceException {
		GDAEpicsInterfaceReader reader = getInstance();
		List<String> allNames = reader.uncheckedReader != null ? reader.uncheckedReader.getAllDeviceNames()
				: new Vector<String>();
		List<String> allNames_checked = reader.checkedReader != null ? reader.checkedReader.getAllDeviceNames()
				: new Vector<String>();

		for (String s : allNames_checked) {
			if (!allNames.contains(s))
				allNames.add(s);
		}
		return allNames;
	}

	/**
	 * @param deviceName
	 * @return String
	 * @throws InterfaceException
	 */
	public static String getPVFromSimplePVType(String deviceName) throws InterfaceException {
		gda.epics.interfaceSpec.Device device = GDAEpicsInterfaceReader.getDeviceFromType(Xml.simplePvType_type_name,
				deviceName);
		return device.getField("RECORD").getPV();
	}

	/**
	 * @param deviceName
	 * @return String
	 * @throws InterfaceException
	 */
	public static String getPVFromSimpleMotor(String deviceName) throws InterfaceException {
		gda.epics.interfaceSpec.Device device = GDAEpicsInterfaceReader.getDeviceFromType(Xml.simpleMotor_type_name,
				deviceName);
		return device.getField("RECORD").getPV();
	}

	/**
	 * @param deviceName
	 * @return String
	 * @throws InterfaceException
	 */
	public static String getPVFromSimpleScaler(String deviceName) throws InterfaceException {
		gda.epics.interfaceSpec.Device device = GDAEpicsInterfaceReader.getDeviceFromType(Xml.simpleScaler_type_name,
				deviceName);
		return device.getField("RECORD").getPV();
	}
}
