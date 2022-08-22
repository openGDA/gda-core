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

import static uk.ac.gda.core.tool.spring.SpringApplicationContextFacade.getBean;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.server.ResponseStatusException;

import gda.device.EnumPositioner;
import gda.device.detector.areadetector.v17.ADBase;
import gda.device.detector.areadetector.v17.NDArray;
import gda.device.detector.areadetector.v17.NDFile;
import gda.device.detector.areadetector.v17.NDFileHDF5;
import gda.device.detector.areadetector.v17.NDROI;
import gda.device.detector.areadetector.v17.NDStats;
import gda.device.motor.MotorBase;
import uk.ac.diamond.daq.service.CommonDeviceService;
import uk.ac.diamond.daq.service.ServiceUtils;
import uk.ac.diamond.daq.service.command.receiver.device.BeanDeviceCommandReceiver;
import uk.ac.diamond.daq.service.command.receiver.device.DeviceCommandReceiver;
import uk.ac.diamond.daq.service.command.receiver.device.DeviceRequest;
import uk.ac.diamond.daq.service.command.strategy.OutputStrategyFactory;
import uk.ac.gda.common.entity.device.DeviceMethods;
import uk.ac.gda.common.entity.device.DeviceValue;
import uk.ac.gda.common.exception.GDAServiceException;

/**
 * Implements the {@link CommonDeviceService} based on {@link DeviceRequest}
 *
 * @author Maurizio Nagni
 *
 */
@Controller
public class DeviceServiceCore {
	
	private static final Logger logger = LoggerFactory.getLogger(DeviceServiceCore.class);

	@Autowired
	private CommonDeviceService deviceService;
	
	private final Map<String, Class<?>> interfaces = new HashMap<>();
	
	public void getDeviceValue(DeviceRequest deviceRequest, HttpServletRequest request, HttpServletResponse response) {
		DeviceCommandReceiver<DeviceValue> ccr = new BeanDeviceCommandReceiver<>(response, request);
		try {
			deviceService.getDeviceValue(deviceRequest, ccr, OutputStrategyFactory.getJSONOutputStrategy());
		} catch (GDAServiceException e) {
			logAndWrapException("Error getting device value", e);
		}
	}

	public void setDeviceValue(DeviceRequest deviceRequest, HttpServletRequest request, HttpServletResponse response) {
		DeviceCommandReceiver<DeviceValue> ccr = new BeanDeviceCommandReceiver<>(response, request);
		try {
			deviceService.setDeviceValue(deviceRequest, ccr, OutputStrategyFactory.getJSONOutputStrategy());
		} catch (GDAServiceException e) {
			logAndWrapException("Error setting device value", e);
		}
	}

	public void getServices(HttpServletResponse response) {
		List<String> services = new ArrayList<>(getInterfaces().keySet());
		try {
			byte[] output = OutputStrategyFactory.getJSONOutputStrategy().write(services);
			getServiceUtils().writeOutput(output, response.getOutputStream());
		} catch (GDAServiceException | IOException e) {
			logAndWrapException("Error getting services", e);
		}
	}

	public void getServiceProperties(String serviceName, HttpServletResponse response) {
		Class<?> service = getInterfaces().getOrDefault(serviceName, null);
		var builder = new DeviceMethods.Builder();
		Map<String, List<String>> map = new HashMap<>();
		try {
			Arrays.stream(service.getMethods())
				.filter(s -> s.getName().startsWith("set") || s.getName().startsWith("get"))
				.forEach(m ->
					map.put(m.getName(),
							Arrays.stream(m.getParameterTypes())
							.map(Object::toString)
							.collect(Collectors.toList()))
				);
			builder.withMethods(map);
			builder.withName(serviceName);
			builder.withUuid(getServiceUtils().creteTimebasedUUID());
			byte[] output = OutputStrategyFactory.getJSONOutputStrategy().write(builder.build());
			getServiceUtils().writeOutput(output, response.getOutputStream());
		} catch (GDAServiceException | IOException e) {
			logAndWrapException("Error getting service properties", e);
		}
	}

	private DeviceValue createDeviceValue(String detectorName, String serviceName, String propertyName) {
		var builder = new DeviceValue.Builder();
		builder.withServiceName(serviceName);
		builder.withProperty(propertyName);
		builder.withName(detectorName);
		builder.withUuid(getServiceUtils().creteTimebasedUUID());
		return builder.build();
	}

	private ServiceUtils getServiceUtils() {
		return getBean(ServiceUtils.class);
	}

	public DeviceRequest createDeviceRequest(String detectorName, String serviceName, String propertyName) {
		var deviceValue = createDeviceValue(detectorName, serviceName, propertyName);
		return createDeviceRequest(deviceValue);
	}

	public DeviceRequest createDeviceRequest(DeviceValue deviceValue) {
		return new DeviceRequest(getService(deviceValue.getName(), deviceValue.getServiceName()), deviceValue);
	}

	private Object getService(String detectorName, String service) {
		Class<?> clazz = Optional.ofNullable(getInterfaces().get(service))
			.filter(c -> EnumPositioner.class.isAssignableFrom(c) || MotorBase.class.isAssignableFrom(c))
			.orElse(null);
		
		var beanName = clazz != null ? detectorName : String.format("%s_%s", detectorName, service); 
		// ask the facade to retrieve the bean from the parent application context
		return getBean(beanName, clazz, true);
	}

	private Map<String, Class<?>> getInterfaces() {
		if (interfaces.isEmpty()) {
			interfaces.put("adbase", ADBase.class);
			interfaces.put("array", NDArray.class);
			interfaces.put("file", NDFile.class);
			interfaces.put("fileHDF5", NDFileHDF5.class);
			interfaces.put("roi", NDROI.class);
			interfaces.put("stat", NDStats.class);
			interfaces.put("positioner", EnumPositioner.class);
			interfaces.put("motor", MotorBase.class);
		}
		return interfaces;
	}
	
	private void logAndWrapException(String message, Exception exception) {
		logger.error(message, exception);
		throw new ResponseStatusException(HttpStatus.PRECONDITION_FAILED, message, exception);
	}
}
