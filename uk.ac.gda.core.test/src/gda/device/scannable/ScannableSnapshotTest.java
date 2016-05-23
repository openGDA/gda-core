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

package gda.device.scannable;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.mockito.Mockito;

import gda.device.Scannable;
import gda.device.ScannableMotionUnits;
import gda.device.scannable.scannablegroup.ScannableGroup;

public class ScannableSnapshotTest {

	private Scannable generateMockScannable(String name,
			String[] inputNames,
			String[] extraNames,
			String[] outputFormat,
			boolean busy,
			String units,
			Object position) throws Exception {
		Scannable mockScn;
		if (units != null) {
			mockScn = Mockito.mock(ScannableMotionUnits.class);
			Mockito.when(((ScannableMotionUnits) mockScn).getUserUnits()).thenReturn(units);
		} else {
			mockScn = Mockito.mock(Scannable.class);
		}
		Mockito.when(mockScn.getName()).thenReturn(name);
		Mockito.when(mockScn.getPosition()).thenReturn(position);
		Mockito.when(mockScn.getInputNames()).thenReturn(inputNames);
		Mockito.when(mockScn.getExtraNames()).thenReturn(extraNames);
		Mockito.when(mockScn.getOutputFormat()).thenReturn(outputFormat);
		Mockito.when(mockScn.isBusy()).thenReturn(busy);
		return mockScn;
	}

	@Test
	public void testSimpleInitialization() throws Exception {
		Scannable scn = generateMockScannable(
				"scn", new String[] {"scn"}, new String[] {"extra_value"}, new String[] {"%5.5d"}, false, null, 12);
		ScannableSnapshot s = new ScannableSnapshot(scn);
		assertEquals("scn", s.name);
		assertArrayEquals(new String[] {"scn"}, s.inputNames);
		assertArrayEquals(new String[] {"extra_value"}, s.extraNames);
		assertArrayEquals(new String[] {"%5.5d"}, s.outputFormat);
		assertEquals(false, s.busy);
		assertArrayEquals(new String[] {""}, s.units);
	}

	@Test
	public void testScannableGroupInitialization() throws Exception {
		Scannable mot1 = generateMockScannable(
				"mot1", new String[] {"mot1"}, new String[] {}, new String[] {"%5.5d"}, false, "mm", 100);
		Scannable mot2 = generateMockScannable(
				"mot2", new String[] {"mot2"}, new String[] {}, new String[] {"%5.5d"}, true, "mm", 0);
		ScannableGroup motGroup = new ScannableGroup("motGroup", new Scannable[] {mot1, mot2});
		Scannable scn = generateMockScannable(
				"scn", new String[] {"scn"}, new String[] {"extra"}, new String[] {"%d", "%s"}, false, null, new Object[] {1, "test"});
		ScannableGroup group = new ScannableGroup("group", new Scannable[] {scn, motGroup});
		ScannableSnapshot s = new ScannableSnapshot(group);
		assertEquals("group", s.name);
		assertArrayEquals(new String[] {"scn", "mot1", "mot2"}, s.inputNames);
		assertArrayEquals(new String[] {"extra"}, s.extraNames);
		assertArrayEquals(new String[] {"%d", "%5.5d", "%5.5d", "%s"}, s.outputFormat);
		assertArrayEquals(new String[] {"", "mm", "mm"}, s.units);
		assertEquals(true, s.busy);
		assertArrayEquals(new Object[] {1, 100, 0, "test"}, (Object[]) s.lastPosition);
	}
}