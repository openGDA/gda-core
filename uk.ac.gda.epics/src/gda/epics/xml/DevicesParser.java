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

package gda.epics.xml;

import gda.configuration.properties.LocalProperties;
import gda.epics.generated.Device;
import gda.epics.generated.Devices;
import gda.epics.generated.Section;
import gda.epics.generated.Type;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DevicesParser Class
 */
public class DevicesParser {
	private static final Logger logger = LoggerFactory.getLogger(DevicesParser.class);

	private static DevicesParser devicesParser = null;

	private static String _devicesFile;

	private BufferedReader _devicesReader;

	private Devices devices = null;

	private static String beamlineName;

	private ArrayList<Device> deviceList;

	/**
	 * Convenience method for constructing a DevicesParser object for the EPICS devices.xml file specified in the local
	 * properties file
	 * 
	 * @return the instance of the devices parser.
	 */
	public static DevicesParser createDevicesParser() {
		beamlineName = LocalProperties.get("gda.epics.beamline.name");
		DevicesParser._devicesFile = LocalProperties.get("gda.epics.devices.xml");
		return devicesParser = getInstance();
	}

	/**
	 * Convenience method for constructing a DevicesParser object, only for test suite programs!!!!!!!!!!!!!
	 * 
	 * @param devicesFile
	 *            fully qualified fileName to specifying in EPICS devices.xml file ServerImpl
	 * @return the instance of the devices parser.
	 */
	public static DevicesParser createDevicesParser(String devicesFile) {
		DevicesParser._devicesFile = devicesFile;
		return devicesParser = getInstance();
	}

	/**
	 * Convenience method for constructing a DevicesParser object with command line arguments
	 * 
	 * @param args
	 *            command line arguments
	 * @return the instance of the devices parser.
	 */
	public static DevicesParser createDevicesParser(String[] args) {
		DevicesParser._devicesFile = LocalProperties.get("gda.epics.devices.xml");
		parseArgs(args);
		return DevicesParser.getInstance();
	}

	/**
	 * Constructor
	 * 
	 * @return devicesFile fully qualified fileName to specifying in XML the EPICS devices to be parsed. If null is
	 *         specified the default types file from the classpath or jar file is used.
	 */
	private static synchronized DevicesParser getInstance() {
		if (devicesParser == null) {
			DevicesParser.devicesParser = new DevicesParser();
			devicesParser.configure();
		}
		return devicesParser;
	}

	private DevicesParser() {
	}

	private void configure() {
		try {
			if (_devicesFile == null) {
				_devicesFile = beamlineName + "-gda-devices.xml";
				_devicesReader = new BufferedReader(new FileReader(_devicesFile));
			} else {
				_devicesReader = new BufferedReader(new FileReader(_devicesFile));
			}

			devices = Devices.unmarshal(_devicesReader);
		} catch (MarshalException e) {
			logger.error("Unmarshal error: " + e.getMessage());
		} catch (ValidationException e) {
			logger.error("Validation error: " + e.getMessage());
		} catch (FileNotFoundException e) {
			logger.error("Error: The required file " + _devicesFile + "is NOT found.");
		}
		deviceList = getDeviceList();
	}

	/**
	 * Return all the sections in an array for a beamline.
	 * 
	 * @return the device array
	 */
	public Section[] getAllSections() {
		return devices.getSection();
	}

	/**
	 * @param index
	 * @return retrieve a specific section at the specified index in the array.
	 */
	public Section getSection(int index) {
		return devices.getSection(index);
	}

	/**
	 * @param sect
	 * @return the device array Return all the devices in an array in a section.
	 */
	public Device[] getDevicesInSection(Section sect) {
		return sect.getDevice();
	}

	/**
	 * @return Return an ArrayList of all devices in a beamline.
	 */
	public ArrayList<Device> getDeviceList() {
		ArrayList<Device> alldevice = new ArrayList<Device>();
		Section[] sections = getAllSections();

		for (Section section : sections) {
			Device[] devices = section.getDevice();
			for (Device device : devices) {
				alldevice.add(device);
			}
		}
		return alldevice;
	}

	/**
	 * @param section
	 * @param index
	 * @return retrieve a specific device at the specified index in the specified section.
	 */
	public Device getDevice(Section section, int index) {
		return section.getDevice(index);
	}

	/**
	 * return the device object for a given device name. All the devices on a beamline must be uniquely named.
	 * 
	 * @param name
	 *            the short name or GDA name of the device.
	 * @return the specific named device instance
	 */
	public Device getDeviceByName(String name) {
		Device namedDevice = null;
		boolean deviceFound = false;
		int i = 0;
		int j = 0;
		while (i < deviceList.size()) {
			if (deviceList.get(i).getName().equals(name)) {
				if (!deviceFound) {
					namedDevice = deviceList.get(i);
					deviceFound = true;
				} else {
					j++;
				}
			}
			i++;
		}

		if (namedDevice == null)
			logger.error("ERROR: Can't find the named device: " + name);

		if (j != 0)
			logger.error("ERROR: " + j + " duplicated device names are found in the devices XML file.");

		return namedDevice;
	}

	/**
	 * Return the Device object for a given Epics device name. All Epics Name for the devices on a single beamline must
	 * be unique.
	 * 
	 * @param epicsname
	 *            the unique EPICS name of the device.
	 * @return the specified device instance.
	 */
	public Device getDeviceByEpicsName(String epicsname) {
		Device namedDevice = null;
		boolean deviceFound = false;
		int i = 0;
		int j = 0;
		while (i < deviceList.size()) {
			if (deviceList.get(i).getEpicsname().equals(epicsname)) {
				if (!deviceFound) {
					namedDevice = deviceList.get(i);
					deviceFound = true;
				} else {
					j++;
				}
			}
			i++;
		}

		if (namedDevice == null)
			logger.error("ERROR: Can't find the named device: " + epicsname);

		if (j != 0)
			logger.error("ERROR: " + j + " duplicated device names are found in the devices XML file.");

		return namedDevice;
	}

	/**
	 * Returns the EPICS name of a device given its GDA name. Both EPICS name and GDA name must be uniquely defined for
	 * a specific device on a beamline.
	 * 
	 * @param deviceGDAname
	 *            the device name sepcified in GDA
	 * @return the EPICS name of this device
	 */
	public String getDeviceEpicsName(String deviceGDAname) {
		return getDeviceByName(deviceGDAname).getEpicsname().toString();
	}

	/**
	 * returns the EPICS name of a given device object.
	 * 
	 * @param device
	 *            the Device object
	 * @return the EPICS name for the device
	 */
	public String getDeviceEpicsName(Device device) {
		return device.getEpicsname().toString();
	}

	/**
	 * returns the GDA name of the device given its EPICS name. It assumes that both names are uniquely defined for a
	 * specific device on a beamline.
	 * 
	 * @param deviceEpicsname
	 *            the EPICS name of the device
	 * @return the GDA name of the specified EPICS name for the device
	 */
	public String getDeviceName(String deviceEpicsname) {
		return getDeviceByEpicsName(deviceEpicsname).getName().toString();
	}

	/**
	 * returns the GDA name of the specifid device object.
	 * 
	 * @param device
	 *            the specified device object
	 * @return the GDA name of the specified device object
	 */
	public String getDeviceName(Device device) {
		return device.getName().toString();
	}

	/**
	 * returns the description for a device specified by its name.
	 * 
	 * @param name
	 *            the GDA name or EPICS name of the device
	 * @return the description of the named device
	 */
	public String getDeviceDescrition(String name) {
		Device device = null;
		String description = null;
		if (name != null) {

			if ((device = getDeviceByName(name)) != null) {
				description = device.getDescription().toString();
			} else if ((device = getDeviceByEpicsName(name)) != null) {
				description = getDeviceByEpicsName(name).getDescription().toString();
			} else {
				logger.error("There is no device called " + name + " on this "
						+ "beamline specified in devices XML files.");
			}
		} else {
			logger.error("ERROR: A device name must be specified.");
		}
		return description;
	}

	/**
	 * returns the desription of the specified device.
	 * 
	 * @param device
	 *            the specified device object
	 * @return the description for this device
	 */
	public String getDeviceDescrition(Device device) {
		return device.getDescription().toString();
	}

	/**
	 * Returns an array of interface types for the specified device.
	 * 
	 * @param device -
	 *            the given device
	 * @return the interface type array for the device
	 */
	public Type[] getDeviceTypes(Device device) {
		return device.getType();
	}

	/**
	 * returns an array of interface types for the device specified by the name.
	 * 
	 * @param name
	 *            the GDA name of the device
	 * @return the interface type array for the device
	 */
	public Type[] getDeviceTypesByName(String name) {
		Device device = getDeviceByName(name);
		return device.getType();
	}

	/**
	 * returns an array of interface types for the device specified by its EPICS name.
	 * 
	 * @param epicsname
	 *            the EPICS name of the device
	 * @return the interface type array of this EPICS named device
	 */
	public Type[] getDeviceTypesByEpicsName(String epicsname) {
		Device device = getDeviceByEpicsName(epicsname);
		return device.getType();
	}

	/**
	 * returns the interface type specified at index for the device
	 * 
	 * @param device
	 *            the specified device
	 * @param index
	 *            the array index
	 * @return the interface type
	 */
	public Type getDeviceType(Device device, int index) {
		return device.getType(index);
	}

	/**
	 * returns the interface type specified at index for the device specified by the name.
	 * 
	 * @param name -
	 *            the GDA name of the device
	 * @param index -
	 *            the array index
	 * @return the interface type of the device at index
	 */
	public Type getDeviceTypeByName(String name, int index) {
		Device device = getDeviceByName(name);
		return device.getType(index);
	}

	/**
	 * returns the interface type specified at index for the device named by EPICS name.
	 * 
	 * @param epicsname
	 *            the EPICS name of the device
	 * @param index
	 *            the array index
	 * @return the interface type of the named device at index
	 */
	public Type getDeviceTypeByEpicsName(String epicsname, int index) {
		Device device = getDeviceByEpicsName(epicsname);
		return device.getType(index);
	}

	/**
	 * @param type
	 * @return device type name
	 */
	public String getDeviceTypeName(Type type) {
		return type.getName().toString();
	}

	/**
	 * returns the name of the interface type at index specified for the specified device.
	 * 
	 * @param device
	 *            the specified device.
	 * @param index
	 *            the array index
	 * @return the name value of the device interface type at index
	 */
	public String getDeviceTypeName(Device device, int index) {
		return device.getType(index).getName().toString();
	}

	/**
	 * returns the name of the interface type at index specified for the device specified by the name.
	 * 
	 * @param name
	 *            the GDA name or EPICS name of the device
	 * @param index
	 *            the array index
	 * @return the name value of the interface type of the named device at index
	 */
	public String getDeviceTypeName(String name, int index) {
		Device device = null;
		String deviceType = null;
		if (name != null) {
			if ((device = getDeviceByEpicsName(name)) != null) {
				deviceType = device.getType(index).getName().toString();
			} else if ((device = getDeviceByName(name)) != null) {
				deviceType = device.getType(index).getName().toString();
			} else {
				logger.error("There is no device called " + name + " on this " + "beamline specified in devices.xml");
			}
		} else {
			logger.error("You must specify a device name.");

		}
		return deviceType;
	}

	/**
	 * @param devicename
	 * @return device type names
	 */
	public String[] getDeviceTypeNames(String devicename) {
		Device device = null;
		if (devicename != null) {

			if ((device = getDeviceByName(devicename)) != null) {
				logger.debug("Device: " + devicename + " found");
			} else if ((device = getDeviceByEpicsName(devicename)) != null) {
				logger.debug("Device: " + devicename + " found");
			} else {
				logger.error("There is no device called " + devicename + " on this "
						+ "beamline specified in devices.xml");
			}
		} else {
			logger.error("You must specify a device name.");

		}

		// if device found
		if (device != null) {
			String[] typenames = new String[device.getTypeCount()];
			for (int i = 0; i < device.getTypeCount(); i++) {
				typenames[i] = device.getType(i).getName().toString();
			}
			return typenames;
		}
		return null;
	}

	/**
	 * List all the devices in a beamline on the console.
	 */
	public void printDeviceList() {
		Section[] sections = devices.getSection();
		for (int i = 0; i < devices.getSectionCount(); i++) {
			logger.debug("SECTION " + i + ": " + sections[i].getName());
			System.out.print("\tcontains " + sections[i].getDeviceCount() + " devices");
			for (int j = 0; j < sections[i].getDeviceCount(); j++) {
				logger.debug("DEVICE " + j + ": " + sections[i].getDevice(j).getName() + " : "
						+ sections[i].getDevice(j).getEpicsname() + " : " + sections[i].getDevice(j).getDescription());
				System.out.print("\tcontains " + sections[i].getDevice(j).getTypeCount() + " interfaces");
				for (int k = 0; k < sections[i].getDevice(j).getTypeCount(); k++)
					System.out.println(" : " + sections[i].getDevice(j).getType(j).getName());
			}

		}
	}

	private static void parseArgs(String[] args) {
		int argno = 0;
		int argc = args.length;
		while (argno < argc) {
			if (args[argno].equals("-f") && (argno + 1 < argc)) {
				_devicesFile = args[++argno];
			} else if (args[argno].equals("-h") || args[argno].equals("--help")) {
				usage();
			}
			argno++;
		}
	}

	/**
	 * Displays a message on the console describing the correct usage of the main program and all of the optional
	 * parameters.
	 */
	public static void usage() {
		logger.debug("Usage: ObjectServer [options]");
		logger.debug("Options:");
		logger.debug("-f <xml filename>");
		logger.debug("-debug      Level 1 debug information");
		logger.debug("-d[level]   debug with levels 1,2,3 or 4");
		logger.debug("-h, --help  Display this help message.");
		System.exit(0);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		DevicesParser.createDevicesParser();
		devicesParser.printDeviceList();
	}

}
