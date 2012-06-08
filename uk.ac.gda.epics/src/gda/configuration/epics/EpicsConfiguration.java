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

import gda.epics.interfaces.Devices;
import gda.epics.interfaces.DevicesChoice;
import gda.epics.interfaces.DevicesChoiceItem;
import gda.epics.interfaces.DevicesItem;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * EPICS interface configuration logic.
 */
public class EpicsConfiguration {

	private static final Logger logger = LoggerFactory.getLogger(EpicsConfiguration.class);

	/**
	 * Cached device configuration map.
	 */
	protected Map<String, Object> deviceMap = new HashMap<String, Object>();

	/**
	 * Creates a new EPICS configuration object, reading the specified file.
	 * 
	 * @param file the EPICS interface file
	 * 
	 * @throws FileNotFoundException
	 * @throws MarshalException
	 * @throws ValidationException
	 */
	public EpicsConfiguration(String file) throws FileNotFoundException, MarshalException, ValidationException {
		initializeConfiguration(file);
	}
	
	/**
	 * Reads interface configuration XML and generates hash map for fast look-ups. Methods requires schema to have
	 * <code>devices</code> as root element and each device element has to have a <code>name</code> attribute as
	 * identifier.
	 * 
	 * @param xmlURI the EPICS interface file
	 * 
	 * @throws FileNotFoundException
	 * @throws MarshalException
	 * @throws ValidationException
	 */
	public void initializeConfiguration(String xmlURI) throws FileNotFoundException, MarshalException, ValidationException {
		FileReader reader = new FileReader(xmlURI);
		Devices devices = Devices.unmarshalDevices(reader);

		DevicesItem[] devicesItems = devices.getDevicesItem();
		for (DevicesItem deviceItem : devicesItems) {
			DevicesChoice devicesChoice = deviceItem.getDevicesChoice();

			DevicesChoiceItem[] devicesChoiceItems = devicesChoice.getDevicesChoiceItem();
			for (DevicesChoiceItem deviceChoiceItem : devicesChoiceItems) {
				// we avoid using castor Descriptor classes here and use
				// introspection...
				Method[] methods = DevicesChoiceItem.class.getMethods();
				for (Method method : methods) {
					// find sequences
					final String methodName = method.getName();
					if (methodName.startsWith("get") && !methodName.equals("getClass")
							&& method.getParameterTypes().length == 0) {
						// get sequence
						try {
							Object element = method.invoke(deviceChoiceItem, (Object[]) null);
							if (element != null) {
								// we require getName() method
								Class<?> elementClass = element.getClass();
								Method nameMethod = elementClass.getMethod("getName", (Class[]) null);

								// add element
								String name = nameMethod.invoke(element, (Object[]) null).toString();
								deviceMap.put(name, element);

							}
						} catch (Throwable th) {
							// failed to access the field, report and keep on
							// trying...
							th.printStackTrace();
						}
					}
				}
			}
		}
		logger.info("EPICS interface parsing completed");

	}

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
	public <T> T getConfiguration(String name, Class<T> clazz) throws ConfigurationNotFoundException {
		Object configuration = deviceMap.get(name);
		if (configuration == null)
			throw new ConfigurationNotFoundException("No configuration found for '" + name + "'.");

		try {
			return clazz.cast(configuration);
		} catch (ClassCastException cce) {
			// exception is encapsulated and rethrown since JVM provides
			// non-descriptive exception
			throw new ClassCastException("failed to cast " + configuration.getClass().getName() + " to "
					+ clazz.getName());
		}
	}

	/**
	 * @param <T>
	 * @param name
	 * @return configuration
	 * @throws ConfigurationNotFoundException
	 */
	@SuppressWarnings("unchecked")
	public <T> T getConfiguration(String name) throws ConfigurationNotFoundException {
		Object configuration = deviceMap.get(name);
		if (configuration == null)
			throw new ConfigurationNotFoundException("No configuration found for '" + name + "'.");
		Class<T> clazz = (Class<T>) configuration.getClass();
		try {
			return clazz.cast(configuration);
		} catch (ClassCastException cce) {
			// exception is encapsulated and rethrown since JVM provides
			// non-descriptive exception
			throw new ClassCastException("failed to cast " + configuration.getClass().getName() + " to "
					+ clazz.getName());
		}
	}

	/**
	 * Prints out all devices.
	 */
	public void printSortedKeys() {
		int i = 0;
		String[] keys = new String[deviceMap.keySet().size()];
		// String[] classname = new String[deviceMap.keySet().size()];

		for (String key : deviceMap.keySet()) {
			keys[i] = key;
			i++;
		}

		System.out.println("\n");
		java.util.Arrays.sort(keys);
		for (int j = 0; j < deviceMap.size(); j++) {
			System.out.printf("%1$-20s %2$-50s %n", keys[j], deviceMap.get(keys[j]).getClass().toString());
		}
	}

}
