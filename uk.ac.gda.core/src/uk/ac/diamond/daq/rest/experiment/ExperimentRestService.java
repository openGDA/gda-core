package uk.ac.diamond.daq.rest.experiment;

import java.net.URL;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import uk.ac.diamond.daq.experiment.api.structure.ExperimentController;
import uk.ac.diamond.daq.experiment.api.structure.ExperimentControllerException;
import uk.ac.gda.core.tool.spring.SpringApplicationContextFacade;

/**
 * Exposes as REST service the server {@link ExperimentController}.
 *
 * <p>
 * This services allows any client to communicate with the {@link ExperimentController} using a simple HTTP request.
 * </p>
 *
 * @author Maurizio Nagni
 */
@RestController
@RequestMapping("/experiment")
public class ExperimentRestService {

	@RequestMapping("/start/{experimentName}")
	public @ResponseBody URL startExperiment(@PathVariable String experimentName) throws ExperimentControllerException {
		return getExperimentController().startExperiment(experimentName);
	}

	@RequestMapping("/name")
	public @ResponseBody String getExperimentName() {
		return getExperimentController().getExperimentName();
	}

	@RequestMapping("/stop")
	public @ResponseBody void stopExperiment() throws ExperimentControllerException {
		getExperimentController().stopExperiment();
	}

	@RequestMapping("/inProgress")
	public @ResponseBody boolean isExperimentInProgress() {
		return getExperimentController().isExperimentInProgress();
	}

	@RequestMapping("/prepareAcquisition/{acquisitionName}")
	public @ResponseBody URL prepareAcquisition(@PathVariable String acquisitionName) throws ExperimentControllerException {
		return getExperimentController().prepareAcquisition(acquisitionName);
	}

	@RequestMapping("/startMultipartAcquisition/{acquisitionName}")
	public @ResponseBody URL startMultipartAcquisition(String acquisitionName) throws ExperimentControllerException {
		return getExperimentController().startMultipartAcquisition(acquisitionName);
	}

	@RequestMapping("/stopMultipartAcquisition")
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
		return SpringApplicationContextFacade.getBean(ExperimentController.class);
	}
}
