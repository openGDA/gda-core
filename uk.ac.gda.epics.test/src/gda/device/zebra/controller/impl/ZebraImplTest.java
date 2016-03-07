/*-
 * Copyright © 2016 Diamond Light Source Ltd.
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

package gda.device.zebra.controller.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import gda.epics.ReadOnlyPV;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests for ZebraImpl
 * <p>
 * Functions that delegate to a PVFactory are not tested here.
 */
public class ZebraImplTest {

	private ZebraImpl zebraImpl;

	@Before
	public void setUp() throws Exception {
		zebraImpl = new ZebraImpl();
		zebraImpl.setZebraPrefix("TESTZEBRA:ZEBRA:");
		zebraImpl.afterPropertiesSet();
	}

	@Test
	public void testInitialState() {
		assertFalse(zebraImpl.isUseAvalField());
		assertEquals("zebra", zebraImpl.getName());
		assertEquals("TESTZEBRA:ZEBRA:", zebraImpl.getZebraPrefix());
	}

	@Test
	public void testSetUseAvalField() {
		zebraImpl.setUseAvalField(true);
		assertTrue(zebraImpl.isUseAvalField());
	}

	@Test
	public void testSetZebraPrefix() {
		zebraImpl.setZebraPrefix("ZEBRA2");
		assertEquals("ZEBRA2", zebraImpl.getZebraPrefix());
	}

	@Test
	public void testAfterPropertiesSetMissingName() {
		final ZebraImpl zebraImpl2 = new ZebraImpl();
		zebraImpl2.setName("");
		zebraImpl2.setZebraPrefix("Zebra");
		try {
			zebraImpl2.afterPropertiesSet();
			fail("Calling afterPropertiesSet() with no name set should fail");
		} catch (Exception e) {
			assertEquals("name is not set", e.getMessage());
		}
	}

	@Test
	public void testAfterPropertiesSetMissingPrefix() {
		final ZebraImpl zebraImpl2 = new ZebraImpl();
		try {
			zebraImpl2.afterPropertiesSet();
			fail("Calling afterPropertiesSet() with no prefix set should fail");
		} catch (Exception e) {
			assertEquals("zebraPrefix is not set", e.getMessage());
		}
	}

	@Test
	public void testGetEnc1AvalPV() {
		ReadOnlyPV<Double[]> result = zebraImpl.getEnc1AvalPV();
		assertEquals("TESTZEBRA:ZEBRA:PC_ENC1", result.getPvName());
	}

}
