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

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import uk.ac.gda.api.acquisition.configuration.calibration.DarkCalibrationDocument;
import uk.ac.gda.api.acquisition.parameters.DevicePositionDocument;

/**
 * Utility class to read {@link DarkCalibrationDocument} documents.
 *
 * @author Maurizio Nagni
 */
public class DarkCalibrationReader extends AcquisitionReaderBase<DarkCalibrationDocument> {

	public DarkCalibrationReader(Supplier<DarkCalibrationDocument> supplier) {
		super(supplier);
	}

	/**
	 * Get value of {@code getImageCalibration().getDarkCalibration().getDarkExposures()}
	 *
	 * @return the existing value, otherwise {@code 0}
	 */
	public int getNumberExposures() {
		return Optional.ofNullable(getData())
			.map(DarkCalibrationDocument::getNumberExposures)
			.orElseGet(() -> 0);
	}

	/**
	 * Get value of {@code getImageCalibration().getDarkCalibration().isBeforeAcquisition()}
	 *
	 * @return the existing value, otherwise {@code false}
	 */
	public boolean isBeforeAcquisition() {
		return Optional.ofNullable(getData())
			.map(DarkCalibrationDocument::isBeforeAcquisition)
			.orElseGet(() -> false);
	}


	/**
	 * Get value of {@code getImageCalibration().getDarkCalibration().isAfterAcquisition()}
	 *
	 * @return the existing value, otherwise {@code false}
	 */
	public boolean isAfterAcquisition() {
		return Optional.ofNullable(getData())
			.map(DarkCalibrationDocument::isAfterAcquisition)
			.orElseGet(() -> false);
	}

	/**
	 * Get value of {@code getImageCalibration().getDarkCalibration().getPosition()}
	 *
	 * @return the existing value, otherwise an empty {@code Set}
	 */
	public Set<DevicePositionDocument> getPosition() {
		return Optional.ofNullable(getData())
			.map(DarkCalibrationDocument::getPosition)
			.orElseGet(HashSet::new);
	}
}
