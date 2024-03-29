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

package uk.ac.diamond.daq.mapping.api.document.helper;

import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import uk.ac.diamond.daq.mapping.api.document.base.AcquisitionConfigurationBase;
import uk.ac.diamond.daq.mapping.api.document.scanning.ScanningConfiguration;
import uk.ac.gda.api.acquisition.configuration.ImageCalibration;
import uk.ac.gda.api.acquisition.configuration.calibration.DarkCalibrationDocument;
import uk.ac.gda.api.acquisition.configuration.calibration.FlatCalibrationDocument;
import uk.ac.gda.api.acquisition.parameters.DetectorDocument;
import uk.ac.gda.api.acquisition.parameters.DevicePositionDocument;

/**
 * Utility class to update {@link ImageCalibration} documents
 *
 * @author Maurizio Nagni
 */
public class ImageCalibrationHelper extends ConfigurationHelperBase {

	public ImageCalibrationHelper(Supplier<? extends AcquisitionConfigurationBase<?>> scanningConfigurationSupplier) {
		super(scanningConfigurationSupplier);
	}

	// DarkCalibrationDocument fields

	public void updateDarkNumberExposures(int numberExposures) {
		updateScanningParameters(getDarkCalibrationBuilder().withNumberExposures(numberExposures));
	}

	public void updateDarkBeforeAcquisitionExposures(boolean beforeAcquisition) {
		updateScanningParameters(getDarkCalibrationBuilder().withBeforeAcquisition(beforeAcquisition));
	}

	public void updateDarkAfterAcquisitionExposures(boolean afterAcquisition) {
		updateScanningParameters(getDarkCalibrationBuilder().withAfterAcquisition(afterAcquisition));
	}

	public void updateDarkDetectorDocument(DetectorDocument detectorDocument) {
		updateScanningParameters(getDarkCalibrationBuilder().withDetectorDocument(detectorDocument));
	}

	public void updateDarkDetectorPositionDocument(Set<DevicePositionDocument> position) {
		updateScanningParameters(getDarkCalibrationBuilder().withPosition(position));
	}

	// FlatCalibrationDocument fields

	public void updateFlatNumberExposures(int numberExposures) {
		updateScanningParameters(getFlatCalibrationBuilder().withNumberExposures(numberExposures));
	}

	public void updateFlatBeforeAcquisitionExposures(boolean beforeAcquisition) {
		updateScanningParameters(getFlatCalibrationBuilder().withBeforeAcquisition(beforeAcquisition));
	}

	public void updateFlatAfterAcquisitionExposures(boolean afterAcquisition) {
		updateScanningParameters(getFlatCalibrationBuilder().withAfterAcquisition(afterAcquisition));
	}

	public void updateFlatDetectorDocument(DetectorDocument detectorDocument) {
		updateScanningParameters(getFlatCalibrationBuilder().withDetectorDocument(detectorDocument));
	}

	public void updateFlatDetectorPositionDocument(Set<DevicePositionDocument> position) {
		updateScanningParameters(getFlatCalibrationBuilder().withPosition(position));
	}

	/**
	 * Clones the existing DarkCalibrationDocument otherwise creates a new one. A class calling this method, is going to modify
	 * the {@link DarkCalibrationDocument} instance returned by
	 * {@code getScanningParameters().getImageCalibration().getDarkCalibration()}, which may
	 * still not exist. Consequently this method return a builder either on the existing {@link DarkCalibrationDocument} or
	 * creates for the request a brand new one.
	 *
	 * @return clones the existing scanpathDocument otherwise creates a new one
	 */
	private DarkCalibrationDocument.Builder getDarkCalibrationBuilder() {
		return Optional.ofNullable(getImageCalibration())
			.map(ImageCalibration::getDarkCalibration)
			.map(DarkCalibrationDocument.Builder::new)
			//otherwise uses the default constructor
			.orElseGet(DarkCalibrationDocument.Builder::new);
	}

	/**
	 * Clones the existing DarkCalibrationDocument otherwise creates a new one. A class calling this method, is going to modify
	 * the {@link FlatCalibrationDocument} instance returned by
	 * {@code getScanningParameters().getImageCalibration().getFlatCalibration()}, which may
	 * still not exist. Consequently this method return a builder either on the existing {@link FlatCalibrationDocument} or
	 * creates for the request a brand new one.
	 *
	 * @return clones the existing scanpathDocument otherwise creates a new one
	 */
	private FlatCalibrationDocument.Builder getFlatCalibrationBuilder() {
		return Optional.ofNullable(getImageCalibration())
				.map(ImageCalibration::getFlatCalibration)
				.map(FlatCalibrationDocument.Builder::new)
				//otherwise uses the default constructor
				.orElseGet(FlatCalibrationDocument.Builder::new);
	}

	/**
	 * Sets the inner {@link DarkCalibrationDocument} with the one generated by the {@code builder}
	 * @param builder the new, to build, {@code DarkCalibrationDocument}
	 */
	private void updateScanningParameters(DarkCalibrationDocument.Builder builder) {
		updateScanningParameters(getImageCalibrationBuilder().withDarkCalibration(builder.build()));
	}

	/**
	 * Sets the inner {@link FlatCalibrationDocument} with the one generated by the {@code builder}
	 * @param builder the new, to build, {@code FlatCalibrationDocument}
	 */
	private void updateScanningParameters(FlatCalibrationDocument.Builder builder) {
		updateScanningParameters(getImageCalibrationBuilder().withFlatCalibration(builder.build()));
	}

	/**
	 * Sets the inner {@link ImageCalibration} with the one generated by the {@code builder}
	 * @param builder the new, to build, {@code ImageCalibration}
	 */
	private void updateScanningParameters(ImageCalibration.Builder builder) {
		getScanningParameters().setImageCalibration(builder.build());
	}

	/**
	 * Return the existing ImageCalibration otherwise set a new one in the underlying ScanningConfiguration
	 * @return value of {@link ScanningConfiguration#getImageCalibration()}
	 */
	private ImageCalibration getImageCalibration() {
		return Optional.ofNullable(getScanningParameters().getImageCalibration())
			.orElseGet(this::createImageCalibration);
	}

	private ImageCalibration createImageCalibration() {
		getScanningParameters().setImageCalibration(new ImageCalibration.Builder().build());
		return getScanningParameters().getImageCalibration();
	}

	/**
	 * Return a builder for the existing ImageCalibration otherwise set a new one in the underlying ScanningConfiguration
	 * @return value of {@code new ImageCalibration.Builder(getImageCalibration())}
	 */
	private ImageCalibration.Builder getImageCalibrationBuilder() {
		return new ImageCalibration.Builder(getImageCalibration());
	}
}
