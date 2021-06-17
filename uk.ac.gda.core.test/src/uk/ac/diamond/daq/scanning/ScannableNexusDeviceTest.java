/*-
 * Copyright © 2021 Diamond Light Source Ltd.
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

import static gda.data.scan.nexus.device.ScannableNexusDevice.ATTR_NAME_GDA_FIELD_NAME;
import static gda.data.scan.nexus.device.ScannableNexusDevice.ATTR_NAME_GDA_SCANNABLE_NAME;
import static gda.data.scan.nexus.device.ScannableNexusDevice.ATTR_NAME_GDA_SCAN_ROLE;
import static gda.data.scan.nexus.device.ScannableNexusDevice.ATTR_NAME_LOCAL_NAME;
import static gda.data.scan.nexus.device.ScannableNexusDevice.ATTR_NAME_UNITS;
import static org.eclipse.dawnsci.nexus.NexusBaseClass.NX_POSITIONER;
import static org.eclipse.dawnsci.nexus.NexusConstants.NXCLASS;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.stream.Stream;

import org.eclipse.dawnsci.analysis.api.tree.Attribute;
import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.analysis.api.tree.NodeLink;
import org.eclipse.dawnsci.nexus.NXcollection;
import org.eclipse.dawnsci.nexus.NXobject;
import org.eclipse.dawnsci.nexus.NXpositioner;
import org.eclipse.dawnsci.nexus.NexusBaseClass;
import org.eclipse.dawnsci.nexus.NexusScanInfo;
import org.eclipse.dawnsci.nexus.NexusScanInfo.ScanRole;
import org.eclipse.dawnsci.nexus.NexusUtils;
import org.eclipse.dawnsci.nexus.builder.NexusObjectProvider;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.IDataset;
import org.junit.Before;
import org.junit.Test;

import gda.data.ServiceHolder;
import gda.data.scan.nexus.device.ScannableNexusDevice;
import gda.data.scan.nexus.device.ScannableNexusDeviceConfiguration;
import gda.data.scan.nexus.device.ScannableNexusDeviceConfigurationRegistry;
import gda.device.ScannableMotionUnits;

public class ScannableNexusDeviceTest {

	private static final String FIELD_NAME_VALUE_SET = NXpositioner.NX_VALUE + "_set";

	private static final String COLLECTION_NAME = "before_scan";
	private static final String SCANNABLE_NAME = "s1";

	private static final String[] INPUT_NAMES = { "input1", "input2", "input3" };
	private static final String[] EXTRA_NAMES = { "extra1", "extra2" };

	private static final Double[] LOWER_LIMITS = new Double[] { -1.0, -2.0, -3.0 };
	private static final Double[] UPPER_LIMITS = new Double[] { 1.0, 2.0, 3.0 };

	private ScannableNexusDevice<?> scannableNexusDevice = null;

	@Before
	public void before() throws Exception {
		// NOTE: nexus writing is more fully tested in ScannableNexusWrapperScanTest
		final ScannableMotionUnits mockScannable = mock(ScannableMotionUnits.class);
		when(mockScannable.getName()).thenReturn(SCANNABLE_NAME);
		final String[] inputNames = INPUT_NAMES;
		final String[] extraNames = EXTRA_NAMES;
		when(mockScannable.getInputNames()).thenReturn(inputNames);
		when(mockScannable.getExtraNames()).thenReturn(extraNames);
		final Object[] position = new Object[] { 1.0, 2.0, 3.0, "One", "Two" };
		when(mockScannable.getPosition()).thenReturn(position);
		when(mockScannable.getUserUnits()).thenReturn("nm");
		final Double[] lowerLimits = new Double[] { -1.0, -2.0, -3.0 };
		when(mockScannable.getLowerGdaLimits()).thenReturn(lowerLimits);
		final Double[] upperLimits = new Double[] { 1.0, 2.0, 3.0 };
		when(mockScannable.getUpperGdaLimits()).thenReturn(upperLimits);

		scannableNexusDevice = new ScannableNexusDevice<>(mockScannable);
		new ServiceHolder().setScannableNexusDeviceConfigurationRegistry(new ScannableNexusDeviceConfigurationRegistry());
	}

	@Test
	public void testGetNexusProvider() throws Exception {
		final NexusScanInfo scanInfo = new NexusScanInfo(List.of(SCANNABLE_NAME));
		final NexusObjectProvider<?> nexusObjectProvider = scannableNexusDevice.getNexusProvider(scanInfo);
		assertThat(nexusObjectProvider, is(notNullValue()));
		assertThat(nexusObjectProvider.getName(), is(equalTo(SCANNABLE_NAME)));
		assertThat(nexusObjectProvider.getNexusBaseClass(), is(NX_POSITIONER));
		assertThat(nexusObjectProvider.getCategory(), is(nullValue()));
		assertThat(nexusObjectProvider.getCollectionName(), is(nullValue()));
		assertThat(nexusObjectProvider.getAxisDataFieldNames(),
				contains("input1", FIELD_NAME_VALUE_SET));
		assertThat(nexusObjectProvider.getDefaultAxisDataFieldName(), is(equalTo(FIELD_NAME_VALUE_SET)));

		final String[] expectedFieldNames = Stream.of(INPUT_NAMES, EXTRA_NAMES)
				.flatMap(Stream::of).toArray(String[]::new);
		final String[] otherDataNodeNames = new String[] { NXpositioner.NX_NAME, FIELD_NAME_VALUE_SET,
				NXpositioner.NX_SOFT_LIMIT_MIN, NXpositioner.NX_SOFT_LIMIT_MAX };
		final String[] expectedDataNodeNames = Stream.of(expectedFieldNames, otherDataNodeNames)
				.flatMap(Stream::of).toArray(String[]::new);

		final NXpositioner nexusObject = (NXpositioner) nexusObjectProvider.getNexusObject();
		assertThat(nexusObject, notNullValue());
		assertThat(nexusObject.getNexusBaseClass(), is(NX_POSITIONER));
		assertThat(nexusObject.getAttributeNames(), containsInAnyOrder(NXCLASS, ATTR_NAME_GDA_SCANNABLE_NAME, ATTR_NAME_GDA_SCAN_ROLE));
		assertThat(nexusObject.getNumberOfAttributes(), is(3));
		assertThat(nexusObject.getGroupNodeNames(), is(empty()));
		assertThat(nexusObject.getNumberOfGroupNodes(), is(0));
		assertThat(nexusObject.getDataNodeNames(), containsInAnyOrder(expectedDataNodeNames));
		assertThat(nexusObject.getNumberOfDataNodes(), is(expectedDataNodeNames.length));
		assertThat(nexusObject.getNameScalar(), is(equalTo(SCANNABLE_NAME)));
		assertThat(nexusObject.getAttrString(null, ATTR_NAME_GDA_SCANNABLE_NAME), is(equalTo(SCANNABLE_NAME)));
		assertThat(nexusObject.getAttrString(null, ATTR_NAME_GDA_SCAN_ROLE),
				equalTo(ScanRole.SCANNABLE.toString().toLowerCase()));

		assertThat(nexusObject.getDataNodeNames(), hasItems(expectedFieldNames));
		for (String fieldName : expectedFieldNames) {
			final DataNode valueDataNode = nexusObject.getDataNode(fieldName);
			assertThat(valueDataNode, notNullValue());
			assertThat(nexusObject.getAttrString(fieldName, ATTR_NAME_LOCAL_NAME), is(equalTo(SCANNABLE_NAME + "." + fieldName)));
			assertThat(nexusObject.getAttrString(fieldName, ATTR_NAME_GDA_FIELD_NAME), is(equalTo(fieldName)));
			assertThat(valueDataNode.getDataset(), is(notNullValue()));
			assertThat(nexusObject.getAttrString(fieldName, ATTR_NAME_UNITS), is(equalTo("nm")));
		}

		final DataNode valueDemandDataNode = nexusObject.getDataNode(FIELD_NAME_VALUE_SET);
		assertThat(valueDemandDataNode, is(notNullValue()));
		assertThat(valueDemandDataNode.getDataset(), is(notNullValue()));

		final DataNode softLimitMin = nexusObject.getDataNode(NXpositioner.NX_SOFT_LIMIT_MIN);
		assertThat(softLimitMin, is(notNullValue()));
		final IDataset softLimitMinDataset = softLimitMin.getDataset().getSlice();
		assertThat(softLimitMinDataset, is(equalTo(DatasetFactory.createFromObject(LOWER_LIMITS))));

		final DataNode softLimitMax = nexusObject.getDataNode(NXpositioner.NX_SOFT_LIMIT_MAX);
		assertThat(softLimitMax, is(notNullValue()));
		final IDataset softLimitMaxDataset = softLimitMax.getDataset().getSlice();
		assertThat(softLimitMaxDataset, is(equalTo(DatasetFactory.createFromObject(UPPER_LIMITS))));
	}

	@Test
	public void testGetNexusProvider_withConfiguration() throws Exception {
		final ScannableNexusDeviceConfiguration config = new ScannableNexusDeviceConfiguration();
		config.setScannableName(SCANNABLE_NAME);
		config.setNexusCategory(NexusBaseClass.NX_SAMPLE);
		config.setNexusBaseClass(NexusBaseClass.NX_COLLECTION);
		config.setCollectionName(COLLECTION_NAME);

		final String[] outputFieldPaths = {
				"theta:NXpositioner/" + NXpositioner.NX_VALUE,
				"phi:NXpositioner/" + NXpositioner.NX_VALUE,
				"phi:NXpositioner/extra",
				"foo:NXpositioner/extra",
				"bar:NXsensor/value"
		};
		config.setFieldPaths(outputFieldPaths);
		final String[] units = { "mm", "mm", "rad" };
		config.setUnits(units);
		config.register();

		final NexusScanInfo scanInfo = new NexusScanInfo(List.of(SCANNABLE_NAME));
		final NexusObjectProvider<?> nexusObjectProvider = scannableNexusDevice.getNexusProvider(scanInfo);
		assertThat(nexusObjectProvider, is(notNullValue()));
		assertThat(nexusObjectProvider.getName(), is(equalTo(SCANNABLE_NAME)));
		assertThat(nexusObjectProvider.getNexusBaseClass(), is(NexusBaseClass.NX_COLLECTION));
		assertThat(nexusObjectProvider.getCategory(), is(NexusBaseClass.NX_SAMPLE));
		assertThat(nexusObjectProvider.getCollectionName(), is(equalTo(COLLECTION_NAME)));
		assertThat(nexusObjectProvider.getAxisDataFieldNames(),
				contains("theta:NXpositioner/" + NXpositioner.NX_VALUE, FIELD_NAME_VALUE_SET));
		assertThat(nexusObjectProvider.getDefaultAxisDataFieldName(), is(equalTo(FIELD_NAME_VALUE_SET)));

		final NXcollection nexusObject = (NXcollection) nexusObjectProvider.getNexusObject();
		assertThat(nexusObject, notNullValue());
		assertThat(nexusObject.getNexusBaseClass(), is(NexusBaseClass.NX_COLLECTION));
		assertThat(nexusObject.getAttributeNames(), containsInAnyOrder(NXCLASS, ATTR_NAME_GDA_SCANNABLE_NAME, ATTR_NAME_GDA_SCAN_ROLE));
		assertThat(nexusObject.getNumberOfAttributes(), is(3));
		assertThat(nexusObject.getGroupNodeNames(), containsInAnyOrder("theta", "phi", "foo", "bar"));
		assertThat(nexusObject.getNumberOfGroupNodes(), is(4));

		final String[] expectedDataNodeNames = new String[] { NXpositioner.NX_NAME, FIELD_NAME_VALUE_SET };
		assertThat(nexusObject.getDataNodeNames(), containsInAnyOrder(expectedDataNodeNames));
		assertThat(nexusObject.getNumberOfDataNodes(), is(expectedDataNodeNames.length));

		assertThat(nexusObject.getDataNode(NXpositioner.NX_NAME).getString(), is(equalTo(SCANNABLE_NAME))); // TODO: should we write 'name' field for nexus classes other than NXpositioner
		assertThat(nexusObject.getAttrString(null, ATTR_NAME_GDA_SCANNABLE_NAME), is(equalTo(SCANNABLE_NAME)));
		assertThat(nexusObject.getAttrString(null, ATTR_NAME_GDA_SCAN_ROLE),
				equalTo(ScanRole.SCANNABLE.toString().toLowerCase()));

		final String[] inputFieldNames = Stream.of(INPUT_NAMES, EXTRA_NAMES).flatMap(Stream::of).toArray(String[]::new);

		assertThat(scannableNexusDevice.getOutputFieldPaths(), arrayContaining(outputFieldPaths));
		for (int i = 0; i < inputFieldNames.length; i++) {
			final String inputFieldName = inputFieldNames[i];
			final String outputFieldPath = outputFieldPaths[i];
			final DataNode valueDataNode = getDataNode(nexusObject, outputFieldPath);
			assertThat(valueDataNode, notNullValue());
			final Attribute localNameAttr = valueDataNode.getAttribute(ATTR_NAME_LOCAL_NAME);
			assertThat(localNameAttr, is(notNullValue()));
			assertThat(localNameAttr.getFirstElement(), is(equalTo(SCANNABLE_NAME + "." + inputFieldName)));
			final Attribute gdaFieldNameAttr = valueDataNode.getAttribute(ATTR_NAME_GDA_FIELD_NAME);
			assertThat(gdaFieldNameAttr, is(notNullValue()));
			assertThat(gdaFieldNameAttr.getFirstElement(), is(equalTo(inputFieldName)));

			final String expectedUnits = i < units.length ? units[i] : "nm";
			final Attribute unitsAttr = valueDataNode.getAttribute(ATTR_NAME_UNITS);
			assertThat(unitsAttr, is(notNullValue()));
			assertThat(unitsAttr.getFirstElement(), is(equalTo(expectedUnits)));
		}

		final DataNode valueDemandDataNode = nexusObject.getDataNode(FIELD_NAME_VALUE_SET);
		assertThat(valueDemandDataNode, is(notNullValue()));
		assertThat(valueDemandDataNode.getDataset(), is(notNullValue()));
	}

	private DataNode getDataNode(NXobject root, String path) {
		final String plainPath = NexusUtils.stripAugmentedPath(path);
		final NodeLink link = root.findNodeLink(plainPath);
		return link != null && link.isDestinationData() ? (DataNode) link.getDestination() : null;
	}

}
