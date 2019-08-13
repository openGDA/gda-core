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

package uk.ac.gda.tomography.service;

import java.io.File;
import java.net.URL;

import uk.ac.gda.tomography.service.message.TomographyRunMessage;

/**
 * Defines service operation for Tomography process.
 *
 * @author Maurizio Nagni
 *
 */
public interface TomographyService {

	void resetInstruments(Arrangement arrangement) throws TomographyServiceException;

	/**
	 * Executes an acquisition driven by a script using a message as configuration.
	 * Depending on the outcome runs an error script or a success script
	 *
	 * @param message
	 * @param script
	 * @param onError
	 * @param onSuccess
	 * @throws TomographyServiceException
	 */
	void runAcquisition(TomographyRunMessage message, File script, File onError, File onSuccess)
			throws TomographyServiceException;

	URL takeDarkImage(TomographyRunMessage message, File script)
			throws TomographyServiceException;

	URL takeFlatImage(TomographyRunMessage message, File script)
			throws TomographyServiceException;
}
