/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.mapping.ui.controller;

import uk.ac.gda.api.acquisition.AcquisitionControllerException;

/**
 * Thrown when a ScanningAcquisition request, either save or run, is incomplete.
 *
 * @author Maurizio Nagni
 */
public class AcquisitionConfigurationException extends AcquisitionControllerException {

	/**
	 *
	 */
	public AcquisitionConfigurationException() {
		super();
	}

	/**
	 * @param message
	 * @param cause
	 * @param enableSuppression
	 * @param writableStackTrace
	 */
	public AcquisitionConfigurationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public AcquisitionConfigurationException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param message
	 */
	public AcquisitionConfigurationException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public AcquisitionConfigurationException(Throwable cause) {
		super(cause);
	}
}
