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

package gda.device;

import static org.junit.Assert.*;
import gda.device.DeviceException;
import gda.device.adc.DummyAdc;
import gda.factory.FactoryException;
import gda.observable.IObserver;

import org.junit.Before;
import org.junit.Test;

/**
 * test for abstract DeviceBase methods using DummyAdc concrete class
 */
public class DeviceBaseTest implements IObserver {

	private DummyAdc dummyAdc;

	/**
	 */
	@Before
	public void setUp() {
		dummyAdc = new DummyAdc();
	}

	/**
	 * 
	 */
	@Test
	public void testDummyAdc() {
		assertNotNull("dummyAdc instance should be non-null", dummyAdc);
	}

	/**
	 * 
	 */
	@Test
	public void testDeviceBase() {
		String name = "DummyAdc";
		dummyAdc.setName(name);
		assertEquals("name should be " + name, dummyAdc.getName(), name);
	}

	/**
	 */
	@Test
	public void testConfigure() {
		// should do nothing
		dummyAdc.configure();
	}

	/**
	 * 
	 */
	@Test
	public void testIsLocal() {
		// default should be false
		assertFalse("local should be false initially", dummyAdc.isLocal());
	}

	/**
	 * 
	 */
	@Test
	public void testSetLocal() {
		dummyAdc.setLocal(true);
		assertTrue("local should now be set true", dummyAdc.isLocal());
	}

	/**
	 * 
	 */
	@Test
	public void testIsConfigureAtStartup() {
		// default should be true
		assertTrue("local should be false initially", dummyAdc.isConfigureAtStartup());
	}

	/**
	 * 
	 */
	@Test
	public void testSetConfigureAtStartup() {
		dummyAdc.setConfigureAtStartup(false);
		assertFalse("local should now be set false", dummyAdc.isConfigureAtStartup());
	}

	/**
	 * @throws FactoryException
	 */
	@Test
	public void testReconfigure() throws FactoryException {
		// should do nothing
		dummyAdc.reconfigure();
	}

	/**
	 * @throws DeviceException
	 */
	@Test
	public void testClose() throws DeviceException {
		// should do nothing
		dummyAdc.close();
	}

	/**
	 * @throws DeviceException
	 */
	@Test
	public void testSetAttribute() throws DeviceException {
		// see if lots can be set
		double val = 0;
		for (int i = 0; i < 1000; i++) {
			dummyAdc.setAttribute("attribute_" + i, new Double(val));
			val += 100;
		}

		// see if last one is there ok
		assertEquals("attribute not as set", dummyAdc.getAttribute("attribute_999"), 99900.);
	}

	/**
	 * @throws DeviceException
	 */
	@Test
	public void testGetAttribute() throws DeviceException {
		String name = "attribute_0";
		double val = 0.12;
		dummyAdc.setAttribute("attribute_0", new Double(val));
		assertEquals("attribute not as set", dummyAdc.getAttribute(name), val);

		// try resetting same name again - should be no error
		dummyAdc.setAttribute("attribute_0", new Double(val));
		val += 1;
		dummyAdc.setAttribute("attribute_0", new Double(val));
		assertEquals("attribute not as set", dummyAdc.getAttribute(name), val);

		// get null from invalid name
		name = "notReallyThere";
		assertNull("non existant attribute should be null", dummyAdc.getAttribute(name));
	}

	/**
	 * 
	 */
	@Test
	public void testAddIObserver() {
		dummyAdc.addIObserver(this);
		dummyAdc.deleteIObserver(this);
	}

	/**
	 * 
	 */
	@Test
	public void testDeleteIObserver() {
		dummyAdc.addIObserver(this);
		dummyAdc.deleteIObserver(this);

		// delete of non-existant IObserver does no harm
		dummyAdc.deleteIObserver(this);
	}

	/**
	 * 
	 */
	@Test
	public void testDeleteIObservers() {
		// delete of non-existant IObserver does no harm
		dummyAdc.deleteIObservers();
		dummyAdc.addIObserver(this);
		dummyAdc.deleteIObservers();
		dummyAdc.deleteIObservers();
	}

	/**
	 * @throws DeviceException
	 */
	@Test
	public void testNotifyIObservers() throws DeviceException {
		dummyAdc.addIObserver(this);
		dummyAdc.notifyIObservers(this, "hello");
		assertEquals("attribute not as set in update", dummyAdc.getAttribute(this.toString()), "hello");

		// see if lots can be set
		double val = 0;
		for (int i = 0; i < 1000; i++) {
			dummyAdc.notifyIObservers("attribute_" + i, new Double(val));
			val += 100;
		}

		// see if 1st and last ones are there ok
		assertEquals("attribute not as set in update", dummyAdc.getAttribute("attribute_0"), 0.);
		assertEquals("attribute not as set in update", dummyAdc.getAttribute("attribute_999"), 99900.);
	}

	// callback code for IObserver interface
	@Override
	public void update(Object theObserved, Object changeCode) {
		// save call back objects as attributes
		try {
			dummyAdc.setAttribute(theObserved.toString(), changeCode);
		} catch (DeviceException e) {
			fail("cannot store update objects as attributes");
		}
		return;
	}
}
