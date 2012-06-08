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

import gda.factory.FactoryException;
import gda.factory.corba.util.NetService;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests the NetService. This test must be run in the same suite as ObjectServerTest, and after that test. This requires
 * a working ObjectServer based on test.xml to already be running.
 */
public class NetServiceTest extends TestCase {
	private static final Logger logger = LoggerFactory.getLogger(NetServiceTest.class);

	/**
	 * @return test suite
	 */
	public static Test suite() {
		return new TestSuite(NetServiceTest.class);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		junit.textui.TestRunner.run(suite());
	}

	NetService netService = null;

	@Override
	protected void setUp() {

		try {
			netService = NetService.getInstance();
		} catch (FactoryException ex) {
			logger.debug(ex.getStackTrace().toString());
		}
	}

	/**
	 * 
	 */
	public void testNetService() {
		netService.listAll();
		// ArrayList<Findable> GenericOEs = netService.listAllGenericOEs();
		// ArrayList<Findable> GenericOEs =
		// Finder.getInstance().listAllObjects("OE");
		/*
		 * Vector<String> names = new Vector(); String name = "TestFactory" + "test28Motor";
		 * System.out.println(netService.getType(name));
		 */
	}

}
