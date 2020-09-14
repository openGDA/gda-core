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

package uk.ac.diamond.daq.mapping.ui.services.position;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.Scannable;
import uk.ac.gda.api.acquisition.parameters.DevicePositionDocument;
import uk.ac.gda.api.exception.GDAException;

/**
 * Base implementation of the logic supporting the <i>Chain of Responsibility</i> for the {@link DevicePositionDocumentService}.
 *
 * @author Maurizio Nagni
 */
abstract class DeviceHandler {

	static final Logger logger = LoggerFactory.getLogger(DeviceHandler.class);

	/**
	 * The next handler to call if this one cannot handle the device
	 */
	private DeviceHandler nextHandler;

	/**
	 * Sets a reference to the next handler.
	 * @param nextHandler
	 */
	void setNextHandler(DeviceHandler nextHandler) {
		this.nextHandler = nextHandler;
	}

	/**
	 * Creates a {@link DevicePositionDocument} from the passed {@code device} object
	 * @param device The device to analyse
	 * @return A position document otherwise {@code null} if cannot be handled
	 */
	public final DevicePositionDocument handleDevice(Scannable device) {
		try {
			return doHandleDevice(device);
		} catch (GDAException e) {
			logger.error("Cannot handle device {} ", device, e);
			return null;
		}
	}

	/**
	 * Creates a {@link DevicePositionDocument} from the passed {@code device} object
	 * @param device The device to analyse
	 * @return A position document otherwise {@code null} if cannot handle
	 * @throws GDAException If the device has the correct type but an error occurred during the analysis
	 */
	abstract DevicePositionDocument devicePositionAsDocument(Scannable device) throws GDAException;

	private DevicePositionDocument doHandleDevice(Scannable device) throws GDAException {
		DevicePositionDocument document = devicePositionAsDocument(device);
		if (document != null)
			return document;
		if (nextHandler != null) {
			return nextHandler.handleDevice(device);
		}
		logger.error("No suitable handler found for device {} ", device);
		return null;
	}
}
