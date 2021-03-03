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

package org.eclipse.scanning.test.scan.nexus;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.dawnsci.nexus.NXbeam;
import org.eclipse.dawnsci.nexus.NXentry;
import org.eclipse.dawnsci.nexus.NXinsertion_device;
import org.eclipse.dawnsci.nexus.NXinstrument;
import org.eclipse.dawnsci.nexus.NXmonochromator;
import org.eclipse.dawnsci.nexus.NXsample;
import org.eclipse.dawnsci.nexus.NXsource;
import org.eclipse.dawnsci.nexus.NXuser;
import org.eclipse.scanning.api.IScannable;
import org.eclipse.scanning.api.device.IRunnableDevice;
import org.eclipse.scanning.api.device.IWritableDetector;
import org.eclipse.scanning.api.scan.models.ScanModel;
import org.eclipse.scanning.device.BeamNexusDevice;
import org.eclipse.scanning.device.CommonBeamlineDevicesConfiguration;
import org.eclipse.scanning.device.InsertionDeviceNexusDevice;
import org.eclipse.scanning.device.InsertionDeviceNexusDevice.InsertionDeviceType;
import org.eclipse.scanning.device.MetadataField;
import org.eclipse.scanning.device.MonochromatorNexusDevice;
import org.eclipse.scanning.device.ScalarField;
import org.eclipse.scanning.device.ScannableField;
import org.eclipse.scanning.device.SourceNexusDevice;
import org.eclipse.scanning.device.UserNexusDevice;
import org.eclipse.scanning.example.detector.MandelbrotModel;
import org.eclipse.scanning.example.scannable.MockScannable;
import org.eclipse.scanning.sequencer.ServiceHolder;
import org.eclipse.scanning.test.util.TestDetectorHelpers;
import org.junit.Before;
import org.junit.Test;

import gda.device.DeviceException;
import gda.jython.IBatonStateProvider;
import gda.jython.InterfaceProvider;
import gda.jython.batoncontrol.ClientDetails;

public class DiamondDefaultNexusStructureTest extends NexusTest {

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

	private static final String EXPECTED_MONOCHROMATOR_NAME = "myMonochromator";
	private static final double EXPECTED_MONOCHROMATOR_ENERGY = 5.432;
	private static final double EXPECTED_MONOCHROMATOR_WAVELENGTH = 543.34;
	private static final double EXPECTED_INSERTION_DEVICE_GAP = 1.234;
	private static final double EXPECTED_INSERTION_DEVICE_BANDWIDTH = 83.34;
	private static final double EXPECTED_INSERTION_DEVICE_LENGTH = 3.5;
	private static final double EXPECTED_SOURCE_ENERGY = 3.0;
	private static final double EXPECTED_SOURCE_CURRENT = 25.5;

	private static final String EXPECTED_ENTRY_IDENTIFER = "1";
	private static final String EXPECTED_PROGRAM_NAME = "GDA 7.11.0";
	private static final String EXPECTED_USER_GROUP_NAME = "user01";
	private static final String EXPECTED_INSTRUMENT_NAME = "i99";
	private static final String EXPECTED_USER_ID = "abc12345";
	private static final String EXPECTED_USER_NAME = "Ted Jones";

	private static final int[] SHAPE = { 5, 2 };

	private IWritableDetector<MandelbrotModel> detector;

	@Before
	public void setUp() throws Exception {
		MandelbrotModel model = createMandelbrotModel();
		detector = TestDetectorHelpers.createAndConfigureMandelbrotDetector(model);
		assertNotNull(detector);
		setUpCommonBeamlineDevices();

		setUpMetadata();
	}

	private void setUpCommonBeamlineDevices() throws DeviceException {
		createBeamDevice();
		createInsertionDevice();
		createMonochromatorDevice();
		createSourceDevice();
		createUserDevice();

		new ServiceHolder().setCommonBeamlineDevicesConfiguration(createCommonBeamlineDevicesConfiguration());
	}

	private CommonBeamlineDevicesConfiguration createCommonBeamlineDevicesConfiguration() {
		final CommonBeamlineDevicesConfiguration deviceConfig = new CommonBeamlineDevicesConfiguration();
		deviceConfig.setBeamName(BEAM_DEVICE_NAME);
		deviceConfig.setInsertionDeviceName(INSERTION_DEVICE_NAME);
		deviceConfig.setMonochromatorName(MONOCHROMATOR_DEVICE_NAME);
		deviceConfig.setSourceName(SOURCE_DEVICE_NAME);
		deviceConfig.setUserDeviceName(USER_DEVICE_NAME);

		return deviceConfig;
	}

	private void createBeamDevice() {
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

	private void createInsertionDevice() {
		final String gapScannableName = "id_gap";
		final String taperScannableName = "id_taper";
		final String harmonicScannableName = "id_harmonic";
		final String bandwidthScannableName = "id_bandwidth";

		createScannable(gapScannableName, EXPECTED_INSERTION_DEVICE_GAP);
		createScannable(taperScannableName, EXPECTED_INSERTION_DEVICE_TAPER);
		createScannable(harmonicScannableName, EXPECTED_INSERTION_DEVICE_HARMONIC);
		createScannable(bandwidthScannableName, EXPECTED_INSERTION_DEVICE_BANDWIDTH);

		final InsertionDeviceNexusDevice insertionDevice = new InsertionDeviceNexusDevice();
		insertionDevice.setName(INSERTION_DEVICE_NAME);
		insertionDevice.setType(InsertionDeviceType.WIGGLER.toString());
		insertionDevice.setGapScannableName(gapScannableName);
		insertionDevice.setTaperScannableName(taperScannableName);
		insertionDevice.setHarmonicScannableName(harmonicScannableName);

		final List<MetadataField> customFields = new ArrayList<>();
		customFields.add(new ScannableField(NXinsertion_device.NX_BANDWIDTH, bandwidthScannableName));
		customFields.add(new ScalarField(NXinsertion_device.NX_LENGTH, EXPECTED_INSERTION_DEVICE_LENGTH));
		insertionDevice.setCustomFields(customFields);

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

	private void createSourceDevice() {
		final String sourceCurrentScannableName = "source_current";
		createScannable(sourceCurrentScannableName, EXPECTED_SOURCE_CURRENT);

		final SourceNexusDevice source = new SourceNexusDevice();
		source.setName(SOURCE_DEVICE_NAME);
		source.setSourceName("Diamond Light Source");
		source.setCurrentScannableName(sourceCurrentScannableName);
		ServiceHolder.getNexusDeviceService().register(source);
	}

	private void createUserDevice() {
		final UserNexusDevice userDevice = new UserNexusDevice();
		userDevice.setName(USER_DEVICE_NAME);
		ServiceHolder.getNexusDeviceService().register(userDevice);
	}

	private void createScannable(final String name, double value) {
		final IScannable<?> scannable = new MockScannable(name, value);
		connector.register(scannable);
	}

	private void setUpMetadata() throws Exception {
		final ClientDetails userDetails = new ClientDetails(0, EXPECTED_USER_ID, EXPECTED_USER_NAME, "ws001", 0, true, "visit1");
		final IBatonStateProvider batonStateProvider = mock(IBatonStateProvider.class);
		when(batonStateProvider.getBatonHolder()).thenReturn(userDetails);
		InterfaceProvider.setBatonStateProviderForTesting(batonStateProvider);
	}

	@Test
	public void testSimpleScan() throws Exception {
		final IRunnableDevice<ScanModel> scanner = createGridScan(detector, output, false, SHAPE);
		scanner.run(null);

		checkNexusFile(scanner);
	}

	private void checkNexusFile(final IRunnableDevice<ScanModel> scanner) throws Exception {
		checkNexusFile(scanner, false, SHAPE);

		final NXentry entry = getNexusRoot(scanner).getEntry();
		checkSampleGroup(entry);
		checkUsers(entry);

		final NXinstrument instrument = entry.getInstrument();
		checkMonochromatorGroup(instrument);
		checkInsertionDeviceGroup(instrument);
		checkSourceGroup(instrument);
	}

	private void checkUsers(NXentry entry) {
		final Map<String, NXuser> users = entry.getAllUser();
		assertThat(users.keySet(), containsInAnyOrder(EXPECTED_USER_GROUP_NAME));
		final NXuser user = users.get(EXPECTED_USER_GROUP_NAME);
		assertThat(user, is(notNullValue()));

		assertThat(user.getDataNodeNames(), containsInAnyOrder(NXuser.NX_NAME, NXuser.NX_FACILITY_USER_ID));
		assertThat(user.getNumberOfDataNodes(), is(2));
		assertThat(user.getFacility_user_idScalar(), is(equalTo(EXPECTED_USER_ID)));
		assertThat(user.getNameScalar(), is(equalTo(EXPECTED_USER_NAME)));
	}

	private void checkSourceGroup(NXinstrument instrument) {
		final NXsource source = instrument.getSource();
		assertThat(source, is(notNullValue()));

		assertThat(source.getNumberOfDataNodes(), is(4));
		assertThat(source.getNumberOfGroupNodes(), is(0));

		assertThat(source.getNameScalar(), is(equalTo("Diamond Light Source")));
		assertThat(source.getProbeScalar(), is(equalTo("x-ray")));
		assertThat(source.getTypeScalar(), is(equalTo("Synchrotron X-ray Source")));
		assertThat(source.getCurrentScalar(), is(equalTo(EXPECTED_SOURCE_CURRENT)));
	}

	private void checkSampleGroup(NXentry entry) {
		final NXsample sample = entry.getSample();
		assertThat(sample, is(notNullValue()));

		final NXbeam beam = sample.getBeam();
		assertThat(beam, is(notNullValue()));
		assertThat(beam.getDistanceScalar(), is(equalTo(0.0)));
		assertThat(beam.getDouble(NXbeam.NX_EXTENT), is(equalTo(EXPECTED_BEAM_EXTENT)));
		assertThat(beam.getIncident_energyScalar(), is(equalTo(EXPECTED_BEAM_INCIDENT_ENERGY)));
		assertThat(beam.getIncident_beam_divergenceScalar(), is(equalTo(EXPECTED_BEAM_INCIDENT_DIVERGENCE)));
		assertThat(beam.getIncident_polarizationScalar(), is(equalTo(EXPECTED_BEAM_INCIDENT_POLARIZATION)));
		assertThat(beam.getFluxScalar(), is(equalTo(EXPECTED_BEAM_FLUX)));
	}

	private void checkInsertionDeviceGroup(NXinstrument instrument) {
		final NXinsertion_device insertionDevice = instrument.getInsertion_device();
		assertThat(insertionDevice, is(notNullValue()));
		assertThat(insertionDevice.getGapScalar(), is(equalTo(EXPECTED_INSERTION_DEVICE_GAP)));
		assertThat(insertionDevice.getTaperScalar(), is(equalTo(EXPECTED_INSERTION_DEVICE_TAPER)));
		// DummyScannable always returns a double which gets converted to a long
		assertThat(insertionDevice.getHarmonicScalar(), is(equalTo((long) EXPECTED_INSERTION_DEVICE_HARMONIC)));

		assertThat(insertionDevice.getBandwidthScalar(), is(equalTo(EXPECTED_INSERTION_DEVICE_BANDWIDTH)));
		assertThat(insertionDevice.getLengthScalar(), is(equalTo(EXPECTED_INSERTION_DEVICE_LENGTH)));
	}

	private void checkMonochromatorGroup(NXinstrument instrument) {
		final NXmonochromator monochromator = instrument.getMonochromator();
		assertThat(monochromator, is(notNullValue()));
		assertThat(monochromator.getEnergyScalar(), is(equalTo(EXPECTED_MONOCHROMATOR_ENERGY)));
		assertThat(monochromator.getEnergy_errorScalar(), is(equalTo(EXPECTED_MONOCHROMATOR_ENERGY_ERROR)));
	}

}
