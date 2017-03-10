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
import gda.device.peem.MicroscopeControl.Microscope;
import gda.device.peem.MicroscopeControl.MicroscopeHelper;
import gda.factory.Configurable;
import gda.factory.Findable;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import org.omg.CORBA.ORB;
import org.omg.CosNaming.NameComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Java Implementation for Connection between PEEM CORBA Server and Client
 */
public class CorbaBridgeConnection implements Configurable, Findable {
	private static final Logger logger = LoggerFactory.getLogger(CorbaBridgeConnection.class);

	private String name = null;

	/**
	 * 
	 */
	public static ORB orb = null;

	/**
	 * 
	 */
	public static Microscope msImpl = null;

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
	public CorbaBridgeConnection() {
	}

	@Override
	public void configure() {
		logger.debug("CorbaBridgeConnection configured!");
		connect();
	}

	/**
	 * Instruct the ORB to connect to & setup the detector
	 * 
	 * @return Microscope detector object
	 */
	public Microscope connect() {
		if (isConnected()){
			logger.info("The Java ORB already connected.");
//			this.disconnect();
		}
		else{
			getConfig();
	
			if (setupORB()){
				connected = true;
				logger.info("The Java ORB has been successfully established.");
			}
			else{
				connected = false;
				logger.info("Can not connecte to the Corba Bridge!");
			}
		}
		
		return msImpl;
	}

	/**
	 * Instruct the ORB to disconnect from the detector
	 * 
	 * @return boolean true after shutdown the connection
	 */
	public boolean disconnect() {
		shutdownORB();
		msImpl = null;
		logger.info("PEEM Corba Bridge Client disconnected!");
		return true;
	}

	/**
	 * Determine if detector is connected
	 * 
	 * @return boolean true if connected else false
	 */
	public boolean isConnected() {
		return connected;
	}

	/**
	 * Read the Config file named in java properties
	 * 
	 * @return int read status 1='file read', 2='file not found'
	 */
	public int getConfig() {
		String clientConfigFile = LocalProperties.get("gda.config") + System.getProperty("file.separator")
				+ "properties" + System.getProperty("file.separator") + "PEEM.Bridge.Client.properties";
		// logger.debug("PEEM config file: " + clientConfigFile);

		CORBACommandArgc = 0;
		if (readConfigurationFile(clientConfigFile)) { // use the configuration file for orb settings
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
	 * Read object reference from named file
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
	 * Read the Client configuration file startupClient.txt
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

	/**
	 * Setup the PEEM Corba Bridge Client
	 * 
	 * @return boolean true if set OK else false
	 */
	public boolean setupORB() {
		try {
			if (connected) {
				logger.debug("The Corba connected with PEEM is already connected. Use the existing one.");
			} else {
				// Create and initialize the ORB
				orb = ORB.init(CORBACommandArg, System.getProperties()); // -ORBInitialPort
				// 1050
				// -ORBInitialHost
				// localhost
			}
			org.omg.CORBA.Object obj;

			if (CORBACommandArgc == 0) {
				// Obtain the IOR Reference from file
				String strIOR = readRef(CORBARef);
				obj = orb.string_to_object(strIOR);

				msImpl = MicroscopeHelper.narrow(obj);
				logger.debug("Obtained a handle from IOR file" + msImpl);
			} else {
				// Obtain Naming service reference
				org.omg.CosNaming.NamingContext rootContext = null;
				obj = orb.resolve_initial_references("NameService");
				rootContext = org.omg.CosNaming.NamingContextHelper.narrow(obj);

				// Obtain the MicroscopeController object reference
				org.omg.CosNaming.NameComponent[] objName = new NameComponent[2];
				objName[0] = new NameComponent("MicroscopeControl", "");
				objName[1] = new NameComponent("LEEM", "");
				obj = rootContext.resolve(objName);

				msImpl = MicroscopeHelper.narrow(obj);
				logger.debug("Obtained a handle from nameing service: " + msImpl);
			}
		} catch (Exception e) {
			logger.debug("ERROR : " + e);
			e.printStackTrace(System.out);
			connected = false;
			msImpl = null;
			logger.info("Can not connecte with the PEEM Server via Corba Bridge!");
			return false;
		}

		logger.debug("The Java ORB has been successfully established.");
		System.out.print("The PEEM Vesion Info: ");
		logger.debug("UView " + msImpl.GetVersion() + ", LEEM2000 " + msImpl.GetLEEM2000Version());
		connected = true;
		logger.info("PEEM Corba Bridge Client established!");
		return true;
	}

	/**
	 * Instruct the ORB to shutdown
	 */
	public void shutdownORB() {
		connected = false;
		orb.shutdown(false);
		orb.destroy();
		msImpl = null;
		logger.info("PEEM Corba Bridge Client disconnected!");
	}

	/**
	 * Print the UView version as Debug message
	 */
	public void getUViewVersion() {
		logger.debug(Float.toString(msImpl.GetVersion()));
	}

	/**
	 * Print the LEEM2000 version as Debug message
	 */
	public void getLEEMVersion() {
		logger.debug(Float.toString(msImpl.GetLEEM2000Version()));
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

}