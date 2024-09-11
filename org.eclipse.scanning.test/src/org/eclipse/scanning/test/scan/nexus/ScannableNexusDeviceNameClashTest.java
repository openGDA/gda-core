/*-
 * Copyright © 2024 Diamond Light Source Ltd.
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

import static org.eclipse.dawnsci.nexus.scan.NexusScanConstants.ATTRIBUTE_NAME_UNITS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import org.eclipse.dawnsci.nexus.NXcrystal;
import org.eclipse.dawnsci.nexus.NXentry;
import org.eclipse.dawnsci.nexus.NXinstrument;
import org.eclipse.dawnsci.nexus.NXmonochromator;
import org.eclipse.dawnsci.nexus.NexusBaseClass;
import org.eclipse.dawnsci.nexus.device.INexusDeviceService;
import org.eclipse.scanning.api.device.IScanDevice;
import org.eclipse.scanning.api.device.IWritableDetector;
import org.eclipse.scanning.device.GroupMetadataNode;
import org.eclipse.scanning.device.MonochromatorNexusDevice;
import org.eclipse.scanning.device.ScalarField;
import org.eclipse.scanning.example.detector.MandelbrotModel;
import org.eclipse.scanning.example.scannable.MockNeXusScannable;
import org.eclipse.scanning.example.scannable.MockScannableConnector;
import org.eclipse.scanning.test.util.TestDetectorHelpers;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import gda.jython.InterfaceProvider;
import gda.jython.MockJythonServerFacade;
import uk.ac.diamond.osgi.services.ServiceProvider;

class ScannableNexusDeviceNameClashTest extends NexusTest {

	private static final int[] SCAN_SHAPE = { 5, 2 };

	private static final String MONOCHROMATOR_NAME = "dcm";
	private static final double MONOCHROMATOR_ENERGY = 387.12;
	private static final String CRYSTAL_NAME = "crystal";
	private static final String CRYSTAL_TYPE = "Cu";
	private static final double CRYSTAL_TEMPERATURE = 83.2;
	private static final String UNITS_KELVIN = "K";

	private IWritableDetector<MandelbrotModel> detector;

	@BeforeAll
	static void setUpBeforeClass() {
		InterfaceProvider.setJythonNamespaceForTesting(new MockJythonServerFacade());
	}

	@AfterAll
	static void tearDownAfterClass() {
		InterfaceProvider.setJythonNamespaceForTesting(null);
	}

	@BeforeEach
	void beforeTest() throws Exception {
		final MockNeXusScannable energyScannable = new MockNeXusScannable(MONOCHROMATOR_NAME, MONOCHROMATOR_ENERGY, 3);
		scannableDeviceService.register(energyScannable);

		final MandelbrotModel model = createMandelbrotModel();
		detector = TestDetectorHelpers.createAndConfigureMandelbrotDetector(model);
		assertThat(detector, is(notNullValue()));

		final MonochromatorNexusDevice monochromatorNexusDevice = new MonochromatorNexusDevice();
		monochromatorNexusDevice.setName(MONOCHROMATOR_NAME);
		monochromatorNexusDevice.setEnergyScannableName(MONOCHROMATOR_NAME);

		final GroupMetadataNode<NXcrystal> crystalGroup = new GroupMetadataNode<>();
		crystalGroup.setName(CRYSTAL_NAME);
		crystalGroup.setNexusBaseClass(NexusBaseClass.NX_CRYSTAL);
		crystalGroup.addChildNode(new ScalarField(NXcrystal.NX_TYPE, CRYSTAL_TYPE));
		crystalGroup.addChildNode(new ScalarField(NXcrystal.NX_TEMPERATURE, CRYSTAL_TEMPERATURE, UNITS_KELVIN));
		monochromatorNexusDevice.addNode(crystalGroup);

		ServiceProvider.getService(INexusDeviceService.class).register(monochromatorNexusDevice);

		((MockScannableConnector) scannableDeviceService).setGlobalPerScanMonitorNames(MONOCHROMATOR_NAME);
	}

	@AfterEach
	void afterTest() {
		((MockScannableConnector) scannableDeviceService).setGlobalPerScanMonitorNames();
	}

	@Test
	void testScanWithNameClash() throws Exception {
		IScanDevice scanner = createGridScan(detector, output, false, SCAN_SHAPE);
		scanner.run();

		checkNexusFile(scanner);
	}

	private void checkNexusFile(IScanDevice scanner) throws Exception {
		checkNexusFile(scanner, false, SCAN_SHAPE);

		final NXentry entry = getNexusRoot(scanner).getEntry();
		final NXinstrument instrument = entry.getInstrument();
		final NXmonochromator monochromator = instrument.getMonochromator(MONOCHROMATOR_NAME);
		assertThat(monochromator, is(notNullValue()));
		assertThat(monochromator.getDataNodeNames(), contains(NXmonochromator.NX_ENERGY));

		assertThat(monochromator.getGroupNodeNames(), contains(CRYSTAL_NAME));
		final NXcrystal crystalGroup = monochromator.getCrystal(CRYSTAL_NAME);
		assertThat(crystalGroup, is(notNullValue()));
		assertThat(crystalGroup.getDataNodeNames(), containsInAnyOrder(NXcrystal.NX_TYPE, NXcrystal.NX_TEMPERATURE));
		assertThat(crystalGroup.getTypeScalar(), is(equalTo(CRYSTAL_TYPE)));
		assertThat(crystalGroup.getTemperatureScalar(), is(closeTo(CRYSTAL_TEMPERATURE, 1e-15)));
		assertThat(crystalGroup.getAttr(NXcrystal.NX_TEMPERATURE, ATTRIBUTE_NAME_UNITS), is(equalTo(UNITS_KELVIN)));
	}

}
