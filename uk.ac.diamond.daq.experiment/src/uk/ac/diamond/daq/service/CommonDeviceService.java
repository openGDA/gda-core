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

package uk.ac.diamond.daq.service;

import org.springframework.stereotype.Controller;

import uk.ac.diamond.daq.service.command.device.GetDeviceValueCommand;
import uk.ac.diamond.daq.service.command.device.SetDeviceValueCommand;
import uk.ac.diamond.daq.service.command.receiver.device.DeviceCommandReceiver;
import uk.ac.diamond.daq.service.command.receiver.device.DeviceRequest;
import uk.ac.diamond.daq.service.command.strategy.OutputStrategy;
import uk.ac.gda.common.command.ExecuteCommand;
import uk.ac.gda.common.entity.device.DeviceValue;
import uk.ac.gda.common.exception.GDAServiceException;

/** 
 * Basic Device services
 * 
 * @author Maurizio Nagni
 *
 */
@Controller
public class CommonDeviceService {

	public <T extends DeviceValue> void getDeviceValue(DeviceRequest deviceRequest,  DeviceCommandReceiver<T> ccr,
			OutputStrategy<T> outputStrategy) throws GDAServiceException {
		ExecuteCommand cc = new GetDeviceValueCommand<T>(deviceRequest, ccr, outputStrategy);
		cc.execute();
	}

	public <T extends DeviceValue> void setDeviceValue(DeviceRequest deviceRequest, DeviceCommandReceiver<T> ccr,
			OutputStrategy<T> outputStrategy) throws GDAServiceException {
		ExecuteCommand cc = new SetDeviceValueCommand<>(deviceRequest, ccr, outputStrategy);
		cc.execute();
	}	
}
