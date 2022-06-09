/*-
 * Copyright © 2022 Diamond Light Source Ltd.
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

import static gda.MockFactory.createMockScannable;
import static gda.configuration.properties.LocalProperties.GDA_DATA_SCAN_DATAWRITER_DATAFORMAT;
import static java.util.Collections.emptySet;
import static org.eclipse.dawnsci.nexus.test.utilities.NexusAssert.assertAxes;
import static org.eclipse.dawnsci.nexus.test.utilities.NexusAssert.assertIndices;
import static org.eclipse.dawnsci.nexus.test.utilities.NexusAssert.assertSignal;
import static org.eclipse.dawnsci.nexus.test.utilities.NexusAssert.assertTarget;
import static org.eclipse.dawnsci.nexus.test.utilities.NexusTestUtils.openNexusFile;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.analysis.api.tree.TreeFile;
import org.eclipse.dawnsci.hdf5.nexus.NexusFileFactoryHDF5;
import org.eclipse.dawnsci.nexus.NXcollection;
import org.eclipse.dawnsci.nexus.NXdata;
import org.eclipse.dawnsci.nexus.NXdetector;
import org.eclipse.dawnsci.nexus.NXentry;
import org.eclipse.dawnsci.nexus.NXinstrument;
import org.eclipse.dawnsci.nexus.NXpositioner;
import org.eclipse.dawnsci.nexus.NXroot;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusFile;
import org.eclipse.dawnsci.nexus.NexusUtils;
import org.eclipse.dawnsci.nexus.builder.impl.DefaultNexusBuilderFactory;
import org.eclipse.dawnsci.nexus.device.impl.NexusDeviceService;
import org.eclipse.dawnsci.nexus.scan.impl.NexusScanFileServiceImpl;
import org.eclipse.dawnsci.nexus.template.impl.NexusTemplateServiceImpl;
import org.eclipse.scanning.device.Services;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.python.core.Py;
import org.python.core.PyTuple;

import gda.TestHelpers;
import gda.configuration.properties.LocalProperties;
import gda.data.ServiceHolder;
import gda.data.scan.nexus.device.GDANexusDeviceAdapterFactory;
import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.scannable.scannablegroup.ScannableGroup;
import gda.scan.ConcurrentScan;
import uk.ac.diamond.daq.scanning.ScannableDeviceConnectorService;

class ScannableWithStringInputFieldTest {

	private static final String DETECTOR_NAME = "det1";
	private static final String SCANNABLE_NAME = "s1";

	private static final String[] NO_FIELDS = new String[0];

	private static final Object[] STRING_POSITION = { "pc", "nc", "nc", "pc" };
	private static final int NUM_POSITIONS = 4;

	private int numFields;
	private String[] fieldNames;
	private Set<Integer> stringFieldIndices;
	private int maxRangeFieldIndex;

	private Detector detector;

	private Scannable scannable;

	private String outputDir;

	@BeforeEach
	public void setUp() throws Exception {
		detector = createDetector();
	}

	private Detector createDetector() throws DeviceException {
		final Detector detector = mock(Detector.class, DETECTOR_NAME);
		when(detector.getName()).thenReturn(DETECTOR_NAME);
		when(detector.getInputNames()).thenReturn(new String[0]);
		when(detector.getExtraNames()).thenReturn(new String[] { NXdetector.NX_DATA });
		when(detector.getOutputFormat()).thenReturn(new String[] { "%5.2g" });
		when(detector.readout()).thenReturn(new Double[] { 0.0 });
		when(detector.isBusy()).thenReturn(false);
		return detector;
	}

	protected void setUpTest(String testName) throws Exception {
		final String testDir = TestHelpers.setUpTest(this.getClass(), testName, true);
		outputDir = testDir + "/Data/";
		// note, setting this property in @BeforeScan doesn't work, it must be reset somewhere before the scan
		LocalProperties.set(GDA_DATA_SCAN_DATAWRITER_DATAFORMAT, NexusScanDataWriter.class.getSimpleName());

		// TODO, this is copied from NexusScanDataWriterScanTest. Move to common location?
		final NexusDeviceService nexusDeviceService = new NexusDeviceService();

		final ServiceHolder gdaDataServiceHolder = new ServiceHolder();
		gdaDataServiceHolder.setNexusScanFileService(new NexusScanFileServiceImpl());
		gdaDataServiceHolder.setNexusDeviceService(nexusDeviceService);

		final org.eclipse.dawnsci.nexus.scan.ServiceHolder oednsServiceHolder = new org.eclipse.dawnsci.nexus.scan.ServiceHolder();
		oednsServiceHolder.setNexusDeviceService(nexusDeviceService);
		oednsServiceHolder.setNexusBuilderFactory(new DefaultNexusBuilderFactory());
		oednsServiceHolder.setTemplateService(new NexusTemplateServiceImpl());

		final org.eclipse.dawnsci.nexus.ServiceHolder oednServiceHolder = new org.eclipse.dawnsci.nexus.ServiceHolder();
		oednServiceHolder.setNexusFileFactory(new NexusFileFactoryHDF5());
		oednServiceHolder.setNexusDeviceAdapterFactory(new GDANexusDeviceAdapterFactory());

		new Services().setScannableDeviceService(new ScannableDeviceConnectorService());
	}

	static Stream<Arguments> provideArgs() {
		return Stream.of(
				Arguments.of(1, 0, emptySet()), // num
				Arguments.of(1, 0, Set.of(0)), // str
				Arguments.of(2, 0, emptySet()), // num (max), num
				Arguments.of(2, 1, emptySet()), // num, num (max)
				Arguments.of(2, 0, Set.of(1)), //  num, str
				Arguments.of(2, 1, Set.of(0)), // str, num
				Arguments.of(2, 0, Set.of(0, 1)), // str, str
				Arguments.of(3, 0, Set.of(2)), // num (max), num, str
				Arguments.of(3, 1, Set.of(2)), // num, num (max), str
				Arguments.of(3, 1, Set.of(0, 2)), // str, num, str
				Arguments.of(3, 0, Set.of(0, 1, 2)), // str, str, str
				Arguments.of(8, 5, emptySet()), // all numbers
				Arguments.of(8, 3, Set.of(0, 1, 2, 4, 5, 6, 7)), // one number, rest strings
				Arguments.of(8, 6, Set.of(1, 3, 5, 7)), // multiple strings and numbers
				Arguments.of(8, 2, Set.of(6)), // one string, rest numbers
				Arguments.of(8, 0, Set.of(0, 1, 2, 3, 4, 5, 6, 7)) // all strings
		);
	}

	@ParameterizedTest(name="numFields: {0}, maxRangeFieldIndex: {1}, stringFieldIndices: {2}")
	@MethodSource("provideArgs")
	void testScannable(int numFields, int maxRangeFieldIndex, Set<Integer> stringFieldIndices) throws Exception {
		this.numFields = numFields;
		this.maxRangeFieldIndex = maxRangeFieldIndex;
		this.stringFieldIndices = stringFieldIndices;

		fieldNames = IntStream.range(0, numFields).mapToObj(i -> "field" + i).toArray(String[]::new);
		this.scannable = createScannable(stringFieldIndices);

		// each test needs a unique name so that we have a unique output dir
		setUpTest("testScannable" + numFields + "-" + maxRangeFieldIndex + "-" +
					String.join("-", stringFieldIndices.stream().map(i -> i.toString()).toArray(String[]::new)));

		final Object[] scanArguments = createScanArguments();
		final ConcurrentScan scan = new ConcurrentScan(scanArguments);

		scan.runScan();

		final File expectedNexusFile = new File(outputDir + "1.nxs");
		final String expectedNexusFilePath = expectedNexusFile.getAbsolutePath();
		assertThat(expectedNexusFile.exists(), is(true));

		// check the contents of the nexus file
		try (final NexusFile nexusFile = openNexusFile(expectedNexusFilePath)) {
			checkNexusFile(nexusFile);
		}
	}

	private Scannable createScannable(Set<Integer> stringFieldIndices) throws Exception {
		final Scannable[] fieldScannables = IntStream.range(0, numFields)
			.mapToObj(fieldIndex -> createFieldScannable(fieldNames[fieldIndex], stringFieldIndices.contains(fieldIndex)))
			.toArray(Scannable[]::new);

		return new ScannableGroup(SCANNABLE_NAME, fieldScannables);
	}

	private Scannable createFieldScannable(String fieldName, boolean isStringField) {
		final String[] inputNames = new String[] { fieldName };
		final String[] outputNames = new String[] { isStringField ? "%s" : "%5.5f" };
		final Object startPos = isStringField ? "none" : 0.0;

		try {
			return createMockScannable(fieldName, inputNames, NO_FIELDS, outputNames, 5, startPos);
		} catch (DeviceException e) {
			throw new RuntimeException(e);
		}
	}

	private Object[] createScanArguments() {
		final List<Object> arguments = new ArrayList<>();
		arguments.add(scannable);
		arguments.add(createScanPositions());
		arguments.add(detector);

		return arguments.toArray();
	}

	private PyTuple createScanPositions() {
		final Object[][] scanPositions = IntStream.range(0, NUM_POSITIONS)
			.mapToObj(posIndex -> createPosition(posIndex))
			.toArray(Object[][]::new);

		// return as PyTuple. Note cast to Object[] is required to match varargs
		return new PyTuple(Py.javas2pys((Object[]) scanPositions));
	}

	private Object createPosition(int posIndex) {
		return IntStream.range(0, numFields)
			.mapToObj(fieldIndex -> createPositionForField(posIndex, fieldIndex))
			.toArray();
	}

	private Object createPositionForField(int posIndex, int fieldIndex) {
		if (stringFieldIndices.contains(fieldIndex)) {
			return STRING_POSITION[posIndex];
		}

		return posIndex * (fieldIndex == maxRangeFieldIndex ? 2.0 : 1.0);
	}

	private void checkNexusFile(NexusFile nexusFile) throws NexusException {
		final TreeFile nexusTree = NexusUtils.loadNexusTree(nexusFile);
		final NXroot nexusRoot = (NXroot) nexusTree.getGroupNode();
		assertThat(nexusRoot, is(notNullValue()));
		final NXentry entry = nexusRoot.getEntry();
		assertThat(entry, is(notNullValue()));

		final NXinstrument instrument = entry.getInstrument();
		assertThat(instrument, is(notNullValue()));

		final NXcollection scannableCollection = instrument.getCollection(SCANNABLE_NAME);
		assertThat(scannableCollection, is(fieldNames.length == 1 ? nullValue() : notNullValue()));

		final int[] scanShape = { NUM_POSITIONS };
		for (int i = 0; i < numFields; i++) {
			final String fieldName = fieldNames[i];
			final String positionerName =  SCANNABLE_NAME + (fieldNames.length == 1 ? "" : "." + fieldName);
			final NXpositioner positioner = instrument.getPositioner(positionerName);
			assertThat(positioner, is(notNullValue()));
			assertThat(positioner.getDataNodeNames(), contains(NXpositioner.NX_NAME, NXpositioner.NX_VALUE));
			assertThat(positioner.getNameScalar(), is(equalTo(positionerName)));

			if (numFields > 1) {
				final DataNode dataNode = scannableCollection.getDataNode(fieldName);
				assertThat(dataNode, is(notNullValue()));
				assertThat(dataNode.getDataset().getShape(), is(scanShape));
				assertThat(positioner.getDataNode(NXpositioner.NX_VALUE), is(sameInstance(dataNode)));
			}
		}

		// check the NXdata group
		assertThat(entry.getAllData().keySet(), contains(DETECTOR_NAME));
		final NXdata dataGroup = entry.getData(DETECTOR_NAME);
		assertThat(dataGroup, is(notNullValue()));
		final String[] scannableLinkedFieldNames = Arrays.stream(fieldNames)
				.map(fieldName -> SCANNABLE_NAME + (numFields == 1 ? "" : "_" + fieldName))
				.toArray(String[]::new);
		final String[] expectedDataGroupFieldNames = ArrayUtils.add(scannableLinkedFieldNames, NXdetector.NX_DATA);
		assertThat(dataGroup.getDataNodeNames(), containsInAnyOrder(expectedDataGroupFieldNames));

		assertSignal(dataGroup, NXdetector.NX_DATA);
		assertAxes(dataGroup, SCANNABLE_NAME + (numFields == 1 ? "" : "_" + fieldNames[maxRangeFieldIndex]));
		for (int i = 0; i < numFields; i++) {
			final String fieldName = fieldNames[i];
			final String dataFieldName = scannableLinkedFieldNames[i];
			final DataNode dataNode = dataGroup.getDataNode(dataFieldName);
			assertThat(dataNode, is(notNullValue()));
			assertIndices(dataGroup, dataFieldName, 0);
			assertTarget(dataGroup, dataFieldName, nexusRoot, "/entry/instrument/" + SCANNABLE_NAME + "/"
						+ (numFields == 1 ? NXpositioner.NX_VALUE: fieldName));

			if (numFields > 1) {
				assertThat(dataNode, is(sameInstance(scannableCollection.getDataNode(fieldName))));
			}
		}
	}

}
