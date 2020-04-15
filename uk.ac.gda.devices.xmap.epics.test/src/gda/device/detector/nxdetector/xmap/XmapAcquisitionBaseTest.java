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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import gda.device.detector.nxdetector.xmap.controller.XmapAcquisitionBaseEpicsLayer;
import gda.device.detector.nxdetector.xmap.controller.XmapAcquisitionBaseEpicsLayerImpl;
import gda.device.detector.nxdetector.xmap.controller.XmapMappingModeEpicsLayer;
import gda.device.detector.nxdetector.xmap.controller.XmapMappingModeEpicsLayerImpl;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"com.cosylab.*", "gov.aps.*"})
@PrepareForTest({XmapAcquisitionBaseEpicsLayer.class})
public class XmapAcquisitionBaseTest {
	private String basePVname;
	private XmapMappingModeEpicsLayer xmapAcquisitionEpicsLayer;
	private XmapAcquisitionBaseEpicsLayer xmapAcquisitionEpicsLayerwithMapping;

	@Before
	public void setUp() {
		basePVname = "basePV:";
		xmapAcquisitionEpicsLayer = PowerMockito.mock(XmapMappingModeEpicsLayerImpl.class);
		xmapAcquisitionEpicsLayerwithMapping = new XmapAcquisitionBaseEpicsLayerImpl(basePVname, xmapAcquisitionEpicsLayer);

	}

	@Test
	public void testFullPVname() {
		assertEquals("Check fullPVname", "basePV:Suffix", xmapAcquisitionEpicsLayerwithMapping.fullPVname("Suffix"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testBasePVnameInConstructorExceptionIsThrown() {
		new XmapAcquisitionBaseEpicsLayerImpl(null, xmapAcquisitionEpicsLayer);
	}

	@Test(expected = NullPointerException.class)
	public void testCollectionModeInConstructorExceptionIsThrown() {
		new XmapAcquisitionBaseEpicsLayerImpl(basePVname, null);
	}

	@Test
	public void testIsXmapMappingModeInstanceIsCorrect() {
		assertTrue("Mapping mode exists", xmapAcquisitionEpicsLayerwithMapping.isXmapMappingModeInstance("Test"));
	}
}
