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

import static gda.MockFactory.createMockScannableMotionUnits;
import static gda.data.scan.nexus.device.BeforeScanSnapshotWriter.BEFORE_SCAN_COLLECTION_NAME;
import static gda.data.scan.nexus.device.GDADeviceNexusConstants.ATTRIBUTE_NAME_DECIMALS;
import static gda.data.scan.nexus.device.GDADeviceNexusConstants.ATTRIBUTE_NAME_UNITS;
import static gda.data.scan.nexus.device.GDADeviceNexusConstants.PROPERTY_VALUE_WRITE_DECIMALS;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.dawnsci.analysis.api.tree.Attribute;
import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.analysis.api.tree.GroupNode;
import org.eclipse.dawnsci.nexus.NXcollection;
import org.eclipse.dawnsci.nexus.NexusBaseClass;
import org.eclipse.dawnsci.nexus.NexusConstants;
import org.eclipse.dawnsci.nexus.NexusScanInfo;
import org.eclipse.dawnsci.nexus.builder.NexusObjectProvider;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.IDataset;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import gda.MockFactory;
import gda.TestHelpers;
import gda.configuration.properties.LocalProperties;
import gda.data.scan.nexus.device.BeforeScanSnapshotWriter;
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.ScannableMotionUnits;
import gda.device.scannable.ScannableUtils;
import gda.factory.Factory;
import gda.factory.Finder;

class BeforeScanSnapshotWriterTest {

	private static final String[] NO_FIELD_NAMES = new String[0];
	private static final int[] SCALAR_SHAPE = new int[0];

	private NexusScanInfo scanInfo;
	private Random random;
	private Set<String> additionalScannableNames;

	@BeforeAll
	public static void setUpClass() {
		LocalProperties.set(PROPERTY_VALUE_WRITE_DECIMALS, true);
	}

	@AfterAll
	public static void tearDownClass() {
		LocalProperties.clearProperty(PROPERTY_VALUE_WRITE_DECIMALS);
	}

	@BeforeEach
	public void setUp() throws Exception {
		random = new Random(12345l);
		scanInfo = new NexusScanInfo();

		final Factory factory = TestHelpers.createTestFactory();
		Finder.addFactory(factory);

		final List<Scannable> scannables = new ArrayList<>();
		scannables.add(createMockScannableMotionUnits("xPos", random.nextDouble(), "mm"));
		scannables.add(createMockScannableMotionUnits("yPos", random.nextDouble(), "mm"));
		scannables.stream().forEach(factory::addFindable);
		scanInfo.setScannableNames(scannables.stream().map(Scannable::getName).collect(toList()));

		final List<Scannable> perPointMonitors = new ArrayList<>();
		perPointMonitors.add(createMockScannableMotionUnits("mon1", random.nextDouble(), "mm"));
		perPointMonitors.add(createMockScannable("mon2", 3, 0));
		perPointMonitors.add(createMockScannable("mon3", 1, 2));
		perPointMonitors.stream().forEach(factory::addFindable);
		scanInfo.setPerPointMonitorNames(perPointMonitors.stream().map(Scannable::getName).collect(toSet()));

		final List<Scannable> perScanMonitors = new ArrayList<>();
		perScanMonitors.add(createMockScannable("meta1", 1, 0));
		perScanMonitors.add(createMockScannable("meta2", 3, 0));
		perScanMonitors.add(createMockScannable("meta3", 1, 1));
		perScanMonitors.add(createMockScannable("meta4", 1, 3));
		perScanMonitors.add(createMockScannable("meta5", 3, 3));
		perScanMonitors.add(createMockScannable("meta6", 0, 2));
		perScanMonitors.add(createStringScannable("strMeta"));
		// TODO: checkScannable does not currently support checking scannables with null (missing) fields.
		//perScanMonitors.add(createScannableWithNullFieldValue("nullFieldMeta"));
		perScanMonitors.stream().forEach(factory::addFindable);
		scanInfo.setPerScanMonitorNames(perScanMonitors.stream().map(Scannable::getName).collect(toSet()));

		final List<Scannable> additionalScannables = new ArrayList<>();
		additionalScannables.add(createMockScannableMotionUnits("add1", random.nextDouble(), "deg"));
		additionalScannables.add(createMockScannable("add2", 3, 0));
		additionalScannables.add(createMockScannable("add3", 1, 2));
		additionalScannables.stream().forEach(factory::addFindable);

		additionalScannableNames = additionalScannables.stream().map(Scannable::getName).collect(toSet());
	}

	@AfterEach
	public void tearDown() {
		Finder.removeAllFactories();
		random = null;
		scanInfo = null;
		additionalScannableNames = null;
	}

	@Test
	void testGetNexusProvider() throws Exception {
		final BeforeScanSnapshotWriter snapshotWriter = new BeforeScanSnapshotWriter();
		snapshotWriter.setAdditionalScannableNames(additionalScannableNames);

		final NexusObjectProvider<NXcollection> nexusProvider = snapshotWriter.getNexusProvider(scanInfo);
		assertThat(nexusProvider.getName(), is(equalTo(BEFORE_SCAN_COLLECTION_NAME)));
		assertThat(nexusProvider.getAdditionalPrimaryDataFieldNames(), is(empty()));
		assertThat(nexusProvider.getAxisDataFieldNames(), is(empty()));
		assertThat(nexusProvider.getCategory(), is(NexusBaseClass.NX_INSTRUMENT));
		assertThat(nexusProvider.getCollectionName(), is(nullValue()));
		assertThat(nexusProvider.getDefaultAxisDataFieldName(), is(nullValue()));
		assertThat(nexusProvider.getExternalFileNames(), is(empty()));
		assertThat(nexusProvider.getNexusBaseClass(), is(NexusBaseClass.NX_COLLECTION));
		assertThat(nexusProvider.getPrimaryDataFieldName(), is(nullValue()));

		final NXcollection beforeScanCollection = nexusProvider.getNexusObject();
		assertThat(beforeScanCollection, is(notNullValue()));
		assertThat(beforeScanCollection.getAttributeNames(), contains(NexusConstants.NXCLASS));
		assertThat(beforeScanCollection.getDataNodeNames(), is(empty()));

		final Set<String> allScannableNames = getAllScannableNames();
		assertThat(beforeScanCollection.getGroupNodeNames(), containsInAnyOrder(allScannableNames.toArray()));
		for (String scannableName : getAllScannableNames()) {
			checkScannable(beforeScanCollection, scannableName);
		}
	}

	private Scannable createMockScannable(String name, int numInputFields, int numExtraFields) throws DeviceException {
		final String[] inputNames = createFieldNames("input", numInputFields);
		final String[] extraNames = createFieldNames("extra", numExtraFields);
		final int numFields = numInputFields + numExtraFields;
		final Double[] position = random.doubles(numFields).mapToObj(Double::valueOf).toArray(Double[]::new);
		final String[] outputFormat = IntStream.rangeClosed(1, numFields).mapToObj(i -> "%5." + i + "5g").toArray(String[]::new);

		return MockFactory.createMockScannable(name, inputNames, extraNames, outputFormat, position);
	}

	private Scannable createStringScannable(String name) throws DeviceException {
		final String[] inputNames = createFieldNames("strInput", 3);
		final String[] extraNames = createFieldNames("strExtra", 2);
		final String[] position = { "one", "two", "three", "four", "five" };
		final String[] outputFormat = Collections.nCopies(5, "%s").toArray(String[]::new);
		return MockFactory.createMockScannable(name, inputNames, extraNames, outputFormat, 5, position);
	}

	private Scannable createScannableWithNullFieldValue(String name) throws DeviceException {
		final String[] inputNames = createFieldNames("input", 2);
		final String[] extraNames = createFieldNames("extra", 3);
		final Object[] position = { random.nextDouble(), null, "foo", null, "bar" };
		final String[] outputFormat = { "%1.0f", "%1.0f", "%s", "%s", "%s" };
		return MockFactory.createMockScannable(name, inputNames, extraNames, outputFormat, 5, position);
	}

	private String[] createFieldNames(String prefix, int numFields) {
		switch (numFields) {
			case 0: return NO_FIELD_NAMES;
			case 1: return new String[] { prefix };
			default: return IntStream.range(0, numFields).mapToObj(i -> prefix + i).toArray(String[]::new);
		}
	}

	private void checkScannable(final NXcollection beforeScanCollection, String scannableName) throws DeviceException {
		final Scannable scannable = (Scannable) Finder.find(scannableName);
		assertThat(scannable, is(notNullValue()));

		final GroupNode scannableGroup = beforeScanCollection.getGroupNode(scannableName);
		assertThat("no collection found for scannable " + scannableName + " in before scan collection", scannableGroup, is(notNullValue()));
		assertThat(scannableGroup, is(instanceOf(NXcollection.class)));
		final NXcollection scannableCollection = (NXcollection) scannableGroup;

		final String[] allFieldNames = ArrayUtils.addAll(scannable.getInputNames(), scannable.getExtraNames());
		final Object[] positionArray = getScannablePosition(scannable);
		assertThat(positionArray.length, is(equalTo(allFieldNames.length)));
		assertThat(scannableCollection.getDataNodeNames(), containsInAnyOrder(allFieldNames));

		final String expectedUnits = scannable instanceof ScannableMotionUnits ? ((ScannableMotionUnits) scannable).getUserUnits() : null;
		final int[] numDecimals = ScannableUtils.getNumDecimalsArray(scannable);
		for (int fieldIndex = 0; fieldIndex < allFieldNames.length; fieldIndex++) {
			final DataNode dataNode = scannableCollection.getDataNode(allFieldNames[fieldIndex]);
			assertThat(dataNode, is(notNullValue()));
			final IDataset dataset = (IDataset) dataNode.getDataset();
			assertThat(dataset.getShape(), is(equalTo(SCALAR_SHAPE)));
			assertThat(dataset, is(equalTo(DatasetFactory.createFromObject(positionArray[fieldIndex]))));

			final Attribute unitsAttr = dataNode.getAttribute(ATTRIBUTE_NAME_UNITS);
			if (expectedUnits == null) {
				assertThat(unitsAttr, is(nullValue()));
			} else {
				assertThat(unitsAttr, is(notNullValue()));
				assertThat(unitsAttr.getFirstElement(), is(equalTo(expectedUnits)));
			}

			final Attribute decimalsAttr = dataNode.getAttribute(ATTRIBUTE_NAME_DECIMALS);
			if (numDecimals == null || numDecimals[fieldIndex] == -1) {
				assertThat(decimalsAttr, is(nullValue()));
			} else {
				assertThat(decimalsAttr, is(notNullValue()));
				assertThat(decimalsAttr.getValue().getLong(), is((long) numDecimals[fieldIndex]));
			}
		}
	}

	private Set<String> getAllScannableNames() {
		return Stream.of(scanInfo.getScannableNames(), scanInfo.getPerPointMonitorNames(),
				scanInfo.getPerScanMonitorNames(), additionalScannableNames)
			.flatMap(Collection::stream)
			.collect(Collectors.toSet());
	}

	private Object[] getScannablePosition(Scannable scannable) throws DeviceException {
		final Object position = scannable.getPosition();
		if (!position.getClass().isArray()) {
			return new Object[] { position };
		}

		return (Object[]) position;
	}

}
