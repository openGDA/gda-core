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
import static gda.data.scan.nexus.device.ScannableNexusDevice.FIELD_NAME_NAME;
import static gda.data.scan.nexus.device.ScannableNexusDevice.FIELD_NAME_VALUE_SET;
import static org.eclipse.dawnsci.nexus.NexusBaseClass.NX_POSITIONER;
import static org.eclipse.dawnsci.nexus.NexusConstants.NXCLASS;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;
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
import org.eclipse.january.DatasetException;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Iterables;

import gda.data.ServiceHolder;
import gda.data.scan.nexus.device.ScannableNexusDevice;
import gda.data.scan.nexus.device.ScannableNexusDeviceConfiguration;
import gda.data.scan.nexus.device.ScannableNexusDeviceConfigurationRegistry;
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.ScannableMotionUnits;

public class ScannableNexusDeviceTest {

	private static final String COLLECTION_NAME = "before_scan";
	private static final String SCANNABLE_NAME = "s1";
	private static final String EXPECTED_UNITS = "nm";

	private ScannableNexusDevice<?> scannableNexusDevice = null;

	private Scannable scannable;

	private String[] inputNames;
	private String[] extraNames;
	private Double[] lowerLimits;
	private Double[] upperLimits;

	@Before
	public void before() throws Exception {
		new ServiceHolder().setScannableNexusDeviceConfigurationRegistry(new ScannableNexusDeviceConfigurationRegistry());
	}

	private ScannableNexusDevice<?> createScannableNexusDevice(int numInputNames, int numExtraNames) throws DeviceException {
		scannable = createScannable(numInputNames, numExtraNames);
		return new ScannableNexusDevice<>(scannable);
	}

	private ScannableMotionUnits createScannable(int numInputNames, int numExtraNames)
			throws DeviceException {
		final ScannableMotionUnits mockScannable = mock(ScannableMotionUnits.class);
		when(mockScannable.getName()).thenReturn(SCANNABLE_NAME);

		inputNames = IntStream.rangeClosed(1, numInputNames).mapToObj(i -> "input" + i).toArray(String[]::new);
		when(mockScannable.getInputNames()).thenReturn(inputNames);
		extraNames = IntStream.rangeClosed(1, numExtraNames).mapToObj(i -> "extra" + i).toArray(String[]::new);
		when(mockScannable.getExtraNames()).thenReturn(extraNames);

		final Object[] position = Stream.concat(IntStream.rangeClosed(1, numInputNames).mapToObj(Double::valueOf),
				IntStream.rangeClosed(1, numExtraNames).mapToObj(i -> i + 100).map(Double::valueOf)).toArray();
		when(mockScannable.getPosition()).thenReturn(position);
		when(mockScannable.getUserUnits()).thenReturn(EXPECTED_UNITS);
		lowerLimits = IntStream.rangeClosed(1, numInputNames).map(i -> -i).mapToObj(Double::valueOf).toArray(Double[]::new);
		when(mockScannable.getLowerGdaLimits()).thenReturn(lowerLimits);
		upperLimits = IntStream.rangeClosed(1, numInputNames).mapToObj(Double::valueOf).toArray(Double[]::new);
		when(mockScannable.getUpperGdaLimits()).thenReturn(upperLimits);
		return mockScannable;
	}

	@Test
	public void testGetNexusProviders_singleInputNoExtra() throws Exception {
		testGetNexusProviders(1, 0);
	}

	@Test
	public void testGetNexusProviders_singleInputSingleExtra() throws Exception {
		testGetNexusProviders(1, 1);
	}

	@Test
	public void testGetNexusProviders_singleInputMultiExtra() throws Exception {
		testGetNexusProviders(1, 3);
	}

	@Test
	public void testGetNexusProviders_multiInputNoExtra() throws Exception {
		testGetNexusProviders(3, 0);
	}

	@Test
	public void testGetNexusProviders_multiInputSingleExtra() throws Exception {
		testGetNexusProviders(3, 1);
	}

	@Test
	public void testGetNexusProviders_multiInputMultiExtra() throws Exception {
		testGetNexusProviders(3, 3);
	}

	public void testGetNexusProviders(int numInputFields, int numExtraFields) throws Exception {
		scannableNexusDevice = createScannableNexusDevice(numInputFields, numExtraFields);

		final boolean singleInputField = numInputFields == 1;
		final NexusScanInfo scanInfo = new NexusScanInfo(List.of(SCANNABLE_NAME));
		final List<NexusObjectProvider<?>> nexusObjectProviders = scannableNexusDevice.getNexusProviders(scanInfo);
		assertThat(nexusObjectProviders.size(), is(equalTo(numInputFields + (singleInputField ? 0 : 1))));

		// check the NXpositioner for each input field
		for (int i = 0; i < numInputFields; i++) {
			@SuppressWarnings("unchecked")
			final NexusObjectProvider<NXpositioner> positionerProvider = (NexusObjectProvider<NXpositioner>) nexusObjectProviders.get(i);
			checkNXPositioner(positionerProvider, i);
		}

		// in the case of multiple input fields, an NXcollection is created with links to the input fields and any extra fields
		if (!singleInputField) {
			checkNXcollection(nexusObjectProviders);
		}
	}

	private void checkNXPositioner(final NexusObjectProvider<NXpositioner> positionerProvider, int inputFieldIndex) throws DatasetException {
		final boolean singleInputField = inputNames.length == 1;
		final String inputName = inputNames[inputFieldIndex];
		final String positionerName = SCANNABLE_NAME + (singleInputField ? "" : "." + inputName);

		assertThat(positionerProvider, is(notNullValue()));
		assertThat(positionerProvider.getName(), is(positionerName));
		assertThat(positionerProvider.getNexusBaseClass(), is(NX_POSITIONER));
		assertThat(positionerProvider.getCategory(), is(nullValue()));
		assertThat(positionerProvider.getCollectionName(), is(nullValue()));

		if (singleInputField) {
			final String[] expectedAxisFieldsNames = new String[] { NXpositioner.NX_VALUE, FIELD_NAME_VALUE_SET };
			assertThat(positionerProvider.getAxisDataFieldNames(), contains(expectedAxisFieldsNames));
			assertThat(positionerProvider.getDefaultAxisDataFieldName(),is(equalTo(FIELD_NAME_VALUE_SET)));
		} else {
			// in the multi-field case the NXcollection is used to get the DataNodes to add to NXdata groups instead.
			assertThat(positionerProvider.getAxisDataFieldNames(), is(empty()));
			assertThat(positionerProvider.getDefaultAxisDataFieldName(), is(nullValue()));
		}

		// extra names are only added to the NXpositioner in the case of a single input name, otherwise they are added to the NXcollection
		final Set<String> expectedDataNodeNameSet = new HashSet<>(List.of(NXpositioner.NX_VALUE,
				NXpositioner.NX_NAME, NXpositioner.NX_SOFT_LIMIT_MIN, NXpositioner.NX_SOFT_LIMIT_MAX));
		if (singleInputField) {
			expectedDataNodeNameSet.addAll(Arrays.asList(extraNames));
			expectedDataNodeNameSet.add(FIELD_NAME_VALUE_SET);
		}
		final String[] expectedDataNodeNames = expectedDataNodeNameSet.toArray(String[]::new);

		final NXpositioner positioner = positionerProvider.getNexusObject();
		assertThat(positioner, notNullValue());
		assertThat(positioner.getNexusBaseClass(), is(NX_POSITIONER));
		assertThat(positioner.getGroupNodeNames(), is(empty()));

		assertThat(positioner.getAttributeNames(), containsInAnyOrder(NXCLASS, ATTR_NAME_GDA_SCANNABLE_NAME, ATTR_NAME_GDA_SCAN_ROLE));
		assertThat(positioner.getAttrString(null, ATTR_NAME_GDA_SCANNABLE_NAME), is(equalTo(SCANNABLE_NAME)));
		assertThat(positioner.getAttrString(null, ATTR_NAME_GDA_SCAN_ROLE), is(equalTo(ScanRole.SCANNABLE.toString().toLowerCase())));

		assertThat(positioner.getDataNodeNames(), containsInAnyOrder(expectedDataNodeNames));
		assertThat(positioner.getNameScalar(), is(equalTo(SCANNABLE_NAME + (singleInputField ? "" : "." + inputName))));
		final DataNode valueDataNode = positioner.getDataNode(NXpositioner.NX_VALUE);
		assertThat(valueDataNode, is(notNullValue()));
		assertThat(valueDataNode.getDataset(), is(notNullValue()));
		assertThat(valueDataNode.getAttributeNames(), containsInAnyOrder(ATTR_NAME_LOCAL_NAME, ATTR_NAME_GDA_FIELD_NAME, ATTR_NAME_UNITS));
		assertThat(positioner.getAttrString(NXpositioner.NX_VALUE, ATTR_NAME_GDA_FIELD_NAME), is(equalTo(inputName)));
		assertThat(positioner.getAttrString(NXpositioner.NX_VALUE, ATTR_NAME_LOCAL_NAME), is(equalTo(SCANNABLE_NAME + "." + inputName)));
		assertThat(positioner.getAttrString(NXpositioner.NX_VALUE, ATTR_NAME_UNITS), is(EXPECTED_UNITS));

		if (singleInputField) {
			for (String extraName : extraNames) {
				final DataNode extraFieldDataNode = positioner.getDataNode(extraName);
				assertThat(extraFieldDataNode, notNullValue());
				assertThat(extraFieldDataNode.getDataset(), is(notNullValue()));
				assertThat(extraFieldDataNode.getAttributeNames(), containsInAnyOrder(ATTR_NAME_LOCAL_NAME, ATTR_NAME_GDA_FIELD_NAME, ATTR_NAME_UNITS));
				assertThat(positioner.getAttrString(extraName, ATTR_NAME_LOCAL_NAME), is(equalTo(SCANNABLE_NAME + "." + extraName)));
				assertThat(positioner.getAttrString(extraName, ATTR_NAME_GDA_FIELD_NAME), is(equalTo(extraName)));
				assertThat(positioner.getAttrString(extraName, ATTR_NAME_UNITS), is(equalTo(EXPECTED_UNITS)));
			}

			final DataNode valueDemandDataNode = positioner.getDataNode(FIELD_NAME_VALUE_SET);
			assertThat(valueDemandDataNode, is(notNullValue()));
			assertThat(valueDemandDataNode.getDataset(), is(notNullValue()));
		}

		assertThat(positioner.getSoft_limit_minScalar().doubleValue(), is(closeTo(lowerLimits[inputFieldIndex], 1e-15)));
		assertThat(positioner.getSoft_limit_maxScalar().doubleValue(), is(closeTo(upperLimits[inputFieldIndex], 1e-15)));
	}

	private void checkNXcollection(final List<NexusObjectProvider<?>> nexusObjectProviders) {
		@SuppressWarnings("unchecked")
		final NexusObjectProvider<NXcollection> collectionProvider = (NexusObjectProvider<NXcollection>) Iterables.getLast(nexusObjectProviders);
		assertThat(collectionProvider, is(notNullValue()));
		assertThat(collectionProvider.getName(), is(equalTo(SCANNABLE_NAME)));
		assertThat(collectionProvider.getNexusBaseClass(), is(NexusBaseClass.NX_COLLECTION));
		assertThat(collectionProvider.getCategory(), is(NexusBaseClass.NX_INSTRUMENT));
		assertThat(collectionProvider.getCollectionName(), is(nullValue()));
		assertThat(collectionProvider.getAxisDataFieldNames(), contains(inputNames));
		assertThat(collectionProvider.getDefaultAxisDataFieldName(), is(inputNames[0]));

		final String[] expectedDataNodeNames = Stream.of(inputNames, extraNames, new String[] { FIELD_NAME_NAME })
				.flatMap(Stream::of).toArray(String[]::new);
		final NXcollection collection = collectionProvider.getNexusObject();
		assertThat(collection, is(notNullValue()));
		assertThat(collection.getNexusBaseClass(), is(NexusBaseClass.NX_COLLECTION));
		assertThat(collection.getAttributeNames(), containsInAnyOrder(NXCLASS, ATTR_NAME_GDA_SCANNABLE_NAME, ATTR_NAME_GDA_SCAN_ROLE));
		assertThat(collection.getGroupNodeNames(), is(empty()));
		assertThat(collection.getDataNodeNames(), containsInAnyOrder(expectedDataNodeNames));
		assertThat(collection.getAttrString(null, ATTR_NAME_GDA_SCANNABLE_NAME), is(equalTo(SCANNABLE_NAME)));
		assertThat(collection.getAttrString(null, ATTR_NAME_GDA_SCAN_ROLE), is(equalTo(ScanRole.SCANNABLE.toString().toLowerCase())));

		// check links to input fields
		for (int i = 0; i < inputNames.length; i++) {
			final String inputName = inputNames[i];
			final DataNode inputFieldDataNode = collection.getDataNode(inputName);
			assertThat(inputFieldDataNode, is(notNullValue()));
			assertThat(inputFieldDataNode, is(sameInstance(
					nexusObjectProviders.get(i).getNexusObject().getDataNode(NXpositioner.NX_VALUE))));
		}

		for (int i = 0; i < extraNames.length; i++) {
			final String extraName = extraNames[i];
			final DataNode extraFieldDataNode = collection.getDataNode(extraName);
			assertThat(extraFieldDataNode, is(notNullValue()));
			assertThat(extraFieldDataNode.getAttributeNames(), containsInAnyOrder(ATTR_NAME_GDA_FIELD_NAME, ATTR_NAME_LOCAL_NAME, ATTR_NAME_UNITS));
			assertThat(collection.getAttrString(extraName, ATTR_NAME_GDA_FIELD_NAME), is(equalTo(extraName)));
			assertThat(collection.getAttrString(extraName, ATTR_NAME_LOCAL_NAME), is(equalTo(SCANNABLE_NAME + "." + extraName)));
			assertThat(collection.getAttrString(extraName, ATTR_NAME_UNITS), is(equalTo(EXPECTED_UNITS)));
		}
	}

	@Test
	public void testGetNexusProvider_withConfiguration() throws Exception {
		scannableNexusDevice = createScannableNexusDevice(3, 2);

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
		final List<NexusObjectProvider<?>> nexusObjectProviders = scannableNexusDevice.getNexusProviders(scanInfo);
		assertThat(nexusObjectProviders.size(), is(1));
		final NexusObjectProvider<?> nexusObjectProvider = nexusObjectProviders.get(0);
		assertThat(nexusObjectProvider, is(notNullValue()));
		assertThat(nexusObjectProvider.getName(), is(equalTo(SCANNABLE_NAME)));
		assertThat(nexusObjectProvider.getNexusBaseClass(), is(NexusBaseClass.NX_COLLECTION));
		assertThat(nexusObjectProvider.getCategory(), is(NexusBaseClass.NX_SAMPLE));
		assertThat(nexusObjectProvider.getCollectionName(), is(equalTo(COLLECTION_NAME)));
		final String[] expectedAxisDataFieldNames = Stream.concat(
				Arrays.stream(outputFieldPaths).limit(scannable.getInputNames().length),
				Stream.of(FIELD_NAME_VALUE_SET)).toArray(String[]::new);
		assertThat(nexusObjectProvider.getAxisDataFieldNames(), contains(expectedAxisDataFieldNames));
		assertThat(nexusObjectProvider.getDefaultAxisDataFieldName(), is(equalTo(FIELD_NAME_VALUE_SET)));

		final NXcollection nexusObject = (NXcollection) nexusObjectProvider.getNexusObject();
		assertThat(nexusObject, notNullValue());
		assertThat(nexusObject.getNexusBaseClass(), is(NexusBaseClass.NX_COLLECTION));
		assertThat(nexusObject.getAttributeNames(), containsInAnyOrder(NXCLASS, ATTR_NAME_GDA_SCANNABLE_NAME, ATTR_NAME_GDA_SCAN_ROLE));
		assertThat(nexusObject.getNumberOfAttributes(), is(3));
		assertThat(nexusObject.getGroupNodeNames(), containsInAnyOrder("theta", "phi", "foo", "bar"));
		assertThat(nexusObject.getNumberOfGroupNodes(), is(4));

		assertThat(nexusObject.getDataNodeNames(), contains(NXpositioner.NX_NAME));
		assertThat(nexusObject.getDataNode(NXpositioner.NX_NAME).getString(), is(equalTo(SCANNABLE_NAME))); // TODO: should we write 'name' field for nexus classes other than NXpositioner
		assertThat(nexusObject.getAttrString(null, ATTR_NAME_GDA_SCANNABLE_NAME), is(equalTo(SCANNABLE_NAME)));
		assertThat(nexusObject.getAttrString(null, ATTR_NAME_GDA_SCAN_ROLE),
				equalTo(ScanRole.SCANNABLE.toString().toLowerCase()));

		final String[] inputFieldNames = Stream.of(scannable.getInputNames(), scannable.getExtraNames()).flatMap(Stream::of).toArray(String[]::new);

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
	}

	private DataNode getDataNode(NXobject root, String path) {
		final String plainPath = NexusUtils.stripAugmentedPath(path);
		final NodeLink link = root.findNodeLink(plainPath);
		return link != null && link.isDestinationData() ? (DataNode) link.getDestination() : null;
	}

}
