/*-
 * Copyright © 2020 Diamond Light Source Ltd.
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

package gda.data.scan.datawriter;

import static gda.data.metadata.GDAMetadataProvider.SCAN_IDENTIFIER;
import static gda.data.scan.datawriter.NexusDataWriter.GDA_NEXUS_CREATE_MEASUREMENT_GROUP;
import static gda.data.scan.datawriter.NexusDataWriter.GROUP_NAME_INSTRUMENT;
import static gda.data.scan.datawriter.NexusDataWriter.GROUP_NAME_MEASUREMENT;
import static gda.data.scan.nexus.device.DummyNexusDetector.FIELD_NAME_EXTERNAL;
import static gda.data.scan.nexus.device.DummyNexusDetector.FIELD_NAME_IMAGE_X;
import static gda.data.scan.nexus.device.DummyNexusDetector.FIELD_NAME_IMAGE_Y;
import static gda.data.scan.nexus.device.DummyNexusDetector.FIELD_NAME_SPECTRUM;
import static gda.data.scan.nexus.device.DummyNexusDetector.FIELD_NAME_VALUE;
import static gda.data.scan.nexus.device.GDADeviceNexusConstants.ATTRIBUTE_NAME_LOCAL_NAME;
import static gda.data.scan.nexus.device.GDADeviceNexusConstants.ATTRIBUTE_NAME_TARGET;
import static gda.data.scan.nexus.device.GDADeviceNexusConstants.ATTRIBUTE_NAME_UNITS;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static org.eclipse.dawnsci.nexus.scan.NexusScanConstants.FIELD_NAME_SCAN_COMMAND;
import static org.eclipse.dawnsci.nexus.test.utilities.NexusAssert.assertAxes;
import static org.eclipse.dawnsci.nexus.test.utilities.NexusAssert.assertSignal;
import static org.eclipse.dawnsci.nexus.test.utilities.NexusAssert.assertUnits;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.dawnsci.analysis.api.tree.Attribute;
import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.analysis.api.tree.GroupNode;
import org.eclipse.dawnsci.hdf5.nexus.NexusFileFactoryHDF5;
import org.eclipse.dawnsci.nexus.INexusFileFactory;
import org.eclipse.dawnsci.nexus.NXcollection;
import org.eclipse.dawnsci.nexus.NXdata;
import org.eclipse.dawnsci.nexus.NXdetector;
import org.eclipse.dawnsci.nexus.NXentry;
import org.eclipse.dawnsci.nexus.NXinsertion_device;
import org.eclipse.dawnsci.nexus.NXinstrument;
import org.eclipse.dawnsci.nexus.NXmonochromator;
import org.eclipse.dawnsci.nexus.NXpositioner;
import org.eclipse.dawnsci.nexus.NXsource;
import org.eclipse.dawnsci.nexus.NXuser;
import org.eclipse.dawnsci.nexus.NexusConstants;
import org.eclipse.dawnsci.nexus.appender.INexusFileAppenderService;
import org.eclipse.dawnsci.nexus.appender.impl.NexusFileAppenderService;
import org.eclipse.dawnsci.nexus.device.INexusDeviceService;
import org.eclipse.dawnsci.nexus.device.impl.NexusDeviceService;
import org.eclipse.dawnsci.nexus.template.NexusTemplateService;
import org.eclipse.dawnsci.nexus.template.impl.NexusTemplateServiceImpl;
import org.eclipse.january.DatasetException;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.IDataset;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import com.google.common.collect.Streams;

import gda.configuration.properties.LocalProperties;
import gda.data.scan.nexus.device.SimpleDummyNexusDetector;
import gda.device.DeviceException;
import gda.device.Scannable;
import uk.ac.diamond.osgi.services.ServiceProvider;

public class NexusDataWriterScanTest extends AbstractNexusDataWriterScanTest {

	private static final int[] METADATA_DATASET_SHAPE = new int[] { 1 };

	private static final String FIELD_NAME_USER_NAME = "username";
	private static final String GROUP_NAME_DEFAULT = "default";

	private static final String ATTRIBUTE_NAME_AXIS = "axis";
	private static final String ATTRIBUTE_NAME_LABEL = "label";
	private static final String ATTRIBUTE_NAME_PRIMARY = "primary";
	private static final String ATTRIBUTE_NAME_SIGNAL = "signal";

	private static final String METADATA_KEY_SCAN_IDENTIFIER = "scan_identifier";
	private static final String METADATA_KEY_MONOCHROMATOR_NAME = "instrument.monochromator.name";
	private static final String METADATA_KEY_MONOCHROMATOR_ENERGY = "instrument.monochromator.energy";
	private static final String METADATA_KEY_MONOCHROMATOR_WAVELENGTH = "instrument.monochromator.wavelength";
	private static final String METADATA_KEY_INSERTION_DEVICE_GAP = "instrument.insertion_device.gap";
	private static final String METADATA_KEY_INSTRUMENT_SOURCE_ENERGY = "instrument.source.energy";
	private static final String METADATA_KEY_INSTRUMENT_SOURCE_CURRENT = "instrument.source.current";

	private static final String FIELD_NAME_SCAN_DIMENSIONS = "scan_dimensions";

	private static final String EXPECTED_INSTRUMENT_NAME = "i06";
	private static final String EXPECTED_SCAN_IDENTIFIER = "12345678";

	@BeforeAll
	public static void setUpServices() {
		ServiceProvider.setService(NexusTemplateService.class, new NexusTemplateServiceImpl());
		ServiceProvider.setService(INexusFileFactory.class, new NexusFileFactoryHDF5());
		ServiceProvider.setService(INexusFileAppenderService.class, new NexusFileAppenderService());
		ServiceProvider.setService(INexusDeviceService.class, new NexusDeviceService());

		LocalProperties.set(GDA_NEXUS_CREATE_MEASUREMENT_GROUP, true);
	}

	@BeforeAll
	public static void setUpProperties() {
		LocalProperties.set(GDA_NEXUS_CREATE_MEASUREMENT_GROUP, true);
	}

	@AfterAll
	public static void tearDownProperties() {
		LocalProperties.clearProperty(GDA_NEXUS_CREATE_MEASUREMENT_GROUP);
	}

	@Override
	protected void setUpTest(String testName) throws Exception {
		super.setUpTest(testName, NexusDataWriter.class);
	}

	@Override
	protected void setUpMetadata() throws Exception {
		super.setUpMetadata();

		// Note: I tried using ScannableMetadataEntries here, but they don't work with no JythonServiceFacade set up, which is hard in a test.
		// Additional note: for some reason metadata values have to be strings.

		addMetadataEntry(METADATA_KEY_SCAN_IDENTIFIER, EXPECTED_SCAN_IDENTIFIER);

		addMetadataEntry(METADATA_KEY_INSTRUMENT, EXPECTED_INSTRUMENT_NAME);

		// metadata entries for NXmonochromator
		addMetadataEntry(METADATA_KEY_MONOCHROMATOR_NAME, EXPECTED_MONOCHROMATOR_NAME);
		addMetadataEntry(METADATA_KEY_MONOCHROMATOR_ENERGY, EXPECTED_MONOCHROMATOR_ENERGY);
		addMetadataEntry(METADATA_KEY_MONOCHROMATOR_WAVELENGTH, EXPECTED_MONOCHROMATOR_WAVELENGTH);

		// metadata entries for NXinsertion_device - this is the only field that NeXusUtils.writeNXinsertionDevice actually writes)
		addMetadataEntry(METADATA_KEY_INSERTION_DEVICE_GAP, EXPECTED_INSERTION_DEVICE_GAP);

		// metadata entries for NXsource
		addMetadataEntry(METADATA_KEY_INSTRUMENT_SOURCE_ENERGY, EXPECTED_SOURCE_ENERGY);
		addMetadataEntry(METADATA_KEY_INSTRUMENT_SOURCE_CURRENT, EXPECTED_SOURCE_CURRENT);
	}

	@Override
	protected String[] getEntryGroupNames() {
		return new String[] { BEFORE_SCAN_COLLECTION_NAME, GROUP_NAME_INSTRUMENT, GROUP_NAME_MEASUREMENT,
				EXPECTED_USER_GROUP_NAME, getExpectedDataGroupName(),
				METADATA_NEXUS_DEVICE_NAME, METADATA_SCANNABLE_NEXUS_DEVICE_NAME, METADATA_SCANNABLE_AND_NEXUS_DEVICE_NAME
		};
	}

	@Override
	protected void checkEntryMetadata(NXentry entry) throws Exception {
		super.checkEntryMetadata(entry);

		assertThat(entry.getDataNodeNames(), containsInAnyOrder(NXentry.NX_START_TIME, NXentry.NX_END_TIME,
				NXentry.NX_ENTRY_IDENTIFIER, NXentry.NX_EXPERIMENT_IDENTIFIER, NXentry.NX_PROGRAM_NAME, NXentry.NX_TITLE,
				FIELD_NAME_SCAN_COMMAND, FIELD_NAME_SCAN_DIMENSIONS,
				SCAN_IDENTIFIER));

		// entry_identifier
		assertThat(entry.getEntry_identifierScalar(), is(equalTo(EXPECTED_ENTRY_IDENTIFER)));
		// experiment_identifier - set to visit it
		assertThat(entry.getExperiment_identifierScalar(), is(equalTo(EXPECTED_VISIT_ID)));
		// program_name
		assertThat(entry.getProgram_nameScalar(), is(equalTo(EXPECTED_PROGRAM_NAME)));
		// scan_command
		assertThat(entry.getDataset(FIELD_NAME_SCAN_COMMAND).getString(), is(equalTo(getExpectedScanCommand())));
		// scan_dimensions
		assertThat(entry.getDataset(FIELD_NAME_SCAN_DIMENSIONS), is(equalTo(DatasetFactory.createFromObject(scanDimensions))));
		// scan identifier
		assertThat(entry.getDataset(SCAN_IDENTIFIER), is(equalTo(EXPECTED_SCAN_IDENTIFIER)));
		// title
		assertThat(entry.getTitleScalar(), is(equalTo(getExpectedScanCommand()))); // title seems to be same as scan command(!)

		checkBeforeScanCollection(entry.getCollection(BEFORE_SCAN_COLLECTION_NAME));
	}

	private void checkBeforeScanCollection(NXcollection beforeScanCollection) throws DeviceException {
		assertThat(beforeScanCollection, is(notNullValue()));

		assertThat(beforeScanCollection.getDataNodeNames(), is(empty()));
		assertThat(beforeScanCollection.getGroupNodeNames(),
				containsInAnyOrder(STRING_VALUED_METADATA_SCANNABLE_NAME, MULTI_FIELD_METADATA_SCANNABLE_NAME));

		final NXcollection stringValuedScannableCollection =
				(NXcollection) beforeScanCollection.getGroupNode(STRING_VALUED_METADATA_SCANNABLE_NAME);
		beforeScanCollection.getGroupNode(STRING_VALUED_METADATA_SCANNABLE_NAME);
		assertThat(stringValuedScannableCollection.getDataNodeNames(), contains(STRING_VALUED_METADATA_SCANNABLE_NAME));

		assertThat(stringValuedScannableCollection.getDataNode(STRING_VALUED_METADATA_SCANNABLE_NAME).getString(),
				is(equalTo(STRING_VALUED_METADATA_SCANNABLE_VALUE)));

		final NXcollection multiFieldScannableCollection =
				(NXcollection) beforeScanCollection.getGroupNode(MULTI_FIELD_METADATA_SCANNABLE_NAME);

		final Map<String, Object> multiFieldScannableExpectedValues = getMultiFieldScannableExpectedValuesMap();
		assertThat(multiFieldScannableCollection.getDataNodeNames(),
				containsInAnyOrder(multiFieldScannableExpectedValues.keySet().toArray()));
		multiFieldScannableExpectedValues.forEach((name, value) ->
			assertThat(multiFieldScannableCollection.getDataset(name), is(equalTo(DatasetFactory.createFromObject(value)))));
	}

	@Override
	protected void checkInstrumentGroupMetadata(final NXinstrument instrument) {
		assertThat(instrument.getDataNodeNames(), contains(NXinstrument.NX_NAME));
		assertThat(instrument.getNameScalar(), is(equalTo(EXPECTED_INSTRUMENT_NAME)));

		// group for each device, plus metadata groups: source, monochromator, insertion_device
		// metadata scannables with no entry in the location map are excluded (they are in the before_scan collection)
		final Set<String> expectedGroupNodeNames = new HashSet<>(Arrays.asList(getScannableAndMonitorNames()));
		if (detector != null) expectedGroupNodeNames.add(detector.getName());
		expectedGroupNodeNames.addAll(getExpectedMetadataScannableNames().stream()
				.filter(this::hasLocationMapEntry)
				.toList());
		expectedGroupNodeNames.addAll(List.of("insertion_device", "monochromator", "source"));
		assertThat(instrument.getGroupNodeNames(), containsInAnyOrder(expectedGroupNodeNames.toArray()));

		checkSource(instrument);
	}

	private void checkSource(NXinstrument instrument) {
		final NXsource source = instrument.getSource();
		assertThat(source, is(notNullValue()));

		final String[] expectedDataNodeNames = { NXsource.NX_NAME, NXsource.NX_PROBE,
				NXsource.NX_TYPE, NXsource.NX_CURRENT, NXsource.NX_ENERGY };
		assertThat(source.getDataNodeNames(), containsInAnyOrder(expectedDataNodeNames));
		assertThat(source.getGroupNodeNames(), is(empty()));

		assertThat(source.getNameScalar(), is(equalTo("DLS")));
		assertThat(source.getProbeScalar(), is(equalTo("x-ray")));
		assertThat(source.getTypeScalar(), is(equalTo("Synchrotron X-ray Source")));
		assertThat(source.getEnergyScalar(), is(equalTo(3.0)));
		assertThat(source.getCurrentScalar(), is(equalTo(25.5)));
	}

	@Override
	protected Set<String> getExpectedPositionerNames() {
		return Streams.concat(Arrays.stream(getScannableAndMonitorNames()),
				getExpectedMetadataScannableNames().stream().filter(this::hasLocationMapEntry))
				.collect(toSet());
	}

	@Override
	protected void checkDefaultScannablePositioner(NXpositioner scannablePos, int i) throws Exception {
		final String scannableName = scannables[i].getName();

		final String[] expectedDataNodeNames = { scannableName, NXpositioner.NX_SOFT_LIMIT_MIN, NXpositioner.NX_SOFT_LIMIT_MAX };
		assertThat(scannablePos.getDataNodeNames(), containsInAnyOrder(expectedDataNodeNames));

		final DataNode scannableValueDataNode = scannablePos.getDataNode(scannableName);
		final String expectedAxes = String.join(",", IntStream.range(0, scanRank).map(j->j+1).mapToObj(Integer::toString).toArray(String[]::new));
		assertThat(scannableValueDataNode, is(notNullValue()));

		final String[] expectedDataNodeAttrNames = { ATTRIBUTE_NAME_AXIS, ATTRIBUTE_NAME_LABEL,
				ATTRIBUTE_NAME_LOCAL_NAME, ATTRIBUTE_NAME_PRIMARY, ATTRIBUTE_NAME_TARGET };
		assertThat(scannableValueDataNode.getAttributeNames(), containsInAnyOrder(expectedDataNodeAttrNames));

		assertThat(scannableValueDataNode.getAttribute(ATTRIBUTE_NAME_AXIS).getFirstElement(), is(equalTo(expectedAxes)));
		assertThat(scannableValueDataNode.getAttribute(ATTRIBUTE_NAME_LABEL).getFirstElement(), is(Integer.toString(i)));
		assertThat(scannableValueDataNode.getAttribute(ATTRIBUTE_NAME_LOCAL_NAME).getFirstElement(),
				is(equalTo(scannableName + "." + scannableName)));
		assertThat(scannableValueDataNode.getAttribute(ATTRIBUTE_NAME_PRIMARY).getFirstElement(), is(equalTo(("1"))));
		assertThat(scannableValueDataNode.getAttribute(ATTRIBUTE_NAME_TARGET).getFirstElement(),
				is(equalTo("/" + ENTRY_NAME + "/" + GROUP_NAME_INSTRUMENT + "/" + scannableName + "/" + scannableName)));

		assertThat(scannableValueDataNode.getDataset().getSlice(),
				is(equalTo(getExpectedScannableDataset(i)))); // check values

		assertThat(scannablePos.getSoft_limit_minScalar(), is(equalTo(SCANNABLE_LOWER_BOUND)));
		assertThat(scannablePos.getSoft_limit_maxScalar(), is(equalTo(SCANNABLE_UPPER_BOUND)));
	}

	@Override
	protected void checkConfiguredScannablePositioner(String scannableName, NXpositioner positioner)
			throws Exception {
		assertThat(positioner.getDataNodeNames(), contains(scannableName));

		final DataNode scannableValueDataNode = positioner.getDataNode(scannableName);
		assertThat(scannableValueDataNode, is(notNullValue()));

		final String[] expectedDataNodeAttrNames = { ATTRIBUTE_NAME_AXIS, ATTRIBUTE_NAME_LOCAL_NAME, ATTRIBUTE_NAME_PRIMARY,
				ATTRIBUTE_NAME_TARGET, ATTRIBUTE_NAME_UNITS };
		assertThat(scannableValueDataNode.getAttributeNames(), containsInAnyOrder(expectedDataNodeAttrNames));

		final String expectedAxes = String.join(",", IntStream.range(0, scanRank).map(j->j+1).mapToObj(Integer::toString).toArray(String[]::new));
		assertThat(scannableValueDataNode.getAttribute(ATTRIBUTE_NAME_AXIS).getFirstElement(), is(equalTo(expectedAxes)));
		assertThat(scannableValueDataNode.getAttribute(ATTRIBUTE_NAME_LOCAL_NAME).getFirstElement(),
				is(equalTo(scannableName + "." + scannableName)));
		assertThat(scannableValueDataNode.getAttribute(ATTRIBUTE_NAME_TARGET).getFirstElement(), is(equalTo(
				"/" + ENTRY_NAME + "/" + GROUP_NAME_INSTRUMENT + "/" + scannableName + "/" + scannableName)));
		assertThat(scannableValueDataNode.getAttribute(ATTRIBUTE_NAME_PRIMARY).getFirstElement(), is(equalTo("1")));
		assertThat(scannableValueDataNode.getAttribute(ATTRIBUTE_NAME_UNITS).getFirstElement(),
				is(equalTo("mm")));

		assertThat(scannableValueDataNode.getDataset().getSlice(),
				is(equalTo(DatasetFactory.createFromObject(getExpectedScannableDataset(0))))); // check values
	}

	@Override
	protected void checkSingleFieldMonitor(final GroupNode monitorGroup) throws DatasetException {
		assertThat(monitorGroup, is(instanceOf(NXpositioner.class)));

		final NXpositioner monitorPos = (NXpositioner) monitorGroup;
		assertThat(monitorPos.getDataNodeNames(), contains(SINGLE_FIELD_MONITOR_NAME));

		final DataNode monitorValueDataNode = monitorPos.getDataNode(monitor.getExtraNames()[0]);
		assertThat(monitorValueDataNode, is(notNullValue()));

		String[] expectedDataNodeAttrNames = { ATTRIBUTE_NAME_LOCAL_NAME, ATTRIBUTE_NAME_TARGET };
		if (detector == null) expectedDataNodeAttrNames = ArrayUtils.add(expectedDataNodeAttrNames, ATTRIBUTE_NAME_SIGNAL);
		assertThat(monitorValueDataNode.getAttributeNames(), containsInAnyOrder(expectedDataNodeAttrNames));

		assertThat(monitorValueDataNode.getAttribute(ATTRIBUTE_NAME_LOCAL_NAME).getFirstElement(),
				is(equalTo(SINGLE_FIELD_MONITOR_NAME + "." + SINGLE_FIELD_MONITOR_NAME)));
		assertThat(monitorValueDataNode.getAttribute(ATTRIBUTE_NAME_TARGET).getFirstElement(),
				is(equalTo("/" + ENTRY_NAME + "/" + GROUP_NAME_INSTRUMENT + "/" + SINGLE_FIELD_MONITOR_NAME + "/" + SINGLE_FIELD_MONITOR_NAME)));
		assertThat(monitorValueDataNode.getDataset().getSlice(),
				is(equalTo(DatasetFactory.zeros(scanDimensions).fill(SINGLE_FIELD_MONITOR_VALUE)))); // check values
		if (detector == null) {
			assertThat(monitorValueDataNode.getAttribute(ATTRIBUTE_NAME_SIGNAL).getFirstElement(), is(equalTo("1")));
		}
	}

	@Override
	protected void checkMultiFieldMonitor(final GroupNode monitorGroup) throws DatasetException {
		assertThat(monitorGroup, is(instanceOf(NXpositioner.class)));

		final NXpositioner multiMonitorPos = (NXpositioner) monitorGroup;
		assertThat(multiMonitorPos.getDataNodeNames(), contains(MULTI_FIELD_MONITOR_FIELD_NAMES));

		for (String fieldName : MULTI_FIELD_MONITOR_FIELD_NAMES) {
			final DataNode dataNode = multiMonitorPos.getDataNode(fieldName);
			assertThat(dataNode, is(notNullValue()));

			String[] expectedDataNodeAttrNames = { ATTRIBUTE_NAME_LOCAL_NAME, ATTRIBUTE_NAME_TARGET, ATTRIBUTE_NAME_AXIS };
			if (fieldName.equals(MULTI_FIELD_MONITOR_FIELD_NAMES[0])) { // the first field is the primary axis field
				expectedDataNodeAttrNames = ArrayUtils.addAll(expectedDataNodeAttrNames, ATTRIBUTE_NAME_LABEL, ATTRIBUTE_NAME_PRIMARY);
			}

			assertThat(dataNode.getAttributeNames(), containsInAnyOrder(expectedDataNodeAttrNames));
		}
	}

	@Override
	protected void checkMetadataScannablePositioner(NXpositioner positioner, int index) throws Exception {
		final String scannableName = METADATA_SCANNABLE_NAMES[index];
		assertThat(positioner.getDataNodeNames(), contains(scannableName));

		final DataNode valueDataNode = positioner.getDataNode(scannableName);
		assertThat(valueDataNode, is(notNullValue()));

		assertThat(valueDataNode.getAttributeNames(), contains(ATTRIBUTE_NAME_AXIS, ATTRIBUTE_NAME_LOCAL_NAME, ATTRIBUTE_NAME_UNITS));

		assertThat(valueDataNode.getAttribute(ATTRIBUTE_NAME_AXIS).getFirstElement(), is(equalTo("1")));
		assertThat(valueDataNode.getAttribute(ATTRIBUTE_NAME_LOCAL_NAME).getFirstElement(),
				is(equalTo(scannableName + "." + scannableName)));
		assertThat(valueDataNode.getAttribute(ATTRIBUTE_NAME_UNITS).getFirstElement(), is(equalTo("mm")));

		assertThat(valueDataNode.getDataset().getShape(), is(equalTo(METADATA_DATASET_SHAPE)));
		assertThat(valueDataNode.getDataset().getSlice().getDouble(0), is(equalTo((double) index)));
	}

	@Override
	protected void checkDataGroups(NXentry entry) throws Exception {
		// NexusDataWriter creates a single NXdata group
		final String expectedDataGroupName = getExpectedDataGroupName();
		final Map<String, NXdata> dataGroups = entry.getAllData();
		assertThat(dataGroups.size(), is(2));
		assertThat(dataGroups.keySet(), containsInAnyOrder(expectedDataGroupName, GROUP_NAME_MEASUREMENT));
		checkMeasurementDataGroup(dataGroups.get(GROUP_NAME_MEASUREMENT));
		checkDataGroup(entry, dataGroups.get(expectedDataGroupName));
	}

	private String getExpectedDataGroupName() {
		return primaryDeviceType.isDetector() ? detector.getName() : GROUP_NAME_DEFAULT;
	}

	private void checkDataGroup(NXentry entry, NXdata data) {
		assertThat(data, is(notNullValue()));

		// assert that all the expected linked data nodes are present
		final Map<String, String> expectedDataNodeLinks = new LinkedHashMap<>();
		expectedDataNodeLinks.putAll(calculateScannableDataNodeLinks());
		expectedDataNodeLinks.putAll(calculateDetectorDataNodeLinks());
		checkLinkedDatasets(data, entry, expectedDataNodeLinks);
	}

	@Override
	protected void checkMeasurementDataGroup(NXdata dataGroup) throws Exception {
		assertThat(dataGroup, is(notNullValue()));

		final List<String> expectedFieldNames = getExpectedMeasurementGroupFieldNames();
		assertSignal(dataGroup, expectedFieldNames.getLast());
		assertAxes(dataGroup, getScannableNames());
		assertThat(dataGroup.getDataNodeNames(), containsInAnyOrder(expectedFieldNames.toArray()));

		assertThat(dataGroup.getAttributeNames(), containsInAnyOrder(Stream.concat(
				Stream.of(NexusConstants.NXCLASS, NexusConstants.DATA_AXES, NexusConstants.DATA_SIGNAL),
				expectedFieldNames.stream().map(name -> name + NexusConstants.DATA_INDICES_SUFFIX)).toArray()));

		final IDataset expectedIndicesAttrValue = DatasetFactory.createFromObject(
				IntStream.range(0, scanDimensions.length).toArray());
		for (String scannableName : expectedFieldNames) {
			final DataNode dataNode = dataGroup.getDataNode(scannableName);
			assertThat(dataNode, is(notNullValue()));
			assertThat(dataNode.getDataset().getElementClass(), is(equalTo(Double.class)));
			assertThat(dataNode.getDataset().getShape(), is(equalTo(scanDimensions)));

			final Attribute indicesAttr = dataGroup.getAttribute(scannableName + NexusConstants.DATA_INDICES_SUFFIX);
			assertThat(indicesAttr, is(notNullValue()));
			assertThat(indicesAttr.getValue(), is(equalTo(expectedIndicesAttrValue)));
		}
	}

	private Map<String, String> calculateScannableDataNodeLinks() {
		final Map<String, String> scannableDataNodeLinks = Arrays.stream(scannables).map(Scannable::getName).
				collect(toMap(Function.identity(),
						scannableName -> String.format("instrument/%s/%s", scannableName, scannableName)));

		if (createMonitor && primaryDeviceType != PrimaryDeviceType.NONE
				&& primaryDeviceType != PrimaryDeviceType.MULTI_FIELD_MONITOR) {
			scannableDataNodeLinks.put(SINGLE_FIELD_MONITOR_NAME,
					String.format("instrument/%s/%s", SINGLE_FIELD_MONITOR_NAME, SINGLE_FIELD_MONITOR_NAME));
		}
		return scannableDataNodeLinks;
	}

	private Map<String, String> calculateDetectorDataNodeLinks() {
		return switch (primaryDeviceType) {
			case NONE, SINGLE_FIELD_MONITOR, NEXUS_DEVICE,
						FILE_CREATOR, NO_DATA_NEXUS_DETECTOR -> Collections.emptyMap(); // no links
			case MULTI_FIELD_MONITOR -> Arrays.stream(MULTI_FIELD_MONITOR_FIELD_NAMES)  // each field of the monitor is linked to
					.collect(toMap(Function.identity(), fieldName -> String.format("instrument/%s/%s", MULTI_FIELD_MONITOR_NAME, fieldName)));
			case COUNTER_TIMER -> Arrays.stream(detector.getExtraNames())
					.collect(toMap(Function.identity(), this::prependDetectorPath)); // a link to each extra name field of the detector
			case GENERIC -> Map.ofEntries(prependDetectorPathEntry(NXdetector.NX_DATA)); // a single link to the data node of the detector
			case SIMPLE_NEXUS_DETECTOR -> Map.ofEntries( // a link to the data and axis field of the detector
					prependDetectorPathEntry(NXdetector.NX_DATA),
					prependDetectorPathEntry(NXdata.NX_DATA + SimpleDummyNexusDetector.AXIS_NAME_SUFFIX + 1));
			case NEXUS_DETECTOR -> List.of(NXdetector.NX_DATA, FIELD_NAME_SPECTRUM, FIELD_NAME_VALUE,
						FIELD_NAME_EXTERNAL, FIELD_NAME_IMAGE_X, FIELD_NAME_IMAGE_Y).stream()
					.collect(toMap(Function.identity(), this::prependDetectorPath));
			default -> throw new IllegalArgumentException("Unknown detector type: " + primaryDeviceType);
		};
	}

	private Map.Entry<String, String> prependDetectorPathEntry(String fieldName) {
		return Map.entry(fieldName, prependDetectorPath(fieldName));
	}

	private String prependDetectorPath(String fieldName) {
		return GROUP_NAME_INSTRUMENT + '/' + detector.getName() + '/' + fieldName;
	}

	@Override
	protected void checkMonochromatorGroup(NXinstrument instrument) {
		final NXmonochromator monochromator = instrument.getMonochromator();
		assertThat(monochromator, is(notNullValue()));

		assertThat(monochromator.getDataNode("name").getString(), is(equalTo(EXPECTED_MONOCHROMATOR_NAME)));
		assertThat(monochromator.getEnergyScalar(), is(equalTo(EXPECTED_MONOCHROMATOR_ENERGY)));
		assertUnits(monochromator.getDataNode(NXmonochromator.NX_ENERGY), "keV");
		assertThat(monochromator.getWavelengthScalar(), is(equalTo(EXPECTED_MONOCHROMATOR_WAVELENGTH)));
		assertUnits(monochromator.getDataNode(NXmonochromator.NX_WAVELENGTH), "Angstrom");
	}

	@Override
	protected void checkInsertionDeviceGroup(NXinstrument instrument) {
		final NXinsertion_device insertionDevice = instrument.getInsertion_device();
		assertThat(insertionDevice, is(notNullValue()));

		assertThat(insertionDevice.getGapScalar(), is(equalTo(EXPECTED_INSERTION_DEVICE_GAP)));
		assertUnits(insertionDevice.getDataNode(NXinsertion_device.NX_GAP), "mm");
	}

	@Override
	protected void checkSourceGroup(NXinstrument instrument) {
		final NXsource source = instrument.getSource();
		assertThat(source, is(notNullValue()));

		assertThat(source.getGroupNodeNames(), is(empty()));
		assertThat(source.getDataNodeNames(), containsInAnyOrder(NXsource.NX_NAME,
				NXsource.NX_PROBE, NXsource.NX_TYPE, NXsource.NX_ENERGY, NXsource.NX_CURRENT));

		assertThat(source.getNameScalar(), is(equalTo("DLS")));
		assertThat(source.getProbeScalar(), is(equalTo("x-ray")));
		assertThat(source.getTypeScalar(), is(equalTo("Synchrotron X-ray Source")));

		assertThat(source.getEnergyScalar(), is(equalTo(EXPECTED_SOURCE_ENERGY)));
		assertUnits(source.getDataNode(NXsource.NX_ENERGY), "GeV");
		assertThat(source.getCurrentScalar(), is(equalTo(EXPECTED_SOURCE_CURRENT)));
		assertUnits(source.getDataNode(NXsource.NX_CURRENT), "mA");
	}

	@Override
	protected void checkSampleGroup(NXentry entry) {
		// NeXusUtil.write_NXsample is not called and only creates an empty NXsample group
		assertThat(entry.getAllSample().size(), is(0));
	}

	@Override
	protected void checkUsers(NXentry entry) {
		// user group
		final Map<String, NXuser> users = entry.getAllUser();
		assertThat(users.keySet(), Matchers.contains(EXPECTED_USER_GROUP_NAME));
		final NXuser user = users.get(EXPECTED_USER_GROUP_NAME);
		assertThat(user, is(notNullValue()));
		assertThat(user.getDataNodeNames(), contains(FIELD_NAME_USER_NAME));
		assertThat(user.getString(FIELD_NAME_USER_NAME), is(equalTo(EXPECTED_USER_ID)));
	}

}
