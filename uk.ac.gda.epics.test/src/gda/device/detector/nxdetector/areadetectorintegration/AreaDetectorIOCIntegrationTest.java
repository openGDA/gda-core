/*-
 * Copyright © 2013 Diamond Light Source Ltd.
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

package gda.device.detector.nxdetector.areadetectorintegration;

import gda.epics.LazyPVFactory;

import java.io.IOException;
import java.net.URL;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class AreaDetectorIOCIntegrationTest {
	
	@BeforeClass
	public static void setUpBeforeClass() {
		URL resource = AreaDetectorIOCIntegrationTest.class.getResource("JCALibrary.p99-ws004.simulation.properties");
		String file = resource.getFile();
		System.setProperty("gov.aps.jca.JCALibrary.properties", file);
	}
	
	@Before
	public void verifyConnection() throws IOException {
		try {
			LazyPVFactory.newIntegerPV("BLRWI-DI-CAM-01:CAMAcquire").get();
		} catch (IOException e) {
			throw new IOException("Representative PV required for test unavailable: ", e);
		}
	}
	
	@Test
	public void testName() {
		
	}

}
