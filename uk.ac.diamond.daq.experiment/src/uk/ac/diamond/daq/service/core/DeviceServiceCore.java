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
package uk.ac.diamond.daq.service.core;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import uk.ac.diamond.daq.service.CommonDeviceService;
import uk.ac.diamond.daq.service.ServiceUtils;
import uk.ac.diamond.daq.service.command.receiver.device.BeanDeviceCommandReceiver;
import uk.ac.diamond.daq.service.command.receiver.device.DeviceCommandReceiver;
import uk.ac.diamond.daq.service.command.receiver.device.DeviceRequest;
import uk.ac.diamond.daq.service.command.strategy.OutputStrategyFactory;
import uk.ac.diamond.daq.service.rest.exception.GDAHttpException;
import uk.ac.gda.common.entity.device.DeviceValue;
import uk.ac.gda.common.exception.GDAServiceException;
import uk.ac.gda.core.tool.spring.SpringApplicationContextFacade;

/**
 * Implements the {@link CommonDeviceService} based on {@link DeviceRequest}
 * 
 * @author Maurizio Nagni
 *
 */
public class DeviceServiceCore extends CommonDeviceService {

	protected void getDeviceValue(DeviceRequest deviceRequest, HttpServletRequest request, HttpServletResponse response) throws GDAHttpException {
		DeviceCommandReceiver<DeviceValue> ccr = new BeanDeviceCommandReceiver<>(response, request);
		try {
			super.getDeviceValue(deviceRequest, ccr, OutputStrategyFactory.getJSONOutputStrategy());
		} catch (GDAServiceException e) {
			throw new GDAHttpException(e.getMessage(), HttpServletResponse.SC_PRECONDITION_FAILED);
		}
	}
	
	protected void setDeviceValue(DeviceRequest deviceRequest, HttpServletRequest request, HttpServletResponse response) throws GDAHttpException {
		DeviceCommandReceiver<DeviceValue> ccr = new BeanDeviceCommandReceiver<>(response, request);
		try {
			super.setDeviceValue(deviceRequest, ccr, OutputStrategyFactory.getJSONOutputStrategy());
		} catch (GDAServiceException e) {
			throw new GDAHttpException(e.getMessage(), HttpServletResponse.SC_PRECONDITION_FAILED);
		}
	}

	protected void getServices(List<String> services, HttpServletRequest request, HttpServletResponse response) throws GDAHttpException {
		try {
			byte[] output = OutputStrategyFactory.getJSONOutputStrategy().write(services);
			SpringApplicationContextFacade.getBean(ServiceUtils.class).writeOutput(output, response);
		} catch (GDAServiceException e) {
			throw new GDAHttpException(e.getMessage(), HttpServletResponse.SC_PRECONDITION_FAILED);
		}
	}
	
	protected void getServiceProperties(Class<?> service, HttpServletRequest request, HttpServletResponse response) throws GDAHttpException {
		Map<String, Class<?>[]> map = new HashMap<>();
		try {
			Arrays.stream(service.getMethods())
				.filter(s -> s.getName().startsWith("set") || s.getName().startsWith("get"))
				.forEach(m -> {					
					map.put(m.getName(), m.getParameterTypes());
				});
			byte[] output = OutputStrategyFactory.getJSONOutputStrategy().write(map);
			SpringApplicationContextFacade.getBean(ServiceUtils.class).writeOutput(output, response);
		} catch (GDAServiceException e) {
			throw new GDAHttpException(e.getMessage(), HttpServletResponse.SC_PRECONDITION_FAILED);
		}
	}
	
}
