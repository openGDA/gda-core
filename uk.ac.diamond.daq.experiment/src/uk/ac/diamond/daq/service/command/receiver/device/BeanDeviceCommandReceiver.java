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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.daq.service.ServiceUtils;
import uk.ac.diamond.daq.service.command.strategy.OutputStrategy;
import uk.ac.gda.common.entity.Document;
import uk.ac.gda.common.entity.device.DeviceValue;
import uk.ac.gda.common.exception.GDAServiceException;
import uk.ac.gda.core.tool.spring.SpringApplicationContextFacade;

/**
 * An implementation of {@link DeviceCommandReceiver} which get or set properties from Spring bean defined by the GDA server
 *
 * @author Maurizio Nagni
 *
 * @param <T>
 */
public class BeanDeviceCommandReceiver<T extends Document> implements DeviceCommandReceiver<T> {

	private static final Logger logger = LoggerFactory.getLogger(BeanDeviceCommandReceiver.class);

	private final HttpServletResponse response;
	private final HttpServletRequest request;

	// To use it call getServiceUtils()
	private ServiceUtils serviceUtils;

	public BeanDeviceCommandReceiver(HttpServletResponse response, HttpServletRequest request) {
		this.response = response;
		this.request = request;
	}

	@Override
	public void getValue(DeviceRequest deviceRequest, OutputStrategy<T> outputStrategy)
			throws GDAServiceException {
		var document = getPropertyValue(deviceRequest);
		getServiceUtils().writeOutput(document, outputStrategy, response);
	}

	@Override
	public void setValue(DeviceRequest deviceRequest, OutputStrategy<T> outputStrategy)
			throws GDAServiceException {
		setPropertyValue(deviceRequest);
		var document = (T) deviceRequest.getDeviceValue();
		getServiceUtils().writeOutput(document, outputStrategy, response);
	}

	private Method getMethod(DeviceRequest deviceRequest) throws NoSuchMethodException, SecurityException {
		return deviceRequest.getDevice().getClass().getMethod(deviceRequest.getDeviceValue().getProperty());
	}

	private void setPropertyValue(DeviceRequest deviceRequest) {
		Optional<Method> method = Arrays.stream(deviceRequest.getDevice().getClass().getMethods())
				.filter(m -> m.getName().equals(deviceRequest.getDeviceValue().getProperty()))
				.findFirst();
		if (deviceRequest.getDeviceValue().getValue() == null) {
			method.ifPresent(m -> invokeSetter(m, deviceRequest.getDevice()));
			return;			
		}
		method.ifPresent(m -> invokeSetter(m, deviceRequest.getDevice(), deviceRequest.getDeviceValue().getValue()));
	}

	private void invokeSetter(Method method, Object instance, Object... args) {
		try {
			method.invoke(instance, args);
			logger.debug("Invoked: {} {} {}", method.getClass().getSimpleName(), instance, args);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			logger.error("Invoked: {} {} {}", method.getClass().getSimpleName(), instance, args);
		}
	}

	private T createDeviceValue(DeviceValue deviceValue, Object result) {
		var builder = new DeviceValue.Builder();
		builder.withValue(result);
		builder.withServiceName(deviceValue.getServiceName());
		builder.withProperty(deviceValue.getProperty());
		builder.withName(deviceValue.getName());
		builder.withUuid(UUID.randomUUID());
		return (T) builder.build();
	}

	private T getPropertyValue(DeviceRequest deviceRequest) {
		try {
			Object result = getMethod(deviceRequest).invoke(deviceRequest.getDevice());
			return createDeviceValue(deviceRequest.getDeviceValue(), result);
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			e.printStackTrace();
		}
		return null;
	}

	private ServiceUtils getServiceUtils() {		
		if (serviceUtils == null) {
			serviceUtils = SpringApplicationContextFacade.getBean(ServiceUtils.class);
		}
		return serviceUtils;
	}
}
