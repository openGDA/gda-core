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

package gda.device.detector.uviewnew.corba.impl;

import gda.configuration.properties.LocalProperties;
import gda.device.DeviceBase;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An abstract base class for all CorbaBridgeConnection classes
 */
abstract public class CorbaBridgeConnectionBase extends DeviceBase {

	private static final Logger logger = LoggerFactory.getLogger(CorbaBridgeConnectionBase.class);

	/**
	 * 
	 */
	public String CORBARef = "c:/leem.ref";

	/**
	 * 
	 */
	public String[] CORBACommandArg = new String[8];

	/**
	 * 
	 */
	public int CORBACommandArgc = 0;

	/**
	 * 
	 */
	public static boolean connected = false;

	/*
	 * public String[] CORBACommandArg={"-ORBNamingAddr", "inet:diamrd2050.dc.diamond.ac.uk:1050", "-ORBInitialPort",
	 * "1050", "-ORBInitialHost", "diamrd2050.dc.diamond.ac.uk", "-ORBInitRef",
	 * "NameService=corbaloc:iiop:1.2@diamrd2050.dc.diamond.ac.uk:1050/NameService" };
	 */

	/**
	 * Constructor.
	 */
	public CorbaBridgeConnectionBase() {
		// getConfig();
	}

	/**
	 * Return if Corba bridge connected
	 * 
	 * @return boolean true if connected else false
	 */
	public boolean isConnected() {
		return connected;
	}

	/**
	 * Read the config file
	 * 
	 * @return int 1='file readOK', 2='error reading file'
	 */
	public int getConfig() {
		String clientConfigFile = LocalProperties.get("gda.config") + System.getProperty("file.separator")
				+ "properties" + System.getProperty("file.separator") + "PEEM.Bridge.Client.properties";
		// logger.debug("PEEM config file: " + clientConfigFile);

		CORBACommandArgc = 0;
		if (readConfigurationFile(clientConfigFile)) { // use the configuration
			// file for orb settings
			return 1;
		}

		// Can not find the configuration file, apply the default setting
		logger.debug("Default setting will be used");
		CORBACommandArg[CORBACommandArgc++] = "-ORBNamingAddr";
		CORBACommandArg[CORBACommandArgc++] = "inet:diamrd2050.dc.diamond.ac.uk:1050";
		CORBACommandArg[CORBACommandArgc++] = "-ORBInitialPort";
		CORBACommandArg[CORBACommandArgc++] = "1050";
		CORBACommandArg[CORBACommandArgc++] = "-ORBInitialHost";
		CORBACommandArg[CORBACommandArgc++] = "diamrd2050.dc.diamond.ac.uk";
		CORBACommandArg[CORBACommandArgc++] = "-ORBInitRef";
		CORBACommandArg[CORBACommandArgc++] = "NameService=corbaloc:iiop:1.2@diamrd2050.dc.diamond.ac.uk:1050/NameService";
		return 2;

	}

	/**
	 * @return boolean
	 */
	abstract public boolean setupORB();

	/**
	 * Read object reference from file
	 * 
	 * @param fileName
	 *            file to read
	 * @return String representation of the serialized object or null
	 */
	public String readRef(String fileName) {
		logger.debug("Reading stringified obj reference from file: " + fileName);
		String strIOR = null;
		try {
			FileReader fr = new FileReader(fileName);
			BufferedReader in = new BufferedReader(fr);
			strIOR = in.readLine();
		} catch (IOException ex) {
			logger.debug("Error reading object reference from file: " + fileName);
			logger.debug(ex.toString());
			return null;
		}

		return strIOR;
	}

	/**
	 * Read the Client configuratioin file startupClient.txt
	 * 
	 * @param fileName
	 *            the String name of the file to read
	 * @return boolean true = 'file read' else false
	 */
	public boolean readConfigurationFile(String fileName) {
		logger.debug("Reading configuration info from file: " + fileName);
		String strLine = null;

		try {
			FileReader fr = new FileReader(fileName);
			BufferedReader in = new BufferedReader(fr);

			while ((strLine = in.readLine()) != null) {
				// logger.debug("Reading out one line: " + strLine + "with Arg "
				// +
				// CORBACommandArgc);

				String[] s1 = strLine.split(" ");
				if (s1.length == 2 && s1[0].compareTo("CORBARefName") == 0)
					CORBARef = s1[1];
				else if (s1.length == 3 && s1[0].compareTo("CORBAComArg1") == 0) {
					CORBACommandArg[CORBACommandArgc++] = s1[1];
					CORBACommandArg[CORBACommandArgc++] = s1[2];
				} else if (s1.length == 3 && s1[0].compareTo("CORBAComArg2") == 0) {
					CORBACommandArg[CORBACommandArgc++] = s1[1];
					CORBACommandArg[CORBACommandArgc++] = s1[2];
				} else if (s1.length == 3 && s1[0].compareTo("CORBAComArg3") == 0) {
					CORBACommandArg[CORBACommandArgc++] = s1[1];
					CORBACommandArg[CORBACommandArgc++] = s1[2];
				} else if (s1.length == 3 && s1[0].compareTo("CORBAComArg4") == 0) {
					CORBACommandArg[CORBACommandArgc++] = s1[1];
					CORBACommandArg[CORBACommandArgc++] = s1[2];
				}
			}

			logger.debug(CORBARef);
			for (int i = 0; i < CORBACommandArgc; i += 2) {
				logger.debug(CORBACommandArg[i] + CORBACommandArg[i + 1]);
			}

		} catch (IOException ex) {
			logger.debug("Error reading client parameters from the configuration file: " + fileName);
			logger.debug(ex.toString());
			return false;
		}

		return true;
	}

}
