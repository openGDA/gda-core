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

package uk.ac.diamond.daq.scanning;

import static org.eclipse.dawnsci.nexus.NexusBaseClass.NX_POSITIONER;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.nexus.NXpositioner;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusScanInfo;
import org.eclipse.dawnsci.nexus.builder.NexusObjectProvider;
import org.eclipse.scanning.api.IScannable;
import org.junit.Before;
import org.junit.Test;

import gda.device.ScannableMotionUnits;
import gda.device.enumpositioner.DummyEnumPositioner;
import gda.device.scannable.DummyScannable;

public class ScannableNexusWrapperTest {

	private IScannable<Object> scannable;

	@Before
	public void before() throws Exception {
		DummyScannable wrappedScannable = new DummyScannable("sax", 3.7);
		wrappedScannable.setLowerGdaLimits(0.5);
		wrappedScannable.setUpperGdaLimits(9.5);
		scannable = new ScannableNexusWrapper<>(wrappedScannable);
	}

	@Test
	public void testGetLevel() {
		assertThat(scannable.getLevel(), is(5));
	}

	@Test
	public void testSetLevel() {
		assertThat(scannable.getLevel(), is(5));
		scannable.setLevel(2);
		assertThat(scannable.getLevel(), is(2));
	}

	@Test
	public void testGetName() {
		assertThat(scannable.getName(), equalTo("sax"));
	}

	@Test
	public void testSetName() {
		assertThat(scannable.getName(), equalTo("sax"));
		scannable.setName("newName");
		assertThat(scannable.getName(), equalTo("newName"));
	}
	@Test
	public void testGetUnit() throws Exception {
		assertThat(scannable.getUnit(), nullValue());

		ScannableMotionUnits mockUnitsScannable = mock(ScannableMotionUnits.class);
		when(mockUnitsScannable.getUserUnits()).thenReturn("mm");
		IScannable<?> unitsScannable = new ScannableNexusWrapper<>(mockUnitsScannable);

		assertThat(unitsScannable.getUnit(), equalTo("mm"));
	}

	@Test
	public void testGetMinimum() {
		assertThat(scannable.getMinimum(), equalTo(0.5));
	}

	@Test
	public void testGetMaximum() {
		assertThat(scannable.getMaximum(), equalTo(9.5));
	}

	@Test
	public void testGetPermittedValues() throws Exception {
		assertThat(scannable.getPermittedValues(), nullValue());

		DummyEnumPositioner enumPositioner = new DummyEnumPositioner();
		enumPositioner.setPositions(Arrays.asList("One", "Two", "Three"));
		IScannable<?> enumScannable = new ScannableNexusWrapper<>(enumPositioner);

		assertThat(enumScannable.getPermittedValues(),
				equalTo(new String[] { "One", "Two", "Three" }));
	}

	@Test
	public void testGetPosition() throws Exception {
		assertThat(scannable.getPosition(), equalTo(3.7));
	}

	@Test
	public void testSetPosition() throws Exception {
		// Note: writing position to nexus file when setPosition is called as part of a scan
		// is tested fully in ScannableNexusWrapperScanTest
		assertThat(scannable.getPosition(), equalTo(3.7));
		scannable.setPosition(8.1);
		assertThat(scannable.getPosition(), equalTo(8.1));
	}

	@Test
	public void testGetFieldNames() throws Exception {
		DummyScannable multiFieldDummyScannable = new DummyScannable("multiField");
		multiFieldDummyScannable.setInputNames(new String[] { "input1", "input2", "input3" });
		multiFieldDummyScannable.setExtraNames(new String[] { "extra1", "extra2" });
		ScannableNexusWrapper<?> multiFieldScannable = new ScannableNexusWrapper<>(multiFieldDummyScannable);

		assertThat(multiFieldScannable.getFieldNames(false),
				contains("input1", "input2", "input3", "extra1", "extra2"));

		// test that the first field name is 'value' where the first input name of the
		// wrapped scannable is the same as the name of the scannable itself
		DummyScannable dummyScannable = new DummyScannable("sax");
		assertThat(dummyScannable.getInputNames(), equalTo(new String[] { "sax" }));
		ScannableNexusWrapper<?> simpleScannable = new ScannableNexusWrapper<>(dummyScannable);
		assertThat(simpleScannable.getFieldNames(false), contains("value"));
	}

	@Test
	public void testGetFieldNamesRecalculate() throws Exception {
		DummyScannable multiFieldDummyScannable = new DummyScannable("multiField");
		multiFieldDummyScannable.setInputNames(new String[] { "input1", "input2", "input3" });
		multiFieldDummyScannable.setExtraNames(new String[] { "extra1", "extra2" });
		ScannableNexusWrapper<?> multiFieldScannable = new ScannableNexusWrapper<>(multiFieldDummyScannable);

		assertThat(multiFieldScannable.getFieldNames(false),
				contains("input1", "input2", "input3", "extra1", "extra2"));

		multiFieldDummyScannable.setInputNames(new String[] { "newInput1", "newInput2" });
		multiFieldDummyScannable.setExtraNames(new String[] { "newExtra" });

		assertThat(multiFieldScannable.getFieldNames(false),
				contains("input1", "input2", "input3", "extra1", "extra2"));
		assertThat(multiFieldScannable.getFieldNames(true),
				contains("newInput1", "newInput2", "newExtra"));
	}

	@Test
	public void testGetNexusProvider() throws NexusException {
		// NOTE: nexus writing is more fully tested in ScannableNexusWrapperScanTest
		DummyScannable dummyScannable = new DummyScannable("xPos") {
			@Override
			public Object rawGetPosition() {
				return new Object[] { 1.0, 2.0, 3.0, "One", "Two" };
			}
		};
		dummyScannable.setInputNames(new String[] { "input1", "input2", "input3" });
		dummyScannable.setExtraNames(new String[] { "extra1", "extra2" });
		ScannableNexusWrapper<?> scannable = new ScannableNexusWrapper<>(dummyScannable);


		NexusScanInfo scanInfo = new NexusScanInfo(Arrays.asList("xPos", "yPos"));
		NexusObjectProvider<?> nexusObjectProvider = scannable.getNexusProvider(scanInfo);
		assertThat(nexusObjectProvider, notNullValue());
		assertThat(nexusObjectProvider.getName(), equalTo("xPos"));
		assertThat(nexusObjectProvider.getNexusBaseClass(), is(NX_POSITIONER));
		assertThat(nexusObjectProvider.getCategory(), nullValue());
		assertThat(nexusObjectProvider.getCollectionName(), nullValue());
		assertThat(nexusObjectProvider.getAxisDataFieldNames(),
				contains("input1", "value_demand"));
		assertThat(nexusObjectProvider.getDefaultAxisDataFieldName(), equalTo("value_demand"));

		NXpositioner nexusObject = (NXpositioner) nexusObjectProvider.getNexusObject();
		assertThat(nexusObject, notNullValue());
		assertThat(nexusObject.getNexusBaseClass(), is(NX_POSITIONER));
		assertThat(nexusObject.getNumberOfAttributes(), is(1));
		assertThat(nexusObject.getNumberOfGroupNodes(), is(0));
		assertThat(nexusObject.getNumberOfDataNodes(), is(7)); // name, input1,2,3, extra1,2, value_demand
		assertThat(nexusObject.getNameScalar(), equalTo("xPos"));
		List<String> fieldNames = scannable.getFieldNames(false);
		assertThat(fieldNames, contains("input1", "input2", "input3", "extra1", "extra2"));
		for (String fieldName : fieldNames) {
			DataNode valueDataNode = nexusObject.getDataNode(fieldName);
			assertThat(valueDataNode, notNullValue());
			assertThat(valueDataNode.getDataset(), notNullValue());
		}

		DataNode valueDemandDataNode = nexusObject.getDataNode("value_demand");
		assertThat(valueDemandDataNode, notNullValue());
		assertThat(valueDemandDataNode.getDataset(), notNullValue());
	}

}
