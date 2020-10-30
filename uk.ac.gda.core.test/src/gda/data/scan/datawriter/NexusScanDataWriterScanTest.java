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

import static org.eclipse.scanning.test.utilities.scan.nexus.NexusAssert.assertAxes;
import static org.eclipse.scanning.test.utilities.scan.nexus.NexusAssert.assertIndices;
import static org.eclipse.scanning.test.utilities.scan.nexus.NexusAssert.assertSignal;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.both;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.hdf5.nexus.NexusFileFactoryHDF5;
import org.eclipse.dawnsci.nexus.IWritableNexusDevice;
import org.eclipse.dawnsci.nexus.NXcollection;
import org.eclipse.dawnsci.nexus.NXdata;
import org.eclipse.dawnsci.nexus.NXdetector;
import org.eclipse.dawnsci.nexus.NXentry;
import org.eclipse.dawnsci.nexus.NXinstrument;
import org.eclipse.dawnsci.nexus.NXpositioner;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusNodeFactory;
import org.eclipse.dawnsci.nexus.NexusScanInfo;
import org.eclipse.dawnsci.nexus.builder.NexusObjectProvider;
import org.eclipse.dawnsci.nexus.builder.NexusObjectWrapper;
import org.eclipse.dawnsci.nexus.builder.impl.DefaultNexusBuilderFactory;
import org.eclipse.dawnsci.nexus.device.INexusDeviceService;
import org.eclipse.dawnsci.nexus.device.impl.NexusDeviceService;
import org.eclipse.dawnsci.nexus.scan.impl.NexusScanFileServiceImpl;
import org.eclipse.january.DatasetException;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.ILazyWriteableDataset;
import org.eclipse.january.dataset.Random;
import org.eclipse.january.dataset.SliceND;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import gda.TestHelpers;
import gda.configuration.properties.LocalProperties;
import gda.data.ServiceHolder;
import gda.data.scan.nexus.device.GDANexusDeviceAdapterFactory;
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.detector.DummyDetector;
import gda.factory.Factory;
import gda.factory.Finder;

@RunWith(value=Parameterized.class)
public class NexusScanDataWriterScanTest extends AbstractNexusDataWriterScanTest {

	/**
	 * An implementation of adapting it to an {@link IWritableNexusDevice}. Used to test
	 * picking up a registered nexus device for a detector from the {@link INexusDeviceService}.
	 */
	private static class DummyDetectorNexusDevice implements IWritableNexusDevice<NXdetector> {

		private ILazyWriteableDataset imageDataset;

		private String name;

		public DummyDetectorNexusDevice(String name) {
			// Note: because writePosition passes us the position to write at each point of the scan
			// we don't actually need to wrap the detector itself, although we do still need a name
			this.name = name;
		}

		@Override
		public NexusObjectProvider<NXdetector> getNexusProvider(NexusScanInfo info) throws NexusException {
			final NXdetector det = NexusNodeFactory.createNXdetector();
			final int scanRank = info.getRank();
			// We add 2 to the scan rank to include the image
			imageDataset = det.initializeLazyDataset(NXdetector.NX_DATA, scanRank + 2, Double.class);

			return new NexusObjectWrapper<>(getName(), det, NXdetector.NX_DATA);
		}

		@Override
		public void writePosition(Object data, SliceND scanSlice) throws NexusException {
			try {
				IWritableNexusDevice.writeDataset(imageDataset, data, scanSlice);
			} catch (DatasetException e) {
				throw new NexusException("Could not write data for detector: " + getName());
			}
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public void scanEnd() throws NexusException {
			imageDataset = null;
		}
	}

	/**
	 * Extends DummyDetector to return an image rather than an array of doubles.
	 */
	private static class DummyImageDetector extends DummyDetector {

		private static final int[] IMAGE_SIZE = new int[] { 8, 8 }; // small image size for tests

		@Override
		protected Object acquireData() {
			// The default implementation returns an array of doubles, one for each extraName (i.e. each field has a scalar value)
			// This implementation instead returns a double array, which it also writes to the dataset
			return Random.rand(IMAGE_SIZE);
		}

	}

	/**
	 * Extends {@link DummyDetector} to implement {@link IWritableNexusDevice}.
	 */
	private static class DummyNexusDeviceDetector extends DummyImageDetector implements IWritableNexusDevice<NXdetector> {

		private ILazyWriteableDataset imageDataset;

		@Override
		public NexusObjectProvider<NXdetector> getNexusProvider(NexusScanInfo info) throws NexusException {
			final NXdetector det = NexusNodeFactory.createNXdetector();
			final int scanRank = info.getRank();
			// We add 2 to the scan rank to include the image
			imageDataset = det.initializeLazyDataset(NXdetector.NX_DATA, scanRank + 2, Double.class);

			return new NexusObjectWrapper<>(getName(), det, NXdetector.NX_DATA);
		}

		@Override
		public void atScanEnd() throws DeviceException {
			imageDataset = null;
		}

		@Override
		public void writePosition(Object data, SliceND scanSlice) throws NexusException {
			try {
				IWritableNexusDevice.writeDataset(imageDataset, data, scanSlice);
			} catch (DatasetException e) {
				throw new NexusException("Could not write data for detector: " + getName());
			}
		}

		@Override
		public void scanEnd() throws NexusException {
			imageDataset = null;
		}
	}

	private static final String CLASS_NAME_NEXUS_SCAN_DATA_WRITER = NexusScanDataWriter.class.getSimpleName();
	private static final String ATTRIBUTE_NAME_GDA_FIELD_NAME = "gda_field_name";
	private static final String GROUP_NAME_SCANNABLES = "scannables";

	private static final String ENTRY_NAME = "entry";
	private static final String INSTRUMENT_NAME = "instrument";
	private static final String MONITOR_NAME = "mon01";

	private static final int NUM_SCANNABLE_DATA_NODES = 2;
	private static final int NUM_MONITOR_DATA_NODES = 2;
	private static final int NUM_SCANNABLE_VALUE_ATTRIBUTES = 3;
	private static final int NUM_MONITOR_VALUE_ATTRIBUTES = 3;
	private static final int NUM_METADATA_SCANNABLE_VALUE_ATTRIBUTES = 4;

	@Parameters(name="scanRank = {0}")
	public static Object[] data() {
		return IntStream.rangeClosed(1, MAX_SCAN_RANK).mapToObj(Integer::valueOf).toArray();
	}

	public NexusScanDataWriterScanTest(int scanRank) {
		super(scanRank);
	}

	@BeforeClass
	public static void setUpServices() {
		AbstractNexusDataWriterScanTest.setUpServices();

		final ServiceHolder gdaDataServiceHolder = new ServiceHolder();
		gdaDataServiceHolder.setNexusScanFileService(new NexusScanFileServiceImpl());
		gdaDataServiceHolder.setNexusDeviceService(new NexusDeviceService());

		final org.eclipse.dawnsci.nexus.scan.ServiceHolder oednsServiceHolder = new org.eclipse.dawnsci.nexus.scan.ServiceHolder();
		oednsServiceHolder.setNexusDeviceService(new NexusDeviceService());
		oednsServiceHolder.setNexusBuilderFactory(new DefaultNexusBuilderFactory());

		final org.eclipse.dawnsci.nexus.ServiceHolder oednServiceHolder = new org.eclipse.dawnsci.nexus.ServiceHolder();
		oednServiceHolder.setNexusFileFactory(new NexusFileFactoryHDF5());
		oednServiceHolder.setNexusDeviceAdapterFactory(new GDANexusDeviceAdapterFactory());
	}

	@Override
	public void setUp() throws Exception { // inherits @Before annotation from superclass
		super.setUp();
		final Factory factory = TestHelpers.createTestFactory();
		Finder.addFactory(factory);
	}

	@Override
	public void tearDown() { // inherits @After annotation from superclass
		super.tearDown();
		Finder.removeAllFactories();
		LocalProperties.clearProperty(LocalProperties.GDA_DATA_SCAN_DATAWRITER_DATAFORMAT);
	}

	@Override
	protected void setUpTest(String testName) throws Exception {
		super.setUpTest(testName);
		LocalProperties.set(LocalProperties.GDA_DATA_SCAN_DATAWRITER_DATAFORMAT, CLASS_NAME_NEXUS_SCAN_DATA_WRITER);
	}

	@Test
	public void concurrentScanNexusDeviceDetector() throws Exception {
		detector = new DummyNexusDeviceDetector();
		detector.setName("det");
		concurrentScan(detector, DetectorType.NEXUS_DEVICE, "NexusDeviceDetector");
	}

	@Test
	public void concurrentScanRegisteredNexusDevice() throws Exception {
		detector = new DummyImageDetector();
		detector.setName("det");
		final IWritableNexusDevice<NXdetector> nexusDevice = new DummyDetectorNexusDevice(detector.getName());
		ServiceHolder.getNexusDeviceService().register(nexusDevice);
		concurrentScan(detector, DetectorType.NEXUS_DEVICE, "RegisteredNexusDevice");
	}

	@Override
	protected String getEntryName() {
		return ENTRY_NAME;
	}

	@Override
	protected void checkNexusMetadata(NXentry entry) {
		super.checkNexusMetadata(entry);

		// TODO: get metadata into nexus file (DAQ-3151)
//		// entry_identifier
//		assertThat(entry.getEntry_identifierScalar(), is(equalTo(EXPECTED_ENTRY_IDENTIFER))); // not set
//		// program_name
//		assertThat(entry.getProgram_nameScalar(), is(equalTo(EXPECTED_PROGRAM_NAME)));
//		assertThat(entry.getDataset(DATASET_NAME_SCAN_COMMAND).getString(), is(equalTo(getExpectedScanCommand())));
//		// scan_dimensions
//		assertThat(entry.getDataset(DATASET_NAME_SCAN_DIMENSIONS), is(equalTo(DatasetFactory.createFromObject(scanDimensions))));
//		// title
//		assertThat(entry.getTitleScalar(), is(equalTo(EXPECTED_SCAN_COMMAND))); // title seems to be same as scan command(!)
	}

	@Override
	protected void checkUsers(NXentry entry) {
		// user group // TODO add user data!!
//		final Map<String, NXuser> users = entry.getAllUser();
//		assertThat(users.keySet(), Matchers.contains(EXPECTED_USER_NAME));
//		final NXuser user = users.get(EXPECTED_USER_NAME);
//		assertThat(user, is(notNullValue()));
//		assertThat(user.getNumberOfNodelinks(), is(0));  // note that the created NXuser group is empty
	}

	@Override
	protected void checkInstrumentGroupMetadata(final NXinstrument instrument) {
		// TODO add instrument name (DAQ-3151), when this is same as NexusDataWriter, move this method up to superclass
		assertThat(instrument.getNumberOfDataNodes(), is(0));
//		assertThat(instrument.getNumberOfDataNodes(), is(1));
//		assertThat(instrument.getNameScalar(), is(equalTo(EXPECTED_INSTRUMENT_NAME)));

		final int expectedGroupNodes = getNumDevices() + 1; // group for each device, plus source group scannables:NXcollection for scannables in the locationMap
		assertThat(instrument.getNumberOfGroupNodes(), is(expectedGroupNodes));
		checkSource(instrument);
	}

	private void checkSource(NXinstrument instrument) {
		// TODO, add source, see DAQ-3151, if same as NexusDataWriter, move method up to superclass
//		final NXsource source = instrument.getSource();
//		assertThat(source, is(notNullValue()));
//
//		assertThat(source.getNumberOfDataNodes(), is(3));
//		assertThat(source.getNumberOfGroupNodes(), is(0));
//
//		assertThat(source.getNameScalar(), is(equalTo("DLS")));
//		assertThat(source.getProbeScalar(), is(equalTo("x-ray")));
//		assertThat(source.getTypeScalar(), is(equalTo("Synchrotron X-ray Source")));
	}

	@Override
	protected void checkScannablesAndMonitors(final NXinstrument instrument) throws Exception {
		super.checkScannablesAndMonitors(instrument);

		// scannables that have an entry in the location map are first written inside
		// the 'scannables' group, and datasets are then linked to from location in the location map
		final NXcollection scannablesCollection = instrument.getCollection(GROUP_NAME_SCANNABLES);
		assertThat(scannablesCollection, is(notNullValue()));
		assertThat(scannablesCollection.getNumberOfGroupNodes(), is(getNumMetadataScannables() + 1));

		final NXpositioner firstScannablePositioner = (NXpositioner) scannablesCollection.getGroupNode(
				scannables[0].getName());
		assertThat(firstScannablePositioner, is(notNullValue()));
		checkDefaultScannablePositioner(firstScannablePositioner, 0);

		final Set<String> expectedMetadataScannableNames = getExpectedMetadataScannableNames();
		for (int i = 0; i < METADATA_SCANNABLE_NAMES.length; i++) {
			final String metadataScannableName = METADATA_SCANNABLE_NAMES[i];
			final NXpositioner positioner = (NXpositioner) scannablesCollection.getGroupNode(metadataScannableName);
			if (expectedMetadataScannableNames.contains(metadataScannableName)) {
				assertThat(positioner, is(notNullValue()));
				checkDefaultMetadataScannablePositioner(positioner, i);
			} else {
				assertThat(positioner, is(nullValue()));
			}
		}
	}

	@Override
	protected void checkDefaultScannablePositioner(NXpositioner scannablePos, int scanIndex) throws DatasetException {
		// This is the NXpositioner created by ScannableNexusDevice
		assertThat(scannablePos.getNumberOfDataNodes(), is(NUM_SCANNABLE_DATA_NODES));
		assertThat(scannablePos.getDataNodeMap().keySet(), containsInAnyOrder(
				NXpositioner.NX_NAME, NXpositioner.NX_VALUE));
//				NXpositioner.NX_SOFT_LIMIT_MAX, NXpositioner.NX_SOFT_LIMIT_MIN, // TODO: write soft limit min/max, DAQ-3165
//				DATASET_NAME_VALUE_SET)); // TODO add demand (set) value, see DAQ-3163

		final String scannableName = scannables[scanIndex].getName();
		final DataNode scannableValueDataNode = scannablePos.getDataNode(NXpositioner.NX_VALUE);
		assertThat(scannableValueDataNode, is(notNullValue()));

		// check attributes
		assertThat(scannableValueDataNode.getNumberOfAttributes(), is(
				NUM_SCANNABLE_VALUE_ATTRIBUTES + (scanIndex == 0 ? 1 : 0)));
		if (scanIndex == 0) {
			// the first scannable has an entry in the location map, the value of which is a ScannableWriter
			// configured with units as 'mm'. Note that the ScannableWriter itself does not write when using
			// NexusScanDataWriter. Instead ScannableNexusDevice write the nexus group for scannables that have
			// an entry in the location map to the 'scannables' group, then creates a link to the dataset in
			// that group for each element of the position array at the path in the array returned by
			// ScannableWriter#getPaths() with the corresponding index.
			assertThat(scannableValueDataNode.getAttribute(ATTRIBUTE_NAME_UNITS).getFirstElement(),
					is(equalTo("mm")));
		}
		assertThat(scannableValueDataNode.getAttribute(ATTRIBUTE_NAME_GDA_FIELD_NAME).getFirstElement(),
				is(equalTo(scannableName)));
		assertThat(scannableValueDataNode.getAttribute(ATTRIBUTE_NAME_LOCAL_NAME).getFirstElement(),
				is(equalTo(scannableName + "." + NXpositioner.NX_VALUE)));
		final String expectedTargetPath = "/" + ENTRY_NAME + "/" + INSTRUMENT_NAME + "/" +
				(scanIndex == 0 ? GROUP_NAME_SCANNABLES + "/" : "" ) + scannableName + "/" + NXpositioner.NX_VALUE;
		assertThat(scannableValueDataNode.getAttribute(ATTRIBUTE_NAME_TARGET).getFirstElement(),
				is(equalTo(expectedTargetPath)));

		// check dataset
		assertThat(scannableValueDataNode.getDataset().getSlice(),
				is(equalTo(DatasetFactory.createFromObject(getExpectedScannableDataset(scanIndex))))); // check values

		 // TODO: write soft limit min/max, DAQ-3154
//		assertThat(scannablePos.getSoft_limit_minScalar(), is(equalTo(SCANNABLE_LOWER_BOUND)));
//		assertThat(scannablePos.getSoft_limit_maxScalar(), is(equalTo(SCANNABLE_UPPER_BOUND)));
	}

	protected void checkDefaultMetadataScannablePositioner(NXpositioner positioner, int index) throws DatasetException {
		final String scannableName = METADATA_SCANNABLE_NAMES[index];
		assertThat(positioner.getNumberOfDataNodes(), is(2));

		assertThat(positioner.getNameScalar(), is(equalTo(scannableName)));

		final DataNode valueDataNode = positioner.getDataNode(NXpositioner.NX_VALUE);
		checkMetadataScannableValueDataNode(index, scannableName, valueDataNode);
	}

	@Override
	protected void checkConfiguredScannablePositioner(final String scannableName, NXpositioner scannablePos) throws DatasetException {
		assertThat(scannablePos.getNumberOfDataNodes(), is(1));
		assertThat(scannablePos.getDataNodeMap().keySet(), contains(scannableName));

		final DataNode scannableValueDataNode = scannablePos.getDataNode(scannableName);
		assertThat(scannableValueDataNode, is(notNullValue()));
		assertThat(scannableValueDataNode.getNumberOfAttributes(), is(4));
		assertThat(scannableValueDataNode.getAttribute(ATTRIBUTE_NAME_LOCAL_NAME).getFirstElement(),
				is(equalTo(scannableName + "." + NXpositioner.NX_VALUE)));
		assertThat(scannableValueDataNode.getAttribute(ATTRIBUTE_NAME_TARGET).getFirstElement(), is(equalTo(
				"/" + ENTRY_NAME + "/" + INSTRUMENT_NAME + "/" + GROUP_NAME_SCANNABLES + "/"
						+ scannableName + "/" + NXpositioner.NX_VALUE)));
		assertThat(scannableValueDataNode.getAttribute(ATTRIBUTE_NAME_GDA_FIELD_NAME).getFirstElement(),
				is(equalTo(scannableName)));
		assertThat(scannableValueDataNode.getAttribute(ATTRIBUTE_NAME_UNITS).getFirstElement(),
				is(equalTo("mm")));

		assertThat(scannableValueDataNode.getDataset().getSlice(),
				is(equalTo(DatasetFactory.createFromObject(getExpectedScannableDataset(0))))); // check values
	}

	@Override
	protected void checkMonitorPositioner(NXpositioner monitorPos) throws Exception {
		assertThat(monitorPos.getNumberOfDataNodes(), is(NUM_MONITOR_DATA_NODES));
		assertThat(monitorPos.getDataNodeMap().keySet(), containsInAnyOrder(NXpositioner.NX_NAME, NXpositioner.NX_VALUE));
		assertThat(monitorPos.getNameScalar(), is(equalTo(MONITOR_NAME)));

		final DataNode monitorValueDataNode = monitorPos.getDataNode(NXpositioner.NX_VALUE);
		assertThat(monitorValueDataNode, is(notNullValue()));
		assertThat(monitorValueDataNode.getNumberOfAttributes(), is(NUM_MONITOR_VALUE_ATTRIBUTES));
		assertThat(monitorValueDataNode.getAttribute(ATTRIBUTE_NAME_GDA_FIELD_NAME).getFirstElement(), is(equalTo(NXpositioner.NX_VALUE)));
		assertThat(monitorValueDataNode.getAttribute(ATTRIBUTE_NAME_LOCAL_NAME).getFirstElement(),
				is(equalTo(MONITOR_NAME + "." + NXpositioner.NX_VALUE)));
		assertThat(monitorValueDataNode.getAttribute(ATTRIBUTE_NAME_TARGET).getFirstElement(),
				is(equalTo("/" + ENTRY_NAME + "/" + INSTRUMENT_NAME + "/" + MONITOR_NAME + "/" + NXpositioner.NX_VALUE)));
		assertThat(monitorValueDataNode.getDataset().getSlice(),
				is(equalTo(DatasetFactory.zeros(scanDimensions).fill(MONITOR_VALUE)))); // check values
	}

	@Override
	protected void checkMetadataScannablePositioner(NXpositioner positioner, int index) throws Exception {
		final String scannableName = METADATA_SCANNABLE_NAMES[index];
		assertThat(positioner.getNumberOfDataNodes(), is(1));

		final DataNode valueDataNode = positioner.getDataNode(scannableName);
		checkMetadataScannableValueDataNode(index, scannableName, valueDataNode);
	}

	private void checkMetadataScannableValueDataNode(int index, final String scannableName,
			final DataNode valueDataNode) throws DatasetException {
		assertThat(valueDataNode, is(notNullValue()));
		assertThat(valueDataNode.getNumberOfAttributes(), is(NUM_METADATA_SCANNABLE_VALUE_ATTRIBUTES));
		assertThat(valueDataNode.getAttribute(ATTRIBUTE_NAME_GDA_FIELD_NAME).getFirstElement(), is(equalTo(scannableName)));
		assertThat(valueDataNode.getAttribute(ATTRIBUTE_NAME_LOCAL_NAME).getFirstElement(),
				is(equalTo(scannableName + "." + NXpositioner.NX_VALUE)));
		assertThat(valueDataNode.getAttribute(ATTRIBUTE_NAME_TARGET).getFirstElement(),
				is(equalTo("/" + ENTRY_NAME + "/" + INSTRUMENT_NAME + "/" + GROUP_NAME_SCANNABLES + "/" + scannableName + "/" + NXpositioner.NX_VALUE)));
		assertThat(valueDataNode.getAttribute(ATTRIBUTE_NAME_UNITS).getFirstElement(), is(equalTo("mm")));

		assertThat(valueDataNode.getDataset().getShape(), is(equalTo(EMPTY_SHAPE)));
		assertThat(valueDataNode.getDataset().getSlice().getDouble(), is(equalTo((double) index)));
	}

	@Override
	protected void checkDataGroups(NXentry entry) {
		final Map<String, NXdata> dataGroups = entry.getAllData();

		final String dataDeviceName = detector != null ? detector.getName() : monitor.getName();
		final String signalFieldName = detector != null ? getDetectorPrimaryFieldName() : monitor.getName();
		assertThat(dataGroups.keySet(), contains(dataDeviceName)); // An NXdata group is created for the monitor as this scan has no detectors
		final NXdata data = dataGroups.get(dataDeviceName);
		assertThat(data, is(notNullValue()));

		// check that the value field of the monitor and scannable have been linked to
		assertThat(data.getNumberOfDataNodes(), is(getNumScannedDevices()));
		assertThat(data.getDataNode(signalFieldName), is(both(notNullValue()).and(sameInstance(detector != null ?
				entry.getInstrument().getDetector(dataDeviceName).getDataNode(getDetectorPrimaryFieldName()) :
				entry.getInstrument().getPositioner(dataDeviceName).getDataNode(NXpositioner.NX_VALUE)))));

		// check that the attributes have been added according to the 2014 NXdata format
		assertThat(data.getNumberOfAttributes(), is(scanRank + 3 + (detector != null ? 1 : 0))); // each scannable, monitor (if signal field not from monitor), signal, axes, NXclass
		assertSignal(data, signalFieldName);
		assertAxes(data, Stream.concat(Arrays.stream(scannables).map(Scannable::getName),
				Collections.nCopies(data.getDataNode(signalFieldName).getRank() - scanRank, ".").stream()).toArray(String[]::new));

		final int[] expectedIndices = IntStream.range(0, scanRank).toArray();
		for (int i = 0; i < scanRank; i++) {
			final String scannableName = scannables[i].getName();
			final DataNode expectedDataNode = entry.getInstrument().getPositioner(scannableName).getDataNode(
					i == 0 ? scannableName : NXpositioner.NX_VALUE);

			assertThat(data.getDataNode(scannableName), is(both(notNullValue()).and(sameInstance(expectedDataNode))));
			assertIndices(data, scannableName, expectedIndices);
		}
	}

	private String getDetectorPrimaryFieldName() {
		switch (detectorType) {
			case NONE: throw new IllegalArgumentException(); // this method is not called in this case
			case NEXUS_DEVICE: return NXdetector.NX_DATA;
			case COUNTER_TIMER: return detector.getExtraNames()[0];
			default: throw new IllegalArgumentException("Unknown detector type: " + detectorType);
		}
	}

}
