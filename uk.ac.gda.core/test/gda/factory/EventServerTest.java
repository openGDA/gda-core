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

package gda.factory;

import gda.configuration.properties.LocalProperties;

import java.util.Properties;

import junit.framework.TestCase;

import org.jacorb.events.EventChannelImpl;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;

import gda.testlib.GDASetup;
import gda.testlib.GeneralSetup;

/**
 * JUnit case to test GDA EventServer starts successfully The name server is required and is started from here. The name
 * and event server are not left running when the test finishes.
 */
public class EventServerTest extends TestCase {
	private static org.omg.CORBA.ORB orb;

	private final String eventChannelName = "stnSimulator.eventChannel";

	/**
	 * Constructor needed if only one test method needs running
	 * 
	 * @param testName
	 *            specific test to run
	 */
	public EventServerTest(String testName) {
		super(testName);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		// use complete general Junit test environment and GDA name server
		GeneralSetup.getInstance().setUpAll();
		GDASetup.getInstance().setUpSimProperties();
		GDASetup.getInstance().setUpNameServer();

		// start server process but leave Orb thread not yet started
		setUpServer();
	}

	/**
	 * Start the server with access for outside Java classes.
	 * 
	 * @throws Exception
	 */
	public void setUpServer() throws Exception {
		// this is here for compatibility in the code later.
		final String args[] = { "" };

		// Following taken from main in gda.factory.corba.util.ChannelServer
		// except orb is now an instance variable and is not run as Thread
		// because it then blocks.
		Properties props = System.getProperties();

		props.put("org.omg.CORBA.ORBClass", LocalProperties.get("gda.ORBClass", "org.jacorb.orb.ORB"));
		props.put("org.omg.CORBA.ORBSingletonClass", LocalProperties.get("gda.ORBSingletonClass", "org.jacorb.orb.ORBSingleton"));

		orb = org.omg.CORBA.ORB.init(args, props);

		org.omg.PortableServer.POA poa = org.omg.PortableServer.POAHelper.narrow(orb
				.resolve_initial_references("RootPOA"));

		NamingContextExt nc = NamingContextExtHelper.narrow(orb.resolve_initial_references("NameService"));

		EventChannelImpl channel = new EventChannelImpl(orb, poa);

		poa.the_POAManager().activate();

		org.omg.CORBA.Object o = poa.servant_to_reference(channel);
		nc.rebind(nc.to_name(eventChannelName), o);
	}

	/*
	 * @see TestCase#tearDown()
	 */
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		GDASetup.getInstance().tearDownAll();
	}

	/**
	 * The returned Orb reference must not be null before being started
	 */
	public void testEventServerCanRun() {
		assertNotNull(orb);
		// TODO add` extra orb tests
	}

	/**
	 * The Orb reference then run in a thread of its own and checks are made that it is alive before and after a time
	 * delay of a few seconds.
	 * 
	 * @throws InterruptedException
	 */
	public void testEventServerWillRun() throws InterruptedException {
		// startServerRunning();
		Thread serverThread = uk.ac.gda.util.ThreadManager.getThread(new Runnable() {
			@Override
			public void run() {
				orb.run();
			}
		});
		serverThread.start();
		Thread.sleep(1000);
		assertTrue(serverThread.isAlive());
		Thread.sleep(3000);
		assertTrue(serverThread.isAlive());
	}

	/**
	 * The Orb reference then run in a thread of its own and checks are made that it is alive before and after a time
	 * delay of a few seconds.
	 */
	public void startServerRunning() {
		// TODO tidy up
	}

}
