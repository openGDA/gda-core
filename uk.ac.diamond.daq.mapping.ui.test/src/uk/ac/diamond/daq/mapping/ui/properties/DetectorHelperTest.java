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

package uk.ac.diamond.daq.mapping.ui.properties;

import java.net.URL;
import java.util.List;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import gda.configuration.properties.LocalProperties;
import uk.ac.diamond.daq.mapping.ui.properties.DetectorHelper.AcquisitionType;
import uk.ac.gda.client.properties.DetectorProperties;

/**
 * Tests for the {@link DetectorHelper} based on a detectors.properties file.
 *
 * @author Maurizio Nagni
 */
public class DetectorHelperTest {

	@Before
	public void before() {
		URL resource = DetectorHelperTest.class.getResource("detectors.properties");
		System.setProperty(LocalProperties.GDA_PROPERTIES_FILE, resource.getPath());
		LocalProperties.reloadAllProperties();
	}

	/**
	 * Verifies that {@link AcquisitionType#DIFFRACTION} has one associated detector
	 */
	@Test
	public void acquisitionDetectorExistsTest() {
		Optional<List<DetectorProperties>> dp = DetectorHelper.getAcquistionDetector(AcquisitionType.DIFFRACTION);
		Assert.assertTrue(dp.isPresent());
		Assert.assertEquals(1, dp.get().size());
	}

	/**
	 * Verifies that {@link AcquisitionType#BEAM_SELECTOR} has no associated detector,because in the properties the
	 * required detector id does not exist.
	 */
	@Test
	public void acquisitionDetectorNotExistsTest() {
		Optional<List<DetectorProperties>> dp = DetectorHelper.getAcquistionDetector(AcquisitionType.BEAM_SELECTOR);
		Assert.assertFalse(dp.isPresent());
	}

	/**
	 * Verifies that {@link AcquisitionType#TOMOGRAPHY} has two associated detectors.
	 */
	@Test
	public void multipleDetectorExistsTest() {
		Optional<List<DetectorProperties>> dp = DetectorHelper.getAcquistionDetector(AcquisitionType.TOMOGRAPHY);
		Assert.assertTrue(dp.isPresent());
		Assert.assertEquals(2, dp.get().size());
	}
}
