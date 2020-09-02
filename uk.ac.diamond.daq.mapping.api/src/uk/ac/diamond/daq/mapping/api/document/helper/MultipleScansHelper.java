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

import java.util.function.Supplier;

import uk.ac.diamond.daq.mapping.api.document.scanning.ScanningConfiguration;
import uk.ac.gda.api.acquisition.configuration.MultipleScansType;

public class MultipleScansHelper extends ConfigurationHelperBase {

	public MultipleScansHelper(Supplier<ScanningConfiguration> scanningConfigurationSupplier) {
		super(scanningConfigurationSupplier);
	}

	public void updateMultipleScanType(MultipleScansType multipleScanType) {
		updateScanningParameters(getMultipleScanBuilder().withMultipleScansType(multipleScanType));
	}

	public void updateMultipleScanWaitingTime(int waitingTime) {
		updateScanningParameters(getMultipleScanBuilder().withWaitingTime(waitingTime));
	}

	public void updateMultipleScanRepetitions(int numberRepetitions) {
		updateScanningParameters(getMultipleScanBuilder().withNumberRepetitions(numberRepetitions));
	}
}
