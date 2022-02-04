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
import static gda.configuration.properties.LocalProperties.GDA_END_STATION_NAME;
import static gda.configuration.properties.LocalProperties.GDA_INSTRUMENT;
import static gda.data.scan.datawriter.NexusScanDataWriter.FIELD_NAME_BEAMLINE;
import static gda.data.scan.datawriter.NexusScanDataWriter.FIELD_NAME_END_STATION;
import static gda.data.scan.datawriter.NexusScanDataWriter.PROPERTY_NAME_ENTRY_NAME;
import static gda.data.scan.datawriter.NexusScanDataWriter.PROPERTY_VALUE_DATA_FORMAT_NEXUS_SCAN;
import static gda.data.scan.nexus.device.AbstractScannableNexusDevice.FIELD_NAME_NAME;
import static gda.data.scan.nexus.device.AbstractScannableNexusDevice.PROPERTY_VALUE_WRITE_DECIMALS;
import static gda.data.scan.nexus.device.BeforeScanSnapshotWriter.BEFORE_SCAN_COLLECTION_NAME;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static org.eclipse.dawnsci.nexus.scan.NexusScanConstants.FIELD_NAME_CURRENT_SCRIPT_NAME;
import static org.eclipse.dawnsci.nexus.scan.NexusScanConstants.FIELD_NAME_SCAN_COMMAND;
import static org.eclipse.dawnsci.nexus.scan.NexusScanConstants.FIELD_NAME_SCAN_DURATION;
import static org.eclipse.dawnsci.nexus.scan.NexusScanConstants.FIELD_NAME_SCAN_END_TIME;
import static org.eclipse.dawnsci.nexus.scan.NexusScanConstants.FIELD_NAME_SCAN_FIELDS;
import static org.eclipse.dawnsci.nexus.scan.NexusScanConstants.FIELD_NAME_SCAN_SHAPE;
import static org.eclipse.dawnsci.nexus.scan.NexusScanConstants.FIELD_NAME_SCAN_START_TIME;
import static org.eclipse.dawnsci.nexus.scan.NexusScanConstants.GROUP_NAME_DIAMOND_SCAN;
import static org.eclipse.dawnsci.nexus.test.utilities.NexusAssert.assertAxes;
import static org.eclipse.dawnsci.nexus.test.utilities.NexusAssert.assertDiamondScanGroup;
import static org.eclipse.dawnsci.nexus.test.utilities.NexusAssert.assertIndices;
import static org.eclipse.dawnsci.nexus.test.utilities.NexusAssert.assertSignal;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.measure.quantity.Length;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.dawnsci.analysis.api.tree.Attribute;
import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.analysis.api.tree.GroupNode;
import org.eclipse.dawnsci.hdf5.nexus.NexusFileFactoryHDF5;
import org.eclipse.dawnsci.nexus.IWritableNexusDevice;
import org.eclipse.dawnsci.nexus.NXbeam;
import org.eclipse.dawnsci.nexus.NXcollection;
import org.eclipse.dawnsci.nexus.NXdata;
import org.eclipse.dawnsci.nexus.NXdetector;
import org.eclipse.dawnsci.nexus.NXentry;
import org.eclipse.dawnsci.nexus.NXinsertion_device;
import org.eclipse.dawnsci.nexus.NXinstrument;
import org.eclipse.dawnsci.nexus.NXmirror;
import org.eclipse.dawnsci.nexus.NXmonochromator;
import org.eclipse.dawnsci.nexus.NXpositioner;
import org.eclipse.dawnsci.nexus.NXsample;
import org.eclipse.dawnsci.nexus.NXslit;
import org.eclipse.dawnsci.nexus.NXsource;
import org.eclipse.dawnsci.nexus.NXuser;
import org.eclipse.dawnsci.nexus.NexusBaseClass;
import org.eclipse.dawnsci.nexus.NexusConstants;
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
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyWriteableDataset;
import org.eclipse.january.dataset.SliceND;
import org.eclipse.scanning.device.BeamNexusDevice;
import org.eclipse.scanning.device.CommonBeamlineDevicesConfiguration;
import org.eclipse.scanning.device.InsertionDeviceNexusDevice;
import org.eclipse.scanning.device.InsertionDeviceNexusDevice.InsertionDeviceType;
import org.eclipse.scanning.device.MetadataNode;
import org.eclipse.scanning.device.MonochromatorNexusDevice;
import org.eclipse.scanning.device.NexusMetadataDevice;
import org.eclipse.scanning.device.ScalarField;
import org.eclipse.scanning.device.ScannableComponentField;
import org.eclipse.scanning.device.ScannableField;
import org.eclipse.scanning.device.Services;
import org.eclipse.scanning.device.SourceNexusDevice;
import org.eclipse.scanning.device.UserNexusDevice;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.google.common.collect.Streams;

import gda.configuration.properties.LocalProperties;
import gda.data.ServiceHolder;
import gda.data.scan.nexus.device.BeforeScanSnapshotWriter;
import gda.data.scan.nexus.device.GDANexusDeviceAdapterFactory;
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.ScannableMotionUnits;
import gda.device.detector.DummyDetector;
import gda.device.scannable.DummyMultiFieldUnitsScannable;
import gda.factory.Finder;
import gda.jython.IBatonStateProvider;
import gda.jython.InterfaceProvider;
import gda.jython.MockJythonServerFacade;
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

	private static final String ATTRIBUTE_NAME_GDA_FIELD_NAME = "gda_field_name";
	private static final String GROUP_NAME_SCANNABLES = "scannables";

	private static final String INSTRUMENT_NAME = "instrument";
	private static final String MONITOR_NAME = "mon01";

	private static final String BEAM_DEVICE_NAME = "beam";
	private static final String INSERTION_DEVICE_NAME = "insertion_device";
	private static final String MONOCHROMATOR_DEVICE_NAME = "monochromator";
	private static final String SOURCE_DEVICE_NAME = "source";
	private static final String USER_DEVICE_NAME = "user";
	private static final String SLIT3_SCANNABLE_NAME = "s3";
	private static final String SLIT4_SCANANBLE_NAME = "s4";
	private static final String SLIT1_DEVICE_NAME = "slit1";
	private static final String SLIT2_DEVICE_NAME = "slit2";
	private static final String SLIT3_DEVICE_NAME = "slit3";
	private static final String SLIT4_DEVICE_NAME = "slit4";

	private static final String MIRROR1_DEVICE_NAME = "mirror1";
	private static final String MIRROR2_DEVICE_NAME = "mirror2";
	private static final String[] SUBSTRATE_SCANNABLE_FIELD_NAMES = { "density", "thickness", "roughness" };
	private static final Double[] MIRROR1_SUBSTRATE_POSITION = { 345.67, 8.63, 43.32 };
	private static final Double[] MIRROR2_SUBSTRATE_POSITION = { 298.33, 14.95, 87.50 };

	private static final double EXPECTED_INSERTION_DEVICE_TAPER = 7.432;
	private static final int EXPECTED_INSERTION_DEVICE_HARMONIC = 3;

	private static final String EXPECTED_INSTRUMENT_NAME = "ES1";
	private static final String EXPECTED_BEAMLINE_NAME = "p66";
	private static final String EXPECTED_END_STATION_NAME = EXPECTED_INSTRUMENT_NAME;

	private static final double EXPECTED_MONOCHROMATOR_ENERGY_ERROR = 2.53;

	private static final double EXPECTED_BEAM_EXTENT = 0.1;
	private static final double EXPECTED_BEAM_INCIDENT_ENERGY = 350.0;
	private static final double EXPECTED_BEAM_INCIDENT_DIVERGENCE = 1.23;
	private static final double EXPECTED_BEAM_INCIDENT_POLARIZATION = 4.55;
	private static final double EXPECTED_BEAM_FLUX = 92.2;

	private static final double EXPECTED_SLIT1_X_GAP = 2.3;
	private static final double EXPECTED_SLIT1_Y_GAP = 5.1;
	private static final double EXPECTED_SLIT2_X_GAP = 43.2;
	private static final double EXPECTED_SLIT2_Y_GAP = 29.9;
	private static final double EXPECTED_SLIT3_X_GAP = 16.2;
	private static final double EXPECTED_SLIT3_Y_GAP = 2.11;
	private static final double EXPECTED_SLIT4_X_GAP = 1.58;
	private static final double EXPECTED_SLIT4_Y_GAP = 13.62;

	// Note: these fields are based on those used by Fajin for i06. These fields are not part of
	// the nexus base class definition for NXmirror: https://manual.nexusformat.org/classes/base_classes/NXmirror.html
	private static final String MIRROR_FIELD_NAME_X = "x";
	private static final String MIRROR_FIELD_NAME_PITCH = "pitch";
	private static final String MIRROR_FIELD_NAME_YAW = "yaw";

	private static final double MIRROR1_X = 0.0;
	private static final double MIRROR1_PITCH = 3.5;
	private static final double MIRROR1_YAW = 13.9;

	private static final double MIRROR2_X = 0.0;
	private static final double MIRROR2_PITCH = 3.5;
	private static final double MIRROR2_YAW = 13.9;

	private static final String EXPECTED_SCRIPT_NAME = "currentScript.py";

	@Parameters(name="scanRank = {0}")
	public static Object[] data() {
		return IntStream.rangeClosed(1, MAX_SCAN_RANK).mapToObj(Integer::valueOf).toArray();
	}

	public NexusScanDataWriterScanTest(int scanRank) {
		super(scanRank);
	}

	@BeforeClass
	public static void setUpProperties() {
		LocalProperties.set(GDA_DATA_SCAN_DATAWRITER_DATAFORMAT, PROPERTY_VALUE_DATA_FORMAT_NEXUS_SCAN);
		LocalProperties.set(PROPERTY_NAME_ENTRY_NAME, ENTRY_NAME);
		LocalProperties.set(GDA_INSTRUMENT, EXPECTED_BEAMLINE_NAME);
		LocalProperties.set(GDA_END_STATION_NAME, EXPECTED_END_STATION_NAME);
		LocalProperties.set(PROPERTY_VALUE_WRITE_DECIMALS, Boolean.TRUE.toString());
	}

	@AfterClass
	public static void tearDownProperties() {
		LocalProperties.clearProperty(GDA_DATA_SCAN_DATAWRITER_DATAFORMAT);
		LocalProperties.clearProperty(PROPERTY_NAME_ENTRY_NAME);
		LocalProperties.clearProperty(GDA_INSTRUMENT);
		LocalProperties.clearProperty(GDA_END_STATION_NAME);
		LocalProperties.clearProperty(PROPERTY_VALUE_WRITE_DECIMALS);
	}

	@BeforeClass
	public static void setUpServices() {
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

		deviceConfig.setAdditionalDeviceNames(Set.of(BEFORE_SCAN_COLLECTION_NAME,
				MIRROR1_DEVICE_NAME, MIRROR2_DEVICE_NAME,
				SLIT1_DEVICE_NAME, SLIT2_DEVICE_NAME, SLIT3_DEVICE_NAME, SLIT4_DEVICE_NAME));

		return deviceConfig;
	}

	@Override
	protected void setUpMetadata() throws Exception {
		super.setUpMetadata();

		LocalProperties.set(GDA_DATA_SCAN_DATAWRITER_DATAFORMAT, PROPERTY_VALUE_DATA_FORMAT_NEXUS_SCAN);
		LocalProperties.set(PROPERTY_NAME_ENTRY_NAME, ENTRY_NAME);

		final ClientDetails userDetails = new ClientDetails(0, EXPECTED_USER_ID, EXPECTED_USER_NAME, "ws001", 0, true, "visit1");
		final IBatonStateProvider batonStateProvider = mock(IBatonStateProvider.class);
		when(batonStateProvider.getBatonHolder()).thenReturn(userDetails);
		InterfaceProvider.setBatonStateProviderForTesting(batonStateProvider);
	}

	@Override
	protected void setUpTest(String testName) throws Exception {
		super.setUpTest(testName);
		setupCommonBeamlineDevices(); // must be done after super.setUpTest() to use jython namespace
		ServiceHolder.getNexusDeviceService().register(new BeforeScanSnapshotWriter());

		((MockJythonServerFacade) InterfaceProvider.getScriptController()).setScriptName(EXPECTED_SCRIPT_NAME);
	}

	@Override
	public void tearDown() { // inherits @After annotation from superclass
		super.tearDown();
		Finder.removeAllFactories();
	}

	private void setupCommonBeamlineDevices() throws DeviceException {
		createBeamDevice();
		createInsertionDevice();
		createMonochromatorDevice();
		createSourceDevice();
		createUserDevice();

		createSlitDevices();
		createMirrorDevices();

		// Test for DAQ-3883: check that nexus writing still works if there is a non-Scannable in the
		// jython namespace with the same name as an INexusDevice
		InterfaceProvider.getJythonNamespace().placeInJythonNamespace(MONOCHROMATOR_DEVICE_NAME, "foo");
		InterfaceProvider.getJythonNamespace().placeInJythonNamespace(SLIT1_DEVICE_NAME, "bar");
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

	private void createSlitDevices() throws DeviceException {
		createMultiScannableSlitDevice(SLIT1_DEVICE_NAME, EXPECTED_SLIT1_X_GAP, EXPECTED_SLIT1_Y_GAP);
		createMultiScannableSlitDevice(SLIT2_DEVICE_NAME, EXPECTED_SLIT2_X_GAP, EXPECTED_SLIT2_Y_GAP);
		createSingleScannableSlitDevice(SLIT3_DEVICE_NAME, SLIT3_SCANNABLE_NAME, EXPECTED_SLIT3_X_GAP, EXPECTED_SLIT3_Y_GAP);
		createSingleScannableSlitDevice(SLIT4_DEVICE_NAME, SLIT4_SCANANBLE_NAME, EXPECTED_SLIT4_X_GAP, EXPECTED_SLIT4_Y_GAP);
	}

	private void createMultiScannableSlitDevice(String name, double xGap, double yGap) throws DeviceException {
		final String xGapScannableName = name + NXslit.NX_X_GAP;
		final String yGapScannableName = name + NXslit.NX_Y_GAP;

		createScannable(xGapScannableName, xGap);
		createScannable(yGapScannableName, yGap);

		final NexusMetadataDevice<NXslit> slitDevice = new NexusMetadataDevice<>();
		slitDevice.setName(name);
		slitDevice.setNexusBaseClass(NexusBaseClass.NX_SLIT);
		slitDevice.setCategory(NexusBaseClass.NX_INSTRUMENT); // NXslit is not in the list of defined child groups for any other group

		final List<MetadataNode> fields = new ArrayList<>();
		fields.add(new ScannableField(NXslit.NX_X_GAP, xGapScannableName));
		fields.add(new ScannableField(NXslit.NX_Y_GAP, yGapScannableName));
		slitDevice.setCustomNodes(fields);

		ServiceHolder.getNexusDeviceService().register(slitDevice);
	}

	private void createSingleScannableSlitDevice(String nexusDeviceName, String scannableName,
			double xGap, double yGap) throws DeviceException {
		final DummyMultiFieldUnitsScannable<Length> scannable = new DummyMultiFieldUnitsScannable<>(scannableName, "mm");
		scannable.setInputNames(new String[] { "x", "y" });
		scannable.setExtraNames(new String[0]);
		scannable.setCurrentPosition(xGap, yGap);
		InterfaceProvider.getJythonNamespace().placeInJythonNamespace(scannableName, scannable);

		final NexusMetadataDevice<NXslit> slitDevice = new NexusMetadataDevice<>();
		slitDevice.setName(nexusDeviceName);
		slitDevice.setNexusBaseClass(NexusBaseClass.NX_SLIT);
		slitDevice.setCategory(NexusBaseClass.NX_INSTRUMENT); // NXslit is not in the list of defined child groups for any other group

		final List<MetadataNode> fields = new ArrayList<>();
		fields.add(new ScannableComponentField(NXslit.NX_X_GAP, scannableName, "x"));
		fields.add(new ScannableComponentField(NXslit.NX_Y_GAP, scannableName, "y"));
		slitDevice.setCustomNodes(fields);

		ServiceHolder.getNexusDeviceService().register(slitDevice);
	}

	private void createMirrorDevices() throws DeviceException {
		createMirrorDevice(MIRROR1_DEVICE_NAME, MIRROR1_X, MIRROR1_PITCH, MIRROR1_YAW, MIRROR1_SUBSTRATE_POSITION);
		createMirrorDevice(MIRROR2_DEVICE_NAME, MIRROR2_X, MIRROR2_PITCH, MIRROR2_YAW, MIRROR2_SUBSTRATE_POSITION);
	}

	private void createMirrorDevice(String name, double x, double pitch, double yaw,
			Double[] substratePosition) throws DeviceException {
		final String substrateScannableName = name + "_substrate";
		final DummyMultiFieldUnitsScannable<?> substrateScannable = new DummyMultiFieldUnitsScannable<>(substrateScannableName);
		substrateScannable.setInputNames(new String[0]);
		substrateScannable.setExtraNames(SUBSTRATE_SCANNABLE_FIELD_NAMES);
		substrateScannable.setCurrentPosition((Object[]) substratePosition);
		InterfaceProvider.getJythonNamespace().placeInJythonNamespace(substrateScannableName, substrateScannable);

		final NexusMetadataDevice<NXmirror> mirrorDevice = new NexusMetadataDevice<>();
		mirrorDevice.setName(name);
		mirrorDevice.setNexusBaseClass(NexusBaseClass.NX_MIRROR);

		final List<MetadataNode> fields = new ArrayList<>();
		fields.add(new ScalarField(MIRROR_FIELD_NAME_X, x));
		fields.add(new ScalarField(MIRROR_FIELD_NAME_PITCH, pitch));
		fields.add(new ScalarField(MIRROR_FIELD_NAME_YAW, yaw));
		fields.add(new ScannableComponentField(NXmirror.NX_SUBSTRATE_DENSITY, substrateScannableName, SUBSTRATE_SCANNABLE_FIELD_NAMES[0]));
		fields.add(new ScannableComponentField(NXmirror.NX_SUBSTRATE_THICKNESS, substrateScannableName, 1));
		fields.add(new ScannableComponentField(NXmirror.NX_SUBSTRATE_ROUGHNESS, substrateScannableName, SUBSTRATE_SCANNABLE_FIELD_NAMES[2]));
		mirrorDevice.setCustomNodes(fields);

		ServiceHolder.getNexusDeviceService().register(mirrorDevice);
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

		// check unique keys and scan timings have been written into the diamond scan NXcollection
		assertDiamondScanGroup(entry, false, false, scanDimensions);

		final Set<String> expectedLinkedFieldNames = Set.of(
				FIELD_NAME_SCAN_START_TIME, FIELD_NAME_SCAN_END_TIME, FIELD_NAME_SCAN_DURATION,
				FIELD_NAME_SCAN_SHAPE, FIELD_NAME_SCAN_COMMAND, FIELD_NAME_SCAN_FIELDS, FIELD_NAME_CURRENT_SCRIPT_NAME);

		final Set<String> otherDataNodeNames = Set.of(NXentry.NX_PROGRAM_NAME);
		final Set<String> allDataNodeNames = Streams.concat(
				expectedLinkedFieldNames.stream(), otherDataNodeNames.stream()).collect(toSet());
		assertThat(entry.getDataNodeNames(), containsInAnyOrder(allDataNodeNames.toArray()));

		final NXcollection diamondScanGroup = entry.getCollection(GROUP_NAME_DIAMOND_SCAN);
		for (String dataNodeName : expectedLinkedFieldNames) {
			assertThat(entry.getDataNode(dataNodeName), is(sameInstance(diamondScanGroup.getDataNode(dataNodeName))));
		}

		assertThat(entry.getDataset(FIELD_NAME_SCAN_COMMAND).getString(), is(equalTo(getExpectedScanCommand())));
		assertThat(entry.getDataset(FIELD_NAME_SCAN_SHAPE), is(equalTo(DatasetFactory.createFromObject(scanDimensions))));
		assertThat(entry.getDataset(FIELD_NAME_SCAN_FIELDS), is(equalTo(DatasetFactory.createFromObject(getExpectedScanFieldNames()))));
		assertThat(entry.getDataset(FIELD_NAME_CURRENT_SCRIPT_NAME).getString(), is(equalTo(EXPECTED_SCRIPT_NAME)));

		// TODO: what further metadata should be added to the nexus file (DAQ-3151)
		// (fields below are added by NexusDataWriter get metadata into nexus file but not yet NexusScanDataWriter)
//		assertThat(entry.getEntry_identifierScalar(), is(equalTo(EXPECTED_ENTRY_IDENTIFER))); // not set
//		assertThat(entry.getProgram_nameScalar(), is(equalTo(EXPECTED_PROGRAM_NAME)));
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
	protected void checkInstrumentGroupMetadata(final NXinstrument instrument) throws Exception {
		assertThat(instrument.getDataNodeNames(), containsInAnyOrder(NXinstrument.NX_NAME,
				NexusScanDataWriter.FIELD_NAME_BEAMLINE, NexusScanDataWriter.FIELD_NAME_END_STATION));
		assertThat(instrument.getNameScalar(), is(equalTo(EXPECTED_INSTRUMENT_NAME)));
		assertThat(instrument.getString(FIELD_NAME_BEAMLINE), is(equalTo(EXPECTED_BEAMLINE_NAME)));
		assertThat(instrument.getString(FIELD_NAME_END_STATION), is(equalTo(EXPECTED_END_STATION_NAME)));

		// group for each device, each common (metadata) device (e.g. NXMonochromator), plus source group scannables:NXcollection (for scannables in the locationMap)
 		assertThat(instrument.getGroupNodeNames(), containsInAnyOrder(getExpectedInstrumentGroupNames()));
 		checkBeforeScanCollection(instrument.getCollection(BEFORE_SCAN_COLLECTION_NAME));
	}

	private String[] getExpectedInstrumentGroupNames() {
		final Set<String> expectedGroupNames = new HashSet<>(List.of(getScannableAndMonitorNames()));
		if (detector != null) {
			expectedGroupNames.add(detector.getName());
		}

		expectedGroupNames.add(GROUP_NAME_SCANNABLES);
		expectedGroupNames.addAll(getExpectedMetadataScannableNames());
		expectedGroupNames.addAll(ServiceHolder.getCommonBeamlineDevicesConfiguration().getCommonDeviceNames());
		expectedGroupNames.removeAll(List.of(USER_DEVICE_NAME, BEAM_DEVICE_NAME)); // added directly to NXentry

		return expectedGroupNames.toArray(new String[expectedGroupNames.size()]);
	}

	protected List<String> getExpectedScanFieldNames() {
		final List<String> scanFields = new ArrayList<>();
		for (Scannable scannable : scannables) {
			scanFields.addAll(Arrays.asList(scannable.getInputNames()));
			scanFields.addAll(Arrays.asList(scannable.getExtraNames()));
		}
		if (monitor != null) {
			scanFields.add(monitor.getName());
		}
		if (detector != null) {
			if (ArrayUtils.isNotEmpty(detector.getExtraNames())) {
				scanFields.addAll(Arrays.asList(detector.getExtraNames()));
			} else {
				scanFields.add(detector.getName());
			}
		}
		return scanFields;
	}

	private void checkBeforeScanCollection(NXcollection beforeScanCollection) throws Exception {
		final Set<String> scannableNames = Set.of(getScannableAndMonitorNames());

		final List<String> allScannableNames = Stream.concat(scannableNames.stream(), getExpectedMetadataScannableNames().stream()).collect(toList());
		assertThat(beforeScanCollection.getGroupNodeNames(), containsInAnyOrder(allScannableNames.toArray()));
		for (String scannableName : allScannableNames) {
			final GroupNode scannableGroup = beforeScanCollection.getGroupNode(scannableName);
			assertThat("no collection found for scannable " + scannableName + " in before scan collection", scannableGroup, is(notNullValue()));
			assertThat(scannableGroup, is(instanceOf(NXcollection.class)));
			final NXcollection scannableCollection = (NXcollection) scannableGroup;

			final Scannable scannable = (Scannable) InterfaceProvider.getJythonNamespace().getFromJythonNamespace(scannableName);
			final String[] allFieldNames = ArrayUtils.addAll(scannable.getInputNames(), scannable.getExtraNames());
			assertThat(scannableCollection.getDataNodeNames(), containsInAnyOrder(allFieldNames));

			final Object position = scannable.getPosition();
			final String expectedUnits = scannable instanceof ScannableMotionUnits ? ((ScannableMotionUnits) scannable).getUserUnits() : null;
			for (int fieldIndex = 0; fieldIndex < allFieldNames.length; fieldIndex++) {
				final DataNode dataNode = scannableCollection.getDataNode(allFieldNames[fieldIndex]);
				assertThat(dataNode, is(notNullValue()));
				final IDataset dataset = dataNode.getDataset().getSlice();
				final double fieldPosition = getPositionForFieldIndex(position, fieldIndex);
				assertThat(dataset.getShape(), is(equalTo(EMPTY_SHAPE)));

				if (!scannableNames.contains(scannableName)) {
					// note: in fact before scan is written after the first point, as this is where DataWriters create the nexus tree
					assertThat(dataset.getDouble(), is(closeTo(fieldPosition, 1e-15)));
				}
				final Attribute unitsAttr = dataNode.getAttribute(ATTRIBUTE_NAME_UNITS);
				if (expectedUnits == null) {
					assertThat(unitsAttr, is(nullValue()));
				} else {
					assertThat(unitsAttr, is(notNullValue()));
					assertThat(unitsAttr.getFirstElement(), is(equalTo(expectedUnits)));
				}
			}
		}
	}

	private double getPositionForFieldIndex(Object position, int fieldIndex) throws DeviceException {
		if (!position.getClass().isArray()) {
			if (fieldIndex != 0) throw new IllegalArgumentException("field index must be 0 for a scalar position, was: " + fieldIndex);
			return (Double) position;
		}

		return ((Double[]) position)[fieldIndex];
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
	protected String[] getExpectedPositionerNames() {
		return Streams.concat(Arrays.stream(getScannableNames()),
				getExpectedMetadataScannableNames().stream())
				.toArray(String[]::new);
	}

	@Override
	protected void checkDefaultScannablePositioner(NXpositioner scannablePos, int scanIndex) throws DatasetException {
		// This is the NXpositioner created by ScannableNexusDevice
		final String[] expectedDataNodeNames = { NXpositioner.NX_NAME, NXpositioner.NX_VALUE,
				NXpositioner.NX_SOFT_LIMIT_MAX, NXpositioner.NX_SOFT_LIMIT_MIN, NXpositioner.NX_CONTROLLER_RECORD };
		assertThat(scannablePos.getDataNodeNames(), containsInAnyOrder(expectedDataNodeNames));

		final String scannableName = scannables[scanIndex].getName();
		final DataNode scannableValueDataNode = scannablePos.getDataNode(NXpositioner.NX_VALUE);
		assertThat(scannableValueDataNode, is(notNullValue()));

		// check attributes
		final String[] expectedAttributeNames = { ATTRIBUTE_NAME_UNITS, ATTRIBUTE_NAME_GDA_FIELD_NAME,
				ATTRIBUTE_NAME_LOCAL_NAME, ATTRIBUTE_NAME_TARGET, ATTRIBUTE_NAME_DECIMALS };
		assertThat(scannableValueDataNode.getAttributeNames(), containsInAnyOrder(expectedAttributeNames));

		assertThat(scannableValueDataNode.getAttribute(ATTRIBUTE_NAME_UNITS).getFirstElement(), is(equalTo("mm")));
		assertThat(scannableValueDataNode.getAttribute(ATTRIBUTE_NAME_GDA_FIELD_NAME).getFirstElement(), is(equalTo(scannableName)));
		assertThat(scannableValueDataNode.getAttribute(ATTRIBUTE_NAME_LOCAL_NAME).getFirstElement(),
				is(equalTo(scannableName + NexusConstants.FIELD_SEPERATOR + scannableName)));
		final String expectedTargetPath = "/" + ENTRY_NAME + "/" + INSTRUMENT_NAME + "/" +
				(scanIndex == 0 ? GROUP_NAME_SCANNABLES + "/" : "" ) + scannableName + "/" + NXpositioner.NX_VALUE;
		assertThat(scannableValueDataNode.getAttribute(ATTRIBUTE_NAME_TARGET).getFirstElement(), is(equalTo(expectedTargetPath)));
		assertThat(scannableValueDataNode.getAttribute(ATTRIBUTE_NAME_DECIMALS).getValue().getInt(), is(scanIndex % 3 + 1));

		// check dataset
		assertThat(scannableValueDataNode.getDataset().getSlice(),
				is(equalTo(DatasetFactory.createFromObject(getExpectedScannableDataset(scanIndex))))); // check values

		// check upper/lower bounds
		assertThat(scannablePos.getSoft_limit_minScalar(), is(equalTo(SCANNABLE_LOWER_BOUND)));
		assertThat(scannablePos.getSoft_limit_maxScalar(), is(equalTo(SCANNABLE_UPPER_BOUND)));
		assertThat(scannablePos.getController_recordScalar(), is(equalTo(SCANNABLE_PV_NAME_PREFIX + scanIndex)));
	}

	protected void checkDefaultMetadataScannablePositioner(NXpositioner positioner, int index) throws DatasetException {
		final String[] expectedDataNodeNames = { NXpositioner.NX_NAME, NXpositioner.NX_VALUE,
				NXpositioner.NX_SOFT_LIMIT_MAX, NXpositioner.NX_SOFT_LIMIT_MIN, NXpositioner.NX_CONTROLLER_RECORD };
		assertThat(positioner.getDataNodeNames(), containsInAnyOrder(expectedDataNodeNames));

		final String scannableName = METADATA_SCANNABLE_NAMES[index];
		assertThat(positioner.getNameScalar(), is(equalTo(scannableName)));
		assertThat(positioner.getController_recordScalar(), is(equalTo(META_SCANNABLE_PV_NAME_PREFIX + index)));

		final DataNode valueDataNode = positioner.getDataNode(NXpositioner.NX_VALUE);
		checkMetadataScannableValueDataNode(index, scannableName, valueDataNode);
	}

	@Override
	protected void checkConfiguredScannablePositioner(final String scannableName, NXpositioner scannablePos) throws DatasetException {
		assertThat(scannablePos.getDataNodeNames(), contains(scannableName));

		final DataNode scannableValueDataNode = scannablePos.getDataNode(scannableName);
		assertThat(scannableValueDataNode, is(notNullValue()));

		final String[] expectedAttributeNames = new String[] { ATTRIBUTE_NAME_LOCAL_NAME,
				ATTRIBUTE_NAME_TARGET, ATTRIBUTE_NAME_GDA_FIELD_NAME, ATTRIBUTE_NAME_UNITS, ATTRIBUTE_NAME_DECIMALS };
		assertThat(scannableValueDataNode.getAttributeNames(), containsInAnyOrder(expectedAttributeNames));

		assertThat(scannableValueDataNode.getAttribute(ATTRIBUTE_NAME_LOCAL_NAME).getFirstElement(),
				is(equalTo(scannableName + NexusConstants.FIELD_SEPERATOR + scannableName)));
		assertThat(scannableValueDataNode.getAttribute(ATTRIBUTE_NAME_TARGET).getFirstElement(), is(equalTo(
				"/" + ENTRY_NAME + "/" + INSTRUMENT_NAME + "/" + GROUP_NAME_SCANNABLES + "/" + scannableName + "/" + NXpositioner.NX_VALUE)));
		assertThat(scannableValueDataNode.getAttribute(ATTRIBUTE_NAME_GDA_FIELD_NAME).getFirstElement(), is(equalTo(scannableName)));
		assertThat(scannableValueDataNode.getAttribute(ATTRIBUTE_NAME_UNITS).getFirstElement(), is(equalTo("mm")));
		assertThat(scannableValueDataNode.getAttribute(ATTRIBUTE_NAME_DECIMALS).getValue().getInt(), is(1));
		assertThat(scannableValueDataNode.getDataset().getSlice(),
				is(equalTo(DatasetFactory.createFromObject(getExpectedScannableDataset(0))))); // check values
	}

	@Override
	protected void checkMonitor(NXinstrument instrument) throws Exception {
		// check the monitor has been written correctly
		final NXcollection monitorCollection = instrument.getCollection(MONITOR_NAME);
		if (monitor == null) {
			assertThat(monitorCollection, is(nullValue()));
			return;
		}

		assertThat(monitorCollection, is(notNullValue()));

		final String[] expectedDataNodeNames = { NXpositioner.NX_NAME, MONITOR_NAME };
		assertThat(monitorCollection.getDataNodeNames(), containsInAnyOrder(expectedDataNodeNames));

		assertThat(monitorCollection.getString(FIELD_NAME_NAME), is(equalTo(MONITOR_NAME)));

		final DataNode monitorValueDataNode = monitorCollection.getDataNode(MONITOR_NAME);
		assertThat(monitorValueDataNode, is(notNullValue()));

		final String[] expectedAttributeNames = { ATTRIBUTE_NAME_GDA_FIELD_NAME,
				ATTRIBUTE_NAME_LOCAL_NAME, ATTRIBUTE_NAME_TARGET, ATTRIBUTE_NAME_DECIMALS };
		assertThat(monitorValueDataNode.getAttributeNames(), containsInAnyOrder(expectedAttributeNames));

		assertThat(monitorValueDataNode.getAttribute(ATTRIBUTE_NAME_GDA_FIELD_NAME).getFirstElement(), is(equalTo(MONITOR_NAME)));
		assertThat(monitorValueDataNode.getAttribute(ATTRIBUTE_NAME_LOCAL_NAME).getFirstElement(),
				is(equalTo(MONITOR_NAME + NexusConstants.FIELD_SEPERATOR + MONITOR_NAME)));
		assertThat(monitorValueDataNode.getAttribute(ATTRIBUTE_NAME_TARGET).getFirstElement(),
				is(equalTo("/" + ENTRY_NAME + "/" + INSTRUMENT_NAME + "/" + MONITOR_NAME + "/" + MONITOR_NAME)));
		assertThat(monitorValueDataNode.getDataset().getSlice(), is(equalTo(DatasetFactory.zeros(scanDimensions).fill(MONITOR_VALUE)))); // check values
		assertThat(monitorValueDataNode.getAttribute(ATTRIBUTE_NAME_DECIMALS).getValue().getInt(), is(5));
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
				ATTRIBUTE_NAME_LOCAL_NAME, ATTRIBUTE_NAME_TARGET,
				ATTRIBUTE_NAME_UNITS, ATTRIBUTE_NAME_DECIMALS };
		assertThat(valueDataNode.getAttributeNames(), containsInAnyOrder(expectedAttributeNames));
		assertThat(valueDataNode.getNumberOfAttributes(), is(expectedAttributeNames.length));

		assertThat(valueDataNode.getAttribute(ATTRIBUTE_NAME_GDA_FIELD_NAME).getFirstElement(), is(equalTo(scannableName)));
		assertThat(valueDataNode.getAttribute(ATTRIBUTE_NAME_LOCAL_NAME).getFirstElement(),
				is(equalTo(scannableName + NexusConstants.FIELD_SEPERATOR + scannableName)));
		assertThat(valueDataNode.getAttribute(ATTRIBUTE_NAME_TARGET).getFirstElement(),
				is(equalTo("/" + ENTRY_NAME + "/" + INSTRUMENT_NAME + "/" + GROUP_NAME_SCANNABLES + "/" + scannableName + "/" + NXpositioner.NX_VALUE)));
		assertThat(valueDataNode.getAttribute(ATTRIBUTE_NAME_UNITS).getFirstElement(), is(equalTo("mm")));
		final int expectedDecimals = index % 2 == 0 ? 3 : 5;
		assertThat(valueDataNode.getAttribute(ATTRIBUTE_NAME_DECIMALS).getValue().getInt(), is(expectedDecimals));

		assertThat(valueDataNode.getDataset().getShape(), is(equalTo(EMPTY_SHAPE)));
		assertThat(valueDataNode.getDataset().getSlice().getDouble(), is(equalTo((double) index)));
	}

	@Override
	protected void checkDataGroups(NXentry entry) {
		final Map<String, NXdata> dataGroups = entry.getAllData();

		final String dataDeviceName = getDataDeviceName();
		final List<String> primaryFieldNames = getPrimaryFieldNames();
		final List<String> expectedDataGroupNames = getExpectedDataGroupNames(dataDeviceName, primaryFieldNames);

		assertThat(dataGroups.keySet(), containsInAnyOrder(expectedDataGroupNames.toArray()));

		for (int i = 0; i < primaryFieldNames.size(); i++) {
			final String groupName = expectedDataGroupNames.get(i);
			final NXdata dataGroup = dataGroups.get(groupName);
			final String primaryFieldName = primaryFieldNames.get(i);
			final String signalFieldName = getSignalFieldName(primaryFieldName);
			checkDataGroup(entry, dataGroup, signalFieldName, dataDeviceName, primaryFieldNames.get(i));
		}
	}

	private boolean isDetectorPrimaryDevice() {
		return detector != null && detectorType != DetectorType.FILE_CREATOR;
	}

	private String getDataDeviceName() {
		if (isDetectorPrimaryDevice()) {
			return detector.getName();
		}

		// An NXdata group is created for the monitor if the scan has no detectors, or the first scannable if there is also no monitor
		return monitor != null ? monitor.getName() : scannables[0].getName();
	}

	private List<String> getPrimaryFieldNames() {
		// return the primary fields of the primary device
		if (isDetectorPrimaryDevice()) return detectorType.getPrimaryFieldNames();
		if (monitor != null) return List.of(MONITOR_NAME);
		return List.of(NXpositioner.NX_VALUE);
	}

	private String getSignalFieldName(String primaryFieldName) {
		if (isDetectorPrimaryDevice()) return primaryFieldName;
		if (monitor != null) return MONITOR_NAME;
		return NXpositioner.NX_VALUE;
	}

	private void checkDataGroup(NXentry entry, final NXdata data, final String signalFieldName,
			final String dataDeviceName, String primaryFieldName) {
		assertThat(data, is(notNullValue()));

		final Map<String, String> expectedDataNodeLinks = new LinkedHashMap<>();
		final Set<String> expectedAttributeNames = new LinkedHashSet<>();

		// check that the value fields of the monitor and scannables have been linked to
		expectedDataNodeLinks.putAll(Arrays.stream(scannables).map(Scannable::getName).collect(
				toMap(Function.identity(), scannableName -> String.format("instrument/%s/%s", scannableName,
						scannableName.equals(scannables[0].getName()) ? scannableName : NXpositioner.NX_VALUE))));
		expectedAttributeNames.addAll(Arrays.stream(scannables)
				.map(Scannable::getName).map(name -> name + "_indices").collect(toSet()));

		// check that the signal field points to the value of the detector (or monitor if no detector, and the first scannable if no monitor)
		expectedDataNodeLinks.put(signalFieldName, String.format("instrument/%s/%s", dataDeviceName,
				dataDeviceName.equals(scannables[0].getName()) ? dataDeviceName : primaryFieldName));

		if (monitor != null && isDetectorPrimaryDevice()) {
			// monitor value already added as the signal field if monitor is primary device
			expectedDataNodeLinks.put(MONITOR_NAME, String.format("instrument/%s/%s", MONITOR_NAME, MONITOR_NAME));
			expectedAttributeNames.add(MONITOR_NAME + "_indices");
		}

		// assert that all the expected linked data nodes are present
		checkLinkedDatasets(data, entry, expectedDataNodeLinks);

		// check that the attributes have been added according to the 2014 NXdata format
		// attributes created for each scannable, monitor (if signal field not from monitor), signal, axes, NXclass
		expectedAttributeNames.addAll(List.of(NXdata.NX_ATTRIBUTE_SIGNAL, NXdata.NX_ATTRIBUTE_AXES, NexusConstants.NXCLASS));

		assertThat(data.getAttributeNames(), containsInAnyOrder(expectedAttributeNames.toArray()));
		assertSignal(data, signalFieldName);
		assertAxes(data, Stream.concat(Arrays.stream(scannables).map(Scannable::getName),
				Collections.nCopies(data.getDataNode(signalFieldName).getRank() - scanRank, NexusConstants.FIELD_SEPERATOR).stream())
						.toArray(String[]::new));

		// check that each field has the expected indices
		final int[] expectedIndices = IntStream.range(0, scanRank).toArray();
		Arrays.stream(scannables).map(Scannable::getName).forEach(
				scannableName -> assertIndices(data, scannableName, expectedIndices));
		if (isDetectorPrimaryDevice()) {
			assertIndices(data, MONITOR_NAME, expectedIndices);
		}
	}

	private List<String> getExpectedDataGroupNames(String dataDeviceName, List<String> primaryFieldNames) {
		if (detectorType == DetectorType.NEXUS_DETECTOR || detectorType == DetectorType.MODIFIED_NEXUS_DETECTOR) {
			return Streams.concat(Stream.of(dataDeviceName),
					primaryFieldNames.stream().skip(1).map(name -> dataDeviceName + NexusConstants.FIELD_SEPERATOR + name)).collect(Collectors.toList());
		}

		return List.of(dataDeviceName);
	}

	@Override
	protected void checkSourceGroup(NXinstrument instrument) {
		final NXsource source = instrument.getSource();
		assertThat(source, is(notNullValue()));

		assertThat(source.getDataNodeNames(), containsInAnyOrder(NXsource.NX_NAME,
				NXsource.NX_PROBE, NXsource.NX_TYPE, NXsource.NX_CURRENT));
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

		assertThat(beam.getDataNodeNames(), containsInAnyOrder(NXbeam.NX_EXTENT, NXbeam.NX_DISTANCE,
				NXbeam.NX_INCIDENT_ENERGY, NXbeam.NX_INCIDENT_BEAM_DIVERGENCE, NXbeam.NX_INCIDENT_POLARIZATION,
				NXbeam.NX_FLUX));
		assertThat(beam.getDistanceScalar(), is(equalTo(0.0)));
		assertThat(beam.getDouble(NXbeam.NX_EXTENT), is(equalTo(EXPECTED_BEAM_EXTENT)));
		assertThat(beam.getIncident_energyScalar(), is(equalTo(EXPECTED_BEAM_INCIDENT_ENERGY)));
		assertThat(beam.getIncident_beam_divergenceScalar(), is(equalTo(EXPECTED_BEAM_INCIDENT_DIVERGENCE)));
		assertThat(beam.getIncident_polarizationScalar(), is(equalTo(EXPECTED_BEAM_INCIDENT_POLARIZATION)));
		assertThat(beam.getFluxScalar(), is(equalTo(EXPECTED_BEAM_FLUX)));
	}

	@Override
	protected void checkMetadataDeviceGroups(NXinstrument instrument) throws Exception {
		super.checkMetadataDeviceGroups(instrument);

		checkSlitGroup(instrument, SLIT1_DEVICE_NAME, EXPECTED_SLIT1_X_GAP, EXPECTED_SLIT1_Y_GAP);
		checkSlitGroup(instrument, SLIT2_DEVICE_NAME, EXPECTED_SLIT2_X_GAP, EXPECTED_SLIT2_Y_GAP);
		checkSlitGroup(instrument, SLIT3_DEVICE_NAME, EXPECTED_SLIT3_X_GAP, EXPECTED_SLIT3_Y_GAP);
		checkSlitGroup(instrument, SLIT4_DEVICE_NAME, EXPECTED_SLIT4_X_GAP, EXPECTED_SLIT4_Y_GAP);

		checkMirrorGroup(instrument, MIRROR1_DEVICE_NAME, MIRROR1_X, MIRROR1_PITCH, MIRROR1_YAW, MIRROR1_SUBSTRATE_POSITION);
		checkMirrorGroup(instrument, MIRROR2_DEVICE_NAME, MIRROR2_X, MIRROR1_PITCH, MIRROR1_YAW, MIRROR2_SUBSTRATE_POSITION);
	}

	@Override
	protected void checkInsertionDeviceGroup(NXinstrument instrument) {
		final NXinsertion_device insertionDevice = instrument.getInsertion_device();
		assertThat(insertionDevice, is(notNullValue()));

		assertThat(insertionDevice.getDataNodeNames(), containsInAnyOrder(NXinsertion_device.NX_TYPE,
				NXinsertion_device.NX_GAP, NXinsertion_device.NX_TAPER, NXinsertion_device.NX_HARMONIC));
		assertThat(insertionDevice.getTypeScalar(), is(equalTo(InsertionDeviceType.WIGGLER.toString())));
		assertThat(insertionDevice.getGapScalar(), is(equalTo(EXPECTED_INSERTION_DEVICE_GAP)));
		assertThat(insertionDevice.getTaperScalar(), is(equalTo(EXPECTED_INSERTION_DEVICE_TAPER)));
		// DummyScannable always returns a double which gets converted to a long
		assertThat(insertionDevice.getHarmonicScalar(), is(equalTo((long) EXPECTED_INSERTION_DEVICE_HARMONIC)));
	}

	@Override
	protected void checkMonochromatorGroup(NXinstrument instrument) {
		final NXmonochromator monochromator = instrument.getMonochromator();
		assertThat(monochromator, is(notNullValue()));
		assertThat(monochromator.getDataNodeNames(), containsInAnyOrder(
				NXmonochromator.NX_ENERGY, NXmonochromator.NX_ENERGY_ERROR));
		assertThat(monochromator.getEnergyScalar(), is(equalTo(EXPECTED_MONOCHROMATOR_ENERGY)));
		assertThat(monochromator.getEnergy_errorScalar(), is(equalTo(EXPECTED_MONOCHROMATOR_ENERGY_ERROR)));
	}

	private void checkMirrorGroup(NXinstrument instrument, String mirrorName,
			double expectedX, double expectedPitch, double expectedYaw, Double[] expectedDoublePosition) {
		final NXmirror mirror = instrument.getMirror(mirrorName);
		assertThat(mirror, is(notNullValue()));
		assertThat(mirror.getDataNodeNames(), containsInAnyOrder(MIRROR_FIELD_NAME_X,
				MIRROR_FIELD_NAME_PITCH, MIRROR_FIELD_NAME_YAW,
				NXmirror.NX_SUBSTRATE_DENSITY, NXmirror.NX_SUBSTRATE_THICKNESS, NXmirror.NX_SUBSTRATE_ROUGHNESS));
		assertThat(mirror.getDouble(MIRROR_FIELD_NAME_X), is(equalTo(expectedX)));
		assertThat(mirror.getDouble(MIRROR_FIELD_NAME_PITCH), is(equalTo(expectedPitch)));
		assertThat(mirror.getDouble(MIRROR_FIELD_NAME_YAW), is(equalTo(expectedYaw)));
		assertThat(mirror.getDouble(NXmirror.NX_SUBSTRATE_DENSITY), is(closeTo(expectedDoublePosition[0], 1e-15)));
		assertThat(mirror.getDouble(NXmirror.NX_SUBSTRATE_THICKNESS), is(closeTo(expectedDoublePosition[1], 1e-15)));
		assertThat(mirror.getDouble(NXmirror.NX_SUBSTRATE_ROUGHNESS), is(closeTo(expectedDoublePosition[2], 1e-15)));
	}

	private void checkSlitGroup(NXinstrument instrument, String slitName,
			double expectedXGap, double expectedYGap) {
		final NXslit slitGroup = (NXslit) instrument.getGroupNode(slitName);
		assertThat(slitGroup, is(notNullValue()));

		assertThat(slitGroup.getDataNodeNames(), containsInAnyOrder(NXslit.NX_X_GAP, NXslit.NX_Y_GAP));
		assertThat(slitGroup.getX_gapScalar(), is(equalTo(expectedXGap)));
		assertThat(slitGroup.getY_gapScalar(), is(equalTo(expectedYGap)));
	}

}
