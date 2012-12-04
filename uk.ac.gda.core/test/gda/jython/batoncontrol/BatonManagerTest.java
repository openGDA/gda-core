/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council
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

package gda.jython.batoncontrol;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import gda.configuration.properties.LocalProperties;
import gda.jython.InterfaceProvider;
import gda.jython.MockJythonServer;
import gda.jython.authenticator.Authenticator;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests the BatonManager component used by JythonServer
 */
public class BatonManagerTest {

	BatonManager manager;

	/**
	 */
	@Before
	public void setUp() {
		LocalProperties.set("gda.accesscontrol.firstClientTakesBaton", "true");
		LocalProperties.set( LocalProperties.GDA_BATON_MANAGEMENT_ENABLED, "true");
		LocalProperties.set( LocalProperties.GDA_ACCESS_CONTROL_ENABLED, "true");
		LocalProperties.set(Authenticator.AUTHENTICATORCLASS_PROPERTY, "JaasAuthenticator");

		InterfaceProvider.setJythonServerNotiferForTesting(new MockJythonServer());
		manager = new BatonManager();
		ClientDetails details1 = new ClientDetails(manager.getNewFacadeIndex(), "abc123", "ABC 123", "pc01234", 0, false, "mx123-1");
		manager.addFacade("opq", details1);
		ClientDetails details2 = new ClientDetails(manager.getNewFacadeIndex(), "def456", "DEF 456", "pc12345", 1, false, "mx123-2");
		manager.addFacade("xyz", details2);
		ClientDetails details3 = new ClientDetails(manager.getNewFacadeIndex(), "ghi789", "GHI 789", "pc23456", 2, false, "mx123-3");
		manager.addFacade("rst", details3);
	}

	/**
	 * 
	 */
	@Test
	public void testAddFacade() {
		ClientDetails details4 = new ClientDetails(manager.getNewFacadeIndex(), "jkl101", "JKL 101", "pc34567", 0, false, "mx123-3");
		manager.addFacade("uvw", details4);
		assertTrue(manager.isJSFRegistered("uvw"));
	}

	/**
	 * 
	 */
	@Test
	public void testRemoveFacade() {
		manager.removeFacade("opq");
		assertEquals(manager.getOtherClientInformation("xyz").length, 1);
	}

	/**
	 * 
	 */
	@Test
	public void testGetNewFacadeIndex() {
		assertEquals(manager.getNewFacadeIndex(), 3);
	}

	/**
	 * 
	 */
	@Test
	public void testAmIBatonHolder() {
		assertTrue(manager.amIBatonHolder("opq"));
		assertFalse(manager.amIBatonHolder("xyz"));
	}

	/**
	 * 
	 */
	@Test
	public void testAssignBaton() {
		manager.requestBaton("opq");
		manager.assignBaton("opq", 1);  //give to xyz
		assertTrue(manager.amIBatonHolder("xyz"));
		assertFalse(manager.amIBatonHolder("opq"));
	}

	/**
	 * 
	 */
	@Test
	public void testGetOtherClientInformation() {
		ClientDetails[] test = manager.getOtherClientInformation("opq");
		assertEquals(test.length, 2);
	}

	/**
	 * 
	 */
	@Test
	public void testRequestAndReturnBaton() {
		manager.returnBaton("opq");
		assertFalse(manager.isBatonHeld());
		manager.requestBaton("xyz");
		assertTrue(manager.amIBatonHolder("xyz"));
		manager.requestBaton("opq");
		assertFalse(manager.amIBatonHolder("opq"));
		manager.requestBaton("rst");
		assertTrue(manager.amIBatonHolder("rst"));
	}
	
	/**
	 * 
	 */
	@Test
	public void testSwitchUser() {
		manager.requestBaton("xyz");
		assertTrue(manager.amIBatonHolder("xyz"));
		manager.switchUser("opq", "abc123", 3, null);
		manager.requestBaton("opq");
		assertTrue(manager.amIBatonHolder("opq"));
		
	}

	/**
	 * 
	 */
	@Test
	public void testIsJSFRegistered() {
		assertFalse(manager.isJSFRegistered(";flakedg"));
		assertTrue(manager.isJSFRegistered("opq"));
	}
}
