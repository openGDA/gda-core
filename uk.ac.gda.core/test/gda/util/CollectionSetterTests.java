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

import java.util.ArrayList;

import junit.framework.TestCase;

/**
 * Tests that the newly-introduced setters for collection fields that are
 */
public class CollectionSetterTests extends TestCase {
	
	public void testGdaMetadata() {
		GdaMetadata gm = new GdaMetadata();
		assertTrue(gm.getMetadataEntries().isEmpty());
		IMetadataEntry entry = new StoredMetadataEntry("name", "value");
		gm.setMetadataEntries(arrayListOf(entry));
		assertEquals(1, gm.getMetadataEntries().size());
	}
	
	public void testNexusMetadataReader() {
		NexusMetadataReader nmr = new NexusMetadataReader();
		assertTrue(nmr.getNexusMetadataEntries().isEmpty());
		NexusMetadataEntry entry = new NexusMetadataEntry();
		nmr.setNexusMetadataEntries(arrayListOf(entry));
		assertEquals(1, nmr.getNexusMetadataEntries().size());
	}
	
	public void testScannableGroupMemberNames() {
		ScannableGroup sg = new ScannableGroup();
		assertEquals(0, sg.getGroupMemberNames().length);
		sg.setGroupMemberNames(arrayListOf("one", "two"));
		assertEquals(2, sg.getGroupMemberNames().length);
	}
	
	public void testCoupledScannableScannableNames() {
		CoupledScannable cs = new CoupledScannable();
		assertNull(cs.getScannableNames());
		cs.setScannableNames(arrayListOf("one", "two", "three"));
		assertEquals(3, cs.getScannableNames().length);
	}
	
	public void testCoupledScannableFunctions() {
		CoupledScannable cs = new CoupledScannable();
		assertEquals(0, cs.getFunctions().length);
		Function function = new IdentityFunction();
		cs.setFunctions(arrayListOf(function));
		assertEquals(1, cs.getFunctions().length);
	}
	
	public void testDummyEnumPositioner() {
		DummyEnumPositioner dep = new DummyEnumPositioner();
		assertEquals(0, dep.getPositionArrayList().size());
		dep.setPositions(arrayOf("1", "2", "3"));
		assertEquals(3, dep.getPositionArrayList().size());
	}
	
//	public void testPolarimeterPinholeEnumPositionerLabels() {
//		PolarimeterPinholeEnumPositioner ppep = new PolarimeterPinholeEnumPositioner();
//		assertEquals(0, ppep.getLabelArrayList().size());
//		ppep.setLabels(vectorOf("one", "two"));
//		assertEquals(2, ppep.getLabelArrayList().size());
//	}
//	
//	public void testPolarimeterPinholeEnumPositionerValues() {
//		PolarimeterPinholeEnumPositioner ppep = new PolarimeterPinholeEnumPositioner();
//		assertEquals(0, ppep.getValueArrayList().size());
//		ppep.setValues(vectorOf("one", "two"));
//		assertEquals(2, ppep.getValueArrayList().size());
//	}
	
//	public void testEpicsMCACounterTimerNames() {
//		EpicsMCACounterTimer emct = new EpicsMCACounterTimer();
//		assertEquals(0, emct.getEpicsMcaNameList().size());
//		emct.setEpicsMcaNames(arrayListOf("one", "two"));
//		assertEquals(2, emct.getEpicsMcaNameList().size());
//	}
//	
//	public void testTfgEpicsMCACounterTimerNames() {
//		TfgEpicsMCACounterTimer temct = new TfgEpicsMCACounterTimer();
//		assertEquals(0, temct.getEpicsMcaNameList().size());
//		temct.setEpicsMcaNames(arrayListOf("one", "two"));
//		assertEquals(2, temct.getEpicsMcaNameList().size());
//	}
	
//	public void testGenericOEMoveables() {
//		GenericOE oe = new GenericOE();
//		assertEquals(0, oe.getMoveableList().size());
//		Moveable m = new SingleAxisLinearDOF();
//		oe.setMoveables(arrayListOf(m));
//		assertEquals(1, oe.getMoveableList().size());
//	}
	
	
	public void testRangeConverterNameProviderConverters() {
		RangeConverterNameProvider rcnp = new RangeConverterNameProvider("name", "converterName");
		assertEquals(0, rcnp.getConverterList().size());
		RangeandConverterNameHolder conv1 = new RangeandConverterNameHolder("one", "one", 1.0, 2.0);
		RangeandConverterNameHolder conv2 = new RangeandConverterNameHolder("two", "two", 3.0, 4.0);
		rcnp.setConverters(arrayListOf(conv1, conv2));
		assertEquals(2, rcnp.getConverterList().size());
	}
	
	private <T> T[] arrayOf(T... items) {
		return items;
	}
	
	private <T> ArrayList<T> arrayListOf(T...items) {
		ArrayList<T> list = new ArrayList<T>();
		for (T i : items) {
			list.add(i);
		}
		return list;
	}
	
}
