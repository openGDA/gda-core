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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import gda.device.detector.nxdetector.xmap.controller.CollectionMode;
import gda.device.detector.nxdetector.xmap.controller.XmapAcquisitionBaseEpicsLayer;
import gda.device.detector.nxdetector.xmap.controller.XmapAcquisitionBaseEpicsLayerImpl;
import gda.device.detector.nxdetector.xmap.controller.XmapMappingModeEpicsLayer;
import gda.device.detector.nxdetector.xmap.controller.XmapMappingModeEpicsLayerImpl;

import java.io.IOException;

import org.junit.Before;
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
	 */
	private String basePVname;
	private XmapAcquisitionBaseEpicsLayer xmapAcquisitionEpicsLayer;
	private XmapMappingModeEpicsLayer mappingMode;
	private CollectionMode mcaMode;
	private XmapAcquisitionBaseEpicsLayer xmapAcquisitionEpicsLayerwithMapping;

	@Before
	public void setUp() throws IOException {
		basePVname = "basePV:";
		CollectionMode mcaMode = PowerMockito.mock(CollectionMode.class);
		mappingMode = PowerMockito.mock(XmapMappingModeEpicsLayerImpl.class);
		xmapAcquisitionEpicsLayer = new XmapAcquisitionBaseEpicsLayerImpl(basePVname, mcaMode);
		xmapAcquisitionEpicsLayerwithMapping = new XmapAcquisitionBaseEpicsLayerImpl(basePVname, (CollectionMode) mappingMode);

	}

	@Test
	public void testFullPVname() {
		assertEquals("Check fullPVname", "basePV:Suffix", xmapAcquisitionEpicsLayer.fullPVname("Suffix"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testBasePVnameInConstructorExceptionIsThrown() throws IOException {
		XmapAcquisitionBaseEpicsLayer xmapAcquisitionEpicsLayerException = new XmapAcquisitionBaseEpicsLayerImpl(null, mcaMode);
	}

	@Test(expected = NullPointerException.class)
	public void testCollectionModeInConstructorExceptionIsThrown() throws IOException {
		XmapAcquisitionBaseEpicsLayer xmapAcquisitionEpicsLayerException = new XmapAcquisitionBaseEpicsLayerImpl(basePVname, null);
	}


	@Test(expected = ClassCastException.class)
	public void testIsXmapMappingModeInstanceExceptionIsThrown() {
		xmapAcquisitionEpicsLayer.isXmapMappingModeInstance("Test");
	}

	@Test
	public void testIsXmapMappingModeInstanceIsCorrect() {
		assertTrue("Mapping mode exists", xmapAcquisitionEpicsLayerwithMapping.isXmapMappingModeInstance("Test"));
	}
}
