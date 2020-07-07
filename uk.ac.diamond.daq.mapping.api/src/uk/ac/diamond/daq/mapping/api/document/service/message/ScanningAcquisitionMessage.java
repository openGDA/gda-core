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

package uk.ac.diamond.daq.mapping.api.document.service.message;

import java.io.Serializable;

import uk.ac.diamond.daq.api.messaging.Message;
import uk.ac.diamond.daq.mapping.api.document.scanning.ScanningAcquisition;

/**
 * Class to transport a serialised {@link ScanningAcquisition} document
 *
 * @author Maurizio Nagni
 */
public class ScanningAcquisitionMessage implements Message, Serializable {
	private final Serializable acquisition;

	/**
	 * @param acquisition a serialised acquistion document
	 */
	public ScanningAcquisitionMessage(Serializable acquisition) {
		super();
		this.acquisition = acquisition;
	}

	/**
	 * @return the serialised document
	 */
	public Serializable getAcquisition() {
		return acquisition;
	}


	@Override
	public String toString() {
		return "Scanning [configuration=" + acquisition + "]";
	}
}
