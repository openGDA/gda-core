package uk.ac.diamond.daq.service.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import uk.ac.diamond.daq.experiment.api.structure.ExperimentControllerException;
import uk.ac.diamond.daq.mapping.api.document.base.AcquisitionBase;
import uk.ac.diamond.daq.mapping.api.document.base.AcquisitionConfigurationBase;
import uk.ac.diamond.daq.mapping.api.document.base.AcquisitionParametersBase;
import uk.ac.diamond.daq.mapping.api.document.exception.ScanningAcquisitionServiceException;
import uk.ac.diamond.daq.service.ScanningAcquisitionService;
import uk.ac.gda.api.acquisition.response.RunAcquisitionResponse;

/**
 * Provides access to a {@link ScanningAcquisitionService}
 * 
 * <p> 
 * For documentation about the API aee <a href="https://confluence.diamond.ac.uk/display/DIAD/Scanning+Acquisition+Service">Acquisition service</a>
 * </p> 
 * 
 * @author Maurizio Nagni
 */
@RestController
@RequestMapping("/acquisition")
public class ScanningAcquisitionRestService {

	@Autowired
	private ScanningAcquisitionService service;

	/**
	 * Receives request for acquisitions.
	 * @param acquisition
	 * @return
	 * @throws ScanningAcquisitionServiceException
	 */
	@RequestMapping(value = "/run", method = RequestMethod.POST)
	public @ResponseBody ResponseEntity<RunAcquisitionResponse> run(
			@RequestBody AcquisitionBase<? extends AcquisitionConfigurationBase<? extends AcquisitionParametersBase>> acquisition)
			throws ScanningAcquisitionServiceException {
		service.run(acquisition);
		RunAcquisitionResponse response = buildResponse(true, "Acquisition submitted");
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	/**
	 * Handles the HTTP response for the {@link ExperimentControllerException}
	 * thrown by this rest service
	 * 
	 * @param e the thrown exception
	 * @return the exception message
	 */
	@ExceptionHandler({ ExperimentControllerException.class })
	public @ResponseBody ResponseEntity<RunAcquisitionResponse> handleException(ExperimentControllerException e) {
		RunAcquisitionResponse response = buildResponse(false, e.getMessage());
		return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	private RunAcquisitionResponse buildResponse(boolean submitted, String message) {
		return new RunAcquisitionResponse.Builder()
				.withSubmitted(submitted)
				.withMessage(message)
				.build();
	}
}