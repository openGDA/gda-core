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

package gda.rcp.views.dashboard;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.mockito.Mockito;

import gda.device.Scannable;
import gda.device.ScannableMotionUnits;
import gda.device.scannable.ScannableSnapshot;
import gda.device.scannable.scannablegroup.ScannableGroup;

class DummySnapshotProvider implements ScannableSnapshotProvider {

	public ScannableSnapshot snapshot;
	public String[] units;

	DummySnapshotProvider(ScannableSnapshot snapshot) {
		this.snapshot = snapshot;
	}

	@Override
	public ScannableSnapshot getSnapshot(String name) throws Exception {
		return snapshot;
	}
}

public class ScannableObjectTest {

	@Test
	public void testNameSet() {
		ScannableObject so = new ScannableObject("test", new DummySnapshotProvider(null));
		assertEquals("test", so.getName());
	}

	@Test
	public void testNullPosition() throws Exception {
		Scannable m = Mockito.mock(Scannable.class);
		when(m.getInputNames()).thenReturn(new String[] {"name"});
		when(m.getExtraNames()).thenReturn(new String[] {"extra"});
		when(m.getOutputFormat()).thenReturn(new String[] {"%5.5g", "%d"});
		when(m.getPosition()).thenReturn(null);
		ScannableSnapshot si = new ScannableSnapshot(m);
		DummySnapshotProvider p = new DummySnapshotProvider(si);
		ScannableObject so = new ScannableObject("name", p);
		so.refresh();
		assertEquals("", so.getOutput());
	}

	@Test
	public void testSimpleScannableOutput() throws Exception {
		Scannable m = Mockito.mock(Scannable.class);
		when(m.getInputNames()).thenReturn(new String[] {"name"});
		when(m.getExtraNames()).thenReturn(new String[] {});
		when(m.getOutputFormat()).thenReturn(new String[] {"%5.5g"});
		when(m.getPosition()).thenReturn(12.0);
		ScannableSnapshot si = new ScannableSnapshot(m);
		DummySnapshotProvider p = new DummySnapshotProvider(si);
		ScannableObject so = new ScannableObject("name", p);
		so.refresh();
		assertEquals("12.000", so.getOutput());
	}

	@Test
	public void testScannableInputNameNotName() throws Exception {
		Scannable m = Mockito.mock(Scannable.class);
		when(m.getName()).thenReturn("name");
		when(m.getInputNames()).thenReturn(new String[] {"input_name"});
		when(m.getExtraNames()).thenReturn(new String[] {});
		when(m.getOutputFormat()).thenReturn(new String[] {"%5.5g"});
		when(m.getPosition()).thenReturn(12.0);
		ScannableSnapshot si = new ScannableSnapshot(m);
		DummySnapshotProvider p = new DummySnapshotProvider(si);
		ScannableObject so = new ScannableObject("name", p);
		so.refresh();
		assertEquals("input_name: 12.000", so.getOutput());
	}

	@Test
	public void testMultipleNamesScannableOutput() throws Exception {
		Scannable m = Mockito.mock(Scannable.class);
		when(m.getInputNames()).thenReturn(new String[] {"name1", "name2"});
		when(m.getExtraNames()).thenReturn(new String[] {"extra"});
		when(m.getOutputFormat()).thenReturn(new String[] {"%5.5g", "%5.5g", "%d"});
		when(m.getPosition()).thenReturn(new Object[] {12., 2., 1});
		ScannableSnapshot si = new ScannableSnapshot(m);
		DummySnapshotProvider p = new DummySnapshotProvider(si);
		ScannableObject so = new ScannableObject("name", p);
		so.refresh();
		assertEquals("name1: 12.000,  name2: 2.0000,  extra: 1", so.getOutput());
	}

	@Test
	public void testScannableUnits() throws Exception {
		ScannableMotionUnits m = Mockito.mock(ScannableMotionUnits.class);
		when(m.getUserUnits()).thenReturn("mm");
		when(m.getInputNames()).thenReturn(new String[] {"name"});
		when(m.getExtraNames()).thenReturn(new String[] {});
		when(m.getOutputFormat()).thenReturn(new String[] {"%d"});
		when(m.getPosition()).thenReturn(12);
		ScannableSnapshot si = new ScannableSnapshot(m);
		DummySnapshotProvider p = new DummySnapshotProvider(si);
		ScannableObject so = new ScannableObject("name", p);
		so.refresh();
		assertEquals("12 mm", so.getOutput());
	}

	@Test
	public void testScannableGroup() throws Exception {
		Scannable m1 = Mockito.mock(Scannable.class);
		when(m1.getName()).thenReturn("m1");
		when(m1.getInputNames()).thenReturn(new String[] {"m1_input_1"});
		when(m1.getExtraNames()).thenReturn(new String[] {});
		when(m1.getOutputFormat()).thenReturn(new String[] {"%d"});
		when(m1.getPosition()).thenReturn(12);

		ScannableMotionUnits m2 = Mockito.mock(ScannableMotionUnits.class);
		when(m2.getName()).thenReturn("m2");
		when(m2.getUserUnits()).thenReturn("mm");
		when(m2.getInputNames()).thenReturn(new String[] {"m2_input_1"});
		when(m2.getExtraNames()).thenReturn(new String[] {});
		when(m2.getOutputFormat()).thenReturn(new String[] {"%5.5g", "%d"});
		when(m2.getPosition()).thenReturn(new Object[] {12.0});

		Scannable m3 = Mockito.mock(Scannable.class);
		when(m3.getName()).thenReturn("m3");
		when(m3.getInputNames()).thenReturn(new String[] {"m3_input_1", "m3_input_2"});
		when(m3.getExtraNames()).thenReturn(new String[] {});
		when(m3.getOutputFormat()).thenReturn(new String[] {"%5.5g","%5.5g"});
		when(m3.getPosition()).thenReturn(new Double [] {1., 2.});

		ScannableGroup g = new ScannableGroup("group", new Scannable[] {m1, m2, m3});
		DummySnapshotProvider p = new DummySnapshotProvider(new ScannableSnapshot(g));
		ScannableObject so = new ScannableObject("group", p);
		so.refresh();
		assertEquals("m1_input_1: 12,  m2_input_1: 12.000 mm,  m3_input_1: 1.0000,  m3_input_2: 2.0000",
				so.getOutput());
	}
}
