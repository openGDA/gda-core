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

import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import gda.configuration.properties.LocalProperties;
import uk.ac.diamond.daq.mapping.ui.properties.AcquisitionsPropertiesHelper.AcquisitionPropertyType;

/**
 * Tests for the {@link AcquisitionsPropertiesHelper} based on a detectors.properties file.
 *
 * @author Maurizio Nagni
 */
public class AcquistionsHelperTest {

	@Before
	public void before() {
		File resource = new File("test/resources/acquisitions.properties");
		Optional.ofNullable(resource)
				.ifPresent(r -> System.setProperty(LocalProperties.GDA_PROPERTIES_FILE, resource.getPath()));
		LocalProperties.reloadAllProperties();
	}

	/**
	 * Verifies that {@link AcquisitionPropertyType#DIFFRACTION} has one associated detector
	 */
	@Test
	public void acquisitionDetectorExistsTest() {
		List<AcquisitionPropertiesDocument> dp = AcquisitionsPropertiesHelper
				.getAcquistionPropertiesDocument(AcquisitionPropertyType.DIFFRACTION);
		Assert.assertEquals(1, dp.size());
	}

	/**
	 * Verifies that {@link AcquisitionPropertyType#BEAM_SELECTOR} has no associated detector,because in the properties
	 * the required detector id does not exist.
	 */
	@Test
	public void acquisitionDetectorNotExistsTest() {
		List<AcquisitionPropertiesDocument> dp = AcquisitionsPropertiesHelper
				.getAcquistionPropertiesDocument(AcquisitionPropertyType.BEAM_SELECTOR);
		Assert.assertTrue(Objects.nonNull(dp));

		// The beam selector detector has one configuration because the second does not exist
		Assert.assertEquals(1, dp.size());
	}

	/**
	 * Verifies that {@link AcquisitionPropertyType#TOMOGRAPHY} has two associated detectors.
	 */
	@Test
	public void multipleDetectorExistsTest() {
		List<AcquisitionPropertiesDocument> dp = AcquisitionsPropertiesHelper
				.getAcquistionPropertiesDocument(AcquisitionPropertyType.TOMOGRAPHY);
		Assert.assertTrue(Objects.nonNull(dp));
		Assert.assertEquals(2, dp.size());
	}

	/**
	 * Verifies that {@link AcquisitionPropertyType#TOMOGRAPHY} has two associated detectors.
	 */
	@Test
	public void detectorHasCamerasTest() {
		List<AcquisitionPropertiesDocument> dp = AcquisitionsPropertiesHelper
				.getAcquistionPropertiesDocument(AcquisitionPropertyType.TOMOGRAPHY);
		Assert.assertTrue(Objects.nonNull(dp));

		// The first tomography detector has one camera
		Assert.assertEquals(1, dp.get(0).getCameras().size());
		// The secnd tomography detector has no camera
		Assert.assertEquals(2, dp.get(1).getCameras().size());
	}

	/**
	 * Verifies that {@link AcquisitionPropertyType#BEAM_SELECTOR} has two associated detectors.
	 */
	@Test
	public void detectorHasTwoCamerasTest() {
		List<AcquisitionPropertiesDocument> dp = AcquisitionsPropertiesHelper
				.getAcquistionPropertiesDocument(AcquisitionPropertyType.BEAM_SELECTOR);
		// The beam selector detector has two cameras
		Assert.assertEquals(2, dp.get(0).getCameras().size());
	}
}
