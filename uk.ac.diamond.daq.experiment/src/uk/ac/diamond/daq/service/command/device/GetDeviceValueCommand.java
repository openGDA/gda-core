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

package uk.ac.diamond.daq.service.command.device;

import uk.ac.diamond.daq.service.command.receiver.device.DeviceCommandReceiver;
import uk.ac.diamond.daq.service.command.receiver.device.DeviceRequest;
import uk.ac.diamond.daq.service.command.strategy.OutputStrategy;
import uk.ac.gda.common.command.ExecuteCommand;
import uk.ac.gda.common.entity.device.DeviceValue;
import uk.ac.gda.common.exception.GDAServiceException;

/**
 * An {@link ExecuteCommand} to get value using a {@link DeviceValue} document
 * 
 * @author Maurizio Nagni
 *
 * @param <T>
 */
public class GetDeviceValueCommand<T extends DeviceValue> implements ExecuteCommand {

	private final DeviceCommandReceiver<T> receiver;
	private final OutputStrategy<T> outputStrategy;
	private final DeviceRequest deviceRequest;

	/**
	 * Constructor for the retreive command
	 * @param ccr defines how collect the device value
	 * @param id the document identifier 
	 * @param outputStrategy defines how write the output
	 */
	public GetDeviceValueCommand(DeviceRequest deviceRequest, DeviceCommandReceiver<T> receiver, OutputStrategy<T> outputStrategy) {
		super();
		this.deviceRequest = deviceRequest;
		this.receiver = receiver;
		this.outputStrategy = outputStrategy;
	}

	@Override
	public void execute() throws GDAServiceException {
		receiver.getValue(deviceRequest, outputStrategy);
	}
}
