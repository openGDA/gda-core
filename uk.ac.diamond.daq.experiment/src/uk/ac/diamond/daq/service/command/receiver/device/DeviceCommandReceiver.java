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

package uk.ac.diamond.daq.service.command.receiver.device;

import uk.ac.diamond.daq.service.command.strategy.OutputStrategy;
import uk.ac.gda.common.entity.Document;
import uk.ac.gda.common.exception.GDAServiceException;

/**
 * Defines getter and setter commands to apply on {@link DeviceRequest} documents
 *
 * @author Maurizio Nagni
 *
 * @param <T>
 */
public interface DeviceCommandReceiver<T extends Document> {

	void getValue(DeviceRequest deviceRequest, OutputStrategy<T> outputStrategy) throws GDAServiceException;

	void setValue(DeviceRequest deviceRequest, OutputStrategy<T> outputStrategy) throws GDAServiceException;
}
