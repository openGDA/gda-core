package uk.ac.diamond.daq.service.rest;

import java.net.URL;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import uk.ac.diamond.daq.experiment.api.structure.ExperimentController;
import uk.ac.diamond.daq.experiment.api.structure.ExperimentControllerException;

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
	
	@RequestMapping(value = "/start/{experimentName}", method = RequestMethod.PUT)
	public @ResponseBody URL startExperiment(@PathVariable String experimentName) throws ExperimentControllerException {
		return getExperimentController().startExperiment(experimentName);
	}

	@RequestMapping(value = "/name", method = RequestMethod.GET )
	public @ResponseBody String getExperimentName() {
		return getExperimentController().getExperimentName();
	}

	@RequestMapping(value = "/stop", method = RequestMethod.POST)
	public @ResponseBody void stopExperiment() throws ExperimentControllerException {
		getExperimentController().stopExperiment();
	}

	@RequestMapping(value = "/inProgress", method = RequestMethod.GET)
	public @ResponseBody boolean isExperimentInProgress() {
		return getExperimentController().isExperimentInProgress();
	}

	@RequestMapping(value = "/prepareAcquisition/{acquisitionName}", method = RequestMethod.PUT)
	public @ResponseBody URL prepareAcquisition(@PathVariable String acquisitionName) throws ExperimentControllerException {
		return getExperimentController().prepareAcquisition(acquisitionName);
	}

	@RequestMapping(value = "/startMultipartAcquisition/{acquisitionName}", method = RequestMethod.PUT)
	public @ResponseBody URL startMultipartAcquisition(String acquisitionName) throws ExperimentControllerException {
		return getExperimentController().startMultipartAcquisition(acquisitionName);
	}

	@RequestMapping(value = "/stopMultipartAcquisition", method = RequestMethod.POST)
	public @ResponseBody void stopMultipartAcquisition() throws ExperimentControllerException {
		getExperimentController().stopMultipartAcquisition();
	}

	/**
	 * Handles the HTTP response for the {@link ExperimentControllerException} thrown by this rest service
	 * @param e the thrown exception
	 * @return the exeption message
	 */
	@ExceptionHandler({ ExperimentControllerException.class })
    public @ResponseBody String handleException(ExperimentControllerException e) {
		return e.getMessage();
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
