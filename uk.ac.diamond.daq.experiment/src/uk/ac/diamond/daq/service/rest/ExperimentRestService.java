package uk.ac.diamond.daq.service.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import uk.ac.diamond.daq.experiment.api.entity.ClosedExperimentsResponse;
import uk.ac.diamond.daq.experiment.api.entity.ExperimentErrorCode;
import uk.ac.diamond.daq.experiment.api.entity.ExperimentServiceResponse;
import uk.ac.diamond.daq.experiment.api.structure.ExperimentController;
import uk.ac.diamond.daq.experiment.api.structure.ExperimentControllerException;
import uk.ac.diamond.daq.experiment.api.structure.ExperimentNodeExistsException;
import uk.ac.gda.core.tool.GDAHttpException;

/**
 * Exposes as REST service the server {@link ExperimentController}.
 *
 * <p>
 * This services allows any client to communicate with the {@link ExperimentController} using a simple HTTP request.
 * </p>
 *
 * <p> 
 * For documentation about the API aee <a href="https://confluence.diamond.ac.uk/display/DIAD/Experiment+Service">Experiment service</a>
 * </p>
 * 
 * @author Maurizio Nagni
 */
@RestController
@RequestMapping("/experiment")
public class ExperimentRestService {

	@Autowired
	private ExperimentController experimentController;
	
	@RequestMapping(value = "/session/start/{experimentName}", method = RequestMethod.PUT)
	public @ResponseBody ExperimentServiceResponse startExperiment(@PathVariable String experimentName) {
		var  response = new ExperimentServiceResponse.Builder();
		response.withErrorCode(ExperimentErrorCode.NONE);
		try {
			response.withRootNode(getExperimentController().startExperiment(experimentName));
		} catch (ExperimentNodeExistsException e) {
			response.withErrorCode(ExperimentErrorCode.EXPERIMENT_EXISTS);
		} catch (ExperimentControllerException e) {
			response.withErrorCode(ExperimentErrorCode.CANNOT_CREATE_EXPERIMENT);
		}
		return response.build();
	}

	@RequestMapping(value = "/session/name", method = RequestMethod.GET )
	public @ResponseBody String getExperimentName() {
		return getExperimentController().getExperimentName();
	}

	@RequestMapping(value = "/session/stop", method = RequestMethod.POST)
	public @ResponseBody void stopExperiment() throws ExperimentControllerException {
		getExperimentController().stopExperiment();
	}

	@RequestMapping(value = "/session/inProgress", method = RequestMethod.GET)
	public @ResponseBody boolean isExperimentInProgress() {
		return getExperimentController().isExperimentInProgress();
	}

	@RequestMapping(value = "/session/prepareAcquisition/{acquisitionName}", method = RequestMethod.PUT)
	public @ResponseBody ExperimentServiceResponse prepareAcquisition(@PathVariable String acquisitionName) {
		var  response = new ExperimentServiceResponse.Builder();
		response.withErrorCode(ExperimentErrorCode.NONE);
		try {
			response.withRootNode(getExperimentController().prepareAcquisition(acquisitionName));
		} catch (ExperimentNodeExistsException e) {
			response.withErrorCode(ExperimentErrorCode.ACQUISITION_EXISTS);
		} catch (ExperimentControllerException e) {
			response.withErrorCode(ExperimentErrorCode.CANNOT_CREATE_ACQUISITION);
		}
		return response.build();
	}

	@RequestMapping(value = "/session/startMultipartAcquisition/{acquisitionName}", method = RequestMethod.PUT)
	public @ResponseBody ExperimentServiceResponse startMultipartAcquisition(String acquisitionName) {
		var response = new ExperimentServiceResponse.Builder();
		response.withErrorCode(ExperimentErrorCode.NONE);
		try {
			response.withRootNode(getExperimentController().startMultipartAcquisition(acquisitionName));
		} catch (ExperimentNodeExistsException e) {
			response.withErrorCode(ExperimentErrorCode.ACQUISITION_EXISTS);
		} catch (ExperimentControllerException e) {
			response.withErrorCode(ExperimentErrorCode.CANNOT_CREATE_ACQUISITION);
		}
		return response.build();
	}

	@RequestMapping(value = "/session/stopMultipartAcquisition", method = RequestMethod.POST)
	public @ResponseBody void stopMultipartAcquisition() throws ExperimentControllerException {
		getExperimentController().stopMultipartAcquisition();
	}

	@RequestMapping(value = "/sessions", method = RequestMethod.GET)
	public @ResponseBody ClosedExperimentsResponse closedExperiments() throws GDAHttpException {
		var response = new ClosedExperimentsResponse.Builder();
		try {
			response.withIndexes(getExperimentController().closedExperiments());
		} catch (ExperimentControllerException e) {
			throw new GDAHttpException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return response.build();
	}
	
	/**
	 * Handles the HTTP response for the {@link ExperimentControllerException} thrown by this rest service
	 * @param e the thrown exception
	 * @return the exception message
	 * @deprecated Use instead {@link ExperimentRestService#exceptionHandler(GDAHttpException)}
	 */
	@Deprecated
	@ExceptionHandler({ ExperimentControllerException.class })
    public @ResponseBody String handleException(ExperimentControllerException e) {
		return e.getMessage();
    }

	@ExceptionHandler(GDAHttpException.class)
	public ResponseEntity<GDAHttpException> exceptionHandler(GDAHttpException exc) {
		return new ResponseEntity<>(exc, HttpStatus.valueOf(exc.getStatus()));
	}
	
	/**
	 * Retrieves the server {@link ExperimentController} instance.
	 * @return
	 */
	private ExperimentController getExperimentController() {
		// It is important to notice that while this rest endpoint ("/experiment") lives in a
		// in its own AnnotationConfigWebApplicationContext, the ExperimentController is retrieved
		// from the main GDA Spring Application context through SpringApplicationContextFacade
		return experimentController;
	}
}
