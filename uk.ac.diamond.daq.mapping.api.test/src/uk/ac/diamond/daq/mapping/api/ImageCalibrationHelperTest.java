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

package uk.ac.diamond.daq.mapping.api;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import uk.ac.diamond.daq.mapping.api.document.helper.ImageCalibrationHelper;
import uk.ac.diamond.daq.mapping.api.document.scanning.ScanningConfiguration;
import uk.ac.gda.api.acquisition.configuration.ImageCalibration;
import uk.ac.gda.api.acquisition.configuration.calibration.DarkCalibrationDocument;
import uk.ac.gda.api.acquisition.configuration.calibration.FlatCalibrationDocument;

/**
 * Tests for the {@link ImageCalibrationHelper}
 *
 * @author Maurizio Nagni
 */
public class ImageCalibrationHelperTest {

	@Before
	public void before() {

	}

	/**
	 * Updating numberExposures, creates a DarkCalibrationDocument if missing in the ScanningConfiguration
	 */
	@Test
	public void useDefaultDarkCalibrationBuilderTest() {
		ScanningConfiguration scanningConfiguraton = new ScanningConfiguration();
		ImageCalibrationHelper imageCalibrationHelper = new ImageCalibrationHelper(() -> scanningConfiguraton);
		imageCalibrationHelper.updateDarkNumberExposures(2);
		Assert.assertEquals(2, scanningConfiguraton.getImageCalibration().getDarkCalibration().getNumberExposures());
		Assert.assertNull(scanningConfiguraton.getImageCalibration().getFlatCalibration());
	}

	/**
	 * Updating numberExposures, creates a DarkCalibrationDocument if missing in the ScanningConfiguration
	 */
	@Test
	public void useDefaultFlatCalibrationBuilderTest() {
		ScanningConfiguration scanningConfiguraton = new ScanningConfiguration();
		ImageCalibrationHelper imageCalibrationHelper = new ImageCalibrationHelper(() -> scanningConfiguraton);
		imageCalibrationHelper.updateFlatNumberExposures(2);
		Assert.assertEquals(2, scanningConfiguraton.getImageCalibration().getFlatCalibration().getNumberExposures());
		Assert.assertNull(scanningConfiguraton.getImageCalibration().getDarkCalibration());
	}

	/**
	 * Updating numberExposures, clones the existing DarkCalibrationDocument if missing in the ScanningConfiguration
	 */
	@Test
	public void useDarkCalibrationBuilderTest() {
		ScanningConfiguration scanningConfiguraton = new ScanningConfiguration();
		ImageCalibration imageCalibration = new ImageCalibration();
		imageCalibration.setDarkCalibration(
				new DarkCalibrationDocument.Builder()
				.withNumberExposures(33)
				.build());
		scanningConfiguraton.setImageCalibration(imageCalibration);
		ImageCalibrationHelper imageCalibrationHelper = new ImageCalibrationHelper(() -> scanningConfiguraton);
		imageCalibrationHelper.updateDarkNumberExposures(2);
		Assert.assertEquals(2, scanningConfiguraton.getImageCalibration().getDarkCalibration().getNumberExposures());
		Assert.assertNull(scanningConfiguraton.getImageCalibration().getFlatCalibration());
	}

	/**
	 * Updating numberExposures, creates a DarkCalibrationDocument if missing in the ScanningConfiguration
	 */
	@Test
	public void useFlatCalibrationBuilderTest() {
		ScanningConfiguration scanningConfiguraton = new ScanningConfiguration();
		ImageCalibration imageCalibration = new ImageCalibration();
		imageCalibration.setFlatCalibration(
				new FlatCalibrationDocument.Builder()
				.withNumberExposures(33)
				.build());
		scanningConfiguraton.setImageCalibration(imageCalibration);
		ImageCalibrationHelper imageCalibrationHelper = new ImageCalibrationHelper(() -> scanningConfiguraton);
		imageCalibrationHelper.updateFlatNumberExposures(2);
		Assert.assertEquals(2, scanningConfiguraton.getImageCalibration().getFlatCalibration().getNumberExposures());
		Assert.assertNull(scanningConfiguraton.getImageCalibration().getDarkCalibration());
	}

}
