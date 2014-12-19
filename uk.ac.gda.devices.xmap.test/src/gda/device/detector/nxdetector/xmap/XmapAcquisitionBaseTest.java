/*-
 * Copyright © 2014 Diamond Light Source Ltd.
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

package gda.device.detector.nxdetector.xmap;

import static org.junit.Assert.fail;
import gda.device.detector.nxdetector.xmap.Controller.CollectionMode;
import gda.device.detector.nxdetector.xmap.Controller.XmapAcquisitionBaseEpicsLayer;
import gda.device.detector.nxdetector.xmap.Controller.XmapMappingModeEpicsLayer;
import gda.epics.LazyPVFactory;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 *
 */

@RunWith(PowerMockRunner.class)
@PrepareForTest({XmapAcquisitionBaseEpicsLayer.class})

public class XmapAcquisitionBaseTest {

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		PowerMockito.mockStatic(LazyPVFactory.class);
		CollectionMode mcaMode = PowerMockito.mock(CollectionMode.class);
		XmapMappingModeEpicsLayer mappingMode = PowerMockito.mock(XmapMappingModeEpicsLayer.class);
		
	
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() {
		fail("Not yet implemented");
	}

}
