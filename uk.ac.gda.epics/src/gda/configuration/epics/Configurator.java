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

package gda.configuration.epics;

import gda.configuration.properties.LocalProperties;

import java.io.FileNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;

/**
 * EPICS interface configuration logic.
 */
public class Configurator {
	private static final Logger logger = LoggerFactory.getLogger(Configurator.class);
	/**
	 * Property name for the EPICS configuration schema.
	 */
	public final static String INTERFACE_CONFIGURATION_SCHEMA_KEY = "gda.epics.interface.schema";

	/**
	 * Property name for the EPICS XML configuration file.
	 */
	public final static String INTERFACE_CONFIGURATION_XML_KEY = "gda.epics.interface.xml";
	
	/**
	 * Property name for the unchecked EPICS XML configuration file.
	 */
	public final static String INTERFACE_UNCHECKED_CONFIGURATION_XML_KEY = INTERFACE_CONFIGURATION_XML_KEY + ".unchecked";

	private static EpicsConfiguration instance;
	

	/**
	 * Get configuration for device. *
	 * 
	 * @param <T>
	 * @param name
	 *            name of the device (configuration id).
	 * @param clazz
	 *            expected configuration Castor class (e.g. <code>gda.epics.interfaces.SimpleMotor.class</code>).
	 * @return configuration instance of class <code>clazz</code>.
	 * @throws ConfigurationNotFoundException
	 */
	public static <T> T getConfiguration(String name, Class<T> clazz) throws ConfigurationNotFoundException {
		if( instance == null){
			// Load GDA_EPICS_interface configuration file if present.
			String interfaceConfigFile = LocalProperties.get(Configurator.INTERFACE_CONFIGURATION_XML_KEY);
			
			if (interfaceConfigFile == null) {
				throw new ConfigurationNotFoundException(String.format(
					"Could not read EPICS configuration: %s is not set",
					Configurator.INTERFACE_CONFIGURATION_XML_KEY));
			}
			
			try {
				logger.info("Configure EPICS interface");
				instance = new EpicsConfiguration(interfaceConfigFile);
				logger.info("EPICS Interface Configuration Completed");
			} catch (MarshalException e) {
				logger.error("GDA_EPICS_Interface marshal Error " + interfaceConfigFile, e);
			} catch (ValidationException e) {
				logger.error("Invalid GDA_EPICS_Interface XML file " + interfaceConfigFile, e);
			} catch (FileNotFoundException e) {
				logger.error("Can not find file " + interfaceConfigFile, e);
			}
			
		}
		return instance.getConfiguration(name, clazz);
	}

	/**
	 * @param <T>
	 * @param name
	 * @return configuration
	 * @throws ConfigurationNotFoundException
	 */
	public static <T> T getConfiguration(String name) throws ConfigurationNotFoundException {
		return instance.getConfiguration(name);
	}

	// simple test (examples)
	/**
	 * @param args
	 */
	public static void main(String[] args) {

		// hardcoded for convenience (for test only)
		System.setProperty(INTERFACE_CONFIGURATION_XML_KEY, LocalProperties.get(INTERFACE_CONFIGURATION_XML_KEY));
		// System.setProperty("gda.epics.interface.schema",
		// LocalProperties.get("gda.epics.interface.schema"));

		// exmaple of usage
		gda.epics.interfaces.SimpleMotorType motorConfig;
		gda.epics.interfaces.SimpleMotorType motorConfig1;
		try {
			motorConfig = Configurator.getConfiguration("MAC4.A", gda.epics.interfaces.SimpleMotorType.class);
			motorConfig1 = Configurator.getConfiguration("MAC5.2A");

			System.out.println(motorConfig);
			String pv = motorConfig.getRECORD().getPv();
			System.out.println(motorConfig1);
			String pv1 = motorConfig1.getRECORD().getPv();

			System.out.printf("%1$30s%n%2$30s %n", pv, pv1);
		} catch (ConfigurationNotFoundException e) {
			System.out.println(e);
		}

		// not found example
		try {
			Configurator.getConfiguration("invalid", gda.epics.interfaces.SimpleMotorType.class);
		} catch (ConfigurationNotFoundException cnfe) {
			System.out.println(cnfe);
		}

		// wrong class name example
		try {
			Configurator.getConfiguration("MAC4.E8", gda.epics.interfaces.SimpleMotorType.class);
		} catch (ClassCastException cce) {
			System.out.println(cce);
		} catch (ConfigurationNotFoundException e) {
			System.out.println(e);
		}
		printSortedKeys();
	}

	private static void printSortedKeys() {
		instance.printSortedKeys();
	}

}
