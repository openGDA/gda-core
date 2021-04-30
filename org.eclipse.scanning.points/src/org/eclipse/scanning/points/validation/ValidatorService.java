/*-
 *******************************************************************************
 * Copyright (c) 2011, 2016 Diamond Light Source Ltd.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthew Gerring - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.scanning.points.validation;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.eclipse.scanning.api.IValidator;
import org.eclipse.scanning.api.IValidatorService;
import org.eclipse.scanning.api.ValidationException;
import org.eclipse.scanning.api.device.IRunnableDevice;
import org.eclipse.scanning.api.device.IRunnableDeviceService;
import org.eclipse.scanning.api.device.models.ModelReflection;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.scan.ScanRequest;
import org.eclipse.scanning.api.points.IPointGeneratorService;
import org.eclipse.scanning.api.points.models.AxialArrayModel;
import org.eclipse.scanning.api.points.models.AxialCollatedStepModel;
import org.eclipse.scanning.api.points.models.AxialMultiStepModel;
import org.eclipse.scanning.api.points.models.AxialPointsModel;
import org.eclipse.scanning.api.points.models.AxialStepModel;
import org.eclipse.scanning.api.points.models.BoundingBox;
import org.eclipse.scanning.api.points.models.CompoundModel;
import org.eclipse.scanning.api.points.models.ConcurrentMultiModel;
import org.eclipse.scanning.api.points.models.ConsecutiveMultiModel;
import org.eclipse.scanning.api.points.models.JythonGeneratorModel;
import org.eclipse.scanning.api.points.models.StaticModel;
import org.eclipse.scanning.api.points.models.TwoAxisGridPointsModel;
import org.eclipse.scanning.api.points.models.TwoAxisGridStepModel;
import org.eclipse.scanning.api.points.models.TwoAxisLinePointsModel;
import org.eclipse.scanning.api.points.models.TwoAxisLineStepModel;
import org.eclipse.scanning.api.points.models.TwoAxisLissajousModel;
import org.eclipse.scanning.api.points.models.TwoAxisPointSingleModel;
import org.eclipse.scanning.api.points.models.TwoAxisPtychographyModel;
import org.eclipse.scanning.api.points.models.TwoAxisSpiralModel;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.api.ui.CommandConstants;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ValidatorService implements IValidatorService {

	private static Logger logger = LoggerFactory.getLogger(ValidatorService.class);

	static {
		System.out.println("Starting ValidatorService");
	}

	private static IPointGeneratorService factory;
	public void setPointGeneratorService(IPointGeneratorService pservice) {
		factory = pservice;
	}

	private static IRunnableDeviceService dservice;
	private static IEventService          eservice;

	public void setEventService(IEventService leservice) {
		eservice = leservice;
	}

	private static ComponentContext context;

	public static IPointGeneratorService getPointGeneratorService() {
		return factory;
	}
	public static IRunnableDeviceService getRunnableDeviceService() {
		// On the server we have a direct IRunnableDeviceService available.
		// On the client we must use a remote one.
		// Since remote one works on server, we always use it.
		if (dservice==null) {
			try {
				dservice = eservice.createRemoteService(new URI(CommandConstants.getScanningBrokerUri()), IRunnableDeviceService.class);
			} catch (EventException | URISyntaxException e) {
				if (context!=null) {
					ServiceReference<IRunnableDeviceService> ref = context.getBundleContext().getServiceReference(IRunnableDeviceService.class);
					dservice = context.getBundleContext().getService(ref);
				}
			}
		}
		return dservice;
	}

	public void setRunnableDeviceService(IRunnableDeviceService service) {
		dservice = service;
	}

	public void start(ComponentContext lcontext) {
		context = lcontext;
	}

	@SuppressWarnings("rawtypes")
	private static final Map<Class<?>, Class<? extends IValidator>> validators;
	static {
		@SuppressWarnings("rawtypes")
		final Map<Class<?>, Class<? extends IValidator>> tmp = new HashMap<>();
		tmp.put(AxialArrayModel.class, AxialArrayModelValidator.class);
		tmp.put(AxialCollatedStepModel.class, AxialCollatedStepModelValidator.class);
		tmp.put(AxialMultiStepModel.class, AxialMultiStepModelValidator.class);
		tmp.put(AxialPointsModel.class, AxialPointsModelValidator.class);
		tmp.put(AxialStepModel.class, AxialStepModelValidator.class);
		tmp.put(BoundingBox.class, BoundingBoxValidator.class);
		tmp.put(CompoundModel.class, CompoundModelValidator.class);
		tmp.put(ConcurrentMultiModel.class, ConcurrentMultiModelValidator.class);
		tmp.put(ConsecutiveMultiModel.class, ConsecutiveMultiModelValidator.class);
		tmp.put(JythonGeneratorModel.class, JythonGeneratorModelValidator.class);
		tmp.put(ScanRequest.class, ScanRequestValidator.class);
		tmp.put(StaticModel.class, StaticModelValidator.class);
		tmp.put(TwoAxisGridPointsModel.class, TwoAxisGridPointsModelValidator.class);
		tmp.put(TwoAxisGridStepModel.class, TwoAxisGridStepModelValidator.class);
		tmp.put(TwoAxisLinePointsModel.class, TwoAxisLinePointsModelValidator.class);
		tmp.put(TwoAxisLineStepModel.class, TwoAxisLineStepModelValidator.class);
		tmp.put(TwoAxisLissajousModel.class, TwoAxisLissajousModelValidator.class);
		tmp.put(TwoAxisPointSingleModel.class, TwoAxisPointSingleModelValidator.class);
		tmp.put(TwoAxisPtychographyModel.class, TwoAxisPtychographyModelValidator.class);
		tmp.put(TwoAxisSpiralModel.class, TwoAxisSpiralModelValidator.class);

		validators = Collections.unmodifiableMap(tmp);
	}

	@Override
	public <T> void validate(T model) throws ValidationException {

		if (model==null) throw new ValidationException("The object to validate is null and cannot be checked!");
		IValidator<T> validator = getValidator(model);
		if (validator==null) throw new ValidationException("There is no validator for class: " + model.getClass());
		validator.setService(this);
		validator.validate(model);
	}

	/**
	 * Get the validator corresponding to the model passed as parameter<br>
	 * As we have separated model validation from point generation, we no longer try to get validators from point
	 * generators.
	 */
	@Override
	public <T> IValidator<T> getValidator(T model) throws ValidationException {

		if (model == null)
			throw new NullPointerException("The model is null!");

		if (model instanceof IValidator)
			throw new IllegalArgumentException(
					"Models should be vanilla and not contain logic for validating themselves!");

		try {
			final IValidator<T> validator = getValidatorForClass(model.getClass());
			if (validator != null) {
				return validator;
			}
		} catch (Exception e) {
			throw new ValidationException("Could not create validator for model class: " + model.getClass(), e);
		}
		return getDeviceFromModel(model).orElseThrow(() -> new ValidationException(
				"Could not find validator for model " + model + " using runnable device service"));
	}

	@SuppressWarnings("unchecked")
	private <T> IValidator<T> getValidatorForClass(Class<?> modelClass) throws Exception {
		Class<?> clazz = modelClass;
		while (clazz != Object.class) {
			if (validators.containsKey(clazz)) {
				return validators.get(clazz).getDeclaredConstructor().newInstance();
			}
			clazz = clazz.getSuperclass();
		}
		return null;
	}

	private <T> Optional<IRunnableDevice<T>> getDeviceFromModel(T model) {
		final String name = ModelReflection.getName(model);

		if (dservice != null && name != null) {
			return getDeviceFromName(name);
		} else {
			return Optional.empty();
		}
	}

	private <T> Optional<IRunnableDevice<T>> getDeviceFromName(String name) {
		try {
			IRunnableDevice<T> d = dservice.getRunnableDevice(name);
			return Optional.ofNullable(d);
		} catch (ScanningException e) {
			logger.trace("No device found for " + name, e);
			return Optional.empty();
		}
	}
}
