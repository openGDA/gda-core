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

import java.lang.reflect.Field;
import java.text.MessageFormat;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.beanutils.BeanMap;
import org.eclipse.scanning.api.IValidator;
import org.eclipse.scanning.api.IValidatorService;
import org.eclipse.scanning.api.ModelValidationException;
import org.eclipse.scanning.api.ValidationException;
import org.eclipse.scanning.api.annotation.ui.DeviceType;
import org.eclipse.scanning.api.annotation.ui.FieldDescriptor;
import org.eclipse.scanning.api.device.IRunnableDevice;
import org.eclipse.scanning.api.device.IRunnableDeviceService;
import org.eclipse.scanning.api.device.models.DeviceRole;
import org.eclipse.scanning.api.device.models.IDetectorModel;
import org.eclipse.scanning.api.device.models.IMalcolmModel;
import org.eclipse.scanning.api.device.models.ScanMode;
import org.eclipse.scanning.api.event.scan.DeviceInformation;
import org.eclipse.scanning.api.event.scan.ScanRequest;
import org.eclipse.scanning.api.points.models.CompoundModel;
import org.eclipse.scanning.api.scan.ScanningException;

class ScanRequestValidator implements IValidator<ScanRequest> {


	private IValidatorService vservice;

	@Override
	public void setService(IValidatorService vservice) {
		this.vservice = vservice;
	}

	@Override
	public void validate(ScanRequest req) throws ValidationException {

	    final CompoundModel cm = req.getCompoundModel();
		if (cm!=null && cm.getModels()!=null && !cm.getModels().isEmpty()) {
			vservice.validate(cm);
		} else {
			throw new ModelValidationException("There is no compound model available", req, "compoundModel");
		}
		try {
			final Map<String, IDetectorModel> dmodels = req.getDetectors();
			if (dmodels!=null && !dmodels.isEmpty()) { // No detectors is allowed.
				validateMalcolmRules(dmodels);
				validateDetectors(dmodels);
				validateAnnotations(dmodels);
			}

		} catch (ScanningException ne) {
            throw new ValidationException(ne);
		}
	}

	private void validateAnnotations(Map<String, IDetectorModel> dmodels) throws ValidationException, ScanningException {
		for (Object model : dmodels.values()) {
			// If the model has an annotated field which points at
			// a detector, that detector must be in the scan.
			final Field[] fields = model.getClass().getDeclaredFields();

			BeanMap beanMap = null; // May need to use newer version of BeanMap in Java9 if it uses setAccessable(true)
			for (Field field : fields) {
				final FieldDescriptor des = field.getAnnotation(FieldDescriptor.class);
				if (des != null && des.device()==DeviceType.RUNNABLE) { // Then its value must be in the devices.
					if (beanMap == null) beanMap = new BeanMap(model);
					final String reference = beanMap.get(field.getName()).toString();
					if (!dmodels.containsKey(reference)) {
						IRunnableDeviceService dservice = ValidatorService.getRunnableDeviceService();
						if (dservice == null || dservice.getRunnableDevice(reference) == null) {
							String label = des.label()!=null && des.label().length()>0 ? des.label() : field.getName();
							throw new ModelValidationException("The value of '"+label+"' references a device ("+reference+") not a valid device!", model, field.getName());
						}
					}
				}
			}
		}
	}

	private void validateMalcolmRules(Map<String, IDetectorModel> dmodels) throws ValidationException, ScanningException {
		// we can't validate without a validation service
		IRunnableDeviceService runnableDeviceService = ValidatorService.getRunnableDeviceService();
		if (runnableDeviceService == null) return;

		// if the scan includes a malcolm device, then it is a hardware scan
		final ScanMode scanMode = dmodels.values().stream().anyMatch(IMalcolmModel.class::isInstance) ?
				ScanMode.HARDWARE : ScanMode.SOFTWARE;

		// check each detector supports the scan mode and that there is at most one malcolm device
		int numMalcolmDevices = 0;
		for (Entry<String, IDetectorModel> entry : dmodels.entrySet()) {
			DeviceRole role = checkDetectorAndGetRole(runnableDeviceService, scanMode, entry.getKey(), entry.getValue());
			if (role == DeviceRole.MALCOLM) {
				numMalcolmDevices++;
			}
		}

		if (numMalcolmDevices > 1) {
			throw new ValidationException("Only one malcolm device may be used per scan.");
		}
	}

	private DeviceRole checkDetectorAndGetRole(IRunnableDeviceService runnableDeviceService, final ScanMode scanMode,
			String name, Object model) throws ScanningException {
		final DeviceInformation<?> info = runnableDeviceService.getDeviceInformation(name);
		if (info == null) {
			final IRunnableDevice<?> device = runnableDeviceService.createRunnableDevice(model);
			if (device.getRole() != DeviceRole.PROCESSING) {
				// Only processing may be created on the fly, the others must have names.
				throw new ValidationException("Detector '"+name+"' cannot be found!");
			}
			return DeviceRole.PROCESSING;
		} else {
			// devices that can run either as a standard hardware detector or as a hardware
			// triggered detector will be switched to the appropriate role according to the scan type
			if (!info.getSupportedScanModes().contains(scanMode)) {
				throw new ValidationException(MessageFormat.format("The device ''{0}'' does not support a {1} scan",
						info.getName(), scanMode.toString().toLowerCase()));
			}
			return info.getDeviceRole();
		}
	}

	private void validateDetectors(Map<String, IDetectorModel> dmodels) throws ValidationException {
		// All the models must validate too
		for (Object model : dmodels.values()) {
			IValidator<Object> validator = vservice.getValidator(model);
			if (validator!=null) validator.validate(model); // We just ignore those without validators.
		}
	}

}
