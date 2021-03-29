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

package uk.ac.gda.ui.tool.document;

import java.util.function.Supplier;

import uk.ac.diamond.daq.mapping.api.document.scanning.ScanningAcquisition;
import uk.ac.gda.client.properties.acquisition.AcquisitionTypeProperties;

/**
 * Base interface for default acquisition documents.
 *
 * @author Maurizio Nagni
 */
interface ScanningAcquisitionFactory {
	/**
	 * Creates a new acquisition document of the specified type
	 * @param acquisitionType the acquisition type.
	 *
	 * @return a new supplier either providing a new default document or an empty {@code ScanningAcquisition}
	 *
	 * @see AcquisitionTypeProperties#getAcquisitionProperties(String)
	 */
	Supplier<ScanningAcquisition> newScanningAcquisition(String acquisitionType);
}
