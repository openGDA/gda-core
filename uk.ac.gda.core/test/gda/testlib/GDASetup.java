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

package gda.testlib;

import gda.configuration.properties.LocalProperties;
import gda.factory.FactoryException;
import gda.util.ObjectServer;

import java.io.File;
import java.util.Properties;

import org.jacorb.events.EventChannelImpl;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;

/**
 * Sets up standard GDA test environment with name (on localhost), event and objectServer with quick check to ensure
 * they are each running ok. Each may be started alone or all together using setUpAll() This class implements the
 * singleton pattern. Also sets up Java system properties for the location of simulator specific gda.propertiesFile and
 * jacorb.config.dir, using "gda.src.java" property. NB sim tests must be started with a valid "gda.src.java" property
 * as VM arg e.g. -Dgda.src.java="E:/eclipse/workspace/java" for Eclipse (Win/Linux)
 */
public class GDASetup {
	// general stuff
	private static final GDASetup gdaSetupObj = new GDASetup();

	private static final char sep = File.separatorChar;

	private static String gdaSrcJ = System.getProperty("gda.src.java");

	private static String propFile = "";

	private static String jacorbConfDir = "";

	// for event server
	private static org.omg.CORBA.ORB orb;

	private final String eventChannelName = "stnSimulator.eventChannel";

	// general server stuff
	private Process nameServerProcess;

	private static Thread objectServerThread, eventServerThread;


	protected GDASetup() {
	}

	/**
	 * method to deliver the single instance of this object to external classes.
	 * 
	 * @return the standard GDA test setup instance
	 */
	public static GDASetup getInstance() {
		return (gdaSetupObj);
	}

	/**
	 * Sets complete GDA sim test environment by calling all public methods in class.
	 * 
	 * @throws Exception
	 */
	public void setUpAll() throws Exception {
		setUpSimProperties();
		setUpNameServer();
		setUpEventServer();
		setUpObjectServer();
//		setUpGUI();
	}

	/**
	 * Starts event server on local machine running in own Thread. Call tearDownAll() to halt it from tearDown() method
	 * 
	 * @throws Exception
	 */
	public void setUpEventServer() throws Exception {
		// this is here for compatibility in the code later.
		final String args[] = { "" };

		// Following taken from main in gda.factory.corba.util.ChannelServer
		// except orb is now an instance variable and is not run as Thread
		// because it then blocks.
		Properties props = System.getProperties();

		props.put("org.omg.CORBA.ORBClass", LocalProperties.get("gda.ORBClass"));
		props.put("org.omg.CORBA.ORBSingletonClass", LocalProperties.get("gda.ORBSingletonClass"));

		orb = org.omg.CORBA.ORB.init(args, props);

		org.omg.PortableServer.POA poa = org.omg.PortableServer.POAHelper.narrow(orb
				.resolve_initial_references("RootPOA"));

		NamingContextExt nc = NamingContextExtHelper.narrow(orb.resolve_initial_references("NameService"));

		EventChannelImpl channel = new EventChannelImpl(orb, poa);

		poa.the_POAManager().activate();

		org.omg.CORBA.Object o = poa.servant_to_reference(channel);
		nc.rebind(nc.to_name(eventChannelName), o);

		// now start the orb in a new thread
		eventServerThread = uk.ac.gda.util.ThreadManager.getThread(new Runnable() {
			@Override
			public void run() {
				orb.run();
			}
		});
		eventServerThread.start();

		Thread.sleep(1000);
		if (eventServerThread == null) {
			throw new Exception("GDA event server has not started (null)");
		}
	}


	/**
	 * Starts name server on local machine running in own process VM. This will carry on after Java tests have completed
	 * so call tearDownAll() to halt it from tearDown() method
	 * 
	 * @throws Exception
	 */
	public void setUpNameServer() throws Exception {
		nameServerProcess = Runtime.getRuntime().exec("tnameserv -ORBInitialPort 6700");

		Thread.sleep(1000);
		if (nameServerProcess == null) {
			throw new Exception("GDA name server has not started (null)");
		}
	}

	/**
	 * Starts object server on local machine running in own process VM. This will carry on after Java tests have
	 * completed so call tearDownAll() to halt it from tearDown() method
	 * 
	 * @throws Exception
	 */
	public void setUpObjectServer() throws Exception {
		objectServerThread = uk.ac.gda.util.ThreadManager.getThread(new Runnable() {
			@Override
			public void run() {
				try {
					ObjectServer.createServerImpl();
				} catch (FactoryException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		objectServerThread.start();

		Thread.sleep(1000);
		if (objectServerThread == null) {
			throw new Exception("GDA object server has not started (null)");
		}
	}

	/**
	 * Builds paths for gda.propertiesFile and jacorb.config.dir from the existing gda.src.java system property. Tests
	 * in GDA Simulator should call this method first or straight after GeneralSetup.genInstance().setUpAll()
	 */
	public void setUpSimProperties() {
		// property paths on PC require backslash separator as they'll fail
		// with mixed separators when jacorb.properties path acquires a
		// "\etc\.."
		// thus initially set all to standard separator of platform.
		gdaSrcJ = SwitchSeparatorsToPlatform(System.getProperty("gda.src.java"));
		System.setProperty("gda.src.java", gdaSrcJ);

		propFile = gdaSrcJ + sep + "tests" + sep + "gda" + sep + "stnSimulator" + sep + "params" + sep + "properties"
				+ sep + "java.properties";
		System.setProperty("gda.propertiesFile", propFile);

		jacorbConfDir = gdaSrcJ + sep + "tests" + sep + "gda" + sep + "stnSimulator" + sep + "params" + sep
				+ "properties";
		System.setProperty("jacorb.config.dir", jacorbConfDir);

		// gda.propertiesFile interpolator string must have forward slashes
		// which must match forward slashes in portable prop file path contents
		gdaSrcJ = SwitchSeparatorsToForward(gdaSrcJ);
		System.setProperty("gda.src.java", gdaSrcJ);
	}

	/**
	 * Make path separators in string all point forwards.
	 * 
	 * @param pathString
	 *            file path name
	 * @return modified file path name
	 */
	public String SwitchSeparatorsToForward(String pathString) {
		return pathString.replace('\\', '/');
	}

	/**
	 * Make path separators in string all conform to standard on that platform.
	 * 
	 * @param pathString
	 *            file path name
	 * @return modified file path name
	 */
	public String SwitchSeparatorsToPlatform(String pathString) {
		return pathString.replace('/', sep);
	}

	/**
	 * resets GDA test environment by stopping event and name servers. NB if things don't need stopping, this should not
	 * generate an error.
	 */
	public void tearDownAll() {
		if (nameServerProcess != null) {
			try {
				nameServerProcess.destroy();
			} catch (Exception e) {
			}
		}
	}

	/**
	 * Main Method.
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		GDASetup.getInstance().setUpAll();
	}

}
