package uk.ac.diamond.daq.rest.service;

import java.net.URL;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
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

	/**
	 * Starts a new named experiment
	 *
	 * @param experimentName A user-friendly identifier for the experiment
	 *
	 * @return the experiment file URL;
	 * 		   created at the end of the experiment ({@link #stopExperiment()}
	 *
	 * @throws ExperimentControllerException if methods fails to create the
	 *                                       experiment location
	 */
	@RequestMapping(value = "/start/{experimentName}", method = RequestMethod.PUT)
	public @ResponseBody URL startExperiment(@PathVariable String experimentName) throws ExperimentControllerException {
		return getExperimentController().startExperiment(experimentName);
	}

	/**
	 * Returns the experiment name, or {@code null} if no experiment is running
	 */
	@RequestMapping(value = "/name", method = RequestMethod.GET )
	public @ResponseBody String getExperimentName() {
		return getExperimentController().getExperimentName();
	}

	/**
	 * Closes the active experiment. Closes also any open multipart acquisition.
	 *
	 * @throws ExperimentControllerException if methods fails or
	 *                                       {@link #isExperimentInProgress()} returns
	 *                                       {@code false}
	 */
	@RequestMapping(value = "/stop", method = RequestMethod.POST)
	public @ResponseBody void stopExperiment() throws ExperimentControllerException {
		getExperimentController().stopExperiment();
	}

	/**
	 * Returns the state of the experiment
	 *
	 * @return {@code true} if the experiment is in progress, otherwise {@code false}
	 */
	@RequestMapping(value = "/inProgress", method = RequestMethod.GET)
	public @ResponseBody boolean isExperimentInProgress() {
		return getExperimentController().isExperimentInProgress();
	}

	/**
	 * Creates a new location within the experiment structure.
	 *
	 * @param acquisitionName A user-friendly identifier for the acquisition
	 *
	 * @return The URL for the acquisition file
	 *
	 * @throws ExperimentControllerException if methods fails to create the
	 *                                       acquisition location
	 */
	@RequestMapping(value = "/prepareAcquisition/{acquisitionName}", method = RequestMethod.PUT)
	public @ResponseBody URL prepareAcquisition(@PathVariable String acquisitionName) throws ExperimentControllerException {
		return getExperimentController().prepareAcquisition(acquisitionName);
	}

	/**
	 * Prepares the controller for an acquisition composed of multiple parts.
	 * Each part should then be given a URL by calling {@link #prepareAcquisition(String)},
	 * and the overall acquisition should be ended with {@link #stopMultipartAcquisition()}
	 *
	 * @param acquisitionName A user-friendly identifier for the acquisition
	 *
	 * @return the URL of the acquisition file,
	 * 		   created when the multipart acquisition is stopped
	 *
	 * @throws ExperimentControllerException
	 */
	@RequestMapping(value = "/startMultipartAcquisition/{acquisitionName}", method = RequestMethod.PUT)
	public @ResponseBody URL startMultipartAcquisition(String acquisitionName) throws ExperimentControllerException {
		return getExperimentController().startMultipartAcquisition(acquisitionName);
	}

	/**
	 * Closes the current multipart acquisition is complete.
	 *
	 * @throws ExperimentControllerException if no open multipart acquisition exists
	 */
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
		return SpringApplicationContextFacade.getBean(ExperimentController.class);
	}
}
