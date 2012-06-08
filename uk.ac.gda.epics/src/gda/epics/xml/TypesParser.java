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
import gda.epics.generated.Interface;
import gda.epics.generated.Subsystem;
import gda.epics.generated.Types;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TypesParser Class
 */
public class TypesParser {
	private static final Logger logger = LoggerFactory.getLogger(TypesParser.class);

	private static TypesParser typesParser = null;

	private static String _typesFile;

	private BufferedReader _typesReader;

	private Types types = null;

	private static String beamlineName;

	/**
	 * Convenience method for constructing a DevicesParser object for the EPICS devices.xml file specified in the local
	 * properties file
	 * 
	 * @return the instance of the devices parser.
	 */
	public static TypesParser createTypesParser() {
		beamlineName = LocalProperties.get("gda.epics.beamline.name");
		TypesParser._typesFile = LocalProperties.get("gda.epics.types.xml");
		return typesParser = getInstance();
	}

	/**
	 * Convenience method for constructing a DevicesParser object, only for test suite programs!!!!!!!!!!!!!
	 * 
	 * @param devicesFile
	 *            fully qualified fileName to specifying in EPICS devices.xml file ServerImpl
	 * @return the instance of the devices parser.
	 */
	public static TypesParser createTypesParser(String devicesFile) {
		TypesParser._typesFile = devicesFile;
		return typesParser = getInstance();
	}

	/**
	 * Convenience method for constructing a DevicesParser object with command line arguments
	 * 
	 * @param args
	 *            command line arguments
	 * @return the instance of the devices parser.
	 */
	public static TypesParser createTypesParser(String[] args) {
		TypesParser._typesFile = LocalProperties.get("gda.epics.types.xml");
		parseArgs(args);
		return TypesParser.getInstance();
	}

	/**
	 * Constructor
	 * 
	 * @return devicesFile fully qualified fileName to specifying in XML the EPICS devices to be parsed. If null is
	 *         specified the default types file from the classpath or jar file is used.
	 */
	private static synchronized TypesParser getInstance() {
		if (typesParser == null) {
			TypesParser.typesParser = new TypesParser();
			typesParser.configure();
		}
		return typesParser;
	}

	private TypesParser() {
	}

	private void configure() {
		try {
			if (_typesFile == null) {
				_typesFile = beamlineName + "-gda-types.xml";
				_typesReader = new BufferedReader(new FileReader(_typesFile));
			} else {
				_typesReader = new BufferedReader(new FileReader(_typesFile));
			}

			types = Types.unmarshal(_typesReader);
		} catch (MarshalException e) {
			logger.error("Unmarshal error: " + e.getMessage());
		} catch (ValidationException e) {
			logger.error("Validation error: " + e.getMessage());
		} catch (FileNotFoundException e) {
			logger.error("Error: The required file " + _typesFile + "is NOT found.");
		}
	}

	/**
	 * @return subsystem list
	 */
	public ArrayList<Subsystem> getSubsystemList() {
		ArrayList<Subsystem> allsubsystem = new ArrayList<Subsystem>();
		Interface[] interfaces = getAllInterfaces();

		for (Interface itf : interfaces) {
			Subsystem[] subsystems = itf.getSubsystem();
			for (Subsystem subsystem : subsystems) {
				allsubsystem.add(subsystem);
			}
		}
		return allsubsystem;
	}

	/**
	 * returns all interfaces in an array that contained in the types.xml file.
	 * 
	 * @return the EPICS interface array
	 */
	public Interface[] getAllInterfaces() {
		return types.getInterface();
	}

	/**
	 * returns a specific interface at the specified index from the types.xml file.
	 * 
	 * @param index
	 *            the array idex of the interface
	 * @return the interface at index
	 */
	public Interface getInterface(int index) {
		return types.getInterface(index);
	}

	/**
	 * returns the interface object of the specified type name
	 * 
	 * @param typename
	 *            the type-name for the interface
	 * @return the interface object
	 */
	public Interface getInterfaceByName(String typename) {
		Interface namedInterface = null;
		// STOPPED HERE - CONTIUE TOMORROW
		Interface[] interfac = types.getInterface();

		int i = 0;
		while (namedInterface == null && i < types.getInterfaceCount()) {
			if (interfac[i].getName().equals(typename)) {
				namedInterface = interfac[i];

			}
			i++;
		}
		if (namedInterface == null)
			logger.error("Can't find the named Interface: " + typename);

		return namedInterface;
	}

	/**
	 * returns the name value of a given interface object
	 * 
	 * @param itf
	 *            the specified interface object
	 * @return the value of the name attribute
	 */
	public String getInterfaceName(Interface itf) {
		return itf.getName().toString();
	}

	/**
	 * returns the label value of a given interface object
	 * 
	 * @param itf
	 *            the specified interface
	 * @return the value of the label attribute
	 */
	public String getInterfaceLabel(Interface itf) {
		return itf.getLabel().toString();
	}

	/**
	 * returns the type value of a given interface object
	 * 
	 * @param itf
	 *            the specified interface
	 * @return the value of the type attribute
	 */
	public String getInterfaceType(Interface itf) {
		return itf.getType().toString();
	}

	/**
	 * return the description value of a given interface object
	 * 
	 * @param itf
	 *            the specified interface
	 * @return the value of the description attribute in the interface
	 */
	public String getInterfaceDescription(Interface itf) {
		return itf.getDescription().toString();
	}

	/**
	 * returns the label value of a given interface name
	 * 
	 * @param itfname
	 *            the name of the interface
	 * @return the value of the label attribute
	 */
	public String getInterfaceLabel(String itfname) {
		return getInterfaceByName(itfname).getLabel().toString();
	}

	/**
	 * returns the type value of a given interface name
	 * 
	 * @param itfname
	 *            the name of the interface
	 * @return the value of the type attribute
	 */
	public String getInterfaceType(String itfname) {
		return getInterfaceByName(itfname).getType().toString();
	}

	/**
	 * returns the description value of a given interface name
	 * 
	 * @param itfname
	 *            the name of the interface
	 * @return the value of the description attribute
	 */
	public String getInterfaceDescription(String itfname) {
		return getInterfaceByName(itfname).getDescription().toString();
	}

	/**
	 * returns a list of subsystem in an array of a given interface object
	 * 
	 * @param itf
	 *            the sepcified interface object
	 * @return an array of the subsystem the given interface contains
	 */
	public Subsystem[] getInterfaceSubsystems(Interface itf) {
		return itf.getSubsystem();
	}

	/**
	 * returns a list of subsystem in an array of a given interface name
	 * 
	 * @param itfname
	 *            the sepcified interface name
	 * @return an array of the subsystem the given interface contains
	 */
	public Subsystem[] getInterfaceSubsystems(String itfname) {
		return getInterfaceByName(itfname).getSubsystem();
	}

	/**
	 * returns a subsystem object at specified index of a given interface object
	 * 
	 * @param itf
	 *            the sepcified interface object
	 * @param index
	 *            the array index
	 * @return the subsystem object
	 */
	public Subsystem getInterfaceSubsystem(Interface itf, int index) {
		return itf.getSubsystem(index);
	}

	/**
	 * returns a subsystem object at specified index of a given interface name
	 * 
	 * @param name
	 *            the sepcified interface name
	 * @param index
	 *            the array index
	 * @return the subsystem object
	 */
	public Subsystem getInterfaceSubsystem(String name, int index) {
		return getInterfaceByName(name).getSubsystem(index);
	}

	/**
	 * returns the name of the subsystem at specified index of a given interface object
	 * 
	 * @param itf
	 *            the sepcified interface object
	 * @param index
	 *            the array index
	 * @return the name of the subsystem
	 */
	public String getInterfaceSubsystemName(Interface itf, int index) {
		return itf.getSubsystem(index).getName().toString();
	}

	/**
	 * returns the name of the subsystem at specified index of a given interface name
	 * 
	 * @param itfname
	 *            the sepcified interface name
	 * @param index
	 *            the array index
	 * @return the name value of the subsystem
	 */
	public String getInterfaceSubsystemName(String itfname, int index) {
		return getInterfaceByName(itfname).getSubsystem(index).getName().toString();
	}

	/**
	 * returns an array of names of all the subsystem contained in a given interface object
	 * 
	 * @param itf
	 *            the interface object
	 * @return the array of names of all subsystems
	 */
	public String[] getInterfaceSubsystemsNames(Interface itf) {
		String[] subsystemNames = new String[itf.getSubsystemCount()];
		for (int i = 0; i < itf.getSubsystemCount(); i++) {
			subsystemNames[i] = itf.getSubsystem(i).getName().toString();
		}
		return subsystemNames;
	}

	/**
	 * returns an array of names of all the subsystem contained in a given interface name
	 * 
	 * @param typename
	 *            the interface name
	 * @return the array of names of all subsystems
	 */
	public String[] getInterfaceSubsystemsNames(String typename) {
		Interface itf = getInterfaceByName(typename);
		return getInterfaceSubsystemsNames(itf);
	}

	/**
	 * returns the Epics record name of the subsystem at specified index of a given interface object
	 * 
	 * @param itf
	 *            the sepcified interface object
	 * @param index
	 *            the array index
	 * @return the EPICS record name of the subsystem
	 */
	public String getInterfaceSubsystemPV(Interface itf, int index) {
		return itf.getSubsystem(index).getPv().toString();
	}

	/**
	 * returns the Epics record name of the subsystem at specified index of a given interface name
	 * 
	 * @param typename
	 *            the sepcified interface name
	 * @param index
	 *            the array index
	 * @return the EPICS record name of the subsystem
	 */
	public String getInterfaceSubsystemPV(String typename, int index) {
		return getInterfaceByName(typename).getSubsystem(index).getPv().toString();
	}

	/**
	 * returns an array of EPICS record names of all the subsystem contained in a given interface object
	 * 
	 * @param itf
	 *            the interface object
	 * @return the array of EPICS record names of all subsystems
	 */
	public String[] getInterfaceSubsystemsPVs(Interface itf) {
		String[] subsystemPvs = new String[itf.getSubsystemCount()];
		for (int i = 0; i < itf.getSubsystemCount(); i++) {
			subsystemPvs[i] = itf.getSubsystem(i).getPv().toString();
		}
		return subsystemPvs;
	}

	/**
	 * returns an array of EPICS record names of all the subsystem contained in a given interface name
	 * 
	 * @param typename
	 *            the interface name
	 * @return the array of EPICS record names of all subsystems
	 */
	public String[] getInterfaceSubsystemsPVs(String typename) {
		Interface itf = getInterfaceByName(typename);
		return getInterfaceSubsystemsPVs(itf);
	}

	/**
	 * returns the value of the subsystem type at specified index of a given interface object
	 * 
	 * @param itf
	 *            the sepcified interface object
	 * @param index
	 *            the array index
	 * @return the value of the subsystem type
	 */
	public String getInterfaceSubsystemType(Interface itf, int index) {
		return itf.getSubsystem(index).getType().toString();
	}

	/**
	 * returns the value of the subsystem type at specified index of a given interface name
	 * 
	 * @param typename
	 *            the sepcified interface name
	 * @param index
	 *            the array index
	 * @return the value of the subsystem type
	 */
	public String getInterfaceSubsystemType(String typename, int index) {
		return getInterfaceByName(typename).getSubsystem(index).getType().toString();
	}

	/**
	 * returns an array of subsystem types contained in a given interface object
	 * 
	 * @param itf
	 *            the interface object
	 * @return the array of values of all subsystem types
	 */
	public String[] getInterfaceSubsystemsTypes(Interface itf) {
		String[] subsystemTypes = new String[itf.getSubsystemCount()];
		for (int i = 0; i < itf.getSubsystemCount(); i++) {
			subsystemTypes[i] = itf.getSubsystem(i).getType().toString();
		}
		return subsystemTypes;
	}

	/**
	 * returns an array of subsystem types contained in a given interface name
	 * 
	 * @param typename
	 *            the interface name
	 * @return the array of values of all subsystem types
	 */
	public String[] getInterfaceSubsystemsTypes(String typename) {
		Interface itf = getInterfaceByName(typename);
		return getInterfaceSubsystemsTypes(itf);
	}

	/**
	 * returns the value of the subsystem description at specified index of a given interface object
	 * 
	 * @param itf
	 *            the sepcified interface object
	 * @param index
	 *            the array index
	 * @return the value of the subsystem description
	 */
	public String getInterfaceSubsystemDescription(Interface itf, int index) {
		return itf.getSubsystem(index).getDescription().toString();
	}

	/**
	 * returns the value of the subsystem description at specified index of a given interface name
	 * 
	 * @param typename
	 *            the sepcified interface name
	 * @param index
	 *            the array index
	 * @return the value of the subsystem description
	 */
	public String getInterfaceSubsystemDescription(String typename, int index) {
		return getInterfaceByName(typename).getSubsystem(index).getDescription().toString();
	}

	/**
	 * returns an array of values of all subsystem descriptions of a given interface object
	 * 
	 * @param itf
	 *            the sepcified interface object
	 * @return the array of values of the subsystem description
	 */
	public String[] getInterfaceSubsystemsDescriptions(Interface itf) {
		String[] subsystemDescriptions = new String[itf.getSubsystemCount()];
		for (int i = 0; i < itf.getSubsystemCount(); i++) {
			subsystemDescriptions[i] = itf.getSubsystem(i).getDescription().toString();
		}
		return subsystemDescriptions;
	}

	/**
	 * returns the array of values of the subsystem descriptions of a given interface name
	 * 
	 * @param typename
	 *            the sepcified interface name
	 * @return the array of values of the subsystem description
	 */
	public String[] getInterfaceSubsystemsDescriptions(String typename) {
		Interface itf = getInterfaceByName(typename);
		return getInterfaceSubsystemsDescriptions(itf);
	}

	/**
	 * returns the name of the given subsystem
	 * 
	 * @param subsys
	 *            the specified subsystem
	 * @return the name of the subsystem
	 */
	public String getSubsystemName(Subsystem subsys) {
		return subsys.getName().toString();
	}

	/**
	 * returns the EPICS record name of the given subsystem
	 * 
	 * @param subsys
	 *            the specified subsystem
	 * @return the EPICS record name of the subsystem
	 */
	public String getSubsystemPV(Subsystem subsys) {
		return subsys.getPv().toString();
	}

	/**
	 * returns the type name of the given subsystem
	 * 
	 * @param subsys
	 *            the specified subsystem
	 * @return the name of the subsystem type
	 */
	public String getSubsystemType(Subsystem subsys) {
		return subsys.getType().toString();
	}

	/**
	 * returns the description of the given subsystem
	 * 
	 * @param subsys
	 *            the specified subsystem
	 * @return the description string of the subsystem
	 */
	public String getSubsystemDescription(Subsystem subsys) {
		return subsys.getDescription().toString();
	}

	/**
	 * returns the subsystem object for a given subsystem name and interface name
	 * 
	 * @param subsysname
	 *            the name of teh subsystem
	 * @param itfname
	 *            the name of the interface
	 * @return the subsystem object
	 */
	public Subsystem getSubsystemByName(String subsysname, String itfname) {
		Subsystem namedSubsystem = null;

		Subsystem[] subsystems = getInterfaceByName(itfname).getSubsystem();

		int i = 0;
		while (namedSubsystem == null && i < subsystems.length) {

			if (subsystems[i].getName().equals(subsysname)) {
				namedSubsystem = subsystems[i];
			}
			i++;
		}
		/*
		 * if (namedSubsystem == null) logger.debug("Can't find the named subsystem: " + subsysname + " in the named
		 * interface: " + itfname);
		 */

		return namedSubsystem;
	}

	/**
	 * 
	 */
	public void printTypeList() {
		Interface type[] = types.getInterface();
		for (int i = 0; i < types.getInterfaceCount(); i++) {
			logger.debug("interface " + i + ":\n\t\t" + type[i].getName() + ",\t " + type[i].getLabel() + ",\t "
					+ type[i].getType() + ",\t " + type[i].getDescription());
			logger.debug("\t\tcontains " + type[i].getSubsystemCount() + " subsystems:");
			for (int j = 0; j < type[i].getSubsystemCount(); j++)
				logger.debug("\t\t\t" + type[i].getSubsystem(j).getName() + ",\t" + type[i].getSubsystem(j).getPv()
						+ ",\t" + type[i].getSubsystem(j).getType() + ",\t" + type[i].getSubsystem(j).getDescription());
			logger.debug("\n");

		}
	}

	private static void parseArgs(String[] args) {
		int argno = 0;
		int argc = args.length;
		while (argno < argc) {
			if (args[argno].equals("-f") && (argno + 1 < argc)) {
				_typesFile = args[++argno];
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
		TypesParser.createTypesParser();
		typesParser.printTypeList();
	}
}
