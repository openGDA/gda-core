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

package uk.ac.diamond.daq.mapping.api.document.helper.reader;

import java.util.Optional;
import java.util.function.Supplier;

import uk.ac.gda.api.acquisition.configuration.ImageCalibration;
import uk.ac.gda.api.acquisition.configuration.calibration.FlatCalibrationDocument;

/**
 * Utility class to read {@link ImageCalibration} documents.
 *
 * @author Maurizio Nagni
 */
public class ImageCalibrationReader extends AcquisitionReaderBase<ImageCalibration>{

	public ImageCalibrationReader(Supplier<ImageCalibration> supplier) {
		super(supplier);
	}

	/**
	 * Get the {@link DarkCalibrationReader}
	 * @return the document {@code DarkCalibrationDocument}, otherwise {@code null}
	 */
	public DarkCalibrationReader getDarkCalibration() {
		return Optional.ofNullable(getData())
				.map(ImageCalibration::getDarkCalibration)
				.map(e -> new DarkCalibrationReader(() -> e))
				.orElseGet(() -> new DarkCalibrationReader(null));
	}

	/**
	 * Get the {@link FlatCalibrationDocument}
	 * @return the document {@code FlatCalibrationDocument}, otherwise {@code null}
	 */
	public FlatCalibrationReader getFlatCalibration() {
		return Optional.ofNullable(getData())
				.map(ImageCalibration::getFlatCalibration)
				.map(e -> new FlatCalibrationReader(() -> e))
				.orElseGet(() -> new FlatCalibrationReader(null));
	}
}
