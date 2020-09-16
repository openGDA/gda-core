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

package uk.ac.diamond.daq.mapping.document;

import java.util.UUID;
import java.util.function.Supplier;

import org.junit.Assert;
import org.junit.Test;

import uk.ac.diamond.daq.mapping.api.document.helper.ImageCalibrationHelper;
import uk.ac.diamond.daq.mapping.api.document.scanning.ScanningAcquisition;
import uk.ac.diamond.daq.mapping.api.document.scanning.ScanningConfiguration;
import uk.ac.diamond.daq.mapping.api.document.scanning.ScanningParameters;
import uk.ac.gda.api.acquisition.configuration.ImageCalibration;
import uk.ac.gda.api.acquisition.configuration.calibration.DarkCalibrationDocument;
import uk.ac.gda.api.acquisition.configuration.calibration.FlatCalibrationDocument;

/**
 * Tests for the {@link ImageCalibrationHelper}
 *
 * @author Maurizio Nagni
 */
public class ImageCalibrationHelperTest extends DocumentTestBase {

	ImageCalibrationHelper imageCalibrationHelper;

	@Test
	public void updatesDarkExposuresTest() {
		Supplier<ScanningConfiguration> configurationSupplier = getScanningAcquisitionSupplier().get()::getAcquisitionConfiguration;

		imageCalibrationHelper = new ImageCalibrationHelper(configurationSupplier);
		imageCalibrationHelper.updateDarkNumberExposures(11);
		Assert.assertEquals(11, configurationSupplier.get().getImageCalibration().getDarkCalibration().getNumberExposures());
	}

	private Supplier<ScanningAcquisition> getScanningAcquisitionSupplier() {
		return () -> {
			ScanningAcquisition newConfiguration = new ScanningAcquisition();
			newConfiguration.setUuid(UUID.randomUUID());
			ScanningConfiguration configuration = new ScanningConfiguration();
			newConfiguration.setAcquisitionConfiguration(configuration);
			configuration.setImageCalibration(createImageCalibrationDocument());

			ScanningParameters acquisitionParameters = new ScanningParameters();
			newConfiguration.getAcquisitionConfiguration().setAcquisitionParameters(acquisitionParameters);
			return newConfiguration;
		};
	}

	private ImageCalibration createImageCalibrationDocument() {
		ImageCalibration.Builder builder = new ImageCalibration.Builder();

		DarkCalibrationDocument.Builder dark = new DarkCalibrationDocument.Builder();
		FlatCalibrationDocument.Builder flat = new FlatCalibrationDocument.Builder();
		builder.withDarkCalibration(dark.build());
		builder.withFlatCalibration(flat.build());

		return builder.build();
	}
}
