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

import static org.eclipse.scanning.example.detector.MandelbrotDetector.FIELD_NAME_SPECTRUM;
import static org.eclipse.scanning.example.detector.MandelbrotDetector.FIELD_NAME_VALUE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import java.util.Arrays;

import org.eclipse.dawnsci.nexus.NXentry;
import org.eclipse.dawnsci.nexus.NXroot;
import org.eclipse.dawnsci.nexus.scan.IDefaultDataGroupCalculator;
import org.eclipse.scanning.api.device.IRunnableDevice;
import org.eclipse.scanning.api.device.IWritableDetector;
import org.eclipse.scanning.api.scan.models.ScanModel;
import org.eclipse.scanning.example.detector.MandelbrotModel;
import org.eclipse.scanning.sequencer.nexus.DefaultDataGroupConfiguration;
import org.eclipse.scanning.test.util.TestDetectorHelpers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import uk.ac.diamond.osgi.services.ServiceProvider;

class DefaultDataGroupConfigurationTest extends NexusTest {

	private static final String IMAGE_DATA_GROUP_NAME = MANDELBROT_DETECTOR_NAME;
	private static final String SPECTRUM_DATA_GROUP_NAME = MANDELBROT_DETECTOR_NAME + "_" + FIELD_NAME_SPECTRUM;
	private static final String VALUE_DATA_GROUP_NAME = MANDELBROT_DETECTOR_NAME + "_" + FIELD_NAME_VALUE;

	private static final int[] SHAPE = { 5, 2 };

	private static IWritableDetector<MandelbrotModel> detector;

	@BeforeEach
	void setUp() throws Exception {
		final MandelbrotModel model = createMandelbrotModel();
		detector = TestDetectorHelpers.createAndConfigureMandelbrotDetector(model);
		assertThat(detector, is(notNullValue()));
	}

	@AfterEach
	void tearDown() {
		// clear this service only, as it is configured differently for each test method
		if (ServiceProvider.getOptionalService(IDefaultDataGroupCalculator.class).isPresent()) {
			ServiceProvider.removeService(IDefaultDataGroupCalculator.class);
		}
	}

	@Test
	void testNoDefaultDataGroupConfiguration() throws Exception {
		runScanAndCheckNexusFile(IMAGE_DATA_GROUP_NAME);
	}

	@Test
	void testDefaultGroupConfiguration() throws Exception {
		final String dataGroupName = SPECTRUM_DATA_GROUP_NAME;
		createDefaultDataGroupConfiguration(dataGroupName);
		runScanAndCheckNexusFile(dataGroupName);
	}

	@Test
	void testDefaultGroupConfigurationNoSuchGroup() throws Exception {
		createDefaultDataGroupConfiguration("nonExist");
		runScanAndCheckNexusFile(IMAGE_DATA_GROUP_NAME);
	}

	@Test
	void testDefaultGroupConfigurationListFirst() throws Exception {
		final String dataGroupName = VALUE_DATA_GROUP_NAME;
		createDefaultDataGroupConfiguration(dataGroupName, "nonExist");
		runScanAndCheckNexusFile(dataGroupName);
	}

	@Test
	void testDefaultGroupConfigurationListSecond() throws Exception {
		createDefaultDataGroupConfiguration("nonExist", VALUE_DATA_GROUP_NAME);
		runScanAndCheckNexusFile(VALUE_DATA_GROUP_NAME);
	}

	@Test
	void testDefaultGroupConfigurationListNoSuchGroup() throws Exception {
		createDefaultDataGroupConfiguration("nonExist1", "nonExist2");
		runScanAndCheckNexusFile(IMAGE_DATA_GROUP_NAME);
	}

	private void createDefaultDataGroupConfiguration(final String... dataGroupNames) {
		final DefaultDataGroupConfiguration dataGroupConfig = new DefaultDataGroupConfiguration();
		if (dataGroupNames.length == 1) {
			dataGroupConfig.setDefaultDataGroupName(dataGroupNames[0]);
		} else {
			dataGroupConfig.setDefaultDataGroupNames(Arrays.asList(dataGroupNames));
		}
		ServiceProvider.setService(IDefaultDataGroupCalculator.class, dataGroupConfig);
	}

	private void runScanAndCheckNexusFile(final String expectedDataGroupName) throws Exception {
		final IRunnableDevice<ScanModel> scanner = createGridScan(detector, output, false, SHAPE);
		scanner.run(null);

		checkNexusFile(scanner, expectedDataGroupName);
	}

	private void checkNexusFile(final IRunnableDevice<ScanModel> scanner,
			String expectedDefaultDataGroupName) throws Exception {
		final NXroot root = checkNexusFile(scanner, false, SHAPE);
		final NXentry entry = root.getEntry();
		assertThat(entry.getAttributeDefault(), is(equalTo(expectedDefaultDataGroupName)));
	}
}
