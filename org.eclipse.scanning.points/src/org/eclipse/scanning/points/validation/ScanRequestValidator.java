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

import java.text.MessageFormat;
import java.util.Map;

import org.eclipse.scanning.api.IValidator;
import org.eclipse.scanning.api.IValidatorService;
import org.eclipse.scanning.api.ModelValidationException;
import org.eclipse.scanning.api.ValidationException;
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
	public ScanRequest validate(ScanRequest req) throws ValidationException {

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
			}

		} catch (ScanningException ne) {
            throw new ValidationException(ne);
		}
		return req;
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
		for (String name: dmodels.keySet()) {
			DeviceRole role = checkDetectorAndGetRole(runnableDeviceService, scanMode, name);
			if (role == DeviceRole.MALCOLM) {
				numMalcolmDevices++;
			}
		}

		if (numMalcolmDevices > 1) {
			throw new ValidationException("Only one malcolm device may be used per scan.");
		}
	}

	private DeviceRole checkDetectorAndGetRole(
			IRunnableDeviceService runnableDeviceService,
			final ScanMode scanMode,
			String name) throws ScanningException {
		final DeviceInformation<?> info = runnableDeviceService.getDeviceInformation(name);
		if (info == null) {
			throw new ScanningException("No info found for device name " + name);
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
