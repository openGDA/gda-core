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

package gda.data.scan.datawriter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.eclipse.dawnsci.nexus.INexusDevice;
import org.eclipse.dawnsci.nexus.NXobject;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusScanInfo.ScanRole;
import org.eclipse.dawnsci.nexus.device.INexusDeviceAdapterFactory;
import org.eclipse.dawnsci.nexus.device.INexusDeviceService;
import org.eclipse.dawnsci.nexus.device.impl.NexusDeviceService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import gda.data.scan.datawriter.AbstractNexusDataWriterScanTest.DummyFileCreatorDetector;
import gda.data.scan.datawriter.AbstractNexusDataWriterScanTest.DummyImageDetector;
import gda.data.scan.datawriter.AbstractNexusDataWriterScanTest.MetadataScannableNexusDevice;
import gda.data.scan.nexus.device.ConfiguredScannableNexusDevice;
import gda.data.scan.nexus.device.CounterTimerNexusDevice;
import gda.data.scan.nexus.device.DefaultScannableNexusDevice;
import gda.data.scan.nexus.device.FileCreatorDetectorNexusDevice;
import gda.data.scan.nexus.device.GDANexusDeviceAdapterFactory;
import gda.data.scan.nexus.device.GenericDetectorNexusDevice;
import gda.data.scan.nexus.device.ScannableNexusDeviceConfiguration;
import gda.data.scan.nexus.device.ScannableNexusDeviceConfigurationRegistry;
import gda.device.Detector;
import gda.device.Scannable;
import gda.device.scannable.DummyScannable;
import uk.ac.diamond.osgi.services.ServiceProvider;

public class GDANexusDeviceAdapterFactoryTest {

	@BeforeAll
	public static void setUp() {
		ServiceProvider.setService(INexusDeviceService.class, new NexusDeviceService());
		ServiceProvider.setService(INexusDeviceAdapterFactory.class, new GDANexusDeviceAdapterFactory());
		ServiceProvider.setService(ScannableNexusDeviceConfigurationRegistry.class, new ScannableNexusDeviceConfigurationRegistry());
	}

	@AfterAll
	public static void tearDown() {
		ServiceProvider.reset();
	}

	public static ScanRole[] scanRoleParameters() {
		return ScanRole.values();
	}

	private void testFactoryCreatesCorrectNexusDeviceForDetector(ScanRole scanRole, Detector detector, Class<?> expectedClass) throws NexusException {
		final INexusDeviceService nexusDeviceService = ServiceProvider.getService(INexusDeviceService.class);
		try {
			final INexusDevice<?> nexusDevice = nexusDeviceService.getNexusDevice(detector, scanRole);
			final boolean correctDevice = scanRole == ScanRole.MONITOR_PER_SCAN ?
				nexusDevice instanceof DefaultScannableNexusDevice :
				nexusDevice.getClass().equals(expectedClass);
			assertThat(correctDevice, is(true));
		} catch (IllegalArgumentException e) {
			assertThat(scanRole, null);
		}
	}

	@ParameterizedTest(name = "scanRole = {0}")
	@MethodSource("scanRoleParameters")
	void testNexusDevice(ScanRole scanRole) throws NexusException {
		final MetadataScannableNexusDevice<NXobject> nexusDevice = new MetadataScannableNexusDevice<>("nexusDevice", null);
		final INexusDeviceService nexusDeviceService = ServiceProvider.getService(INexusDeviceService.class);
		final INexusDevice<?> nexusDeviceFromService = nexusDeviceService.getNexusDevice(nexusDevice, scanRole);
		assertThat(nexusDeviceFromService, is(nexusDevice));
	}

	@ParameterizedTest(name = "scanRole = {0}")
	@MethodSource("scanRoleParameters")
	void testCounterTimerDetector(ScanRole scanRole) throws NexusException {
		final DummyImageDetector detector = new DummyImageDetector();
		detector.setExtraNames(new String[] {"1", "2"});
		testFactoryCreatesCorrectNexusDeviceForDetector(scanRole, detector, CounterTimerNexusDevice.class);
	}

	@ParameterizedTest(name = "scanRole = {0}")
	@MethodSource("scanRoleParameters")
	void testGenericDetector(ScanRole scanRole) throws NexusException {
		testFactoryCreatesCorrectNexusDeviceForDetector(scanRole, new DummyImageDetector(), GenericDetectorNexusDevice.class);
	}

	@ParameterizedTest(name = "scanRank = {0}")
	@MethodSource("scanRoleParameters")
	void testCreatOwnFilesDetector(ScanRole scanRole) throws NexusException {
		testFactoryCreatesCorrectNexusDeviceForDetector(scanRole, new DummyFileCreatorDetector(), FileCreatorDetectorNexusDevice.class);
	}

	@ParameterizedTest(name = "scanRank = {0}")
	@MethodSource("scanRoleParameters")
	void testDefaultScannable(ScanRole scanRole) throws NexusException {
		final INexusDeviceService nexusDeviceService = ServiceProvider.getService(INexusDeviceService.class);
		final INexusDevice<?> nexusDevice = nexusDeviceService.getNexusDevice(new DummyScannable("defaultScannable"), scanRole);
		final boolean correctDevice = nexusDevice instanceof DefaultScannableNexusDevice;
		assertThat(correctDevice, is(true));
	}

	@Test
	void testConfiguredScannable() throws NexusException {
		final String scannableName = "configuredScannable";
		final Scannable scannable = new DummyScannable(scannableName);
		final ScannableNexusDeviceConfiguration config = new ScannableNexusDeviceConfiguration();
		config.setScannableName(scannableName);
		config.register();
		final INexusDeviceService nexusDeviceService = ServiceProvider.getService(INexusDeviceService.class);
		final INexusDevice<?> nexusDevice = nexusDeviceService.getNexusDevice(scannable, ScanRole.SCANNABLE);
		final boolean correctDevice = nexusDevice instanceof ConfiguredScannableNexusDevice;
		assertThat(correctDevice, is(true));
	}
}
