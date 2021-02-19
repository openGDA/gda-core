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

import static gda.configuration.properties.LocalProperties.GDA_DATA_SCAN_DATAWRITER_DATAFORMAT;
import static gda.data.scan.datawriter.NexusScanDataWriter.PROPERTY_NAME_ENTRY_NAME;
import static org.eclipse.scanning.test.utilities.scan.nexus.NexusAssert.assertAxes;
import static org.eclipse.scanning.test.utilities.scan.nexus.NexusAssert.assertIndices;
import static org.eclipse.scanning.test.utilities.scan.nexus.NexusAssert.assertSignal;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.both;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.hdf5.nexus.NexusFileFactoryHDF5;
import org.eclipse.dawnsci.nexus.IWritableNexusDevice;
import org.eclipse.dawnsci.nexus.NXbeam;
import org.eclipse.dawnsci.nexus.NXcollection;
import org.eclipse.dawnsci.nexus.NXdata;
import org.eclipse.dawnsci.nexus.NXdetector;
import org.eclipse.dawnsci.nexus.NXentry;
import org.eclipse.dawnsci.nexus.NXinsertion_device;
import org.eclipse.dawnsci.nexus.NXinstrument;
import org.eclipse.dawnsci.nexus.NXmonochromator;
import org.eclipse.dawnsci.nexus.NXpositioner;
import org.eclipse.dawnsci.nexus.NXsample;
import org.eclipse.dawnsci.nexus.NXsource;
import org.eclipse.dawnsci.nexus.NXuser;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusNodeFactory;
import org.eclipse.dawnsci.nexus.NexusScanInfo;
import org.eclipse.dawnsci.nexus.builder.NexusObjectProvider;
import org.eclipse.dawnsci.nexus.builder.NexusObjectWrapper;
import org.eclipse.dawnsci.nexus.builder.impl.DefaultNexusBuilderFactory;
import org.eclipse.dawnsci.nexus.device.INexusDeviceService;
import org.eclipse.dawnsci.nexus.device.impl.NexusDeviceService;
import org.eclipse.dawnsci.nexus.scan.impl.NexusScanFileServiceImpl;
import org.eclipse.dawnsci.nexus.template.impl.NexusTemplateServiceImpl;
import org.eclipse.january.DatasetException;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.ILazyWriteableDataset;
import org.eclipse.january.dataset.SliceND;
import org.eclipse.scanning.device.BeamNexusDevice;
import org.eclipse.scanning.device.CommonBeamlineDevicesConfiguration;
import org.eclipse.scanning.device.InsertionDeviceNexusDevice;
import org.eclipse.scanning.device.InsertionDeviceNexusDevice.InsertionDeviceType;
import org.eclipse.scanning.device.MonochromatorNexusDevice;
import org.eclipse.scanning.device.Services;
import org.eclipse.scanning.device.SourceNexusDevice;
import org.eclipse.scanning.device.UserNexusDevice;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import gda.configuration.properties.LocalProperties;
import gda.data.ServiceHolder;
import gda.data.scan.nexus.device.GDANexusDeviceAdapterFactory;
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.detector.DummyDetector;
import gda.factory.Finder;
import gda.jython.IBatonStateProvider;
import gda.jython.InterfaceProvider;
import gda.jython.batoncontrol.ClientDetails;
import uk.ac.diamond.daq.scanning.ScannableDeviceConnectorService;

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

	private static final String INSTRUMENT_NAME = "instrument";
	private static final String MONITOR_NAME = "mon01";

	private static final String BEAM_DEVICE_NAME = "beam";
	private static final String INSERTION_DEVICE_NAME = "insertion_device";
	private static final String MONOCHROMATOR_DEVICE_NAME = "monochromator";
	private static final String SOURCE_DEVICE_NAME = "source";
	private static final String USER_DEVICE_NAME = "user";

	private static final double EXPECTED_INSERTION_DEVICE_TAPER = 7.432;
	private static final int EXPECTED_INSERTION_DEVICE_HARMONIC = 3;

	private static final double EXPECTED_MONOCHROMATOR_ENERGY_ERROR = 2.53;

	private static final double EXPECTED_BEAM_EXTENT = 0.1;
	private static final double EXPECTED_BEAM_INCIDENT_ENERGY = 350.0;
	private static final double EXPECTED_BEAM_INCIDENT_DIVERGENCE = 1.23;
	private static final double EXPECTED_BEAM_INCIDENT_POLARIZATION = 4.55;
	private static final double EXPECTED_BEAM_FLUX = 92.2;

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

		final NexusDeviceService nexusDeviceService = new NexusDeviceService();

		final ServiceHolder gdaDataServiceHolder = new ServiceHolder();
		gdaDataServiceHolder.setNexusScanFileService(new NexusScanFileServiceImpl());
		gdaDataServiceHolder.setNexusDeviceService(nexusDeviceService);
		gdaDataServiceHolder.setCommonBeamlineDevicesConfiguration(createCommonBeamLineDevicesConfiguration());

		final org.eclipse.dawnsci.nexus.scan.ServiceHolder oednsServiceHolder = new org.eclipse.dawnsci.nexus.scan.ServiceHolder();
		oednsServiceHolder.setNexusDeviceService(nexusDeviceService);
		oednsServiceHolder.setNexusBuilderFactory(new DefaultNexusBuilderFactory());
		oednsServiceHolder.setTemplateService(new NexusTemplateServiceImpl());

		final org.eclipse.dawnsci.nexus.ServiceHolder oednServiceHolder = new org.eclipse.dawnsci.nexus.ServiceHolder();
		oednServiceHolder.setNexusFileFactory(new NexusFileFactoryHDF5());
		oednServiceHolder.setNexusDeviceAdapterFactory(new GDANexusDeviceAdapterFactory());

		new Services().setScannableDeviceService(new ScannableDeviceConnectorService());
	}

	private static CommonBeamlineDevicesConfiguration createCommonBeamLineDevicesConfiguration() {
		final CommonBeamlineDevicesConfiguration deviceConfig = new CommonBeamlineDevicesConfiguration();
		deviceConfig.setBeamName(BEAM_DEVICE_NAME);
		deviceConfig.setInsertionDeviceName(INSERTION_DEVICE_NAME);
		deviceConfig.setMonochromatorName(MONOCHROMATOR_DEVICE_NAME);
		deviceConfig.setSourceName(SOURCE_DEVICE_NAME);
		deviceConfig.setUserDeviceName(USER_DEVICE_NAME);

		return deviceConfig;
	}

	@Override
	protected void setUpMetadata() throws Exception {
		super.setUpMetadata();

		final ClientDetails userDetails = new ClientDetails(0, EXPECTED_USER_ID, EXPECTED_USER_NAME, "ws001", 0, true, "visit1");
		final IBatonStateProvider batonStateProvider = mock(IBatonStateProvider.class);
		when(batonStateProvider.getBatonHolder()).thenReturn(userDetails);
		InterfaceProvider.setBatonStateProviderForTesting(batonStateProvider);
	}

	@Override
	public void tearDown() { // inherits @After annotation from superclass
		super.tearDown();
		Finder.removeAllFactories();
		LocalProperties.clearProperty(GDA_DATA_SCAN_DATAWRITER_DATAFORMAT);
	}

	@Override
	protected void setUpTest(String testName) throws Exception {
		super.setUpTest(testName);
		LocalProperties.set(GDA_DATA_SCAN_DATAWRITER_DATAFORMAT, CLASS_NAME_NEXUS_SCAN_DATA_WRITER);
		LocalProperties.set(PROPERTY_NAME_ENTRY_NAME, ENTRY_NAME);
		setupCommonBeamlineDevices(); // must be done after super.setUpTest() to use jython namespace
	}

	private void setupCommonBeamlineDevices() throws DeviceException {
		createBeamDevice();
		createInsertionDevice();
		createMonochromatorDevice();
		createSourceDevice();
		createUserDevice();
	}

	private void createBeamDevice() throws DeviceException {
		final String beamExtentScannableName = "beam_extent";
		final String incidentEnergyScannableName = "incident_energy";
		final String incidentPolarizationScannableName = "incident_polarization";
		final String incidentBeamDivergenceScannableName = "incident_beam_divergence";
		final String fluxScannableName = "flux";

		createScannable(beamExtentScannableName, EXPECTED_BEAM_EXTENT);
		createScannable(incidentEnergyScannableName, EXPECTED_BEAM_INCIDENT_ENERGY);
		createScannable(incidentBeamDivergenceScannableName, EXPECTED_BEAM_INCIDENT_DIVERGENCE);
		createScannable(incidentPolarizationScannableName, EXPECTED_BEAM_INCIDENT_POLARIZATION);
		createScannable(fluxScannableName, EXPECTED_BEAM_FLUX);

		final BeamNexusDevice beamDevice = new BeamNexusDevice();
		beamDevice.setName(BEAM_DEVICE_NAME);
		beamDevice.setIncidentEnergyScannableName(incidentEnergyScannableName);
		beamDevice.setIncidentBeamDivergenceScannableName(incidentBeamDivergenceScannableName);
		beamDevice.setIncidentPolarizationScannableName(incidentPolarizationScannableName);
		beamDevice.setBeamExtentScannableName(beamExtentScannableName);
		beamDevice.setFluxScannableName(fluxScannableName);
		ServiceHolder.getNexusDeviceService().register(beamDevice);
	}

	private void createInsertionDevice() throws DeviceException {
		final String gapScannableName = "id_gap";
		final String taperScannableName = "id_taper";
		final String harmonicScannableName = "id_harmonic";

		createScannable(gapScannableName, EXPECTED_INSERTION_DEVICE_GAP);
		createScannable(taperScannableName, EXPECTED_INSERTION_DEVICE_TAPER);
		createScannable(harmonicScannableName, EXPECTED_INSERTION_DEVICE_HARMONIC);

		final InsertionDeviceNexusDevice insertionDevice = new InsertionDeviceNexusDevice();
		insertionDevice.setName(INSERTION_DEVICE_NAME);
		insertionDevice.setType(InsertionDeviceType.WIGGLER.toString());
		insertionDevice.setGapScannableName(gapScannableName);
		insertionDevice.setTaperScannableName(taperScannableName);
		insertionDevice.setHarmonicScannableName(harmonicScannableName);
		ServiceHolder.getNexusDeviceService().register(insertionDevice);
	}

	private void createMonochromatorDevice() throws DeviceException {
		final String energyScannableName = "mono_energy";
		final String energyErrorScannableName = "mono_energy_error";

		createScannable(energyScannableName, EXPECTED_MONOCHROMATOR_ENERGY);
		createScannable(energyErrorScannableName, EXPECTED_MONOCHROMATOR_ENERGY_ERROR);

		final MonochromatorNexusDevice monochromator = new MonochromatorNexusDevice();
		monochromator.setName(MONOCHROMATOR_DEVICE_NAME);
		monochromator.setEnergyScannableName(energyScannableName);
		monochromator.setEnergyErrorScannableName(energyErrorScannableName);
		ServiceHolder.getNexusDeviceService().register(monochromator);
	}

	private void createSourceDevice() throws DeviceException {
		final String sourceCurrentScannableName = "source_current";
		createScannable(sourceCurrentScannableName, EXPECTED_SOURCE_CURRENT);

		final SourceNexusDevice source = new SourceNexusDevice();
		source.setName(SOURCE_DEVICE_NAME);
		source.setSourceName("Diamond Light Source");
		source.setCurrentScannableName(sourceCurrentScannableName);
		ServiceHolder.getNexusDeviceService().register(source);
	}

	private void createUserDevice() throws DeviceException {
		final UserNexusDevice userDevice = new UserNexusDevice();
		userDevice.setName(USER_DEVICE_NAME);
		ServiceHolder.getNexusDeviceService().register(userDevice);
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
		final Map<String, NXuser> users = entry.getAllUser();
		assertThat(users.keySet(), contains(EXPECTED_USER_GROUP_NAME));
		final NXuser user = users.get(EXPECTED_USER_GROUP_NAME);
		assertThat(user, is(notNullValue()));

		assertThat(user.getDataNodeNames(), containsInAnyOrder(NXuser.NX_FACILITY_USER_ID, NXuser.NX_NAME));
		assertThat(user.getFacility_user_idScalar(), is(equalTo(EXPECTED_USER_ID)));
		assertThat(user.getNameScalar(), is(equalTo(EXPECTED_USER_NAME)));
	}

	@Override
	protected void checkInstrumentGroupMetadata(final NXinstrument instrument) {
		// TODO add instrument name (DAQ-3151), when this is same as NexusDataWriter, move this method up to superclass
		assertThat(instrument.getDataNodeNames(), is(empty()));
//		assertThat(instrument.getNameScalar(), is(equalTo(EXPECTED_INSTRUMENT_NAME)));

		// group for each device, each common (metadata) device (e.g. NXMonochromator), plus source group scannables:NXcollection (for scannables in the locationMap)
		assertThat(instrument.getGroupNodeNames(), containsInAnyOrder(getExpectedInstrumentGroupNames()));
	}

	private String[] getExpectedInstrumentGroupNames() {
		final Set<String> expectedGroupNames = new HashSet<>(Arrays.asList(getScannableAndMonitorNames()));
		if (detector != null) {
			expectedGroupNames.add(detector.getName());
		}

		expectedGroupNames.add(GROUP_NAME_SCANNABLES);
		expectedGroupNames.addAll(getExpectedMetadataScannableNames());
		expectedGroupNames.addAll(ServiceHolder.getCommonBeamlineDevicesConfiguration().getCommonDeviceNames());
		expectedGroupNames.removeAll(Arrays.asList(USER_DEVICE_NAME, BEAM_DEVICE_NAME)); // added directly to NXentry

		return expectedGroupNames.toArray(new String[expectedGroupNames.size()]);
	}

	@Override
	protected void checkScannablesAndMonitors(final NXinstrument instrument) throws Exception {
		super.checkScannablesAndMonitors(instrument);

		// scannables that have an entry in the location map are first written inside
		// the 'scannables' group, and datasets are then linked to from location in the location map
		// this is the first (but not subsequent) scanned scannables, and all metadata scannables
		final NXcollection scannablesCollection = instrument.getCollection(GROUP_NAME_SCANNABLES);
		assertThat(scannablesCollection, is(notNullValue()));
		final Set<String> expectedScannableNames = new HashSet<>(getExpectedMetadataScannableNames());
		expectedScannableNames.add(scannables[0].getName());
		assertThat(scannablesCollection.getGroupNodeNames(), containsInAnyOrder(
				expectedScannableNames.toArray(new String[expectedScannableNames.size()])));

		final NXpositioner firstScannablePositioner = (NXpositioner) scannablesCollection.getGroupNode(
				scannables[0].getName());
		assertThat(firstScannablePositioner, is(notNullValue()));
		checkDefaultScannablePositioner(firstScannablePositioner, 0);

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
		final String[] expectedDataNodeNames = { NXpositioner.NX_NAME, NXpositioner.NX_VALUE,
				NXpositioner.NX_SOFT_LIMIT_MAX, NXpositioner.NX_SOFT_LIMIT_MIN };
		assertThat(scannablePos.getDataNodeNames(), containsInAnyOrder(expectedDataNodeNames));

		final String scannableName = scannables[scanIndex].getName();
		final DataNode scannableValueDataNode = scannablePos.getDataNode(NXpositioner.NX_VALUE);
		assertThat(scannableValueDataNode, is(notNullValue()));

		// check attributes
		final String[] expectedAttributeNames = { ATTRIBUTE_NAME_UNITS, ATTRIBUTE_NAME_GDA_FIELD_NAME,
				ATTRIBUTE_NAME_LOCAL_NAME, ATTRIBUTE_NAME_TARGET };
		assertThat(scannableValueDataNode.getAttributeNames(), containsInAnyOrder(expectedAttributeNames));

		assertThat(scannableValueDataNode.getAttribute(ATTRIBUTE_NAME_UNITS).getFirstElement(),
				is(equalTo("mm")));
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

		// check upper/lower bounds
		assertThat(scannablePos.getSoft_limit_minScalar(), is(equalTo(SCANNABLE_LOWER_BOUND)));
		assertThat(scannablePos.getSoft_limit_maxScalar(), is(equalTo(SCANNABLE_UPPER_BOUND)));
	}

	protected void checkDefaultMetadataScannablePositioner(NXpositioner positioner, int index) throws DatasetException {
		final String[] expectedDataNodeNames = { NXpositioner.NX_NAME, NXpositioner.NX_VALUE,
				NXpositioner.NX_SOFT_LIMIT_MAX, NXpositioner.NX_SOFT_LIMIT_MIN };
		assertThat(positioner.getDataNodeNames(), containsInAnyOrder(expectedDataNodeNames));

		final String scannableName = METADATA_SCANNABLE_NAMES[index];
		assertThat(positioner.getNameScalar(), is(equalTo(scannableName)));

		final DataNode valueDataNode = positioner.getDataNode(NXpositioner.NX_VALUE);
		checkMetadataScannableValueDataNode(index, scannableName, valueDataNode);
	}

	@Override
	protected void checkConfiguredScannablePositioner(final String scannableName, NXpositioner scannablePos) throws DatasetException {
		assertThat(scannablePos.getDataNodeNames(), contains(scannableName));

		final DataNode scannableValueDataNode = scannablePos.getDataNode(scannableName);
		assertThat(scannableValueDataNode, is(notNullValue()));

		final String[] expectedAttributeNames = new String[] { ATTRIBUTE_NAME_LOCAL_NAME,
				ATTRIBUTE_NAME_TARGET, ATTRIBUTE_NAME_GDA_FIELD_NAME, ATTRIBUTE_NAME_UNITS };
		assertThat(scannableValueDataNode.getAttributeNames(), containsInAnyOrder(expectedAttributeNames));

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
		final String[] expectedDataNodeNames = { NXpositioner.NX_NAME, NXpositioner.NX_VALUE };
		assertThat(monitorPos.getDataNodeNames(), containsInAnyOrder(expectedDataNodeNames));

		assertThat(monitorPos.getNameScalar(), is(equalTo(MONITOR_NAME)));

		final DataNode monitorValueDataNode = monitorPos.getDataNode(NXpositioner.NX_VALUE);
		assertThat(monitorValueDataNode, is(notNullValue()));

		final String[] expectedAttributeNames = { ATTRIBUTE_NAME_GDA_FIELD_NAME,
				ATTRIBUTE_NAME_LOCAL_NAME, ATTRIBUTE_NAME_TARGET };
		assertThat(monitorValueDataNode.getAttributeNames(), containsInAnyOrder(expectedAttributeNames));

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
		assertThat(positioner.getDataNodeNames(), contains(scannableName));

		final DataNode valueDataNode = positioner.getDataNode(scannableName);
		checkMetadataScannableValueDataNode(index, scannableName, valueDataNode);
	}

	private void checkMetadataScannableValueDataNode(int index, final String scannableName,
			final DataNode valueDataNode) throws DatasetException {
		assertThat(valueDataNode, is(notNullValue()));

		final String[] expectedAttributeNames = { ATTRIBUTE_NAME_GDA_FIELD_NAME,
				ATTRIBUTE_NAME_LOCAL_NAME, ATTRIBUTE_NAME_TARGET, ATTRIBUTE_NAME_UNITS };
		assertThat(valueDataNode.getAttributeNames(), containsInAnyOrder(expectedAttributeNames));
		assertThat(valueDataNode.getNumberOfAttributes(), is(expectedAttributeNames.length));

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

		final boolean detectorIsPrimaryDevice = detector != null && detectorType != DetectorType.FILE_CREATOR;
		final String dataDeviceName = detectorIsPrimaryDevice ? detector.getName() : monitor.getName();
		final String signalFieldName = detectorIsPrimaryDevice ? getDetectorPrimaryFieldName() : monitor.getName();
		assertThat(dataGroups.keySet(), contains(dataDeviceName)); // An NXdata group is created for the monitor as this scan has no detectors
		final NXdata data = dataGroups.get(dataDeviceName);
		assertThat(data, is(notNullValue()));

		// check that the value field of the monitor and scannable have been linked to
		final int numExpectedDevices = getNumScannedDevices() - (detector != null && !detectorIsPrimaryDevice ? 1 : 0);
		assertThat(data.getNumberOfDataNodes(), is(numExpectedDevices));
		assertThat(data.getDataNode(signalFieldName), is(both(notNullValue()).and(sameInstance(detectorIsPrimaryDevice ?
				entry.getInstrument().getDetector(dataDeviceName).getDataNode(getDetectorPrimaryFieldName()) :
				entry.getInstrument().getPositioner(dataDeviceName).getDataNode(NXpositioner.NX_VALUE)))));

		// check that the attributes have been added according to the 2014 NXdata format
		// attributes created for each scannable, monitor (if signal field not from monitor), signal, axes, NXclass
		final int expectedNumAttributes = scanRank + 3 + (detector != null && detectorIsPrimaryDevice ? 1 : 0);
		assertThat(data.getNumberOfAttributes(), is(expectedNumAttributes));
		assertSignal(data, signalFieldName);
		assertAxes(data, Stream.concat(Arrays.stream(scannables).map(Scannable::getName),
				Collections.nCopies(data.getDataNode(signalFieldName).getRank() - scanRank, ".").stream())
						.toArray(String[]::new));

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
			case GENERIC: return NXdetector.NX_DATA;
			case FILE_CREATOR: throw new IllegalArgumentException("File creator detector does not have primary fields.");
			default: throw new IllegalArgumentException("Unknown detector type: " + detectorType);
		}
	}

	@Override
	protected void checkSourceGroup(NXinstrument instrument) {
		final NXsource source = instrument.getSource();
		assertThat(source, is(notNullValue()));

		assertThat(source.getNumberOfDataNodes(), is(4));
		assertThat(source.getNumberOfGroupNodes(), is(0));

		assertThat(source.getNameScalar(), is(equalTo("Diamond Light Source")));
		assertThat(source.getProbeScalar(), is(equalTo("x-ray")));
		assertThat(source.getTypeScalar(), is(equalTo("Synchrotron X-ray Source")));
		assertThat(source.getCurrentScalar(), is(equalTo(EXPECTED_SOURCE_CURRENT)));
	}

	@Override
	protected void checkSampleGroup(NXentry entry) {
		final NXsample sample = entry.getSample();
		assertThat(sample, is(notNullValue()));

		final NXbeam beam = sample.getBeam();
		assertThat(beam, is(notNullValue()));
		assertThat(beam.getDistanceScalar(), is(equalTo(0.0)));
		assertThat(beam.getDouble(BeamNexusDevice.FIELD_NAME_EXTENT), is(equalTo(EXPECTED_BEAM_EXTENT)));
		assertThat(beam.getIncident_energyScalar(), is(equalTo(EXPECTED_BEAM_INCIDENT_ENERGY)));
		assertThat(beam.getIncident_beam_divergenceScalar(), is(equalTo(EXPECTED_BEAM_INCIDENT_DIVERGENCE)));
		assertThat(beam.getIncident_polarizationScalar(), is(equalTo(EXPECTED_BEAM_INCIDENT_POLARIZATION)));
		assertThat(beam.getFluxScalar(), is(equalTo(EXPECTED_BEAM_FLUX)));
	}

	@Override
	protected void checkInsertionDeviceGroup(NXinstrument instrument) {
		final NXinsertion_device insertionDevice = instrument.getInsertion_device();
		assertThat(insertionDevice, is(notNullValue()));
		assertThat(insertionDevice.getGapScalar(), is(equalTo(EXPECTED_INSERTION_DEVICE_GAP)));
		assertThat(insertionDevice.getTaperScalar(), is(equalTo(EXPECTED_INSERTION_DEVICE_TAPER)));
		// DummyScannable always returns a double which gets converted to a long
		assertThat(insertionDevice.getHarmonicScalar(), is(equalTo((long) EXPECTED_INSERTION_DEVICE_HARMONIC)));
	}

	@Override
	protected void checkMonochromatorGroup(NXinstrument instrument) {
		final NXmonochromator monochromator = instrument.getMonochromator();
		assertThat(monochromator, is(notNullValue()));
		assertThat(monochromator.getEnergyScalar(), is(equalTo(EXPECTED_MONOCHROMATOR_ENERGY)));
		assertThat(monochromator.getEnergy_errorScalar(), is(equalTo(EXPECTED_MONOCHROMATOR_ENERGY_ERROR)));
	}

}
