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

package gda.util;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import gda.data.metadata.GdaMetadata;
import gda.data.metadata.IMetadataEntry;
import gda.data.metadata.NexusMetadataEntry;
import gda.data.metadata.NexusMetadataReader;
import gda.data.metadata.StoredMetadataEntry;
import gda.device.enumpositioner.DummyEnumPositioner;
import gda.device.scannable.CoupledScannable;
import gda.device.scannable.scannablegroup.ScannableGroup;
import gda.function.Function;
import gda.function.IdentityFunction;
import gda.util.converters.util.RangeConverterNameProvider;
import gda.util.converters.util.RangeandConverterNameHolder;

/**
 * Tests that the newly-introduced setters for collection fields that are
 */
public class CollectionSetterTest {

	@Test
	public void testGdaMetadata() {
		GdaMetadata gm = new GdaMetadata();
		assertTrue(gm.getMetadataEntries().isEmpty());
		IMetadataEntry entry = new StoredMetadataEntry("name", "value");
		gm.setMetadataEntries(asList(entry));
		assertEquals(1, gm.getMetadataEntries().size());
	}

	@Test
	public void testNexusMetadataReader() {
		NexusMetadataReader nmr = new NexusMetadataReader();
		assertTrue(nmr.getNexusMetadataEntries().isEmpty());
		NexusMetadataEntry entry = new NexusMetadataEntry();
		nmr.setNexusMetadataEntries(asList(entry));
		assertEquals(1, nmr.getNexusMetadataEntries().size());
	}

	@Test
	public void testScannableGroupMemberNames() {
		ScannableGroup sg = new ScannableGroup();
		assertEquals(0, sg.getGroupMemberNames().length);
		sg.setGroupMemberNames(asList("one", "two"));
		assertEquals(2, sg.getGroupMemberNames().length);
	}

	@Test
	public void testCoupledScannableScannableNames() {
		CoupledScannable cs = new CoupledScannable();
		assertNull(cs.getScannableNames());
		cs.setScannableNames(asList("one", "two", "three"));
		assertEquals(3, cs.getScannableNames().size());
	}

	@Test
	public void testCoupledScannableFunctions() {
		CoupledScannable cs = new CoupledScannable();
		assertEquals(0, cs.getFunctions().size());
		Function function = new IdentityFunction();
		cs.setFunctions(asList(function));
		assertEquals(1, cs.getFunctions().size());
	}

	@Test
	public void testDummyEnumPositioner() {
		DummyEnumPositioner dep = new DummyEnumPositioner();
		assertEquals(0, dep.getPositionsList().size());
		dep.setPositions(asList("1", "2", "3"));
		assertEquals(3, dep.getPositionsList().size());
	}

	@Test
	public void testRangeConverterNameProviderConverters() {
		RangeConverterNameProvider rcnp = new RangeConverterNameProvider("name", "converterName");
		assertEquals(0, rcnp.getConverterList().size());
		RangeandConverterNameHolder conv1 = new RangeandConverterNameHolder("one", "one", 1.0, 2.0);
		RangeandConverterNameHolder conv2 = new RangeandConverterNameHolder("two", "two", 3.0, 4.0);
		rcnp.setConverters(asList(conv1, conv2));
		assertEquals(2, rcnp.getConverterList().size());
	}

}
