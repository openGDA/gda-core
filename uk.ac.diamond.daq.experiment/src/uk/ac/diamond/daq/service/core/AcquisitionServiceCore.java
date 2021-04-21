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

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import uk.ac.diamond.daq.mapping.api.document.base.AcquisitionBase;
import uk.ac.diamond.daq.mapping.api.document.exception.ScanningAcquisitionServiceException;
import uk.ac.diamond.daq.mapping.api.document.scanning.ScanningAcquisition;
import uk.ac.diamond.daq.service.ScanningAcquisitionService;
import uk.ac.diamond.daq.service.rest.ScanningAcquisitionRestService;
import uk.ac.gda.api.acquisition.request.MscanRequest;
import uk.ac.gda.api.acquisition.response.ExceptionResponse;
import uk.ac.gda.api.acquisition.response.RunAcquisitionResponse;
import uk.ac.gda.core.tool.spring.SpringApplicationContextFacade;

/**
 * Implements the {@link ScanningAcquisitionRestService} logic
 * 
 * @author Maurizio Nagni
 *
 */
public class AcquisitionServiceCore {

	private static final Logger logger = LoggerFactory.getLogger(AcquisitionServiceCore.class);

	/**
	 * Submit a {@link ScanningAcquisition} to queue an acquisition
	 * @param acquisition
	 * @param service
	 * @return
	 * @throws ScanningAcquisitionServiceException
	 */
	protected ResponseEntity<RunAcquisitionResponse> runAcquisition(AcquisitionBase<?> acquisition) throws ScanningAcquisitionServiceException {
		SpringApplicationContextFacade.getBean(ScanningAcquisitionService.class).run(acquisition);
		RunAcquisitionResponse response = buildResponse(true, "Acquisition submitted");
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	protected ResponseEntity<RunAcquisitionResponse> runMScan(MscanRequest request) throws ScanningAcquisitionServiceException {
		new MScanServiceCoreHelper().runMScan(request);
		RunAcquisitionResponse response = buildResponse(true, "mscan submitted");
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	protected RunAcquisitionResponse buildResponse(boolean submitted, String message) {
		return responseBuilder(submitted, message)
				.build();
	}

	protected RunAcquisitionResponse buildResponse(boolean submitted, String message, Exception exception) {
		ExceptionResponse.Builder exceptionResponseBuilder = new ExceptionResponse.Builder()
				.withMessage(exception.getMessage());
		Optional.ofNullable(exception.getCause())
			.ifPresent(c -> exceptionResponseBuilder.withCauseMessage(c.getMessage()));
		return responseBuilder(submitted, message)
				.withException(exceptionResponseBuilder.build())
				.build();
	}

	private RunAcquisitionResponse.Builder responseBuilder(boolean submitted, String message) {
		return new RunAcquisitionResponse.Builder()
				.withSubmitted(submitted)
				.withMessage(message);
	}
}
